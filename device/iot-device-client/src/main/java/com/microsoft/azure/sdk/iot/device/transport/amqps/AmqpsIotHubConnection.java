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
import com.microsoft.azure.sdk.iot.device.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingDeviceUnauthorizedException;
import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;
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
import java.util.*;
import java.util.concurrent.*;

/**
 * An AMQPS IotHub connection between a device and an IoTHub or Edgehub. This class is responsible for reacting to connection level and
 * reactor level events. It is also responsible for creating sessions and handlers for those sessions. An instance of this
 * object may be reused after it has been closed. This class is used for both single-plexed and multiplexed connections.
 */
@Slf4j
public final class AmqpsIotHubConnection extends BaseHandler implements IotHubTransportConnection, AmqpsSessionStateCallback, ReactorRunnerStateCallback
{
    // Timeouts
    private static final int MAX_WAIT_TO_CLOSE_CONNECTION = 20 * 1000; // 20 second timeout

    // Web socket constants
    private static final String WEB_SOCKET_PATH = "/$iothub/websocket";
    private static final String WEB_SOCKET_SUB_PROTOCOL = "AMQPWSB10";
    private static final String WEB_SOCKET_QUERY = "iothub-no-client-cert=true";
    private static final int MAX_MESSAGE_PAYLOAD_SIZE = 256 * 1024; //max IoT Hub message size is 256 kb, so amqp websocket layer should buffer at most that much space
    private static final int MAX_FRAME_SIZE = 4 * 1024;
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
    private final String hostName;
    private SSLContext sslContext;
    private final boolean isWebsocketConnection;
    private final ClientConfiguration.AuthType authenticationType;
    private final Set<ClientConfiguration> clientConfigurations;
    private IotHubListener listener;
    private TransportException savedException;
    private final Object executorServiceLock = new Object();
    private ExecutorService executorService;
    private final ProxySettings proxySettings;
    private final String transportUniqueIdentifier;

    // State latches are used for asynchronous open and close operations
    private CountDownLatch authenticationSessionOpenedLatch; // tracks if the authentication session has opened yet or not
    private Map<String, CountDownLatch> deviceSessionsOpenedLatches; // tracks if all expected device sessions have opened yet or not. Keys are deviceId's
    private CountDownLatch closeReactorLatch; // tracks if the reactor has been closed yet or not

    // Proton-j primitives and wrappers for the device and authentication sessions
    private Connection connection;
    private Reactor reactor;

    // keys are device Ids, values are the session handlers associated with that device id
    private final Map<String, AmqpsSessionHandler> reconnectingDeviceSessionHandlers = new ConcurrentHashMap<>();

    // keys are device Ids, values are the session handlers associated with that device id
    private final Map<String, AmqpsSessionHandler> sessionHandlers = new ConcurrentHashMap<>();

    private final Queue<AmqpsSasTokenRenewalHandler> sasTokenRenewalHandlers = new ConcurrentLinkedQueue<>();
    private AmqpsCbsSessionHandler amqpsCbsSessionHandler;

    // Multiplexed device registrations and un-registrations come from a non-reactor thread, so they get queued into these
    // queues and are executed when onTimerTask checks them.
    private final Set<ClientConfiguration> multiplexingClientsToRegister;

    // keys are the configs of the clients to unregister, values are the flag that determines if the session should be cached locally for re-use upon reconnection
    private final Map<ClientConfiguration, Boolean> multiplexingClientsToUnregister;

    // Only set when the connection is for single device and not the multiplexing client.
    private ClientConfiguration clientConfiguration = null;

    private final boolean isMultiplexing;

    private final int keepAliveInterval;

    private final Map<IotHubTransportMessage, IotHubMessageResult> queuedAcknowledgements = new ConcurrentHashMap<>();

    public AmqpsIotHubConnection(ClientConfiguration config, String transportUniqueIdentifier)
    {
        // This allows us to create thread safe sets despite there being no such type default in Java 7 or 8
        this.clientConfigurations = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.multiplexingClientsToRegister = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.multiplexingClientsToUnregister = new ConcurrentHashMap<>();

        this.clientConfigurations.add(config);

        this.clientConfiguration = config;
        this.transportUniqueIdentifier = transportUniqueIdentifier;
        this.isWebsocketConnection = config.isUsingWebsocket();
        this.authenticationType = config.getAuthenticationType();
        this.proxySettings = config.getProxySettings();

        String gatewayHostname = config.getGatewayHostname();
        if (gatewayHostname != null && !gatewayHostname.isEmpty())
        {
            log.debug("Gateway hostname was present in config, connecting to gateway rather than directly to hub");
            this.hostName = gatewayHostname;
        }
        else
        {
            log.trace("No gateway hostname was present in config, connecting directly to hub");
            this.hostName = config.getIotHubHostname();
        }

        add(new Handshaker());

        this.isMultiplexing = false;

        this.keepAliveInterval = config.getKeepAliveInterval();

        this.state = IotHubConnectionStatus.DISCONNECTED;
        log.trace("AmqpsIotHubConnection object is created successfully and will use port {}", this.isWebsocketConnection ? WEB_SOCKET_PORT : AMQP_PORT);
    }

