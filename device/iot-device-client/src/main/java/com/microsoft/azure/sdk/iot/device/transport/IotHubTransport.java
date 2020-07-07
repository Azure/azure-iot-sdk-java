/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.*;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsIotHubConnection;
import com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions.AmqpConnectionThrottledException;
import com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions.AmqpUnauthorizedAccessException;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsIotHubConnection;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttIotHubConnection;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions.MqttUnauthorizedException;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Manages queueing of message sending, receiving and callbacks. Manages notifying users of connection status change updates
 */
@Slf4j
public class IotHubTransport implements IotHubListener
{
    private static final int MAX_MESSAGES_TO_SEND_PER_THREAD = 10;
    private volatile IotHubConnectionStatus connectionStatus;
    private IotHubTransportConnection iotHubTransportConnection;

    /* Messages waiting to be sent to the IoT Hub. */
    private final Queue<IotHubTransportPacket> waitingPacketsQueue = new ConcurrentLinkedQueue<>();

    /* Messages which are sent to the IoT Hub but did not receive ack yet. */
    private final Map<String, IotHubTransportPacket> inProgressPackets = new ConcurrentHashMap<>();

    /* Messages received from the IoT Hub */
    private final Queue<IotHubTransportMessage> receivedMessagesQueue = new ConcurrentLinkedQueue<>();

    /* Messages whose callbacks that are waiting to be invoked. */
    private final Queue<IotHubTransportPacket> callbackPacketsQueue = new ConcurrentLinkedQueue<>();

    /*Connection Status callback information (deprecated)*/
    private IotHubConnectionStateCallback stateCallback;
    private Object stateCallbackContext;

    /*Connection Status change callback information */
    private IotHubConnectionStatusChangeCallback connectionStatusChangeCallback;
    private Object connectionStatusChangeCallbackContext;

    // Callback for notifying the DeviceIO layer of connection status change events. The deviceIO layer
    // should stop spawning send/receive threads when this layer is disconnected or disconnected retrying
    private IotHubConnectionStatusChangeCallback deviceIOConnectionStatusChangeCallback;

    //Lock on reading and writing on the inProgressPackets map
    final private Object inProgressMessagesLock = new Object();

    private DeviceClientConfig defaultConfig;
    private Queue<DeviceClientConfig> deviceClientConfigs;

    private int currentReconnectionAttempt;
    private long reconnectionAttemptStartTimeMillis;
    private ScheduledExecutorService taskScheduler;

    final private Object reconnectionLock = new Object();

    private ScheduledExecutorService scheduledExecutorService;
    private static final int POOL_SIZE = 1;

    // State lock used to communicate to the IotHubSendTask thread when a message needs to be sent or a callback needs to be invoked.
    // It is this layer's responsibility to notify that task each time a message is queued to send, or when a callback is queued to be invoked.
    private final Object sendThreadLock = new Object();

    // State lock used to communicate to the IotHubReceiveTask thread when a received message needs to be handled. It is this
    // layer's responsibility to notify that task each time a message is received.
    private final Object receiveThreadLock = new Object();

    /**
     * Constructor for an IotHubTransport object with default values
     * @param defaultConfig the config used for opening connections, retrieving retry policy, and checking protocol
     * @throws IllegalArgumentException if defaultConfig is null
     */
    public IotHubTransport(DeviceClientConfig defaultConfig, IotHubConnectionStatusChangeCallback deviceIOConnectionStatusChangeCallback) throws IllegalArgumentException
    {
        if (defaultConfig == null)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_002: [If the provided config is null, this function shall throw an
            // IllegalArgumentException.]
            throw new IllegalArgumentException("Config cannot be null");
        }

        //Codes_SRS_IOTHUBTRANSPORT_34_001: [The constructor shall save the default config.]
        this.defaultConfig = defaultConfig;

        //Codes_SRS_IOTHUBTRANSPORT_34_003: [The constructor shall set the connection status as DISCONNECTED and the
        // current retry attempt to 0.]
        this.connectionStatus = IotHubConnectionStatus.DISCONNECTED;
        this.currentReconnectionAttempt = 0;

        this.deviceIOConnectionStatusChangeCallback = deviceIOConnectionStatusChangeCallback;
    }

    public Object getSendThreadLock()
    {
        return this.sendThreadLock;
    }

    public Object getReceiveThreadLock()
    {
        return this.receiveThreadLock;
    }

    public boolean hasMessagesToSend()
    {
        synchronized (sendThreadLock)
        {
            return this.waitingPacketsQueue.size() > 0;
        }
    }

    public boolean hasReceivedMessagesToHandle()
    {
        synchronized (receiveThreadLock)
        {
            return this.receivedMessagesQueue.size() > 0;
        }
    }

    public boolean hasCallbacksToExecute()
    {
        synchronized (sendThreadLock)
        {
            return this.callbackPacketsQueue.size() > 0;
        }
    }

    public boolean isClosed()
    {
        return this.connectionStatus == IotHubConnectionStatus.DISCONNECTED;
    }

    @Override
    public void onMessageSent(Message message, Throwable e)
    {
        if (message == null)
        {
            log.warn("onMessageSent called with null message");
            return;
        }

        this.log.debug("IotHub message was acknowledged. Checking if there is record of sending this message ({})", message);

        // remove from in progress queue and add to callback queue
        IotHubTransportPacket packet = null;
        synchronized (this.inProgressMessagesLock)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_004: [This function shall retrieve a packet from the inProgressPackets
            // queue with the message id from the provided message if there is one.]
            packet = inProgressPackets.remove(message.getMessageId());
        }

        if (packet != null)
        {
            if (e == null)
            {
                //Codes_SRS_IOTHUBTRANSPORT_34_005: [If there was a packet in the inProgressPackets queue tied to the
                // provided message, and the provided throwable is null, this function shall set the status of that
                // packet to OK_EMPTY and add it to the callbacks queue.]
                this.log.trace("Message was sent by this client, adding it to callbacks queue with OK_EMPTY ({})", message);
                packet.setStatus(IotHubStatusCode.OK_EMPTY);
                this.addToCallbackQueue(packet);
            }
            else
            {
                if (e instanceof TransportException)
                {
                    //Codes_SRS_IOTHUBTRANSPORT_34_006: [If there was a packet in the inProgressPackets queue tied to
                    // the provided message, and the provided throwable is a TransportException, this function shall
                    // call "handleMessageException" with the provided packet and transport exception.]
                    this.handleMessageException(packet, (TransportException) e);
                }
                else
                {
                    //Codes_SRS_IOTHUBTRANSPORT_34_007: [If there was a packet in the inProgressPackets queue tied to
                    // the provided message, and the provided throwable is not a TransportException, this function
                    // shall call "handleMessageException" with the provided packet and a new transport exception with
                    // the provided exception as the inner exception.]
                    this.handleMessageException(packet, new TransportException(e));
                }
            }
        }
        else if (message != null)
        {
            log.warn("A message was acknowledged by IoT Hub, but this client has no record of sending it ({})", message);
        }
    }

