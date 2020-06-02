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
import com.microsoft.azure.sdk.iot.deps.transport.amqp.ErrorLoggingBaseHandler;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.message.impl.MessageImpl;
import org.apache.qpid.proton.reactor.FlowController;
import org.apache.qpid.proton.reactor.Handshaker;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.ReactorOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.*;

import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.*;
import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_METHODS;
import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_TWIN;

/**
 * An AMQPS IotHub connection between a device and an IoTHub. This class contains functionality for sending/receiving
 * a message, and logic to re-establish the connection with the IoTHub in case it gets lost.
 */
@Slf4j
public final class AmqpsIotHubConnection extends ErrorLoggingBaseHandler implements IotHubTransportConnection, SubscriptionMessageRequestSentCallback, AmqpsConnectionStateCallback
{
    // Timeouts
    private static final int MAX_WAIT_TO_CLOSE_CONNECTION = 60 * 1000; // 60 second timeout
    private static final int MAX_WAIT_TO_OPEN_CBS_LINKS = 20 * 1000; // 20 second timeout
    private static final int MAX_WAIT_TO_OPEN_WORKER_LINKS = 60 * 1000; // 60 second timeout
    private static final int MAX_WAIT_TO_TERMINATE_EXECUTOR = 30;

    // Web socket constants
    private static final String WEB_SOCKET_PATH = "/$iothub/websocket";
    private static final String WEB_SOCKET_SUB_PROTOCOL = "AMQPWSB10";
    private static final String WEB_SOCKET_QUERY = "iothub-no-client-cert=true";
    private static final int MAX_MESSAGE_PAYLOAD_SIZE = 256*1024; //max IoT Hub message size is 256 kb, so amqp websocket layer should buffer at most that much space
    private static final int WEB_SOCKET_PORT = 443;

    private static final int AMQP_PORT = 5671;
    private static final int REACTOR_COUNT = 1;
    private static final int CBS_SESSION_COUNT = 1; //even for multiplex scenarios

    // Message send constants
    private static final int SEND_MESSAGES_PERIOD_MILLIS = 50; //every 50 milliseconds, the method onTimerTask will fire to send queued messages
    private static final int MAX_MESSAGES_TO_SEND_PER_CALLBACK = 1000; //Max number of queued messages to send per periodic sending task

    public String connectionId;
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

    // States of outgoing messages, incoming messages, and outgoing subscriptions
    private final Queue<com.microsoft.azure.sdk.iot.device.Message> messagesToSend = new ConcurrentLinkedQueue<>();
    private final Map<Integer, com.microsoft.azure.sdk.iot.device.Message> inProgressMessages = new ConcurrentHashMap<>();
    private final Map<Integer, SubscriptionType> inProgressSubscriptionMessages = new ConcurrentHashMap<>();
    private final Map<com.microsoft.azure.sdk.iot.device.Message, AmqpsMessage> sendAckMessages = new ConcurrentHashMap<>();

    // Proton-j primitives and wrappers
    private Reactor reactor;
    private Connection connection;
    private AmqpsAuthenticationLinkHandler amqpsAuthenticationLinkHandler;
    private ArrayList<AmqpsSessionHandler> sessionHandlerList = new ArrayList<>();

    /**
     * Constructor to set up connection parameters using the {@link DeviceClientConfig}.
     *
     * @param config The {@link DeviceClientConfig} corresponding to the device associated with this {@link com.microsoft.azure.sdk.iot.device.DeviceClient}.
     */
    public AmqpsIotHubConnection(DeviceClientConfig config)
    {
        if (config == null)
        {
            throw new IllegalArgumentException("The DeviceClientConfig cannot be null.");
        }
        if (config.getIotHubHostname() == null || config.getIotHubHostname().length() == 0)
        {
            throw new IllegalArgumentException("hostName cannot be null or empty.");
        }
        if (config.getDeviceId() == null || config.getDeviceId().length() == 0)
        {
            throw new IllegalArgumentException("deviceID cannot be null or empty.");
        }
        if (config.getIotHubName() == null || config.getIotHubName().length() == 0)
        {
            throw new IllegalArgumentException("hubName cannot be null or empty.");
        }

        this.deviceClientConfig = config;

        this.hostName = this.chooseHostname();

        add(new Handshaker());
        add(new FlowController());

        this.state = IotHubConnectionStatus.DISCONNECTED;

        DeviceClientConfig.AuthType authType = config.getAuthenticationType();
        if (authType.equals(DeviceClientConfig.AuthType.SAS_TOKEN))
        {
            this.amqpsAuthenticationLinkHandler = new AmqpsAuthenticationLinkHandlerCBS();
        }
        else if (authType == DeviceClientConfig.AuthType.X509_CERTIFICATE)
        {
            this.amqpsAuthenticationLinkHandler = new AmqpsAuthenticationLinkHandlerX509();
        }
        else
        {
            throw new IllegalArgumentException("AMQP only supports SAS_TOKEN and X509 authentication types");
        }

        log.trace("AmqpsIotHubConnection object is created successfully and will use port {}", this.deviceClientConfig.isUseWebsocket() ? WEB_SOCKET_PORT : AMQP_PORT);
    }

