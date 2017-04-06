// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodMessage;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceTwinMessage;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.transport.State;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;

import java.io.IOException;
import java.net.URLEncoder;


public class MqttIotHubConnection
{
    /** The MQTT connection lock. */
    protected final Object MQTT_CONNECTION_LOCK = new Object();

    protected final DeviceClientConfig config;
    protected State state = State.CLOSED;

    private String iotHubUserName;
    private String iotHubUserPassword;

    //string constants
    private static String sslPrefix = "ssl://";
    private static String sslPortSuffix = ":8883";
    private static String TWIN_API_VERSION = "api-version=2016-11-14";

    //Messaging clients
    private MqttMessaging deviceMessaging;
    private MqttDeviceTwin deviceTwin;
    private MqttDeviceMethod deviceMethod;

    /**
     * Constructs an instance from the given {@link DeviceClientConfig}
     * object.
     *
     * @param config the client configuration.
     */
    public MqttIotHubConnection(DeviceClientConfig config) throws IllegalArgumentException
    {
        synchronized (MQTT_CONNECTION_LOCK)
        {
            // Codes_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
            // if any of the parameters of the configuration is null or empty.]
            if (config == null)
            {
                throw new IllegalArgumentException("The DeviceClientConfig cannot be null.");
            }
            if (config.getIotHubHostname() == null || config.getIotHubHostname().length() == 0)
            {
                throw new IllegalArgumentException("hostName cannot be null or empty.");
            }
            if (config.getDeviceId() == null || config.getDeviceId().length() == 0)
            {
                throw new IllegalArgumentException("deviceID cannot be null or empty.");
            }
            if (config.getIotHubName() == null || config.getIotHubName().length() == 0)
            {
                throw new IllegalArgumentException("hubName cannot be null or empty.");
            }
            if (config.getDeviceKey() == null || config.getDeviceKey().length() == 0)
            {
                if(config.getSharedAccessToken() == null || config.getSharedAccessToken().length() == 0)

                    throw new IllegalArgumentException("Both deviceKey and shared access signature cannot be null or empty.");
            }

            // Codes_SRS_MQTTIOTHUBCONNECTION_15_001: [The constructor shall save the configuration.]
            this.config = config;
            this.deviceMessaging = null;
            this.deviceMethod = null;
            this.deviceTwin = null;
        }
    }

    /**
     * Establishes a connection for the device and IoT Hub given in the client
     * configuration. If the connection is already open, the function shall do
     * nothing.
     *
     * @throws IOException if a connection could not to be established.
     */
    public void open() throws IOException
    {
        synchronized (MQTT_CONNECTION_LOCK)
        {
            //Codes_SRS_MQTTIOTHUBCONNECTION_15_006: [If the MQTT connection is already open,
            // the function shall do nothing.]
            if (this.state == State.OPEN)
            {
                return;
            }

            // Codes_SRS_MQTTIOTHUBCONNECTION_15_004: [The function shall establish an MQTT connection
            // with an IoT Hub using the provided host name, user name, device ID, and sas token.]
            try {
                IotHubSasToken sasToken = new IotHubSasToken(this.config, System.currentTimeMillis() / 1000l +
                        this.config.getTokenValidSecs() + 1l);
                this.iotHubUserPassword = sasToken.toString();

                String clientIdentifier = "DeviceClientType=" + URLEncoder.encode(TransportUtils.javaDeviceClientIdentifier + TransportUtils.clientVersion, "UTF-8");
                this.iotHubUserName = this.config.getIotHubHostname() + "/" + this.config.getDeviceId() + "/" + TWIN_API_VERSION + "/" + clientIdentifier;


                this.deviceMessaging = new MqttMessaging(sslPrefix + this.config.getIotHubHostname() + sslPortSuffix,
                        this.config.getDeviceId(), this.iotHubUserName, this.iotHubUserPassword, this.config.getIotHubSSLContext());
                this.deviceMethod = new MqttDeviceMethod();
                this.deviceTwin = new MqttDeviceTwin();

                this.deviceMessaging.start();
                this.state = State.OPEN;
            }
            catch (Exception e)
            {
                this.state = State.CLOSED;
                // Codes_SRS_MQTTIOTHUBCONNECTION_15_005: [If an MQTT connection is unable to be established
                // for any reason, the function shall throw an IOException.]
                if (this.deviceMethod != null)
                {
                    this.deviceMethod.stop();
                }
                if (this.deviceTwin != null )
                {
                    this.deviceTwin.stop();
                }
                if (this.deviceMessaging != null)
                {
                    this.deviceMessaging.stop();
                }
                throw new IOException(e.getMessage(), e.getCause());
            }

        }
    }

