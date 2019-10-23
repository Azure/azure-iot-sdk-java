// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.exceptions.DeviceClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubReceiveTask;
import com.microsoft.azure.sdk.iot.device.transport.IotHubSendTask;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransport;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
public final class DeviceIO  implements TransportConnectionListener
{
    /** The state of the IoT Hub client's connection with the IoT Hub. */
    protected enum IotHubClientState
    {
        OPEN, CLOSED
    }

    private long sendPeriodInMilliseconds;
    private long receivePeriodInMilliseconds;

    private final IotHubTransport transport;
    private IotHubSendTask sendTask = null;
    private IotHubReceiveTask receiveTask = null;
    private final IotHubClientProtocol protocol;

    private ScheduledExecutorService taskScheduler;

    private List<DeviceClientConfig> deviceClientConfigs = new ArrayList<>();

    private static final int SEND_RECEIVE_THREAD_POOL_SIZE = 2;

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
        /* Codes_SRS_DEVICE_IO_21_002: [If the `config` is null, the constructor shall throw an IllegalArgumentException.] */
        if (config == null)
        {
            throw new IllegalArgumentException("Config cannot be null.");
        }

        this.protocol = config.getProtocol();

        if (this.protocol == IotHubClientProtocol.AMQPS_WS || this.protocol == IotHubClientProtocol.MQTT_WS)
        {
            config.setUseWebsocket(true);
        }

        /* Codes_SRS_DEVICE_IO_21_001: [The constructor shall store the provided protocol and config information.] */
        this.deviceClientConfigs.add(config);

        this.transport = new IotHubTransport(config, this);

