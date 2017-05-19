// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.Message;
import mockit.*;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.Assert.*;

/* Unit tests for Mqtt */
public class MqttTest {
    final String serverUri = "test.host.name";
    final String clientId = "test.iothub";
    final String userName = "test-deviceId";
    final String password = "test-devicekey?&test";
    final String mockParseTopic = "testTopic";

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

    private Mqtt instantiateMqtt(boolean withParameters) throws IOException
    {

        if (withParameters)
        {
            Mqtt mockMqtt = new Mqtt(serverUri, clientId, userName, password, mockIotHubSSLContext)
            {

                @Mock
                void $clinit()
                {
                    // Do nothing here (usually).
                }

                @Mock
                String parseTopic() throws IOException
                {
                    return mockParseTopic;
                }

                @Mock
                byte[] parsePayload(String topic) throws IOException
                {
                    return new byte[0];
                }
            };

            return mockMqtt;
        } else {
            Mqtt mockMqtt = new Mqtt()
            {

                @Mock
                void $clinit()
                {
                    // Do nothing here (usually).
                }

                @Override
                String parseTopic() throws IOException
                {
                    return mockParseTopic;
                }

                @Override
                byte[] parsePayload(String topic) throws IOException
                {
                    return new byte[0];
                }

            };

            return mockMqtt;

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

    public void baseConnectExpectation() throws MqttException
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

    public void baseDisconnectExpectations() throws MqttException
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

    public void basePublishExpectations() throws MqttException
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

    public void basePublishVerifications() throws MqttException
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

    public void baseDisconnectVerifications() throws MqttException
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

    public void baseConnectVerifications() throws MqttException
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

    public void testCleanUp(Mqtt mockMqtt)
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
    public void manyExtendsOfAbstractClassDoesntChangeConfig() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations(true);
        baseConstructorExpectations(false);

        //act
        Mqtt mockMqtt1 = instantiateMqtt(true);
        Mqtt.MqttConnectionInfo actualInfoInstance1 = Deencapsulation.getField(mockMqtt1, "info");
        ConcurrentSkipListMap<String, byte[]> actualMap1 = Deencapsulation.getField(mockMqtt1, "allReceivedMessages");
        Object actualLock1 = Deencapsulation.getField(mockMqtt1, "MQTT_LOCK");

        Mqtt mockMqtt2 = instantiateMqtt(false);
        Mqtt.MqttConnectionInfo actualInfoInstance2 = Deencapsulation.getField(mockMqtt2, "info");
        ConcurrentSkipListMap<String, byte[]> actualMap2 = Deencapsulation.getField(mockMqtt2, "allReceivedMessages");
        Object actualLock2 = Deencapsulation.getField(mockMqtt2, "MQTT_LOCK");

        //assert
        assertEquals(actualInfoInstance1, actualInfoInstance2);
        assertEquals(actualMap1, actualMap2);
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
    public void constructorInitiliasesWithConfig() throws IOException, MqttException
    {
        //arrange
        baseConstructorExpectations(true);

        //act
        Mqtt mockMqtt = instantiateMqtt(true);

        //assert
        Mqtt.MqttConnectionInfo actualInfo = Deencapsulation.getField(mockMqtt, "info");
        assertNotNull(actualInfo);
        assertNotNull(actualInfo.mqttAsyncClient);
        MqttConnectOptions actualConnectionOptions = Deencapsulation.getField(actualInfo, "connectionOptions");
        assertNotNull(actualConnectionOptions);
        ConcurrentSkipListMap<String, byte[]> actualMap = Deencapsulation.getField(mockMqtt, "allReceivedMessages");
        assertNotNull(actualMap);
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
            mockMqtt = new Mqtt(null, clientId, userName, password,  mockIotHubSSLContext)
            {

                @Mock
                void $clinit()
                {
                    // Do nothing here (usually).
                }

                @Mock
                String parseTopic() throws IOException
                {
                    return mockParseTopic;
                }

                @Mock
                byte[] parsePayload(String topic) throws IOException
                {
                    return new byte[0];
                }
            };
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
        Mqtt.MqttConnectionInfo actualInfoInstance = Deencapsulation.getField(mockMqtt, "info");
        assertNull(actualInfoInstance);
        ConcurrentSkipListMap<String, byte[]> actualMap = Deencapsulation.getField(mockMqtt, "allReceivedMessages");
        assertNull(actualMap);

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
        Mqtt.MqttConnectionInfo actualInfoInstance1 = Deencapsulation.getField(mockMqtt1, "info");
        ConcurrentSkipListMap<String, byte[]> actualMap1 = Deencapsulation.getField(mockMqtt1, "allReceivedMessages");
        Mqtt mockMqtt2 = instantiateMqtt(false);
        Mqtt.MqttConnectionInfo actualInfoInstance2 = Deencapsulation.getField(mockMqtt2, "info");
        ConcurrentSkipListMap<String, byte[]> actualMap2 = Deencapsulation.getField(mockMqtt2, "allReceivedMessages");

        Object actualLock1 = Deencapsulation.getField(mockMqtt1, "MQTT_LOCK");
        Object actualLock2 = Deencapsulation.getField(mockMqtt2, "MQTT_LOCK");


        assertEquals(actualInfoInstance1, actualInfoInstance2);
        assertEquals(actualMap1, actualMap2);
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
        mockMqtt.connect();

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
        mockMqtt.connect();

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
            mockMqtt.connect();

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
            mockMqtt.connect();

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
            mockMqtt.connect();

            //act
            mockMqtt.disconnect();

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

            Mqtt.MqttConnectionInfo actualInfoInstance = Deencapsulation.getField(mockMqtt, "info");
            assertNull(actualInfoInstance.mqttAsyncClient);
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
        mockMqtt.connect();

        //act
        mockMqtt.disconnect();

        //assert
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                times = 2;
            }
        };

        Mqtt.MqttConnectionInfo actualInfoInstance = Deencapsulation.getField(mockMqtt, "info");
        assertNull(actualInfoInstance.mqttAsyncClient);

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
        mockMqtt.connect();

        //act
        mockMqtt.publish(mockParseTopic, payload);

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
            mockMqtt.publish(mockParseTopic, payload);

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

        mockMqtt2.connect();

        //act
        mockMqtt1.publish(mockParseTopic, payload);
        mockMqtt2.publish(mockParseTopic2, payload);

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
            mockMqtt.connect();

            //act
            mockMqtt.publish(mockParseTopic, payload);

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
            mockMqtt.connect();

            //act
            mockMqtt.publish(null, payload);

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
            mockMqtt.connect();

            //act
            mockMqtt.publish(mockParseTopic, payload);

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
        mockMqtt.connect();

        //act
        mockMqtt.subscribe(mockParseTopic);

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
            mockMqtt.subscribe(mockParseTopic);

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
            mockMqtt.subscribe(mockParseTopic);

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
            mockMqtt.connect();

            //act
            mockMqtt.subscribe(null);

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
            mockMqtt.connect();

            //act
            mockMqtt.subscribe(mockParseTopic);

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

    /*
    **Tests_SRS_Mqtt_25_021: [**This method shall call parseTopic to parse the topic from the recevived Messages queue corresponding to the messaging client's operation.**]**
     */
    /*
    **Tests_SRS_Mqtt_25_023: [**This method shall call parsePayload to get the message payload from the recevived Messages queue corresponding to the messaging client's operation.**]**
     */
    /*
    Tests_SRS_Mqtt_25_024: [**This method shall construct new Message with the bytes obtained from parsePayload and return the message.**]**
     */
    @Test
    public void receiveSuccess() throws IOException, MqttException
    {
        //arrange
        final byte[] payload = {0x61, 0x62, 0x63};
        baseConstructorExpectations(true);
        baseConnectExpectation();
        final Mqtt mockMqtt = new Mqtt(serverUri, clientId, userName, password,  mockIotHubSSLContext)
        {

            @Mock
            String parseTopic() throws IOException
            {
                return mockParseTopic;
            }

            @Mock
            byte[] parsePayload(String topic) throws IOException
            {
                return payload;
            }
        };

        try
        {
            new NonStrictExpectations()
            {
                {
                    mockMqttAsyncClient.isConnected();
                    result = true;
                }
            };

            mockMqtt.connect();

            //act
            Message receivedMessage = mockMqtt.receive();

            //assert
            byte[] actualPayload = receivedMessage.getBytes();
            assertTrue(actualPayload.length == payload.length);
            for (int i = 0; i < payload.length; i++)
            {
                assertEquals(actualPayload[i], payload[i]);
            }

        }
        finally
        {
            testCleanUp(mockMqtt);
        }

    }

    /*
    **Tests_SRS_Mqtt_25_022: [**If the call parseTopic returns null or empty string then this method shall do nothing and return null**]**
     */
    @Test
    public void receiveReturnsNullMessageWhenParseTopicReturnsNull() throws IOException, MqttException
    {
        //arrange
        final byte[] payload = {0x61, 0x62, 0x63};
        baseConstructorExpectations(true);
        baseConnectExpectation();
        final Mqtt mockMqtt = new Mqtt(serverUri, clientId, userName, password,  mockIotHubSSLContext)
        {

            @Mock
            String parseTopic() throws IOException
            {
                return null;
            }

            @Mock
            byte[] parsePayload(String topic) throws IOException
            {
                return payload;
            }

        };

        try
        {

            new NonStrictExpectations()
            {
                {
                    mockMqttAsyncClient.isConnected();
                    result = true;
                }
            };

            mockMqtt.connect();

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

    /*
    **Tests_SRS_Mqtt_25_025: [**If the call to parsePayload returns null when topic is non-null then this method will throw IOException**]**
     */
    @Test(expected = IOException.class)
    public void receiveThrowsIOExceptionWhenParsePayloadReturnsNull() throws IOException, MqttException
    {
        //arrange
        final byte[] payload = {0x61, 0x62, 0x63};
        baseConstructorExpectations(true);
        final Mqtt mockMqtt = new Mqtt(serverUri, clientId, userName, password,  mockIotHubSSLContext)
        {

            @Mock
            String parseTopic() throws IOException
            {
                return mockParseTopic;
            }

            @Mock
            byte[] parsePayload(String topic) throws IOException
            {
                return null;
            }

        };
        //act
        try
        {
            Message receivedMessage = mockMqtt.receive();
        } finally
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
        final Mqtt mockMqtt = new Mqtt()
        {

            @Mock
            String parseTopic() throws IOException
            {
                return mockParseTopic;
            }

            @Mock
            byte[] parsePayload(String topic) throws IOException
            {
                return null;
            }

        };

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
            baseConstructorExpectations(true);
            baseConnectExpectation();

            new NonStrictExpectations()
            {
                {
                    mockMqttMessage.getPayload();
                    result = actualPayload;
                }
            };

            mockMqtt = instantiateMqtt(true);
            mockMqtt.connect();

            //act
            mockMqtt.messageArrived(mockParseTopic, new MqttMessage(actualPayload));

            //assert
            ConcurrentSkipListMap<String, byte[]> actualMap = Deencapsulation.getField(mockMqtt, "allReceivedMessages");
            assertTrue(actualMap.containsKey(mockParseTopic));

            byte[] receivedPayload = actualMap.get(mockParseTopic);
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
    **Tests_SRS_Mqtt_25_026: [**The function shall notify all its concrete classes by calling abstract method onReconnect at the entry of the function**]**
     */
    /*
    **Tests_SRS_Mqtt_25_029: [**The function shall notify all its concrete classes by calling abstract method onReconnectComplete at the exit of the function**]**
     */
    @Test
    public void connectionLostAttemptsToReconnect() throws IOException, MqttException
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
                    mockMqttAsyncClient.isConnected();
                    result = false;
                    mockMqttAsyncClient.connect(mockMqttConnectionOptions);
                    result = mockMqttException;

                    mockMqttAsyncClient.isConnected();
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

}
