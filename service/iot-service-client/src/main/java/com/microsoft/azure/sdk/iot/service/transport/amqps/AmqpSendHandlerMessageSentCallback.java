package com.microsoft.azure.sdk.iot.service.transport.amqps;

public interface AmqpSendHandlerMessageSentCallback
{
    /**
     * This callback is executed when a sent message is acknowledged by the service
     * @param deliveryAcknowledgement the acknowledgement of the delivery
     */
    public void onMessageSent(AmqpResponseVerification deliveryAcknowledgement);
}