        /* Codes_SRS_DEVICE_IO_21_037: [The constructor shall initialize the `sendPeriodInMilliseconds` with default value of 10 milliseconds.] */
        this.sendPeriodInMilliseconds = sendPeriodInMilliseconds;
        /* Codes_SRS_DEVICE_IO_21_038: [The constructor shall initialize the `receivePeriodInMilliseconds` with default value of each protocol.] */
        this.receivePeriodInMilliseconds = receivePeriodInMilliseconds;
    }

    /**
     * Starts asynchronously sending and receiving messages from an IoT Hub. If
     * the client is already open, the function shall do nothing.
     *
     * @throws IOException if a connection to an IoT Hub cannot be established.
     */
    void open() throws IOException
    {
        try
        {
            this.transport.open(deviceClientConfigs);
        }
        catch (DeviceClientException e)
        {
            throw new IOException("Could not open the connection", e);
        }
    }

    /**
     * Adds a device client config to the saved list. Each device client config will be used in multiplexing
     * @param config the config tied to the device client to multiplex with
     */
    void addClient(DeviceClientConfig config)
    {
        if (config == null)
        {
            throw new IllegalArgumentException("Config cannot be null");
        }

        // add client to transport
        deviceClientConfigs.add(config);
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
        try
        {
            this.transport.close(IotHubConnectionStatusChangeReason.CLIENT_CLOSE, null);
        }
        catch (DeviceClientException e)
        {
            throw new IOException(e);
        }
    }

    /**
     * Completes all current outstanding requests and closes the IoT Hub client.
     * Must be called to terminate the background thread that is sending data to
     * IoT Hub. After {@code close()} is called, the IoT Hub client is no longer
     *  usable. If the client is already closed, the function shall do nothing.
     *
     * @throws IOException if the connection to an IoT Hub cannot be closed.
     */
    public void multiplexClose() throws IOException
    {
        // Codes_SRS_DEVICE_IO_12_009: [THe function shall call close().]
        close();
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
        /* Codes_SRS_DEVICE_IO_21_023: [If the message given is null, the sendEventAsync shall throw an IllegalArgumentException.] */
        if (message == null)
        {
            throw new IllegalArgumentException("Cannot send message 'null'.");
        }

        // Codes_SRS_DEVICE_IO_12_001: [The function shall set the deviceId on the message if the deviceId parameter is not null.]
        if (deviceId != null)
        {
            message.setConnectionDeviceId(deviceId);
        }

        /* Codes_SRS_DEVICE_IO_21_022: [The sendEventAsync shall add the message, with its associated callback and callback context, to the transport.] */
        transport.addMessage(message, callback, callbackContext);
    }

    /**
     * Getter for the receive period in milliseconds.
     *
     * @return a long with the number of milliseconds between receives.
     */
    public long getReceivePeriodInMilliseconds()
    {
        /* Codes_SRS_DEVICE_IO_21_026: [The getReceivePeriodInMilliseconds shall return the programed receive period in milliseconds.] */
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
        /* Codes_SRS_DEVICE_IO_21_030: [If the the provided interval is zero or negative, the setReceivePeriodInMilliseconds shall throw IllegalArgumentException.] */
        if(newIntervalInMilliseconds <= 0L)
        {
            throw new IllegalArgumentException("receive interval can not be zero or negative");
        }

        /* Codes_SRS_DEVICE_IO_21_027: [The setReceivePeriodInMilliseconds shall store the new receive period in milliseconds.] */
        this.receivePeriodInMilliseconds = newIntervalInMilliseconds;

        /* Codes_SRS_DEVICE_IO_21_028: [If the task scheduler already exists, the setReceivePeriodInMilliseconds shall change the `scheduleAtFixedRate` for the receiveTask to the new value.] */
        if(this.taskScheduler != null)
        {
            /* Codes_SRS_DEVICE_IO_21_029: [If the `receiveTask` is null, the setReceivePeriodInMilliseconds shall throw IOException.] */
            if(this.receiveTask == null)
            {
                throw new IOException("transport receive task not set");
            }

            this.taskScheduler.scheduleAtFixedRate(this.receiveTask, 0,
                    this.receivePeriodInMilliseconds, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Getter for the send period in milliseconds.
     *
     * @return a long with the number of milliseconds between sends.
     */
    public long getSendPeriodInMilliseconds()
    {
        /* Codes_SRS_DEVICE_IO_21_032: [The getSendPeriodInMilliseconds shall return the programed send period in milliseconds.] */
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
        /* Codes_SRS_DEVICE_IO_21_036: [If the the provided interval is zero or negative, the setSendPeriodInMilliseconds shall throw IllegalArgumentException.] */
        if(newIntervalInMilliseconds <= 0L)
        {
            throw new IllegalArgumentException("send interval can not be zero or negative");
        }

        /* Codes_SRS_DEVICE_IO_21_033: [The setSendPeriodInMilliseconds shall store the new send period in milliseconds.] */
        this.sendPeriodInMilliseconds = newIntervalInMilliseconds;

        /* Codes_SRS_DEVICE_IO_21_034: [If the task scheduler already exists, the setSendPeriodInMilliseconds shall change the `scheduleAtFixedRate` for the sendTask to the new value.] */
        if(this.taskScheduler != null)
        {
            /* Codes_SRS_DEVICE_IO_21_035: [If the `sendTask` is null, the setSendPeriodInMilliseconds shall throw IOException.] */
            if(this.sendTask == null)
            {
                throw new IOException("transport send task not set");
            }

            this.taskScheduler.scheduleAtFixedRate(this.sendTask, 0,
                    this.sendPeriodInMilliseconds, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Getter for the transport protocol.
     *
     * @return a protocol for transport.
     */
    public IotHubClientProtocol getProtocol()
    {
        /* Codes_SRS_DEVICE_IO_21_025: [The getProtocol shall return the protocol for transport.] */
        return this.protocol;
    }

    /**
     * Getter for the connection state.
     *
     * @return a boolean true if the connection is open, or false if it is closed.
     */
    public boolean isOpen()
    {
        /* Codes_SRS_DEVICE_IO_21_031: [The isOpen shall return the connection state, true if connection is open, false if it is closed.] */
        return (this.transport.isConnected());
    }

    /**
     * Getter for the transport empty queue.
     * @return a boolean true if the transport queue is empty, or false if there is messages to send.
     */
    public boolean isEmpty()
    {
        /* Codes_SRS_DEVICE_IO_21_039: [The isEmpty shall return the transport queue state, true if the queue is empty, false if there is pending messages in the queue.] */
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
        /* Codes_SRS_DEVICE_IO_99_001: [The registerConnectionStateCallback shall register the callback with the transport.]*/
        this.transport.registerConnectionStateCallback(callback, callbackContext);
    }

    public void registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback statusChangeCallback, Object callbackContext)
    {
        //Codes_SRS_DEVICE_IO_34_020: [This function shall register the callback with the transport.]
        this.transport.registerConnectionStatusChangeCallback(statusChangeCallback, callbackContext);
    }

    @Override
    public void onTransportConnectionClosed()
    {
        if (taskScheduler != null)
        {
            this.taskScheduler.shutdown();
            this.taskScheduler = null;
        }
    }

    @Override
    public void onTransportConnectionOpened()
    {
        if (this.sendTask == null)
        {
            this.sendTask = new IotHubSendTask(this.transport);
        }

        if (this.receiveTask == null)
        {
            this.receiveTask = new IotHubReceiveTask(this.transport);
        }

        if (this.taskScheduler == null)
        {
            this.taskScheduler = Executors.newScheduledThreadPool(SEND_RECEIVE_THREAD_POOL_SIZE);

            // the scheduler waits until each execution is finished before
            // scheduling the next one, so executions of a given task
            // will never overlap.
            /* Codes_SRS_DEVICE_IO_21_013: [The open shall schedule send tasks to run every SEND_PERIOD_MILLIS milliseconds.] */
            this.taskScheduler.scheduleAtFixedRate(this.sendTask, 0,
                    sendPeriodInMilliseconds, TimeUnit.MILLISECONDS);
            /* Codes_SRS_DEVICE_IO_21_014: [The open shall schedule receive tasks to run every receivePeriodInMilliseconds milliseconds.] */
            this.taskScheduler.scheduleAtFixedRate(this.receiveTask, 0,
                    receivePeriodInMilliseconds, TimeUnit.MILLISECONDS);
        }
    }
}
