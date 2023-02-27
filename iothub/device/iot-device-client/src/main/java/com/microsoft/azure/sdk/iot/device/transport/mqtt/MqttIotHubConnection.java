// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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

    private static final int CONNECTION_TIMEOUT = 60 * 1000;
    private static final int DISCONNECTION_TIMEOUT = 60 * 1000;
    private static final int QOS = 1;
    private static final int MAX_SUBSCRIBE_ACK_WAIT_TIME = 15 * 1000;

    /* Each property is separated by & and all system properties start with an encoded $ (except for iothub-ack) */
    final static char MESSAGE_PROPERTY_SEPARATOR = '&';
    private final static String MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_ENCODED = "%24";
    private final static char MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED = '$';
    final static char MESSAGE_PROPERTY_KEY_VALUE_SEPARATOR = '=';
    private final static int PROPERTY_KEY_INDEX = 0;
    private final static int PROPERTY_VALUE_INDEX = 1;

    /* The system property keys expected in a message */
    private final static String ABSOLUTE_EXPIRY_TIME = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".exp";
    final static String CORRELATION_ID = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".cid";
    final static String MESSAGE_ID = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".mid";
    final static String TO = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".to";
    final static String USER_ID = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".uid";
    final static String OUTPUT_NAME = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".on";
    final static String CONNECTION_DEVICE_ID = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".cdid";
    final static String CONNECTION_MODULE_ID = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".cmid";
    final static String CONTENT_TYPE = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".ct";
    final static String CONTENT_ENCODING = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".ce";
    final static String CREATION_TIME_UTC = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".ctime";
    final static String MQTT_SECURITY_INTERFACE_ID = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".ifid";
    final static String COMPONENT_ID = MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_DECODED + ".sub";

    private final static String IOTHUB_ACK = "iothub-ack";

    private final static String INPUTS_PATH_STRING = "inputs";
    private final static String MODULES_PATH_STRING = "modules";

    private String connectionId;
    private String webSocketQueryString;
    private final Object mqttConnectionStateLock = new Object(); // lock for preventing simultaneous open and close calls
    private final ClientConfiguration config;
    private IotHubConnectionStatus state = IotHubConnectionStatus.DISCONNECTED;
    private final String clientId;
    private final MqttConnectOptions.MqttConnectOptionsBuilder connectOptions;
    private final Map<IotHubTransportMessage, Integer> receivedMessagesToAcknowledge = new ConcurrentHashMap<>();

    private IAsyncMqttClient mqttClient;
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
                .username(iotHubUserName)
                .proxySettings(config.getProxySettings());

        try
        {
            this.connectOptions.sslContext(this.config.getAuthenticationProvider().getSSLContext());
        }
        catch (IOException e)
        {
            throw new TransportException("Failed to get SSLContext", e);
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
            this.mqttClient = new PahoAsyncMqttClient(); //TODO get from config

            log.debug("Opening MQTT connection...");
            CountDownLatch connectLatch = new CountDownLatch(1);
            this.mqttClient.connectAsync(options, new Consumer<Integer>()
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
                //TODO timeout value?
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

            //if (moduleId == null || moduleId.isEmpty())
            //{
            CountDownLatch cloudToDeviceMessageSubscriptionLatch = new CountDownLatch(1);
            this.mqttClient.subscribeAsync("devices/" + config.getDeviceId() + "/messages/devicebound/#", QOS, new Consumer<Integer>()
            {
                @Override
                public void accept(Integer integer)
                {
                    cloudToDeviceMessageSubscriptionLatch.countDown();
                }
            });

            TransportException transportException = new TransportException("Timed out waiting for cloud to device message subscription to be acknowledged");
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
            //}
            //else
            //{
                //this.publishTopic = "devices/" + deviceId + "/modules/" + moduleId +"/messages/events/";
                //this.eventsSubscribeTopic = "devices/" + deviceId + "/modules/" + moduleId + "/messages/devicebound/#";
                //this.inputsSubscribeTopic = "devices/" + deviceId + "/modules/" + moduleId +"/inputs/#";
            //}

            this.mqttClient.setMessageCallback(receivedMqttMessage ->
            {
                IotHubTransportMessage message = constructMessage(receivedMqttMessage);

                if (message.getQualityOfService() == 0)
                {
                    // Direct method messages and Twin messages are always sent with QoS 0, so there is no need for this SDK
                    // to acknowledge them.
                    log.trace("MQTT received message with QoS 0 so it has not been added to the messages to acknowledge list ({})", message);
                }
                else
                {
                    log.trace("MQTT received message so it has been added to the messages to acknowledge list ({})", message);
                    this.receivedMessagesToAcknowledge.put(message, receivedMqttMessage.getMessageId());
                }

                switch (message.getMessageType())
                {
                    case DEVICE_TWIN:
                        message.setMessageCallback(this.config.getDeviceTwinMessageCallback());
                        message.setMessageCallbackContext(this.config.getDeviceTwinMessageContext());
                        break;
                    case DEVICE_METHODS:
                        message.setMessageCallback(this.config.getDirectMethodsMessageCallback());
                        message.setMessageCallbackContext(this.config.getDirectMethodsMessageContext());
                        break;
                    case DEVICE_TELEMETRY:
                        message.setMessageCallback(this.config.getDeviceTelemetryMessageCallback(message.getInputName()));
                        message.setMessageCallbackContext(this.config.getDeviceTelemetryMessageContext(message.getInputName()));
                        break;
                    case UNKNOWN:
                    default:
                        //do nothing
                }

                this.listener.onMessageReceived(message, null);
            });

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
        }
        else if (message.getMessageType() == DEVICE_TWIN)
        {
        }
        else
        {
            return sendTelemetry(message);
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

    private IotHubStatusCode sendTelemetry(Message message) throws TransportException
    {
        //TODO modules
        String topic = "devices/" + this.config.getDeviceId() + "/messages/events/";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(topic);

        boolean separatorNeeded;

        separatorNeeded = appendPropertyIfPresent(stringBuilder, false, MESSAGE_ID, message.getMessageId(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, CORRELATION_ID, message.getCorrelationId(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, USER_ID, message.getUserId(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, TO, message.getTo(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, OUTPUT_NAME, message.getOutputName(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, CONNECTION_DEVICE_ID, message.getConnectionDeviceId(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, CONNECTION_MODULE_ID, message.getConnectionModuleId(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, CONTENT_ENCODING, message.getContentEncoding(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, CONTENT_TYPE, message.getContentType(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, CREATION_TIME_UTC, message.getCreationTimeUTCString(), false);
        if (message.isSecurityMessage())
        {
            separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, MQTT_SECURITY_INTERFACE_ID, MessageProperty.IOTHUB_SECURITY_INTERFACE_ID_VALUE, false);
        }

        if (message.getComponentName() != null && !message.getComponentName().isEmpty())
        {
            separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, COMPONENT_ID, message.getComponentName(), false);
        }

        for (MessageProperty property : message.getProperties())
        {
            separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, property.getName(), property.getValue(), true);
        }

        //if (this.moduleId != null && !this.moduleId.isEmpty())
        //{
        //    stringBuilder.append("/");
        //}

        String messagePublishTopic = stringBuilder.toString();

        mqttClient.publishAsync(
            messagePublishTopic,
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

    /**
     * Appends the property to the provided stringbuilder if the property value is not null.
     * @param stringBuilder the builder to build upon
     * @param separatorNeeded if a separator should precede the new property
     * @param propertyKey the mqtt topic string property key
     * @param propertyValue the property value (message id, correlation id, etc.)
     * @return true if a separator will be needed for any later properties appended on
     */
    private boolean appendPropertyIfPresent(StringBuilder stringBuilder, boolean separatorNeeded, String propertyKey, String propertyValue, boolean isApplicationProperty) throws TransportException
    {
        try
        {
            if (propertyValue != null && !propertyValue.isEmpty())
            {
                if (separatorNeeded)
                {
                    stringBuilder.append(MESSAGE_PROPERTY_SEPARATOR);
                }

                if (isApplicationProperty)
                {
                    // URLEncoder.Encode incorrectly encodes space characters as '+'. For MQTT to work, we need to replace those '+' with "%20"
                    stringBuilder.append(URLEncoder.encode(propertyKey, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20"));
                }
                else
                {
                    stringBuilder.append(propertyKey);
                }

                stringBuilder.append(MESSAGE_PROPERTY_KEY_VALUE_SEPARATOR);

                // URLEncoder.Encode incorrectly encodes space characters as '+'. For MQTT to work, we need to replace those '+' with "%20"
                stringBuilder.append(URLEncoder.encode(propertyValue, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20"));

                return true;
            }

            return separatorNeeded;
        }
        catch (UnsupportedEncodingException e)
        {
            throw new TransportException("Could not utf-8 encode the property with name " + propertyKey + " and value " + propertyValue, e);
        }
    }

    // Converts an MQTT message into our native "IoT hub" message
    private IotHubTransportMessage constructMessage(ReceivedMqttMessage mqttMessage)
    {
        String topic = mqttMessage.getTopic();

        IotHubTransportMessage message = new IotHubTransportMessage(mqttMessage.getPayload(), MessageType.DEVICE_TELEMETRY);

        message.setQualityOfService(mqttMessage.getQos());

        int propertiesStringStartingIndex = topic.indexOf(MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_ENCODED);
        if (propertiesStringStartingIndex != -1)
        {
            String propertiesString = topic.substring(propertiesStringStartingIndex);

            assignPropertiesToMessage(message, propertiesString);

            String routeString = topic.substring(0, propertiesStringStartingIndex);
            String[] routeComponents = routeString.split("/");

            if (routeComponents.length > 2 && routeComponents[2].equals(MODULES_PATH_STRING))
            {
                message.setConnectionModuleId(routeComponents[3]);
            }

            if (routeComponents.length > 4 && routeComponents[4].equals(INPUTS_PATH_STRING))
            {
                message.setInputName(routeComponents[5]);
            }
        }

        return message;
    }

    /**
     * Takes propertiesString and parses it for all the properties it holds and then assigns them to the provided message
     * @param propertiesString the string to parse containing all the properties
     * @param message the message to add the parsed properties to
     * @throws IllegalArgumentException if a property's key and value are not separated by the '=' symbol
     * @throws IllegalStateException if the property for expiry time is present, but the value cannot be parsed as a Long
     * */
    private void assignPropertiesToMessage(Message message, String propertiesString) throws IllegalStateException, IllegalArgumentException
    {
        for (String propertyString : propertiesString.split(String.valueOf(MESSAGE_PROPERTY_SEPARATOR)))
        {
            if (propertyString.contains("="))
            {
                //Expected format is <key>=<value> where both key and value may be encoded
                String key = propertyString.split("=")[PROPERTY_KEY_INDEX];
                String value = propertyString.split("=")[PROPERTY_VALUE_INDEX];

                try
                {
                    key = URLDecoder.decode(key, StandardCharsets.UTF_8.name());
                    value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
                }
                catch (UnsupportedEncodingException e)
                {
                    // should never happen, since the encoding is hard-coded.
                    throw new IllegalStateException(e);
                }

                //Some properties are reserved system properties and must be saved in the message differently
                //Codes_SRS_Mqtt_34_057: [This function shall parse the messageId, correlationId, outputname, content encoding and content type from the provided property string]
                switch (key)
                {
                    case TO:
                    case IOTHUB_ACK:
                    case USER_ID:
                    case ABSOLUTE_EXPIRY_TIME:
                        //do nothing
                        break;
                    case MESSAGE_ID:
                        message.setMessageId(value);
                        break;
                    case CORRELATION_ID:
                        message.setCorrelationId(value);
                        break;
                    case OUTPUT_NAME:
                        message.setOutputName(value);
                        break;
                    case CONTENT_ENCODING:
                        message.setContentEncoding(value);
                        break;
                    case CONTENT_TYPE:
                        message.setContentType(value);
                        break;
                    default:
                        message.setProperty(key, value);
                }
            }
            else
            {
                throw new IllegalArgumentException("Unexpected property string provided. Expected '=' symbol between key and value of the property in string: " + propertyString);
            }
        }
    }
}
