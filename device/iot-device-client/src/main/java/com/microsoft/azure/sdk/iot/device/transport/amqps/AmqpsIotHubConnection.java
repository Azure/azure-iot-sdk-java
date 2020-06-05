/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.proton.transport.proxy.ProxyAuthenticationType;
import com.microsoft.azure.proton.transport.proxy.ProxyConfiguration;
import com.microsoft.azure.proton.transport.proxy.ProxyHandler;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyHandlerImpl;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyImpl;
import com.microsoft.azure.proton.transport.ws.impl.WebSocketImpl;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.reactor.Handshaker;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.ReactorOptions;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * An AMQPS IotHub connection between a device and an IoTHub or Edgehub. This class is responsible for reacting to connection level and
 * reactor level events. It is also responsible for creating sessions and handlers for those sessions. An instance of this
 * object may be reused after it has been closed.
 */
@Slf4j
public final class AmqpsIotHubConnection extends BaseHandler implements IotHubTransportConnection, AmqpsSessionStateCallback
{
    // Timeouts
    private static final int MAX_WAIT_TO_CLOSE_CONNECTION = 20 * 1000; // 20 second timeout
    private static final int MAX_WAIT_TO_OPEN_AUTHENTICATION_SESSION = 20 * 1000; // 20 second timeout
    private static final int MAX_WAIT_TO_OPEN_WORKER_SESSIONS = 60 * 1000; // 60 second timeout
    private static final int MAX_WAIT_TO_TERMINATE_EXECUTOR = 10;

    // Web socket constants
    private static final String WEB_SOCKET_PATH = "/$iothub/websocket";
    private static final String WEB_SOCKET_SUB_PROTOCOL = "AMQPWSB10";
    private static final String WEB_SOCKET_QUERY = "iothub-no-client-cert=true";
    private static final int MAX_MESSAGE_PAYLOAD_SIZE = 256 * 1024; //max IoT Hub message size is 256 kb, so amqp websocket layer should buffer at most that much space
    private static final int WEB_SOCKET_PORT = 443;

    private static final int AMQP_PORT = 5671;
    private static final int REACTOR_COUNT = 1;
    private static final int CBS_SESSION_COUNT = 1; //even for multiplex scenarios

    // Message send constants
    private static final int SEND_MESSAGES_PERIOD_MILLIS = 50; //every 50 milliseconds, the method onTimerTask will fire to send, at most, MAX_MESSAGES_TO_SEND_PER_CALLBACK queued messages
    private static final int MAX_MESSAGES_TO_SEND_PER_CALLBACK = 1000; //Max number of queued messages to send per periodic sending task

    // States of outgoing messages, incoming messages, and outgoing subscriptions
    private final Queue<Message> messagesToSend = new ConcurrentLinkedQueue<>();
    private String connectionId;
    private IotHubConnectionStatus state;
    private String hostName;
    private DeviceClientConfig deviceClientConfig;
    private IotHubListener listener;
    private TransportException savedException;
    private boolean reconnectionScheduled = false;
    private ExecutorService executorService;

    // State latches are used for asynchronous open and close operations
    private CountDownLatch authenticationSessionOpenedLatch; // tracks if the authentication session has opened yet or not
    private CountDownLatch deviceSessionsOpenedLatch; // tracks if all expected device sessions have opened yet or not
    private CountDownLatch closeReactorLatch; // tracks if the reactor has been closed yet or not

    // Proton-j primitives and wrappers for the device and authentication sessions
    private Connection connection;
    private ArrayList<AmqpsSessionHandler> sessionHandlerList = new ArrayList<>();
    private ArrayList<AmqpsSasTokenRenewalHandler> sasTokenRenwalHandlerList = new ArrayList<>();
    private AmqpsCbsSessionHandler amqpsCbsSessionHandler;

