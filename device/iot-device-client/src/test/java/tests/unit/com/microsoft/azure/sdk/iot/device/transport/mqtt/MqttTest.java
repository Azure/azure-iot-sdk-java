// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.
package tests.unit.com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.Mqtt;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttDeviceTwin;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttMessaging;

import mockit.*;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.Assert.*;

/**
 * Unit test for Mqtt class.
 * 90% methods, 86% lines covered
 */
public class MqttTest {
    final String serverUri = "test.host.name";
    final String clientId = "test.iothub";
    final String userName = "test-deviceId";
    final String password = "test-devicekey?&test";
    final String mockParseTopic = "devices/deviceID/messages/devicebound/%24.mid=69ea4caf-d83e-454b-81f2-caafda4c81c8&%24.exp=0&%24.to=%2Fdevices%2FdeviceID%2Fmessages%2FdeviceBound&%24.cid=169c34b3-99b0-49f9-b0f6-8fa9d2c99345&iothub-ack=full&property1=value1";
    final byte[] expectedPayload = {0x61, 0x62, 0x63};
    static Message expectedMessage;


    @Mocked
    private MqttAsyncClient mockMqttAsyncClient;

    @Mocked
    private MqttConnectOptions mockMqttConnectionOptions;

    @Mocked
    private MemoryPersistence mockMemoryPersistence;

    @Mocked
    private IMqttToken mockMqttToken;

    @Mocked
    protected IMqttDeliveryToken mockMqttDeliveryToken;

    @Mocked
    protected MqttException mockMqttException;

    @Mocked
    protected MqttMessage mockMqttMessage;

    @Mocked
    IotHubSSLContext mockIotHubSSLContext;

    @Mocked
    protected IotHubSasToken mockSASToken;

    @Mocked
    protected DeviceClientConfig mockDeviceClientConfig;


