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
        this.message = message;
        this.messageCallback = messageCallback;
        this.messageContext = messageContext;
    }

    Message getMessage()
    {
        return message;
    }

    MessageCallback getMessageCallback()
    {
        return messageCallback;
    }

    Object getMessageContext()
    {
        return messageContext;
    }
}
