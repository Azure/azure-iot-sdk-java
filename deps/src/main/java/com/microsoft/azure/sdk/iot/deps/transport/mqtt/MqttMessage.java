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
     * @param topic
     * @param mqttMessage
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
     * @param topic
     * @param payload
     */
    public MqttMessage(String topic, byte[] payload)
    {
        this.topic = topic;
        this.payload = payload;
    }

    /**
     * Converts the MqttQOS value to a integer representation
     * @param qos MqttQos value
     * @return
     */
    public static int retrieveQosValue(MqttQos qos)
    {
        switch (qos)
        {
            case DELIVER_AT_MOST_ONCE:
                return 0;
            case DELIVER_AT_LEAST_ONCE:
                return 1;
            case DELIVER_EXACTLY_ONCE:
                return 2;
            default:
            case DELIVER_FAILURE:
                return 128;
        }
    }

    /**
     * Sets the topic of the message
     * @param topic
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
     * @return
     */
    public byte[] getPayload()
    {
        return this.payload;
    }

    /**
     * Sets the Message Payload
     * @param payload
     */
    public void setPayload(byte[] payload)
    {
        this.payload = payload;
    }

    /**
     * Sets the Qos value of the Message
     * @param qos
     */
    public void setQos(MqttQos qos)
    {
        this.qos = qos;
    }

    /**
     * Gets the Qos value of the Message
     * @return
     */
    public MqttQos getQos()
    {
        return this.qos;
    }

    /**
     * Gets the PAHO MqttMessage from an existing message
     * @return
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
