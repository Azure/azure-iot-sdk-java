/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.deps.transport.amqp;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.AmqpMessage;
import org.apache.qpid.proton.amqp.messaging.Target;

import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.AmqpDeviceOperations;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.Map;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/** Unit tests for AmqpConnection.
 * Coverage : 100% method, 100% line */
@RunWith(JMockit.class)
public class AmqpDeviceOperationsTest
{
    private static final String TEST_SENDER_LINK_NAME = "provision_sender_link";
    private static final String TEST_RECEIVER_LINK_NAME = "provision_receiver_link";

    @Mocked
    private Receiver mockedReceiverLink;

    @Mocked
    private Sender mockedSenderLink;

    @Mocked
    private Delivery mockedDelivery;

    @Mocked
    private AmqpMessage mockedAmqpMessage;

    @Mocked
    private Session mockedSession;

    @Mocked
    private Link mockedLink;

    @Mocked
    private Target mockedTarget;

    @Test
    public void AmqpDeviceOperationsSucceeds()
    {
        new AmqpDeviceOperations();
    }

    @Test
    public void ReceiverMessageFromLinkReturnNullOnLinkNameNull() throws IOException
    {
        // Arrange
        AmqpDeviceOperations amqpDeviceOperation = new AmqpDeviceOperations();

        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        AmqpMessage result = amqpDeviceOperation.receiverMessageFromLink(null);

        //assert
        Assert.assertNull(result);
    }

    @Test
    public void ReceiverMessageFromLinkReturnNullOnLinkNameEmpty() throws IOException
    {
        // Arrange
        AmqpDeviceOperations amqpDeviceOperation = new AmqpDeviceOperations();

        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        AmqpMessage result = amqpDeviceOperation.receiverMessageFromLink("");

        //assert
        Assert.assertNull(result);
    }

    @Test
    public void ReceiverMessageFromLinkReceiverLinkNull() throws IOException
    {
        // Arrange
        AmqpDeviceOperations amqpDeviceOperation = new AmqpDeviceOperations();

        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        AmqpMessage result = amqpDeviceOperation.receiverMessageFromLink(TEST_SENDER_LINK_NAME);

        //assert
        Assert.assertNull(result);
    }

    @Test
    public void ReceiverMessageFromLinkWrongName() throws IOException
    {
        // Arrange
        AmqpDeviceOperations amqpDeviceOperation = new AmqpDeviceOperations();

        Deencapsulation.setField(amqpDeviceOperation, "receiverLink", mockedReceiverLink);

        new NonStrictExpectations()
        {
            {
                mockedReceiverLink.current();
                result = mockedDelivery;

                mockedDelivery.isReadable();
                result = true;

                mockedDelivery.pending();
                result = 10;

                mockedReceiverLink.recv((byte[])any, 0, anyInt);
                mockedReceiverLink.advance();

                mockedAmqpMessage.decode((byte[])any, 0, anyInt);
            }
        };

        // Act
        AmqpMessage result = amqpDeviceOperation.receiverMessageFromLink("WRONG_LINK_NAME");

        //assert
        Assert.assertNull(result);
    }

    @Test
    public void ReceiverMessageFromLinkSucceeds() throws IOException
    {
        // Arrange
        AmqpDeviceOperations amqpDeviceOperation = new AmqpDeviceOperations();

        Deencapsulation.setField(amqpDeviceOperation, "receiverLink", mockedReceiverLink);

        new NonStrictExpectations()
        {
            {
                mockedReceiverLink.current();
                result = mockedDelivery;

                mockedDelivery.isReadable();
                result = true;

                mockedDelivery.pending();
                result = 10;

                mockedReceiverLink.recv((byte[])any, 0, anyInt);
                mockedReceiverLink.advance();

                mockedAmqpMessage.decode((byte[])any, 0, anyInt);
            }
        };

        // Act
        AmqpMessage result = amqpDeviceOperation.receiverMessageFromLink(TEST_RECEIVER_LINK_NAME);

        //assert
        Assert.assertNotNull(result);
    }

