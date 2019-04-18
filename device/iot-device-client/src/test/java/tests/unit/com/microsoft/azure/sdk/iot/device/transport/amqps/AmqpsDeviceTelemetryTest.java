/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.impl.MessageImpl;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.device.MessageProperty.OUTPUT_NAME_PROPERTY;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

/**
*  Unit tests for AmqpsDeviceTelemetryTest
* 100% methods covered
* 86% lines covered
*/
public class AmqpsDeviceTelemetryTest
{
    final String deviceId = "test-deviceId";

    @Mocked
    Session mockSession;

    @Mocked
    Sender mockSender;

    @Mocked
    Receiver mockReceiver;

    @Mocked
    Delivery mockDelivery;

    @Mocked
    MessageCallback mockMessageCallback;

    @Mocked
    DeviceClientConfig mockDeviceClientConfig;

    @Mocked
    ProductInfo mockedProductInfo;

    /*
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_002: [**The constructor shall set the sender and receiver endpoint path to IoTHub specific values.**]**
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_003: [**The constructor shall concatenate a sender specific prefix to the sender link tag's current value.**]**
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_004: [**The constructor shall concatenate a receiver specific prefix to the receiver link tag's current value.**]**
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_005: [**The constructor shall insert the given deviceId argument to the sender and receiver link address.**]**
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
                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";
            }
        };

        //act
        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
        String SENDER_LINK_ENDPOINT_PATH = Deencapsulation.getField(amqpsDeviceTelemetry, "SENDER_LINK_ENDPOINT_PATH_DEVICES");
        String RECEIVER_LINK_ENDPOINT_PATH = Deencapsulation.getField(amqpsDeviceTelemetry, "RECEIVER_LINK_ENDPOINT_PATH_DEVICES");
        String SENDER_LINK_TAG_PREFIX = Deencapsulation.getField(amqpsDeviceTelemetry, "SENDER_LINK_TAG_PREFIX");
        String RECEIVER_LINK_TAG_PREFIX = Deencapsulation.getField(amqpsDeviceTelemetry, "RECEIVER_LINK_TAG_PREFIX");
        String senderLinkEndpointPath = Deencapsulation.getField(amqpsDeviceTelemetry, "senderLinkEndpointPath");
        String receiverLinkEndpointPath = Deencapsulation.getField(amqpsDeviceTelemetry, "receiverLinkEndpointPath");
        String senderLinkTag = Deencapsulation.invoke(amqpsDeviceTelemetry, "getSenderLinkTag");
        String receiverLinkTag = Deencapsulation.invoke(amqpsDeviceTelemetry, "getReceiverLinkTag");
        String senderLinkAddress = Deencapsulation.invoke(amqpsDeviceTelemetry, "getSenderLinkAddress");
        String receiverLinkAddress = Deencapsulation.invoke(amqpsDeviceTelemetry, "getReceiverLinkAddress");

        assertNotNull(amqpsDeviceTelemetry);

        assertTrue(SENDER_LINK_ENDPOINT_PATH.equals(senderLinkEndpointPath));
        assertTrue(RECEIVER_LINK_ENDPOINT_PATH.equals(receiverLinkEndpointPath));

        assertTrue(senderLinkTag.startsWith(SENDER_LINK_TAG_PREFIX));
        assertTrue(receiverLinkTag.startsWith(RECEIVER_LINK_TAG_PREFIX));

        assertTrue(senderLinkTag.endsWith(uuidStr));
        assertTrue(receiverLinkTag.endsWith(uuidStr));

        assertTrue(senderLinkAddress.contains(mockDeviceClientConfig.getDeviceId()));
        assertTrue(receiverLinkAddress.contains(mockDeviceClientConfig.getDeviceId()));
    }

    // Tests_SRS_AMQPSDEVICETELEMETRY_34_034: [If a moduleId is present, the constructor shall set the sender and receiver endpoint path to IoTHub specific values for module communication.]
    // Tests_SRS_AMQPSDEVICETELEMETRY_34_035: [If a moduleId is present, the constructor shall concatenate a sender specific prefix including the moduleId to the sender link tag's current value.]
    // Tests_SRS_AMQPSDEVICETELEMETRY_34_036: [If a moduleId is present, the constructor shall insert the given deviceId and moduleId argument to the sender and receiver link address.]
    // Tests_SRS_AMQPSDEVICETELEMETRY_34_037: [If a moduleId is present, the constructor shall add correlation ID key and <deviceId>/<moduleId> value to the amqpProperties.]
    @Test
    public void constructorInitializesAllMembersWithModuleId(
            @Mocked final UUID mockUUID
    )
    {
        // arrange
        final String deviceId = "deviceId";
        final String moduleId = "moduleId";
        final String uuidStr = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
        new NonStrictExpectations()
        {
            {
                UUID.randomUUID();
                result = mockUUID;
                mockUUID.toString();
                result = uuidStr;
                mockDeviceClientConfig.getDeviceId();
                result = deviceId;
                mockDeviceClientConfig.getModuleId();
                result = moduleId;
            }
        };

        //act
        AmqpsDeviceTelemetry amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);


        String SENDER_LINK_ENDPOINT_PATH_MODULES = Deencapsulation.getField(amqpsDeviceMethods, "SENDER_LINK_ENDPOINT_PATH_MODULES");
        String RECEIVER_LINK_ENDPOINT_PATH_MODULES = Deencapsulation.getField(amqpsDeviceMethods, "RECEIVER_LINK_ENDPOINT_PATH_MODULES");
        String SENDER_LINK_TAG_PREFIX = Deencapsulation.getField(amqpsDeviceMethods, "SENDER_LINK_TAG_PREFIX");
        String RECEIVER_LINK_TAG_PREFIX = Deencapsulation.getField(amqpsDeviceMethods, "RECEIVER_LINK_TAG_PREFIX");
        String senderLinkEndpointPath = Deencapsulation.getField(amqpsDeviceMethods, "senderLinkEndpointPath");
        String receiverLinkEndpointPath = Deencapsulation.getField(amqpsDeviceMethods, "receiverLinkEndpointPath");

        String senderLinkTag = Deencapsulation.invoke(amqpsDeviceMethods, "getSenderLinkTag");
        String receiverLinkTag = Deencapsulation.invoke(amqpsDeviceMethods, "getReceiverLinkTag");
        String senderLinkAddress = Deencapsulation.invoke(amqpsDeviceMethods, "getSenderLinkAddress");
        String receiverLinkAddress = Deencapsulation.invoke(amqpsDeviceMethods, "getReceiverLinkAddress");

        //assert
        assertNotNull(amqpsDeviceMethods);

        assertTrue(SENDER_LINK_ENDPOINT_PATH_MODULES.equals(senderLinkEndpointPath));
        assertTrue(RECEIVER_LINK_ENDPOINT_PATH_MODULES.equals(receiverLinkEndpointPath));

        assertTrue(senderLinkTag.startsWith(SENDER_LINK_TAG_PREFIX));
        assertTrue(receiverLinkTag.startsWith(RECEIVER_LINK_TAG_PREFIX));

        assertTrue(senderLinkTag.contains(moduleId));
        assertTrue(receiverLinkTag.contains(moduleId));

        assertTrue(senderLinkTag.endsWith(uuidStr));
        assertTrue(receiverLinkTag.endsWith(uuidStr));

        assertTrue(senderLinkAddress.contains(mockDeviceClientConfig.getDeviceId()));
        assertTrue(receiverLinkAddress.contains(mockDeviceClientConfig.getDeviceId()));

        assertTrue(senderLinkAddress.contains(mockDeviceClientConfig.getModuleId()));
        assertTrue(receiverLinkAddress.contains(mockDeviceClientConfig.getModuleId()));
    }

    // Tests_SRS_AMQPSDEVICETELEMETRY_34_050: [This constructor shall call super with the provided user agent string.]
    @Test
    public void constructorCallsSuperWithConfigUserAgentString()
    {
        //arrange
        final String expectedUserAgentString = "asdf";

        new NonStrictExpectations()
        {
            {
                mockDeviceClientConfig.getProductInfo();
                result = mockedProductInfo;

                mockedProductInfo.getUserAgentString();
                result = expectedUserAgentString;
            }
        };

        //act
        AmqpsDeviceTelemetry actual = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);

        //assert
        Map<Symbol, Object> amqpProperties = Deencapsulation.getField(actual, "amqpProperties");
        assertTrue(amqpProperties.containsValue(expectedUserAgentString));
    }

    // Tests_SRS_AMQPSDEVICETELEMETRY_12_026: [The function shall return true and set the sendLinkState to OPENED if the senderLinkTag is equal to the given linkName.]
    @Test
    public void isLinkFoundReturnsTrueIfSenderLinkTagMatches()
    {
        // arrange
        String linkName = "linkName";

        //act
        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
        Deencapsulation.setField(amqpsDeviceTelemetry, "senderLinkTag", linkName);
        AmqpsDeviceOperationLinkState linkSate1 = Deencapsulation.getField(amqpsDeviceTelemetry, "amqpsSendLinkState");
        Boolean retVal = Deencapsulation.invoke(amqpsDeviceTelemetry, "isLinkFound", linkName);
        AmqpsDeviceOperationLinkState linkSate2 = Deencapsulation.getField(amqpsDeviceTelemetry, "amqpsSendLinkState");

        // assert
        assertTrue(retVal);
        assertEquals(linkSate1, AmqpsDeviceOperationLinkState.CLOSED);
        assertEquals(linkSate2, AmqpsDeviceOperationLinkState.OPENED);
    }

    // Tests_SRS_AMQPSDEVICETELEMETRY_12_027: [The function shall return true and set the recvLinkState to OPENED if the receiverLinkTag is equal to the given linkName.]
    @Test
    public void isLinkFoundReturnsTrueIfReceiverLinkTagMatches()
    {
        // arrange
        String linkName = "linkName";

        //act
        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
        Deencapsulation.setField(amqpsDeviceTelemetry, "receiverLinkTag", linkName);
        AmqpsDeviceOperationLinkState linkSate1 = Deencapsulation.getField(amqpsDeviceTelemetry, "amqpsRecvLinkState");
        Boolean retVal = Deencapsulation.invoke(amqpsDeviceTelemetry, "isLinkFound", linkName);
        AmqpsDeviceOperationLinkState linkSate2 = Deencapsulation.getField(amqpsDeviceTelemetry, "amqpsRecvLinkState");

        // assert
        assertTrue(retVal);
        assertEquals(linkSate1, AmqpsDeviceOperationLinkState.CLOSED);
        assertEquals(linkSate2, AmqpsDeviceOperationLinkState.OPENED);
    }

    // Tests_SRS_AMQPSDEVICETELEMETRY_12_028: [The function shall return false if neither the senderLinkTag nor the receiverLinkTag is matcing with the given linkName.]
    @Test
    public void isLinkFoundReturnsFalseIfThereIsNoMatch()
    {
        // arrange
        String linkName = "linkName";

        //act
        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
        Deencapsulation.setField(amqpsDeviceTelemetry, "senderLinkTag", "xxx");
        Deencapsulation.setField(amqpsDeviceTelemetry, "receiverLinkTag", "yyy");
        Boolean retVal = Deencapsulation.invoke(amqpsDeviceTelemetry, "isLinkFound", linkName);

        // assert
        Assert.assertFalse(retVal);
    }

    /*
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_006: [**The function shall return an AmqpsSendReturnValue object with false and -1 if the message type is not telemetry.**]**
    */
    @Test
    public void sendMessageAndGetDeliveryTagReturnsFalseIfMessageTypeIsNotDeviceTelemetry() throws IOException
    {
        //arrange
        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
        final byte[] msgData = new byte[1];
        final int offset = 0;
        final int length = 1;
        byte[] deliveryTag = "0".getBytes();

        //act
        AmqpsSendReturnValue amqpsSendReturnValue = Deencapsulation.invoke(amqpsDeviceTelemetry, "sendMessageAndGetDeliveryTag", MessageType.DEVICE_METHODS, msgData, offset, length, deliveryTag);
        boolean deliverySuccessful = Deencapsulation.invoke(amqpsSendReturnValue, "isDeliverySuccessful");
        deliveryTag = Deencapsulation.invoke(amqpsSendReturnValue, "getDeliveryTag");


        //assert
        assertEquals(false, deliverySuccessful);
        assertEquals(-1, Integer.parseInt(new String(deliveryTag)));
    }

