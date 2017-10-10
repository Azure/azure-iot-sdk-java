// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubCallbackPacket;
import com.microsoft.azure.sdk.iot.device.transport.IotHubOutboundPacket;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransport;
import com.microsoft.azure.sdk.iot.device.transport.State;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <p>
 * An AMQPS transport. Contains functionality for adding messages and sending
 * messages to an IoT Hub. Buffers unsent messages until they are received by an
 * IoT Hub. A transport is bound at construction to the following parameters:
 * IoT Hub name, device ID and device key.
 * </p>
 * The transport also receives messages from IoT Hub and invokes a
 * user-defined message callback if a message and callback are found.
 */
public final class AmqpsTransport implements IotHubTransport, ServerListener
{
    /** The state of the AMQPS transport. */
    private State state;

    /** The {@link AmqpsIotHubConnection} underlying this transport. */
    private AmqpsIotHubConnection connection;

    /** Messages waiting to be sent to the IoT Hub. */
    private final Queue<IotHubOutboundPacket> waitingMessages = new LinkedBlockingDeque<>();

    /** Messages which are sent to the IoT Hub but did not receive ack yet. */
    private final Map<Integer, IotHubOutboundPacket> inProgressMessages = new ConcurrentHashMap<>();

    /** Messages received from the IoT Hub */
    private final Queue<AmqpsMessage> receivedMessages = new LinkedBlockingQueue<>();

    /** Messages whose callbacks that are waiting to be invoked. */
    private final Queue<IotHubCallbackPacket> callbackList = new LinkedBlockingDeque<>();

    /** Connection state change callback */
    private IotHubConnectionStateCallback stateCallback;
    private Object stateCallbackContext;

    private final DeviceClientConfig config;
    private final CustomLogger logger;

    private ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList;

    /**
     * Constructs an instance from the given {@link DeviceClientConfig}
     * object.
     *
     * @param config configuration parameters for an AMQPS session with an IoT Hub
     *
     */
    public AmqpsTransport(final DeviceClientConfig config)
    {
        // Codes_SRS_AMQPSTRANSPORT_15_001: [The constructor shall save the input parameters into instance variables.]
        this.config = config;

        // Codes_SRS_AMQPSTRANSPORT_15_002: [The constructor shall set the transport state to CLOSED.]
        this.state = State.CLOSED;
        this.logger = new CustomLogger(this.getClass());

        // Codes_SRS_AMQPSTRANSPORT_12_001: [The constructor shall create device operation list with DEVICE_TELEMETRY, DEVICE_METHODS and DEVICE_TWIN objects.]
        amqpsDeviceOperationsList = new ArrayList<>();

        amqpsDeviceOperationsList.add(new AmqpsDeviceTelemetry(this.config.getDeviceId()));
        amqpsDeviceOperationsList.add(new AmqpsDeviceMethods(this.config.getDeviceId()));
        amqpsDeviceOperationsList.add(new AmqpsDeviceTwin(this.config.getDeviceId()));
    }

    /**
     * Establishes a communication channel with an IoT Hub. If a channel is
     * already open, the function shall do nothing.
     *
     * @throws IOException if a communication channel cannot be established.
     */
    public synchronized void open() throws IOException
    {
        // Codes_SRS_AMQPSTRANSPORT_15_003: [If an AMQPS connection is already open, the function shall do nothing.]
        if (this.state == State.OPEN)
        {
            return;
        }
        logger.LogInfo("Opening the connection..., method name is %s ", logger.getMethodName());
        // Codes_SRS_AMQPSTRANSPORT_15_004: [The function shall open an AMQPS connection with the IoT Hub given in the configuration.]
        this.connection = new AmqpsIotHubConnection(this.config, amqpsDeviceOperationsList);

        try
        {
            // Codes_SRS_AMQPSTRANSPORT_15_005: [The function shall add the transport to the list of listeners subscribed to the connection events.]
            this.connection.addListener(this);

            this.connection.open();
        }
        catch (Exception e)
        {
            // Codes_SRS_AMQPSTRANSPORT_12_004: [The function shall throw IOException if connection open throws.]
            logger.LogError(e);
            throw new IOException(e);
        }

        // Codes_SRS_AMQPSTRANSPORT_15_006: [If the connection was opened successfully, the transport state shall be set to OPEN.]
        this.state = State.OPEN;
        logger.LogInfo("Connection has been opened, method name is %s ", logger.getMethodName());
    }

