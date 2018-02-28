// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.*;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Queue;

public class MqttIotHubConnection implements MqttConnectionStateListener, IotHubTransportConnection
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


    private IotHubListener listener;

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
    public MqttIotHubConnection(DeviceClientConfig config) throws TransportException
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
                        //Codes_SRS_MQTTIOTHUBCONNECTION_34_020: [If the config has no shared access token, device key, or x509 certificates, this constructor shall throw a TransportException.]
                        throw new TransportException(new IllegalArgumentException("Must have a deviceKey, a shared access token, or x509 certificate saved."));
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
     * @throws TransportException if a connection could not to be established.
     */
    public void open(Queue<DeviceClientConfig> deviceClientConfigs) throws TransportException
    {
        if (deviceClientConfigs.size() > 1)
        {
            throw new UnsupportedOperationException("Mqtt does not support Multiplexing");
        }
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
                        //Codes_SRS_MQTTIOTHUBCONNECTION_34_027: [If this function is called while using websockets and x509 authentication, a TransportException shall be thrown.]
                        throw new TransportException(new UnsupportedOperationException("X509 authentication is not supported over MQTT_WS"));
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
            catch (IOException e)
            {
                this.state = State.CLOSED;
                // Codes_SRS_MQTTIOTHUBCONNECTION_15_005: [If an MQTT connection is unable to be established
                // for any reason, the function shall throw a TransportException.]
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
                throw new TransportException(e);
            }
        }
    }

    /**
     * Closes the connection. After the connection is closed, it is no longer usable.
     * If the connection is already closed, the function shall do nothing.
     */
    public void close() throws TransportException
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
        catch (TransportException e)
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
     * @throws TransportException if the connection state is currently closed.
     */
    public Message receiveMessage() throws TransportException
    {
        // Codes_SRS_MQTTIOTHUBCONNECTION_15_015: [If the MQTT connection is closed,
        // the function shall throw a TransportException.]
        if (this.state == State.CLOSED)
        {
            throw new TransportException(new IllegalStateException("The MQTT connection is currently closed. Call open() before attempting " +
                    "to receive a message."));
        }

        // Codes_SRS_MQTTIOTHUBCONNECTION_15_014: [The function shall attempt to consume a message
        // from various messaging clients.]
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

    public void onConnectionLost(Throwable throwable)
    {
        listener.onConnectionLost(throwable);
    }

    public void onConnectionEstablished()
    {
        //Codes_SRS_MQTTIOTHUBCONNECTION_34_036: [This function shall notify its listeners that connection was established successfully.]
        listener.onConnectionEstablished(null);
    }

    @Override
    public void addListener(IotHubListener listener) throws TransportException
    {
        if (listener == null)
        {
            //Codes_SRS_MQTTIOTHUBCONNECTION_34_049: [If the provided listener object is null, this function shall throw a TransportException.]
            throw new TransportException(new IllegalArgumentException("listener cannot be null"));
        }

        //Codes_SRS_MQTTIOTHUBCONNECTION_34_050: [This function shall save the provided listener object.]
        this.listener = listener;
    }

    /**
     * Sends an event message.
     *
     * @param message the event message.
     *
     * @return the status code from sending the event message.
     *
     * @throws TransportException if the MqttIotHubConnection is not open
     */
    @Override
    public IotHubStatusCode sendMessage(Message message) throws TransportException
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
            // the function shall throw a TransportException.]
            if (this.state == State.CLOSED)
            {
                throw new TransportException(new IllegalStateException("Cannot send event using a closed MQTT connection"));
            }

            // Codes_SRS_MQTTIOTHUBCONNECTION_15_008: [The function shall send an event message
            // to the IoT Hub given in the configuration.]
            // Codes_SRS_MQTTIOTHUBCONNECTION_15_011: [If the message was successfully received by the service,
            // the function shall return status code OK_EMPTY.]
            IotHubStatusCode result = IotHubStatusCode.OK_EMPTY;

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

            return result;
        }
    }

    @Override
    public boolean sendMessageResult(Message message, IotHubMessageResult result) throws TransportException
    {
        return false;
    }
}
