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
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.FlowController;
import org.apache.qpid.proton.reactor.Handshaker;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.ReactorOptions;

import java.io.IOException;
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
public final class AmqpsIotHubConnection extends ErrorLoggingBaseHandler implements IotHubTransportConnection
{
    private static final int MAX_WAIT_TO_CLOSE_CONNECTION = 60 * 1000; // 60 second timeout
    private static final int MAX_WAIT_TO_OPEN_CBS_LINKS = 20 * 1000; // 20 second timeout
    private static final int MAX_WAIT_TO_OPEN_WORKER_LINKS = 60 * 1000; // 60 second timeout
    private static final int MAX_WAIT_TO_TERMINATE_EXECUTOR = 30;
    private static final int SEND_MESSAGES_PERIOD_MILLIS = 50; //every 50 seconds, the method onTimerTask will fire to send queued messages
    /**
     * The {@link Delivery} tag.
     */
    private static final String WEB_SOCKET_PATH = "/$iothub/websocket";
    private static final String WEB_SOCKET_SUB_PROTOCOL = "AMQPWSB10";
    private static final String WEBSOCKET_QUERY = "iothub-no-client-cert=true";
    private static final int WEBSOCKET_PORT = 443;
    private static final int AMQP_PORT = 5671;
    private static final int AMQP_WEB_SOCKET_PORT = 443;
    private static final int REACTOR_COUNT = 1;
    private static final int CBS_LINK_COUNT = 2; //even for multiplex scenarios
    private final static String APPLICATION_PROPERTY_STATUS_CODE = "status-code";
    private final static String APPLICATION_PROPERTY_STATUS_DESCRIPTION = "status-description";
    //sending messages is done on reactor thread, but we don't want to hog that thread indefinitely, so there is a limit
    // on how many messages to send per reactor callback
    private final static int MAX_MESSAGES_TO_SEND_PER_CALLBACK = 1000;
    private final Boolean useWebSockets;
    private final Map<Integer, com.microsoft.azure.sdk.iot.device.Message> inProgressMessages = new ConcurrentHashMap<>();
    private final Map<com.microsoft.azure.sdk.iot.device.Message, AmqpsMessage> sendAckMessages = new ConcurrentHashMap<>();
    public String connectionId;
    public AmqpsSessionManager amqpsSessionManager;
    //Used to track if the full connection is authenticated. This means cbs auth messages have received 200 from service for each device's sas auth
    boolean isAuthenticated;
    private IotHubConnectionStatus state;
    private Connection connection;
    private String hostName;
    private DeviceClientConfig deviceClientConfig;
    private IotHubListener listener;
    //When the connection is lost for any reason, a thread is spawned to notify the Transport layer to re-establish
    // this connection. The original thread completes its shutdown. That thread should only be spawned once.
    private boolean reconnectionScheduled = false;
    private ExecutorService executorService;
    private AmqpSasTokenRenewalHandler sasTokenRenewalHandler;
    private CountDownLatch authenticationLinkOpenLatch;
    private CountDownLatch workerLinksOpenLatch;
    private CountDownLatch cbsLinkAuthorizedLatch;
    private CountDownLatch closeReactorLatch;
    private Reactor reactor;
    private TransportException savedException;
    private Queue<com.microsoft.azure.sdk.iot.device.Message> messagesToSend = new ConcurrentLinkedQueue<>();

