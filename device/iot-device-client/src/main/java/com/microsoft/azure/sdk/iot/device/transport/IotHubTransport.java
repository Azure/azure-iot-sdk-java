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
import com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions.AmqpUnauthorizedAccessException;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsIotHubConnection;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttIotHubConnection;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions.MqttUnauthorizedException;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Manages queueing of message sending, receiving and callbacks. Manages notifying users of connection status change updates
 */
public class IotHubTransport implements IotHubListener
{
    private static final int MAX_MESSAGES_TO_SEND_PER_THREAD = 10;
    private IotHubConnectionStatus connectionStatus;
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

    //Lock on reading and writing on the inProgressPackets map
    final private Object inProgressMessagesLock = new Object();

    private DeviceClientConfig defaultConfig;
    private Queue<DeviceClientConfig> deviceClientConfigs;

    private int currentReconnectionAttempt;
    private long reconnectionAttemptStartTimeMillis;
    private ScheduledExecutorService taskScheduler;

    private final CustomLogger logger;

    public IotHubTransport(DeviceClientConfig defaultConfig)
    {
        this.connectionStatus = IotHubConnectionStatus.DISCONNECTED;
        this.defaultConfig = defaultConfig;
        this.logger = new CustomLogger(this.getClass());
        this.currentReconnectionAttempt = 0;
    }

    @Override
    public void onMessageSent(Message message, Throwable e)
    {
        // remove from in progress queue and add to callback queue
        IotHubTransportPacket packet;
        synchronized (this.inProgressMessagesLock)
        {
            packet = inProgressPackets.remove(message.getMessageId());
        }

        if (packet != null)
        {
            if (e == null)
            {
                packet.setStatus(IotHubStatusCode.OK);
                this.callbackPacketsQueue.add(packet);
            }
            else
            {
                if (e instanceof TransportException)
                {
                    this.handleMessageException(packet, (TransportException) e);
                }
                else
                {
                    this.handleMessageException(packet, new TransportException(e));
                }
            }
        }
        else
        {
            logger.LogError("Message with message id %s was delivered to IoTHub, but was never sent, method name is %s ", message.getMessageId(), logger.getMethodName());
        }
    }

    @Override
    public void onMessageReceived(IotHubTransportMessage message, Throwable e)
    {
        if (message != null && e != null)
        {
            this.logger.LogError("IllegalArgumentException encountered, method name is %s", this.logger.getMethodName());
            this.logger.LogError(new IllegalArgumentException("Cannot call onMessageReceived with non-null message and non-null throwable"));
        }
        else if (message != null)
        {
            logger.LogInfo("Message with hashcode %s is received from IotHub on %s, method name is %s ", message.hashCode(), new Date(), logger.getMethodName());
            this.receivedMessagesQueue.add(message);
        }
        else if (e != null)
        {
            this.logger.LogError("Exception encountered while receiving messages from service, method name is %s", this.logger.getMethodName());
            this.logger.LogError(e);
        }
    }

    @Override
    public void onConnectionLost(Throwable e)
    {
        if (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED)
        {
            //Codes_SRS_IOTHUBTRANSPORTPACKET_34_011: [If this function is called while the connection status is DISCONNECTED, this function shall do nothing.]
            //Don't want to try to reconnect because user is still opening manually or they already closed connection anyways
            return;
        }

        if (e instanceof TransportException)
        {
            this.handleDisconnection((TransportException) e);
        }
        else
        {
            this.handleDisconnection(new TransportException(e));
        }
    }

    @Override
    public void onConnectionEstablished()
    {
        logger.LogInfo("The connection to the IoT Hub has been established, method name is %s ", logger.getMethodName());
        this.updateStatus(IotHubConnectionStatus.CONNECTED, IotHubConnectionStatusChangeReason.CONNECTION_OK, null);
    }

    /**
     * Establishes a communication channel with an IoT Hub. If a channel is
     * already open, the function shall do nothing.
     *
     * If reconnection is occurring when this is called, this function shall block and wait for the reconnection
     * to finish before trying to open the connection
     *
     * @throws DeviceClientException if a communication channel cannot be
     * established.
     */
    public void open(Collection<DeviceClientConfig> deviceClientConfigs) throws DeviceClientException
    {
        if ((deviceClientConfigs == null) || deviceClientConfigs.isEmpty())
        {
            throw new IllegalArgumentException("deviceClientConfigs cannot be null or empty");
        }

        if (this.connectionStatus == IotHubConnectionStatus.CONNECTED)
        {
            return;
        }

        this.deviceClientConfigs = new LinkedBlockingQueue<>(deviceClientConfigs);
        this.defaultConfig = this.deviceClientConfigs.peek();
        if (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED_RETRYING)
        {
            throw new TransportException("Open cannot be called while transport is reconnecting");
        }

        if (this.sasTokenHasExpired())
        {
            throw new SecurityException("Your sas token has expired");
        }

        this.taskScheduler = Executors.newScheduledThreadPool(1);

        openConnection();
    }

