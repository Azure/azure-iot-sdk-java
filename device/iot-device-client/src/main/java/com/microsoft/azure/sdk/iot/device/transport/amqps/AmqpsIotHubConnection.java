/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.deps.ws.impl.WebSocketImpl;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.State;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.FlowController;
import org.apache.qpid.proton.reactor.Handshaker;
import org.apache.qpid.proton.reactor.Reactor;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * An AMQPS IotHub connection between a device and an IoTHub. This class contains functionality for sending/receiving
 * a message, and logic to re-establish the connection with the IoTHub in case it gets lost.
 */
public final class AmqpsIotHubConnection extends BaseHandler
{
    private static final int MAX_WAIT_TO_OPEN_CLOSE_CONNECTION = 1*60*1000; // 1 minute timeout
    private static final int MAX_WAIT_TO_TERMINATE_EXECUTOR = 30;
    private State state;

    private int linkCredit = -1;
    /** The {@link Delivery} tag. */
    private long nextTag = 0;
    private static final String WEB_SOCKET_PATH = "/$iothub/websocket";
    private static final String WEB_SOCKET_SUB_PROTOCOL = "AMQPWSB10";
    private static final int AMQP_PORT = 5671;
    private static final int AMQP_WEB_SOCKET_PORT = 443;
    private String sasToken;

    private Connection connection;
    private Session session;

    private String hostName;
    private String userName;

    private final Boolean useWebSockets;
    private DeviceClientConfig config;

    private final List<ServerListener> listeners = new ArrayList<>();
    private ExecutorService executorService;

    private final ObjectLock openLock = new ObjectLock();
    private final ObjectLock closeLock = new ObjectLock();

    private Reactor reactor;

    private Boolean reconnectCall = false;
    private int currentReconnectionAttempt = 1;
    private CustomLogger logger;

    private ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList;

    /**
     * Constructor to set up connection parameters using the {@link DeviceClientConfig}.
     *
     * @param config The {@link DeviceClientConfig} corresponding to the device associated with this {@link com.microsoft.azure.sdk.iot.device.DeviceClient}.
     * @throws IOException if failed connecting to iothub.
     */
    public AmqpsIotHubConnection(DeviceClientConfig config, ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList) throws IOException

