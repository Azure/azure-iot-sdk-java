// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.*;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions.PahoExceptionTranslator;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_METHODS;
import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_TWIN;

@Slf4j
public class MqttIotHubConnection implements IotHubTransportConnection, MqttMessageListener
{
    //string constants
    private static final String WS_SSL_PREFIX = "wss://";

    private static final String WEBSOCKET_RAW_PATH = "/$iothub/websocket";
    private static final String NO_CLIENT_CERT_QUERY_STRING = "?iothub-no-client-cert=true";

    private static final String SSL_PREFIX = "ssl://";
    private static final String SSL_PORT_SUFFIX = ":8883";

    private static final int KEEP_ALIVE_INTERVAL = 230;
    private static final int MQTT_VERSION = 4;
    private static final boolean SET_CLEAN_SESSION = false;

    private static final String MODEL_ID = "model-id";

    private String connectionId;
    private String webSocketQueryString;
    private final Object mqttConnectionStateLock = new Object(); // lock for preventing simultaneous open and close calls
    private final DeviceClientConfig config;
    private IotHubConnectionStatus state = IotHubConnectionStatus.DISCONNECTED;
    private IotHubListener listener;
    private final MqttConnectOptions connectOptions;
    private final String clientId;
    private final String serverUri;

    //Messaging clients, never null
    private final MqttMessaging deviceMessaging;
    private final MqttDeviceTwin deviceTwin;
    private final MqttDeviceMethod deviceMethod;

    private final Map<IotHubTransportMessage, Integer> receivedMessagesToAcknowledge = new ConcurrentHashMap<>();