    /**
     * Closes all resources used to communicate with an IoT Hub. Once {@code close()} is
     * called, the transport is no longer usable. If the transport is already
     * closed, the function shall do nothing.
     *
     * @throws IOException if an error occurs in closing the transport.
     */
    public synchronized void close() throws IOException
    {
        // Codes_SRS_AMQPSTRANSPORT_15_007: [If the AMQPS connection is closed, the function shall do nothing.]
        if (this.state == State.CLOSED)
        {
            logger.LogInfo("The connection is already in closed state, method name is %s ", logger.getMethodName());
            return;
        }
        
        // Codes_SRS_AMQPSTRANSPORT_99_036: [The method will remove all the messages which are in progress or waiting to be sent and add them to the callback list.]
        while (!this.waitingMessages.isEmpty())
        {
           IotHubOutboundPacket packet = this.waitingMessages.remove();
           Message message = packet.getMessage();
            
           // Codes_SRS_AMQPSTRANSPORT_15_015: [The function shall skip messages with null or empty body.]
            if (message != null && message.getBytes().length > 0)
            {
                IotHubCallbackPacket callbackPacket = new IotHubCallbackPacket(IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE, packet.getCallback(), packet.getContext());
                this.callbackList.add(callbackPacket);
                
            }
        }

        // Codes_SRS_AMQPSTRANSPORT_12_005: [The function shall add a new outbound packet to the callback list.]
        for (Map.Entry<Integer, IotHubOutboundPacket> entry : inProgressMessages.entrySet())
        {
            IotHubOutboundPacket packet = entry.getValue();
            IotHubCallbackPacket callbackPacket = new IotHubCallbackPacket(IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE, packet.getCallback(), packet.getContext());
            this.callbackList.add(callbackPacket);
        }
                    
        // Codes_SRS_AMQPSTRANSPORT_99_037: [The method will invoke all the callbacks..]
        invokeCallbacks(); 
        
        // Codes_SRS_AMQPSTRANSPORT_15_033: [The map of messages in progress is cleared.]
        inProgressMessages.clear();
                       
        logger.LogInfo("Starting to close the connection..., method name is %s ", logger.getMethodName());
       
        // Codes_SRS_AMQPSTRANSPORT_15_008: [The function shall close an AMQPS connection with the IoT Hub given in the configuration.]
        this.connection.close();

        // Codes_SRS_AMQPSTRANSPORT_15_009: [The function shall set the transport state to CLOSED.]
        this.state = State.CLOSED;
        logger.LogInfo("Connection has been closed, method name is %s ", logger.getMethodName());
    }

    /**
     * Adds a message to the transport queue.
     *
     * @param message the message to be sent.
     * @param callback the callback to be invoked when a response for the message is received.
     * @param callbackContext the context to be passed in when the callback is invoked.
     *
     * @throws IllegalStateException if the transport is closed.
     */
    public void addMessage(Message message, IotHubEventCallback callback, Object callbackContext) throws IllegalStateException
    {
        // Codes_SRS_AMQPSTRANSPORT_15_010: [If the AMQPS session is closed, the function shall throw an IllegalStateException.]
        if (this.state == State.CLOSED)
        {
            logger.LogError("Cannot add a message when the AMQPS transport is closed, method name is %s ", logger.getMethodName());
            throw new IllegalStateException("Cannot add a message when the AMQPS transport is closed.");
        }

        // Codes_SRS_AMQPSTRANSPORT_15_011: [The function shall add a packet containing the message, callback, and callback context to the queue of messages waiting to be sent.]
        IotHubOutboundPacket packet = new IotHubOutboundPacket(message, callback, callbackContext);
        this.waitingMessages.add(packet);
    }

