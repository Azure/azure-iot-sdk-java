// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubServiceException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.Mqtt;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttConnection;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttDeviceTwin;
import mockit.*;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.*;
import static org.junit.Assert.*;

/* Unit tests for MqttDeviceTwin
 * Code coverage: 100% methods, 94% lines
 */
public class MqttDeviceTwinTest
{
    final String resTopic = "$iothub/twin/res/#";
    final String anyString = new String();
    final String mockVersion = "1.0.1";
    final String mockReqId = String.valueOf(100);

    @Mocked
    IOException mockIOException;

    @Mocked
    MqttConnection mockedMqttConnection;

    private void baseConstructorExpectation()
    {
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedMqttConnection, "getAllReceivedMessages");
                result = new ConcurrentLinkedQueue<>();
                Deencapsulation.invoke(mockedMqttConnection, "getMqttLock");
                result = new Object();
            }
        };
    }

    /*
    **Tests_SRS_MQTTDEVICETWIN_25_001: [The constructor shall instantiate super class without any parameters.]
    **Tests_SRS_MQTTDEVICETWIN_25_002: [The constructor shall construct device twin response subscribeTopic.]
     */
    @Test
    public void constructorConstructsSubscribeTopicForTwin(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        //arrange

        //act
        MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
        //assert
        String actualSubscribeTopic = Deencapsulation.getField(testTwin, "subscribeTopic");
        assertNotNull(actualSubscribeTopic);
        assertEquals(actualSubscribeTopic, resTopic);
    }
    /*
    **Tests_SRS_MQTTDEVICETWIN_25_019: [start method shall subscribe to twin response topic ($iothub/twin/res/#) if connected.]
     */
    @Test
    public void startSubscribesToDeviceTwinResponse(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        //arrange
        MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");

        //act

        testTwin.start();
        //assert

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockMqtt, "subscribe", resTopic);
                times = 1;
            }
        };
    }
    /*
    **Tests_SRS_MQTTDEVICETWIN_25_019: [start method shall subscribe to twin response topic ($iothub/twin/res/#) if connected and throw IoException otherwise.]
     */
    @Test (expected = IOException.class)
    public void startThrowsExceptionIfSubscribesFails(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        try
        {
            //arrange
            new StrictExpectations()
            {
                {
                    Deencapsulation.invoke(mockMqtt, "subscribe", resTopic);
                    result = mockIOException;
                }
            };

            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");

            //act
            testTwin.start();
        }
        finally
        {
            //assert

        }
    }

    /*
    **Tests_SRS_MQTTDEVICETWIN_25_024: [send method shall build the get request topic of the format mentioned in spec ($iothub/twin/GET/?$rid={request id}) if the operation is of type DEVICE_OPERATION_TWIN_GET_REQUEST.]
     */
    @Test
    public void sendPublishesMessageForGetTwinOnCorrectTopic(@Mocked final Mqtt mockMqtt, @Mocked final IotHubTransportMessage mockMessage) throws TransportException
    {
        //arrange
        final byte[] actualPayload = {0x61, 0x62, 0x63};
        final String expectedTopic = "$iothub/twin/GET/?$rid="+mockReqId;
        MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
        testTwin.start();
        new NonStrictExpectations()
        {
            {
                mockMessage.getBytes();
                result = actualPayload;
                mockMessage.getMessageType();
                result = MessageType.DEVICE_TWIN;
                mockMessage.getDeviceOperationType();
                result = DEVICE_OPERATION_TWIN_GET_REQUEST;
                mockMessage.getRequestId();
                result = mockReqId;
            }
        };

        //act
        testTwin.send(mockMessage);

        //assert
        new Verifications()
        {
            {
                mockMessage.getBytes();
                times = 1;
                Deencapsulation.invoke(mockMqtt, "publish", expectedTopic, mockMessage);
                times = 1;
            }
        };
    }
    /*
    **Tests_SRS_MQTTDEVICETWIN_25_025: [send method shall throw an IllegalArgumentException if message contains a null or empty request id if the operation is of type DEVICE_OPERATION_TWIN_GET_REQUEST.]
     */
    @Test (expected = IllegalArgumentException.class)
    public void sendThrowsIllegalArgumentExceptionForGetTwinOnCorrectTopicIfReqIdIsNullOrEmpty(@Mocked final Mqtt mockMqtt, @Mocked final IotHubTransportMessage mockMessage) throws TransportException
    {
        final byte[] actualPayload = {0x61, 0x62, 0x63};
        final String expectedTopic = "$iothub/twin/GET/?$rid=" + mockReqId;
        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            testTwin.start();
            new NonStrictExpectations()
            {
                {
                    mockMessage.getBytes();
                    result = actualPayload;
                    mockMessage.getMessageType();
                    result = MessageType.DEVICE_TWIN;
                    mockMessage.getDeviceOperationType();
                    result = DEVICE_OPERATION_TWIN_GET_REQUEST;
                    mockMessage.getRequestId();
                    result = null;
                }
            };

            //act
            testTwin.send(mockMessage);
        }
        finally
        {
            //assert
            new Verifications()
            {
                {
                    mockMessage.getBytes();
                    times = 1;
                    Deencapsulation.invoke(mockMqtt, "publish", expectedTopic, actualPayload, mockMessage);
                    times = 0;

                }
            };
        }
    }
    /*
    **Tests_SRS_MQTTDEVICETWIN_25_026: [send method shall build the update reported properties request topic of the format mentioned in spec ($iothub/twin/PATCH/properties/reported/?$rid={request id}&$version={base version}) if the operation is of type DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST.]
     */
    @Test
    public void sendPublishesMessageForUpdateReportedPropertiesOnCorrectTopic(@Mocked final Mqtt mockMqtt, @Mocked final IotHubTransportMessage mockMessage) throws TransportException
    {
        //arrange
        final byte[] actualPayload = {0x61, 0x62, 0x63};
        final String expectedTopic = "$iothub/twin/PATCH/properties/reported/?$rid="+ mockReqId + "&$version=" + mockVersion;
        MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
        testTwin.start();
        new NonStrictExpectations()
        {
            {
                mockMessage.getBytes();
                result = actualPayload;
                mockMessage.getMessageType();
                result = MessageType.DEVICE_TWIN;
                mockMessage.getDeviceOperationType();
                result = DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST;
                mockMessage.getRequestId();
                result = mockReqId;
                mockMessage.getVersion();
                result = mockVersion;
            }
        };

        //act
        testTwin.send(mockMessage);

        //assert
        new Verifications()
        {
            {
                mockMessage.getBytes();
                times = 1;
                Deencapsulation.invoke(mockMqtt, "publish", expectedTopic, mockMessage);
                times = 1;
            }
        };
    }
    /*
    **Tests_SRS_MQTTDEVICETWIN_25_027: [send method shall throw an IllegalArgumentException if message contains a null or empty request id if the operation is of type DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST.]
     */
    @Test (expected = IllegalArgumentException.class)
    public void sendThrowsIllegalArgumentExceptionForUpdateReportedPropertiesOnCorrectTopicIfReqIdIsNullOrEmpty(@Mocked final Mqtt mockMqtt, @Mocked final IotHubTransportMessage mockMessage) throws TransportException
    {
        final byte[] actualPayload = {0x61, 0x62, 0x63};
        final String expectedTopic = "$iothub/twin/PATCH/properties/reported/?$rid=" + mockReqId + "&$version=" + mockVersion;
        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            testTwin.start();
            new NonStrictExpectations()
            {
                {
                    mockMessage.getBytes();
                    result = actualPayload;
                    mockMessage.getMessageType();
                    result = MessageType.DEVICE_TWIN;
                    mockMessage.getDeviceOperationType();
                    result = DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST;
                    mockMessage.getRequestId();
                    result = null;
                }
            };

            //act
            testTwin.send(mockMessage);
        }
        finally
        {
             //assert
            new Verifications()
            {
                {
                    mockMessage.getBytes();
                    times = 1;
                    Deencapsulation.invoke(mockMqtt, "publish", expectedTopic, actualPayload, mockMessage);
                    times = 0;

                }
            };
        }
    }
    /*
    **Tests_SRS_MQTTDEVICETWIN_25_029: [send method shall build the subscribe to desired properties request topic of the format mentioned in spec ($iothub/twin/PATCH/properties/desired/?$version={new version}) if the operation is of type DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.]
     */
    @Test
    public void sendDoesNotPublishesMessageForSubscribeToDesiredPropertiesOnCorrectTopic(@Mocked final Mqtt mockMqtt, @Mocked final IotHubTransportMessage mockMessage) throws TransportException
    {
        //arrange
        final byte[] actualPayload = {0x61, 0x62, 0x63};
        final String expectedTopic = "$iothub/twin/PATCH/properties/desired/#";
        MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
        testTwin.start();
        new NonStrictExpectations()
        {
            {
                mockMessage.getBytes();
                result = actualPayload;
                mockMessage.getMessageType();
                result = MessageType.DEVICE_TWIN;
                mockMessage.getDeviceOperationType();
                result = DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST;
                mockMessage.getVersion();
                result = mockVersion;
            }
        };

        //act
        testTwin.send(mockMessage);

        //assert
        new Verifications()
        {
            {
                mockMessage.getBytes();
                times = 1;
                Deencapsulation.invoke(mockMqtt, "subscribe", expectedTopic);
                times = 1;
                Deencapsulation.invoke(mockMqtt, "publish", expectedTopic, mockMessage);
                times = 0;
            }
        };

    }

    //Tests_SRS_MQTTDEVICETWIN_25_031: [send method shall publish a message to the IOT Hub on the respective publish topic by calling method publish().]
    @Test
    public void sendPublishesMessage(@Mocked final Mqtt mockMqtt, @Mocked final IotHubTransportMessage mockMessage) throws TransportException
    {
        //arrange
        final byte[] actualPayload = {0x61, 0x62, 0x63};
        MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
        testTwin.start();
        new NonStrictExpectations()
        {
            {
                mockMessage.getBytes();
                result = actualPayload;
                mockMessage.getMessageType();
                result = MessageType.DEVICE_TWIN;
                mockMessage.getDeviceOperationType();
                result = DEVICE_OPERATION_TWIN_GET_REQUEST;
                mockMessage.getVersion();
                result = mockVersion;
                mockMessage.getRequestId();
                result = "some request id";
                Deencapsulation.invoke(mockMqtt, "publish", new Class[] {String.class, Message.class}, anyString, (Message) any);
            }
        };

        //act
        testTwin.send(mockMessage);

        //assert
        new Verifications()
        {
            {
                mockMessage.getBytes();
                times = 1;
                Deencapsulation.invoke(mockMqtt, "subscribe", anyString);
                times = 0;
                Deencapsulation.invoke(mockMqtt, "publish", anyString, mockMessage);
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_MQTTDEVICETWIN_25_032: [send method shall subscribe to desired properties by calling method subscribe() on topic "$iothub/twin/PATCH/properties/desired/#" specified in spec if the operation is DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.]
     */
    @Test
    public void sendSubscribesMessageForSubscribeToDesiredPropertiesOnCorrectTopic(@Mocked final Mqtt mockMqtt, @Mocked final IotHubTransportMessage mockMessage) throws TransportException
    {
        //arrange
        final byte[] actualPayload = {0x61, 0x62, 0x63};
        final String expectedTopic = "$iothub/twin/PATCH/properties/desired/?$version="+ mockVersion;
        final String expectedSubscribeTopic = "$iothub/twin/PATCH/properties/desired/#";
        MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
        testTwin.start();
        new NonStrictExpectations()
        {
            {
                mockMessage.getMessageType();
                result = MessageType.DEVICE_TWIN;
                mockMessage.getDeviceOperationType();
                result = DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST;
                mockMessage.getVersion();
                result = mockVersion;
                mockMessage.getBytes();
                result = actualPayload;
            }
        };

        //act
        testTwin.send(mockMessage);

        //assert
        new Verifications()
        {
            {
                mockMessage.getBytes();
                times = 1;
                Deencapsulation.invoke(mockMqtt, "subscribe", expectedSubscribeTopic);
                times = 1;
                Deencapsulation.invoke(mockMqtt, "publish", expectedTopic, mockMessage);
                times = 0;
            }
        };
    }
    /*
    **Tests_SRS_MQTTDEVICETWIN_25_021: [send method shall throw an IllegalArgumentException if the message is null.]
     */
    @Test (expected = IllegalArgumentException.class)
    public void sendThrowsIllegalArgumentExceptionIfMessageIsNull(@Mocked final Mqtt mockMqtt, @Mocked final IotHubTransportMessage mockMessage) throws TransportException
    {
        final byte[] actualPayload = {0x61, 0x62, 0x63};
        final String expectedTopic = "$iothub/twin/PATCH/properties/reported/?$rid=" + mockReqId + "&$version=" + mockVersion;
        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");

            //act
            testTwin.send(null);
        }
        finally{
            //assert
            new Verifications()
            {
                {
                    mockMessage.getBytes();
                    times = 0;
                    Deencapsulation.invoke(mockMqtt, "publish", expectedTopic, actualPayload, mockMessage);
                    times = 0;
                }
            };
        }
    }
    @Test
    public void sendDoesNotThrowsIoExceptionIfMessageIsEmpty(@Mocked final Mqtt mockMqtt, @Mocked final IotHubTransportMessage mockMessage) throws TransportException
    {
        final byte[] actualPayload = {};
        final String expectedTopic = "$iothub/twin/PATCH/properties/reported/?$rid=" + mockReqId;
        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            testTwin.start();
            new NonStrictExpectations()
            {
                {
                    mockMessage.getBytes();
                    result = actualPayload;
                    mockMessage.getMessageType();
                    result = MessageType.DEVICE_TWIN;
                    mockMessage.getDeviceOperationType();
                    result = DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST;
                    mockMessage.getRequestId();
                    result = mockReqId;
                }
            };

            //act
            testTwin.send(mockMessage);
        }
        finally{
            //assert
            new Verifications()
            {
                {
                    mockMessage.getBytes();
                    times = 1;
                    Deencapsulation.invoke(mockMqtt, "publish", expectedTopic, mockMessage);
                    times = 1;
                }
            };
        }
    }

    /*
    **Tests_SRS_MQTTDEVICETWIN_25_038: [If the topic is of type response topic then this method shall parse further for status and set it for the message by calling setStatus for the message]
     */
    @Test
    public void receiveParsesResponseTopicForGetTwinSucceeds() throws TransportException
    {
        final byte[] actualPayload = "GetTwinResponseDataContainingDesiredAndReportedPropertiesDocument".getBytes();
        final String expectedTopic = "$iothub/twin/res/" + "200" + "/?$rid=" + mockReqId;
        IotHubTransportMessage receivedMessage = null;
        baseConstructorExpectation();

        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            String insertTopic = expectedTopic;
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            testAllReceivedMessages.add(new MutablePair<>(insertTopic, actualPayload));
            Deencapsulation.setField(testTwin, "allReceivedMessages", testAllReceivedMessages);
            Map<String, DeviceOperations> requestMap = new HashMap<>();
            requestMap.put(mockReqId, DEVICE_OPERATION_TWIN_GET_REQUEST);
            Deencapsulation.setField(testTwin, "requestMap", requestMap);

            //act
            receivedMessage = (IotHubTransportMessage) testTwin.receive();

        }
        finally
        {
            //assert
            assertNotNull(receivedMessage);
            assertTrue(receivedMessage.getMessageType() == MessageType.DEVICE_TWIN);
            assertTrue(receivedMessage.getDeviceOperationType() == DEVICE_OPERATION_TWIN_GET_RESPONSE);
            assertTrue(receivedMessage.getRequestId().equals(mockReqId));
            assertTrue(receivedMessage.getStatus().equals("200"));
            assertTrue(receivedMessage.getVersion() == null);
        }
    }
    @Test
    public void receiveParsesResponseTopicForUpdateReportedPropertiesSucceeds() throws TransportException
    {
        final byte[] actualPayload = "".getBytes();
        /*
            The following does not work
            final byte[] actualPayload = null;
         */
        final String expectedTopic = "$iothub/twin/res/" + "200" + "/?$rid=" + mockReqId + "&$version=" + mockVersion;
        IotHubTransportMessage receivedMessage = null;
        baseConstructorExpectation();

        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            String insertTopic = expectedTopic;
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            testAllReceivedMessages.add(new MutablePair<>(insertTopic, actualPayload));
            Deencapsulation.setField(testTwin, "allReceivedMessages", testAllReceivedMessages);

            Map<String, DeviceOperations> requestMap = new HashMap<>();
            requestMap.put(mockReqId, DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST);
            Deencapsulation.setField(testTwin, "requestMap", requestMap);

            //act
            receivedMessage = (IotHubTransportMessage) testTwin.receive();
        }
        finally
        {
            //assert
            assertNotNull(receivedMessage);
            assertTrue(receivedMessage.getMessageType() == MessageType.DEVICE_TWIN);
            assertTrue(receivedMessage.getDeviceOperationType() == DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE);
            assertTrue(receivedMessage.getStatus().equals("200"));
            assertTrue(receivedMessage.getRequestId().equals(mockReqId));
            assertTrue(receivedMessage.getVersion().equals(mockVersion));
        }
    }
    /*
    **Tests_SRS_MQTTDEVICETWIN_25_042: [If the topic is of type patch for desired properties then this method shall parse further to look for version which if found is set by calling setVersion]
     */
    @Test
    public void receiveParsesPatchTopicForDesiredPropertiesNotificationSucceeds() throws TransportException
    {
        final byte[] actualPayload = "UpdateDesiredPropertiesNotificationData".getBytes();
        final String expectedTopic = "$iothub/twin/PATCH/properties/desired/" + "?$version=" + mockVersion;
        IotHubTransportMessage receivedMessage = null;
        baseConstructorExpectation();

        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            String insertTopic = expectedTopic;
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            testAllReceivedMessages.add(new MutablePair<>(insertTopic, actualPayload));
            Deencapsulation.setField(testTwin, "allReceivedMessages", testAllReceivedMessages);

            //act
            receivedMessage = (IotHubTransportMessage) testTwin.receive();
        }
        finally
        {
            //assert
            assertNotNull(receivedMessage);
            assertTrue(receivedMessage.getMessageType() == MessageType.DEVICE_TWIN);
            assertTrue(receivedMessage.getDeviceOperationType() == DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);
            assertTrue(receivedMessage.getVersion().equals(mockVersion));
            assertTrue(receivedMessage.getRequestId() == null);
            assertTrue(receivedMessage.getStatus() == null);
        }
    }
    /*
    **SRS_MQTTDEVICETWIN_25_039: [If the topic is of type response topic and if status is either a non 3 digit number or not found then receive shall throw TransportException ]
     */
    @Test (expected = TransportException.class)
    public void receiveParsesResponseTopicMandatoryStatusNotFoundException() throws TransportException
    {
        final byte[] actualPayload = "GetTwinResponseDataContainingDesiredAndReportedPropertiesDocument".getBytes();
        final String expectedTopic = "$iothub/twin/res/" + "?$rid=" + mockReqId;
        IotHubTransportMessage receivedMessage = null;
        baseConstructorExpectation();

        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            String insertTopic = expectedTopic;
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            testAllReceivedMessages.add(new MutablePair<>(insertTopic, actualPayload));
            Deencapsulation.setField(testTwin, "allReceivedMessages", testAllReceivedMessages);

            //act
            receivedMessage = (IotHubTransportMessage) testTwin.receive();
        }
        finally
        {
            //assert
            assertNull(receivedMessage);
        }
    }
    /*
    **Tests_SRS_MQTTDEVICETWIN_25_039: [If the topic is of type response topic and if status is either a non 3 digit number or not found then receive shall throw TransportException ]
     */
    @Test (expected = TransportException.class)
    public void receiveParsesResponseTopicInvalidStatusThrowsException() throws TransportException
    {
        final byte[] actualPayload = "GetTwinResponseDataContainingDesiredAndReportedPropertiesDocument".getBytes();
        final String expectedTopic = "$iothub/twin/res/" + "abc/" + "?$rid=" + mockReqId;
        IotHubTransportMessage receivedMessage = null;
        baseConstructorExpectation();
        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            String insertTopic = expectedTopic;
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            testAllReceivedMessages.add(new MutablePair<>(insertTopic, actualPayload));
            Deencapsulation.setField(testTwin, "allReceivedMessages", testAllReceivedMessages);
            Deencapsulation.setField(testTwin, "mqttLock", new Object());

            //act
            receivedMessage = (IotHubTransportMessage) testTwin.receive();
        }
        finally
        {
            //assert
            assertNull(receivedMessage);
        }
    }

    /*
    **Tests_SRS_MQTTDEVICETWIN_25_040: [If the topic is of type response topic then this method shall parse further to look for request id which if found is set by calling setRequestId]
     */
    @Test
    public void receiveSetsReqIdOnResTopic() throws TransportException
    {
        final byte[] actualPayload = "GetTwinResponseDataContainingDesiredAndReportedPropertiesDocument".getBytes();
        final String expectedTopic = "$iothub/twin/res/" + "200" + "/?$rid=" + mockReqId;
        IotHubTransportMessage receivedMessage = null;
        baseConstructorExpectation();
        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            String insertTopic = expectedTopic;
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            testAllReceivedMessages.add(new MutablePair<>(insertTopic, actualPayload));
            Deencapsulation.setField(testTwin, "allReceivedMessages", testAllReceivedMessages);
            Deencapsulation.setField(testTwin, "mqttLock", new Object());


            Map<String, DeviceOperations> requestMap = new HashMap<>();
            requestMap.put(mockReqId, DEVICE_OPERATION_TWIN_GET_REQUEST);
            Deencapsulation.setField(testTwin, "requestMap", requestMap);

            //act
            receivedMessage = (IotHubTransportMessage) testTwin.receive();

        }
        finally
        {
            //assert
            assertNotNull(receivedMessage);
            assertTrue(receivedMessage.getMessageType() == MessageType.DEVICE_TWIN);
            assertTrue(receivedMessage.getDeviceOperationType() == DEVICE_OPERATION_TWIN_GET_RESPONSE);
            assertTrue(receivedMessage.getRequestId().equals(mockReqId));
            assertTrue(receivedMessage.getStatus().equals("200"));
            assertTrue(receivedMessage.getVersion() == null);
        }
    }

    @Test
    public void receiveDoesNotSetReqIdOnResTopicIfNotFound() throws TransportException
    {
        final byte[] actualPayload = "GetTwinResponseDataContainingDesiredAndReportedPropertiesDocument".getBytes();
        final String expectedTopic = "$iothub/twin/res/" + "200";
        IotHubTransportMessage receivedMessage = null;
        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            String insertTopic = expectedTopic;
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            testAllReceivedMessages.add(new MutablePair<>(insertTopic, actualPayload));
            Deencapsulation.setField(testTwin, "allReceivedMessages", testAllReceivedMessages);

            Map<String, DeviceOperations> requestMap = new HashMap<>();
            requestMap.put(mockReqId, DEVICE_OPERATION_TWIN_GET_REQUEST);
            Deencapsulation.setField(testTwin, "requestMap", requestMap);
            Deencapsulation.setField(testTwin, "mqttLock", new Object());

            //act
            receivedMessage = (IotHubTransportMessage) testTwin.receive();
        }
        finally
        {
            //assert
            assertNotNull(receivedMessage);
            assertTrue(receivedMessage.getMessageType() == MessageType.DEVICE_TWIN);
            assertTrue(receivedMessage.getDeviceOperationType() == DEVICE_OPERATION_UNKNOWN);
            assertTrue(receivedMessage.getRequestId() == null);
            assertTrue(receivedMessage.getStatus().equals("200"));
            assertTrue(receivedMessage.getVersion() == null);
        }
    }

    /*
    **Tests_SRS_MQTTDEVICETWIN_25_041: [If the topic is of type response topic then this method shall parse further to look for version which if found is set by calling setVersion]
     */
    @Test
    public void receiveSetsVersionOnResTopic() throws TransportException
    {
        final byte[] actualPayload = "GetTwinResponseDataContainingDesiredAndReportedPropertiesDocument".getBytes();
        final String expectedTopic = "$iothub/twin/res/" + "201" + "/?$rid=" + mockReqId + "&$version=" + mockVersion;
        IotHubTransportMessage receivedMessage = null;
        baseConstructorExpectation();
        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            String insertTopic = expectedTopic;
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            testAllReceivedMessages.add(new MutablePair<>(insertTopic, actualPayload));
            Deencapsulation.setField(testTwin, "allReceivedMessages", testAllReceivedMessages);

            Map<String, DeviceOperations> requestMap = new HashMap<>();
            requestMap.put(mockReqId, DEVICE_OPERATION_TWIN_GET_REQUEST);
            Deencapsulation.setField(testTwin, "requestMap", requestMap);

            //act
            receivedMessage = (IotHubTransportMessage) testTwin.receive();

        }
        finally
        {
            //assert
            assertNotNull(receivedMessage);
            assertTrue(receivedMessage.getMessageType() == MessageType.DEVICE_TWIN);
            assertTrue(receivedMessage.getDeviceOperationType() == DEVICE_OPERATION_TWIN_GET_RESPONSE);
            assertTrue(receivedMessage.getRequestId().equals(mockReqId));
            assertTrue(receivedMessage.getStatus().equals("201"));
            assertTrue(receivedMessage.getVersion().equals(mockVersion));
        }

    }

    /*
    **Tests_SRS_MQTTDEVICETWIN_25_041: [If the topic is of type response topic then this method shall parse further to look for version which if found is set by calling setVersion]
     */
    @Test
    public void receiveDoesNotSetVersionOnResTopicIfNotFound() throws TransportException
    {
        final byte[] actualPayload = "GetTwinResponseDataContainingDesiredAndReportedPropertiesDocument".getBytes();
        final String expectedTopic = "$iothub/twin/res/" + "201" + "/?$rid=" + mockReqId;
        IotHubTransportMessage receivedMessage = null;
        baseConstructorExpectation();
        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            String insertTopic = expectedTopic;
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            testAllReceivedMessages.add(new MutablePair<>(insertTopic, actualPayload));
            Deencapsulation.setField(testTwin, "allReceivedMessages", testAllReceivedMessages);
            Map<String, DeviceOperations> requestMap = new HashMap<>();
            requestMap.put(mockReqId, DEVICE_OPERATION_TWIN_GET_REQUEST);
            Deencapsulation.setField(testTwin, "requestMap", requestMap);

            //act
            receivedMessage = (IotHubTransportMessage) testTwin.receive();
        }
        finally
        {
            //assert
            assertNotNull(receivedMessage);
            assertTrue(receivedMessage.getMessageType() == MessageType.DEVICE_TWIN);
            assertTrue(receivedMessage.getDeviceOperationType() == DEVICE_OPERATION_TWIN_GET_RESPONSE);
            assertTrue(receivedMessage.getRequestId().equals(mockReqId));
            assertTrue(receivedMessage.getStatus().equals("201"));
            assertTrue(receivedMessage.getVersion() == null);
        }
    }

    /*
    **Tests_SRS_MQTTDEVICETWIN_25_044: [If the topic is of type response then this method shall set data and operation type as DEVICE_OPERATION_TWIN_GET_RESPONSE if data is not null]
     */
    @Test
    public void receiveSetsDataForGetTwinResp() throws TransportException
    {
        final byte[] actualPayload = "GetTwinResponseDataContainingDesiredAndReportedPropertiesDocument".getBytes();
        final String expectedTopic = "$iothub/twin/res/" + "200" + "/?$rid=" + mockReqId;
        IotHubTransportMessage receivedMessage = null;
        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            String insertTopic = expectedTopic;
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            testAllReceivedMessages.add(new MutablePair<>(insertTopic, actualPayload));
            Deencapsulation.setField(testTwin, "allReceivedMessages", testAllReceivedMessages);
            Deencapsulation.setField(testTwin, "mqttLock", new Object());

            Map<String, DeviceOperations> requestMap = new HashMap<>();
            requestMap.put(mockReqId, DEVICE_OPERATION_TWIN_GET_REQUEST);
            Deencapsulation.setField(testTwin, "requestMap", requestMap);

            //act
            receivedMessage = (IotHubTransportMessage) testTwin.receive();
        }
        finally
        {
            //assert
            assertNotNull(receivedMessage);
            assertTrue(receivedMessage.getMessageType() == MessageType.DEVICE_TWIN);
            assertTrue(receivedMessage.getDeviceOperationType() == DEVICE_OPERATION_TWIN_GET_RESPONSE);
            assertTrue(receivedMessage.getRequestId().equals(mockReqId));
            assertTrue(receivedMessage.getStatus().equals("200"));
            assertTrue(receivedMessage.getVersion() == null);

            byte[] receivedMessageBytes = receivedMessage.getBytes();
            assertTrue(receivedMessageBytes.length == actualPayload.length);
            for (int i = 0; i < receivedMessageBytes.length; i++)
            {
                assertTrue(receivedMessageBytes[i] == actualPayload[i]);
            }

        }
    }

    /*
    ** Tests_SRS_MQTTDEVICETWIN_25_045: [If the topic is of type response then this method shall set empty data and operation type as DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE if data is null or empty]
     */
    @Test
    public void receiveDoesNotSetDataForUpdateReportedPropResp() throws TransportException
    {
        final byte[] actualPayload = "".getBytes();
        /*
            The following does not work
            final byte[] actualPayload = null;
         */
        final String expectedTopic = "$iothub/twin/res/" + "200" + "/?$rid=" + mockReqId + "&$version=" + mockVersion;

        IotHubTransportMessage receivedMessage = null;
        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            String insertTopic = expectedTopic;
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            testAllReceivedMessages.add(new MutablePair<>(insertTopic, actualPayload));
            Deencapsulation.setField(testTwin, "allReceivedMessages", testAllReceivedMessages);

            Map<String, DeviceOperations> requestMap = new HashMap<>();
            requestMap.put(mockReqId, DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST);
            Deencapsulation.setField(testTwin, "requestMap", requestMap);
            Deencapsulation.setField(testTwin, "mqttLock", new Object());

            //act
            receivedMessage = (IotHubTransportMessage) testTwin.receive();
        }
        finally
        {
            //assert
            assertNotNull(receivedMessage);
            assertTrue(receivedMessage.getMessageType() == MessageType.DEVICE_TWIN);
            assertTrue(receivedMessage.getDeviceOperationType() == DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE);
            assertTrue(receivedMessage.getRequestId().equals(mockReqId));
            assertTrue(receivedMessage.getStatus().equals("200"));
            assertTrue(receivedMessage.getVersion().equals(mockVersion));

            byte[] receivedMessageBytes = receivedMessage.getBytes();
            assertTrue(receivedMessageBytes.length == 0);
        }
    }

    /*
    **Tests_SRS_MQTTDEVICETWIN_25_046: [If the topic is of type patch for desired properties then this method shall set the data and operation type as DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE if data is not null or empty]
     */
    @Test
    public void receiveSetsDataForDesiredPropNotifResp() throws TransportException
    {
        final byte[] actualPayload = "NotificationResponseDataContainingDesiredPropertiesDocument".getBytes();
        final String expectedTopic = "$iothub/twin/PATCH/properties/desired/";
        IotHubTransportMessage receivedMessage = null;
        baseConstructorExpectation();
        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            String insertTopic = expectedTopic;
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            testAllReceivedMessages.add(new MutablePair<>(insertTopic, actualPayload));
            Deencapsulation.setField(testTwin, "allReceivedMessages", testAllReceivedMessages);

            //act
            receivedMessage = (IotHubTransportMessage) testTwin.receive();

        }
        finally
        {
            //assert
            assertNotNull(receivedMessage);
            assertTrue(receivedMessage.getMessageType() == MessageType.DEVICE_TWIN);
            assertTrue(receivedMessage.getDeviceOperationType() == DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);
            assertTrue(receivedMessage.getRequestId() == null);
            assertTrue(receivedMessage.getStatus() == null);
            assertTrue(receivedMessage.getVersion() == null);

            byte[] receivedMessageBytes = receivedMessage.getBytes();
            assertTrue(receivedMessageBytes.length == actualPayload.length);
            for (int i = 0; i < receivedMessageBytes.length; i++)
            {
                assertTrue(receivedMessageBytes[i] == actualPayload[i]);
            }
        }
    }

    /*
    **Tests_SRS_MQTTDEVICETWIN_25_042: [If the topic is of type patch for desired properties then this method shall parse further to look for version which if found is set by calling setVersion]
     */
    @Test
    public void receiveDoesNotSetVersionForDesiredPropNotifRespIfNotFound() throws TransportException
    {
        final byte[] actualPayload = "NotificationResponseDataContainingDesiredPropertiesDocument".getBytes();
        final String expectedTopic = "$iothub/twin/PATCH/properties/desired/";
        IotHubTransportMessage receivedMessage = null;
        baseConstructorExpectation();
        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            String insertTopic = expectedTopic;
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            testAllReceivedMessages.add(new MutablePair<>(insertTopic, actualPayload));
            Deencapsulation.setField(testTwin, "allReceivedMessages", testAllReceivedMessages);

            //act
            receivedMessage = (IotHubTransportMessage) testTwin.receive();
        }
        finally
        {
            //assert
            assertNotNull(receivedMessage);
            assertTrue(receivedMessage.getMessageType() == MessageType.DEVICE_TWIN);
            assertTrue(receivedMessage.getDeviceOperationType() == DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);
            assertTrue(receivedMessage.getRequestId() == null);
            assertTrue(receivedMessage.getStatus() == null);
            assertTrue(receivedMessage.getVersion() == null);

            byte[] receivedMessageBytes = receivedMessage.getBytes();
            assertTrue(receivedMessageBytes.length == actualPayload.length);
            for (int i = 0; i < receivedMessageBytes.length; i++)
            {
                assertTrue(receivedMessageBytes[i] == actualPayload[i]);
            }
        }
    }

    /*
    **Tests_SRS_MQTTDEVICETWIN_25_042: [If the topic is of type patch for desired properties then this method shall parse further to look for version which if found is set by calling setVersion]
     */
    @Test
    public void receiveSetVersionForDesiredPropNotifRespIfFound() throws TransportException
    {
        final byte[] actualPayload = "NotificationResponseDataContainingDesiredPropertiesDocument".getBytes();
        final String expectedTopic = "$iothub/twin/PATCH/properties/desired/" + "?$version=" + mockVersion ;
        IotHubTransportMessage receivedMessage = null;
        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            String insertTopic = expectedTopic;
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            testAllReceivedMessages.add(new MutablePair<>(insertTopic, actualPayload));
            Deencapsulation.setField(testTwin, "allReceivedMessages", testAllReceivedMessages);
            Deencapsulation.setField(testTwin, "mqttLock", new Object());

            //act
            receivedMessage = (IotHubTransportMessage) testTwin.receive();
        }
        finally
        {
            //assert
            assertNotNull(receivedMessage);
            assertTrue(receivedMessage.getMessageType() == MessageType.DEVICE_TWIN);
            assertTrue(receivedMessage.getDeviceOperationType() == DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);
            assertTrue(receivedMessage.getRequestId() == null);
            assertTrue(receivedMessage.getStatus() == null);
            assertTrue(receivedMessage.getVersion().equals(mockVersion));

            byte[] receivedMessageBytes = receivedMessage.getBytes();
            assertTrue(receivedMessageBytes.length == actualPayload.length);
            for (int i = 0; i < receivedMessageBytes.length; i++)
            {
                assertTrue(receivedMessageBytes[i] == actualPayload[i]);
            }
        }
    }

    /*
    **Tests_SRS_MQTTDEVICETWIN_25_043: [If the topic is not of type response for desired properties then this method shall throw TransportException]
     */
    @Test (expected = TransportException.class)
    public void receiveThrowsTransportExceptionOnAnythingOtherThenPatchDesiredProp() throws TransportException
    {
        final byte[] actualPayload = "NotificationResponseDataContainingDesiredPropertiesDocument".getBytes();
        final String expectedTopic = "$iothub/twin/PATCH/properties/" + "?$version=" + mockVersion ;
        IotHubTransportMessage receivedMessage = null;
        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            String insertTopic = expectedTopic;
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            testAllReceivedMessages.add(new MutablePair<>(insertTopic, actualPayload));
            Deencapsulation.setField(testTwin, "allReceivedMessages", testAllReceivedMessages);
            Deencapsulation.setField(testTwin, "mqttLock", new Object());

            //act
            receivedMessage = (IotHubTransportMessage) testTwin.receive();
        }
        finally
        {
            //assert
            assertNull(receivedMessage);
        }
    }

    /*
    **Tests_SRS_MQTTDEVICETWIN_25_037: [This method shall parse topic to look for only either twin response topic or twin patch topic and thorw TransportException other wise.]
     */
    @Test (expected = TransportException.class)
    public void receiveThrowsTransportExceptionOnAnythingOtherThenPatchOrResTopic() throws TransportException
    {
        final byte[] actualPayload = "NotificationResponseDataContainingDesiredPropertiesDocument".getBytes();
        final String expectedTopic = "$iothub/twin/NOTPATCH_NOTRES/properties/" + "?$version=" + mockVersion ;
        IotHubTransportMessage receivedMessage = null;
        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            String insertTopic = expectedTopic;
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            testAllReceivedMessages.add(new MutablePair<>(insertTopic, actualPayload));
            Deencapsulation.setField(testTwin, "allReceivedMessages", testAllReceivedMessages);
            Deencapsulation.setField(testTwin, "mqttLock", new Object());

            //act
            receivedMessage = (IotHubTransportMessage) testTwin.receive();
        }
        finally
        {
            //assert
            assertNull(receivedMessage);
        }
    }

    /*
     * Tests_SRS_MQTTDEVICETWIN_34_034: [If the call peekMessage returns null or empty string then this method shall do nothing and return null]
     */
    @Test
    public void receiveReturnsNullMessageIfTopicNotFound(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        //can't be initialized to null, so set it as a default message
        IotHubTransportMessage receivedMessage = new IotHubTransportMessage(new byte[] {1}, MessageType.DEVICE_TWIN);
        try
        {
            //arrange
            MqttDeviceTwin testTwin = new MqttDeviceTwin(mockedMqttConnection, "");
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            Deencapsulation.setField(mockMqtt, "allReceivedMessages", testAllReceivedMessages);
            Deencapsulation.setField(testTwin, "mqttLock", new Object());

            //act
            receivedMessage = (IotHubTransportMessage) testTwin.receive();
        }
        finally
        {
            //assert
            assertNull(receivedMessage);
        }
    }
}