    /**
     * Opens the {@link AmqpsIotHubConnection}.
     * <p>
     * This method will start the {@link Reactor}, set the connection to open and make it ready for sending.
     * </p>
     *
     * <p>
     * Do not call this method after calling close on this object, instead, create a whole new AmqpsIotHubConnection
     * object and open that instead.
     * </p>
     *
     * @throws TransportException If the reactor could not be initialized.
     */
    public void open(Queue<DeviceClientConfig> deviceClientConfigs, ScheduledExecutorService scheduledExecutorService) throws TransportException
    {
        this.log.debug("Opening amqp layer...");
        reconnectionScheduled = false;
        connectionId = UUID.randomUUID().toString();

        this.savedException = null;

        if (this.state == IotHubConnectionStatus.DISCONNECTED)
        {
            for (DeviceClientConfig clientConfig : deviceClientConfigs)
            {
                this.addDeviceSession(clientConfig, this, false);
            }

            initializeStateLatches();

            try
            {
                this.openAsync();

                this.log.trace("Waiting for authentication links to open...");
                boolean authenticationSessionOpenedTimedOut = !this.authenticationSessionOpenedLatch.await(MAX_WAIT_TO_OPEN_CBS_LINKS, TimeUnit.MILLISECONDS);

                if (this.savedException != null)
                {
                    throw this.savedException;
                }

                if (authenticationSessionOpenedTimedOut)
                {
                    closeConnectionWithException("Timed out waiting for authentication session to open", true);
                }

                this.log.trace("Waiting for device sessions to open...");
                boolean deviceSessionsOpenTimedOut = !this.deviceSessionsOpenedLatch.await(MAX_WAIT_TO_OPEN_WORKER_LINKS, TimeUnit.MILLISECONDS);

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
                log.error("Interrupted while waiting for links to open for AMQP connection", e);
                throw new TransportException("Interrupted while waiting for links to open for AMQP connection", e);
            }
        }

        this.state = IotHubConnectionStatus.CONNECTED;
        this.listener.onConnectionEstablished(this.connectionId);

        this.log.debug("Amqp connection opened successfully");
    }

    private void initializeStateLatches()
    {
        this.closeReactorLatch = new CountDownLatch(REACTOR_COUNT);

        if (deviceClientConfig.getAuthenticationProvider() instanceof IotHubSasTokenAuthenticationProvider)
        {
            this.log.trace("Initializing authentication link latch count to {}", CBS_SESSION_COUNT);
            this.authenticationSessionOpenedLatch = new CountDownLatch(CBS_SESSION_COUNT);
        }
        else
        {
            this.log.trace("Initializing authentication link latch count to 0 because x509 connections don't have authentication links");
            this.authenticationSessionOpenedLatch = new CountDownLatch(0);
        }

        int expectedDeviceSessionCount = sessionHandlerList.size();
        this.deviceSessionsOpenedLatch = new CountDownLatch(expectedDeviceSessionCount);
        this.log.trace("Initializing device session latch count to {}", expectedDeviceSessionCount);
    }

    private void closeConnectionWithException(String errorMessage, boolean isRetryable) throws TransportException
    {
        TransportException transportException = new TransportException(errorMessage);
        transportException.setRetryable(isRetryable);
        this.log.error(errorMessage, transportException);

        this.close();
        throw transportException;
    }

    /**
     * Private helper for open.
     * Starts the Proton reactor.
     */
    private void openAsync() throws TransportException
    {
        this.log.trace("OpenAsnyc called for amqp connection");
        if (this.reactor == null)
        {
            this.reactor = createReactor();
        }

        if (executorService == null)
        {
            executorService = Executors.newFixedThreadPool(1);
        }

        ReactorRunner reactorRunner = new ReactorRunner(new IotHubReactor(reactor), this.listener, this.connectionId);
        executorService.submit(reactorRunner);
    }

