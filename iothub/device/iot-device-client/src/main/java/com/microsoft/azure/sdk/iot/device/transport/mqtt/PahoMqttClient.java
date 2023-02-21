package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.IMqttClient;
import com.microsoft.azure.sdk.iot.device.MqttConnectOptions;
import com.microsoft.azure.sdk.iot.device.ReceivedMqttMessage;
import org.eclipse.paho.client.mqttv3.*;

import java.util.function.Consumer;

public class PahoMqttClient implements IMqttClient, MqttCallback
{
    private MqttClient pahoClient;
    private Consumer<ReceivedMqttMessage> messageCallback;
    private Consumer<Integer> connectionLossEvent;

    @Override
    public void connect(MqttConnectOptions options)
    {
        try
        {
            this.pahoClient = new MqttClient(options.getServerUri(), options.getClientId());
        }
        catch (MqttException e)
        {
            //TODO
            e.printStackTrace();
        }

        this.pahoClient.setCallback(this);

        org.eclipse.paho.client.mqttv3.MqttConnectOptions pahoOptions =
                new org.eclipse.paho.client.mqttv3.MqttConnectOptions();
        pahoOptions.setUserName(options.getUsername());
        pahoOptions.setPassword(options.getPassword());
        pahoOptions.setMqttVersion(4); //TODO hardcoded for now
        pahoOptions.setKeepAliveInterval(options.getKeepAlivePeriod());

        try
        {
            this.pahoClient.connect(pahoOptions);
        }
        catch (MqttException e)
        {
            //TODO
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect()
    {
        try
        {
            this.pahoClient.disconnect();
        }
        catch (MqttException e)
        {
            //TODO
            e.printStackTrace();
        }
    }

    @Override
    public void subscribe(String topic)
    {
        try
        {
            this.pahoClient.subscribe(topic);
        }
        catch (MqttException e)
        {
            //TODO
            e.printStackTrace();
        }
    }

    @Override
    public void unsubscribe(String topic)
    {
        try
        {
            this.pahoClient.unsubscribe(topic);
        }
        catch (MqttException e)
        {
            //TODO
            e.printStackTrace();
        }
    }

    @Override
    public void publish(String topic, byte[] payload, int qos)
    {
        try
        {
            MqttMessage pahoMessage = new MqttMessage(payload);
            pahoMessage.setQos(qos);
            this.pahoClient.publish(topic, pahoMessage);
        }
        catch (MqttException e)
        {
            //TODO
            e.printStackTrace();
        }
    }

    @Override
    public void acknowledgeMessage(int messageId)
    {
        try
        {
            this.pahoClient.messageArrivedComplete(messageId, 0); //TODO qos?
        }
        catch (MqttException e)
        {
            //TODO
            e.printStackTrace();
        }
    }

    @Override
    public void setMessageCallback(Consumer<ReceivedMqttMessage> messageCallback)
    {
        this.messageCallback = messageCallback;
    }

    @Override
    public void setConnectionLostCallback(Consumer<Integer> connectionLossEvent)
    {
        this.connectionLossEvent = connectionLossEvent;
    }

    @Override
    public void connectionLost(Throwable throwable)
    {
        this.connectionLossEvent.accept(0); //todo details
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception
    {
        ReceivedMqttMessage message = ReceivedMqttMessage.builder()
                .payload(mqttMessage.getPayload())
                .topic(topic)
                .qos(mqttMessage.getQos())
                .build();

        this.messageCallback.accept(message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken)
    {
        //TODO
        System.out.println("Message delivered");
    }
}
