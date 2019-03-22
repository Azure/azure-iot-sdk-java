/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.deps.ws.impl.WebSocketImpl;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.device.transport.IotHubListener;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportConnection;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
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
import static com.microsoft.azure.sdk.iot.device.MessageType.*;

/**
 * An AMQPS IotHub connection between a device and an IoTHub. This class contains functionality for sending/receiving
 * a message, and logic to re-establish the connection with the IoTHub in case it gets lost.
 */
public final class AmqpsIotHubConnection extends BaseHandler implements IotHubTransportConnection
{
    private static final int MAX_WAIT_TO_OPEN_CLOSE_CONNECTION = 90*1000; // 90 second timeout
    private static final int MAX_WAIT_TO_TERMINATE_EXECUTOR = 30;
    private IotHubConnectionStatus state;

    private int linkCredit = -1;
    /** The {@link Delivery} tag. */
    private static final String WEB_SOCKET_PATH = "/$iothub/websocket";
    private static final String WEB_SOCKET_SUB_PROTOCOL = "AMQPWSB10";
    private static final int AMQP_PORT = 5671;
    private static final int AMQP_WEB_SOCKET_PORT = 443;

    private Connection connection;

    private String hostName;

    private final Boolean useWebSockets;
    private DeviceClientConfig deviceClientConfig;

    private final Map<Integer, com.microsoft.azure.sdk.iot.device.Message> inProgressMessages = new ConcurrentHashMap<>();
    private final Map<com.microsoft.azure.sdk.iot.device.Message, AmqpsMessage> sendAckMessages = new ConcurrentHashMap<>();

    private IotHubListener listener;

    //When the connection is lost for any reason, a thread is spawned to notify the Transport layer to re-establish
    // this connection. The original thread completes its shutdown. That thread should only be spawned once.
    private boolean reconnectionScheduled = false;

    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;

    private CountDownLatch openLatch;
    private CountDownLatch closeLatch;

    public String connectionId;

    private Reactor reactor;

    private CustomLogger logger;

    private TransportException savedException;

    public AmqpsSessionManager amqpsSessionManager;
    private final static String APPLICATION_PROPERTY_STATUS_CODE = "status-code";
    private final static String APPLICATION_PROPERTY_STATUS_DESCRIPTION = "status-description";

    private boolean methodSubscribed;
    private boolean twinSubscribed;

