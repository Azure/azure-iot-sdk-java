package com.microsoft.azure.sdk.iot.service.transport.amqps;

public interface AmqpMessageSentCallback
{
    /**
     * This callback is executed when a sent message is acknowledged by the service
     * @param acknowledgement the details of the acknowledgement for the sent message. Includes delivery state
     *                      (Accepted, Received, Rejected, Released, or Modified), and exception details, if any exception occurred
     */
    public void onMessageSent(AmqpMessageAcknowledgement acknowledgement);
}
