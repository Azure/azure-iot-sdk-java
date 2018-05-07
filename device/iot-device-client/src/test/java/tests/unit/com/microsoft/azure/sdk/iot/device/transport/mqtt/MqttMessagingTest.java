// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.


package tests.unit.com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubListener;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.Mqtt;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttConnection;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttMessaging;
import mockit.*;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/*
 * Unit tests for MqttMessaging.java
 * Code coverage: 100% methods, 100% lines
 */
public class MqttMessagingTest
{
    private static final String CLIENT_ID = "test.iothub";
    private static final String MOCK_PARSE_TOPIC = "testTopic";

    @Mocked
    private IOException mockedIOException;

    @Mocked
    private Message mockedMessage;

    @Mocked
    private MqttConnection mockedMqttConnection;

    @Mocked
    private IotHubListener mockedIotHubListener;

    //Tests_SRS_MqttMessaging_25_002: [The constructor shall use the configuration to instantiate super class and passing the parameters.]
    //Tests_SRS_MqttMessaging_25_003: [The constructor construct publishTopic and subscribeTopic from deviceId.]
    //Tests_SRS_MqttMessaging_25_004: [The constructor shall save the provided listener.]
    @Test
    public void constructorCallsBaseConstructorWithArguments(@Mocked final Mqtt mockMqtt) throws TransportException
    {

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "");

