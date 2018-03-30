// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.DeviceClientException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.*;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsIotHubConnection;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsIotHubConnection;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttIotHubConnection;
import mockit.*;
import org.junit.Test;

import javax.xml.ws.http.HTTPBinding;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;

import static com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason.*;
import static com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus.CONNECTED;
import static com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus.DISCONNECTED;
import static com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus.DISCONNECTED_RETRYING;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Unit tests for IotHubTransportPacket.
 */
public class IotHubTransportTest
{
    @Mocked
    DeviceClientConfig mockedConfig;

    @Mocked
    Message mockedMessage;

    @Mocked
    IotHubTransportMessage mockedTransportMessage;

    @Mocked
    TransportException mockedTransportException;

    @Mocked
    IotHubTransportPacket mockedPacket;

    @Mocked
    IotHubEventCallback mockedEventCallback;

    @Mocked
    CustomLogger mockedLogger;

    @Mocked
    ScheduledExecutorService mockedScheduledExecutorService;

    @Mocked
    IotHubTransportConnection mockedIotHubTransportConnection;

    @Mocked
    HttpsIotHubConnection mockedHttpsIotHubConnection;

    @Mocked
    AmqpsIotHubConnection mockedAmqpsIotHubConnection;

    @Mocked
    MqttIotHubConnection mockedMqttIotHubConnection;

