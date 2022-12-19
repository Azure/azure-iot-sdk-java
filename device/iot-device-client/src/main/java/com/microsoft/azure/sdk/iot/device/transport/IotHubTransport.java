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
import com.microsoft.azure.sdk.iot.device.transport.https.exceptions.UnauthorizedException;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttIotHubConnection;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions.MqttUnauthorizedException;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.util.*;
import java.util.concurrent.*;

import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.DEVICE_OPERATION_TIMED_OUT;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Manages queueing of message sending, receiving and callbacks. Manages notifying users of connection status change updates
 */
@Slf4j
public class IotHubTransport implements IotHubListener
{
    private static final int DEFAULT_MAX_MESSAGES_TO_SEND_PER_THREAD = 10;

    private static final int DEFAULT_CORRELATION_ID_LIVE_TIME = 60000;

    // For tracking the state of this layer in particular. If multiplexing, this value may be CONNECTED while a
    // device specific state is DISCONNECTED_RETRYING. If this state is DISCONNECTED_RETRYING, then the multiplexed
    // connection will be completely torn down and re-opened.
    private IotHubConnectionStatus connectionStatus;
    private Throwable connectionStatusLastException;

    private int maxNumberOfMessagesToSendPerThread = DEFAULT_MAX_MESSAGES_TO_SEND_PER_THREAD;

    // for multiplexing. A particular device can be disconnected retrying while the tcp connection is fine and the other
    // device sessions are open.
    private final Map<String, MultiplexedDeviceState> multiplexedDeviceConnectionStates = new HashMap<>();

    private final Map<String, Exception> multiplexingDeviceRegistrationFailures = new ConcurrentHashMap<>();

    private IotHubTransportConnection iotHubTransportConnection;

    // Messages waiting to be sent to the IoT Hub.
    private final Queue<IotHubTransportPacket> waitingPacketsQueue = new ConcurrentLinkedQueue<>();

    // Messages which are sent to the IoT Hub but did not receive ack yet.
    private final Map<String, IotHubTransportPacket> inProgressPackets = new ConcurrentHashMap<>();

    // Messages received from the IoT Hub
    private final Queue<IotHubTransportMessage> receivedMessagesQueue = new ConcurrentLinkedQueue<>();

    // Messages whose callbacks that are waiting to be invoked.
    private final Queue<IotHubTransportPacket> callbackPacketsQueue = new ConcurrentLinkedQueue<>();

    // Connection Status change callback information
    private final Map<String, IotHubConnectionStatusChangeCallback> connectionStatusChangeCallbacks = new ConcurrentHashMap<>();
    private final Map<String, Object> connectionStatusChangeCallbackContexts = new ConcurrentHashMap<>();

    // Connection Status callback information for multiplexed connection level events (whole multiplexed connection dropped, for instance)
    private IotHubConnectionStatusChangeCallback multiplexingStateCallback;
    private Object multiplexingStateCallbackContext;

    private RetryPolicy multiplexingRetryPolicy = new ExponentialBackoffWithJitter();

    // Callback for notifying the DeviceIO layer of connection status change events. The deviceIO layer
    // should stop spawning send/receive threads when this layer is disconnected or disconnected retrying
    private final IotHubConnectionStatusChangeCallback deviceIOConnectionStatusChangeCallback;

    // Lock on reading and writing on the inProgressPackets map
    final private Object inProgressMessagesLock = new Object();

    // Lock on setting and reading the state of multiplexed devices.
    final private Object multiplexingDeviceStateLock = new Object();

    // Keys are deviceIds. Helps with getting configs based on deviceIds
    private final Map<String, ClientConfiguration> deviceClientConfigs = new ConcurrentHashMap<>();

    private final String transportUniqueIdentifier = UUID.randomUUID().toString().substring(0, 8);

    private ScheduledExecutorService taskScheduler;

    // state lock to prevent simultaneous close and reconnect operations. Also prevents multiple reconnect threads from executing at once
    final private Object reconnectionLock = new Object();

    // State lock used to communicate to the IotHubSendTask thread when a message needs to be sent or a callback needs to be invoked.
    // It is this layer's responsibility to notify that task each time a message is queued to send, or when a callback is queued to be invoked.
    private final Semaphore sendThreadSemaphore = new Semaphore(0);

    // State lock used to communicate to the IotHubReceiveTask thread when a received message needs to be handled. It is this
    // layer's responsibility to notify that task each time a message is received.
    private final Semaphore receiveThreadSemaphore = new Semaphore(0);

    // State lock used to communicate to the IotHubReconnectTask thread when a reconnection needs to be handled. It is this
    // layer's responsibility to notify that task each time a connection is lost.
    private final Semaphore reconnectThreadSemaphore = new Semaphore(0);

    private final IotHubClientProtocol protocol;
    private final String hostName;
    private final ProxySettings proxySettings;
    private final int keepAliveInterval;
    private SSLContext sslContext;
    private final boolean isMultiplexing;

    // Flag set when close() starts. Acts as a signal to any running reconnection logic to not try again.
    private boolean isClosing;

    // Used to store the CorrelationCallbackMessage for a correlationId
    private final Map<String, CorrelatingMessageCallback> correlationCallbacks = new ConcurrentHashMap<>();
    private final Map<String, Object> correlationCallbackContexts = new ConcurrentHashMap<>();

    // Used to store the number of milliseconds since epoch that this packet was created for a correlationId
    private final Map<String, Long> correlationStartTimeMillis = new ConcurrentHashMap<>();

    /**
     * Constructor for an IotHubTransport object with default values
     *
     * @param defaultConfig the config used for opening connections, retrieving retry policy, and checking protocol
     * @param deviceIOConnectionStatusChangeCallback the connection status callback used to notify the DeviceIO
     * layer when connection events happen.
     * @param isMultiplexing true if this connection will multiplex. False otherwise.
     * @throws IllegalArgumentException if defaultConfig is null
     */
    public IotHubTransport(ClientConfiguration defaultConfig, IotHubConnectionStatusChangeCallback deviceIOConnectionStatusChangeCallback, boolean isMultiplexing) throws IllegalArgumentException
    {
        if (defaultConfig == null)
        {
            throw new IllegalArgumentException("Config cannot be null");
        }

        this.protocol = defaultConfig.getProtocol();
        this.hostName = defaultConfig.getIotHubHostname();
        this.deviceClientConfigs.put(defaultConfig.getDeviceId(), defaultConfig);
        this.multiplexedDeviceConnectionStates.put(defaultConfig.getDeviceId(), new MultiplexedDeviceState(IotHubConnectionStatus.DISCONNECTED));
        this.proxySettings = defaultConfig.getProxySettings();
        this.connectionStatus = IotHubConnectionStatus.DISCONNECTED;
        this.isMultiplexing = isMultiplexing;

        this.deviceIOConnectionStatusChangeCallback = deviceIOConnectionStatusChangeCallback;
        this.keepAliveInterval = defaultConfig.getKeepAliveInterval();
    }

    public IotHubTransport(
            String hostName,
            IotHubClientProtocol protocol,
            SSLContext sslContext,
            ProxySettings proxySettings,
            IotHubConnectionStatusChangeCallback deviceIOConnectionStatusChangeCallback,
            int keepAliveInterval) throws IllegalArgumentException
    {
        this.protocol = protocol;
        this.hostName = hostName;
        this.sslContext = sslContext;
        this.proxySettings = proxySettings;
        this.connectionStatus = IotHubConnectionStatus.DISCONNECTED;
        this.deviceIOConnectionStatusChangeCallback = deviceIOConnectionStatusChangeCallback;
        this.isMultiplexing = true;
        this.keepAliveInterval = keepAliveInterval;
    }

    public Semaphore getSendThreadSemaphore()
    {
        return this.sendThreadSemaphore;
    }

    public Semaphore getReceiveThreadSemaphore()
    {
        return this.receiveThreadSemaphore;
    }

    public Semaphore getReconnectThreadSemaphore()
    {
        return this.reconnectThreadSemaphore;
    }

    public boolean hasMessagesToSend()
    {
        return this.waitingPacketsQueue.size() > 0;
    }

    public boolean hasReceivedMessagesToHandle()
    {
        return this.receivedMessagesQueue.size() > 0;
    }

