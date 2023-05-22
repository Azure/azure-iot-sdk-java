package com.microsoft.azure.sdk.iot.device.transport.mqtt5;

/**
 * The connection state to report to the E4K MQTT broker after connecting and before disconnecting.
 */
public enum E4KConnectionState
{
    /**
     * The client has successfully connected to the MQTT broker.
     */
    Connected,

    /**
     * The client is about to disconnect from the MQTT broker.
     */
    Disconnected,
}