        String actualPublishTopic = Deencapsulation.getField(testMqttMessaging, "publishTopic");
        assertNotNull(actualPublishTopic);
        String actualSubscribeTopic = Deencapsulation.getField(testMqttMessaging, "subscribeTopic");
        assertNotNull(actualSubscribeTopic);
        String actualParseTopic = Deencapsulation.getField(testMqttMessaging, "parseTopic");
        assertNotNull(actualParseTopic);
    }

    /*
    **Tests_SRS_MqttMessaging_25_001: [The constructor shall throw IllegalArgumentException if any of the parameters are null or empty .]
     */
    @Test (expected = IllegalArgumentException.class)
    public void constructorFailsIfMqttConnectionIsNull() throws TransportException
    {
        MqttMessaging testMqttMessaging = new MqttMessaging(null, CLIENT_ID, mockedIotHubListener, null, "");
    }

    /*
    **Tests_SRS_MqttMessaging_25_001: [The constructor shall throw IllegalArgumentException if any of the parameters are null or empty .]
     */
    @Test (expected = IllegalArgumentException.class)
    public void constructorFailsIfDeviceIDIsEmpty() throws TransportException
    {
        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, "", mockedIotHubListener, null, "");
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorFailsIfDeviceIDIsNull() throws TransportException
    {
        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, null, mockedIotHubListener, null, "");
    }

    /*
    **Tests_SRS_MqttMessaging_25_020: [start method shall be call connect to establish a connection to IOT Hub with the given configuration.]
    **Tests_SRS_MqttMessaging_25_021: [start method shall subscribe to messaging subscribe topic once connected.]
     */
    @Test
    public  void startCallsConnectAndSubscribe(@Mocked final Mqtt mockMqtt) throws TransportException
    {

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockMqtt, "connect");
                Deencapsulation.invoke(mockMqtt, "subscribe", anyString);
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "");

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

    @Test (expected = TransportException.class)
    public void startThrowsIoExceptionIfConnectFails(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(mockMqtt, "connect");
                result = new TransportException();
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "");
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

    @Test (expected = TransportException.class)
    public void startThrowsIoExceptionIfSubscribeFails(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(mockMqtt, "connect");
                Deencapsulation.invoke(mockMqtt, "subscribe", anyString);
                result = new TransportException();
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "");
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
    **Tests_SRS_MqttMessaging_25_022: [stop method shall be call disconnect to tear down a connection to IOT Hub with the given configuration.]

    **Tests_SRS_MqttMessaging_25_023: [stop method shall be call restartBaseMqtt to tear down a the base class even if disconnect fails.]
     */
    @Test
    public void stopCallsDisconnect(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockMqtt, "disconnect");
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "");
        testMqttMessaging.start();
        testMqttMessaging.stop();

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockMqtt, "disconnect");
                times = 1;
            }
        };
    }

    @Test (expected = TransportException.class)
    public void stopIfDisconnectFailsThrowsIOException(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(mockMqtt, "connect");
                Deencapsulation.invoke(mockMqtt, "subscribe", anyString);
                Deencapsulation.invoke(mockMqtt, "disconnect");
                result = new TransportException();
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "");
        testMqttMessaging.start();
        testMqttMessaging.stop();

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockMqtt, "disconnect");
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_MqttMessaging_25_024: [send method shall publish a message to the IOT Hub on the publish topic by calling method publish().]
     */
    @Test
    public void sendShallMessageToLowerLayer(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        final byte[] messageBody = {0x61, 0x62, 0x63};
        new NonStrictExpectations()
        {
            {
                mockedMessage.getBytes();
                result = messageBody;
                Deencapsulation.invoke(mockMqtt, "publish", new Class[] {String.class, Message.class}, anyString, (Message) any);
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "");
        testMqttMessaging.send(mockedMessage);

        //assert
        new Verifications()
        {
            {
                mockedMessage.getBytes();
                times = 1;
                Deencapsulation.invoke(mockMqtt, "publish", new Class[]{String.class, Message.class}, anyString, mockedMessage);
                times = 1;
            }
        };

    }

    //Tests_SRS_MqttMessaging_25_025: [send method shall throw an IllegalArgumentException if the message is null.]
    @Test (expected =  IllegalArgumentException.class)
    public void sendShallThrowIllegalArgumentExceptionIfMessageIsEmpty(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        final byte[] messageBody = {};
        new NonStrictExpectations()
        {
            {
                mockedMessage.getBytes();
                result = messageBody;
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "");
        testMqttMessaging.send(null);

        new Verifications()
        {
            {
                mockedMessage.getBytes();
                times = 1;
                Deencapsulation.invoke(mockMqtt, "publish", MOCK_PARSE_TOPIC, new byte[1]);
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_MqttMessaging_25_025: [send method shall throw an IllegalArgumentException if the message is null.]
     */
    @Test (expected = IllegalArgumentException.class)
    public void sendShallThrowTransportExceptionIfMessageIsNull(@Mocked final Mqtt mockMqtt) throws TransportException
    {

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "");
        testMqttMessaging.send(null);

        new Verifications()
        {
            {
                mockedMessage.getBytes();
                times = 0;
                Deencapsulation.invoke(mockMqtt, "publish", MOCK_PARSE_TOPIC, new byte[1]);
                times = 0;
            }
        };
    }

    //Tests_SRS_MqttMessaging_34_026: [This method shall append each custom property's name and value to the publishTopic before publishing.]
    @Test
    public void sendShallIncludeAllCustomPropertiesInPublishTopic(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        final byte[] messageBody = {0x61, 0x62, 0x63};
        final String propertyName1 = "key1";
        final String propertyValue1 = "value1";
        final String propertyName2 = "key2";
        final String propertyValue2 = "value2";
        final MessageProperty[] messageProperties = new MessageProperty[]
                {
                        new MessageProperty(propertyName1, propertyValue1),
                        new MessageProperty(propertyName2, propertyValue2)
                };
        new NonStrictExpectations()
        {
            {
                mockedMessage.getBytes();
                result = messageBody;
                mockedMessage.getProperties();
                result = messageProperties;
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "");
        final String publishTopicWithCustomProperties = String.format(
                "devices/%s/messages/events/%s=%s&%s=%s", CLIENT_ID, propertyName1, propertyValue1, propertyName2, propertyValue2);

        // act
        testMqttMessaging.send(mockedMessage);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockMqtt, "publish", publishTopicWithCustomProperties, mockedMessage);
                times = 1;
            }
        };
    }

    //Tests_SRS_MqttMessaging_21_027: [send method shall append the messageid to publishTopic before publishing using the key name `$.mid`.]
    @Test
    public void sendShallIncludeMessageIdInPublishTopic(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        //arrange
        final byte[] messageBody = {0x61, 0x62, 0x63};
        final MessageProperty[] messageProperties = new MessageProperty[]{};
        final String messageId = "test-message-id";
        final String publishTopicWithMessageId = String.format("devices/%s/messages/events/$.mid=%s", CLIENT_ID, messageId);
        new NonStrictExpectations()
        {
            {
                mockedMessage.getBytes();
                result = messageBody;
                mockedMessage.getProperties();
                result = messageProperties;
                mockedMessage.getMessageId();
                result = messageId;
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "");

        //act
        testMqttMessaging.send(mockedMessage);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockMqtt, "publish", publishTopicWithMessageId, mockedMessage);
                times = 1;
            }
        };
    }

    //Tests_SRS_MqttMessaging_34_028: [If the message has a correlationId, this method shall append that correlationid to publishTopic before publishing using the key name `$.cid`.]
    @Test
    public void sendShallIncludeCorrelationIdInPublishTopic(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        //arrange
        final byte[] messageBody = {0x61, 0x62, 0x63};
        final MessageProperty[] messageProperties = new MessageProperty[]{};
        final String correlationId = "test-correlation-id";
        final String publishTopicWithCorrelationId = String.format("devices/%s/messages/events/$.cid=%s", CLIENT_ID, correlationId);
        new NonStrictExpectations()
        {
            {
                mockedMessage.getBytes();
                result = messageBody;
                mockedMessage.getProperties();
                result = messageProperties;
                mockedMessage.getCorrelationId();
                result = correlationId;
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "");

        //act
        testMqttMessaging.send(mockedMessage);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockMqtt, "publish", publishTopicWithCorrelationId, mockedMessage);
                times = 1;
            }
        };
    }

    //Tests_SRS_MqttMessaging_34_030: [If the message has a UserId, this method shall append that userId to publishTopic before publishing using the key name `$.uid`.]
    @Test
    public void sendShallIncludeUserIdInPublishTopic(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        //arrange
        final byte[] messageBody = {0x61, 0x62, 0x63};
        final MessageProperty[] messageProperties = new MessageProperty[]{};
        final String userId = "test-user-id";
        final String publishTopicWithUserId = String.format("devices/%s/messages/events/$.uid=%s", CLIENT_ID, userId);
        new NonStrictExpectations()
        {
            {
                mockedMessage.getBytes();
                result = messageBody;
                mockedMessage.getProperties();
                result = messageProperties;
                mockedMessage.getUserId();
                result = userId;
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "");

        //act
        testMqttMessaging.send(mockedMessage);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockMqtt, "publish", publishTopicWithUserId, mockedMessage);
                times = 1;
            }
        };
    }

    //Tests_SRS_MqttMessaging_34_029: [If the message has a To, this method shall append that To to publishTopic before publishing using the key name `$.to`.]
    @Test
    public void sendShallIncludeToInPublishTopic(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        //arrange
        final byte[] messageBody = {0x61, 0x62, 0x63};
        final MessageProperty[] messageProperties = new MessageProperty[]{};
        final String to = "test-to";
        final String publishTopicWithTo = String.format("devices/%s/messages/events/$.to=%s", CLIENT_ID, to);
        new NonStrictExpectations()
        {
            {
                mockedMessage.getBytes();
                result = messageBody;
                mockedMessage.getProperties();
                result = messageProperties;
                mockedMessage.getTo();
                result = to;
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "");

        //act
        testMqttMessaging.send(mockedMessage);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockMqtt, "publish", publishTopicWithTo, mockedMessage);
                times = 1;
            }
        };
    }

    //Tests_SRS_MqttMessaging_34_029: [If the message has a To, this method shall append that To to publishTopic before publishing using the key name `$.to`.]
    //Tests_SRS_MqttMessaging_34_030: [If the message has a UserId, this method shall append that userId to publishTopic before publishing using the key name `$.uid`.]
    //Tests_SRS_MqttMessaging_34_028: [If the message has a correlationId, this method shall append that correlationid to publishTopic before publishing using the key name `$.cid`.]
    //Tests_SRS_MqttMessaging_21_027: [send method shall append the messageid to publishTopic before publishing using the key name `$.mid`.]
    //Tests_SRS_MqttMessaging_34_026: [This method shall append each custom property's name and value to the publishTopic before publishing.]
    @Test
    public void sendShallIncludeAllSystemPropertiesAndAllCustomPropertiesInPublishTopic(@Mocked final Mqtt mockMqtt) throws TransportException
    {
        final byte[] messageBody = {0x61, 0x62, 0x63};
        final String propertyName1 = "key1";
        final String propertyValue1 = "value1";
        final String propertyName2 = "key2";
        final String propertyValue2 = "value2";
        final String messageId = "test-message-id";
        final String correlationId = "test-correlation-id";
        final String userId = "test-user-id";
        final String to = "test-to";
        final MessageProperty[] messageProperties = new MessageProperty[]
                {
                        new MessageProperty(propertyName1, propertyValue1),
                        new MessageProperty(propertyName2, propertyValue2)
                };
        new NonStrictExpectations()
        {
            {
                mockedMessage.getBytes();
                result = messageBody;
                mockedMessage.getMessageId();
                result = messageId;
                mockedMessage.getCorrelationId();
                result = correlationId;
                mockedMessage.getUserId();
                result = userId;
                mockedMessage.getTo();
                result = to;
                mockedMessage.getProperties();
                result = messageProperties;
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "");
        final String publishTopicWithAllSystemAndCustomProperties = String.format(
                "devices/%s/messages/events/$.mid=%s&$.cid=%s&$.uid=%s&$.to=%s&%s=%s&%s=%s", CLIENT_ID, messageId, correlationId, userId, to, propertyName1, propertyValue1, propertyName2, propertyValue2);

        // act
        testMqttMessaging.send(mockedMessage);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockMqtt, "publish", publishTopicWithAllSystemAndCustomProperties,  mockedMessage);
                times = 1;
            }
        };
    }
}