    public AmqpsIotHubConnection(DeviceClientConfig config)
    {
        this.deviceClientConfig = config;

        String gatewayHostname = this.deviceClientConfig.getGatewayHostname();
        if (gatewayHostname != null && !gatewayHostname.isEmpty())
        {
            log.debug("Gateway hostname was present in config, connecting to gateway rather than directly to hub");
            this.hostName = gatewayHostname;
        }
        else
        {
            log.trace("No gateway hostname was present in config, connecting directly to hub");
            this.hostName = this.deviceClientConfig.getIotHubHostname();
        }

        add(new Handshaker());

        this.state = IotHubConnectionStatus.DISCONNECTED;
        log.trace("AmqpsIotHubConnection object is created successfully and will use port {}", this.deviceClientConfig.isUseWebsocket() ? WEB_SOCKET_PORT : AMQP_PORT);
    }

    public void open(Queue<DeviceClientConfig> deviceClientConfigs, ScheduledExecutorService scheduledExecutorService) throws TransportException
    {
        log.debug("Opening amqp layer...");
        reconnectionScheduled = false;
        connectionId = UUID.randomUUID().toString();

        this.savedException = null;

        if (this.state == IotHubConnectionStatus.DISCONNECTED)
        {
            for (DeviceClientConfig clientConfig : deviceClientConfigs)
            {
                this.addDeviceSession(clientConfig, false);
            }

            initializeStateLatches();

            try
            {
                this.openAsync();

                log.trace("Waiting for authentication links to open...");
                boolean authenticationSessionOpenTimedOut = !this.authenticationSessionOpenedLatch.await(MAX_WAIT_TO_OPEN_AUTHENTICATION_SESSION, TimeUnit.MILLISECONDS);

                if (this.savedException != null)
                {
                    throw this.savedException;
                }

                if (authenticationSessionOpenTimedOut)
                {
                    closeConnectionWithException("Timed out waiting for authentication session to open", true);
                }

                log.trace("Waiting for device sessions to open...");
                boolean deviceSessionsOpenTimedOut = !this.deviceSessionsOpenedLatch.await(MAX_WAIT_TO_OPEN_WORKER_SESSIONS, TimeUnit.MILLISECONDS);

                if (this.savedException != null)
                {
                    throw this.savedException;
                }

                if (deviceSessionsOpenTimedOut)
                {
                    closeConnectionWithException("Timed out waiting for worker links to open", true);
                }
            }
            catch (InterruptedException e)
            {
                executorServicesCleanup();
                throw new TransportException("Interrupted while waiting for links to open for AMQP connection", e);
            }
        }

        this.state = IotHubConnectionStatus.CONNECTED;
        this.listener.onConnectionEstablished(this.connectionId);

        log.debug("Amqp connection opened successfully");
    }

    public void close() throws TransportException
    {
        log.debug("Shutting down amqp layer...");
        closeAsync();

        try
        {
            log.trace("Waiting for reactor to close...");
            closeReactorLatch.await(MAX_WAIT_TO_CLOSE_CONNECTION, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            throw new TransportException("Interrupted while closing proton reactor", e);
        }

        this.executorServicesCleanup();

        log.trace("Amqp connection closed successfully");
        this.state = IotHubConnectionStatus.DISCONNECTED;
    }

    @Override
    public void onReactorInit(Event event)
    {
        Reactor reactor = event.getReactor();

        String hostName = this.hostName;
        int port = AMQP_PORT;

        if (this.deviceClientConfig.isUseWebsocket())
        {
            ProxySettings proxySettings = this.deviceClientConfig.getProxySettings();
            if (proxySettings != null)
            {
                hostName = proxySettings.getHostname();
                port = proxySettings.getPort();
            }
            else
            {
                port = WEB_SOCKET_PORT;
            }
        }

        reactor.connectionToHost(hostName, port, this);
        reactor.schedule(SEND_MESSAGES_PERIOD_MILLIS, this);
    }

    @Override
    public void onReactorFinal(Event event)
    {
        log.trace("Amqps reactor finalized");
        releaseLatch(authenticationSessionOpenedLatch);
        releaseLatch(deviceSessionsOpenedLatch);
        releaseLatch(closeReactorLatch);

        if (this.savedException != null)
        {
            this.scheduleReconnection(this.savedException);
        }
    }

    @Override
    public void onConnectionInit(Event event)
    {
        this.connection = event.getConnection();
        this.connection.setHostname(hostName);
        this.connection.open();

        //Create one session per multiplexed device, or just one session if not multiplexing
        if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            Session cbsSession = connection.session();

            amqpsCbsSessionHandler = new AmqpsCbsSessionHandler(cbsSession, this);

            // sas token handler list has no information that needs to be carried over after a reconnect, so clear the list and
            // add a new handler to the list for each device session.
            sasTokenRenwalHandlerList.clear();

            // Open a device session per device, and create a sas token renewal handler for each device session
            for (AmqpsSessionHandler amqpsSessionHandler : this.sessionHandlerList)
            {
                amqpsSessionHandler.setSession(connection.session());

                sasTokenRenwalHandlerList.add(new AmqpsSasTokenRenewalHandler(amqpsCbsSessionHandler, amqpsSessionHandler));
            }
        }
        else
        {
            // should only be one session since x509 doesn't support multiplexing, so just get the first in the list
            AmqpsSessionHandler amqpsSessionHandler = this.sessionHandlerList.get(0);
            amqpsSessionHandler.setSession(connection.session());
        }
    }

