package com.microsoft.azure.sdk.iot.device;

import lombok.Builder;
import lombok.Getter;

@Builder
public class ReceivedMqttMessage
{
    @Getter
    private int qos;

    @Getter
    private byte[] payload;

    @Getter
    private String topic;

    @Getter int messageId;
}
