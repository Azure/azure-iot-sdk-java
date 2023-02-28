// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.*;
import com.microsoft.azure.sdk.iot.device.twin.DeviceOperations;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_METHODS;
import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_TWIN;
import static com.microsoft.azure.sdk.iot.device.twin.DeviceOperations.*;
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

    private static final int CONNECTION_TIMEOUT = 60 * 1000;
    private static final int DISCONNECTION_TIMEOUT = 60 * 1000;
    private static final int QOS = 1;
    private static final int MAX_SUBSCRIBE_ACK_WAIT_TIME = 15 * 1000;

    private String connectionId;
    private String webSocketQueryString;
    private final Object mqttConnectionStateLock = new Object(); // lock for preventing simultaneous open and close calls
    private final ClientConfiguration config;
    private IotHubConnectionStatus state = IotHubConnectionStatus.DISCONNECTED;
    private final MqttConnectOptions.MqttConnectOptionsBuilder connectOptions;
    private final Map<IotHubTransportMessage, Integer> receivedMessagesToAcknowledge = new ConcurrentHashMap<>();
    private final Map<String, DeviceOperations> twinRequestMap = new HashMap<>();

    private final IMqttAsyncClient mqttClient;
    private IotHubListener listener;

    private final String publishTelemetryTopic;
    private final String deviceboundMessagesTopic;
    private final String moduleboundMessagesTopic;
    private final String moduleInputMessagesTopic;
    private static final String DesiredPropertiesTopic = "$iothub/twin/PATCH/properties/desired";
    private static final String DirectMethodTopic = "$iothub/methods/POST";
    private static final String TwinResponseTopic = "$iothub/twin/res";

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
        String clientId;
        if (moduleId != null && !moduleId.isEmpty())
        {
            clientId = deviceId + "/" + moduleId;
        }
        else
        {
            clientId = deviceId;
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
                serviceParams = TransportUtils.IOTHUB_API_VERSION + "&model-id=" + URLEncoder.encode(modelId, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
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
                .username(iotHubUserName)
                .clientId(clientId)
                .proxySettings(config.getProxySettings());

        this.mqttClient = config.getMqttAsyncClient();

        try
        {
            this.connectOptions.sslContext(this.config.getAuthenticationProvider().getSSLContext());
        }
        catch (IOException e)
        {
            throw new TransportException("Failed to get SSLContext", e);
        }

        if (config.getModuleId() != null && !config.getModuleId().isEmpty())
        {
            this.publishTelemetryTopic = "devices/" + deviceId + "/modules/" + moduleId +"/messages/events/";
            this.moduleboundMessagesTopic = "devices/" + config.getDeviceId() + "/modules/" + config.getModuleId() + "/messages/devicebound";
            this.moduleInputMessagesTopic = "devices/" + config.getDeviceId() + "/modules/" + config.getModuleId() + "/inputs";

            this.deviceboundMessagesTopic = null;
        }
        else
        {
            this.publishTelemetryTopic = "devices/" + this.config.getDeviceId() + "/messages/events/";
            this.deviceboundMessagesTopic = "devices/" + config.getDeviceId() + "/messages/devicebound";

            this.moduleboundMessagesTopic = null;
            this.moduleInputMessagesTopic = null;
        }
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

            this.mqttClient.setConnectionLostCallback(new Consumer<Integer>()
            {
                @Override
                public void accept(Integer integer)
                {
                    state = IotHubConnectionStatus.DISCONNECTED;
                    TransportException exception = new TransportException("TODO");
                    exception.setRetryable(true);
                    listener.onConnectionLost(exception, connectionId);
                }
            });

            log.debug("Opening MQTT connection...");
            CountDownLatch connectLatch = new CountDownLatch(1);
            this.mqttClient.connectAsync(this.connectOptions.build(), new Consumer<Integer>()
            {
                @Override
                public void accept(Integer integer)
                {
                    connectLatch.countDown();
                }
            });

            TransportException timeoutException = new TransportException("Timed out waiting for MQTT connection to open");
            timeoutException.setRetryable(true);
            try
            {
                boolean timedOut = !connectLatch.await(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

                if (timedOut)
                {
                    throw timeoutException;
                }
            }
            catch (InterruptedException e)
            {
                throw timeoutException;
            }

            if (config.getModuleId() == null || config.getModuleId().isEmpty())
            {
                subscribeSynchronously(
                    this.deviceboundMessagesTopic + "/#",
                    "Timed out waiting for cloud to device message subscription to be acknowledged");
            }
            else
            {
                subscribeSynchronously(
                    this.moduleboundMessagesTopic + "/#",
                    "Timed out waiting for cloud to module messaging subscription to be acknowledged");

                subscribeSynchronously(
                    this.moduleInputMessagesTopic + "/#",
                    "Timed out waiting for module input messaging subscription to be acknowledged");
            }

            this.mqttClient.setMessageCallback(messageReceiveHandler);

            subscribeSynchronously(
                TwinResponseTopic + "/#",
                "Timed out waiting for twin response subscription to be acknowledged");

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

            CountDownLatch connectLatch = new CountDownLatch(1);
            this.mqttClient.disconnectAsync(new Consumer<Integer>()
            {
                @Override
                public void accept(Integer integer)
                {
                    connectLatch.countDown();
                }
            });

            try
            {
                //TODO timeout value?
                boolean timedOut = !connectLatch.await(DISCONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);

                if (timedOut)
                {
                    log.warn("Timed out waiting for the service to acknowledge the disconnection.");
                }
            }
            catch (InterruptedException e)
            {
                log.warn("Timed out waiting for the service to acknowledge the disconnection.");
            }
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
        if (message == null || message.getBytes() == null)
        {
            return IotHubStatusCode.BAD_FORMAT;
        }

        if (this.state == IotHubConnectionStatus.DISCONNECTED)
        {
            throw new IllegalStateException("Cannot send event using a closed MQTT connection");
        }

        if (message.getMessageType() == DEVICE_METHODS)
        {
            IotHubTransportMessage transportMessage = (IotHubTransportMessage) message;
            if (transportMessage.getDeviceOperationType() == DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST)
            {
                return subscribeToDirectMethods();
            }
            else if (transportMessage.getDeviceOperationType() == DEVICE_OPERATION_METHOD_SEND_RESPONSE)
            {
                return sendDirectMethodResponseAsync(transportMessage);
            }
        }
        else if (message.getMessageType() == DEVICE_TWIN)
        {
            IotHubTransportMessage transportMessage = (IotHubTransportMessage) message;
            if (transportMessage.getDeviceOperationType() == DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST)
            {
                return subscribeToDesiredPropertyUpdates();
            }
            else if (transportMessage.getDeviceOperationType() == DEVICE_OPERATION_TWIN_GET_REQUEST)
            {
                return getTwinAsync(transportMessage);
            }
            else
            {
                return sendReportedPropertyUpdateAsync(transportMessage);
            }
        }
        else
        {
            return sendTelemetryAsync(message);
        }

        return IotHubStatusCode.BAD_FORMAT;
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
            messageId = receivedMessagesToAcknowledge.remove(message);
            mqttClient.acknowledgeMessageAsync(messageId, 0);
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

    private IotHubStatusCode sendTelemetryAsync(Message message) throws TransportException
    {
        String topic = TopicParser.BuildTelemetryTopic(this.publishTelemetryTopic, message);

        mqttClient.publishAsync(
            topic,
            message.getBytes(),
            QOS,  //TODO qos
            new Consumer<Integer>()
            {
                @Override
                public void accept(Integer integer)
                {
                    listener.onMessageSent(message, config.getDeviceId(), null); //TODO error case?
                }
            });

        return IotHubStatusCode.OK;
    }

    private IotHubStatusCode sendDirectMethodResponseAsync(IotHubTransportMessage transportMessage)
    {
        if (transportMessage.getRequestId() == null || transportMessage.getRequestId().isEmpty())
        {
            throw new IllegalArgumentException("Request id cannot be null or empty");
        }

        String topic = "$iothub/methods/res/" + transportMessage.getStatus() + "/" + "?$rid=" + transportMessage.getRequestId();

        this.mqttClient.publishAsync(topic, transportMessage.getBytes(), QOS, new Consumer<Integer>()
        {
            @Override
            public void accept(Integer integer)
            {
                listener.onMessageSent(transportMessage, transportMessage.getConnectionDeviceId(), null);
            }
        });

        return IotHubStatusCode.OK;
    }

    private IotHubStatusCode subscribeToDesiredPropertyUpdates() throws TransportException
    {
        log.debug("Subscribing to desired property updates...");
        CountDownLatch desiredPropertyUpdateSubscriptionLatch = new CountDownLatch(1);
        this.mqttClient.subscribeAsync(DesiredPropertiesTopic + "/#", QOS, new Consumer<Integer>()
        {
            @Override
            public void accept(Integer integer)
            {
                log.debug("IoT hub acknowledged the desired properties update subscription request");
                desiredPropertyUpdateSubscriptionLatch.countDown();
            }
        });

        TransportException transportException = new TransportException("Timed out waiting for desired properties update subscription to be acknowledged by the service");
        transportException.setRetryable(true);

        try
        {
            boolean timedOut = !desiredPropertyUpdateSubscriptionLatch.await(MAX_SUBSCRIBE_ACK_WAIT_TIME, TimeUnit.MILLISECONDS);

            if (timedOut)
            {
                throw transportException;
            }
        }
        catch (InterruptedException e)
        {
            transportException.initCause(e);
            throw transportException;
        }

        return IotHubStatusCode.OK;
    }

    private IotHubStatusCode subscribeToDirectMethods() throws TransportException
    {
        log.debug("Subscribing to direct methods...");
        CountDownLatch directMethodsSubscriptionLatch = new CountDownLatch(1);
        this.mqttClient.subscribeAsync(DirectMethodTopic + "/#", QOS, new Consumer<Integer>()
        {
            @Override
            public void accept(Integer integer)
            {
                log.debug("IoT hub acknowledged the direct method subscription request");
                directMethodsSubscriptionLatch.countDown();
            }
        });

        TransportException transportException = new TransportException("Timed out waiting for direct methods subscription to be acknowledged by the service");
        transportException.setRetryable(true);

        try
        {
            boolean timedOut = !directMethodsSubscriptionLatch.await(MAX_SUBSCRIBE_ACK_WAIT_TIME, TimeUnit.MILLISECONDS);

            if (timedOut)
            {
                throw transportException;
            }
        }
        catch (InterruptedException e)
        {
            transportException.initCause(e);
            throw transportException;
        }

        return IotHubStatusCode.OK;
    }

    private IotHubStatusCode getTwinAsync(IotHubTransportMessage message)
    {
        log.debug("Sending 'get twin' request with id {}", message.getRequestId());
        String topic = "$iothub/twin/GET/?$rid=" + message.getRequestId();
        this.twinRequestMap.put(message.getRequestId(), message.getDeviceOperationType());
        this.mqttClient.publishAsync(topic, new byte[0], 1, new Consumer<Integer>()
        {
            @Override
            public void accept(Integer integer)
            {
                log.debug("IoT hub acknowledged the 'get twin' request with id {}", message.getRequestId());
                listener.onMessageSent(message, message.getConnectionDeviceId(), null);
            }
        });

        return IotHubStatusCode.OK;
    }

    private IotHubStatusCode sendReportedPropertyUpdateAsync(IotHubTransportMessage message)
    {
        log.debug("Sending 'patch twin' request with id {}", message.getRequestId());
        String topic = "$iothub/twin/PATCH/properties/reported/?$rid=" + message.getRequestId() + "&$version=" + message.getVersion();
        this.twinRequestMap.put(message.getRequestId(), message.getDeviceOperationType());
        this.mqttClient.publishAsync(topic, message.getBytes(), 1, new Consumer<Integer>()
        {
            @Override
            public void accept(Integer integer)
            {
                log.debug("IoT hub acknowledged the 'patch twin' request with id {}", message.getRequestId());
                listener.onMessageSent(message, message.getConnectionDeviceId(), null);
            }
        });


        return IotHubStatusCode.OK;
    }

    private final Consumer<ReceivedMqttMessage> messageReceiveHandler = new Consumer<ReceivedMqttMessage>()
    {
        @Override
        public void accept(ReceivedMqttMessage receivedMqttMessage)
        {
            String topic = receivedMqttMessage.getTopic();
            IotHubTransportMessage message;
            if (topic.startsWith(DirectMethodTopic))
            {
                message = MessageParser.ConstructDirectMethodMessage(receivedMqttMessage);
            }
            else if (topic.startsWith(DesiredPropertiesTopic))
            {
                message = MessageParser.ConstructDesiredPropertiesUpdateMessage(receivedMqttMessage);
            }
            else if (topic.startsWith(TwinResponseTopic))
            {
                message = MessageParser.ConstructTwinResponseMessage(receivedMqttMessage, twinRequestMap);
            }
            else
            {
                message = MessageParser.ConstructTelemetryMessage(receivedMqttMessage);
            }

            if (message.getQualityOfService() == 0)
            {
                // Direct method messages and Twin messages are always sent with QoS 0, so there is no need for this SDK
                // to acknowledge them.
                log.trace("MQTT received message with QoS 0 so it has not been added to the messages to acknowledge list ({})", message);
            }
            else
            {
                log.trace("MQTT received message so it has been added to the messages to acknowledge list ({})", message);
                receivedMessagesToAcknowledge.put(message, receivedMqttMessage.getMessageId());
            }

            switch (message.getMessageType())
            {
                case DEVICE_TWIN:
                    message.setMessageCallback(config.getDeviceTwinMessageCallback());
                    message.setMessageCallbackContext(config.getDeviceTwinMessageContext());
                    break;
                case DEVICE_METHODS:
                    message.setMessageCallback(config.getDirectMethodsMessageCallback());
                    message.setMessageCallbackContext(config.getDirectMethodsMessageContext());
                    break;
                case DEVICE_TELEMETRY:
                    message.setMessageCallback(config.getDeviceTelemetryMessageCallback(message.getInputName()));
                    message.setMessageCallbackContext(config.getDeviceTelemetryMessageContext(message.getInputName()));
                    break;
                case UNKNOWN:
                default:
                    //do nothing
            }

            listener.onMessageReceived(message, null);
        }
    };

    private void subscribeSynchronously(String topic, String timeoutMessage) throws TransportException
    {
        CountDownLatch cloudToDeviceMessageSubscriptionLatch = new CountDownLatch(1);
        this.mqttClient.subscribeAsync(topic, QOS, new Consumer<Integer>()
        {
            @Override
            public void accept(Integer integer)
            {
                cloudToDeviceMessageSubscriptionLatch.countDown();
            }
        });

        TransportException transportException = new TransportException(timeoutMessage);
        transportException.setRetryable(true);
        try
        {
            boolean timedOut = !cloudToDeviceMessageSubscriptionLatch.await(MAX_SUBSCRIBE_ACK_WAIT_TIME, TimeUnit.MILLISECONDS);

            if (timedOut)
            {
                throw transportException;
            }
        }
        catch (InterruptedException e)
        {
            transportException.initCause(e);
            throw transportException;
        }
    }
}
