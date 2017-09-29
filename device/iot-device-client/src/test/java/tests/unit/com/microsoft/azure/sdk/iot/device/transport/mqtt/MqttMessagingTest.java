// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.


package tests.unit.com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.Mqtt;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttConnection;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttMessaging;
import mockit.*;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/* Unit tests for MqttMessaging
 * Code coverage: 100% methods, 78% lines
 */
public class MqttMessagingTest
{
    private static final String CLIENT_ID = "test.iothub";
    private static final String MOCK_PARSE_TOPIC = "testTopic";

    @Mocked
    private IOException mockIOException;

    @Mocked
    private Message mockMessage;

    @Mocked
    private MqttConnection mockedMqttConnection;

    /*
    **Tests_SRS_MqttMessaging_25_002: [**The constructor shall use the configuration to instantiate super class and passing the parameters.**]**
    */
    /*
    **Tests_SRS_MqttMessaging_25_003: [**The constructor construct publishTopic and subscribeTopic from deviceId.**]**
    */
    @Test
    public void constructorCallsBaseConstructorWithArguments(@Mocked final Mqtt mockMqtt) throws IOException
    {

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID);

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
    @Test (expected = IllegalArgumentException.class)
    public void constructorFailsIfMqttConnectionIsNull() throws IOException
    {

        MqttMessaging testMqttMessaging = new MqttMessaging(null, CLIENT_ID);
    }

    /*
    **Tests_SRS_MqttMessaging_25_001: [**The constructor shall throw InvalidParameter Exception if any of the parameters are null or empty .**]**
     */

    @Test (expected = IllegalArgumentException.class)
    public void constructorFailsIfDeviceIDIsEmpty() throws IOException
    {

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, "");
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorFailsIfDeviceIDIsNull() throws IOException
    {

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, null);
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

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID);

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

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID);
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

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID);
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
    public void stopCallsDisconnect(@Mocked final Mqtt mockMqtt) throws IOException
    {
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockMqtt, "disconnect");
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID);
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

    @Test (expected = IOException.class)
    public void stopIfDisconnectFailsThrowsIOException(@Mocked final Mqtt mockMqtt) throws IOException
    {
        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(mockMqtt, "connect");
                Deencapsulation.invoke(mockMqtt, "subscribe", anyString);
                Deencapsulation.invoke(mockMqtt, "disconnect");
                result = mockIOException;
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID);
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

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID);;
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

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID);;
        testMqttMessaging.send(null);

        new Verifications()
        {
            {
                mockMessage.getBytes();
                times = 1;
                Deencapsulation.invoke(mockMqtt, "publish", MOCK_PARSE_TOPIC, new byte[1]);
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

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID);;
        testMqttMessaging.send(null);

        new Verifications()
        {
            {
                mockMessage.getBytes();
                times = 0;
                Deencapsulation.invoke(mockMqtt, "publish", MOCK_PARSE_TOPIC, new byte[1]);
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
        final String expectedCorrelationId = "1234";
        final String expectedMessageId = "5678";
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
                mockMessage.getCorrelationId();
                result = expectedCorrelationId;
                mockMessage.getMessageId();
                result = expectedMessageId;
            }
        };

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID);

        // act
        testMqttMessaging.send(mockMessage);

        final String publishTopicWithProperties = String.format(
                "devices/%s/messages/events/$.mid=%s&$.cid=%s&%s=%s", CLIENT_ID, expectedMessageId, expectedCorrelationId, propertyName, propertyValue);
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

        MqttMessaging testMqttMessaging = new MqttMessaging(mockedMqttConnection, CLIENT_ID);;
        testMqttMessaging.send(mockMessage);
        final String publishTopicWithProperties = String.format(
                "devices/%s/messages/events/$.mid=%s&%s=%s", CLIENT_ID, messageidValue, propertyName, propertyValue);

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
