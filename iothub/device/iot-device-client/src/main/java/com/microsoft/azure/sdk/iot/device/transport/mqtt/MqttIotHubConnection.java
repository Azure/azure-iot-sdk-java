// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.*;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_METHODS;
import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_TWIN;
import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1_1;

@Slf4j
public class MqttIotHubConnection implements IotHubTransportConnection
{
    //string constants
    private static final String WS_SSL_PREFIX = "wss://";
    private static final String WEBSOCKET_RAW_PATH = "/$iothub/websocket";
    private static final String NO_CLIENT_CERT_QUERY_STRING = "?iothub-no-client-cert=true";
    private static final String SSL_PREFIX = "ssl://";
    private static final String SSL_PORT_SUFFIX = ":8883";
    private static final int MQTT_VERSION = MQTT_VERSION_3_1_1;
    private static final String MODEL_ID = "model-id";

    private String connectionId;
    private String webSocketQueryString;
    private final Object mqttConnectionStateLock = new Object(); // lock for preventing simultaneous open and close calls
    private final ClientConfiguration config;
    private IotHubConnectionStatus state = IotHubConnectionStatus.DISCONNECTED;
    private final String clientId;
    private final MqttConnectOptions.MqttConnectOptionsBuilder connectOptions;
    private final Map<IotHubTransportMessage, Integer> receivedMessagesToAcknowledge = new ConcurrentHashMap<>();

    private IMqttClient mqttClient;
    private IotHubListener listener;

    /**
     * Constructs an instance from the given {@link ClientConfiguration}
     * object.
     *
     * @param config the client configuration.
     * @throws TransportException if the mqtt connection configuration cannot be constructed.
     */
    // The warning is for how getSasTokenAuthentication() may return null, but the check that our config uses SAS_TOKEN
    // auth is sufficient at confirming that getSasTokenAuthentication() will return a non-null instance
    public MqttIotHubConnection(ClientConfiguration config) throws TransportException
    {
        if (config == null)
        {
            throw new IllegalArgumentException("The ClientConfiguration cannot be null.");
        }
        if (config.getIotHubHostname() == null || config.getIotHubHostname().length() == 0)
        {
            throw new IllegalArgumentException("hostName cannot be null or empty.");
        }
        if (config.getDeviceId() == null || config.getDeviceId().length() == 0)
        {
            throw new IllegalArgumentException("deviceID cannot be null or empty.");
        }

        this.config = config;

        SSLContext sslContext;
        try
        {
            sslContext = this.config.getAuthenticationProvider().getSSLContext();
        }
        catch (IOException e)
        {
            throw new TransportException("Failed to get SSLContext", e);
        }

        if (this.config.getAuthenticationType() == ClientConfiguration.AuthType.SAS_TOKEN)
        {
            log.trace("MQTT connection will use sas token based auth");
            this.webSocketQueryString = NO_CLIENT_CERT_QUERY_STRING;
        }
        else if (this.config.getAuthenticationType() == ClientConfiguration.AuthType.X509_CERTIFICATE)
        {
            log.trace("MQTT connection will use X509 certificate based auth");
        }

        // URLEncoder follows HTML spec for encoding urls, which includes substituting space characters with '+'
        // We want "%20" for spaces, not '+', however, so replace them manually after utf-8 encoding.
        String userAgentString = this.config.getProductInfo().getUserAgentString();
        String clientUserAgentIdentifier;
        try
        {
            clientUserAgentIdentifier = "DeviceClientType=" + URLEncoder.encode(userAgentString, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new TransportException("Failed to URLEncode the user agent string", e);
        }

        String deviceId = this.config.getDeviceId();
        String moduleId = this.config.getModuleId();
        if (moduleId != null && !moduleId.isEmpty())
        {
            this.clientId = deviceId + "/" + moduleId;
        }
        else
        {
            this.clientId = deviceId;
        }

        String serviceParams;
        String modelId = this.config.getModelId();
        if (modelId == null || modelId.isEmpty())
        {
            serviceParams = TransportUtils.IOTHUB_API_VERSION;
        }
        else
        {
            try
            {
                // URLEncoder follows HTML spec for encoding urls, which includes substituting space characters with '+'
                // We want "%20" for spaces, not '+', however, so replace them manually after utf-8 encoding.
                serviceParams = TransportUtils.IOTHUB_API_VERSION + "&" + MODEL_ID + "=" + URLEncoder.encode(modelId, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            }
            catch (UnsupportedEncodingException e)
            {
                throw new TransportException("Failed to URLEncode the modelId string", e);
            }
        }

        String iotHubUserName = this.config.getIotHubHostname() + "/" + clientId + "/?api-version=" + serviceParams + "&" + clientUserAgentIdentifier;

        String host = this.config.getGatewayHostname();
        if (host == null || host.isEmpty())
        {
            host = this.config.getIotHubHostname();
        }

        String serverUri;
        if (this.config.isUsingWebsocket())
        {
            if (this.webSocketQueryString == null)
            {
                serverUri = WS_SSL_PREFIX + host + WEBSOCKET_RAW_PATH;
            }
            else
            {
                serverUri = WS_SSL_PREFIX + host + WEBSOCKET_RAW_PATH + this.webSocketQueryString;
            }
        }
        else
        {
            serverUri = SSL_PREFIX + host + SSL_PORT_SUFFIX;
        }

        this.connectOptions = MqttConnectOptions.builder()
                .serverUri(serverUri)
                .keepAlivePeriod(config.getKeepAliveInterval())
                .mqttVersion(MQTT_VERSION)
                .username(iotHubUserName);

        /*ProxySettings proxySettings = config.getProxySettings();
        if (proxySettings != null)
        {
            if (proxySettings.getProxy().type() == Proxy.Type.SOCKS)
            {
                try
                {
                    connectOptions.setSocketFactory(new Socks5SocketFactory(proxySettings.getHostname(), proxySettings.getPort()));
                }
                catch (UnknownHostException e)
                {
                    throw new TransportException("Failed to build the Socks5SocketFactory", e);
                }
            }
            else if (proxySettings.getProxy().type() == Proxy.Type.HTTP)
            {
                connectOptions.setSocketFactory(new HttpProxySocketFactory(sslContext.getSocketFactory(), proxySettings));
            }
            else
            {
                throw new IllegalArgumentException("Proxy settings must be configured to use either SOCKS or HTTP");
            }
        }
        else
        {
            connectOptions.setSocketFactory(sslContext.getSocketFactory());
        }*/ //TODO proxy support
    }

    /**
     * Establishes a connection for the device and IoT Hub given in the client
     * configuration. If the connection is already open, the function shall do
     * nothing.
     *
     * @throws TransportException if a connection could not to be established.
     */
    public void open() throws TransportException
    {
        synchronized (this.mqttConnectionStateLock)
        {
            this.connectionId = UUID.randomUUID().toString();

            if (this.state == IotHubConnectionStatus.CONNECTED)
            {
                return;
            }

            log.debug("Opening MQTT connection...");

            if (this.config.getSasTokenAuthentication() != null)
            {
                try
                {
                    log.trace("Setting password for MQTT connection since it is a SAS token authenticated connection");
                    this.connectOptions.password(this.config.getSasTokenAuthentication().getSasToken());
                }
                catch (IOException e)
                {
                    throw new TransportException("Failed to open the MQTT connection because a SAS token could not be retrieved", e);
                }
            }

            this.connectOptions.clientId(this.clientId);

            MqttConnectOptions options = this.connectOptions.build();
            this.mqttClient = new PahoMqttClient(); //TODO get from config

            this.mqttClient.connect(options);

            this.state = IotHubConnectionStatus.CONNECTED;

            log.debug("MQTT connection opened successfully");

            this.listener.onConnectionEstablished(this.connectionId);
        }
    }

    @Override
    public void setListener(IotHubListener listener) throws IllegalArgumentException
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("listener cannot be null");
        }

        this.listener = listener;
    }

