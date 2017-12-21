package com.microsoft.azure.sdk.iot.deps.transport.mqtt;

public interface MqttListener
{
    /**
     * Called when the message gets received by PAHO
     * @param message
     */
    void messageReceived(MqttMessage message);

    /**
     * Called when PAHO establishes a connection to a server
     */
    void connectionEstablished();

    /**
     * Called by PAHO when the connection is lost
     */
    void connectionLost(Throwable throwable);
}
