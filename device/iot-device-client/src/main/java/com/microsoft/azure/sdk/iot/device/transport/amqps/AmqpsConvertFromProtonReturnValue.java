package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;

public class AmqpsConvertFromProtonReturnValue
{
    private Message message;

    private MessageCallback messageCallback;

    private Object messageContext;

    AmqpsConvertFromProtonReturnValue(Message message, MessageCallback messageCallback, Object messageContext)
    {
        // Codes_SRS_AMQPSCONVERTFROMPROTONRETURNVALUE_12_001: [The constructor shall initialize message, messageCallback and messageContext private member variables with the given arguments.]
        this.message = message;
        this.messageCallback = messageCallback;
        this.messageContext = messageContext;
    }

    Message getMessage()
    {
        // Codes_SRS_AMQPSCONVERTFROMPROTONRETURNVALUE_12_002: [The function shall return the current value of message private member.]
        return message;
    }

    MessageCallback getMessageCallback()
    {
        // Codes_SRS_AMQPSCONVERTFROMPROTONRETURNVALUE_12_003: [The function shall return the current value of messageCallback private member.]
        return messageCallback;
    }

    Object getMessageContext()
    {
        // Codes_SRS_AMQPSCONVERTFROMPROTONRETURNVALUE_12_004: [The function shall return the current value of messageContext private member.]
        return messageContext;
    }
}
