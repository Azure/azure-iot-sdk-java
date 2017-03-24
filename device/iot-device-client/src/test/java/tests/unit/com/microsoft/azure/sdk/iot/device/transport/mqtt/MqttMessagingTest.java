// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.


package tests.unit.com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.Message;

import com.microsoft.azure.sdk.iot.device.MessageProperty;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.Mqtt;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttMessaging;
import mockit.*;

import org.junit.Test;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.*;

/* Unit tests for Mqtt Messaging */
public class MqttMessagingTest {

    final String serverUri = "test.host.name";
    final String clientId = "test.iothub";
    final String userName = "test-deviceId";
    final String password = "test-devicekey?&test";
    final String mockParseTopic = "testTopic";

    @Mocked
    IOException mockIOException;

    @Mocked
    Message mockMessage;

    /*
    **Tests_SRS_MqttMessaging_25_002: [**The constructor shall use the configuration to instantiate super class and passing the parameters.**]**
    */
    /*
    **Tests_SRS_MqttMessaging_25_003: [**The constructor construct publishTopic and subscribeTopic from deviceId.**]**
    */
    @Test
    public void constructorCallsBaseConstructorWithArguments(@Mocked final Mqtt mockMqtt) throws IOException
    {

        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);

        String actualPublishTopic = Deencapsulation.getField(testMqttMessaging, "publishTopic");
        assertNotNull(actualPublishTopic);
        String actualSubscribeTopic = Deencapsulation.getField(testMqttMessaging, "subscribeTopic");
        assertNotNull(actualSubscribeTopic);
        String actualParseTopic = Deencapsulation.getField(testMqttMessaging, "parseTopic");
        assertNotNull(actualParseTopic);

    }

    /*
    **Tests_SRS_MqttMessaging_25_001: [**The constructor shall throw InvalidParameter Exception if any of the parameters are null or empty .**]**
     */
    @Test (expected = InvalidParameterException.class)
    public void constructorFailsIfAnyOfTheParametersAreNull() throws IOException
    {

        MqttMessaging testMqttMessaging = new MqttMessaging(null, clientId, userName, password);

    }

    /*
    **Tests_SRS_MqttMessaging_25_001: [**The constructor shall throw InvalidParameter Exception if any of the parameters are null or empty .**]**
     */

    @Test (expected = InvalidParameterException.class)
    public void constructorFailsIfAnyOfTheParametersAreEmpty() throws IOException
    {

        MqttMessaging testMqttMessaging = new MqttMessaging("", clientId, userName, password);

    }

    /*
    **Tests_SRS_MqttMessaging_25_020: [**start method shall be call connect to establish a connection to IOT Hub with the given configuration.**]**

    **Tests_SRS_MqttMessaging_25_021: [**start method shall subscribe to messaging subscribe topic once connected.**]**
     */
    @Test
    public  void startCallsConnectAndSubscribe(@Mocked final Mqtt mockMqtt) throws IOException
    {

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockMqtt, "connect");
                Deencapsulation.invoke(mockMqtt, "subscribe", anyString);
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);

        testMqttMessaging.start();
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockMqtt, "connect");
                times = 1;
                Deencapsulation.invoke(mockMqtt, "subscribe", anyString);
                times = 1;
            }
        };

    }

    @Test (expected = IOException.class)
    public void startThrowsIoExceptionIfConnectFails(@Mocked final Mqtt mockMqtt) throws IOException
    {
        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(mockMqtt, "connect");
                result = mockIOException;
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);
        testMqttMessaging.start();

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockMqtt, "connect");
                times = 1;
                Deencapsulation.invoke(mockMqtt, "subscribe", anyString);
                times = 0;

            }
        };

    }

    @Test (expected = IOException.class)
    public void startThrowsIoExceptionIfSubscribeFails(@Mocked final Mqtt mockMqtt) throws IOException
    {
        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(mockMqtt, "connect");
                Deencapsulation.invoke(mockMqtt, "subscribe", anyString);
                result = mockIOException;
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);
        testMqttMessaging.start();

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockMqtt, "connect");
                times = 1;
                Deencapsulation.invoke(mockMqtt, "subscribe", anyString);
                times = 1;

            }
        };

    }

    /*
    **Tests_SRS_MqttMessaging_25_022: [**stop method shall be call disconnect to tear down a connection to IOT Hub with the given configuration.**]**

    **Tests_SRS_MqttMessaging_25_023: [**stop method shall be call restartBaseMqtt to tear down a the base class even if disconnect fails.**]**
     */
    @Test
    public void stopCallsDisconnectAndRestartBase(@Mocked final Mqtt mockMqtt) throws IOException
    {
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockMqtt, "disconnect");
                mockMqtt.restartBaseMqtt();
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);
        testMqttMessaging.start();
        testMqttMessaging.stop();

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockMqtt, "disconnect");
                times = 1;
                mockMqtt.restartBaseMqtt();
                times = 1;

            }
        };

    }

    @Test (expected = IOException.class)
    public void stopCallsRestartBaseEvenIfDisconnectFailsAndThrowsIOException(@Mocked final Mqtt mockMqtt) throws IOException
    {
        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(mockMqtt, "connect");
                Deencapsulation.invoke(mockMqtt, "subscribe", anyString);
                Deencapsulation.invoke(mockMqtt, "disconnect");
                result = mockIOException;
                mockMqtt.restartBaseMqtt();
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);
        testMqttMessaging.start();
        testMqttMessaging.stop();

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockMqtt, "disconnect");
                times = 1;
                mockMqtt.restartBaseMqtt();
                times = 1;

            }
        };

    }

    /*
    **Tests_SRS_MqttMessaging_25_005: [**parseTopic shall look for the subscribe topic prefix from received message queue.**]**
     */
    @Test
    public void parseTopicLooksForNextAvailableMessagesForDeviceMessagingTopic(@Mocked final Mqtt mockMqtt) throws IOException
    {
        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);

        String insertTopic = "devices/" + clientId + "/messages/devicebound/abc";
        ConcurrentSkipListMap<String, byte[]> testMap = new ConcurrentSkipListMap<String, byte[]>();
        testMap.put(insertTopic, "DataData".getBytes());
        Deencapsulation.setField(mockMqtt, "allReceivedMessages", testMap);

        String retrieveTopic = Deencapsulation.invoke(testMqttMessaging, "parseTopic");

        assertEquals(retrieveTopic.length(),insertTopic.length());
        for (int i = 0 ; i < retrieveTopic.length(); i++)
        {
            assertEquals(retrieveTopic.charAt(i), insertTopic.charAt(i));
        }
    }

    /*
    **Tests_SRS_MqttMessaging_25_007: [**If received messages queue is empty then parseTopic shall return null string.**]**
     */
    @Test
    public void parseTopicReturnsNullIfQueueIsEmpty(@Mocked final Mqtt mockMqtt) throws IOException
    {
        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);

        ConcurrentSkipListMap<String, byte[]> testMap = new ConcurrentSkipListMap<String, byte[]>();

        Deencapsulation.setField(mockMqtt, "allReceivedMessages", testMap);

        String retrieveTopic = Deencapsulation.invoke(testMqttMessaging, "parseTopic");

        assertNull(retrieveTopic);
    }

    /*
    **Tests_SRS_MqttMessaging_25_006: [**If none of the topics from the received queue match the subscribe topic prefix then this method shall return null string .**]**
     */
    @Test
    public void parseTopicReturnsNullIfNoMessageMatchingKeyIsFound(@Mocked final Mqtt mockMqtt) throws IOException
    {
        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);

        String insertTopic = "devices/" + clientId + "/fakemessages/devicebound/abc";
        ConcurrentSkipListMap<String, byte[]> testMap = new ConcurrentSkipListMap<String, byte[]>();
        testMap.put(insertTopic, "DataData".getBytes());
        Deencapsulation.setField(mockMqtt, "allReceivedMessages", testMap);

        String retrieveTopic = Deencapsulation.invoke(testMqttMessaging, "parseTopic");

        assertNull(retrieveTopic);

    }

    /*
    **Tests_SRS_MqttMessaging_25_010: [**This parsePayload method look for payload for the corresponding topic from the received messagesqueue.**]**
     */
    @Test
    public void parsePayloadLooksForValueWithGivenKeyTopic(@Mocked final Mqtt mockMqtt) throws IOException
    {
        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);

        final String insertTopic = "devices/" + clientId + "/messages/devicebound/abc";
        final byte[] insertMessage = {0x61, 0x62, 0x63};
        ConcurrentSkipListMap<String, byte[]> testMap = new ConcurrentSkipListMap<String, byte[]>();
        testMap.put(insertTopic, insertMessage);
        Deencapsulation.setField(mockMqtt, "allReceivedMessages", testMap);

        byte[] retrieveMessage = Deencapsulation.invoke(testMqttMessaging, "parsePayload", insertTopic);

        assertEquals(insertMessage.length, retrieveMessage.length);
        for (int i = 0 ; i < retrieveMessage.length; i++)
        {
            assertEquals(retrieveMessage[i], insertMessage[i]);
        }

    }

    /*
    **Tests_SRS_MqttMessaging_25_014: [**If the topic is found in the message queue then parsePayload shall delete it from the queue.**]**
     */
    @Test
    public void parsePayloadRemovesTheKeyValuePairFromQueueIfFound(@Mocked final Mqtt mockMqtt) throws IOException
    {
        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);

        final String insertTopic = "devices/" + clientId + "/messages/devicebound/abc";
        final byte[] insertMessage = {0x61, 0x62, 0x63};
        ConcurrentSkipListMap<String, byte[]> testMap = new ConcurrentSkipListMap<String, byte[]>();
        testMap.put(insertTopic, insertMessage);
        Deencapsulation.setField(mockMqtt, "allReceivedMessages", testMap);

        byte[] retrieveMessage = Deencapsulation.invoke(testMqttMessaging, "parsePayload", insertTopic);

        assertTrue(testMap.isEmpty());

    }

    @Test (expected = IOException.class)
    public void parsePayloadShallThrowIOExceptionIfQueueIsEmpty(@Mocked final Mqtt mockMqtt) throws IOException
    {
        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);

        final String insertTopic = "devices/" + clientId + "/messages/devicebound/abc";
        final byte[] insertMessage = {0x61, 0x62, 0x63};
        ConcurrentSkipListMap<String, byte[]> testMap = new ConcurrentSkipListMap<String, byte[]>();

        Deencapsulation.setField(mockMqtt, "allReceivedMessages", testMap);

        byte[] retrieveMessage = Deencapsulation.invoke(testMqttMessaging, "parsePayload", insertTopic);
        assertNull(retrieveMessage);

    }

    /*
    **Tests_SRS_MqttMessaging_25_011: [**If the topic is null then parsePayload shall stop parsing for payload and return.**]**
     */
    @Test
    public void parsePayloadShallReturnNullIfTopicIsNull(@Mocked final Mqtt mockMqtt) throws IOException
    {
        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);

        final String insertTopic_messaging = "devices/" + clientId + "/messages/devicebound/abc";
        final byte[] insertMessage = {0x61, 0x62, 0x63};
        ConcurrentSkipListMap<String, byte[]> testMap = new ConcurrentSkipListMap<String, byte[]>();
        testMap.put(insertTopic_messaging, insertMessage);
        Deencapsulation.setField(mockMqtt, "allReceivedMessages", testMap);


        byte[] retrieveMessage = Deencapsulation.invoke(testMqttMessaging, "parsePayload", String.class);
        assertNull(retrieveMessage);
    }

    /*
    **Tests_SRS_MqttMessaging_25_012: [**If the topic is non-null and received messagesqueue could not locate the payload then this method shall throw IOException**]**
     */
    @Test (expected =  IOException.class)
    public void parsePayloadShallThrowIOExceptionIfTopicIsNotFound(@Mocked final Mqtt mockMqtt) throws IOException
    {
        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);

        final String insertTopic_actual = "$iothub/twin/PATCH/properties/desired/#";
        final String insertTopic_messaging = "devices/" + clientId + "/messages/devicebound/abc";
        final byte[] insertMessage = {0x61, 0x62, 0x63};
        ConcurrentSkipListMap<String, byte[]> testMap = new ConcurrentSkipListMap<String, byte[]>();
        testMap.put(insertTopic_actual, insertMessage);
        Deencapsulation.setField(mockMqtt, "allReceivedMessages", testMap);


        byte[] retrieveMessage = Deencapsulation.invoke(testMqttMessaging, "parsePayload", insertTopic_messaging);

    }

    /*
    **Tests_SRS_MqttMessaging_25_013: [**If receiveMessage queue is null then this method shall throw IOException.**]**
     */
    @Test (expected =  IOException.class)
    public void parsePayloadShallThrowIOExceptionIfQueueIsNull(@Mocked final Mqtt mockMqtt) throws IOException
    {
        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);

        final String insertTopic = "$iothub/twin/PATCH/properties/desired/#";
        ConcurrentSkipListMap<String, byte[]> testMap = null;

        Deencapsulation.setField(mockMqtt, "allReceivedMessages", testMap);


        byte[] retrieveMessage = Deencapsulation.invoke(testMqttMessaging, "parsePayload", insertTopic);

    }

    /*
    **Tests_SRS_MqttMessaging_25_024: [**send method shall publish a message to the IOT Hub on the publish topic by calling method publish().**]**
     */
    @Test
    public void sendShallMessageToLowerLayer(@Mocked final Mqtt mockMqtt) throws IOException
    {
        final byte[] messageBody = {0x61, 0x62, 0x63};
        new NonStrictExpectations()
        {
            {
                mockMessage.getBytes();
                result = messageBody;
                Deencapsulation.invoke(mockMqtt, "publish", anyString, messageBody);
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);
        testMqttMessaging.send(mockMessage);

        new Verifications()
        {
            {
                mockMessage.getBytes();
                times = 2;
                Deencapsulation.invoke(mockMqtt, "publish", anyString, messageBody);
                times = 1;

            }
        };

    }

    @Test (expected =  IOException.class)
    public void sendShallThrowIOExceptionIfMessageIsEmpty(@Mocked final Mqtt mockMqtt) throws IOException
    {
        final byte[] messageBody = {};
        new NonStrictExpectations()
        {
            {
                mockMessage.getBytes();
                result = messageBody;
                Deencapsulation.invoke(mockMqtt, "publish", anyString, messageBody);
                result = mockIOException;

            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);
        testMqttMessaging.send(null);

        new Verifications()
        {
            {
                mockMessage.getBytes();
                times = 1;
                Deencapsulation.invoke(mockMqtt, "publish", mockParseTopic,  new byte[1]);
                times = 1;

            }
        };

    }

    /*
    **Tests_SRS_MqttMessaging_25_025: [**send method shall throw an exception if the message is null.**]**
     */
    @Test (expected = IOException.class)
    public void sendShallThrowIOExceptionIfMessageIsNull(@Mocked final Mqtt mockMqtt) throws IOException
    {

        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);
        testMqttMessaging.send(null);

        new Verifications()
        {
            {
                mockMessage.getBytes();
                times = 0;
                Deencapsulation.invoke(mockMqtt, "publish", mockParseTopic,  new byte[1]);
                times = 0;

            }
        };

    }

    /*
     **Tests_SRS_MqttMessaging_25_026: [**send method shall append the message properties to publishTopic before publishing.**]**
     */
    @Test
    public void sendShallMessageWithPropertiesToLowerLayer(@Mocked final Mqtt mockMqtt) throws IOException
    {
        final byte[] messageBody = {0x61, 0x62, 0x63};
        final String propertyName = "key";
        final String propertyValue = "value";
        final MessageProperty[] messageProperties = new MessageProperty[]
                {
                        new MessageProperty(propertyName, propertyValue)
                };
        new NonStrictExpectations()
        {
            {
                mockMessage.getBytes();
                result = messageBody;
                mockMessage.getProperties();
                result = messageProperties;
                Deencapsulation.invoke(mockMqtt, "publish", anyString, messageBody);
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);
        testMqttMessaging.send(mockMessage);
        final String publishTopicWithProperties = String.format(
                "devices/%s/messages/events/%s=%s", clientId, propertyName, propertyValue);

        new Verifications()
        {
            {
                mockMessage.getBytes();
                times = 2;
                mockMessage.getProperties();
                Deencapsulation.invoke(mockMqtt, "publish", publishTopicWithProperties,  messageBody);
                times = 1;
            }
        };
    }

    /*
     **Tests_SRS_MqttMessaging_21_027: [**send method shall append the messageid to publishTopic before publishing using the key name `$.mid`.**]**
     */
    @Test
    public void sendShallMessageWithPropsAndMessageIdToLowerLayer(@Mocked final Mqtt mockMqtt) throws IOException
    {
        final byte[] messageBody = {0x61, 0x62, 0x63};
        final String propertyName = "key";
        final String propertyValue = "value";
        final MessageProperty[] messageProperties = new MessageProperty[]
                {
                        new MessageProperty(propertyName, propertyValue)
                };
        final String messageidValue = "test-message-id";
        new NonStrictExpectations()
        {
            {
                mockMessage.getBytes();
                result = messageBody;
                mockMessage.getProperties();
                result = messageProperties;
                mockMessage.getMessageId();
                result = messageidValue;
                Deencapsulation.invoke(mockMqtt, "publish", anyString, messageBody);
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(serverUri, clientId, userName, password);
        testMqttMessaging.send(mockMessage);
        final String publishTopicWithProperties = String.format(
                "devices/%s/messages/events/%s=%s&$.mid=%s", clientId, propertyName, propertyValue,messageidValue);

        new Verifications()
        {
            {
                mockMessage.getBytes();
                times = 2;
                mockMessage.getProperties();
                Deencapsulation.invoke(mockMqtt, "publish", publishTopicWithProperties, messageBody);
                times = 1;
                mockMessage.getMessageId();
                times = 2;
            }
        };
    }

}