    /**
     * Closes all resources used to communicate with an IoT Hub. Once {@code close()} is
     * called, the transport is no longer usable. If the transport is already
     * closed, the function shall do nothing.
     *
     * @throws DeviceClientException if an error occurs in closing the transport.
     */
    public void close(IotHubConnectionStatusChangeReason reason, Throwable cause) throws DeviceClientException
    {
        if (reason == null)
        {
            throw new IllegalArgumentException("reason cannot be null");
        }

        if (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED)
        {
            return;
        }

        // Move waiting messages to callback to inform user of close
        IotHubTransportPacket packet = this.waitingPacketsQueue.poll();
        while (packet != null)
        {
            packet.setStatus(IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE);
            this.callbackPacketsQueue.add(packet);

            packet = this.waitingPacketsQueue.poll();
        }

        synchronized (this.inProgressMessagesLock)
        {
            // Move in progress message to callback to inform user of close
            for (Map.Entry<String, IotHubTransportPacket> packetEntry : inProgressPackets.entrySet())
            {
                IotHubTransportPacket inProgressPacket = packetEntry.getValue();
                inProgressPacket.setStatus(IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE);
                this.callbackPacketsQueue.add(inProgressPacket);
            }

            inProgressPackets.clear();
        }

        // invoke all the callbacks
        invokeCallbacks();

        this.taskScheduler.shutdown();

        this.iotHubTransportConnection.close();

        this.updateStatus(IotHubConnectionStatus.DISCONNECTED, reason, cause);
    }

    private IotHubConnectionStatusChangeReason exceptionToStatusChangeReason(Throwable e)
    {
        if (e instanceof TransportException)
        {
            TransportException transportException = (TransportException) e;
            if (transportException.isRetryable())
            {
                return IotHubConnectionStatusChangeReason.NO_NETWORK;
            }
            else if (sasTokenHasExpired())
            {
                return IotHubConnectionStatusChangeReason.EXPIRED_SAS_TOKEN;
            }
            else
            {
                return IotHubConnectionStatusChangeReason.BAD_CREDENTIAL;
            }
        }

        return IotHubConnectionStatusChangeReason.COMMUNICATION_ERROR;
    }