    @Override
    public void onConnectionBound(Event event)
    {
        Transport transport = event.getTransport();

        if (this.deviceClientConfig.isUseWebsocket())
        {
            addWebSocketLayer(transport);
        }

        try
        {
            SSLContext sslContext = this.deviceClientConfig.getAuthenticationProvider().getSSLContext();
            if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
            {
                Sasl sasl = transport.sasl();
                sasl.setMechanisms("ANONYMOUS");
            }

            SslDomain domain = Proton.sslDomain();
            domain.setSslContext(sslContext);
            domain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
            domain.init(SslDomain.Mode.CLIENT);
            transport.ssl(domain);
        }
        catch (IOException e)
        {
            this.savedException = new TransportException(e);
            log.error("Encountered an exception while setting ssl domain for the amqp connection", this.savedException);
        }

        // Adding proxy layer needs to be done after sending SSL message
        if (this.deviceClientConfig.getProxySettings() != null)
        {
            addProxyLayer(transport, event.getConnection().getHostname() + ":" + WEB_SOCKET_PORT);
        }
    }

    @Override
    public void onConnectionLocalOpen(Event event)
    {
        log.trace("Amqp connection opened locally");
    }

    @Override
    public void onConnectionRemoteOpen(Event event)
    {
        log.trace("Amqp connection opened remotely");
    }

    @Override
    public void onConnectionLocalClose(Event event)
    {
        log.debug("Amqp connection closed locally, shutting down all active sessions...");
        for (AmqpsSessionHandler amqpSessionHandler : sessionHandlerList)
        {
            amqpSessionHandler.closeSession();
        }

        // cbs session handler is only null if using x509 auth
        if (amqpsCbsSessionHandler != null)
        {
            log.debug("Shutting down cbs session...");
            amqpsCbsSessionHandler.close();
        }

        log.trace("Closing reactor since connection has closed");
        event.getReactor().stop();
    }

    @Override
    public void onConnectionRemoteClose(Event event)
    {
        Connection connection = event.getConnection();
        if (connection.getLocalState() == EndpointState.ACTIVE)
        {
            ErrorCondition errorCondition = connection.getRemoteCondition();
            this.savedException = AmqpsExceptionTranslator.convertFromAmqpException(errorCondition);
            log.error("Amqp connection was closed remotely", this.savedException);
            this.connection.close();
        }
        else
        {
            log.trace("Closing reactor since connection has closed");
            event.getReactor().stop();
        }
    }

    @Override
    public void onTransportError(Event event)
    {
        super.onTransportError(event);
        this.state = IotHubConnectionStatus.DISCONNECTED;

        //Error may be on remote, and it may be local
        ErrorCondition errorCondition = event.getTransport().getRemoteCondition();
        if (errorCondition == null)
        {
            errorCondition = event.getTransport().getCondition();
        }

        this.savedException = AmqpsExceptionTranslator.convertFromAmqpException(errorCondition);

        log.error("Amqp transport error occurred, closing the amqps connection", this.savedException);
        event.getConnection().close();
    }

