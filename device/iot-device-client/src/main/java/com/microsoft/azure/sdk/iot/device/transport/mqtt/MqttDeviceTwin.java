// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubServiceException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class MqttDeviceTwin extends Mqtt
{
    private String subscribeTopic;
    private final Map<String, DeviceOperations> requestMap = new HashMap<>();
    private boolean isStarted = false;

    private final String BACKSLASH = "/";
    private final String AND = "&";
    private final String QUESTION = "?";
    private final String POUND = "#";

    private final String TWIN = "$iothub/twin";
    private final String GET = TWIN + BACKSLASH + "GET";
    private final String RES = TWIN + BACKSLASH + "res";
    private final String PATCH = TWIN + BACKSLASH + "PATCH";
    private final String PROPERTIES = "properties";
    private final String DESIRED = "desired";
    private final String REPORTED = "reported";
    private final String REQ_ID = QUESTION + "$rid=";
    private final String VERSION = "$version=";

    //Placement in $iothub/twin/res/{status}/?$rid={request id}&$version={new version}
    private final int RES_TOKEN = 2;
    private final int STATUS_TOKEN = 3;
    private final int REQID_TOKEN = 4;
    private final int VERSION_TOKEN = 4;

    //Placement for $iothub/twin/PATCH/properties/desired/?$version={new version}
    private final int PATCH_TOKEN = 2;
    private final int PROPERTIES_TOKEN = 3;
    private final int DESIRED_TOKEN = 4;
    private final int PATCH_VERSION_TOKEN = 5;

    public MqttDeviceTwin(MqttConnection mqttConnection, String connectionId, Map<Integer, Message> unacknowledgedSentMessages) throws TransportException
    {
        //Codes_SRS_MQTTDEVICETWIN_25_001: [The constructor shall instantiate super class without any parameters.]
        super(mqttConnection, null, null, connectionId, unacknowledgedSentMessages);

        //Codes_SRS_MQTTDEVICETWIN_25_002: [The constructor shall construct device twin response subscribeTopic.]
        this.subscribeTopic = RES + BACKSLASH + POUND;
    }

    public void start() throws TransportException
    {
        if (!isStarted)
        {
            //Codes_SRS_MQTTDEVICETWIN_25_019: [start method shall subscribe to twin response topic ($iothub/twin/res/#) if connected and throw TransportException otherwise.]
            this.subscribe(subscribeTopic);
            isStarted = true;
        }
    }

    public void stop()
    {
        isStarted = false;

        if (!requestMap.isEmpty())
        {
            log.trace("Pending {} responses from IotHub yet unsubscribed", requestMap.size());
        }
    }

    private String buildTopic(final IotHubTransportMessage message) throws TransportException
    {
        StringBuilder topic = new StringBuilder();
        switch (message.getDeviceOperationType())
        {
            case DEVICE_OPERATION_TWIN_GET_REQUEST:
            {
                //Building $iothub/twin/GET/?$rid={request id}
                //Codes_SRS_MQTTDEVICETWIN_25_024: [send method shall build the get request topic of the format mentioned in spec ($iothub/twin/GET/?$rid={request id}) if the operation is of type DEVICE_OPERATION_TWIN_GET_REQUEST.]
                topic.append(GET);

                String reqid = message.getRequestId();
                if (reqid != null && reqid.length() > 0)
                {
                    topic.append(BACKSLASH);
                    topic.append(REQ_ID);
                    topic.append(reqid);
                }
                else
                {
                    //Codes_SRS_MQTTDEVICETWIN_25_025: [send method shall throw an IllegalArgumentException if message contains a null or empty request id if the operation is of type DEVICE_OPERATION_TWIN_GET_REQUEST.]
                    throw new IllegalArgumentException("Request Id is Mandatory");
                }
                break;
            }
            case DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST:
            {
                // Building $iothub/twin/PATCH/properties/reported/?$rid={request id}&$version={base version}
                //Codes_SRS_MQTTDEVICETWIN_25_026: [send method shall build the update reported properties request topic of the format mentioned in spec ($iothub/twin/PATCH/properties/reported/?$rid={request id}&$version={base version}) if the operation is of type DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST.]
                topic.append(PATCH);
                topic.append(BACKSLASH);
                topic.append(PROPERTIES);
                topic.append(BACKSLASH);
                topic.append(REPORTED);

                String reqid = message.getRequestId();
                if (reqid != null && reqid.length() > 0)
                {
                    topic.append(BACKSLASH);
                    topic.append(REQ_ID);
                    topic.append(message.getRequestId());
                }
                else
                {
                    //Codes_SRS_MQTTDEVICETWIN_25_027: [send method shall throw an IllegalArgumentException if message contains a null or empty request id if the operation is of type DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST.]
                    throw new IllegalArgumentException("Request Id is Mandatory");
                }

                String version = message.getVersion();

                //Codes_SRS_MQTTDEVICETWIN_25_028: [send method shall not throw an exception if message contains a null or empty version if the operation is of type DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST as version is optional]
                if (version != null)
                {
                    topic.append(AND);
                    topic.append(VERSION);
                    topic.append(version);
                }
                break;
            }
            case DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST:
            {
                // Building $iothub/twin/PATCH/properties/desired/?$version={new version}
                //Codes_SRS_MQTTDEVICETWIN_25_029: [send method shall build the subscribe to desired properties request topic of the format mentioned in spec ($iothub/twin/PATCH/properties/desired/?$version={new version}) if the operation is of type DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.]
                topic.append(PATCH);
                topic.append(BACKSLASH);
                topic.append(PROPERTIES);
                topic.append(BACKSLASH);
                topic.append(DESIRED);

                String version = message.getVersion();
                if (version != null)
                {
                    topic.append(BACKSLASH);
                    topic.append(QUESTION);
                    topic.append(VERSION);
                    topic.append(version);
                }
                break;

            }
            default:
            {
                //Codes_SRS_MQTTDEVICETWIN_25_023: [send method shall throw an exception if the getDeviceOperationType() returns DEVICE_OPERATION_UNKNOWN.]
                throw new UnsupportedOperationException("Device Twin Operation is not supported - " + message.getDeviceOperationType());
            }
        }

        return topic.toString();
    }

    /**
     * Sends the provided device twin message over the mqtt connection
     *
     * @param message the message to send
     * @throws TransportException if any exception is encountered while sending the message
     */
    public void send(final IotHubTransportMessage message) throws TransportException
    {
        if (message == null || message.getBytes() == null)
        {
            //Codes_SRS_MQTTDEVICETWIN_25_021: [send method shall throw an IllegalArgumentException if the message is null.]
            throw new IllegalArgumentException("Message cannot be null");
        }

        if(!this.isStarted)
        {
            throw new IllegalStateException("Start device twin before using it");
        }

        if (message.getMessageType() != MessageType.DEVICE_TWIN)
        {
            //Codes_SRS_MQTTDEVICETWIN_25_022: [send method shall return if the message is not of Type DEVICE_TWIN.]
            return;
        }

        String publishTopic = buildTopic(message);
        requestMap.put(message.getRequestId(), message.getDeviceOperationType());

        //Codes_SRS_MqttMessaging_25_024: [send method shall publish a message to the IOT Hub on the publish topic by calling method publish().]
        if (message.getDeviceOperationType() == DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST)
        {
            // Subscribe to "$iothub/twin/PATCH/properties/desired/#"
            //Codes_SRS_MQTTDEVICETWIN_25_032: [send method shall subscribe to desired properties by calling method subscribe() on topic "$iothub/twin/PATCH/properties/desired/#" specified in spec if the operation is DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.]
            String subscribeTopic = PATCH +
                    BACKSLASH +
                    PROPERTIES +
                    BACKSLASH +
                    DESIRED +
                    BACKSLASH +
                    POUND;

            //Codes_SRS_MQTTDEVICETWIN_25_032: [send method shall subscribe to desired properties by calling method subscribe() on topic "$iothub/twin/PATCH/properties/desired/#" specified in spec if the operation is DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.]
            this.subscribe(subscribeTopic);
        }
        else
        {
            //Codes_SRS_MQTTDEVICETWIN_25_031: [send method shall publish a message to the IOT Hub on the respective publish topic by calling method publish().]
            this.publish(publishTopic, message);
        }
    }

    private String getStatus(String token) throws TransportException
    {
        String status = null;

        if (token != null && token.matches("\\d{3}")) // 3 digit number
        {
            status = token;
        }
        else
        {
            //Codes_SRS_MQTTDEVICETWIN_25_039: [If the topic is of type response topic and if status is either a non 3 digit number or not found then receive shall throw TransportException]
            this.throwDeviceTwinTransportException("Status could not be parsed");
        }

        return status;
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

    @Override
    public IotHubTransportMessage receive() throws TransportException
    {
        synchronized (this.incomingLock)
        {
            IotHubTransportMessage message = null;

            // Codes_SRS_MQTTDEVICETWIN_25_035: [This method shall call peekMessage to get the message payload from the received Messages queue corresponding to the messaging client's operation.]
            Pair<String, byte[]> messagePair = peekMessage();

            if (messagePair != null)
            {
                String topic = messagePair.getKey();

                if (topic != null && topic.length() > 0)
                {
                    if (topic.length() > TWIN.length() && topic.startsWith(TWIN))
                    {
                        byte[] data = messagePair.getValue();

                        //remove this message from the queue as this is the correct handler
                        allReceivedMessages.poll();

                        if (topic.length() > RES.length() && topic.startsWith(RES))
                        {
                            // Tokenize on backslash
                            String[] topicTokens = topic.split(Pattern.quote("/"));
                            if (data != null && data.length > 0)
                            {
                                //Codes_SRS_MQTTDEVICETWIN_25_044: [If the topic is of type response then this method shall set data and operation type as DEVICE_OPERATION_TWIN_GET_RESPONSE if data is not null]
                                message = new IotHubTransportMessage(data, MessageType.DEVICE_TWIN);
                                message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_UNKNOWN);
                            }
                            else
                            {
                                // Case for $iothub/twin/res/{status}/?$rid={request id}
                                //Codes_SRS_MQTTDEVICETWIN_25_045: [If the topic is of type response then this method shall set empty data and operation type as DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE if data is null or empty]
                                message = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_TWIN); // empty body
                                message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_UNKNOWN);

                            }

                            // Case for $iothub/twin/res/{status}/?$rid={request id}&$version={new version}
                            if (topicTokens.length > STATUS_TOKEN)
                            {
                                //Codes_SRS_MQTTDEVICETWIN_25_038: [If the topic is of type response topic then this method shall parse further for status and set it for the message by calling setStatus for the message]
                                message.setStatus(getStatus(topicTokens[STATUS_TOKEN]));
                            }
                            else
                            {
                                this.throwDeviceTwinTransportException(new IotHubServiceException("Message received without status"));
                            }

                            if (topicTokens.length > REQID_TOKEN)
                            {
                                //Codes_SRS_MQTTDEVICETWIN_25_040: [If the topic is of type response topic then this method shall parse further to look for request id which if found is set by calling setRequestId]
                                String requestId = getRequestId(topicTokens[REQID_TOKEN]);
                                message.setRequestId(requestId);
                                if (requestMap.containsKey(requestId))
                                {
                                    switch (requestMap.remove(requestId))
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
                                    this.throwDeviceTwinTransportException(new UnsupportedOperationException("Request Id is mandatory"));
                                }
                            }

                            if (topicTokens.length > VERSION_TOKEN)
                            {
                                //Codes_SRS_MQTTDEVICETWIN_25_041: [If the topic is of type response topic then this method shall parse further to look for version which if found is set by calling setVersion]
                                message.setVersion(getVersion(topicTokens[VERSION_TOKEN]));
                            }
                        }
                        else if (topic.length() > PATCH.length() && topic.startsWith(PATCH))
                        {
                            if (topic.startsWith(PATCH + BACKSLASH + PROPERTIES + BACKSLASH + DESIRED))
                            {
                                if (data != null)
                                {
                                    //Codes_SRS_MQTTDEVICETWIN_25_046: [If the topic is of type patch for desired properties then this method shall set the data and operation type as DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE if data is not null or empty]
                                    message = new IotHubTransportMessage(data, MessageType.DEVICE_TWIN);
                                    message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);
                                }
                                else
                                {

                                    //Codes_SRS_MQTTDEVICETWIN_25_047: [If the topic is of type patch for desired properties then this method shall throw TransportException if data is null or empty]
                                    this.throwDeviceTwinTransportException(new UnsupportedOperationException());
                                }

                                // Case for $iothub/twin/PATCH/properties/desired/?$version={new version}
                                // Tokenize on backslash
                                String[] topicTokens = topic.split(Pattern.quote("/"));
                                if (topicTokens.length > PATCH_VERSION_TOKEN)
                                {
                                    if (message != null)
                                    {
                                        //Codes_SRS_MQTTDEVICETWIN_25_042: [If the topic is of type patch for desired properties then this method shall parse further to look for version which if found is set by calling setVersion]
                                        message.setVersion(getVersion(topicTokens[PATCH_VERSION_TOKEN]));
                                    }
                                }
                            }
                            else
                            {
                                //Codes_SRS_MQTTDEVICETWIN_25_043: [If the topic is not of type response for desired properties then this method shall throw TransportException]
                                this.throwDeviceTwinTransportException(new UnsupportedOperationException());
                            }
                        }
                        else
                        {
                            //Codes_SRS_MQTTDEVICETWIN_25_037: [This method shall parse topic to look for only either twin response topic or twin patch topic and thorw TransportException other wise.]
                            this.throwDeviceTwinTransportException(new UnsupportedOperationException());
                        }
                    }
                }
            }

            // Codes_SRS_MQTTDEVICETWIN_34_034: [If the call peekMessage returns null or empty string then this method shall do nothing and return null]
            return message;
        }
    }

    private void throwDeviceTwinTransportException(String message) throws TransportException
    {
        TransportException transportException = new TransportException(message);
        transportException.setIotHubService(TransportException.IotHubService.TWIN);
        throw transportException;
    }

    private void throwDeviceTwinTransportException(Exception e) throws TransportException
    {
        TransportException transportException = new TransportException(e);
        transportException.setIotHubService(TransportException.IotHubService.TWIN);
        throw transportException;
    }
}