    public AmqpsIotHubConnection(String hostName, String transportUniqueIdentifier, boolean isWebsocketConnection, SSLContext sslContext, ProxySettings proxySettings, int keepAliveInterval)
    {
        // This allows us to create thread safe sets despite there being no such type default in Java 7 or 8
        this.clientConfigurations = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.multiplexingClientsToRegister = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.multiplexingClientsToUnregister = new ConcurrentHashMap<>();

        this.isWebsocketConnection = isWebsocketConnection;

        // This constructor is only called when multiplexing, and multiplexing only supports SAS auth
        this.authenticationType = ClientConfiguration.AuthType.SAS_TOKEN;

        this.hostName = hostName;
        this.transportUniqueIdentifier = transportUniqueIdentifier;
        this.proxySettings = proxySettings;
        this.sslContext = sslContext;

        add(new Handshaker());

        this.isMultiplexing = true; // This constructor is only ever called when multiplexing

        this.state = IotHubConnectionStatus.DISCONNECTED;
        log.trace("AmqpsIotHubConnection object is created successfully and will use port {}", this.isWebsocketConnection ? WEB_SOCKET_PORT : AMQP_PORT);

        this.keepAliveInterval = keepAliveInterval;
    }

    public void registerMultiplexedDevice(ClientConfiguration config)
    {
        if (this.state == IotHubConnectionStatus.CONNECTED)
        {
            // session opening logic should be done from a proton reactor thread, not this thread. This queue gets polled
            // onTimerTask so that this client gets registered on that thread instead.
            log.trace("Queuing the registration of device {} to an active multiplexed connection", config.getDeviceId());
            deviceSessionsOpenedLatches.put(config.getDeviceId(), new CountDownLatch(1));
            this.multiplexingClientsToRegister.add(config);
        }

        clientConfigurations.add(config);
    }

    /**
     * Asynchronously unregister a multiplexed device from an active multiplexed connection or synchronously unregister
     * a multiplexed device from a closed multiplexed connection.
     * @param config the config of the device that should be unregistered.
     * @param willReconnect true if the device will be re-registered soon because it is reconnecting.
     */
    public void unregisterMultiplexedDevice(ClientConfiguration config, boolean willReconnect)
    {
        if (this.state == IotHubConnectionStatus.CONNECTED)
        {
            // session closing logic should be done from a proton reactor thread, not this thread. This queue gets polled
            // onTimerTask so that this client gets unregistered on that thread instead.
            if (willReconnect)
            {
                log.trace("Queuing the unregistration of device {} from an active multiplexed connection. The device will be re-registered for reconnection purposes.", config.getDeviceId());
            }
            else
            {
                log.trace("Queuing the unregistration of device {} from an active multiplexed connection", config.getDeviceId());
            }

            this.multiplexingClientsToUnregister.put(config, willReconnect);
        }

        clientConfigurations.remove(config);
        deviceSessionsOpenedLatches.remove(config.getDeviceId());
    }

    public void open() throws TransportException
    {
        log.debug("Opening amqp layer...");
        connectionId = UUID.randomUUID().toString();

        this.savedException = null;

        if (this.state == IotHubConnectionStatus.DISCONNECTED)
        {
            for (ClientConfiguration clientConfig : clientConfigurations)
            {
                this.addSessionHandler(clientConfig);
            }

            initializeStateLatches();

            try
            {
                this.openAsync();

                if (this.authenticationType == ClientConfiguration.AuthType.SAS_TOKEN)
                {
                    // x509 authenticated connections don't open authentication links since the SSL handshake does all the authentication
                    log.trace("Waiting for authentication links to open...");
                }

                Iterator<ClientConfiguration> configsIterator = this.clientConfigurations.iterator();
                ClientConfiguration defaultConfig = configsIterator.hasNext() ? configsIterator.next() : null;
                int timeoutSeconds = ClientConfiguration.DEFAULT_AMQP_OPEN_AUTHENTICATION_SESSION_TIMEOUT_IN_SECONDS;
                if (defaultConfig != null)
                {
                    timeoutSeconds = defaultConfig.getAmqpOpenAuthenticationSessionTimeout();
                }
                boolean authenticationSessionOpenTimedOut = !this.authenticationSessionOpenedLatch.await(timeoutSeconds, TimeUnit.SECONDS);

                if (this.savedException != null)
                {
                    throw this.savedException;
                }

                if (authenticationSessionOpenTimedOut)
                {
                    closeConnectionWithException("Timed out waiting for authentication session to open", true);
                }

                log.trace("Waiting for device sessions to open...");
                boolean deviceSessionsOpenTimedOut = false;
                for (ClientConfiguration config : this.clientConfigurations)
                {
                    //Each device has its own worker session timeout according to its config settings
                    deviceSessionsOpenTimedOut = !this.deviceSessionsOpenedLatches.get(config.getDeviceId()).await(config.getAmqpOpenDeviceSessionsTimeout(), TimeUnit.SECONDS);

                    if (deviceSessionsOpenTimedOut)
                    {
                        // If any device session times out while opening, don't wait for the others
                        break;
                    }
                }

                if (this.savedException != null)
                {
                    throw this.savedException;
                }

                if (deviceSessionsOpenTimedOut)
                {
                    closeConnectionWithException("Timed out waiting for worker links to open", true);
                }
            }
            catch (TransportException e)
            {
                // since some session handlers were created and saved locally, they must be deleted so that the next open
                // call doesn't add new session handlers on top of a list of the same session handlers from the last attempt.
                clearLocalState();

                // clean up network resources and thread scheduler before exiting this layer. Subsequent open attempts
                // will create a new reactor and a new executor service
                closeNetworkResources();
                throw e;
            }
            catch (InterruptedException e)
            {
                // since some session handlers were created and saved locally, they must be deleted so that the next open
                // call doesn't add new session handlers on top of a list of the same session handlers from the last attempt.
                clearLocalState();

                // clean up network resources and thread scheduler before exiting this layer. Subsequent open attempts
                // will create a new reactor and a new executor service
                closeNetworkResources();

                TransportException interruptedTransportException = new TransportException("Interrupted while waiting for links to open for AMQP connection", e);
                interruptedTransportException.setRetryable(true);
                throw interruptedTransportException;
            }
        }

        this.state = IotHubConnectionStatus.CONNECTED;
        this.listener.onConnectionEstablished(this.connectionId);

        log.debug("Amqp connection opened successfully");
    }

