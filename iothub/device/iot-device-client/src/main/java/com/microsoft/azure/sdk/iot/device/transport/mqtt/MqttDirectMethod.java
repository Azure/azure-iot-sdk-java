// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.twin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.transport.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Map;
import java.util.Queue;

@Slf4j
class MqttDirectMethod extends Mqtt
{
    private final String subscribeTopic;
    private final String responseTopic;
    private final boolean isConnectingToMqttGateway;
    private final String deviceId;

    private boolean isStarted = false;

    private static final String POST_HUB = "$iothub/methods/POST";
    private static final String POST_E4K = "$iothub/methods/%s/POST";
    private static final String RES_HUB = "$iothub/methods/res";
    private static final String RES_E4K = "$iothub/methods/%s/res";

    public MqttDirectMethod(
        String deviceId,
        MqttConnectOptions connectOptions,
        Map<Integer, Message> unacknowledgedSentMessages,
        Queue<Pair<String, MqttMessage>> receivedMessages,
        boolean isConnectingToMqttGateway)
    {
        super(null, deviceId, connectOptions, unacknowledgedSentMessages, receivedMessages);

        this.isConnectingToMqttGateway = isConnectingToMqttGateway;
        this.deviceId = deviceId;

        if (!isConnectingToMqttGateway)
        {
            // $iothub/methods/POST/#
            this.subscribeTopic = POST_HUB + "/#";

            // $iothub/methods/res
            this.responseTopic = RES_HUB;
        }
        else
        {
            // $iothub/methods/{clientId}/POST/#
            this.subscribeTopic = String.format(POST_E4K, deviceId) + "/#";

            // $iothub/methods/{clientId}/res
            this.responseTopic = String.format(RES_E4K, deviceId);
        }
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
                this.subscribe(subscribeTopic);
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
                String topic = String.format(format, this.responseTopic, message.getStatus(), message.getRequestId());

                this.publish(topic, message);
                break;
            }
            default:
            {
                throw new TransportException("Mismatched device method operation");
            }
        }
    }

    @Override
    public IotHubTransportMessage receive()
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

                    if (isReceivingFromCorrectTopic(topic))
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

    private boolean isReceivingFromCorrectTopic(String topic)
    {
        if (!this.isConnectingToMqttGateway)
        {
            return topic.length() > POST_HUB.length() && topic.startsWith(POST_HUB);
        }

        String e4kReceivingTopic = String.format(POST_E4K, this.deviceId);
        return topic.length() > e4kReceivingTopic.length() && topic.startsWith(e4kReceivingTopic);
    }
}
