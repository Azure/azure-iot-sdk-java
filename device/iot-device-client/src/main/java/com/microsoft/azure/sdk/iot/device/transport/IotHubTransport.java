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

import javax.net.ssl.SSLContext;
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

    // For tracking the state of this layer in particular. If multiplexing, this value may be CONNECTED while a
    // device specific state is DISCONNECTED_RETRYING. If this state is DISCONNECTED_RETRYING, then the multiplexed
    // connection will be completely torn down and re-opened.
    private volatile IotHubConnectionStatus connectionStatus;

    // for multiplexing. A particular device can be disconnected retrying while the tcp connection is fine and the other
    // device sessions are open.
    private final Map<String, IotHubConnectionStatus> deviceConnectionStates = new HashMap<>();

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

    // Connection Status callback information (deprecated)
    private IotHubConnectionStateCallback stateCallback;
    private Object stateCallbackContext;

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
    private final Map<String, DeviceClientConfig> deviceClientConfigs;

    private ScheduledExecutorService taskScheduler;

    final private Object reconnectionLock = new Object();

    // State lock used to communicate to the IotHubSendTask thread when a message needs to be sent or a callback needs to be invoked.
    // It is this layer's responsibility to notify that task each time a message is queued to send, or when a callback is queued to be invoked.
    private final Object sendThreadLock = new Object();

    // State lock used to communicate to the IotHubReceiveTask thread when a received message needs to be handled. It is this
    // layer's responsibility to notify that task each time a message is received.
    private final Object receiveThreadLock = new Object();

    private final IotHubClientProtocol protocol;
    private final String hostName;
    private final ProxySettings proxySettings;
    private SSLContext sslContext;

    /**
     * Constructor for an IotHubTransport object with default values
     * @param defaultConfig the config used for opening connections, retrieving retry policy, and checking protocol
     * @throws IllegalArgumentException if defaultConfig is null
     */
    public IotHubTransport(DeviceClientConfig defaultConfig, IotHubConnectionStatusChangeCallback deviceIOConnectionStatusChangeCallback) throws IllegalArgumentException
    {
        if (defaultConfig == null)
        {
            throw new IllegalArgumentException("Config cannot be null");
        }

        this.deviceClientConfigs = new ConcurrentHashMap<>();

        this.protocol = defaultConfig.getProtocol();
        this.hostName = defaultConfig.getIotHubHostname();
        this.deviceClientConfigs.put(defaultConfig.getDeviceId(), defaultConfig);
        this.deviceConnectionStates.put(defaultConfig.getDeviceId(), IotHubConnectionStatus.DISCONNECTED);
        this.proxySettings = defaultConfig.getProxySettings();
        this.connectionStatus = IotHubConnectionStatus.DISCONNECTED;

        this.deviceIOConnectionStatusChangeCallback = deviceIOConnectionStatusChangeCallback;
    }

    public IotHubTransport(String hostName, IotHubClientProtocol protocol, SSLContext sslContext, ProxySettings proxySettings, IotHubConnectionStatusChangeCallback deviceIOConnectionStatusChangeCallback) throws IllegalArgumentException
    {
        this.protocol = protocol;
        this.hostName = hostName;
        this.sslContext = sslContext;
        this.proxySettings = proxySettings;
        this.connectionStatus = IotHubConnectionStatus.DISCONNECTED;
        this.deviceIOConnectionStatusChangeCallback = deviceIOConnectionStatusChangeCallback;
        this.deviceClientConfigs = new ConcurrentHashMap<>();
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

    //Renaming it to isOpen would be confusing considering this layer's state is either open/closed/reconnecting
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isClosed()
    {
        return this.connectionStatus == IotHubConnectionStatus.DISCONNECTED;
    }

    @Override
    public void onMessageSent(Message message, String deviceId, Throwable e)
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
                log.trace("Message was sent by this client, adding it to callbacks queue with OK_EMPTY ({})", message);
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
        else
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
            log.error("Exception encountered while receiving a message from service {}", message, e);
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
            log.error("Exception encountered while receiving messages from service", e);
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
                log.trace("OnConnectionLost was fired, but for an outdated connection. Ignoring...");
                return;
            }

            if (this.connectionStatus != IotHubConnectionStatus.CONNECTED)
            {
                log.trace("OnConnectionLost was fired, but connection is already disconnected. Ignoring...", e);
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
    public void onMultiplexedDeviceSessionLost(Throwable e, String connectionId, String deviceId)
    {
        if (connectionId.equals(this.iotHubTransportConnection.getConnectionId()))
        {
            log.debug("The device session in the multiplexed connection to the IoT Hub has been lost for device {}", deviceId);
            if (e == null)
            {
                this.updateStatus(IotHubConnectionStatus.DISCONNECTED, IotHubConnectionStatusChangeReason.CLIENT_CLOSE, null, deviceId);
            }
            else
            {
                this.updateStatus(IotHubConnectionStatus.DISCONNECTED_RETRYING, exceptionToStatusChangeReason(e), e, deviceId);

                if (e instanceof TransportException)
                {
                    this.reconnectDeviceSession((TransportException) e, deviceId);
                }
                else
                {
                    this.reconnectDeviceSession(new TransportException(e), deviceId);
                }
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
     *
     * If reconnection is occurring when this is called, this function shall block and wait for the reconnection
     * to finish before trying to open the connection
     *
     * @throws TransportException if a communication channel cannot be established.
     */
    public void open() throws TransportException
    {
        if (this.connectionStatus == IotHubConnectionStatus.CONNECTED)
        {
            return;
        }

        if (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED_RETRYING)
        {
            throw new TransportException("Open cannot be called while transport is reconnecting");
        }

        // The default config is only null when someone creates a multiplexing client and opens it before
        // registering any devices to it. No need to check for SAS token expiry if no devices are registered yet.
        if (this.getDefaultConfig() != null)
        {
            if (this.isSasTokenExpired())
            {
                throw new SecurityException("Your sas token has expired");
            }
        }

        this.taskScheduler = Executors.newScheduledThreadPool(1);

        //Codes_SRS_IOTHUBTRANSPORT_34_019: [This function shall open the invoke the method openConnection.]
        openConnection();

        log.debug("Client connection opened successfully");
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

        log.debug("Client connection closed successfully");
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
    public void addMessage(Message message, IotHubEventCallback callback, Object callbackContext, String deviceId)
    {
        if (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_041: [If this object's connection state is DISCONNECTED, this function shall
            // throw an IllegalStateException.]
            throw new IllegalStateException("Cannot add a message when the transport is closed.");
        }

        //Codes_SRS_IOTHUBTRANSPORT_34_042: [This function shall build a transport packet from the provided message,
        // callback, and context and then add that packet to the waiting queue.]

        // We will get the nested messages and queue them normally if this is a batch message but the protocol is not HTTPS
        // Currently only HTTPS is supports batch message events.
        if (message instanceof BatchMessage && !(this.iotHubTransportConnection instanceof HttpsIotHubConnection))
        {
            for (Message singleMessage : ((BatchMessage)message).getNestedMessages())
            {
                this.addToWaitingQueue(new IotHubTransportPacket(singleMessage, callback, callbackContext,null, System.currentTimeMillis(), deviceId));
                log.info("Messages were queued to be sent later ({})", singleMessage);
            }

            return;
        }

        IotHubTransportPacket packet = new IotHubTransportPacket(message, callback, callbackContext, null, System.currentTimeMillis(), deviceId);
        this.addToWaitingQueue(packet);

        log.info("Message was queued to be sent later ({})", message);
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

            log.debug("Invoking the callback function for sent message, IoT Hub responded to message ({}) with status {}", packet.getMessage(), status);

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
                log.trace("Sending http request to check for any cloud to device messages...");
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
    public void registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext, String deviceId)
    {
        if (callbackContext != null && callback == null)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_051: [If the provided callback is null but the context is not, this function shall throw an IllegalArgumentException.]
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

    public void registerMultiplexingConnectionStateCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext)
    {
        if (callback == null && callbackContext != null)
        {
            throw new IllegalArgumentException("Cannot have a null callback and a non-null context associated with it");
        }

        this.multiplexingStateCallback = callback;
        this.multiplexingStateCallbackContext = callbackContext;
    }

    public void registerMultiplexedDeviceClient(List<DeviceClientConfig> configs, long timeoutMilliseconds) throws InterruptedException, MultiplexingClientException
    {
        if (getProtocol() != IotHubClientProtocol.AMQPS && getProtocol() != IotHubClientProtocol.AMQPS_WS)
        {
            throw new UnsupportedOperationException("Cannot add a multiplexed device unless connection is over AMQPS or AMQPS_WS");
        }

        multiplexingDeviceRegistrationFailures.clear();

        for (DeviceClientConfig configToRegister : configs)
        {
            this.deviceClientConfigs.put(configToRegister.getDeviceId(), configToRegister);

            this.deviceConnectionStates.put(configToRegister.getDeviceId(), IotHubConnectionStatus.DISCONNECTED);
            if (this.iotHubTransportConnection != null)
            {
                // Safe cast since amqps and amqps_ws always use this transport connection type.
                ((AmqpsIotHubConnection) this.iotHubTransportConnection).registerMultiplexedDevice(configToRegister);
            }
        }

        // If the multiplexed connection is active, block until all the registered devices have been connected.
        long timeoutTime = System.currentTimeMillis() + timeoutMilliseconds;
        MultiplexingClientDeviceRegistrationAuthenticationException registrationException = null;
        if (this.connectionStatus != IotHubConnectionStatus.DISCONNECTED)
        {
            for (DeviceClientConfig newlyRegisteredConfig : configs)
            {
                String deviceId = newlyRegisteredConfig.getDeviceId();
                boolean deviceIsNotConnected = deviceConnectionStates.get(deviceId) != IotHubConnectionStatus.CONNECTED;
                Exception deviceRegistrationException = multiplexingDeviceRegistrationFailures.remove(deviceId);
                while (deviceIsNotConnected && deviceRegistrationException == null)
                {
                    Thread.sleep(100);

                    deviceIsNotConnected = deviceConnectionStates.get(deviceId) != IotHubConnectionStatus.CONNECTED;
                    deviceRegistrationException = multiplexingDeviceRegistrationFailures.remove(deviceId);
                    boolean operationHasTimedOut = System.currentTimeMillis() >= timeoutTime;
                    if (operationHasTimedOut)
                    {
                        throw new MultiplexingClientDeviceRegistrationTimeoutException("Timed out waiting for all device registrations to finish.");
                    }
                }

                if (deviceRegistrationException != null)
                {
                    if (registrationException == null)
                    {
                        registrationException = new MultiplexingClientDeviceRegistrationAuthenticationException("Failed to register one or more devices to the multiplexed connection.");
                    }

                    registrationException.addRegistrationException(deviceId, deviceRegistrationException);
                }
            }

            if (registrationException != null)
            {
                throw registrationException;
            }
        }
    }

    public void unregisterMultiplexedDeviceClient(List<DeviceClientConfig> configs, long timeoutMilliseconds) throws InterruptedException, MultiplexingClientException
    {
        if (getProtocol() != IotHubClientProtocol.AMQPS && getProtocol() != IotHubClientProtocol.AMQPS_WS)
        {
            throw new UnsupportedOperationException("Cannot add a multiplexed device unless connection is over AMQPS or AMQPS_WS.");
        }

        for (DeviceClientConfig configToRegister : configs)
        {
            if (this.iotHubTransportConnection != null)
            {
                // Safe cast since amqps and amqps_ws always use this transport connection type.
                ((AmqpsIotHubConnection) this.iotHubTransportConnection).unregisterMultiplexedDevice(configToRegister);
            }
            else
            {
                this.deviceConnectionStates.remove(configToRegister.getDeviceId());
            }

            this.deviceClientConfigs.remove(configToRegister.getDeviceId());
        }

        // If the multiplexed connection is active, block until all the unregistered devices have been disconnected.
        long timeoutTime = System.currentTimeMillis() + timeoutMilliseconds;
        if (this.connectionStatus != IotHubConnectionStatus.DISCONNECTED)
        {
            for (DeviceClientConfig newlyUnregisteredConfig : configs)
            {
                while (deviceConnectionStates.get(newlyUnregisteredConfig.getDeviceId()) != IotHubConnectionStatus.DISCONNECTED)
                {
                    Thread.sleep(100);

                    boolean operationHasTimedOut = System.currentTimeMillis() >= timeoutTime;
                    if (operationHasTimedOut)
                    {
                        throw new MultiplexingClientDeviceRegistrationTimeoutException("Timed out waiting for all device unregistrations to finish.");
                    }
                }
            }
        }
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
            log.debug("Executing callback for received message ({})", receivedMessage);
            //Codes_SRS_IOTHUBTRANSPORT_34_053: [This function shall execute the callback associate with the provided
            // transport message with the provided message and its saved callback context.]
            IotHubMessageResult result = messageCallback.execute(receivedMessage, messageCallbackContext);

            try
            {
                //Codes_SRS_IOTHUBTRANSPORT_34_054: [This function shall send the message callback result along the
                // connection as the ack to the service.]
                log.debug("Sending acknowledgement for received cloud to device message ({})", receivedMessage);
                this.iotHubTransportConnection.sendMessageResult(receivedMessage, result);
            }
            catch (TransportException e)
            {
                //Codes_SRS_IOTHUBTRANSPORT_34_055: [If an exception is thrown while acknowledging the received message,
                // this function shall add the received message back into the receivedMessagesQueue and then rethrow the exception.]
                log.warn("Sending acknowledgement for received cloud to device message failed, adding it back to the queue ({})", receivedMessage, e);
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
                log.debug("Mapping throwable to NO_NETWORK because it was a retryable exception", e);

                //Codes_SRS_IOTHUBTRANSPORT_34_033: [If the provided exception is a retryable TransportException,
                // this function shall return NO_NETWORK.]
                return IotHubConnectionStatusChangeReason.NO_NETWORK;
            }
            else if (isSasTokenExpired())
            {
                log.debug("Mapping throwable to EXPIRED_SAS_TOKEN because it was a non-retryable exception and the saved sas token has expired", e);

                //Codes_SRS_IOTHUBTRANSPORT_34_034: [If the provided exception is a TransportException that isn't
                // retryable and the saved sas token has expired, this function shall return EXPIRED_SAS_TOKEN.]
                return IotHubConnectionStatusChangeReason.EXPIRED_SAS_TOKEN;
            }
            else if (e instanceof UnauthorizedException || e instanceof MqttUnauthorizedException || e instanceof AmqpUnauthorizedAccessException)
            {
                log.debug("Mapping throwable to BAD_CREDENTIAL because it was a non-retryable exception authorization exception but the saved sas token has not expired yet", e);

                //Codes_SRS_IOTHUBTRANSPORT_34_035: [If the provided exception is a TransportException that isn't
                // retryable and the saved sas token has not expired, but the exception is an unauthorized exception,
                // this function shall return BAD_CREDENTIAL.]
                return IotHubConnectionStatusChangeReason.BAD_CREDENTIAL;
            }
        }

        log.debug("Mapping exception throwable to COMMUNICATION_ERROR because the sdk was unable to classify the thrown exception to anything other category", e);

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
        if (this.iotHubTransportConnection == null)
        {
            switch (this.protocol) {
                case HTTPS:
                    this.iotHubTransportConnection = new HttpsIotHubConnection(this.getDefaultConfig());
                    break;
                case MQTT:
                case MQTT_WS:
                    this.iotHubTransportConnection = new MqttIotHubConnection(this.getDefaultConfig());
                    break;
                case AMQPS:
                case AMQPS_WS:
                    if (this.getDefaultConfig() == null)
                    {
                        // The default config is only null when someone creates a multiplexing client and opens it before
                        // registering any devices to it
                        this.iotHubTransportConnection = new AmqpsIotHubConnection(
                                this.hostName,
                                this.protocol == IotHubClientProtocol.AMQPS_WS,
                                this.sslContext,
                                this.proxySettings);
                    }
                    else
                    {
                        this.iotHubTransportConnection = new AmqpsIotHubConnection(this.getDefaultConfig());
                    }

                    // If multiplexing, register all devices in the amqp connection that are registered here at this point
                    if (this.deviceClientConfigs.size() > 1)
                    {
                        for (DeviceClientConfig config : this.deviceClientConfigs.values())
                        {
                            ((AmqpsIotHubConnection) this.iotHubTransportConnection).registerMultiplexedDevice(config);
                        }
                    }

                    break;
                default:
                    throw new TransportException("Protocol not supported");
            }
        }

        //Codes_SRS_IOTHUBTRANSPORT_34_038: [This function shall set this object as the listener of the iotHubTransportConnection object.]
        this.iotHubTransportConnection.setListener(this);

        //Codes_SRS_IOTHUBTRANSPORT_34_039: [This function shall open the iotHubTransportConnection object with the saved list of configs.]
        this.iotHubTransportConnection.open();

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
        log.info("Handling a disconnection event", transportException);

        synchronized (this.inProgressMessagesLock)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_057: [This function shall move all packets from inProgressQueue to waiting queue.]
            log.trace("Due to disconnection event, clearing active queues, and re-queueing them to waiting queues to be re-processed later upon reconnection");
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

        log.debug("Starting reconnection logic");
        //Codes_SRS_IOTHUBTRANSPORT_34_060: [This function shall invoke reconnect with the provided exception.]
        reconnect(transportException);
    }

    // should only be called when multiplexing an only a particular device went offline
    private void reconnectDeviceSession(TransportException transportException, String deviceId)
    {
        long reconnectionStartTimeMillis = System.currentTimeMillis();
        int reconnectionAttempts = 0;
        boolean hasReconnectOperationTimedOut = this.hasOperationTimedOut(reconnectionStartTimeMillis, deviceId);
        RetryDecision retryDecision = null;

        while (this.deviceConnectionStates.get(deviceId) == IotHubConnectionStatus.DISCONNECTED_RETRYING
                && !hasReconnectOperationTimedOut)
        {
            log.trace("Attempting to reconnect device session: attempt {}", reconnectionAttempts);
            reconnectionAttempts++;

            RetryPolicy retryPolicy = this.getConfig(deviceId).getRetryPolicy();
            retryDecision = retryPolicy.getRetryDecision(reconnectionAttempts, transportException);
            if (!retryDecision.shouldRetry())
            {
                break;
            }

            // This call triggers some async amqp logic, so all this function can do is wait for a bit and check the connection
            // status for this device before retrying.
            singleDeviceReconnectAttemptAsync(deviceId);

            log.trace("Sleeping between device reconnect attempts for device {}", deviceId);
            IotHubTransport.sleepUninterruptibly(retryDecision.getDuration(), MILLISECONDS);

            hasReconnectOperationTimedOut = this.hasOperationTimedOut(reconnectionStartTimeMillis);
        }

        // reconnection may have failed, so check last retry decision, check for timeout, and check if last exception
        // was terminal
        try
        {
            if (retryDecision != null && !retryDecision.shouldRetry())
            {
                log.debug("Reconnection was abandoned due to the retry policy");
                this.close(IotHubConnectionStatusChangeReason.RETRY_EXPIRED, transportException);
            }
            else if (this.hasOperationTimedOut(reconnectionStartTimeMillis))
            {
                log.debug("Reconnection was abandoned due to the operation timeout");
                this.close(
                        IotHubConnectionStatusChangeReason.RETRY_EXPIRED,
                        new DeviceOperationTimeoutException("Device operation for reconnection timed out"));
            }
            else if (transportException != null && !transportException.isRetryable())
            {
                log.error("Reconnection was abandoned due to encountering a non-retryable exception", transportException);
                this.close(this.exceptionToStatusChangeReason(transportException), transportException);
            }
        }
        catch (DeviceClientException ex)
        {
            log.error("Encountered an exception while closing the client object, client instance should no longer be used as the state is unknown", ex);
            this.updateStatus(IotHubConnectionStatus.DISCONNECTED, IotHubConnectionStatusChangeReason.COMMUNICATION_ERROR, transportException, deviceId);
        }
    }

    /**
     * Attempts to close and then re-open the connection until connection reestablished, retry policy expires, or a
     * terminal exception is encountered. At the end of this call, the state of this object should be either
     * CONNECTED or DISCONNECTED depending on how reconnection goes.
     *
     * If multiplexing, this will close all open device sessions and the amqp connection and then will attempt to re-open all
     * of them.
     */
    // warning is about how this.getDefaultConfig() may return null. In this case, it never will since we already check
    // the deviceClientConfigs size prior to getting the default config
    @SuppressWarnings("ConstantConditions")
    private void reconnect(TransportException transportException)
    {
        long reconnectionStartTimeMillis = System.currentTimeMillis();
        int reconnectionAttempts = 0;

        boolean hasReconnectOperationTimedOut = this.hasOperationTimedOut(reconnectionStartTimeMillis);
        RetryDecision retryDecision = null;

        while (this.connectionStatus == IotHubConnectionStatus.DISCONNECTED_RETRYING
                && !hasReconnectOperationTimedOut
                && transportException != null
                && transportException.isRetryable())
        {
            log.trace("Attempting reconnect attempt {}", reconnectionAttempts);
            reconnectionAttempts++;

            RetryPolicy retryPolicy;
            if (this.deviceClientConfigs.size() != 1)
            {
                retryPolicy = multiplexingRetryPolicy;
            }
            else
            {
                retryPolicy = this.getDefaultConfig().getRetryPolicy();
            }
            retryDecision = retryPolicy.getRetryDecision(reconnectionAttempts, transportException);
            if (!retryDecision.shouldRetry())
            {
                break;
            }

            log.trace("Sleeping between reconnect attempts");
            //Want to sleep without interruption because the only interruptions expected are threads that add a message
            // to the waiting list again. Those threads should wait until after reconnection finishes first because
            // they will constantly fail until connection is re-established
            IotHubTransport.sleepUninterruptibly(retryDecision.getDuration(), MILLISECONDS);

            hasReconnectOperationTimedOut = this.hasOperationTimedOut(reconnectionStartTimeMillis);

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
                log.debug("Reconnection was abandoned due to the retry policy");
                this.close(IotHubConnectionStatusChangeReason.RETRY_EXPIRED, transportException);
            }
            else if (this.hasOperationTimedOut(reconnectionStartTimeMillis))
            {
                //Codes_SRS_IOTHUBTRANSPORT_34_069: [If the reconnection effort ends because the reconnection timed out,
                // this function shall invoke close with RETRY_EXPIRED and a DeviceOperationTimeoutException.]
                log.debug("Reconnection was abandoned due to the operation timeout");
                this.close(
                        IotHubConnectionStatusChangeReason.RETRY_EXPIRED,
                        new DeviceOperationTimeoutException("Device operation for reconnection timed out"));
            }
            else if (transportException != null && !transportException.isRetryable())
            {
                //Codes_SRS_IOTHUBTRANSPORT_34_070: [If the reconnection effort ends because a terminal exception is
                // encountered, this function shall invoke close with that terminal exception.]
                log.error("Reconnection was abandoned due to encountering a non-retryable exception", transportException);
                this.close(this.exceptionToStatusChangeReason(transportException), transportException);
            }
        }
        catch (DeviceClientException ex)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_071: [If an exception is encountered while closing, this function shall invoke
            // updateStatus with DISCONNECTED, COMMUNICATION_ERROR, and the last transport exception.]
            log.error("Encountered an exception while closing the client object, client instance should no longer be used as the state is unknown", ex);
            this.updateStatus(IotHubConnectionStatus.DISCONNECTED, IotHubConnectionStatusChangeReason.COMMUNICATION_ERROR, transportException);
        }
    }

    //For reconnecting multiplexed devices only. Since this triggers asynchronous functions in the AMQP layer, there
    // is no guarantee that the reconnect worked just because the unregister/register calls return successfully.
    // Still need to check the device connection status before you can report the device to be re-connected.
    private void singleDeviceReconnectAttemptAsync(String deviceId)
    {
        DeviceClientConfig config = getConfig(deviceId);
        ((AmqpsIotHubConnection) this.iotHubTransportConnection).unregisterMultiplexedDevice(config);
        ((AmqpsIotHubConnection) this.iotHubTransportConnection).registerMultiplexedDevice(config);
    }

    private DeviceClientConfig getConfig(String deviceId)
    {
        DeviceClientConfig config = this.deviceClientConfigs.get(deviceId);

        if (config != null)
        {
            return config;
        }

        throw new IllegalStateException(String.format("Device client config does not exist for device %s", deviceId));
    }

    /**
     * Attempts to close and then re-open the iotHubTransportConnection once
     * @return the exception encountered during closing or opening, or null if reconnection succeeded
     */
    private TransportException singleReconnectAttempt()
    {
        try
        {
            log.trace("Attempting to close and re-open the iot hub transport connection...");
            //Codes_SRS_IOTHUBTRANSPORT_34_061: [This function shall close the saved connection, and then invoke openConnection and return null.]
            this.iotHubTransportConnection.close();
            this.openConnection();
            log.trace("Successfully closed and re-opened the iot hub transport connection");
        }
        catch (TransportException newTransportException)
        {
            //Codes_SRS_IOTHUBTRANSPORT_34_062: [If an exception is encountered while closing or opening the connection,
            // this function shall invoke checkForUnauthorizedException on that exception and then return it.]
            checkForUnauthorizedException(newTransportException);
            log.warn("Failed to close and re-open the iot hub transport connection, checking if another retry attempt should be made", newTransportException);
            return newTransportException;
        }

        return null;
    }

    /**
     * Task for adding a packet back to the waiting queue. Used for delaying message retry
     */
    public static class MessageRetryRunnable implements Runnable
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
            synchronized (this.sendThreadLock)
            {
                this.sendThreadLock.notifyAll();
            }
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
        log.warn("Handling an exception from sending message: Attempt number {}", packet.getCurrentRetryAttempt(), transportException);

        packet.incrementRetryAttempt();
        if (!this.hasOperationTimedOut(packet.getStartTimeMillis()))
        {
            if (transportException.isRetryable())
            {
                RetryDecision retryDecision = this.getConfig(packet.getDeviceId()).getRetryPolicy().getRetryDecision(packet.getCurrentRetryAttempt(), transportException);
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

            //Codes_SRS_IOTHUBTRANSPORT_34_073: [This function shall send the provided message over the saved connection
            // and save the response code.]
            log.info("Sending message ({})", message);
            IotHubStatusCode statusCode = this.iotHubTransportConnection.sendMessage(message);
            log.trace("Sent message ({}) to protocol level, returned status code was {}", message, statusCode);

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
            log.warn("Encountered exception while sending message with correlation id {}", message.getCorrelationId(), transportException);
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
            log.warn("Message with has expired, adding to callbacks queue with MESSAGE_EXPIRED ({})", message);
            packet.setStatus(IotHubStatusCode.MESSAGE_EXPIRED);
            this.addToCallbackQueue(packet);
            return false;
        }

        if (isSasTokenExpired())
        {
            //Codes_SRS_IOTHUBTRANSPORT_28_010:[This function shall set the packet status to UNAUTHORIZED if sas token has expired.]
            //Codes_SRS_IOTHUBTRANSPORT_28_011:[This function shall add the packet which sas token has expired to the Callback Queue.]
            log.debug("Creating a callback for the message with expired sas token with UNAUTHORIZED status");
            packet.setStatus(IotHubStatusCode.UNAUTHORIZED);
            this.addToCallbackQueue(packet);
            this.updateStatus(
                    IotHubConnectionStatus.DISCONNECTED,
                    IotHubConnectionStatusChangeReason.EXPIRED_SAS_TOKEN,
                    new SecurityException("Your sas token has expired"),
                    packet.getMessage().getConnectionDeviceId());

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
                log.info("Updating transport status to new status {} with reason {}", newConnectionStatus, reason);
            }
            else
            {
                log.warn("Updating transport status to new status {} with reason {}", newConnectionStatus, reason, throwable);
            }

            this.connectionStatus = newConnectionStatus;

            //invoke connection status callbacks
            log.debug("Invoking connection status callbacks with new status details");
            invokeConnectionStateCallback(newConnectionStatus, reason);

            if (deviceClientConfigs.size() < 2 || newConnectionStatus != IotHubConnectionStatus.CONNECTED)
            {
                // When multiplexing, a different method will notify each device-specific callback when that device is online,
                // but in cases when the tcp connection is lost and everything is disconnected retrying or disconnected, this is where the
                // callback should be fired
                invokeConnectionStatusChangeCallback(newConnectionStatus, reason, throwable);

                for (DeviceClientConfig config : deviceClientConfigs.values())
                {
                    deviceConnectionStates.put(config.getDeviceId(), newConnectionStatus);
                }
            }

            // If multiplexing, fire the multiplexing state callback as long as it was set.
            if (deviceClientConfigs.size() > 1 && this.multiplexingStateCallback != null)
            {
                this.multiplexingStateCallback.execute(newConnectionStatus, reason, throwable, this.multiplexingStateCallbackContext);
            }

            this.deviceIOConnectionStatusChangeCallback.execute(newConnectionStatus, reason, throwable, null);
        }
    }

    private void updateStatus(IotHubConnectionStatus newConnectionStatus, IotHubConnectionStatusChangeReason reason, Throwable throwable, String deviceId)
    {
        if (this.deviceConnectionStates.containsKey(deviceId) && this.deviceConnectionStates.get(deviceId) != newConnectionStatus)
        {
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
                this.deviceConnectionStates.put(deviceId, newConnectionStatus);

                log.debug("Invoking connection status callbacks with new status details");
                invokeConnectionStateCallback(newConnectionStatus, reason);
                invokeConnectionStatusChangeCallback(newConnectionStatus, reason, throwable, deviceId);
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
        for (String registeredDeviceId : this.connectionStatusChangeCallbacks.keySet())
        {
            this.connectionStatusChangeCallbacks.get(registeredDeviceId).execute(status, reason, e, this.connectionStatusChangeCallbackContexts.get(registeredDeviceId));
        }
    }

    private void invokeConnectionStatusChangeCallback(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason reason, Throwable e, String deviceId)
    {
        if (deviceId == null)
        {
            for (String registeredDeviceId : this.connectionStatusChangeCallbacks.keySet())
            {
                this.connectionStatusChangeCallbacks.get(registeredDeviceId).execute(status, reason, e, this.connectionStatusChangeCallbackContexts.get(registeredDeviceId));
            }
        }
        else if (this.connectionStatusChangeCallbacks.containsKey(deviceId))
        {
            this.connectionStatusChangeCallbacks.get(deviceId).execute(status, reason, e, this.connectionStatusChangeCallbackContexts.get(deviceId));
        }
        else
        {
            log.trace("Device {} did not have a connection status change callback registered, so no callback was fired.");
        }
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

        return this.getDefaultConfig().getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN
                && this.getDefaultConfig().getSasTokenAuthentication().isAuthenticationProviderRenewalNecessary();
    }

    /**
     * Returns if the provided packet has lasted longer than the device operation timeout
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
     * @return true if the packet has been in the queues for longer than the device operation timeout and false otherwise
     */
    private boolean hasOperationTimedOut(long startTime, String deviceId)
    {
        if (startTime == 0)
        {
            return false;
        }

        return (System.currentTimeMillis() - startTime) > this.getConfig(deviceId).getOperationTimeout();
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

    private DeviceClientConfig getDefaultConfig()
    {
        for (DeviceClientConfig config : this.deviceClientConfigs.values())
        {
            // just return the first entry in the list.
            return config;
        }

        // should only happen when using multiplexing client and opening the connection before registering any devices
        return null;
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
    @SuppressWarnings("SameParameterValue") // The TimeUnit is currently always MilliSeconds, but this method can be used generically as well.
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
