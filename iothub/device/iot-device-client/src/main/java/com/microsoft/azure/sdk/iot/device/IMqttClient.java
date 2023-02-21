package com.microsoft.azure.sdk.iot.device;

import org.eclipse.paho.client.mqttv3.*;

import java.util.function.Consumer;

public interface IMqttClient
{
    //TODO all methods should be async

    public void connect(MqttConnectOptions options);

    public void disconnect();

    public void subscribe(String topic);

    public void unsubscribe(String topic);

    public void publish(String topic, byte[] payload, int qos);

    public void acknowledgeMessage(int messageId);

    public void setMessageCallback(Consumer<ReceivedMqttMessage> messageCallback);

    public void setConnectionLostCallback(Consumer<Integer> connectionLossEvent); //TODO error codes, stacktrace, etc.
}
