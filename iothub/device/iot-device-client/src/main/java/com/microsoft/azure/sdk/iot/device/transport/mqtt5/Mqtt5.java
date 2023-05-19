// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.transport.mqtt5;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.TopicParser;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions.PahoExceptionTranslator;
import com.microsoft.azure.sdk.iot.device.twin.DeviceOperations;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Pattern;

@Slf4j
public class Mqtt5
{
    private static final int QOS = 1;
    private static final int MAX_SUBSCRIBE_ACK_WAIT_TIME = 15 * 1000;

    // relatively arbitrary, but only because Paho doesn't have any particular recommendations here. Just a high enough
    // value that users who are building a gateway type solution don't find this value to be a bottleneck.
    static final int MAX_IN_FLIGHT_COUNT = 65000;

    private final String deviceId;
    private final String moduleId;
    private final String clientId;
    private final boolean isEdgeHub;
    private final boolean isConnectingToMqttGateway;

    final Object receivedMessagesLock; // lock for making operations on the receivedMessagesQueue atomic
    final Queue<Pair<String, MqttMessage>> receivedMessages;

    private boolean isStarted = false;

    private MqttAsyncClient mqttAsyncClient;
    private final Map<Integer, Message> unacknowledgedSentMessages;
    private final Map<String, DeviceOperations> twinRequestMap;

    // Telemetry topics
    private final String telemetryEventsSubscribeTopic;
    private final String telemetryInputsSubscribeTopic;
    private final String telemetryPublishTopic;

    // Direct method topics
    private final String methodSubscribeTopic;
    private final String methodResponseTopic;

    // Twin topics
    private final String twinSubscribeTopic;

    // Telemetry topic formatters
    private static final String TELEMETRY_EVENTS_HUB = "devices/%s/messages/events/";
    private static final String TELEMETRY_EVENTS_E4K = "$iothub/devices/%s/messages/events/";
    private static final String TELEMETRY_DEVICEBOUND_HUB = "devices/%s/messages/devicebound/#";
    private static final String TELEMETRY_DEVICEBOUND_E4K = "$iothub/devices/%s/messages/devicebound/#";
    private static final String TELEMETRY_INPUTS_HUB = "devices/%s/inputs/#";
    private static final String TELEMETRY_INPUTS_E4K = "$iothub/devices/%s/inputs/#";

    // Direct method topic formatters
    private static final String DIRECT_METHOD_POST_HUB = "$iothub/methods/POST";
    private static final String DIRECT_METHOD_POST_E4K = "$iothub/methods/%s/POST";
    private static final String DIRECT_METHOD_RES_HUB = "$iothub/methods/res";
    private static final String DIRECT_METHOD_RES_E4K = "$iothub/methods/%s/res";

    // Twin topic formatters
    private static final String TWIN_GET_HUB = "$iothub/twin/GET";
    private static final String TWIN_GET_E4K = "$iothub/twin/%s/GET";
    private static final String TWIN_PATCH_HUB = "$iothub/twin/PATCH";
    private static final String TWIN_PATCH_E4K = "$iothub/twin/%s/PATCH";
    private static final String TWIN_RES_HUB = "$iothub/twin/res";
    private static final String TWIN_RES_E4K = "$iothub/twin/%s/res";

    private static final String TWIN = "$iothub/twin";
    private static final String VERSION = "$version=";
    private static final String REQ_ID = "?$rid=";

    private final Object stateLock; // lock for preventing simultaneous connects and subscribe requests
    private final Object unacknowledgedSentMessagesLock; // lock for making operations on unacknowledgedSentMessages atomic

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