    /**
     * Closes the {@link AmqpsIotHubConnection}.
     * <p>
     * If the current connection is not closed, this function
     * will set the current state to closed and invalidate all connection related variables.
     * </p>
     *
     * @throws TransportException if it failed closing the iothub connection.
     */
    public void close() throws TransportException
    {
        this.log.debug("Shutting down amqp layer...");
        closeAsync();

        try
        {
            closeReactorLatch.await(MAX_WAIT_TO_CLOSE_CONNECTION, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            log.warn("Interrupted while closing proton reactor", e);
            throw new TransportException("Waited too long for the connection to close.", e);
        }

        this.executorServicesCleanup();

        this.log.trace("Amqp connection closed successfully");
        this.state = IotHubConnectionStatus.DISCONNECTED;
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

    /**
     * Private helper for close.
     * Closes the AmqpsIotHubConnection, the connection and stops the Proton reactor.
     */
    private void closeAsync()
    {
        this.log.debug("Closing amqp connection...");
        this.connection.close();

        for (AmqpsSessionHandler sessionHandler : this.sessionHandlerList)
        {
            sessionHandler.close();
        }

        this.log.trace("Closing amqp authentication links");
        this.amqpsAuthenticationLinkHandler.closeLinks();

        if (this.reactor != null)
        {
            this.reactor.stop();
        }

        log.trace("Proton reactor has been stopped");
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

        if (this.deviceClientConfig.getAuthenticationProvider() instanceof IotHubSasTokenAuthenticationProvider)
        {
            // Each device client may have a different sas token expiry time, so when multiplexing, there must be a handler per multiplexed device
            for (AmqpsSessionHandler amqpsSessionHandler : sessionHandlerList)
            {
                int millisecondDelayUntilNextAuthentication = amqpsSessionHandler.getDeviceClientConfig().getSasTokenAuthentication().getMillisecondsBeforeProactiveRenewal();
                reactor.schedule(millisecondDelayUntilNextAuthentication, new AmqpSasTokenRenewalHandler(this, amqpsSessionHandler));
            }
        }
    }

    @Override
    public void onReactorFinal(Event event)
    {
        releaseLatch(authenticationSessionOpenedLatch);
        releaseLatch(deviceSessionsOpenedLatch);
        releaseLatch(closeReactorLatch);

        this.reactor = null;
    }

    @Override
    public void onConnectionInit(Event event)
    {
        this.connection = event.getConnection();
        this.connection.setHostname(hostName);
        this.connection.open();

        //Create one session per multiplexed device, or just one session if not multiplexing
        for (AmqpsSessionHandler amqpsSessionHandler : this.sessionHandlerList)
        {
            amqpsSessionHandler.session = connection.session();
            amqpsSessionHandler.session.open();
        }
    }

    @Override
    public void onConnectionBound(Event event)
    {
        Transport transport = event.getTransport();

        if (this.deviceClientConfig.isUseWebsocket())
        {
            addWebsocketLayer(transport);
        }

        try
        {
            this.amqpsAuthenticationLinkHandler.setSslDomain(transport, this.deviceClientConfig.getAuthenticationProvider().getSSLContext());
        }
        catch (IOException e)
        {
            this.savedException = new TransportException(e);
            log.error("Encountered an exception while setting ssl domain for the amqp connection", this.savedException);
        }

        // Adding proxy layer needs to be done after sending SSL message
        if (this.deviceClientConfig.getProxySettings() != null)
        {
            this.log.debug("Proxy settings present, adding proxy layer to amqp connection");
            addProxyLayer(transport, event.getConnection().getHostname() + ":" + WEB_SOCKET_PORT);
        }
    }

    private void addWebsocketLayer(Transport transport)
    {
        WebSocketImpl webSocket = new WebSocketImpl(MAX_MESSAGE_PAYLOAD_SIZE);
        webSocket.configure(this.hostName, WEB_SOCKET_PATH, WEB_SOCKET_QUERY, WEB_SOCKET_PORT, WEB_SOCKET_SUB_PROTOCOL, null, null);
        ((TransportInternal) transport).addTransportLayer(webSocket);
    }

    private void addProxyLayer(Transport transport, String hostName)
    {
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

    @Override
    public void onDelivery(Event event)
    {
        Link link = event.getLink();

        if (link instanceof Sender)
        {
            handleMessageAcknowledgmentDelivery(event.getDelivery(), (Sender) link);
        }
        else if (link instanceof Receiver)
        {
            handleMessageDelivery((Receiver) link);
        }
    }

    private void handleMessageAcknowledgmentDelivery(Delivery messageAcknowledgement, Sender link)
    {
        while (messageAcknowledgement != null && !messageAcknowledgement.isSettled() && messageAcknowledgement.getRemoteState() != null)
        {
            DeliveryState remoteState = messageAcknowledgement.getRemoteState();

            int deliveryTag = Integer.valueOf(new String(messageAcknowledgement.getTag()));

            if (!link.getSource().getAddress().equalsIgnoreCase(AmqpsAuthenticationLinkHandlerCBS.SENDER_LINK_ENDPOINT_PATH))
            {
                this.log.trace("Amqp delivery received that acknowledged a sent message with delivery tag {}", deliveryTag);

                this.log.trace("Checking if amqp in progress messages contains delivery tag {}", deliveryTag);
                if (this.inProgressMessages.containsKey(deliveryTag))
                {
                    this.log.trace("Amqp in progress messages does contain delivery tag {}", deliveryTag);

                    com.microsoft.azure.sdk.iot.device.Message acknowledgedMessage = inProgressMessages.remove(deliveryTag);

                    if (remoteState instanceof Accepted)
                    {
                        this.log.trace("AMQP connection received Accepted acknowledgement for iot hub message ({})", acknowledgedMessage);

                        this.listener.onMessageSent(acknowledgedMessage, null);
                    }
                    else if (remoteState instanceof Rejected)
                    {
                        this.log.trace("AMQP connection received Rejected acknowledgement for iot hub message  ({})", acknowledgedMessage);

                        TransportException transportException;
                        ErrorCondition errorCondition = ((Rejected) remoteState).getError();
                        if (errorCondition != null && errorCondition.getCondition() != null)
                        {
                            String errorCode = errorCondition.getCondition().toString();
                            String errorDescription = "";
                            if (errorCondition.getDescription() != null)
                            {
                                errorDescription = errorCondition.getDescription();
                            }

                            transportException = AmqpsExceptionTranslator.convertToAmqpException(errorCode, errorDescription);
                        }
                        else
                        {
                            transportException = new TransportException("IotHub rejected the message");
                        }

                        this.listener.onMessageSent(inProgressMessages.remove(deliveryTag), transportException);

                    }
                    else if (remoteState instanceof Modified || remoteState instanceof Released || remoteState instanceof Received)
                    {
                        this.log.trace("AMQP connection received Modified, Released or Received acknowledgement for iot hub message  ({})", acknowledgedMessage);

                        TransportException transportException = new TransportException("IotHub responded to message with Modified, Received or Released; message needs to be re-delivered");
                        transportException.setRetryable(true);
                        this.listener.onMessageSent(inProgressMessages.remove(deliveryTag), transportException);
                    }
                }
                else if (this.inProgressSubscriptionMessages.containsKey(deliveryTag))
                {
                    SubscriptionType subscriptionType = this.inProgressSubscriptionMessages.remove(deliveryTag);
                    log.debug("Successfully sent amqp subscription message of type {}", subscriptionType);
                }
                else
                {
                    this.log.warn("Unable to correlate acknowledgement with delivery tag {} to a sent message, ignoring it", deliveryTag);
                    this.listener.onMessageReceived(null, new TransportException("Received response from service about a message that this client did not send"));
                }
            }
            else
            {
                this.log.trace("Amqp delivery received that acknowledged a sent authentication message");
            }

            messageAcknowledgement.free();

            // get next delivery to handle, or null if there isn't one
            messageAcknowledgement = link.head();
        }
    }

    private void handleMessageDelivery(Receiver link)
    {
        this.log.trace("Amqp delivery received on a receiver link, checking receiver links for delivered message");
        AmqpsMessage amqpsMessage = this.getMessageFromReceiverLink(link.getName());

        if (amqpsMessage != null)
        {
            if (amqpsMessage.getAmqpsMessageType() == MessageType.CBS_AUTHENTICATION)
            {
                //Received message was a CBS message, and was already handled within the cbs handler during retrieval
                return;
            }

            this.log.debug("Handling a received message with amqp message correlation id {}", amqpsMessage.getCorrelationId());
            IotHubTransportMessage iotHubTransportMessage = this.convertFromProton(amqpsMessage, amqpsMessage.getDeviceClientConfig());

            if (iotHubTransportMessage == null)
            {
                // Should never happen; message type was not telemetry, twin, methods, or CBS
                log.warn("No handler found for received message with amqp correlation id {}, ignoring it", amqpsMessage.getCorrelationId());
                return;
            }

            if (iotHubTransportMessage.getMessageCallback() == null)
            {
                log.warn("No callback found for received message with amqp correlation id {}, ignoring it", amqpsMessage.getCorrelationId());
                return;
            }

            this.log.trace("Adding received message to the amqp message map to be acknowledged later ({})", iotHubTransportMessage);

            this.sendAckMessages.put(iotHubTransportMessage, amqpsMessage);

            this.listener.onMessageReceived(iotHubTransportMessage, null);
        }
        else
        {
            log.warn("onDelivery called on receiver link, but no message was found on any receiver link");
        }
    }

    @Override
    public void onLinkInit(Event event)
    {
        Link link = event.getLink();

        if (!this.isAuthenticationOpened())
        {
            // First link to be initialized will be the auth links
            this.amqpsAuthenticationLinkHandler.initLink(link);
        }
        else
        {
            for (AmqpsSessionHandler sessionHandler : this.sessionHandlerList)
            {
                sessionHandler.initLink(link);
            }
        }
    }

    private void sendQueuedMessages()
    {
        int messagesAttemptedToBeProcessed = 0;
        int lastDeliveryTag = 0;
        com.microsoft.azure.sdk.iot.device.Message message = messagesToSend.poll();
        while (message != null && messagesAttemptedToBeProcessed < MAX_MESSAGES_TO_SEND_PER_CALLBACK && lastDeliveryTag >= 0)
        {
            if (!subscriptionChangeHandler(message))
            {
                messagesAttemptedToBeProcessed++;
                lastDeliveryTag = sendQueuedMessage(message);

                if (lastDeliveryTag == -1)
                {
                    //message failed to send, likely due to lack of link credit available. Re-queue and try again later
                    this.log.trace("Amqp message failed to send, adding it back to messages to send queue ({})", message);
                    messagesToSend.add(message);
                }
            }

            message = messagesToSend.poll();
        }

        if (message != null)
        {
            //message was polled out of list, but loop exited from processing too many messages before it could process this message, so re-queue it for later
            messagesToSend.add(message);
        }
    }

    private int sendQueuedMessage(com.microsoft.azure.sdk.iot.device.Message message)
    {
        int lastDeliveryTag = -1;
        MessageImpl protonMessage = this.convertToProton(message);

        if (protonMessage == null)
        {
            this.log.warn("No handler found for message conversion. Abandoning message ({})", message);
            return lastDeliveryTag;
        }
        else
        {
            this.log.trace("Sending message over amqp ({})", message);

            MessageType messageType = message.getMessageType();
            for (AmqpsSessionHandler sessionHandler : this.sessionHandlerList)
            {
                lastDeliveryTag = sessionHandler.sendMessage(protonMessage, messageType, message.getConnectionDeviceId());

                if (lastDeliveryTag != -1)
                {
                    //Message was sent by its correct session, no need to keep looping to find the right session
                    this.inProgressMessages.put(lastDeliveryTag, message);
                    this.log.trace("Amqp message was sent, adding amqp delivery tag {} to in progress messages ({})", lastDeliveryTag, message);
                    break;
                }
            }
        }

        return lastDeliveryTag;
    }

    /**
     * Event handler for the link remote open event. This signifies that the
     * {@link org.apache.qpid.proton.reactor.Reactor} is ready, so we set the connection to CONNECTED.
     *
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkRemoteOpen(Event event)
    {
        Link remotelyOpenedLink = event.getLink();
        if (this.amqpsAuthenticationLinkHandler.onLinkRemoteOpen(remotelyOpenedLink))
        {
            if (this.amqpsAuthenticationLinkHandler.isOpen())
            {
                this.onAuthenticationSessionOpened();

                for (AmqpsSessionHandler sessionHandler : this.sessionHandlerList)
                {
                    try
                    {
                        this.authenticate(sessionHandler);
                    }
                    catch (TransportException e)
                    {
                        log.warn("Failed to authenticate a device", e);
                    }
                }
            }
        }

        if (this.isAuthenticationOpened())
        {
            for (AmqpsSessionHandler sessionHandler : this.sessionHandlerList)
            {
                sessionHandler.onLinkRemoteOpen(remotelyOpenedLink, this);
            }
        }
    }

    /**
     * Event handler for the link remote close event. This triggers reconnection attempts until successful.
     * Both sender and receiver links closing trigger this event, so we only handle one of them,
     * since the other is redundant.
     *
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkRemoteClose(Event event)
    {
        super.onLinkRemoteClose(event);

        if (event.getLink().getLocalState() == EndpointState.ACTIVE)
        {
            String linkName = event.getLink().getName();

            for (AmqpsSessionHandler sessionHandler : this.sessionHandlerList)
            {
                sessionHandler.onLinkRemoteClose(linkName);
            }

            this.amqpsAuthenticationLinkHandler.onLinkRemoteClose(linkName);

            log.trace("onLinkRemoteClose fired by proton, setting amqp connection state as DISCONNECTED");
            this.state = IotHubConnectionStatus.DISCONNECTED;

            //Codes_SRS_AMQPSIOTHUBCONNECTION_34_061 [If the provided event object's transport holds a remote error condition object, this function shall report the associated TransportException to this object's listeners.]
            this.savedException = getTransportExceptionFromEvent(event);

            this.scheduleReconnection(this.savedException);
        }
    }

    /**
     * Event handler for the transport error event. This triggers reconnection attempts until successful.
     *
     * @param event The Proton Event object.
     */
    @Override
    public void onTransportError(Event event)
    {
        super.onTransportError(event);
        this.state = IotHubConnectionStatus.DISCONNECTED;

        //Codes_SRS_AMQPSIOTHUBCONNECTION_34_060 [If the provided event object's transport holds an error condition object, this function shall report the associated TransportException to this object's listeners.]
        this.savedException = getTransportExceptionFromEvent(event);

        this.scheduleReconnection(this.savedException);
    }

    /**
     * Create a Proton reactor
     *
     * @return the Proton reactor
     * @throws TransportException if Proton throws
     */
    private Reactor createReactor() throws TransportException
    {
        try
        {
            if (this.deviceClientConfig.getAuthenticationType() == DeviceClientConfig.AuthType.X509_CERTIFICATE)
            {
                //Codes_SRS_AMQPSIOTHUBCONNECTION_34_053: [If the config is using x509 Authentication, the created Proton reactor shall not have SASL enabled by default.]
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

    @Override
    public void onTimerTask(Event event)
    {
        sendQueuedMessages();

        event.getReactor().schedule(SEND_MESSAGES_PERIOD_MILLIS, this);
    }

    @Override
    public void setListener(IotHubListener listener) throws IllegalArgumentException
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("listener cannot be null");
        }

        this.listener = listener;
    }

    @Override
    public IotHubStatusCode sendMessage(com.microsoft.azure.sdk.iot.device.Message message) throws TransportException
    {
        this.log.trace("Adding message to amqp message queue to be sent later ({})", message);
        messagesToSend.add(message);
        return IotHubStatusCode.OK;
    }

    /**
     * Sends the Ack for the provided message with the result
     *
     * @param message the message to acknowledge
     * @param result  the result to attach to the ack (COMPLETE, ABANDON, or REJECT)
     * @return true if the ack was sent successfully, and false otherwise
     */
    @Override
    public boolean sendMessageResult(com.microsoft.azure.sdk.iot.device.Message message, IotHubMessageResult result)
    {
        if (this.state != IotHubConnectionStatus.CONNECTED)
        {
            log.warn("Unable to send message acknowledgement because amqp connection is not open");
            return false;
        }

        this.log.trace("Sending amqp acknowledgement for iothub message ({}) with result {}", message, result);

        if (this.sendAckMessages.containsKey(message))
        {
            AmqpsMessage amqpsMessage = sendAckMessages.get(message);

            switch (result)
            {
                case ABANDON:
                    amqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.ABANDON);
                    break;
                case REJECT:
                    amqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.REJECT);
                    break;
                case COMPLETE:
                    amqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.COMPLETE);
                    break;
                default:
                    log.warn("Invalid IoT Hub message result {}", result.name());
                    return false;
            }

            this.log.trace("Removing message from amqp map of messages to acknowledge ({})", message);
            this.sendAckMessages.remove(message);
            return true;
        }
        else
        {
            this.log.error("Amqp connection cannot send ack for this iot hub message because it has no mapping from it to any amqp message ({})", message);
        }

        return false;
    }

    @Override
    public String getConnectionId()
    {
        return this.connectionId;
    }

    /**
     * Schedules a thread to start the reconnection process for AMQP
     *
     * @param throwable the reason why the reconnection needs to take place, for reporting purposes
     */
    private void scheduleReconnection(Throwable throwable)
    {
        this.log.warn("Amqp connection was closed, creating a thread to notify transport layer", throwable);
        if (!reconnectionScheduled)
        {
            reconnectionScheduled = true;
            ReconnectionNotifier.notifyDisconnectAsync(throwable, this.listener, this.connectionId);
        }
    }

    private String chooseHostname()
    {
        String gatewayHostname = this.deviceClientConfig.getGatewayHostname();
        if (gatewayHostname != null && !gatewayHostname.isEmpty())
        {
            log.debug("Gateway hostname was present in config, connecting to gateway rather than directly to hub");
            return gatewayHostname;
        }

        log.debug("No gateway hostname was present in config, connecting directly to hub");
        return this.deviceClientConfig.getIotHubHostname();
    }

    private ErrorCondition getErrorConditionFromEndpoint(Endpoint endpoint)
    {
        return endpoint.getCondition() != null && endpoint.getCondition().getCondition() != null ? endpoint.getCondition() : endpoint.getRemoteCondition();
    }

    private TransportException getTransportExceptionFromProtonEndpoints(Endpoint... endpoints)
    {
        for (Endpoint endpoint : endpoints)
        {
            if (endpoint == null)
            {
                continue;
            }

            ErrorCondition errorCondition = getErrorConditionFromEndpoint(endpoint);
            if (errorCondition == null || errorCondition.getCondition() == null)
            {
                continue;
            }

            String error = errorCondition.getCondition().toString();
            String errorDescription = errorCondition.getDescription();

            return AmqpsExceptionTranslator.convertToAmqpException(error, errorDescription);
        }

        return null;
    }

    /**
     * Derive the transport exception from the provided event, defaulting to a generic, retryable TransportException
     *
     * @param event the event context
     * @return the transport exception derived from the provided event
     */
    private TransportException getTransportExceptionFromEvent(Event event)
    {
        TransportException transportException = getTransportExceptionFromProtonEndpoints(event.getSender(), event.getReceiver(), event.getConnection(), event.getTransport(), event.getSession(), event.getLink());

        if (transportException == null)
        {
            transportException = new TransportException("Unknown transport exception occurred");
            transportException.setRetryable(true);
        }

        return transportException;
    }

    private boolean subscriptionChangeHandler(com.microsoft.azure.sdk.iot.device.Message message)
    {
        boolean handled = false;
        if (message.getMessageType() != null)
        {
            switch (message.getMessageType())
            {
                case DEVICE_METHODS:
                    if (((IotHubTransportMessage) message).getDeviceOperationType() == DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST)
                    {
                        this.subscribeDeviceToMessageType(DEVICE_METHODS, message.getConnectionDeviceId());
                        this.listener.onMessageSent(message, null);
                        handled = true;
                    }

                    break;
                case DEVICE_TWIN:
                    if (((IotHubTransportMessage) message).getDeviceOperationType() == DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST)
                    {
                        //TODO: unsubscribe desired property from application
                        //this.amqpSessionManager to sever the connection
                        //twinSubscribed = false;
                    }
                    else if (((IotHubTransportMessage) message).getDeviceOperationType() == DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST)
                    {
                        this.subscribeDeviceToMessageType(DEVICE_TWIN, message.getConnectionDeviceId());
                        this.listener.onMessageSent(message, null);
                        handled = true;
                    }
                    break;
                default:
                    break;
            }
        }

        return handled;
    }

    private void releaseLatch(CountDownLatch latch)
    {
        //Countdown the latch until it hits 0
        for (int i = 0; i < latch.getCount(); i++)
        {
            latch.countDown();
        }
    }

    @Override
    public void onSubscriptionMessageSent(int deliveryTag, SubscriptionType subscriptionType)
    {
        this.inProgressSubscriptionMessages.put(deliveryTag, subscriptionType);
    }

    @Override
    public void onDeviceSessionOpened(String deviceId)
    {
        this.deviceSessionsOpenedLatch.countDown();
    }

    @Override
    public void onAuthenticationSessionOpened() {
        this.authenticationSessionOpenedLatch.countDown();
    }

    final void addDeviceSession(DeviceClientConfig deviceClientConfig, SubscriptionMessageRequestSentCallback subscriptionMessageRequestSentCallback, boolean afterOpen) throws TransportException {
        if (deviceClientConfig == null)
        {
            throw new IllegalArgumentException("deviceClientConfig cannot be null.");
        }

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
            amqpsSessionHandler = new AmqpsSessionHandler(deviceClientConfig, subscriptionMessageRequestSentCallback);
            this.sessionHandlerList.add(amqpsSessionHandler);
        }

        if (afterOpen)
        {
            amqpsSessionHandler.session = this.connection.session();
            amqpsSessionHandler.session.open();
        }
    }

