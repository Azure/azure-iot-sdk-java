// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.CustomLogger;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import org.apache.commons.lang3.tuple.Pair;
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

    public MqttDeviceMethod(MqttConnection mqttConnection) throws IOException
    {
        /*
        Codes_SRS_MqttDeviceMethod_25_001: [**The constructor shall instantiate super class without any parameters.**]**
         */
        super(mqttConnection);
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
        isStarted = false;

        if (!requestMap.isEmpty())
        {
            logger.LogInfo("Pending %d responses to be sent to IotHub yet unsubscribed %s", requestMap.size(), logger.getMethodName());
        }
    }

    public void send(final IotHubTransportMessage message) throws IOException
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

        if (message.getMessageType() != MessageType.DEVICE_METHODS)
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
        synchronized (this.mqttLock)
        {
            IotHubTransportMessage message = null;

            Pair<String, byte[]> messagePair = peekMessage();

            if (messagePair != null)
            {
                String topic = messagePair.getKey();

                if (topic != null && topic.length() > 0)
                {
                    // Codes_SRS_MQTTDEVICEMETHOD_25_026: [This method shall call peekMessage to get the message payload from the received Messages queue corresponding to the messaging client's operation.]
                    byte[] data = messagePair.getValue();

                    if (topic.length() > METHOD.length() && topic.startsWith(METHOD))
                    {
                        //Codes_SRS_MqttDeviceMethod_34_027: [This method shall parse message to look for Post topic ($iothub/methods/POST/) and return null other wise.]
                        if (topic.length() > POST.length() && topic.startsWith(POST))
                        {
                            //remove this message from the queue as this is the correct handler
                            allReceivedMessages.poll();

                            // Case for $iothub/methods/POST/{method name}/?$rid={request id}
                            TopicParser topicParser = new TopicParser(topic);

                            if (data != null && data.length > 0)
                            {
                                message = new IotHubTransportMessage(data, MessageType.DEVICE_METHODS);

                                message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_UNKNOWN);
                            }
                            else
                            {
                                message = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_METHODS);

                                message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_UNKNOWN);
                            }

                            try
                            {
                            /*
                            Codes_SRS_MqttDeviceMethod_25_028: [**If the topic is of type post topic then this method shall parse further for method name and set it for the message by calling setMethodName for the message**]**
                            */
                                String methodName = topicParser.getMethodName(METHOD_TOKEN);
                                message.setMethodName(methodName);
                            } catch (Exception e)
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
                            } catch (Exception e)
                            {
                                throw new IOException("Method Invoke received without request ID");
                            }
                        }
                    }
                }
            }
            return message;
        }
    }
}