    /**
     * Adds a message to the transport queue.
     * !!! This functionality is not supported yet, please use `addMessage` with `IotHubEventCallback`!!!
     *
     * @param message the message to be sent.
     * @param callback the callback to be invoked when a response for the message is received.
     * @param callbackContext the context to be passed in when the callback is invoked.
     *
     * @throws UnsupportedOperationException always.
     */
    public void addMessage(Message message, IotHubResponseCallback callback, Object callbackContext) throws UnsupportedOperationException
    {
        // Codes_SRS_AMQPSTRANSPORT_21_040: [The function shall throws `UnsupportedOperationException`.]
        throw new UnsupportedOperationException("AMQP do not support callback with message response");
    }

    /**
     * <p>
     * Sends all messages from the waiting list, one at a time. If a previous
     * send attempt had failed, the function will attempt to resend the messages
     * in the previous attempt.
     * </p>
     *
     * @throws IOException if the server could not be reached.
     * @throws IllegalStateException if the transport has not been opened or is
     * already closed.
     */
    public void sendMessages() throws IOException, IllegalStateException
    {
        // Codes_SRS_AMQPSTRANSPORT_15_012: [If the AMQPS session is closed, the function shall throw an IllegalStateException.]
        if (this.state == State.CLOSED)
        {
            logger.LogError("Cannot send messages when the AMQPS transport is closed, method name is %s ", logger.getMethodName());
            throw new IllegalStateException("Cannot send messages when the AMQPS transport is closed.");
        }

        // Codes_SRS_AMQPSTRANSPORT_15_013: [If there are no messages in the waiting list, the function shall return.]
        if (this.waitingMessages.size() <= 0)
        {
            return;
        }

        Collection<IotHubOutboundPacket> failedMessages = new ArrayList<>() ;

        // Codes_SRS_AMQPSTRANSPORT_15_014: [The function shall attempt to send every message on its waiting list, one at a time.]
        while (!this.waitingMessages.isEmpty())
        {
            logger.LogInfo("Get the message from waiting message queue to be sent to IoT Hub, method name is %s ", logger.getMethodName());
            IotHubOutboundPacket packet = this.waitingMessages.remove();

            Message message = packet.getMessage();

            // Codes_SRS_AMQPSTRANSPORT_15_015: [The function shall skip messages with null or empty body.]
            if (message != null)
            {
                // Codes_SRS_AMQPSTRANSPORT_15_039: [If the message is expired, the function shall create a callback
                // with the MESSAGE_EXPIRED status and add it to the callback list.]
                if (message.isExpired())
                {
                    logger.LogInfo("Creating a callback for the expired message with MESSAGE_EXPIRED status, method name is %s ", logger.getMethodName());
                    IotHubCallbackPacket callbackPacket = new IotHubCallbackPacket(IotHubStatusCode.MESSAGE_EXPIRED, packet.getCallback(), packet.getContext());
                    this.callbackList.add(callbackPacket);
                }
                else
                {
                    logger.LogInfo("Converting the IoT Hub message into AmqpsMessage, method name is %s ", logger.getMethodName());

                    // Codes_SRS_AMQPSTRANSPORT_12_002: [The function shall call device operation objects to convert the IoTHubMessage to Proton message.]
                    AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = null;
                    if (amqpsDeviceOperationsList != null)
                    {
                        for (int i = 0; i < amqpsDeviceOperationsList.size(); i++)
                        {
                            amqpsConvertToProtonReturnValue = amqpsDeviceOperationsList.get(i).convertToProton(message);
                            if (amqpsConvertToProtonReturnValue != null)
                            {
                                break;
                            }
                        }
                    }

                    // Codes_SRS_AMQPSTRANSPORT_12_003: [The function throws IllegalStateException if none of the device operation object could handle the conversion.]
                    if (amqpsConvertToProtonReturnValue == null)
                    {
                        // Should never happen
                        throw new IllegalStateException("No handler found for message conversion!");
                    }

                    if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN && this.config.getSasTokenAuthentication().isRenewalNecessary())
                    {
                        //Codes_SRS_AMQPSTRANSPORT_34_041: [If the config is using sas token authentication and its sas token has expired and cannot be renewed, the message shall not be sent, an UNAUTHORIZED message callback shall be added to the callback queue and SAS_TOKEN_EXPIRED state callback shall be fired.]
                        failedMessages.add(packet);
                        logger.LogInfo("Creating a callback for the message with expired sas token with UNAUTHORIZED status, method name is %s ", logger.getMethodName());
                        IotHubCallbackPacket callbackPacket = new IotHubCallbackPacket(IotHubStatusCode.UNAUTHORIZED, packet.getCallback(), packet.getContext());
                        this.callbackList.add(callbackPacket);

                        if (this.stateCallback != null)
                        {
                            this.stateCallback.execute(IotHubConnectionState.SAS_TOKEN_EXPIRED, this.stateCallbackContext);
                        }
                    }
                    else
                    {
                        // Codes_SRS_AMQPSTRANSPORT_15_037: [The function shall attempt to send the Proton message to IoTHub using the underlying AMQPS connection.]
                        Integer sendHash = connection.sendMessage(amqpsConvertToProtonReturnValue.getMessageImpl(), amqpsConvertToProtonReturnValue.getMessageType());

                        // Codes_SRS_AMQPSTRANSPORT_15_016: [If the sent message hash is valid, it shall be added to the in progress map.]
                        if (sendHash != -1)
                        {
                            this.inProgressMessages.put(sendHash, packet);
                        }
                        // Codes_SRS_AMQPSTRANSPORT_15_017: [If the sent message hash is not valid, it shall be buffered to be sent in a subsequent attempt.]
                        else
                        {
                            failedMessages.add(packet);
                        }
                    }
                }
            }
        }

