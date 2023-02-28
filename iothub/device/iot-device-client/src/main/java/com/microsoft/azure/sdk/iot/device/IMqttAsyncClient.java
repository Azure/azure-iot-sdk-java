package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions.MqttConnectException;

import java.util.function.Consumer;

public interface IMqttAsyncClient
{
    void connect(MqttConnectOptions options) throws MqttConnectException;

    void disconnectAsync(Consumer<Integer> onDisconnectionAcknowledged);

    void subscribeAsync(String topic, int qos, Consumer<Integer> onSubscriptionAcknowledged);

    void unsubscribe(String topic, Consumer<Integer> onUnsubscriptionAcknowledged);

    void publishAsync(String topic, byte[] payload, int qos, Consumer<Integer> onMessageAcknowledged);

    void acknowledgeMessageAsync(int messageId, int qos);

    void setMessageCallback(Consumer<ReceivedMqttMessage> messageCallback);

    void setConnectionLostCallback(Consumer<Integer> connectionLossEvent); //TODO error codes, stacktrace, etc.
}