    /*
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_007: [**The function shall call the ssuper function with the arguments and return with it's return value.**]**
    */
    @Test
    public void sendMessageAndGetDeliveryTagReturnsWithSuperResult() throws IOException
    {
        //arrange
        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceTelemetry, "openLinks", mockSession);
        final byte[] msgData = new byte[1];
        final int offset = 0;
        final int length = 1;
        byte[] deliveryTag = "0".getBytes();
        amqpsDeviceTelemetry.onLinkFlow(100);

        //act
        AmqpsSendReturnValue amqpsSendReturnValue = Deencapsulation.invoke(amqpsDeviceTelemetry, "sendMessageAndGetDeliveryTag", MessageType.DEVICE_TELEMETRY, msgData, offset, length, deliveryTag);
            boolean deliverySuccessful = Deencapsulation.invoke(amqpsSendReturnValue, "isDeliverySuccessful");
            deliveryTag = Deencapsulation.invoke(amqpsSendReturnValue, "getDeliveryTag");

        //assert
        assertTrue(deliverySuccessful);
        assertTrue(Integer.parseInt(new String(deliveryTag)) > -1);
    }

    /*
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_008: [**The function shall return null if the Proton message type is not null or DeviceTelemetry.**]**
    */
    @Test
    public void convertFromProtonReturnsNullIfNotDeviceTelemetry(
            @Mocked final  AmqpsMessage mockAmqpsMessage
    )
    {
        //arrange
        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);

        new NonStrictExpectations()
        {
            {
                mockAmqpsMessage.getAmqpsMessageType();
                result = MessageType.DEVICE_METHODS;
            }
        };

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTelemetry, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);

        //assert
        assertEquals(null, amqpsConvertFromProtonReturnValue);
    }

    @Test
    public void convertFromProtonEmptyBodySuccess(
            @Mocked final AmqpsMessage mockAmqpsMessage,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    )
    {
        //arrange
        String deviceId = "deviceId";
        byte[] bytes = new byte[1];
        final Object messageContext = "context";

        new NonStrictExpectations()
        {
            {
                mockAmqpsMessage.getAmqpsMessageType();
                result = MessageType.DEVICE_TELEMETRY;
                mockAmqpsMessage.getMessageAnnotations();
                result = null;
                mockAmqpsMessage.getProperties();
                result = null;
                mockDeviceClientConfig.getDeviceTelemetryMessageCallback(anyString);
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceTelemetryMessageContext(anyString);
                result = messageContext;
                mockAmqpsMessage.getBody();
                result = null;

                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";            }
        };

        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceTelemetry, "openLinks", mockSession);

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTelemetry, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        assertNotNull(actualMessage);
        assertEquals(0, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    /*
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_014: [**The function shall return null if the Proton message type is not null or DeviceTelelemtry.**]**
    */
    @Test
    public void convertToProtonReturnsNullIfNotDeviceTelemetry(
            @Mocked final Message mockMessage,
            @Mocked final Properties properties,
            @Mocked final  AmqpsMessage mockAmqpsMessage
    )
    {
        //arrange
        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
        final byte[] msgData = new byte[1];
        final int offset = 0;
        final int length = 1;
        final byte[] deliveryTag = new byte[1];

        new NonStrictExpectations()
        {
            {
                mockAmqpsMessage.getAmqpsMessageType();
                result = MessageType.DEVICE_METHODS;
            }
        };

        //act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTelemetry, "convertToProton", mockMessage);

        //assert
        assertNull(amqpsConvertToProtonReturnValue);
    }



    // Tests_SRS_AMQPSDEVICETELEMETRY_12_020: [The function shall call the super function.]
    // Tests_SRS_AMQPSDEVICETELEMETRY_12_021: [The function shall set the MessageType to DEVICE_TELEMETRY if the super function returned not null.]
    // Tests_SRS_AMQPSDEVICETELEMETRY_12_022: [The function shall return the super function return value.]
    @Test
    public void getMessageFromReceiverLinkSuccess() throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        String linkName = "receiver";
        byte[] bytes = new byte[1];

        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceTelemetry, "openLinks", mockSession);
        Deencapsulation.setField(amqpsDeviceTelemetry, "receiverLink", mockReceiver);
        Deencapsulation.setField(amqpsDeviceTelemetry, "senderLink", mockSender);
        Deencapsulation.setField(amqpsDeviceTelemetry, "receiverLinkTag", linkName);

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
        AmqpsMessage amqpsMessage = Deencapsulation.invoke(amqpsDeviceTelemetry, "getMessageFromReceiverLink", linkName);

        //assert
        assertNotNull(amqpsMessage);
        assertEquals(MessageType.DEVICE_TELEMETRY, amqpsMessage.getAmqpsMessageType());
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

    // Tests_SRS_AMQPSDEVICETELEMETRY_12_022: [The function shall return the super function return value.]
    @Test
    public void getMessageFromReceiverLinkSuperFailed() throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        String linkName = "receiver";
        byte[] bytes = new byte[1];


        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceTelemetry, "openLinks", mockSession);
        Deencapsulation.setField(amqpsDeviceTelemetry, "receiverLink", mockReceiver);
        Deencapsulation.setField(amqpsDeviceTelemetry, "senderLink", mockSender);
        Deencapsulation.setField(amqpsDeviceTelemetry, "receiverLinkTag", linkName);

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
        AmqpsMessage amqpsMessage = Deencapsulation.invoke(amqpsDeviceTelemetry, "getMessageFromReceiverLink", linkName);

        //assert
        assertNull(amqpsMessage);
        new Verifications()
        {
            {
                mockReceiver.current();
                times = 1;
                mockDelivery.isReadable();
                times = 1;
                mockDelivery.isPartial();
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSDEVICETELEMETRY_34_052: [If the amqp message contains an application property of
    // "x-opt-input-name", this function shall assign its value to the IotHub message's input name.]
    @Test
    public void protonMessageToIoTHubMessageWithInputName(@Mocked final MessageImpl mockMessage, @Mocked final MessageAnnotations mockedAnnotations, @Mocked final Data mockData)
    {
        //arrange
        final String expectedInputName = "some input name";
        final Map<Symbol, Object> mockedAnnotationsMap = new HashMap<>();
        mockedAnnotationsMap.put(Deencapsulation.newInstance(Symbol.class, Deencapsulation.getField(AmqpsDeviceTelemetry.class, "INPUT_NAME_PROPERTY_KEY")), expectedInputName);
        new NonStrictExpectations()
        {
            {
                mockMessage.getBody();
                result = mockData;

                mockMessage.getMessageAnnotations();
                result = mockedAnnotations;

                mockedAnnotations.getValue();
                result = mockedAnnotationsMap;
            }
        };

        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);

        //act
        Message message = Deencapsulation.invoke(amqpsDeviceTelemetry, "protonMessageToIoTHubMessage", mockMessage);

        //assert
        assertEquals(expectedInputName, message.getInputName());
    }

    // Tests_SRS_AMQPSDEVICETELEMETRY_34_051: [This function shall set the message's saved outputname in the application properties of the new proton message.]
    @Test
    public void IoTHubMessageToProtonMessageWithOutputName(@Mocked final Message mockMessage)
    {
        //arrange
        final String expectedOutputName = "some output name";
        new NonStrictExpectations()
        {
            {
                mockMessage.getOutputName();
                result = expectedOutputName;
            }
        };

        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);

        //act
        MessageImpl message = Deencapsulation.invoke(amqpsDeviceTelemetry, "iotHubMessageToProtonMessage", mockMessage);

        //assert
        assertTrue(message.getApplicationProperties().getValue().containsKey(OUTPUT_NAME_PROPERTY));
        assertEquals(expectedOutputName, message.getApplicationProperties().getValue().get(OUTPUT_NAME_PROPERTY));
    }
}


