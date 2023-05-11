package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class E4KConnectionMessagePayloadTest
{
    @Test
    public void TestSerialization()
    {
        String expectedDeviceClientType = "some device client type";
        String expectedMqttVersion = "3.1.1";
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
}
