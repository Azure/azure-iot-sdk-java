package com.microsoft.azure.sdk.iot.device.transport.amqps;

public class AmqpsSendReturnValue
{
    private boolean deliverySuccessful;
    private int  deliveryHash;

    /**
     * Create a return value object containing the delivery status and the delivery hash
     *
     * @param deliverySuccessful the delivery state
     * @param deliveryHash the delivery hash
     */
    AmqpsSendReturnValue(boolean deliverySuccessful, int deliveryHash)
    {
        // Codes_SRS_AMQPSSENDRETURNVALUE_12_001: [The constructor shall initialize deliverySuccessful and deliveryHash private member variables with the given arguments.]
        this.deliverySuccessful = deliverySuccessful;
        this.deliveryHash = deliveryHash;
    }

    /**
     * Getter for the delivery status
     *
     * @return true is delivery was successful, false otherwise
     */
    boolean isDeliverySuccessful()
    {
        // Codes_SRS_AMQPSSENDRETURNVALUE_12_002: [The function shall return the current value of deliverySuccessful private member.]
        return deliverySuccessful;
    }

    /**
     * Getter for the delivery hash.
     *
     * @return the delivery hash.
     */
    int getDeliveryHash()
    {
        // Codes_SRS_AMQPSSENDRETURNVALUE_12_003: [The function shall return the current value of deliveryHash private member.]
        return deliveryHash;
    }
}
