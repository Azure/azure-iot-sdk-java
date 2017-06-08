// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.transport.IotHubReceiveTask;
import com.microsoft.azure.sdk.iot.device.transport.IotHubSendTask;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransport;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsTransport;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransport;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttTransport;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implement the standard I/O interface with the IoTHub.
 *
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
public final class DeviceIO
{
    /** The state of the IoT Hub client's connection with the IoT Hub. */
    protected enum IotHubClientState
    {
        OPEN, CLOSED
    }

    private long sendPeriodInMilliseconds;
    private long receivePeriodInMilliseconds;

    private CustomLogger logger;
    private IotHubTransport transport;
    private DeviceClientConfig config;
    private IotHubSendTask sendTask = null;
    private IotHubReceiveTask receiveTask = null;
    private IotHubClientProtocol protocol = null;

    private ScheduledExecutorService taskScheduler;
    private IotHubClientState state;

    /**
     * Constructor that takes a connection string as an argument.
     *
     * @param config the connection configuration.
     * @param protocol the communication protocol used (i.e. HTTPS).
     * @param sendPeriodInMilliseconds the period of time that iot hub will try to send messages in milliseconds.
     * @param receivePeriodInMilliseconds the period of time that iot hub will try to receive messages in milliseconds.
     *
     * @throws IllegalArgumentException if any of {@code config} or
     * {@code protocol} are {@code null}.
     */
    DeviceIO(DeviceClientConfig config, IotHubClientProtocol protocol,
                    long sendPeriodInMilliseconds, long receivePeriodInMilliseconds)
    {
        long defaultReceivePeriodInMilliseconds;
        /* Codes_SRS_DEVICE_IO_21_002: [If the `config` is null, the constructor shall throw an IllegalArgumentException.] */
        if(config == null)
        {
            throw new IllegalArgumentException("Config cannot be null.");
        }

        /* Codes_SRS_DEVICE_IO_21_004: [If the `protocol` is null, the constructor shall throw an IllegalArgumentException.] */
        if (protocol == null)
        {
            throw new IllegalArgumentException("Protocol cannot be null.");
        }

        /* Codes_SRS_DEVICE_IO_21_001: [The constructor shall store the provided protocol and config information.] */
        this.config = config;
        this.protocol = protocol;

        /* Codes_SRS_DEVICE_IO_21_003: [The constructor shall initialize the IoT Hub transport that uses the `protocol` specified.] */
        switch (protocol)
        {
            case HTTPS:
                this.transport = new HttpsTransport(this.config);
                break;
            case AMQPS:
                this.transport = new AmqpsTransport(this.config, false);
                break;
            case AMQPS_WS:
                this.transport = new AmqpsTransport(this.config, true);
                break;
            case MQTT:
                this.transport = new MqttTransport(this.config);
                break;
            default:
                /* Codes_SRS_DEVICE_IO_21_005: [If the `protocol` is not valid, the constructor shall throw an IllegalArgumentException.] */
                // should never happen.
                throw new IllegalStateException("Invalid client protocol specified.");
        }

        /* Codes_SRS_DEVICE_IO_21_037: [The constructor shall initialize the `sendPeriodInMilliseconds` with default value of 10 milliseconds.] */
        this.sendPeriodInMilliseconds = sendPeriodInMilliseconds;
        /* Codes_SRS_DEVICE_IO_21_038: [The constructor shall initialize the `receivePeriodInMilliseconds` with default value of each protocol.] */
        this.receivePeriodInMilliseconds = receivePeriodInMilliseconds;

        /* Codes_SRS_DEVICE_IO_21_006: [The constructor shall set the `state` as `CLOSED`.] */
        this.state = IotHubClientState.CLOSED;

        this.logger = new CustomLogger(this.getClass());
        logger.LogInfo("DeviceIO object is created successfully, method name is %s ", logger.getMethodName());
    }

