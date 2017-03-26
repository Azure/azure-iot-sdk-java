// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.CustomLogger;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceTwinMessage;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MqttDeviceTwin extends Mqtt
{
    private String subscribeTopic;
    private Map<String, DeviceOperations> requestMap = new HashMap<>();
    private boolean isStarted = false;
    private CustomLogger logger = new CustomLogger(this.getClass());

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

    public MqttDeviceTwin() throws IOException
    {
        /*
        **Codes_SRS_MQTTDEVICETWIN_25_001: [**The constructor shall instantiate super class without any parameters.**]**
         */
        super();
        /*
        **Codes_SRS_MQTTDEVICETWIN_25_002: [**The constructor shall construct device twin response subscribeTopic.**]**
         */
        this.subscribeTopic = RES + BACKSLASH + POUND;

    }

    public void start() throws IOException
    {
        if (!isStarted)
        {
            /*
            **Codes_SRS_MQTTDEVICETWIN_25_019: [**start method shall subscribe to twin response topic ($iothub/twin/res/#) if connected and throw IoException otherwise.**]**
             */
            this.subscribe(subscribeTopic);
            isStarted = true;
        }
    }

    public void stop() throws IOException
    {
        /*
        **Codes_SRS_MQTTDEVICETWIN_25_020: [**stop method shall unsubscribe from twin response topic ($iothub/twin/res/#) and throw IoException otherwise.**]**
         */
        if (isStarted)
        {
            this.unsubscribe(subscribeTopic);
            isStarted = false;
        }

        if (!requestMap.isEmpty())
        {
            logger.LogInfo("Pending %d responses from IotHub yet unsubscribed %s", requestMap.size(), logger.getMethodName());
        }
    }

    @Override
    String parseTopic() throws IOException
    {
        /*
        **Codes_SRS_MQTTDEVICETWIN_25_003: [**parseTopic concrete method shall be implemeted by MqttDeviceTwin concrete class.**]**
         */
        String topic = null;

        if (allReceivedMessages == null)
        {
            /*
            **Codes_SRS_MQTTDEVICETWIN_25_007: [**If receiveMessage queue is null then parseTopic shall throw IOException.**]**
             */
            throw new IOException("Queue cannot be null");
        }

        /*
        **Codes_SRS_MQTTDEVICETWIN_25_006: [**If received messages queue is empty then parseTopic shall return null string.**]**
         */
        if (!allReceivedMessages.isEmpty())
        {

            for (Map.Entry<String, byte[]> data : allReceivedMessages.entrySet())
            {
                String topicFound = data.getKey();

               /*
               **Codes_SRS_MQTTDEVICETWIN_25_004: [**parseTopic shall look for the twin topic($iothub/twin) prefix from received message queue as per spec.**]**
                */
               /*
                **Codes_SRS_MQTTDEVICETWIN_25_005: [**If none of the topics from the received queue match the twin topic prefix then this method shall return null string .**]**
                */
                if (topicFound != null && topicFound.length() > TWIN.length() && topicFound.startsWith(TWIN))
                {
                    topic = topicFound;
                    break;
                }
            }
        }
        return topic;

    }

    @Override
    byte[] parsePayload(String topic) throws IOException
    {
        /*
            This method is called only when you are certain that there is a message in the queue meant for device messaging that needs to be retrieved and then deleted.
         */
        /*
        **Codes_SRS_MQTTDEVICETWIN_25_008: [**parsePayload concrete method shall be implemeted by MqttDeviceTwin concrete class.**]**
         */

        if (topic == null)
        {
            /*
            **Codes_SRS_MQTTDEVICETWIN_25_010: [**If the topic is null then parsePayload shall stop parsing for payload and return.**]**
             */
            return null;
        }
        if (allReceivedMessages == null)
        {
            /*
            **Codes_SRS_MQTTDEVICETWIN_25_012: [**If receiveMessage queue is null then this method shall throw IOException.**]**
             */
            throw new IOException("Invalid State - topic is not null and could not be found in queue");
        }

        if (!allReceivedMessages.containsKey(topic))
        {
            /*
            **Codes_SRS_MQTTDEVICETWIN_25_011: [**If the topic is non-null and received messagesqueue could not locate the payload then this method shall throw IOException**]**
             */
            throw new IOException("Topic should be present in received queue at this point");
        }

        /*
        **Codes_SRS_MQTTDEVICETWIN_25_009: [**This parsePayload method look for payload for the corresponding topic from the received messagesqueue.**]**
         */
        if (!allReceivedMessages.isEmpty())
        {
            /*
            **Codes_SRS_MQTTDEVICETWIN_25_013: [**If the topic is found in the message queue then parsePayload shall delete it from the queue.**]**
             */
            return allReceivedMessages.remove(topic);
        }

        return null;
    }

    private String buildTopic(final DeviceTwinMessage message) throws IOException
    {
        StringBuilder topic = new StringBuilder();
        switch (message.getDeviceOperationType())
        {
            case DEVICE_OPERATION_TWIN_GET_REQUEST:
            {
                //Building $iothub/twin/GET/?$rid={request id}
                /*
                **Codes_SRS_MQTTDEVICETWIN_25_024: [**send method shall build the get request topic of the format mentioned in spec ($iothub/twin/GET/?$rid={request id}) if the operation is of type DEVICE_OPERATION_TWIN_GET_REQUEST.**]**
                 */
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
                    /*
                    **Codes_SRS_MQTTDEVICETWIN_25_025: [**send method shall throw an exception if message contains a null or empty request id if the operation is of type DEVICE_OPERATION_TWIN_GET_REQUEST.**]**
                     */
                    throw new IOException("Request Id is Mandatory");
                }
                break;
            }
            case DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST:
            {
                // Building $iothub/twin/PATCH/properties/reported/?$rid={request id}&$version={base version}
                /*
                **Codes_SRS_MQTTDEVICETWIN_25_026: [**send method shall build the update reported properties request topic of the format mentioned in spec ($iothub/twin/PATCH/properties/reported/?$rid={request id}&$version={base version}) if the operation is of type DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST.**]**
                 */
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
                    /*
                    **Codes_SRS_MQTTDEVICETWIN_25_027: [**send method shall throw an exception if message contains a null or empty request id if the operation is of type DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST.**]**
                     */
                    throw new IOException("Request Id is Mandatory");
                }
                String version = message.getVersion();
                /*
                **Codes_SRS_MQTTDEVICETWIN_25_028: [**send method shall not throw an exception if message contains a null or empty version if the operation is of type DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST as version is optional**]**
                 */
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
                /*
                **Codes_SRS_MQTTDEVICETWIN_25_029: [**send method shall build the subscribe to desired properties request topic of the format mentioned in spec ($iothub/twin/PATCH/properties/desired/?$version={new version}) if the operation is of type DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.**]**
                 */
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
                /*
                **Codes_SRS_MQTTDEVICETWIN_25_023: [**send method shall throw an exception if the getDeviceOperationType() returns DEVICE_OPERATION_UNKNOWN.**]**
                 */
                throw new UnsupportedOperationException("Device Twin Operation is not supported - " + message.getDeviceOperationType());
            }
        }
        return topic.toString();
    }

    public void send(final DeviceTwinMessage message) throws IOException
    {
        if (message == null || message.getBytes() == null)
        {
            /*
            **Tests_SRS_MQTTDEVICETWIN_25_021: [**send method shall throw an exception if the message is null.**]**
             */
            throw new IOException("Message cannot be null");
        }

        if(!isStarted)
        {
            throw new IOException("Start device twin before using it");
        }

        if (message.getMessageType() != MessageType.DeviceTwin)
        {
            /*
            **Codes_SRS_MQTTDEVICETWIN_25_022: [**send method shall return if the message is not of Type DeviceTwin.**]**
             */
            return;
        }

        String publishTopic = buildTopic(message);
        requestMap.put(message.getRequestId(), message.getDeviceOperationType());

        /*
        **Codes_SRS_MqttMessaging_25_024: [**send method shall publish a message to the IOT Hub on the publish topic by calling method publish().**]**
         */
        if (message.getDeviceOperationType() == DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST)
        {
            // Subscribe to "$iothub/twin/PATCH/properties/desired/#"
            /*
            **Codes_SRS_MQTTDEVICETWIN_25_032: [**send method shall subscribe to desired properties by calling method subscribe() on topic "$iothub/twin/PATCH/properties/desired/#" specified in spec if the operation is DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.**]**
             */
            StringBuilder subscribeTopic = new StringBuilder();
            subscribeTopic.append(PATCH);
            subscribeTopic.append(BACKSLASH);
            subscribeTopic.append(PROPERTIES);
            subscribeTopic.append(BACKSLASH);
            subscribeTopic.append(DESIRED);
            subscribeTopic.append(BACKSLASH);
            subscribeTopic.append(POUND);
            /*
            **Codes_SRS_MQTTDEVICETWIN_25_032: [**send method shall subscribe to desired properties by calling method subscribe() on topic "$iothub/twin/PATCH/properties/desired/#" specified in spec if the operation is DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.**]**
             */
            this.subscribe(subscribeTopic.toString());
        }
        else
        {
            /*
            **Codes_SRS_MQTTDEVICETWIN_25_031: [**send method shall publish a message to the IOT Hub on the respective publish topic by calling method publish().**]**
             */
            this.publish(publishTopic, message.getBytes());
        }
    }

    private String getStatus(String token) throws IOException
    {
        String status = null;

        if (token != null && token.matches("\\d{3}")) // 3 digit number
        {
            status = token;
        }
        else
        {
            /*
            **Codes_SRS_MQTTDEVICETWIN_25_039: [**If the topic is of type response topic and if status is either a non 3 digit number or not found then receive shall throw IOException **]**
             */
            throw new IOException("Status could not be parsed");
        }

        return status;
    }

    private String getRequestId(String token) throws IOException
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

    private String getVersion(String token) throws IOException
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
    public Message receive() throws IOException
    {
        DeviceTwinMessage messsage = null;
        /*
        **Codes_SRS_MQTTDEVICETWIN_25_033: [**This method shall call parseTopic to parse the topic from the recevived Messages queue corresponding to the messaging client's operation.**]**
         */
        String topic = parseTopic();

        /*
        **Codes_SRS_MQTTDEVICETWIN_25_034: [**If the call parseTopic returns null or empty string then this method shall do nothing and return null**]**
         */

        if (topic != null && topic.length() > 0)
        {
            /*
            **Codes_SRS_MQTTDEVICETWIN_25_035: [**This method shall call parsePayload to get the message payload from the recevived Messages queue corresponding to the messaging client's operation.**]**
             */
            byte[] data = parsePayload(topic);

            if (topic.length() > RES.length() && topic.startsWith(RES))
            {
                // Tokenize on backslash
                String[] topicTokens = topic.split(Pattern.quote("/"));

                if (data != null && data.length > 0)
                {
                    /*
                    **Codes_SRS_MQTTDEVICETWIN_25_044: [**If the topic is of type response then this method shall set data and operation type as DEVICE_OPERATION_TWIN_GET_RESPONSE if data is not null**]**
                     */
                    messsage = new DeviceTwinMessage(data);
                    messsage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_UNKNOWN);
                }
                else
                {
                    // Case for $iothub/twin/res/{status}/?$rid={request id}
                    /*
                    **Tests_SRS_MQTTDEVICETWIN_25_045: [**If the topic is of type response then this method shall set empty data and operation type as DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE if data is null or empty**]**
                     */
                    messsage = new DeviceTwinMessage(new byte[0]); // empty body
                    messsage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_UNKNOWN);

                }

                // Case for $iothub/twin/res/{status}/?$rid={request id}&$version={new version}

                if (topicTokens.length > STATUS_TOKEN)
                {
                    /*
                    **Codes_SRS_MQTTDEVICETWIN_25_038: [**If the topic is of type response topic then this method shall parse further for status and set it for the message by calling setStatus for the message**]**
                     */
                    messsage.setStatus(getStatus(topicTokens[STATUS_TOKEN]));
                }
                else
                {
                    throw new IOException("Message received without status");
                }

                if (topicTokens.length > REQID_TOKEN)
                {
                    /*
                    **Codes_SRS_MQTTDEVICETWIN_25_040: [**If the topic is of type response topic then this method shall parse further to look for request id which if found is set by calling setRequestId**]**
                     */
                    String requestId = getRequestId(topicTokens[REQID_TOKEN]);
                    messsage.setRequestId(requestId);
                    if (requestMap.containsKey(requestId))
                    {
                        switch (requestMap.remove(requestId))
                        {
                            case DEVICE_OPERATION_TWIN_GET_REQUEST:
                                messsage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_GET_RESPONSE);
                                break;
                            case DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST:
                                messsage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE);
                                break;
                            default:
                                messsage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_UNKNOWN);
                        }
                    }
                    else
                    {
                        throw new UnsupportedOperationException();
                    }
                }

                if (topicTokens.length > VERSION_TOKEN)
                {
                    /*
                    **Codes_SRS_MQTTDEVICETWIN_25_041: [**If the topic is of type response topic then this method shall parse further to look for version which if found is set by calling setVersion**]**
                     */
                    messsage.setVersion(getVersion(topicTokens[VERSION_TOKEN]));
                }

            }
            else if (topic.length() > PATCH.length() && topic.startsWith(PATCH))
            {
                if (topic.startsWith(PATCH + BACKSLASH + PROPERTIES + BACKSLASH + DESIRED))
                {
                    if (data != null)
                    {
                        /*
                        **Codes_SRS_MQTTDEVICETWIN_25_046: [**If the topic is of type patch for desired properties then this method shall set the data and operation type as DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE if data is not null or empty**]**
                         */
                        messsage = new DeviceTwinMessage(data);
                        messsage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);
                    }
                    else
                    {
                        /*
                        **Tests_SRS_MQTTDEVICETWIN_25_047: [**If the topic is of type patch for desired properties then this method shall throw unsupportedoperation exception if data is null or empty**]**
                         */
                        throw new UnsupportedOperationException();
                    }
                    // Case for $iothub/twin/PATCH/properties/desired/?$version={new version}

                    // Tokenize on backslash
                    String[] topicTokens = topic.split(Pattern.quote("/"));

                    if (topicTokens.length > PATCH_VERSION_TOKEN)
                    {
                        /*
                        **Codes_SRS_MQTTDEVICETWIN_25_042: [**If the topic is of type patch for desired properties then this method shall parse further to look for version which if found is set by calling setVersion**]**
                         */
                        messsage.setVersion(getVersion(topicTokens[PATCH_VERSION_TOKEN]));
                    }
                }
                else
                {
                    /*
                    **Codes_SRS_MQTTDEVICETWIN_25_043: [**If the topic is not of type response for desired properties then this method shall throw unsupportedoperation exception**]**
                     */
                    throw new UnsupportedOperationException();
                }
            }
            else
            {
                /*
                **Codes_SRS_MQTTDEVICETWIN_25_037: [**This method shall parse topic to look for only either twin response topic or twin patch topic and thorw unsupportedoperation exception other wise.**]**
                 */
                throw new UnsupportedOperationException();

            }

            return messsage;
        }
        else
        {
            return null;
        }
    }
}