        this.waitingMessages.addAll(failedMessages);
    }

    /**
     * Invokes the callbacks for all completed requests.
     *
     * @throws IllegalStateException if the transport is closed.
     */
    public void invokeCallbacks() throws IllegalStateException
    {
        // Codes_SRS_AMQPSTRANSPORT_15_019: [If the transport closed, the function shall throw an IllegalStateException.]
        if (this.state == State.CLOSED)
        {
            logger.LogError("Cannot invoke callbacks when AMQPS transport is closed, method name is %s ", logger.getMethodName());
            throw new IllegalStateException("Cannot invoke callbacks when AMQPS transport is closed.");
        }

        // Codes_SRS_AMQPSTRANSPORT_15_020: [The function shall invoke all the callbacks from the callback queue.]
        while (!this.callbackList.isEmpty())
        {
            IotHubCallbackPacket packet = this.callbackList.remove();

            IotHubStatusCode status = packet.getStatus();
            IotHubEventCallback callback = packet.getCallback();
            Object context = packet.getContext();

            logger.LogInfo("Invoking the callback function for sent message, IoT Hub responded to message with status %s, method name is %s ", status.name(), logger.getMethodName());
            callback.execute(status, context);
        }
    }

    /**
     * <p>
     * Invokes the message callback if a message is found and
     * responds to the IoT Hub on how the processed message should be
     * handled by the IoT Hub.
     * </p>
     * If no message callback is set, the function will do nothing.
     *
     * @throws IllegalStateException if the transport is closed.
     */
    public void handleMessage() throws IllegalStateException, IOException
    {
        // Codes_SRS_AMQPSTRANSPORT_15_021: [If the transport is closed, the function shall throw an IllegalStateException.]
        if (this.state == State.CLOSED)
        {
            logger.LogError("Cannot handle messages when AMQPS transport is closed, method name is %s ", logger.getMethodName());
            throw new IllegalStateException("Cannot handle messages when AMQPS transport is closed.");
        }
        
        logger.LogInfo("Get the callback function for the received message, method name is %s ", logger.getMethodName());

        // Codes_SRS_AMQPSTRANSPORT_15_023: [The function shall attempt to consume a message from the IoT Hub.]
        // Codes_SRS_AMQPSTRANSPORT_15_024: [If no message was received from IotHub, the function shall return.]
        if (this.receivedMessages.size() > 0)
        {
            logger.LogInfo("Consuming a message received from IoT Hub using receive message queue, method name is %s ", logger.getMethodName());
            AmqpsMessage receivedMessage = this.receivedMessages.remove();

            AmqpsConvertFromProtonReturnValue amqpsHandleMessageReturnValue = null;

            logger.LogInfo("Converting the AmqpsMessage to IoT Hub message, method name is %s ", logger.getMethodName());

            // Codes_SRS_AMQPSTRANSPORT_12_006: [The function shall call device operation objects to convert the Proton message to IoTHubMessage.]
            if (amqpsDeviceOperationsList != null)
            {
                for (int i = 0; i < amqpsDeviceOperationsList.size(); i++)
                {
                    amqpsHandleMessageReturnValue = amqpsDeviceOperationsList.get(i).convertFromProton(receivedMessage, config);
                    if (amqpsHandleMessageReturnValue != null)
                    {
                        break;
                    }
                }
            }

            // Codes_SRS_AMQPSTRANSPORT_12_007: [The function throws IllegalStateException if none of the device operation object could handle the conversion.]
            if (amqpsHandleMessageReturnValue == null)
            {
                // Should never happen
                throw new IllegalStateException("No handler found for received message!");
            }

            // Codes_SRS_AMQPSTRANSPORT_12_008: [The function shall return if there is no message callback defined.]
            if (amqpsHandleMessageReturnValue.getMessageCallback() == null)
            {
                logger.LogError("Callback is not defined therefore response to IoT Hub cannot be generated. All received messages will be removed from receive message queue, method name is %s ", logger.getMethodName());
                return;
            }

            logger.LogInfo("Executing the callback function for received message, method name is %s ", logger.getMethodName());
            // Codes_SRS_AMQPSTRANSPORT_15_026: [The function shall invoke the callback on the message.]
            IotHubMessageResult result = amqpsHandleMessageReturnValue.getMessageCallback().execute(amqpsHandleMessageReturnValue.getMessage(), amqpsHandleMessageReturnValue.getMessageContext());

            // Codes_SRS_AMQPSTRANSPORT_15_027: [The function shall return the message result (one of COMPLETE, ABANDON, or REJECT) to the IoT Hub.]
            Boolean ackResult = this.connection.sendMessageResult(receivedMessage, result);
            // Codes_SRS_AMQPSTRANSPORT_15_028: [If the result could not be sent to IoTHub, the message shall be put back in the received messages queue to be processed again.]
            if (!ackResult)
            {
                logger.LogWarn("Callback did not return a response for IoT Hub. Message has been added in the queue to be processed again, method name is %s", logger.getMethodName());
                receivedMessages.add(receivedMessage);
            }
        }
    }

    /**
     * When a message is acknowledged by IoTHub, it is removed from the list of in progress messages and its callback
     * is added to the list of callbacks to be executed. If the message was not successfully delivered, it is buffered
     * to be sent again.
     * @param messageHash The hash of the message.
     * @param deliveryState The state of the delivery.
     */
    public void messageSent(Integer messageHash, Boolean deliveryState)
    {
        // Codes_SRS_AMQPSTRANSPORT_15_029: [If the hash cannot be found in the list of keys for the messages in progress, the method returns.]
        if (inProgressMessages.containsKey(messageHash))
        {
            IotHubOutboundPacket packet = inProgressMessages.remove(messageHash);
            if (deliveryState)
            {
                logger.LogInfo("Message with messageid %s has been successfully delivered to IoTHub, adding a callback to callbacklist with IotHubStatusCode.OK_EMPTY, method name is %s ", packet.getMessage().getMessageId(), logger.getMethodName());
                // Codes_SRS_AMQPSTRANSPORT_15_030: [If the message was successfully delivered,
                // its callback is added to the list of callbacks to be executed.]
                IotHubCallbackPacket callbackPacket = new IotHubCallbackPacket(IotHubStatusCode.OK_EMPTY, packet.getCallback(), packet.getContext());
                this.callbackList.add(callbackPacket);
            } else
            {
                logger.LogInfo("Message with messageid %s was not delivered to IoTHub, it is buffered to be sent again, method name is %s ", packet.getMessage().getMessageId(), logger.getMethodName());
                // Codes_SRS_AMQPSTRANSPORT_15_031: [If the message was not delivered successfully, it is buffered to be sent again.]
                waitingMessages.add(packet);
            }
        }
    }

    /**
     * If the connection is lost, all the messages in progress are buffered to be sent again.
     */
    public void connectionLost()
    {
        logger.LogInfo("The messages in progress are buffered to be sent again due to a connection loss, method name is %s ", logger.getMethodName());
        // Codes_SRS_AMQPSTRANSPORT_15_032: [The messages in progress are buffered to be sent again.]
        for (Map.Entry<Integer, IotHubOutboundPacket> entry : inProgressMessages.entrySet())
        {
            this.waitingMessages.add(entry.getValue());
        }

        // Codes_SRS_AMQPSTRANSPORT_15_033: [The map of messages in progress is cleared.]
        inProgressMessages.clear();

        // Notify the listener that the connection is down
        // Codes_SRS_AMQPSTRANSPORT_99_001: [Registered connection state callback is notified that the connection has been lost.]
        if (this.stateCallback != null) {
            this.stateCallback.execute(IotHubConnectionState.CONNECTION_DROP, this.stateCallbackContext);
        }
    }

    /**
     * Need to alert all listeners that the connection has been established.
     */
    public void connectionEstablished()
    {
        logger.LogInfo("The connection to the IoT Hub has been established, method name is %s ", logger.getMethodName());
        // Notify listener that the connection is up
        // Codes_SRS_AMQPSTRANSPORT_99_002: [Registered connection state callback is notified that the connection has been established.]
        if (this.stateCallback != null) {
            this.stateCallback.execute(IotHubConnectionState.CONNECTION_SUCCESS, this.stateCallbackContext);
        }
    }

    /**
     * When a message is received, it is added to the list of messages to be processed.
     * @param message The message received.
     */
    public void messageReceived(AmqpsMessage message)
    {
        logger.LogInfo("Message with hashcode %s is received from IotHub on %s, method name is %s ", message.hashCode(), new Date(), logger.getMethodName());
        // Codes_SRS_AMQPSTRANSPORT_15_034: [The message received is added to the list of messages to be processed.]
        this.receivedMessages.add(message);
    }

    /**
     * Returns {@code true} if the transport has no more messages to handle,
     * and {@code false} otherwise.
     *
     * @return {@code true} if the transport has no more messages to handle,
     * and {@code false} otherwise.
     */
    public boolean isEmpty()
    {
        // Codes_SRS_AMQPSTRANSPORT_15_035: [The function shall return true if the waiting list,
        // in progress list and callback list are all empty, and false otherwise.]
        return this.waitingMessages.isEmpty() && this.inProgressMessages.size() == 0 && this.callbackList.isEmpty();

    }

    /**
     * Registers a callback to be executed whenever the amqps connection is lost or established.
     * 
     * @param callback the callback to be called.
     * @param callbackContext a context to be passed to the callback. Can be
     * {@code null} if no callback is provided.
     */
    public void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext)
    {
        //Codes_SRS_AMQPSTRANSPORT_34_042: If the provided callback is null, an IllegalArgumentException shall be thrown.]
        if (callback == null)
        {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        // Codes_SRS_AMQPSTRANSPORT_99_003: [The registerConnectionStateCallback shall register the connection state callback.]
        this.stateCallback = callback;
        this.stateCallbackContext = callbackContext;
    }
}