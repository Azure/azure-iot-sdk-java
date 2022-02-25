// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.exceptions.DeviceClientException;
import com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.device.transport.IotHubReceiveTask;
import com.microsoft.azure.sdk.iot.device.transport.IotHubSendTask;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransport;
import com.microsoft.azure.sdk.iot.device.transport.RetryPolicy;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT_WS;

/*
 *     +-------------------------------------+                  +-----------------------------------+
 *     |                                     |                  |                                   |
 *     |             DeviceClient            |------------------+        DeviceClientConfig         |
 *     |                                     |                  |                                   |
 *     +-------------------------------------+                  +-----------------------------------+
 *        |                        |
 *        |                       \/
 *        |  +---------------------------------------------------------------------------------------------+
 *        |  | Services                                                                                    |
 *        |  |  +-----------+    +------------+    +--------------+                        +------------+  |
 *        |  |  | Telemetry |    | DeviceTwin |    | DirectMethod |                        | FileUpload |  |
 *        |  |  +-----------+    +------------+    +--------------+                        +---------+--+  |
 *        |  +---------------------------------------------------------------------------------------|-----+
 *        |                                    |                                                     |
 *       \/                                   \/                                                     |
 *     #####################################################################################         |
 *     # DeviceIO                                                                          #         |
 *     #  +----------------+    +-------------------------------------+    +------------+  #         |
 *     #  |                |    |                open                 |    |            |  #         |
 *     #  | sendEventAsync |    |                   +---------------+ |    |   close    |  #         |
 *     #  |                |    |                   | taskScheduler | |    |            |  #         |
 *     #  +--------+-------+    +--+----------------+--+---------+--+-+    +--------+---+  #         |
 *     ############|###############|###################|#########|##################|#######         |
 *                 |               |                   |         |                  |                |
 *                 |               |                  \/        \/                  |                |
 *                 |               |    +----------------+   +-------------------+  |                |
 *                 |               |    | IoTHubSendTask |   | IoTHubReceiveTask |  |                |
 *                 |               |    |   +--------+   |   |    +---------+    |  |                |
 *                 |               |    |   |   Run  |   |   |    |   Run   |    |  |                |
 *                 |               |    +---+---+----+---+   +----+----+----+----+  |                |
 * IotHubTransport |               |            |                      |            |                |
 *       +---------|---------------|------------|----------------------|------------|--------+   +----------------------------------------------+
 *       |        \/              \/           \/                     \/           \/        |   | IoTHubTransportManager                       |
 *       |  +------------+  +------------+  +--------------+  +---------------+  +---------+ |   |  +------+  +-------+  +------+  +---------+  |
 *       |  | addMessage |  |    Open    |  | sendMessages |  | handleMessage |  |  Close  | |   |  | Open |  | Close |  | send |  | receive |  |
 *       |  +------------+  +------------+  +--------------+  +---------------+  +---------+ |   |  +------+  +-------+  +------+  +---------+  |
 *       +----------+--------------------------------+-------------------------+-------------+   +---+------------------------------------------+
 *                  |                                |                         |                     |
 *                 \/                               \/                        \/                    \/
 *      +-------------------------+    +-------------------------+    +------------------+  +-----------------------+
 *      |      AmqpsTransport     |    |      MqttTransport      |    |  HttpsTransport  |  | HttpsTransportManager |
 *      +-------------------------+    +-------------------------+    +---------------------------------------------+
 *      |  AmqpsIotHubConnection  |    |  MqttIotHubConnection   |    |             HttpsIotHubConnection           |
 *      +-------------------------+    +-------------------------+    +---------------------------------------------+
 *
 */

/**
 * The task scheduler for sending and receiving messages for the Device Client
 */
@Slf4j
final class DeviceIO implements IotHubConnectionStatusChangeCallback
{
    private static final int SEND_PERIOD_MILLIS = 10;
    private static final int RECEIVE_PERIOD_MILLIS = 10;

