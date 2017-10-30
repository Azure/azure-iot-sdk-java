package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;

public class AmqpsConvertFromProtonReturnValue
{
    private Message message;

    private MessageCallback messageCallback;

    private Object messageContext;

    /**
     * Create a return value object containing the message, callback and user context.
     *
     * @param message the IoTHub message
     * @param messageCallback the associated callback.
     * @param messageContext the associated user context.
     */
    AmqpsConvertFromProtonReturnValue(Message message, MessageCallback messageCallback, Object messageContext)
    {
        // Codes_SRS_AMQPSCONVERTFROMPROTONRETURNVALUE_12_001: [The constructor shall initialize message, messageCallback and messageContext private member variables with the given arguments.]
        this.message = message;
        this.messageCallback = messageCallback;
        this.messageContext = messageContext;
    }

    /**
     * Getter for the message.
     *
     * @return the message.
     */
    Message getMessage()
    {
        // Codes_SRS_AMQPSCONVERTFROMPROTONRETURNVALUE_12_002: [The function shall return the current value of message private member.]
        return message;
    }

    /**
     * Getter for the callback.
     *
     * @return the callback.
     */
    MessageCallback getMessageCallback()
    {
        // Codes_SRS_AMQPSCONVERTFROMPROTONRETURNVALUE_12_003: [The function shall return the current value of messageCallback private member.]
        return messageCallback;
    }

    /**
     * Getter for the user context.
     *
     * @return the context.
     */
    Object getMessageContext()
    {
        // Codes_SRS_AMQPSCONVERTFROMPROTONRETURNVALUE_12_004: [The function shall return the current value of messageContext private member.]
        return messageContext;
    }
}
