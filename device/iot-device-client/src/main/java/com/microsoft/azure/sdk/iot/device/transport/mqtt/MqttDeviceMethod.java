// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.CustomLogger;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodMessage;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class MqttDeviceMethod extends Mqtt
{
    private String subscribeTopic;
    private String responseTopic;
    private final Map<String, DeviceOperations> requestMap = new HashMap<>();
    private boolean isStarted = false;
    private final CustomLogger logger = new CustomLogger(this.getClass());

    private final String POUND = "#";
    private final String BACKSLASH = "/";
    private final String QUESTION = "?";

    private final String METHOD = "$iothub/methods/";
    private final String POST = METHOD + "POST";
    private final String RES = METHOD + "res";
    private final String REQ_ID = QUESTION + "$rid=";

    //Placement for $iothub/methods/POST/{method name}/?$rid={request id}
    private final int POST_TOKEN = 2;
    private final int METHOD_TOKEN = 3;
    private final int REQID_TOKEN = 4;

    public MqttDeviceMethod() throws IOException
    {
        /*
        Codes_SRS_MqttDeviceMethod_25_001: [**The constructor shall instantiate super class without any parameters.**]**
         */
        super();
        /*
        Codes_SRS_MqttDeviceMethod_25_002: [**The constructor shall create subscribe and response topics strings for device methods as per the spec.**]**
         */
        this.subscribeTopic = POST + BACKSLASH + POUND;
        this.responseTopic = RES;
    }

    public void start()
    {
        if (!isStarted)
        {
            /*
            Codes_SRS_MqttDeviceMethod_25_014: [**start method shall just mark that this class is ready to start.**]**
             */

            isStarted = true;
        }

    }

    public void stop() throws IOException
    {
        if (isStarted)
        {
            /*
            Codes_SRS_MqttDeviceMethod_25_015: [**stop method shall unsubscribe from method subscribe topic ($iothub/methods/POST/#) and throw IoException otherwise.**]**
             */
            this.unsubscribe(subscribeTopic);
            isStarted = false;
        }
        if (!requestMap.isEmpty())
        {
            logger.LogInfo("Pending %d responses to be sent to IotHub yet unsubscribed %s", requestMap.size(), logger.getMethodName());
        }

    }

    @Override
    String parseTopic() throws IOException
    {
        /*
        Codes_SRS_MqttDeviceMethod_25_003: [**parseTopic concrete method shall be implemeted by MqttDeviceMethod concrete class.**]**
         */
        String topic = null;

        if (allReceivedMessages == null)
        {
            /*
            Codes_SRS_MqttDeviceMethod_25_007: [**If receiveMessage queue is null then parseTopic shall throw IOException.**]**
             */
            throw new IOException("Queue cannot be null");
        }

        /*
        Codes_SRS_MqttDeviceMethod_25_006: [**If received messages queue is empty then parseTopic shall return null string.**]**
         */
        if (!allReceivedMessages.isEmpty())
        {

            for (Map.Entry<String, byte[]> data : allReceivedMessages.entrySet())
            {
                String topicFound = data.getKey();

               /*
               Codes_SRS_MqttDeviceMethod_25_004: [**parseTopic shall look for the method topic($iothub/methods) prefix from received message queue as per spec and if found shall return it as string.**]**
                */
               /*
                Codes_SRS_MqttDeviceMethod_25_005: [**If none of the topics from the received queue match the methods topic prefix then this method shall return null string .**]**
                */
                if (topicFound != null && topicFound.length() > METHOD.length() && topicFound.startsWith(METHOD))
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
            This method is called only when you are certain that there is a message in the queue meant for device methods that needs to be retrieved and then deleted.
         */
        /*
        Codes_SRS_MqttDeviceMethod_25_008: [**parsePayload concrete method shall be implemeted by MqttDeviceMethod concrete class.**]**
         */

        if (topic == null)
        {
            /*
            Codes_SRS_MqttDeviceMethod_25_010: [**If the topic is null then parsePayload shall stop parsing for payload and return.**]**
             */
            return null;
        }
        if (allReceivedMessages == null)
        {
            /*
            Codes_SRS_MqttDeviceMethod_25_012: [**If receiveMessage queue is null then this method shall throw IOException.**]**
             */
            throw new IOException("Invalid State - topic is not null and could not be found in queue");
        }

        if (!allReceivedMessages.containsKey(topic))
        {
            /*
            Codes_SRS_MqttDeviceMethod_25_011: [**If the topic is non-null and received messagesqueue could not locate the payload then this method shall throw IOException**]**
             */
            throw new IOException("Topic should be present in received queue at this point");
        }

        /*
        Codes_SRS_MqttDeviceMethod_25_009: [**parsePayload method shall look for payload for the corresponding topic from the received messagesqueue.**]**
         */
        if (!allReceivedMessages.isEmpty())
        {
            /*
            Codes_SRS_MqttDeviceMethod_25_013: [**If the topic is found in the message queue then parsePayload shall delete it from the queue and return it.**]**
             */
            return allReceivedMessages.remove(topic);
        }

        return null;
    }

    public void send(final DeviceMethodMessage message) throws IOException
    {
        if (message == null || message.getBytes() == null)
        {
            /*
            Codes_SRS_MqttDeviceMethod_25_016: [**send method shall throw an exception if the message is null.**]**
             */
            throw new IllegalArgumentException("Message cannot be null");
        }

        if(!isStarted)
        {
            /*
            Codes_SRS_MqttDeviceMethod_25_018: [**send method shall throw an IoException if device method has not been started yet.**]**
             */
            throw new IOException("Start device method before using send");
        }

        if (message.getMessageType() != MessageType.DeviceMethods)
        {
            /*
            Codes_SRS_MqttDeviceMethod_25_017: [**send method shall return if the message is not of Type DeviceMethod.**]**
             */
            return;
        }

        switch (message.getDeviceOperationType())
        {
            case DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST:
            {
                this.subscribe(subscribeTopic);
                break;
            }
            case DEVICE_OPERATION_METHOD_SEND_RESPONSE:
            {
                if (message.getRequestId() == null || message.getRequestId().isEmpty())
                {
                    /*
                    Codes_SRS_MqttDeviceMethod_25_021: [**send method shall throw an IoException if message contains a null or empty request id if the operation is of type DEVICE_OPERATION_METHOD_SEND_RESPONSE.**]**
                     */
                    throw new IOException("Request id cannot be null or empty");
                }

                if (requestMap.containsKey(message.getRequestId()))
                {
                    switch (requestMap.remove(message.getRequestId()))
                    {
                        case DEVICE_OPERATION_METHOD_RECEIVE_REQUEST:
                            break;
                        default:
                            throw new IOException("Mismatched request and response operation");
                    }
                }
                else
                {
                    /*
                    Codes_SRS_MqttDeviceMethod_25_023: [**send method shall throw an exception if a response is sent without having a method invoke on the request id if the operation is of type DEVICE_OPERATION_METHOD_SEND_RESPONSE.**]**
                     */
                    throw new IOException("Sending a response for the method that was never invoked");
                }

                String topic = this.responseTopic + BACKSLASH +
                        message.getStatus() +
                        BACKSLASH +
                        REQ_ID +
                        message.getRequestId();
                /*
                Codes_SRS_MqttDeviceMethod_25_022: [**send method shall build the publish topic of the format mentioned in spec ($iothub/methods/res/{status}/?$rid={request id}) and publish if the operation is of type DEVICE_OPERATION_METHOD_SEND_RESPONSE.**]**
                 */
                this.publish(topic, message.getBytes());
                break;
            }
            default:
            {
                /*
                Codes_SRS_MqttDeviceMethod_25_019: [**send method shall throw an IoException if the getDeviceOperationType() is not of type DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST or DEVICE_OPERATION_METHOD_SEND_RESPONSE .**]**
                 */
                throw new IOException("Mismatched device method operation");
            }
        }
    }

    @Override
    public Message receive() throws IOException
    {
        DeviceMethodMessage message = null;

        /*
        Codes_SRS_MqttDeviceMethod_25_024: [**This method shall call parseTopic to parse the topic from the received Messages queue looking for presence of $iothub/methods/ in the topics .**]**
         */
        String topic = parseTopic();

        if (topic != null && topic.length() > 0)
        {
            /*
            Codes_SRS_MqttDeviceMethod_25_026: [**This method shall call parsePayload to get the message payload from the recevived Messages queue corresponding to the messaging client's operation.**]**
             */
            byte[] data = parsePayload(topic);

            if (topic.length() > POST.length() && topic.startsWith(POST))
            {
                // Case for $iothub/methods/POST/{method name}/?$rid={request id}
                TopicParser topicParser = new TopicParser(topic);

                if (data != null && data.length > 0)
                {
                    message = new DeviceMethodMessage(data);

                    message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_UNKNOWN);
                }
                else
                {
                    message = new DeviceMethodMessage(new byte[0]);

                    message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_UNKNOWN);
                }

                try
                {
                    /*
                    Codes_SRS_MqttDeviceMethod_25_028: [**If the topic is of type post topic then this method shall parse further for method name and set it for the message by calling setMethodName for the message**]**
                     */
                    String methodName = topicParser.getMethodName(METHOD_TOKEN);
                    message.setMethodName(methodName);
                }
                catch (Exception e)
                {
                    /*
                    Codes_SRS_MqttDeviceMethod_25_029: [**If method name not found or is null then receive shall throw IOException **]**
                     */
                    throw new IOException("Method name could not be parsed");
                }


                try
                {
                    String reqId = topicParser.getRequestId(REQID_TOKEN);
                    if (reqId != null)
                    {
                        /*
                        Codes_SRS_MqttDeviceMethod_25_030: [**If the topic is of type post topic then this method shall parse further to look for request id which if found is set by calling setRequestId**]**
                         */
                        message.setRequestId(reqId);
                        /*
                        Codes_SRS_MqttDeviceMethod_25_032: [**If the topic is of type post topic and if method name and request id has been successfully parsed then this method shall set operation type as DEVICE_OPERATION_METHOD_RECEIVE_REQUEST **]**
                         */
                        message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);
                        requestMap.put(reqId, DeviceOperations.DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);
                    }
                    else
                    {
                        /*
                        Codes_SRS_MqttDeviceMethod_25_031: [**If request id is not found or is null then receive shall throw IOException **]**
                         */
                        throw new IOException("Request ID cannot be null");
                    }
                }
                catch (Exception e)
                {
                    throw new IOException("Method Invoke received without request ID");
                }
            }
            else
            {
                /*
                Codes_SRS_MqttDeviceMethod_25_027: [**This method shall parse topic to look for Post topic ($iothub/methods/POST/) and throw unsupportedoperation exception other wise.**]**
                 */
                throw new UnsupportedOperationException();
            }
        }
        return message;
    }

}
