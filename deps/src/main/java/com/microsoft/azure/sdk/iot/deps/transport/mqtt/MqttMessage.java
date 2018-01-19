/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.transport.mqtt;

public class MqttMessage
{
    private String topic;
    private byte[] payload;
    private MqttQos qos = MqttQos.DELIVER_UNKNOWN;

    /**
     * Constructor that takes the topic of the Message
     * @param topic The topic of the message
     */
    public MqttMessage(String topic)
    {
        this.topic = topic;
    }

    /**
     * Constructor that takes the topic of the Message and a PAHO MqttMessage
     * @param topic The topic of the message
     * @param mqttMessage The mqtt message
     */
    public MqttMessage(String topic, org.eclipse.paho.client.mqttv3.MqttMessage mqttMessage)
    {
        if (mqttMessage == null)
        {
            throw new IllegalArgumentException();
        }

        this.topic = topic;
        this.payload = mqttMessage.getPayload();
        int qosValue = mqttMessage.getQos();
        if (qosValue == 0)
        {
            this.qos = MqttQos.DELIVER_AT_MOST_ONCE;
        }
        else if (qosValue == 2)
        {
            this.qos = MqttQos.DELIVER_EXACTLY_ONCE;
        }
        else if (qosValue == 3)
        {
            this.qos = MqttQos.DELIVER_AT_LEAST_ONCE;
        }
        else
        {
            this.qos = MqttQos.DELIVER_FAILURE;
        }
    }

    /**
     * Constructor that takes the topic of the Message and payload
     * @param topic The topic of the message
     * @param payload The payload of the message
     */
    public MqttMessage(String topic, byte[] payload)
    {
        this.topic = topic;
        this.payload = payload;
    }

    /**
     * Converts the MqttQOS value to a integer representation
     * @param qos MqttQos value
     * @return the qos value
     */
    public static int retrieveQosValue(MqttQos qos)
    {
        int result;
        if (qos == MqttQos.DELIVER_AT_MOST_ONCE)
        {
            result = 0;
        }
        else if (qos == MqttQos.DELIVER_AT_LEAST_ONCE)
        {
            result = 1;
        }
        else if (qos == MqttQos.DELIVER_EXACTLY_ONCE)
        {
            result = 2;
        }
        else
        {
            result = 128;
        }
        return result;
    }

    /**
     * Sets the topic of the message
     * @param topic The topic of the message
     */
    public void setTopic(String topic)
    {
        this.topic = topic;
    }

    /**
     * Gets the topic of the message
     * @return Topic represented as a string
     */
    public String getTopic()
    {
        return this.topic;
    }

    /**
     * Returns the Message Payload
     * @return the message in byte[]
     */
    public byte[] getPayload()
    {
        return this.payload;
    }

    /**
     * Sets the Message Payload
     * @param payload the message in byte[]
     */
    public void setPayload(byte[] payload)
    {
        this.payload = payload;
    }

    /**
     * Sets the Qos value of the Message
     * @param qos the quality of service
     */
    public void setQos(MqttQos qos)
    {
        this.qos = qos;
    }

    /**
     * Gets the Qos value of the Message
     * @return the quality of service
     */
    public MqttQos getQos()
    {
        return this.qos;
    }

    /**
     * Gets the PAHO MqttMessage from an existing message
     * @return the mqtt message
     */
    public org.eclipse.paho.client.mqttv3.MqttMessage getMqttMessage()
    {
        org.eclipse.paho.client.mqttv3.MqttMessage mqttMessage = new org.eclipse.paho.client.mqttv3.MqttMessage();
        if (this.payload != null)
        {
            mqttMessage.setPayload(this.payload);
        }
        if (this.qos != MqttQos.DELIVER_UNKNOWN)
        {
            mqttMessage.setQos(retrieveQosValue(this.qos));
        }
        return mqttMessage;
    }
}
