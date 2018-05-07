// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubListener;

public class MqttMessaging extends Mqtt
{
    private String subscribeTopic;
    private String publishTopic;
    private String parseTopic;

    public MqttMessaging(MqttConnection mqttConnection, String deviceId, IotHubListener listener, MqttMessageListener messageListener, String connectionId) throws TransportException
    {
        //Codes_SRS_MqttMessaging_25_002: [The constructor shall use the configuration to instantiate super class and passing the parameters.]
        super(mqttConnection, listener, messageListener, connectionId);

        if (deviceId == null || deviceId.isEmpty())
        {
            //Codes_SRS_MqttMessaging_25_001: [The constructor shall throw IllegalArgumentException if any of the parameters are null or empty .]
            throw new IllegalArgumentException("Device id cannot be null or empty");
        }

        //Codes_SRS_MqttMessaging_25_003: [The constructor construct publishTopic and subscribeTopic from deviceId.]
        //Codes_SRS_MqttMessaging_25_004: [The constructor shall save the provided listener.]
        this.publishTopic = "devices/" + deviceId + "/messages/events/";
        this.subscribeTopic = "devices/" + deviceId + "/messages/devicebound/#";
        this.parseTopic = "devices/" + deviceId + "/messages/devicebound/";
    }

    public void start() throws TransportException
    {
        //Codes_SRS_MqttMessaging_25_020: [start method shall be call connect to establish a connection to IOT Hub with the given configuration.]
        //Codes_SRS_MqttMessaging_25_021: [start method shall subscribe to messaging subscribe topic once connected.]
        this.connect();
        this.subscribe(subscribeTopic);
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

        if (message.getMessageId() != null)
        {
            //Codes_SRS_MqttMessaging_21_027: [send method shall append the messageid to publishTopic before publishing using the key name `$.mid`.]
            stringBuilder.append(MESSAGE_ID);
            stringBuilder.append(MESSAGE_PROPERTY_KEY_VALUE_SEPARATOR);
            stringBuilder.append(message.getMessageId());

            separatorNeeded = true;
        }

        if (message.getCorrelationId() != null)
        {
            if (separatorNeeded)
            {
                stringBuilder.append(MESSAGE_PROPERTY_SEPARATOR);
            }

            //Codes_SRS_MqttMessaging_34_028: [If the message has a correlationId, this method shall append that correlationid to publishTopic before publishing using the key name `$.cid`.]
            stringBuilder.append(CORRELATION_ID);
            stringBuilder.append(MESSAGE_PROPERTY_KEY_VALUE_SEPARATOR);
            stringBuilder.append(message.getCorrelationId());

            separatorNeeded = true;
        }

        if (message.getUserId() != null)
        {
            if (separatorNeeded)
            {
                stringBuilder.append(MESSAGE_PROPERTY_SEPARATOR);
            }

            //Codes_SRS_MqttMessaging_34_030: [If the message has a UserId, this method shall append that userId to publishTopic before publishing using the key name `$.uid`.]
            stringBuilder.append(USER_ID);
            stringBuilder.append(MESSAGE_PROPERTY_KEY_VALUE_SEPARATOR);
            stringBuilder.append(message.getUserId());

            separatorNeeded = true;
        }

        if (message.getTo() != null)
        {
            if (separatorNeeded)
            {
                stringBuilder.append(MESSAGE_PROPERTY_SEPARATOR);
            }

            //Codes_SRS_MqttMessaging_34_029: [If the message has a To, this method shall append that To to publishTopic before publishing using the key name `$.to`.]
            stringBuilder.append(TO);
            stringBuilder.append(MESSAGE_PROPERTY_KEY_VALUE_SEPARATOR);
            stringBuilder.append(message.getTo());

            separatorNeeded = true;
        }

        for (MessageProperty property : message.getProperties())
        {
            if (separatorNeeded)
            {
                stringBuilder.append(MESSAGE_PROPERTY_SEPARATOR);
            }

            //Codes_SRS_MqttMessaging_34_026: [This method shall append each custom property's name and value to the publishTopic before publishing.]
            stringBuilder.append(property.getName());
            stringBuilder.append(MESSAGE_PROPERTY_KEY_VALUE_SEPARATOR);
            stringBuilder.append(property.getValue());

            separatorNeeded = true;
        }

        String messagePublishTopic = stringBuilder.toString();

        //Codes_SRS_MqttMessaging_25_024: [send method shall publish a message to the IOT Hub on the publish topic by calling method publish().]
        this.publish(messagePublishTopic, message);
    }

    private void throwTelemetryTransportException(Exception e) throws TransportException
    {
        TransportException transportException = new TransportException(e);
        transportException.setIotHubService(TransportException.IotHubService.TELEMETRY);
        throw transportException;
    }
}
