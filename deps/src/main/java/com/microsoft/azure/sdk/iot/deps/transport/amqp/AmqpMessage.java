// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.transport.amqp;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.impl.MessageImpl;
import org.apache.qpid.proton.amqp.messaging.Data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

public class AmqpMessage
{
    MessageImpl messageImpl;

    public AmqpMessage()
    {
        this.messageImpl = (MessageImpl) Proton.message();
    }

    /**
     * Consule a MessageImpl object
     * @param messageImpl the ProtonJ Impl message.
     */
    public AmqpMessage(MessageImpl messageImpl)
    {
        this.messageImpl = messageImpl;
    }

    /**
     * set the Body of the AMQP message
     * @param msgData the AMQP message body.
     */
    public void setBody(Data msgData)
    {
        messageImpl.setBody(msgData);
    }

    public void setBody(byte[] msgData)
    {
        Binary binary = new Binary(msgData);
        Data dataSection = new Data(binary);
        messageImpl.setBody(dataSection);
    }

    /**
     * Returns the amqp body used in the message
     * @return Byte array
     */
    public byte[] getAmqpBody()
    {
        Data msgData = (Data)this.messageImpl.getBody();
        Binary binData = msgData.getValue();
        byte[] msgBody = new byte[binData.getLength()];
        ByteBuffer buffer = binData.asByteBuffer();
        buffer.get(msgBody);
        return msgBody;
    }

    /**
     * Set the application property for the message
     * @param userProperties The user prototype.
     */
    public void setApplicationProperty(Map<String, Object> userProperties)
    {
        ApplicationProperties applicationProperties = new ApplicationProperties(userProperties);
        this.messageImpl.setApplicationProperties(applicationProperties);
    }

    /**
     * Set the application property for the message
     * @return Map of properties
     */
    public Map<String, Object> getApplicationProperty()
    {
        ApplicationProperties appProperty = this.messageImpl.getApplicationProperties();
        if (appProperty == null)
        {
            return null;
        }
        else
        {
            return appProperty.getValue();
        }
    }

    /**
     * Sets the data value
     * @param data the {@code byte[]} to be decoded
     * @param offset the offset in the byte[]
     * @param length the total length
     */
    public void decode(byte[] data, int offset, int length)
    {
        if (data == null)
        {
            throw new IllegalArgumentException("The data cannot be null.");
        }
        this.messageImpl.decode(data, offset, length);
    }

    public int encode(byte[] data, int offset) throws IOException
    {
        if (data == null)
        {
            throw new IllegalArgumentException("The data cannot be null.");
        }
        return this.messageImpl.encode(data, offset, data.length);
    }
}
