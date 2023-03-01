package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.transport.TransportException;

import java.util.function.Consumer;

public interface IMqttClient
{
    /**
     * Send the MQTT CONNECT packet to open the connection.
     * @param settings the settings to open the connection with.
     * @throws TransportException if any error is encountered while opening the connection. This includes when the
     * service returns a connect code other than 0
     */
    void connect(MqttConnectSettings settings) throws TransportException;

    /**
     * Send the MQTT DISCONNECT packet and close the client.
     */
    void disconnect() throws TransportException;

    void subscribe(String topic, int qos) throws TransportException;

    void unsubscribe(String topic) throws TransportException;

    void publishAsync(String topic, byte[] payload, int qos, Runnable onMessageAcknowledged, Consumer<TransportException> onFailure);

    void acknowledgeMessageAsync(int messageId, int qos);

    void setMessageCallback(Consumer<ReceivedMqttMessage> messageCallback);

    void setConnectionLostCallback(Consumer<TransportException> connectionLossEvent);
}