    /**
     * Starts asynchronously sending and receiving messages from an IoT Hub. If
     * the client is already open, the function shall do nothing.
     *
     * @throws IOException if a connection to an IoT Hub is cannot be established.
     */
    public void open() throws IOException
    {
        /* Codes_SRS_DEVICE_IO_21_007: [If the client is already open, the open shall do nothing.] */
        if (this.state == IotHubClientState.OPEN)
        {
            return;
        }

        if (this.config.getPathToCertificate() == null && this.config.getUserCertificateString() == null)
        {
            try
            {
                /* Codes_SRS_DEVICE_IO_21_008: [The open shall create default IotHubSSL context if no certificate input was provided by user and save it by calling setIotHubSSLContext.] */
                IotHubSSLContext iotHubSSLContext = new IotHubSSLContext();
                this.config.setIotHubSSLContext(iotHubSSLContext);
            }
            catch (Exception e)
            {
                /* Codes_SRS_DEVICE_IO_21_011: [If an exception is thrown when creating a SSL context then Open shall throw IOException to the user indicating the failure] */
                throw new IOException(e.getCause());
            }
        }
        else if (this.config.getPathToCertificate() != null)
        {
            try
            {
                /* Codes_SRS_DEVICE_IO_21_009: [The open shall create IotHubSSL context with the certificate path if input was provided by user and save it by calling setIotHubSSLContext.] */
                IotHubSSLContext iotHubSSLContext = new IotHubSSLContext(this.config.getPathToCertificate(), true);
                this.config.setIotHubSSLContext(iotHubSSLContext);
            }
            catch (Exception e)
            {
                /* Codes_SRS_DEVICE_IO_21_011: [If an exception is thrown when creating a SSL context then open shall throw IOException to the user indicating the failure] */
                throw new IOException(e.getCause());
            }
        }
        else
        {
            try
            {
                /* Codes_SRS_DEVICE_IO_21_010: [The open shall create IotHubSSL context with the certificate String if input was provided by user and save it by calling setIotHubSSLContext.] */
                IotHubSSLContext iotHubSSLContext = new IotHubSSLContext(this.config.getUserCertificateString(), false);
                this.config.setIotHubSSLContext(iotHubSSLContext);
            }
            catch (Exception e)
            {
                /* Codes_SRS_DEVICE_IO_21_011: [If an exception is thrown when creating a SSL context then open shall throw IOException to the user indicating the failure] */
                throw new IOException(e.getCause());
            }
        }


        /* Codes_SRS_DEVICE_IO_21_012: [The open shall open the transport to communicate with an IoT Hub.] */
        /* Codes_SRS_DEVICE_IO_21_015: [If an error occurs in opening the transport, the open shall throw an IOException.] */
        this.transport.open();

        this.sendTask = new IotHubSendTask(this.transport);
        this.receiveTask = new IotHubReceiveTask(this.transport);

        this.taskScheduler = Executors.newScheduledThreadPool(2);
        // the scheduler waits until each execution is finished before
        // scheduling the next one, so executions of a given task
        // will never overlap.
        /* Codes_SRS_DEVICE_IO_21_013: [The open shall schedule send tasks to run every SEND_PERIOD_MILLIS milliseconds.] */
        this.taskScheduler.scheduleAtFixedRate(this.sendTask, 0,
                sendPeriodInMilliseconds, TimeUnit.MILLISECONDS);
        /* Codes_SRS_DEVICE_IO_21_014: [The open shall schedule receive tasks to run every receivePeriodInMilliseconds milliseconds.] */
        this.taskScheduler.scheduleAtFixedRate(this.receiveTask, 0,
                receivePeriodInMilliseconds, TimeUnit.MILLISECONDS);

        /* Codes_SRS_DEVICE_IO_21_016: [The open shall set the `state` as `OPEN`.] */
        this.state = IotHubClientState.OPEN;
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
        /* Codes_SRS_DEVICE_IO_21_020: [If the client is already closed, the close shall do nothing.] */
        if (this.state == IotHubClientState.CLOSED)
        {
            return;
        }

        /* Codes_SRS_DEVICE_IO_21_017: [The close shall finish all ongoing tasks.] */
        /* Codes_SRS_DEVICE_IO_21_018: [The close shall cancel all recurring tasks.] */
        this.taskScheduler.shutdown();

        /* Codes_SRS_DEVICE_IO_21_019: [The close shall close the transport.] */
        this.transport.close();

        /* Codes_SRS_DEVICE_IO_21_021: [The close shall set the `state` as `CLOSE`.] */
        this.state = IotHubClientState.CLOSED;
    }

    /**
     * Asynchronously sends an event message to the IoT Hub.
     *
     * @param message the message to be sent.
     * @param callback the callback to be invoked when a response is received.
     * Can be {@code null}.
     * @param callbackContext a context to be passed to the callback. Can be
     * {@code null} if no callback is provided.
     *
     * @throws IllegalArgumentException if the message provided is {@code null}.
     * @throws IllegalStateException if the client has not been opened yet or is already closed.
     */
    public void sendEventAsync(Message message,
                               IotHubEventCallback callback,
                               Object callbackContext)
    {
        /* Codes_SRS_DEVICE_IO_21_024: [If the client is closed, the sendEventAsync shall throw an IllegalStateException.] */
        if (this.state == IotHubClientState.CLOSED)
        {
            throw new IllegalStateException(
                    "Cannot send event from "
                            + "an IoT Hub client that is closed.");
        }

        /* Codes_SRS_DEVICE_IO_21_023: [If the message given is null, the sendEventAsync shall throw an IllegalArgumentException.] */
        if (message == null)
        {
            throw new IllegalArgumentException("Cannot send message 'null'.");
        }

        logger.LogInfo("Message with messageid %s along with callback and callbackcontext is added to the queue, method name is %s ", message.getMessageId(), logger.getMethodName());
        /* Codes_SRS_DEVICE_IO_21_022: [The sendEventAsync shall add the message, with its associated callback and callback context, to the transport.] */
        transport.addMessage(message, callback, callbackContext);
    }

    /**
     * Asynchronously sends an event message to the IoT Hub. Use IotHubResponseCallback if you
     * need the message payload received as a response for a sent message, together with the
     * status.
     *
     * @param message the message to be sent.
     * @param callback the callback to be invoked when a response is received.
     * Can be {@code null}.
     * @param callbackContext a context to be passed to the callback. Can be
     * {@code null} if no callback is provided.
     *
     * @throws IllegalArgumentException if the message provided is {@code null}.
     * @throws IllegalStateException if the client has not been opened yet or is already closed.
     */
    public void sendEventAsync(Message message,
                               IotHubResponseCallback callback,
                               Object callbackContext)
    {
        /* Codes_SRS_DEVICE_IO_21_042: [If the client is closed, the sendEventAsync shall throw an IllegalStateException.] */
        if (this.state == IotHubClientState.CLOSED)
        {
            throw new IllegalStateException(
                    "Cannot send event from "
                            + "an IoT Hub client that is closed.");
        }

        /* Codes_SRS_DEVICE_IO_21_041: [If the message given is null, the sendEventAsync shall throw an IllegalArgumentException.] */
        if (message == null)
        {
            throw new IllegalArgumentException("Cannot send message 'null'.");
        }

        logger.LogInfo("Message with messageid %s along with callback and callbackContext is added to the queue, method name is %s ", message.getMessageId(), logger.getMethodName());
        /* Codes_SRS_DEVICE_IO_21_040: [The sendEventAsync shall add the message, with its associated callback and callback context, to the transport.] */
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
        return (this.state == IotHubClientState.OPEN);
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

}
