/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.impl.MessageImpl;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
*  Unit tests for AmqpsDeviceMethods
* 100% methods covered
* 98% lines covered
*/
public class AmqpsDeviceMethodsTest
{
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
    IotHubConnectionString mockIotHubConnectionString;

    /*
    **Tests_SRS_AMQPSDEVICEMETHODS_12_001: [**The constructor shall throw IllegalArgumentException if the deviceId argument is null or empty.**]**
    */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfDeviceClientConfigNull()
    {
        //act
        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class);
    }

    // Tests_SRS_AMQPSDEVICEMETHODS_12_047: [The function shall return true and set the sendLinkState to OPENED if the senderLinkTag is equal to the given linkName.]
    @Test
    public void isLinkFoundReturnsTrueIfSenderLinkTagMatches()
    {
        // arrange
        String linkName = "linkName";

        //act
        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        Deencapsulation.setField(amqpsDeviceMethods, "senderLinkTag", linkName);
        AmqpsDeviceOperationLinkState linkSate1 = Deencapsulation.getField(amqpsDeviceMethods, "amqpsSendLinkState");
        Boolean retVal = Deencapsulation.invoke(amqpsDeviceMethods, "isLinkFound", linkName);
        AmqpsDeviceOperationLinkState linkSate2 = Deencapsulation.getField(amqpsDeviceMethods, "amqpsSendLinkState");

        // assert
        assertTrue(retVal);
        assertEquals(linkSate1, AmqpsDeviceOperationLinkState.CLOSED);
        assertEquals(linkSate2, AmqpsDeviceOperationLinkState.OPENED);
    }

    // Tests_SRS_AMQPSDEVICEMETHODS_12_048: [The function shall return true and set the recvLinkState to OPENED if the receiverLinkTag is equal to the given linkName.]
    @Test
    public void isLinkFoundReturnsTrueIfReceiverLinkTagMatches()
    {
        // arrange
        String linkName = "linkName";

        //act
        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        Deencapsulation.setField(amqpsDeviceMethods, "receiverLinkTag", linkName);
        AmqpsDeviceOperationLinkState linkSate1 = Deencapsulation.getField(amqpsDeviceMethods, "amqpsRecvLinkState");
        Boolean retVal = Deencapsulation.invoke(amqpsDeviceMethods, "isLinkFound", linkName);
        AmqpsDeviceOperationLinkState linkSate2 = Deencapsulation.getField(amqpsDeviceMethods, "amqpsRecvLinkState");

        // assert
        assertTrue(retVal);
        assertEquals(linkSate1, AmqpsDeviceOperationLinkState.CLOSED);
        assertEquals(linkSate2, AmqpsDeviceOperationLinkState.OPENED);
    }

    // Tests_SRS_AMQPSDEVICEMETHODS_12_049: [The function shall return false if neither the senderLinkTag nor the receiverLinkTag is matcing with the given linkName.]
    @Test
    public void isLinkFoundReturnsFalseIfThereIsNoMatch()
    {
        // arrange
        String linkName = "linkName";

        //act
        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        Deencapsulation.setField(amqpsDeviceMethods, "senderLinkTag", "xxx");
        Deencapsulation.setField(amqpsDeviceMethods, "receiverLinkTag", "yyy");
        Boolean retVal = Deencapsulation.invoke(amqpsDeviceMethods, "isLinkFound", linkName);

        // assert
        assertFalse(retVal);
    }

    /*
    **Tests_SRS_AMQPSDEVICEMETHODS_12_002: [**The constructor shall set the sender and receiver endpoint path to IoTHub specific values.**]**
    **Tests_SRS_AMQPSDEVICEMETHODS_12_003: [**The constructor shall concatenate a sender specific prefix to the sender link tag's current value.**]**
    **Tests_SRS_AMQPSDEVICEMETHODS_12_004: [**The constructor shall concatenate a receiver specific prefix to the receiver link tag's current value.**]**
    **Tests_SRS_AMQPSDEVICEMETHODS_12_005: [**The constructor shall insert the given deviceId argument to the sender and receiver link address.**]**
    **Tests_SRS_AMQPSDEVICEMETHODS_12_006: [**The constructor shall add API version key to the amqpProperties.**]**     */
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
        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        String API_VERSION_KEY = Deencapsulation.getField(amqpsDeviceMethods, "API_VERSION_KEY");
        String CORRELATION_ID_KEY = Deencapsulation.getField(amqpsDeviceMethods, "CORRELATION_ID_KEY");
        String SENDER_LINK_ENDPOINT_PATH = Deencapsulation.getField(amqpsDeviceMethods, "SENDER_LINK_ENDPOINT_PATH");
        String RECEIVER_LINK_ENDPOINT_PATH = Deencapsulation.getField(amqpsDeviceMethods, "RECEIVER_LINK_ENDPOINT_PATH");
        String SENDER_LINK_TAG_PREFIX = Deencapsulation.getField(amqpsDeviceMethods, "SENDER_LINK_TAG_PREFIX");
        String RECEIVER_LINK_TAG_PREFIX = Deencapsulation.getField(amqpsDeviceMethods, "RECEIVER_LINK_TAG_PREFIX");
        String senderLinkEndpointPath = Deencapsulation.getField(amqpsDeviceMethods, "senderLinkEndpointPath");
        String receiverLinkEndpointPath = Deencapsulation.getField(amqpsDeviceMethods, "receiverLinkEndpointPath");

        Map<Symbol, Object> amqpsProperties = Deencapsulation.invoke(amqpsDeviceMethods, "getAmqpProperties");
        String senderLinkTag = Deencapsulation.invoke(amqpsDeviceMethods, "getSenderLinkTag");
        String receiverLinkTag = Deencapsulation.invoke(amqpsDeviceMethods, "getReceiverLinkTag");
        String senderLinkAddress = Deencapsulation.invoke(amqpsDeviceMethods, "getSenderLinkAddress");
        String receiverLinkAddress = Deencapsulation.invoke(amqpsDeviceMethods, "getReceiverLinkAddress");

        //assert
        assertNotNull(amqpsDeviceMethods);

        assertTrue(SENDER_LINK_ENDPOINT_PATH.equals(senderLinkEndpointPath));
        assertTrue(RECEIVER_LINK_ENDPOINT_PATH.equals(receiverLinkEndpointPath));

        assertTrue(senderLinkTag.startsWith(SENDER_LINK_TAG_PREFIX));
        assertTrue(receiverLinkTag.startsWith(RECEIVER_LINK_TAG_PREFIX));

        assertTrue(senderLinkTag.endsWith(uuidStr));
        assertTrue(receiverLinkTag.endsWith(uuidStr));

        assertTrue(senderLinkAddress.contains(mockDeviceClientConfig.getDeviceId()));
        assertTrue(receiverLinkAddress.contains(mockDeviceClientConfig.getDeviceId()));

        assertTrue(amqpsProperties.containsKey(Symbol.getSymbol(API_VERSION_KEY)));
        assertTrue(amqpsProperties.containsKey(Symbol.getSymbol(CORRELATION_ID_KEY)));
    }

    // Codes_SRS_AMQPSDEVICEMETHODS_12_010: [The function shall call the super function if the MessageType is DEVICE_METHODS, and return with it's return value.]
    @Test
    public void sendMessageAndGetDeliveryHashCallsSuper() throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        byte[] bytes = new byte[1];

        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceMethods, "openLinks", mockSession);

        //act
        AmqpsSendReturnValue amqpsSendReturnValue = Deencapsulation.invoke(amqpsDeviceMethods, "sendMessageAndGetDeliveryHash", MessageType.DEVICE_METHODS, bytes, 0, 1, bytes);
        boolean deliverySuccessful = Deencapsulation.invoke(amqpsSendReturnValue, "isDeliverySuccessful");
        int deliveryHash = Deencapsulation.invoke(amqpsSendReturnValue, "getDeliveryHash");

        //assert
        assertEquals(true, deliverySuccessful);
        assertNotEquals(-1, deliveryHash);
    }

    // Codes_SRS_AMQPSDEVICEMETHODS_12_011: [The function shall return with AmqpsSendReturnValue with false success and -1 delivery hash.]
    @Test
    public void sendMessageAndGetDeliveryHashReturnsFalse() throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        byte[] bytes = new byte[1];

        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceMethods, "openLinks", mockSession);

        //act
        AmqpsSendReturnValue amqpsSendReturnValue = Deencapsulation.invoke(amqpsDeviceMethods, "sendMessageAndGetDeliveryHash",MessageType.DEVICE_TWIN, bytes, 0, 1, bytes);
        boolean deliverySuccessful = Deencapsulation.invoke(amqpsSendReturnValue, "isDeliverySuccessful");
        int deliveryHash = Deencapsulation.invoke(amqpsSendReturnValue, "getDeliveryHash");

        //assert
        assertEquals(false, deliverySuccessful);
        assertEquals(-1, deliveryHash);
    }

    // Codes_SRS_AMQPSDEVICEMETHODS_12_012: [The function shall call the super function.]
    // Codes_SRS_AMQPSDEVICEMETHODS_12_013: [The function shall set the MessageType to DEVICE_METHODS if the super function returned not null.]
    // Codes_SRS_AMQPSDEVICEMETHODS_12_014: [The function shall return the super function return value.]
    @Test
    public void getMessageFromReceiverLinkSuccess() throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        String linkName = "receiver";
        byte[] bytes = new byte[1];

        new NonStrictExpectations()
        {
            {
                mockReceiver.current();
                result = mockDelivery;
                mockDelivery.isReadable();
                result = true;
                mockDelivery.isPartial();
                result = false;

                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";               }
        };

        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceMethods, "openLinks", mockSession);
        Deencapsulation.setField(amqpsDeviceMethods, "receiverLink", mockReceiver);
        Deencapsulation.setField(amqpsDeviceMethods, "senderLink", mockSender);
        Deencapsulation.setField(amqpsDeviceMethods, "receiverLinkTag", linkName);

        //act
        AmqpsMessage amqpsMessage = Deencapsulation.invoke(amqpsDeviceMethods, "getMessageFromReceiverLink", linkName);

        //assert
        assertNotNull(amqpsMessage);
        assertEquals(MessageType.DEVICE_METHODS, amqpsMessage.getAmqpsMessageType());
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

    // Codes_SRS_AMQPSDEVICEMETHODS_12_014: [The function shall return the super function return value.]
    @Test
    public void getMessageFromReceiverLinkSuperFailed() throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        String linkName = "receiver";
        byte[] bytes = new byte[1];

        new NonStrictExpectations()
        {
            {
                mockReceiver.current();
                result = mockDelivery;
                mockDelivery.isReadable();
                result = false;
                mockDelivery.isPartial();
                result = false;

                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";               }
        };

        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceMethods, "openLinks", mockSession);
        Deencapsulation.setField(amqpsDeviceMethods, "receiverLink", mockReceiver);
        Deencapsulation.setField(amqpsDeviceMethods, "senderLink", mockSender);
        Deencapsulation.setField(amqpsDeviceMethods, "receiverLinkTag", linkName);

        //act
        AmqpsMessage amqpsMessage = Deencapsulation.invoke(amqpsDeviceMethods, "getMessageFromReceiverLink", linkName);

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

    // Codes_SRS_AMQPSDEVICEMETHODS_12_015: [The function shall return null if the message type is not DEVICE_METHODS.]
    @Test
    public void convertFromProtonReturnsNull(
            @Mocked final AmqpsMessage mockAmqpsMessage,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    )
    {
        //arrange
        String deviceId = "deviceId";
        byte[] bytes = new byte[1];

        new NonStrictExpectations()
        {
            {
                mockAmqpsMessage.getAmqpsMessageType();
                result = MessageType.DEVICE_TWIN;

                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";            }
        };

        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceMethods, "openLinks", mockSession);

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceMethods, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);

        //assert
        assertNull(amqpsConvertFromProtonReturnValue);
    }

    // Codes_SRS_AMQPSDEVICEMETHODS_12_016: [The function shall convert the amqpsMessage to IoTHubTransportMessage.]
    // Codes_SRS_AMQPSDEVICEMETHODS_12_017: [The function shall create a new empty buffer for message body if the proton message body is null.]
    // Codes_SRS_AMQPSDEVICEMETHODS_12_027: [The function shall create a AmqpsConvertFromProtonReturnValue and set the message field to the new IotHubTransportMessage.]
    // Codes_SRS_AMQPSDEVICEMETHODS_12_028: [The function shall create a AmqpsConvertFromProtonReturnValue and copy the DeviceClientConfig callback and context to it.]
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
                result = MessageType.DEVICE_METHODS;
                mockAmqpsMessage.getMessageAnnotations();
                result = null;
                mockAmqpsMessage.getProperties();
                result = null;
                mockDeviceClientConfig.getDeviceMethodsMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceMethodsMessageContext();
                result = messageContext;
                mockAmqpsMessage.getBody();
                result = null;

                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";            }
        };

        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceMethods, "openLinks", mockSession);

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceMethods, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        assertNotNull(actualMessage);
        assertEquals(0, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Codes_SRS_AMQPSDEVICEMETHODS_12_025: [The function shall copy the correlationId, messageId, To and userId properties to the IotHubTransportMessage properties.]
    @Test
    public void convertFromProtonPropertiesSet(
            @Mocked final Properties mockProperties,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    )
    {
        //arrange
        String deviceId = "deviceId";
        final String correlationId = "correlationId";
        final String messageId = "messageId";
        final String to = "to";
        final byte[] bytes = new byte[] {1, 2};
        final Object messageContext = "context";

        AmqpsMessage amqpsMessage = new AmqpsMessage();
        final Binary userId = new Binary(bytes);
        Section section = new Data(userId);
        amqpsMessage.setBody(section);
        amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_METHODS);
        amqpsMessage.setMessageAnnotations(null);
        amqpsMessage.setProperties(mockProperties);
        amqpsMessage.setApplicationProperties(null);

        new NonStrictExpectations()
        {
            {
                mockProperties.getCorrelationId();
                result = correlationId;
                mockProperties.getMessageId();
                result = messageId;
                mockProperties.getTo();
                result = to;
                mockProperties.getUserId();
                result = userId;

                mockDeviceClientConfig.getDeviceMethodsMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceMethodsMessageContext();
                result = messageContext;

                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        final String AMQPS_APP_PROPERTY_PREFIX = Deencapsulation.getField(amqpsDeviceMethods, "AMQPS_APP_PROPERTY_PREFIX");
        final String TO_KEY = Deencapsulation.getField(amqpsDeviceMethods, "TO_KEY");
        final String USER_ID_KEY = Deencapsulation.getField(amqpsDeviceMethods, "USER_ID_KEY");
        Deencapsulation.invoke(amqpsDeviceMethods, "openLinks", mockSession);

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceMethods, "convertFromProton", amqpsMessage, mockDeviceClientConfig);
        IotHubTransportMessage actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        assertNotNull(actualMessage);
        assertEquals(correlationId, (actualMessage).getRequestId());
        assertEquals(messageId, ((IotHubTransportMessage)actualMessage).getMessageId());
        assertEquals(to, ((IotHubTransportMessage)actualMessage).getProperty(AMQPS_APP_PROPERTY_PREFIX + TO_KEY));
        assertEquals(userId.toString(), ((IotHubTransportMessage)actualMessage).getProperty(AMQPS_APP_PROPERTY_PREFIX + USER_ID_KEY));

        assertEquals(bytes.length, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Codes_SRS_AMQPSDEVICEMETHODS_12_026: [The function shall copy the Proton application properties to IotHubTransportMessage properties excluding the reserved property names.]
    @Test
    public void convertFromProtonApplicationPropertiesReservedNotSet(
            @Mocked final Map<String, String> mockMapStringString,
            @Mocked final ApplicationProperties mockApplicationProperties,
            @Mocked final Map.Entry<String, String> mockStringStringEntry,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    )
    {
        //arrange
        String deviceId = "deviceId";
        final byte[] bytes = new byte[] {1, 2};
        final Object messageContext = "context";
        final String propertyKey = "iothub-absolute-expiry-time";
        final String propertyValue = "testPropertyValue";

        AmqpsMessage amqpsMessage = new AmqpsMessage();
        Binary binary = new Binary(bytes);
        Section section = new Data(binary);
        amqpsMessage.setBody(section);
        amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_METHODS);
        amqpsMessage.setMessageAnnotations(null);
        amqpsMessage.setProperties(null);
        amqpsMessage.setApplicationProperties(mockApplicationProperties);

        new NonStrictExpectations()
        {
            {
                mockApplicationProperties.getValue();
                result = mockMapStringString;
                mockMapStringString.entrySet();
                result = mockStringStringEntry;
                mockStringStringEntry.getKey();
                result = propertyKey;
                mockStringStringEntry.getValue();
                result = propertyValue;

                mockDeviceClientConfig.getDeviceMethodsMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceMethodsMessageContext();
                result = messageContext;

                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceMethods, "openLinks", mockSession);

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceMethods, "convertFromProton", amqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        assertNotNull(actualMessage);
        assertEquals(null, ((IotHubTransportMessage)actualMessage).getProperty(propertyKey));

        assertEquals(bytes.length, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Codes_SRS_AMQPSDEVICEMETHODS_12_026: [The function shall copy the Proton application properties to IotHubTransportMessage properties excluding the reserved property names.]
    @Test
    public void convertFromProtonApplicationPropertiesSet(
            @Mocked final Map<String, String> mockMapStringString,
            @Mocked final ApplicationProperties mockApplicationProperties,
            @Mocked final Map.Entry<String, String> mockStringStringEntry,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    )
    {
        //arrange
        String deviceId = "deviceId";
        final byte[] bytes = new byte[] {1, 2};
        final Object messageContext = "context";
        final String propertyKey = "testPropertyKey";
        final String propertyValue = "testPropertyValue";

        AmqpsMessage amqpsMessage = new AmqpsMessage();
        Binary binary = new Binary(bytes);
        Section section = new Data(binary);
        amqpsMessage.setBody(section);
        amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_METHODS);
        amqpsMessage.setMessageAnnotations(null);
        amqpsMessage.setProperties(null);
        amqpsMessage.setApplicationProperties(mockApplicationProperties);

        new NonStrictExpectations()
        {
            {
                mockApplicationProperties.getValue();
                result = mockMapStringString;
                mockMapStringString.entrySet();
                result = mockStringStringEntry;
                mockStringStringEntry.getKey();
                result = propertyKey;
                mockStringStringEntry.getValue();
                result = propertyValue;

                mockDeviceClientConfig.getDeviceMethodsMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceMethodsMessageContext();
                result = messageContext;

                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceMethods, "openLinks", mockSession);

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceMethods, "convertFromProton", amqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        assertNotNull(actualMessage);
        assertEquals(propertyValue, ((IotHubTransportMessage)actualMessage).getProperty(propertyKey));

        assertEquals(bytes.length, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Codes_SRS_AMQPSDEVICEMETHODS_12_025: [The function shall copy the method name from Proton application properties and set IotHubTransportMessage method name with it.]
    @Test
    public void convertFromProtonApplicationPropertiesMethodName(
            @Mocked final Map<String, String> mockMapStringString,
            @Mocked final ApplicationProperties mockApplicationProperties,
            @Mocked final Map.Entry<String, String> mockStringStringEntry,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    )
    {
        //arrange
        String deviceId = "deviceId";
        final byte[] bytes = new byte[] {1, 2};
        final Object messageContext = "context";
        final String propertyKey = "IoThub-methodname";
        final String propertyValue = "testPropertyValue";

        AmqpsMessage amqpsMessage = new AmqpsMessage();
        Binary binary = new Binary(bytes);
        Section section = new Data(binary);
        amqpsMessage.setBody(section);
        amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_METHODS);
        amqpsMessage.setMessageAnnotations(null);
        amqpsMessage.setProperties(null);
        amqpsMessage.setApplicationProperties(mockApplicationProperties);

        new NonStrictExpectations()
        {
            {
                mockApplicationProperties.getValue();
                result = mockMapStringString;
                mockMapStringString.entrySet();
                result = mockStringStringEntry;
                mockStringStringEntry.getKey();
                result = propertyKey;
                mockStringStringEntry.getValue();
                result = propertyValue;

                mockDeviceClientConfig.getDeviceMethodsMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceMethodsMessageContext();
                result = messageContext;

                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceMethods, "openLinks", mockSession);

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceMethods, "convertFromProton", amqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        assertNotNull(actualMessage);
        assertEquals(null, ((IotHubTransportMessage)actualMessage).getProperty(propertyKey));
        assertEquals(propertyValue, ((IotHubTransportMessage)actualMessage).getMethodName());

        assertEquals(bytes.length, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Codes_SRS_AMQPSDEVICEMETHODS_12_029: [The function shall return null if the message type is not DEVICE_METHODS.]
    @Test
    public void convertToProtonReturnsNull(
            @Mocked final IotHubTransportMessage mockIotHubTransportMessage
    )
    {
        //arrange
        String deviceId = "deviceId";

        new NonStrictExpectations()
        {
            {
                mockIotHubTransportMessage.getMessageType();
                result = MessageType.DEVICE_TWIN;

                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";               }
        };

        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceMethods, "openLinks", mockSession);

        //act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsDeviceMethods, "convertToProton", mockIotHubTransportMessage);

        //assert
        assertNull(amqpsConvertToProtonReturnValue);
    }

    // Codes_SRS_AMQPSDEVICEMETHODS_12_030: [The function shall convert the IoTHubTransportMessage to a proton message.]
    // Codes_SRS_AMQPSDEVICEMETHODS_12_041: [The function shall create a AmqpsConvertToProtonReturnValue and set the message field to the new proton message.]
    // Codes_SRS_AMQPSDEVICEMETHODS_12_042: [The function shall create a AmqpsConvertToProtonReturnValue and set the message type to DEVICE_METHODS.]
    @Test
    public void convertToProtonReturnsProtonMessage(
            @Mocked final IotHubTransportMessage mockIotHubTransportMessage
    )
    {
        //arrange
        String deviceId = "deviceId";

        new NonStrictExpectations()
        {
            {
                mockIotHubTransportMessage.getMessageType();
                result = MessageType.DEVICE_METHODS;

                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";               }
        };

        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceMethods, "openLinks", mockSession);

        //act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsDeviceMethods, "convertToProton", mockIotHubTransportMessage);

        //assert
        assertNotNull(amqpsConvertToProtonReturnValue);
        assertNotNull(Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageImpl"));
        assertEquals(MessageType.DEVICE_METHODS, Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageType"));
    }

    @Test
    public void convertToProtonSetsProperties(
            @Mocked final Properties mockProperties,
            @Mocked final IotHubTransportMessage mockIotHubTransportMessage,
            @Mocked final MessageImpl mockMessageImpl,
            @Mocked final UUID mockUUID
    )
    {
        //arrange
        String deviceId = "deviceId";
        final String messageId = "messageId";
        final String correlationId = "correlationId";

        new NonStrictExpectations()
        {
            {
                new Properties();
                result = mockProperties;
                mockIotHubTransportMessage.getMessageType();
                result = MessageType.DEVICE_METHODS;
                mockIotHubTransportMessage.getMessageId();
                times = 2;
                result = messageId;
                mockIotHubTransportMessage.getRequestId();
                times = 2;
                result = correlationId;

                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";               }
        };

        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceMethods, "openLinks", mockSession);

        //act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsDeviceMethods, "convertToProton", mockIotHubTransportMessage);

        //assert
        new Verifications()
        {
            {
                mockProperties.setMessageId(any);
                times = 1;
                mockProperties.setCorrelationId(any);
                times = 1;
                mockMessageImpl.setProperties(mockProperties);
                times = 1;
            }
        };

        assertNotNull(amqpsConvertToProtonReturnValue);
        assertNotNull(Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageImpl"));
        assertEquals(MessageType.DEVICE_METHODS, Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageType"));
    }

    // Codes_SRS_AMQPSDEVICEMETHODS_12_032: [The function shall copy the user properties to Proton message application properties excluding the reserved property names.]
    @Test
    public void convertToProtonSetsUserProperties(
            @Mocked final Properties mockProtonProperties,
            @Mocked final MessageProperty mockMessageProperty,
            @Mocked final IotHubTransportMessage mockIotHubTransportMessage,
            @Mocked final MessageImpl mockMessageImpl,
            @Mocked final ApplicationProperties mockApplicationProperties
    )
    {
        //arrange
        String deviceId = "deviceId";
        final String messageId = "messageId";
        final String correlationId = "correlationId";
        final MessageProperty[] properties = new MessageProperty[1];
        properties[0] = mockMessageProperty;
        final String propertyKey = "testPropertyKey";
        final String propertyValue = "testPropertyValue";

        new NonStrictExpectations()
        {
            {
                new Properties();
                result = null;
                mockIotHubTransportMessage.getMessageType();
                result = MessageType.DEVICE_METHODS;
                mockIotHubTransportMessage.getMessageId();
                result = null;
                mockIotHubTransportMessage.getCorrelationId();
                result = null;

                mockIotHubTransportMessage.getProperties();
                times = 2;
                result = properties;
                mockMessageProperty.getName();
                times = 2;
                result = propertyKey;
                mockMessageProperty.getValue();
                result = propertyValue;

                new ApplicationProperties((Map) any);
                result = mockApplicationProperties;

                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";            }
        };

        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceMethods, "openLinks", mockSession);

        //act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsDeviceMethods, "convertToProton", mockIotHubTransportMessage);

        //assert
        new Verifications()
        {
            {
                mockMessageImpl.setApplicationProperties(mockApplicationProperties);
                times = 1;
            }
        };

        assertNotNull(amqpsConvertToProtonReturnValue);
        assertNotNull(Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageImpl"));
        assertEquals(MessageType.DEVICE_METHODS, Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageType"));
    }

    // Codes_SRS_AMQPSDEVICEMETHODS_12_033: [The function shall set the proton message status field to the value of IoTHubTransportMessage status field.]
    @Test
    public void convertToProtonSetsStatus(
            @Mocked final Properties mockProtonProperties,
            @Mocked final MessageProperty mockMessageProperty,
            @Mocked final IotHubTransportMessage mockIotHubTransportMessage,
            @Mocked final MessageImpl mockMessageImpl,
            @Mocked final ApplicationProperties mockApplicationProperties
    )
    {
        //arrange
        String deviceId = "deviceId";
        final String messageId = "messageId";
        final String correlationId = "correlationId";
        final MessageProperty[] properties = new MessageProperty[1];
        properties[0] = mockMessageProperty;
        final String propertyKey = "testPropertyKey";
        final int statusValue = 404;

        new NonStrictExpectations()
        {
            {
                new Properties();
                result = null;
                mockIotHubTransportMessage.getMessageType();
                result = MessageType.DEVICE_METHODS;
                mockIotHubTransportMessage.getMessageId();
                result = null;
                mockIotHubTransportMessage.getCorrelationId();
                result = null;

                mockIotHubTransportMessage.getProperties();
                times = 2;
                result = properties;
                mockMessageProperty.getName();
                times = 2;
                result = propertyKey;

                mockIotHubTransportMessage.getStatus();
                times = 2;
                result = statusValue;

                mockIotHubTransportMessage.getCorrelationId();
                result = null;

                new ApplicationProperties((Map) any);
                result = mockApplicationProperties;

                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsDeviceMethods amqpsDeviceMethods = Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceMethods, "openLinks", mockSession);

        //act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsDeviceMethods, "convertToProton", mockIotHubTransportMessage);

        //assert
        new Verifications()
        {
            {
                mockMessageImpl.setApplicationProperties(mockApplicationProperties);
                times = 1;
            }
        };

        assertNotNull(amqpsConvertToProtonReturnValue);
        assertNotNull(Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageImpl"));
        assertEquals(MessageType.DEVICE_METHODS, Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageType"));
    }
}