    public void authenticate(AmqpsSessionHandler sessionHandler) throws TransportException
    {
        UUID authenticationCorrelationId = UUID.randomUUID();
        sessionHandler.cbsCorrelationIdList.add(authenticationCorrelationId);
        log.debug("Sending authentication message with correlationId {} to authenticate device {}", authenticationCorrelationId, sessionHandler.getDeviceId());
        this.amqpsAuthenticationLinkHandler.authenticate(sessionHandler.getDeviceClientConfig(), authenticationCorrelationId);
    }

    public void authenticate() throws TransportException
    {
        for (AmqpsSessionHandler sessionHandler : this.sessionHandlerList)
        {
            authenticate(sessionHandler);
        }
    }

    protected void subscribeDeviceToMessageType(MessageType messageType, String deviceId)
    {
        this.log.trace("Subscribing to {}", messageType);
        for (AmqpsSessionHandler sessionHandler : this.sessionHandlerList)
        {
            if (sessionHandler.getDeviceId().equals(deviceId))
            {
                sessionHandler.subscribeToMessageType(messageType);
                return;
            }
        }
    }

    @Override
    public void onSessionRemoteOpen(Event e)
    {
        if (this.amqpsAuthenticationLinkHandler instanceof AmqpsAuthenticationLinkHandlerCBS)
        {
            this.amqpsAuthenticationLinkHandler.openLinks(e.getSession());
        }
        else
        {
            //For x509 auth, there is no CBS session to start up. Once a session is open, just open the worker links
            for (AmqpsSessionHandler sessionHandler : this.sessionHandlerList)
            {
                sessionHandler.openLinks();
            }
        }
    }

