package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.ReceivedMqttMessage;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.TransportException;
import com.microsoft.azure.sdk.iot.device.twin.DeviceOperations;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class MessageParser
{
    private final static String MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_ENCODED = "%24";

    //Placement for $iothub/methods/POST/{method name}/?$rid={request id}
    private static final int METHOD_TOKEN = 3;
    private static final int METHODS_REQID_TOKEN = 4;

    //Placement in $iothub/twin/res/{status}/?$rid={request id}&$version={new version}
    private static final int STATUS_TOKEN = 3;
    private static final int TWIN_REQID_TOKEN = 4;
    private static final int VERSION_TOKEN = 4;

    //Placement for $iothub/twin/PATCH/properties/desired/?$version={new version}
    private static final int PATCH_VERSION_TOKEN = 5;

    private static final String TWIN_REQ_ID = "?$rid=";

    private static final String VERSION = "$version=";

    private final static String INPUTS_PATH_STRING = "inputs";
    private final static String MODULES_PATH_STRING = "modules";

    static IotHubTransportMessage ConstructDirectMethodMessage(ReceivedMqttMessage mqttMessage)
    {
        byte[] data = mqttMessage.getPayload();
        IotHubTransportMessage message;
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

        String methodName = TopicParser.getMethodName(mqttMessage.getTopic(), METHOD_TOKEN);
        message.setMethodName(methodName);

        String reqId = TopicParser.getRequestId(mqttMessage.getTopic(), METHODS_REQID_TOKEN);
        if (reqId != null)
        {
            message.setRequestId(reqId);

            message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);
        }
        else
        {
            log.warn("Request ID cannot be null");
        }

        return message;
    }

    static IotHubTransportMessage ConstructDesiredPropertiesUpdateMessage(ReceivedMqttMessage receivedMqttMessage)
    {
        IotHubTransportMessage message = new IotHubTransportMessage(receivedMqttMessage.getPayload(), MessageType.DEVICE_TWIN);
        message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);

        // Case for $iothub/twin/PATCH/properties/desired/?$version={new version}
        // Tokenize on backslash
        String[] topicTokens = receivedMqttMessage.getTopic().split(Pattern.quote("/"));
        if (topicTokens.length > PATCH_VERSION_TOKEN)
        {
            message.setVersion(Integer.parseInt(GetVersion(topicTokens[PATCH_VERSION_TOKEN])));
        }

        return message;
    }

    static IotHubTransportMessage ConstructTwinResponseMessage(ReceivedMqttMessage receivedMqttMessage, Map<String, DeviceOperations> twinRequestMap)
    {
        IotHubTransportMessage message;
        byte[] data = receivedMqttMessage.getPayload();

        // Tokenize on backslash
        String[] topicTokens = receivedMqttMessage.getTopic().split(Pattern.quote("/"));
        if (data != null && data.length > 0)
        {
            message = new IotHubTransportMessage(data, MessageType.DEVICE_TWIN);
        }
        else
        {
            // Case for $iothub/twin/res/{status}/?$rid={request id}
            message = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_TWIN); // empty body
        }

        message.setQualityOfService(receivedMqttMessage.getQos());

        message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_UNKNOWN);

        // Case for $iothub/twin/res/{status}/?$rid={request id}&$version={new version}
        if (topicTokens.length > STATUS_TOKEN)
        {
            message.setStatus(GetStatus(topicTokens[STATUS_TOKEN]));
        }
        else
        {
            log.warn("Message received without status");
        }

        if (topicTokens.length > TWIN_REQID_TOKEN)
        {
            String[] queryStringKeyValuePairs = topicTokens[TWIN_REQID_TOKEN].split(Pattern.quote("&"));
            String requestId = GetRequestId(queryStringKeyValuePairs[0]);

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

        if (topicTokens.length > VERSION_TOKEN)
        {
            String version = GetVersion(topicTokens[VERSION_TOKEN]);
            if (version != null && !version.isEmpty())
            {
                message.setVersion(Integer.parseInt(version));
            }
        }

        return message;
    }

    static String GetVersion(String token)
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

    static String GetRequestId(String token)
    {
        String reqId = null;

        if (token.contains(TWIN_REQ_ID)) // restriction for request id
        {
            int startIndex = token.indexOf(TWIN_REQ_ID) + TWIN_REQ_ID.length();
            int endIndex = token.length();

            reqId = token.substring(startIndex, endIndex);
        }

        return reqId;
    }

    static String GetStatus(String token)
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

    // Converts an MQTT message into our native "IoT hub" message
    static IotHubTransportMessage ConstructTelemetryMessage(ReceivedMqttMessage mqttMessage)
    {
        String topic = mqttMessage.getTopic();

        IotHubTransportMessage message = new IotHubTransportMessage(mqttMessage.getPayload(), MessageType.DEVICE_TELEMETRY);

        message.setQualityOfService(mqttMessage.getQos());

        int propertiesStringStartingIndex = topic.indexOf(MESSAGE_SYSTEM_PROPERTY_IDENTIFIER_ENCODED);
        if (propertiesStringStartingIndex != -1)
        {
            String propertiesString = topic.substring(propertiesStringStartingIndex);

            TopicParser.AssignPropertiesToMessage(message, propertiesString);

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
}