    private long sendPeriodInMilliseconds = SEND_PERIOD_MILLIS;
    private long receivePeriodInMilliseconds = RECEIVE_PERIOD_MILLIS;

    private final IotHubTransport transport;
    private final IotHubSendTask sendTask;
    private final IotHubReceiveTask receiveTask;

    private ScheduledExecutorService receiveTaskScheduler;
    private ScheduledExecutorService sendTaskScheduler;
    private IotHubConnectionStatus state;


    // This lock is used to keep calls to open/close/connection status changes synchronous.
    private final Object stateLock = new Object();

    /**
     * Constructor that takes a connection string as an argument.
     *
     * @param config the connection configuration.
     *
     * @throws IllegalArgumentException if any of {@code config} or
     * {@code protocol} are {@code null}.
     */
    DeviceIO(DeviceClientConfig config)
    {
        if (config == null)
        {
            throw new IllegalArgumentException("Config cannot be null.");
        }

        IotHubClientProtocol protocol = config.getProtocol();
        config.setUseWebsocket(protocol == IotHubClientProtocol.AMQPS_WS || protocol == MQTT_WS);

        this.state = IotHubConnectionStatus.DISCONNECTED;

        this.transport = new IotHubTransport(config, this, false);

        this.state = IotHubConnectionStatus.DISCONNECTED;

        this.sendTask = new IotHubSendTask(this.transport);
        this.receiveTask = new IotHubReceiveTask(this.transport);
    }

    DeviceIO(
        String hostName,
        IotHubClientProtocol protocol,
        SSLContext sslContext,
        ProxySettings proxySettings,
        int keepAliveInterval)
    {
        this.state = IotHubConnectionStatus.DISCONNECTED;
        this.transport = new IotHubTransport(hostName, protocol, sslContext, proxySettings, this, keepAliveInterval);
        this.sendTask = new IotHubSendTask(this.transport);
        this.receiveTask = new IotHubReceiveTask(this.transport);
    }

    /**
     * Starts asynchronously sending and receiving messages from an IoT Hub. If
     * the client is already open, the function shall do nothing.
     *
     * @throws IOException if a connection to an IoT Hub cannot be established.
     */
    void open(boolean withRetry) throws IOException
    {
        synchronized (this.stateLock)
        {
            if (this.isOpen())
            {
                return;
            }

            try
            {
                this.transport.open(withRetry);
            }
            catch (DeviceClientException e)
            {
                throw new IOException("Could not open the connection", e);
            }
        }
    }

    // Functionally the same as "open()", but without wrapping any thrown TransportException into an IOException
    void openWithoutWrappingException(boolean withRetry) throws TransportException
    {
        try
        {
            open(withRetry);
        }
        catch (IOException e)
        {
            // We did this silly thing in the DeviceClient to work around the fact that we can't throw TransportExceptions
            // directly in methods like deviceClient.open() because the open API existed before the TransportException did.
            // To get around it, we just nested the meaningful exception into an IOException. The multiplexing client doesn't
            // have to do the same thing though, so this code un-nests the exception when possible.
            Throwable cause = e.getCause();
            if (cause instanceof TransportException)
            {
                throw (TransportException) cause;
            }

            // should never happen. Open only throws IOExceptions with an inner exception of type TransportException
            throw new IllegalStateException("Encountered a wrapped IOException with no inner transport exception", e);
        }
    }

    void registerMultiplexedDeviceClient(List<DeviceClientConfig> configs, long timeoutMilliseconds) throws InterruptedException, MultiplexingClientException
    {
        this.transport.registerMultiplexedDeviceClient(configs, timeoutMilliseconds);
    }

    void unregisterMultiplexedDeviceClient(List<DeviceClientConfig> configs, long timeoutMilliseconds) throws InterruptedException, MultiplexingClientException
    {
        this.transport.unregisterMultiplexedDeviceClient(configs, timeoutMilliseconds);
    }

