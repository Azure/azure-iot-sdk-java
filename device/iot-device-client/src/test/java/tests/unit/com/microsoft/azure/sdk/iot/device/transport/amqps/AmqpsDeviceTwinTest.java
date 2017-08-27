/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.impl.MessageImpl;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.*;
import static org.junit.Assert.*;

/* Unit tests for AmqpsDeviceTwin
* 100% methods covered
* 100% lines covered
*/
public class AmqpsDeviceTwinTest
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

    // Tests_SRS_AMQPSDEVICETWIN_12_001: [The constructor shall throw IllegalArgumentException if the deviceId argument is null or empty.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfDeviceIdNull()
    {
        //act
        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_001: [The constructor shall throw IllegalArgumentException if the deviceId argument is null or empty.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfDeviceIdEmpty()
    {
        //act
        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, "");
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_002: [The constructor shall set the sender and receiver endpoint path to IoTHub specific values.]
    // Tests_SRS_AMQPSDEVICETWIN_12_003: [The constructor shall concatenate a sender specific prefix to the sender link tag's current value.]
    // Tests_SRS_AMQPSDEVICETWIN_12_004: [The constructor shall concatenate a receiver specific prefix to the receiver link tag's current value.]
    // Tests_SRS_AMQPSDEVICETWIN_12_005: [The constructor shall insert the given deviceId argument to the sender and receiver link address.]
    // Tests_SRS_AMQPSDEVICETWIN_12_006: [The constructor shall add the API version key to the amqpProperties.]
    // Tests_SRS_AMQPSDEVICETWIN_12_007: [The constructor shall generate a UUID amd add it as a correlation ID to the amqpProperties.]
    // Tests_SRS_AMQPSDEVICETWIN_12_009: [The constructor shall create a HashMap for correlationId list.]
    @Test
    public void constructorInitializesAllMembers()
    {
        //arrange
        String deviceId = "deviceId";

        //act
        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        String API_VERSION_KEY = Deencapsulation.getField(amqpsDeviceTwin, "API_VERSION_KEY");
        String CORRELATION_ID_KEY = Deencapsulation.getField(amqpsDeviceTwin, "CORRELATION_ID_KEY");
        String SENDER_LINK_ENDPOINT_PATH = Deencapsulation.getField(amqpsDeviceTwin, "SENDER_LINK_ENDPOINT_PATH");
        String RECEIVER_LINK_ENDPOINT_PATH = Deencapsulation.getField(amqpsDeviceTwin, "RECEIVER_LINK_ENDPOINT_PATH");
        String SENDER_LINK_TAG_PREFIX = Deencapsulation.getField(amqpsDeviceTwin, "SENDER_LINK_TAG_PREFIX");
        String RECEIVER_LINK_TAG_PREFIX = Deencapsulation.getField(amqpsDeviceTwin, "RECEIVER_LINK_TAG_PREFIX");
        String senderLinkEndpointPath = Deencapsulation.getField(amqpsDeviceTwin, "senderLinkEndpointPath");
        String receiverLinkEndpointPath = Deencapsulation.getField(amqpsDeviceTwin, "receiverLinkEndpointPath");

        Map<Symbol, Object> amqpsProperties = Deencapsulation.invoke(amqpsDeviceTwin, "getAmqpProperties");
        String senderLinkTag = Deencapsulation.invoke(amqpsDeviceTwin, "getSenderLinkTag");
        String receiverLinkTag = Deencapsulation.invoke(amqpsDeviceTwin, "getReceiverLinkTag");
        String senderLinkAddress = Deencapsulation.invoke(amqpsDeviceTwin, "getSenderLinkAddress");
        String receiverLinkAddress = Deencapsulation.invoke(amqpsDeviceTwin, "getReceiverLinkAddress");

        //assert
        assertNotNull(amqpsDeviceTwin);

        assertTrue(SENDER_LINK_ENDPOINT_PATH.equals(senderLinkEndpointPath));
        assertTrue(RECEIVER_LINK_ENDPOINT_PATH.equals(receiverLinkEndpointPath));

        assertTrue(senderLinkTag.startsWith(SENDER_LINK_TAG_PREFIX));
        assertTrue(receiverLinkTag.startsWith(RECEIVER_LINK_TAG_PREFIX));

        assertTrue(senderLinkAddress.contains(deviceId));
        assertTrue(receiverLinkAddress.contains(deviceId));

        assertTrue(amqpsProperties.containsKey(Symbol.getSymbol(API_VERSION_KEY)));
        assertTrue(amqpsProperties.containsKey(Symbol.getSymbol(CORRELATION_ID_KEY)));

        Map<String, DeviceOperations> correlationIdList = Deencapsulation.getField(amqpsDeviceTwin, "correlationIdList");
        assertNotNull(correlationIdList);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_008: [The class has static members for AMQP annotation fields containing IoTHub specific values.]
    @Test
    public void classHasAnnotationFieldsValues()
    {
        //arrange
        String deviceId = "deviceId";

        //act
        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        String MESSAGE_ANNOTATION_FIELD_KEY_OPERATION = Deencapsulation.getField(amqpsDeviceTwin, "MESSAGE_ANNOTATION_FIELD_KEY_OPERATION");
        String MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE = Deencapsulation.getField(amqpsDeviceTwin, "MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE");
        String MESSAGE_ANNOTATION_FIELD_VALUE_GET = Deencapsulation.getField(amqpsDeviceTwin, "MESSAGE_ANNOTATION_FIELD_VALUE_GET");
        String MESSAGE_ANNOTATION_FIELD_VALUE_PATCH = Deencapsulation.getField(amqpsDeviceTwin, "MESSAGE_ANNOTATION_FIELD_VALUE_PATCH");
        String MESSAGE_ANNOTATION_FIELD_VALUE_PUT = Deencapsulation.getField(amqpsDeviceTwin, "MESSAGE_ANNOTATION_FIELD_VALUE_PUT");
        String MESSAGE_ANNOTATION_FIELD_VALUE_DELETE = Deencapsulation.getField(amqpsDeviceTwin, "MESSAGE_ANNOTATION_FIELD_VALUE_DELETE");
        String MESSAGE_ANNOTATION_FIELD_VALUE_PROPERTIES_REPORTED = Deencapsulation.getField(amqpsDeviceTwin, "MESSAGE_ANNOTATION_FIELD_VALUE_PROPERTIES_REPORTED");
        String MESSAGE_ANNOTATION_FIELD_VALUE_NOTIFICATIONS_TWIN_PROPERTIES_DESIRED = Deencapsulation.getField(amqpsDeviceTwin, "MESSAGE_ANNOTATION_FIELD_VALUE_NOTIFICATIONS_TWIN_PROPERTIES_DESIRED");

        //assert
        assertTrue(MESSAGE_ANNOTATION_FIELD_KEY_OPERATION.equals("operation"));
        assertTrue(MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE.equals("resource"));

        assertTrue(MESSAGE_ANNOTATION_FIELD_VALUE_GET.equals("GET"));
        assertTrue(MESSAGE_ANNOTATION_FIELD_VALUE_PATCH.equals("PATCH"));
        assertTrue(MESSAGE_ANNOTATION_FIELD_VALUE_PUT.equals("PUT"));
        assertTrue(MESSAGE_ANNOTATION_FIELD_VALUE_DELETE.equals("DELETE"));

        assertTrue(MESSAGE_ANNOTATION_FIELD_VALUE_PROPERTIES_REPORTED.equals("/properties/reported"));
        assertTrue(MESSAGE_ANNOTATION_FIELD_VALUE_NOTIFICATIONS_TWIN_PROPERTIES_DESIRED.equals("/notifications/twin/properties/desired"));
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_010: [The function shall call the super function if the MessageType is DEVICE_TWIN, and return with it's return value.]
    @Test
    public void sendMessageAndGetDeliveryHashCallsSuper() throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        byte[] bytes = new byte[1];

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        //act


        AmqpsSendReturnValue amqpsSendReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "sendMessageAndGetDeliveryHash", MessageType.DEVICE_TWIN, bytes, 0, 1, bytes);

        //assert
        assertEquals(true, amqpsSendReturnValue.isDeliverySuccessful());
        assertNotEquals(amqpsSendReturnValue.getDeliveryHash(), -1);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_011: [The function shall return with AmqpsSendReturnValue with false success and -1 delivery hash.]
    @Test
    public void sendMessageAndGetDeliveryHashReturnsFalse() throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        byte[] bytes = new byte[1];

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        //act
        AmqpsSendReturnValue amqpsSendReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "sendMessageAndGetDeliveryHash", MessageType.DEVICE_METHODS, bytes, 0, 1, bytes);

        //assert
        assertEquals(false, amqpsSendReturnValue.isDeliverySuccessful());
        assertEquals(-1, amqpsSendReturnValue.getDeliveryHash());
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_012: [The function shall call the super function.]
    // Tests_SRS_AMQPSDEVICETWIN_12_013: [The function shall set the MessageType to DEVICE_TWIN if the super function returned not null.]
    // Tests_SRS_AMQPSDEVICETWIN_12_014: [The function shall return the super function return value.]
    @Test
    public void getMessageFromReceiverLinkSuccess() throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        String linkName = "receiver";
        byte[] bytes = new byte[1];

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);
        Deencapsulation.setField(amqpsDeviceTwin, "receiverLink", mockReceiver);
        Deencapsulation.setField(amqpsDeviceTwin, "senderLink", mockSender);
        Deencapsulation.setField(amqpsDeviceTwin, "receiverLinkTag", linkName);

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
        AmqpsMessage amqpsMessage = Deencapsulation.invoke(amqpsDeviceTwin, "getMessageFromReceiverLink", linkName);

        //assert
        assertNotNull(amqpsMessage);
        assertEquals(MessageType.DEVICE_TWIN, amqpsMessage.getAmqpsMessageType());
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

    // Tests_SRS_AMQPSDEVICETWIN_12_014: [The function shall return the super function return value.]
    @Test
    public void getMessageFromReceiverLinkSuperFailed() throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        String linkName = "receiver";
        byte[] bytes = new byte[1];

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);
        Deencapsulation.setField(amqpsDeviceTwin, "receiverLink", mockReceiver);
        Deencapsulation.setField(amqpsDeviceTwin, "senderLink", mockSender);
        Deencapsulation.setField(amqpsDeviceTwin, "receiverLinkTag", linkName);

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
        AmqpsMessage amqpsMessage = Deencapsulation.invoke(amqpsDeviceTwin, "getMessageFromReceiverLink", linkName);

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

    // Tests_SRS_AMQPSDEVICETWIN_12_015: [The function shall return null if the message type is null or not DEVICE_TWIN.]
    @Test
    public void convertFromProtonReturnsNull(
            @Mocked final AmqpsMessage mockAmqpsMessage,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        byte[] bytes = new byte[1];

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                mockAmqpsMessage.getAmqpsMessageType();
                result = MessageType.DEVICE_METHODS;
            }
        };

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);

        //assert
        assertNull(amqpsConvertFromProtonReturnValue);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_016: [The function shall convert the amqpsMessage to IoTHubTransportMessage.]
    // Tests_SRS_AMQPSDEVICETWIN_12_017: [The function shall create a new empty buffer for message body if the proton message body is null.]
    // Tests_SRS_AMQPSDEVICETWIN_12_027: [The function shall create a AmqpsConvertFromProtonReturnValue and set the message field to the new IotHubTransportMessage.]
    // Tests_SRS_AMQPSDEVICETWIN_12_028: [The function shall create a AmqpsConvertFromProtonReturnValue and copy the DeviceClientConfig callback and context to it.]
    @Test
    public void convertFromProtonEmptyBodySuccess(
            @Mocked final AmqpsMessage mockAmqpsMessage,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        byte[] bytes = new byte[1];
        final Object messageContext = "context";

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                mockAmqpsMessage.getAmqpsMessageType();
                result = MessageType.DEVICE_TWIN;
                mockAmqpsMessage.getMessageAnnotations();
                result = null;
                mockAmqpsMessage.getProperties();
                result = null;
                mockDeviceClientConfig.getDeviceTwinMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceTwinMessageContext();
                result = messageContext;
                mockAmqpsMessage.getBody();
                result = null;
            }
        };

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        assertNotNull(actualMessage);
        assertEquals(0, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_020: [The function shall read the proton message annotations and set the status to the value of STATUS key.]
    @Test
    public void convertFromProtonSuccessWithStatusAnnotation(
            @Mocked final Symbol mockSymbol,
            @Mocked final Map<Symbol, Object> mockMapSymbolObject,
            @Mocked final MessageAnnotations mockMessageAnnotations,
            @Mocked final Map.Entry<Symbol, Object> mockSymbolObjectEntry,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        final String status = "404";
        final byte[] bytes = new byte[] {1, 2};
        final Object messageContext = "context";

        AmqpsMessage amqpsMessage = new AmqpsMessage();
        Binary binary = new Binary(bytes);
        Section section = new Data(binary);
        amqpsMessage.setBody(section);
        amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_TWIN);
        amqpsMessage.setMessageAnnotations(mockMessageAnnotations);
        amqpsMessage.setProperties(null);

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        final String MESSAGE_ANNOTATION_FIELD_KEY_STATUS = Deencapsulation.getField(amqpsDeviceTwin, "MESSAGE_ANNOTATION_FIELD_KEY_STATUS");
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                mockMessageAnnotations.getValue();
                result = mockMapSymbolObject;
                mockMapSymbolObject.entrySet();
                result = mockSymbolObjectEntry;

                mockSymbolObjectEntry.getKey();
                result = mockSymbol;
                mockSymbol.toString();
                result = MESSAGE_ANNOTATION_FIELD_KEY_STATUS;

                mockSymbolObjectEntry.getValue();
                result = status;

                mockDeviceClientConfig.getDeviceTwinMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceTwinMessageContext();
                result = messageContext;
            }
        };

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertFromProton", amqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");
        //assert
        assertNotNull(actualMessage);
        assertEquals(status, ((IotHubTransportMessage)actualMessage).getStatus());

        assertEquals(bytes.length, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_021: [The function shall read the proton message annotations and set the version to the value of VERSION key.]
    @Test
    public void convertFromProtonSuccessWithVersionAnnotation(
            @Mocked final Symbol mockSymbol,
            @Mocked final Map<Symbol, Object> mockMapSymbolObject,
            @Mocked final MessageAnnotations mockMessageAnnotations,
            @Mocked final Map.Entry<Symbol, Object> mockSymbolObjectEntry,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        final String version = "1.0.0";
        final byte[] bytes = new byte[] {1, 2};
        final Object messageContext = "context";

        AmqpsMessage amqpsMessage = new AmqpsMessage();
        Binary binary = new Binary(bytes);
        Section section = new Data(binary);
        amqpsMessage.setBody(section);
        amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_TWIN);
        amqpsMessage.setMessageAnnotations(mockMessageAnnotations);
        amqpsMessage.setProperties(null);

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        final String MESSAGE_ANNOTATION_FIELD_KEY_VERSION = Deencapsulation.getField(amqpsDeviceTwin, "MESSAGE_ANNOTATION_FIELD_KEY_VERSION");
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                mockMessageAnnotations.getValue();
                result = mockMapSymbolObject;
                mockMapSymbolObject.entrySet();
                result = mockSymbolObjectEntry;

                mockSymbolObjectEntry.getKey();
                result = mockSymbol;
                mockSymbol.toString();
                result = MESSAGE_ANNOTATION_FIELD_KEY_VERSION;

                mockSymbolObjectEntry.getValue();
                result = version;

                mockDeviceClientConfig.getDeviceTwinMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceTwinMessageContext();
                result = messageContext;
            }
        };

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertFromProton", amqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        assertNotNull(actualMessage);
        assertEquals(version, ((IotHubTransportMessage)actualMessage).getVersion());

        assertEquals(bytes.length, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_022: [The function shall read the proton message annotations and set the operation type to SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE if the PROPERTIES_DESIRED resource exist.]
    @Test
    public void convertFromProtonSuccessWithResourceAnnotation(
            @Mocked final Symbol mockSymbol,
            @Mocked final Map<Symbol, Object> mockMapSymbolObject,
            @Mocked final MessageAnnotations mockMessageAnnotations,
            @Mocked final Map.Entry<Symbol, Object> mockSymbolObjectEntry,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        final byte[] bytes = new byte[] {1, 2};
        final Object messageContext = "context";

        AmqpsMessage amqpsMessage = new AmqpsMessage();
        Binary binary = new Binary(bytes);
        Section section = new Data(binary);
        amqpsMessage.setBody(section);
        amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_TWIN);
        amqpsMessage.setMessageAnnotations(mockMessageAnnotations);
        amqpsMessage.setProperties(null);

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        final String MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE = Deencapsulation.getField(amqpsDeviceTwin, "MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE");
        final String MESSAGE_ANNOTATION_FIELD_VALUE_PROPERTIES_DESIRED = Deencapsulation.getField(amqpsDeviceTwin, "MESSAGE_ANNOTATION_FIELD_VALUE_PROPERTIES_DESIRED");
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                mockMessageAnnotations.getValue();
                result = mockMapSymbolObject;
                mockMapSymbolObject.entrySet();
                result = mockSymbolObjectEntry;

                mockSymbolObjectEntry.getKey();
                result = mockSymbol;
                mockSymbol.toString();
                result = MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE;

                mockSymbolObjectEntry.getValue();
                result = MESSAGE_ANNOTATION_FIELD_VALUE_PROPERTIES_DESIRED;

                mockDeviceClientConfig.getDeviceTwinMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceTwinMessageContext();
                result = messageContext;
            }
        };

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertFromProton", amqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");


        //assert
        assertNotNull(actualMessage);
        assertEquals(DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE, ((IotHubTransportMessage)actualMessage).getDeviceOperationType());

        assertEquals(bytes.length, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_024: [THe function shall set the operation type to SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE if the proton correlation ID is null.]
    @Test
    public void convertFromProtonPropertiesCorrelationIdNull(
            @Mocked final Properties mockProperties,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        final byte[] bytes = new byte[] {1, 2};
        final Object messageContext = "context";

        AmqpsMessage amqpsMessage = new AmqpsMessage();
        Binary binary = new Binary(bytes);
        Section section = new Data(binary);
        amqpsMessage.setBody(section);
        amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_TWIN);
        amqpsMessage.setMessageAnnotations(null);
        amqpsMessage.setProperties(mockProperties);

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                mockProperties.getCorrelationId();
                result = null;
                mockProperties.getMessageId();
                result = null;
                mockProperties.getTo();
                result = null;
                mockProperties.getUserId();
                result = null;

                mockDeviceClientConfig.getDeviceTwinMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceTwinMessageContext();
                result = messageContext;
            }
        };

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertFromProton", amqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        assertNotNull(actualMessage);
        assertEquals(DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE, ((IotHubTransportMessage)actualMessage).getDeviceOperationType());

        assertEquals(bytes.length, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_044: [The function shall set the IotHubTransportMessage correlationID to the proton correlationId.]
    @Test
    public void convertFromProtonPropertiesCorrelationIdSet(
            @Mocked final Properties mockProperties,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        final String correlationId = "correlationId";
        final byte[] bytes = new byte[] {1, 2};
        final Object messageContext = "context";

        AmqpsMessage amqpsMessage = new AmqpsMessage();
        Binary binary = new Binary(bytes);
        Section section = new Data(binary);
        amqpsMessage.setBody(section);
        amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_TWIN);
        amqpsMessage.setMessageAnnotations(null);
        amqpsMessage.setProperties(mockProperties);

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                mockProperties.getCorrelationId();
                result = correlationId;
                mockProperties.getMessageId();
                result = null;
                mockProperties.getTo();
                result = null;
                mockProperties.getUserId();
                result = null;

                mockDeviceClientConfig.getDeviceTwinMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceTwinMessageContext();
                result = messageContext;
            }
        };

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertFromProton", amqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        assertNotNull(actualMessage);
        assertEquals(correlationId, (actualMessage).getCorrelationId());

        assertEquals(bytes.length, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_023: [The function shall find the proton correlation ID in the correlationIdList and if it is found, set the operation type to the related response.]
    @Test
    public void convertFromProtonPropertiesCorrelationIdGetRequest(
            @Mocked final Properties mockProperties,
            @Mocked final Map<String, DeviceOperations> mockCorrelationIdList,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        final String correlationId = "correlationId";
        final byte[] bytes = new byte[] {1, 2};
        final Object messageContext = "context";

        AmqpsMessage amqpsMessage = new AmqpsMessage();
        Binary binary = new Binary(bytes);
        Section section = new Data(binary);
        amqpsMessage.setBody(section);
        amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_TWIN);
        amqpsMessage.setMessageAnnotations(null);
        amqpsMessage.setProperties(mockProperties);

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.setField(amqpsDeviceTwin, "correlationIdList", mockCorrelationIdList);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                mockProperties.getCorrelationId();
                result = correlationId;

                mockCorrelationIdList.containsKey(correlationId.toString());
                result = true;
                mockCorrelationIdList.get(correlationId.toString());
                result = DEVICE_OPERATION_TWIN_GET_REQUEST;

                mockProperties.getMessageId();
                result = null;
                mockProperties.getTo();
                result = null;
                mockProperties.getUserId();
                result = null;

                mockDeviceClientConfig.getDeviceTwinMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceTwinMessageContext();
                result = messageContext;
            }
        };

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertFromProton", amqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        assertNotNull(actualMessage);
        assertEquals(correlationId, (actualMessage).getCorrelationId());
        assertEquals(DEVICE_OPERATION_TWIN_GET_RESPONSE, ((IotHubTransportMessage)actualMessage).getDeviceOperationType());

        assertEquals(bytes.length, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_023: [The function shall find the proton correlation ID in the correlationIdList and if it is found, set the operation type to the related response.]
    @Test
    public void convertFromProtonPropertiesCorrelationIdUpdateReportedPropertiesRequest(
            @Mocked final Properties mockProperties,
            @Mocked final Map<String, DeviceOperations> mockCorrelationIdList,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        final String correlationId = "correlationId";
        final byte[] bytes = new byte[] {1, 2};
        final Object messageContext = "context";

        AmqpsMessage amqpsMessage = new AmqpsMessage();
        Binary binary = new Binary(bytes);
        Section section = new Data(binary);
        amqpsMessage.setBody(section);
        amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_TWIN);
        amqpsMessage.setMessageAnnotations(null);
        amqpsMessage.setProperties(mockProperties);

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.setField(amqpsDeviceTwin, "correlationIdList", mockCorrelationIdList);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                mockProperties.getCorrelationId();
                result = correlationId;

                mockCorrelationIdList.containsKey(correlationId.toString());
                result = true;
                mockCorrelationIdList.get(correlationId.toString());
                result = DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST;

                mockProperties.getMessageId();
                result = null;
                mockProperties.getTo();
                result = null;
                mockProperties.getUserId();
                result = null;

                mockDeviceClientConfig.getDeviceTwinMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceTwinMessageContext();
                result = messageContext;
            }
        };

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertFromProton", amqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        assertNotNull(actualMessage);
        assertEquals(correlationId, (actualMessage).getCorrelationId());
        assertEquals(DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE, ((IotHubTransportMessage)actualMessage).getDeviceOperationType());

        assertEquals(bytes.length, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_023: [The function shall find the proton correlation ID in the correlationIdList and if it is found, set the operation type to the related response.]
    @Test
    public void convertFromProtonPropertiesCorrelationIdSubscribeDesiredPropertiesRequest(
            @Mocked final Properties mockProperties,
            @Mocked final Map<String, DeviceOperations> mockCorrelationIdList,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        final String correlationId = "correlationId";
        final byte[] bytes = new byte[] {1, 2};
        final Object messageContext = "context";

        AmqpsMessage amqpsMessage = new AmqpsMessage();
        Binary binary = new Binary(bytes);
        Section section = new Data(binary);
        amqpsMessage.setBody(section);
        amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_TWIN);
        amqpsMessage.setMessageAnnotations(null);
        amqpsMessage.setProperties(mockProperties);

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.setField(amqpsDeviceTwin, "correlationIdList", mockCorrelationIdList);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                mockProperties.getCorrelationId();
                result = correlationId;

                mockCorrelationIdList.containsKey(correlationId.toString());
                result = true;
                mockCorrelationIdList.get(correlationId.toString());
                result = DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST;

                mockProperties.getMessageId();
                result = null;
                mockProperties.getTo();
                result = null;
                mockProperties.getUserId();
                result = null;

                mockDeviceClientConfig.getDeviceTwinMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceTwinMessageContext();
                result = messageContext;
            }
        };

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertFromProton", amqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        assertNotNull(actualMessage);
        assertEquals(correlationId, (actualMessage).getCorrelationId());
        assertEquals(DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE, ((IotHubTransportMessage)actualMessage).getDeviceOperationType());

        assertEquals(bytes.length, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_023: [The function shall find the proton correlation ID in the correlationIdList and if it is found, set the operation type to the related response.]
    @Test
    public void convertFromProtonPropertiesCorrelationIdUnSubscribeDesiredPropertiesRequest(
            @Mocked final Properties mockProperties,
            @Mocked final Map<String, DeviceOperations> mockCorrelationIdList,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        final String correlationId = "correlationId";
        final byte[] bytes = new byte[] {1, 2};
        final Object messageContext = "context";

        AmqpsMessage amqpsMessage = new AmqpsMessage();
        Binary binary = new Binary(bytes);
        Section section = new Data(binary);
        amqpsMessage.setBody(section);
        amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_TWIN);
        amqpsMessage.setMessageAnnotations(null);
        amqpsMessage.setProperties(mockProperties);

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.setField(amqpsDeviceTwin, "correlationIdList", mockCorrelationIdList);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                mockProperties.getCorrelationId();
                result = correlationId;

                mockCorrelationIdList.containsKey(correlationId.toString());
                result = true;
                mockCorrelationIdList.get(correlationId.toString());
                result = DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST;

                mockProperties.getMessageId();
                result = null;
                mockProperties.getTo();
                result = null;
                mockProperties.getUserId();
                result = null;

                mockDeviceClientConfig.getDeviceTwinMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceTwinMessageContext();
                result = messageContext;
            }
        };

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertFromProton", amqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        assertNotNull(actualMessage);
        assertEquals(correlationId, (actualMessage).getCorrelationId());
        assertEquals(DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_RESPONSE, ((IotHubTransportMessage)actualMessage).getDeviceOperationType());

        assertEquals(bytes.length, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_043: [The function shall remove the correlation from the correlationId list.]
    @Test
    public void convertFromProtonPropertiesCorrelationIdRemoves(
            @Mocked final Properties mockProperties,
            @Mocked final Map<String, DeviceOperations> mockCorrelationIdList,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        final String correlationId = "correlationId";
        final byte[] bytes = new byte[] {1, 2};
        final Object messageContext = "context";

        AmqpsMessage amqpsMessage = new AmqpsMessage();
        Binary binary = new Binary(bytes);
        Section section = new Data(binary);
        amqpsMessage.setBody(section);
        amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_TWIN);
        amqpsMessage.setMessageAnnotations(null);
        amqpsMessage.setProperties(mockProperties);

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.setField(amqpsDeviceTwin, "correlationIdList", mockCorrelationIdList);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                mockProperties.getCorrelationId();
                result = correlationId;

                mockCorrelationIdList.containsKey(correlationId.toString());
                result = true;
                mockCorrelationIdList.get(correlationId.toString());
                result = DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST;

                mockProperties.getMessageId();
                result = null;
                mockProperties.getTo();
                result = null;
                mockProperties.getUserId();
                result = null;

                mockDeviceClientConfig.getDeviceTwinMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceTwinMessageContext();
                result = messageContext;
            }
        };

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertFromProton", amqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        new Verifications()
        {
            {
                mockCorrelationIdList.remove(correlationId.toString());
                times = 1;
            }
        };

        assertNotNull(actualMessage);
        assertEquals(correlationId, (actualMessage).getCorrelationId());
        assertEquals(DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_RESPONSE, ((IotHubTransportMessage)actualMessage).getDeviceOperationType());

        assertEquals(bytes.length, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_025: [The function shall copy the correlationId, messageId, To and userId properties to the IotHubTransportMessage properties.]
    @Test
    public void convertFromProtonPropertiesSet(
            @Mocked final Properties mockProperties,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        final String messageId = "messageId";
        final String to = "to";
        final byte[] bytes = new byte[] {1, 2};
        final Object messageContext = "context";

        AmqpsMessage amqpsMessage = new AmqpsMessage();
        final Binary userId = new Binary(bytes);
        Section section = new Data(userId);
        amqpsMessage.setBody(section);
        amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_TWIN);
        amqpsMessage.setMessageAnnotations(null);
        amqpsMessage.setProperties(mockProperties);
        amqpsMessage.setApplicationProperties(null);

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        final String AMQPS_APP_PROPERTY_PREFIX = Deencapsulation.getField(amqpsDeviceTwin, "AMQPS_APP_PROPERTY_PREFIX");
        final String TO_KEY = Deencapsulation.getField(amqpsDeviceTwin, "TO_KEY");
        final String USER_ID_KEY = Deencapsulation.getField(amqpsDeviceTwin, "USER_ID_KEY");
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                mockProperties.getCorrelationId();
                result = null;
                mockProperties.getMessageId();
                result = messageId;
                mockProperties.getTo();
                result = to;
                mockProperties.getUserId();
                result = userId;

                mockDeviceClientConfig.getDeviceTwinMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceTwinMessageContext();
                result = messageContext;
            }
        };

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertFromProton", amqpsMessage, mockDeviceClientConfig);
        Message actualMessage = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "message");
        MessageCallback actualMessageCallback = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageCallback");
        Object actualMessageContext = Deencapsulation.getField(amqpsConvertFromProtonReturnValue, "messageContext");

        //assert
        assertNotNull(actualMessage);
        assertEquals(messageId, ((IotHubTransportMessage)actualMessage).getMessageId());
        assertEquals(to, ((IotHubTransportMessage)actualMessage).getProperty(AMQPS_APP_PROPERTY_PREFIX + TO_KEY));
        assertEquals(userId.toString(), ((IotHubTransportMessage)actualMessage).getProperty(AMQPS_APP_PROPERTY_PREFIX + USER_ID_KEY));

        assertEquals(bytes.length, actualMessage.getBytes().length);
        assertEquals(mockMessageCallback, actualMessageCallback);
        assertEquals(messageContext, actualMessageContext);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_026: [The function shall copy the Proton application properties to IotHubTransportMessage properties excluding the reserved property names.]
    @Test
    public void convertFromProtonApplicationPropertiesReservedNotSet(
            @Mocked final Map<String, String> mockMapStringString,
            @Mocked final ApplicationProperties mockApplicationProperties,
            @Mocked final Map.Entry<String, String> mockStringStringEntry,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    ) throws IOException
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
        amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_TWIN);
        amqpsMessage.setMessageAnnotations(null);
        amqpsMessage.setProperties(null);
        amqpsMessage.setApplicationProperties(mockApplicationProperties);

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

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

                mockDeviceClientConfig.getDeviceTwinMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceTwinMessageContext();
                result = messageContext;
            }
        };

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertFromProton", amqpsMessage, mockDeviceClientConfig);
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

    // Tests_SRS_AMQPSDEVICETWIN_12_026: [The function shall copy the Proton application properties to IotHubTransportMessage properties excluding the reserved property names.]
    @Test
    public void convertFromProtonApplicationPropertiesSet(
            @Mocked final Map<String, String> mockMapStringString,
            @Mocked final ApplicationProperties mockApplicationProperties,
            @Mocked final Map.Entry<String, String> mockStringStringEntry,
            @Mocked final DeviceClientConfig mockDeviceClientConfig
    ) throws IOException
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
        amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_TWIN);
        amqpsMessage.setMessageAnnotations(null);
        amqpsMessage.setProperties(null);
        amqpsMessage.setApplicationProperties(mockApplicationProperties);

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

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

                mockDeviceClientConfig.getDeviceTwinMessageCallback();
                result = mockMessageCallback;
                mockDeviceClientConfig.getDeviceTwinMessageContext();
                result = messageContext;
            }
        };

        //act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertFromProton", amqpsMessage, mockDeviceClientConfig);
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

    // Tests_SRS_AMQPSDEVICETWIN_12_029: [*The function shall return null if the message type is null or not DEVICE_TWIN.]
    @Test
    public void convertToProtonReturnsNull(
            @Mocked final IotHubTransportMessage mockIotHubTransportMessage
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                mockIotHubTransportMessage.getMessageType();
                result = MessageType.DEVICE_METHODS;
            }
        };

        //act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertToProton", mockIotHubTransportMessage);

        //assert
        assertNull(amqpsConvertToProtonReturnValue);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_030: [The function shall convert the IoTHubTransportMessage to a proton message.]
    // Tests_SRS_AMQPSDEVICETWIN_12_041: [The function shall create a AmqpsConvertToProtonReturnValue and set the message field to the new proton message.]
    // Tests_SRS_AMQPSDEVICETWIN_12_042: [The function shall create a AmqpsConvertToProtonReturnValue and set the message type to DEVICE_TWIN.]
    @Test
    public void convertToProtonReturnsProtonMessage(
            @Mocked final IotHubTransportMessage mockIotHubTransportMessage
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                mockIotHubTransportMessage.getMessageType();
                result = MessageType.DEVICE_TWIN;
            }
        };

        //act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertToProton", mockIotHubTransportMessage);
        MessageImpl actualMessageImpl = Deencapsulation.getField(amqpsConvertToProtonReturnValue, "messageImpl");
        MessageType actualMessageType = Deencapsulation.getField(amqpsConvertToProtonReturnValue, "messageType");

        //assert
        assertNotNull(amqpsConvertToProtonReturnValue);
        assertNotNull(actualMessageImpl);
        assertEquals(MessageType.DEVICE_TWIN, actualMessageType);
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_031: [The function shall copy the correlationId, messageId properties to the Proton message properties.]
    // Tests_SRS_AMQPSDEVICETWIN_12_045: [The function shall add the correlationId to the correlationIdList if it is not null.]
    @Test
    public void convertToProtonSetsProperties(
            @Mocked final Properties mockProperties,
            @Mocked final Map<String, DeviceOperations> mockCorrelationIdList,
            @Mocked final IotHubTransportMessage mockIotHubTransportMessage,
            @Mocked final MessageImpl mockMessageImpl,
            @Mocked final UUID mockUUID
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        final String messageId = "messageId";
        final String correlationId = "correlationId";

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.setField(amqpsDeviceTwin, "correlationIdList", mockCorrelationIdList);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                new Properties();
                result = mockProperties;
                mockIotHubTransportMessage.getMessageType();
                result = MessageType.DEVICE_TWIN;
                mockIotHubTransportMessage.getMessageId();
                times = 2;
                result = messageId;
                mockIotHubTransportMessage.getCorrelationId();
                times = 3;
                result = correlationId;
            }
        };

        //act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertToProton", mockIotHubTransportMessage);

        //assert
        new Verifications()
        {
            {
                mockProperties.setMessageId(any);
                times = 1;
                mockProperties.setCorrelationId(any);
                times = 1;
                mockCorrelationIdList.put(anyString, (DeviceOperations) any);
                times = 1;
                mockMessageImpl.setProperties(mockProperties);
                times = 1;
            }
        };

        assertNotNull(amqpsConvertToProtonReturnValue);
        assertNotNull(Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageImpl"));
        assertEquals(MessageType.DEVICE_TWIN, Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageType"));
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_032: [The function shall copy the user properties to Proton message application properties excluding the reserved property names.]
    @Test
    public void convertToProtonSetsUserProperties(
            @Mocked final Properties mockProtonProperties,
            @Mocked final MessageProperty mockMessageProperty,
            @Mocked final IotHubTransportMessage mockIotHubTransportMessage,
            @Mocked final MessageImpl mockMessageImpl,
            @Mocked final ApplicationProperties mockApplicationProperties
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        final String messageId = "messageId";
        final String correlationId = "correlationId";
        final MessageProperty[] properties = new MessageProperty[1];
        properties[0] = mockMessageProperty;
        final String propertyKey = "testPropertyKey";
        final String propertyValue = "testPropertyValue";

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                new Properties();
                result = null;
                mockIotHubTransportMessage.getMessageType();
                result = MessageType.DEVICE_TWIN;
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
            }
        };

        //act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertToProton", mockIotHubTransportMessage);

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
        assertEquals(MessageType.DEVICE_TWIN, Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageType"));
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_033: [The function shall set the proton message annotation operation field to GET if the IotHubTransportMessage operation type is GET_REQUEST.]
    @Test
    public void convertToProtonSetsMessageAnnotationsGetRequest(
            @Mocked final Properties mockProtonProperties,
            @Mocked final MessageProperty mockMessageProperty,
            @Mocked final IotHubTransportMessage mockIotHubTransportMessage,
            @Mocked final MessageImpl mockMessageImpl,
            @Mocked final ApplicationProperties mockApplicationProperties,
            @Mocked final Map<Symbol, Object> mockMessageAnnotationsMap,
            @Mocked final MessageAnnotations mockMessageAnnotations
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        final MessageProperty[] properties = new MessageProperty[1];
        properties[0] = mockMessageProperty;

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                new Properties();
                result = null;
                mockIotHubTransportMessage.getMessageType();
                result = MessageType.DEVICE_TWIN;
                mockIotHubTransportMessage.getMessageId();
                result = null;
                mockIotHubTransportMessage.getCorrelationId();
                result = null;
                mockIotHubTransportMessage.getProperties();
                result = properties;
                new ApplicationProperties((Map) any);
                result = mockApplicationProperties;

                mockIotHubTransportMessage.getDeviceOperationType();
                result = DEVICE_OPERATION_TWIN_GET_REQUEST;

                new MessageAnnotations((Map) any);
                result = mockMessageAnnotations;
            }
        };

        //act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertToProton", mockIotHubTransportMessage);

        //assert
        new Verifications()
        {
            {
                mockMessageImpl.setMessageAnnotations(mockMessageAnnotations);
                times = 1;
            }
        };

        assertNotNull(amqpsConvertToProtonReturnValue);
        assertNotNull(Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageImpl"));
        assertEquals(MessageType.DEVICE_TWIN, Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageType"));
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_033: [The function shall set the proton message annotation operation field to GET if the IotHubTransportMessage operation type is GET_REQUEST.]
    @Test
    public void convertToProtonSetsMessageAnnotationsUpdateReportedPropertiesRequest(
            @Mocked final Properties mockProtonProperties,
            @Mocked final MessageProperty mockMessageProperty,
            @Mocked final IotHubTransportMessage mockIotHubTransportMessage,
            @Mocked final MessageImpl mockMessageImpl,
            @Mocked final ApplicationProperties mockApplicationProperties,
            @Mocked final Map<Symbol, Object> mockMessageAnnotationsMap,
            @Mocked final MessageAnnotations mockMessageAnnotations
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        final MessageProperty[] properties = new MessageProperty[1];
        properties[0] = mockMessageProperty;

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                new Properties();
                result = null;
                mockIotHubTransportMessage.getMessageType();
                result = MessageType.DEVICE_TWIN;
                mockIotHubTransportMessage.getMessageId();
                result = null;
                mockIotHubTransportMessage.getCorrelationId();
                result = null;
                mockIotHubTransportMessage.getProperties();
                result = properties;
                new ApplicationProperties((Map) any);
                result = mockApplicationProperties;

                mockIotHubTransportMessage.getDeviceOperationType();
                result = DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST;

                new MessageAnnotations((Map) any);
                result = mockMessageAnnotations;
            }
        };

        //act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertToProton", mockIotHubTransportMessage);

        //assert
        new Verifications()
        {
            {
                mockMessageImpl.setMessageAnnotations(mockMessageAnnotations);
                times = 1;
            }
        };

        assertNotNull(amqpsConvertToProtonReturnValue);
        assertNotNull(Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageImpl"));
        assertEquals(MessageType.DEVICE_TWIN, Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageType"));
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_033: [The function shall set the proton message annotation operation field to GET if the IotHubTransportMessage operation type is GET_REQUEST.]
    @Test
    public void convertToProtonSetsMessageAnnotationsSubscribeDesiredPropertiesRequest(
            @Mocked final Properties mockProtonProperties,
            @Mocked final MessageProperty mockMessageProperty,
            @Mocked final IotHubTransportMessage mockIotHubTransportMessage,
            @Mocked final MessageImpl mockMessageImpl,
            @Mocked final ApplicationProperties mockApplicationProperties,
            @Mocked final Map<Symbol, Object> mockMessageAnnotationsMap,
            @Mocked final MessageAnnotations mockMessageAnnotations
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        final MessageProperty[] properties = new MessageProperty[1];
        properties[0] = mockMessageProperty;

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                new Properties();
                result = null;
                mockIotHubTransportMessage.getMessageType();
                result = MessageType.DEVICE_TWIN;
                mockIotHubTransportMessage.getMessageId();
                result = null;
                mockIotHubTransportMessage.getCorrelationId();
                result = null;
                mockIotHubTransportMessage.getProperties();
                result = properties;
                new ApplicationProperties((Map) any);
                result = mockApplicationProperties;

                mockIotHubTransportMessage.getDeviceOperationType();
                result = DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST;

                new MessageAnnotations((Map) any);
                result = mockMessageAnnotations;
            }
        };

        //act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertToProton", mockIotHubTransportMessage);

        //assert
        new Verifications()
        {
            {
                mockMessageImpl.setMessageAnnotations(mockMessageAnnotations);
                times = 1;
            }
        };

        assertNotNull(amqpsConvertToProtonReturnValue);
        assertNotNull(Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageImpl"));
        assertEquals(MessageType.DEVICE_TWIN, Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageType"));
    }

    // Tests_SRS_AMQPSDEVICETWIN_12_033: [The function shall set the proton message annotation operation field to GET if the IotHubTransportMessage operation type is GET_REQUEST.]
    @Test
    public void convertToProtonSetsMessageAnnotationsUnSubscribeDesiredPropertiesRequest(
            @Mocked final Properties mockProtonProperties,
            @Mocked final MessageProperty mockMessageProperty,
            @Mocked final IotHubTransportMessage mockIotHubTransportMessage,
            @Mocked final MessageImpl mockMessageImpl,
            @Mocked final ApplicationProperties mockApplicationProperties,
            @Mocked final Map<Symbol, Object> mockMessageAnnotationsMap,
            @Mocked final MessageAnnotations mockMessageAnnotations
    ) throws IOException
    {
        //arrange
        String deviceId = "deviceId";
        final MessageProperty[] properties = new MessageProperty[1];
        properties[0] = mockMessageProperty;

        AmqpsDeviceTwin amqpsDeviceTwin = Deencapsulation.newInstance(AmqpsDeviceTwin.class, deviceId);
        Deencapsulation.invoke(amqpsDeviceTwin, "openLinks", mockSession);

        new NonStrictExpectations()
        {
            {
                new Properties();
                result = null;
                mockIotHubTransportMessage.getMessageType();
                result = MessageType.DEVICE_TWIN;
                mockIotHubTransportMessage.getMessageId();
                result = null;
                mockIotHubTransportMessage.getCorrelationId();
                result = null;
                mockIotHubTransportMessage.getProperties();
                result = properties;
                new ApplicationProperties((Map) any);
                result = mockApplicationProperties;

                mockIotHubTransportMessage.getDeviceOperationType();
                result = DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST;

                new MessageAnnotations((Map) any);
                result = mockMessageAnnotations;
            }
        };

        //act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsDeviceTwin, "convertToProton", mockIotHubTransportMessage);

        //assert
        new Verifications()
        {
            {
                mockMessageImpl.setMessageAnnotations(mockMessageAnnotations);
                times = 1;
            }
        };

        assertNotNull(amqpsConvertToProtonReturnValue);
        assertNotNull(Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageImpl"));
        assertEquals(MessageType.DEVICE_TWIN, Deencapsulation.invoke(amqpsConvertToProtonReturnValue, "getMessageType"));
    }
}