    /**
     * Constructor to set up connection parameters using the {@link DeviceClientConfig}.
     *
     * @param config The {@link DeviceClientConfig} corresponding to the device associated with this {@link com.microsoft.azure.sdk.iot.device.DeviceClient}.
     */
    public AmqpsIotHubConnection(DeviceClientConfig config)
    {
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
        // any of the parameters of the configuration is null or empty.]
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

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_002: [The constructor shall save the configuration into private member variables.]
        this.deviceClientConfig = config;

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_017: [The constructor shall set the AMQP socket port using the configuration.]
        this.useWebSockets = this.deviceClientConfig.isUseWebsocket();
        if (useWebSockets)
        {
            this.hostName = String.format("%s:%d", this.chooseHostname(), AMQP_WEB_SOCKET_PORT);
        }
        else
        {
            this.hostName = String.format("%s:%d", this.chooseHostname(), AMQP_PORT);
        }

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_004: [The constructor shall initialize a new Handshaker
        // (Proton) object to handle communication handshake.]
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_005: [The constructor shall initialize a new FlowController
        // (Proton) object to handle communication flow.]
        add(new Handshaker());
        add(new FlowController());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_006: [The constructor shall set its state to DISCONNECTED.]
        this.state = IotHubConnectionStatus.DISCONNECTED;

        this.amqpsSessionManager = new AmqpsSessionManager(this.deviceClientConfig);

        log.trace("AmqpsIotHubConnection object is created successfully and will use port {}", useWebSockets ? AMQP_WEB_SOCKET_PORT : AMQP_PORT);
    }

    /**
     * Creates a new DeviceOperation using the given configuration..
     *
     * @param deviceClientConfig the device configuration to add.
     * @throws TransportException if adding the device fails
     */
    public void addDeviceOperationSession(DeviceClientConfig deviceClientConfig) throws TransportException
    {
        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_018: [The function shall do nothing if the deviceClientConfig parameter is null.]
        if (deviceClientConfig != null)
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_12_019: [The function shall call AmqpsSessionManager.addDeviceOperationSession with the given deviceClientConfig.]
            this.amqpsSessionManager.addDeviceOperationSession(deviceClientConfig);

            this.log.trace("Added device to session list");
        }
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
        this.log.debug("Opening AMQP connection");
        reconnectionScheduled = false;
        connectionId = UUID.randomUUID().toString();

        this.isAuthenticated = false;

        this.savedException = null;

        this.sasTokenRenewalHandler = new AmqpSasTokenRenewalHandler(this.amqpsSessionManager, this.deviceClientConfig);


        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_007: [If the AMQPS connection is already open, the function shall do nothing.]
        if (this.state == IotHubConnectionStatus.DISCONNECTED)
        {
            if (deviceClientConfigs.size() > 1)
            {
                deviceClientConfigs.remove();
                while (!deviceClientConfigs.isEmpty())
                {
                    this.addDeviceOperationSession(deviceClientConfigs.remove());
                }
            }

            initializeStateLatches(deviceClientConfigs.size());

            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_010: [The function shall wait for the reactor to be ready and for
            // enough link credit to become available.]
            try
            {
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_009: [The function shall trigger the Reactor (Proton) to begin running.]
                this.openAsync();

                // Codes_SRS_AMQPSIOTHUBCONNECTION_12_059: [The function shall call await on open latch.]
                this.log.trace("Waiting for authentication links to open...");
                boolean authenticationLinksOpenTimedOut = !this.authenticationLinkOpenLatch.await(MAX_WAIT_TO_OPEN_CBS_LINKS, TimeUnit.MILLISECONDS);

                if (this.savedException != null)
                {
                    throw this.savedException;
                }

                if (authenticationLinksOpenTimedOut)
                {
                    closeConnectionWithException("Timed out waiting for authentication links to open", true);
                }

                this.log.trace("Waiting for worker links to open...");
                boolean workerLinksOpenTimedOut = !this.workerLinksOpenLatch.await(MAX_WAIT_TO_OPEN_WORKER_LINKS, TimeUnit.MILLISECONDS);

                if (this.savedException != null)
                {
                    throw this.savedException;
                }

                if (workerLinksOpenTimedOut)
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

        this.listener.onConnectionEstablished(this.connectionId);

        this.state = IotHubConnectionStatus.CONNECTED;

        this.log.debug("AMQP connection opened successfully");
    }

    /**
     * @param deviceCount The number of devices involved in this connection. For Multiplexed scenarios, this should be greater than 1.
     *                    For non-multiplexed scenarios, this should be exactly 1.
     */
    private void initializeStateLatches(int deviceCount)
    {
        this.closeReactorLatch = new CountDownLatch(REACTOR_COUNT);

        if (deviceClientConfig.getAuthenticationProvider() instanceof IotHubSasTokenAuthenticationProvider)
        {
            this.log.trace("Initializing authentication link latch count to {}", CBS_LINK_COUNT);
            this.authenticationLinkOpenLatch = new CountDownLatch(CBS_LINK_COUNT);
        }
        else
        {
            this.log.trace("Initializing authentication link latch count to 0");
            //x509 connections don't have authentication links to open
            this.authenticationLinkOpenLatch = new CountDownLatch(0);
        }

        //Each session has a device, and each device can have 2, 4, or 6 worker links depending on if that device is subscribed to twin and methods or not
        int expectedWorkerLinkCount = this.amqpsSessionManager.getExpectedWorkerLinkCount();
        this.workerLinksOpenLatch = new CountDownLatch(expectedWorkerLinkCount);
        this.log.trace("Initializing worker link latch count to {}", expectedWorkerLinkCount);

        //expect one cbs 200 per device
        this.cbsLinkAuthorizedLatch = new CountDownLatch(deviceCount);
        this.log.trace("Initializing authentication links authorized latch count to {}", deviceCount);
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
        this.log.trace("OpenAsnyc called for AMQP connection");
        if (this.reactor == null)
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_12_003: [The constructor shall throw TransportException if the Proton reactor creation failed.]
            this.reactor = createReactor();
        }

        if (executorService == null)
        {
            executorService = Executors.newFixedThreadPool(1);
        }

        IotHubReactor iotHubReactor = new IotHubReactor(reactor);
        ReactorRunner reactorRunner = new ReactorRunner(iotHubReactor, this.listener, this.connectionId);
        executorService.submit(reactorRunner);
    }

    /**
     * Starts the authentication by calling the AmqpsSessionManager.
     *
     * @throws TransportException if authentication open throws.
     */
    public void authenticate() throws TransportException
    {
        this.log.trace("Authenticate called on amqp connection");

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_020: [The function shall do nothing if the authentication is already open.]
        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_021: [The function shall call AmqpsSessionManager.authenticate.]
        this.amqpsSessionManager.authenticate();
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
        this.log.debug("Closing amqp connection...");
        closeAsync();

        try
        {
            closeReactorLatch.await(MAX_WAIT_TO_CLOSE_CONNECTION, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_12_004: [The function shall TransportException throws if the waitLock throws.]
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
                // Codes_SRS_AMQPSIOTHUBCONNECTION_12_005: [The function shall throw TransportException if the executor shutdown is interrupted.]
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
     * Closes the AmqpsSessionManager, the connection and stops the Proton reactor.
     */
    private void closeAsync()
    {
        this.log.trace("CloseAsync called on amqp connection");
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_013: [The function shall closeNow the AmqpsSessionManager and the AMQP connection.]
        if (this.amqpsSessionManager != null)
        {
            this.amqpsSessionManager.closeNow();
        }

        if (this.connection != null)
        {
            this.connection.close();
        }

        // Codes_SRS_AMQPSIOTHUBCONNECTION_34_014: [If this object's proton reactor is not null, this function shall stop the Proton reactor.]
        if (this.reactor != null)
        {
            this.reactor.stop();
        }

        log.trace("Proton reactor has been stopped");
    }

    private Integer sendMessage(AmqpsConvertToProtonReturnValue protonReturnValue, String deviceId) throws TransportException
    {
        Integer deliveryTag = -1;

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_015: [If the state of the connection is DISCONNECTED or there is not enough
        // credit, the function shall return -1.]
        if (this.state == IotHubConnectionStatus.DISCONNECTED || protonReturnValue == null)
        {
            log.trace("Amqp connection is disconnected, rejecting attempt to send message with delivery tag -1");
            deliveryTag = -1;
        }
        else
        {
            Message protonMessage = protonReturnValue.getMessageImpl();
            MessageType messageType = protonReturnValue.getMessageType();
            // Codes_SRS_AMQPSIOTHUBCONNECTION_12_024: [The function shall call AmqpsSessionManager.sendMessage with the given parameters.]
            deliveryTag = this.amqpsSessionManager.sendMessage(protonMessage, messageType, deviceId);
        }

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_021: [The function shall return the delivery hash.]
        return deliveryTag;
    }

    /**
     * Event handler for reactor init event.
     *
     * @param event Proton Event object
     */
    @Override
    public void onReactorInit(Event event)
    {
        Reactor reactor = event.getReactor();

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_033: [The event handler shall set the current handler to handle the connection events.]
        if (this.useWebSockets)
        {
            ProxySettings proxySettings = this.deviceClientConfig.getProxySettings();
            if (proxySettings != null)
            {
                reactor.connectionToHost(proxySettings.getHostname(), proxySettings.getPort(), this);
            }
            else
            {
                reactor.connectionToHost(this.chooseHostname(), AMQP_WEB_SOCKET_PORT, this);
            }
        }
        else
        {
            reactor.connectionToHost(this.chooseHostname(), AMQP_PORT, this);
        }

        reactor.schedule(SEND_MESSAGES_PERIOD_MILLIS, this);

        if (this.deviceClientConfig.getAuthenticationProvider() instanceof IotHubSasTokenAuthenticationProvider)
        {
            int sasTokenRenewalPeriod = this.deviceClientConfig.getSasTokenAuthentication().getMillisecondsBeforeProactiveRenewal();
            reactor.schedule(sasTokenRenewalPeriod, sasTokenRenewalHandler);
        }
    }

    /**
     * Event handler for reactor final event. Releases the close lock.
     * If reconnection has been set starts the reconnection by calling openAsync()
     *
     * @param event Proton Event object
     */
    @Override
    public void onReactorFinal(Event event)
    {
        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_011: [The function shall call countdown on close latch and open latch.]
        releaseLatch(authenticationLinkOpenLatch);
        releaseLatch(workerLinksOpenLatch);
        releaseLatch(closeReactorLatch);

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_012: [The function shall set the reactor member variable to null.]
        this.reactor = null;
    }

    /**
     * Event handler for the connection init event
     *
     * @param event The Proton Event object.
     */
    @Override
    public void onConnectionInit(Event event)
    {
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_025: [The event handler shall get the Connection (Proton) object from the event handler and set the host name on the connection.]
        this.connection = event.getConnection();
        this.connection.setHostname(this.hostName);

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_029: [The event handler shall open the connection.]
        this.connection.open();
        try
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_12_009: [The event handler shall call the amqpsSessionManager.onConnectionInit function with the connection.]
            this.amqpsSessionManager.onConnectionInit(this.connection);
        }
        catch (TransportException e)
        {
            this.savedException = e;
            log.error("Encountered an exception while reacting to onConnectionInit within amqp session", e);
        }
    }

    @Override
    public void onSessionRemoteOpen(Event e)
    {
        this.amqpsSessionManager.onSessionRemoteOpen(e.getSession());
    }

    /**
     * Event handler for the connection bound event. Sets Sasl authentication and proper authentication mode.
     *
     * @param event The Proton Event object.
     */
    @Override
    public void onConnectionBound(Event event)
    {
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_030: [The event handler shall get the Transport (Proton) object from the event.]
        Transport transport = event.getConnection().getTransport();
        if (transport != null)
        {
            if (this.useWebSockets)
            {
                addWebsocketLayer(transport);
            }

            try
            {
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_031: [The event handler shall call the AmqpsSessionManager.onConnectionBound with the transport and the SSLContext.]
                this.amqpsSessionManager.onConnectionBound(transport);
            }
            catch (TransportException e)
            {
                this.savedException = e;
                log.error("Encountered an exception while reacting to onConnectionBound event within amqp session", this.savedException);
            }

            //Adding proxy layer needs to be done after sending SSL message
            if (this.deviceClientConfig.getProxySettings() != null)
            {
                this.log.debug("Proxy settings set, adding amqp layer for proxy");
                addProxyLayer(transport, event.getConnection().getHostname());
            }
        }
    }

    private void addWebsocketLayer(Transport transport)
    {
        // Codes_SRS_AMQPSIOTHUBCONNECTION_25_049: [If websocket enabled the event handler shall configure the transport layer for websocket.]
        WebSocketImpl webSocket = new WebSocketImpl();
        webSocket.configure(this.hostName, WEB_SOCKET_PATH, WEBSOCKET_QUERY, WEBSOCKET_PORT, WEB_SOCKET_SUB_PROTOCOL, null, null);
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

    /**
     * Event handler for the connection unbound event. Sets the connection state to DISCONNECTED.
     *
     * @param event The Proton Event object.
     */
    @Override
    public void onConnectionUnbound(Event event)
    {
        log.trace("onConnectionUnbound event fired by proton, setting AMQP connection state to DISCONNECTED");
        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_010: [The function sets the state to closed.]
        this.state = IotHubConnectionStatus.DISCONNECTED;
    }

    /**
     * Event handler for the delivery event. This method handles both sending and receiving a message.
     *
     * @param event The Proton Event object.
     */
    @Override
    public void onDelivery(Event event)
    {
        Link link = event.getLink();

        if (link instanceof Sender)
        {
            //ack received for a message that this SDK sent earlier

            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_038: [If this link is the Sender link and the event type is DELIVERY, the event handler shall get the Delivery (Proton) object from the event.]
            Delivery delivery = event.getDelivery();
            while (delivery != null && !delivery.isSettled() && delivery.getRemoteState() != null)
            {
                DeliveryState remoteState = delivery.getRemoteState();

                int deliveryTag = Integer.valueOf(new String(delivery.getTag()));

                if (!link.getSource().getAddress().equalsIgnoreCase(AmqpsDeviceAuthenticationCBS.SENDER_LINK_ENDPOINT_PATH))
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

                            // Codes_SRS_AMQPSIOTHUBCONNECTION_34_064: [If the acknowledgement sent from the service is "Accepted", this function shall notify its listener that the message was successfully sent.]
                            this.listener.onMessageSent(acknowledgedMessage, null);
                        }
                        else if (remoteState instanceof Rejected)
                        {
                            this.log.trace("AMQP connection received Rejected acknowledgement for iot hub message  ({})", acknowledgedMessage);

                            TransportException transportException;
                            ErrorCondition errorCondition = ((Rejected) remoteState).getError();
                            if (errorCondition != null && errorCondition.getCondition() != null)
                            {
                                // Codes_SRS_AMQPSIOTHUBCONNECTION_28_001: [If the acknowledgement sent from the service is "Rejected", this function shall map the error condition if it exists to amqp exceptions.]
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
                                // Codes_SRS_AMQPSIOTHUBCONNECTION_34_065: [If the acknowledgement sent from the service is "Rejected", this function shall notify its listener that the sent message was rejected and that it should not be retried.]
                                transportException = new TransportException("IotHub rejected the message");
                            }

                            this.listener.onMessageSent(inProgressMessages.remove(deliveryTag), transportException);

                        }
                        else if (remoteState instanceof Modified || remoteState instanceof Released || remoteState instanceof Received)
                        {
                            this.log.trace("AMQP connection received Modified, Released or Received acknowledgement for iot hub message  ({})", acknowledgedMessage);

                            // Codes_SRS_AMQPSIOTHUBCONNECTION_34_066: [If the acknowledgement sent from the service is "Modified", "Released", or "Received", this function shall notify its listener that the sent message needs to be retried.]
                            TransportException transportException = new TransportException("IotHub responded to message with Modified, Received or Released; message needs to be re-delivered");
                            transportException.setRetryable(true);
                            this.listener.onMessageSent(inProgressMessages.remove(deliveryTag), transportException);
                        }
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

                delivery.free();

                //get next delivery to handle, or null if there isn't one
                delivery = link.head();
            }
        }
        else if (link instanceof Receiver)
        {
            //receiver link has a message from iot hub to retrieve
            AmqpsMessage amqpsMessage = null;

            // Codes_SRS_AMQPSIOTHUBCONNECTION_12_015: [The function shall call AmqpsSessionManager.getMessageFromReceiverLink.]
            try
            {
                this.log.trace("Amqp delivery received on a receiver link, checking receiver links for delivered message");
                amqpsMessage = this.amqpsSessionManager.getMessageFromReceiverLink(link.getName());
            }
            catch (TransportException e)
            {
                this.log.error("Failed to receive message from receiver link", e);
                this.listener.onMessageReceived(null, e);
            }

            if (amqpsMessage != null)
            {
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_050: [All the listeners shall be notified that a message was received from the server.]
                try
                {
                    this.handleCloudToDeviceMessage(amqpsMessage);
                }
                catch (TransportException e)
                {
                    this.listener.onMessageReceived(null, e);
                }
            }
            else
            {
                log.warn("onDelivery called on receiver link, but no message was found on any receiver link");
            }
        }
    }

    /**
     * Event handler for the link init event. Sets the proper target address on the link.
     *
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkInit(Event event)
    {
        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_016: [The function shall get the link from the event and call device operation objects with it.]
        Link link = event.getLink();
        this.amqpsSessionManager.onLinkInit(link);
    }

    private void processOutgoingMessages()
    {
        int messagesAttemptedToBeProcessed = 0;
        int lastDeliveryTag = 0;
        com.microsoft.azure.sdk.iot.device.Message message = messagesToSend.poll();
        while (message != null && messagesAttemptedToBeProcessed < MAX_MESSAGES_TO_SEND_PER_CALLBACK && lastDeliveryTag >= 0)
        {
            if (!subscriptionChangeHandler(message))
            {
                messagesAttemptedToBeProcessed++;
                lastDeliveryTag = processMessage(message);
            }

            message = messagesToSend.poll();
        }

        if (message != null)
        {
            //message was polled out of list, but loop exited from processing too many messages before it could process this message, so re-queue it for later
            messagesToSend.add(message);
        }
    }

    private int processMessage(com.microsoft.azure.sdk.iot.device.Message message)
    {
        int lastDeliveryTag = -1;
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = null;
        try
        {
            amqpsConvertToProtonReturnValue = this.convertToProton(message);
        }
        catch (TransportException e)
        {
            if (e.isRetryable())
            {
                this.log.warn("Encountered exception while converting message to proton message, retrying ({})", message, e);
                messagesToSend.add(message);
            }
            else
            {
                this.log.error("Encountered non-retryable exception while converting message to proton message, not retryable so discarding message ({})", message, e);
            }

            return lastDeliveryTag;
        }

        if (amqpsConvertToProtonReturnValue == null)
        {
            // Codes_SRS_AMQPSTRANSPORT_34_076: [The function throws IllegalStateException if none of the device operation object could handle the conversion.]
            this.log.warn("No handler found for message conversion! Abandoning message ({})", message);
            return lastDeliveryTag;
        }
        else
        {
            try
            {
                this.log.debug("Sending message over amqp ({})", message);
                lastDeliveryTag = this.sendMessage(amqpsConvertToProtonReturnValue, message.getConnectionDeviceId());
            }
            catch (TransportException e)
            {
                if (e.isRetryable())
                {
                    this.log.warn("Encountered exception while sending amqp message, retrying ({})", message, e);
                    messagesToSend.add(message);
                }
                else
                {
                    this.log.error("Encountered non-retryable exception while sending amqp message, abandoning message ({})", message, e);
                }

                return lastDeliveryTag;
            }

            if (lastDeliveryTag != -1)
            {
                this.log.trace("Amqp message was sent, waiting for ack ({})", message);
                // Codes_SRS_AMQPSTRANSPORT_34_078: [If the sent message hash is valid, it shall be added to the in progress map and this function shall return OK.]
                this.inProgressMessages.put(lastDeliveryTag, message);
                this.log.trace("Adding amqp delivery tag {} to in progress messages ({})", lastDeliveryTag, message);
            }
            else
            {
                //message failed to send, likely due to lack of link credit available. Re-queue and try again later
                this.log.trace("Amqp message failed to send, adding it back to messages to send queue ({})", message);
                messagesToSend.add(message);
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
        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_052: [The function shall call AmqpsSessionManager.onLinkRemoteOpen with the given link.]
        this.amqpsSessionManager.onLinkRemoteOpen(event.getLink());

        if (this.amqpsSessionManager.isAuthenticationOpened() && !isAuthenticated)
        {
            try
            {
                this.authenticate();
            }
            catch (TransportException e)
            {
                this.savedException = e;
            }
        }

        if (event.getLink().getName().startsWith(AmqpsDeviceAuthenticationCBS.RECEIVER_LINK_TAG_PREFIX) || event.getLink().getName().startsWith(AmqpsDeviceAuthenticationCBS.SENDER_LINK_TAG_PREFIX))
        {
            this.log.trace("authenticationLinkOpenLatch counted down");
            //TODO maybe pass the latches down into the session, into the links so that this comparison can be done down there instead?
            authenticationLinkOpenLatch.countDown();
        }
        else
        {
            this.log.trace("workerLinksOpenLatch counted down");
            //TODO maybe pass the latches down into the session, into the links so that this comparison can be done down there instead?
            workerLinksOpenLatch.countDown();
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
        this.amqpsSessionManager.onLinkRemoteClose(event.getLink());

        log.trace("onLinkRemoteClose fired by proton, setting AMQP connection state as DISCONNECTED");
        this.state = IotHubConnectionStatus.DISCONNECTED;

        //Codes_SRS_AMQPSIOTHUBCONNECTION_34_061 [If the provided event object's transport holds a remote error condition object, this function shall report the associated TransportException to this object's listeners.]
        this.savedException = getTransportExceptionFromEvent(event);

        this.scheduleReconnection(this.savedException);
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
     * Calls the AmqpsSessionManager to find the appropriate convertToProton converter.
     *
     * @param message the message to convert.
     * @return AmqpsConvertToProtonReturnValue containing the status and converted message.
     * @throws TransportException if conversion fails.
     */
    protected AmqpsConvertToProtonReturnValue convertToProton(com.microsoft.azure.sdk.iot.device.Message message) throws TransportException
    {
        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_056: [The function shall call AmqpsSessionManager.convertToProton with the given message.]
        return this.amqpsSessionManager.convertToProton(message);
    }

    /**
     * Calls the AmqpsSessionManager to find the appropriate convertFromProton converter.
     *
     * @param amqpsMessage       the message to convert.
     * @param deviceClientConfig the configuration to identify the message.
     * @return AmqpsConvertFromProtonReturnValue containing the status and converted message.
     * @throws TransportException if conversion fails.
     */
    protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws TransportException
    {
        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_056: [*The function shall call AmqpsSessionManager.convertFromProton with the given message. ]
        return this.amqpsSessionManager.convertFromProton(amqpsMessage, deviceClientConfig);
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
        processOutgoingMessages();

        event.getReactor().schedule(SEND_MESSAGES_PERIOD_MILLIS, this);
    }

    /**
     * Notifies all the listeners that a message was received from the server.
     *
     * @param amqpsMessage The message received from server.
     */
    private void handleCloudToDeviceMessage(AmqpsMessage amqpsMessage) throws TransportException
    {
        this.log.debug("Handling a received message with amqp message correlation id {}", amqpsMessage.getCorrelationId());
        AmqpsConvertFromProtonReturnValue amqpsHandleMessageReturnValue = this.convertFromProton(amqpsMessage, amqpsMessage.getDeviceClientConfig());

        if (amqpsHandleMessageReturnValue == null)
        {
            if (amqpsMessage.getAmqpsMessageType() == MessageType.CBS_AUTHENTICATION)
            {
                handleCbsMessage(amqpsMessage);

                //CBS messages require no acknowledgement from client side
                return;
            }

            // Should never happen; message type was not telemetry, twin, methods, or CBS
            log.warn("No handler found for received message with amqp correlation id {}, ignoring it", amqpsMessage.getCorrelationId());
            return;
        }

        // Codes_SRS_AMQPSTRANSPORT_12_008: [The function shall return if there is no message callback defined.]
        if (amqpsHandleMessageReturnValue.getMessageCallback() == null)
        {
            log.warn("Callback is not defined therefore response to IoT Hub cannot be generated. All received messages will be removed from receive message queue");
            throw new TransportException("callback is not defined");
        }

        IotHubTransportMessage transportMessage = amqpsHandleMessageReturnValue.getMessage();

        transportMessage.setMessageCallback(amqpsHandleMessageReturnValue.getMessageCallback());
        transportMessage.setMessageCallbackContext(amqpsHandleMessageReturnValue.getMessageContext());

        this.log.trace("Adding received message to the amqp message map to be acknowledged later ({})", transportMessage);

        this.sendAckMessages.put(transportMessage, amqpsMessage);

        //Codes_SRS_AMQPSIOTHUBCONNECTION_34_090: [If an amqp message can be received from the receiver link, and that amqp message contains a status code that is 200 or 204, this function shall notify this object's listeners that that message was received with a null exception.]
        //Codes_SRS_AMQPSIOTHUBCONNECTION_34_091: [If an amqp message can be received from the receiver link, and that amqp message contains no status code, this function shall notify this object's listeners that that message was received with a null exception.]
        //Codes_SRS_AMQPSIOTHUBCONNECTION_34_092: [If an amqp message can be received from the receiver link, and that amqp message contains no application properties, this function shall notify this object's listeners that that message was received with a null exception.]
        //Codes_SRS_AMQPSIOTHUBCONNECTION_34_093: [If an amqp message can be received from the receiver link, and that amqp message contains a status code, but that status code cannot be parsed to an integer, this function shall notify this object's listeners that that message was received with a null exception.]
        this.listener.onMessageReceived(transportMessage, null);
    }

    private void handleCbsMessage(AmqpsMessage amqpsMessage)
    {
        this.log.debug("Received message with correlation id {} was a cbs message, handling...", amqpsMessage.getCorrelationId());
        if (amqpsMessage.getApplicationProperties() != null && amqpsMessage.getApplicationProperties().getValue() != null)
        {
            Map<String, Object> properties = amqpsMessage.getApplicationProperties().getValue();

            if (properties.containsKey(APPLICATION_PROPERTY_STATUS_CODE))
            {
                String statusCodeString = properties.get(APPLICATION_PROPERTY_STATUS_CODE).toString();
                try
                {
                    int statusCode = Integer.valueOf(statusCodeString);
                    IotHubStatusCode iotHubStatusCode = IotHubStatusCode.getIotHubStatusCode(statusCode);

                    if (iotHubStatusCode != IotHubStatusCode.OK && iotHubStatusCode != IotHubStatusCode.OK_EMPTY)
                    {
                        //This status code is read during open time since the message is a CBS_AUTHENTICATION
                        // message, so save the iot hub status code it contains to return as the open result
                        String statusDescription = "";
                        if (properties.containsKey(APPLICATION_PROPERTY_STATUS_DESCRIPTION))
                        {
                            statusDescription = (String) properties.get(APPLICATION_PROPERTY_STATUS_DESCRIPTION);
                        }

                        //Codes_SRS_AMQPSIOTHUBCONNECTION_34_089: [If an amqp message can be received from the receiver link, and that amqp message contains a status code that is not 200 or 204, this function shall notify this object's listeners that that message was received and provide the status code's mapped exception.]
                        this.savedException = IotHubStatusCode.getConnectionStatusException(iotHubStatusCode, statusDescription);

                        log.error("CBS authentication was rejected by service", this.savedException);

                        releaseLatch(cbsLinkAuthorizedLatch);
                    }
                    else
                    {
                        cbsLinkAuthorizedLatch.countDown();
                        if (this.state != IotHubConnectionStatus.CONNECTED)
                        {
                            this.log.trace("CBS link received 200, one connection has been authorized");
                        }
                        else
                        {
                            this.log.trace("CBS link received 200, one connection has been re-authorized");
                        }

                        if (this.cbsLinkAuthorizedLatch.getCount() <= 0)
                        {
                            if (this.state != IotHubConnectionStatus.CONNECTED)
                            {
                                this.log.debug("All CBS links have received a 200, opening worker links");
                            }

                            this.isAuthenticated = true;
                            this.amqpsSessionManager.openWorkerLinks();
                        }
                    }
                }
                catch (NumberFormatException nfe)
                {
                    this.savedException = new TransportException("Encountered message from service with invalid status code value");
                    log.error("status code received from service on cbs link could not be parsed to integer {}", statusCodeString);
                }
            }
            else
            {
                log.warn("CBS message had no status code application property, so it was ignored");
            }
        }
        else
        {
            log.warn("CBS message had no application properties, so it was ignored");
        }
    }

    @Override
    public void setListener(IotHubListener listener) throws IllegalArgumentException
    {
        if (listener == null)
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_34_063: [If the provided listener is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("listener cannot be null");
        }

        // Codes_SRS_AMQPSIOTHUBCONNECTION_34_054: [The function shall save the given listener.]
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
            // Codes_SRS_AMQPSTRANSPORT_34_073: [If this object is not CONNECTED, this function shall return false.]
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
                    // Codes_SRS_AMQPSTRANSPORT_34_068: [If the provided message is saved in the saved map of messages
                    // to acknowledge, and if the provided result is ABANDON, this function shall send the amqp ack with ABANDON.]
                    amqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.ABANDON);
                    break;
                case REJECT:
                    // Codes_SRS_AMQPSTRANSPORT_34_069: [If the provided message is saved in the saved map of messages
                    // to acknowledge, and if the provided result is REJECT, this function shall send the amqp ack with REJECT.]
                    amqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.REJECT);
                    break;
                case COMPLETE:
                    // Codes_SRS_AMQPSTRANSPORT_34_070: [If the provided message is saved in the saved map of messages
                    // to acknowledge, and if the provided result is COMPLETE, this function shall send the amqp ack with COMPLETE.]
                    amqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.COMPLETE);
                    break;
                default:
                    log.warn("Invalid IoT Hub message result {}", result.name());
                    return false;
            }

            // Codes_SRS_AMQPSTRANSPORT_34_071: [If the amqp message is acknowledged, this function shall remove it from the saved map of messages to acknowledge and return true.]
            this.log.trace("Removing message from amqp map of messages to acknowledge ({})", message);
            this.sendAckMessages.remove(message);
            return true;
        }
        else
        {
            this.log.error("Amqp connection cannot send ack for this iot hub message because it has no mapping from it to any amqp message ({})", message);
        }

        // Codes_SRS_AMQPSTRANSPORT_34_072: [If the provided message is not saved in the saved map of messages to acknowledge, this function shall return false.]
        return false;
    }

    @Override
    public String getConnectionId()
    {
        // Codes_SRS_AMQPSTRANSPORT_34_094: [This function shall return the saved connection id.]
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
        // Codes_SRS_AMQPSIOTHUBCONNECTION_34_080: [If no exception can be found in the sender, receiver, session, connection, link, or transport, this function shall return a generic TransportException.]
        // Codes_SRS_AMQPSIOTHUBCONNECTION_34_081: [If an exception can be found in the sender, this function shall return a the mapped amqp exception derived from that exception.]
        // Codes_SRS_AMQPSIOTHUBCONNECTION_34_082: [If an exception can be found in the receiver, this function shall return a the mapped amqp exception derived from that exception.]
        // Codes_SRS_AMQPSIOTHUBCONNECTION_34_083: [If an exception can be found in the session, this function shall return a the mapped amqp exception derived from that exception.]
        // Codes_SRS_AMQPSIOTHUBCONNECTION_34_084: [If an exception can be found in the connection, this function shall return a the mapped amqp exception derived from that exception.]
        // Codes_SRS_AMQPSIOTHUBCONNECTION_34_085: [If an exception can be found in the link, this function shall return a the mapped amqp exception derived from that exception.]
        // Codes_SRS_AMQPSIOTHUBCONNECTION_34_086: [If an exception can be found in the transport, this function shall return a the mapped amqp exception derived from that exception.]
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
                        this.amqpsSessionManager.subscribeDeviceToMessageType(DEVICE_METHODS, message.getConnectionDeviceId());
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
                        this.amqpsSessionManager.subscribeDeviceToMessageType(DEVICE_TWIN, message.getConnectionDeviceId());
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
