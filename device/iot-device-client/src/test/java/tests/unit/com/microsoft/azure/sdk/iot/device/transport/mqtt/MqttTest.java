// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.
package tests.unit.com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.Mqtt;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttConnection;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttDeviceTwin;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttMessaging;
import mockit.*;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.*;

/**
 * Unit test for Mqtt class.
 * 86% methods, 82% lines covered
 */
public class MqttTest {
    private static final String CLIENT_ID = "test.iothub";
    private static final String MOCK_PARSE_TOPIC = "devices/deviceID/messages/devicebound/%24.mid=69ea4caf-d83e-454b-81f2-caafda4c81c8&%24.exp=0&%24.to=%2Fdevices%2FdeviceID%2Fmessages%2FdeviceBound&%24.cid=169c34b3-99b0-49f9-b0f6-8fa9d2c99345&iothub-ack=full&property1=value1";
    private static final byte[] EXPECTED_PAYLOAD = {0x61, 0x62, 0x63};
    private Message expectedMessage;
    private static final String EXPECTED_EXPIRED_SAS_TOKEN = "SharedAccessSignature sr=hostname&sig=Signature&se=0";

    @Mocked
    private IMqttDeliveryToken mockMqttDeliveryToken;

    @Mocked
    private MqttAsyncClient mockMqttAsyncClient;

    @Mocked
    private IMqttToken mockMqttToken;

    @Mocked
    private MqttConnectOptions mockMqttConnectionOptions;

    @Mocked
    private MqttException mockMqttException;

    @Mocked
    private MqttMessage mockMqttMessage;

    @Mocked
    private IotHubSasToken mockSASToken;

    @Mocked
    private DeviceClientConfig mockDeviceClientConfig;

    @Mocked
    private MqttConnection mockedMqttConnection;

    @Before
    public void setUp()
    {
        expectedMessage = new Message(EXPECTED_PAYLOAD);
        expectedMessage.setProperty("property1", "value1");
        expectedMessage.setMessageId("69ea4caf-d83e-454b-81f2-caafda4c81c8");
        expectedMessage.setCorrelationId("169c34b3-99b0-49f9-b0f6-8fa9d2c99345");
    }

    private Mqtt instantiateMqtt(boolean withParameters) throws IOException
    {
        if (withParameters)
        {
            new MockUp<MqttMessaging>()
            {
                @Mock
                void $clinit()
                {
                    // Do nothing here (usually).
                }

                @Mock
                Pair<String, byte[]> peekMessage() throws IOException
                {
                    return new MutablePair<>(MOCK_PARSE_TOPIC, new byte[0]);
                }
            };
            return new MqttMessaging(mockedMqttConnection, CLIENT_ID);
        }
        else
        {
            new MockUp<MqttDeviceTwin>()
            {
                @Mock
                void $clinit()
                {
                    // Do nothing here (usually).
                }

                @Mock
                Pair<String, byte[]> peekMessage() throws IOException
                {
                    return new MutablePair<>(MOCK_PARSE_TOPIC, new byte[0]);
                }
            };
            return new MqttDeviceTwin(mockedMqttConnection);
        }
    }

    private void baseConstructorExpectations() throws MqttException
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

