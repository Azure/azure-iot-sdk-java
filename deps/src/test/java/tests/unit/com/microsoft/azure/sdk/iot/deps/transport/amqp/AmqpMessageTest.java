/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.deps.transport.amqp;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.message.impl.MessageImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.AmqpMessage;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Unit tests for AmqpConnection.
 * Coverage : 100% method, 100% line */
@RunWith(JMockit.class)
public class AmqpMessageTest
{
    @Mocked
    private MessageImpl mockedMessageImpl;

    @Mocked
    private Data mockedData;

    @Mocked
    private Message mockedMessage;

    @Test
    public void amqpMessageEmptyConstructorSucceeds()
    {
        new AmqpMessage();
    }

    @Test
    public void amqpMessageConstructorSucceeds()
    {
        new NonStrictExpectations()
        {
            {
            }
        };
        new AmqpMessage(mockedMessageImpl);
    }

    @Test
    public void setBodySucceeds()
    {
        AmqpMessage amqpMessage = new AmqpMessage(mockedMessageImpl);

        new NonStrictExpectations()
        {
            {
            }
        };

        amqpMessage.setBody(mockedData);
    }

    @Test
    public void getAmqpBodySucceeds()
    {
        AmqpMessage amqpMessage = new AmqpMessage(mockedMessageImpl);

        new NonStrictExpectations()
        {
            {
                mockedMessageImpl.getBody();
            }
        };

        amqpMessage.getAmqpBody();
    }

    @Test
    public void setApplicationPropertySucceeds()
    {
        AmqpMessage amqpMessage = new AmqpMessage(mockedMessageImpl);

        new NonStrictExpectations()
        {
            {
                mockedMessageImpl.setApplicationProperties((ApplicationProperties)any);
            }
        };

        Map<String, String> userProperties = new HashMap<>();
        amqpMessage.setApplicationProperty(userProperties);
    }

    @Test (expected = IllegalArgumentException.class)
    public void decodeThrowsOnDataNull()
    {
        AmqpMessage amqpMessage = new AmqpMessage(mockedMessageImpl);
        amqpMessage.decode(null, 0, 10);
    }

    @Test
    public void decodeSucceeds()
    {
        AmqpMessage amqpMessage = new AmqpMessage(mockedMessageImpl);

        new NonStrictExpectations()
        {
            {
                mockedMessageImpl.decode((byte[])any, 0, 10);
            }
        };

        byte[] data = new byte[10];

        amqpMessage.decode(data, 0, 10);
    }

    @Test (expected = IllegalArgumentException.class)
    public void encodeThrowsOnDataNull() throws IOException
    {
        AmqpMessage amqpMessage = new AmqpMessage(mockedMessageImpl);
        amqpMessage.encode(null, 0);
    }

    @Test
    public void encodeSucceeds() throws IOException
    {
        AmqpMessage amqpMessage = new AmqpMessage(mockedMessageImpl);

        new NonStrictExpectations()
        {
            {
                mockedMessageImpl.decode((byte[])any, 0, 10);
            }
        };

        byte[] data = new byte[10];

        amqpMessage.encode(data, 0);
    }
}
