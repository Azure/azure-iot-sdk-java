// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.*;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions.PahoExceptionTranslator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_METHODS;
import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_TWIN;
import static com.microsoft.azure.sdk.iot.device.transport.mqtt.Mqtt.MAX_IN_FLIGHT_COUNT;
import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1_1;

@Slf4j
public class MqttIotHubConnection implements IotHubTransportConnection, MqttMessageListener
{
    //string constants
    private static final String WS_SSL_PREFIX = "wss://";

    private static final String WEBSOCKET_RAW_PATH = "/$iothub/websocket";
    private static final String NO_CLIENT_CERT_QUERY_STRING = "?iothub-no-client-cert=true";

    private static final String SSL_PREFIX = "ssl://";
    private static final String SSL_PORT_SUFFIX = ":8883";

    private static final int MQTT_VERSION = MQTT_VERSION_3_1_1;
    private static final boolean SET_CLEAN_SESSION = false;

    private static final String MODEL_ID = "model-id";

    private String connectionId;
    private String webSocketQueryString;
    private final Object mqttConnectionStateLock = new Object(); // lock for preventing simultaneous open and close calls
    private final ClientConfiguration config;
    private IotHubConnectionStatus state = IotHubConnectionStatus.DISCONNECTED;
    private IotHubListener listener;
    private final String clientId;
    private final String serverUri;

    private static final String E4K_WILL_TOPIC = "to be defined later";
    private static final byte[] E4K_WILL_MESSAGE_PAYLOAD = "to be defined later".getBytes(StandardCharsets.UTF_8);
    private static final int E4K_WILL_MESSAGE_QOS = 1;
    private static final boolean E4K_WILL_MESSAGE_RETAINED = true;

    private static final String E4K_CONNECTION_CLOSE_TOPIC = "to be defined later";
    private static final byte[] E4K_CONNECTION_CLOSE_MESSAGE_PAYLOAD = "to be defined later".getBytes(StandardCharsets.UTF_8);
    private static final int E4K_CONNECTION_CLOSE_MESSAGE_QOS = 1;
    private static final boolean E4K_CONNECTION_CLOSE_MESSAGE_RETAINED = true;
    private static final int E4K_CONNECTION_CLOSE_MESSAGE_TIMEOUT_MILLISECONDS = 15 * 1000; // 15 seconds

    private static final String E4K_CONNECTION_OPEN_TOPIC = "to be defined later";
    private static final byte[] E4K_CONNECTION_OPEN_MESSAGE_PAYLOAD = "to be defined later".getBytes(StandardCharsets.UTF_8);
    private static final int E4K_CONNECTION_OPEN_MESSAGE_QOS = 1;
    private static final boolean E4K_CONNECTION_OPEN_MESSAGE_RETAINED = true;
    private static final int E4K_CONNECTION_OPEN_MESSAGE_TIMEOUT_MILLISECONDS = 15 * 1000; // 15 seconds

    //Messaging clients, never null
    private final MqttMessaging deviceMessaging;
    private final MqttTwin deviceTwin;
    private final MqttDirectMethod directMethod;

    private MqttAsyncClient mqttAsyncClient;

    private final Map<IotHubTransportMessage, Integer> receivedMessagesToAcknowledge = new ConcurrentHashMap<>();

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

