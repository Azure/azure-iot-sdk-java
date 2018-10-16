/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttConnection;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.*;

/*
    Unit test for MqttConnection
    Coverage : 100% method, 100% line
 */
public class MqttConnectionTest
{
    private static final String SERVER_URI = "test.host.name";
    private static final String CLIENT_ID = "test.iothub";
    private static final String USER_NAME = "test-deviceId";
    private static final String PWORD = "this is not a secret";

    @Mocked
    SSLContext mockIotHubSSLContext;

    @Mocked
    private MqttAsyncClient mockMqttAsyncClient;

    @Mocked
    private MqttConnectOptions mockMqttConnectionOptions;

    @Mocked
    private MemoryPersistence mockMemoryPersistence;

    @Mocked
    private IMqttToken mockMqttToken;

    private void baseConstructorExpectations() throws MqttException
    {
        new NonStrictExpectations()
        {
            {
                new MemoryPersistence();
                result = mockMemoryPersistence;
                new MqttAsyncClient(SERVER_URI, CLIENT_ID, mockMemoryPersistence);
                result = mockMqttAsyncClient;
                new MqttConnectOptions();
                result = mockMqttConnectionOptions;
            }
        };
    }

    private void baseConstructorVerifications() throws MqttException
    {
        new Verifications()
        {
            {
                mockMqttConnectionOptions.setKeepAliveInterval(anyInt);
                times = 1;
                mockMqttConnectionOptions.setCleanSession(anyBoolean);
                times = 1;
                mockMqttConnectionOptions.setMqttVersion(anyInt);
                times = 1;
                mockMqttConnectionOptions.setUserName(USER_NAME);
                times = 1;
                mockMqttConnectionOptions.setPassword(PWORD.toCharArray());
                times = 1;
                mockMqttConnectionOptions.setSocketFactory(mockIotHubSSLContext.getSocketFactory());
                times = 1;
                new ConcurrentLinkedQueue<>();
                times = 1;
                new Object();
                times = 1;
            }
        };
    }

    //Tests_SRS_MQTTCONNECTION_25_003: [The constructor shall create lock, queue for this MqttConnection.]
    //Tests_SRS_MQTTCONNECTION_25_004: [The constructor shall create an MqttAsync client and update the connection options using the provided SERVER_URI, CLIENT_ID, USER_NAME, PWORD and sslContext.]
    @Test
    public void constructorSucceeds() throws Exception
    {
        //arrange
        baseConstructorExpectations();
        //act
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);

        //assert
        baseConstructorVerifications();

