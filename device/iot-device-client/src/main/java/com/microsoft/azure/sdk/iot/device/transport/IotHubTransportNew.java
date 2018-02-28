/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.DeviceClientException;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsIotHubConnection;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsIotHubConnection;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttIotHubConnection;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class IotHubTransportNew implements IotHubListener
{
    private static final int MAX_MESSAGES_TO_SEND_PER_THREAD = 10;
    private State state;
    private IotHubTransportConnection iotHubTransportConnection;

    /* Messages waiting to be sent to the IoT Hub. */
    private final Queue<IotHubOutboundPacket> waitingMessages;

    /* Messages which are sent to the IoT Hub but did not receive ack yet. */
    //private final Queue<IotHubOutboundPacket> inProgressMessages;
    private final Map<Integer, IotHubOutboundPacket> inProgressMessages;

    /* Messages received from the IoT Hub */
    private final Queue<Message> receivedMessages;

    /* Messages whose callbacks that are waiting to be invoked. */
    private final Queue<IotHubCallbackPacket> callbackList;

    /*Connection Status callback information */
    private IotHubConnectionStateCallback stateCallback;
    private Object stateCallbackContext;

    /* Locks to synchronise queuing operations between send and receive */
    final private Object sendLock;
    final private Object receiveLock;

    private DeviceClientConfig defaultConfig;
    private Queue<DeviceClientConfig> deviceClientConfigs;

    private final CustomLogger logger;

    public IotHubTransportNew(DeviceClientConfig defaultConfig)
    {
        this.state = State.CLOSED;
        this.waitingMessages = new LinkedBlockingQueue<>();
        this.inProgressMessages = new ConcurrentHashMap<>();
        this.receivedMessages = new LinkedBlockingQueue<>();
        this.callbackList  = new LinkedBlockingQueue<>();
        this.sendLock = new Object();
        this.receiveLock = new Object();
        this.defaultConfig = defaultConfig;
        this.logger = new CustomLogger(this.getClass());
    }

    /**
     * Method executed when a message was acknowledged by IoTHub.
     *
     * @param message
     * @param e
     */
    @Override
    public void onMessageSent(Message message, Throwable e)
    {
        synchronized (this)
        {
            // remove from in progress queue and add to callback queue
            if (inProgressMessages.containsKey(message.hashCode()))
            {
                if (e == null)
                {
                    IotHubOutboundPacket packet = inProgressMessages.get(message.hashCode());
                    IotHubCallbackPacket callbackPacket = new IotHubCallbackPacket(IotHubStatusCode.OK_EMPTY, packet.getCallback(), packet.getContext());
                    this.callbackList.add(callbackPacket);
                }
                else
                {
                    // TODO : Handle retry of message sent but not acked or improper ack. At the moment retrying by default
                    this.waitingMessages.add(inProgressMessages.get(message.hashCode()));
                }
            }
            else
            {
                logger.LogError("Message with message id %s was delivered to IoTHub, but was never sent, method name is %s ", message.getMessageId(), logger.getMethodName());
            }
        }
    }

    @Override
    public void onMessageReceived(Message message, Throwable e)
    {
        synchronized (receiveLock)
        {
            handleException(e);

            logger.LogInfo("Message with hashcode %s is received from IotHub on %s, method name is %s ", message.hashCode(), new Date(), logger.getMethodName());
            // Codes_SRS_AMQPSTRANSPORT_15_034: [The message received is added to the list of messages to be processed.]
            // TODO : Add service callback for this message in the queue
            this.receivedMessages.add(message);
        }
    }

    @Override
    public void onConnectionLost(Throwable e)
    {
        handleException(e);

        this.state = State.CLOSED;

        // Move in progress to waiting as they may not have sent
        logger.LogInfo("The messages in progress are buffered to be sent again due to a connection loss, method name is %s ", logger.getMethodName());
        // Codes_SRS_AMQPSTRANSPORT_15_032: [The messages in progress are buffered to be sent again.]
        this.waitingMessages.addAll(inProgressMessages.values());
        inProgressMessages.clear();

        // Attempt retry here

        // inform user of connection drop
        invokeStatusCallback(IotHubConnectionState.CONNECTION_DROP);
    }

    @Override
    public void onConnectionEstablished(Throwable e)
    {
        handleException(e);
        // Inform user that connection is established
        logger.LogInfo("The connection to the IoT Hub has been established, method name is %s ", logger.getMethodName());
        // Notify listener that the connection is up
        // Codes_SRS_AMQPSTRANSPORT_99_002: [Registered connection state callback is notified that the connection has been established.]
        invokeStatusCallback(IotHubConnectionState.CONNECTION_SUCCESS);
        this.state = State.OPEN;
    }

    /**
     * Establishes a communication channel with an IoT Hub. If a channel is
     * already open, the function shall do nothing.
     *
     * @throws IOException if a communication channel cannot be
     * established.
     */
    public synchronized void open(Collection<DeviceClientConfig> deviceClientConfigs) throws DeviceClientException
    {
        if ((deviceClientConfigs == null) || deviceClientConfigs.isEmpty())
        {
            throw new IllegalArgumentException("deviceClientConfigs cannot be null or empty");
        }

        if(this.state == State.OPEN)
        {
            return;
        }

        this.deviceClientConfigs = new LinkedBlockingQueue<DeviceClientConfig>(deviceClientConfigs);
        this.defaultConfig = this.deviceClientConfigs.peek();

        try
        {
            switch (defaultConfig.getProtocol())
            {
                case HTTPS:
                    this.iotHubTransportConnection = new HttpsIotHubConnection(defaultConfig);
                    break;
                case MQTT:
                case MQTT_WS:
                    this.iotHubTransportConnection = new MqttIotHubConnection(defaultConfig);
                    break;
                case AMQPS:
                case AMQPS_WS:
                    this.iotHubTransportConnection = new AmqpsIotHubConnection(defaultConfig, 1);
                    break;
                default:
                    throw new IOException("Protocol not supported");
            }

            this.iotHubTransportConnection.addListener(this);
            this.iotHubTransportConnection.open(this.deviceClientConfigs);
            this.state = State.OPEN;
        }
        catch (Exception e)
        {
            //TODO this will be just a TransportException catch once AMQP and HTTP are also just throwing TransportExceptions
        }
    }

    /**
     * Establishes a communication channel usingmultiplexing with an IoT Hub. If a channel is
     * already open, the function shall do nothing.
     *
     * @param deviceClientList the list of clients use the same transport.
     * @throws IOException if a communication channel cannot be
     * established.
     */
/*    void multiplexOpen(List<DeviceClient> deviceClientList) throws IOException
    {

    }*/

    /**
     * Closes all resources used to communicate with an IoT Hub. Once {@code close()} is
     * called, the transport is no longer usable. If the transport is already
     * closed, the function shall do nothing.
     *
     * @throws IOException if an error occurs in closing the transport.
     */
    public synchronized void close() throws DeviceClientException
    {
        if (this.state == State.CLOSED)
        {
            return;
        }

        // Move waiting messages to callback to inform user of close
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

        // Move in progress message to callback to inform user of close
        // TODO: check how HTTP transport will behave here where it needs response message as oppose to packet
        for (Map.Entry<Integer, IotHubOutboundPacket> packetEntry : inProgressMessages.entrySet())
        {
            IotHubOutboundPacket packet = packetEntry.getValue();
            IotHubCallbackPacket callbackPacket = new IotHubCallbackPacket(IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE, packet.getCallback(), packet.getContext());
            this.callbackList.add(callbackPacket);
        }

        // invoke all the callbacks
        invokeCallbacks();
        inProgressMessages.clear();

        try
        {
            this.iotHubTransportConnection.close();
        }
        catch (Exception e)
        {
            //TODO (Tim) this will only throw TranpsortException soon witch won't require a catch at all. Ignore this catch for now
        }

        this.state = State.CLOSED;
    }

    private void handleException(Throwable e)
    {
        if (e != null)
        {
            // TODO: decide what to do with this exception Rem to not throw it back as it may be coming from threads from external libraries
        }
    }

    /**
     * Adds a message to the transport queue.
     *
     * @param message the message to be sent.
     * @param callback the callback to be invoked when a response for the
     * message is received.
     * @param callbackContext the context to be passed in when the callback is
     * invoked.
     */
    public void addMessage(Message message,
                    IotHubEventCallback callback,
                    Object callbackContext)
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
     *
     * @param message the message to be sent.
     * @param callback the callback to be invoked when a response for the
     * message is received.
     * @param callbackContext the context to be passed in when the callback is
     * invoked.
     */
    public void addMessage(Message message,
                           IotHubResponseCallback callback,
                           Object callbackContext)
    {
        // TODO : Check if you can get rid of this
    }

    /**
     * Sends all messages on the transport queue. If a previous send attempt had
     * failed, the function will attempt to resend the messages in the previous
     * attempt.
     *
     * @throws IOException if the server could not be reached.
     */
    public synchronized void sendMessages() throws DeviceClientException
    {

        // Codes_SRS_AMQPSTRANSPORT_15_012: [If the AMQPS session is closed, the function shall throw an IllegalStateException.]
        if (this.state == State.CLOSED)
        {
            logger.LogError("Cannot send messages when the AMQPS transport is closed, method name is %s ", logger.getMethodName());
            throw new IllegalStateException("Cannot send messages when the AMQPS transport is closed.");
        }

        int timeSlice = MAX_MESSAGES_TO_SEND_PER_THREAD;

        while (!this.waitingMessages.isEmpty() && timeSlice-- > 0)
        {
            logger.LogInfo("Get the message from waiting message queue to be sent to IoT Hub, method name is %s ", logger.getMethodName());
            IotHubOutboundPacket packet = this.waitingMessages.remove();
            Message message = packet.getMessage();

            if (message != null && this.isMessageValid(packet))
            {
                try
                {
                    this.iotHubTransportConnection.sendMessage(message);
                    this.inProgressMessages.put(message.hashCode(), packet);
                }
                catch (IOException e)
                {
                    // TODO: Check retry policy for message and attempt to resend
                    // defaulting to retry at the moment by adding it back to waiting queue

                    this.waitingMessages.add(packet);
                }
                catch (Exception e)
                {
                    //TODO (Tim) this will only throw TranpsortException soon witch won't require a catch at all. Ignore this catch for now
                }
            }
        }
    }

    private boolean isMessageValid(IotHubOutboundPacket packet)
    {
        Message message = packet.getMessage();

        if (message.isExpired())
        {
            logger.LogInfo("Creating a callback for the expired message with MESSAGE_EXPIRED status, method name is %s ", logger.getMethodName());
            IotHubCallbackPacket callbackPacket = new IotHubCallbackPacket(IotHubStatusCode.MESSAGE_EXPIRED, packet.getCallback(), packet.getContext());
            this.callbackList.add(callbackPacket);
            return false;
        }

        if (this.defaultConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN && this.defaultConfig.getSasTokenAuthentication().isRenewalNecessary())
        {
            //Codes_SRS_AMQPSTRANSPORT_34_041: [If the config is using sas token authentication and its sas token has expired and cannot be renewed, the message shall not be sent, an UNAUTHORIZED message callback shall be added to the callback queue and SAS_TOKEN_EXPIRED state callback shall be fired.]
            logger.LogInfo("Creating a callback for the message with expired sas token with UNAUTHORIZED status, method name is %s ", logger.getMethodName());
            IotHubCallbackPacket callbackPacket = new IotHubCallbackPacket(IotHubStatusCode.UNAUTHORIZED, packet.getCallback(), packet.getContext());
            this.callbackList.add(callbackPacket);
            this.invokeStatusCallback(IotHubConnectionState.SAS_TOKEN_EXPIRED);

            return false;
            //Codes_SRS_AMQPSTRANSPORT_34_043: [If the config is using sas token authentication and its sas token has expired and cannot be renewed, the message shall not be put back into the waiting messages queue to be re-sent.]
        }

        return true;
    }

    private void invokeStatusCallback(IotHubConnectionState state)
    {
        if (this.stateCallback != null)
        {
            this.stateCallback.execute(state, this.stateCallbackContext);
        }
    }

    /** Invokes the callbacks for all completed requests. */
    public synchronized void invokeCallbacks()
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
     * @throws IOException if the server could not be reached.
     */
    public void handleMessage() throws DeviceClientException
    {
        synchronized (receiveLock)
        {
            // TODO: execute callback, sendMessage result, add to received queue if acked
            // received message queue could contain iothub transport message, in which case trigger callback on that transport message
            // Codes_SRS_AMQPSTRANSPORT_15_021: [If the transport is closed, the function shall throw an IllegalStateException.]

            if (this.state == State.OPEN)
            {
                logger.LogDebug("Get the callback function for the received message, method name is %s ", logger.getMethodName());

                // Codes_SRS_AMQPSTRANSPORT_15_023: [The function shall attempt to consume a message from the IoT Hub.]
                // Codes_SRS_AMQPSTRANSPORT_15_024: [If no message was received from IotHub, the function shall return.]
                if (this.receivedMessages.size() > 0)
                {
                    // execute callback, sendMessage result, add to received queue if acked

                    Message receivedMessage = this.receivedMessages.remove();
                    IotHubMessageResult result = null;
                    if (receivedMessage instanceof IotHubTransportMessage)
                    {
                        // As the callbacks may be different for DT, DM and telemetry, obtain the resultant CB and execute
                        result = ((IotHubTransportMessage) receivedMessage).getMessageCallback().
                                execute(receivedMessage,
                                       ((IotHubTransportMessage) receivedMessage).getMessageCallbackContext());
                    }
                    else
                    {
                        // TODO : For MQTT and HTTP, make sure the message in the Queue is IotHubTransport Message else message will never be received
                        throw new DeviceClientException("Unknown transport message could not be handled");
                    }

                    try
                    {
                        if (!this.iotHubTransportConnection.sendMessageResult(receivedMessage, result))
                        {
                            this.receivedMessages.add(receivedMessage);
                        }
                    }
                    catch (Exception e)
                    {
                        //TODO (Tim) this will only throw TranpsortException soon witch won't require a catch at all. Ignore this catch for now
                    }
                }
            }
        }
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
        return this.waitingMessages.isEmpty() && this.inProgressMessages.size() == 0 && this.callbackList.isEmpty();
    }

    /**
     * Registers a callback to be executed whenever the connection to the IoT Hub is lost or established.
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
