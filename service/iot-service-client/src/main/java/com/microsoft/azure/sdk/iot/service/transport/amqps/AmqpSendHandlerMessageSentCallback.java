package com.microsoft.azure.sdk.iot.service.transport.amqps;

public interface AmqpSendHandlerMessageSentCallback
{
    public void onMessageSent(AmqpResponseVerification deliveryAcknowledgement);
}
