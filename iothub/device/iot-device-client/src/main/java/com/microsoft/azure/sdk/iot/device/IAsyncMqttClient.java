package com.microsoft.azure.sdk.iot.device;

import java.util.function.Consumer;

public interface IAsyncMqttClient
{
    void connectAsync(MqttConnectOptions options, Consumer<Integer> onConnectionAcknowledged);

    void disconnectAsync(Consumer<Integer> onDisconnectionAcknowledged);

    void subscribeAsync(String topic, int qos, Consumer<Integer> onSubscriptionAcknowledged);

    void unsubscribe(String topic, Consumer<Integer> onUnsubscriptionAcknowledged);

    void publishAsync(String topic, byte[] payload, int qos, Consumer<Integer> onMessageAcknowledged);

    void acknowledgeMessageAsync(int messageId, int qos);

    void setMessageCallback(Consumer<ReceivedMqttMessage> messageCallback);

    void setConnectionLostCallback(Consumer<Integer> connectionLossEvent); //TODO error codes, stacktrace, etc.
}