        if (this.config.isUsingWebsocket())
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

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setKeepAliveInterval(config.getKeepAliveInterval());
        connectOptions.setCleanSession(SET_CLEAN_SESSION);
        connectOptions.setMqttVersion(MQTT_VERSION);
        connectOptions.setUserName(iotHubUserName);
        connectOptions.setMaxInflight(MAX_IN_FLIGHT_COUNT);
        if (this.config.isConnectingToMqttGateway())
        {
            connectOptions.setWill(E4K_WILL_TOPIC, E4K_WILL_MESSAGE_PAYLOAD, E4K_WILL_MESSAGE_QOS, E4K_WILL_MESSAGE_RETAINED);
        }
        ProxySettings proxySettings = config.getProxySettings();
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
        }

        // these variables are shared between the messaging, twin and method subclients
        Map<Integer, Message> unacknowledgedSentMessages = new ConcurrentHashMap<>();
        Queue<Pair<String, MqttMessage>> receivedMessages = new ConcurrentLinkedQueue<>();

        this.deviceMessaging = new MqttMessaging(
            deviceId,
            this,
            moduleId,
            this.config.getGatewayHostname() != null && !this.config.getGatewayHostname().isEmpty(),
            connectOptions,
            unacknowledgedSentMessages,
            receivedMessages);

        this.directMethod = new MqttDirectMethod(
            deviceId,
            connectOptions,
            unacknowledgedSentMessages,
            receivedMessages);

        this.deviceTwin = new MqttTwin(
            deviceId,
            connectOptions,
            unacknowledgedSentMessages,
            receivedMessages);
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
            this.directMethod.setConnectionId(this.connectionId);

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
                    this.deviceMessaging.updatePassword(this.config.getSasTokenAuthentication().getSasToken());
                }
                catch (IOException e)
                {
                    throw new TransportException("Failed to open the MQTT connection because a SAS token could not be retrieved", e);
                }
            }

            // MqttAsyncClient's are unusable after they have been closed. This logic creates a new client
            // each time an open is called
            this.mqttAsyncClient = buildMqttAsyncClient(this.serverUri, this.clientId);
            this.mqttAsyncClient.setCallback(this.deviceMessaging);
            this.deviceMessaging.setMqttAsyncClient(this.mqttAsyncClient);
            this.deviceTwin.setMqttAsyncClient(this.mqttAsyncClient);
            this.directMethod.setMqttAsyncClient(this.mqttAsyncClient);

            this.deviceMessaging.start();

            // When connecting to E4K, the client is expected to publish a message after opening the connection
            // so that E4K knows to start the upstream connection for this device.
            if (this.config.isConnectingToMqttGateway())
            {
                try
                {
                    log.debug("Sending the ");
                    IMqttDeliveryToken token = this.mqttAsyncClient.publish(
                        E4K_CONNECTION_OPEN_TOPIC,
                        E4K_CONNECTION_OPEN_MESSAGE_PAYLOAD,
                        E4K_CONNECTION_OPEN_MESSAGE_QOS,
                        E4K_CONNECTION_OPEN_MESSAGE_RETAINED);

                    // If the message fails to be sent/acknowledged in this time span, an MqttException will be thrown here
                    token.waitForCompletion(E4K_CONNECTION_OPEN_MESSAGE_TIMEOUT_MILLISECONDS);
                }
                catch (MqttException e)
                {
                    try
                    {
                        this.mqttAsyncClient.close();
                    }
                    catch (MqttException ex)
                    {
                        log.warn("Encountered an error while closing the MQTT connection", ex);
                    }

                    TransportException transportException =
                        new TransportException("Successfully opened the MQTT connection but failed to send the \"Connection established\" message to the MQTT gateway. Closing the connection...");
                    transportException.setRetryable(true);
                    throw transportException;
                }
            }

            this.state = IotHubConnectionStatus.CONNECTED;

            log.debug("MQTT connection opened successfully");

            this.listener.onConnectionEstablished(this.connectionId);
        }
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

            // When connecting to E4K, the client is expected to publish a message before gracefully closing the connection
            // so that E4K knows to end the upstream connection for this device. Note that the MQTT connection's "will"
            // message isn't sufficient to handle this case because the "will" message is only sent on ungraceful disconnections.
            if (this.config.isConnectingToMqttGateway())
            {
                try
                {
                    IMqttDeliveryToken token = this.mqttAsyncClient.publish(
                        E4K_CONNECTION_CLOSE_TOPIC,
                        E4K_CONNECTION_CLOSE_MESSAGE_PAYLOAD,
                        E4K_CONNECTION_CLOSE_MESSAGE_QOS,
                        E4K_CONNECTION_CLOSE_MESSAGE_RETAINED);

                    // If the message fails to be sent/acknowledged in this time span, an MqttException will be thrown here
                    token.waitForCompletion(E4K_CONNECTION_CLOSE_MESSAGE_TIMEOUT_MILLISECONDS);
                }
                catch (MqttException e)
                {
                    log.warn("Failed to send the \"Connection will be closed gracefully\" message to the MQTT gateway. " +
                        "Still closing the connection anyways", e);
                }
            }

            this.directMethod.stop();
            this.deviceTwin.stop();
            this.deviceMessaging.stop();

            this.state = IotHubConnectionStatus.DISCONNECTED;
            log.debug("Successfully closed MQTT connection");
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

        IotHubStatusCode result = IotHubStatusCode.OK;

        if (message.getMessageType() == DEVICE_METHODS)
        {
            this.directMethod.start();
            log.trace("Sending MQTT device method message ({})", message);
            this.directMethod.send((IotHubTransportMessage) message);
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

        log.trace("Sending MQTT ACK for a received message ({})", message);
        if (message.getMessageType() == DEVICE_METHODS)
        {
            this.directMethod.start();
            this.directMethod.sendMessageAcknowledgement(messageId);
        }
        else if (message.getMessageType() == DEVICE_TWIN)
        {
            this.deviceTwin.start();
            this.deviceTwin.sendMessageAcknowledgement(messageId);
        }
        else
        {
            this.deviceMessaging.sendMessageAcknowledgement(messageId);
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

    @Override
    public void onMessageArrived(int messageId)
    {
        IotHubTransportMessage transportMessage = this.directMethod.receive();
        if (transportMessage != null)
        {
            log.trace("Received MQTT device method message ({})", transportMessage);
        }
        else
        {
            transportMessage = deviceTwin.receive();
            if (transportMessage != null)
            {
                log.trace("Received MQTT device twin message ({})", transportMessage);
            }
            else
            {
                transportMessage = deviceMessaging.receive();
                if (transportMessage != null)
                {
                    log.trace("Received MQTT device messaging message ({})", transportMessage);
                }
            }
        }

        if (transportMessage == null)
        {
            //Ack is not sent to service for this message because we cannot interpret the message. Service will likely re-send
            this.listener.onMessageReceived(null, new TransportException("Message sent from service could not be parsed"));
            log.warn("Received message that could not be parsed. That message has been ignored.");
        }
        else
        {
            if (transportMessage.getQualityOfService() == 0)
            {
                // Direct method messages and Twin messages are always sent with QoS 0, so there is no need for this SDK
                // to acknowledge them.
                log.trace("MQTT received message with QoS 0 so it has not been added to the messages to acknowledge list ({})", transportMessage);
            }
            else
            {
                log.trace("MQTT received message so it has been added to the messages to acknowledge list ({})", transportMessage);
                this.receivedMessagesToAcknowledge.put(transportMessage, messageId);
            }

            switch (transportMessage.getMessageType())
            {
                case DEVICE_TWIN:
                    transportMessage.setMessageCallback(this.config.getDeviceTwinMessageCallback());
                    transportMessage.setMessageCallbackContext(this.config.getDeviceTwinMessageContext());
                    break;
                case DEVICE_METHODS:
                    transportMessage.setMessageCallback(this.config.getDirectMethodsMessageCallback());
                    transportMessage.setMessageCallbackContext(this.config.getDirectMethodsMessageContext());
                    break;
                case DEVICE_TELEMETRY:
                    transportMessage.setMessageCallback(this.config.getDeviceTelemetryMessageCallback(transportMessage.getInputName()));
                    transportMessage.setMessageCallbackContext(this.config.getDeviceTelemetryMessageContext(transportMessage.getInputName()));
                    break;
                case UNKNOWN:
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
