package com.microsoft.azure.sdk.iot.device.transport.amqps;

public class AmqpsSendReturnValue
{
    private boolean deliverySuccessful;
    private int  deliveryHash;

    public AmqpsSendReturnValue(boolean deliverySuccessful, int deliveryHash)
    {
        this.deliverySuccessful = deliverySuccessful;
        this.deliveryHash = deliveryHash;
    }

    public boolean isDeliverySuccessful()
    {
        return deliverySuccessful;
    }

    public int getDeliveryHash()
    {
        return deliveryHash;
    }
}
