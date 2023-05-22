package com.microsoft.azure.sdk.iot.device.transport.mqtt5;

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
    /**
     * The connection state to report. For instance, after connecting to the MQTT broker, this SDK sends a message
     * with {@link E4KConnectionState#Connected}. Before disconnecting, this SDK sends a message with
     * {@link E4KConnectionState#Disconnected}.
     */
    @Expose(deserialize = false)
    @Getter
    @Setter
    private E4KConnectionState connectionState;

    /**
     * The user agent string for this client.
     */
    @Expose(deserialize = false)
    @Getter
    @Setter
    private String deviceClientType;

    /**
     * The version of MQTT that this client uses to connect to the MQTT broker.
     */
    @Expose(deserialize = false)
    @Getter
    @Setter
    private String mqttVersion;

    /**
     * The optional PnP model Id for this client.
     */
    @Expose(deserialize = false)
    @Getter
    @Setter
    private String modelId;
}
