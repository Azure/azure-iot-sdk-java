// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.
package tests.unit.com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubListener;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.*;
import mockit.*;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST;
import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST;
import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST;
import static org.junit.Assert.*;

/**
 * Unit test for Mqtt class.
 * 100% methods, 92% lines covered
 */
public class MqttTest
{
    private static final String expectedInputName = "someInputName";
    private static final String expectedModuleId = "someModuleId";
    private static final String CLIENT_ID = "test.iothub";
    private static final String MOCK_PARSE_TOPIC = "devices/deviceID/messages/devicebound/%24.mid=69ea4caf-d83e-454b-81f2-caafda4c81c8&%24.exp=0&%24.to=%2Fdevices%2FdeviceID%2Fmessages%2FdeviceBound&%24.cid=169c34b3-99b0-49f9-b0f6-8fa9d2c99345&iothub-ack=full&property1=value1";
    private static final String MOCK_PARSE_TOPIC_WITH_INPUT_NAME = "devices/deviceID/modules/" + expectedModuleId + "/inputs/" + expectedInputName + "/messages/devicebound/%24.mid=69ea4caf-d83e-454b-81f2-caafda4c81c8&%24.exp=0&%24.to=%2Fdevices%2FdeviceID%2Fmessages%2FdeviceBound&%24.cid=169c34b3-99b0-49f9-b0f6-8fa9d2c99345&iothub-ack=full&property1=value1";
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

    @Mocked
    private IotHubListener mockedIotHubListener;

    @Mocked
    private ProtocolException mockedProtocolException;

    @Mocked
    private MqttMessageListener mockedMessageListener;

    @Before
    public void setUp()
    {
        expectedMessage = new Message(EXPECTED_PAYLOAD);
        expectedMessage.setProperty("property1", "value1");
        expectedMessage.setMessageId("69ea4caf-d83e-454b-81f2-caafda4c81c8");
        expectedMessage.setCorrelationId("169c34b3-99b0-49f9-b0f6-8fa9d2c99345");
    }

    private Mqtt instantiateMqtt(boolean withParameters) throws TransportException
    {
        return instantiateMqtt(withParameters, null);
    }

