package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.MessageType;
import org.apache.qpid.proton.message.impl.MessageImpl;

public class AmqpsConvertToProtonReturnValue
{
    private MessageImpl messageImpl;

    private MessageType messageType;

    /**
     * Create a return value object containing the Proton message and the message type.
     *
     * @param messageImpl the Proton message.
     * @param messageType the IoTHub type of the message.
     */
    AmqpsConvertToProtonReturnValue(MessageImpl messageImpl, MessageType messageType)
    {
        // Codes_SRS_AMQPSCONVERTTOPROTONRETURNVALUE_12_001: [The constructor shall initialize messageImpl and messageType private member variables with the given arguments.]
        this.messageImpl = messageImpl;
        this.messageType = messageType;
    }

    /**
     * Getter for the Proton message.
     *
     * @return the Proton message.
     */
    MessageImpl getMessageImpl()
    {
        // Codes_SRS_AMQPSCONVERTTOPROTONRETURNVALUE_12_002: [The function shall return the current value of messageImpl private member.]
        return messageImpl;
    }

    /**
     * Getter for the IoTHub message type.
     *
     * @return the IoTHub type of the message.
     */
    MessageType getMessageType()
    {
        // Codes_SRS_AMQPSCONVERTTOPROTONRETURNVALUE_12_003: [The function shall return the current value of messageType private member.]
        return messageType;
    }
}
