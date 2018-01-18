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
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
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

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

/**
*  Unit tests for AmqpsDeviceTelemetryTest
* 100% methods covered
* 98% lines covered
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

    // Tests_SRS_AMQPSDEVICETELEMETRY_12_001: [The constructor shall throw IllegalArgumentException if the deviceClientConfig argument is null.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfDeviceIdNull()
    {
        //act
        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class);
    }

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
        String SENDER_LINK_ENDPOINT_PATH = Deencapsulation.getField(amqpsDeviceTelemetry, "SENDER_LINK_ENDPOINT_PATH");
        String RECEIVER_LINK_ENDPOINT_PATH = Deencapsulation.getField(amqpsDeviceTelemetry, "RECEIVER_LINK_ENDPOINT_PATH");
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
    public void sendMessageAndGetDeliveryHashReturnsFalseIfMessageTypeIsNotDeviceTelemetry() throws IOException
    {
        //arrange
        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
        final byte[] msgData = new byte[1];
        final int offset = 0;
        final int length = 1;
        final byte[] deliveryTag = new byte[0];

        //act
        AmqpsSendReturnValue amqpsSendReturnValue = Deencapsulation.invoke(amqpsDeviceTelemetry, "sendMessageAndGetDeliveryHash", MessageType.DEVICE_METHODS, msgData, offset, length, deliveryTag);
        boolean deliverySuccessful = Deencapsulation.invoke(amqpsSendReturnValue, "isDeliverySuccessful");
        int deliveryHash = Deencapsulation.invoke(amqpsSendReturnValue, "getDeliveryHash");


        //assert
        assertEquals(false, deliverySuccessful);
        assertEquals(-1, deliveryHash);
    }

    /*
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_007: [**The function shall call the ssuper function with the arguments and return with it's return value.**]**
    */
    @Test
    public void sendMessageAndGetDeliveryHashReturnsWithSuperResult() throws IOException
    {
        //arrange
        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceTelemetry, "openLinks", mockSession);
        final byte[] msgData = new byte[1];
        final int offset = 0;
        final int length = 1;
        final byte[] deliveryTag = new byte[1];

        //act
        AmqpsSendReturnValue amqpsSendReturnValue = Deencapsulation.invoke(amqpsDeviceTelemetry, "sendMessageAndGetDeliveryHash", MessageType.DEVICE_TELEMETRY, msgData, offset, length, deliveryTag);
            boolean deliverySuccessful = Deencapsulation.invoke(amqpsSendReturnValue, "isDeliverySuccessful");
            int deliveryHash = Deencapsulation.invoke(amqpsSendReturnValue, "getDeliveryHash");

        //assert
        assertTrue(deliverySuccessful);
        assertTrue(deliveryHash > 0);
    }

    /*
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_008: [**The function shall return null if the Proton message type is not null or DeviceTelelemtry.**]**
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

    // Codes_SRS_AMQPSDEVICETELEMETRY_12_025: [The function shall create a new empty buffer for message body if the proton message body is null.]
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

        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceTelemetry, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                mockAmqpsMessage.getAmqpsMessageType();
                result = MessageType.DEVICE_TELEMETRY;
                mockAmqpsMessage.getMessageAnnotations();
                result = null;
                mockAmqpsMessage.getProperties();
                result = null;
                mockDeviceClientConfig.getDeviceTelemetryMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceTelemetryMessageContext();
                result = messageContext;
                mockAmqpsMessage.getBody();
                result = null;
            }
        };

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
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_009: [**The function shall create a new IoTHubMessage using the Proton message body.**]**
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_010: [**The function shall copy the correlationId, messageId, To and userId properties to the IotHubMessage properties.**]**
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_011: [**The function shall copy the Proton application properties to IoTHubMessage properties excluding the reserved property names.**]**
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_012: [**The function shall create a new AmqpsConvertFromProtonReturnValue object and fill it with the converted message and the user callback and user context values from the deviceClientConfig.**]**
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_013: [**The function shall return with the new AmqpsConvertFromProtonReturnValue object.**]**
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_024: [**The function shall shall create a new buffer for message body and copy the proton message body to it.**]**
    */
    @Test
    public void convertFromProtonSuccess(
            @Mocked final MessageImpl mockProtonMessage,
            @Mocked final Properties properties,
            @Mocked final MessageCallback mockMessageCallback,
            @Mocked final  AmqpsMessage mockAmqpsMessage
    )
    {
        //arrange
        final String correlationId = "1234";
        final String messageId = "5678";
        final Binary userId = new Binary("user1".getBytes());
        final String to = "devices/deviceID/messages/devicebound/";
        final Date absoluteExpiryTime = new Date(Long.MAX_VALUE);
        final String customPropertyKey = "appProp";
        final String customPropertyValue = "appValue";
        final String toKey = "to";
        final String userIdKey = "userId";

        Map<String, Object> applicationPropertiesMap = new HashMap();
        applicationPropertiesMap.put(customPropertyKey, customPropertyValue);

        final ApplicationProperties applicationProperties = new ApplicationProperties(applicationPropertiesMap);

        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
        final String AMQPS_APP_PROPERTY_PREFIX = Deencapsulation.getField(amqpsDeviceTelemetry, "AMQPS_APP_PROPERTY_PREFIX");
        final byte[] msgData = new byte[1];
        final int offset = 0;
        final int length = 1;
        final byte[] deliveryTag = new byte[1];

        new NonStrictExpectations()
        {
            {
                mockAmqpsMessage.getAmqpsMessageType();
                result = MessageType.DEVICE_TELEMETRY;
                mockProtonMessage.getBody();
                result = new Data(new Binary("body".getBytes()));
                mockProtonMessage.getApplicationProperties();
                result = applicationProperties;
                properties.getMessageId();
                result = messageId;
                properties.getCorrelationId();
                result = correlationId;
                properties.getTo();
                result = to;
                properties.getUserId();
                result = userId;
                properties.getAbsoluteExpiryTime();
                result = absoluteExpiryTime;
                mockDeviceClientConfig.getDeviceTelemetryMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceTelemetryMessageContext();
                result = "myContext";

            }
        };

        //act
        final AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTelemetry, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        assertTrue(amqpsConvertFromProtonReturnValue != null);
        assertTrue(actualMessageCallback != null);
        assertTrue(actualMessageContext != null);

        assertEquals(correlationId, actualMessage.getCorrelationId());
        assertEquals(messageId, actualMessage.getMessageId());
        assertFalse(actualMessage.isExpired());

        assertTrue(applicationProperties.getValue().containsKey(customPropertyKey));
        assertNotNull(actualMessage.getProperty(customPropertyKey));
        assertEquals(customPropertyValue, actualMessage.getProperty(customPropertyKey));

        assertNotNull(actualMessage.getProperty(AMQPS_APP_PROPERTY_PREFIX + toKey));
        assertEquals(to, actualMessage.getProperty(AMQPS_APP_PROPERTY_PREFIX + toKey));
        assertNotNull(actualMessage.getProperty(AMQPS_APP_PROPERTY_PREFIX + userIdKey));
        assertEquals(userId.toString(), actualMessage.getProperty(AMQPS_APP_PROPERTY_PREFIX + userIdKey));

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

    /*
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_015: [**The function shall create a new Proton message using the IoTHubMessage body.**]**
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_016: [**The function shall copy the correlationId, messageId properties to the Proton message properties.**]**
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_017: [**The function shall copy the user properties to Proton message application properties excluding the reserved property names.**]**
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_018: [**The function shall create a new AmqpsConvertToProtonReturnValue object and fill it with the Proton message and the message type.**]**
    **Tests_SRS_AMQPSDEVICETELEMETRY_12_019: [**The function shall return with the new AmqpsConvertToProtonReturnValue object.**]**
    */
    @Test
    public void convertToProtonSuccess(
            @Mocked final Message mockMessage,
            @Mocked final MessageImpl mockProtonMessage
    )
    {
        //arrange
        final String correlationId = "1234";
        final String messageId = "5678";
        final MessageProperty[] iotHubMessageProperties = new MessageProperty[]
                {
                        new MessageProperty("key1", "value1"),
                        new MessageProperty("key2", "value2")
                };
        final Map<String, Object> userProperties = new HashMap<>(2);
        userProperties.put(iotHubMessageProperties[0].getName(), iotHubMessageProperties[0].getValue());
        userProperties.put(iotHubMessageProperties[1].getName(), iotHubMessageProperties[1].getValue());

        AmqpsDeviceTelemetry amqpsDeviceTelemetry = Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
        final byte[] msgData = new byte[1];
        final int offset = 0;
        final int length = 1;
        final byte[] deliveryTag = new byte[1];

        new NonStrictExpectations()
        {
            {
                mockMessage.getMessageType();
                result = MessageType.DEVICE_TELEMETRY;
                mockMessage.getMessageId();
                result = messageId;
                mockMessage.getCorrelationId();
                result = correlationId;
                mockMessage.getProperties();
                result = iotHubMessageProperties;
                new ApplicationProperties(userProperties);
            }
        };

        //act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTelemetry, "convertToProton", mockMessage);

        //assert
        assertTrue(amqpsConvertToProtonReturnValue != null);
        assertTrue(Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageImpl") != null);
        assertTrue(Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageType") == MessageType.DEVICE_TELEMETRY);
        new Verifications()
        {
            {
                mockProtonMessage.setProperties((Properties)any);
                mockProtonMessage.setApplicationProperties((ApplicationProperties) any);
                times = 1;
            }
        };
    }

    // Codes_SRS_AMQPSDEVICETELEMETRY_12_020: [The function shall call the super function.]
    // Codes_SRS_AMQPSDEVICETELEMETRY_12_021: [The function shall set the MessageType to DEVICE_TELEMETRY if the super function returned not null.]
    // Codes_SRS_AMQPSDEVICETELEMETRY_12_022: [The function shall return the super function return value.]
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

    // Codes_SRS_AMQPSDEVICETELEMETRY_12_022: [The function shall return the super function return value.]
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
}