    {
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
        // any of the parameters of the configuration is null or empty.]
        if(config == null)
        {
            throw new IllegalArgumentException("The DeviceClientConfig cannot be null.");
        }
        if(config.getIotHubHostname() == null || config.getIotHubHostname().length() == 0)
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
        if (config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            if (config.getIotHubConnectionString().getSharedAccessKey() == null || config.getIotHubConnectionString().getSharedAccessKey().isEmpty())
            {
                if(config.getSasTokenAuthentication().getCurrentSasToken() == null || config.getSasTokenAuthentication().getCurrentSasToken().isEmpty())
                {
                    throw new IllegalArgumentException("Both deviceKey and shared access signature cannot be null or empty.");
                }
            }
        }

        if (amqpsDeviceOperationsList == null)
        {
            throw new IllegalArgumentException("amqpsDeviceOperationsList cannot be null or empty.");
        }
        if (amqpsDeviceOperationsList.size() == 0)
        {
            throw new IllegalArgumentException("amqpsDeviceOperationsList cannot be an empty list.");
        }

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_001: [The constructor shall save the device operation list to private member variable.]
        this.amqpsDeviceOperationsList = amqpsDeviceOperationsList;

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_002: [The constructor shall save the configuration into private member variables.]
        this.config = config;

        String deviceId = this.config.getDeviceId();
        String iotHubName = this.config.getIotHubName();

        this.userName = deviceId + "@sas." + iotHubName;

        this.useWebSockets = this.config.isUseWebsocket();
        if (useWebSockets)
        {
            this.hostName = String.format("%s:%d", this.config.getIotHubHostname(), AMQP_WEB_SOCKET_PORT);
        }
        else
        {
            this.hostName = String.format("%s:%d", this.config.getIotHubHostname(), AMQP_PORT);
        }

        this.logger = new CustomLogger(this.getClass());
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_004: [The constructor shall initialize a new Handshaker
        // (Proton) object to handle communication handshake.]
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_005: [The constructor shall initialize a new FlowController
        // (Proton) object to handle communication flow.]
        add(new Handshaker());
        add(new FlowController());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_006: [The constructor shall set its state to CLOSED.]
        this.state = State.CLOSED;

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_002: [The constructor shall create a Proton reactor.]
        try
        {
            reactor = Proton.reactor(this);

        } catch (IOException e)
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_12_003: [The constructor shall throw IOException if the Proton reactor creation failed.]
            logger.LogError(e);
            throw new IOException("Could not create Proton reactor");
        }
        logger.LogInfo("AmqpsIotHubConnection object is created successfully using port %s in %s method ", useWebSockets ? AMQP_WEB_SOCKET_PORT : AMQP_PORT, logger.getMethodName());
    }

    /**
     * Opens the {@link AmqpsIotHubConnection}.
     * <p>
     *     If the current connection is not open, this method
     *     will create a new SasToken. This method will
     *     start the {@link Reactor}, set the connection to open and make it ready for sending.
     * </p>
     *
     * @throws IOException If the reactor could not be initialized.
     */
    public void open() throws IOException
    {
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_007: [If the AMQPS connection is already open, the function shall do nothing.]
        if(this.state == State.CLOSED)
        {
            try
            {
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_009: [The function shall trigger the Reactor (Proton) to begin running.]
                openAsync();
            }
            catch(Exception e)
            {
                logger.LogError(e);
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_011: [If any exception is thrown while attempting to trigger
                // the reactor, the function shall close the connection and throw an IOException.]
                this.close();
                throw new IOException("Error opening Amqp connection: ", e);
            }

            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_010: [The function shall wait for the reactor to be ready and for
            // enough link credit to become available.]
            try
            {
                synchronized (openLock)
                {
                    openLock.waitLock(MAX_WAIT_TO_OPEN_CLOSE_CONNECTION);
                }
            } catch (InterruptedException e)
            {
                logger.LogError(e);
                throw new IOException("Waited too long for the connection to open.");
            }
        }
    }

    /**
     * Closes the {@link AmqpsIotHubConnection}.
     * <p>
     *     If the current connection is not closed, this function
     *     will set the current state to closed and invalidate all connection related variables.
     * </p>
     *
     * @throws IOException if it failed closing the iothub connection.
     */
    public void close() throws IOException
    {
        closeAsync();

        try
        {
            synchronized (closeLock)
            {
                closeLock.waitLock(MAX_WAIT_TO_OPEN_CLOSE_CONNECTION);
            }
        } catch (InterruptedException e)
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_12_004: [The function shall IOException throws if the waitLock throws.]
            logger.LogError(e);
            throw new IOException("Waited too long for the connection to close.");
        }

        if (this.executorService != null) {
            logger.LogInfo("Shutdown of executor service has started, method name is %s ", logger.getMethodName());
            this.executorService.shutdown();
            try
            {
                // Wait a while for existing tasks to terminate
                if (!this.executorService.awaitTermination(MAX_WAIT_TO_TERMINATE_EXECUTOR, TimeUnit.SECONDS)) {
                    this.executorService.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!this.executorService.awaitTermination(MAX_WAIT_TO_TERMINATE_EXECUTOR, TimeUnit.SECONDS)){
                        logger.LogInfo("Pool did not terminate");
                    }
                }
            } catch (InterruptedException ie)
            {
                // Codes_SRS_AMQPSIOTHUBCONNECTION_12_005: [The function shall throw IOException if the executor shutdown is interrupted.]
                logger.LogError(ie);
                // (Re-)Cancel if current thread also interrupted
                this.executorService.shutdownNow();
            }
            logger.LogInfo("Shutdown of executor service completed, method name is %s ", logger.getMethodName());
        }
    }

    private void openAsync() throws IOException
    {
        if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            this.sasToken = this.config.getSasTokenAuthentication().getRenewedSasToken();
        }
        else
        {
            //Codes_SRS_AMQPSIOTHUBCONNECTION_34_043: [If the config is not using sas token authentication, this function shall throw an IOException.]
            throw new IOException("AMQPS operations do not support using x509 authentication");
        }

        if (this.reactor == null)
        {
            this.reactor = Proton.reactor(this);
        }

        if (executorService == null)
        {
            executorService = Executors.newFixedThreadPool(1);
        }

        IotHubReactor iotHubReactor = new IotHubReactor(reactor);
        ReactorRunner reactorRunner = new ReactorRunner(iotHubReactor);
        executorService.submit(reactorRunner);
        logger.LogInfo("Reactor is assigned to executor service, method name is %s ", logger.getMethodName());
    }