    //Tests_SRS_IOTHUBTRANSPORT_34_001: [The constructor shall save the default config.]
    //Tests_SRS_IOTHUBTRANSPORT_34_003: [The constructor shall set the connection status as DISCONNECTED and the current retry attempt to 0.]
    @Test
    public void constructorSucceeds()
    {
        //act
        IotHubTransport transport = new IotHubTransport(mockedConfig);

        //assert
        assertEquals(mockedConfig, Deencapsulation.getField(transport, "defaultConfig"));
        assertEquals(DISCONNECTED, Deencapsulation.getField(transport, "connectionStatus"));
        assertEquals(0, Deencapsulation.getField(transport, "currentReconnectionAttempt"));
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_002: [If the provided config is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullConfig()
    {
        //act
        IotHubTransport transport = new IotHubTransport(null);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_004: [This function shall retrieve a packet from the inProgressPackets queue with the message id from the provided message if there is one.]
    //Tests_SRS_IOTHUBTRANSPORT_34_006: [If there was a packet in the inProgressPackets queue tied to the provided message, and the provided throwable is a TransportException, this function shall call "handleMessageException" with the provided packet and transport exception.]
    @Test
    public void onMessageSentRetrievesFromInProgressAndCallsHandleMessageExceptionForTransportException()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        final String messageId = "1234";
        final Map<String, IotHubTransportPacket> inProgressPackets = new ConcurrentHashMap<>();
        inProgressPackets.put(messageId, mockedPacket);
        Deencapsulation.setField(transport, "connectionStatus", CONNECTED);
        Deencapsulation.setField(transport, "inProgressPackets", inProgressPackets);
        new Expectations(IotHubTransport.class)
        {
            {
                mockedMessage.getMessageId();
                result = messageId;

                Deencapsulation.invoke(transport, "handleMessageException", new Class[] {IotHubTransportPacket.class, TransportException.class}, mockedPacket, mockedTransportException);
            }
        };

        //act
        transport.onMessageSent(mockedMessage, mockedTransportException);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(transport, "handleMessageException", new Class[] {IotHubTransportPacket.class, TransportException.class}, mockedPacket, mockedTransportException);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_005: [If there was a packet in the inProgressPackets queue tied to the provided message, and the provided throwable is null, this function shall set the status of that packet to OK and add it to the callbacks queue.]
    @Test
    public void onMessageSentRetrievesFromInProgressAndAddsToCallbackForNoException()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        final String messageId = "1234";
        final Map<String, IotHubTransportPacket> inProgressPackets = new ConcurrentHashMap<>();
        inProgressPackets.put(messageId, mockedPacket);
        Deencapsulation.setField(transport, "connectionStatus", CONNECTED);
        Deencapsulation.setField(transport, "inProgressPackets", inProgressPackets);
        new NonStrictExpectations()
        {
            {
                mockedMessage.getMessageId();
                result = messageId;
            }
        };

        //act
        transport.onMessageSent(mockedMessage, null);

        //assert
        Queue<IotHubTransportPacket> callbackPacketsQueue = Deencapsulation.getField(transport, "callbackPacketsQueue");
        assertEquals(1, callbackPacketsQueue.size());
        assertTrue(callbackPacketsQueue.contains(mockedPacket));
        new Verifications()
        {
            {
                mockedPacket.setStatus(IotHubStatusCode.OK);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_007: [If there was a packet in the inProgressPackets queue tied to the provided message, and the provided throwable is not a TransportException, this function shall call "handleMessageException" with the provided packet and a new transport exception with the provided exception as the inner exception.]
    @Test
    public void onMessageSentRetrievesFromInProgressAndCallsHandleMessageExceptionForNonTransportException()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        final String messageId = "1234";
        final IOException nonTransportException = new IOException();
        final Map<String, IotHubTransportPacket> inProgressPackets = new ConcurrentHashMap<>();
        inProgressPackets.put(messageId, mockedPacket);
        Deencapsulation.setField(transport, "connectionStatus", CONNECTED);
        Deencapsulation.setField(transport, "inProgressPackets", inProgressPackets);
        new Expectations(IotHubTransport.class)
        {
            {
                mockedMessage.getMessageId();
                result = messageId;

                new TransportException(nonTransportException);
                result = mockedTransportException;

                Deencapsulation.invoke(transport, "handleMessageException", new Class[] {IotHubTransportPacket.class, TransportException.class}, mockedPacket, mockedTransportException);
            }
        };

        //act
        transport.onMessageSent(mockedMessage, nonTransportException);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(transport, "handleMessageException", new Class[] {IotHubTransportPacket.class, TransportException.class}, mockedPacket, mockedTransportException);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_008: [If this function is called with a non-null message and a non-null throwable, this function shall log an IllegalArgumentException.]
    @Test
    public void onMessageReceivedWithMessageAndExceptionOnlyLogsException()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig);

        //act
        transport.onMessageReceived(mockedTransportMessage, mockedTransportException);

        //assert
        Queue<IotHubTransportPacket> receivedMessagesQueue = Deencapsulation.getField(transport, "receivedMessagesQueue");
        assertTrue(receivedMessagesQueue.isEmpty());
        new Verifications()
        {
            {
                mockedLogger.LogError((IllegalArgumentException) any);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_009: [If this function is called with a non-null message and a null exception, this function shall add that message to the receivedMessagesQueue.]
    @Test
    public void onMessageReceivedWithMessageAndNoExceptionAddsToQueue()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig);

        //act
        transport.onMessageReceived(mockedTransportMessage, null);

        //assert
        Queue<IotHubTransportPacket> receivedMessagesQueue = Deencapsulation.getField(transport, "receivedMessagesQueue");
        assertEquals(1, receivedMessagesQueue.size());
        assertEquals(mockedTransportMessage, receivedMessagesQueue.poll());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_010: [If this function is called with a null message and a non-null throwable, this function shall log that exception.]
    @Test
    public void onMessageReceivedWithOnlyExceptionOnlyLogsException()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig);

        //act
        transport.onMessageReceived(null, mockedTransportException);

        //assert
        Queue<IotHubTransportPacket> receivedMessagesQueue = Deencapsulation.getField(transport, "receivedMessagesQueue");
        assertTrue(receivedMessagesQueue.isEmpty());
        new Verifications()
        {
            {
                mockedLogger.LogError(mockedTransportException);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_011: [If this function is called while the connection status is DISCONNECTED, this function shall do nothing.]
    @Test
    public void onConnectionLostWhileDisconnectedDoesNothing()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        Deencapsulation.setField(transport, "connectionStatus", IotHubConnectionStatus.DISCONNECTED);
        new NonStrictExpectations(IotHubTransport.class)
        {
            {
                Deencapsulation.invoke(transport, "handleDisconnection", new Class[] {TransportException.class}, mockedTransportException);
            }
        };

        //act
        transport.onConnectionLost(mockedTransportException);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(transport, "handleDisconnection", new Class[] {TransportException.class}, mockedTransportException);
                times = 0;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_012: [If this function is called with a TransportException, this function shall call handleDisconnection with that exception.]
    @Test
    public void onConnectionLostWithTransportExceptionCallsHandleDisconnection()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        final IOException nonTransportException = new IOException();
        Deencapsulation.setField(transport, "connectionStatus", IotHubConnectionStatus.CONNECTED);
        new Expectations(IotHubTransport.class)
        {
            {
                Deencapsulation.invoke(transport, "handleDisconnection", new Class[] {TransportException.class}, mockedTransportException);
            }
        };

        //act
        transport.onConnectionLost(mockedTransportException);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(transport, "handleDisconnection", new Class[] {TransportException.class}, mockedTransportException);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_013: [If this function is called with any other type of exception, this function shall call handleDisconnection with that exception as the inner exception in a new TransportException.]
    @Test
    public void onConnectionLostWithOtherExceptionType()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        final IOException nonTransportException = new IOException();
        Deencapsulation.setField(transport, "connectionStatus", IotHubConnectionStatus.CONNECTED);
        new Expectations(IotHubTransport.class)
        {
            {
                new TransportException(nonTransportException);
                result = mockedTransportException;

                Deencapsulation.invoke(transport, "handleDisconnection", new Class[] {TransportException.class}, mockedTransportException);
            }
        };

        //act
        transport.onConnectionLost(nonTransportException);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(transport, "handleDisconnection", new Class[] {TransportException.class}, mockedTransportException);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_014: [This function shall invoke updateStatus with status CONNECTED, change reason CONNECTION_OK and a null throwable.]
    @Test
    public void onConnectionEstablishedCallsUpdateStatus()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        new Expectations(IotHubTransport.class)
        {
            {
                Deencapsulation.invoke(transport, "updateStatus",
                        new Class[] {IotHubConnectionStatus.class, IotHubConnectionStatusChangeReason.class, Throwable.class},
                        CONNECTED, CONNECTION_OK, null);
            }
        };

        //act
        transport.onConnectionEstablished();

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(transport, "updateStatus",
                        new Class[] {IotHubConnectionStatus.class, IotHubConnectionStatusChangeReason.class, Throwable.class},
                        CONNECTED, CONNECTION_OK, null);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_015: [If the provided list of configs is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void openThrowsForNullConfigList() throws DeviceClientException
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig);

        //act
        transport.open(null);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_015: [If the provided list of configs is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void openThrowsForEmptyConfigList() throws DeviceClientException
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig);

        //act
        transport.open(new ArrayList<DeviceClientConfig>());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_016: [If the connection status of this object is DISCONNECTED_RETRYING, this function shall throw a TransportException.]
    @Test (expected = TransportException.class)
    public void openThrowsIfConnectionStatusIsDisconnectedRetrying() throws DeviceClientException
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig);
        Deencapsulation.setField(transport, "connectionStatus", DISCONNECTED_RETRYING);
        Collection<DeviceClientConfig> configs = new ArrayList<>();
        configs.add(mockedConfig);

        //act
        transport.open(configs);
    }


    //Tests_SRS_IOTHUBTRANSPORT_34_018: [If the saved SAS token has expired, this function shall throw a SecurityException.]
    @Test (expected = SecurityException.class)
    public void openThrowsIfSasTokenExpired() throws DeviceClientException
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        Deencapsulation.setField(transport, "connectionStatus", DISCONNECTED);
        Collection<DeviceClientConfig> configs = new ArrayList<>();
        configs.add(mockedConfig);

        new Expectations(IotHubTransport.class)
        {
            {
                Deencapsulation.invoke(transport, "isSasTokenExpired");
                result = true;
            }
        };

        //act
        transport.open(configs);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_019: [This function shall open the invoke the method openConnection.]
    @Test
    public void openCallsOpenConnection() throws DeviceClientException
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        Deencapsulation.setField(transport, "connectionStatus", DISCONNECTED);
        Collection<DeviceClientConfig> configs = new ArrayList<>();
        configs.add(mockedConfig);

        new Expectations(IotHubTransport.class)
        {
            {
                Deencapsulation.invoke(transport, "isSasTokenExpired");
                result = false;

                Deencapsulation.invoke(transport, "openConnection");
            }
        };

        //act
        transport.open(configs);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(transport, "openConnection");
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_017: [If the connection status of this object is CONNECTED, this function shall do nothing.]
    @Test
    public void openDoesNothingIfConnectionStatusIsConnected() throws DeviceClientException
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        Deencapsulation.setField(transport, "connectionStatus", CONNECTED);
        Collection<DeviceClientConfig> configs = new ArrayList<>();
        configs.add(mockedConfig);

        new NonStrictExpectations(IotHubTransport.class)
        {
            {
                Deencapsulation.invoke(transport, "openConnection");
                times = 0;
            }
        };

        //act
        transport.open(configs);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(transport, "openConnection");
                times = 0;
            }
        };
    }


    //Tests_SRS_IOTHUBTRANSPORT_34_026: [If the supplied reason is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void closeThrowsForNullReason() throws DeviceClientException
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig);