    private void clearLocalState()
    {
        this.sessionHandlers.clear();
        this.sasTokenRenewalHandlers.clear();
    }

    private void closeNetworkResources()
    {
        this.reactor.free();
        this.executorServicesCleanup();
    }

    public void close()
    {
        log.debug("Shutting down amqp layer...");
        try
        {
            closeAsync();

            try
            {
                log.trace("Waiting for reactor to close...");
                // Result is not used
                //noinspection ResultOfMethodCallIgnored
                closeReactorLatch.await(MAX_WAIT_TO_CLOSE_CONNECTION, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                // If the SDK's disconnect message doesn't make it to the service, just clean up the local resources.
                // The service will eventually figure out that the connection was lost since the keep-alive pings stop.
                log.warn("Interrupted while waiting on reactor to close gracefully. Forcefully closing the reactor now.", e);
            }
        }
        finally
        {
            // always clean up the executor service, free the reactor and set the state as DISCONNECTED even when the close
            // isn't successful. Failing to free the reactor in particular leaks network resources
            clearLocalState();
            closeNetworkResources();
            this.state = IotHubConnectionStatus.DISCONNECTED;
            log.trace("Amqp connection closed successfully");
        }
    }

    @Override
    public void onReactorInit(Event event)
    {
        this.reactor = event.getReactor();

        String hostName = this.hostName;
        int port = AMQP_PORT;

        if (this.isWebsocketConnection)
        {
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

        this.reactor.connectionToHost(hostName, port, this);
        this.reactor.schedule(SEND_MESSAGES_PERIOD_MILLIS, this);
    }

    @Override
    public void onReactorFinal(Event event)
    {
        log.trace("Amqps reactor finalized");
        releaseLatch(authenticationSessionOpenedLatch);
        releaseDeviceSessionLatches();
        releaseLatch(closeReactorLatch);

        if (this.savedException != null)
        {
            this.reconnectingDeviceSessionHandlers.putAll(this.sessionHandlers);
            this.listener.onConnectionLost(this.savedException, this.connectionId);
        }
    }

    @Override
    public void onConnectionInit(Event event)
    {
        this.connection = event.getConnection();
        this.connection.setHostname(hostName);
        this.connection.open();
    }

    @Override
    public void onConnectionBound(Event event)
    {
        Transport transport = event.getTransport();

        // Convert from seconds to milliseconds since this proton-j API only accepts keep alive in milliseconds
        transport.setIdleTimeout(keepAliveInterval * 1000);

        if (this.isWebsocketConnection)
        {
            addWebSocketLayer(transport);
        }

        try
        {
            Iterator<ClientConfiguration> configsIterator = this.clientConfigurations.iterator();
            ClientConfiguration defaultConfig = configsIterator.hasNext() ? configsIterator.next() : null;
            SSLContext sslContext;
            if (defaultConfig != null)
            {
                sslContext = defaultConfig.getAuthenticationProvider().getSSLContext();
            }
            else if (this.sslContext != null)
            {
                // This should only be hit when a user creates a multiplexing client and specifies an SSLContext
                // that they want to use
                sslContext = this.sslContext;
            }
            else
            {
                // This should only be hit when a user creates a multiplexing client and doesn't specify an SSLContext
                // that they want to use
                sslContext = new IotHubSSLContext().getSSLContext();
            }

            if (this.authenticationType == ClientConfiguration.AuthType.SAS_TOKEN)
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
        if (proxySettings != null)
        {
            addProxyLayer(transport, event.getConnection().getHostname() + ":" + WEB_SOCKET_PORT);
        }
    }

    // The warning is for how sessionHandlers.peek() may return null, but for x509 cases, this code only executes
    // if one session handler is present in the list, so it is a false positive
    @Override
    public void onConnectionLocalOpen(Event event)
    {
        log.trace("Amqp connection opened locally");

        //Create one session per multiplexed device, or just one session if not multiplexing
        if (this.authenticationType == ClientConfiguration.AuthType.SAS_TOKEN)
        {
            // The CBS ("Claims-Based-Security") session is dedicated to sending SAS tokens to the service to authenticate
            // all of the device sessions in this AMQP connection.
            Session cbsSession = connection.session();

            amqpsCbsSessionHandler = new AmqpsCbsSessionHandler(cbsSession, this);

            // sas token handler list has no information that needs to be carried over after a reconnect, so close and
            // clear the list and add a new handler to the list for each device session.
            for (AmqpsSasTokenRenewalHandler sasTokenRenewalHandler : this.sasTokenRenewalHandlers)
            {
                sasTokenRenewalHandler.close();
            }
            sasTokenRenewalHandlers.clear();

            // Open a device session per device, and create a sas token renewal handler for each device session
            for (AmqpsSessionHandler amqpsSessionHandler : this.sessionHandlers.values())
            {
                amqpsSessionHandler.setSession(connection.session());
                sasTokenRenewalHandlers.add(new AmqpsSasTokenRenewalHandler(amqpsCbsSessionHandler, amqpsSessionHandler));
            }
        }
        else
        {
            // should only be one session since x509 doesn't support multiplexing, so just get the first in the list
            AmqpsSessionHandler amqpsSessionHandler = this.sessionHandlers.values().iterator().next();
            amqpsSessionHandler.setSession(connection.session());
        }
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
        for (AmqpsSessionHandler amqpSessionHandler : sessionHandlers.values())
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

        //Sometimes the remote errorCondition object is not null, but all of its fields are null. In this case, check the local error condition
        //for the error details.
        boolean isALocalErrorCondition = false;
        if (errorCondition == null || (errorCondition.getCondition() == null && errorCondition.getDescription() == null && errorCondition.getInfo() == null))
        {
            errorCondition = event.getTransport().getCondition();
            isALocalErrorCondition = true;
        }

        this.savedException = AmqpsExceptionTranslator.convertFromAmqpException(errorCondition);

        // In the event that we get a local error and the connection is already in the closed state we need to manually call
        // onConnectionLocalClose. Proton will not queue the CONNECTION_LOCAL_CLOSE event since the Endpoint status is CLOSED
        if (event.getConnection().getLocalState() == EndpointState.CLOSED && isALocalErrorCondition)
        {
            log.error("Amqp transport error occurred, calling onConnectionLocalClose", this.savedException);
            onConnectionLocalClose(event);
        }
        else
        {
            log.error("Amqp transport error occurred, closing the AMQPS connection", this.savedException);
            event.getConnection().close();
        }
    }

    @Override
    public void onTimerTask(Event event)
    {
        sendQueuedMessages();
        sendQueuedAcknowledgements();

        checkForNewlyUnregisteredMultiplexedClientsToStop();
        checkForNewlyRegisteredMultiplexedClientsToStart();

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
        // don't send acknowledgements from outside the proton reactor thread. Queue them locally so that the reactor
        // thread can pick them up and send them later
        queuedAcknowledgements.put(message, result);
        return true;
    }

    private void sendQueuedAcknowledgements()
    {
        while (!queuedAcknowledgements.isEmpty())
        {
            IotHubTransportMessage queuedAcknowledgement = queuedAcknowledgements.keySet().iterator().next();
            IotHubMessageResult result = queuedAcknowledgements.get(queuedAcknowledgement);

            queuedAcknowledgements.remove(queuedAcknowledgement);

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
                continue;
            }

            AmqpsSessionHandler sessionHandler = sessionHandlers.get(queuedAcknowledgement.getConnectionDeviceId());
            if (sessionHandler != null && sessionHandler.acknowledgeReceivedMessage(queuedAcknowledgement, ackType))
            {
                continue;
            }

            log.warn("No sessions could acknowledge the message ({})", queuedAcknowledgement);
        }
    }

    @Override
    public String getConnectionId()
    {
        return this.connectionId;
    }

    @Override
    public void onDeviceSessionOpened(String deviceId)
    {
        if (this.deviceSessionsOpenedLatches.containsKey(deviceId))
        {
            log.trace("Device session for device {} opened, counting down the device sessions opening latch", deviceId);
            this.deviceSessionsOpenedLatches.get(deviceId).countDown();

            if (isMultiplexing)
            {
                // For non-multiplexing cases, the above logic for counting down the latch for the single device session
                // will notify the open() thread that the reactor thread has successfully opened the device session.
                // That will then onStatusChanged the onConnectionEstablished callback on this same listener. Because of that,
                // there is no listener callback to onStatusChanged here for non-multiplexing cases.
                this.listener.onMultiplexedDeviceSessionEstablished(this.connectionId, deviceId);
            }
        }
        else
        {
            log.warn("Unrecognized deviceId {} reported its device session as opened, ignoring it.", deviceId);
        }
    }

    @Override
    public void onAuthenticationSessionOpened()
    {
        log.trace("Authentication session opened, counting down the authentication session opening latch");
        this.authenticationSessionOpenedLatch.countDown();

        if (this.authenticationType == ClientConfiguration.AuthType.SAS_TOKEN)
        {
            if (this.isWebsocketConnection)
            {
                // AMQPS_WS has an issue where the remote host kills the connection if 31+ multiplexed devices are all
                // authenticated at once. To work around this, the authentications will be done at most 30 at a time.
                // Once a given device's authentication finishes, it will trigger the next authentication
                List<AmqpsSasTokenRenewalHandler> handlers = new ArrayList<>(sasTokenRenewalHandlers);
                int maxInFlightAuthenticationMessages = 30;
                for (int i = 0; i < handlers.size() - maxInFlightAuthenticationMessages; i++)
                {
                    if (i + maxInFlightAuthenticationMessages < handlers.size())
                    {
                        handlers.get(i).setNextToAuthenticate(handlers.get(i + maxInFlightAuthenticationMessages));
                    }
                }

                int min = Math.min(maxInFlightAuthenticationMessages, handlers.size());
                for (int i = 0; i < min; i++)
                {
                    try
                    {
                        // Sending the first authentication message will eventually trigger sending the second, which will trigger the third, and so on.
                        handlers.get(i).sendAuthenticationMessage(this.connection.getReactor());
                    }
                    catch (TransportException e)
                    {
                        log.error("Failed to send CBS authentication message", e);
                        this.savedException = e;
                    }
                }
            }
            else
            {
                for (AmqpsSasTokenRenewalHandler amqpsSasTokenRenewalHandler : sasTokenRenewalHandlers)
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
    }

    @Override
    public void onMessageAcknowledged(Message message, DeliveryState deliveryState, String deviceId)
    {
        if (deliveryState == Accepted.getInstance())
        {
            this.listener.onMessageSent(message, deviceId, null);
        }
        else if (deliveryState instanceof Rejected)
        {
            // The message was not accepted by the server, and the reason why is found within the nested error
            TransportException ex = AmqpsExceptionTranslator.convertFromAmqpException(((Rejected) deliveryState).getError());
            this.listener.onMessageSent(message, deviceId, ex);
        }
        else if (deliveryState == Released.getInstance())
        {
            // As per AMQP spec, this state means the message should be re-delivered to the server at a later time
            ProtocolException protocolException = new ProtocolException("Message was released by the amqp server");
            protocolException.setRetryable(true);
            this.listener.onMessageSent(message, deviceId, protocolException);
        }
        else
        {
            log.warn("Unexpected delivery state for sent message ({})", message);
        }
    }

    @Override
    public void onMessageReceived(IotHubTransportMessage message)
    {
        this.listener.onMessageReceived(message, null);
    }

    @Override
    public void onAuthenticationFailed(String deviceId, TransportException transportException)
    {
        if (this.isMultiplexing)
        {
            if (this.state != IotHubConnectionStatus.CONNECTED)
            {
                if (this.savedException == null)
                {
                    this.savedException = new MultiplexingDeviceUnauthorizedException("One or more multiplexed devices failed to authenticate");
                }

                if (this.savedException instanceof MultiplexingDeviceUnauthorizedException)
                {
                    ((MultiplexingDeviceUnauthorizedException)this.savedException).addRegistrationException(deviceId, transportException);
                }
            }
            else
            {
                // When the muxed connection is already open, no need to save the exception to this.savedException
                // The call to onMultiplexedDeviceSessionRegistrationFailed will propagate the exception up to the
                // transport layer to handle accordingly.
                log.trace("Not saving the authentication failure locally. Just notifying upper layer directly.");
            }
        }
        else
        {
            this.savedException = transportException;
        }

        this.listener.onMultiplexedDeviceSessionRegistrationFailed(this.connectionId, deviceId, transportException);

        if (this.deviceSessionsOpenedLatches.containsKey(deviceId))
        {
            this.deviceSessionsOpenedLatches.get(deviceId).countDown();
        }
        else
        {
            log.warn("Unrecognized device Id reported authentication failure, could not map it to a device session latch", transportException);
        }
    }

    @Override
    public void onSessionClosedUnexpectedly(ErrorCondition errorCondition, String deviceId)
    {
        TransportException savedException = AmqpsExceptionTranslator.convertFromAmqpException(errorCondition);

        if (this.isMultiplexing)
        {
            // If a device is registered to an active multiplexed connection, and the session closes locally before opening,
            // need to notify upper layer that the register call failed, and that it shouldn't wait for it to report having opened.
            this.listener.onMultiplexedDeviceSessionRegistrationFailed(this.connectionId, deviceId, savedException);
        }

        // If the session closes during an open call, need to decrement this latch so that the open call doesn't wait
        // for this session to open. The above call to onMultiplexedDeviceSessionRegistrationFailed will report
        // the relevant exception.
        CountDownLatch deviceSessionsOpenedLatch = this.deviceSessionsOpenedLatches.get(deviceId);
        if (deviceSessionsOpenedLatch != null)
        {
            deviceSessionsOpenedLatch.countDown();
        }

        if (isMultiplexing)
        {
            // When multiplexing, don't kill the connection just because a session dropped.
            log.error("Amqp session closed unexpectedly. notifying the transport layer to start reconnection logic...", this.savedException);
            this.reconnectingDeviceSessionHandlers.putAll(this.sessionHandlers);
            this.listener.onMultiplexedDeviceSessionLost(savedException, this.connectionId, deviceId);
        }
        else
        {
            // When not multiplexing, reconnection logic will just spin up the whole connection again.
            this.savedException = savedException;
            log.error("Amqp session closed unexpectedly. Closing this connection...", this.savedException);
            this.connection.close();
        }
    }

    @Override
    public void onCBSSessionClosedUnexpectedly(ErrorCondition errorCondition)
    {
        this.savedException = AmqpsExceptionTranslator.convertFromAmqpException(errorCondition);
        log.error("Amqp CBS session closed unexpectedly. Closing this connection...", this.savedException);
        this.connection.close();
    }

    @Override
    public void onSessionClosedAsExpected(String deviceId)
    {
        if (this.isMultiplexing)
        {
            log.trace("onSessionClosedAsExpected callback executed, notifying transport layer");
            this.listener.onMultiplexedDeviceSessionLost(this.savedException, this.connectionId, deviceId);
        }
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
        Message message = messagesToSend.poll();
        while (message != null && messagesAttemptedToBeProcessed < MAX_MESSAGES_TO_SEND_PER_CALLBACK)
        {
            messagesAttemptedToBeProcessed++;
            SendResult sendResult = sendQueuedMessage(message);

            // If no active device sessions are responsible for sending messages for the device that the transport message belongs to,
            // then either the device session is reconnecting and the message should be requeued, or the device session
            // was unregistered by the user and the message should report that it failed to send before the device session was unregistered.
            if (sendResult == SendResult.WRONG_DEVICE)
            {
                AmqpsSessionHandler reconnectingDeviceSessionHandler = this.reconnectingDeviceSessionHandlers.get(message.getConnectionDeviceId());
                if (reconnectingDeviceSessionHandler != null)
                {
                    log.trace("Amqp message failed to send because its AMQP session is currently reconnecting. Adding it back to messages to send queue ({})", message);
                    TransportException transportException = new TransportException("Amqp message failed to send because its AMQP session is currently reconnecting");
                    transportException.setRetryable(true);
                    this.listener.onMessageSent(message, message.getConnectionDeviceId(), transportException);
                }
                else
                {
                    TransportException transportException = new TransportException("Message failed to send because it belonged to a device that was unregistered from the AMQP connetion");
                    transportException.setRetryable(false);
                    this.listener.onMessageSent(message, message.getConnectionDeviceId(), transportException);
                }
            }
            else if (sendResult == SendResult.DUPLICATE_SUBSCRIPTION_MESSAGE)
            {
                // No need to send a twin/method subscription message if you are already subscribed or if the subscription is in progress
                log.trace("Attempted to send subscription message while the subscription was already in progress. Discarding the message ({})", message);
            }
            else if (sendResult == SendResult.SUBSCRIPTION_IN_PROGRESS)
            {
                // Proton-j doesn't handle the scenario of sending twin/method messages on links that haven't been opened remotely yet, so hold
                // off on sending them until the subscription has finished.
                log.trace("Attempted to send twin/method message while the twin/method subscription was in progress. Adding it back to messages to send queue to try again after the subscription has finished ({})", message);
                TransportException transportException = new TransportException("Subscription in progress needs to be completed before this message can be sent");
                transportException.setRetryable(true);
                this.listener.onMessageSent(message, message.getConnectionDeviceId(), transportException);
            }
            else if (sendResult == SendResult.LINKS_NOT_OPEN)
            {
                // Shouldn't happen. If it does, it signals that we have a bug in this SDK.
                log.warn("Failed to send a message because its AMQP links were not open yet. Adding it back to messages to send queue ({})", message);
                TransportException transportException = new TransportException("Amqp links not open for this message");
                transportException.setRetryable(true);
                this.listener.onMessageSent(message, message.getConnectionDeviceId(), transportException);
            }
            else if (sendResult == SendResult.UNKNOWN_FAILURE)
            {
                // Shouldn't happen. If it does, it signals that we have a bug in this SDK.
                log.warn("Unknown failure occurred while attempting to send. Adding it back to messages to send queue ({})", message);
                TransportException transportException = new TransportException("Unknown failure");
                transportException.setRetryable(true);
                this.listener.onMessageSent(message, message.getConnectionDeviceId(), transportException);
            }

            message = messagesToSend.poll();
        }

        if (message != null)
        {
            //message was polled out of list, but loop exited from processing too many messages before it could process this message, so re-queue it for later
            messagesToSend.add(message);
        }
    }

    private SendResult sendQueuedMessage(Message message)
    {
        log.trace("Sending message over amqp ({})", message);

        AmqpsSessionHandler sessionHandler = this.sessionHandlers.get(message.getConnectionDeviceId());

        if (sessionHandler == null)
        {
            // no active session handler for the device that is sending this telemetry
            return SendResult.WRONG_DEVICE;
        }

        return sessionHandler.sendMessage(message);
    }

    private Reactor createReactor() throws TransportException
    {
        try
        {
            ReactorOptions options = new ReactorOptions();

            // If this option isn't set, proton defaults to 16 * 1024 max frame size. This used to default to 4 * 1024,
            // and this change to 16 * 1024 broke the websocket implementation that we layer on top of proton-j.
            // By setting this frame size back to 4 * 1024, AMQPS_WS clients can send messages with payloads up to the
            // expected 256 * 1024 bytes. For more context, see https://github.com/Azure/azure-iot-sdk-java/issues/742
            options.setMaxFrameSize(MAX_FRAME_SIZE);

            if (this.authenticationType == ClientConfiguration.AuthType.X509_CERTIFICATE)
            {
                // x509 authentication does not use SASL, so disable it
                options.setEnableSaslByDefault(false);
            }

            return Proton.reactor(options, this);
        }
        catch (IOException e)
        {
            throw new TransportException("Could not create Proton reactor", e);
        }
    }

    private void releaseLatch(CountDownLatch latch)
    {
        for (int i = 0; i < latch.getCount(); i++)
        {
            latch.countDown();
        }
    }

    private void releaseDeviceSessionLatches()
    {
        for (String deviceId : this.deviceSessionsOpenedLatches.keySet())
        {
            releaseLatch(this.deviceSessionsOpenedLatches.get(deviceId));
        }
    }

    private AmqpsSessionHandler addSessionHandler(ClientConfiguration clientConfiguration)
    {
        String deviceId = clientConfiguration.getDeviceId();

        // Check if the device session still exists from a previous connection
        AmqpsSessionHandler amqpsSessionHandler = this.sessionHandlers.get(deviceId);
        if (amqpsSessionHandler != null)
        {
            // session handler already existed for this device. No need to create a new session handler and add it to this.sessionHandlers
            return amqpsSessionHandler;
        }

        // If the device session was temporarily unregistered during reconnection, reuse the cached session handler
        // since it holds the subscription information needed to fully reconnect all links that were open prior to reconnection.
        if (this.reconnectingDeviceSessionHandlers.containsKey(deviceId))
        {
            // Since a session is being added for the reconnecting device, it can be removed from the reconnectingDeviceSessionHandlers collection
            amqpsSessionHandler = this.reconnectingDeviceSessionHandlers.remove(deviceId);
        }
        else
        {
            // If the device session did not exist in the previous connection, or if there was no previous connection,
            // create a new session
            amqpsSessionHandler = new AmqpsSessionHandler(clientConfiguration, this);
        }

        this.sessionHandlers.put(deviceId, amqpsSessionHandler);

        return amqpsSessionHandler;
    }

    // This function is called periodically from the onTimerTask reactor callback so that any newly registered device sessions
    // can be opened on a reactor thread instead of from one of our threads.
    private void checkForNewlyRegisteredMultiplexedClientsToStart()
    {
        Iterator<ClientConfiguration> configsToRegisterIterator = this.multiplexingClientsToRegister.iterator();
        ClientConfiguration configToRegister = configsToRegisterIterator.hasNext() ? configsToRegisterIterator.next() : null;
        Set<ClientConfiguration> configsRegisteredSuccessfully = new HashSet<>();
        while (configToRegister != null)
        {
            AmqpsSessionHandler amqpsSessionHandler = addSessionHandler(configToRegister);

            log.trace("Adding device session for device {} to an active connection", configToRegister.getDeviceId());
            amqpsSessionHandler.setSession(this.connection.session());
            AmqpsSasTokenRenewalHandler amqpsSasTokenRenewalHandler = new AmqpsSasTokenRenewalHandler(amqpsCbsSessionHandler, amqpsSessionHandler);
            sasTokenRenewalHandlers.add(amqpsSasTokenRenewalHandler);
            try
            {
                amqpsSasTokenRenewalHandler.sendAuthenticationMessage(this.reactor);

                //Only add to this set if it was added successfully. Otherwise let it stay in the set to allow for retry
                configsRegisteredSuccessfully.add(configToRegister);
            }
            catch (TransportException e)
            {
                log.warn("Failed to send authentication message for device {}; will try again.", amqpsSasTokenRenewalHandler.amqpsSessionHandler.getDeviceId());

                //sas token renewal handler will be recreated when this function gets called again.
                amqpsSasTokenRenewalHandler.close();
                sasTokenRenewalHandlers.remove(amqpsSasTokenRenewalHandler);
                return;
            }

            configToRegister = configsToRegisterIterator.hasNext() ? configsToRegisterIterator.next() : null;
        }

        this.multiplexingClientsToRegister.removeAll(configsRegisteredSuccessfully);
    }

    // This function is called periodically from the onTimerTask reactor callback so that any newly registered device sessions
    // can be opened on a reactor thread instead of from one of our threads.
    private void checkForNewlyUnregisteredMultiplexedClientsToStop()
    {
        Iterator<ClientConfiguration> configsToUnregisterIterator = this.multiplexingClientsToUnregister.keySet().iterator();
        ClientConfiguration configToUnregister = configsToUnregisterIterator.hasNext() ? configsToUnregisterIterator.next() : null;
        Set<ClientConfiguration> configsUnregisteredSuccessfully = new HashSet<>();
        while (configToUnregister != null)
        {
            String deviceId = configToUnregister.getDeviceId();

            // Check if the device session still exists from a previous connection
            AmqpsSessionHandler amqpsSessionHandler = this.sessionHandlers.get(deviceId);

            // If a device session doesn't currently exist for this device identity
            if (amqpsSessionHandler == null)
            {
                log.warn("Attempted to remove device session for device {} from multiplexed connection, but device was not currently registered.", deviceId);
            }
            else
            {
                log.trace("Removing session handler for device {}", deviceId);
                this.sessionHandlers.remove(deviceId);

                // if the client being unregistered is doing so for reconnection purposes
                boolean isSessionReconnecting = this.multiplexingClientsToUnregister.get(configToUnregister);
                if (isSessionReconnecting)
                {
                    // save the session handler for later since it has state for what subscriptions the device had before this reconnection
                    this.reconnectingDeviceSessionHandlers.put(deviceId, amqpsSessionHandler);
                }
                else
                {
                    // remove the cached session handler since the device is being unregistered manually if it is cached
                    this.reconnectingDeviceSessionHandlers.remove(deviceId);
                }

                // Need to find the sas token renewal handler that is tied to this device
                AmqpsSasTokenRenewalHandler sasTokenRenewalHandlerToRemove = null;
                for (AmqpsSasTokenRenewalHandler existingSasTokenRenewalHandler : this.sasTokenRenewalHandlers)
                {
                    if (existingSasTokenRenewalHandler.amqpsSessionHandler.getDeviceId().equals(configToUnregister.getDeviceId()))
                    {
                        sasTokenRenewalHandlerToRemove = existingSasTokenRenewalHandler;

                        // Stop the sas token renewal handler from sending any more authentication messages on behalf of this device
                        log.trace("Closing sas token renewal handler for device {}", configToUnregister.getDeviceId());
                        sasTokenRenewalHandlerToRemove.close();
                        break;
                    }
                }

                if (sasTokenRenewalHandlerToRemove != null)
                {
                    this.sasTokenRenewalHandlers.remove(sasTokenRenewalHandlerToRemove);
                }

                log.debug("Closing device session for multiplexed device {}", configToUnregister.getDeviceId());
                amqpsSessionHandler.closeSession();
            }

            configsUnregisteredSuccessfully.add(configToUnregister);
            configToUnregister = configsToUnregisterIterator.hasNext() ? configsToUnregisterIterator.next() : null;
        }

        for (ClientConfiguration successfullyUnregisteredConfig : configsUnregisteredSuccessfully)
        {
            this.multiplexingClientsToUnregister.remove(successfullyUnregisteredConfig);
        }

        this.clientConfigurations.removeAll(configsUnregisteredSuccessfully);
    }

    private void initializeStateLatches()
    {
        this.closeReactorLatch = new CountDownLatch(REACTOR_COUNT);

        if (this.authenticationType == ClientConfiguration.AuthType.SAS_TOKEN)
        {
            log.trace("Initializing authentication link latch count to {}", CBS_SESSION_COUNT);
            this.authenticationSessionOpenedLatch = new CountDownLatch(CBS_SESSION_COUNT);
        }
        else
        {
            log.trace("Initializing authentication link latch count to 0 because x509 connections don't have authentication links");
            this.authenticationSessionOpenedLatch = new CountDownLatch(0);
        }

        this.deviceSessionsOpenedLatches = new ConcurrentHashMap<>();
        for (AmqpsSessionHandler sessionHandler : sessionHandlers.values())
        {
            String deviceId = sessionHandler.getDeviceId();
            log.trace("Initializing device session latch for device {}", deviceId);
            this.deviceSessionsOpenedLatches.put(deviceId, new CountDownLatch(1));
        }
    }

    @SuppressWarnings("SameParameterValue") // "isRetryable" is currently always "true", but can be "false" if required.
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

        synchronized (this.executorServiceLock)
        {
            if (executorService == null)
            {
                log.trace("Creating new executor service");
                executorService = Executors.newFixedThreadPool(1);
            }
        }

        this.reactor = createReactor();

        String runnerUniqueIdentifier = this.isMultiplexing
                ? "Multiplexed-" + this.transportUniqueIdentifier
                : this.clientConfiguration.getDeviceClientUniqueIdentifier();

        String reactorRunnerPrefix = this.hostName + "-" + runnerUniqueIdentifier + "-" + "Cnx" + this.connectionId;
        ReactorRunner reactorRunner = new ReactorRunner(
            this.reactor,
            this.listener,
            this.connectionId,
            reactorRunnerPrefix,
            "ConnectionOwner",
            this);

        executorService.submit(reactorRunner);
    }

    private void closeAsync()
    {
        log.trace("CloseAsync called for amqp connection");

        // This may be called before a connection or reactor have been established, so need to check the state
        if (this.connection == null && this.reactor == null) {
            // If both the connection and reactor were never initialized, then just release the latches to signal the end of the connection closing
            releaseLatch(authenticationSessionOpenedLatch);
            releaseDeviceSessionLatches();
            releaseLatch(closeReactorLatch);
        }
        else if (this.connection == null)
        {
            // If only the reactor was initialized, then just stop the reactor. OnReactorFinal will release the latches to signal the end of the connection closing
            this.reactor.stop();
        }
        else if (this.reactor == null)
        {
            // This should never happen as this block is only hit when the connection was initialized but its reactor was not
            log.warn("Connection was initialized without a reactor, connection is in an unknown state; closing connection anyways.");
            this.connection.close();
        }
        else if (this.connection.getLocalState() == EndpointState.CLOSED && this.connection.getRemoteState() == EndpointState.CLOSED)
        {
            log.trace("Closing amqp reactor since the connection was already closed");
            this.connection.getReactor().stop();
        }
        else
        {
            //client is initializing this close, so don't shut down the reactor yet
            log.trace("Closing amqp connection");
            this.connection.close();
        }
    }

    private void executorServicesCleanup()
    {
        synchronized (this.executorServiceLock)
        {
            if (this.executorService != null)
            {
                log.trace("Shutdown of executor service has started");
                this.executorService.shutdownNow();
                this.executorService = null;
                log.trace("Shutdown of executor service completed");
            }
        }
    }

    @Override
    public void onReactorClosedUnexpectedly()
    {
        // The reactor thread that executes the onConnectionLocalOpen type events has crashed unexpectedly. These latches
        // are normally closed on the onReactorFinal callback, but that callback will never happen since the thread that
        // would call it has crashed. Release these latches so that the cleanup logic can progress without waiting on these
        // latches
        releaseLatch(authenticationSessionOpenedLatch);
        releaseDeviceSessionLatches();
        releaseLatch(closeReactorLatch);
    }
}
