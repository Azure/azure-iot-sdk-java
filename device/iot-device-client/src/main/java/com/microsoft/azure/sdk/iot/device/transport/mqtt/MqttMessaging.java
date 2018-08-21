// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MqttMessaging extends Mqtt
{
    private String moduleId;
    private String eventsSubscribeTopic;
    private String inputsSubscribeTopic;
    private String publishTopic;
    private boolean isEdgeHub;

    public MqttMessaging(MqttConnection mqttConnection, String deviceId, IotHubListener listener, MqttMessageListener messageListener, String connectionId, String moduleId, boolean isEdgeHub) throws TransportException
    {
        //Codes_SRS_MqttMessaging_25_002: [The constructor shall use the configuration to instantiate super class and passing the parameters.]
        super(mqttConnection, listener, messageListener, connectionId);

        if (deviceId == null || deviceId.isEmpty())
        {
            //Codes_SRS_MqttMessaging_25_001: [The constructor shall throw IllegalArgumentException if any of the parameters are null or empty .]
            throw new IllegalArgumentException("Device id cannot be null or empty");
        }

        if (moduleId == null || moduleId.isEmpty())
        {
            //Codes_SRS_MqttMessaging_25_003: [The constructor construct publishTopic and eventsSubscribeTopic from deviceId.]
            this.publishTopic = "devices/" + deviceId + "/messages/events/";
            this.eventsSubscribeTopic = "devices/" + deviceId + "/messages/devicebound/#";
            this.inputsSubscribeTopic = null;
        }
        else
        {
            //Codes_SRS_MqttMessaging_34_031: [The constructor construct publishTopic and eventsSubscribeTopic from deviceId and moduleId.]
            this.publishTopic = "devices/" + deviceId + "/modules/" + moduleId +"/messages/events/";
            this.eventsSubscribeTopic = "devices/" + deviceId + "/modules/" + moduleId + "/messages/devicebound/#";
            this.inputsSubscribeTopic = "devices/" + deviceId + "/modules/" + moduleId +"/inputs/#";
        }

        this.moduleId = moduleId;
        this.isEdgeHub = isEdgeHub;
    }

    public void start() throws TransportException
    {
        //Codes_SRS_MqttMessaging_25_020: [start method shall be call connect to establish a connection to IOT Hub with the given configuration.]
        this.connect();

        if (!this.isEdgeHub)
        {
            //Codes_SRS_MqttMessaging_34_035: [start method shall subscribe to the cloud to device events if not communicating to an edgeHub.]
            this.subscribe(this.eventsSubscribeTopic);
        }

        if (this.moduleId != null && !this.moduleId.isEmpty())
        {
            //Codes_SRS_MqttMessaging_34_036: [start method shall subscribe to the inputs channel if communicating as a module.]
            this.subscribe(this.inputsSubscribeTopic);
        }
    }

    public void stop() throws TransportException
    {
        //Codes_SRS_MqttMessaging_25_022: [stop method shall be call disconnect to tear down a connection to IOT Hub with the given configuration.]
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
            //Codes_SRS_MqttMessaging_25_025: [send method shall throw an IllegalArgumentException if the message is null.]
            throw new IllegalArgumentException("Message cannot be null");
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.publishTopic);

        boolean separatorNeeded = false;

        //Codes_SRS_MqttMessaging_34_029: [If the message has a To, this method shall append that To to publishTopic before publishing using the key name `$.to`.]
        //Codes_SRS_MqttMessaging_34_030: [If the message has a UserId, this method shall append that userId to publishTopic before publishing using the key name `$.uid`.]
        //Codes_SRS_MqttMessaging_34_028: [If the message has a correlationId, this method shall append that correlationid to publishTopic before publishing using the key name `$.cid`.]
        //Codes_SRS_MqttMessaging_21_027: [send method shall append the messageid to publishTopic before publishing using the key name `$.mid`.]
        //Codes_SRS_MqttMessaging_34_026: [This method shall append each custom property's name and value to the publishTopic before publishing.]
        //Codes_SRS_MqttMessaging_34_032: [If the message has a OutputName, this method shall append that to publishTopic before publishing using the key name `$.on`.]
        //Codes_SRS_MqttMessaging_34_032: [If the message has a content type, this method shall append that to publishTopic before publishing using the key name `$.ct`.]
        //Codes_SRS_MqttMessaging_34_032: [If the message has a content encoding, this method shall append that to publishTopic before publishing using the key name `$.ce`.]
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, MESSAGE_ID, message.getMessageId());
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, CORRELATION_ID, message.getCorrelationId());
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, USER_ID, message.getUserId());
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, TO, message.getTo());
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, OUTPUT_NAME, message.getOutputName());
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, CONNECTION_DEVICE_ID, message.getConnectionDeviceId());
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, CONNECTION_MODULE_ID, message.getConnectionModuleId());
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, CONTENT_ENCODING, message.getContentEncoding());
        separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, CONTENT_TYPE, message.getContentType());

        for (MessageProperty property : message.getProperties())
        {
            separatorNeeded = appendPropertyIfPresent(stringBuilder, separatorNeeded, property.getName(), property.getValue());
        }

        if (this.moduleId != null && !this.moduleId.isEmpty())
        {
            stringBuilder.append("/");
        }

        String messagePublishTopic = stringBuilder.toString();

        //Codes_SRS_MqttMessaging_25_024: [send method shall publish a message to the IOT Hub on the publish topic by calling method publish().]
        this.publish(messagePublishTopic, message);
    }

    /**
     * Appends the property to the provided stringbuilder if the property value is not null.
     * @param stringBuilder the builder to build upon
     * @param separatorNeeded if a seperator should precede the new property
     * @param propertyKey the mqtt topic string property key
     * @param propertyValue the property value (message id, correlation id, etc.)
     * @return true if a separator will be needed for any later properties appended on
     */
    private boolean appendPropertyIfPresent(StringBuilder stringBuilder, boolean separatorNeeded, String propertyKey, String propertyValue) throws TransportException
    {
        try
        {
            if (propertyValue != null && !propertyValue.isEmpty())
            {
                if (separatorNeeded)
                {
                    stringBuilder.append(MESSAGE_PROPERTY_SEPARATOR);
                }

                stringBuilder.append(propertyKey);
                stringBuilder.append(MESSAGE_PROPERTY_KEY_VALUE_SEPARATOR);
                stringBuilder.append(URLEncoder.encode(propertyValue, StandardCharsets.UTF_8.name()));

                return true;
            }

            return separatorNeeded;
        }
        catch (UnsupportedEncodingException e)
        {
            throw new TransportException("Could not utf-8 encode the mqtt property", e);
        }
    }
}