        //act
        transport.close(null, mockedTransportException);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_021: [This function shall move all waiting messages to the callback queue with status MESSAGE_CANCELLED_ONCLOSE.]
    //Tests_SRS_IOTHUBTRANSPORT_34_022: [This function shall move all in progress messages to the callback queue with status MESSAGE_CANCELLED_ONCLOSE.]
    //Tests_SRS_IOTHUBTRANSPORT_34_023: [This function shall invoke all callbacks.]
    //Tests_SRS_IOTHUBTRANSPORT_34_024: [This function shall close the connection.]
    //Tests_SRS_IOTHUBTRANSPORT_34_025: [This function shall invoke updateStatus with status DISCONNECTED and the supplied reason and cause.]
    @Test
    public void closeMovesAllWaitingAndInProgressMessagesToCallbackQueueWithStatusMessageCancelledOnClose() throws DeviceClientException
    {
        //arrange
        final Queue<IotHubTransportPacket> waitingPacketsQueue = new ConcurrentLinkedQueue<>();
        final Map<String, IotHubTransportPacket> inProgressPackets = new ConcurrentHashMap<>();
        final Queue<IotHubTransportPacket> callbackPacketsQueue = new ConcurrentLinkedQueue<>();
        waitingPacketsQueue.add(mockedPacket);
        waitingPacketsQueue.add(mockedPacket);
        inProgressPackets.put("1", mockedPacket);
        inProgressPackets.put("2", mockedPacket);


        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        Deencapsulation.setField(transport, "connectionStatus", CONNECTED);
        Deencapsulation.setField(transport, "waitingPacketsQueue", waitingPacketsQueue);
        Deencapsulation.setField(transport, "inProgressPackets", inProgressPackets);
        Deencapsulation.setField(transport, "callbackPacketsQueue", callbackPacketsQueue);
        Deencapsulation.setField(transport, "taskScheduler", mockedScheduledExecutorService);
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedIotHubTransportConnection);