    /**
     * Constructor to instantiate mqtt v5 broker connection.
     */
    Mqtt5(
        String deviceId,
        String moduleId,
        boolean isEdgeHub,
        boolean isConnectingToMqttGateway,
        Map<Integer, Message> unacknowledgedSentMessages,
        Queue<Pair<String, MqttMessage>> receivedMessages)
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("Device id cannot be null or empty");
        }

        String clientId;

        if (!isConnectingToMqttGateway)
        {
            clientId = (moduleId == null || moduleId.isEmpty()) ?
                deviceId :
                deviceId + "/modules/" + moduleId;
        }
        else
        {
            clientId = (moduleId == null || moduleId.isEmpty()) ?
                deviceId :
                deviceId + "/" + moduleId;
        }

        if (!isConnectingToMqttGateway)
        {
            // devices/{clientId}/messages/events/
            this.telemetryPublishTopic = String.format(TELEMETRY_EVENTS_HUB, clientId);

            // devices/{clientId}/messages/devicebound/#
            this.telemetryEventsSubscribeTopic = String.format(TELEMETRY_DEVICEBOUND_HUB, clientId);

            this.telemetryInputsSubscribeTopic = (moduleId == null || moduleId.isEmpty()) ?
                    null :
                    String.format(TELEMETRY_INPUTS_HUB, clientId); // devices/{clientId}/inputs/#

            // $iothub/methods/POST/#
            this.methodSubscribeTopic = DIRECT_METHOD_POST_HUB + "/#";

            // $iothub/methods/res
            this.methodResponseTopic = DIRECT_METHOD_RES_HUB;

            // $iothub/twin/res/#
            this.twinSubscribeTopic = TWIN_RES_HUB + "/#";
        }
        else
        {
            // $iothub/devices/{clientId}/messages/events/
            this.telemetryPublishTopic = String.format(TELEMETRY_EVENTS_E4K, clientId);

            // $iothub/devices/{clientId}/messages/devicebound/#
            this.telemetryEventsSubscribeTopic = String.format(TELEMETRY_DEVICEBOUND_E4K, clientId);

            this.telemetryInputsSubscribeTopic = (moduleId == null || moduleId.isEmpty()) ?
                    null :
                    String.format(TELEMETRY_INPUTS_E4K, clientId); // $iothub/devices/{clientId}/inputs/#

            // $iothub/methods/{clientId}/POST/#
            this.methodSubscribeTopic = String.format(DIRECT_METHOD_POST_E4K, clientId) + "/#";

            // $iothub/methods/{clientId}/res
            this.methodResponseTopic = String.format(DIRECT_METHOD_RES_E4K, clientId);

            // $iothub/twin/{clientId}/res/#
            this.twinSubscribeTopic = String.format(TWIN_RES_E4K, clientId) + "/#";
        }

        this.deviceId = deviceId;
        this.moduleId = moduleId;
        this.clientId = clientId;
        this.isEdgeHub = isEdgeHub;
        this.isConnectingToMqttGateway = isConnectingToMqttGateway;
        this.unacknowledgedSentMessages = unacknowledgedSentMessages;
        this.twinRequestMap = new HashMap<>();
        this.receivedMessages = receivedMessages;
        this.receivedMessagesLock = new Object();
        this.stateLock = new Object();
        this.unacknowledgedSentMessagesLock = new Object();
    }

    public void start() throws TransportException
    {
        if (!isStarted)
        {
            this.subscribe(this.twinSubscribeTopic);
            isStarted = true;
        }

        if (!this.isEdgeHub)
        {
            this.subscribe(this.telemetryEventsSubscribeTopic);
        }
        else if (this.moduleId != null && !this.moduleId.isEmpty())
        {
            this.subscribe(this.telemetryInputsSubscribeTopic);
        }
    }

    public void stop()
    {
        isStarted = false;

        if (!twinRequestMap.isEmpty())
        {
            log.trace("Pending {} responses from IotHub yet unsubscribed", twinRequestMap.size());
        }
    }

    /**
     * Method to publish to mqtt broker connection.
     *
     * @param publishTopic the topic to publish on mqtt broker connection.
     * @param message the message to publish.
     * @throws TransportException if sas token has expired, if connection hasn't been established yet, or if Paho throws
     * for any other reason
     */
    public void publish(String publishTopic, Message message) throws TransportException
    {
        try
        {
            if (!this.mqttAsyncClient.isConnected())
            {
                TransportException transportException = new TransportException("Cannot publish when mqtt client is disconnected");
                transportException.setRetryable(true);
                throw transportException;
            }

            if (message == null || publishTopic == null || publishTopic.length() == 0 || message.getBytes() == null)
            {
                throw new IllegalArgumentException("Cannot publish on null or empty publish topic");
            }

            byte[] payload = message.getBytes();

            // Wait until either the number of in flight messages is below the limit before publishing another message
            // Or wait until the connection is lost so the message can be requeued for later
            while (this.mqttAsyncClient.getPendingTokens().length >= MAX_IN_FLIGHT_COUNT)
            {
                //noinspection BusyWait
                Thread.sleep(10);

                if (!this.mqttAsyncClient.isConnected())
                {
                    TransportException transportException = new TransportException("Cannot publish when mqtt client is holding " + MAX_IN_FLIGHT_COUNT + " tokens and is disconnected");
                    transportException.setRetryable(true);
                    throw transportException;
                }
            }

            MqttMessage mqttMessage = (payload.length == 0) ? new MqttMessage() : new MqttMessage(payload);

            mqttMessage.setQos(QOS);

            synchronized (this.unacknowledgedSentMessagesLock)
            {
                log.trace("Publishing message ({}) to MQTT topic {}", message, publishTopic);
                IMqttToken publishToken = this.mqttAsyncClient.publish(publishTopic, mqttMessage);
                unacknowledgedSentMessages.put(publishToken.getMessageId(), message);
                log.trace("Message published to MQTT topic {}. Mqtt message id {} added to list of messages to wait for acknowledgement ({})", publishTopic, publishToken.getMessageId(), message);
            }
        }
        catch (MqttException e)
        {
            log.warn("Message could not be published to MQTT topic {} ({})", publishTopic, message, e);
            throw PahoExceptionTranslator.convertToMqttException(e, "Unable to publish message on topic : " + publishTopic);
        }
        catch (InterruptedException e)
        {
            throw new TransportException("Interrupted, Unable to publish message on topic : " + publishTopic, e);
        }
    }

    /**
     * Method to subscribe to mqtt broker connection.
     *
     * @param topic the topic to subscribe on mqtt broker connection.
     * @throws TransportException if failed to subscribe the mqtt topic.
     * @throws IllegalArgumentException if topic is null
     */
    public void subscribe(String topic) throws TransportException
    {
        synchronized (this.stateLock)
        {
            try
            {
                if (topic == null)
                {
                    throw new IllegalArgumentException("Topic cannot be null");
                }
                else if (!this.mqttAsyncClient.isConnected())
                {

                    TransportException transportException = new TransportException("Cannot subscribe when mqtt client is disconnected");
                    transportException.setRetryable(true);
                    throw transportException;
                }

                log.debug("Sending MQTT SUBSCRIBE packet for topic {}", topic);

                IMqttToken subToken = this.mqttAsyncClient.subscribe(topic, QOS);

                subToken.waitForCompletion(MAX_SUBSCRIBE_ACK_WAIT_TIME);
                log.debug("Sent MQTT SUBSCRIBE packet for topic {} was acknowledged", topic);

            }
            catch (MqttException e)
            {
                log.warn("Encountered exception while sending MQTT SUBSCRIBE packet for topic {}", topic, e);

                throw PahoExceptionTranslator.convertToMqttException(e, "Unable to subscribe to topic :" + topic);
            }
        }
    }

    /**
     * Method to receive messages for telemetry on mqtt broker connection.
     *
     * @return a received message. It can be {@code null}
     */
    public IotHubTransportMessage receiveTelemetryMessage()
    {
        synchronized (this.receivedMessagesLock)
        {
            Pair<String, MqttMessage> messagePair = this.receivedMessages.peek();
            if (messagePair != null)
            {
                String topic = messagePair.getKey();
                if (topic != null)
                {
                    MqttMessage message = messagePair.getValue();
                    if (message != null)
                    {
                        // remove this message from the queue as this is the correct handler
                        this.receivedMessages.poll();

                        return constructMessage(message, topic);
                    }
                    else
                    {
                        log.warn("Data cannot be null when topic is non-null");
                    }
                }
                else
                {
                    return null;
                }
            }

            return null;
        }
    }

    /**
     * Method to receive messages for direct method on mqtt broker connection.
     */
    public IotHubTransportMessage receiveDirectMethodMessage()
    {
        synchronized (this.receivedMessagesLock)
        {
            IotHubTransportMessage message = null;

            Pair<String, MqttMessage> messagePair = this.receivedMessages.peek();

            if (messagePair != null)
            {
                String topic = messagePair.getKey();

                if (topic != null && topic.length() > 0)
                {
                    MqttMessage mqttMessage = messagePair.getValue();
                    byte[] data = mqttMessage.getPayload();

                    if (isReceivingFromCorrectDirectMethodTopic(topic))
                    {
                        //remove this message from the queue as this is the correct handler
                        this.receivedMessages.poll();

                        //parse the topic string to get information (method name, request Id, etc.) further
                        TopicParser topicParser = new TopicParser(topic);

                        if (data != null && data.length > 0)
                        {
                            message = new IotHubTransportMessage(data, MessageType.DEVICE_METHODS);
                        }
                        else
                        {
                            message = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_METHODS);
                        }

                        message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_UNKNOWN);
                        message.setQualityOfService(mqttMessage.getQos());

                        String methodName = this.isConnectingToMqttGateway ?
                                topicParser.getMethodName(4) :
                                topicParser.getMethodName(3);
                        message.setMethodName(methodName);

                        String reqId = this.isConnectingToMqttGateway ?
                                topicParser.getRequestId(5) :
                                topicParser.getRequestId(4);
                        if (reqId != null)
                        {
                            message.setRequestId(reqId);

                            message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);
                        }
                        else
                        {
                            log.warn("Request ID cannot be null");
                        }
                    }
                }
            }

            return message;
        }
    }

    /**
     * Method to receive messages for twin on mqtt broker connection.
     */
    public IotHubTransportMessage receiveTwinMessage()
    {
        synchronized (this.receivedMessagesLock)
        {
            IotHubTransportMessage message = null;

            Pair<String, MqttMessage> messagePair = this.receivedMessages.peek();

            if (messagePair != null)
            {
                String topic = messagePair.getKey();

                if (topic != null && topic.length() > 0)
                {
                    if (topic.length() > TWIN.length() && topic.startsWith(TWIN))
                    {
                        MqttMessage mqttMessage = messagePair.getValue();
                        byte[] data = mqttMessage.getPayload();

                        //remove this message from the queue as this is the correct handler
                        this.receivedMessages.poll();

                        if (isReceivingFromCorrectTwinTopic(topic, "res"))
                        {
                            // Tokenize on backslash
                            String[] topicTokens = topic.split(Pattern.quote("/"));
                            if (data != null && data.length > 0)
                            {
                                message = new IotHubTransportMessage(data, MessageType.DEVICE_TWIN);
                            }
                            else
                            {
                                message = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_TWIN); // empty body
                            }

                            message.setQualityOfService(mqttMessage.getQos());

                            message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_UNKNOWN);

                            // Case for $iothub/twin/res/{status}/?$rid={request id}&$version={new version} or
                            // $iothub/twin/{clientId}/res/{status}/?$rid={request id}&$version={new version},
                            // depending on the value of isConnectingToMqttGateway.
                            int statusToken = this.isConnectingToMqttGateway ? 4 : 3;

                            if (topicTokens.length > statusToken)
                            {
                                message.setStatus(getStatus(topicTokens[statusToken]));
                            }
                            else
                            {
                                log.warn("Message received without status");
                            }

                            int reqIdToken = this.isConnectingToMqttGateway ? 5 : 4;
                            if (topicTokens.length > reqIdToken)
                            {
                                String requestId = getRequestId(topicTokens[reqIdToken]);
                                // MQTT does not have the concept of correlationId for request/response handling but it does have a requestId
                                // To handle this we are setting the correlationId to the requestId to better handle correlation
                                // whether we use MQTT or AMQP.
                                message.setRequestId(requestId);
                                message.setCorrelationId(requestId);
                                if (twinRequestMap.containsKey(requestId))
                                {
                                    switch (twinRequestMap.remove(requestId))
                                    {
                                        case DEVICE_OPERATION_TWIN_GET_REQUEST:
                                            message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_RESPONSE);
                                            break;
                                        case DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST:
                                            message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE);
                                            break;
                                        default:
                                            message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_UNKNOWN);
                                    }
                                }
                                else
                                {
                                    log.warn("Request ID cannot be null");
                                }
                            }

                            int versionToken = this.isConnectingToMqttGateway ? 5 : 4;
                            if (topicTokens.length > versionToken)
                            {
                                String version = getVersion(topicTokens[versionToken]);
                                if (version != null && !version.isEmpty())
                                {
                                    message.setVersion(Integer.parseInt(version));
                                }
                            }
                        }
                        else if (isReceivingFromCorrectTwinTopic(topic, "patch"))
                        {
                            if (data != null)
                            {
                                message = new IotHubTransportMessage(data, MessageType.DEVICE_TWIN);
                                message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);
                            }

                            // Case for $iothub/twin/PATCH/properties/desired/?$version={new version} or
                            // $iothub/twin/{clientId}/PATCH/properties/desired/?$version={new version},
                            // depending on the value of isConnectingToMqttGateway.

                            // Tokenize on backslash
                            String[] topicTokens = topic.split(Pattern.quote("/"));

                            int patchVersionToken = this.isConnectingToMqttGateway ? 6 : 5;
                            if (topicTokens.length > patchVersionToken)
                            {
                                if (message != null)
                                {
                                    message.setVersion(Integer.parseInt(getVersion(topicTokens[patchVersionToken])));
                                }
                            }
                        }
                    }
                }
            }

            return message;
        }
    }

    /**
     * Send ack for the provided message.
     * @param messageId The message id to send the ack for
     * @throws TransportException If the ack fails to be sent
     */
    void sendMessageAcknowledgement(int messageId) throws TransportException
    {
        log.trace("Sending mqtt ack for received message with mqtt message id {}", messageId);
        try
        {
            this.mqttAsyncClient.messageArrivedComplete(messageId, QOS);
        }
        catch (MqttException e)
        {
            throw PahoExceptionTranslator.convertToMqttException(e, "Error sending message ack");
        }
    }

    /**
     * Sends the provided telemetry message over the mqtt connection
     *
     * @param message the message to send
     * @throws TransportException if any exception is encountered while sending the message
     */
    public void sendTelemetryMessage(Message message) throws TransportException
    {
        if (message == null || message.getBytes() == null)
        {
            throw new IllegalArgumentException("Message cannot be null");
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.telemetryPublishTopic);

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

        if (this.moduleId != null && !this.moduleId.isEmpty())
        {
            stringBuilder.append("/");
        }

        String messagePublishTopic = stringBuilder.toString();

        this.publish(messagePublishTopic, message);
    }

    /**
     * Sends the provided direct method message over the mqtt connection
     *
     * @param message the message to send
     * @throws TransportException if any exception is encountered while sending the message
     * @throws IllegalArgumentException if the provided message is null or has a null body
     */
    public void sendDirectMethodMessage(final IotHubTransportMessage message) throws TransportException, IllegalArgumentException
    {
        if (message == null || message.getBytes() == null)
        {
            throw new IllegalArgumentException("Message cannot be null");
        }

        if (!isStarted)
        {
            throw new TransportException("Start device method before using send");
        }

        if (message.getMessageType() != MessageType.DEVICE_METHODS)
        {
            return;
        }

        switch (message.getDeviceOperationType())
        {
            case DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST:
            {
                this.subscribe(methodSubscribeTopic);
                break;
            }
            case DEVICE_OPERATION_METHOD_SEND_RESPONSE:
            {
                if (message.getRequestId() == null || message.getRequestId().isEmpty())
                {
                    throw new IllegalArgumentException("Request id cannot be null or empty");
                }

                String format = "%s/%s/?$rid=%s";
                // "$iothub/methods/res/{status}?$rid={request Id}" if connecting to an IoT hub or Edge hub,
                // while "$iothub/methods/{clientId}/res/{status}?$rid={request Id}" if connecting to E4k.
                String topic = String.format(format, this.methodResponseTopic, message.getStatus(), message.getRequestId());

                this.publish(topic, message);
                break;
            }
            default:
            {
                throw new TransportException("Mismatched device method operation");
            }
        }
    }

    /**
     * Sends the provided device twin message over the mqtt connection
     *
     * @param message the message to send
     * @throws TransportException if any exception is encountered while sending the message
     */
    public void sendTwinMessage(final IotHubTransportMessage message) throws TransportException
    {
        if (message == null || message.getBytes() == null)
        {
            throw new IllegalArgumentException("Message cannot be null");
        }

        if (!this.isStarted)
        {
            throw new IllegalStateException("Start device twin before using it");
        }

        if (message.getMessageType() != MessageType.DEVICE_TWIN)
        {
            return;
        }

        String publishTopic = buildTopic(message);
        twinRequestMap.put(message.getRequestId(), message.getDeviceOperationType());

        if (message.getDeviceOperationType() == DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST)
        {
            // Subscribe to "$iothub/twin/{clientId}/PATCH/properties/desired/#" if connecting to E4K,
            // or "$iothub/twin/PATCH/properties/desired/#" if not.
            String subscribeTopic = this.isConnectingToMqttGateway ?
                String.format(TWIN_PATCH_E4K, this.clientId) + "/properties/desired/#" :
                TWIN_PATCH_HUB + "/properties/desired/#";

            this.subscribe(subscribeTopic);
        }
        else
        {
            this.publish(publishTopic, message);
        }
    }

    private String buildTopic(final IotHubTransportMessage message)
    {
        String topic = "";
        switch (message.getDeviceOperationType())
        {
            case DEVICE_OPERATION_TWIN_GET_REQUEST:
            {
                // Building $iothub/twin/{clientId}/GET/?$rid={request id} if connecting to E4K,
                // or $iothub/twin/GET/?$rid={request id} if not.
                topic += isConnectingToMqttGateway ?
                    String.format(TWIN_GET_E4K, this.clientId) :
                    TWIN_GET_HUB;

                String reqid = message.getRequestId();
                if (reqid != null && reqid.length() > 0)
                {
                    topic += "/?$rid=" + reqid;
                }
                else
                {
                    throw new IllegalArgumentException("Request Id is Mandatory");
                }
                break;
            }
            case DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST:
            {
                // Building $iothub/twin/{clientId}/PATCH/properties/reported/?$rid={request id}&$version={base version} if connecting to E4K,
                // or $iothub/twin/PATCH/properties/reported/?$rid={request id}&$version={base version} if not.
                topic += isConnectingToMqttGateway ?
                    String.format(TWIN_PATCH_E4K, this.clientId) :
                    TWIN_PATCH_HUB;

                topic += "/properties/reported";

                String reqid = message.getRequestId();
                if (reqid != null && reqid.length() > 0)
                {
                    topic += "/?$rid=" + message.getRequestId();
                }
                else
                {
                    throw new IllegalArgumentException("Request Id is Mandatory");
                }

                topic += "&$version=" + message.getVersion();
                break;
            }
            case DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST:
            {
                // Building $iothub/twin/{clientId}/PATCH/properties/desired/?$version={new version} if connecting to E4K,
                // or $iothub/twin/PATCH/properties/desired/?$version={new version} if not
                topic += isConnectingToMqttGateway ?
                    String.format(TWIN_PATCH_E4K, this.clientId) :
                    TWIN_PATCH_HUB;

                topic += "/properties/desired/?$version=" + message.getVersion();
                break;

            }
            default:
            {
                throw new UnsupportedOperationException("Device Twin Operation is not supported - " + message.getDeviceOperationType());
            }
        }

        return topic;
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
    private IotHubTransportMessage constructMessage(MqttMessage mqttMessage, String topic)
    {
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

    private boolean isReceivingFromCorrectDirectMethodTopic(String topic)
    {
        if (!this.isConnectingToMqttGateway)
        {
            return topic.length() > DIRECT_METHOD_POST_HUB.length() && topic.startsWith(DIRECT_METHOD_POST_HUB);
        }

        String e4kReceivingTopic = String.format(DIRECT_METHOD_POST_E4K, this.clientId);
        return topic.length() > e4kReceivingTopic.length() && topic.startsWith(e4kReceivingTopic);
    }

    private boolean isReceivingFromCorrectTwinTopic(String topic, String operation)
    {
        switch (operation)
        {
            case "res":
            {
                if (!this.isConnectingToMqttGateway)
                {
                    return topic.length() > TWIN_RES_HUB.length() && topic.startsWith(TWIN_RES_HUB);
                }
                else
                {
                    String e4kReceivingTopic = String.format(TWIN_RES_E4K, this.clientId);
                    return topic.length() > e4kReceivingTopic.length() && topic.startsWith(e4kReceivingTopic);
                }
            }
            case "patch":
            {
                if (!this.isConnectingToMqttGateway)
                {
                    return topic.length() > TWIN_PATCH_HUB.length() && topic.startsWith(TWIN_PATCH_HUB + "/properties/desired");
                }
                else
                {
                    String e4kReceivingTopic = String.format(TWIN_PATCH_E4K, this.clientId);
                    return topic.length() > e4kReceivingTopic.length() && topic.startsWith(e4kReceivingTopic + "/properties/desired");
                }
            }
            default:
            {
                return false;
            }
        }
    }

    private String getVersion(String token)
    {
        String version = null;

        if (token.contains(VERSION)) //restriction for version
        {
            int startIndex = token.indexOf(VERSION) + VERSION.length();
            int endIndex = token.length();

            version = token.substring(startIndex, endIndex);
        }

        return version;
    }

    private String getRequestId(String token)
    {
        String reqId = null;

        if (token.contains(REQ_ID)) // restriction for request id
        {
            int startIndex = token.indexOf(REQ_ID) + REQ_ID.length();
            int endIndex = token.length();

            if (token.contains(VERSION))
            {
                endIndex = token.indexOf(VERSION) - 1;
            }

            reqId = token.substring(startIndex, endIndex);
        }

        return reqId;
    }

    private String getStatus(String token)
    {
        if (token != null && token.matches("\\d{3}")) // 3 digit number
        {
            return token;
        }
        else
        {
            throw new IllegalArgumentException("Status could not be parsed");
        }
    }
}