    private Mqtt instantiateMqtt(boolean withParameters, IotHubListener listener) throws TransportException
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
            return new MqttMessaging(mockedMqttConnection, CLIENT_ID, listener, mockedMessageListener, "", "", false);
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
            return new MqttDeviceTwin(mockedMqttConnection, "");
        }
    }

    private void baseConstructorExpectations()
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

    private void basePublishExpectations(final Message mockedMessage) throws MqttException
    {
        final byte[] payload = {0x61, 0x62, 0x63};
        new NonStrictExpectations()
        {
            {
                mockedMessage.getBytes();
                result = payload;
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
    ** Tests_SRS_Mqtt_25_004: [If an instance of the inner class MqttConnectionInfo is already created than it shall return doing nothing.]
    *  Tests_SRS_Mqtt_25_003: [The constructor shall retrieve lock, queue from the provided connection information and save the connection.]
     */
    @Test
    public void manyExtendsOfAbstractClassDoesNotChangeConfig() throws TransportException
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
        Tests_SRS_Mqtt_25_002: [The constructor shall throw IllegalArgumentException if mqttConnection is null .]
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsExceptionIfConnectionIsNotInitialised() throws TransportException
    {
        Mqtt mockMqtt = null;
        //act
        mockMqtt = new MqttMessaging(null, CLIENT_ID, mockedIotHubListener, null, "", "", false);
    }

    /*
    ** Tests_SRS_Mqtt_25_004: [If an instance of the inner class MqttConnectionInfo is already created than it shall return doing nothing.]
     */
    @Test
    public void constructorWithParametersIfCalledMultipleTimesDoesntReinitialize() throws TransportException
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

    //Tests_SRS_Mqtt_25_005: [The function shall establish an MQTT connection with an IoT Hub using the provided host name, user name, device ID, and sas token.]
    //Tests_SRS_Mqtt_34_020: [If the MQTT connection is established successfully, this function shall notify its listener that connection was established.]
    @Test
    public void connectSuccess() throws TransportException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        baseConnectExpectation();
        Mqtt mockMqtt = instantiateMqtt(true, mockedIotHubListener);

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
                mockMqttToken.waitForCompletion(anyLong);
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_Mqtt_25_008: [If the MQTT connection is already open, the function shall do nothing.]
     */
    @Test
    public void connectDoesNothingIfAlreadyConnected() throws TransportException, MqttException
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
    **Tests_SRS_Mqtt_25_009: [The function shall close the MQTT connection.]
     */
    @Test
    public void disconnectSucceeds() throws TransportException, MqttException
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
                Deencapsulation.invoke(mockedMqttConnection, "close");
                times = 1;
            }
        };
    }

    //Tests_SRS_Mqtt_34_055: [If an MQTT connection is connected, the function shall disconnect that connection.]
    @Test
    public void disconnectDisconnectsIfConnected() throws MqttException, TransportException
    {
        baseConstructorExpectations();
        baseConnectExpectation();
        baseDisconnectExpectations();
        Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "connect");
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedMqttConnection, "isConnected");
                result = true;
            }
        };

        //act
        Deencapsulation.invoke(mockMqtt, "disconnect");

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedMqttConnection, "disconnect");
                times = 1;
            }
        };
    }

    //Tests_SRS_Mqtt_34_055: [If an MQTT connection is connected, the function shall disconnect that connection.]
    @Test
    public void disconnectDoesNotDisconnectIfNotConnected() throws IOException, MqttException, TransportException
    {
        //arrange
        baseConstructorExpectations();
        baseConnectExpectation();
        baseDisconnectExpectations();
        Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "connect");
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedMqttConnection, "isConnected");
                result = false;
            }
        };

        //act
        Deencapsulation.invoke(mockMqtt, "disconnect");

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedMqttConnection, "disconnect");
                times = 0;
            }
        };
    }

    //Tests_SRS_Mqtt_25_014: [The function shall publish message payload on the publishTopic specified to the IoT Hub given in the configuration.]
    @Test
    public void publishSucceedsWhenConnected(final @Mocked Message mockedMessage) throws TransportException, MqttException
    {
        //arrange
        baseConstructorExpectations();
        baseConnectExpectation();
        basePublishExpectations(mockedMessage);

        final byte[] payload = {0x61, 0x62, 0x63};
        Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Deencapsulation.invoke(mockMqtt, "publish", MOCK_PARSE_TOPIC, mockedMessage);

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
    **Tests_SRS_Mqtt_99_049: [If the user supplied SAS token has expired, the function shall throw a TransportException.]
     */
    @Test (expected = TransportException.class)
    public void publishThrowsExceptionIfUserSuppliedSASTokenHasExpired(final @Mocked Message mockedMessage) throws TransportException
    {
        //arrange
        baseConstructorExpectations();
        final byte[] payload = {0x61, 0x62, 0x63};

        Mqtt mockMqtt = instantiateMqtt(true);

        //act
        Deencapsulation.setField(mockMqtt,"userSpecifiedSASTokenExpiredOnRetry",true);
        Deencapsulation.invoke(mockMqtt, "publish", MOCK_PARSE_TOPIC, mockedMessage);
    }

    /*
    **Tests_SRS_Mqtt_25_012: [If the MQTT connection is closed, the function shall throw a TransportException.]
     */
    @Test(expected = TransportException.class)
    public void publishFailsWhenNotConnected(final @Mocked Message mockedMessage) throws TransportException
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
        Deencapsulation.invoke(mockMqtt, "publish", MOCK_PARSE_TOPIC, mockedMessage);
    }

    /*
    **Tests_SRS_Mqtt_25_012: [If the MQTT connection is closed, the function shall throw a TransportException.]
    */
    @Test (expected = TransportException.class)
    public void publishFailsWhenConnectionBrokenWhilePublishing(final @Mocked Message mockedMessage) throws TransportException
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
        Deencapsulation.invoke(mockMqtt, "publish", MOCK_PARSE_TOPIC, mockedMessage);
    }


    /*
    **Tests_SRS_Mqtt_25_014: [The function shall publish message payload on the publishTopic specified to the IoT Hub given in the configuration.]
     */
    @Test
    public void publishWithDifferentTopicsFromDifferentConcreteClassSucceeds(final @Mocked Message mockedMessage) throws MqttException, TransportException
    {
        //arrange
        baseConstructorExpectations();
        baseConstructorExpectations();
        baseConnectExpectation();
        basePublishExpectations(mockedMessage);
        basePublishExpectations(mockedMessage);

        final byte[] payload = {0x61, 0x62, 0x63};
        String mockParseTopic2 = MOCK_PARSE_TOPIC + 2;
        Mqtt mockMqtt1 = instantiateMqtt(true);
        Mqtt mockMqtt2 = instantiateMqtt(false);
        Deencapsulation.invoke(mockMqtt2, "connect");

        //act
        Deencapsulation.invoke(mockMqtt1, "publish", MOCK_PARSE_TOPIC, mockedMessage);
        Deencapsulation.invoke(mockMqtt2, "publish", mockParseTopic2, mockedMessage);

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
     **Tests_SRS_Mqtt_25_047: [If the Mqtt Client Async throws MqttException, the function shall throw a ProtocolException with the message.]
     */
    @Test(expected = ProtocolException.class)
    public void publishThrowsIOExceptionWhenAnyOfTheAsyncMethodsThrow(final @Mocked Message mockedMessage) throws MqttException, TransportException
    {
        //arrange
        baseConstructorExpectations();
        basePublishExpectations(mockedMessage);
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
        Deencapsulation.invoke(mockMqtt, "publish", MOCK_PARSE_TOPIC, mockedMessage);

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
    **Tests_SRS_Mqtt_25_013: [If the either publishTopic or payload is null or empty, the function shall throw an IllegalArgumentException.]
     */
    @Test(expected = IllegalArgumentException.class)
    public void publishThrowsExceptionWhenPublishTopicIsNull(final @Mocked Message mockedMessage) throws TransportException
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
        Deencapsulation.invoke(mockMqtt, "publish", String.class, payload, mockedMessage);

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
    **Tests_SRS_Mqtt_25_013: [If the either publishTopic or payload is null or empty, the function shall throw an IllegalArgumentException.]
    */
    @Test(expected = IllegalArgumentException.class)
    public void publishThrowsExceptionWhenPayloadIsNull(final @Mocked Message mockedMessage) throws TransportException
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
        Deencapsulation.invoke(mockMqtt, "publish", MOCK_PARSE_TOPIC, byte[].class, mockedMessage);

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
    **Tests_SRS_Mqtt_25_017: [The function shall subscribe to subscribeTopic specified to the IoT Hub given in the configuration.]
     */
    @Test
    public void subscribeSucceeds() throws MqttException, TransportException
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
    **Tests_SRS_Mqtt_25_015: [If the MQTT connection is closed, the function shall throw a TransportException with message.]
     */
    @Test(expected = TransportException.class)
    public void subscribeFailsWhenNotConnected() throws TransportException
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

    @Test(expected = TransportException.class)
    public void subscribeFailsWhenConfigIsNotSet() throws TransportException
    {
        //arrange
        baseConstructorExpectations();
        Mqtt mockMqtt = instantiateMqtt(false);

        //act
        Deencapsulation.invoke(mockMqtt, "subscribe", MOCK_PARSE_TOPIC);
    }

    /*
    **Tests_SRS_Mqtt_25_016: [If the subscribeTopic is null or empty, the function shall throw an IllegalArgumentException.]
     */
    @Test(expected = IllegalArgumentException.class)
    public void subscribeThrowsExceptionWhenTopicIsNull() throws TransportException
    {
        //arrange
        baseConstructorExpectations();

        Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Deencapsulation.invoke(mockMqtt, "subscribe", String.class);
    }

    /*
    **Tests_SRS_Mqtt_99_049: [If the user supplied SAS token has expired, the function shall throw a TransportException.]
     */
    @Test(expected = TransportException.class)
    public void subscribeThrowsExceptionWhenUserSuppliedSASTokenHasExpired() throws TransportException
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
    **Tests_SRS_Mqtt_25_048: [If the Mqtt Client Async throws MqttException for any reason, the function shall throw a ProtocolException with the message.]
     */
    @Test(expected = ProtocolException.class)
    public void subscribeThrowsProtocolConnectionExceptionWhenMqttAsyncThrows() throws TransportException, MqttException
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
    public void receiveSuccess() throws MqttException, TransportException
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

        final Mqtt mockMqtt = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "", "", false);
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

    //Tests_SRS_Mqtt_34_050: [This function shall extract the inputName from the topic if the topic string fits the following convention: 'devices/<deviceId>/modules/<moduleId>/inputs/<inputName>']
    //Tests_SRS_Mqtt_34_051: [This function shall extract the moduleId from the topic if the topic string fits the following convention: 'devices/<deviceId>/modules/<moduleId>']
    @Test
    public void receiveSuccessWithModuleIdAndInputName() throws MqttException, TransportException
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
                return new MutablePair<>(MOCK_PARSE_TOPIC_WITH_INPUT_NAME, payload);
            }
        };

        final Mqtt mockMqtt = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "", "", false);
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
        assertEquals(expectedInputName, receivedMessage.getInputName());
        assertEquals(expectedModuleId, receivedMessage.getConnectionModuleId());
    }

    // Tests_SRS_Mqtt_34_022: [If the call peekMessage returns a null or empty string then this method shall do nothing and return null]
    @Test
    public void receiveReturnsNullMessageWhenParseTopicReturnsNull() throws TransportException, MqttException
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
        final Mqtt mockMqtt = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "", "", false);

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

    // Tests_SRS_Mqtt_34_025: [If the call to peekMessage returns null when topic is non-null then this method will throw a TransportException]
    @Test(expected = TransportException.class)
    public void receiveThrowsIotHubServiceExceptionWhenParsePayloadReturnsNull() throws TransportException
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

        final Mqtt mockMqtt = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "", "", false);
        //act
        Message receivedMessage = mockMqtt.receive();
    }

    //Tests_SRS_Mqtt_25_002: [The constructor shall throw a TransportException if mqttConnection is null .]
    @Test(expected = TransportException.class)
    public void receiveThrowsExceptionWhenConfigurationIsNotSet() throws TransportException
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

        final Mqtt mockMqtt = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "", "", false);
        Deencapsulation.setField(mockMqtt, "mqttConnection", null);

        //act
        Message receivedMessage = mockMqtt.receive();
    }

    //Tests_SRS_Mqtt_25_030: [The payload of the message and the topic is added to the received messages queue .]
    //Tests_SRS_Mqtt_34_045: [If there is a saved listener, this function shall notify that listener that a message arrived.]
    @Test
    public void messageArrivedAddsToQueue() throws TransportException, MqttException
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

        new Verifications()
        {
            {
                mockedMessageListener.onMessageArrived(anyInt);
                times = 1;
            }
        };
    }

    //Tests_SRS_Mqtt_34_045: [If this object has a saved listener, this function shall notify the listener that connection was lost.]
    @Test
    public void connectionLostAttemptsToReconnectWithSASTokenStillValid() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        final Throwable t = new Throwable();
        baseConstructorExpectations();

        new StrictExpectations()
        {
            {
                mockedIotHubListener.onConnectionLost((TransportException) any, anyString);
            }
        };

        //act
        try
        {
            mockMqtt = instantiateMqtt(true, mockedIotHubListener);
            mockMqtt.connectionLost(t);
        }
        catch (Exception e)
        {
            fail("Completed throwing exception - " + e.getCause() + e.getMessage());
        }
    }

    // Tests_SRS_Mqtt_34_021: [If the call peekMessage returns null then this method shall do nothing and return null]
    @Test
    public void receiveReturnsNullMessageIfTopicNotFound() throws TransportException
    {
        //can't be initialized to null, so set it as a default message
        baseConstructorExpectations();
        Message receivedMessage = new Message();
        try
        {
            //arrange
            MqttMessaging testMqttClient = new MqttMessaging(mockedMqttConnection,"deviceId", mockedIotHubListener, null, "", "", false);
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
    **Tests_SRS_Mqtt_34_051: [If a topic string's property's key and value are not separated by the '=' symbol, an IllegalArgumentException shall be thrown]
     */
    @Test (expected = IllegalArgumentException.class)
    public void receiveFailureFromInvalidPropertyStringThrowsIllegalArgumentException() throws MqttException, IllegalArgumentException, TransportException
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

        final Mqtt mockMqtt = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "", "", false);
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
    **Test_SRS_Mqtt_34_054: [A message may have 0 to many custom properties]
    */
    @Test
    public void receiveSuccessNoCustomProperties() throws TransportException, MqttException
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

        final Mqtt mockMqtt = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "", "", false);

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
    **Tests_SRS_Mqtt_34_053: [A property's key and value may include unusual characters such as &, %, $]
    */
    @Test
    public void receiveSuccessCustomPropertyHasUnusualCharacters() throws TransportException, MqttException
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

        final Mqtt mockMqtt = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "", "", false);
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

    //Tests_SRS_Mqtt_34_057: [This function shall parse the messageId, correlationId, outputname, content encoding and content type from the provided property string]
    @Test
    public void receiveSuccessWithSystemProperties() throws TransportException, MqttException, UnsupportedEncodingException
    {
        //arrange
        final byte[] payload = {0x61, 0x62, 0x63};
        final String msgId = "69ea4caf-d83e-454b-81f2-caafda4c81c8";
        final String corId = "169c34b3-99b0-49f9-b0f6-8fa9d2c99345";
        final String expTime = "1234";
        final String contentEncoding = "utf-8";
        final String contentType = "application/json";
        final String outputName = "outputChannel1";
        final String to = "/devices/deviceID/messages/deviceBound";
        final String mockParseTopicWithUnusualCharacters = "devices/deviceID/messages/devicebound/%24.mid=" + msgId + "&%24.exp=" + expTime + "&%24.to=" + URLEncoder.encode(to, StandardCharsets.UTF_8.name()) + "&%24.cid=" + corId + "&iothub-ack=full&%24.ce=" + contentEncoding + "&%24.ct=" + URLEncoder.encode(contentType, StandardCharsets.UTF_8.name()) + "&%24.on=" + outputName + "&property1=%24&property2=%26&%25=%22&finalProperty=%3d";
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

        final Mqtt mockMqtt = new MqttMessaging(mockedMqttConnection, CLIENT_ID, mockedIotHubListener, null, "", "", false);
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

        assertEquals(msgId, receivedMessage.getMessageId());
        assertEquals(corId, receivedMessage.getCorrelationId());
        assertEquals(contentEncoding, receivedMessage.getContentEncoding());
        assertEquals(contentType, receivedMessage.getContentType());
        assertEquals(outputName, receivedMessage.getOutputName());
    }

    //Tests_SRS_Mqtt_34_037: [If the provided throwable is an instance of MqttException, this function shall derive the associated TransportException and notify the listeners of that derived exception.]
    @Test
    public void connectionDropFiresCallbackWithMqttException() throws TransportException
    {
        //arrange
        baseConstructorExpectations();
        Mqtt mockMqtt = instantiateMqtt(true, mockedIotHubListener);
        Deencapsulation.setField(mockMqtt, "mqttConnection", null);

        //act
        mockMqtt.connectionLost(new MqttException(404));

        //assert
        new Verifications()
        {
            {
                mockedIotHubListener.onConnectionLost((TransportException) any, anyString);
                times = 1;
            }
        };
    }

    //Tests_SRS_Mqtt_34_042: [If this object has a saved listener, that listener shall be notified of the successfully delivered message.]
    @Test
    public void deliveryCompleteNotifiesListener() throws TransportException
    {
        //arrange
        final int expectedMessageId = 13;
        baseConstructorExpectations();
        final Message otherMessage = new Message();
        final Message expectedMessage = new Message();
        Mqtt mockMqtt = instantiateMqtt(true, mockedIotHubListener);
        Map<Integer, Message> unacknowledgedMessages = new HashMap<>();
        unacknowledgedMessages.put(12, otherMessage);
        unacknowledgedMessages.put(expectedMessageId, expectedMessage);
        Deencapsulation.setField(mockMqtt, "unacknowledgedSentMessages", unacknowledgedMessages);
        new NonStrictExpectations()
        {
            {
                mockMqttDeliveryToken.getMessageId();
                result = expectedMessageId;
            }
        };

        //act
        mockMqtt.deliveryComplete(mockMqttDeliveryToken);

        //assert
        new Verifications()
        {
            {
                mockedIotHubListener.onMessageSent(expectedMessage, null);
                times = 1;
                mockedIotHubListener.onMessageSent(otherMessage, null);
                times = 0;
            }
        };
    }

    //Tests_SRS_Mqtt_34_056: [If the acknowledged message is of type
    // DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST, DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST,
    // or DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST, this function shall not notify the saved
    // listener that the message was sent.]
    @Test
    public void deliveryCompleteDoesNotNotifyListenerIfSubscribeToDesiredProperties() throws TransportException
    {
        //arrange
        final int expectedMessageId = 13;
        baseConstructorExpectations();
        final Message otherMessage = new Message();
        final IotHubTransportMessage expectedMessage = new IotHubTransportMessage("some body");
        expectedMessage.setDeviceOperationType(DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST);
        Mqtt mockMqtt = instantiateMqtt(true, mockedIotHubListener);
        Map<Integer, Message> unacknowledgedMessages = new HashMap<>();
        unacknowledgedMessages.put(12, otherMessage);
        unacknowledgedMessages.put(expectedMessageId, expectedMessage);
        Deencapsulation.setField(mockMqtt, "unacknowledgedSentMessages", unacknowledgedMessages);
        new NonStrictExpectations()
        {
            {
                mockMqttDeliveryToken.getMessageId();
                result = expectedMessageId;
            }
        };

        //act
        mockMqtt.deliveryComplete(mockMqttDeliveryToken);

        //assert
        new Verifications()
        {
            {
                mockedIotHubListener.onMessageSent(expectedMessage, null);
                times = 0;
            }
        };
    }

    //Tests_SRS_Mqtt_34_056: [If the acknowledged message is of type
    // DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST, DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST,
    // or DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST, this function shall not notify the saved
    // listener that the message was sent.]
    @Test
    public void deliveryCompleteDoesNotNotifyListenerIfSubscribeToMethods() throws TransportException
    {
        //arrange
        final int expectedMessageId = 13;
        baseConstructorExpectations();
        final Message otherMessage = new Message();
        final IotHubTransportMessage expectedMessage = new IotHubTransportMessage("some body");
        expectedMessage.setDeviceOperationType(DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST);
        Mqtt mockMqtt = instantiateMqtt(true, mockedIotHubListener);
        Map<Integer, Message> unacknowledgedMessages = new HashMap<>();
        unacknowledgedMessages.put(12, otherMessage);
        unacknowledgedMessages.put(expectedMessageId, expectedMessage);
        Deencapsulation.setField(mockMqtt, "unacknowledgedSentMessages", unacknowledgedMessages);
        new NonStrictExpectations()
        {
            {
                mockMqttDeliveryToken.getMessageId();
                result = expectedMessageId;
            }
        };

        //act
        mockMqtt.deliveryComplete(mockMqttDeliveryToken);

        //assert
        new Verifications()
        {
            {
                mockedIotHubListener.onMessageSent(expectedMessage, null);
                times = 0;
            }
        };
    }

    //Tests_SRS_Mqtt_34_056: [If the acknowledged message is of type
    // DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST, DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST,
    // or DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST, this function shall not notify the saved
    // listener that the message was sent.]
    @Test
    public void deliveryCompleteDoesNotNotifyListenerIfUnsubscribeToDesiredProperties() throws TransportException
    {
        //arrange
        final int expectedMessageId = 13;
        baseConstructorExpectations();
        final Message otherMessage = new Message();
        final IotHubTransportMessage expectedMessage = new IotHubTransportMessage("some body");
        expectedMessage.setDeviceOperationType(DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST);
        Mqtt mockMqtt = instantiateMqtt(true, mockedIotHubListener);
        Map<Integer, Message> unacknowledgedMessages = new HashMap<>();
        unacknowledgedMessages.put(12, otherMessage);
        unacknowledgedMessages.put(expectedMessageId, expectedMessage);
        Deencapsulation.setField(mockMqtt, "unacknowledgedSentMessages", unacknowledgedMessages);
        new NonStrictExpectations()
        {
            {
                mockMqttDeliveryToken.getMessageId();
                result = expectedMessageId;
            }
        };

        //act
        mockMqtt.deliveryComplete(mockMqttDeliveryToken);

        //assert
        new Verifications()
        {
            {
                mockedIotHubListener.onMessageSent(expectedMessage, null);
                times = 0;
            }
        };
    }

    //Tests_SRS_Mqtt_34_043: [This function shall invoke the saved mqttConnection object to send the message acknowledgement for the provided messageId and return that result.]
    @Test
    public void sendMessageAcknowledgementSendsMessageAcknowledgement() throws TransportException
    {
        //arrange
        final int expectedMessageId = 134;
        Mqtt mockMqtt = instantiateMqtt(true);
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedMqttConnection, "sendMessageAcknowledgement", expectedMessageId);
                result = true;
            }
        };

        //act
        boolean result = Deencapsulation.invoke(mockMqtt, "sendMessageAcknowledgement", expectedMessageId);

        //assert
        assertTrue(result);
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedMqttConnection, "sendMessageAcknowledgement", expectedMessageId);
                times = 1;
            }
        };
    }

    //Tests_SRS_Mqtt_25_011: [If an MQTT connection is unable to be closed for any reason, the function shall throw a TransportException.]
    @Test (expected = TransportException.class)
    public void mqttDisconnectThrowsMqttExceptionHandled() throws TransportException
    {
        //arrange
        final Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.setField(mockMqtt, "mqttConnection", mockedMqttConnection);
        final MqttException mqttException = new MqttException(new Throwable());
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedMqttConnection, "isConnected");
                result = mqttException;
            }
        };

        //act
        Deencapsulation.invoke(mockMqtt, "disconnect");
    }

    //Tests_SRS_Mqtt_34_044: [If an MqttException is encountered while connecting, this function shall throw the associated ProtocolException.]
    @Test (expected = ProtocolException.class)
    public void mqttConnectThrowsMqttExceptionHandled() throws TransportException
    {
        //arrange
        final Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.setField(mockMqtt, "mqttConnection", mockedMqttConnection);
        Deencapsulation.setField(mockMqtt, "mqttLock", new Object());

        final MqttException mqttException = new MqttException(new Throwable());
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedMqttConnection, "getMqttAsyncClient");
                result = mqttException;
            }
        };

        //act
        Deencapsulation.invoke(mockMqtt, "connect");
    }
}