        new Expectations(IotHubTransport.class)
        {
            {
                Deencapsulation.invoke(transport, "invokeCallbacks");

                mockedPacket.setStatus(IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE);
                times = 4;

                Deencapsulation.invoke(transport, "updateStatus",
                        new Class[] {IotHubConnectionStatus.class, IotHubConnectionStatusChangeReason.class, Throwable.class},
                        DISCONNECTED, RETRY_EXPIRED, mockedTransportException);
            }
        };

        //act
        transport.close(RETRY_EXPIRED, mockedTransportException);

        //assert
        assertEquals(4, callbackPacketsQueue.size());
        while (!callbackPacketsQueue.isEmpty())
        {
            assertEquals(mockedPacket, callbackPacketsQueue.poll());
        }
        new Verifications()
        {
            {
                Deencapsulation.invoke(transport, "invokeCallbacks");
                times = 1;

                mockedIotHubTransportConnection.close();
                times = 1;

                Deencapsulation.invoke(transport, "updateStatus",
                        new Class[] {IotHubConnectionStatus.class, IotHubConnectionStatusChangeReason.class, Throwable.class},
                        DISCONNECTED, RETRY_EXPIRED, mockedTransportException);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_020: [If this object's connection status is DISCONNECTED, this function shall do nothing.]
    @Test
    public void closeWhenDisconnectedDoesNothing() throws DeviceClientException
    {
        //arrange
        final Queue<IotHubTransportPacket> waitingPacketsQueue = new ConcurrentLinkedQueue<>();
        final Map<String, IotHubTransportPacket> inProgressPackets = new ConcurrentHashMap<>();
        final Queue<IotHubTransportPacket> callbackPacketsQueue = new ConcurrentLinkedQueue<>();
        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        Deencapsulation.setField(transport, "connectionStatus", DISCONNECTED);
        Deencapsulation.setField(transport, "waitingPacketsQueue", waitingPacketsQueue);
        Deencapsulation.setField(transport, "inProgressPackets", inProgressPackets);
        Deencapsulation.setField(transport, "callbackPacketsQueue", callbackPacketsQueue);
        Deencapsulation.setField(transport, "taskScheduler", mockedScheduledExecutorService);
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedIotHubTransportConnection);

        new Expectations(IotHubTransport.class)
        {
            {
                mockedPacket.setStatus(IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE);
                times = 0;
            }
        };

        //act
        transport.close(RETRY_EXPIRED, mockedTransportException);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(transport, "invokeCallbacks");
                times = 0;

                mockedIotHubTransportConnection.close();
                times = 0;

                Deencapsulation.invoke(transport, "updateStatus",
                        new Class[] {IotHubConnectionStatus.class, IotHubConnectionStatusChangeReason.class, Throwable.class},
                        DISCONNECTED, RETRY_EXPIRED, mockedTransportException);
                times = 0;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_032: [If the provided exception is not a TransportException, this function shall return COMMUNICATION_ERROR.]
    @Test
    public void exceptionToStatusChangeReasonWithNonTransportException()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig);

        //act
        IotHubConnectionStatusChangeReason reason = Deencapsulation.invoke(transport, "exceptionToStatusChangeReason", new Class[] {Throwable.class}, new IOException());

        //assert
        assertEquals(COMMUNICATION_ERROR, reason);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_033: [If the provided exception is a retryable TransportException, this function shall return NO_NETWORK.]
    @Test
    public void exceptionToStatusChangeReasonWithRetryableTransportException()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig);

        new NonStrictExpectations()
        {
            {
                mockedTransportException.isRetryable();
                result = true;
            }
        };

        //act
        IotHubConnectionStatusChangeReason reason = Deencapsulation.invoke(transport, "exceptionToStatusChangeReason", new Class[] {Throwable.class}, mockedTransportException);

        //assert
        assertEquals(NO_NETWORK, reason);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_034: [If the provided exception is a TransportException that isn't retryable and the saved sas token has expired, this function shall return EXPIRED_SAS_TOKEN.]
    @Test
    public void exceptionToStatusChangeReasonSasTokenExpired()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig);

        new NonStrictExpectations(IotHubTransport.class)
        {
            {
                mockedTransportException.isRetryable();
                result = false;

                Deencapsulation.invoke(transport, "isSasTokenExpired");
                result = true;
            }
        };

        //act
        IotHubConnectionStatusChangeReason reason = Deencapsulation.invoke(transport, "exceptionToStatusChangeReason", new Class[] {Throwable.class}, mockedTransportException);

        //assert
        assertEquals(EXPIRED_SAS_TOKEN, reason);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_035: [If the provided exception is a TransportException that isn't retryable and the saved sas token has not expired, this function shall return BAD_CREDENTIAL.]
    @Test
    public void exceptionToStatusChangeReasonBadCredential()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig);

        new NonStrictExpectations(IotHubTransport.class)
        {
            {
                mockedTransportException.isRetryable();
                result = false;

                Deencapsulation.invoke(transport, "isSasTokenExpired");
                result = false;
            }
        };

        //act
        IotHubConnectionStatusChangeReason reason = Deencapsulation.invoke(transport, "exceptionToStatusChangeReason", new Class[] {Throwable.class}, mockedTransportException);

        //assert
        assertEquals(BAD_CREDENTIAL, reason);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_035: [If the default config's protocol is HTTPS, this function shall set this object's iotHubTransportConnection to a new HttpsIotHubConnection object.]
    //Tests_SRS_IOTHUBTRANSPORT_34_038: [This function shall set this object as the listener of the iotHubTransportConnection object.]
    //Tests_SRS_IOTHUBTRANSPORT_34_039: [This function shall open the iotHubTransportConnection object with the saved list of configs.]
    //Tests_SRS_IOTHUBTRANSPORT_34_040: [This function shall invoke the method updateStatus with status CONNECTED, reason CONNECTION_OK, and a null throwable.]
    @Test
    public void openConnectionWithHttp()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        final Queue<DeviceClientConfig> configs = new ConcurrentLinkedQueue<>();
        configs.add(mockedConfig);
        Deencapsulation.setField(transport, "deviceClientConfigs", configs);
        new Expectations(IotHubTransport.class)
        {
            {
                mockedConfig.getProtocol();
                result = IotHubClientProtocol.HTTPS;

                new HttpsIotHubConnection(mockedConfig);
                result = mockedHttpsIotHubConnection;
            }
        };

        //act
        Deencapsulation.invoke(transport, "openConnection");

        //assert
        new Verifications()
        {
            {
                mockedHttpsIotHubConnection.setListener(transport);
                times = 1;

                mockedHttpsIotHubConnection.open(configs);
                times = 1;

                Deencapsulation.invoke(transport, "updateStatus",
                        new Class[] {IotHubConnectionStatus.class, IotHubConnectionStatusChangeReason.class, Throwable.class},
                        CONNECTED, CONNECTION_OK, null);
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_036: [If the default config's protocol is MQTT or MQTT_WS, this function shall set this object's iotHubTransportConnection to a new MqttIotHubConnection object.]
    @Test
    public void openConnectionWithMqtt() throws TransportException
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        final Queue<DeviceClientConfig> configs = new ConcurrentLinkedQueue<>();
        configs.add(mockedConfig);
        Deencapsulation.setField(transport, "deviceClientConfigs", configs);
        new Expectations(IotHubTransport.class)
        {
            {
                mockedConfig.getProtocol();
                result = IotHubClientProtocol.MQTT;

                new MqttIotHubConnection(mockedConfig);
                result = mockedMqttIotHubConnection;
            }
        };

        //act
        Deencapsulation.invoke(transport, "openConnection");

        //assert
        new Verifications()
        {
            {
                mockedMqttIotHubConnection.setListener(transport);
                times = 1;

                mockedMqttIotHubConnection.open(configs);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_036: [If the default config's protocol is MQTT or MQTT_WS, this function shall set this object's iotHubTransportConnection to a new MqttIotHubConnection object.]
    @Test
    public void openConnectionWithMqttWS() throws TransportException
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        final Queue<DeviceClientConfig> configs = new ConcurrentLinkedQueue<>();
        configs.add(mockedConfig);
        Deencapsulation.setField(transport, "deviceClientConfigs", configs);
        new Expectations(IotHubTransport.class)
        {
            {
                mockedConfig.getProtocol();
                result = IotHubClientProtocol.MQTT_WS;

                new MqttIotHubConnection(mockedConfig);
                result = mockedMqttIotHubConnection;
            }
        };

        //act
        Deencapsulation.invoke(transport, "openConnection");

        //assert
        new Verifications()
        {
            {
                mockedMqttIotHubConnection.setListener(transport);
                times = 1;

                mockedMqttIotHubConnection.open(configs);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_037: [If the default config's protocol is AMQPS or AMQPS_WS, this function shall set this object's iotHubTransportConnection to a new AmqpsIotHubConnection object.]
    @Test
    public void openConnectionWithAmqps() throws TransportException
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        final Queue<DeviceClientConfig> configs = new ConcurrentLinkedQueue<>();
        configs.add(mockedConfig);
        Deencapsulation.setField(transport, "deviceClientConfigs", configs);
        new Expectations(IotHubTransport.class)
        {
            {
                mockedConfig.getProtocol();
                result = IotHubClientProtocol.AMQPS;

                new AmqpsIotHubConnection(mockedConfig);
                result = mockedAmqpsIotHubConnection;
            }
        };

        //act
        Deencapsulation.invoke(transport, "openConnection");

        //assert
        new Verifications()
        {
            {
                mockedAmqpsIotHubConnection.setListener(transport);
                times = 1;

                mockedAmqpsIotHubConnection.open(configs);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_037: [If the default config's protocol is AMQPS or AMQPS_WS, this function shall set this object's iotHubTransportConnection to a new AmqpsIotHubConnection object.]
    @Test
    public void openConnectionWithAmqpsWS() throws TransportException
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        final Queue<DeviceClientConfig> configs = new ConcurrentLinkedQueue<>();
        configs.add(mockedConfig);
        Deencapsulation.setField(transport, "deviceClientConfigs", configs);
        new Expectations(IotHubTransport.class)
        {
            {
                mockedConfig.getProtocol();
                result = IotHubClientProtocol.AMQPS_WS;

                new AmqpsIotHubConnection(mockedConfig);
                result = mockedAmqpsIotHubConnection;
            }
        };

        //act
        Deencapsulation.invoke(transport, "openConnection");

        //assert
        new Verifications()
        {
            {
                mockedAmqpsIotHubConnection.setListener(transport);
                times = 1;

                mockedAmqpsIotHubConnection.open(configs);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_043: [This function return true if and only if there are no packets in the waiting queue, in progress, or in the callbacks queue.]
    @Test
    public void isEmptyReturnsTrueIfAllQueuesEmpty()
    {
        //arrange
        final Queue<IotHubTransportPacket> waitingPacketsQueue = new ConcurrentLinkedQueue<>();
        final Map<String, IotHubTransportPacket> inProgressPackets = new ConcurrentHashMap<>();
        final Queue<IotHubTransportPacket> callbackPacketsQueue = new ConcurrentLinkedQueue<>();
        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        Deencapsulation.setField(transport, "waitingPacketsQueue", waitingPacketsQueue);
        Deencapsulation.setField(transport, "inProgressPackets", inProgressPackets);
        Deencapsulation.setField(transport, "callbackPacketsQueue", callbackPacketsQueue);

        //act
        boolean isEmpty = transport.isEmpty();

        //assert
        assertTrue(isEmpty);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_043: [This function return true if and only if there are no packets in the waiting queue, in progress, or in the callbacks queue.]
    @Test
    public void isEmptyReturnsFalseIfWaitingQueueNotEmpty()
    {
        //arrange
        final Queue<IotHubTransportPacket> waitingPacketsQueue = new ConcurrentLinkedQueue<>();
        final Map<String, IotHubTransportPacket> inProgressPackets = new ConcurrentHashMap<>();
        final Queue<IotHubTransportPacket> callbackPacketsQueue = new ConcurrentLinkedQueue<>();
        waitingPacketsQueue.add(mockedPacket);

        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        Deencapsulation.setField(transport, "waitingPacketsQueue", waitingPacketsQueue);
        Deencapsulation.setField(transport, "inProgressPackets", inProgressPackets);
        Deencapsulation.setField(transport, "callbackPacketsQueue", callbackPacketsQueue);

        //act
        boolean isEmpty = transport.isEmpty();

        //assert
        assertFalse(isEmpty);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_043: [This function return true if and only if there are no packets in the waiting queue, in progress, or in the callbacks queue.]
    @Test
    public void isEmptyReturnsFalseIfInProgressMapNotEmpty()
    {
        //arrange
        final Queue<IotHubTransportPacket> waitingPacketsQueue = new ConcurrentLinkedQueue<>();
        final Map<String, IotHubTransportPacket> inProgressPackets = new ConcurrentHashMap<>();
        final Queue<IotHubTransportPacket> callbackPacketsQueue = new ConcurrentLinkedQueue<>();
        inProgressPackets.put("asdf", mockedPacket);

        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        Deencapsulation.setField(transport, "waitingPacketsQueue", waitingPacketsQueue);
        Deencapsulation.setField(transport, "inProgressPackets", inProgressPackets);
        Deencapsulation.setField(transport, "callbackPacketsQueue", callbackPacketsQueue);

        //act
        boolean isEmpty = transport.isEmpty();

        //assert
        assertFalse(isEmpty);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_043: [This function return true if and only if there are no packets in the waiting queue, in progress, or in the callbacks queue.]
    @Test
    public void isEmptyReturnsFalseIfCallbackQueueNotEmpty()
    {
        //arrange
        final Queue<IotHubTransportPacket> waitingPacketsQueue = new ConcurrentLinkedQueue<>();
        final Map<String, IotHubTransportPacket> inProgressPackets = new ConcurrentHashMap<>();
        final Queue<IotHubTransportPacket> callbackPacketsQueue = new ConcurrentLinkedQueue<>();
        callbackPacketsQueue.add(mockedPacket);

        final IotHubTransport transport = new IotHubTransport(mockedConfig);
        Deencapsulation.setField(transport, "waitingPacketsQueue", waitingPacketsQueue);
        Deencapsulation.setField(transport, "inProgressPackets", inProgressPackets);
        Deencapsulation.setField(transport, "callbackPacketsQueue", callbackPacketsQueue);

        //act
        boolean isEmpty = transport.isEmpty();

        //assert
        assertFalse(isEmpty);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_044: [This function shall return if the provided start time was long enough ago that it has passed the device operation timeout threshold.]
    @Test
    public void hasOperationTimedOutTrue()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig);
        new NonStrictExpectations()
        {
            {
                mockedConfig.getOperationTimeout();
                result = 0;
            }
        };

        //act
        boolean hasTimedOut = Deencapsulation.invoke(transport, "hasOperationTimedOut", 0L);

        //assert
        assertTrue(hasTimedOut);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_044: [This function shall return if the provided start time was long enough ago that it has passed the device operation timeout threshold.]
    @Test
    public void hasOperationTimedOutFalse()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig);
        new NonStrictExpectations()
        {
            {
                mockedConfig.getOperationTimeout();
                result = Long.MAX_VALUE;
            }
        };

        //act
        boolean hasTimedOut = Deencapsulation.invoke(transport, "hasOperationTimedOut", 0L);

        //assert
        assertFalse(hasTimedOut);
    }

    //TODO
    //Tests_SRS_IOTHUBTRANSPORT_34_041: [If this object's connection state is DISCONNECTED, this function shall throw an IllegalStateException.]
    //Tests_SRS_IOTHUBTRANSPORT_34_042: [This function shall build a transport packet from the provided message, callback, and context and then add that packet to the waiting queue.]


}