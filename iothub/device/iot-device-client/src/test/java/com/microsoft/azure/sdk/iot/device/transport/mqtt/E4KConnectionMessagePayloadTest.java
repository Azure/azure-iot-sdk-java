package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.google.gson.Gson;
import com.microsoft.azure.sdk.iot.device.transport.mqtt5.E4KConnectionMessagePayload;
import com.microsoft.azure.sdk.iot.device.transport.mqtt5.E4KConnectionState;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class E4KConnectionMessagePayloadTest
{
    @Test
    public void TestSerialization()
    {
        String expectedDeviceClientType = "some device client type";
        String expectedMqttVersion = "5.0.0";
        String expectedModelId = "someModelId";
        E4KConnectionState expectedConnectionState = E4KConnectionState.Connected;

        String expectedJson =
            "{" +
                "\"connectionState\":\"" + expectedConnectionState + "\"," +
                "\"deviceClientType\":\"" + expectedDeviceClientType + "\"," +
                "\"mqttVersion\":\"" + expectedMqttVersion + "\"," +
                "\"modelId\":\"" + expectedModelId + "\"" +
            "}";

        E4KConnectionMessagePayload payload = E4KConnectionMessagePayload.builder()
            .deviceClientType(expectedDeviceClientType)
            .connectionState(expectedConnectionState)
            .mqttVersion(expectedMqttVersion)
            .modelId(expectedModelId)
            .build();

        String actualJson = new Gson().toJson(payload);
        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void TestSerializationWithoutModelId()
    {
        String expectedDeviceClientType = "some device client type";
        String expectedMqttVersion = "5.0.0";
        E4KConnectionState expectedConnectionState = E4KConnectionState.Connected;

        String expectedJson =
            "{" +
                "\"connectionState\":\"" + expectedConnectionState + "\"," +
                "\"deviceClientType\":\"" + expectedDeviceClientType + "\"," +
                "\"mqttVersion\":\"" + expectedMqttVersion + "\"" +
            "}";

        E4KConnectionMessagePayload payload = E4KConnectionMessagePayload.builder()
            .deviceClientType(expectedDeviceClientType)
            .connectionState(expectedConnectionState)
            .mqttVersion(expectedMqttVersion)
            .build();

        String actualJson = new Gson().toJson(payload);
        assertEquals(expectedJson, actualJson);
    }
}