    private void closeAsync()
    {
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_048 [If the AMQPS connection is already closed, the function shall do nothing.]
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_012: [The function shall set the status of the AMQPS connection to CLOSED.]
        this.state = State.CLOSED;

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_013: [The function shall close the AMQPS sender and receiver links,
        // the AMQPS session and the AMQPS connection.]
        for (int i = 0; i < amqpsDeviceOperationsList.size(); i++)
        {
            amqpsDeviceOperationsList.get(i).closeLinks();
        }

        if (this.session != null)
        {
            this.session.close();
        }
        if (this.connection != null)

            this.connection.close();

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_014: [The function shall stop the Proton reactor.]

        this.reactor.stop();
        logger.LogInfo("Proton reactor has been stopped, method name is %s ", logger.getMethodName());
    }

    /**
     * Creates a binary message using the given content and messageId. Sends the created message using the sender link.
     * @param message The message to be sent.
     * @return An {@link Integer} representing the hash of the message, or -1 if the connection is closed.
     */
    public Integer sendMessage(Message message, MessageType messageType) throws IOException
    {
        Integer deliveryHash;

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_015: [If the state of the connection is CLOSED or there is not enough
        // credit, the function shall return -1.]
        if (this.state == State.CLOSED || this.linkCredit <= 0)
        {
            deliveryHash = -1;
        }
        else
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_016: [The function shall encode the message and copy the contents to the byte buffer.]
            byte[] msgData = new byte[1024];
            int length;

            logger.LogInfo("Started encoding of message - entering in while loop, method name is %s ", logger.getMethodName());
            while (true)
            {
                try
                {
                    length = message.encode(msgData, 0, msgData.length);
                    logger.LogInfo("Completed encoding of message, length is %s - breaking the while loop to come out, method name is %s ", length, logger.getMethodName());
                    break;
                }
                catch (BufferOverflowException e)
                {
                    // Codes_SRS_AMQPSIOTHUBCONNECTION_12_007: [The function shall doubles the buffer if encode throws BufferOverflowException.]
                    logger.LogError(e);
                    msgData = new byte[msgData.length * 2];
                }
            }
            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_017: [The function shall set the delivery tag for the sender.]
            byte[] tag = String.valueOf(this.nextTag++).getBytes();

            // Codes_SRS_AMQPSIOTHUBCONNECTION_12_006: [The function shall call sendMessageAndGetDeliveryHash on all device operation objects.]
            deliveryHash = -1;
            for (int i = 0; i < amqpsDeviceOperationsList.size(); i++)
            {
                AmqpsSendReturnValue amqpsSendReturnValue = null;
                try
                {
                    amqpsSendReturnValue = amqpsDeviceOperationsList.get(i).sendMessageAndGetDeliveryHash(messageType, msgData, 0, length, tag);
                    if (amqpsSendReturnValue.isDeliverySuccessful())
                    {
                        deliveryHash = amqpsSendReturnValue.getDeliveryHash();
                        break;
                    }
                }
                catch (Exception e)
                {
                    throw new IOException("sendMessage failed!");
                }
            }
        }

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_021: [The function shall return the delivery hash.]
        return deliveryHash;
    }

    /**
     * Sends the message result for the previously received message.
     *
     * @param message the message to be acknowledged.
     * @param result the message result (one of {@link IotHubMessageResult#COMPLETE},
     *               {@link IotHubMessageResult#ABANDON}, or {@link IotHubMessageResult#REJECT}).
     * @return a boolean true if sent message was received with success, or false on fail.
     */
    public Boolean sendMessageResult(AmqpsMessage message, IotHubMessageResult result)
    {
        Boolean ackResult = false;
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_022: [If the AMQPS Connection is closed, the function shall return false.]
        if(this.state != State.CLOSED)
        {
            try
            {
                logger.LogInfo("Acknowledgement for received message is %s, method name is %s ", result.name(), logger.getMethodName());
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_023: [If the message result is COMPLETE, ABANDON, or REJECT,
                // the function shall acknowledge the last message with acknowledgement type COMPLETE, ABANDON, or REJECT respectively.]
                switch (result)
                {
                    case COMPLETE:
                        message.acknowledge(AmqpsMessage.ACK_TYPE.COMPLETE);
                        break;
                    case REJECT:
                        message.acknowledge(AmqpsMessage.ACK_TYPE.REJECT);
                        break;
                    case ABANDON:
                        message.acknowledge(AmqpsMessage.ACK_TYPE.ABANDON);
                        break;
                    default:
                        // should never happen.
                        logger.LogError("Invalid IoT Hub message result (%s), method name is %s ", result.name(), logger.getMethodName());
                        throw new IllegalStateException("Invalid IoT Hub message result.");
                }

                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_024: [The function shall return true after the message was acknowledged.]
                ackResult = true;
            }
            catch (Exception e)
            {
                // Codes_SRS_AMQPSIOTHUBCONNECTION_12_008: [The function shall return false if message acknowledge throws exception.]
                logger.LogError(e);
                //do nothing, since ackResult is already false
            }
        }
        return ackResult;
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

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_026: [The event handler shall create a Session (Proton) object from the connection.]
        this.session = this.connection.session();

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_029: [The event handler shall open the connection, session, sender and receiver objects.]
        this.connection.open();
        this.session.open();

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_009: [The event handler shall calls the openLink on all device operation objects.]
        for (int i = 0; i < amqpsDeviceOperationsList.size(); i++)
        {
            try
            {
                amqpsDeviceOperationsList.get(i).openLinks(this.session);
            }
            catch (Exception e)
            {
                logger.LogDebug("openLinks has thrown exception: %s", e.getMessage());
            }
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
        if(transport != null){

            if (this.useWebSockets)
            {
                WebSocketImpl webSocket = new WebSocketImpl();
                webSocket.configure(this.hostName, WEB_SOCKET_PATH, 0, WEB_SOCKET_SUB_PROTOCOL, null, null);
                ((TransportInternal)transport).addTransportLayer(webSocket);
            }

            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_031: [The event handler shall set the SASL_PLAIN authentication on the transport using the given user name and sas token.]
            Sasl sasl = transport.sasl();
            sasl.plain(this.userName, this.sasToken);

            try
            {
                SslDomain domain = makeDomain();
                transport.ssl(domain);
            }
            catch (IOException e)
            {
                logger.LogDebug("onConnectionBound has thrown exception while creating ssl context: %s", e.getMessage());
            }
        }
        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    @Override
    public void onConnectionUnbound(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());
        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_010: [The function sets the state to closed.]
        this.state = State.CLOSED;
        logger.LogDebug("Exited from method %s", logger.getMethodName());
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
            event.getReactor().connectionToHost(this.config.getIotHubHostname(), AMQP_WEB_SOCKET_PORT, this);
        }
        else
        {
            event.getReactor().connectionToHost(this.config.getIotHubHostname(), AMQP_PORT, this);
        }
        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    @Override
    public void onReactorFinal(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_011: [The function shall call notify lock on close lock.]
        synchronized (closeLock)
        {
            closeLock.notifyLock();
        }

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_012: [The function shall set the reactor member variable to null.]
        this.reactor = null;

        if (reconnectCall)
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_12_013: [The function shall call openAsync and disable reconnection if it is a reconnection attempt.]
            reconnectCall = false;
            try
            {
                openAsync();
            } catch (IOException e)
            {
                // Codes_SRS_AMQPSIOTHUBCONNECTION_12_014: [The function shall log the error if openAsync failed.]
                logger.LogDebug("onReactorFinal has thrown exception: %s", e.getMessage());
            }
        }
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

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_015: [The function shall call getMessageFromReceiverLink on all device operation objects.]
        AmqpsDeviceOperations receiverDeviceOperation = null;
        AmqpsMessage amqpsMessage = null;
        for (int i = 0; i < amqpsDeviceOperationsList.size(); i++)
        {
            try
            {
                amqpsMessage = amqpsDeviceOperationsList.get(i).getMessageFromReceiverLink(event.getLink().getName());
                if (amqpsMessage != null)
                {
                    receiverDeviceOperation = amqpsDeviceOperationsList.get(i);
                    break;
                }
            } catch (IOException e)
            {
                logger.LogDebug("onDelivery has thrown exception: %s", e.getMessage());
            }
        }

        if (receiverDeviceOperation != null)
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_050: [All the listeners shall be notified that a message was received from the server.]
            this.messageReceivedFromServer(amqpsMessage);
        }
        else
        {
            //Sender specific section for dispositions it receives
            if(event.getType() == Event.Type.DELIVERY)
            {
                logger.LogInfo("Reading the delivery event in Sender link, method name is %s ", logger.getMethodName());
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_038: [If this link is the Sender link and the event type is DELIVERY, the event handler shall get the Delivery (Proton) object from the event.]
                Delivery d = event.getDelivery();
                DeliveryState remoteState = d.getRemoteState();

                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_039: [The event handler shall note the remote delivery state and use it and the Delivery (Proton) hash code to inform the AmqpsIotHubConnection of the message receipt.]
                boolean state = remoteState.equals(Accepted.getInstance());
                logger.LogInfo("Is state of remote Delivery COMPLETE ? %s, method name is %s ", state, logger.getMethodName());
                logger.LogInfo("Inform listener that a message has been sent to IoT Hub along with remote state, method name is %s ", logger.getMethodName());
                //let any listener know that the message was received by the server
                for(ServerListener listener : listeners)
                {
                    listener.messageSent(d.hashCode(), state);
                }
		        // release the delivery object which created in sendMessage().
		        d.free();
            }
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
     * {@link org.apache.qpid.proton.reactor.Reactor} is ready, so we set the connection to OPEN.
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkRemoteOpen(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_041: [The connection state shall be considered OPEN when the sender link is open remotely.]
        boolean senderFound = false;
        String linkName = event.getLink().getName();
        for (int i = 0; i < amqpsDeviceOperationsList.size(); i++)
        {
            if (linkName.equals(amqpsDeviceOperationsList.get(i).getReceiverLinkTag()))
            {
                senderFound = true;
                break;
            }
        }

        if (senderFound)
        {
            this.state = State.OPEN;
            // Codes_SRS_AMQPSIOTHUBCONNECTION_99_001: [All server listeners shall be notified when that the connection has been established.]
            for(ServerListener listener : listeners)
            {
                listener.connectionEstablished();
            }
            // Codes_SRS_AMQPSIOTHUBCONNECTION_21_051 [The open lock shall be notified when that the connection has been established.]
            synchronized (openLock)
            {
                openLock.notifyLock();
            }
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
        this.state = State.CLOSED;

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_042 [The event handler shall attempt to startReconnect to the IoTHub.]
        boolean senderFound = false;
        String linkName = event.getLink().getName();
        for (int i = 0; i < amqpsDeviceOperationsList.size(); i++)
        {
            if (linkName.equals(amqpsDeviceOperationsList.get(i).getReceiverLinkTag()))
            {
                senderFound = true;
                break;
            }
        }

        if (senderFound)
        {
            logger.LogInfo("Starting to reconnect to IotHub, method name is %s ", logger.getMethodName());
            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_048: [The event handler shall attempt to startReconnect to IoTHub.]
            startReconnect();
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
        for (int i = 0; i < amqpsDeviceOperationsList.size(); i++)
        {
            try
            {
                amqpsDeviceOperationsList.get(i).initLink(link);
            }
            catch (Exception e)
            {
                logger.LogDebug("Exception in onLinkInit: %s", e.getMessage());
            }
        }

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
        this.state = State.CLOSED;
        logger.LogInfo("Starting to reconnect to IotHub, method name is %s ", logger.getMethodName());
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_048: [The event handler shall attempt to startReconnect to IoTHub.]
        startReconnect();
        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Subscribe a listener to the list of listeners.
     * @param listener the listener to be subscribed.
     */
    public void addListener(ServerListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Notifies all listeners that the connection was lost and attempts to startReconnect to the IoTHub
     * using an exponential backoff interval.
     */
    private void startReconnect()
    {
        reconnectCall = true;

        // Codes_SRS_AMQPSIOTHUBCONNECTION_12_007: [The event handler shall notify all server listeners when that the connection has been lost.]
        for(ServerListener listener : listeners)
        {
            listener.connectionLost();
        }

        if (currentReconnectionAttempt == Integer.MAX_VALUE)
            currentReconnectionAttempt = 0;

        System.out.println("Lost connection to the server. Reconnection attempt " + currentReconnectionAttempt++ + "...");
        logger.LogInfo("Lost connection to the server. Reconnection attempt %s, method name is %s ", currentReconnectionAttempt, logger.getMethodName());
        try
        {
            Thread.sleep(TransportUtils.generateSleepInterval(currentReconnectionAttempt));
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }

        closeAsync();
    }

    /**
     * Notifies all the listeners that a message was received from the server.
     * @param msg The message received from server.
     */
    private void messageReceivedFromServer(AmqpsMessage msg)
    {
        logger.LogInfo("All the listeners are informed that a message has been received, method name is %s ", logger.getMethodName());
        for(ServerListener listener : listeners)
        {
            listener.messageReceived(msg);
        }
    }

    /**
     * Create Proton SslDomain object from Address using the given Ssl mode
     * @return the created Ssl domain
     */
    private SslDomain makeDomain() throws IOException
    {
        SslDomain domain = Proton.sslDomain();
        domain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
        domain.init(SslDomain.Mode.CLIENT);

        /*
        Codes_SRS_AMQPSIOTHUBCONNECTION_25_049: [**The event handler shall set the SSL Context to IOTHub SSL context containing valid certificates.**]**
         */
        if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            domain.setSslContext(this.config.getSasTokenAuthentication().getSSLContext());
        }

        return domain;
    }

    /**
     * Class which runs the reactor.
     */
    private class ReactorRunner implements Callable
    {
        private final IotHubReactor iotHubReactor;

        ReactorRunner(IotHubReactor iotHubReactor)
        {
            this.iotHubReactor = iotHubReactor;
        }

        @Override
        public Object call()
        {
            iotHubReactor.run();
            return null;
        }
    }
}

