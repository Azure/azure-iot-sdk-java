// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.Mqtt;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttConnection;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttDeviceMethod;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.DEVICE_OPERATION_METHOD_RECEIVE_REQUEST;
import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.DEVICE_OPERATION_METHOD_SEND_RESPONSE;
import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST;
import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.DEVICE_OPERATION_UNKNOWN;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/* Unit tests for MqttDeviceMethod
 * Code coverage: 100% methods, 97% lines
 */
public class MqttDeviceMethodTest
{
    @Mocked
    MqttConnection mockedMqttConnection;

    private ConcurrentLinkedQueue<Pair<String, byte[]>> testAllReceivedMessages;

    @Before
    public void baseConstructorExpectation()
    {
        testAllReceivedMessages = new ConcurrentLinkedQueue<>();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedMqttConnection, "getAllReceivedMessages");
                result = testAllReceivedMessages;
                Deencapsulation.invoke(mockedMqttConnection, "getMqttLock");
                result = new Object();
            }
        };
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_001: [**The constructor shall instantiate super class without any parameters.**]**

    Tests_SRS_MqttDeviceMethod_25_002: [**The constructor shall create subscribe and response topics strings for device methods as per the spec.**]**
     */
    @Test
    public void constructorSucceeds(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        //arrange
        String actualSubscribeTopic = "$iothub/methods/POST/#";
        String actualResTopic = "$iothub/methods/res";

        //act
        MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");

        //assert
        String testSubscribeTopic = Deencapsulation.getField(testMethod, "subscribeTopic");
        String testResTopic = Deencapsulation.getField(testMethod, "responseTopic");

        assertNotNull(testSubscribeTopic);
        assertNotNull(testResTopic);
        assertTrue(testSubscribeTopic.equals(actualSubscribeTopic));
        assertTrue(testResTopic.equals(actualResTopic));

    }

    /*
    Tests_SRS_MqttDeviceMethod_25_014: [**start method shall just mark that this class is ready to start.**]**
     */
    @Test
    public void startSucceedsCalls(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        //arrange
        final MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");

        //act
        testMethod.start();

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(testMethod, "subscribe", anyString);
                times = 0;
            }
        };
    }


    @Test
    public void startSucceedsDoesNotCallsSubscribeIfStarted(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        //arrange
        final MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");
        testMethod.start();
        //act
        testMethod.start();

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(testMethod, "subscribe", anyString);
                maxTimes = 0;
            }
        };
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_020: [**send method shall subscribe to topic from spec ($iothub/methods/POST/#) if the operation is of type DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST.**]**
     */
    @Test
    public void sendSucceedsCallsSubscribe(@Mocked final Mqtt mockMqtt) throws IOException, TransportException
    {
        //arrange
        final String actualSubscribeTopic = "$iothub/methods/POST/#";
        byte[] actualPayload = "TestMessage".getBytes();
        IotHubTransportMessage testMessage = new IotHubTransportMessage(actualPayload, MessageType.DEVICE_METHODS);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST);
        final MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");
        testMethod.start();

        //act
        testMethod.send(testMessage);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(testMethod, "subscribe", actualSubscribeTopic);
                maxTimes = 1;
            }
        };
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_022: [**send method shall build the publish topic of the format mentioned in spec ($iothub/methods/res/{status}/?$rid={request id}) and publish if the operation is of type DEVICE_OPERATION_METHOD_SEND_RESPONSE.**]**
     */
    @Test
    public void sendSucceedsCallsPublish(@Mocked final Mqtt mockMqtt) throws IOException, TransportException
    {
        final byte[] actualPayload = "TestMessage".getBytes();
        final IotHubTransportMessage testMessage = new IotHubTransportMessage(actualPayload, MessageType.DEVICE_METHODS);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_SEND_RESPONSE);
        testMessage.setRequestId("ReqId");
        testMessage.setStatus("testStatus");
        final MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");
        Map<String, DeviceOperations> testRequestMap = new HashMap<>();
        testRequestMap.put("ReqId", DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);
        Deencapsulation.setField(testMethod, "requestMap", testRequestMap);
        testMethod.start();

        //act
        testMethod.send(testMessage);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(testMethod, "publish", new Class[] {String.class, Message.class}, anyString, any);
                maxTimes = 1;
            }
        };
        assertTrue(testRequestMap.isEmpty());
    }

    @Test (expected = TransportException.class)
    public void sendThrowsOnInvalidOperation(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        final byte[] actualPayload = "TestMessage".getBytes();
        final IotHubTransportMessage testMessage = new IotHubTransportMessage(actualPayload, MessageType.DEVICE_METHODS);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_UNKNOWN);
        MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");
        testMethod.start();

        //act
        testMethod.send(testMessage);
    }

    //Tests_SRS_MqttDeviceMethod_25_018: [send method shall throw a TransportException if device method has not been started yet.]
    @Test (expected = TransportException.class)
    public void sendThrowsIfNotStarted(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        final byte[] actualPayload = "TestMessage".getBytes();
        final IotHubTransportMessage testMessage = new IotHubTransportMessage(actualPayload, MessageType.DEVICE_METHODS);
        MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");

        //act
        testMethod.send(testMessage);
    }

    //Tests_SRS_MqttDeviceMethod_25_016: [send method shall throw an exception if the message is null.]
    @Test (expected = IllegalArgumentException.class)
    public void sendThrowsOnMessageNull() throws TransportException
    {
        MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");
        testMethod.start();
        //act
        testMethod.send(null);
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_017: [**send method shall return if the message is not of Type DeviceMethod.**]**
     */
    @Test
    public void sendDoesNotSendOnDifferentMessageType(@Mocked final Mqtt mockMqtt, final @Mocked IotHubTransportMessage mockedMessage) throws TransportException
    {
        final byte[] actualPayload = "TestMessage".getBytes();
        final IotHubTransportMessage testMessage = new IotHubTransportMessage(actualPayload, MessageType.DEVICE_METHODS);
        testMessage.setMessageType(MessageType.DEVICE_TWIN);
        final MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");

        testMethod.start();

        //act
        testMethod.send(testMessage);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(testMethod, "publish", anyString, mockedMessage);
                maxTimes = 0;
                Deencapsulation.invoke(testMethod, "subscribe", anyString);
                maxTimes = 0;
            }
        };
    }

    //Tests_SRS_MqttDeviceMethod_25_021: [send method shall throw an IllegalArgumentException if message contains a null or empty request id if the operation is of type DEVICE_OPERATION_METHOD_SEND_RESPONSE.]
    @Test (expected = IllegalArgumentException.class)
    public void sendThrowsOnNullRequestID() throws TransportException
    {
        final byte[] actualPayload = "TestMessage".getBytes();
        final IotHubTransportMessage testMessage = new IotHubTransportMessage(actualPayload, MessageType.DEVICE_METHODS);
        testMessage.setMessageType(MessageType.DEVICE_METHODS);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_SEND_RESPONSE);
        MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");
        testMethod.start();

        //act
        testMethod.send(testMessage);
    }

    //Tests_SRS_MqttDeviceMethod_25_023: [send method shall throw an exception if a response is sent without having a method invoke on the request id if the operation is of type DEVICE_OPERATION_METHOD_SEND_RESPONSE.]
    @Test (expected = TransportException.class)
    public void sendThrowsOnSendingResponseWithoutReceivingMethodInvoke() throws TransportException
    {
        final byte[] actualPayload = "TestMessage".getBytes();
        final IotHubTransportMessage testMessage = new IotHubTransportMessage(actualPayload, MessageType.DEVICE_METHODS);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_SEND_RESPONSE);
        testMessage.setRequestId("ReqId");
        testMessage.setStatus("testStatus");
        MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");
        testMethod.start();

        //act
        testMethod.send(testMessage);
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_019: [**send method shall throw a TransportException if the getDeviceOperationType() is not of type DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST or DEVICE_OPERATION_METHOD_SEND_RESPONSE .**]**
     */
    @Test (expected = TransportException.class)
    public void sendThrowsOnMismatchedRequestType() throws TransportException
    {
        final byte[] actualPayload = "TestMessage".getBytes();
        final IotHubTransportMessage testMessage = new IotHubTransportMessage(actualPayload, MessageType.DEVICE_METHODS);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_SEND_RESPONSE);
        testMessage.setRequestId("ReqId");
        testMessage.setStatus("testStatus");
        MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");
        Map<String, DeviceOperations> testRequestMap = new HashMap<>();
        testRequestMap.put("ReqId", DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST);
        testMethod.start();

        //act
        testMethod.send(testMessage);
    }

    /*
    * Tests_SRS_MQTTDEVICEMETHOD_25_026: [**This method shall call peekMessage to get the message payload from the received Messages queue corresponding to the messaging client's operation.**]**
    * Tests_SRS_MQTTDEVICEMETHOD_25_028: [**If the topic is of type post topic then this method shall parse further for method name and set it for the message by calling setMethodName for the message**]**
    * Tests_SRS_MQTTDEVICEMETHOD_25_030: [**If the topic is of type post topic then this method shall parse further to look for request id which if found is set by calling setRequestId**]**
    * Tests_SRS_MQTTDEVICEMETHOD_25_032: [**If the topic is of type post topic and if method name and request id has been successfully parsed then this method shall set operation type as DEVICE_OPERATION_METHOD_RECEIVE_REQUEST **]**
    */
    @Test
    public void receiveSucceeds() throws TransportException
    {
        //arrange
        String topic = "$iothub/methods/POST/testMethod/?$rid=10";
        byte[] actualPayload = "TestPayload".getBytes();
        testAllReceivedMessages.add(new MutablePair<>(topic, actualPayload));
        MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");
        testMethod.start();

        //act
        Message testMessage = testMethod.receive();
        IotHubTransportMessage testDMMessage = (IotHubTransportMessage) testMessage;

        //assert
        assertNotNull(testMessage);
        assertTrue(testMessage.getMessageType().equals(MessageType.DEVICE_METHODS));
        assertTrue(testDMMessage.getRequestId().equals(String.valueOf(10)));
        assertTrue(testDMMessage.getMethodName().equals("testMethod"));
        assertTrue(testDMMessage.getDeviceOperationType().equals(DEVICE_OPERATION_METHOD_RECEIVE_REQUEST));
    }

    // Tests_SRS_MQTTDEVICEMETHOD_25_026: [**This method shall call peekMessage to get the message payload from the received Messages queue corresponding to the messaging client's operation.**]**
    @Test
    public void receiveReturnsNullMessageIfTopicNotFound() throws TransportException
    {
        //arrange
        Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
        MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");
        testMethod.start();

        //act
        Message testMessage = testMethod.receive();

        //assert
        assertNull(testMessage);
    }


    //Tests_SRS_MqttDeviceMethod_34_027: [This method shall parse message to look for Post topic ($iothub/methods/POST/) and return null other wise.]
    @Test
    public void receiveReturnsNullMessageIfTopicWasNotPost() throws TransportException
    {
        //arrange
        String topic = "$iothub/methods/Not_POST/testMethod/?$rid=10";
        byte[] actualPayload = "TestPayload".getBytes();
        Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
        testAllReceivedMessages.add(new MutablePair<>(topic, actualPayload));
        MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");
        testMethod.start();

        //act
        Message actualMessage = testMethod.receive();

        //assert
        assertNull(actualMessage);
    }

    // Tests_SRS_MQTTDEVICEMETHOD_25_029: [**If method name not found or is null then receive shall throw TransportException **]**
    @Test (expected = TransportException.class)
    public void receiveThrowsIfMethodNameCouldNotBeParsed() throws TransportException
    {
        //arrange
        String topic = "$iothub/methods/POST/";
        byte[] actualPayload = "TestPayload".getBytes();
        testAllReceivedMessages.add(new MutablePair<>(topic, actualPayload));
        MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");
        testMethod.start();

        //act
        testMethod.receive();
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_031: [**If request id is not found or is null then receive shall throw TransportException **]**
     */
    @Test (expected = TransportException.class)
    public void receiveThrowsIfRIDCouldNotBeParsed() throws TransportException
    {
        //arrange
        String topic = "$iothub/methods/POST/testMethod/";
        byte[] actualPayload = "TestPayload".getBytes();
        testAllReceivedMessages.add(new MutablePair<>(topic, actualPayload));
        MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");

        testMethod.start();

        //act
        testMethod.receive();
    }

    @Test
    public void receiveReturnsEmptyPayLoadIfNullPayloadParsed() throws TransportException
    {
        //arrange
        String topic = "$iothub/methods/POST/testMethod/?$rid=10";
        byte[] actualPayload = "".getBytes();
        testAllReceivedMessages.add(new MutablePair<>(topic, actualPayload));
        MqttDeviceMethod testMethod = new MqttDeviceMethod(mockedMqttConnection, "");
        testMethod.start();

        //act
        Message testMessage = testMethod.receive();
        IotHubTransportMessage testDMMessage = (IotHubTransportMessage) testMessage;

        //assert
        assertNotNull(testMessage);
        assertTrue(testMessage.getBytes().length == 0);
        assertTrue(testMessage.getMessageType().equals(MessageType.DEVICE_METHODS));
        assertTrue(testDMMessage.getRequestId().equals(String.valueOf(10)));
        assertTrue(testDMMessage.getMethodName().equals("testMethod"));
        assertTrue(testDMMessage.getDeviceOperationType().equals(DEVICE_OPERATION_METHOD_RECEIVE_REQUEST));
    }
}