    private void baseConnectExpectation() throws MqttException
    {
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = false;
                mockMqttAsyncClient.connect();
                result = mockMqttToken;
                mockMqttToken.waitForCompletion();
            }
        };
    }

    private void baseDisconnectExpectations() throws MqttException
    {
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = true;
                mockMqttAsyncClient.disconnect();
                result = mockMqttToken;
                mockMqttToken.waitForCompletion();
            }
        };
    }

    private void basePublishExpectations() throws MqttException
    {
        final byte[] payload = {0x61, 0x62, 0x63};
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = true;
                new MqttMessage(payload);
                result = mockMqttMessage;
                mockMqttAsyncClient.publish(MOCK_PARSE_TOPIC, mockMqttMessage);
                result = mockMqttDeliveryToken;
            }
        };
    }

    private void basePublishVerifications() throws MqttException
    {
        final byte[] payload = {0x61, 0x62, 0x63};
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                minTimes = 1;
                new MqttMessage(payload);
                times = 1;
                mockMqttMessage.setQos(anyInt);
                times = 1;
                mockMqttAsyncClient.publish(MOCK_PARSE_TOPIC, mockMqttMessage);
                times = 1;
                mockMqttDeliveryToken.waitForCompletion();
                times = 1;
                mockMqttToken.waitForCompletion();
                times = 1;

            }
        };
    }

    private void baseDisconnectVerifications() throws MqttException
    {
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                times = 1;
                mockMqttAsyncClient.disconnect();
                times = 1;
                mockMqttToken.waitForCompletion();
                times = 1;
            }
        };

    }

    private void baseConnectVerifications() throws MqttException
    {
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                minTimes = 1;
                mockMqttAsyncClient.connect(mockMqttConnectionOptions);
                times = 1;
                mockMqttToken.waitForCompletion();
                times = 1;
            }
        };
    }

    private void baseConstructorVerifications(boolean withParameter) throws MqttException
    {
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedMqttConnection, "getAllReceivedMessages");
                times = 1;
                Deencapsulation.invoke(mockedMqttConnection, "getMqttLock");
                times = 1;

            }
        };
    }

    /*
    ** Tests_SRS_Mqtt_25_004: [**If an instance of the inner class MqttConnectionInfo is already created than it shall return doing nothing.**]**
    *  Tests_SRS_Mqtt_25_003: [The constructor shall retrieve lock, queue from the provided connection information and save the connection.]
     */
    @Test
    public void manyExtendsOfAbstractClassDoesNotChangeConfig() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        //act
        Mqtt mockMqtt1 = instantiateMqtt(true);
        MqttConnection actualInfoInstance1 = Deencapsulation.getField(mockMqtt1, "mqttConnection");
        Queue<Pair<String, byte[]>> actualQueue1 = Deencapsulation.getField(mockMqtt1, "allReceivedMessages");
        Object actualLock1 = Deencapsulation.getField(mockMqtt1, "mqttLock");

        Mqtt mockMqtt2 = instantiateMqtt(false);
        MqttConnection actualInfoInstance2 = Deencapsulation.getField(mockMqtt2, "mqttConnection");
        Queue<Pair<String, byte[]>> actualQueue2 = Deencapsulation.getField(mockMqtt2, "allReceivedMessages");
        Object actualLock2 = Deencapsulation.getField(mockMqtt2, "mqttLock");

        //assert
        assertEquals(actualInfoInstance1, actualInfoInstance2);
        assertEquals(actualQueue1, actualQueue2);
        assertEquals(actualLock1, actualLock2);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedMqttConnection, "getAllReceivedMessages");
                times = 2;
                Deencapsulation.invoke(mockedMqttConnection, "getMqttLock");
                times = 2;

            }
        };
    }

    /*
        Tests_SRS_Mqtt_25_002: [The constructor shall throw InvalidParameter Exception if mqttConnection is null .]
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsExceptionIfConnectionIsNotInitialised() throws IOException, MqttException
    {
        Mqtt mockMqtt = null;
        //act
        mockMqtt = new MqttMessaging(null, CLIENT_ID);
    }

    /*
    ** Tests_SRS_Mqtt_25_004: [**If an instance of the inner class MqttConnectionInfo is already created than it shall return doing nothing.**]**
     */
    @Test
    public void constructorWithParametersIfCalledMultipleTimesDoesntReinitialize() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();

        //act
        Mqtt mockMqtt1 = instantiateMqtt(true);

        //assert
        Object actualInfoInstance1 = Deencapsulation.getField(mockMqtt1, "mqttConnection");
        Queue<Pair<String, byte[]>> actualQueue1 = Deencapsulation.getField(mockMqtt1, "allReceivedMessages");

        Mqtt mockMqtt2 = instantiateMqtt(false);
        Object actualInfoInstance2 = Deencapsulation.getField(mockMqtt2, "mqttConnection");
        Queue<Pair<String, byte[]>> actualQueue2 = Deencapsulation.getField(mockMqtt2, "allReceivedMessages");

        Object actualLock1 = Deencapsulation.getField(mockMqtt1, "mqttLock");
        Object actualLock2 = Deencapsulation.getField(mockMqtt2, "mqttLock");

        assertEquals(actualInfoInstance1, actualInfoInstance2);
        assertEquals(actualQueue1, actualQueue2);
        assertEquals(actualLock1, actualLock2);
    }

    /*
    **Tests_SRS_Mqtt_25_005: [**The function shall establish an MQTT connection with an IoT Hub using the provided host name, user name, device ID, and sas token.**]**
     */
    @Test
    public void connectSuccess() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        baseConnectExpectation();
        Mqtt mockMqtt = instantiateMqtt(true);

        //act
        Deencapsulation.invoke(mockMqtt, "connect");

        //assert
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                minTimes = 1;
                mockMqttAsyncClient.connect(mockMqttConnectionOptions);
                times = 1;
                mockMqttToken.waitForCompletion();
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_Mqtt_25_008: [**If the MQTT connection is already open, the function shall do nothing.**]**
     */
    @Test
    public void connectDoesNothingIfAlreadyConnected() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = true;
            }
        };
        Mqtt mockMqtt = instantiateMqtt(true);

        //act
        Deencapsulation.invoke(mockMqtt, "connect");

        //assert
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                minTimes = 1;
                mockMqttAsyncClient.connect(mockMqttConnectionOptions);
                times = 0;
                mockMqttToken.waitForCompletion();
                times = 0;
            }
        };
    }

    /*
    **Tests_SRS_Mqtt_25_006: [**If the inner class MqttConnectionInfo has not been instantiated then the function shall throw IOException.**]**
     */
    @Test(expected = IOException.class)
    public void connectFailsIfNoConfigIsProvided() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        Mqtt mockMqtt = instantiateMqtt(false);
        Deencapsulation.setField(mockMqtt, "mqttConnection", null);

        //act
        Deencapsulation.invoke(mockMqtt, "connect");
    }

    /*
    **Tests_SRS_Mqtt_25_007: [**If an MQTT connection is unable to be established for any reason, the function shall throw an IOException.**]**
     */
    @Test(expected = IOException.class)
    public void connectThrowsIoExceptionOnMqttException() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = false;
                mockMqttAsyncClient.connect(mockMqttConnectionOptions);
                result = mockMqttException;
            }
        };
        Mqtt mockMqtt = instantiateMqtt(true);

        //act
        Deencapsulation.invoke(mockMqtt, "connect");

        //assert
        baseConnectVerifications();
    }

    /*
    **Tests_SRS_Mqtt_25_009: [**The function shall close the MQTT connection.**]**
     */
    @Test
    public void disconnectSucceeds() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        baseConnectExpectation();
        baseDisconnectExpectations();
        Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Deencapsulation.invoke(mockMqtt, "disconnect");

        //assert
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                times = 2;
                mockMqttAsyncClient.disconnect();
                times = 1;
                mockMqttToken.waitForCompletion();
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_Mqtt_25_010: [**If the MQTT connection is closed, the function shall do nothing.**]**
     */
    @Test
    public void disconnectDoesNothingWhenNotConnected() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        baseConnectExpectation();
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = false;
            }
        };

        Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Deencapsulation.invoke(mockMqtt, "disconnect");

        //assert
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                times = 2;
            }
        };
    }

    /*
    **Tests_SRS_Mqtt_25_014: [**The function shall publish message payload on the publishTopic specified to the IoT Hub given in the configuration.**]**
     */
    @Test
    public void publishSucceedsWhenConnected() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        baseConnectExpectation();
        basePublishExpectations();

        final byte[] payload = {0x61, 0x62, 0x63};
        Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Deencapsulation.invoke(mockMqtt, "publish", MOCK_PARSE_TOPIC, payload );

        //assert
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                minTimes = 2;
                mockMqttAsyncClient.publish(MOCK_PARSE_TOPIC, mockMqttMessage);
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_Mqtt_99_049: [**If the user supplied SAS token has expired, the function shall throw an IOException.**]**
     */
    @Test (expected = IOException.class) 
    public void publishThrowsExceptionIfUserSuppliedSASTokenHasExpired() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        final byte[] payload = {0x61, 0x62, 0x63};

        Mqtt mockMqtt = instantiateMqtt(true);

        //act
        Deencapsulation.setField(mockMqtt,"userSpecifiedSASTokenExpiredOnRetry",true);
        Deencapsulation.invoke(mockMqtt, "publish", MOCK_PARSE_TOPIC, payload);
    }

    /*
    **Tests_SRS_Mqtt_25_012: [**If the MQTT connection is closed, the function shall throw an IOException.**]**
     */
    @Test(expected = IOException.class)
    public void publishFailsWhenNotConnected() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        final byte[] payload = {0x61, 0x62, 0x63};
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = false;
            }
        };
        Mqtt mockMqtt = instantiateMqtt(true);

        //act
        Deencapsulation.invoke(mockMqtt, "publish", MOCK_PARSE_TOPIC, payload);
    }

    /*
    **Tests_SRS_Mqtt_25_012: [**If the MQTT connection is closed, the function shall throw an IOException.**]**
    */
    @Test (expected = IOException.class)
    public void publishFailsWhenConnectionBrokenWhilePublishing() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        final byte[] payload = {0x61, 0x62, 0x63};
        final IMqttDeliveryToken[] testTokens = {mockMqttDeliveryToken, mockMqttDeliveryToken, mockMqttDeliveryToken,
                mockMqttDeliveryToken, mockMqttDeliveryToken, mockMqttDeliveryToken,
                mockMqttDeliveryToken, mockMqttDeliveryToken, mockMqttDeliveryToken,
                mockMqttDeliveryToken, mockMqttDeliveryToken, mockMqttDeliveryToken
        };
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = true;
                mockMqttAsyncClient.getPendingDeliveryTokens();
                result = testTokens;
                mockMqttAsyncClient.isConnected();
                result = false;
            }
        };
        Mqtt mockMqtt = instantiateMqtt(true);

        //act
        Deencapsulation.invoke(mockMqtt, "publish", MOCK_PARSE_TOPIC, payload);
    }


    /*
    **Tests_SRS_Mqtt_25_014: [**The function shall publish message payload on the publishTopic specified to the IoT Hub given in the configuration.**]**
     */
    @Test
    public void publishWithDifferentTopicsFromDifferentConcreteClassSucceeds() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        baseConstructorExpectations();
        baseConnectExpectation();
        basePublishExpectations();
        basePublishExpectations();

        final byte[] payload = {0x61, 0x62, 0x63};
        String mockParseTopic2 = MOCK_PARSE_TOPIC + 2;
        Mqtt mockMqtt1 = instantiateMqtt(true);
        Mqtt mockMqtt2 = instantiateMqtt(false);
        Deencapsulation.invoke(mockMqtt2, "connect");

        //act
        Deencapsulation.invoke(mockMqtt1, "publish", MOCK_PARSE_TOPIC, payload);
        Deencapsulation.invoke(mockMqtt2, "publish", mockParseTopic2, payload);

        //assert
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                minTimes = 3;
                mockMqttAsyncClient.publish(anyString, mockMqttMessage);
                times = 2;
            }
        };
    }

    /*
     **Tests_SRS_Mqtt_25_047: [**If the Mqtt Client Async throws MqttException, the function shall throw an IOException with the message.**]**
     */
    @Test(expected = IOException.class)
    public void publishThrowsIOExceptionWhenAnyOfTheAsyncMethodsThrow() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        final byte[] payload = {0x61, 0x62, 0x63};
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = true;
                new MqttMessage(payload);
                result = mockMqttMessage;
                mockMqttAsyncClient.publish(MOCK_PARSE_TOPIC, mockMqttMessage);
                result = mockMqttException;
            }
        };
        Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Deencapsulation.invoke(mockMqtt, "publish", MOCK_PARSE_TOPIC, payload);

        //assert
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                minTimes = 1;
            }
        };
    }

    /*
    **Tests_SRS_Mqtt_25_013: [**If the either publishTopic or payload is null or empty, the function shall throw an IOException.**]**
     */
    @Test(expected = IOException.class)
    public void publishThrowsExceptionWhenPublishTopicIsNull() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        final byte[] payload = {0x61, 0x62, 0x63};
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = true;
            }
        };
        Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Deencapsulation.invoke(mockMqtt, "publish", String.class, payload);

        //assert
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                minTimes = 1;
            }
        };
    }

    /*
    **Tests_SRS_Mqtt_25_013: [**If the either publishTopic or payload is null or empty, the function shall throw an IOException.**]**
    */
    @Test(expected = IOException.class)
    public void publishThrowsExceptionWhenPayloadIsNull() throws IOException, MqttException
    {
        //arrange

        baseConstructorExpectations();
        final byte[] payload = null;
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = true;
            }
        };
        Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Deencapsulation.invoke(mockMqtt, "publish", MOCK_PARSE_TOPIC, byte[].class);

        //assert
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                minTimes = 1;
            }
        };
    }

    /*
    **Tests_SRS_Mqtt_25_017: [**The function shall subscribe to subscribeTopic specified to the IoT Hub given in the configuration.**]**
     */
    @Test
    public void subscribeSucceeds() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        baseConnectExpectation();
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = true;
                mockMqttAsyncClient.subscribe(MOCK_PARSE_TOPIC, anyInt);
                result = mockMqttToken;
            }
        };
        Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Deencapsulation.invoke(mockMqtt, "subscribe", MOCK_PARSE_TOPIC);

        //assert
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                minTimes = 1;
                mockMqttAsyncClient.subscribe(MOCK_PARSE_TOPIC, anyInt);
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_Mqtt_25_015: [**If the MQTT connection is closed, the function shall throw an IOexception with message.**]**
     */
    @Test(expected = IOException.class)
    public void subscribeFailsWhenNotConnected() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = false;
            }
        };

        Mqtt mockMqtt = instantiateMqtt(true);

        //act
        Deencapsulation.invoke(mockMqtt, "subscribe", MOCK_PARSE_TOPIC);

        //assert
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                minTimes = 1;
            }
        };
    }

    @Test(expected = IOException.class)
    public void subscribeFailsWhenConfigIsNotSet() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        Mqtt mockMqtt = instantiateMqtt(false);

        //act
        Deencapsulation.invoke(mockMqtt, "subscribe", MOCK_PARSE_TOPIC);
    }

    /*
    **Tests_SRS_Mqtt_25_016: [**If the subscribeTopic is null or empty, the function shall throw an InvalidParameter Exception.**]**
     */
    @Test(expected = InvalidParameterException.class)
    public void subscribeThrowsExceptionWhenTopicIsNull() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();

        Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Deencapsulation.invoke(mockMqtt, "subscribe", String.class);
    }

    /*
    **Tests_SRS_Mqtt_99_049: [**If the user supplied SAS token has expired, the function shall throw an IOException.**]**
     */
    @Test(expected = IOException.class)
    public void subscribeThrowsExceptionWhenUserSuppliedSASTokenHasExpired() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();

        Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Deencapsulation.setField(mockMqtt,"userSpecifiedSASTokenExpiredOnRetry",true);
        Deencapsulation.invoke(mockMqtt, "subscribe", MOCK_PARSE_TOPIC);
    }
    
    /*
    **Tests_SRS_Mqtt_25_048: [**If the Mqtt Client Async throws MqttException for any reason, the function shall throw an IOException with the message.**]**
     */
    @Test(expected = IOException.class)
    public void subscribeThrowsIOExceptionWhenMqttAsyncThrows() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        baseConnectExpectation();

        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = true;
                mockMqttAsyncClient.subscribe(MOCK_PARSE_TOPIC, anyInt);
                result = mockMqttException;
            }
        };

        Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Deencapsulation.invoke(mockMqtt, "subscribe", MOCK_PARSE_TOPIC);

        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                minTimes = 1;
                mockMqttAsyncClient.subscribe(MOCK_PARSE_TOPIC, anyInt);
                times = 1;
            }
        };
    }

    // Tests_SRS_Mqtt_34_023: [This method shall call peekMessage to get the message payload from the received Messages queue corresponding to the messaging client's operation.]
    // Tests_SRS_Mqtt_34_024: [This method shall construct new Message with the bytes obtained from peekMessage and return the message.]
   @Test
    public void receiveSuccess() throws IOException, MqttException
    {
        //arrange
        final byte[] payload = {0x61, 0x62, 0x63};
        baseConstructorExpectations();
        baseConnectExpectation();
        new MockUp<MqttMessaging>()
        {
            @Mock
            Pair<String, byte[]> peekMessage() throws IOException
            {
                return new MutablePair<>(MOCK_PARSE_TOPIC, payload);
            }
        };

        final Mqtt mockMqtt = new MqttMessaging(mockedMqttConnection, CLIENT_ID);
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = true;
            }
        };

        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Message receivedMessage = mockMqtt.receive();

        //assert
        byte[] actualPayload = receivedMessage.getBytes();
        assertTrue(actualPayload.length == payload.length);
        for (int i = 0; i < payload.length; i++)
        {
            assertEquals(actualPayload[i], payload[i]);
        }
        assertEquals(expectedMessage.getMessageId(), receivedMessage.getMessageId());
        assertEquals(expectedMessage.getCorrelationId(), receivedMessage.getCorrelationId());

        assertEquals(expectedMessage.getProperties().length, receivedMessage.getProperties().length);
        assertEquals(expectedMessage.getProperties()[0].getName(), receivedMessage.getProperties()[0].getName());
        assertEquals(expectedMessage.getProperties()[0].getValue(), receivedMessage.getProperties()[0].getValue());
    }

    // Codes_SRS_Mqtt_34_022: [If the call peekMessage returns a null or empty string then this method shall do nothing and return null]
    @Test
    public void receiveReturnsNullMessageWhenParseTopicReturnsNull() throws IOException, MqttException
    {
        //arrange
        final byte[] payload = {0x61, 0x62, 0x63};
        baseConstructorExpectations();
        baseConnectExpectation();
        new MockUp<MqttMessaging>()
        {
            @Mock
            Pair<String, byte[]> peekMessage() throws IOException
            {
                return new MutablePair<>(null, payload);
            }
        };
        final Mqtt mockMqtt = new MqttMessaging(mockedMqttConnection, CLIENT_ID);

        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = true;
            }
        };

        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Message receivedMessage = mockMqtt.receive();

        //assert
        assertNull(receivedMessage);
    }

    // Codes_SRS_Mqtt_34_025: [If the call to peekMessage returns null when topic is non-null then this method will throw IOException]
    @Test(expected = IOException.class)
    public void receiveThrowsIOExceptionWhenParsePayloadReturnsNull() throws IOException, MqttException
    {
        //arrange
        final byte[] payload = {0x61, 0x62, 0x63};
        baseConstructorExpectations();
        new MockUp<MqttMessaging>()
        {
            @Mock
            Pair<String, byte[]> peekMessage() throws IOException
            {
                return new MutablePair<>(MOCK_PARSE_TOPIC, null);
            }
        };

        final Mqtt mockMqtt = new MqttMessaging(mockedMqttConnection, CLIENT_ID);
        //act
        Message receivedMessage = mockMqtt.receive();
    }

    @Test(expected = InvalidParameterException.class)
    public void receiveThrowsExceptionWhenConfigurationIsNotSet() throws IOException, MqttException
    {
        //arrange
        final byte[] payload = {0x61, 0x62, 0x63};
        baseConstructorExpectations();
        new MockUp<MqttMessaging>()
        {
            @Mock
            Pair<String, byte[]> peekMessage() throws IOException
            {
                return new MutablePair<>(MOCK_PARSE_TOPIC, null);
            }
        };

        final Mqtt mockMqtt = new MqttMessaging(mockedMqttConnection, CLIENT_ID);
        Deencapsulation.setField(mockMqtt, "mqttConnection", null);

        //act
        Message receivedMessage = mockMqtt.receive();
    }

    /*
    **Tests_SRS_Mqtt_25_030: [**The payload of the message and the topic is added to the received messages queue .**]**
     */
    @Test
    public void messageArrivedAddsToQueue() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        final byte[] actualPayload = {0x61, 0x62, 0x63};
        baseConstructorExpectations();
        baseConnectExpectation();

        new NonStrictExpectations()
        {
            {
                mockMqttMessage.getPayload();
                result = actualPayload;
            }
        };

        mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        mockMqtt.messageArrived(MOCK_PARSE_TOPIC, new MqttMessage(actualPayload));

        //assert
        Queue<Pair<String, byte[]>> actualQueue = Deencapsulation.getField(mockMqtt, "allReceivedMessages");
        Pair<String, byte[]> messagePair = actualQueue.poll();
        assertNotNull(messagePair);
        assertTrue(messagePair.getKey().equals(MOCK_PARSE_TOPIC));

        byte[] receivedPayload = messagePair.getValue();
        assertTrue(actualPayload.length == receivedPayload.length);
        for (int i = 0; i < actualPayload.length; i++)
        {
            assertEquals(actualPayload[i], receivedPayload[i]);
        }
    }

   /*
    **Tests_SRS_Mqtt_99_050: [**The function shall check if SAS token has already expired.**]**
    */
    /*
    **Tests_SRS_Mqtt_25_026: [**The function shall notify all its concrete classes by calling abstract method onReconnect at the entry of the function**]**
     */
    /*
    **Tests_SRS_Mqtt_25_029: [**The function shall notify all its concrete classes by calling abstract method onReconnectComplete at the exit of the function**]**
     */
    @Test
    public void connectionLostAttemptsToReconnectWithSASTokenStillValid() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        Throwable t = new Throwable();
        baseConstructorExpectations();

        new StrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = false;

                mockDeviceClientConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;

                IotHubSasToken.isExpired(anyString);
                result = false;

                mockMqttAsyncClient.isConnected();
                result = false;

                mockMqttAsyncClient.connect(mockMqttConnectionOptions);
                result = mockMqttToken;
                mockMqttToken.waitForCompletion();

                mockMqttAsyncClient.isConnected();
                result = true;
            }
        };

        //act
        try
        {
            mockMqtt = instantiateMqtt(true);
            Deencapsulation.invoke(mockMqtt, "setDeviceClientConfig", mockDeviceClientConfig);
            mockMqtt.connectionLost(t);
        }
        catch (Exception e)
        {
            System.out.print("Completed throwing exception - " + e.getCause() + e.getMessage());
        }
    }

    /*
     **Tests_SRS_Mqtt_99_050: [**The function shall check if SAS token has already expired.**]**
    */
    /*
     **Tests_SRS_Mqtt_99_051: [**The function shall check if SAS token in based on user supplied SharedAccessKey.**]**
    */
     /*
     **Tests_SRS_Mqtt_99_052: [**The function shall generate a new SAS token.**]**
    */
    /*
    **Tests_SRS_Mqtt_25_026: [**The function shall notify all its concrete classes by calling abstract method onReconnect at the entry of the function**]**
     */
    /*
    **Tests_SRS_Mqtt_25_029: [**The function shall notify all its concrete classes by calling abstract method onReconnectComplete at the exit of the function**]**
    */
    @Test
    public void connectionLostAttemptsToReconnectWithUserSuppliedSharedKeyBasedSASTokenAlreadyExpired() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        Throwable t = new Throwable();
        baseConstructorExpectations();

        new StrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = false;
                mockDeviceClientConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockMqttConnectionOptions.getPassword();
                result = EXPECTED_EXPIRED_SAS_TOKEN.toCharArray();
                IotHubSasToken.isExpired(EXPECTED_EXPIRED_SAS_TOKEN);
                result = true; // SAS token has expired
                mockDeviceClientConfig.getIotHubConnectionString().getSharedAccessKey();
                result = anyString;

                mockDeviceClientConfig.getSasTokenAuthentication().getRenewedSasToken();
                result = EXPECTED_EXPIRED_SAS_TOKEN;

                mockMqttConnectionOptions.setPassword((char[])any);
                mockMqttAsyncClient.isConnected();
                result = false;
                mockMqttAsyncClient.connect(mockMqttConnectionOptions);
                result = mockMqttToken;
                mockMqttToken.waitForCompletion();
                mockMqttAsyncClient.isConnected();
                result = true;
            }
        };

        //act
        try
        {
            mockMqtt = instantiateMqtt(true);
            Deencapsulation.setField(mockMqtt, "deviceClientConfig", mockDeviceClientConfig);
            mockMqtt.connectionLost(t);
        }
        catch (Exception e)
        {
            System.out.print("Completed throwing exception - " + e.getCause() + e.getMessage());
        }
    }

	/*
     **Tests_SRS_Mqtt_99_050: [**The function shall check if SAS token has already expired.**]**
    */
	/*
     **Tests_SRS_Mqtt_99_051: [**The function shall check if SAS token in based on user supplied SharedAccessKey.**]**
    */

    @Test(expected = IOException.class)
    public void connectionLostAttemptsToReconnectWithUserSuppliedSASTokenAlreadyExpired() throws IOException, MqttException
    {
        //arrange
        final byte[] payload = {0x61, 0x62, 0x63};
        Mqtt mockMqtt = null;
        Throwable t = new Throwable();
        baseConstructorExpectations();
        new StrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = false;

                mockDeviceClientConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;

                IotHubSasToken.isExpired(anyString);
                result = true; // User specified SAS token has expired

                mockDeviceClientConfig.getIotHubConnectionString().getSharedAccessKey();
                result = null;
            }
        };

        //act
        mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "setDeviceClientConfig", mockDeviceClientConfig);
        mockMqtt.connectionLost(t);
        Deencapsulation.invoke(mockMqtt, "publish", MOCK_PARSE_TOPIC, payload);
    }
    
    /*
    **Tests_SRS_Mqtt_25_027: [**The function shall attempt to reconnect to the IoTHub in a loop with exponential backoff until it succeeds**]**
     */
    @Test
    public void connectionLostAttemptsToReconnectAgainIfConnectFails() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        Throwable t = new Throwable();
        baseConstructorExpectations();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedMqttConnection, "getMqttAsyncClient");
                result = mockMqttAsyncClient;
                Deencapsulation.invoke(mockedMqttConnection, "getConnectionOptions");
                result = mockMqttConnectionOptions;
            }
        };

        new StrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = false;

                mockDeviceClientConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;

                IotHubSasToken.isExpired(anyString);
                result = false;

                mockMqttAsyncClient.isConnected();
                result = false;

                mockMqttAsyncClient.connect(mockMqttConnectionOptions);
                result = mockMqttException;


                mockMqttAsyncClient.isConnected();
                result = false;

                mockDeviceClientConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;

                IotHubSasToken.isExpired(anyString);
                result = false;

                mockMqttAsyncClient.isConnected();
                result = false;

                mockMqttAsyncClient.connect(mockMqttConnectionOptions);
                result = mockMqttToken;

                mockMqttToken.waitForCompletion();

                mockMqttAsyncClient.isConnected();
                result = true;
            }
        };
        //act
        try
        {
            mockMqtt = instantiateMqtt(true);
            Deencapsulation.invoke(mockMqtt, "setDeviceClientConfig", mockDeviceClientConfig);
            mockMqtt.connectionLost(t);
        }
        catch (Exception e)
        {
            System.out.print("Completed throwing exception - " + e.getCause() + e.getMessage());
        }

    }

    // Tests_SRS_Mqtt_34_021: [If the call peekMessage returns null then this method shall do nothing and return null]
    @Test
    public void receiveReturnsNullMessageIfTopicNotFound() throws IOException, MqttException
    {
        //can't be initialized to null, so set it as a default message
        baseConstructorExpectations();
        Message receivedMessage = new Message();
        try
        {
            //arrange
            MqttMessaging testMqttClient = new MqttMessaging(mockedMqttConnection,"deviceId");
            Queue<Pair<String, byte[]>> testAllReceivedMessages = new ConcurrentLinkedQueue<>();
            Deencapsulation.setField(testMqttClient, "allReceivedMessages", testAllReceivedMessages);

            //act
            receivedMessage = testMqttClient.receive();
        }
        finally
        {
            //assert
            assertNull(receivedMessage);
        }
    }

    /*
    **Tests_SRS_Mqtt_34_051: [**If a topic string's property's key and value are not separated by the '=' symbol, an IllegalArgumentException shall be thrown**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void receiveFailureFromInvalidPropertyStringThrowsIllegalArgumentException() throws IOException, MqttException, IllegalArgumentException
    {
        //arrange
        final byte[] payload = {0x61, 0x62, 0x63};
        final String mockParseTopicInvalidPropertyFormat = "devices/deviceID/messages/devicebound/%24.mid=69ea4caf-d83e-454b-81f2-caafda4c81c8&%24.exp=99999&%24.to=%2Fdevices%2FdeviceID%2Fmessages%2FdeviceBound&%24.cid=169c34b3-99b0-49f9-b0f6-8fa9d2c99345&iothub-ack=full&property1value1";
        baseConstructorExpectations();
        baseConnectExpectation();
        new MockUp<MqttMessaging>()
        {
            @Mock
            Pair<String, byte[]> peekMessage()
            {
                return new MutablePair<>(mockParseTopicInvalidPropertyFormat, payload);
            }
        };

        final Mqtt mockMqtt = new MqttMessaging(mockedMqttConnection, CLIENT_ID);
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = true;
            }
        };

        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        mockMqtt.receive();
    }

    /*
    **Test_SRS_Mqtt_34_054: [**A message may have 0 to many custom properties**]**
    */
    @Test
    public void receiveSuccessNoCustomProperties() throws IOException, MqttException
    {
        //arrange
        final byte[] payload = {0x61, 0x62, 0x63};
        final String mockParseTopicNoCustomProperties = "devices/deviceID/messages/devicebound/%24.mid=69ea4caf-d83e-454b-81f2-caafda4c81c8&%24.exp=0&%24.to=%2Fdevices%2FdeviceID%2Fmessages%2FdeviceBound&%24.cid=169c34b3-99b0-49f9-b0f6-8fa9d2c99345&iothub-ack=full";
        baseConstructorExpectations();
        baseConnectExpectation();
        new MockUp<MqttMessaging>()
        {
            @Mock
            Pair<String, byte[]> peekMessage()
            {
                return new MutablePair<>(mockParseTopicNoCustomProperties, payload);
            }
        };

        final Mqtt mockMqtt = new MqttMessaging(mockedMqttConnection, CLIENT_ID);

        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = true;
            }
        };

        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Message receivedMessage = mockMqtt.receive();

        //assert
        assertEquals(receivedMessage.getProperties().length, 0);
    }

    /*
    **Tests_SRS_Mqtt_34_053: [**A property's key and value may include unusual characters such as &, %, $**]**
    */
    @Test
    public void receiveSuccessCustomPropertyHasUnusualCharacters() throws IOException, MqttException
    {
        //arrange
        final byte[] payload = {0x61, 0x62, 0x63};
        final String mockParseTopicWithUnusualCharacters = "devices/deviceID/messages/devicebound/%24.mid=69ea4caf-d83e-454b-81f2-caafda4c81c8&%24.exp=0&%24.to=%2Fdevices%2FdeviceID%2Fmessages%2FdeviceBound&%24.cid=169c34b3-99b0-49f9-b0f6-8fa9d2c99345&iothub-ack=full&property1=%24&property2=%26&%25=%22&finalProperty=%3d";
        baseConstructorExpectations();
        baseConnectExpectation();
        new MockUp<MqttMessaging>()
        {
            @Mock
            Pair<String, byte[]> peekMessage()
            {
                return new MutablePair<>(mockParseTopicWithUnusualCharacters, payload);
            }
        };

        final Mqtt mockMqtt = new MqttMessaging(mockedMqttConnection, CLIENT_ID);
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = true;
            }
        };

        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Message receivedMessage = mockMqtt.receive();

        //assert
        byte[] actualPayload = receivedMessage.getBytes();
        assertTrue(actualPayload.length == payload.length);
        for (int i = 0; i < payload.length; i++)
        {
            assertEquals(actualPayload[i], payload[i]);
        }

        assertEquals(4, receivedMessage.getProperties().length);
        assertEquals("$", receivedMessage.getProperties()[0].getValue());
        assertEquals("&", receivedMessage.getProperties()[1].getValue());
        assertEquals("%", receivedMessage.getProperties()[2].getName());
        assertEquals("\"", receivedMessage.getProperties()[2].getValue());
        assertEquals("=", receivedMessage.getProperties()[3].getValue());
    }

    /*
    ** Codes_SRS_Mqtt_99_50: [**If deviceConfig is null, the function shall throw an IllegalArgumentException**]**
    */
    @Test  (expected = IllegalArgumentException.class)
    public void deviceConfigNullThrows() throws IOException, MqttException
    {
        // Act
        Mqtt mockMqtt = instantiateMqtt(true);

        Deencapsulation.invoke(mockMqtt,"setDeviceClientConfig", new Class[] {DeviceClientConfig.class},(DeviceClientConfig)null);
    } 
}