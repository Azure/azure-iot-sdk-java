// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Queue;

public class MqttMessaging extends Mqtt
{
    private final String moduleId;
    private final String eventsSubscribeTopic;
    private final String inputsSubscribeTopic;
    private final String publishTopic;
    private final boolean isEdgeHub;

    public MqttMessaging(
        String deviceId,
        MqttMessageListener messageListener,
        String moduleId,
        boolean isEdgeHub,
        MqttConnectOptions connectOptions,
        Map<Integer, Message> unacknowledgedSentMessages,
        Queue<Pair<String, byte[]>> receivedMessages)
    {
        super(messageListener, deviceId, connectOptions, unacknowledgedSentMessages, receivedMessages);

        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("Device id cannot be null or empty");
        }

        if (moduleId == null || moduleId.isEmpty())
        {
            this.publishTopic = "devices/" + deviceId + "/messages/events/";
            this.eventsSubscribeTopic = "devices/" + deviceId + "/messages/devicebound/#";
            this.inputsSubscribeTopic = null;
        }
        else
        {
            this.publishTopic = "devices/" + deviceId + "/modules/" + moduleId +"/messages/events/";
            this.eventsSubscribeTopic = "devices/" + deviceId + "/modules/" + moduleId + "/messages/devicebound/#";
            this.inputsSubscribeTopic = "devices/" + deviceId + "/modules/" + moduleId +"/inputs/#";
        }

        this.moduleId = moduleId;
        this.isEdgeHub = isEdgeHub;
    }

    public void start() throws TransportException
    {
        this.connect();

        if (!this.isEdgeHub)
        {
            this.subscribe(this.eventsSubscribeTopic);
        }
        else if (this.moduleId != null && !this.moduleId.isEmpty())
        {
            this.subscribe(this.inputsSubscribeTopic);
        }
    }

    public void stop()
    {
        this.disconnect();
    }

    /**
     * Sends the provided telemetry message over the mqtt connection
     *
     * @param message the message to send
     * @throws TransportException if any exception is encountered while sending the message
     */
    public void send(Message message) throws TransportException
    {
        if (message == null || message.getBytes() == null)
        {
            throw new IllegalArgumentException("Message cannot be null");
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.publishTopic);

        boolean separatorNeeded;

        separatorNeeded = appendPropertyIfPresent(stringBuilder, false, MESSAGE_ID, message.getMessageId(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, CORRELATION_ID, message.getCorrelationId(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, USER_ID, message.getUserId(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, TO, message.getTo(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, OUTPUT_NAME, message.getOutputName(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, CONNECTION_DEVICE_ID, message.getConnectionDeviceId(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, CONNECTION_MODULE_ID, message.getConnectionModuleId(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, CONTENT_ENCODING, message.getContentEncoding(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, CONTENT_TYPE, message.getContentType(), false);
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, CREATION_TIME_UTC, message.getCreationTimeUTCString(), false);
        if (message.isSecurityMessage())
        {
            separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, MQTT_SECURITY_INTERFACE_ID, MessageProperty.IOTHUB_SECURITY_INTERFACE_ID_VALUE, false);
        }

        for (MessageProperty property : message.getProperties())
        {
            separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, property.getName(), property.getValue(), true);
        }

        if (this.moduleId != null && !this.moduleId.isEmpty())
        {
            stringBuilder.append("/");
        }

        String messagePublishTopic = stringBuilder.toString();

        this.publish(messagePublishTopic, message);
    }

    /**
     * Appends the property to the provided stringbuilder if the property value is not null.
     * @param stringBuilder the builder to build upon
     * @param separatorNeeded if a separator should precede the new property
     * @param propertyKey the mqtt topic string property key
     * @param propertyValue the property value (message id, correlation id, etc.)
     * @return true if a separator will be needed for any later properties appended on
     */
    private boolean appendPropertyIfPresent(StringBuilder stringBuilder, boolean separatorNeeded, String propertyKey, String propertyValue, boolean isApplicationProperty) throws TransportException
    {
        try
        {
            if (propertyValue != null && !propertyValue.isEmpty())
            {
                if (separatorNeeded)
                {
                    stringBuilder.append(MESSAGE_PROPERTY_SEPARATOR);
                }

                if (isApplicationProperty)
                {
                    // URLEncoder.Encode incorrectly encodes space characters as '+'. For MQTT to work, we need to replace those '+' with "%20"
                    stringBuilder.append(URLEncoder.encode(propertyKey, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20"));
                }
                else
                {
                    stringBuilder.append(propertyKey);
                }

                stringBuilder.append(MESSAGE_PROPERTY_KEY_VALUE_SEPARATOR);

                // URLEncoder.Encode incorrectly encodes space characters as '+'. For MQTT to work, we need to replace those '+' with "%20"
                stringBuilder.append(URLEncoder.encode(propertyValue, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20"));

                return true;
            }

            return separatorNeeded;
        }
        catch (UnsupportedEncodingException e)
        {
            throw new TransportException("Could not utf-8 encode the property with name " + propertyKey + " and value " + propertyValue, e);
        }
    }
}