    @Override
    public void onMessageReceived(IotHubTransportMessage message, Throwable e)
    {
        if (message != null && e != null)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_008: [If this function is called with a non-null message and a non-null
            // throwable, this function shall log an IllegalArgumentException.]
            this.log.error("Exception encountered while receiving a message from service {}", message, e);
        }
        else if (message != null)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_009: [If this function is called with a non-null message and a null
            // exception, this function shall add that message to the receivedMessagesQueue.]
            log.info("Message was received from IotHub ({})", message);
            this.addToReceivedMessagesQueue(message);
        }
        else
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_010: [If this function is called with a null message and a non-null
            // throwable, this function shall log that exception.]
            this.log.error("Exception encountered while receiving messages from service", e);
        }
    }

    @Override
    public void onConnectionLost(Throwable e, String connectionId)
    {
        synchronized (this.reconnectionLock)
        {
            if (!connectionId.equals(this.iotHubTransportConnection.getConnectionId()))
            {
                //Codes_SRS_IOTHUBTRANSPORT_34_078: [If this function is called with a connection id that is not the same
                // as the current connection id, this function shall do nothing.]

                //This connection status update is for a connection that is no longer tracked at this level, so it can be ignored.
                this.log.trace("OnConnectionLost was fired, but for an outdated connection. Ignoring...");
                return;
            }

            if (this.connectionStatus != IotHubConnectionStatus.CONNECTED)
            {
                //Codes_SRS_IOTHUBTRANSPORT_34_011: [If this function is called while the connection status is DISCONNECTED,
                // this function shall do nothing.]
                this.log.trace("OnConnectionLost was fired, but connection is already disocnnected. Ignoring...");
                return;
            }

            if (e instanceof TransportException)
            {
                //Codes_SRS_IOTHUBTRANSPORT_34_012: [If this function is called with a TransportException, this function
                // shall call handleDisconnection with that exception.]
                this.handleDisconnection((TransportException) e);
            }
            else
            {
                //Codes_SRS_IOTHUBTRANSPORT_34_013: [If this function is called with any other type of exception, this
                // function shall call handleDisconnection with that exception as the inner exception in a new
                // TransportException.]
                this.handleDisconnection(new TransportException(e));
            }
        }
    }

    @Override
    public void onConnectionEstablished(String connectionId)
    {
        if (connectionId.equals(this.iotHubTransportConnection.getConnectionId()))
        {
            log.info("The connection to the IoT Hub has been established");

            //Codes_SRS_IOTHUBTRANSPORT_34_014: [If the provided connectionId is associated with the current connection, This function shall invoke updateStatus with status CONNECTED, change
            // reason CONNECTION_OK and a null throwable.]
            this.updateStatus(IotHubConnectionStatus.CONNECTED, IotHubConnectionStatusChangeReason.CONNECTION_OK, null);
        }
    }

    /**
     * Establishes a communication channel with an IoT Hub. If a channel is
     * already open, the function shall do nothing.
     *
     * If reconnection is occurring when this is called, this function shall block and wait for the reconnection
     * to finish before trying to open the connection
     *
     * @param deviceClientConfigs the configs for the devices to open
     *
     * @throws DeviceClientException if a communication channel cannot be
     * established.
     */
    public void open(Collection<DeviceClientConfig> deviceClientConfigs) throws DeviceClientException
    {
        if ((deviceClientConfigs == null) || deviceClientConfigs.isEmpty())
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_015: [If the provided list of configs is null or empty, this function shall
            // throw an IllegalArgumentException.]
            throw new IllegalArgumentException("deviceClientConfigs cannot be null or empty");
        }

        if (this.connectionStatus == IotHubConnectionStatus.CONNECTED)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_017: [If the connection status of this object is CONNECTED, this function
            // shall do nothing.]
            return;
        }

        if (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED_RETRYING)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_016: [If the connection status of this object is DISCONNECTED_RETRYING, this
            // function shall throw a TransportException.]
            throw new TransportException("Open cannot be called while transport is reconnecting");
        }

        if (this.isSasTokenExpired())
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_018: [If the saved SAS token has expired, this function shall throw a
            // SecurityException.]
            throw new SecurityException("Your sas token has expired");
        }

        this.deviceClientConfigs = new LinkedBlockingQueue<>(deviceClientConfigs);
        this.defaultConfig = this.deviceClientConfigs.peek();
        this.taskScheduler = Executors.newScheduledThreadPool(1);

        //Codes_SRS_IOTHUBTRANSPORT_34_019: [This function shall open the invoke the method openConnection.]
        openConnection();

        log.info("Client connection opened successfully");
    }

