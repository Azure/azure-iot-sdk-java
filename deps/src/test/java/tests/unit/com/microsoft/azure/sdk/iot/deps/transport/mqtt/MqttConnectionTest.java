/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.deps.transport.mqtt;

import com.microsoft.azure.sdk.iot.deps.transport.mqtt.MqttMessage;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.net.ssl.SSLContext;
import java.io.IOException;

import com.microsoft.azure.sdk.iot.deps.transport.mqtt.*;

import static org.junit.Assert.assertEquals;

/** Unit tests for AmqpConnection.
 * Coverage : 100% method, 96% line */
@RunWith(JMockit.class)
public class MqttConnectionTest
{
    private static final String TEST_HOST_NAME = "testHostName";
    private static final String TEST_CLIENT_ID = "testClientId";
    private static final String TEST_USERNAME = "testUserName";
    private static final String TEST_PASSWORD = "testPassword";
    private static final String WEB_SOCKET_URI_NAME = "wss://testHostName:443";
    private static final String SOCKET_URI_NAME = "ssl://testHostName:8883";
    private static final String TEST_TOPIC = "testTopic";

    @Mocked
    private SSLContext mockedSSLContext;

    @Mocked
    private MqttAsyncClient mockedMqttAsyncClient;

    @Mocked
    private MqttConnectOptions mockedMqttConnectOptions;

    @Mocked
    private MqttListener mockedMqttListener;

    @Mocked
    private MqttMessage mockedMqttMessage;

    @Mocked
    private MqttQos mockedMqttQos;

    @Mocked
    private IMqttToken mockedIMqttToken;

    @Mocked
    private IMqttDeliveryToken mockedIMqttDeliveryToken;

    @Mocked
    private org.eclipse.paho.client.mqttv3.MqttMessage mockedPahoMqttMessage;

    @Mocked
    private Throwable mockedThrowable;