        MqttAsyncClient actualAsyncClient = Deencapsulation.getField(mqttConnection, "mqttAsyncClient");
        assertNotNull(actualAsyncClient);
        MqttConnectOptions actualConnectionOptions = Deencapsulation.getField(mqttConnection, "connectionOptions");
        assertNotNull(actualConnectionOptions);
        Queue<Pair<String, byte[]>> actualQueue = Deencapsulation.getField(mqttConnection, "allReceivedMessages");
        assertNotNull(actualQueue);
        Object actualLock = Deencapsulation.getField(mqttConnection, "mqttLock");
        assertNotNull(actualLock);
    }

    @Test (expected = ProtocolException.class)
    public void constructorThrowsOnAsyncClientFailure() throws Exception
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                new MemoryPersistence();
                result = mockMemoryPersistence;
                new MqttAsyncClient(SERVER_URI, CLIENT_ID, mockMemoryPersistence);
                result = mockMqttAsyncClient;
                new MqttConnectOptions();
                result = new MqttException(anyInt, (Throwable)any);
            }
        };

        //act
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);
    }

    //Tests_SRS_MQTTCONNECTION_25_001: [The constructor shall throw IllegalArgumentException if any of the input parameters are null other than password.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithNullServerUriThrows() throws Exception
    {
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, null, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithNullClientIdThrows() throws Exception
    {
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, null, USER_NAME, PWORD, mockIotHubSSLContext);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithNullUserNameThrows() throws Exception
    {
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, null, PWORD, mockIotHubSSLContext);
    }

    //Tests_SRS_MQTTCONNECTION_25_002: [The constructor shall throw IllegalArgumentException if SERVER_URI, CLIENT_ID, USER_NAME, PWORD are empty.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithEmptyServerUriThrows() throws Exception
    {
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, "", CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithEmptyClientIdThrows() throws Exception
    {
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, "", USER_NAME, PWORD, mockIotHubSSLContext);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithEmptyUserNameThrows() throws Exception
    {
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, "", PWORD, mockIotHubSSLContext);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithEmptySSLThrows() throws Exception
    {
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, null);
    }

    //Tests_SRS_MQTTCONNECTION_25_007: [Getter for the MqttAsyncClient.]
    @Test
    public void getAsyncClientSucceeds() throws Exception
    {
        //arrange
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);

        //act
        MqttAsyncClient mqttAsyncClient = Deencapsulation.invoke(mqttConnection, "getMqttAsyncClient");

        //assert
        assertNotNull(mqttAsyncClient);
    }

    //Tests_SRS_MQTTCONNECTION_25_008: [Getter for the Message Queue.]
    @Test
    public void getAllReceivedMessagesSucceeds() throws Exception
    {
        //arrange
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);

        //act
        ConcurrentLinkedQueue concurrentLinkedQueue = Deencapsulation.invoke(mqttConnection, "getAllReceivedMessages");

        //assert
        assertNotNull(concurrentLinkedQueue);
    }

    //Tests_SRS_MQTTCONNECTION_25_009: [Getter for the Mqtt Lock on this connection.]
    @Test
    public void getMqttLockSucceeds() throws Exception
    {
        //arrange
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);

        //act
        Object mqttLock = Deencapsulation.invoke(mqttConnection, "getMqttLock");

        //assert
        assertNotNull(mqttLock);
    }

    //Tests_SRS_MQTTCONNECTION_25_010: [Getter for the MqttConnectionOptions.]
    @Test
    public void getConnectionOptionsSucceeds() throws Exception
    {
        //arrange
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);

        //act
        MqttConnectOptions mqttConnectOptions = Deencapsulation.invoke(mqttConnection, "getConnectionOptions");

        //assert
        assertNotNull(mqttConnectOptions);
    }

    //Tests_SRS_MQTTCONNECTION_25_011: [Setter for the MqttAsyncClient which can be null.]
    @Test
    public void setMqttAsyncClientSucceeds() throws Exception
    {
        //arrange
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);
        MqttAsyncClient testMqttAsyncClient = null;

        //act
        Deencapsulation.invoke(mqttConnection, "setMqttAsyncClient", new Class[] {MqttAsyncClient.class}, testMqttAsyncClient);

        //assert
       MqttAsyncClient actualMqttAsyncClient = Deencapsulation.getField(mqttConnection,  "mqttAsyncClient");
       assertEquals(actualMqttAsyncClient, testMqttAsyncClient);
    }

    //Tests_SRS_MQTTCONNECTION_25_005: [This method shall set the callback for Mqtt.]
    @Test
    public void setMqttCallbackSucceeds(@Mocked MqttCallback mockedMqttCallback) throws Exception
    {
        //arrange
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);
        MqttCallback testMqttCallback = mockedMqttCallback;

        //act
        Deencapsulation.invoke(mqttConnection, "setMqttCallback", new Class[] {MqttCallback.class}, testMqttCallback);

        //assert
        MqttCallback actualMqttCallback = Deencapsulation.getField(mqttConnection,  "mqttCallback");
        assertEquals(actualMqttCallback, testMqttCallback);
    }

    //Tests_SRS_MQTTCONNECTION_25_006: [This method shall throw IllegalArgumentException if callback is null.]
    @Test (expected = IllegalArgumentException.class)
    public void setMqttCallbackThrowsOnNull(@Mocked MqttCallback mockedMqttCallback) throws Exception
    {
        //arrange
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);
        MqttCallback testMqttCallback = null;

        //act
        Deencapsulation.invoke(mqttConnection, "setMqttCallback", new Class[] {MqttCallback.class}, testMqttCallback);

        //assert
        MqttCallback actualMqttCallback = Deencapsulation.getField(mqttConnection,  "mqttCallback");
        assertEquals(actualMqttCallback, testMqttCallback);
    }


    //Tests_SRS_MQTTCONNECTION_25_012: [This function shall invoke the saved mqttAsyncClient to send the message ack for the provided messageId and then return true.]
    @Test
    public void sendMessageAckSendsAck() throws MqttException
    {
        //arrange
        final int expectedMessageId = 13;
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);

        //act
        boolean returnedValue = Deencapsulation.invoke(mqttConnection, "sendMessageAcknowledgement", expectedMessageId);

        //assert
        assertTrue(returnedValue);
        new Verifications()
        {
            {
                mockMqttAsyncClient.setManualAcks(true);
                times = 1;

                mockMqttAsyncClient.messageArrivedComplete(expectedMessageId, anyInt);
            }
        };
    }

    //Tests_SRS_MQTTCONNECTION_25_013: [If this function encounters an MqttException when sending the message ack over the mqtt async client, this function shall translate that exception and throw it.]
    @Test (expected = ProtocolException.class)
    public void sendMessageCatchsMqttExceptionAndTranslates() throws MqttException
    {
        //arrange
        final int expectedMessageId = 13;
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);
        final MqttException mqttException = new MqttException(new Throwable());
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.messageArrivedComplete(anyInt, anyInt);
                result = mqttException;
            }
        };


        //act
        boolean returnedValue = Deencapsulation.invoke(mqttConnection, "sendMessageAcknowledgement", expectedMessageId);
    }

    //Tests_SRS_MQTTCONNECTION_34_014: [If the saved mqttAsyncClient is not null, this function shall return the
    // result of invoking isConnected on that object.]
    @Test
    public void isConnectedChecksMqttAsyncClientFalse()
    {
        //arrange
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);
        Deencapsulation.setField(mqttConnection, "mqttAsyncClient", mockMqttAsyncClient);
        
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = false;
            }
        };
        
        //act
        boolean isConnected = Deencapsulation.invoke(mqttConnection, "isConnected");
        
        //assert
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                times = 1;
            }
        };
        assertFalse(isConnected);
    }

    //Tests_SRS_MQTTCONNECTION_34_014: [If the saved mqttAsyncClient is not null, this function shall return the
    // result of invoking isConnected on that object.]
    @Test
    public void isConnectedChecksMqttAsyncClientTrue()
    {
        //arrange
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);
        Deencapsulation.setField(mqttConnection, "mqttAsyncClient", mockMqttAsyncClient);

        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.isConnected();
                result = true;
            }
        };

        //act
        boolean isConnected = Deencapsulation.invoke(mqttConnection, "isConnected");

        //assert
        new Verifications()
        {
            {
                mockMqttAsyncClient.isConnected();
                times = 1;
            }
        };
        assertTrue(isConnected);
    }
    
    //Tests_SRS_MQTTCONNECTION_34_015: [If the saved mqttAsyncClient is null, this function shall return false.]
    @Test
    public void isConnectedReturnsFalseIfMqttAsyncClientIsNull()
    {
        //arrange
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);
        Deencapsulation.setField(mqttConnection, "mqttAsyncClient", null);

        //act
        boolean isConnected = Deencapsulation.invoke(mqttConnection, "isConnected");

        //assert
        assertFalse(isConnected);
    }
    
    //Tests_SRS_MQTTCONNECTION_34_016: [If the saved mqttAsyncClient is not null, this function shall return the
    // result of invoking disconnect on that object.]
    @Test
    public void disconnectInvokesDisconnectOnAsyncClient() throws MqttException
    {
        //arrange
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);
        Deencapsulation.setField(mqttConnection, "mqttAsyncClient", mockMqttAsyncClient);
        new NonStrictExpectations()
        {
            {
                mockMqttAsyncClient.disconnect();
                result = mockMqttToken;
            }
        };

        //act
        IMqttToken actualToken = Deencapsulation.invoke(mqttConnection, "disconnect");

        //assert
        assertEquals(mockMqttToken, actualToken);
        new Verifications()
        {
            {
                mockMqttAsyncClient.disconnect();
                times = 1;
            }
        };
    }

    //Tests_SRS_MQTTCONNECTION_34_017: [If the saved mqttAsyncClient is null, this function shall return null.]
    @Test
    public void disconnectReturnsNullIfNullAsyncClient() throws MqttException
    {
        //arrange
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);
        Deencapsulation.setField(mqttConnection, "mqttAsyncClient", null);

        //act
        IMqttToken actualToken = Deencapsulation.invoke(mqttConnection, "disconnect");

        //assert
        assertNull(actualToken);
    }

    //Tests_SRS_MQTTCONNECTION_34_018: [If the saved mqttAsyncClient is not null, this function shall invoke 
    // close on that object.]
    @Test
    public void closeInvokesCloseOnAsyncClient() throws MqttException
    {
        //arrange
        final MqttConnection mqttConnection = Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, SERVER_URI, CLIENT_ID, USER_NAME, PWORD, mockIotHubSSLContext);
        Deencapsulation.setField(mqttConnection, "mqttAsyncClient", mockMqttAsyncClient);

        //act
        Deencapsulation.invoke(mqttConnection, "close");

        //assert
        new Verifications()
        {
            {
                mockMqttAsyncClient.close();
                times = 1;
            }
        };
    }
}
