package com.microsoft.azure.sdk.iot.device.transport.mqtt;

public interface MqttConnectionStateListener
{
    /**
     * Method executed when the connection with the IoTHub is lost.
     */
    void onConnectionLost(Throwable throwable);

    /**
     * Method executed when the connection with the IoTHub is established
     */
    void onConnectionEstablished();
}
