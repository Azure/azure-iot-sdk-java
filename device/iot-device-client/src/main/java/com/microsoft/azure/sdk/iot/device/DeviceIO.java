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
 *        |  |  | Telemetry |    | DeviceTwin |    | DeviceMethod |                        | FileUpload |  |
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
public final class DeviceIO implements IotHubConnectionStatusChangeCallback
{
    private long sendPeriodInMilliseconds;
    private long receivePeriodInMilliseconds;

    private final IotHubTransport transport;
    private IotHubSendTask sendTask = null;
    private IotHubReceiveTask receiveTask = null;

    private ScheduledExecutorService receiveTaskScheduler;
    private ScheduledExecutorService sendTaskScheduler;
    private IotHubConnectionStatus state;


    // This lock is used to keep calls to open/close/connection status changes synchronous.
    private final Object stateLock = new Object();

    /**
     * Constructor that takes a connection string as an argument.
     *
     * @param config the connection configuration.
     * @param sendPeriodInMilliseconds the period of time that iot hub will try to send messages in milliseconds.
     * @param receivePeriodInMilliseconds the period of time that iot hub will try to receive messages in milliseconds.
     *
     * @throws IllegalArgumentException if any of {@code config} or
     * {@code protocol} are {@code null}.
     */
    DeviceIO(DeviceClientConfig config, long sendPeriodInMilliseconds, long receivePeriodInMilliseconds)
    {
        this(config, sendPeriodInMilliseconds, receivePeriodInMilliseconds, false);
    }

    DeviceIO(DeviceClientConfig config, long sendPeriodInMilliseconds, long receivePeriodInMilliseconds, boolean isMultiplexing)
    {
        if (config == null)
        {
            throw new IllegalArgumentException("Config cannot be null.");
        }

        IotHubClientProtocol protocol = config.getProtocol();
        config.setUseWebsocket(protocol == IotHubClientProtocol.AMQPS_WS || protocol == MQTT_WS);

        this.sendPeriodInMilliseconds = sendPeriodInMilliseconds;
        this.receivePeriodInMilliseconds = receivePeriodInMilliseconds;

        this.state = IotHubConnectionStatus.DISCONNECTED;

        this.transport = new IotHubTransport(config, this, isMultiplexing);

        this.sendPeriodInMilliseconds = sendPeriodInMilliseconds;
        this.receivePeriodInMilliseconds = receivePeriodInMilliseconds;

        this.state = IotHubConnectionStatus.DISCONNECTED;
    }