    void setMultiplexingRetryPolicy(RetryPolicy retryPolicy)
    {
        this.transport.setMultiplexingRetryPolicy(retryPolicy);
    }

    void setMaxNumberOfMessagesSentPerSendThread(int maxNumberOfMessagesSentPerSendThread)
    {
        this.transport.setMaxNumberOfMessagesSentPerSendThread(maxNumberOfMessagesSentPerSendThread);
    }

    /**
     * Handles logic common to all open functions.
     */
    private void startWorkerThreads()
    {
        // while startWorkerThreads should never be called when threads are already active, it doesn't hurt to double
        // check that any previous thread pools have been shut down.
        stopWorkerThreads();

        log.debug("Starting worker threads");

        this.sendTaskScheduler = Executors.newScheduledThreadPool(1);
        this.receiveTaskScheduler = Executors.newScheduledThreadPool(1);

        // Note that even though these threads are scheduled at a fixed interval, the sender/receiver threads will wait
        // if no messages are available to process. These waiting threads will still count against the pool size defined above,
        // so threads will not be needlessly scheduled during times when this SDK has no messages to process.

        // the scheduler waits until each execution is finished before
        // scheduling the next one, so executions of a given task
        // will never overlap.

        // Note that this is scheduleWithFixedDelay, not scheduleAtFixedRate. There is no reason to spawn a new
        // send/receive thread until after the previous one has finished.
        this.sendTaskScheduler.scheduleWithFixedDelay(this.sendTask, 0,
                sendPeriodInMilliseconds, TimeUnit.MILLISECONDS);
        this.receiveTaskScheduler.scheduleWithFixedDelay(this.receiveTask, 0,
                receivePeriodInMilliseconds, TimeUnit.MILLISECONDS);

        this.state = IotHubConnectionStatus.CONNECTED;
    }

    private void stopWorkerThreads()
    {
        if (this.sendTaskScheduler != null)
        {
            log.trace("Shutting down sendTaskScheduler");
            this.sendTaskScheduler.shutdownNow();
            this.sendTaskScheduler = null;
        }

        if (this.receiveTaskScheduler != null)
        {
            log.trace("Shutting down receiveTaskScheduler");
            this.receiveTaskScheduler.shutdownNow();
            this.receiveTaskScheduler = null;
        }
    }

    /**
     * Completes all current outstanding requests and closes the IoT Hub client.
     * Must be called to terminate the background thread that is sending data to
     * IoT Hub. After {@code close()} is called, the IoT Hub client is no longer
     *  usable. If the client is already closed, the function shall do nothing.
     */
    void close()
    {
        synchronized (this.stateLock)
        {
            if (state == IotHubConnectionStatus.DISCONNECTED)
            {
                return;
            }

            this.transport.close(IotHubConnectionStatusChangeReason.CLIENT_CLOSE, null);
            this.state = IotHubConnectionStatus.DISCONNECTED;
        }
    }

    /**
     * Asynchronously sends an event message to the IoT Hub.
     *
     * @param message the message to be sent.
     * @param callback the callback to be invoked when a response is received.
     * Can be {@code null}.
     * @param callbackContext a context to be passed to the callback. Can be
     * {@code null} if no callback is provided.
     * @param deviceId the id of the device sending the message
     *
     * @throws IllegalArgumentException if the message provided is {@code null}.
     * @throws IllegalStateException if the client has not been opened yet or is already closed.
     */
    void sendEventAsync(Message message,
                               IotHubEventCallback callback,
                               Object callbackContext,
                               String deviceId)
    {
        if (!this.isOpen())
        {
            throw new IllegalStateException(
                    "Cannot send event from "
                            + "an IoT Hub client that is closed.");
        }

        if (message == null)
        {
            throw new IllegalArgumentException("Cannot send message 'null'.");
        }

        if (deviceId != null)
        {
            message.setConnectionDeviceId(deviceId);
        }

        transport.addMessage(message, callback, callbackContext, deviceId);
    }