    /**
     * Closes all resources used to communicate with an IoT Hub. Once {@code close()} is
     * called, the transport is no longer usable. If the transport is already
     * closed, the function shall do nothing.
     *
     * @param cause the cause of why this connection is closing, to be reported over connection status change callback
     * @param reason the reason to close this connection, to be reported over connection status change callback
     *
     * @throws DeviceClientException if an error occurs in closing the transport.
     */
    public void close(IotHubConnectionStatusChangeReason reason, Throwable cause) throws DeviceClientException
    {
        if (reason == null)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_026: [If the supplied reason is null, this function shall throw an
            // IllegalArgumentException.]
            throw new IllegalArgumentException("reason cannot be null");
        }

        this.cancelPendingPackets();

        //Codes_SRS_IOTHUBTRANSPORT_34_023: [This function shall invoke all callbacks.]
        this.invokeCallbacks();

        if (this.taskScheduler != null)
        {
            this.taskScheduler.shutdown();
        }

        if (this.scheduledExecutorService != null)
        {
            this.scheduledExecutorService.shutdownNow();
            this.scheduledExecutorService = null;
        }

        //Codes_SRS_IOTHUBTRANSPORT_34_024: [This function shall close the connection.]
        if (this.iotHubTransportConnection != null)
        {
            this.iotHubTransportConnection.close();
        }

        //Codes_SRS_IOTHUBTRANSPORT_34_025: [This function shall invoke updateStatus with status DISCONNECTED and the
        // supplied reason and cause.]
        this.updateStatus(IotHubConnectionStatus.DISCONNECTED, reason, cause);

        // Notify send thread to finish up so it doesn't survive this close
        synchronized (this.sendThreadLock)
        {
            this.sendThreadLock.notifyAll();
        }

        // Notify receive thread to finish up so it doesn't survive this close
        synchronized (this.receiveThreadLock)
        {
            this.receiveThreadLock.notifyAll();
        }

        log.info("Client connection closed successfully");
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
    public void addMessage(Message message, IotHubEventCallback callback, Object callbackContext)
    {
        if (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_041: [If this object's connection state is DISCONNECTED, this function shall
            // throw an IllegalStateException.]
            throw new IllegalStateException("Cannot add a message when the transport is closed.");
        }

        //Codes_SRS_IOTHUBTRANSPORT_34_042: [This function shall build a transport packet from the provided message,
        // callback, and context and then add that packet to the waiting queue.]
        IotHubTransportPacket packet = new IotHubTransportPacket(message, callback, callbackContext, null, System.currentTimeMillis());
        this.addToWaitingQueue(packet);

        log.info("Message was queued to be sent later ({})", message);
    }

    public IotHubClientProtocol getProtocol()
    {
        return this.defaultConfig.getProtocol();
    }

    /**
     * Sends all messages on the transport queue. If a previous send attempt had
     * failed, the function will attempt to resend the messages in the previous
     * attempt.
     */
    public void sendMessages()
    {
        checkForExpiredMessages();

        if (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED
                || this.connectionStatus == IotHubConnectionStatus.DISCONNECTED_RETRYING)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_043: [If the connection status of this object is not CONNECTED, this function shall do nothing]
            return;
        }

        int timeSlice = MAX_MESSAGES_TO_SEND_PER_THREAD;

        while (this.connectionStatus == IotHubConnectionStatus.CONNECTED && timeSlice-- > 0)
        {
            IotHubTransportPacket packet = waitingPacketsQueue.poll();
            if (packet != null)
            {
                Message message = packet.getMessage();
                log.trace("Dequeued a message from waiting queue to be sent ({})", message);

                if (message != null && this.isMessageValid(packet))
                {
                    //Codes_SRS_IOTHUBTRANSPORT_34_044: [This function continue to dequeue packets saved in the waiting
                    // queue and send them until connection status isn't CONNECTED or until 10 messages have been sent]
                    sendPacket(packet);
                }
            }
        }
    }

    private void checkForExpiredMessages()
    {
        //Check waiting packets, remove any that have expired.
        IotHubTransportPacket packet = this.waitingPacketsQueue.poll();
        Queue<IotHubTransportPacket> packetsToAddBackIntoWaitingPacketsQueue = new LinkedBlockingQueue<>();
        while (packet != null)
        {
            if (packet.getMessage().isExpired())
            {
                packet.setStatus(IotHubStatusCode.MESSAGE_EXPIRED);
                this.addToCallbackQueue(packet);
            }
            else
            {
                //message not expired, requeue it
                packetsToAddBackIntoWaitingPacketsQueue.add(packet);
            }

            packet = this.waitingPacketsQueue.poll();
        }

        //Requeue all the non-expired messages.
        this.waitingPacketsQueue.addAll(packetsToAddBackIntoWaitingPacketsQueue);

        //Check in progress messages
        synchronized (this.inProgressMessagesLock)
        {
            List<String> expiredPacketMessageIds = new ArrayList<>();
            for (String messageId : this.inProgressPackets.keySet())
            {
                if (this.inProgressPackets.get(messageId).getMessage().isExpired())
                {
                    expiredPacketMessageIds.add(messageId);
                }
            }

            for (String messageId : expiredPacketMessageIds)
            {
                IotHubTransportPacket expiredPacket = this.inProgressPackets.remove(messageId);
                expiredPacket.setStatus(IotHubStatusCode.MESSAGE_EXPIRED);
                this.addToCallbackQueue(expiredPacket);
            }
        }
    }

    /**
     * Invokes the callbacks for all completed requests.
     */
    public void invokeCallbacks()
    {
        IotHubTransportPacket packet = this.callbackPacketsQueue.poll();
        while (packet != null)
        {
            IotHubStatusCode status = packet.getStatus();
            IotHubEventCallback callback = packet.getCallback();
            Object context = packet.getContext();

            log.info("Invoking the callback function for sent message, IoT Hub responded to message ({}) with status {}", packet.getMessage(), status);

            //Codes_SRS_IOTHUBTRANSPORT_34_045: [This function shall dequeue each packet in the callback queue and
            // execute their saved callback with their saved status and context]
            callback.execute(status, context);

            packet = this.callbackPacketsQueue.poll();
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
     * @throws DeviceClientException if the server could not be reached.
     */
    public void handleMessage() throws DeviceClientException
    {
        //Codes_SRS_IOTHUBTRANSPORT_34_046: [If this object's connection status is not CONNECTED, this function shall do nothing.]
        if (this.connectionStatus == IotHubConnectionStatus.CONNECTED)
        {
            if (this.iotHubTransportConnection instanceof HttpsIotHubConnection)
            {
                this.log.trace("Sending http request to check for any cloud to device messages...");
                //Codes_SRS_IOTHUBTRANSPORT_34_047: [If this object's connection status is CONNECTED and is using HTTPS,
                // this function shall invoke addReceivedMessagesOverHttpToReceivedQueue.]
                addReceivedMessagesOverHttpToReceivedQueue();
            }

            IotHubTransportMessage receivedMessage = this.receivedMessagesQueue.poll();
            if (receivedMessage != null)
            {
                //Codes_SRS_IOTHUBTRANSPORT_34_048: [If this object's connection status is CONNECTED and there is a
                // received message in the queue, this function shall acknowledge the received message
                this.acknowledgeReceivedMessage(receivedMessage);
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
        synchronized (this.inProgressMessagesLock)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_043: [This function return true if and only if there are no packets in the
            // waiting queue, in progress, or in the callbacks queue.]
            return this.waitingPacketsQueue.isEmpty() && this.inProgressPackets.size() == 0 && this.callbackPacketsQueue.isEmpty();
        }
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
        if (callback == null)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_049: [If the provided callback is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Callback cannot be null");
        }

        //Codes_SRS_IOTHUBTRANSPORT_34_050: [This function shall save the provided callback and context.]
        this.stateCallback = callback;
        this.stateCallbackContext = callbackContext;
    }

    /**
     * Registers a callback to be executed whenever the connection status to the IoT Hub has changed.
     *
     * @param callback the callback to be called. Can be null if callbackContext is not null
     * @param callbackContext a context to be passed to the callback. Can be {@code null}.
     */
    public void registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext)
    {
        if (callbackContext != null && callback == null)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_051: [If the provided callback is null but the context is not, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Callback cannot be null if callback context is null");
        }

        //Codes_SRS_IOTHUBTRANSPORT_34_052: [This function shall save the provided callback and context.]
        this.connectionStatusChangeCallback = callback;
        this.connectionStatusChangeCallbackContext = callbackContext;
    }

    /**
     * Moves all packets from waiting queue and in progress map into callbacks queue with status MESSAGE_CANCELLED_ONCLOSE
     */
    private void cancelPendingPackets()
    {
        //Codes_SRS_IOTHUBTRANSPORT_34_021: [This function shall move all waiting messages to the callback queue with
        // status MESSAGE_CANCELLED_ONCLOSE.]
        IotHubTransportPacket packet = this.waitingPacketsQueue.poll();
        while (packet != null)
        {
            packet.setStatus(IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE);
            this.addToCallbackQueue(packet);

            packet = this.waitingPacketsQueue.poll();
        }

        synchronized (this.inProgressMessagesLock)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_022: [This function shall move all in progress messages to the callback queue
            // with status MESSAGE_CANCELLED_ONCLOSE.]
            for (Map.Entry<String, IotHubTransportPacket> packetEntry : inProgressPackets.entrySet())
            {
                IotHubTransportPacket inProgressPacket = packetEntry.getValue();
                inProgressPacket.setStatus(IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE);
                this.addToCallbackQueue(inProgressPacket);
            }

            inProgressPackets.clear();
        }
    }

    /**
     * If the provided received message has a saved callback, this function shall execute that callback and send the ack
     * to the service
     * @param receivedMessage the message to acknowledge
     * @throws TransportException if any exception is encountered while sending the acknowledgement
     */
    private void acknowledgeReceivedMessage(IotHubTransportMessage receivedMessage) throws TransportException
    {
        MessageCallback messageCallback = receivedMessage.getMessageCallback();
        Object messageCallbackContext = receivedMessage.getMessageCallbackContext();

        if (messageCallback != null)
        {
            this.log.debug("Executing callback for received message ({})", receivedMessage);
            //Codes_SRS_IOTHUBTRANSPORT_34_053: [This function shall execute the callback associate with the provided
            // transport message with the provided message and its saved callback context.]
            IotHubMessageResult result = messageCallback.execute(receivedMessage, messageCallbackContext);

            try
            {
                //Codes_SRS_IOTHUBTRANSPORT_34_054: [This function shall send the message callback result along the
                // connection as the ack to the service.]
                this.log.debug("Sending acknowledgement for received cloud to device message ({})", receivedMessage);
                this.iotHubTransportConnection.sendMessageResult(receivedMessage, result);
            }
            catch (TransportException e)
            {
                //Codes_SRS_IOTHUBTRANSPORT_34_055: [If an exception is thrown while acknowledging the received message,
                // this function shall add the received message back into the receivedMessagesQueue and then rethrow the exception.]
                this.log.warn("Sending acknowledgement for received cloud to device message failed, adding it back to the queue ({})", receivedMessage, e);
                this.addToReceivedMessagesQueue(receivedMessage);
                throw e;
            }
        }
    }

    /**
     * Checks if any messages were received over HTTP and adds all of them to the received messages queue
     * @throws TransportException if an exception occurs while receiving messages over HTTP connection
     */
    private void addReceivedMessagesOverHttpToReceivedQueue() throws TransportException
    {
        //since Http behaves synchronously, we need to check synchronously for any messages it may have received
        IotHubTransportMessage transportMessage = ((HttpsIotHubConnection)this.iotHubTransportConnection).receiveMessage();

        if (transportMessage != null)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_056: [If the saved http transport connection can receive a message, add it to receivedMessagesQueue.]
            log.info("Message was received from IotHub ({})", transportMessage);
            this.addToReceivedMessagesQueue(transportMessage);
        }
    }

    /**
     * Maps a given throwable to an IotHubConnectionStatusChangeReason
     * @param e the throwable to map to an IotHubConnectionStatusChangeReason
     * @return the mapped IotHubConnectionStatusChangeReason
     */
    private IotHubConnectionStatusChangeReason exceptionToStatusChangeReason(Throwable e)
    {
        if (e instanceof TransportException)
        {
            TransportException transportException = (TransportException) e;
            if (transportException.isRetryable())
            {
                this.log.debug("Mapping throwable to NO_NETWORK because it was a retryable exception", e);

                //Codes_SRS_IOTHUBTRANSPORT_34_033: [If the provided exception is a retryable TransportException,
                // this function shall return NO_NETWORK.]
                return IotHubConnectionStatusChangeReason.NO_NETWORK;
            }
            else if (isSasTokenExpired())
            {
                this.log.debug("Mapping throwable to EXPIRED_SAS_TOKEN because it was a non-retryable exception and the saved sas token has expired", e);

                //Codes_SRS_IOTHUBTRANSPORT_34_034: [If the provided exception is a TransportException that isn't
                // retryable and the saved sas token has expired, this function shall return EXPIRED_SAS_TOKEN.]
                return IotHubConnectionStatusChangeReason.EXPIRED_SAS_TOKEN;
            }
            else if (e instanceof UnauthorizedException || e instanceof MqttUnauthorizedException || e instanceof AmqpUnauthorizedAccessException)
            {
                this.log.debug("Mapping throwable to BAD_CREDENTIAL because it was a non-retryable exception authorization exception but the saved sas token has not expired yet", e);

                //Codes_SRS_IOTHUBTRANSPORT_34_035: [If the provided exception is a TransportException that isn't
                // retryable and the saved sas token has not expired, but the exception is an unauthorized exception,
                // this function shall return BAD_CREDENTIAL.]
                return IotHubConnectionStatusChangeReason.BAD_CREDENTIAL;
            }
        }

        this.log.debug("Mapping exception throwable to COMMUNICATION_ERROR because the sdk was unable to classify the thrown exception to anything other category", e);

        //Codes_SRS_IOTHUBTRANSPORT_34_032: [If the provided exception is not a TransportException, this function shall
        // return COMMUNICATION_ERROR.]
        return IotHubConnectionStatusChangeReason.COMMUNICATION_ERROR;
    }

    /**
     * Creates a new iotHubTransportConnection instance, sets this object as its listener, and opens that connection
     * @throws TransportException if any exception is thrown while opening the connection
     */
    private void openConnection() throws TransportException
    {
        scheduledExecutorService = Executors.newScheduledThreadPool(POOL_SIZE);

        if (this.iotHubTransportConnection == null)
        {
            switch (defaultConfig.getProtocol()) {
                case HTTPS:
                    //Codes_SRS_IOTHUBTRANSPORT_34_035: [If the default config's protocol is HTTPS, this function shall set
                    // this object's iotHubTransportConnection to a new HttpsIotHubConnection object.]
                    this.iotHubTransportConnection = new HttpsIotHubConnection(defaultConfig);
                    break;
                case MQTT:
                case MQTT_WS:
                    //Codes_SRS_IOTHUBTRANSPORT_34_036: [If the default config's protocol is MQTT or MQTT_WS, this function
                    // shall set this object's iotHubTransportConnection to a new MqttIotHubConnection object.]
                    this.iotHubTransportConnection = new MqttIotHubConnection(defaultConfig);
                    break;
                case AMQPS:
                case AMQPS_WS:
                    //Codes_SRS_IOTHUBTRANSPORT_34_037: [If the default config's protocol is AMQPS or AMQPS_WS, this
                    // function shall set this object's iotHubTransportConnection to a new AmqpsIotHubConnection object.]
                    this.iotHubTransportConnection = new AmqpsIotHubConnection(defaultConfig);
                    break;
                default:
                    throw new TransportException("Protocol not supported");
            }
        }

        //Codes_SRS_IOTHUBTRANSPORT_34_038: [This function shall set this object as the listener of the iotHubTransportConnection object.]
        this.iotHubTransportConnection.setListener(this);

        //Codes_SRS_IOTHUBTRANSPORT_34_039: [This function shall open the iotHubTransportConnection object with the saved list of configs.]
        this.iotHubTransportConnection.open(this.deviceClientConfigs, scheduledExecutorService);

        //Codes_SRS_IOTHUBTRANSPORT_34_040: [This function shall invoke the method updateStatus with status CONNECTED,
        // reason CONNECTION_OK, and a null throwable.]
        this.updateStatus(IotHubConnectionStatus.CONNECTED, IotHubConnectionStatusChangeReason.CONNECTION_OK, null);
    }

    /**
     * Attempts to reconnect. By the end of this call, the state of this object shall be either CONNECTED or DISCONNECTED
     * @param transportException the exception that caused the disconnection
     */
    private void handleDisconnection(TransportException transportException)
    {
        this.log.info("Handling a disconnection event", transportException);

        synchronized (this.inProgressMessagesLock)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_057: [This function shall move all packets from inProgressQueue to waiting queue.]
            this.log.trace("Due to disconnection event, clearing active queues, and re-queueing them to waiting queues to be re-processed later upon reconnection");
            for (IotHubTransportPacket packetToRequeue : inProgressPackets.values())
            {
                this.addToWaitingQueue(packetToRequeue);
            }

            inProgressPackets.clear();
        }

        //Codes_SRS_IOTHUBTRANSPORT_34_058: [This function shall invoke updateStatus with DISCONNECTED_RETRYING, and the provided transportException.]
        this.updateStatus(IotHubConnectionStatus.DISCONNECTED_RETRYING, exceptionToStatusChangeReason(transportException), transportException);

        //Codes_SRS_IOTHUBTRANSPORT_34_059: [This function shall invoke checkForUnauthorizedException with the provided exception.]
        checkForUnauthorizedException(transportException);

        this.log.debug("Starting reconnection logic");
        //Codes_SRS_IOTHUBTRANSPORT_34_060: [This function shall invoke reconnect with the provided exception.]
        reconnect(transportException);
    }

    /**
     * Attempts to close and then re-open the connection until connection reestablished, retry policy expires, or a
     * terminal exception is encountered. At the end of this call, the state of this object should be either
     * CONNECTED or DISCONNECTED depending on how reconnection goes.
     */
    private void reconnect(TransportException transportException)
    {
        if (this.reconnectionAttemptStartTimeMillis == 0)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_065: [If the saved reconnection attempt start time is 0, this function shall
            // save the current time as the time that reconnection started.]
            this.reconnectionAttemptStartTimeMillis = System.currentTimeMillis();
        }

        boolean hasReconnectOperationTimedOut = this.hasOperationTimedOut(this.reconnectionAttemptStartTimeMillis);
        RetryDecision retryDecision = null;

        //Codes_SRS_IOTHUBTRANSPORT_34_066: [This function shall attempt to reconnect while this object's state is
        // DISCONNECTED_RETRYING, the operation hasn't timed out, and the last transport exception is retryable.]
        while (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED_RETRYING
                && !hasReconnectOperationTimedOut
                && transportException != null
                && transportException.isRetryable())
        {
            this.log.trace("Attempting reconnect attempt {}", this.currentReconnectionAttempt);
            this.currentReconnectionAttempt++;

            RetryPolicy retryPolicy = this.defaultConfig.getRetryPolicy();
            retryDecision = retryPolicy.getRetryDecision(this.currentReconnectionAttempt, transportException);
            if (!retryDecision.shouldRetry())
            {
                break;
            }

            this.log.trace("Sleeping between reconnect attempts");
            //Want to sleep without interruption because the only interruptions expected are threads that add a message
            // to the waiting list again. Those threads should wait until after reconnection finishes first because
            // they will constantly fail until connection is re-established
            IotHubTransport.sleepUninterruptibly(retryDecision.getDuration(), MILLISECONDS);

            hasReconnectOperationTimedOut = this.hasOperationTimedOut(this.reconnectionAttemptStartTimeMillis);

            transportException = singleReconnectAttempt();
        }

        // reconnection may have failed, so check last retry decision, check for timeout, and check if last exception
        // was terminal
        try
        {
            if (retryDecision != null && !retryDecision.shouldRetry())
            {
                //Codes_SRS_IOTHUBTRANSPORT_34_068: [If the reconnection effort ends because the retry policy said to
                // stop, this function shall invoke close with RETRY_EXPIRED and the last transportException.]
                this.log.debug("Reconnection was abandoned due to the retry policy");
                this.close(IotHubConnectionStatusChangeReason.RETRY_EXPIRED, transportException);
            }
            else if (this.hasOperationTimedOut(this.reconnectionAttemptStartTimeMillis))
            {
                //Codes_SRS_IOTHUBTRANSPORT_34_069: [If the reconnection effort ends because the reconnection timed out,
                // this function shall invoke close with RETRY_EXPIRED and a DeviceOperationTimeoutException.]
                this.log.debug("Reconnection was abandoned due to the operation timeout");
                this.close(
                        IotHubConnectionStatusChangeReason.RETRY_EXPIRED,
                        new DeviceOperationTimeoutException("Device operation for reconnection timed out"));
            }
            else if (transportException != null && !transportException.isRetryable())
            {
                //Codes_SRS_IOTHUBTRANSPORT_34_070: [If the reconnection effort ends because a terminal exception is
                // encountered, this function shall invoke close with that terminal exception.]
                this.log.error("Reconnection was abandoned due to encountering a non-retryable exception", transportException);
                this.close(this.exceptionToStatusChangeReason(transportException), transportException);
            }
        }
        catch (DeviceClientException ex)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_071: [If an exception is encountered while closing, this function shall invoke
            // updateStatus with DISCONNECTED, COMMUNICATION_ERROR, and the last transport exception.]
            this.log.error("Encountered an exception while closing the client object, client instance should no longer be used as the state is unknown", ex);
            this.updateStatus(IotHubConnectionStatus.DISCONNECTED, IotHubConnectionStatusChangeReason.COMMUNICATION_ERROR, transportException);
        }
    }

    /**
     * Attempts to close and then re-open the iotHubTransportConnection once
     * @return the exception encountered during closing or opening, or null if reconnection succeeded
     */
    private TransportException singleReconnectAttempt()
    {
        try
        {
            this.log.trace("Attempting to close and re-open the iot hub transport connection...");
            //Codes_SRS_IOTHUBTRANSPORT_34_061: [This function shall close the saved connection, and then invoke openConnection and return null.]
            this.iotHubTransportConnection.close();
            this.openConnection();
            this.log.trace("Successfully closed and re-opened the iot hub transport connection");
        }
        catch (TransportException newTransportException)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_062: [If an exception is encountered while closing or opening the connection,
            // this function shall invoke checkForUnauthorizedException on that exception and then return it.]
            checkForUnauthorizedException(newTransportException);
            this.log.warn("Failed to close and re-open the iot hub transport connection, checking if another retry attempt should be made", newTransportException);
            return newTransportException;
        }

        return null;
    }

    /**
     * Task for adding a packet back to the waiting queue. Used for delaying message retry
     */
    public class MessageRetryRunnable implements Runnable
    {
        final IotHubTransportPacket transportPacket;
        final Queue<IotHubTransportPacket> waitingPacketsQueue;
        final Object sendThreadLock;

        public MessageRetryRunnable(Queue<IotHubTransportPacket> waitingPacketsQueue, IotHubTransportPacket transportPacket, Object sendThreadLock)
        {
            this.waitingPacketsQueue = waitingPacketsQueue;
            this.transportPacket = transportPacket;
            this.sendThreadLock = sendThreadLock;
        }

        @Override
        public void run()
        {
            this.waitingPacketsQueue.add(this.transportPacket);

            // Wake up send messages thread so that it can send this message
            this.sendThreadLock.notifyAll();
        }
    }

    /**
     * Spawn a task to add the provided packet back to the waiting list if the provided transportException is retryable
     * and if the message hasn't timed out
     * @param packet the packet to retry
     * @param transportException the exception encountered while sending this packet before
     */
    private void handleMessageException(IotHubTransportPacket packet, TransportException transportException)
    {
        this.log.warn("Handling an exception from sending message: Attempt number {}", packet.getCurrentRetryAttempt(), transportException);

        packet.incrementRetryAttempt();
        if (!this.hasOperationTimedOut(packet.getStartTimeMillis()))
        {
            if (transportException.isRetryable())
            {
                RetryDecision retryDecision = this.defaultConfig.getRetryPolicy().getRetryDecision(packet.getCurrentRetryAttempt(), transportException);
                if (retryDecision.shouldRetry())
                {
                    //Codes_SRS_IOTHUBTRANSPORT_34_063: [If the provided transportException is retryable, the packet has not
                    // timed out, and the retry policy allows, this function shall schedule a task to add the provided
                    // packet to the waiting list after the amount of time determined by the retry policy.]
                    this.taskScheduler.schedule(new MessageRetryRunnable(this.waitingPacketsQueue, packet, this), retryDecision.getDuration(), MILLISECONDS);
                    return;
                }
                else
                {
                    this.log.warn("Retry policy dictated that the message should be abandoned, so it has been abandoned ({})", packet.getMessage(), transportException);
                }
            }
            else
            {
                this.log.warn("Encountering an non-retryable exception while sending a message, so it has been abandoned ({})", packet.getMessage(), transportException);
            }
        }
        else
        {
            this.log.warn("The device operation timeout has been exceeded for the message, so it has been abandoned ({})", packet.getMessage(), transportException);
        }

        //Codes_SRS_IOTHUBTRANSPORT_34_064: [If the provided transportException is not retryable, the packet has expired,
        // or if the retry policy says to not retry, this function shall add the provided packet to the callback queue.]
        IotHubStatusCode errorCode = (transportException instanceof IotHubServiceException) ?
                ((IotHubServiceException) transportException).getStatusCode() : IotHubStatusCode.ERROR;

        if (transportException instanceof AmqpConnectionThrottledException)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_079: [If the provided transportException is an AmqpConnectionThrottledException,
            // this function shall set the status of the callback packet to the error code for THROTTLED.]
            errorCode = IotHubStatusCode.THROTTLED;
        }

        packet.setStatus(errorCode);
        this.addToCallbackQueue(packet);
    }

    /**
     * Sends a single packet over the iotHubTransportConnection and handles the response
     * @param packet the packet to send
     */
    private void sendPacket(IotHubTransportPacket packet)
    {
        Message message = packet.getMessage();

        //Codes_SRS_IOTHUBTRANSPORT_34_072: [This function shall check if the provided message should expect an ACK or not.]
        boolean messageAckExpected = !(message instanceof IotHubTransportMessage
                && !((IotHubTransportMessage) message).isMessageAckNeeded(this.defaultConfig.getProtocol()));

        try
        {
            if (messageAckExpected)
            {
                synchronized (this.inProgressMessagesLock)
                {
                    this.log.trace("Adding transport message to the inProgressPackets to wait for acknowledgement ({})", message);
                    this.inProgressPackets.put(message.getMessageId(), packet);
                }
            }

            //Codes_SRS_IOTHUBTRANSPORT_34_073: [This function shall send the provided message over the saved connection
            // and save the response code.]
            this.log.info("Sending message ({})", message);
            IotHubStatusCode statusCode = this.iotHubTransportConnection.sendMessage(message);
            this.log.trace("Sent message ({}) to protocol level, returned status code was {}", message, statusCode);

            if (statusCode != IotHubStatusCode.OK_EMPTY && statusCode != IotHubStatusCode.OK)
            {
                //Codes_SRS_IOTHUBTRANSPORT_34_074: [If the response from sending is not OK or OK_EMPTY, this function
                // shall invoke handleMessageException with that message.]
                this.handleMessageException(this.inProgressPackets.remove(message.getMessageId()), IotHubStatusCode.getConnectionStatusException(statusCode, ""));
            }
            else if (!messageAckExpected)
            {
                //Codes_SRS_IOTHUBTRANSPORT_34_075: [If the response from sending is OK or OK_EMPTY and no ack is expected,
                // this function shall put that set that status in the sent packet and add that packet to the callbacks queue.]
                packet.setStatus(statusCode);
                this.addToCallbackQueue(packet);
            }
        }
        catch (TransportException transportException)
        {
            this.log.warn("Encountered exception while sending message with correlation id {}", message.getCorrelationId(), transportException);
            IotHubTransportPacket outboundPacket;

            if (messageAckExpected)
            {
                synchronized (this.inProgressMessagesLock)
                {
                    outboundPacket = this.inProgressPackets.remove(message.getMessageId());
                }
            }
            else
            {
                outboundPacket = packet;
            }

            //Codes_SRS_IOTHUBTRANSPORT_34_076: [If an exception is encountered while sending the message, this function
            // shall invoke handleMessageException with that packet.]
            this.handleMessageException(outboundPacket, transportException);
        }
    }

    /**
     * Checks if the provided packet has expired or if the sas token has expired
     * @param packet the packet to check for expiry
     * @return if the message has not expired and if the sas token has not expired
     */
    private boolean isMessageValid(IotHubTransportPacket packet)
    {
        Message message = packet.getMessage();

        if (message.isExpired())
        {
            //Codes_SRS_IOTHUBTRANSPORT_28_008:[This function shall set the packet status to MESSAGE_EXPIRED if packet has expired.]
            //Codes_SRS_IOTHUBTRANSPORT_28_009:[This function shall add the expired packet to the Callback Queue.]
            this.log.warn("Message with has expired, adding to callbacks queue with MESSAGE_EXPIRED ({})", message);
            packet.setStatus(IotHubStatusCode.MESSAGE_EXPIRED);
            this.addToCallbackQueue(packet);
            return false;
        }

        if (isSasTokenExpired())
        {
            //Codes_SRS_IOTHUBTRANSPORT_28_010:[This function shall set the packet status to UNAUTHORIZED if sas token has expired.]
            //Codes_SRS_IOTHUBTRANSPORT_28_011:[This function shall add the packet which sas token has expired to the Callback Queue.]
            log.info("Creating a callback for the message with expired sas token with UNAUTHORIZED status");
            packet.setStatus(IotHubStatusCode.UNAUTHORIZED);
            this.addToCallbackQueue(packet);
            this.updateStatus(
                    IotHubConnectionStatus.DISCONNECTED,
                    IotHubConnectionStatusChangeReason.EXPIRED_SAS_TOKEN,
                    new SecurityException("Your sas token has expired"));

            return false;
        }

        return true;
    }

    /**
     * If the provided newConnectionStatus is different from the current connection status, this function shall update
     * the saved connection status and notify all callbacks listening for connection state changes
     * @param newConnectionStatus the new connection status
     * @param reason the reason for the new connection status
     * @param throwable the associated exception to the connection status change
     */
    private void updateStatus(IotHubConnectionStatus newConnectionStatus, IotHubConnectionStatusChangeReason reason, Throwable throwable)
    {
        //Codes_SRS_IOTHUBTRANSPORT_28_005:[This function shall updated the saved connection status if the connection status has changed.]
        //Codes_SRS_IOTHUBTRANSPORT_28_006:[This function shall invoke all callbacks listening for the state change if the connection status has changed.]
        if (this.connectionStatus != newConnectionStatus)
        {
            if (throwable == null)
            {
                this.log.info("Updating transport status to new status {} with reason {}", newConnectionStatus, reason);
            }
            else
            {
                this.log.warn("Updating transport status to new status {} with reason {}", newConnectionStatus, reason, throwable);
            }

            this.connectionStatus = newConnectionStatus;

            //invoke connection status callbacks
            this.log.debug("Invoking connection status callbacks with new status details");
            invokeConnectionStateCallback(newConnectionStatus, reason);
            invokeConnectionStatusChangeCallback(newConnectionStatus, reason, throwable);
            this.deviceIOConnectionStatusChangeCallback.execute(newConnectionStatus, reason, throwable, null);

            if (newConnectionStatus == IotHubConnectionStatus.CONNECTED)
            {
                //Codes_SRS_IOTHUBTRANSPORT_28_007: [This function shall reset currentReconnectionAttempt and reconnectionAttemptStartTimeMillis if connection status is changed to CONNECTED.]
                this.currentReconnectionAttempt = 0;
                this.reconnectionAttemptStartTimeMillis = 0;
            }
        }
    }

    /**
     * Notify the deprecated connection state callback if the provided status bears reporting
     * @param status the status to report
     * @param reason the reason to report
     */
    private void invokeConnectionStateCallback(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason reason)
    {
        if (this.stateCallback != null)
        {
            if (status == IotHubConnectionStatus.CONNECTED)
            {
                this.stateCallback.execute(IotHubConnectionState.CONNECTION_SUCCESS, this.stateCallbackContext);
            }
            else if (reason == IotHubConnectionStatusChangeReason.EXPIRED_SAS_TOKEN)
            {
                this.stateCallback.execute(IotHubConnectionState.SAS_TOKEN_EXPIRED, this.stateCallbackContext);
            }
            else if (status == IotHubConnectionStatus.DISCONNECTED)
            {
                this.stateCallback.execute(IotHubConnectionState.CONNECTION_DROP, this.stateCallbackContext);
            }
        }
    }

    /**
     * Notify the connection status change callback
     * @param status the status to notify of
     * @param reason the reason for that status
     * @param e the associated exception. May be null
     */
    private void invokeConnectionStatusChangeCallback(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason reason, Throwable e)
    {
        //Codes_SRS_IOTHUBTRANSPORT_28_004:[This function shall notify the connection status change callback if the callback is not null]
        if (this.connectionStatusChangeCallback != null)
        {
            this.connectionStatusChangeCallback.execute(status, reason, e, this.connectionStatusChangeCallbackContext);
        }
    }

    /**
     * @return if the saved sas token has expired and needs manual renewal
     */
    private boolean isSasTokenExpired()
    {
        //Codes_SRS_IOTHUBTRANSPORT_28_003: [This function shall indicate if the device's sas token is expired.]
        return this.defaultConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN
                && this.defaultConfig.getSasTokenAuthentication().isRenewalNecessary();
    }

    /**
     * Returns if the provided packet has lasted longer than the device operation timeout
     * @return true if the packet has been in the queues for longer than the device operation timeout and false otherwise
     */
    private boolean hasOperationTimedOut(long startTime)
    {
        if (startTime == 0)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_077: [If the provided start time is 0, this function shall return false.]
            return false;
        }

        //Codes_SRS_IOTHUBTRANSPORT_34_044: [This function shall return if the provided start time was long enough ago
        // that it has passed the device operation timeout threshold.]
        return (System.currentTimeMillis() - startTime) > this.defaultConfig.getOperationTimeout();
    }

    /**
     * Adds the packet to the callback queue if the provided packet has a callback. The packet is ignored otherwise.
     * @param packet the packet to add
     */
    private void addToCallbackQueue(IotHubTransportPacket packet)
    {
        //Codes_SRS_IOTHUBTRANSPORT_28_002: [This function shall add the packet to the callback queue if it has a callback.]
        if (packet.getCallback() != null)
        {
            synchronized (this.sendThreadLock)
            {
                this.callbackPacketsQueue.add(packet);

                //Wake up send messages thread so that it can process this new callback if it was asleep
                this.sendThreadLock.notifyAll();
            }
        }
    }

    private void addToWaitingQueue(IotHubTransportPacket packet)
    {
        synchronized (this.sendThreadLock)
        {
            this.waitingPacketsQueue.add(packet);

            // Wake up IotHubSendTask so it can send this message
            this.sendThreadLock.notifyAll();
        }
    }

    private void addToReceivedMessagesQueue(IotHubTransportMessage message)
    {
        synchronized (this.receiveThreadLock)
        {
            this.receivedMessagesQueue.add(message);

            // Wake up IotHubReceiveTask so it can handle receiving this message
            this.receiveThreadLock.notifyAll();
        }
    }

    /**
     * Sleep for a length of time without interruption
     * @param sleepFor length of time to sleep for
     * @param unit time unit associated with sleepFor
     */
    private static void sleepUninterruptibly(long sleepFor, TimeUnit unit)
    {
        boolean interrupted = false;
        try
        {
            long remainingNanos = unit.toNanos(sleepFor);
            long end = System.nanoTime() + remainingNanos;
            while (true)
            {
                try
                {
                    NANOSECONDS.sleep(remainingNanos);
                    return;
                }
                catch (InterruptedException e)
                {
                    interrupted = true;
                    remainingNanos = end - System.nanoTime();
                }
            }
        }
        finally
        {
            if (interrupted)
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * If Unauthorized exception occurs, but sas token has not expired, this function sets the provided
     * transportException as retryable
     * @param transportException the transport exception to check
     */
    private void checkForUnauthorizedException(TransportException transportException)
    {
        //Codes_SRS_IOTHUBTRANSPORT_28_001: [This function shall set the MqttUnauthorizedException, UnauthorizedException or
        //AmqpUnauthorizedAccessException as retryable if the sas token has not expired.]
        if (!this.isSasTokenExpired() && (transportException instanceof MqttUnauthorizedException
                || transportException instanceof UnauthorizedException
                || transportException instanceof AmqpUnauthorizedAccessException))
        {
            //Device key is present, sas token will be renewed upon re-opening the connection
            transportException.setRetryable(true);
        }
    }
}
