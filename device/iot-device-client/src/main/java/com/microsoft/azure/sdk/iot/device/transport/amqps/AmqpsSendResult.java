package com.microsoft.azure.sdk.iot.device.transport.amqps;

import lombok.Getter;

public class AmqpsSendResult
{
    private static final int failedDeliveryTag = -1;

    @Getter
    private boolean deliverySuccessful;
    @Getter
    private int deliveryTag;

    /**
     * Create a return value object containing the delivery status and the delivery hash
     *
     * @param deliverySuccessful the delivery state
     */
    AmqpsSendResult(boolean deliverySuccessful)
    {
        this.deliverySuccessful = deliverySuccessful;
        this.deliveryTag = failedDeliveryTag;
    }

    AmqpsSendResult(boolean deliverySuccessful, byte[] deliveryTag)
    {
        this.deliverySuccessful = deliverySuccessful;
        this.deliveryTag = Integer.parseInt(new String(deliveryTag));
    }
}