    @Override
    public void onTimerTask(Event event)
    {
        sendQueuedMessages();

        event.getReactor().schedule(SEND_MESSAGES_PERIOD_MILLIS, this);
    }

    @Override
    public void setListener(IotHubListener listener)
    {
        this.listener = listener;
    }

    @Override
    public IotHubStatusCode sendMessage(com.microsoft.azure.sdk.iot.device.Message message)
    {
        // Note that you cannot just send this message from this thread. Proton-j's reactor is not thread safe. As such,
        // all message sending must be done from the proton-j thread that is exposed to this SDK through callbacks
        // such as onLinkFlow(), or onTimerTask()
        log.trace("Adding message to amqp message queue to be sent later ({})", message);
        messagesToSend.add(message);
        return IotHubStatusCode.OK;
    }

    @Override
    public boolean sendMessageResult(IotHubTransportMessage message, IotHubMessageResult result)
    {
        DeliveryState ackType;

        // Complete/Abandon/Reject is an IoTHub concept. For AMQP, they map to Accepted/Released/Rejected.
        if (result == IotHubMessageResult.ABANDON)
        {
            ackType = Released.getInstance();
        }
        else if (result == IotHubMessageResult.REJECT)
        {
            ackType = new Rejected();
        }
        else if (result == IotHubMessageResult.COMPLETE)
        {
            ackType = Accepted.getInstance();
        }
        else
        {
            log.warn("Invalid IoT Hub message result {}", result.name());
            return false;
        }

        //Check each session handler to see who is responsible for sending this acknowledgement
        for (AmqpsSessionHandler sessionHandler : sessionHandlerList)
        {
            if (sessionHandler.acknowledgeReceivedMessage(message, ackType))
            {
                return true;
            }
        }

        log.warn("No sessions could acknowledge the message ({})", message);
        return false;
    }

    @Override
    public String getConnectionId()
    {
        return this.connectionId;
    }

    @Override
    public void onDeviceSessionOpened(String deviceId)
    {
        log.trace("Device session opened, counting down the device sessions opening latch");
        this.deviceSessionsOpenedLatch.countDown();
    }

