// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;

import java.io.IOException;

public class MqttMessaging extends Mqtt
{
    private String subscribeTopic;
    private String publishTopic;
    private String parseTopic;

    public MqttMessaging(MqttConnection mqttConnection, String deviceId) throws IOException
    {
        /*
        **Codes_SRS_MqttMessaging_25_001: [**The constructor shall throw InvalidParameter Exception if any of the parameters are null or empty .**]**
         */
        /*
        **Codes_SRS_MqttMessaging_25_002: [**The constructor shall use the configuration to instantiate super class and passing the parameters.**]**
         */
        super(mqttConnection);

        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("Device id cannot be null or empty");
        }
        /*
        **Codes_SRS_MqttMessaging_25_003: [**The constructor construct publishTopic and subscribeTopic from deviceId.**]**
         */
        this.publishTopic = "devices/" + deviceId + "/messages/events/";
        this.subscribeTopic = "devices/" + deviceId + "/messages/devicebound/#";
        this.parseTopic = "devices/" + deviceId + "/messages/devicebound/";

    }

    public void start() throws IOException
    {
        /*
        **Codes_SRS_MqttMessaging_25_020: [**start method shall be call connect to establish a connection to IOT Hub with the given configuration.**]**

        **Codes_SRS_MqttMessaging_25_021: [**start method shall subscribe to messaging subscribe topic once connected.**]**
         */

        this.connect();
        this.subscribe(subscribeTopic);
    }

    public void stop() throws IOException
    {
       /*
       **Codes_SRS_MqttMessaging_25_022: [**stop method shall be call disconnect to tear down a connection to IOT Hub with the given configuration.**]**
       */
       this.disconnect();
    }

    public void send(Message message) throws IOException
    {
        if (message == null || message.getBytes() == null)
        {
            /*
            **Codes_SRS_MqttMessaging_25_025: [**send method shall throw an exception if the message is null.**]**
             */
            throw new IOException("Message cannot be null");
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

        for(MessageProperty property : message.getProperties())
        {
            if (separatorNeeded)
            {
                stringBuilder.append(MESSAGE_PROPERTY_SEPARATOR);
            }

            /*
            **Codes_SRS_MqttMessaging_34_026: [This method shall append each custom property's name and value to the publishTopic before publishing.]
            */
            stringBuilder.append(property.getName());
            stringBuilder.append(MESSAGE_PROPERTY_KEY_VALUE_SEPARATOR);
            stringBuilder.append(property.getValue());

            separatorNeeded = true;
        }

        String messagePublishTopic = stringBuilder.toString();

        //Codes_SRS_MqttMessaging_25_024: [send method shall publish a message to the IOT Hub on the publish topic by calling method publish().]
        this.publish(messagePublishTopic, message.getBytes());
    }
}
