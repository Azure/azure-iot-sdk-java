package com.microsoft.azure.sdk.iot.device.transport.amqps;

import lombok.Getter;

import java.nio.charset.StandardCharsets;

public class AmqpsSendResult
{
    private static final int failedDeliveryTag = -1;

    @Getter
    private final boolean deliverySuccessful;
    @Getter
    private final int deliveryTag;

    /**
     * Create a return value object containing the delivery status and the delivery hash
     *
     */
    AmqpsSendResult()
    {
        this.deliverySuccessful = false;
        this.deliveryTag = failedDeliveryTag;
    }

    AmqpsSendResult(byte[] deliveryTag)
    {
        this.deliverySuccessful = true;
        this.deliveryTag = Integer.parseInt(new String(deliveryTag, StandardCharsets.UTF_8));
    }
}