    /**
     * Closes the connection. After the connection is closed, it is no longer usable.
     * If the connection is already closed, the function shall do nothing.
     */
    public void close()
    {
        synchronized (this.mqttConnectionStateLock)
        {
            if (this.state == IotHubConnectionStatus.DISCONNECTED)
            {
                return;
            }

            log.debug("Closing MQTT connection");

            this.mqttClient.disconnect();

            this.state = IotHubConnectionStatus.DISCONNECTED;
            log.debug("Successfully closed MQTT connection");
        }
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
        if (message == null || message.getBytes() == null ||
            ((message.getMessageType() != DEVICE_TWIN
                    && message.getMessageType() != DEVICE_METHODS)
                    && message.getBytes().length == 0))
        {
            return IotHubStatusCode.BAD_FORMAT;
        }

        if (this.state == IotHubConnectionStatus.DISCONNECTED)
        {
            throw new IllegalStateException("Cannot send event using a closed MQTT connection");
        }

        IotHubStatusCode result = IotHubStatusCode.OK;

        if (message.getMessageType() == DEVICE_METHODS)
        {
        }
        else if (message.getMessageType() == DEVICE_TWIN)
        {
        }
        else
        {
        }

        return result;
    }

    /**
     * Sends an ACK to the service for the provided message
     * @param message the message to acknowledge to the service
     * @param result Ignored. The only ack that can be sent in MQTT is COMPLETE
     * @return true if the ACK was sent successfully and false otherwise
     * @throws TransportException if the ACK could not be sent successfully
     */
    @Override
    public boolean sendMessageResult(IotHubTransportMessage message, IotHubMessageResult result) throws TransportException
    {
        if (message == null || result == null)
        {
            throw new TransportException(new IllegalArgumentException("message and result must be non-null"));
        }

        if (message.getQualityOfService() == 0)
        {
            // messages that the service sent with QoS 0 don't need to be acknowledged.
            return true;
        }

        int messageId;
        log.trace("Checking if MQTT layer can acknowledge the received message ({})", message);
        if (receivedMessagesToAcknowledge.containsKey(message))
        {
            messageId = receivedMessagesToAcknowledge.get(message);
        }
        else
        {
            TransportException e = new TransportException(new IllegalArgumentException("Provided message cannot be acknowledged because it was already acknowledged or was never received from service"));
            log.error("Mqtt layer could not acknowledge received message because it has no mapping to an outstanding mqtt message id ({})", message, e);
            throw e;
        }

        log.trace("MQTT ACK was sent for a received message so it has been removed from the messages to acknowledge list ({})", message);
        this.receivedMessagesToAcknowledge.remove(message);

        return true;
    }

    @Override
    public String getConnectionId()
    {
        return this.connectionId;
    }
}