    @Test
    public void openLinksSucceeds() throws IOException, IllegalArgumentException
    {
        // Arrange
        AmqpDeviceOperations amqpDeviceOperation = new AmqpDeviceOperations();

        new NonStrictExpectations()
        {
            {
                mockedSession.receiver(anyString);
                result = mockedReceiverLink;

                mockedReceiverLink.setProperties((Map< Symbol, Object>)any);
                mockedReceiverLink.open();

                mockedSession.sender(anyString);
                result = mockedSenderLink;

                mockedSenderLink.setProperties((Map< Symbol, Object>)any);
                mockedSenderLink.open();
            }
        };

        // Act
        amqpDeviceOperation.openLinks(mockedSession);

        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void openLinksThrowsOnSessionNull() throws IOException, IllegalArgumentException
    {
        // Arrange
        AmqpDeviceOperations amqpDeviceOperation = new AmqpDeviceOperations();

        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        amqpDeviceOperation.openLinks(null);

        //assert
    }

    @Test (expected = IOException.class)
    public void openLinksThrowsOnSessionReceiver() throws IOException, IllegalArgumentException
    {
        // Arrange
        AmqpDeviceOperations amqpDeviceOperation = new AmqpDeviceOperations();

        new NonStrictExpectations()
        {
            {
                mockedSession.receiver(anyString);
                result = new Exception();
            }
        };

        // Act
        amqpDeviceOperation.openLinks(mockedSession);

        //assert
    }

    @Test
    public void isRecieverLinkTagFalseSucceeds() throws IOException, IllegalArgumentException
    {
        // Arrange
        AmqpDeviceOperations amqpDeviceOperation = new AmqpDeviceOperations();

        // Act
        boolean result = amqpDeviceOperation.isReceiverLinkTag("");

        //assert
        Assert.assertFalse(result);
    }

    @Test
    public void isRecieverLinkTagTrueSucceeds() throws IOException, IllegalArgumentException
    {
        // Arrange
        AmqpDeviceOperations amqpDeviceOperation = new AmqpDeviceOperations();

        // Act
        boolean result = amqpDeviceOperation.isReceiverLinkTag(TEST_RECEIVER_LINK_NAME);

        //assert
        Assert.assertTrue(result);
    }

    @Test (expected = IllegalArgumentException.class)
    public void initLinkThrowOnLinkNull() throws IOException, IllegalArgumentException
    {
        // Arrange
        AmqpDeviceOperations amqpDeviceOperation = new AmqpDeviceOperations();

        // Act
        amqpDeviceOperation.initLink(null);

        //assert
    }

    @Test
    public void initLinkSenderSuccess() throws IOException, IllegalArgumentException
    {
        // Arrange
        AmqpDeviceOperations amqpDeviceOperation = new AmqpDeviceOperations();

        new NonStrictExpectations()
        {
            {
                mockedLink.getName();
                result = TEST_SENDER_LINK_NAME;

                new Target();
                result = mockedTarget;

                mockedTarget.setAddress(anyString);
                mockedLink.setTarget(mockedTarget);
                mockedLink.setSenderSettleMode(SenderSettleMode.UNSETTLED);
            }
        };

        // Act
        amqpDeviceOperation.initLink(mockedLink);

        //assert
    }

    @Test
    public void initLinkReceiverSuccess() throws IOException, IllegalArgumentException
    {
        // Arrange
        AmqpDeviceOperations amqpDeviceOperation = new AmqpDeviceOperations();

        new NonStrictExpectations()
        {
            {
                mockedLink.getName();
                result = TEST_RECEIVER_LINK_NAME;

                //new Target();
                //result = mockedTarget;

                mockedTarget.setAddress(anyString);
                mockedLink.setTarget(mockedTarget);
                mockedLink.setSenderSettleMode(SenderSettleMode.UNSETTLED);
            }
        };

        // Act
        amqpDeviceOperation.initLink(mockedLink);

        //assert
    }

    @Test (expected = Exception.class)
    public void initLinkThrowsOnGetName() throws IOException, IllegalArgumentException
    {
        // Arrange
        AmqpDeviceOperations amqpDeviceOperation = new AmqpDeviceOperations();

        new NonStrictExpectations()
        {
            {
                mockedLink.getName();
                result = new Exception();
            }
        };

        // Act
        amqpDeviceOperation.initLink(mockedLink);

        //assert
    }

    @Test
    public void sendMessageSuccess() throws IOException, IllegalArgumentException
    {
        // Arrange
        AmqpDeviceOperations amqpDeviceOperation = new AmqpDeviceOperations();

        Deencapsulation.setField(amqpDeviceOperation, "senderLink", mockedSenderLink);

        new NonStrictExpectations()
        {
            {
                mockedSenderLink.delivery((byte[])any);
            }
        };

        byte[] tag = new byte[10];
        byte[] data = new byte[10];

        // Act
        amqpDeviceOperation.sendMessage(tag, data, 128, 0);

        //assert
    }

    @Test
    public void closeLinksSuccess() throws IOException, IllegalArgumentException
    {
        // Arrange
        AmqpDeviceOperations amqpDeviceOperation = new AmqpDeviceOperations();

        Deencapsulation.setField(amqpDeviceOperation, "receiverLink", mockedReceiverLink);
        Deencapsulation.setField(amqpDeviceOperation, "senderLink", mockedSenderLink);

        new NonStrictExpectations()
        {
            {
                mockedReceiverLink.close();
                mockedSenderLink.close();
            }
        };

        // Act
        amqpDeviceOperation.closeLinks();

        //assert
    }

}
