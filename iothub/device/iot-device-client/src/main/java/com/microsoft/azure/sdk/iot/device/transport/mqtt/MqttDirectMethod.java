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
    private boolean isStarted = false;

    private static final String POUND = "#";
    private static final String BACKSLASH = "/";
    private static final String QUESTION = "?";

    private static final String METHOD = "$iothub/methods";
    private static final String POST = "POST";
    private static final String RES = "res";
    private static final String REQ_ID = "$rid=";

    //Placement for $iothub/methods/{clientId}/POST/{methodName}/?$rid={request id}
    private static final int METHOD_TOKEN = 4;
    private static final int REQID_TOKEN = 5;

    public MqttDirectMethod(
        String deviceId,
        MqttConnectOptions connectOptions,
        Map<Integer, Message> unacknowledgedSentMessages,
        Queue<Pair<String, MqttMessage>> receivedMessages)
    {
        super(null, deviceId, connectOptions, unacknowledgedSentMessages, receivedMessages);

        // Subscribe to "$iothub/methods/{clientId}/POST/#"
        this.subscribeTopic = METHOD + BACKSLASH + deviceId + BACKSLASH + POST + BACKSLASH + POUND;

        // Create the topic to response - "$iothub/methods/{clientId}/res"
        this.responseTopic = METHOD + BACKSLASH + deviceId + BACKSLASH + RES;
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

                // Respond to "$iothub/methods/{clientId}/res/{status}/?$rid={request-id}"
                String topic = this.responseTopic +
                        BACKSLASH +
                        message.getStatus() +
                        BACKSLASH +
                        QUESTION +
                        REQ_ID +
                        message.getRequestId();

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

                    if (topic.length() > METHOD.length() && topic.startsWith(METHOD))
                    {
                        String[] parts = topic.split(BACKSLASH);
                        // The topic is expected to start with "$iothub/methods/{clientId}/POST/"
                        if (parts.length > 4 && parts[3].equals(POST))
                        {
                            //remove this message from the queue as this is the correct handler
                            this.receivedMessages.poll();

                            // Case for $iothub/methods/{clientId}/POST/{method name}/?$rid={request id}
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

                            String methodName = topicParser.getMethodName(METHOD_TOKEN);
                            message.setMethodName(methodName);

                            String reqId = topicParser.getRequestId(REQID_TOKEN);
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
            }

            return message;
        }
    }
}
