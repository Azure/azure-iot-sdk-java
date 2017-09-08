package com.microsoft.azure.sdk.iot.device.transport.amqps;

public class AmqpsSendReturnValue
{
    private boolean deliverySuccessful;
    private int  deliveryHash;

    AmqpsSendReturnValue(boolean deliverySuccessful, int deliveryHash)
    {
        // Codes_SRS_AMQPSSENDRETURNVALUE_12_001: [The constructor shall initialize deliverySuccessful and deliveryHash private member variables with the given arguments.]
        this.deliverySuccessful = deliverySuccessful;
        this.deliveryHash = deliveryHash;
    }

    boolean isDeliverySuccessful()
    {
        // Codes_SRS_AMQPSSENDRETURNVALUE_12_002: [The function shall return the current value of deliverySuccessful private member.]
        return deliverySuccessful;
    }

    int getDeliveryHash()
    {
        // Codes_SRS_AMQPSSENDRETURNVALUE_12_003: [The function shall return the current value of deliveryHash private member.]
        return deliveryHash;
    }
}
