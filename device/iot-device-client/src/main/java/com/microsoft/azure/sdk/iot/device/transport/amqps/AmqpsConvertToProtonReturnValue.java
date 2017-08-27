package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.MessageType;
import org.apache.qpid.proton.message.impl.MessageImpl;

public class AmqpsConvertToProtonReturnValue
{
    private MessageImpl messageImpl;

    private MessageType messageType;

    AmqpsConvertToProtonReturnValue(MessageImpl messageImpl, MessageType messageType)
    {
        this.messageImpl = messageImpl;
        this.messageType = messageType;
    }

    MessageImpl getMessageImpl()
    {
        return messageImpl;
    }

    MessageType getMessageType()
    {
        return messageType;
    }
}
