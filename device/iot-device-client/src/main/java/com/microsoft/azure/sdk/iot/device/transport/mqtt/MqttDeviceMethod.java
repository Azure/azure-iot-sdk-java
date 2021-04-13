// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

@Slf4j
public class MqttDeviceMethod extends Mqtt
{
    private final String subscribeTopic;
    private final String responseTopic;
    private final Map<String, DeviceOperations> requestMap = new HashMap<>();
    private boolean isStarted = false;

    private static final String POUND = "#";
    private static final String BACKSLASH = "/";
    private static final String QUESTION = "?";

    private static final String METHOD = "$iothub/methods/";
    private static final String POST = METHOD + "POST";
    private static final String RES = METHOD + "res";
    private static final String REQ_ID = QUESTION + "$rid=";

    //Placement for $iothub/methods/POST/{method name}/?$rid={request id}
    private static final int METHOD_TOKEN = 3;
    private static final int REQID_TOKEN = 4;

    public MqttDeviceMethod(
        String deviceId,
        MqttConnectOptions connectOptions,
        Map<Integer, Message> unacknowledgedSentMessages,
        Queue<Pair<String, byte[]>> receivedMessages)
    {
        super(null, deviceId, connectOptions, unacknowledgedSentMessages, receivedMessages);

        this.subscribeTopic = POST + BACKSLASH + POUND;
        this.responseTopic = RES;
    }

    public void start()
    {
        if (!isStarted)
        {
            isStarted = true;
        }
    }

    public void stop()
    {
        isStarted = false;

        if (!requestMap.isEmpty())
        {
            log.trace("Pending {} responses to be sent to IotHub yet unsubscribed", requestMap.size());
        }
    }

    /**
     * Sends the provided device method message over the mqtt connection
     *
     * @param message the message to send
     * @throws TransportException if any exception is encountered while sending the message
     * @throws IllegalArgumentException if the provided message is null or has a null body
     */
    public void send(final IotHubTransportMessage message) throws TransportException, IllegalArgumentException
    {
        if (message == null || message.getBytes() == null)
        {
            throw new IllegalArgumentException("Message cannot be null");
        }

        if(!isStarted)
        {
            throwMethodsTransportException("Start device method before using send");
        }

        if (message.getMessageType() != MessageType.DEVICE_METHODS)
        {
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
                    throw new IllegalArgumentException("Request id cannot be null or empty");
                }

                if (requestMap.containsKey(message.getRequestId()))
                {
                    if (requestMap.remove(message.getRequestId()) != DeviceOperations.DEVICE_OPERATION_METHOD_RECEIVE_REQUEST)
                    {
                        throwMethodsTransportException("Mismatched request and response operation");
                    }
                }
                else
                {
                    throwMethodsTransportException("Sending a response for the method that was never invoked");
                }

                String topic = this.responseTopic + BACKSLASH +
                        message.getStatus() +
                        BACKSLASH +
                        REQ_ID +
                        message.getRequestId();

                this.publish(topic, message);
                break;
            }
            default:
            {
                throwMethodsTransportException("Mismatched device method operation");
            }
        }
    }

    @Override
    public IotHubTransportMessage receive() throws TransportException
    {
        synchronized (this.receivedMessagesLock)
        {
            IotHubTransportMessage message = null;

            Pair<String, byte[]> messagePair = this.receivedMessages.peek();

            if (messagePair != null)
            {
                String topic = messagePair.getKey();

                if (topic != null && topic.length() > 0)
                {
                    byte[] data = messagePair.getValue();

                    if (topic.length() > METHOD.length() && topic.startsWith(METHOD))
                    {
                        if (topic.length() > POST.length() && topic.startsWith(POST))
                        {
                            //remove this message from the queue as this is the correct handler
                            this.receivedMessages.poll();

                            // Case for $iothub/methods/POST/{method name}/?$rid={request id}
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

                            String methodName = topicParser.getMethodName(METHOD_TOKEN);
                            message.setMethodName(methodName);

                            String reqId = topicParser.getRequestId(REQID_TOKEN);
                            if (reqId != null)
                            {
                                message.setRequestId(reqId);

                                message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);
                                requestMap.put(reqId, DeviceOperations.DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);
                            }
                            else
                            {
                                throwMethodsTransportException("Request ID cannot be null");
                            }
                        }
                    }
                }
            }

            return message;
        }
    }

    private void throwMethodsTransportException(String message) throws TransportException
    {
        TransportException transportException = new TransportException(message);
        transportException.setIotHubService(TransportException.IotHubService.METHODS);
        throw transportException;
    }
}