    DeviceIO(String hostName, IotHubClientProtocol protocol, SSLContext sslContext, ProxySettings proxySettings, long sendPeriodInMilliseconds, long receivePeriodInMilliseconds)
    {
        this.sendPeriodInMilliseconds = sendPeriodInMilliseconds;
        this.receivePeriodInMilliseconds = receivePeriodInMilliseconds;
        this.state = IotHubConnectionStatus.DISCONNECTED;
        this.transport = new IotHubTransport(hostName, protocol, sslContext, proxySettings, this);
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
        this.sendTask = new IotHubSendTask(this.transport);
        this.receiveTask = new IotHubReceiveTask(this.transport);

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

    /**
     * Completes all current outstanding requests and closes the IoT Hub client.
     * Must be called to terminate the background thread that is sending data to
     * IoT Hub. After {@code close()} is called, the IoT Hub client is no longer
     *  usable. If the client is already closed, the function shall do nothing.
     *
     * @throws IOException if the connection to an IoT Hub cannot be closed.
     */
    public void close() throws IOException
    {
        synchronized (this.stateLock)
        {
            if (state == IotHubConnectionStatus.DISCONNECTED)
            {
                return;
            }

            if (this.sendTaskScheduler != null)
            {
                this.sendTaskScheduler.shutdown();
            }

            if (this.receiveTaskScheduler != null)
            {
                this.receiveTaskScheduler.shutdown();
            }

            this.transport.close(IotHubConnectionStatusChangeReason.CLIENT_CLOSE, null);
            this.state = IotHubConnectionStatus.DISCONNECTED;
        }
    }

    // Functionally the same as "close()", but without wrapping any thrown TransportException into an IOException
    public void closeWithoutWrappingException() throws TransportException
    {
        try
        {
            close();
        }
        catch (IOException e)
        {
            // We did this silly thing in the DeviceClient to work around the fact that we can't throw TransportExceptions
            // directly in methods like deviceClient.close() because the close API existed before the TransportException did.
            // To get around it, we just nested the meaningful exception into an IOException. The multiplexing client doesn't
            // have to do the same thing though, so this code un-nests the exception when possible.
            if (e.getCause() != null && e.getCause() instanceof TransportException)
            {
                throw (TransportException) e.getCause();
            }

            // should never happen. Open only throws IOExceptions with an inner exception of type TransportException
            throw new IllegalStateException("Encountered a wrapped IOException with no inner transport exception", e);
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
    public synchronized void sendEventAsync(Message message,
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
     * Getter for the receive period in milliseconds.
     *
     * @return a long with the number of milliseconds between receives.
     */
    public long getReceivePeriodInMilliseconds()
    {
        return this.receivePeriodInMilliseconds;
    }

    /**
     * Setter for the receive period in milliseconds.
     *
     * @param newIntervalInMilliseconds is the new interval in milliseconds.
     * @throws IOException if the task schedule exist but there is no receive task function to call.
     * @throws IllegalArgumentException if the provided interval is invalid (zero or negative).
     */
    public void setReceivePeriodInMilliseconds(long newIntervalInMilliseconds) throws IOException
    {
        if(newIntervalInMilliseconds <= 0L)
        {
            throw new IllegalArgumentException("receive interval can not be zero or negative");
        }

        this.receivePeriodInMilliseconds = newIntervalInMilliseconds;

        if (this.receiveTaskScheduler != null)
        {
            if (this.receiveTask == null)
            {
                throw new IOException("transport receive task not set");
            }

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
     * Getter for the send period in milliseconds.
     *
     * @return a long with the number of milliseconds between sends.
     */
    public long getSendPeriodInMilliseconds()
    {
        return this.sendPeriodInMilliseconds;
    }

    /**
     * Setter for the send period in milliseconds.
     *
     * @param newIntervalInMilliseconds is the new interval in milliseconds.
     * @throws IOException if the task schedule exist but there is no send task function to call.
     * @throws IllegalArgumentException if the provided interval is invalid (zero or negative).
     */
    public void setSendPeriodInMilliseconds(long newIntervalInMilliseconds) throws IOException
    {
        if(newIntervalInMilliseconds <= 0L)
        {
            throw new IllegalArgumentException("send interval can not be zero or negative");
        }

        this.sendPeriodInMilliseconds = newIntervalInMilliseconds;

        if (this.sendTaskScheduler != null)
        {
            if (this.sendTask == null)
            {
                throw new IOException("transport send task not set");
            }

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
    public IotHubClientProtocol getProtocol()
    {
        return this.transport.getProtocol();
    }

    /**
     * Getter for the connection state.
     *
     * @return a boolean true if the connection is open or reconnecting, and false otherwise.
     */
    public boolean isOpen()
    {
        // Although the method is called "isOpen", it has always returned true even when the client is in a reconnecting state.
        // This allows users to still queue messages as they will be sent after the reconnection completes.
        return (this.state == IotHubConnectionStatus.CONNECTED || this.state == IotHubConnectionStatus.DISCONNECTED_RETRYING);
    }

    /**
     * Getter for the transport empty queue.
     * @return a boolean true if the transport queue is empty, or false if there is messages to send.
     */
    public boolean isEmpty()
    {
        return this.transport.isEmpty();
    }

    /**
     * Registers a callback with the configured transport to be executed whenever the connection to the device is lost or established.
     *
     * @param callback the callback to be called.
     * @param callbackContext a context to be passed to the callback. Can be
     * {@code null} if no callback is provided.
     */
    public void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext)
    {
        this.transport.registerConnectionStateCallback(callback, callbackContext);
    }

    void registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback statusChangeCallback, Object callbackContext, String deviceId)
    {
        this.transport.registerConnectionStatusChangeCallback(statusChangeCallback, callbackContext, deviceId);
    }

    void registerMultiplexingConnectionStateCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext)
    {
        this.transport.registerMultiplexingConnectionStateCallback(callback, callbackContext);
    }

    /*
     * IotHubTransport layer will notify this layer when the connection is established and when it is lost. This layer should start/stop
     * the send/receive threads accordingly
     */
    @Override
    public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
    {
        synchronized (this.stateLock)
        {
            if (status == IotHubConnectionStatus.DISCONNECTED || status == IotHubConnectionStatus.DISCONNECTED_RETRYING)
            {
                // No need to keep spawning send/receive tasks during reconnection or when the client is closed
                if (this.sendTaskScheduler != null)
                {
                    this.sendTaskScheduler.shutdown();
                }

                if (this.receiveTaskScheduler != null)
                {
                    this.receiveTaskScheduler.shutdown();
                }
            }
            else if (status == IotHubConnectionStatus.CONNECTED)
            {
                // Restart the task scheduler so that send/receive tasks start spawning again
                this.startWorkerThreads();
            }

            this.state = status;
        }
    }
}
