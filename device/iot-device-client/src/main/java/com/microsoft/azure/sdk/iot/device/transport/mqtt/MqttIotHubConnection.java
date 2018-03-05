// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.State;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URLEncoder;

public class MqttIotHubConnection implements MqttConnectionStateListener
{
    /** The MQTT connection lock. */
    private final Object MQTT_CONNECTION_LOCK = new Object();

    private final DeviceClientConfig config;
    private State state = State.CLOSED;

    private String iotHubUserName;
    private String iotHubUserPassword;
    private MqttConnection mqttConnection;

    //string constants
    private static final String WS_SSL_PREFIX = "wss://";
    private static final String WS_SSL_PORT_SUFFIX = ":443";

    private static final String WEBSOCKET_RAW_PATH = "/$iothub/websocket";
    private static final String WEBSOCKET_QUERY = "?iothub-no-client-cert=true";

    private static final String SSL_PREFIX = "ssl://";
    private static final String SSL_PORT_SUFFIX = ":8883";

    private static final String TWIN_API_VERSION = "api-version=2016-11-14";

    //Messaging clients
    private MqttMessaging deviceMessaging;
    private MqttDeviceTwin deviceTwin;
    private MqttDeviceMethod deviceMethod;

    private IotHubConnectionStateCallback stateCallback;
    private Object stateCallbackContext;

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
            if (config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
            {
                if (config.getIotHubConnectionString().getSharedAccessKey() == null || config.getIotHubConnectionString().getSharedAccessKey().isEmpty())
                {
                    if(config.getSasTokenAuthentication().getCurrentSasToken() == null || config.getSasTokenAuthentication().getCurrentSasToken().isEmpty())
                    {
                        //Codes_SRS_MQTTIOTHUBCONNECTION_34_020: [If the config has no shared access token, device key, or x509 certificates, this constructor shall throw an IllegalArgumentException.]
                        throw new IllegalArgumentException("Must have a deviceKey, a shared access token, or x509 certificate saved.");
                    }
                }
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
            try
            {
                SSLContext sslContext = null;
                if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
                {
                    this.iotHubUserPassword = this.config.getSasTokenAuthentication().getRenewedSasToken();
                    sslContext = this.config.getSasTokenAuthentication().getSSLContext();
                }
                else if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.X509_CERTIFICATE)
                {
                    if (this.config.isUseWebsocket())
                    {
                        //Codes_SRS_MQTTIOTHUBCONNECTION_34_027: [If this function is called while using websockets and x509 authentication, an UnsupportedOperation shall be thrown.]
                        throw new UnsupportedOperationException("X509 authentication is not supported over MQTT_WS");
                    }

                    this.iotHubUserPassword = null;
                    sslContext = this.config.getX509Authentication().getSSLContext();
                }

                String clientIdentifier = "DeviceClientType=" + URLEncoder.encode(TransportUtils.JAVA_DEVICE_CLIENT_IDENTIFIER + TransportUtils.CLIENT_VERSION, "UTF-8");
                this.iotHubUserName = this.config.getIotHubHostname() + "/" + this.config.getDeviceId() + "/" + TWIN_API_VERSION + "&" + clientIdentifier;

                if (this.config.isUseWebsocket())
                {
                    //Codes_SRS_MQTTIOTHUBCONNECTION_25_018: [The function shall establish an MQTT WS connection with a server uri as wss://<hostName>/$iothub/websocket?iothub-no-client-cert=true if websocket was enabled.]
                    final String wsServerUri = WS_SSL_PREFIX + this.config.getIotHubHostname() + WEBSOCKET_RAW_PATH + WEBSOCKET_QUERY ;
                    mqttConnection = new MqttConnection(wsServerUri,
                            this.config.getDeviceId(), this.iotHubUserName, this.iotHubUserPassword, sslContext);
                }
                else
                {
                    //Codes_SRS_MQTTIOTHUBCONNECTION_25_019: [The function shall establish an MQTT connection with a server uri as ssl://<hostName>:8883 if websocket was not enabled.]
                    final String serverUri = SSL_PREFIX + this.config.getIotHubHostname() + SSL_PORT_SUFFIX;
                    mqttConnection = new MqttConnection(serverUri,
                            this.config.getDeviceId(), this.iotHubUserName, this.iotHubUserPassword, sslContext);
                }

                //Codes_SRS_MQTTIOTHUBCONNECTION_34_030: [This function shall instantiate this object's MqttMessaging object with this object as the listener.]
                this.deviceMessaging = new MqttMessaging(mqttConnection, this.config.getDeviceId(), this);
                this.mqttConnection.setMqttCallback(this.deviceMessaging);
                this.deviceMethod = new MqttDeviceMethod(mqttConnection);
                this.deviceTwin = new MqttDeviceTwin(mqttConnection);

                // Codes_SRS_MQTTIOTHUBCONNECTION_99_017 : [The function shall set DeviceClientConfig object needed for SAS token renewal.]
                this.deviceMessaging.setDeviceClientConfig(this.config);

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
                throw new IOException(e);
            }
        }
    }

    /**
     * Closes the connection. After the connection is closed, it is no longer usable.
     * If the connection is already closed, the function shall do nothing.
     *
     */
    public void close() throws IOException
    {
        // Codes_SRS_MQTTIOTHUBCONNECTION_15_007: [If the MQTT session is closed, the function shall do nothing.]
        if (this.state == State.CLOSED)
        {
            return;
        }

        // Codes_SRS_MQTTIOTHUBCONNECTION_15_006: [The function shall close the MQTT connection.]
        try
        {
            if (this.deviceMethod != null)
            {
                this.deviceMethod.stop();
                this.deviceMethod = null;
            }

            if (this.deviceTwin != null)
            {
                this.deviceTwin.stop();
                this.deviceTwin = null;
            }

            if (this.deviceMessaging != null)
            {
                this.deviceMessaging.stop();
                this.deviceMessaging = null;
            }

            this.state = State.CLOSED;
        }
        catch (IOException e)
        {
            // Codes_SRS_MQTTIOTHUBCONNECTION_34_037: [If an IOException is encountered while closing the mqtt connection, this function shall set this object's state to CLOSED and rethrow that exception.]
            this.state = State.CLOSED;
            throw e;
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
                    (
                            (message.getMessageType() != MessageType.DEVICE_TWIN
                                    && message.getMessageType() != MessageType.DEVICE_METHODS)
                                    && message.getBytes().length == 0))
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

            if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN && this.config.getSasTokenAuthentication().isRenewalNecessary())
            {
                if (this.stateCallback != null)
                {
                    //Codes_SRS_MQTTIOTHUBCONNECTION_34_036: [If the sas token saved in the config has expired and needs to be renewed and if there is a connection state callback saved, this function shall invoke that callback with Status SAS_TOKEN_EXPIRED.]
                    this.stateCallback.execute(IotHubConnectionState.SAS_TOKEN_EXPIRED, this.stateCallbackContext);
                }

                //Codes_SRS_MQTTIOTHUBCONNECTION_34_035: [If the sas token saved in the config has expired and needs to be renewed, this function shall return UNAUTHORIZED.]
                return IotHubStatusCode.UNAUTHORIZED;
            }

            try
            {
                // Codes_SRS_MQTTIOTHUBCONNECTION_15_009: [The function shall send the message payload.]
                if (message.getMessageType() == MessageType.DEVICE_METHODS)
                {
                    this.deviceMethod.start();
                    this.deviceMethod.send((IotHubTransportMessage) message);
                }
                else if (message.getMessageType() == MessageType.DEVICE_TWIN)
                {
                    this.deviceTwin.start();
                    this.deviceTwin.send((IotHubTransportMessage) message);
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


        // Codes_SRS_MQTTIOTHUBCONNECTION_15_014: [The function shall attempt to consume a message
        // from various messaging clients.]
        // Codes_SRS__MQTTIOTHUBCONNECTION_34_016: [If any of the messaging clients throw an exception, The associated message will be removed from the queue and the exception will be propagated up to the receive task.]
        Message message = this.deviceMethod.receive();
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

    /**
     * Saves the provided callback and callbackContext objects to be used for connection state updates
     * @param callback the callback to fire
     * @param callbackContext the context to include
     * @throws IllegalArgumentException if the provided callback object is null
     */
    void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext)
    {
        if (callback == null)
        {
            //Codes_SRS_MQTTIOTHUBCONNECTION_34_033: [If the provided callback object is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Callback cannot be null");
        }

        //Codes_SRS_MQTTIOTHUBCONNECTION_34_034: [This function shall save the provided callback and callback context.]
        this.stateCallback = callback;
        this.stateCallbackContext = callbackContext;
    }

    public void connectionLost()
    {
        if (this.stateCallback != null)
        {
            //Codes_SRS_MQTTIOTHUBCONNECTION_34_028: [If this object's connection state callback is not null, this function shall fire that callback with the saved context and status CONNECTION_DROP.]
            this.stateCallback.execute(IotHubConnectionState.CONNECTION_DROP, this.stateCallbackContext);
        }
    }

    public void connectionEstablished()
    {
        if (this.stateCallback != null)
        {
            //Codes_SRS_MQTTIOTHUBCONNECTION_34_029: [If this object's connection state callback is not null, this function shall fire that callback with the saved context and status CONNECTION_SUCCESS.]
            this.stateCallback.execute(IotHubConnectionState.CONNECTION_SUCCESS, this.stateCallbackContext);
        }
    }
}
