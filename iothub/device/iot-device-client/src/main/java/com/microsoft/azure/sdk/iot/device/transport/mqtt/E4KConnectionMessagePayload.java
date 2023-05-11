package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.google.gson.annotations.Expose;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * The payload object to be sent in all connect/disconnect messages sent to the E4K MQTT broker. Not to be confused
 * with the payload of the MQTT CONNECT/DISCONNECT packet.
 */
@Builder
public class E4KConnectionMessagePayload
{
    @Expose(deserialize = false)
    @Getter
    @Setter
    private E4KConnectionState connectionState;

    @Expose(deserialize = false)
    @Getter
    @Setter
    private String deviceClientType;

    @Expose(deserialize = false)
    @Getter
    @Setter
    private String mqttVersion = "3.1.1"; // this SDK currently only ever uses 3.1.1

    @Expose(deserialize = false)
    @Getter
    @Setter
    private String modelId;
}