    /**
     * Constructs an instance from the given {@link DeviceClientConfig}
     * object.
     *
     * @param config the client configuration.
     */
    // The warning is for how getSasTokenAuthentication() may return null, but the check that our config uses SAS_TOKEN
    // auth is sufficient at confirming that getSasTokenAuthentication() will return a non-null instance
    @SuppressWarnings("ConstantConditions")
    public MqttIotHubConnection(DeviceClientConfig config) throws IllegalArgumentException, TransportException
    {
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

        char[] password = null;
        if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            log.trace("MQTT connection will use sas token based auth");
            try
            {
                password = this.config.getSasTokenAuthentication().getSasToken();
            }
            catch (IOException e)
            {
                throw new TransportException("Failed to get sas token", e);
            }

            this.webSocketQueryString = NO_CLIENT_CERT_QUERY_STRING;
        }
        else if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.X509_CERTIFICATE)
        {
            log.trace("MQTT connection will use X509 certificate based auth");
            password = null;
        }

        //URLEncoder follows HTML spec for encoding urls, which includes substituting space characters with '+'
        // We want "%20" for spaces, not '+', however, so replace them manually after utf-8 encoding
        String userAgentString = this.config.getProductInfo().getUserAgentString();
        String clientUserAgentIdentifier;
        try
        {
            clientUserAgentIdentifier = "DeviceClientType=" + URLEncoder.encode(userAgentString, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new TransportException("Failed to get URLEncode the user agent string", e);
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
        if(modelId == null || modelId.isEmpty())
        {
            serviceParams = TransportUtils.IOTHUB_API_VERSION;
        }
        else
        {
            serviceParams = TransportUtils.IOTHUB_API_VERSION + "&" + MODEL_ID + "=" + modelId;
        }

        String iotHubUserName = this.config.getIotHubHostname() + "/" + clientId + "/?api-version=" + serviceParams + "&" + clientUserAgentIdentifier;

        String host = this.config.getGatewayHostname();
        if (host == null || host.isEmpty())
        {
            host = this.config.getIotHubHostname();
        }

        if (this.config.isUseWebsocket())
        {
            if (this.webSocketQueryString == null)
            {
                this.serverUri = WS_SSL_PREFIX + host + WEBSOCKET_RAW_PATH;
            }
            else
            {
                this.serverUri = WS_SSL_PREFIX + host + WEBSOCKET_RAW_PATH + this.webSocketQueryString;
            }
        }
        else
        {
            this.serverUri = SSL_PREFIX + host + SSL_PORT_SUFFIX;
        }

        MqttAsyncClient mqttAsyncClient = buildMqttAsyncClient(this.serverUri, clientId);
        this.connectOptions = new MqttConnectOptions();
        this.connectOptions.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
        this.connectOptions.setCleanSession(SET_CLEAN_SESSION);
        this.connectOptions.setMqttVersion(MQTT_VERSION);
        this.connectOptions.setUserName(iotHubUserName);
        ProxySettings proxySettings = config.getProxySettings();
        if (proxySettings != null)
        {
            if (proxySettings.getProxy().type() == Proxy.Type.SOCKS)
            {
                try
                {
                    this.connectOptions.setSocketFactory(new Socks5SocketFactory(proxySettings.getHostname(), proxySettings.getPort()));
                }
                catch (UnknownHostException e)
                {
                    throw new TransportException("Failed to build the Socks5SocketFactory", e);
                }
            }
            else if (proxySettings.getProxy().type() == Proxy.Type.HTTP)
            {
                this.connectOptions.setSocketFactory(new HttpProxySocketFactory(sslContext.getSocketFactory(), proxySettings));
            }
            else
            {
                throw new IllegalArgumentException("Proxy settings must be configured to use either SOCKS or HTTP");
            }
        }
        else
        {
            this.connectOptions.setSocketFactory(sslContext.getSocketFactory());
        }

        if (password != null && password.length > 0)
        {
            this.connectOptions.setPassword(password);
        }

        this.deviceMessaging = new MqttMessaging(
            mqttAsyncClient,
            deviceId,
            this,
            moduleId,
            this.config.getGatewayHostname() != null && !this.config.getGatewayHostname().isEmpty(),
            this.connectOptions);

        mqttAsyncClient.setCallback(this.deviceMessaging);

        this.deviceMethod = new MqttDeviceMethod(
            mqttAsyncClient,
            deviceId,
            this.connectOptions);

        this.deviceTwin = new MqttDeviceTwin(
            mqttAsyncClient,
            deviceId,
            this.connectOptions);
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
            this.deviceMessaging.setConnectionId(this.connectionId);
            this.deviceTwin.setConnectionId(this.connectionId);
            this.deviceMethod.setConnectionId(this.connectionId);

            if (this.state == IotHubConnectionStatus.CONNECTED)
            {
                return;
            }

            log.debug("Opening MQTT connection...");

            this.deviceMessaging.start();
            this.state = IotHubConnectionStatus.CONNECTED;

            log.debug("MQTT connection opened successfully");

            this.listener.onConnectionEstablished(this.connectionId);
        }
    }

    /**
     * Closes the connection. After the connection is closed, it is no longer usable.
     * If the connection is already closed, the function shall do nothing.
     */
    public void close() throws TransportException
    {
        synchronized (this.mqttConnectionStateLock)
        {
            if (this.state == IotHubConnectionStatus.DISCONNECTED)
            {
                return;
            }

            log.debug("Closing MQTT connection");

            this.deviceMethod.stop();
            this.deviceTwin.stop();
            this.deviceMessaging.stop();

            this.state = IotHubConnectionStatus.DISCONNECTED;
            log.debug("Successfully closed MQTT connection");

            // MqttAsyncClient's are unusable after they have been closed. This logic creates a new client
            // so that if this MqttIotHubConnection layer is opened again, it will have a usable mqttAsyncClient
            MqttAsyncClient mqttAsyncClient = buildMqttAsyncClient(this.serverUri, this.clientId);
            this.deviceMessaging.setMqttAsyncClient(mqttAsyncClient);
            mqttAsyncClient.setCallback(this.deviceMessaging);
            this.deviceTwin.setMqttAsyncClient(mqttAsyncClient);
            this.deviceMethod.setMqttAsyncClient(mqttAsyncClient);
        }
    }

    /**
     * Receives a message, if one exists.
     *
     * @return the message received, or null if none exists.
     *
     * @throws TransportException if the connection state is currently closed.
     */
    private IotHubTransportMessage receiveMessage() throws TransportException
    {
        IotHubTransportMessage message = this.deviceMethod.receive();
        if (message != null)
        {
            log.trace("Received MQTT device method message ({})", message);
            return message;
        }

        message = deviceTwin.receive();
        if (message != null)
        {
            log.trace("Received MQTT device twin message ({})", message);
            return message;
        }

        message = deviceMessaging.receive();
        if (message != null)
        {
            log.trace("Received MQTT device messaging message ({})", message);
            return message;
        }

        return null;
    }

    @Override
    public void setListener(IotHubListener listener) throws IllegalArgumentException
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("listener cannot be null");
        }

        this.listener = listener;
        this.deviceMessaging.setListener(listener);
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

        IotHubStatusCode result = IotHubStatusCode.OK_EMPTY;

        if (message.getMessageType() == DEVICE_METHODS)
        {
            this.deviceMethod.start();
            log.trace("Sending MQTT device method message ({})", message);
            this.deviceMethod.send((IotHubTransportMessage) message);
        }
        else if (message.getMessageType() == DEVICE_TWIN)
        {
            this.deviceTwin.start();
            log.trace("Sending MQTT device twin message ({})", message);
            this.deviceTwin.send((IotHubTransportMessage) message);
        }
        else
        {
            log.trace("Sending MQTT device telemetry message ({})", message);
            this.deviceMessaging.send(message);
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

        boolean ackSent;
        log.trace("Sending MQTT ACK for a received message ({})", message);
        if (message.getMessageType() == DEVICE_METHODS)
        {
            this.deviceMethod.start();
            ackSent = this.deviceMethod.sendMessageAcknowledgement(messageId);
        }
        else if (message.getMessageType() == DEVICE_TWIN)
        {
            this.deviceTwin.start();
            ackSent = this.deviceTwin.sendMessageAcknowledgement(messageId);
        }
        else
        {
            ackSent = this.deviceMessaging.sendMessageAcknowledgement(messageId);
        }

        if (ackSent)
        {
            log.trace("MQTT ACK was sent for a received message so it has been removed from the messages to acknowledge list ({})", message);
            this.receivedMessagesToAcknowledge.remove(message);
        }

        return ackSent;
    }

    @Override
    public String getConnectionId()
    {
        return this.connectionId;
    }

    @Override
    public void onMessageArrived(int messageId)
    {
        IotHubTransportMessage transportMessage = null;
        try
        {
            transportMessage = this.receiveMessage();
        }
        catch (TransportException e)
        {
            this.listener.onMessageReceived(null, new TransportException("Failed to receive message from service", e));
            log.error("Encountered exception while receiving message over MQTT", e);
        }

        if (transportMessage == null)
        {
            //Ack is not sent to service for this message because we cannot interpret the message. Service will likely re-send
            this.listener.onMessageReceived(null, new TransportException("Message sent from service could not be parsed"));
            log.warn("Received message that could not be parsed. That message has been ignored.");
        }
        else
        {
            log.trace("MQTT received message so it has been added to the messages to acknowledge list ({})", transportMessage);
            this.receivedMessagesToAcknowledge.put(transportMessage, messageId);

            switch (transportMessage.getMessageType())
            {
                case DEVICE_TWIN:
                    transportMessage.setMessageCallback(this.config.getDeviceTwinMessageCallback());
                    transportMessage.setMessageCallbackContext(this.config.getDeviceTwinMessageContext());
                    break;
                case DEVICE_METHODS:
                    transportMessage.setMessageCallback(this.config.getDeviceMethodsMessageCallback());
                    transportMessage.setMessageCallbackContext(this.config.getDeviceMethodsMessageContext());
                    break;
                case DEVICE_TELEMETRY:
                    transportMessage.setMessageCallback(this.config.getDeviceTelemetryMessageCallback(transportMessage.getInputName()));
                    transportMessage.setMessageCallbackContext(this.config.getDeviceTelemetryMessageContext(transportMessage.getInputName()));
                    break;
                case UNKNOWN:
                case CBS_AUTHENTICATION:
                default:
                    //do nothing
            }

            this.listener.onMessageReceived(transportMessage, null);
        }
    }

    private MqttAsyncClient buildMqttAsyncClient(String serverUri, String clientId) throws TransportException
    {
        MqttAsyncClient mqttAsyncClient;
        try
        {
            mqttAsyncClient = new MqttAsyncClient(serverUri, clientId, new MemoryPersistence());
        }
        catch (MqttException e)
        {
            throw PahoExceptionTranslator.convertToMqttException(e, "Failed to create mqtt client");
        }

        mqttAsyncClient.setManualAcks(true);
        return mqttAsyncClient;
    }
}