    /**
     * Delegate the onDelivery call to device operation objects.
     * Loop through the device operation list and find the receiver `
     * object by link name.
     *
     * @param linkName the link name to identify the receiver.
     *
     * @return AmqpsMessage if the receiver found the received
     *         message, otherwise null.
     */
    AmqpsMessage getMessageFromReceiverLink(String linkName)
    {
        AmqpsMessage amqpsMessage = null;

        if (linkName.startsWith(AmqpsAuthenticationLinkHandlerCBS.RECEIVER_LINK_TAG_PREFIX) || linkName.startsWith(AmqpsAuthenticationLinkHandlerCBS.SENDER_LINK_TAG_PREFIX))
        {
            amqpsMessage = this.amqpsAuthenticationLinkHandler.getMessageFromReceiverLink(linkName);

            for (AmqpsSessionHandler sessionHandler : this.sessionHandlerList)
            {
                if (sessionHandler.handleAuthenticationMessage(amqpsMessage, this.amqpsAuthenticationLinkHandler))
                {
                    log.debug("Successfully authenticated device {}", sessionHandler.getDeviceId());
                    sessionHandler.openLinks();
                    break;
                }
            }
        }
        else
        {
            for (AmqpsSessionHandler sessionHandler : this.sessionHandlerList)
            {
                amqpsMessage = sessionHandler.getMessageFromReceiverLink(linkName);
                if (amqpsMessage != null)
                {
                    break;
                }
            }
        }

        return amqpsMessage;
    }

