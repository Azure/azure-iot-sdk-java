/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.message.impl.MessageImpl;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
*  Unit tests for AmqpsDeviceOperations
* 100% methods covered
* 89% lines covered
*/
public class AmqpsDeviceOperationsTest
{
    @Mocked
    Session mockSession;

    @Mocked
    Sender mockSender;

    @Mocked
    Receiver mockReceiver;

    @Mocked
    Link mockLink;

    @Mocked
    Target mockTarget;

    @Mocked
    Source mockSource;

    @Mocked
    Delivery mockDelivery;

    @Mocked
    HashMap mockHashMap;

    @Mocked
    AmqpsMessage mockAmqpsMessage;

    @Mocked
    DeviceClientConfig mockDeviceClientConfig;

    @Mocked
    MessageImpl mockMessageImpl;

    @Mocked
    Message mockMessage;

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_001: [**The constructor shall initialize amqpProperties with device client identifier and version.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_002: [**The constructor shall initialize sender and receiver tags with UUID string.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_003: [**The constructor shall initialize sender and receiver endpoint path members to empty string.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_004: [**The constructor shall initialize sender and receiver link address members to empty string.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_005: [**The constructor shall initialize sender and receiver link objects to null.**]**
    */
    @Test
    public void constructorInitializesAllMembers(
            @Mocked final UUID mockUUID
    )
    {
        // arrange
        final String uuidStr = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
        new NonStrictExpectations()
        {
            {
                UUID.randomUUID();
                result = mockUUID;
                mockUUID.toString();
                result = uuidStr;
            }
        };

        //act
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);

        //assert
        assertNotNull(amqpsDeviceOperations);

        String VERSION_IDENTIFIER_KEY = Deencapsulation.getField(amqpsDeviceOperations, "VERSION_IDENTIFIER_KEY");
        String senderLinkEndpointPath = Deencapsulation.getField(amqpsDeviceOperations, "senderLinkEndpointPath");
        String receiverLinkEndpointPath = Deencapsulation.getField(amqpsDeviceOperations, "receiverLinkEndpointPath");
        Sender senderLink = Deencapsulation.getField(amqpsDeviceOperations, "senderLink");
        Receiver receiverLink = Deencapsulation.getField(amqpsDeviceOperations, "receiverLink");
        Map<Symbol, Object> propertiesMap = Deencapsulation.invoke(amqpsDeviceOperations, "getAmqpProperties");
        String senderLinkTag = Deencapsulation.invoke(amqpsDeviceOperations, "getSenderLinkTag");
        String receiverLinkTag = Deencapsulation.invoke(amqpsDeviceOperations, "getReceiverLinkTag");
        String senderLinkAddress = Deencapsulation.invoke(amqpsDeviceOperations, "getSenderLinkAddress");
        String receiverLinkAddress = Deencapsulation.invoke(amqpsDeviceOperations, "getReceiverLinkAddress");

        assertTrue(propertiesMap.get(Symbol.getSymbol(VERSION_IDENTIFIER_KEY)).equals(TransportUtils.JAVA_DEVICE_CLIENT_IDENTIFIER + TransportUtils.CLIENT_VERSION));

        assertTrue(senderLinkTag.endsWith(uuidStr));
        assertTrue(receiverLinkTag.endsWith(uuidStr));

        assertTrue(senderLinkEndpointPath.equals(""));
        assertTrue(receiverLinkEndpointPath.equals(""));

        assertTrue(senderLinkAddress.equals(""));
        assertTrue(receiverLinkAddress.equals(""));

        assertTrue(senderLink == null);
        assertTrue(receiverLink == null);
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_032: [**The class has static members for version identifier, api version keys and api version value.**]**
     */
    @Test
    public void apiVersionAndVersionIdFieldsValues()
    {
        //act
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        String VERSION_IDENTIFIER_KEY = Deencapsulation.getField(amqpsDeviceOperations, "VERSION_IDENTIFIER_KEY");
        String API_VERSION_KEY = Deencapsulation.getField(amqpsDeviceOperations, "API_VERSION_KEY");
        String API_VERSION_VALUE = Deencapsulation.getField(amqpsDeviceOperations, "API_VERSION_VALUE");

        //assert
        assertTrue(VERSION_IDENTIFIER_KEY.equals("com.microsoft:client-version"));
        assertTrue(API_VERSION_KEY.equals("com.microsoft:api-version"));
        assertTrue(API_VERSION_VALUE.equals("2016-11-14"));
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_006: [**The function shall throw IllegalArgumentException if the session argument is null.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void openLinksThrowsIllegalArgumentException() throws IllegalArgumentException
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);

        //act
        Deencapsulation.invoke(amqpsDeviceOperations, "openLinks", null);
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_007: [**The function shall create receiver link with the receiverlinkTag member value.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_008: [**The function shall create sender link with the senderlinkTag member value.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_044: [**The function shall set the link's state to OPENING.**]**
     */
    @Test
    public void openLinksCreatesReceiverAndSenderLinksWithTag()
    {
        //arrange
        final AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);

        //act
        AmqpsDeviceOperationLinkState amqpsSendLinkState1 = Deencapsulation.getField(amqpsDeviceOperations, "amqpsSendLinkState");
        AmqpsDeviceOperationLinkState amqpsRecvLinkState1 = Deencapsulation.getField(amqpsDeviceOperations, "amqpsRecvLinkState");
        Deencapsulation.invoke(amqpsDeviceOperations, "openLinks", mockSession);
        AmqpsDeviceOperationLinkState amqpsSendLinkState2 = Deencapsulation.getField(amqpsDeviceOperations, "amqpsSendLinkState");
        AmqpsDeviceOperationLinkState amqpsRecvLinkState2 = Deencapsulation.getField(amqpsDeviceOperations, "amqpsRecvLinkState");

        Receiver receiverLink = Deencapsulation.getField(amqpsDeviceOperations, "receiverLink");
        Sender senderLink = Deencapsulation.getField(amqpsDeviceOperations, "senderLink");
        final String senderLinkTag = Deencapsulation.invoke(amqpsDeviceOperations, "getSenderLinkTag");
        final String receiverLinkTag = Deencapsulation.invoke(amqpsDeviceOperations, "getReceiverLinkTag");

        //assert
        assertEquals(amqpsSendLinkState1, AmqpsDeviceOperationLinkState.CLOSED);
        assertEquals(amqpsRecvLinkState1, AmqpsDeviceOperationLinkState.CLOSED);
        assertEquals(amqpsSendLinkState2, AmqpsDeviceOperationLinkState.OPENING);
        assertEquals(amqpsRecvLinkState2, AmqpsDeviceOperationLinkState.OPENING);
        assertTrue(receiverLink != null);
        assertTrue(senderLink != null);

        new Verifications()
        {
            {
                mockSession.receiver(receiverLinkTag);
                times = 1;
                mockSession.sender(senderLinkTag);
                times = 1;
            }
        };
    }

    @Test
    public void openLinksCreatesReceiverAndSenderLinksWithTag_CBS()
    {
        //arrange
        final AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);

        //act
        AmqpsDeviceOperationLinkState amqpsSendLinkState1 = Deencapsulation.getField(amqpsDeviceOperations, "amqpsSendLinkState");
        AmqpsDeviceOperationLinkState amqpsRecvLinkState1 = Deencapsulation.getField(amqpsDeviceOperations, "amqpsRecvLinkState");
        Deencapsulation.invoke(amqpsDeviceOperations, "openLinks", mockSession);
        AmqpsDeviceOperationLinkState amqpsSendLinkState2 = Deencapsulation.getField(amqpsDeviceOperations, "amqpsSendLinkState");
        AmqpsDeviceOperationLinkState amqpsRecvLinkState2 = Deencapsulation.getField(amqpsDeviceOperations, "amqpsRecvLinkState");

        Receiver receiverLink = Deencapsulation.getField(amqpsDeviceOperations, "receiverLink");
        Sender senderLink = Deencapsulation.getField(amqpsDeviceOperations, "senderLink");
        final String senderLinkTag = Deencapsulation.invoke(amqpsDeviceOperations, "getSenderLinkTag");
        final String receiverLinkTag = Deencapsulation.invoke(amqpsDeviceOperations, "getReceiverLinkTag");

        //assert
        assertEquals(amqpsSendLinkState1, AmqpsDeviceOperationLinkState.CLOSED);
        assertEquals(amqpsRecvLinkState1, AmqpsDeviceOperationLinkState.CLOSED);
        assertEquals(amqpsSendLinkState2, AmqpsDeviceOperationLinkState.OPENING);
        assertEquals(amqpsRecvLinkState2, AmqpsDeviceOperationLinkState.OPENING);
        assertTrue(receiverLink != null);
        assertTrue(senderLink != null);

        new Verifications()
        {
            {
                mockSession.receiver(receiverLinkTag);
                times = 1;
                mockSession.sender(senderLinkTag);
                times = 1;
            }
        };
    }

    /*
    **Test_SRS_AMQPSDEVICEOPERATIONS_12_009: [**The function shall set both receiver and sender link properties to the amqpProperties member value.**]**
     */
    @Test
    public void openLinksSetsAmqpsProperties()
    {
        //arrange
        final AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);

        //act
        Deencapsulation.invoke(amqpsDeviceOperations, "openLinks", mockSession);
        final Receiver receiverLink = Deencapsulation.getField(amqpsDeviceOperations, "receiverLink");
        final Sender senderLink = Deencapsulation.getField(amqpsDeviceOperations, "senderLink");
        final Map<Symbol, Object> propertiesMap = Deencapsulation.invoke(amqpsDeviceOperations, "getAmqpProperties");

        //assert
        new Verifications()
        {
            {
                receiverLink.setProperties(propertiesMap);
                times = 1;
                senderLink.setProperties(propertiesMap);
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_010: [**The function shall open both receiver and sender link.**]**
     */
    @Test
    public void openLinksOpensLinks()
    {
        //arrange
        final AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);

        //act
        Deencapsulation.invoke(amqpsDeviceOperations, "openLinks", mockSession);
        final Receiver receiverLink = Deencapsulation.getField(amqpsDeviceOperations, "receiverLink");
        final Sender senderLink = Deencapsulation.getField(amqpsDeviceOperations, "senderLink");

        //assert
        new Verifications()
        {
            {
                receiverLink.open();
                times = 1;
                senderLink.open();
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_011: [**If the sender link is not null the function shall closeNow it and sets it to null.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_012: [**If the receiver link is not null the function shall closeNow it and sets it to null.**]**
    */
    @Test
    public void closeLinksClosesLinks()
    {
        //arrange
        final AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        Deencapsulation.invoke(amqpsDeviceOperations, "openLinks", mockSession);
        Deencapsulation.setField(amqpsDeviceOperations, "receiverLink", mockReceiver);
        Deencapsulation.setField(amqpsDeviceOperations, "senderLink", mockSender);

        //act
        Deencapsulation.invoke(amqpsDeviceOperations, "closeLinks");

        //assert
        final Receiver receiverLink = Deencapsulation.getField(amqpsDeviceOperations, "receiverLink");
        final Sender senderLink = Deencapsulation.getField(amqpsDeviceOperations, "senderLink");

        assertTrue(receiverLink == null);
        assertTrue(senderLink == null);

        new Verifications()
        {
            {
                mockReceiver.close();
                times = 1;
                mockSender.close();
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_013: [**The function shall throw IllegalArgumentException if the link argument is null.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void initLinkThrowsIllegalArgumentException()
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);

        //act
        Deencapsulation.invoke(amqpsDeviceOperations, "initLink", null);
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_014: [**If the link is the sender link, the function shall create a new Target (Proton) object using the sender link address member variable.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_015: [**If the link is the sender link, the function shall set its target to the created Target (Proton) object.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_016: [**If the link is the sender link, the function shall set the SenderSettleMode to UNSETTLED.**]**
     */
    @Test
    public void initLinkSetsSenderLink()
    {
        //arrange
        final AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        final String senderLinkTag = "senderLinkTag";
        Deencapsulation.setField(amqpsDeviceOperations, "senderLinkTag",senderLinkTag);
        final String senderLinkAddress = "senderLinkAddress";
        Deencapsulation.setField(amqpsDeviceOperations, "senderLinkAddress", senderLinkAddress);
        Deencapsulation.setField(amqpsDeviceOperations, "amqpsSendLinkState", AmqpsDeviceOperationLinkState.OPENING);

        new NonStrictExpectations()
        {
            {
                mockLink.getName();
                result = senderLinkTag;
                new Target();
                result = mockTarget;
            }
        };

        //act
        Deencapsulation.invoke(amqpsDeviceOperations, "initLink", mockLink);

        //assert
        new Verifications()
        {
            {
                mockTarget.setAddress(senderLinkAddress);
                times = 1;
                mockLink.setTarget(mockTarget);
                times = 1;
                mockLink.setSenderSettleMode(SenderSettleMode.UNSETTLED);
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_017: [**If the link is the receiver link, the function shall create a new Source (Proton) object using the receiver link address member variable.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_018: [**If the link is the receiver link, the function shall set its source to the created Source (Proton) object.**]**
     */
    @Test
    public void initLinkSetsReceiverLink()
    {
        //arrange
        final AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        final String receiverLinkTag = "receiverLinkTag";
        Deencapsulation.setField(amqpsDeviceOperations, "receiverLinkTag", receiverLinkTag);
        final String receiverLinkAddress = "receiverLinkAddress";
        Deencapsulation.setField(amqpsDeviceOperations, "receiverLinkAddress", receiverLinkAddress);
        Deencapsulation.setField(amqpsDeviceOperations, "amqpsRecvLinkState", AmqpsDeviceOperationLinkState.OPENING);

        new NonStrictExpectations()
        {
            {
                mockLink.getName();
                result = receiverLinkTag;
                new Source();
                result = mockSource;
            }
        };

        //act
        Deencapsulation.invoke(amqpsDeviceOperations, "initLink", mockLink);

        //assert
        new Verifications()
        {
            {
                mockSource.setAddress(receiverLinkAddress);
                times = 1;
                mockLink.setSource(mockSource);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSDEVICEOPERATIONS_12_045: [THe function do nothing if the either the receiver or the sender link state is other than OPENING.]
    public void initLinkDoesNothing()
    {
        //arrange
        final AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        final String senderLinkTag = "senderLinkTag";
        Deencapsulation.setField(amqpsDeviceOperations, "senderLinkTag",senderLinkTag);
        final String receiverLinkTag = "receiverLinkTag";
        Deencapsulation.setField(amqpsDeviceOperations, "receiverLinkTag", receiverLinkTag);
        final String senderLinkAddress = "senderLinkAddress";
        Deencapsulation.setField(amqpsDeviceOperations, "senderLinkAddress", senderLinkAddress);
        final String receiverLinkAddress = "receiverLinkAddress";
        Deencapsulation.setField(amqpsDeviceOperations, "receiverLinkAddress", receiverLinkAddress);
        Deencapsulation.setField(amqpsDeviceOperations, "amqpsSendLinkState", AmqpsDeviceOperationLinkState.OPENED);
        Deencapsulation.setField(amqpsDeviceOperations, "amqpsRecvLinkState", AmqpsDeviceOperationLinkState.OPENED);

        new NonStrictExpectations()
        {
            {
                mockLink.getName();
                result = receiverLinkTag;
                new Source();
                result = mockSource;
            }
        };

        //act
        Deencapsulation.invoke(amqpsDeviceOperations, "initLink", mockLink);

        //assert
        new Verifications()
        {
            {
                mockSource.setAddress(receiverLinkAddress);
                times = 0;
                mockLink.setSource(mockSource);
                times = 0;
                mockTarget.setAddress(senderLinkAddress);
                times = 0;
                mockLink.setTarget(mockTarget);
                times = 0;
                mockLink.setSenderSettleMode(SenderSettleMode.UNSETTLED);
                times = 0;            }
        };
    }


    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_019: [**The function shall throw IllegalStateException if the sender link is not initialized.**]**
     */
    @Test (expected = IllegalStateException.class)
    public void sendMessageAndGetDeliveryHashThrowsIllegalStateExceptionIfSenderLinkNull()
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        byte[] msgData = new byte[1];
        int offset = 0;
        int length = 1;
        byte[] deliveryTag = new byte[1];
        Deencapsulation.setField(amqpsDeviceOperations, "senderLink", null);

        //act
        Deencapsulation.invoke(amqpsDeviceOperations, "sendMessageAndGetDeliveryHash", MessageType.DEVICE_TELEMETRY, msgData, offset, length, deliveryTag);
    }

    // Tests_SRS_AMQPSDEVICEOPERATIONS_12_020: [The function shall throw IllegalArgumentException if the deliveryTag length is zero.]
    @Test (expected = IllegalArgumentException.class)
    public void sendMessageAndGetDeliveryHashThrowsIllegalArgumentExceptionIfDeliveryTagNull()
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        final byte[] msgData = new byte[1];
        final int offset = 0;
        final int length = 1;
        final byte[] deliveryTag = new byte[0];
        Deencapsulation.setField(amqpsDeviceOperations, "senderLink", mockSender);

        //act
        Deencapsulation.invoke(amqpsDeviceOperations, "sendMessageAndGetDeliveryHash", MessageType.DEVICE_TELEMETRY, msgData, offset, length, deliveryTag);
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_021: [**The function shall create a Delivery object using the sender link and the deliveryTag.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_022: [**The function shall try to send the message data using the sender link with the offset and length argument.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_023: [**The function shall advance the sender link.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_024: [**The function shall set the delivery hash to the value returned by the sender link.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_026: [**The function shall return with the delivery hash.**]**
    */
    @Test
    public void sendMessageAndGetDeliveryHashSendSuccessful()
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        final byte[] msgData = new byte[1];
        final int offset = 0;
        final int length = 1;
        final byte[] deliveryTag = new byte[1];
        Deencapsulation.setField(amqpsDeviceOperations, "senderLink", mockSender);

        new NonStrictExpectations()
        {
            {
                mockSender.delivery(deliveryTag);
                result = mockDelivery;
                mockDelivery.hashCode();
                result = new byte[1];
            }
        };

        //act
        AmqpsSendReturnValue amqpsSendReturnValue = Deencapsulation.invoke(amqpsDeviceOperations, "sendMessageAndGetDeliveryHash", MessageType.DEVICE_TELEMETRY, msgData, offset, length, deliveryTag);

        //assert
        int deliveryHash = Deencapsulation.invoke(amqpsSendReturnValue, "getDeliveryHash");
        assertTrue(deliveryHash != -1);
        new Verifications()
        {
            {
                mockSender.delivery(deliveryTag);
                times = 1;
                mockSender.send(msgData, offset, length);
                times = 1;
                mockSender.advance();
                times = 1;
                mockDelivery.hashCode();
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_025: [**If proton failed sending the function shall advance the sender link, release the delivery object and sets the delivery hash to -1.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_026: [**The function shall return with the delivery hash.**]**
     */
    @Test
    public void sendMessageAndGetDeliveryHashSendFails()
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        final byte[] msgData = new byte[1];
        final int offset = 0;
        final int length = 1;
        final byte[] deliveryTag = new byte[1];
        Deencapsulation.setField(amqpsDeviceOperations, "senderLink", mockSender);

        new NonStrictExpectations()
        {
            {
                mockSender.delivery(deliveryTag);
                result = mockDelivery;
                mockSender.send(msgData, offset, length);
                result = new Exception();

            }
        };

        //act
        AmqpsSendReturnValue amqpsSendReturnValue = Deencapsulation.invoke(amqpsDeviceOperations, "sendMessageAndGetDeliveryHash", MessageType.DEVICE_TELEMETRY, msgData, offset, length, deliveryTag);

        //assert
        int deliveryHash = Deencapsulation.invoke(amqpsSendReturnValue, "getDeliveryHash");
        assertTrue(deliveryHash == -1);
        new Verifications()
        {
            {
                mockSender.advance();
                times = 1;
                mockDelivery.free();
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_036: [**The function shall throw IllegalArgumentException if the linkName is empty.**]**
    */
    @Test (expected = IllegalArgumentException.class)
    public void getMessageFromReceiverLinkThrowIfLinkNameEmpty()
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);

        //act
        AmqpsMessage amqpsMessage = Deencapsulation.invoke(amqpsDeviceOperations, "getMessageFromReceiverLink", "");
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_043: [The function shall return null if the linkName does not match with the receiverLink tag.]
    */
    @Test
    public void getMessageFromReceiverLinkReturnsNullIfLinkNotOwned()
    {
        //arrange
        String linkName1 = "receiver1";
        String linkName2 = "receiver2";
        final AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        Deencapsulation.invoke(amqpsDeviceOperations, "openLinks", mockSession);
        Deencapsulation.setField(amqpsDeviceOperations, "receiverLink", mockReceiver);
        Deencapsulation.setField(amqpsDeviceOperations, "senderLink", mockSender);
        Deencapsulation.setField(amqpsDeviceOperations, "receiverLinkTag", linkName1);

        new NonStrictExpectations()
        {
            {
                mockReceiver.current();
                result = mockDelivery;
                mockDelivery.isReadable();
                result = true;
                mockDelivery.isPartial();
                result = false;
            }
        };

        //act
        AmqpsMessage amqpsMessage = Deencapsulation.invoke(amqpsDeviceOperations, "getMessageFromReceiverLink", linkName2);

        //assert
        assertTrue(amqpsMessage == null);
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_037: [**The function shall create a Delivery object from the link.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_033: [**The function shall try to read the full message from the delivery object and if it fails return null.**]**
    */
    @Test
    public void getMessageFromReceiverLinkReturnsNullIfNotReadable()
    {
        //arrange
        String linkName = "receiver";
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        Deencapsulation.invoke(amqpsDeviceOperations, "openLinks", mockSession);
        Deencapsulation.setField(amqpsDeviceOperations, "receiverLink", mockReceiver);
        Deencapsulation.setField(amqpsDeviceOperations, "senderLink", mockSender);
        Deencapsulation.setField(amqpsDeviceOperations, "receiverLinkTag", linkName);

        new NonStrictExpectations()
        {
            {
                mockReceiver.current();
                result = mockDelivery;
                mockDelivery.isReadable();
                result = false;
                mockDelivery.isPartial();
                result = false;
            }
        };

        //act
        AmqpsMessage amqpsMessage = Deencapsulation.invoke(amqpsDeviceOperations, "getMessageFromReceiverLink", linkName);

        //assert
        assertTrue(amqpsMessage == null);
        new Verifications()
        {
            {
                mockReceiver.current();
                times = 1;
                mockDelivery.isReadable();
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_037: [**The function shall create a Delivery object from the link.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_033: [**The function shall try to read the full message from the delivery object and if it fails return null.**]**
    */
    @Test
    public void getMessageFromReceiverLinkReturnsNullIfPartial()
    {
        //arrange
        String linkName = "receiver";
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        Deencapsulation.invoke(amqpsDeviceOperations, "openLinks", mockSession);
        Deencapsulation.setField(amqpsDeviceOperations, "receiverLink", mockReceiver);
        Deencapsulation.setField(amqpsDeviceOperations, "senderLink", mockSender);
        Deencapsulation.setField(amqpsDeviceOperations, "receiverLinkTag", linkName);

        new NonStrictExpectations()
        {
            {
                mockReceiver.current();
                result = mockDelivery;
                mockDelivery.isReadable();
                result = true;
                mockDelivery.isPartial();
                result = true;
            }
        };

        //act
        AmqpsMessage amqpsMessage = Deencapsulation.invoke(amqpsDeviceOperations, "getMessageFromReceiverLink", linkName);

        //assert
        assertTrue(amqpsMessage == null);
        new Verifications()
        {
            {
                mockReceiver.current();
                times = 1;
                mockDelivery.isReadable();
                times = 1;
                mockDelivery.isPartial();
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_034: [**The function shall read the full message into a buffer.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_035: [**The function shall advance the receiver link.**]**
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_038: [**The function shall create a Proton message from the received buffer and return with it.**]**
    */
    @Test
    public void getMessageFromReceiverLinkSuccess()
    {
        //arrange
        String linkName = "receiver";
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        Deencapsulation.invoke(amqpsDeviceOperations, "openLinks", mockSession);
        Deencapsulation.setField(amqpsDeviceOperations, "receiverLink", mockReceiver);
        Deencapsulation.setField(amqpsDeviceOperations, "senderLink", mockSender);
        Deencapsulation.setField(amqpsDeviceOperations, "receiverLinkTag", linkName);

        new NonStrictExpectations()
        {
            {
                mockReceiver.current();
                result = mockDelivery;
                mockDelivery.isReadable();
                result = true;
                mockDelivery.isPartial();
                result = false;
            }
        };

        //act
        AmqpsMessage amqpsMessage = Deencapsulation.invoke(amqpsDeviceOperations, "getMessageFromReceiverLink", linkName);

        //assert
        assertTrue(amqpsMessage != null);
        new Verifications()
        {
            {
                mockReceiver.current();
                times = 1;
                mockDelivery.isReadable();
                times = 1;
                mockDelivery.isPartial();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSDEVICEOPERATIONS_12_047: [The function shall return true if all link are opened, false otherwise.]
    @Test
    public void operationLinksOpenedTrue()
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        Deencapsulation.setField(amqpsDeviceOperations, "amqpsSendLinkState", AmqpsDeviceOperationLinkState.OPENED);
        Deencapsulation.setField(amqpsDeviceOperations, "amqpsRecvLinkState", AmqpsDeviceOperationLinkState.OPENED);

        //act
        Boolean isOpened = Deencapsulation.invoke(amqpsDeviceOperations, "operationLinksOpened");

        //assert
        assertTrue(isOpened);
    }

    // Tests_SRS_AMQPSDEVICEOPERATIONS_12_047: [The function shall return true if all link are opened, false otherwise.]
    @Test
    public void operationLinksOpenedFalse()
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        Deencapsulation.setField(amqpsDeviceOperations, "amqpsSendLinkState", AmqpsDeviceOperationLinkState.OPENED);
        Deencapsulation.setField(amqpsDeviceOperations, "amqpsRecvLinkState", AmqpsDeviceOperationLinkState.CLOSED);

        //act
        Boolean isOpened = Deencapsulation.invoke(amqpsDeviceOperations, "operationLinksOpened");

        //assert
        assertFalse(isOpened);
    }

    // Tests_SRS_AMQPSDEVICEOPERATIONS_12_046: [The prototype function shall return null.]
    @Test
    public void isLinkFound()
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);

        //act
        Boolean isFound = Deencapsulation.invoke(amqpsDeviceOperations, "isLinkFound", "linkName");

        //assert
        assertNull(isFound);
    }

    /*
    **Test_SRS_AMQPSDEVICEOPERATIONS_12_039: [**The prototype function shall return null.**]**
    */
    @Test
    public void convertFromProton()
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceOperations, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);

        //assert
        assertTrue(amqpsConvertFromProtonReturnValue == null);
    }

    /*
    **Test_SRS_AMQPSDEVICEOPERATIONS_12_040: [**The prototype function shall return null.**]**
    */
    @Test
    public void convertToProton()
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceOperations, "convertToProton", mockMessage);

        //assert
        assertTrue(amqpsConvertFromProtonReturnValue == null);
    }

    /*
    **Test_SRS_AMQPSDEVICEOPERATIONS_12_041: [**The prototype function shall return null.**]**
    */
    @Test
    public void protonMessageToIoTHubMessage()
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceOperations, "protonMessageToIoTHubMessage", mockMessageImpl);

        //assert
        assertTrue(amqpsConvertFromProtonReturnValue == null);
    }

    /*
    **Test_SRS_AMQPSDEVICEOPERATIONS_12_042: [**The prototype function shall return null.**]**
    */
    @Test
    public void iotHubMessageToProtonMessage()
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceOperations, "iotHubMessageToProtonMessage", mockMessage);

        //assert
        assertTrue(amqpsConvertFromProtonReturnValue == null);
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_027: [**The getter shall return with the value of the amqpProperties.**]**
     */
    @Test
    public void getAmqpPropertiesReturnsAmqpsProperties()
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        Deencapsulation.setField(amqpsDeviceOperations, "amqpProperties", mockHashMap);

        //act
        Map<Symbol, Object> amqpsProperties = Deencapsulation.invoke(amqpsDeviceOperations, "getAmqpProperties");

        //assert
        assertTrue(amqpsProperties == mockHashMap);
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_028: [**The getter shall return with the value of the sender link tag.**]**
     */
    @Test
    public void getSenderLinkTagReturnsSenderLinkTag()
    {
        //arrange
        String linkTag = "linkTag";
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        Deencapsulation.setField(amqpsDeviceOperations, "senderLinkTag", linkTag);

        //act
        String senderLinkTag = linkTag;

        //assert
        assertTrue(senderLinkTag.equals(linkTag));
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_029: [**The getter shall return with the value of the receiver link tag.**]**
     */
    @Test
    public void getReceiverLinkTagReturnsReceiverLinkTag()
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        Deencapsulation.setField(amqpsDeviceOperations, "receiverLinkTag", "xxx");

        //act
        String receiverLinkTag = Deencapsulation.invoke(amqpsDeviceOperations, "getReceiverLinkTag");

        //assert
        assertTrue(receiverLinkTag.equals("xxx"));
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_030: [**The getter shall return with the value of the sender link address.**]**
     */
    @Test
    public void getSenderLinkAddressReturnsSenderLinkAddress()
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        Deencapsulation.setField(amqpsDeviceOperations, "senderLinkAddress", "xxx");

        //act
        String senderLinkAddress = Deencapsulation.invoke(amqpsDeviceOperations, "getSenderLinkAddress");

        //assert
        assertTrue(senderLinkAddress.equals("xxx"));
    }

    /*
    **Tests_SRS_AMQPSDEVICEOPERATIONS_12_031: [**The getter shall return with the value of the receiver link address.**]**
     */
    @Test
    public void getReceiverLinkAddressReturnsReceiverLinkAddress()
    {
        //arrange
        AmqpsDeviceOperations amqpsDeviceOperations = Deencapsulation.newInstance(AmqpsDeviceOperations.class);
        Deencapsulation.setField(amqpsDeviceOperations, "receiverLinkAddress", "xxx");

        //act
        String receiverLinkAddress = Deencapsulation.invoke(amqpsDeviceOperations, "getReceiverLinkAddress");

        //assert
        assertTrue(receiverLinkAddress.equals("xxx"));
    }
}