    /**
     * Closes the connection. After the connection is closed, it is no longer usable.
     * If the connection is already closed, the function shall do nothing.
     *
     */
    public void close()
    {
        synchronized (MQTT_CONNECTION_LOCK)
        {
            // Codes_SRS_MQTTIOTHUBCONNECTION_15_007: [If the MQTT session is closed, the function shall do nothing.]
            if (this.state == State.CLOSED)
            {
                return;
            }

            // Codes_SRS_MQTTIOTHUBCONNECTION_15_006: [**The function shall close the MQTT connection.]

            try
            {
                this.deviceMethod.stop();
                this.deviceMethod = null;

                this.deviceTwin.stop();
                this.deviceTwin = null;

                this.deviceMessaging.stop();
                this.deviceMessaging = null;

                this.state = State.CLOSED;
            }
            catch (Exception e)
            {
                this.state = State.CLOSED;
            }

        }
    }

    /**
     * Sends an event message.
     *
     * @param message the event message.
     *
     * @return the status code from sending the event message.
     *
     * @throws IllegalStateException if the MqttIotHubConnection is not open
     */
    public IotHubStatusCode sendEvent(Message message) throws IllegalStateException
    {
        synchronized (MQTT_CONNECTION_LOCK)
        {
            // Codes_SRS_MQTTIOTHUBCONNECTION_15_010: [If the message is null or empty,
            // the function shall return status code BAD_FORMAT.]
            if (message == null || message.getBytes() == null ||
                    ((message.getMessageType() != MessageType.DeviceTwin && message.getMessageType() != MessageType.DeviceMethods) && message.getBytes().length == 0))
            {
                return IotHubStatusCode.BAD_FORMAT;
            }

            // Codes_SRS_MQTTIOTHUBCONNECTION_15_013: [If the MQTT connection is closed,
            // the function shall throw an IllegalStateException.]
            if (this.state == State.CLOSED)
            {
                throw new IllegalStateException("Cannot send event using a closed MQTT connection");
            }

            // Codes_SRS_MQTTIOTHUBCONNECTION_15_008: [The function shall send an event message
            // to the IoT Hub given in the configuration.]
            // Codes_SRS_MQTTIOTHUBCONNECTION_15_011: [If the message was successfully received by the service,
            // the function shall return status code OK_EMPTY.]
            IotHubStatusCode result = IotHubStatusCode.OK_EMPTY;

            try
            {
                // Codes_SRS_MQTTIOTHUBCONNECTION_15_009: [The function shall send the message payload.]
                if (message.getMessageType() == MessageType.DeviceMethods)
                {
                    this.deviceMethod.start();
                    this.deviceMethod.send((DeviceMethodMessage) message);
                }
                else if (message.getMessageType() == MessageType.DeviceTwin)
                {
                    this.deviceTwin.start();
                    this.deviceTwin.send((DeviceTwinMessage) message);
                }
                else
                {
                    this.deviceMessaging.send(message);
                }
            }
            // Codes_SRS_MQTTIOTHUBCONNECTION_15_012: [If the message was not successfully
            // received by the service, the function shall return status code ERROR.]
            catch (Exception e)
            {
                result = IotHubStatusCode.ERROR;
            }

            return result;
        }
    }

    /**
     * Receives a message, if one exists.
     *
     * @return the message received, or null if none exists.
     *
     * @throws IllegalStateException if the connection state is currently closed.
     * @throws IOException if receiving on any of messaging clients fail.
     */
    public Message receiveMessage() throws IllegalStateException, IOException
    {
        // Codes_SRS_MQTTIOTHUBCONNECTION_15_015: [If the MQTT connection is closed,
        // the function shall throw an IllegalStateException.]
        if (this.state == State.CLOSED)
        {
            throw new IllegalStateException("The MQTT connection is currently closed. Call open() before attempting " +
                    "to receive a message.");
        }

        Message message = null;

        // Codes_SRS_MQTTIOTHUBCONNECTION_15_014: [The function shall attempt to consume a message
        // from various messaging clients.]

        /*
        **Codes_SRS_MQTTIOTHUBCONNECTION_25_016: [**If any of the messaging clients fail to receive, the function shall throw an IOException.**]**
         */
        message = this.deviceMethod.receive();
        if (message == null)
        {
            message = deviceTwin.receive();
        }
        if (message == null)
        {
            message = deviceMessaging.receive();
        }

        return message;
    }

}
