// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.certificatesigning.IotHubCertificateSigningError;
import com.microsoft.azure.sdk.iot.device.certificatesigning.IotHubCertificateSigningRequestAccepted;
import com.microsoft.azure.sdk.iot.device.certificatesigning.IotHubCertificateSigningResponse;
import com.microsoft.azure.sdk.iot.device.certificatesigning.IotHubCertificateSigningResponseCallback;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.TransportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Pattern;

@Slf4j
class MqttCertificateSigning extends Mqtt
{
    private static final String certificateSigningRequestTopic = "$iothub/credentials/POST/issueCertificate/?$rid=";
    private static final String certificateSigningResponseTopicFilter = "$iothub/credentials/res/#";
    private static final String certificateSigningResponseTopic = "$iothub/credentials/res/";
    private boolean isStarted = false;
    private Map<String, IotHubCertificateSigningResponseCallback> inProgressRequestIdMap = new HashMap<>();
    private static final String REQ_ID = "?$rid=";

    public MqttCertificateSigning(
        String deviceId,
        MqttConnectOptions connectOptions,
        Map<Integer, Message> unacknowledgedSentMessages,
        Queue<Pair<String, MqttMessage>> receivedMessages)
    {
        super(null, deviceId, connectOptions, unacknowledgedSentMessages, receivedMessages);

        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("Device id cannot be null or empty");
        }
    }

    public void start() throws TransportException
    {
        if (!this.isStarted)
        {
            this.subscribe(certificateSigningResponseTopicFilter);
            this.isStarted = true;
        }
    }

    public void stop()
    {
        this.isStarted = false;
    }

    public void send(IotHubTransportMessage message) throws TransportException
    {
        IotHubCertificateSigningResponseCallback signingCallback = message.getIotHubCertificateSigningResponseCallback();

        // Service will ack this immediately, then later publish a message to the response topic
        inProgressRequestIdMap.put(message.getRequestId(), signingCallback);
        this.publish(certificateSigningRequestTopic + message.getRequestId(), message);

        // IoT hub will respond to this request by sending a few response messages over the subscribed topic.
    }

    @Override
    public IotHubTransportMessage receive()
    {
        synchronized (this.receivedMessagesLock)
        {
            Pair<String, MqttMessage> messagePair = this.receivedMessages.peek();

            if (messagePair == null)
            {
                return null;
            }

            String topic = messagePair.getKey();

            //$iothub/credentials/res/{status}/?$rid={request_id}
            if (topic == null || !topic.startsWith(certificateSigningResponseTopic))
            {
                return null;
            }

            MqttMessage mqttMessage = messagePair.getValue();
            byte[] payload = mqttMessage.getPayload();

            //remove this message from the queue as this is the correct handler
            this.receivedMessages.poll();

            String[] topicTokens = topic.split(Pattern.quote("/"));
            if (topicTokens.length != 5)
            {
                log.warn("Received MQTT message on certificate signing response topic with an unexpected topic pattern. Ignoring it.");
                return createTransportMessage(mqttMessage.getQos());
            }

            String status = topicTokens[3];
            String requestId = getRequestId(topicTokens[4]);
            if (!this.inProgressRequestIdMap.containsKey(requestId))
            {
                log.warn("Received certificate signing response message for an unknown request Id. Ignoring it.");
                return createTransportMessage(mqttMessage.getQos());
            }
            else
            {
                IotHubCertificateSigningResponseCallback iotHubCertificateSigningResponseCallback = this.inProgressRequestIdMap.get(requestId);

                if (status.equals("202"))
                {
                    try
                    {
                        IotHubCertificateSigningRequestAccepted accepted = new IotHubCertificateSigningRequestAccepted(new String(payload, StandardCharsets.UTF_8));
                        iotHubCertificateSigningResponseCallback.onCertificateSigningRequestAccepted(accepted);
                        return createTransportMessage(mqttMessage.getQos());
                    }
                    catch (IllegalArgumentException e)
                    {
                        log.error("Received certificate signing request accepted message with malformed payload. Ignoring it.");
                        return createTransportMessage(mqttMessage.getQos());
                    }
                }
                else if (status.equals("200"))
                {
                    try
                    {
                        this.inProgressRequestIdMap.remove(requestId);
                        IotHubCertificateSigningResponse response = new IotHubCertificateSigningResponse(new String(payload, StandardCharsets.UTF_8));
                        iotHubCertificateSigningResponseCallback.onCertificateSigningComplete(response);
                        return createTransportMessage(mqttMessage.getQos());
                    }
                    catch (IllegalArgumentException e)
                    {
                        log.error("Received certificate signing response message with malformed payload. Ignoring it.");
                        return createTransportMessage(mqttMessage.getQos());
                    }
                }
                else
                {
                    try
                    {
                        this.inProgressRequestIdMap.remove(requestId);
                        IotHubCertificateSigningError error = new IotHubCertificateSigningError(new String(payload, StandardCharsets.UTF_8));
                        iotHubCertificateSigningResponseCallback.onCertificateSigningError(error);
                        return createTransportMessage(mqttMessage.getQos());
                    }
                    catch (IllegalArgumentException e)
                    {
                        log.error("Received certificate signing error message with malformed payload. Ignoring it.");
                        return createTransportMessage(mqttMessage.getQos());
                    }
                }
            }
        }
    }

    private static IotHubTransportMessage createTransportMessage(int qos)
    {
        IotHubTransportMessage transportMessage = new IotHubTransportMessage(new byte[0], MessageType.CERTIFICATE_SIGNING);
        transportMessage.setQualityOfService(qos);
        return transportMessage;
    }

    private String getRequestId(String token)
    {
        String reqId = null;

        if (token.contains(REQ_ID)) // restriction for request id
        {
            int startIndex = token.indexOf(REQ_ID) + REQ_ID.length();
            int endIndex = token.length();

            reqId = token.substring(startIndex, endIndex);
        }

        return reqId;
    }
}