    @Before
    public void setUp() {
        expectedMessage = new Message(expectedPayload);
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
                    return new MutablePair<>(mockParseTopic, new byte[0]);
                }
            };
            return new MqttMessaging(serverUri, clientId, userName, password, mockIotHubSSLContext);
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
                    return new MutablePair<>(mockParseTopic, new byte[0]);
                }
            };
            return new MqttDeviceTwin();
        }
    }

    private void baseConstructorExpectations(boolean withParameter) throws MqttException
    {
        if (withParameter)
        {
            new NonStrictExpectations()
            {
                {
                    new MemoryPersistence();
                    result = mockMemoryPersistence;
                    new MqttAsyncClient(serverUri, clientId, mockMemoryPersistence);
                    result = mockMqttAsyncClient;
                    new MqttConnectOptions();
                    result = mockMqttConnectionOptions;
                }
            };
        }
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
                mockMqttAsyncClient.publish(mockParseTopic, mockMqttMessage);
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
                mockMqttAsyncClient.publish(mockParseTopic, mockMqttMessage);
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
        if (withParameter)
        {
            new Verifications()
            {
                {
                    new MqttAsyncClient(serverUri, clientId, mockMemoryPersistence);
                    times = 1;
                    mockMqttAsyncClient.setCallback((Mqtt) any);
                    times = 1;
                    mockMqttConnectionOptions.setKeepAliveInterval(anyInt);
                    times = 1;
                    mockMqttConnectionOptions.setCleanSession(anyBoolean);
                    times = 1;
                    mockMqttConnectionOptions.setMqttVersion(anyInt);
                    times = 1;
                    mockMqttConnectionOptions.setUserName(userName);
                    times = 1;
                    mockMqttConnectionOptions.setPassword(password.toCharArray());
                    times = 1;
                    mockMqttConnectionOptions.setSocketFactory(mockIotHubSSLContext.getIotHubSSlContext().getSocketFactory());
                    times = 1;
                    new ConcurrentSkipListMap<>();
                    times = 1;
                    new Object();
                    times = 1;
                }

            };
        }
        else
        {
            new Verifications()
            {
                {

                }

            };

        }
    }

    private void testCleanUp(Mqtt mockMqtt)
    {
        if (mockMqtt != null)
        {
            mockMqtt.restartBaseMqtt();
        }
    }

    /*
    ** Tests_SRS_Mqtt_25_004: [**If an instance of the inner class MqttConnectionInfo is already created than it shall return doing nothing.**]**
     */
    @Test
    public void manyExtendsOfAbstractClassDoesNotChangeConfig() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations(true);
        baseConstructorExpectations(false);

        //act
        Mqtt mockMqtt1 = instantiateMqtt(true);
        Object actualInfoInstance1 = Deencapsulation.getField(mockMqtt1, "info");
        Queue<Pair<String, byte[]>> actualQueue1 = Deencapsulation.getField(mockMqtt1, "allReceivedMessages");
        Object actualLock1 = Deencapsulation.getField(mockMqtt1, "MQTT_LOCK");

        Mqtt mockMqtt2 = instantiateMqtt(false);
        Object actualInfoInstance2 = Deencapsulation.getField(mockMqtt2, "info");
        Queue<Pair<String, byte[]>> actualQueue2 = Deencapsulation.getField(mockMqtt2, "allReceivedMessages");
        Object actualLock2 = Deencapsulation.getField(mockMqtt2, "MQTT_LOCK");

        //assert
        assertEquals(actualInfoInstance1, actualInfoInstance2);
        assertEquals(actualQueue1, actualQueue2);
        assertEquals(actualLock1, actualLock2);

        baseConstructorVerifications(true);
        baseConstructorVerifications(false);

        //cleanup
        testCleanUp(mockMqtt1);
    }

    /*
    **Tests_SRS_Mqtt_25_003: [**The constructor shall use the configuration to instantiate an instance of the inner class MqttConnectionInfo if not already created.**]**
     */
    @Test
    public void constructorInitialisesWithConfig() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations(true);

        //act
        Mqtt mockMqtt = instantiateMqtt(true);

        //assert
        Object actualInfo = Deencapsulation.getField(mockMqtt, "info");
        assertNotNull(actualInfo);
        MqttAsyncClient actualAsyncClient = Deencapsulation.getField(actualInfo, "mqttAsyncClient");
        assertNotNull(actualAsyncClient);
        MqttConnectOptions actualConnectionOptions = Deencapsulation.getField(actualInfo, "connectionOptions");
        assertNotNull(actualConnectionOptions);
        Queue<Pair<String, byte[]>> actualQueue = Deencapsulation.getField(mockMqtt, "allReceivedMessages");
        assertNotNull(actualQueue);
        Object actualLock = Deencapsulation.getField(mockMqtt, "MQTT_LOCK");
        assertNotNull(actualLock);

        baseConstructorVerifications(true);
        mockMqtt.restartBaseMqtt();
    }

    /*
    **Tests_SRS_Mqtt_25_045: [**The constructor throws IOException if MqttException is thrown and doesn't instantiate this instance.**]**
     */
    @Test(expected = IOException.class)
    public void constructorThrowsExceptionIfMqttAsyncClientFails() throws IOException, MqttException
    {
        Mqtt mockMqtt = null;
        try {
            //arrange
            new NonStrictExpectations()
            {
                {
                    new MemoryPersistence();
                    result = mockMemoryPersistence;
                    new MqttAsyncClient(serverUri, clientId, mockMemoryPersistence);
                    result = mockMqttException;
                }
            };

            //act
            mockMqtt = instantiateMqtt(true);
        }
        finally
        {
            testCleanUp(mockMqtt);
        }
    }

    /*
        Tests_SRS_Mqtt_25_002: [**The constructor shall throw InvalidParameter Exception if any of the parameters are null or empty .**]**
    */
    @Test(expected = InvalidParameterException.class)
    public void constructorThrowsInvalidParameterExceptionOnInvalidInput() throws IOException, MqttException
    {
        Mqtt mockMqtt = null;
        try
        {
            //act
            mockMqtt = new MqttMessaging(null, clientId, userName, password,  mockIotHubSSLContext);
        }
        finally
        {
            testCleanUp(mockMqtt);
        }

    }

    /*
    **Tests_SRS_Mqtt_25_001: [**The constructor shall instantiate MQTT lock for using base class.**]**
    */
    @Test
    public void constructorDoesntInitiliaseConfigWithoutParameters() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations(false);

        //act
        Mqtt mockMqtt = instantiateMqtt(false);

        //assert
        Object actualInfoInstance = Deencapsulation.getField(mockMqtt, "info");
        assertNull(actualInfoInstance);
        Queue<Pair<String, byte[]>> actualQueue = Deencapsulation.getField(mockMqtt, "allReceivedMessages");
        assertNull(actualQueue);

        Object actualLock = Deencapsulation.getField(mockMqtt, "MQTT_LOCK");
        assertNotNull(actualLock);
        baseConstructorVerifications(false);
        mockMqtt.restartBaseMqtt();
    }

    /*
    ** Tests_SRS_Mqtt_25_004: [**If an instance of the inner class MqttConnectionInfo is already created than it shall return doing nothing.**]**
     */
    @Test
    public void constructorWithParametersIfCalledMultipleTimesDoesntReinitialize() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations(true);
        baseConstructorExpectations(true);

        //act
        Mqtt mockMqtt1 = instantiateMqtt(true);

        //assert
        Object actualInfoInstance1 = Deencapsulation.getField(mockMqtt1, "info");
        Queue<Pair<String, byte[]>> actualQueue1 = Deencapsulation.getField(mockMqtt1, "allReceivedMessages");
        Mqtt mockMqtt2 = instantiateMqtt(false);
        Object actualInfoInstance2 = Deencapsulation.getField(mockMqtt2, "info");
        Queue<Pair<String, byte[]>> actualQueue2 = Deencapsulation.getField(mockMqtt2, "allReceivedMessages");

        Object actualLock1 = Deencapsulation.getField(mockMqtt1, "MQTT_LOCK");
        Object actualLock2 = Deencapsulation.getField(mockMqtt2, "MQTT_LOCK");


        assertEquals(actualInfoInstance1, actualInfoInstance2);
        assertEquals(actualQueue1, actualQueue2);
        assertEquals(actualLock1, actualLock2);

        baseConstructorVerifications(true);
        baseConstructorVerifications(false);
        mockMqtt1.restartBaseMqtt();

    }

    /*
    **Tests_SRS_Mqtt_25_005: [**The function shall establish an MQTT connection with an IoT Hub using the provided host name, user name, device ID, and sas token.**]**
     */
    @Test
    public void connectSuccess() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations(true);
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
        mockMqtt.restartBaseMqtt();
    }

    /*
    **Tests_SRS_Mqtt_25_008: [**If the MQTT connection is already open, the function shall do nothing.**]**
     */
    @Test
    public void connectDoesNothingIfAlreadyConnected() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations(true);
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
        mockMqtt.restartBaseMqtt();
    }

    /*
    **Tests_SRS_Mqtt_25_006: [**If the inner class MqttConnectionInfo has not been instantiated then the function shall throw IOException.**]**
     */
    @Test(expected = IOException.class)
    public void connectFailsIfNoConfigIsProvided() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        try
        {
            baseConstructorExpectations(false);
            mockMqtt = instantiateMqtt(false);

            //act
            Deencapsulation.invoke(mockMqtt, "connect");

        }
        finally
        {
            mockMqtt.restartBaseMqtt();
        }

    }

    /*
    **Tests_SRS_Mqtt_25_007: [**If an MQTT connection is unable to be established for any reason, the function shall throw an IOException.**]**
     */
    @Test(expected = IOException.class)
    public void connectThrowsIoExceptionOnMqttException() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        try
        {
            baseConstructorExpectations(true);
            new NonStrictExpectations()
            {
                {
                    mockMqttAsyncClient.isConnected();
                    result = false;
                    mockMqttAsyncClient.connect(mockMqttConnectionOptions);
                    result = mockMqttException;
                }
            };
            mockMqtt = instantiateMqtt(true);

            //act
            Deencapsulation.invoke(mockMqtt, "connect");

            //assert
            baseConnectVerifications();
        }
        finally
        {
            testCleanUp(mockMqtt);
        }

    }

    /*
    **Tests_SRS_Mqtt_25_009: [**The function shall close the MQTT connection.**]**
     */
    @Test
    public void disconnectSucceeds() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        try
        {
            baseConstructorExpectations(true);
            baseConnectExpectation();
            baseDisconnectExpectations();
            mockMqtt = instantiateMqtt(true);
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

            Object actualInfoInstance = Deencapsulation.getField(mockMqtt, "info");
            MqttAsyncClient actualMqttAsyncClient = Deencapsulation.getField(actualInfoInstance, "mqttAsyncClient");
            assertNull(actualMqttAsyncClient);
        }
        finally
        {
            testCleanUp(mockMqtt);
        }
    }

    /*
    **Tests_SRS_Mqtt_25_010: [**If the MQTT connection is closed, the function shall do nothing.**]**
     */
    @Test
    public void disconnectDoesNothingWhenNotConnected() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations(true);
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

        Object actualInfoInstance = Deencapsulation.getField(mockMqtt, "info");
        MqttAsyncClient actualMqttAsyncClient = Deencapsulation.getField(actualInfoInstance, "mqttAsyncClient");
        assertNull(actualMqttAsyncClient);
        testCleanUp(mockMqtt);
    }

    /*
    **Tests_SRS_Mqtt_25_014: [**The function shall publish message payload on the publishTopic specified to the IoT Hub given in the configuration.**]**
     */
    @Test
    public void publishSucceedsWhenConnected() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations(true);
        baseConnectExpectation();
        basePublishExpectations();

        final byte[] payload = {0x61, 0x62, 0x63};
        Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Deencapsulation.invoke(mockMqtt, "publish", mockParseTopic, payload );

        //assert
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                minTimes = 2;
                mockMqttAsyncClient.publish(mockParseTopic, mockMqttMessage);
                times = 1;
            }
        };
        testCleanUp(mockMqtt);
    }

    /*
    **Tests_SRS_Mqtt_99_049: [**If the user supplied SAS token has expired, the function shall throw an IOException.**]**
     */
    @Test (expected = IOException.class) 
    public void publishThrowsExceptionifUserSuppliedSASTokenHasExpired() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        try
        {
            baseConstructorExpectations(true);
            final byte[] payload = {0x61, 0x62, 0x63};
           
            mockMqtt = instantiateMqtt(true);
            
            //act
            Deencapsulation.setField(mockMqtt,"userSpecifiedSASTokenExpiredOnRetry",true);
            Deencapsulation.invoke(mockMqtt, "publish", mockParseTopic, payload);
            
        }
        finally
        {
            testCleanUp(mockMqtt);
        }
    }

    /*
    **Tests_SRS_Mqtt_25_012: [**If the MQTT connection is closed, the function shall throw an IOException.**]**
     */
    @Test(expected = IOException.class)
    public void publishFailsWhenNotConnected() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        try
        {
            baseConstructorExpectations(true);
            final byte[] payload = {0x61, 0x62, 0x63};
            new NonStrictExpectations()
            {
                {
                    mockMqttAsyncClient.isConnected();
                    result = false;
                }
            };
            mockMqtt = instantiateMqtt(true);

            //act
            Deencapsulation.invoke(mockMqtt, "publish", mockParseTopic, payload);
        }
        finally
        {
            testCleanUp(mockMqtt);
        }
    }

    /*
    **Tests_SRS_Mqtt_25_012: [**If the MQTT connection is closed, the function shall throw an IOException.**]**
    */
    @Test (expected = IOException.class)
    public void publishFailsWhenConnectionBrokenWhilePublishing() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        try
        {
            baseConstructorExpectations(true);
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
            mockMqtt = instantiateMqtt(true);

            //act
            Deencapsulation.invoke(mockMqtt, "publish", mockParseTopic, payload);
        }
        finally
        {
            testCleanUp(mockMqtt);
        }
    }


    /*
    **Tests_SRS_Mqtt_25_014: [**The function shall publish message payload on the publishTopic specified to the IoT Hub given in the configuration.**]**
     */
    @Test
    public void publishWithDifferentTopicsFromDifferentConcreteClassSucceeeds() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations(true);
        baseConstructorExpectations(false);
        baseConnectExpectation();
        basePublishExpectations();
        basePublishExpectations();

        final byte[] payload = {0x61, 0x62, 0x63};
        String mockParseTopic2 = mockParseTopic + 2;
        Mqtt mockMqtt1 = instantiateMqtt(true);
        Mqtt mockMqtt2 = instantiateMqtt(false);
        Deencapsulation.invoke(mockMqtt2, "connect");

        //act
        Deencapsulation.invoke(mockMqtt1, "publish", mockParseTopic, payload);
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
        testCleanUp(mockMqtt1);
    }

    /*
     **Tests_SRS_Mqtt_25_047: [**If the Mqtt Client Async throws MqttException, the function shall throw an IOException with the message.**]**
     */
    @Test(expected = IOException.class)
    public void publishThrowsIOExceptionWhenAnyOfTheAsyncMethodsThrow() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        try
        {

            baseConstructorExpectations(true);
            final byte[] payload = {0x61, 0x62, 0x63};
            new NonStrictExpectations()
            {
                {
                    mockMqttAsyncClient.isConnected();
                    result = true;
                    new MqttMessage(payload);
                    result = mockMqttMessage;
                    mockMqttAsyncClient.publish(mockParseTopic, mockMqttMessage);
                    result = mockMqttException;
                }
            };
            mockMqtt = instantiateMqtt(true);
            Deencapsulation.invoke(mockMqtt, "connect");

            //act
            Deencapsulation.invoke(mockMqtt, "publish", mockParseTopic, payload);

            //assert
            new Verifications()
            {
                {
                    mockMqttAsyncClient.isConnected();
                    minTimes = 1;
                }
            };
        }
        finally
        {
            testCleanUp(mockMqtt);
        }
    }

    /*
    **Tests_SRS_Mqtt_25_013: [**If the either publishTopic or payload is null or empty, the function shall throw an IOException.**]**
     */
    @Test(expected = IOException.class)
    public void publishThrowsExceptionWhenPublishTopicIsNull() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        try
        {
            baseConstructorExpectations(true);
            final byte[] payload = {0x61, 0x62, 0x63};
            new NonStrictExpectations()
            {
                {
                    mockMqttAsyncClient.isConnected();
                    result = true;
                }
            };
            mockMqtt = instantiateMqtt(true);
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
        finally
        {
            testCleanUp(mockMqtt);
        }
    }

    /*
    **Tests_SRS_Mqtt_25_013: [**If the either publishTopic or payload is null or empty, the function shall throw an IOException.**]**
    */
    @Test(expected = IOException.class)
    public void publishThrowsExceptionWhenPayloadIsNull() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        try
        {
            baseConstructorExpectations(true);
            final byte[] payload = null;
            new NonStrictExpectations()
            {
                {
                    mockMqttAsyncClient.isConnected();
                    result = true;
                }
            };
            mockMqtt = instantiateMqtt(true);
            Deencapsulation.invoke(mockMqtt, "connect");

            //act
            Deencapsulation.invoke(mockMqtt, "publish", mockParseTopic, byte[].class);

            //assert
            new Verifications()
            {
                {
                    mockMqttAsyncClient.isConnected();
                    minTimes = 1;
                }
            };
        }
        finally
        {
            testCleanUp(mockMqtt);
        }
    }

    /*
    **Tests_SRS_Mqtt_25_017: [**The function shall subscribe to subscribeTopic specified to the IoT Hub given in the configuration.**]**
     */
    @Test
    public void subscribeSucceeds() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations(true);
        baseConnectExpectation();
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = true;
                mockMqttAsyncClient.subscribe(mockParseTopic, anyInt);
                result = mockMqttToken;
            }
        };
        Mqtt mockMqtt = instantiateMqtt(true);
        Deencapsulation.invoke(mockMqtt, "connect");

        //act
        Deencapsulation.invoke(mockMqtt, "subscribe", mockParseTopic);

        //assert
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                minTimes = 1;
                mockMqttAsyncClient.subscribe(mockParseTopic, anyInt);
                times = 1;
            }
        };
        testCleanUp(mockMqtt);
    }

    /*
    **Tests_SRS_Mqtt_25_015: [**If the MQTT connection is closed, the function shall throw an IOexception with message.**]**
     */
    @Test(expected = IOException.class)
    public void subscribeFailsWhenNotConnected() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        try
        {
            baseConstructorExpectations(true);
            new NonStrictExpectations()
            {
                {
                    mockMqttAsyncClient.isConnected();
                    result = false;
                }
            };

            mockMqtt = instantiateMqtt(true);

            //act
            Deencapsulation.invoke(mockMqtt, "subscribe", mockParseTopic);

            //assert
            new Verifications()
            {
                {
                    mockMqttAsyncClient.isConnected();
                    minTimes = 1;
                }
            };
        }
        finally
        {
            testCleanUp(mockMqtt);
        }
    }

    @Test(expected = IOException.class)
    public void subscribeFailsWhenConfigIsNotSet() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        try
        {
            baseConstructorExpectations(false);
            mockMqtt = instantiateMqtt(false);

            //act
            Deencapsulation.invoke(mockMqtt, "subscribe", mockParseTopic);
        }
        finally
        {
            testCleanUp(mockMqtt);
        }
    }

    /*
    **Tests_SRS_Mqtt_25_016: [**If the subscribeTopic is null or empty, the function shall throw an InvalidParameter Exception.**]**
     */
    @Test(expected = InvalidParameterException.class)
    public void subscribeThrowsExceptionWhenTopicIsNull() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        try
        {
            baseConstructorExpectations(true);

            mockMqtt = instantiateMqtt(true);
            Deencapsulation.invoke(mockMqtt, "connect");

            //act
            Deencapsulation.invoke(mockMqtt, "subscribe", String.class);
        }
        finally
        {
            testCleanUp(mockMqtt);
        }
    }

    /*
    **Tests_SRS_Mqtt_99_049: [**If the user supplied SAS token has expired, the function shall throw an IOException.**]**
     */
    @Test(expected = IOException.class)
    public void subscribeThrowsExceptionWhenUserSuppliedSASTokenHasExpired() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        try
        {
            baseConstructorExpectations(true);

            mockMqtt = instantiateMqtt(true);
            Deencapsulation.invoke(mockMqtt, "connect");

            //act
            Deencapsulation.setField(mockMqtt,"userSpecifiedSASTokenExpiredOnRetry",true);
            Deencapsulation.invoke(mockMqtt, "subscribe", mockParseTopic);
        }
        
        finally
        {
            testCleanUp(mockMqtt);
        }
    }
    
    /*
    **Tests_SRS_Mqtt_25_048: [**If the Mqtt Client Async throws MqttException for any reason, the function shall throw an IOException with the message.**]**
     */
    @Test(expected = IOException.class)
    public void subscribeThrowsIOExceptionWhenMqttAsyncThrows() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        try
        {

            baseConstructorExpectations(true);
            baseConnectExpectation();

            new NonStrictExpectations()
            {
                {
                    mockMqttAsyncClient.isConnected();
                    result = true;
                    mockMqttAsyncClient.subscribe(mockParseTopic, anyInt);
                    result = mockMqttException;
                }
            };

            mockMqtt = instantiateMqtt(true);
            Deencapsulation.invoke(mockMqtt, "connect");

            //act
            Deencapsulation.invoke(mockMqtt, "subscribe", mockParseTopic);

            new Verifications()
            {
                {
                    mockMqttAsyncClient.isConnected();
                    minTimes = 1;
                    mockMqttAsyncClient.subscribe(mockParseTopic, anyInt);
                    times = 1;
                }
            };
        }
        finally
        {
            testCleanUp(mockMqtt);
        }
    }

    // Tests_SRS_Mqtt_34_023: [This method shall call peekMessage to get the message payload from the recevived Messages queue corresponding to the messaging client's operation.]
    // Tests_SRS_Mqtt_34_024: [This method shall construct new Message with the bytes obtained from peekMessage and return the message.]
   @Test
    public void receiveSuccess() throws IOException, MqttException
    {
        //arrange
        final byte[] payload = {0x61, 0x62, 0x63};
        baseConstructorExpectations(true);
        baseConnectExpectation();
        new MockUp<MqttMessaging>()
        {
            @Mock
            Pair<String, byte[]> peekMessage() throws IOException
            {
                return new MutablePair<>(mockParseTopic, payload);
            }
        };

        final Mqtt mockMqtt = new MqttMessaging(serverUri, clientId, userName, password,  mockIotHubSSLContext);
        try
        {
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
        finally
        {
            testCleanUp(mockMqtt);
        }
    }

    // Codes_SRS_Mqtt_34_022: [If the call peekMessage returns a null or empty string then this method shall do nothing and return null]
    @Test
    public void receiveReturnsNullMessageWhenParseTopicReturnsNull() throws IOException, MqttException
    {
        //arrange
        final byte[] payload = {0x61, 0x62, 0x63};
        baseConstructorExpectations(true);
        baseConnectExpectation();
        new MockUp<MqttMessaging>()
        {
            @Mock
            Pair<String, byte[]> peekMessage() throws IOException
            {
                return new MutablePair<>(null, payload);
            }
        };
        final Mqtt mockMqtt = new MqttMessaging(serverUri, clientId, userName, password,  mockIotHubSSLContext);
        try
        {
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
        finally
        {
            testCleanUp(mockMqtt);
        }
    }

    // Codes_SRS_Mqtt_34_025: [If the call to peekMessage returns null when topic is non-null then this method will throw IOException]
    @Test(expected = IOException.class)
    public void receiveThrowsIOExceptionWhenParsePayloadReturnsNull() throws IOException, MqttException
    {
        //arrange
        final byte[] payload = {0x61, 0x62, 0x63};
        baseConstructorExpectations(true);
        new MockUp<MqttMessaging>()
        {
            @Mock
            Pair<String, byte[]> peekMessage() throws IOException
            {
                return new MutablePair<>(mockParseTopic, null);
            }
        };

        final Mqtt mockMqtt = new MqttMessaging(serverUri, clientId, userName, password,  mockIotHubSSLContext);
        //act
        try
        {
            Message receivedMessage = mockMqtt.receive();
        }
        finally
        {
            testCleanUp(mockMqtt);
        }
    }

    @Test(expected = InvalidParameterException.class)
    public void receiveThrowsExceptionWhenConfigurationIsNotSet() throws IOException, MqttException
    {
        //arrange
        final byte[] payload = {0x61, 0x62, 0x63};
        baseConstructorExpectations(false);
        new MockUp<MqttMessaging>()
        {
            @Mock
            Pair<String, byte[]> peekMessage() throws IOException
            {
                return new MutablePair<>(mockParseTopic, null);
            }
        };

        final Mqtt mockMqtt = new MqttMessaging(serverUri, clientId, userName, password,  mockIotHubSSLContext);
        Deencapsulation.setField(mockMqtt, "info", null);

        //act
        try
        {
            Message receivedMessage = mockMqtt.receive();
        }
        finally
        {
            testCleanUp(mockMqtt);
        }
    }

    /*
    **Tests_SRS_Mqtt_25_030: [**The payload of the message and the topic is added to the received messages queue .**]**
     */
    @Test
    public void messageArrivedAddsToQueue() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        try
        {
            final byte[] actualPayload = {0x61, 0x62, 0x63};
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
            mockMqtt.messageArrived(mockParseTopic, new MqttMessage(actualPayload));

            //assert
            Queue<Pair<String, byte[]>> actualQueue = Deencapsulation.getField(mockMqtt, "allReceivedMessages");
            Pair<String, byte[]> messagePair = actualQueue.poll();
            assertNotNull(messagePair);
            assertTrue(messagePair.getKey().equals(mockParseTopic));

            byte[] receivedPayload = messagePair.getValue();
            assertTrue(actualPayload.length == receivedPayload.length);
            for (int i = 0; i < actualPayload.length; i++)
            {
                assertEquals(actualPayload[i], receivedPayload[i]);
            }
        }
        finally
        {
            testCleanUp(mockMqtt);
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

        try
        {
            new StrictExpectations()
            {
                {
                    new MemoryPersistence();
                    result = mockMemoryPersistence;
                    new MqttAsyncClient(serverUri, clientId, mockMemoryPersistence);
                    result = mockMqttAsyncClient;
                    mockMqttAsyncClient.setCallback((Mqtt) any);

                    new MqttConnectOptions();
                    result = mockMqttConnectionOptions;
                    mockMqttConnectionOptions.setKeepAliveInterval(anyInt);
                    mockMqttConnectionOptions.setCleanSession(anyBoolean);
                    mockMqttConnectionOptions.setMqttVersion(anyInt);
                    mockMqttConnectionOptions.setUserName(anyString);
                    mockMqttConnectionOptions.setPassword(password.toCharArray());
                    mockMqttConnectionOptions.setSocketFactory(mockIotHubSSLContext.getIotHubSSlContext().getSocketFactory());
                    mockMqttAsyncClient.isConnected();
                    result = false;

                    IotHubSasToken.isSasTokenExpired(new String(mockMqttConnectionOptions.getPassword()));
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
                mockMqtt.connectionLost(t);
            }
            catch (Exception e)
            {
                System.out.print("Completed throwing exception - " + e.getCause() + e.getMessage());
            }
        }
        finally
        {
            testCleanUp(mockMqtt);
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

        try
        {
            new NonStrictExpectations()
            {
                {
                    new MemoryPersistence();
                    result = mockMemoryPersistence;
                    new MqttAsyncClient(serverUri, clientId, mockMemoryPersistence);
                    result = mockMqttAsyncClient;
                    mockMqttAsyncClient.setCallback((Mqtt) any);

                    new MqttConnectOptions();
                    result = mockMqttConnectionOptions;
                    mockMqttConnectionOptions.setKeepAliveInterval(anyInt);
                    mockMqttConnectionOptions.setCleanSession(anyBoolean);
                    mockMqttConnectionOptions.setMqttVersion(anyInt);
                    mockMqttConnectionOptions.setUserName(anyString);
                    mockMqttConnectionOptions.setPassword(password.toCharArray());
                    mockMqttConnectionOptions.setSocketFactory(mockIotHubSSLContext.getIotHubSSlContext().getSocketFactory());
                }
            };

            new StrictExpectations()
            {
                {
                    mockMqttAsyncClient.isConnected();
                    result = false;
                    mockMqttConnectionOptions.getPassword();
                    result = anyString.toCharArray();
                    IotHubSasToken.isSasTokenExpired(anyString);
                    result = true; // SAS token has expired
                    mockDeviceClientConfig.getDeviceKey();
                    result = anyString;
                    mockDeviceClientConfig.getTokenValidSecs();
                    result = anyLong;
                }
            };

            new NonStrictExpectations()
            {
                {
                    new IotHubSasToken((DeviceClientConfig)any, anyLong);
                    result = mockSASToken;
                }
            };

            new StrictExpectations()
            {
                {
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
                Deencapsulation.invoke(mockMqtt, "setDeviceClientConfig", mockDeviceClientConfig);
                mockMqtt.connectionLost(t);
            }
            catch (Exception e)
            {
                System.out.print("Completed throwing exception - " + e.getCause() + e.getMessage());
            }
        }
        finally
        {
            testCleanUp(mockMqtt);
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
        final byte[] payload = {0x61, 0x62, 0x63};
        //arrange
        Mqtt mockMqtt = null;
        Throwable t = new Throwable();

        try
        {
            new StrictExpectations()
            {
                {
                    new MemoryPersistence();
                    result = mockMemoryPersistence;
                    new MqttAsyncClient(serverUri, clientId, mockMemoryPersistence);
                    result = mockMqttAsyncClient;
                    mockMqttAsyncClient.setCallback((Mqtt) any);

                    new MqttConnectOptions();
                    result = mockMqttConnectionOptions;
                    mockMqttConnectionOptions.setKeepAliveInterval(anyInt);
                    mockMqttConnectionOptions.setCleanSession(anyBoolean);
                    mockMqttConnectionOptions.setMqttVersion(anyInt);
                    mockMqttConnectionOptions.setUserName(anyString);
                    mockMqttConnectionOptions.setPassword(password.toCharArray());
                    mockMqttConnectionOptions.setSocketFactory(mockIotHubSSLContext.getIotHubSSlContext().getSocketFactory());
                    mockMqttAsyncClient.isConnected();
                    result = false;

                    IotHubSasToken.isSasTokenExpired(new String(mockMqttConnectionOptions.getPassword()));
                    result = true; // User specified SAS token has expired

                    mockDeviceClientConfig.getDeviceKey();
                    result = null;

                }
            };

            //act
            mockMqtt = instantiateMqtt(true);
            Deencapsulation.invoke(mockMqtt, "setDeviceClientConfig", mockDeviceClientConfig);
            mockMqtt.connectionLost(t);
            Deencapsulation.invoke(mockMqtt, "publish", mockParseTopic, payload);

        }

        finally
        {
            testCleanUp(mockMqtt);
        }
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
        try
        {
            new StrictExpectations()
            {
                {
                    new MemoryPersistence();
                    result = mockMemoryPersistence;
                    new MqttAsyncClient(serverUri, clientId, mockMemoryPersistence);
                    result = mockMqttAsyncClient;
                    mockMqttAsyncClient.setCallback((Mqtt) any);

                    new MqttConnectOptions();
                    result = mockMqttConnectionOptions;
                    mockMqttConnectionOptions.setKeepAliveInterval(anyInt);
                    mockMqttConnectionOptions.setCleanSession(anyBoolean);
                    mockMqttConnectionOptions.setMqttVersion(anyInt);
                    mockMqttConnectionOptions.setUserName(anyString);
                    mockMqttConnectionOptions.setPassword(password.toCharArray());
                    mockMqttConnectionOptions.setSocketFactory(mockIotHubSSLContext.getIotHubSSlContext().getSocketFactory());

                    mockMqttAsyncClient.isConnected();
                    result = false;
                
                    IotHubSasToken.isSasTokenExpired(new String(mockMqttConnectionOptions.getPassword()));
                    result = false;

                    mockMqttAsyncClient.isConnected();
                    result = false;
                    mockMqttAsyncClient.connect(mockMqttConnectionOptions);
                    result = mockMqttException;

                    mockMqttAsyncClient.isConnected();
                    result = false;
                    
                    IotHubSasToken.isSasTokenExpired(new String(mockMqttConnectionOptions.getPassword()));
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
                mockMqtt.connectionLost(t);
            }
            catch (Exception e)
            {
                System.out.print("Completed throwing exception - " + e.getCause() + e.getMessage());
            }
        }
        finally
        {
            testCleanUp(mockMqtt);
        }
    }

    // Tests_SRS_Mqtt_34_021: [If the call peekMessage returns null then this method shall do nothing and return null]
    @Test
    public void receiveReturnsNullMessageIfTopicNotFound(@Mocked final IotHubSSLContext iotHubSSLContext) throws IOException
    {
        //can't be initialized to null, so set it as a default message
        Message receivedMessage = new Message();
        try
        {
            //arrange
            MqttMessaging testMqttClient = new MqttMessaging("serverURI","deviceId","username","password", iotHubSSLContext);
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
        baseConnectExpectation();
        new MockUp<MqttMessaging>()
        {
            @Mock
            Pair<String, byte[]> peekMessage()
            {
                return new MutablePair<>(mockParseTopicInvalidPropertyFormat, payload);
            }
        };

        final Mqtt mockMqtt = new MqttMessaging(serverUri, clientId, userName, password,  mockIotHubSSLContext);
        try
        {
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
        finally
        {
            testCleanUp(mockMqtt);
        }
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
        baseConstructorExpectations(true);
        baseConnectExpectation();
        new MockUp<MqttMessaging>()
        {
            @Mock
            Pair<String, byte[]> peekMessage()
            {
                return new MutablePair<>(mockParseTopicNoCustomProperties, payload);
            }
        };

        final Mqtt mockMqtt = new MqttMessaging(serverUri, clientId, userName, password,  mockIotHubSSLContext);
        try
        {
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
        finally
        {
            testCleanUp(mockMqtt);
        }
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
        baseConstructorExpectations(true);
        baseConnectExpectation();
        new MockUp<MqttMessaging>()
        {
            @Mock
            Pair<String, byte[]> peekMessage()
            {
                return new MutablePair<>(mockParseTopicWithUnusualCharacters, payload);
            }
        };

        final Mqtt mockMqtt = new MqttMessaging(serverUri, clientId, userName, password,  mockIotHubSSLContext);
        try
        {
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
        finally
        {
            testCleanUp(mockMqtt);
        }
    }
    /*
    **Tests_SRS_Mqtt_99_049: [**If the user supplied SAS token has expired, the function shall throw an IOException.**]**
     */
    @Test(expected = IOException.class)
    public void unsubscribeThrowsExceptionWhenUserSuppliedSASTokenHasExpired() throws IOException, MqttException
    {
        //arrange
        Mqtt mockMqtt = null;
        try
        {
            baseConstructorExpectations(true);
            new NonStrictExpectations()
            {
                {
                    mockMqttAsyncClient.isConnected();
                    result = true;
                }
            };

            mockMqtt = instantiateMqtt(true);
            Deencapsulation.invoke(mockMqtt, "connect");

            //act
            Deencapsulation.setField(mockMqtt,"userSpecifiedSASTokenExpiredOnRetry",true);
            Deencapsulation.invoke(mockMqtt, "unsubscribe", mockParseTopic);
        }
        
        finally
        {
            testCleanUp(mockMqtt);
        }
    }
   
    /*
    ** Codes_SRS_Mqtt_99_50: [**If deviceConfig is null, the function shall throw an IllegalArgumentException**]**
    */
    @Test  (expected = IllegalArgumentException.class)
    public void deviceConfigNullThrows() throws IOException, MqttException
    {
        // Act
        Mqtt mockMqtt = instantiateMqtt(true);

        try
        {
           Deencapsulation.invoke(mockMqtt,"setDeviceClientConfig", new Class[] {DeviceClientConfig.class},(DeviceClientConfig)null);
        }
        finally
        {
            testCleanUp(mockMqtt);
        }
    } 
}