    private void openConnection() throws TransportException
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
                this.iotHubTransportConnection = new AmqpsIotHubConnection(defaultConfig);
                break;
            default:
                throw new TransportException("Protocol not supported");
        }

        this.iotHubTransportConnection.setListener(this);
        this.iotHubTransportConnection.open(this.deviceClientConfigs);
        this.updateStatus(IotHubConnectionStatus.CONNECTED, IotHubConnectionStatusChangeReason.CONNECTION_OK, null);
    }

    private void handleDisconnection(TransportException transportException)
    {
        // Move in progress to waiting as they may not have sent
        logger.LogInfo("The messages in progress are buffered to be sent again due to a connection loss, method name is %s ", logger.getMethodName());

        // Codes_SRS_AMQPSTRANSPORT_15_032: [The messages in progress are buffered to be sent again.]
        synchronized (this.inProgressMessagesLock)
        {
            this.waitingPacketsQueue.addAll(inProgressPackets.values());
            inProgressPackets.clear();
        }

        this.reconnectionAttemptStartTimeMillis = System.currentTimeMillis();
        boolean hasReconnectOperationTimedOut = this.hasOperationTimedOut(this.reconnectionAttemptStartTimeMillis);
        RetryDecision retryDecision = null;
        this.updateStatus(IotHubConnectionStatus.DISCONNECTED_RETRYING, exceptionToStatusChangeReason(transportException), transportException);
        while (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED_RETRYING && !hasReconnectOperationTimedOut && transportException.isRetryable())
        {
            this.currentReconnectionAttempt++;
            retryDecision = this.defaultConfig.getRetryPolicy().getRetryDecision(this.currentReconnectionAttempt, transportException);
            if (!retryDecision.shouldRetry())
            {
                break;
            }

            //Want to sleep without interruption because the only interruptions expected are threads that add a message to the waiting list again.
            // Those threads should wait until after reconnection finishes first because they will constantly fail until connection is re-established
            IotHubTransport.sleepUninterruptibly((long) retryDecision.getDuration().toMillis(), MILLISECONDS);
            hasReconnectOperationTimedOut = this.hasOperationTimedOut(this.reconnectionAttemptStartTimeMillis);

            try
            {
                handleConnectionException(transportException);
            }
            catch (TransportException newTransportException)
            {
                // New exception encountered shall override the previous exception
                transportException = newTransportException;
            }
        }

        // Check this value again as the above loop may have exited due to retry expiring
        Throwable cause = transportException;
        if (retryDecision != null && !retryDecision.shouldRetry() || hasReconnectOperationTimedOut)
        {
            if (hasReconnectOperationTimedOut)
            {
                cause = new DeviceOperationTimeoutException("Device operation for reconnecting has timed out", transportException);
            }

            try
            {
                this.close(IotHubConnectionStatusChangeReason.RETRY_EXPIRED, cause);
            }
            catch (DeviceClientException closeException)
            {
                this.updateStatus(IotHubConnectionStatus.DISCONNECTED, IotHubConnectionStatusChangeReason.RETRY_EXPIRED, cause);
            }
        }
        else if (!transportException.isRetryable())
        {
            try
            {
                this.close(this.exceptionToStatusChangeReason(transportException), transportException);
            }
            catch (DeviceClientException e)
            {
                this.updateStatus(IotHubConnectionStatus.DISCONNECTED, IotHubConnectionStatusChangeReason.COMMUNICATION_ERROR, transportException);
            }
        }
    }

    /**
     * Analyze the provided exception and act on it accordingly
     *
     * @param e The exception to handle
     * @throws DeviceClientException If attempts to handle the provided exception caused another exception
     */
    private void handleConnectionException(Throwable e) throws TransportException
    {
        if (e != null)
        {
            if ((e instanceof MqttUnauthorizedException || e instanceof UnauthorizedException || e instanceof AmqpUnauthorizedAccessException) && !this.sasTokenHasExpired())
            {
                //Device key is present, sas token will be renewed upon re-opening the connection
                ((TransportException) e).setRetryable(true);
            }

            this.iotHubTransportConnection.close();

            //Only re-open connection if exception is retryable and not IotHubService (Those are retried elsewhere)
            if (e instanceof TransportException
                    && ((TransportException) e).isRetryable()
                    && !(e instanceof IotHubServiceException))
            {
                this.openConnection();
            }
        }
    }

    private class MessageRetryRunnable implements Runnable
    {
        IotHubTransportPacket transportPacket;
        Queue<IotHubTransportPacket> waitingPacketsQueue;

        MessageRetryRunnable(Queue<IotHubTransportPacket> waitingPacketsQueue, IotHubTransportPacket transportPacket)
        {
            this.waitingPacketsQueue = waitingPacketsQueue;
            this.transportPacket = transportPacket;
        }

        @Override
        public void run()
        {
            this.waitingPacketsQueue.add(this.transportPacket);
        }
    }

    private void handleMessageException(IotHubTransportPacket packet, TransportException transportException)
    {
        packet.incrementRetryAttempt();
        if (!this.hasOperationTimedOut(packet.getStartTimeMillis()) || !(transportException.isRetryable()))
        {
            RetryDecision retryDecision = this.defaultConfig.getRetryPolicy().getRetryDecision(packet.getCurrentRetryAttempt(), transportException);
            if (retryDecision.shouldRetry())
            {
                // message will be retried
                this.taskScheduler.schedule(new MessageRetryRunnable(this.waitingPacketsQueue, packet), (long) retryDecision.getDuration().toMillis(), MILLISECONDS);
                return;
            }
        }

        //If transport exception has iot hub status code, use it, otherwise ERROR
        IotHubStatusCode errorCode = (transportException instanceof IotHubServiceException) ? ((IotHubServiceException) transportException).getStatusCode() : IotHubStatusCode.ERROR;
        packet.setStatus(errorCode);
        this.callbackPacketsQueue.add(packet);
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
        if (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED)
        {
            logger.LogError("Cannot add a message when the transport is closed, method name is %s ", logger.getMethodName());
            throw new IllegalStateException("Cannot add a message when the transport is closed.");
        }

        // Codes_SRS_AMQPSTRANSPORT_15_011: [The function shall add a packet containing the message, callback, and callback context to the queue of messages waiting to be sent.]
        IotHubTransportPacket packet = new IotHubTransportPacket(message, callback, callbackContext, null, System.currentTimeMillis());
        this.waitingPacketsQueue.add(packet);
    }

    /**
     * Sends all messages on the transport queue. If a previous send attempt had
     * failed, the function will attempt to resend the messages in the previous
     * attempt.
     *
     * @throws DeviceClientException if the server could not be reached.
     */
    public void sendMessages()
    {
        // Codes_SRS_AMQPSTRANSPORT_15_012: [If the AMQPS session is closed, the function shall throw an IllegalStateException.]
        if (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED || this.connectionStatus == IotHubConnectionStatus.DISCONNECTED_RETRYING)
        {
            logger.LogError("Cannot send messages when the AMQPS transport is closed, method name is %s ", logger.getMethodName());
            throw new IllegalStateException("Cannot send messages when the AMQPS transport is closed.");
        }

        int timeSlice = MAX_MESSAGES_TO_SEND_PER_THREAD;

        while (this.connectionStatus == IotHubConnectionStatus.CONNECTED && timeSlice-- > 0)
        {
            IotHubTransportPacket packet = waitingPacketsQueue.poll();
            if (packet != null)
            {
                logger.LogInfo("Get the message from waiting message queue to be sent to IoT Hub, method name is %s ", logger.getMethodName());
                Message message = packet.getMessage();

                if (message != null && this.isMessageValid(packet) && this.connectionStatus == IotHubConnectionStatus.CONNECTED)
                {
                    boolean messageAckExpected = !(message instanceof IotHubTransportMessage
                            && !((IotHubTransportMessage) message).isMessageAckNeeded(this.defaultConfig.getProtocol()));

                    try
                    {
                        if (messageAckExpected)
                        {
                            synchronized (this.inProgressMessagesLock)
                            {
                                this.inProgressPackets.put(message.getMessageId(), packet);
                            }
                        }

                        IotHubStatusCode statusCode = this.iotHubTransportConnection.sendMessage(message);

                        if (statusCode != IotHubStatusCode.OK_EMPTY && statusCode != IotHubStatusCode.OK)
                        {
                            this.handleMessageException(packet, IotHubStatusCode.getConnectionStatusException(statusCode, ""));
                        }
                        else if (!messageAckExpected)
                        {
                            //when no ack is expected for the sent message, all we can do is add callback
                            packet.setStatus(statusCode);
                            this.callbackPacketsQueue.add(packet);
                        }
                    }
                    catch (TransportException transportException)
                    {
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

                        this.handleMessageException(outboundPacket, transportException);
                    }
                }
            }
        }
    }

    private boolean isMessageValid(IotHubTransportPacket packet)
    {
        Message message = packet.getMessage();

        if (message.isExpired())
        {
            logger.LogInfo("Creating a callback for the expired message with MESSAGE_EXPIRED status, method name is %s ", logger.getMethodName());
            packet.setStatus(IotHubStatusCode.MESSAGE_EXPIRED);
            this.callbackPacketsQueue.add(packet);
            return false;
        }

        if (sasTokenHasExpired())
        {
            //Codes_SRS_AMQPSTRANSPORT_34_041: [If the config is using sas token authentication and its sas token has expired and cannot be renewed, the message shall not be sent, an UNAUTHORIZED message callback shall be added to the callback queue and SAS_TOKEN_EXPIRED connectionStatus callback shall be fired.]
            logger.LogInfo("Creating a callback for the message with expired sas token with UNAUTHORIZED status, method name is %s ", logger.getMethodName());
            packet.setStatus(IotHubStatusCode.UNAUTHORIZED);
            this.callbackPacketsQueue.add(packet);
            this.updateStatus(IotHubConnectionStatus.DISCONNECTED, IotHubConnectionStatusChangeReason.EXPIRED_SAS_TOKEN, new SecurityException("Your sas token has expired"));

            //Codes_SRS_AMQPSTRANSPORT_34_043: [If the config is using sas token authentication and its sas token has expired and cannot be renewed, the message shall not be put back into the waiting messages queue to be re-sent.]
            return false;
        }

        return true;
    }

    /** Invokes the callbacks for all completed requests. */
    public void invokeCallbacks()
    {
        // Codes_SRS_AMQPSTRANSPORT_15_020: [The function shall invoke all the callbacks from the callback queue.]
        IotHubTransportPacket packet = this.callbackPacketsQueue.poll();
        while (packet != null)
        {
            IotHubStatusCode status = packet.getStatus();
            IotHubEventCallback callback = packet.getCallback();
            Object context = packet.getContext();

            logger.LogInfo("Invoking the callback function for sent message, IoT Hub responded to message with status %s, method name is %s ", status.name(), logger.getMethodName());
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
        // received message queue could contain iothub transport message, in which case trigger callback on that transport message
        // Codes_SRS_AMQPSTRANSPORT_15_021: [If the transport is closed, the function shall throw an IllegalStateException.]
        if (this.connectionStatus == IotHubConnectionStatus.CONNECTED)
        {
            logger.LogDebug("Get the callback function for the received message, method name is %s ", logger.getMethodName());

            if (this.iotHubTransportConnection instanceof HttpsIotHubConnection)
            {
                IotHubTransportMessage transportMessage = ((HttpsIotHubConnection)this.iotHubTransportConnection).receiveMessage();

                if (transportMessage != null)
                {
                    this.receivedMessagesQueue.add(transportMessage);
                }
            }

            // Codes_SRS_AMQPSTRANSPORT_15_023: [The function shall attempt to consume a message from the IoT Hub.]
            // Codes_SRS_AMQPSTRANSPORT_15_024: [If no message was received from IotHub, the function shall return.]
            IotHubTransportMessage receivedMessage = this.receivedMessagesQueue.poll();
            if (receivedMessage != null)
            {
                // As the callbacks may be different for DT, DM and telemetry, obtain the resultant CB and execute
                MessageCallback messageCallback = receivedMessage.getMessageCallback();
                Object messageCallbackContext = receivedMessage.getMessageCallbackContext();

                // execute callback, sendMessage result, add to received queue if acked
                IotHubMessageResult result = null;
                if (messageCallback != null)
                {
                    result = messageCallback.execute(receivedMessage, messageCallbackContext);
                }

                try
                {
                    //ACK sent by default even if message QOS does not require ack
                    this.iotHubTransportConnection.sendMessageResult(receivedMessage, result);
                }
                catch (TransportException e)
                {
                    this.receivedMessagesQueue.add(receivedMessage);
                    throw e;
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
        return this.waitingPacketsQueue.isEmpty() && this.inProgressPackets.size() == 0 && this.callbackPacketsQueue.isEmpty();
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

        // Codes_SRS_AMQPSTRANSPORT_99_003: [The registerConnectionStateCallback shall register the connection connectionStatus callback.]
        this.stateCallback = callback;
        this.stateCallbackContext = callbackContext;
    }

    /**
     * Registers a callback to be executed whenever the connection status to the IoT Hub has changed.
     *
     * @param callback the callback to be called.
     * @param callbackContext a context to be passed to the callback. Can be
     * {@code null} if no callback is provided.
     */
    public void registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext)
    {
        if (callback == null)
        {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        this.connectionStatusChangeCallback = callback;
        this.connectionStatusChangeCallbackContext = callbackContext;
    }

    private void updateStatus(IotHubConnectionStatus newConnectionStatus, IotHubConnectionStatusChangeReason reason, Throwable throwable)
    {
        if (this.connectionStatus != newConnectionStatus)
        {
            this.connectionStatus = newConnectionStatus;

            //invoke connection status callbacks
            invokeConnectionStateCallback(newConnectionStatus, reason);
            invokeConnectionStatusChangeCallback(newConnectionStatus, reason, throwable);

            if (newConnectionStatus == IotHubConnectionStatus.CONNECTED)
            {
                this.currentReconnectionAttempt = 0;
            }
        }
    }

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

    private void invokeConnectionStatusChangeCallback(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason reason, Throwable e)
    {
        if (this.connectionStatusChangeCallback != null)
        {
            this.connectionStatusChangeCallback.execute(status, reason, e, this.connectionStatusChangeCallbackContext);
        }
    }

    private boolean sasTokenHasExpired()
    {
        return this.defaultConfig.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN && this.defaultConfig.getSasTokenAuthentication().isRenewalNecessary();
    }

    /**
     * Returns if the provided packet has lasted longer than the device operation timeout
     * @return true if the packet has been in the queues for longer than the device operation timeout and false otherwise
     */
    private boolean hasOperationTimedOut(long startTime)
    {
        return (System.currentTimeMillis() - startTime) > this.defaultConfig.getOperationTimeout();
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
                    // TimeUnit.sleep() treats negative timeouts just like zero.
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
}