    public boolean hasCallbacksToExecute()
    {
        return this.callbackPacketsQueue.size() > 0;
    }

    public boolean needsReconnect()
    {
        if (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED_RETRYING)
        {
            return true;
        }

        for (MultiplexedDeviceState multiplexedDeviceState : this.multiplexedDeviceConnectionStates.values())
        {
            if (multiplexedDeviceState.getConnectionStatus() == IotHubConnectionStatus.DISCONNECTED_RETRYING)
            {
                return true;
            }
        }

        return false;
    }

    //Renaming it to isOpen would be confusing considering this layer's state is either open/closed/reconnecting
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isClosed()
    {
        return this.connectionStatus == IotHubConnectionStatus.DISCONNECTED;
    }

    @Override
    public void onMessageSent(Message message, String deviceId, TransportException e)
    {
        if (message == null)
        {
            log.warn("onMessageSent called with null message");
            return;
        }

        log.debug("IotHub message was acknowledged. Checking if there is record of sending this message ({})", message);

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
                log.trace("Message was sent by this client, adding it to callbacks queue with OK ({})", message);
                packet.setStatus(IotHubStatusCode.OK);
                this.addToCallbackQueue(packet);
            }
            else
            {
                this.handleMessageException(packet, e);
            }

            try
            {
                String correlationId = message.getCorrelationId();

                if (!correlationId.isEmpty())
                {
                    CorrelatingMessageCallback callback = correlationCallbacks.get(correlationId);

                    if (callback != null)
                    {
                        Object context = correlationCallbackContexts.get(correlationId);
                        IotHubClientException clientException = null;
                        if (e != null)
                        {
                            clientException = e.toIotHubClientException();
                        }
                        callback.onRequestAcknowledged(packet.getMessage(), context, clientException);
                    }
                }

            }
            catch (Exception ex)
            {
                log.warn("Exception thrown while calling the onRequestAcknowledged callback in onMessageSent", ex);
            }
        }
        else
        {
            // For instance, a message is sent by a multiplexed device client, the client is unregistered, and then the
            // client receives the acknowledgement for that sent message. Safe to ignore since the user has opted to stop
            // tracking it.
            log.trace("A message was acknowledged by IoT hub, but this client has already stopped tracking it ({})", message);
        }
    }

    @Override
    public void onMessageReceived(IotHubTransportMessage message, TransportException e)
    {
        if (message != null && e != null)
        {
            log.error("Exception encountered while receiving a message from service {}", message, e);
        }
        else if (message != null)
        {
            log.debug("Message was received from IotHub ({})", message);
            this.addToReceivedMessagesQueue(message);
        }
        else
        {
            log.error("Exception encountered while receiving messages from service", e);
        }

        try
        {
            if (message != null)
            {
                String correlationId = message.getCorrelationId();
                if (!correlationId.isEmpty())
                {
                    CorrelatingMessageCallback callback = correlationCallbacks.get(correlationId);

                    if (callback != null)
                    {
                        Object context = correlationCallbackContexts.get(correlationId);
                        IotHubClientException clientException = null;
                        if (e != null)
                        {
                            // This case indicates that the transport layer failed to construct a valid message out of
                            // a message delivered by the service
                            clientException = e.toIotHubClientException();
                        }
                        else
                        {
                            // This case indicates that the transport layer constructed a valid message out of a message
                            // delivered by the service, but that message may contain an unsuccessful status code in cases
                            // such as if an operation was rejected because it was badly formatted.
                            IotHubStatusCode statusCode = IotHubStatusCode.getIotHubStatusCode(Integer.parseInt(message.getStatus()));
                            if (!IotHubStatusCode.isSuccessful(statusCode))
                            {
                                clientException = new IotHubClientException(statusCode, "Received an unsuccessful operation error code from the service: " + statusCode);
                            }
                        }

                        callback.onResponseReceived(message, context, clientException);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            log.warn("Exception thrown while calling the onResponseReceived callback in onMessageReceived", ex);
        }
    }

    @Override
    public void onConnectionLost(TransportException e, String connectionId)
    {
        if (!connectionId.equals(this.iotHubTransportConnection.getConnectionId()))
        {
            //This connection status update is for a connection that is no longer tracked at this level, so it can be ignored.
            log.trace("OnConnectionLost was fired, but for an outdated connection. Ignoring...");
            return;
        }

        if (this.connectionStatus != IotHubConnectionStatus.CONNECTED)
        {
            log.trace("OnConnectionLost was fired, but connection is already disconnected. Ignoring...", e);
            return;
        }

        this.updateStatus(IotHubConnectionStatus.DISCONNECTED_RETRYING, exceptionToStatusChangeReason(e), e);

        log.trace("Waking up reconnection thread");
        this.reconnectThreadSemaphore.release();
    }

    @Override
    public void onConnectionEstablished(String connectionId)
    {
        if (connectionId.equals(this.iotHubTransportConnection.getConnectionId()))
        {
            log.debug("The connection to the IoT Hub has been established");

            this.updateStatus(IotHubConnectionStatus.CONNECTED, IotHubConnectionStatusChangeReason.CONNECTION_OK, null);
        }
    }

    @Override
    public void onMultiplexedDeviceSessionEstablished(String connectionId, String deviceId)
    {
        if (connectionId.equals(this.iotHubTransportConnection.getConnectionId()))
        {
            log.debug("The device session in the multiplexed connection to the IoT Hub has been established for device {}", deviceId);
            this.updateStatus(IotHubConnectionStatus.CONNECTED, IotHubConnectionStatusChangeReason.CONNECTION_OK, null, deviceId);
        }
    }

    @Override
    public void onMultiplexedDeviceSessionLost(TransportException e, String connectionId, String deviceId, boolean shouldReconnect)
    {
        if (connectionId.equals(this.iotHubTransportConnection.getConnectionId()))
        {
            log.debug("The device session in the multiplexed connection to the IoT Hub has been lost for device {}", deviceId);
            if (shouldReconnect)
            {
                this.updateStatus(IotHubConnectionStatus.DISCONNECTED_RETRYING, exceptionToStatusChangeReason(e), e, deviceId);

                log.trace("Waking up reconnection thread");
                this.reconnectThreadSemaphore.release();
            }
            else
            {
                // if the session shouldn't be reconnected, then it was a user-initiated close of the session
                this.updateStatus(IotHubConnectionStatus.DISCONNECTED, IotHubConnectionStatusChangeReason.CLIENT_CLOSE, null, deviceId);
            }
        }
    }

    @Override
    public void onMultiplexedDeviceSessionRegistrationFailed(String connectionId, String deviceId, Exception e)
    {
        if (connectionId != null && connectionId.equals(this.iotHubTransportConnection.getConnectionId()))
        {
            this.multiplexingDeviceRegistrationFailures.put(deviceId, e);
        }
    }

    public void setMultiplexingRetryPolicy(RetryPolicy retryPolicy)
    {
        this.multiplexingRetryPolicy = retryPolicy;
    }

    /**
     * Establishes a communication channel with an IoT Hub. If a channel is
     * already open, the function shall do nothing.
     * <p>
     * If reconnection is occurring when this is called, this function shall block and wait for the reconnection
     * to finish before trying to open the connection
     *
     * @param withRetry if true, this open call will apply the current retry policy to allow for the open call to be
     * retried if it fails.
     *
     * @throws TransportException if a communication channel cannot be established.
     */
    public void open(boolean withRetry) throws TransportException, IotHubClientException
    {
        if (this.connectionStatus == IotHubConnectionStatus.CONNECTED)
        {
            return;
        }

        if (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED_RETRYING)
        {
            throw new TransportException("Open cannot be called while transport is reconnecting");
        }

        this.isClosing = false;

        this.taskScheduler = Executors.newScheduledThreadPool(1);

        if (withRetry)
        {
            int connectionAttempt = 0;
            long startTime = System.currentTimeMillis();

            // this loop either ends in throwing an exception when retry expires, or by a break statement upon a successful openConnection() call
            while (true)
            {
                RetryPolicy retryPolicy = isMultiplexing ?  multiplexingRetryPolicy : this.getDefaultConfig().getRetryPolicy();

                try
                {
                    openConnection();
                    break; // openConnection() only returns without throwing if the connection attempt was successful
                }
                catch (TransportException transportException)
                {
                    log.debug("Encountered an exception while opening the client. Checking the configured retry policy to see if another attempt should be made.", transportException);
                    RetryDecision retryDecision = retryPolicy.getRetryDecision(connectionAttempt, transportException);
                    if (!retryDecision.shouldRetry())
                    {
                        throw new TransportException("Retry expired while attempting to open the connection", transportException);
                    }

                    connectionAttempt++;

                    if (hasOperationTimedOut(startTime))
                    {
                        throw new TransportException("Open operation timed out. The nested exception is the most recent exception thrown while attempting to open the connection", transportException);
                    }

                    try
                    {
                        log.trace("The configured retry policy allows for another attempt. Sleeping for {} milliseconds before the next attempt", retryDecision.getDuration());
                        Thread.sleep(retryDecision.getDuration());
                    }
                    catch (InterruptedException e)
                    {
                        throw new TransportException("InterruptedException thrown while sleeping between connection attempts", e);
                    }
                }
            }
        }
        else
        {
            openConnection();
        }

        log.debug("Client connection opened successfully");
    }

    /**
     * Closes all resources used to communicate with an IoT Hub. Once {@code close()} is
     * called, the transport is no longer usable. If the transport is already
     * closed, the function shall do nothing.
     *
     * @param cause the cause of why this connection is closing, to be reported over connection status change callback
     * @param reason the reason to close this connection, to be reported over connection status change callback
     */
    public void close(IotHubConnectionStatusChangeReason reason, Throwable cause)
    {
        if (reason == null)
        {
            throw new IllegalArgumentException("reason cannot be null");
        }

        // Set the flag outside of the synchronized block so that any currently
        // running reconnection logic knows to give up when this flag is set to true.
        // Then the rest of the close() code is in the synchronization block so that
        // it waits for the reconnection logic to end before it starts.
        this.isClosing = true;

        // Wait until no reconnection logic is taking place
        synchronized (this.reconnectionLock)
        {
            this.cancelPendingPackets();

            this.invokeCallbacks();

            if (this.taskScheduler != null)
            {
                this.taskScheduler.shutdown();
            }

            try
            {
                if (this.iotHubTransportConnection != null)
                {
                    this.iotHubTransportConnection.close();
                }
            }
            finally
            {
                this.updateStatus(IotHubConnectionStatus.DISCONNECTED, reason, cause);

                // Notify send thread to finish up so it doesn't survive this close
                this.sendThreadSemaphore.release();

                // Notify receive thread to finish up so it doesn't survive this close
                this.receiveThreadSemaphore.release();

                // Notify reconnect thread to finish up so it doesn't survive this close
                this.reconnectThreadSemaphore.release();

                log.debug("Client connection closed successfully");
            }
        }
    }

    // should only be called from IotHubReconnectTask
    public void reconnect() throws InterruptedException
    {
        synchronized (this.reconnectionLock)
        {
            long reconnectionStartTimeMillis = 0;
            int reconnectionAttempt = 0;
            String deviceSessionToReconnect = null;

            // retry policy to be used for connection level retry, not device session specific retry
            RetryPolicy retryPolicy = isMultiplexing ? multiplexingRetryPolicy : this.getDefaultConfig().getRetryPolicy();

            // keep attempting to reconnect the connection and any multiplexed device sessions until they are all CONNECTED
            // or they reach a DISCONNECTED state due to retry expired, timeout, encountering a non-retryable exception, etc.
            // This logic will prioritize reconnecting the amqp/mqtt connection before it attempts to reconnect any multiplexed
            // device sessions. And while it is reconnecting device sessions, it will reconnect them sequentially.
            while (needsReconnect())
            {
                // If user initiates a close of this client, abandon all reconnection logic
                if (this.isClosing)
                {
                    log.trace("Abandoning reconnection logic since this client has started closing");
                    return;
                }

                // if the connection as a whole is DISCONNECTED_RETRYING (as opposed to one or many multiplexed device
                // sessions being DISCONNECTED_RETRYING)
                if (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED_RETRYING)
                {
                    clearInProgressMessages();

                    if (reconnectionStartTimeMillis == 0)
                    {
                        reconnectionStartTimeMillis = System.currentTimeMillis();
                    }

                    singleReconnectAttempt(retryPolicy, reconnectionAttempt, reconnectionStartTimeMillis);
                    reconnectionAttempt++;
                }
                else // one or more multiplexed device sessions lost connectivity
                {
                    // pick one of the DISCONNECTED_RETRYING device sessions to attempt to reconnect
                    deviceSessionToReconnect = pickDeviceSessionToReconnect(deviceSessionToReconnect);

                    if (deviceSessionToReconnect != null)
                    {
                        singleDeviceReconnectAttemptAsync(deviceSessionToReconnect);
                    }
                }
            }
        }
    }

    /**
     * Check if the previous reconnection attempt for the given device session has reached a terminal state yet or not
     * @param deviceSessionToReconnect the deviceId of the device session to check on.
     * @return true if the reconnection attempt has reached a terminal state (CONNECTED or DISCONNECTED), and false otherwise.
     */
    private boolean checkIfPreviousReconnectionAttemptFinished(String deviceSessionToReconnect)
    {
        MultiplexedDeviceState lastReconnectAttemptsDeviceSession = this.multiplexedDeviceConnectionStates.get(deviceSessionToReconnect);

        if (lastReconnectAttemptsDeviceSession == null)
        {
            return true; // the device was unregistered during its reconnection, so its reconnection attempts can stop
        }

        if (lastReconnectAttemptsDeviceSession.getConnectionStatus() != IotHubConnectionStatus.DISCONNECTED_RETRYING)
        {
            // signals that a device session that attempted to reconnect has either successfully reconnected or has
            // reached a terminal DISCONNECTED state due to exhausting its retry, encountering a non-retryable exception, etc.
            log.trace("Finished reconnection logic for device session for device {} with terminal state {}", deviceSessionToReconnect, lastReconnectAttemptsDeviceSession.getConnectionStatus());
            return true;
        }

        return false;
    }

    /**
     * Pick which device session out of possibly many DISCONNECTED_RETRYING multiplexed device sessions to attempt a retry
     * on next.
     * @param previousDeviceSessionToReconnect the device Id of the device session that the last reconnection attempt
     * was for, or null if this is the first reconnect attempt for any device session.
     * @return The device Id of the multiplexed device session that should attempt to reconnect next. This will be
     * the same value as the passed in previousDeviceSessionToReconnect if that device session's reconnection attempts
     * have not reached a terminal state yet. If null, then no device sessions need reconnecting anymore.
     */
    private String pickDeviceSessionToReconnect(String previousDeviceSessionToReconnect)
    {
        boolean previousReconnectionAttemptFinished = checkIfPreviousReconnectionAttemptFinished(previousDeviceSessionToReconnect);

        if (previousReconnectionAttemptFinished)
        {
            // if the last device session to attempt reconnection has reached a terminal state, pick a new device session
            // from the set of DISCONNECTED_RETRYING device sessions
            for (String deviceId : this.multiplexedDeviceConnectionStates.keySet())
            {
                IotHubConnectionStatus status = this.multiplexedDeviceConnectionStates.get(deviceId).getConnectionStatus();
                if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING)
                {
                    return deviceId;
                }
            }

            return null; // no devices are DISCONNECTED_RETRYING
        }

        // if the previous reconnect attempt hasn't reached a terminal state yet, just continue retrying it
        return previousDeviceSessionToReconnect;
    }

    private void clearInProgressMessages()
    {
        synchronized (this.inProgressMessagesLock)
        {
            if (inProgressPackets.size() > 0)
            {
                log.trace("Due to disconnection event, clearing active queues, and re-queueing them to waiting queues to be re-processed later upon reconnection");
                for (IotHubTransportPacket packetToRequeue : inProgressPackets.values())
                {
                    this.addToWaitingQueue(packetToRequeue);
                }

                inProgressPackets.clear();
            }
        }
    }

    /**
     * Adds a message to the transport queue.
     *
     * @param message the message to be sent.
     * @param callback the callback to be invoked when a response for the
     * message is received.
     * @param callbackContext the context to be passed in when the callback is
     * @param deviceId the Id of the device that is sending this message.
     * invoked.
     */
    public void addMessage(Message message, MessageSentCallback callback, Object callbackContext, String deviceId)
    {
        if (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED)
        {
            throw new IllegalStateException("Cannot add a message when the transport is closed.");
        }

        IotHubTransportPacket packet = new IotHubTransportPacket(message, callback, callbackContext, null, System.currentTimeMillis(), deviceId);
        this.addToWaitingQueue(packet);

        log.debug("Message was queued to be sent later ({})", message);
    }

    public IotHubClientProtocol getProtocol()
    {
        return this.protocol;
    }

    /**
     * Sends all messages on the transport queue. If a previous send attempt had
     * failed, the function will attempt to resend the messages in the previous
     * attempt.
     */
    public void sendMessages()
    {
        checkForExpiredMessages();
        checkForOldMessages();

        if (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED
                || this.connectionStatus == IotHubConnectionStatus.DISCONNECTED_RETRYING)
        {
            return;
        }

        int timeSlice = maxNumberOfMessagesToSendPerThread;

        while (this.connectionStatus == IotHubConnectionStatus.CONNECTED && timeSlice-- > 0)
        {
            IotHubTransportPacket packet = waitingPacketsQueue.poll();

            if (packet != null)
            {
                Message message = packet.getMessage();
                log.trace("Dequeued a message from waiting queue to be sent ({})", message);

                if (message != null && this.isMessageValid(packet))
                {
                    sendPacket(packet);

                    try
                    {
                        String correlationId = message.getCorrelationId();

                        if (!correlationId.isEmpty())
                        {
                            CorrelatingMessageCallback callback = correlationCallbacks.get(correlationId);

                            if (callback != null)
                            {
                                Object context = correlationCallbackContexts.get(correlationId);
                                callback.onRequestSent(message, context);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        log.warn("Exception thrown while calling the onRequestSent callback in sendMessages", e);
                    }
                }
            }
        }
    }

    String getTransportConnectionId() {
        return this.iotHubTransportConnection.getConnectionId();
    }

    String getDeviceClientUniqueIdentifier()
    {
        // If it's not a multithreaded transport layer, we will use the device configuration to get the device unique identifier.
        if (!this.isMultiplexing && this.getDefaultConfig() != null)
        {
            return this.hostName + "-" + this.getDefaultConfig().getDeviceClientUniqueIdentifier();
        }

        // If this is a multithread transport layer, we will use its unique identifier.
        return this.hostName + "-Multiplexed-" + this.transportUniqueIdentifier;
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

    // Check if the "correlationCallbacks" map contains any correlation ID which has existed over the
    // default correlation ID live time. If so, remove the old correlation IDs from the map. Otherwise,
    // the size of map will grow endlessly which results in OutOfMemory eventually.
    private void checkForOldMessages()
    {
        List<String> correlationIdsToRemove = new ArrayList<>();

        for (String correlationId : correlationCallbacks.keySet())
        {
            if (System.currentTimeMillis() - correlationStartTimeMillis.get(correlationId) >= DEFAULT_CORRELATION_ID_LIVE_TIME)
            {
                correlationIdsToRemove.add(correlationId);
                correlationCallbackContexts.remove(correlationId);
                correlationStartTimeMillis.remove(correlationId);
            }
        }

        for (String correlationId : correlationIdsToRemove)
        {
            correlationCallbacks.remove(correlationId);
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
            MessageSentCallback callback = packet.getCallback();
            Object context = packet.getContext();

            log.debug("Invoking the callback function for sent message, IoT Hub responded to message ({}) with status {}", packet.getMessage(), status);

            IotHubClientException clientException = null;
            if (status != OK)
            {
                clientException = new IotHubClientException(status, "Received an unsuccessful operation error code from the service: " + status);
            }

            callback.onMessageSent(packet.getMessage(), clientException, context);

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
     * @throws TransportException if the server could not be reached.
     */
    public void handleMessage() throws TransportException
    {
        if (this.connectionStatus == IotHubConnectionStatus.CONNECTED)
        {
            if (this.iotHubTransportConnection instanceof HttpsIotHubConnection)
            {
                log.trace("Sending http request to check for any cloud to device messages...");
                addReceivedMessagesOverHttpToReceivedQueue();
            }

            IotHubTransportMessage receivedMessage = this.receivedMessagesQueue.poll();
            if (receivedMessage != null)
            {
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
            return this.waitingPacketsQueue.isEmpty() && this.inProgressPackets.size() == 0 && this.callbackPacketsQueue.isEmpty();
        }
    }

    /**
     * Registers a callback to be executed whenever the connection status to the IoT Hub has changed.
     *
     * @param callback the callback to be called. Can be null if callbackContext is not null
     * @param callbackContext a context to be passed to the callback. Can be {@code null}.
     * @param deviceId the device that the connection status events being subscribed to are for.
     */
    public void setConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext, String deviceId)
    {
        if (callbackContext != null && callback == null)
        {
            throw new IllegalArgumentException("Callback cannot be null if callback context is null");
        }

        if (callback == null)
        {
            this.connectionStatusChangeCallbacks.remove(deviceId);
            this.connectionStatusChangeCallbackContexts.remove(deviceId);
        }
        else
        {
            this.connectionStatusChangeCallbacks.put(deviceId, callback);

            if (callbackContext != null)
            {
                // ConcurrentHashMaps don't support null values. If user provides null context,
                // then calls to connectionStatusChangeCallbackContexts.get(...) will return null which lets this layer still work as expected.
                this.connectionStatusChangeCallbackContexts.put(deviceId, callbackContext);
            }
        }
    }

    public void setMultiplexingConnectionStateCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext)
    {
        if (callback == null && callbackContext != null)
        {
            throw new IllegalArgumentException("Cannot have a null callback and a non-null context associated with it");
        }

        this.multiplexingStateCallback = callback;
        this.multiplexingStateCallbackContext = callbackContext;
    }

    public void registerMultiplexedDeviceClient(List<ClientConfiguration> configs, long timeoutMilliseconds) throws InterruptedException, IotHubClientException, MultiplexingClientRegistrationException
    {
        if (getProtocol() != IotHubClientProtocol.AMQPS && getProtocol() != IotHubClientProtocol.AMQPS_WS)
        {
            throw new UnsupportedOperationException("Cannot add a multiplexed device unless connection is over AMQPS or AMQPS_WS");
        }

        multiplexingDeviceRegistrationFailures.clear();

        for (ClientConfiguration configToRegister : configs)
        {
            this.deviceClientConfigs.put(configToRegister.getDeviceId(), configToRegister);
            this.multiplexedDeviceConnectionStates.put(configToRegister.getDeviceId(), new MultiplexedDeviceState(IotHubConnectionStatus.DISCONNECTED));
            if (this.iotHubTransportConnection != null)
            {
                // Safe cast since amqps and amqps_ws always use this transport connection type.
                ((AmqpsIotHubConnection) this.iotHubTransportConnection).registerMultiplexedDevice(configToRegister);
            }
        }

        // If the multiplexed connection is active, block until all the registered devices have been connected.
        long timeoutTime = System.currentTimeMillis() + timeoutMilliseconds;
        MultiplexingClientRegistrationException registrationException = null;
        if (this.connectionStatus != IotHubConnectionStatus.DISCONNECTED)
        {
            for (ClientConfiguration newlyRegisteredConfig : configs)
            {
                String deviceId = newlyRegisteredConfig.getDeviceId();
                boolean deviceIsNotConnected = multiplexedDeviceConnectionStates.get(deviceId).getConnectionStatus() != IotHubConnectionStatus.CONNECTED;
                Exception deviceRegistrationException = multiplexingDeviceRegistrationFailures.remove(deviceId);
                while (deviceIsNotConnected && deviceRegistrationException == null)
                {
                    Thread.sleep(100);

                    deviceIsNotConnected = multiplexedDeviceConnectionStates.get(deviceId).getConnectionStatus() != IotHubConnectionStatus.CONNECTED;
                    deviceRegistrationException = multiplexingDeviceRegistrationFailures.remove(deviceId);
                    boolean operationHasTimedOut = System.currentTimeMillis() >= timeoutTime;
                    if (operationHasTimedOut)
                    {
                        throw new IotHubClientException(DEVICE_OPERATION_TIMED_OUT, "Timed out waiting for all device registrations to finish.");
                    }
                }

                if (deviceRegistrationException != null)
                {
                    if (registrationException == null)
                    {
                        registrationException = new MultiplexingClientRegistrationException("Failed to register one or more devices to the multiplexed connection.");
                    }

                    registrationException.addRegistrationException(deviceId, deviceRegistrationException);

                    // Since the registration failed, need to remove the device from the list of multiplexed devices
                    ClientConfiguration configThatFailedToRegister = this.deviceClientConfigs.remove(deviceId);
                    this.multiplexedDeviceConnectionStates.remove(deviceId);
                    ((AmqpsIotHubConnection) this.iotHubTransportConnection).unregisterMultiplexedDevice(configThatFailedToRegister, false);
                }
            }

            if (registrationException != null)
            {
                throw registrationException;
            }
        }
    }

    public void unregisterMultiplexedDeviceClient(List<ClientConfiguration> configs, long timeoutMilliseconds) throws InterruptedException, IotHubClientException
    {
        if (getProtocol() != IotHubClientProtocol.AMQPS && getProtocol() != IotHubClientProtocol.AMQPS_WS)
        {
            throw new UnsupportedOperationException("Cannot add a multiplexed device unless connection is over AMQPS or AMQPS_WS.");
        }

        for (ClientConfiguration configToRegister : configs)
        {
            if (this.iotHubTransportConnection != null)
            {
                // Safe cast since amqps and amqps_ws always use this transport connection type.
                ((AmqpsIotHubConnection) this.iotHubTransportConnection).unregisterMultiplexedDevice(configToRegister, false);
            }
            else
            {
                this.multiplexedDeviceConnectionStates.remove(configToRegister.getDeviceId());
            }

            this.deviceClientConfigs.remove(configToRegister.getDeviceId());
        }

        // If the multiplexed connection is active, block until all the unregistered devices have been disconnected.
        long timeoutTime = System.currentTimeMillis() + timeoutMilliseconds;
        if (this.connectionStatus != IotHubConnectionStatus.DISCONNECTED)
        {
            for (ClientConfiguration newlyUnregisteredConfig : configs)
            {
                while (multiplexedDeviceConnectionStates.get(newlyUnregisteredConfig.getDeviceId()).getConnectionStatus() != IotHubConnectionStatus.DISCONNECTED)
                {
                    //noinspection BusyWait
                    Thread.sleep(100);

                    boolean operationHasTimedOut = System.currentTimeMillis() >= timeoutTime;
                    if (operationHasTimedOut)
                    {
                        throw new IotHubClientException(DEVICE_OPERATION_TIMED_OUT, "Timed out waiting for all device unregistrations to finish.");
                    }
                }

                this.multiplexedDeviceConnectionStates.remove(newlyUnregisteredConfig.getDeviceId());
            }
        }

        // When a client is unregistered, remove all "waiting" and "in progress" messages that it had queued.
        for (IotHubTransportPacket waitingPacket : this.waitingPacketsQueue)
        {
            String deviceIdForMessage = waitingPacket.getDeviceId();
            for (ClientConfiguration unregisteredConfig : configs)
            {
                if (unregisteredConfig.getDeviceId().equals(deviceIdForMessage))
                {
                    this.waitingPacketsQueue.remove(waitingPacket);
                    waitingPacket.setStatus(IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE);
                    this.addToCallbackQueue(waitingPacket);
                }
            }
        }

        synchronized (this.inProgressMessagesLock)
        {
            for (String messageId : this.inProgressPackets.keySet())
            {
                String deviceIdForMessage = this.inProgressPackets.get(messageId).getDeviceId();
                for (ClientConfiguration unregisteredConfig : configs)
                {
                    if (unregisteredConfig.getDeviceId().equals(deviceIdForMessage))
                    {
                        IotHubTransportPacket cancelledPacket = this.inProgressPackets.remove(messageId);
                        cancelledPacket.setStatus(IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE);
                        this.addToCallbackQueue(cancelledPacket);
                    }
                }
            }
        }
    }

    public void setMaxNumberOfMessagesSentPerSendThread(int maxNumberOfMessagesSentPerSendThread)
    {
        if (maxNumberOfMessagesSentPerSendThread < 0)
        {
            throw new IllegalArgumentException("Maximum messages sent per thread cannot be negative");
        }

        this.maxNumberOfMessagesToSendPerThread = maxNumberOfMessagesSentPerSendThread;
    }

    /**
     * Moves all packets from waiting queue and in progress map into callbacks queue with status MESSAGE_CANCELLED_ONCLOSE
     */
    private void cancelPendingPackets()
    {
        IotHubTransportPacket packet = this.waitingPacketsQueue.poll();
        while (packet != null)
        {
            packet.setStatus(IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE);
            this.addToCallbackQueue(packet);

            packet = this.waitingPacketsQueue.poll();
        }

        synchronized (this.inProgressMessagesLock)
        {
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
     * If the provided received message has a saved callback, this function shall onStatusChanged that callback and send the ack
     * returned by the callback to the service
     *
     * @param receivedMessage the message to acknowledge
     *
     * @throws TransportException if any exception is encountered while sending the acknowledgement
     */
    private void acknowledgeReceivedMessage(IotHubTransportMessage receivedMessage) throws TransportException
    {
        MessageCallback messageCallback = receivedMessage.getMessageCallback();
        Object messageCallbackContext = receivedMessage.getMessageCallbackContext();

        if (messageCallback != null)
        {
            // If a message callback throws an exception here the acknowledge will never be sent and this message will
            // live in Iot hub until it expires.
            IotHubMessageResult result;
            try
            {
                log.debug("Executing callback for received message ({})", receivedMessage);
                result = messageCallback.onCloudToDeviceMessageReceived(receivedMessage, messageCallbackContext);
            }
            catch (Throwable ex)
            {
                // We want to log this exception and bubble up to the transport
                log.warn("Exception thrown while calling the message callback for received message {} in acknowledgeReceivedMessage. " +
                        "This exception is preventing the completion of message delivery and can result in messages being" +
                        "stuck in IoT hub until they expire. This can prevent the client from receiving futher messages.", receivedMessage, ex);
                throw ex;
            }

            try
            {
                log.debug("Sending acknowledgement for received cloud to device message ({})", receivedMessage);
                this.iotHubTransportConnection.sendMessageResult(receivedMessage, result);

                try
                {
                    String correlationId = receivedMessage.getCorrelationId();
                    if (!correlationId.isEmpty())
                    {
                        CorrelatingMessageCallback callback = correlationCallbacks.get(correlationId);

                        if (callback != null)
                        {
                            Object context = correlationCallbackContexts.get(correlationId);
                            callback.onResponseAcknowledged(receivedMessage, context);

                            correlationCallbackContexts.remove(correlationId);
                        }

                        // We need to remove the CorrelatingMessageCallback with the current correlation ID from the map after the received C2D
                        // message has been acknowledged. Otherwise, the size of map will grow endlessly which results in OutOfMemory eventually.
                        correlationCallbacks.remove(correlationId);
                        correlationStartTimeMillis.remove(correlationId);
                    }
                }
                catch (Exception ex)
                {
                    log.warn("Exception thrown while calling the onResponseAcknowledged callback in acknowledgeReceivedMessage", ex);
                }
            }
            catch (TransportException e)
            {
                log.warn("Sending acknowledgement for received cloud to device message failed, adding it back to the queue ({})", receivedMessage, e);
                this.addToReceivedMessagesQueue(receivedMessage);
                throw e;
            }
        }
    }

    /**
     * Checks if any messages were received over HTTP and adds all of them to the received messages queue
     *
     * @throws TransportException if an exception occurs while receiving messages over HTTP connection
     */
    private void addReceivedMessagesOverHttpToReceivedQueue() throws TransportException
    {
        //since Http behaves synchronously, we need to check synchronously for any messages it may have received
        IotHubTransportMessage transportMessage = ((HttpsIotHubConnection) this.iotHubTransportConnection).receiveMessage();

        if (transportMessage != null)
        {
            log.debug("Message was received from IotHub ({})", transportMessage);
            this.addToReceivedMessagesQueue(transportMessage);

            try
            {
                String correlationId = transportMessage.getCorrelationId();
                if (!correlationId.isEmpty())
                {
                    CorrelatingMessageCallback callback = correlationCallbacks.get(correlationId);

                    if (callback != null)
                    {
                        Object context = correlationCallbackContexts.get(correlationId);
                        callback.onResponseReceived(transportMessage, context, null);
                    }
                }
            }
            catch (Exception e)
            {
                log.warn("Exception thrown while calling the onResponseReceived callback in addReceivedMessagesOverHttpToReceivedQueue", e);
            }
        }
    }

    /**
     * Maps a given throwable to an IotHubConnectionStatusChangeReason
     *
     * @param e the throwable to map to an IotHubConnectionStatusChangeReason
     *
     * @return the mapped IotHubConnectionStatusChangeReason
     */
    private IotHubConnectionStatusChangeReason exceptionToStatusChangeReason(Throwable e)
    {
        if (e instanceof TransportException)
        {
            TransportException transportException = (TransportException) e;
            if (isSasTokenExpired())
            {
                log.debug("Mapping throwable to EXPIRED_SAS_TOKEN because it was a non-retryable exception and the saved sas token has expired", e);
                return IotHubConnectionStatusChangeReason.EXPIRED_SAS_TOKEN;
            }
            else if (e instanceof UnauthorizedException || e instanceof MqttUnauthorizedException || e instanceof AmqpUnauthorizedAccessException)
            {
                log.debug("Mapping throwable to BAD_CREDENTIAL because it was a non-retryable exception authorization exception but the saved sas token has not expired yet", e);
                return IotHubConnectionStatusChangeReason.BAD_CREDENTIAL;
            }
            else if (transportException.isRetryable())
            {
                log.debug("Mapping throwable to NO_NETWORK because it was a retryable exception", e);
                return IotHubConnectionStatusChangeReason.NO_NETWORK;
            }
        }

        log.debug("Mapping exception throwable to COMMUNICATION_ERROR because the sdk was unable to classify the thrown exception to anything other category", e);

        return IotHubConnectionStatusChangeReason.COMMUNICATION_ERROR;
    }

    /**
     * Creates a new iotHubTransportConnection instance, sets this object as its listener, and opens that connection
     *
     * @throws TransportException if any exception is thrown while opening the connection
     */
    private void openConnection() throws TransportException
    {
        if (this.iotHubTransportConnection == null)
        {
            switch (this.protocol)
            {
                case HTTPS:
                    this.iotHubTransportConnection = new HttpsIotHubConnection(this.getDefaultConfig());
                    break;
                case MQTT:
                case MQTT_WS:
                    this.iotHubTransportConnection = new MqttIotHubConnection(this.getDefaultConfig());
                    break;
                case AMQPS:
                case AMQPS_WS:
                    if (this.isMultiplexing)
                    {
                        // The default config is only null when someone creates a multiplexing client and opens it before
                        // registering any devices to it
                        this.iotHubTransportConnection = new AmqpsIotHubConnection(
                                this.hostName,
                                this.transportUniqueIdentifier,
                                this.protocol == IotHubClientProtocol.AMQPS_WS,
                                this.sslContext,
                                this.proxySettings,
                                this.keepAliveInterval);

                        for (ClientConfiguration config : this.deviceClientConfigs.values())
                        {
                            ((AmqpsIotHubConnection) this.iotHubTransportConnection).registerMultiplexedDevice(config);
                        }
                    }
                    else
                    {
                        this.iotHubTransportConnection = new AmqpsIotHubConnection(this.getDefaultConfig(), this.transportUniqueIdentifier);
                    }

                    break;
                default:
                    throw new TransportException("Protocol not supported");
            }
        }

        this.iotHubTransportConnection.setListener(this);
        this.iotHubTransportConnection.open();
        this.updateStatus(IotHubConnectionStatus.CONNECTED, IotHubConnectionStatusChangeReason.CONNECTION_OK, null);
    }

    // For reconnecting multiplexed devices only. Since this triggers asynchronous functions in the AMQP layer, there
    // is no guarantee that the reconnect worked just because the unregister/register calls return successfully.
    // Still need to check the device connection status before you can report the device to be connected.
    private void singleDeviceReconnectAttemptAsync(String deviceSessionToReconnect) throws InterruptedException
    {
        MultiplexedDeviceState multiplexedDeviceState = multiplexedDeviceConnectionStates.get(deviceSessionToReconnect);
        if (multiplexedDeviceState.getConnectionStatus() == IotHubConnectionStatus.DISCONNECTED_RETRYING)
        {
            TransportException transportException = getTransportExceptionFromThrowable(multiplexedDeviceState.getLastException());

            if (multiplexedDeviceState.getReconnectionAttemptNumber() == 0)
            {
                multiplexedDeviceState.setStartReconnectTime(System.currentTimeMillis());
            }

            if (this.hasOperationTimedOut(multiplexedDeviceState.getStartReconnectTime()))
            {
                this.updateStatus(IotHubConnectionStatus.DISCONNECTED, IotHubConnectionStatusChangeReason.RETRY_EXPIRED, transportException, deviceSessionToReconnect);
                log.debug("Reconnection for device {} was abandoned due to the operation timeout", deviceSessionToReconnect);
            }

            multiplexedDeviceState.incrementReconnectionAttemptNumber();

            ClientConfiguration config = this.getConfig(deviceSessionToReconnect);

            if (config == null)
            {
                log.debug("Reconnection for device {} was abandoned because it was unregistered while reconnecting", deviceSessionToReconnect);
                return;
            }

            RetryPolicy retryPolicy = config.getRetryPolicy();
            RetryDecision retryDecision = retryPolicy.getRetryDecision(multiplexedDeviceState.getReconnectionAttemptNumber(), transportException);
            if (!retryDecision.shouldRetry())
            {
                this.updateStatus(IotHubConnectionStatus.DISCONNECTED, IotHubConnectionStatusChangeReason.RETRY_EXPIRED, transportException, deviceSessionToReconnect);
                log.debug("Reconnection for device {} was abandoned due to the retry policy", deviceSessionToReconnect);
            }

            log.trace("Attempting to reconnect device session: attempt {}", multiplexedDeviceState.getReconnectionAttemptNumber());

            // This call triggers some async amqp logic, so all this function can do is wait for a bit and check the connection
            // status for this device before retrying.
            ((AmqpsIotHubConnection) this.iotHubTransportConnection).unregisterMultiplexedDevice(config, true);
            ((AmqpsIotHubConnection) this.iotHubTransportConnection).registerMultiplexedDevice(config);

            log.trace("Sleeping between device reconnect attempts for device {}", deviceSessionToReconnect);
            MILLISECONDS.sleep(retryDecision.getDuration());

            if (!transportException.isRetryable())
            {
                this.updateStatus(IotHubConnectionStatus.DISCONNECTED, this.exceptionToStatusChangeReason(transportException), transportException, deviceSessionToReconnect);
                log.error("Reconnection for device {} was abandoned due to encountering a non-retryable exception", deviceSessionToReconnect, transportException);
            }
        }
    }

    private ClientConfiguration getConfig(String deviceId)
    {
        // if the map doesn't contain this deviceId as a key, then it is because the device was unregistered from
        // the multiplexed connection. Methods that call this method should assume that the return value from this
        // function may be null, and handle that case accordingly.
        return this.deviceClientConfigs.get(deviceId);
    }

    /**
     * Attempts to close and then re-open the iotHubTransportConnection once
     */
    private void singleReconnectAttempt(RetryPolicy retryPolicy, int reconnectionAttempt, long reconnectionStartTimeMillis) throws InterruptedException
    {
        if (this.hasOperationTimedOut(reconnectionStartTimeMillis))
        {
            log.debug("Reconnection was abandoned due to the operation timeout");
            this.close(
                    IotHubConnectionStatusChangeReason.RETRY_EXPIRED,
                    new IotHubClientException(DEVICE_OPERATION_TIMED_OUT, "Device operation for reconnection timed out"));
            return;
        }

        TransportException transportException = getTransportExceptionFromThrowable(this.connectionStatusLastException);

        log.trace("Attempting reconnect attempt {}", reconnectionAttempt);

        RetryDecision retryDecision = retryPolicy.getRetryDecision(reconnectionAttempt, transportException);
        if (!retryDecision.shouldRetry())
        {
            log.debug("Reconnection was abandoned due to the retry policy");
            this.close(IotHubConnectionStatusChangeReason.RETRY_EXPIRED, transportException);
            return;
        }

        log.trace("Sleeping between reconnect attempts");
        MILLISECONDS.sleep(retryDecision.getDuration());

        try
        {
            log.trace("Attempting to close and re-open the iot hub transport connection...");
            this.iotHubTransportConnection.close();
            this.openConnection();
            log.trace("Successfully closed and re-opened the iot hub transport connection");
        }
        catch (TransportException newTransportException)
        {
            checkForUnauthorizedException(newTransportException);
            log.warn("Failed to close and re-open the iot hub transport connection, checking if another retry attempt should be made", newTransportException);
            transportException = newTransportException;
        }

        if (!transportException.isRetryable())
        {
            log.error("Reconnection was abandoned due to encountering a non-retryable exception", transportException);
            this.close(this.exceptionToStatusChangeReason(transportException), transportException);
        }
    }

    /**
     * Task for adding a packet back to the waiting queue. Used for delaying message retry
     */
    public static class MessageRetryRunnable implements Runnable
    {
        final IotHubTransportPacket transportPacket;
        final Queue<IotHubTransportPacket> waitingPacketsQueue;
        final Semaphore sendThreadSemaphore;

        MessageRetryRunnable(
                Queue<IotHubTransportPacket> waitingPacketsQueue,
                IotHubTransportPacket transportPacket,
                Semaphore sendThreadSemaphore)
        {
            this.waitingPacketsQueue = waitingPacketsQueue;
            this.transportPacket = transportPacket;
            this.sendThreadSemaphore = sendThreadSemaphore;
        }

        @Override
        public void run()
        {
            this.waitingPacketsQueue.add(this.transportPacket);

            // Wake up send messages thread so that it can send this message
            this.sendThreadSemaphore.release();
        }
    }

    /**
     * Spawn a task to add the provided packet back to the waiting list if the provided transportException is retryable
     * and if the message hasn't timed out
     *
     * @param packet the packet to retry
     * @param transportException the exception encountered while sending this packet before
     */
    private void handleMessageException(IotHubTransportPacket packet, TransportException transportException)
    {
        log.warn("Handling an exception from sending message: Attempt number {}", packet.getCurrentRetryAttempt(), transportException);

        packet.incrementRetryAttempt();
        if (!this.hasOperationTimedOut(packet.getStartTimeMillis()))
        {
            String deviceId = packet.getDeviceId();
            if (transportException.isRetryable())
            {
                ClientConfiguration config = this.getConfig(deviceId);
                if (config == null)
                {
                    log.debug("Abandoning handling the message exception since the device it was associated with has been unregistered.");
                    return;
                }

                RetryDecision retryDecision = config.getRetryPolicy().getRetryDecision(packet.getCurrentRetryAttempt(), transportException);
                if (retryDecision.shouldRetry())
                {
                    this.taskScheduler.schedule(new MessageRetryRunnable(this.waitingPacketsQueue, packet, this.sendThreadSemaphore), retryDecision.getDuration(), MILLISECONDS);
                    return;
                }
                else
                {
                    log.warn("Retry policy dictated that the message should be abandoned, so it has been abandoned ({})", packet.getMessage(), transportException);
                }
            }
            else
            {
                log.warn("Encountering an non-retryable exception while sending a message, so it has been abandoned ({})", packet.getMessage(), transportException);
            }
        }
        else
        {
            log.warn("The device operation timeout has been exceeded for the message, so it has been abandoned ({})", packet.getMessage(), transportException);
        }

        packet.setStatus(transportException.toIotHubClientException().getStatusCode());
        this.addToCallbackQueue(packet);
    }

    /**
     * Sends a single packet over the iotHubTransportConnection and handles the response
     *
     * @param packet the packet to send
     */
    private void sendPacket(IotHubTransportPacket packet)
    {
        Message message = packet.getMessage();

        boolean messageAckExpected = !(message instanceof IotHubTransportMessage
                && !((IotHubTransportMessage) message).isMessageAckNeeded(this.protocol));

        try
        {
            if (messageAckExpected)
            {
                synchronized (this.inProgressMessagesLock)
                {
                    log.trace("Adding transport message to the inProgressPackets to wait for acknowledgement ({})", message);
                    this.inProgressPackets.put(message.getMessageId(), packet);
                }
            }

            log.debug("Sending message ({})", message);
            IotHubStatusCode statusCode = this.iotHubTransportConnection.sendMessage(message);
            log.trace("Sent message ({}) to protocol level, returned status code was {}", message, statusCode);

            if (statusCode != IotHubStatusCode.OK)
            {
                this.inProgressPackets.remove(message.getMessageId());
                this.handleMessageException(packet, IotHubStatusCode.getConnectionStatusException(statusCode, ""));
            }
            else if (!messageAckExpected)
            {
                packet.setStatus(statusCode);
                this.addToCallbackQueue(packet);
            }
        }
        catch (TransportException transportException)
        {
            log.warn("Encountered exception while sending message with correlation id {}", message.getCorrelationId(), transportException);

            if (messageAckExpected)
            {
                synchronized (this.inProgressMessagesLock)
                {
                    this.inProgressPackets.remove(message.getMessageId());
                }
            }

            this.handleMessageException(packet, transportException);
        }
    }

    /**
     * Checks if the provided packet has expired or if the sas token has expired
     *
     * @param packet the packet to check for expiry
     *
     * @return if the message has not expired and if the sas token has not expired
     */
    private boolean isMessageValid(IotHubTransportPacket packet)
    {
        Message message = packet.getMessage();

        if (message.isExpired())
        {
            log.warn("Message with has expired, adding to callbacks queue with MESSAGE_EXPIRED ({})", message);
            packet.setStatus(IotHubStatusCode.MESSAGE_EXPIRED);
            this.addToCallbackQueue(packet);
            return false;
        }

        return true;
    }

    private void updateStatus(IotHubConnectionStatus newConnectionStatus, IotHubConnectionStatusChangeReason reason, Throwable throwable)
    {
        if (this.connectionStatus != newConnectionStatus)
        {
            if (throwable == null)
            {
                log.debug("Updating transport status to new status {} with reason {}", newConnectionStatus, reason);
            }
            else
            {
                log.warn("Updating transport status to new status {} with reason {}", newConnectionStatus, reason, throwable);
            }

            ConnectionStatusChangeContext connectionStatusChangeContext = new ConnectionStatusChangeContext(newConnectionStatus, this.connectionStatus, reason, throwable, null);

            this.connectionStatus = newConnectionStatus;
            this.connectionStatusLastException = throwable;

            this.deviceIOConnectionStatusChangeCallback.onStatusChanged(connectionStatusChangeContext);

            //invoke connection status callbacks
            log.debug("Invoking connection status callbacks with new status details");

            if (!isMultiplexing || newConnectionStatus != IotHubConnectionStatus.CONNECTED)
            {
                // When multiplexing, a different method will notify each device-specific callback when that device is online,
                // but in cases when the tcp connection is lost and everything is disconnected retrying or disconnected, this is where the
                // callback should be fired
                invokeConnectionStatusChangeCallback(newConnectionStatus, reason, throwable);

                for (ClientConfiguration config : deviceClientConfigs.values())
                {
                    MultiplexedDeviceState deviceState = multiplexedDeviceConnectionStates.get(config.getDeviceId());
                    deviceState.setConnectionStatus(newConnectionStatus);
                    deviceState.setReconnectionAttemptNumber(0);
                }
            }

            // If multiplexing, fire the multiplexing state callback as long as it was set.
            if (isMultiplexing && this.multiplexingStateCallback != null)
            {
                this.multiplexingStateCallback.onStatusChanged(connectionStatusChangeContext);
            }
        }
    }

    private void updateStatus(IotHubConnectionStatus newConnectionStatus, IotHubConnectionStatusChangeReason reason, Throwable throwable, String deviceId)
    {
        if (!this.multiplexedDeviceConnectionStates.containsKey(deviceId))
        {
            // not tracking the state of this device, likely because it was unregistered. No need to update any status here.
            return;
        }

        IotHubConnectionStatus previousStatus = this.multiplexedDeviceConnectionStates.get(deviceId).getConnectionStatus();
        if (previousStatus == newConnectionStatus)
        {
            // new status is the same as the current status, so no need to update anything here.
            return;
        }

        if (throwable == null)
        {
            log.debug("Updating device {} status to new status {} with reason {}", deviceId, newConnectionStatus, reason);
        }
        else
        {
            log.warn("Updating device {} status to new status {} with reason {}", deviceId, newConnectionStatus, reason, throwable);
        }

        synchronized (this.multiplexingDeviceStateLock)
        {
            MultiplexedDeviceState deviceState = new MultiplexedDeviceState(newConnectionStatus, throwable);

            if (newConnectionStatus == IotHubConnectionStatus.DISCONNECTED_RETRYING)
            {
                // When the reconnect thread wakes up, it will know that this device session has not attempted any
                // reconnect attempts yet.
                deviceState.setReconnectionAttemptNumber(0);
            }

            this.multiplexedDeviceConnectionStates.put(deviceId, deviceState);

            log.debug("Invoking connection status callbacks with new status details");
            invokeConnectionStatusChangeCallback(newConnectionStatus, previousStatus, reason, throwable, deviceId);
        }
    }

    private void invokeConnectionStatusChangeCallback(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason reason, Throwable e)
    {
        for (String registeredDeviceId : this.connectionStatusChangeCallbacks.keySet())
        {
            MultiplexedDeviceState multiplexedDeviceState = this.multiplexedDeviceConnectionStates.get(registeredDeviceId);
            if (multiplexedDeviceState != null && multiplexedDeviceState.getConnectionStatus() != status)
            {
                // only onStatusChanged the callback if the state of the device is changing.
                ConnectionStatusChangeContext connectionStatusChangeContext = new ConnectionStatusChangeContext(status, multiplexedDeviceState.getConnectionStatus(), reason, e, this.connectionStatusChangeCallbackContexts.get(registeredDeviceId));
                this.connectionStatusChangeCallbacks.get(registeredDeviceId).onStatusChanged(connectionStatusChangeContext);
            }
        }
    }

    private void invokeConnectionStatusChangeCallback(IotHubConnectionStatus newStatus, IotHubConnectionStatus previousStatus, IotHubConnectionStatusChangeReason reason, Throwable e, String deviceId)
    {
        if (deviceId == null)
        {
            for (String registeredDeviceId : this.connectionStatusChangeCallbacks.keySet())
            {
                ConnectionStatusChangeContext connectionStatusChangeContext = new ConnectionStatusChangeContext(newStatus, previousStatus, reason, e, this.connectionStatusChangeCallbackContexts.get(registeredDeviceId));
                this.connectionStatusChangeCallbacks.get(registeredDeviceId).onStatusChanged(connectionStatusChangeContext);
            }
        }
        else if (this.connectionStatusChangeCallbacks.containsKey(deviceId))
        {
            ConnectionStatusChangeContext connectionStatusChangeContext = new ConnectionStatusChangeContext(newStatus, previousStatus, reason, e, this.connectionStatusChangeCallbackContexts.get(deviceId));
            this.connectionStatusChangeCallbacks.get(deviceId).onStatusChanged(connectionStatusChangeContext);
        }
        else
        {
            log.trace("Device {} did not have a connection status change callback registered, so no callback was fired.", deviceId);
        }
    }

    // warning is about how getSasTokenAuthentication() may return null. In this case, it never will since we only
    // check SAS token expiry when using SAS based auth, and there is always a SAS token authentication provider
    // when using SAS based auth.
    @SuppressWarnings("ConstantConditions")
    private boolean isAuthenticationProviderExpired()
    {
        if (this.getDefaultConfig() == null)
        {
            return false;
        }

        return this.getDefaultConfig().getAuthenticationType() == ClientConfiguration.AuthType.SAS_TOKEN
                && this.getDefaultConfig().getSasTokenAuthentication().isAuthenticationProviderRenewalNecessary();
    }

    // warning is about how getSasTokenAuthentication() may return null. In this case, it never will since we only
    // check SAS token expiry when using SAS based auth, and there is always a SAS token authentication provider
    // when using SAS based auth.
    @SuppressWarnings("ConstantConditions")
    private boolean isSasTokenExpired()
    {
        if (this.getDefaultConfig() == null)
        {
            return false;
        }

        return this.getDefaultConfig().getAuthenticationType() == ClientConfiguration.AuthType.SAS_TOKEN
            && this.getDefaultConfig().getSasTokenAuthentication().isSasTokenExpired();
    }

    /**
     * Returns if the provided packet has lasted longer than the device operation timeout
     *
     * @return true if the packet has been in the queues for longer than the device operation timeout and false otherwise
     */
    private boolean hasOperationTimedOut(long startTime)
    {
        if (startTime == 0 || this.getDefaultConfig() == null)
        {
            // multiplexed connection with no registered devices, and that scenario doesn't have a device operation timeout
            return false;
        }

        return (System.currentTimeMillis() - startTime) > this.getDefaultConfig().getOperationTimeout();
    }

    /**
     * Returns if the provided packet has lasted longer than the device operation timeout
     *
     * @return true if the packet has been in the queues for longer than the device operation timeout and false otherwise
     */
    private boolean hasOperationTimedOut(long startTime, String deviceId)
    {
        if (startTime == 0)
        {
            return false;
        }

        ClientConfiguration config = this.getConfig(deviceId);
        if (config == null)
        {
            log.debug("Operation has not timed out since the device it was associated with has been unregistered already.");
            return false;
        }

        return (System.currentTimeMillis() - startTime) > config.getOperationTimeout();
    }

    /**
     * Adds the packet to the callback queue if the provided packet has a callback. The packet is ignored otherwise.
     *
     * @param packet the packet to add
     */
    private void addToCallbackQueue(IotHubTransportPacket packet)
    {
        if (packet.getCallback() != null)
        {
            this.callbackPacketsQueue.add(packet);

            //Wake up send messages thread so that it can process this new callback if it was asleep
            this.sendThreadSemaphore.release();
        }
    }

    private ClientConfiguration getDefaultConfig()
    {
        for (ClientConfiguration config : this.deviceClientConfigs.values())
        {
            // just return the first entry in the list.
            return config;
        }

        // should only happen when using multiplexing client and opening the connection before registering any devices
        return null;
    }

    private void addToWaitingQueue(IotHubTransportPacket packet)
    {
        try
        {
            if (packet != null)
            {
                Message message = packet.getMessage();
                if (message != null)
                {
                    String correlationId = message.getCorrelationId();
                    CorrelatingMessageCallback correlationCallback = message.getCorrelatingMessageCallback();
                    if (!correlationId.isEmpty() && correlationCallback != null)
                    {
                        correlationCallbacks.put(correlationId, correlationCallback);
                        correlationStartTimeMillis.put(correlationId, System.currentTimeMillis());

                        Object correlationCallbackContext = message.getCorrelatingMessageCallbackContext();
                        if (correlationCallbackContext != null)
                        {
                            correlationCallbackContexts.put(correlationId, correlationCallbackContext);
                        }
                        correlationCallback.onRequestQueued(message, correlationCallbackContext);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            log.warn("Exception thrown while calling the onQueueRequest callback in addToWaitingQueue", ex);
        }

        this.waitingPacketsQueue.add(packet);

        // Wake up IotHubSendTask so it can send this message
        this.sendThreadSemaphore.release();
    }

    private void addToReceivedMessagesQueue(IotHubTransportMessage message)
    {
        this.receivedMessagesQueue.add(message);

        // Wake up IotHubReceiveTask so it can handle receiving this message
        this.receiveThreadSemaphore.release();
    }

    /**
     * If Unauthorized exception occurs, but sas token has not expired, this function sets the provided
     * transportException as retryable
     *
     * @param transportException the transport exception to check
     */
    private void checkForUnauthorizedException(TransportException transportException)
    {
        if (!this.isAuthenticationProviderExpired() && (transportException instanceof MqttUnauthorizedException
                || transportException instanceof UnauthorizedException
                || transportException instanceof AmqpUnauthorizedAccessException))
        {
            //Device key is present, sas token will be renewed upon re-opening the connection
            transportException.setRetryable(true);
        }
    }

    private static TransportException getTransportExceptionFromThrowable(Throwable cause)
    {
        TransportException transportException;
        if (cause instanceof TransportException)
        {
            return (TransportException) cause;
        }

        transportException = new TransportException(cause);
        transportException.setRetryable(true);
        return transportException;
    }
}