    /**
     * Constructor to set up connection parameters using the {@link DeviceClientConfig}.
     *
     * @param config The {@link DeviceClientConfig} corresponding to the device associated with this {@link com.microsoft.azure.sdk.iot.device.DeviceClient}.
     */
    public AmqpsIotHubConnection(DeviceClientConfig config)
    {
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
        // any of the parameters of the configuration is null or empty.]
        if(config == null)
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

        this.logger = new CustomLogger(this.getClass());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_004: [The constructor shall initialize a new Handshaker
        // (Proton) object to handle communication handshake.]
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_005: [The constructor shall initialize a new FlowController
        // (Proton) object to handle communication flow.]
        add(new Handshaker());
        add(new FlowController());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_006: [The constructor shall set its state to DISCONNECTED.]
        this.state = IotHubConnectionStatus.DISCONNECTED;

        logger.LogInfo("AmqpsIotHubConnection object is created successfully using port %s in %s method ", useWebSockets ? AMQP_WEB_SOCKET_PORT : AMQP_PORT, logger.getMethodName());
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
        }
    }

    /**
     * Opens the {@link AmqpsIotHubConnection}.
     * <p>
     *     This method will start the {@link Reactor}, set the connection to open and make it ready for sending.
     * </p>
     *
     * <p>
     *     Do not call this method after calling close on this object, instead, create a whole new AmqpsIotHubConnection
     *     object and open that instead.
     * </p>
     *
     * @throws TransportException If the reactor could not be initialized.
     */
    public void open(Queue<DeviceClientConfig> deviceClientConfigs, ScheduledExecutorService scheduledExecutorService) throws TransportException
    {
        reconnectionScheduled = false;
        connectionId = UUID.randomUUID().toString();

        this.scheduledExecutorService = scheduledExecutorService;

        this.closeLatch = new CountDownLatch(1);
        this.openLatch = new CountDownLatch(1);
        this.savedException = null;

        this.amqpsSessionManager = new AmqpsSessionManager(this.deviceClientConfig, Executors.newScheduledThreadPool(2));

        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_007: [If the AMQPS connection is already open, the function shall do nothing.]
        if(this.state == IotHubConnectionStatus.DISCONNECTED)
        {
            if(deviceClientConfigs.size() > 1)
            {
                deviceClientConfigs.remove();
                while (!deviceClientConfigs.isEmpty())
                {
                    this.addDeviceOperationSession(deviceClientConfigs.remove());
                }
            }

            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_010: [The function shall wait for the reactor to be ready and for
            // enough link credit to become available.]
            try
            {
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_009: [The function shall trigger the Reactor (Proton) to begin running.]
                this.openAsync();

                // Codes_SRS_AMQPSIOTHUBCONNECTION_12_059: [The function shall call await on open latch.]
                this.openLatch.await(MAX_WAIT_TO_OPEN_CLOSE_CONNECTION, TimeUnit.MILLISECONDS);

                // Codes_SRS_AMQPSIOTHUBCONNECTION_12_057: [The function shall call the connection to authenticate.]
                this.authenticate();

                // Codes_SRS_AMQPSIOTHUBCONNECTION_12_058: [The function shall call the connection to open device client links.]
                this.openLinks();

                if (this.savedException != null)
                {
                    // Codes_SRS_AMQPSIOTHUBCONNECTION_34_062: [If, after attempting to open the connection, this
                    // object has a saved exception, this function shall throw that saved exception.]
                    throw this.savedException;
                }

                if (!this.amqpsSessionManager.isAuthenticationOpened())
                {
                    // Codes_SRS_AMQPSIOTHUBCONNECTION_12_074: [If authentication has not succeeded after calling
                    // authenticate() and openLinks(), or if all links are not open yet,
                    // this function shall throw a retryable transport exception.]
                    this.close();
                    TransportException transportException = new TransportException("Timed out waiting for authentication links to open from service");
                    transportException.setRetryable(true);
                    throw transportException;
                }

                if (!this.amqpsSessionManager.areAllLinksOpen())
                {
                    // Codes_SRS_AMQPSIOTHUBCONNECTION_12_074: [If authentication has not succeeded after calling
                    // authenticate() and openLinks(), or if all links are not open yet,
                    // this function shall throw a retryable transport exception.]
                    this.close();
                    TransportException transportException = new TransportException("Timed out waiting for worker links to open from service");
                    transportException.setRetryable(true);
                    throw transportException;
                }
            }
            catch (InterruptedException e)
            {
                executorServicesCleanup();
                logger.LogError(e);
                throw new TransportException("Waited too long for the connection to open.");
            }
        }

        this.listener.onConnectionEstablished(this.connectionId);

        this.state = IotHubConnectionStatus.CONNECTED;

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Private helper for open.
     * Starts the Proton reactor.
     */
    private void openAsync() throws TransportException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

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

        logger.LogInfo("Reactor is assigned to executor service, method name is %s ", logger.getMethodName());
    }

    /**
     * Starts the authentication by calling the AmqpsSessionManager.
     * @throws TransportException if authentication open throws.
     */
    public void authenticate() throws TransportException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_020: [The function shall do nothing if the authentication is already open.]
        if (this.amqpsSessionManager.isAuthenticationOpened())
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_12_021: [The function shall call AmqpsSessionManager.authenticate.]
            this.amqpsSessionManager.authenticate();
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Opens all the operation links by calling the AmqpsSessionManager.
     *
     * @throws TransportException if Proton throws.
     */
    public void openLinks() throws TransportException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_022: [The function shall do nothing if the authentication is already open.]
        if (this.amqpsSessionManager.isAuthenticationOpened())
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_12_023: [The function shall call AmqpsSessionManager.openDeviceOperationLinks.]
            this.amqpsSessionManager.openDeviceOperationLinks(DEVICE_TELEMETRY);

            if (methodSubscribed)
            {
                this.amqpsSessionManager.openDeviceOperationLinks(DEVICE_METHODS);
            }

            if (twinSubscribed)
            {
                this.amqpsSessionManager.openDeviceOperationLinks(DEVICE_TWIN);
            }
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Closes the {@link AmqpsIotHubConnection}.
     * <p>
     *     If the current connection is not closed, this function
     *     will set the current state to closed and invalidate all connection related variables.
     * </p>
     *
     * @throws TransportException if it failed closing the iothub connection.
     */
    public void close() throws TransportException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        closeAsync();

        try
        {
            closeLatch.await(MAX_WAIT_TO_OPEN_CLOSE_CONNECTION, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_12_004: [The function shall TransportException throws if the waitLock throws.]
            logger.LogError(e);
            throw new TransportException("Waited too long for the connection to close.", e);
        }

        this.executorServicesCleanup();

        this.state = IotHubConnectionStatus.DISCONNECTED;

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    private void executorServicesCleanup() throws TransportException
    {
        if (this.executorService != null)
        {
            logger.LogInfo("Shutdown of executor service has started, method name is %s ", logger.getMethodName());
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
                        logger.LogInfo("Pool did not terminate");
                    }
                }

                this.executorService = null;
            }
            catch (InterruptedException e)
            {
                // Codes_SRS_AMQPSIOTHUBCONNECTION_12_005: [The function shall throw TransportException if the executor shutdown is interrupted.]
                logger.LogError(e);
                // (Re-)Cancel if current thread also interrupted
                this.executorService.shutdownNow();
                this.executorService = null;
                throw new TransportException("Waited too long for the connection to close.", e);
            }
            logger.LogInfo("Shutdown of executor service completed, method name is %s ", logger.getMethodName());
        }
    }