    @Override
    public void onAuthenticationSessionOpened()
    {
        log.trace("Authentication session opened, counting down the authentication session opening latch");
        this.authenticationSessionOpenedLatch.countDown();

        if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            for (AmqpsSasTokenRenewalHandler amqpsSasTokenRenewalHandler : sasTokenRenwalHandlerList)
            {
                try
                {
                    amqpsSasTokenRenewalHandler.sendAuthenticationMessage(this.connection.getReactor());
                }
                catch (TransportException e)
                {
                    log.error("Failed to send CBS authentication message", e);
                    this.savedException = e;
                }
            }
        }
    }

    @Override
    public void onMessageAcknowledged(Message message)
    {
        this.listener.onMessageSent(message, null);
    }

    @Override
    public void onMessageReceived(IotHubTransportMessage message)
    {
        this.listener.onMessageReceived(message, null);
    }

    @Override
    public void onAuthenticationFailed(TransportException transportException)
    {
        this.savedException = transportException;
        releaseLatch(authenticationSessionOpenedLatch);
        releaseLatch(deviceSessionsOpenedLatch);
    }

    @Override
    public void onSessionClosedUnexpectedly(ErrorCondition errorCondition)
    {
        this.savedException = AmqpsExceptionTranslator.convertFromAmqpException(errorCondition);
        log.error("Amqp session closed unexpectedly. Closing this connection...", this.savedException);
        this.connection.close();
    }

    private void addWebSocketLayer(Transport transport)
    {
        log.debug("Adding websocket layer to amqp transport");
        WebSocketImpl webSocket = new WebSocketImpl(MAX_MESSAGE_PAYLOAD_SIZE);
        webSocket.configure(this.hostName, WEB_SOCKET_PATH, WEB_SOCKET_QUERY, WEB_SOCKET_PORT, WEB_SOCKET_SUB_PROTOCOL, null, null);
        ((TransportInternal) transport).addTransportLayer(webSocket);
    }

    private void addProxyLayer(Transport transport, String hostName)
    {
        log.debug("Adding proxy layer to amqp transport");
        ProxySettings proxySettings = this.deviceClientConfig.getProxySettings();

        ProxyImpl proxy;

        if (proxySettings.getUsername() != null && proxySettings.getPassword() != null)
        {
            log.trace("Adding proxy username and password to amqp proxy configuration");
            ProxyConfiguration proxyConfiguration = new ProxyConfiguration(ProxyAuthenticationType.BASIC, proxySettings.getProxy(), proxySettings.getUsername(), new String(proxySettings.getPassword()));
            proxy = new ProxyImpl(proxyConfiguration);
        }
        else
        {
            log.trace("No proxy username and password will be used amqp proxy configuration");
            proxy = new ProxyImpl();
        }

        final ProxyHandler proxyHandler = new ProxyHandlerImpl();
        proxy.configure(hostName, null, proxyHandler, transport);
        ((TransportInternal) transport).addTransportLayer(proxy);
    }

    private void sendQueuedMessages()
    {
        int messagesAttemptedToBeProcessed = 0;
        boolean lastSendSucceeded = true;
        Message message = messagesToSend.poll();
        while (message != null && messagesAttemptedToBeProcessed < MAX_MESSAGES_TO_SEND_PER_CALLBACK && lastSendSucceeded)
        {
            messagesAttemptedToBeProcessed++;
            lastSendSucceeded = sendQueuedMessage(message);

            if (!lastSendSucceeded)
            {
                //message failed to send, likely due to lack of link credit available. Re-queue and try again later
                log.trace("Amqp message failed to send, adding it back to messages to send queue ({})", message);
                messagesToSend.add(message);
            }

            message = messagesToSend.poll();
        }

        if (message != null)
        {
            //message was polled out of list, but loop exited from processing too many messages before it could process this message, so re-queue it for later
            messagesToSend.add(message);
        }
    }

    private boolean sendQueuedMessage(Message message)
    {
        boolean sendSucceeded = false;

        log.trace("Sending message over amqp ({})", message);

        for (AmqpsSessionHandler sessionHandler : this.sessionHandlerList)
        {
            sendSucceeded = sessionHandler.sendMessage(message);

            if (sendSucceeded)
            {
                break;
            }
        }

        return sendSucceeded;
    }

    private Reactor createReactor() throws TransportException
    {
        try
        {
            if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.X509_CERTIFICATE)
            {
                ReactorOptions options = new ReactorOptions();
                options.setEnableSaslByDefault(false);
                return Proton.reactor(options, this);
            }
            else
            {
                return Proton.reactor(this);
            }
        }
        catch (IOException e)
        {
            throw new TransportException("Could not create Proton reactor", e);
        }
    }

    private void scheduleReconnection(Throwable throwable)
    {
        if (!reconnectionScheduled)
        {
            reconnectionScheduled = true;
            log.warn("Amqp connection was closed, creating a thread to notify transport layer", throwable);
            ReconnectionNotifier.notifyDisconnectAsync(throwable, this.listener, this.connectionId);
        }
    }

    private void releaseLatch(CountDownLatch latch)
    {
        for (int i = 0; i < latch.getCount(); i++)
        {
            latch.countDown();
        }
    }

    private void addDeviceSession(DeviceClientConfig deviceClientConfig, boolean afterOpen)
    {
        // Check if the device session still exists from a previous connection
        AmqpsSessionHandler amqpsSessionHandler = null;
        for (AmqpsSessionHandler existingAmqpsSessionHandler : this.sessionHandlerList)
        {
            if (existingAmqpsSessionHandler.getDeviceId().equals(deviceClientConfig.getDeviceId()))
            {
                amqpsSessionHandler = existingAmqpsSessionHandler;
                break;
            }
        }

        // If the device session did not exist in the previous connection, or if there was no previous connection,
        // create a new session
        if (amqpsSessionHandler == null)
        {
            amqpsSessionHandler = new AmqpsSessionHandler(deviceClientConfig, this);
            this.sessionHandlerList.add(amqpsSessionHandler);
        }

        if (afterOpen)
        {
            amqpsSessionHandler.setSession(this.connection.session());
        }
    }

    private void initializeStateLatches()
    {
        this.closeReactorLatch = new CountDownLatch(REACTOR_COUNT);

        if (deviceClientConfig.getAuthenticationProvider() instanceof IotHubSasTokenAuthenticationProvider)
        {
            log.trace("Initializing authentication link latch count to {}", CBS_SESSION_COUNT);
            this.authenticationSessionOpenedLatch = new CountDownLatch(CBS_SESSION_COUNT);
        }
        else
        {
            log.trace("Initializing authentication link latch count to 0 because x509 connections don't have authentication links");
            this.authenticationSessionOpenedLatch = new CountDownLatch(0);
        }

        int expectedDeviceSessionCount = sessionHandlerList.size();
        this.deviceSessionsOpenedLatch = new CountDownLatch(expectedDeviceSessionCount);
        log.trace("Initializing device session latch count to {}", expectedDeviceSessionCount);
    }

    private void closeConnectionWithException(String errorMessage, boolean isRetryable) throws TransportException
    {
        TransportException transportException = new TransportException(errorMessage);
        transportException.setRetryable(isRetryable);
        log.error(errorMessage, transportException);

        this.close();
        throw transportException;
    }

    private void openAsync() throws TransportException
    {
        log.trace("OpenAsnyc called for amqp connection");

        if (executorService == null)
        {
            log.trace("Creating new executor service");
            executorService = Executors.newFixedThreadPool(1);
        }

        ReactorRunner reactorRunner = new ReactorRunner(new IotHubReactor(createReactor()), this.listener, this.connectionId);
        executorService.submit(reactorRunner);
    }

    private void closeAsync()
    {
        log.trace("OpenAsync called for amqp connection");

        if (this.connection.getLocalState() == EndpointState.CLOSED && this.connection.getRemoteState() == EndpointState.CLOSED)
        {
            log.trace("Closing amqp reactor since the connection was already closed");
            this.connection.getReactor().stop();
        }
        else
        {
            log.trace("Closing amqp connection");
            this.connection.close();
        }
    }

    private void executorServicesCleanup() throws TransportException
    {
        if (this.executorService != null)
        {
            log.trace("Shutdown of executor service has started");
            this.executorService.shutdown();
            try
            {
                // Wait a while for existing tasks to terminate
                if (!this.executorService.awaitTermination(MAX_WAIT_TO_TERMINATE_EXECUTOR, TimeUnit.SECONDS))
                {
                    this.executorService.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!this.executorService.awaitTermination(MAX_WAIT_TO_TERMINATE_EXECUTOR, TimeUnit.SECONDS))
                    {
                        log.trace("Pool did not terminate");
                    }
                }

                this.executorService = null;
            }
            catch (InterruptedException e)
            {
                log.warn("Interrupted while cleaning up executor services", e);
                // (Re-)Cancel if current thread also interrupted
                this.executorService.shutdownNow();
                this.executorService = null;
                throw new TransportException("Waited too long for the connection to close.", e);
            }
            log.trace("Shutdown of executor service completed");
        }
    }

    private class ReactorRunner implements Callable
    {
        private static final String THREAD_NAME = "azure-iot-sdk-ReactorRunner";
        private final IotHubReactor iotHubReactor;
        private final IotHubListener listener;
        private String connectionId;

        ReactorRunner(IotHubReactor iotHubReactor, IotHubListener listener, String connectionId)
        {
            this.listener = listener;
            this.iotHubReactor = iotHubReactor;
            this.connectionId = connectionId;
        }

        @Override
        public Object call()
        {
            try
            {
                Thread.currentThread().setName(THREAD_NAME);
                iotHubReactor.run();
            }
            catch (HandlerException e)
            {
                this.listener.onConnectionLost(new TransportException(e), connectionId);
            }

            return null;
        }
    }
}