    /**
     * Setter for the receive period in milliseconds.
     *
     * @param newIntervalInMilliseconds is the new interval in milliseconds.
     * @throws IllegalArgumentException if the provided interval is invalid (zero or negative).
     */
    void setReceivePeriodInMilliseconds(long newIntervalInMilliseconds)
    {
        if (newIntervalInMilliseconds <= 0L)
        {
            throw new IllegalArgumentException("receive interval can not be zero or negative");
        }

        this.receivePeriodInMilliseconds = newIntervalInMilliseconds;

        if (this.receiveTaskScheduler != null)
        {
            // close the old scheduler and start a new one with the new receive period
            this.receiveTaskScheduler.shutdown();
            this.receiveTaskScheduler = Executors.newScheduledThreadPool(1);
            this.receiveTaskScheduler.scheduleAtFixedRate(
                this.receiveTask,
                0,
                this.receivePeriodInMilliseconds,
                TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Setter for the send period in milliseconds.
     *
     * @param newIntervalInMilliseconds is the new interval in milliseconds.
     * @throws IllegalArgumentException if the provided interval is invalid (zero or negative).
     */
    void setSendPeriodInMilliseconds(long newIntervalInMilliseconds)
    {
        if (newIntervalInMilliseconds <= 0L)
        {
            throw new IllegalArgumentException("send interval can not be zero or negative");
        }

        this.sendPeriodInMilliseconds = newIntervalInMilliseconds;

        if (this.sendTaskScheduler != null)
        {
            // close the old scheduler and start a new one with the new send period
            this.sendTaskScheduler.shutdown();
            this.sendTaskScheduler = Executors.newScheduledThreadPool(1);
            this.sendTaskScheduler.scheduleAtFixedRate(
                this.sendTask,
                0,
                this.sendPeriodInMilliseconds,
                TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Getter for the transport protocol.
     *
     * @return a protocol for transport.
     */
    IotHubClientProtocol getProtocol()
    {
        return this.transport.getProtocol();
    }

    /**
     * Getter for the connection state.
     *
     * @return a boolean true if the connection is open or reconnecting, and false otherwise.
     */
    boolean isOpen()
    {
        // Although the method is called "isOpen", it has always returned true even when the client is in a reconnecting state.
        // This allows users to still queue messages as they will be sent after the reconnection completes.
        return (this.state == IotHubConnectionStatus.CONNECTED || this.state == IotHubConnectionStatus.DISCONNECTED_RETRYING);
    }

    void setConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback statusChangeCallback, Object callbackContext, String deviceId)
    {
        this.transport.setConnectionStatusChangeCallback(statusChangeCallback, callbackContext, deviceId);
    }

    void setMultiplexingConnectionStateCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext)
    {
        this.transport.setMultiplexingConnectionStateCallback(callback, callbackContext);
    }

    /*
     * IotHubTransport layer will notify this layer when the connection is established and when it is lost. This layer should start/stop
     * the send/receive threads accordingly
     */
    @Override
    public void onStatusChanged(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
    {
        log.trace("DeviceIO notified of status {} with reason {}", status, statusChangeReason);

        if (status == this.state)
        {
            // no change in status, so no need to start/stop worker threads.
            return;
        }

        if (status == IotHubConnectionStatus.DISCONNECTED || status == IotHubConnectionStatus.DISCONNECTED_RETRYING)
        {
            // No need to keep spawning send/receive tasks during reconnection or when the client is closed
            this.stopWorkerThreads();
        }
        else if (status == IotHubConnectionStatus.CONNECTED)
        {
            // Restart the task scheduler so that send/receive tasks start spawning again
            this.startWorkerThreads();
        }

        this.state = status;
    }
}