    /**
     * Get the status of the authentication links.
     *
     * @return Boolean true if all link open, false otherwise.
     */
    boolean isAuthenticationOpened()
    {
        return (this.amqpsAuthenticationLinkHandler.isOpen());
    }

    /**
     * Find the converter to convert from IoTHub message to Proton
     * message.
     *
     * @param message the message to convert.
     *
     * @return AmqpsConvertToProtonReturnValue the result of the
     *         conversion containing the Proton message.
     */
    MessageImpl convertToProton(com.microsoft.azure.sdk.iot.device.Message message)
    {
        MessageImpl protonMessage = null;

        for (AmqpsSessionHandler sessionHandler : this.sessionHandlerList)
        {
            protonMessage = sessionHandler.convertToProton(message);
            if (protonMessage != null)
            {
                break;
            }
        }

        return protonMessage;
    }

    /**
     * Find the converter to convert Proton message to IoTHub
     * message. Loop through the managed devices and find the
     * converter.
     *
     * @param amqpsMessage the Proton message to convert.
     * @param deviceClientConfig the device client configuration for
     *                           add identification data to the
     *                           message.
     *
     * @return the result of the conversion containing the IoTHub message.
     * @throws TransportException if converting the message fails
     */
    IotHubTransportMessage convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig)
    {
        IotHubTransportMessage iotHubTransportMessage = null;

        for (AmqpsSessionHandler sessionHandler : this.sessionHandlerList)
        {
            iotHubTransportMessage = sessionHandler.convertFromProton(amqpsMessage, deviceClientConfig);
            if (iotHubTransportMessage != null)
            {
                break;
            }
        }

        return iotHubTransportMessage;
    }

    /**
     * Class which runs the reactor.
     */
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
