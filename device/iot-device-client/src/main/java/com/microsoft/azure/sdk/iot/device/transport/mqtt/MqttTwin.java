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

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Pattern;

@Slf4j
class MqttTwin extends Mqtt
{
    private final String subscribeTopic;
    private final Map<String, DeviceOperations> requestMap = new HashMap<>();
    private boolean isStarted = false;

    private static final String BACKSLASH = "/";
    private static final String AND = "&";
    private static final String QUESTION = "?";
    private static final String POUND = "#";

    private static final String TWIN = "$iothub/twin";
    private static final String GET = TWIN + BACKSLASH + "GET";
    private static final String RES = TWIN + BACKSLASH + "res";
    private static final String PATCH = TWIN + BACKSLASH + "PATCH";
    private static final String PROPERTIES = "properties";
    private static final String DESIRED = "desired";
    private static final String REPORTED = "reported";
    private static final String REQ_ID = QUESTION + "$rid=";
    private static final String VERSION = "$version=";

    //Placement in $iothub/twin/res/{status}/?$rid={request id}&$version={new version}
    private static final int STATUS_TOKEN = 3;
    private static final int REQID_TOKEN = 4;
    private static final int VERSION_TOKEN = 4;

    //Placement for $iothub/twin/PATCH/properties/desired/?$version={new version}
    private static final int PATCH_VERSION_TOKEN = 5;

    public MqttTwin(
        String deviceId,
        MqttConnectOptions connectOptions,
        Map<Integer, Message> unacknowledgedSentMessages,
        Queue<Pair<String, byte[]>> receivedMessages)
    {
        super(null, deviceId, connectOptions, unacknowledgedSentMessages, receivedMessages);

        this.subscribeTopic = RES + BACKSLASH + POUND;
    }

    public void start() throws TransportException
    {
        if (!isStarted)
        {
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

    private String buildTopic(final IotHubTransportMessage message)
    {
        StringBuilder topic = new StringBuilder();
        switch (message.getDeviceOperationType())
        {
            case DEVICE_OPERATION_TWIN_GET_REQUEST:
            {
                //Building $iothub/twin/GET/?$rid={request id}
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
                    throw new IllegalArgumentException("Request Id is Mandatory");
                }
                break;
            }
            case DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST:
            {
                // Building $iothub/twin/PATCH/properties/reported/?$rid={request id}&$version={base version}
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
                    throw new IllegalArgumentException("Request Id is Mandatory");
                }

                int version = message.getVersion();
                topic.append(AND);
                topic.append(VERSION);
                topic.append(version);
                break;
            }
            case DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST:
            {
                // Building $iothub/twin/PATCH/properties/desired/?$version={new version}
                topic.append(PATCH);
                topic.append(BACKSLASH);
                topic.append(PROPERTIES);
                topic.append(BACKSLASH);
                topic.append(DESIRED);

                int version = message.getVersion();
                topic.append(BACKSLASH);
                topic.append(QUESTION);
                topic.append(VERSION);
                topic.append(version);
                break;

            }
            default:
            {
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
        requestMap.put(message.getRequestId(), message.getDeviceOperationType());

        if (message.getDeviceOperationType() == DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST)
        {
            // Subscribe to "$iothub/twin/PATCH/properties/desired/#"
            String subscribeTopic = PATCH +
                    BACKSLASH +
                    PROPERTIES +
                    BACKSLASH +
                    DESIRED +
                    BACKSLASH +
                    POUND;

            this.subscribe(subscribeTopic);
        }
        else
        {
            this.publish(publishTopic, message);
        }
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
    public IotHubTransportMessage receive()
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
                    if (topic.length() > TWIN.length() && topic.startsWith(TWIN))
                    {
                        byte[] data = messagePair.getValue();

                        //remove this message from the queue as this is the correct handler
                        this.receivedMessages.poll();

                        if (topic.length() > RES.length() && topic.startsWith(RES))
                        {
                            // Tokenize on backslash
                            String[] topicTokens = topic.split(Pattern.quote("/"));
                            if (data != null && data.length > 0)
                            {
                                message = new IotHubTransportMessage(data, MessageType.DEVICE_TWIN);
                            }
                            else
                            {
                                // Case for $iothub/twin/res/{status}/?$rid={request id}
                                message = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_TWIN); // empty body

                            }
                            message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_UNKNOWN);

                            // Case for $iothub/twin/res/{status}/?$rid={request id}&$version={new version}
                            if (topicTokens.length > STATUS_TOKEN)
                            {
                                message.setStatus(getStatus(topicTokens[STATUS_TOKEN]));
                            }
                            else
                            {
                                log.warn("Message received without status");
                            }

                            if (topicTokens.length > REQID_TOKEN)
                            {
                                String requestId = getRequestId(topicTokens[REQID_TOKEN]);
                                // MQTT does not have the concept of correlationId for request/response handling but it does have a requestId
                                // To handle this we are setting the correlationId to the requestId to better handle correlation
                                // whether we use MQTT or AMQP.
                                message.setRequestId(requestId);
                                message.setCorrelationId(requestId);
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
                                    log.warn("Request ID cannot be null");
                                }
                            }

                            if (topicTokens.length > VERSION_TOKEN)
                            {
                                String version = getVersion(topicTokens[VERSION_TOKEN]);
                                if (version != null && !version.isEmpty())
                                {
                                    message.setVersion(Integer.parseInt(version));
                                }
                            }
                        }
                        else if (topic.length() > PATCH.length() && topic.startsWith(PATCH))
                        {
                            if (topic.startsWith(PATCH + BACKSLASH + PROPERTIES + BACKSLASH + DESIRED))
                            {
                                if (data != null)
                                {
                                    message = new IotHubTransportMessage(data, MessageType.DEVICE_TWIN);
                                    message.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);
                                }

                                // Case for $iothub/twin/PATCH/properties/desired/?$version={new version}
                                // Tokenize on backslash
                                String[] topicTokens = topic.split(Pattern.quote("/"));
                                if (topicTokens.length > PATCH_VERSION_TOKEN)
                                {
                                    if (message != null)
                                    {
                                        message.setVersion(Integer.parseInt(getVersion(topicTokens[PATCH_VERSION_TOKEN])));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return message;
        }
    }
}