    /**
     * Private helper for close.
     * Closes the AmqpsSessionManager, the connection and stops the Proton reactor.
     */
    private void closeAsync()
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_013: [The function shall closeNow the AmqpsSessionManager and the AMQP connection.]
        if (this.amqpsSessionManager != null)
        {
            this.amqpsSessionManager.closeNow();
            this.amqpsSessionManager = null;
        }

        if (this.connection != null)
        {
            this.connection.close();
            this.connection = null;
        }

        // Codes_SRS_AMQPSIOTHUBCONNECTION_34_014: [If this object's proton reactor is not null, this function shall stop the Proton reactor.]
        if (this.reactor != null)
        {
            this.reactor.stop();
            this.reactor = null;
        }

        logger.LogInfo("Proton reactor has been stopped, method name is %s ", logger.getMethodName());

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Creates a binary message using the given content and messageId. Sends the created message using the sender link.
     *
     * @param message The message to be sent.
     * @param messageType the type of the message being sent
     * @throws TransportException if send message fails
     * @return An {@link Integer} representing the hash of the message, or -1 if the connection is closed.
     */
    private synchronized Integer sendMessage(Message message, MessageType messageType, String deviceId) throws TransportException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        Integer deliveryHash = -1;

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_015: [If the state of the connection is DISCONNECTED or there is not enough
        // credit, the function shall return -1.]
        if (this.state == IotHubConnectionStatus.DISCONNECTED || this.linkCredit <= 0)
        {
            deliveryHash = -1;
        }
        else
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_12_024: [The function shall call AmqpsSessionManager.sendMessage with the given parameters.]
            deliveryHash = this.amqpsSessionManager.sendMessage(message, messageType, deviceId);
        }

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_021: [The function shall return the delivery hash.]
        return deliveryHash;
    }

    /**
     * Event handler for reactor init event.
     * @param event Proton Event object
     */
    @Override
    public void onReactorInit(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_033: [The event handler shall set the current handler to handle the connection events.]
        if(this.useWebSockets)
        {
            event.getReactor().connectionToHost(this.chooseHostname(), AMQP_WEB_SOCKET_PORT, this);
        }
        else
        {
            event.getReactor().connectionToHost(this.chooseHostname(), AMQP_PORT, this);
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for reactor final event. Releases the close lock.
     * If reconnection has been set starts the reconnection by calling openAsync()
     * @param event Proton Event object
     */
    @Override
    public void onReactorFinal(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_011: [The function shall call countdown on close latch and open latch.]
        closeLatch.countDown();
        openLatch.countDown();

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_012: [The function shall set the reactor member variable to null.]
        this.reactor = null;

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for the connection init event
     * @param event The Proton Event object.
     */
    @Override
    public void onConnectionInit(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

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
            logger.LogError(e);
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for the connection bound event. Sets Sasl authentication and proper authentication mode.
     * @param event The Proton Event object.
     */
    @Override
    public void onConnectionBound(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_030: [The event handler shall get the Transport (Proton) object from the event.]
        Transport transport = event.getConnection().getTransport();
        if(transport != null)
        {
            if (this.useWebSockets)
            {
                // Codes_SRS_AMQPSIOTHUBCONNECTION_25_049: [If websocket enabled the event handler shall configure the transport layer for websocket.]
                WebSocketImpl webSocket = new WebSocketImpl();
                webSocket.configure(this.hostName, WEB_SOCKET_PATH, 0, WEB_SOCKET_SUB_PROTOCOL, null, null);
                ((TransportInternal)transport).addTransportLayer(webSocket);
            }

            try
            {
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_031: [The event handler shall call the AmqpsSessionManager.onConnectionBound with the transport and the SSLContext.]
                this.amqpsSessionManager.onConnectionBound(transport);
            }
            catch (TransportException e)
            {
                this.savedException = e;
                logger.LogError(this.savedException);
            }
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for the connection unbound event. Sets the connection state to DISCONNECTED.
     * @param event The Proton Event object.
     */
    @Override
    public void onConnectionUnbound(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_010: [The function sets the state to closed.]
        this.state = IotHubConnectionStatus.DISCONNECTED;

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for the delivery event. This method handles both sending and receiving a message.
     * @param event The Proton Event object.
     */
    @Override
    public void onDelivery(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        AmqpsMessage amqpsMessage = null;

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_015: [The function shall call AmqpsSessionManager.getMessageFromReceiverLink.]
        try
        {
            String linkName = event.getLink().getName();
            amqpsMessage = this.amqpsSessionManager.getMessageFromReceiverLink(linkName);
        }
        catch (TransportException e)
        {
            this.listener.onMessageReceived(null, e);
        }

        if (amqpsMessage != null)
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_050: [All the listeners shall be notified that a message was received from the server.]
            try
            {
                this.messageReceivedFromServer(amqpsMessage);
            }
            catch (TransportException e)
            {
                this.listener.onMessageReceived(null, e);
            }
        }
        else
        {
            //Sender specific section for dispositions it receives
            if (event.getType() == Event.Type.DELIVERY)
            {
                logger.LogInfo("Reading the delivery event in Sender link, method name is %s ", logger.getMethodName());
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_038: [If this link is the Sender link and the event type is DELIVERY, the event handler shall get the Delivery (Proton) object from the event.]
                Delivery d = event.getDelivery();
                DeliveryState remoteState = d.getRemoteState();

                logger.LogInfo("Is state of remote Delivery COMPLETE ? %s, method name is %s ", state, logger.getMethodName());
                logger.LogInfo("Inform listener that a message has been sent to IoT Hub along with remote state, method name is %s ", logger.getMethodName());

                if (!event.getLink().getSource().getAddress().equalsIgnoreCase(AmqpsDeviceAuthenticationCBS.RECEIVER_LINK_ENDPOINT_PATH))
                {
                    if (this.inProgressMessages.containsKey(d.hashCode()))
                    {
                        if (remoteState instanceof Accepted)
                        {
                            // Codes_SRS_AMQPSIOTHUBCONNECTION_34_064: [If the acknowledgement sent from the service is "Accepted", this function shall notify its listener that the message was successfully sent.]
                            this.listener.onMessageSent(inProgressMessages.remove(d.hashCode()), null);
                        }
                        else if (remoteState instanceof Rejected)
                        {
                            TransportException transportException;
                            ErrorCondition errorCondition = ((Rejected) remoteState).getError();
                            if (errorCondition !=  null && errorCondition.getCondition() != null)
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

                            this.listener.onMessageSent(inProgressMessages.remove(d.hashCode()), transportException);

                        }
                        else if (remoteState instanceof Modified || remoteState instanceof Released || remoteState instanceof Received)
                        {
                            // Codes_SRS_AMQPSIOTHUBCONNECTION_34_066: [If the acknowledgement sent from the service is "Modified", "Released", or "Received", this function shall notify its listener that the sent message needs to be retried.]
                            TransportException transportException = new TransportException("IotHub responded to message " +
                                    "with Modified, Received or Released; message needs to be re-delivered");
                            transportException.setRetryable(true);
                            this.listener.onMessageSent(inProgressMessages.remove(d.hashCode()), transportException);
                        }
                    }
                    else
                    {
                        this.listener.onMessageReceived(null, new TransportException("Received response from service about a message that this client did not send"));
                    }
                }

                // release the delivery object which created in sendMessage().
                d.free();
            }
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for the link init event. Sets the proper target address on the link.
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkInit(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_016: [The function shall get the link from the event and call device operation objects with it.]
        Link link = event.getLink();
        try
        {
            this.amqpsSessionManager.onLinkInit(link);
        }
        catch (TransportException e)
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_34_067: [If an exception is thrown while executing the callback onLinkInit on the saved amqpsSessionManager, that exception shall be saved.]
            this.savedException = e;
            logger.LogError(this.savedException);
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());

    }

    /**
     * Event handler for the link flow event. Handles sending a single message.
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkFlow(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_040: [The event handler shall save the remaining link credit.]
        this.linkCredit = event.getLink().getCredit();
        logger.LogDebug("The link credit value is %s, method name is %s", this.linkCredit, logger.getMethodName());

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for the link remote open event. This signifies that the
     * {@link org.apache.qpid.proton.reactor.Reactor} is ready, so we set the connection to CONNECTED.
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkRemoteOpen(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_052: [The function shall call AmqpsSessionManager.onLinkRemoteOpen with the given link.]
        if (this.amqpsSessionManager.onLinkRemoteOpen(event))
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_21_051 [The open latch shall be notified when that the connection has been established.]
            openLatch.countDown();
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for the link remote close event. This triggers reconnection attempts until successful.
     * Both sender and receiver links closing trigger this event, so we only handle one of them,
     * since the other is redundant.
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkRemoteClose(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        this.state = IotHubConnectionStatus.DISCONNECTED;

        //Codes_SRS_AMQPSIOTHUBCONNECTION_34_061 [If the provided event object's transport holds a remote error condition object, this function shall report the associated TransportException to this object's listeners.]
        this.savedException = getTransportExceptionFromEvent(event);

        this.scheduleReconnection(this.savedException);

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for the transport error event. This triggers reconnection attempts until successful.
     * @param event The Proton Event object.
     */
    @Override
    public void onTransportError(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        this.state = IotHubConnectionStatus.DISCONNECTED;

        //Codes_SRS_AMQPSIOTHUBCONNECTION_34_060 [If the provided event object's transport holds an error condition object, this function shall report the associated TransportException to this object's listeners.]
        this.savedException = getTransportExceptionFromEvent(event);

        this.scheduleReconnection(this.savedException);

        logger.LogDebug("Exited from method %s", logger.getMethodName());
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
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_056: [The function shall call AmqpsSessionManager.convertToProton with the given message.]
        return this.amqpsSessionManager.convertToProton(message);
    }

    /**
     * Calls the AmqpsSessionManager to find the appropriate convertFromProton converter.
     *
     * @param amqpsMessage the message to convert.
     * @param deviceClientConfig the configuration to identify the message.
     * @return AmqpsConvertFromProtonReturnValue containing the status and converted message.
     * @throws TransportException if conversion fails.
     */
    protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws TransportException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_056: [*The function shall call AmqpsSessionManager.convertFromProton with the given message. ]
        return this.amqpsSessionManager.convertFromProton(amqpsMessage, deviceClientConfig);
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
        catch(IOException e)
        {
            throw new TransportException("Could not create Proton reactor", e);
        }
    }

    /**
     * Notifies all the listeners that a message was received from the server.
     * @param amqpsMessage The message received from server.
     */
    private void messageReceivedFromServer(AmqpsMessage amqpsMessage) throws TransportException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        logger.LogInfo("All the listeners are informed that a message has been received, method name is %s ", logger.getMethodName());

        AmqpsConvertFromProtonReturnValue amqpsHandleMessageReturnValue = this.convertFromProton(amqpsMessage, amqpsMessage.getDeviceClientConfig());

        if (amqpsHandleMessageReturnValue == null)
        {
            if (amqpsMessage.getAmqpsMessageType() == MessageType.CBS_AUTHENTICATION)
            {
                if (amqpsMessage.getApplicationProperties() != null && amqpsMessage.getApplicationProperties().getValue() != null)
                {
                    Map<String, Object> properties = amqpsMessage.getApplicationProperties().getValue();

                    if (properties.containsKey(APPLICATION_PROPERTY_STATUS_CODE))
                    {
                        try
                        {
                            int statusCode = Integer.valueOf(properties.get(APPLICATION_PROPERTY_STATUS_CODE).toString());
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
                                logger.LogError(this.savedException);
                            }
                        }
                        catch (NumberFormatException nfe)
                        {
                            this.savedException = new TransportException("Encountered message from service with invalid status code value");
                            logger.LogInfo("status code received from service could not be parsed to integer, method name is %s ", logger.getMethodName());
                        }
                    }
                }

                //CBS messages require no acknowledgement from client side
                return;
            }

            // Should never happen; message type was not telemetry, twin, methods, or CBS
            logger.LogError("No handler found for received message, method name is %s ", logger.getMethodName());
            return;
        }

        // Codes_SRS_AMQPSTRANSPORT_12_008: [The function shall return if there is no message callback defined.]
        if (amqpsHandleMessageReturnValue.getMessageCallback() == null)
        {
            logger.LogError("Callback is not defined therefore response to IoT Hub cannot be generated. All received messages will be removed from receive message queue, method name is %s ", logger.getMethodName());
            throw new TransportException("callback is not defined");
        }

        com.microsoft.azure.sdk.iot.device.Message message = amqpsHandleMessageReturnValue.getMessage();
        IotHubTransportMessage transportMessage;
        if (message instanceof IotHubTransportMessage)
        {
            //preserve the properties of the transport message
            transportMessage = (IotHubTransportMessage) message;
        }
        else
        {
            transportMessage = new IotHubTransportMessage(message.getBytes(), message.getMessageType(), message.getMessageId(), message.getCorrelationId(), message.getProperties());
            transportMessage.setIotHubConnectionString(message.getIotHubConnectionString());
        }

        transportMessage.setMessageCallback(amqpsHandleMessageReturnValue.getMessageCallback());
        transportMessage.setMessageCallbackContext(amqpsHandleMessageReturnValue.getMessageContext());

        this.sendAckMessages.put(transportMessage, amqpsMessage);

        //Codes_SRS_AMQPSIOTHUBCONNECTION_34_090: [If an amqp message can be received from the receiver link, and that amqp message contains a status code that is 200 or 204, this function shall notify this object's listeners that that message was received with a null exception.]
        //Codes_SRS_AMQPSIOTHUBCONNECTION_34_091: [If an amqp message can be received from the receiver link, and that amqp message contains no status code, this function shall notify this object's listeners that that message was received with a null exception.]
        //Codes_SRS_AMQPSIOTHUBCONNECTION_34_092: [If an amqp message can be received from the receiver link, and that amqp message contains no application properties, this function shall notify this object's listeners that that message was received with a null exception.]
        //Codes_SRS_AMQPSIOTHUBCONNECTION_34_093: [If an amqp message can be received from the receiver link, and that amqp message contains a status code, but that status code cannot be parsed to an integer, this function shall notify this object's listeners that that message was received with a null exception.]
        this.listener.onMessageReceived(transportMessage, null);
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
        if (!subscriptionChangeHandler(message))
        {
            AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = this.convertToProton(message);

            if (amqpsConvertToProtonReturnValue == null)
            {
                // Codes_SRS_AMQPSTRANSPORT_34_076: [The function throws IllegalStateException if none of the device operation object could handle the conversion.]
                throw new IllegalStateException("No handler found for message conversion!");
            }

            // Codes_SRS_AMQPSTRANSPORT_34_077: [The function shall attempt to send the Proton message to IoTHub using the underlying AMQPS connection.]
            Integer sendHash = this.sendMessage(amqpsConvertToProtonReturnValue.getMessageImpl(), amqpsConvertToProtonReturnValue.getMessageType(), message.getConnectionDeviceId());

            if (sendHash != -1)
            {
                // Codes_SRS_AMQPSTRANSPORT_34_078: [If the sent message hash is valid, it shall be added to the in progress map and this function shall return OK.]
                this.inProgressMessages.put(sendHash, message);
            }
            else
            {
                // Codes_SRS_AMQPSTRANSPORT_34_079: [If the sent message hash is -1, this function shall throw a retriable ProtocolException.]
                ProtocolException protocolException = new ProtocolException("Send failure");
                protocolException.setRetryable(true);
                throw protocolException;
            }
        }

        return IotHubStatusCode.OK;
    }

    /**
     * Sends the Ack for the provided message with the result
     * @param message the message to acknowledge
     * @param result the result to attach to the ack (COMPLETE, ABANDON, or REJECT)
     * @return true if the ack was sent successfully, and false otherwise
     */
    @Override
    public boolean sendMessageResult(com.microsoft.azure.sdk.iot.device.Message message, IotHubMessageResult result)
    {
        if (this.state != IotHubConnectionStatus.CONNECTED)
        {
            // Codes_SRS_AMQPSTRANSPORT_34_073: [If this object is not CONNECTED, this function shall return false.]
            return false;
        }

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
                    logger.LogError("Invalid IoT Hub message result (%s), method name is %s ", result.name(), logger.getMethodName());
                    return false;
            }

            // Codes_SRS_AMQPSTRANSPORT_34_071: [If the amqp message is acknowledged, this function shall remove it from the saved map of messages to acknowledge and return true.]
            this.sendAckMessages.remove(message);
            return true;
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
     * @param throwable the reason why the reconnection needs to take place, for reporting purposes
     */
    private void scheduleReconnection(Throwable throwable)
    {
        if (!reconnectionScheduled)
        {
            reconnectionScheduled = true;
            scheduledExecutorService.schedule(new ReconnectionTask(throwable, this.listener, this.connectionId), 0, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Runnable task to restart the AMQPS connection. Cannot do this work synchronously because the proton reactor can't
     * block and wait for itself to be closed. The thread spawning this thread will close the reactor thread while the
     * spawned thread creates a new reactor once the old reactor closes
     */
    public static class ReconnectionTask implements Callable
    {
        private final static String THREAD_NAME = "azure-iot-sdk-ReconnectionTask";
        private Throwable connectionLossCause;
        private IotHubListener listener;
        private String connectionId;

        private ReconnectionTask(Throwable connectionLossCause, IotHubListener listener, String connectionId)
        {
            this.connectionLossCause = connectionLossCause;
            this.listener = listener;
            this.connectionId = connectionId;
        }

        @Override
        public Object call()
        {
            Thread.currentThread().setName(THREAD_NAME);
            this.listener.onConnectionLost(this.connectionLossCause, this.connectionId);
            return null;
        }
    }

    private String chooseHostname()
    {
        String gatewayHostname = this.deviceClientConfig.getGatewayHostname();
        if (gatewayHostname != null && !gatewayHostname.isEmpty())
        {
            return gatewayHostname;
        }

        return this.deviceClientConfig.getIotHubHostname();
    }

    /**
     * Derive the transport exception from the provided event, defaulting to a generic, retryable TransportException
     * @param event the event context
     * @return the transport exception derived from the provided event
     */
    private TransportException getTransportExceptionFromEvent(Event event)
    {
        TransportException transportException = new TransportException("Unknown transport exception occurred");
        transportException.setRetryable(true);

        String error = "";
        String errorDescription = "";

        String senderError = event.getSender() != null && event.getSender().getRemoteCondition() != null && event.getSender().getRemoteCondition().getCondition() != null ? event.getSender().getRemoteCondition().getCondition().toString() : "";
        String receiverError = event.getReceiver() != null && event.getReceiver().getRemoteCondition() != null && event.getReceiver().getRemoteCondition().getCondition() != null ? event.getReceiver().getRemoteCondition().getCondition().toString() : "";
        String sessionError = event.getSession() != null && event.getSession().getRemoteCondition() != null && event.getSession().getRemoteCondition().getCondition() != null ? event.getSession().getRemoteCondition().getCondition().toString() : "";
        String connectionError = event.getConnection() != null && event.getConnection().getRemoteCondition() != null && event.getConnection().getRemoteCondition().getCondition() != null ? event.getConnection().getRemoteCondition().getCondition().toString() : "";
        String linkError = event.getLink() != null && event.getLink().getRemoteCondition() != null && event.getLink().getRemoteCondition().getCondition() != null ? event.getLink().getRemoteCondition().getCondition().toString() : "";
        String transportError = event.getTransport() != null && event.getTransport().getRemoteCondition() != null && event.getTransport().getRemoteCondition().getCondition() != null ? event.getTransport().getRemoteCondition().getCondition().toString() : "";

        String senderErrorDescription = event.getSender() != null && event.getSender().getRemoteCondition() != null && event.getSender().getRemoteCondition().getDescription() != null ? event.getSender().getRemoteCondition().getDescription() : "";
        String receiverErrorDescription = event.getReceiver() != null && event.getReceiver().getRemoteCondition() != null && event.getReceiver().getRemoteCondition().getDescription() != null ? event.getReceiver().getRemoteCondition().getDescription() : "";
        String sessionErrorDescription = event.getSession() != null && event.getSession().getRemoteCondition() != null && event.getSession().getRemoteCondition().getDescription() != null ? event.getSession().getRemoteCondition().getDescription() : "";
        String connectionErrorDescription = event.getConnection() != null && event.getConnection().getRemoteCondition() != null && event.getConnection().getRemoteCondition().getDescription() != null ? event.getConnection().getRemoteCondition().getDescription() : "";
        String linkErrorDescription = event.getLink() != null && event.getLink().getRemoteCondition() != null && event.getLink().getRemoteCondition().getDescription() != null ? event.getLink().getRemoteCondition().getDescription() : "";
        String transportErrorDescription = event.getTransport() != null && event.getTransport().getRemoteCondition() != null && event.getTransport().getRemoteCondition().getDescription() != null ? event.getTransport().getRemoteCondition().getDescription() : "";

        if (!senderError.isEmpty())
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_34_081: [If an exception can be found in the sender, this function shall return a the mapped amqp exception derived from that exception.]
            error = senderError;
            errorDescription = senderErrorDescription;
        }
        else if (!receiverError.isEmpty())
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_34_082: [If an exception can be found in the receiver, this function shall return a the mapped amqp exception derived from that exception.]
            error = receiverError;
            errorDescription = receiverErrorDescription;
        }
        else if (!sessionError.isEmpty())
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_34_083: [If an exception can be found in the session, this function shall return a the mapped amqp exception derived from that exception.]
            error = sessionError;
            errorDescription = sessionErrorDescription;
        }
        else if (!connectionError.isEmpty())
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_34_084: [If an exception can be found in the connection, this function shall return a the mapped amqp exception derived from that exception.]
            error = connectionError;
            errorDescription = connectionErrorDescription;
        }
        else if (!linkError.isEmpty())
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_34_085: [If an exception can be found in the link, this function shall return a the mapped amqp exception derived from that exception.]
            error = linkError;
            errorDescription = linkErrorDescription;
        }
        else if (!transportError.isEmpty())
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_34_086: [If an exception can be found in the transport, this function shall return a the mapped amqp exception derived from that exception.]
            error = transportError;
            errorDescription = transportErrorDescription;
        }


        // Codes_SRS_AMQPSIOTHUBCONNECTION_34_080: [If no exception can be found in the sender, receiver, session, connection, link, or transport, this function shall return a generic TransportException.]
        if (!error.isEmpty())
        {
            transportException = AmqpsExceptionTranslator.convertToAmqpException(error, errorDescription);
        }

        return transportException;
    }

    private boolean subscriptionChangeHandler(com.microsoft.azure.sdk.iot.device.Message message) throws TransportException
    {
        boolean handled = false;
        if (message.getMessageType() != null)
        {
            switch (message.getMessageType())
            {
                case DEVICE_METHODS:
                    if (((IotHubTransportMessage) message).getDeviceOperationType() == DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST)
                    {
                        this.amqpsSessionManager.openDeviceOperationLinks(DEVICE_METHODS);
                        methodSubscribed = true;
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
                    else
                    {
                        this.amqpsSessionManager.openDeviceOperationLinks(DEVICE_TWIN);
                        twinSubscribed = true;
                        if (((IotHubTransportMessage) message).getDeviceOperationType() == DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST)
                        {
                            handled = true;
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        return handled;
    }
}
