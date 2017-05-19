// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodMessage;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.Mqtt;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttDeviceMethod;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.*;
import static org.junit.Assert.*;

public class MqttDeviceMethodTest
{
    @Mocked
    Mqtt mockedMqtt;

    /*
    Tests_SRS_MqttDeviceMethod_25_001: [**The constructor shall instantiate super class without any parameters.**]**

    Tests_SRS_MqttDeviceMethod_25_002: [**The constructor shall create subscribe and response topics strings for device methods as per the spec.**]**
     */
    @Test
    public void constructorSucceeds() throws IOException
    {
        //arrange
        String actualSubscribeTopic = "$iothub/methods/POST/#";
        String actualResTopic = "$iothub/methods/res";

        //act
        MqttDeviceMethod testMethod = new MqttDeviceMethod();

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
    public void startSucceedsCalls() throws IOException
    {
        //arrange
        MqttDeviceMethod testMethod = new MqttDeviceMethod();

        //act
        testMethod.start();

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedMqtt, "subscribe", anyString);
                times = 0;
            }
        };
    }


    @Test
    public void startSucceedsDoesNotCallsSubscribeIfStarted() throws IOException
    {
        //arrange
        MqttDeviceMethod testMethod = new MqttDeviceMethod();
        testMethod.start();
        //act
        testMethod.start();

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedMqtt, "subscribe", anyString);
                maxTimes = 0;
            }
        };
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_015: [**stop method shall unsubscribe from method subscribe topic ($iothub/methods/POST/#) and throw IoException otherwise.**]**
     */
    @Test
    public void stopSucceedsCallsUnSubscribe() throws IOException
    {
        //arrange
        MqttDeviceMethod testMethod = new MqttDeviceMethod();
        testMethod.start();

        //act
        testMethod.stop();

        //assert

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedMqtt, "unsubscribe", anyString);
                maxTimes = 1;
            }
        };
    }

    @Test
    public void stopSucceedsDoesNotCallUnSubscribeIfStopped() throws IOException
    {
        //arrange
        MqttDeviceMethod testMethod = new MqttDeviceMethod();
        testMethod.start();
        testMethod.stop();

        //act
        testMethod.stop();

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedMqtt, "unsubscribe", anyString);
                maxTimes = 1;
            }
        };
    }

    @Test
    public void stopSucceedsDoesNotCallUnSubscribeIfNotStarted() throws IOException
    {
        //arrange
        MqttDeviceMethod testMethod = new MqttDeviceMethod();

        //act
        testMethod.stop();

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedMqtt, "unsubscribe", anyString);
                maxTimes = 0;
            }
        };
    }
    /*
    Tests_SRS_MqttDeviceMethod_25_004: [**parseTopic shall look for the method topic($iothub/methods) prefix from received message queue as per spec and if found shall return it as string.**]**

    Tests_SRS_MqttDeviceMethod_25_005: [**If none of the topics from the received queue match the methods topic prefix then this method shall return null string .**]**

    Tests_SRS_MqttDeviceMethod_25_006: [**If received messages queue is empty then parseTopic shall return null string.**]**

    Tests_SRS_MqttDeviceMethod_25_020: [**send method shall subscribe to topic from spec ($iothub/methods/POST/#) if the operation is of type DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST.**]**
     */
    @Test
    public void sendSucceedsCallsSubscribe() throws IOException
    {
        //arrange
        final String actualSubscribeTopic = "$iothub/methods/POST/#";
        byte[] actualPayload = "TestMessage".getBytes();
        DeviceMethodMessage testMessage = new DeviceMethodMessage(actualPayload);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST);
        MqttDeviceMethod testMethod = new MqttDeviceMethod();
        testMethod.start();

        //act
        testMethod.send(testMessage);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedMqtt, "subscribe", actualSubscribeTopic);
                maxTimes = 1;
            }
        };
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_022: [**send method shall build the publish topic of the format mentioned in spec ($iothub/methods/res/{status}/?$rid={request id}) and publish if the operation is of type DEVICE_OPERATION_METHOD_SEND_RESPONSE.**]**
     */
    @Test
    public void sendSucceedsCallsPublish() throws IOException
    {
        final byte[] actualPayload = "TestMessage".getBytes();
        final DeviceMethodMessage testMessage = new DeviceMethodMessage(actualPayload);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_SEND_RESPONSE);
        testMessage.setRequestId("ReqId");
        testMessage.setStatus("testStatus");
        MqttDeviceMethod testMethod = new MqttDeviceMethod();
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
                Deencapsulation.invoke(mockedMqtt, "publish", anyString, actualPayload);
                maxTimes = 1;
            }
        };
        assertTrue(testRequestMap.isEmpty());
    }

    @Test (expected = IOException.class)
    public void sendThrowsOnInvalidOperation() throws IOException
    {
        final byte[] actualPayload = "TestMessage".getBytes();
        final DeviceMethodMessage testMessage = new DeviceMethodMessage(actualPayload);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_UNKNOWN);
        MqttDeviceMethod testMethod = new MqttDeviceMethod();
        testMethod.start();

        //act
        testMethod.send(testMessage);
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_018: [**send method shall throw an IoException if device method has not been started yet.**]**
     */
    @Test (expected = IOException.class)
    public void sendThrowsIfNotStarted() throws IOException
    {
        final byte[] actualPayload = "TestMessage".getBytes();
        final DeviceMethodMessage testMessage = new DeviceMethodMessage(actualPayload);
        MqttDeviceMethod testMethod = new MqttDeviceMethod();

        //act
        testMethod.send(testMessage);
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_016: [**send method shall throw an exception if the message is null.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void sendThrowsOnMessageNull() throws IOException
    {
        MqttDeviceMethod testMethod = new MqttDeviceMethod();
        testMethod.start();
        //act
        testMethod.send(null);
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_017: [**send method shall return if the message is not of Type DeviceMethod.**]**
     */
    @Test
    public void sendDoesNotSendOnDifferentMessageType() throws IOException
    {
        final byte[] actualPayload = "TestMessage".getBytes();
        final DeviceMethodMessage testMessage = new DeviceMethodMessage(actualPayload);
        testMessage.setMessageType(MessageType.DeviceTwin);
        MqttDeviceMethod testMethod = new MqttDeviceMethod();
        testMethod.start();

        //act
        testMethod.send(testMessage);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedMqtt, "publish", anyString, actualPayload);
                maxTimes = 0;
                Deencapsulation.invoke(mockedMqtt, "subscribe", anyString);
                maxTimes = 0;
            }
        };

    }

    /*
    Tests_SRS_MqttDeviceMethod_25_021: [**send method shall throw an IoException if message contains a null or empty request id if the operation is of type DEVICE_OPERATION_METHOD_SEND_RESPONSE.**]**
     */
    @Test (expected = IOException.class)
    public void sendThrowsOnNullRequestID() throws IOException
    {
        final byte[] actualPayload = "TestMessage".getBytes();
        final DeviceMethodMessage testMessage = new DeviceMethodMessage(actualPayload);
        testMessage.setMessageType(MessageType.DeviceMethods);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_SEND_RESPONSE);
        MqttDeviceMethod testMethod = new MqttDeviceMethod();
        testMethod.start();

        //act
        testMethod.send(testMessage);
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_023: [**send method shall throw an exception if a response is sent without having a method invoke on the request id if the operation is of type DEVICE_OPERATION_METHOD_SEND_RESPONSE.**]**
     */
    @Test (expected = IOException.class)
    public void sendThrowsOnSendingResponseWithoutReceivingMethodInvoke() throws IOException
    {
        final byte[] actualPayload = "TestMessage".getBytes();
        final DeviceMethodMessage testMessage = new DeviceMethodMessage(actualPayload);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_SEND_RESPONSE);
        testMessage.setRequestId("ReqId");
        testMessage.setStatus("testStatus");
        MqttDeviceMethod testMethod = new MqttDeviceMethod();
        testMethod.start();

        //act
        testMethod.send(testMessage);
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_019: [**send method shall throw an IoException if the getDeviceOperationType() is not of type DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST or DEVICE_OPERATION_METHOD_SEND_RESPONSE .**]**
     */
    @Test (expected = IOException.class)
    public void sendThrowsOnMismatchedRequestType() throws IOException
    {
        final byte[] actualPayload = "TestMessage".getBytes();
        final DeviceMethodMessage testMessage = new DeviceMethodMessage(actualPayload);
        testMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_SEND_RESPONSE);
        testMessage.setRequestId("ReqId");
        testMessage.setStatus("testStatus");
        MqttDeviceMethod testMethod = new MqttDeviceMethod();
        Map<String, DeviceOperations> testRequestMap = new HashMap<>();
        testRequestMap.put("ReqId", DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST);
        Deencapsulation.setField(testMethod, "requestMap", testRequestMap);
        testMethod.start();

        //act
        testMethod.send(testMessage);
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_024: [**This method shall call parseTopic to parse the topic from the received Messages queue looking for presence of $iothub/methods/ in the topics .**]**
    Tests_SRS_MqttDeviceMethod_25_026: [**This method shall call parsePayload to get the message payload from the recevived Messages queue corresponding to the messaging client's operation.**]**
    Tests_SRS_MqttDeviceMethod_25_028: [**If the topic is of type post topic then this method shall parse further for method name and set it for the message by calling setMethodName for the message**]**
    Tests_SRS_MqttDeviceMethod_25_030: [**If the topic is of type post topic then this method shall parse further to look for request id which if found is set by calling setRequestId**]**
    Tests_SRS_MqttDeviceMethod_25_032: [**If the topic is of type post topic and if method name and request id has been successfully parsed then this method shall set operation type as DEVICE_OPERATION_METHOD_RECEIVE_REQUEST **]**
     */
    @Test
    public void receiveSucceeds() throws IOException
    {
        //arrange
        String topic = "$iothub/methods/POST/testMethod/?$rid=10";
        byte[] actualPayload = "TestPayload".getBytes();
        ConcurrentSkipListMap<String, byte[]> testAllReceivedMessages = new ConcurrentSkipListMap<>();
        testAllReceivedMessages.put(topic, actualPayload);
        Deencapsulation.setField(mockedMqtt, "allReceivedMessages", testAllReceivedMessages);

        MqttDeviceMethod testMethod = new MqttDeviceMethod();
        testMethod.start();

        //act
        Message testMessage = testMethod.receive();
        DeviceMethodMessage testDMMessage = (DeviceMethodMessage) testMessage;

        //assert
        assertNotNull(testMessage);
        assertTrue(testMessage.getMessageType().equals(MessageType.DeviceMethods));
        assertTrue(testDMMessage.getRequestId().equals(String.valueOf(10)));
        assertTrue(testDMMessage.getMethodName().equals("testMethod"));
        assertTrue(testDMMessage.getDeviceOperationType().equals(DEVICE_OPERATION_METHOD_RECEIVE_REQUEST));
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_025: [**If the call parseTopic returns null or empty string then this method shall do nothing and return null**]**
     */
    @Test
    public void receiveReturnsNullMessageIfTopicNotFound() throws IOException
    {
        //arrange
        String topic = "$iothub/not_methods/POST/testMethod/?$rid=10";
        byte[] actualPayload = "TestPayload".getBytes();
        ConcurrentSkipListMap<String, byte[]> testAllReceivedMessages = new ConcurrentSkipListMap<>();
        testAllReceivedMessages.put(topic, actualPayload);
        Deencapsulation.setField(mockedMqtt, "allReceivedMessages", testAllReceivedMessages);

        MqttDeviceMethod testMethod = new MqttDeviceMethod();
        testMethod.start();

        //act
        Message testMessage = testMethod.receive();

        //assert
        assertNull(testMessage);
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_027: [**This method shall parse topic to look for Post topic ($iothub/methods/POST/) and throw unsupportedoperation exception other wise.**]**
     */
    @Test (expected = UnsupportedOperationException.class)
    public void receiveReturnsNullMessageIfTopicWasNotPost() throws IOException
    {
        //arrange
        String topic = "$iothub/methods/Not_POST/testMethod/?$rid=10";
        byte[] actualPayload = "TestPayload".getBytes();
        ConcurrentSkipListMap<String, byte[]> testAllReceivedMessages = new ConcurrentSkipListMap<>();
        testAllReceivedMessages.put(topic, actualPayload);
        Deencapsulation.setField(mockedMqtt, "allReceivedMessages", testAllReceivedMessages);

        MqttDeviceMethod testMethod = new MqttDeviceMethod();
        testMethod.start();

        //act
        Message testMessage = testMethod.receive();
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_029: [**If method name not found or is null then receive shall throw IOException **]**
     */
    @Test (expected = IOException.class)
    public void receiveThrowsIfMethodNameCouldNotBeParsed() throws IOException
    {
        //arrange
        String topic = "$iothub/methods/POST/";
        byte[] actualPayload = "TestPayload".getBytes();
        ConcurrentSkipListMap<String, byte[]> testAllReceivedMessages = new ConcurrentSkipListMap<>();
        testAllReceivedMessages.put(topic, actualPayload);
        Deencapsulation.setField(mockedMqtt, "allReceivedMessages", testAllReceivedMessages);

        MqttDeviceMethod testMethod = new MqttDeviceMethod();
        testMethod.start();

        //act
        Message testMessage = testMethod.receive();
    }

    /*
    Tests_SRS_MqttDeviceMethod_25_031: [**If request id is not found or is null then receive shall throw IOException **]**
     */
    @Test (expected = IOException.class)
    public void receiveThrowsIfRIDCouldNotBeParsed() throws IOException
    {
        //arrange
        String topic = "$iothub/methods/POST/testMethod/";
        byte[] actualPayload = "TestPayload".getBytes();
        ConcurrentSkipListMap<String, byte[]> testAllReceivedMessages = new ConcurrentSkipListMap<>();
        testAllReceivedMessages.put(topic, actualPayload);
        Deencapsulation.setField(mockedMqtt, "allReceivedMessages", testAllReceivedMessages);

        MqttDeviceMethod testMethod = new MqttDeviceMethod();
        testMethod.start();

        //act
        Message testMessage = testMethod.receive();
    }

    @Test
    public void receiveReturnsEmptyPayLoadIfNullPayloadParsed() throws IOException
    {
        //arrange
        String topic = "$iothub/methods/POST/testMethod/?$rid=10";
        byte[] actualPayload = "".getBytes();
        ConcurrentSkipListMap<String, byte[]> testAllReceivedMessages = new ConcurrentSkipListMap<>();
        testAllReceivedMessages.put(topic, actualPayload);
        Deencapsulation.setField(mockedMqtt, "allReceivedMessages", testAllReceivedMessages);

        MqttDeviceMethod testMethod = new MqttDeviceMethod();
        testMethod.start();

        //act
        Message testMessage = testMethod.receive();
        DeviceMethodMessage testDMMessage = (DeviceMethodMessage) testMessage;

        //assert
        assertNotNull(testMessage);
        assertTrue(testMessage.getBytes().length == 0);
        assertTrue(testMessage.getMessageType().equals(MessageType.DeviceMethods));
        assertTrue(testDMMessage.getRequestId().equals(String.valueOf(10)));
        assertTrue(testDMMessage.getMethodName().equals("testMethod"));
        assertTrue(testDMMessage.getDeviceOperationType().equals(DEVICE_OPERATION_METHOD_RECEIVE_REQUEST));

    }
}