    @Test (expected = IllegalArgumentException.class)
    public void mqttConnectionConstructorThrowsWhenHostnameNULL() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        MqttConnection mqttConnection = new MqttConnection(null, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, false);

        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void mqttConnectionConstructorThrowsWhenHostnameEmpty() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        MqttConnection mqttConnection = new MqttConnection("", TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, false);

        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void mqttConnectionConstructorThrowsWhenClientIdNULL() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, null, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, false);

        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void mqttConnectionConstructorThrowsWhenClientIdEmpty() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, "", TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, false);

        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void MqttConnectionConstructorThrowsWhenUsernameNULL() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, null, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, false);

        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void mqttConnectionConstructorThrowsWhenUsernameEmpty() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, "", TEST_PASSWORD, mockedSSLContext, mockedMqttListener, false);

        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void MqttConnectionConstructorThrowsWhenSslContextNULL() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, null, mockedMqttListener, false);

        //assert
    }

    @Test
    public void mqttConnectionConstructorSucceeds() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };

        // Act
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, false);

        //assert
    }

    @Test
    public void mqttConnectionConstructorWebSocketsSucceeds() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(WEB_SOCKET_URI_NAME, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };

        // Act
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        //assert
    }

    @Test (expected = IOException.class)
    public void mqttConnectionConstructorThrowsOnMqttAsyncClient() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(WEB_SOCKET_URI_NAME, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = new MqttException(0);
            }
        };

        // Act
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        //assert
    }


    @Test (expected = IllegalArgumentException.class)
    public void mqttConnectionConstructorThrowsOnListenerNullFail() throws IOException, MqttException
    {
        // Arrange

        // Act
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, null, true);

        //assert
    }

    @Test
    public void isMqttConnectedSucceeds() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                mockedMqttAsyncClient.isConnected();
                result = true;
            }
        };

        // Act
        boolean result = mqttConnection.isMqttConnected();

        //assert
        Assert.assertTrue(result);
    }

    @Test
    public void isMqttConnectedMqttAsyncClientNullSucceeds() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        // Act
        boolean result = mqttConnection.isMqttConnected();

        //assert
        Assert.assertFalse(result);
    }

    @Test
    public void connectSucceeds() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                mockedMqttAsyncClient.isConnected();
                result = false;

                mockedMqttAsyncClient.connect((MqttConnectOptions)any);
                result = mockedIMqttToken;

                mockedIMqttToken.waitForCompletion();
            }
        };

        // Act
        mqttConnection.connect();

        //assert
    }

    @Test
    public void connectWithListnerSucceeds() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                mockedMqttAsyncClient.isConnected();
                result = false;

                mockedMqttAsyncClient.connect((MqttConnectOptions)any);
                result = mockedIMqttToken;

                mockedIMqttToken.waitForCompletion();
            }
        };

        // Act
        mqttConnection.connect();

        //assert
    }

    @Test (expected = IOException.class)
    public void connectThrowsOnConnectFail() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                mockedMqttAsyncClient.isConnected();
                result = false;

                mockedMqttAsyncClient.connect((MqttConnectOptions)any);
                result = new MqttException(0);
            }
        };

        // Act
        mqttConnection.connect();

        //assert
    }

    @Test
    public void disconnectSucceeds() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                mockedMqttAsyncClient.isConnected();
                result = true;

                mockedMqttAsyncClient.disconnect();
                result = mockedIMqttToken;

                mockedIMqttToken.waitForCompletion();
            }
        };

        // Act
        mqttConnection.disconnect();

        //assert
    }

    @Test (expected = IOException.class)
    public void disconnectThrowsOnDisconnect() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                mockedMqttAsyncClient.isConnected();
                result = true;

                mockedMqttAsyncClient.disconnect();
                result = new MqttException(0);
            }
        };

        // Act
        mqttConnection.disconnect();

        //assert
    }

    @Test
    public void publishMessageTopicSucceeds() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                new MqttMessage(TEST_TOPIC);
                result = mockedMqttMessage;

                mockedMqttMessage.setQos(mockedMqttQos);

                mockedMqttAsyncClient.isConnected();
                result = true;

                mockedMqttAsyncClient.publish(anyString, (org.eclipse.paho.client.mqttv3.MqttMessage)any);
                result = mockedIMqttDeliveryToken;

                mockedIMqttDeliveryToken.waitForCompletion();

            }
        };

        // Act
        mqttConnection.publishMessage(TEST_TOPIC, mockedMqttQos, null);

        //assert
    }

    @Test
    public void publishMessageTopicWithMessageSucceeds() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                new MqttMessage(TEST_TOPIC, (byte[])any);
                result = mockedMqttMessage;

                mockedMqttMessage.setQos(mockedMqttQos);

                mockedMqttAsyncClient.isConnected();
                result = true;

                mockedMqttAsyncClient.publish(TEST_TOPIC, (org.eclipse.paho.client.mqttv3.MqttMessage)any);
                result = mockedIMqttDeliveryToken;

                mockedIMqttToken.waitForCompletion();
            }
        };

        byte[] message = new byte[10];

        // Act
        mqttConnection.publishMessage(TEST_TOPIC, mockedMqttQos, message);

        //assert
    }

    @Test
    public void publishMessageMqttMessageSucceeds() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                mockedMqttAsyncClient.isConnected();
                result = true;

                mockedMqttAsyncClient.publish(anyString, (org.eclipse.paho.client.mqttv3.MqttMessage)any);
                result = mockedIMqttDeliveryToken;

                mockedIMqttDeliveryToken.waitForCompletion();
            }
        };

        // Act
        mqttConnection.publishMessage(mockedMqttMessage);

        //assert
    }

    @Test (expected = IOException.class)
    public void publishMessageThrowsOnPublishFail() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                mockedMqttAsyncClient.isConnected();
                result = true;

                mockedMqttAsyncClient.publish(anyString, (org.eclipse.paho.client.mqttv3.MqttMessage)any);
                result = new MqttException(0);
            }
        };

        // Act
        mqttConnection.publishMessage(mockedMqttMessage);

        //assert
    }

    @Test (expected = IOException.class)
    public void publishMessageThrowsOnNoConnectedFail() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                mockedMqttAsyncClient.isConnected();
                result = false;
            }
        };

        // Act
        mqttConnection.publishMessage(mockedMqttMessage);

        //assert
    }

    @Test (expected = IOException.class)
    public void publishMessageThrowsOnMessageNullFail() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                mockedMqttAsyncClient.isConnected();
                result = true;
            }
        };

        // Act
        mqttConnection.publishMessage(null);

        //assert
    }

    @Test
    public void subscribeSucceeds() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                mockedMqttAsyncClient.isConnected();
                result = true;

                mockedMqttAsyncClient.subscribe(TEST_TOPIC, anyInt);
                result = mockedIMqttToken;

                mockedIMqttToken.waitForCompletion();
            }
        };

        // Act
        mqttConnection.subscribe(TEST_TOPIC, mockedMqttQos);

        //assert
    }

    @Test (expected = IOException.class)
    public void subscribeThrowsOnSubscribeFail() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                mockedMqttAsyncClient.isConnected();
                result = true;

                mockedMqttAsyncClient.subscribe(TEST_TOPIC, anyInt);
                result = new MqttException(0);
            }
        };

        // Act
        mqttConnection.subscribe(TEST_TOPIC, mockedMqttQos);

        //assert
    }

    @Test (expected = IOException.class)
    public void subscribeThrowsOnNotConnectedFail() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                mockedMqttAsyncClient.isConnected();
                result = false;
            }
        };

        // Act
        mqttConnection.subscribe(TEST_TOPIC, mockedMqttQos);

        //assert
    }

    @Test
    public void unsubscribeSucceeds() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                mockedMqttAsyncClient.unsubscribe(TEST_TOPIC);
                result = mockedIMqttToken;

                mockedIMqttToken.waitForCompletion();
            }
        };

        // Act
        mqttConnection.unsubscribe(TEST_TOPIC);

        //assert
    }

    @Test (expected = IOException.class)
    public void unsubscribeThrowsOnSubscribeFail() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                mockedMqttAsyncClient.unsubscribe(TEST_TOPIC);
                result = new MqttException(0);
            }
        };

        // Act
        mqttConnection.unsubscribe(TEST_TOPIC);

        //assert
    }

    @Test
    public void messageArrivedSucceeds() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                mockedMqttListener.messageReceived((MqttMessage)any);
            }
        };

        // Act
        mqttConnection.messageArrived(TEST_TOPIC, mockedPahoMqttMessage);

        //assert
    }

    @Test
    public void deliveryCompleteSucceeds() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        mqttConnection.deliveryComplete(mockedIMqttDeliveryToken);

        //assert
    }

    @Test
    public void connectionLostSucceeds() throws IOException, MqttException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                new MqttAsyncClient(anyString, TEST_CLIENT_ID, (MemoryPersistence)any);
                result = mockedMqttAsyncClient;

                new MqttConnectOptions();
                result = mockedMqttConnectOptions;
            }
        };
        MqttConnection mqttConnection = new MqttConnection(TEST_HOST_NAME, TEST_CLIENT_ID, TEST_USERNAME, TEST_PASSWORD, mockedSSLContext, mockedMqttListener, true);

        new NonStrictExpectations()
        {
            {
                //mockedMqttListener.connectionLost(new Throwable());
            }
        };

        // Act
        mqttConnection.connectionLost(mockedThrowable);

        //assert
    }

}
