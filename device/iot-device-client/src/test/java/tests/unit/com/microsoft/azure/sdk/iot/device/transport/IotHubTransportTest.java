// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.DeviceClientException;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubServiceException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.exceptions.UnauthorizedException;
import com.microsoft.azure.sdk.iot.device.transport.*;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsIotHubConnection;
import com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions.AmqpConnectionThrottledException;
import com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions.AmqpUnauthorizedAccessException;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsIotHubConnection;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttIotHubConnection;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions.MqttUnauthorizedException;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason.*;
import static com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus.*;
import static junit.framework.TestCase.*;

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
    ScheduledExecutorService mockedScheduledExecutorService;

    @Mocked
    IotHubTransportConnection mockedIotHubTransportConnection;

    @Mocked
    HttpsIotHubConnection mockedHttpsIotHubConnection;

    @Mocked
    AmqpsIotHubConnection mockedAmqpsIotHubConnection;

    @Mocked
    MqttIotHubConnection mockedMqttIotHubConnection;

    @Mocked
    IotHubConnectionStateCallback mockedIotHubConnectionStateCallback;

    @Mocked
    IotHubConnectionStatusChangeCallback mockedIotHubConnectionStatusChangeCallback;

    @Mocked
    IotHubConnectionStatusChangeReason mockedIotHubConnectionStatusChangeReason;

    @Mocked
    RetryPolicy mockedRetryPolicy;

    @Mocked
    RetryDecision mockedRetryDecision;

    @Mocked
    MessageCallback mockedMessageCallback;

    @Mocked
    ScheduledExecutorService mockedTaskScheduler;

    @Mocked
    IotHubTransport.MessageRetryRunnable mockedMessageRetryRunnable;

    @Mocked
    IotHubServiceException mockedIothubServiceException;

    @Mocked
    Executors mockExecutors;

    //Tests_SRS_IOTHUBTRANSPORT_34_001: [The constructor shall save the default config.]
    //Tests_SRS_IOTHUBTRANSPORT_34_003: [The constructor shall set the connection status as DISCONNECTED and the current retry attempt to 0.]
    @Test
    public void constructorSucceeds()
    {
        //act
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

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
        IotHubTransport transport = new IotHubTransport(null, mockedIotHubConnectionStatusChangeCallback);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_004: [This function shall retrieve a packet from the inProgressPackets queue with the message id from the provided message if there is one.]
    //Tests_SRS_IOTHUBTRANSPORT_34_006: [If there was a packet in the inProgressPackets queue tied to the provided message, and the provided throwable is a TransportException, this function shall call "handleMessageException" with the provided packet and transport exception.]
    @Test
    public void onMessageSentRetrievesFromInProgressAndCallsHandleMessageExceptionForTransportException()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
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

    //Tests_SRS_IOTHUBTRANSPORT_34_005: [If there was a packet in the inProgressPackets queue tied to the provided message, and the provided throwable is null, this function shall set the status of that packet to OK_EMPTY and add it to the callbacks queue.]
    @Test
    public void onMessageSentRetrievesFromInProgressAndAddsToCallbackForNoException()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
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
                mockedPacket.setStatus(IotHubStatusCode.OK_EMPTY);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_007: [If there was a packet in the inProgressPackets queue tied to the provided message, and the provided throwable is not a TransportException, this function shall call "handleMessageException" with the provided packet and a new transport exception with the provided exception as the inner exception.]
    @Test
    public void onMessageSentRetrievesFromInProgressAndCallsHandleMessageExceptionForNonTransportException()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
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

    //Tests_SRS_IOTHUBTRANSPORT_34_009: [If this function is called with a non-null message and a null exception, this function shall add that message to the receivedMessagesQueue.]
    @Test
    public void onMessageReceivedWithMessageAndNoExceptionAddsToQueue()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

        //act
        transport.onMessageReceived(mockedTransportMessage, null);

        //assert
        Queue<IotHubTransportPacket> receivedMessagesQueue = Deencapsulation.getField(transport, "receivedMessagesQueue");
        assertEquals(1, receivedMessagesQueue.size());
        assertEquals(mockedTransportMessage, receivedMessagesQueue.poll());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_011: [If this function is called while the connection status is DISCONNECTED, this function shall do nothing.]
    @Test
    public void onConnectionLostWhileDisconnectedDoesNothing()
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock void handleDisconnection(TransportException exception)
            {
                if (exception.equals(mockedTransportException))
                {
                    methodsCalled.append("handleDisconnection");
                }
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", IotHubConnectionStatus.DISCONNECTED);
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedIotHubTransportConnection);
        Deencapsulation.setField(transport, "connectionStatus", IotHubConnectionStatus.CONNECTED);
        new Expectations()
        {
            {
                mockedIotHubTransportConnection.getConnectionId();
                result = "";
            }
        };

        //act
        transport.onConnectionLost(mockedTransportException, "");

        //assert
        assertEquals("handleDisconnection", methodsCalled.toString());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_078: [If this function is called with a connection id that is not the same
    // as the current connection id, this function shall do nothing.]
    @Test
    public void onConnectionLostWithWrongConnectionIdDoesNothing()
    {
        //arrange
        new MockUp<IotHubTransport>()
        {
            @Mock void handleDisconnection(TransportException transportException)
            {
                fail("should not have called this method");
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        final String expectedConnectionId = "1234";
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedIotHubTransportConnection);

        new Expectations()
        {
            {
                mockedIotHubTransportConnection.getConnectionId();
                result = expectedConnectionId;
            }
        };

        //act
        transport.onConnectionLost(mockedTransportException, "not the expected connection id");
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_012: [If this function is called with a TransportException, this function shall call handleDisconnection with that exception.]
    @Test
    public void onConnectionLostWithTransportExceptionCallsHandleDisconnection()
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock void handleDisconnection(TransportException exception)
            {
                if (exception.equals(mockedTransportException))
                {
                    methodsCalled.append("handleDisconnection");
                }
            }

            @Mock void addReceivedMessagesOverHttpToReceivedQueue()
            {
                methodsCalled.append("addReceivedMessagesOverHttpToReceivedQueue");
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        final IOException nonTransportException = new IOException();
        final String expectedConnectionId = "1234";
        Deencapsulation.setField(transport, "connectionStatus", IotHubConnectionStatus.CONNECTED);
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedIotHubTransportConnection);

        new Expectations()
        {
            {
                mockedIotHubTransportConnection.getConnectionId();
                result = expectedConnectionId;
            }
        };

        //act
        transport.onConnectionLost(mockedTransportException, expectedConnectionId);

        //assert
        assertEquals("handleDisconnection", methodsCalled.toString());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_013: [If this function is called with any other type of exception, this function shall call handleDisconnection with that exception as the inner exception in a new TransportException.]
    @Test
    public void onConnectionLostWithOtherExceptionType()
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock void handleDisconnection(TransportException exception)
            {
                if (exception.equals(mockedTransportException))
                {
                    methodsCalled.append("handleDisconnection");
                }
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        final IOException nonTransportException = new IOException();
        Deencapsulation.setField(transport, "connectionStatus", IotHubConnectionStatus.CONNECTED);
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedIotHubTransportConnection);
        final String expectedConnectionId = "1234";

        new Expectations()
        {
            {
                new TransportException(nonTransportException);
                result = mockedTransportException;

                Deencapsulation.invoke(transport, "handleDisconnection", new Class[] {TransportException.class}, mockedTransportException);

                mockedIotHubTransportConnection.getConnectionId();
                result = expectedConnectionId;
            }
        };

        //act
        transport.onConnectionLost(nonTransportException, expectedConnectionId);

        //assert
        assertEquals("handleDisconnection", methodsCalled.toString());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_014: [If the provided connectionId is associated with the current connection, This function shall invoke updateStatus with status CONNECTED, change reason CONNECTION_OK and a null throwable.]
    @Test
    public void onConnectionEstablishedCallsUpdateStatus()
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock void updateStatus(IotHubConnectionStatus newConnectionStatus, IotHubConnectionStatusChangeReason reason, Throwable throwable)
            {
                if (newConnectionStatus == CONNECTED && reason == CONNECTION_OK)
                {
                    methodsCalled.append("updateStatus");
                }
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        final String expectedConnectionId = "1234";
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedIotHubTransportConnection);

        new Expectations()
        {
            {
                mockedIotHubTransportConnection.getConnectionId();
                result = expectedConnectionId;
            }
        };

        //act
        transport.onConnectionEstablished(expectedConnectionId);

        //assert
        assertEquals("updateStatus", methodsCalled.toString());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_015: [If the provided list of configs is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void openThrowsForNullConfigList() throws DeviceClientException
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

        //act
        transport.open(null);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_015: [If the provided list of configs is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void openThrowsForEmptyConfigList() throws DeviceClientException
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

        //act
        transport.open(new ArrayList<DeviceClientConfig>());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_016: [If the connection status of this object is DISCONNECTED_RETRYING, this function shall throw a TransportException.]
    @Test (expected = TransportException.class)
    public void openThrowsIfConnectionStatusIsDisconnectedRetrying() throws DeviceClientException
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
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
        new MockUp<IotHubTransport>()
        {
            @Mock boolean isSasTokenExpired()
            {
                return true;
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", DISCONNECTED);
        Collection<DeviceClientConfig> configs = new ArrayList<>();
        configs.add(mockedConfig);

        //act
        transport.open(configs);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_019: [This function shall open the invoke the method openConnection.]
    @Test
    public void openCallsOpenConnection() throws DeviceClientException
    {
        //arrange
        final StringBuilder verifier = new StringBuilder();
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", DISCONNECTED);
        Collection<DeviceClientConfig> configs = new ArrayList<>();
        configs.add(mockedConfig);

        new MockUp<IotHubTransport>()
        {
            @Mock boolean isSasTokenExpired()
            {
                return false;
            }

            @Mock void openConnection()
            {
                verifier.append("Success");
            }
        };

        //act
        transport.open(configs);

        //assert
        assertTrue(verifier.toString().equalsIgnoreCase("Success"));
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_017: [If the connection status of this object is CONNECTED, this function shall do nothing.]
    @Test
    public void openDoesNothingIfConnectionStatusIsConnected() throws DeviceClientException
    {
        //arrange
        new MockUp<IotHubTransport>()
        {
            @Mock void openConnection()
            {
                fail("This method should not be called");
            }
        };

        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", CONNECTED);
        Collection<DeviceClientConfig> configs = new ArrayList<>();
        configs.add(mockedConfig);

        //act
        transport.open(configs);
    }


    //Tests_SRS_IOTHUBTRANSPORT_34_026: [If the supplied reason is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void closeThrowsForNullReason() throws DeviceClientException
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

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
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock  void invokeCallbacks()
            {
                methodsCalled.append("invokeCallbacks");
            }

            @Mock void updateStatus(IotHubConnectionStatus newConnectionStatus, IotHubConnectionStatusChangeReason reason, Throwable throwable)
            {
                if (newConnectionStatus == DISCONNECTED && reason == RETRY_EXPIRED)
                {
                    methodsCalled.append("updateStatus");
                }
            }
        };
        final Queue<IotHubTransportPacket> waitingPacketsQueue = new ConcurrentLinkedQueue<>();
        final Map<String, IotHubTransportPacket> inProgressPackets = new ConcurrentHashMap<>();
        final Queue<IotHubTransportPacket> callbackPacketsQueue = new ConcurrentLinkedQueue<>();
        waitingPacketsQueue.add(mockedPacket);
        waitingPacketsQueue.add(mockedPacket);
        inProgressPackets.put("1", mockedPacket);
        inProgressPackets.put("2", mockedPacket);


        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", CONNECTED);
        Deencapsulation.setField(transport, "waitingPacketsQueue", waitingPacketsQueue);
        Deencapsulation.setField(transport, "inProgressPackets", inProgressPackets);
        Deencapsulation.setField(transport, "callbackPacketsQueue", callbackPacketsQueue);
        Deencapsulation.setField(transport, "taskScheduler", mockedScheduledExecutorService);
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedIotHubTransportConnection);

        new Expectations()
        {
            {
                mockedPacket.setStatus(IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE);
                times = 4;
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
                mockedIotHubTransportConnection.close();
                times = 1;
            }
        };
        assertTrue(methodsCalled.toString().contains("updateStatus"));
        assertTrue(methodsCalled.toString().contains("invokeCallbacks"));
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_032: [If the provided exception is not a TransportException, this function shall return COMMUNICATION_ERROR.]
    @Test
    public void exceptionToStatusChangeReasonWithNonTransportException()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

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
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

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
        new MockUp<IotHubTransport>()
        {
            @Mock boolean isSasTokenExpired()
            {
                return true;
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

        new Expectations()
        {
            {
                mockedTransportException.isRetryable();
                result = false;
            }
        };

        //act
        IotHubConnectionStatusChangeReason reason = Deencapsulation.invoke(transport, "exceptionToStatusChangeReason", new Class[] {Throwable.class}, mockedTransportException);

        //assert
        assertEquals(EXPIRED_SAS_TOKEN, reason);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_035: [If the provided exception is a TransportException that isn't retryable and the saved sas token has not expired, but the exception is an unauthorized exception, this function shall return BAD_CREDENTIAL]
    @Test
    public void exceptionToStatusChangeReasonBadCredentialForAmqpUnauthorizedException(@Mocked final AmqpUnauthorizedAccessException mockedUnauthorizedException)
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

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
        IotHubConnectionStatusChangeReason reason = Deencapsulation.invoke(transport, "exceptionToStatusChangeReason", new Class[] {Throwable.class}, mockedUnauthorizedException);

        //assert
        assertEquals(BAD_CREDENTIAL, reason);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_035: [If the provided exception is a TransportException that isn't retryable and the saved sas token has not expired, but the exception is an unauthorized exception, this function shall return BAD_CREDENTIAL]
    @Test
    public void exceptionToStatusChangeReasonBadCredentialForMqttUnauthorizedException(@Mocked final MqttUnauthorizedException mockedUnauthorizedException)
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

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
        IotHubConnectionStatusChangeReason reason = Deencapsulation.invoke(transport, "exceptionToStatusChangeReason", new Class[] {Throwable.class}, mockedUnauthorizedException);

        //assert
        assertEquals(BAD_CREDENTIAL, reason);
    }


    //Tests_SRS_IOTHUBTRANSPORT_34_035: [If the provided exception is a TransportException that isn't retryable and the saved sas token has not expired, but the exception is an unauthorized exception, this function shall return BAD_CREDENTIAL]
    @Test
    public void exceptionToStatusChangeReasonBadCredentialForGenericUnauthorizedException(@Mocked final UnauthorizedException mockedUnauthorizedException)
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

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
        IotHubConnectionStatusChangeReason reason = Deencapsulation.invoke(transport, "exceptionToStatusChangeReason", new Class[] {Throwable.class}, mockedUnauthorizedException);

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
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        final Queue<DeviceClientConfig> configs = new ConcurrentLinkedQueue<>();
        configs.add(mockedConfig);
        Deencapsulation.setField(transport, "deviceClientConfigs", configs);
        Deencapsulation.setField(transport, "iotHubTransportConnection", null);
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
        final ScheduledExecutorService scheduledExecutorService = Deencapsulation.getField(transport, "scheduledExecutorService");
        new Verifications()
        {
            {
                mockedHttpsIotHubConnection.setListener(transport);
                times = 1;

                mockedHttpsIotHubConnection.open(configs, scheduledExecutorService);
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
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
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
        final ScheduledExecutorService scheduledExecutorService = Deencapsulation.getField(transport, "scheduledExecutorService");
        new Verifications()
        {
            {
                mockedMqttIotHubConnection.setListener(transport);
                times = 1;

                mockedMqttIotHubConnection.open(configs, scheduledExecutorService);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_036: [If the default config's protocol is MQTT or MQTT_WS, this function shall set this object's iotHubTransportConnection to a new MqttIotHubConnection object.]
    @Test
    public void openConnectionWithMqttWS() throws TransportException
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
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
        final ScheduledExecutorService scheduledExecutorService = Deencapsulation.getField(transport, "scheduledExecutorService");
        new Verifications()
        {
            {
                mockedMqttIotHubConnection.setListener(transport);
                times = 1;

                mockedMqttIotHubConnection.open(configs, scheduledExecutorService);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_037: [If the default config's protocol is AMQPS or AMQPS_WS, this function shall set this object's iotHubTransportConnection to a new AmqpsIotHubConnection object.]
    @Test
    public void openConnectionWithAmqps() throws TransportException
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        final Queue<DeviceClientConfig> configs = new ConcurrentLinkedQueue<>();
        configs.add(mockedConfig);
        Deencapsulation.setField(transport, "deviceClientConfigs", configs);
        new Expectations(IotHubTransport.class)
        {
            {
                mockedConfig.getProtocol();
                result = IotHubClientProtocol.AMQPS;
                Executors.newScheduledThreadPool(1);
                result = mockedScheduledExecutorService;
                new AmqpsIotHubConnection(mockedConfig);
                result = mockedAmqpsIotHubConnection;
            }
        };

        //act
        Deencapsulation.invoke(transport, "openConnection");

        //assert
        final ScheduledExecutorService scheduledExecutorService = Deencapsulation.getField(transport, "scheduledExecutorService");
        new Verifications()
        {
            {
                mockedAmqpsIotHubConnection.setListener(transport);
                times = 1;

                mockedAmqpsIotHubConnection.open(configs, scheduledExecutorService);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_037: [If the default config's protocol is AMQPS or AMQPS_WS, this function shall set this object's iotHubTransportConnection to a new AmqpsIotHubConnection object.]
    @Test
    public void openConnectionWithAmqpsWS() throws TransportException
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        final Queue<DeviceClientConfig> configs = new ConcurrentLinkedQueue<>();
        configs.add(mockedConfig);
        Deencapsulation.setField(transport, "deviceClientConfigs", configs);
        new Expectations(IotHubTransport.class)
        {
            {
                mockedConfig.getProtocol();
                result = IotHubClientProtocol.AMQPS_WS;
                Executors.newScheduledThreadPool(1);
                result = mockedScheduledExecutorService;
                new AmqpsIotHubConnection(mockedConfig);
                result = mockedAmqpsIotHubConnection;
            }
        };

        //act
        Deencapsulation.invoke(transport, "openConnection");

        //assert
        final ScheduledExecutorService scheduledExecutorService = Deencapsulation.getField(transport, "scheduledExecutorService");
        new Verifications()
        {
            {
                mockedAmqpsIotHubConnection.setListener(transport);
                times = 1;

                mockedAmqpsIotHubConnection.open(configs, scheduledExecutorService);
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
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
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

        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
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

        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
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

        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
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
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        new NonStrictExpectations()
        {
            {
                mockedConfig.getOperationTimeout();
                result = 0;
            }
        };

        //act
        boolean hasTimedOut = Deencapsulation.invoke(transport, "hasOperationTimedOut", 1L);

        //assert
        assertTrue(hasTimedOut);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_077: [If the provided start time is 0, this function shall return false.]
    @Test
    public void hasOperationTimedOutReturnsFalseIfProvidedTimeIsZero()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
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
        assertFalse(hasTimedOut);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_044: [This function shall return if the provided start time was long enough ago that it has passed the device operation timeout threshold.]
    @Test
    public void hasOperationTimedOutFalse()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
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

    //Tests_SRS_IOTHUBTRANSPORT_34_041: [If this object's connection state is DISCONNECTED, this function shall throw an IllegalStateException.]
    @Test (expected = IllegalStateException.class)
    public void addMessageThrowsIfDisconnected()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", DISCONNECTED);

        //act
        transport.addMessage(mockedMessage, mockedEventCallback, new Object());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_042: [This function shall build a transport packet from the provided message, callback, and context and then add that packet to the waiting queue.]
    @Test
    public void addMessageAddsMessage()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", CONNECTED);
        Queue<IotHubTransportPacket> waitingPacketsQueue = new ConcurrentLinkedQueue<>();
        Deencapsulation.setField(transport, "waitingPacketsQueue", waitingPacketsQueue);

        new NonStrictExpectations()
        {
            {
                new IotHubTransportPacket(mockedMessage, mockedEventCallback, any, null, anyLong);
                result = mockedPacket;
            }
        };

        //act
        transport.addMessage(mockedMessage, mockedEventCallback, new Object());

        //assert
        assertEquals(1, waitingPacketsQueue.size());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_043: [If the connection status of this object is not CONNECTED, this function shall do nothing]
    @Test
    public void sendMessagesDoesNothingIfNotConnected()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", DISCONNECTED);
        Queue<IotHubTransportPacket> waitingPacketsQueue = new ConcurrentLinkedQueue<>();
        waitingPacketsQueue.add(mockedPacket);
        Deencapsulation.setField(transport, "waitingPacketsQueue", waitingPacketsQueue);

        //act
        transport.sendMessages();

        //assert
        assertFalse(waitingPacketsQueue.isEmpty());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_044: [This function continue to dequeue packets saved in the waiting
    // queue and send them until connection status isn't CONNECTED or until 10 messages have been sent]
    @Test
    public void sendMessagesSendsMessages()
    {
        //arrange
        new MockUp<IotHubTransport>()
        {
            @Mock void sendPacket(IotHubTransportPacket packet)
            {
                //do nothing
            }
        };

        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        final int MAX_MESSAGES_TO_SEND_PER_THREAD = Deencapsulation.getField(transport, "MAX_MESSAGES_TO_SEND_PER_THREAD");
        Deencapsulation.setField(transport, "connectionStatus", CONNECTED);
        Queue<IotHubTransportPacket> waitingPacketsQueue = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < MAX_MESSAGES_TO_SEND_PER_THREAD + 1; i++)
        {
            waitingPacketsQueue.add(mockedPacket);
        }

        Deencapsulation.setField(transport, "waitingPacketsQueue", waitingPacketsQueue);

        //act
        transport.sendMessages();

        //assert
        assertEquals(1, waitingPacketsQueue.size());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_045: [This function shall dequeue each packet in the callback queue and execute
    // their saved callback with their saved status and context]
    @Test
    public void invokeCallbacksInvokesAllCallbacks(final @Mocked IotHubStatusCode mockedStatus)
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Queue<IotHubTransportPacket> callbackPacketsQueue = new ConcurrentLinkedQueue<>();
        callbackPacketsQueue.add(mockedPacket);
        callbackPacketsQueue.add(mockedPacket);
        callbackPacketsQueue.add(mockedPacket);
        Deencapsulation.setField(transport, "callbackPacketsQueue", callbackPacketsQueue);
        final Object context = new Object();
        new NonStrictExpectations()
        {
            {
                mockedPacket.getCallback();
                result = mockedEventCallback;

                mockedPacket.getContext();
                result = context;

                mockedPacket.getStatus();
                result = mockedStatus;
            }
        };

        //act
        transport.invokeCallbacks();

        //assert
        assertTrue(callbackPacketsQueue.isEmpty());
        new Verifications()
        {
            {
                mockedEventCallback.execute(mockedStatus, context);
                times = 3;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_046: [If this object's connection status is not CONNEECTED, this function shall do nothing.]
    @Test
    public void handleMessageDoesNothingIfNotConnected() throws DeviceClientException
    {
        //arrange
        new MockUp<IotHubTransport>()
        {
            @Mock void addReceivedMessagesOverHttpToReceivedQueue()
            {
                fail("should not have called this method");
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", DISCONNECTED);
        Queue<IotHubTransportMessage> receivedMessagesQueue = new ConcurrentLinkedQueue<>();
        receivedMessagesQueue.add(mockedTransportMessage);
        receivedMessagesQueue.add(mockedTransportMessage);
        Deencapsulation.setField(transport, "receivedMessagesQueue", receivedMessagesQueue);

        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedHttpsIotHubConnection);

        //act
        transport.handleMessage();

        //assert
        assertEquals(2, receivedMessagesQueue.size());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_047: [If this object's connection status is CONNECTED and is using HTTPS,
    // this function shall invoke addReceivedMessagesOverHttpToReceivedQueue.]
    @Test
    public void handleMessageChecksForHttpMessages() throws DeviceClientException
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock void acknowledgeReceivedMessage(IotHubTransportMessage receivedMessage)
            {
                if (receivedMessage.equals(mockedTransportMessage))
                {
                    methodsCalled.append("acknowledgeReceivedMessage");
                }
            }

            @Mock void addReceivedMessagesOverHttpToReceivedQueue()
            {
                methodsCalled.append("addReceivedMessagesOverHttpToReceivedQueue");
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", CONNECTED);
        Queue<IotHubTransportMessage> receivedMessagesQueue = new ConcurrentLinkedQueue<>();
        receivedMessagesQueue.add(mockedTransportMessage);
        receivedMessagesQueue.add(mockedTransportMessage);
        Deencapsulation.setField(transport, "receivedMessagesQueue", receivedMessagesQueue);

        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedHttpsIotHubConnection);

        //act
        transport.handleMessage();

        //assert
        assertEquals(1, receivedMessagesQueue.size());
        assertTrue(methodsCalled.toString().contains("addReceivedMessagesOverHttpToReceivedQueue"));
        assertTrue(methodsCalled.toString().contains("acknowledgeReceivedMessage"));
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_048: [If this object's connection status is CONNECTED and there is a
    // received message in the queue, this function shall acknowledge the received message
    @Test
    public void handleMessageAcknowledgesAReceivedMessages() throws DeviceClientException
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock void acknowledgeReceivedMessage(IotHubTransportMessage receivedMessage)
            {
                if (receivedMessage.equals(mockedTransportMessage))
                {
                    methodsCalled.append("acknowledgeReceivedMessage");
                }
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", CONNECTED);
        Queue<IotHubTransportMessage> receivedMessagesQueue = new ConcurrentLinkedQueue<>();
        receivedMessagesQueue.add(mockedTransportMessage);
        receivedMessagesQueue.add(mockedTransportMessage);
        Deencapsulation.setField(transport, "receivedMessagesQueue", receivedMessagesQueue);

        //act
        transport.handleMessage();

        //assert
        assertEquals(1, receivedMessagesQueue.size());
        assertEquals("acknowledgeReceivedMessage", methodsCalled.toString());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_049: [If the provided callback is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void registerConnectionStateCallbackThrowsForNullCallback()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

        //act
        transport.registerConnectionStateCallback(null, new Object());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_050: [This function shall save the provided callback and context.]
    @Test
    public void registerConnectionStateCallbackSavesProvidedCallbackAndContext()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        final Object context = new Object();

        //act
        transport.registerConnectionStateCallback(mockedIotHubConnectionStateCallback, context);

        //assert
        assertEquals(mockedIotHubConnectionStateCallback, Deencapsulation.getField(transport, "stateCallback"));
        assertEquals(context, Deencapsulation.getField(transport, "stateCallbackContext"));
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_051: [If the provided callback is null but the context is not, this function shall throw an IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
    public void registerConnectionStatusChangeCallbackThrowsForNullCallback()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

        //act
        transport.registerConnectionStatusChangeCallback(null, new Object());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_051: [If the provided callback is null but the context is not, this function shall throw an IllegalArgumentException.]
    @Test
    public void registerConnectionStatusChangeCallbackDoesNotThrowForNullCallbackIfContextIsAlsoNull()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        transport.registerConnectionStatusChangeCallback(mockedIotHubConnectionStatusChangeCallback, new Object());
        assertNotNull(Deencapsulation.getField(transport, "connectionStatusChangeCallback"));
        assertNotNull(Deencapsulation.getField(transport, "connectionStatusChangeCallbackContext"));

        //act
        transport.registerConnectionStatusChangeCallback(null, null);

        //assert
        assertNull(Deencapsulation.getField(transport, "connectionStatusChangeCallback"));
        assertNull(Deencapsulation.getField(transport, "connectionStatusChangeCallbackContext"));

    }

    //Tests_SRS_IOTHUBTRANSPORT_34_052: [This function shall save the provided callback and context.]
    @Test
    public void registerConnectionStatusChangeCallbackSavesProvidedCallbackAndContext()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        final Object context = new Object();

        //act
        transport.registerConnectionStatusChangeCallback(mockedIotHubConnectionStatusChangeCallback, context);

        //assert
        assertEquals(mockedIotHubConnectionStatusChangeCallback, Deencapsulation.getField(transport, "connectionStatusChangeCallback"));
        assertEquals(context, Deencapsulation.getField(transport, "connectionStatusChangeCallbackContext"));
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_053: [This function shall execute the callback associate with the provided
    // transport message with the provided message and its saved callback context.]
    //Tests_SRS_IOTHUBTRANSPORT_34_054: [This function shall send the message callback result along the
    // connection as the ack to the service.]
    @Test
    public void acknowledgeReceivedMessageSendsAck() throws TransportException
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        final Object context = new Object();
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedIotHubTransportConnection);
        new Expectations()
        {
            {
                mockedTransportMessage.getMessageCallback();
                result = mockedMessageCallback;

                mockedTransportMessage.getMessageCallbackContext();
                result = context;

                mockedMessageCallback.execute(mockedTransportMessage, context);
                result = IotHubMessageResult.COMPLETE;
            }
        };

        //act
        Deencapsulation.invoke(transport, "acknowledgeReceivedMessage", mockedTransportMessage);

        //assert
        new Verifications()
        {
            {
                mockedIotHubTransportConnection.sendMessageResult(mockedTransportMessage, IotHubMessageResult.COMPLETE);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_055: [If an exception is thrown while acknowledging the received message,
    // this function shall add the received message back into the receivedMessagesQueue and then rethrow the exception.]
    @Test
    public void acknowledgeReceivedMessageReQueuesFailedMessages() throws TransportException
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        final Object context = new Object();
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedIotHubTransportConnection);
        new Expectations()
        {
            {
                mockedTransportMessage.getMessageCallback();
                result = mockedMessageCallback;

                mockedTransportMessage.getMessageCallbackContext();
                result = context;

                mockedMessageCallback.execute(mockedTransportMessage, context);
                result = IotHubMessageResult.COMPLETE;

                mockedIotHubTransportConnection.sendMessageResult(mockedTransportMessage, IotHubMessageResult.COMPLETE);
                result = mockedTransportException;
            }
        };

        boolean exceptionRethrown = false;

        //act
        try
        {
            Deencapsulation.invoke(transport, "acknowledgeReceivedMessage", mockedTransportMessage);
        }
        catch (Exception e)
        {
            exceptionRethrown = true;
        }

        //assert
        assertTrue(exceptionRethrown);
        Queue<IotHubTransportMessage> receivedMessagesQueue = Deencapsulation.getField(transport, "receivedMessagesQueue");
        assertEquals(1, receivedMessagesQueue.size());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_056: [If the saved http transport connection can receive a message, add it to receivedMessagesQueue.]
    @Test
    public void addReceivedMessagesOverHttpToReceivedQueueChecksForHttpMessages() throws TransportException
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedHttpsIotHubConnection);

        //act
        Deencapsulation.invoke(transport, "addReceivedMessagesOverHttpToReceivedQueue");

        //assert
        Queue<IotHubTransportMessage> receivedMessagesQueue = Deencapsulation.getField(transport, "receivedMessagesQueue");
        assertEquals(1, receivedMessagesQueue.size());
        new Verifications()
        {
            {
                mockedHttpsIotHubConnection.receiveMessage();
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_057: [This function shall move all packets from inProgressQueue to waiting queue.]
    //Tests_SRS_IOTHUBTRANSPORT_34_058: [This function shall invoke updateStatus with DISCONNECTED_RETRYING, and the provided transportException.]
    //Tests_SRS_IOTHUBTRANSPORT_34_059: [This function shall invoke checkForUnauthorizedException with the provided exception.]
    //Tests_SRS_IOTHUBTRANSPORT_34_060: [This function shall invoke reconnect with the provided exception.]
    @Test
    public void handleDisconnectionClearsInProgressAndReconnects()
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock IotHubConnectionStatusChangeReason exceptionToStatusChangeReason(Throwable e)
            {
                methodsCalled.append("exceptionToStatusChangeReason");
                return mockedIotHubConnectionStatusChangeReason;
            }

            @Mock void updateStatus(IotHubConnectionStatus newConnectionStatus, IotHubConnectionStatusChangeReason reason, Throwable throwable)
            {
                methodsCalled.append("updateStatus");
            }

            @Mock void checkForUnauthorizedException(TransportException transportException)
            {
                methodsCalled.append("checkForUnauthorizedException");
            }

            @Mock void reconnect(TransportException transportException)
            {
                methodsCalled.append("reconnect");
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

        //act
        Deencapsulation.invoke(transport, "handleDisconnection", mockedTransportException);

        //assert
        assertTrue(methodsCalled.toString().contains("exceptionToStatusChangeReason"));
        assertTrue(methodsCalled.toString().contains("updateStatus"));
        assertTrue(methodsCalled.toString().contains("checkForUnauthorizedException"));
        assertTrue(methodsCalled.toString().contains("reconnect"));
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_061: [This function shall close the saved connection, and then invoke openConnection and return null.]
    @Test
    public void singleReconnectAttemptSuccess() throws TransportException
    {
        //arrange
        new MockUp<IotHubTransport>()
        {
            @Mock void openConnection()
            {
                //do nothing
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedIotHubTransportConnection);

        new Expectations()
        {
            {
                //open and close happen with no exception
                mockedIotHubTransportConnection.close();
            }
        };

        //act
        Exception result = Deencapsulation.invoke(transport, "singleReconnectAttempt");

        //assert
        assertNull(result);
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_062: [If an exception is encountered while closing or opening the connection,
    // this function shall invoke checkForUnauthorizedException on that exception and then return it.]
    @Test
    public void singleReconnectAttemptReturnsEncounteredException() throws TransportException
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedIotHubTransportConnection);

        new Expectations(IotHubTransport.class)
        {
            {
                //open and close happen with no exception
                mockedIotHubTransportConnection.close();
                result = mockedTransportException;

                Deencapsulation.invoke(transport, "checkForUnauthorizedException", mockedTransportException);
            }
        };

        //act
        Exception result = Deencapsulation.invoke(transport, "singleReconnectAttempt");

        //assert
        assertEquals(mockedTransportException, result);
        new Verifications()
        {
            {
                Deencapsulation.invoke(transport, "checkForUnauthorizedException", mockedTransportException);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_063: [If the provided transportException is retryable, the packet has not
    // timed out, and the retry policy allows, this function shall schedule a task to add the provided
    // packet to the waiting list after the amount of time determined by the retry policy.]
    @Test
    public void handleMessageExceptionSchedulesRetryIfRetryable()
    {
        //arrange
        new MockUp<IotHubTransport>()
        {
            @Mock boolean hasOperationTimedOut(long time)
            {
                return false;
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

        final long expectedDelay = 0;
        final long duration = 0;
        Deencapsulation.setField(transport, "taskScheduler", mockedTaskScheduler);
        new Expectations()
        {
            {
                mockedTransportException.isRetryable();
                result = true;

                mockedConfig.getRetryPolicy();
                result = mockedRetryPolicy;

                mockedRetryPolicy.getRetryDecision(anyInt, mockedTransportException);
                result = mockedRetryDecision;

                mockedRetryDecision.shouldRetry();
                result = true;

                mockedRetryDecision.getDuration();
                result = duration;
            }
        };

        //act
        Deencapsulation.invoke(transport, "handleMessageException", mockedPacket, mockedTransportException);

        //assert
        new Verifications()
        {
            {
                mockedPacket.incrementRetryAttempt();
                times = 1;

                mockedTaskScheduler.schedule((IotHubTransport.MessageRetryRunnable) any, expectedDelay, TimeUnit.MILLISECONDS);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_064: [If the provided transportException is not retryable, the packet has expired,
    // or if the retry policy says to not retry, this function shall add the provided packet to the callback queue.]
    @Test
    public void handleMessageExceptionDoesNotRetryIfDeviceOperationTimedOut(final @Mocked IotHubStatusCode mockedStatus)
    {
        //arrange
        new MockUp<IotHubTransport>()
        {
            @Mock boolean hasOperationTimedOut(long time)
            {
                return true;
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        final long expectedDelay = 0;
        Deencapsulation.setField(transport, "taskScheduler", mockedTaskScheduler);
        new NonStrictExpectations()
        {
            {
                mockedIothubServiceException.isRetryable();
                result = true;

                mockedConfig.getRetryPolicy();
                result = mockedRetryPolicy;

                mockedRetryPolicy.getRetryDecision(anyInt, mockedTransportException);
                result = mockedRetryDecision;

                mockedRetryDecision.shouldRetry();
                result = true;

                mockedIothubServiceException.getStatusCode();
                result = mockedStatus;
            }
        };

        //act
        Deencapsulation.invoke(transport, "handleMessageException", mockedPacket, mockedIothubServiceException);

        //assert
        Queue<IotHubTransportPacket> callbackQueue = Deencapsulation.getField(transport, "callbackPacketsQueue");
        assertEquals(1, callbackQueue.size());
        assertEquals(mockedPacket, callbackQueue.poll());
        new Verifications()
        {
            {
                mockedPacket.setStatus(mockedStatus);
                times = 1;

                mockedTaskScheduler.schedule((IotHubTransport.MessageRetryRunnable) any, expectedDelay, TimeUnit.MILLISECONDS);
                times = 0;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_064: [If the provided transportException is not retryable, the packet has expired,
    // or if the retry policy says to not retry, this function shall add the provided packet to the callback queue.]
    @Test
    public void handleMessageExceptionDoesNotRetryIfExceptionIsNotRetryable(final @Mocked IotHubStatusCode mockedStatus)
    {
        //arrange
        new MockUp<IotHubTransport>()
        {
            @Mock boolean hasOperationTimedOut(long time)
            {
                return false;
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        final long expectedDelay = 0;
        Deencapsulation.setField(transport, "taskScheduler", mockedTaskScheduler);
        new NonStrictExpectations()
        {
            {
                mockedTransportException.isRetryable();
                result = false;

                mockedConfig.getRetryPolicy();
                result = mockedRetryPolicy;

                mockedRetryPolicy.getRetryDecision(anyInt, mockedTransportException);
                result = mockedRetryDecision;

                mockedRetryDecision.shouldRetry();
                result = true;

                mockedIothubServiceException.getStatusCode();
                result = mockedStatus;
            }
        };

        //act
        Deencapsulation.invoke(transport, "handleMessageException", mockedPacket, mockedIothubServiceException);

        //assert
        Queue<IotHubTransportPacket> callbackQueue = Deencapsulation.getField(transport, "callbackPacketsQueue");
        assertEquals(1, callbackQueue.size());
        assertEquals(mockedPacket, callbackQueue.poll());
        new Verifications()
        {
            {
                mockedPacket.setStatus(mockedStatus);
                times = 1;

                mockedTaskScheduler.schedule((IotHubTransport.MessageRetryRunnable) any, expectedDelay, TimeUnit.MILLISECONDS);
                times = 0;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_064: [If the provided transportException is not retryable, the packet has expired,
    // or if the retry policy says to not retry, this function shall add the provided packet to the callback queue.]
    @Test
    public void handleMessageExceptionDoesNotRetryIfRetryPolicySaysToNotRetry(final @Mocked IotHubStatusCode mockedStatus)
    {
        //arrange
        new MockUp<IotHubTransport>()
        {
            @Mock boolean hasOperationTimedOut(long time)
            {
                return false;
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        final long expectedDelay = 0;
        Deencapsulation.setField(transport, "taskScheduler", mockedTaskScheduler);
        new NonStrictExpectations()
        {
            {
                mockedTransportException.isRetryable();
                result = true;

                mockedConfig.getRetryPolicy();
                result = mockedRetryPolicy;

                mockedRetryPolicy.getRetryDecision(anyInt, mockedTransportException);
                result = mockedRetryDecision;

                mockedRetryDecision.shouldRetry();
                result = false;

                mockedIothubServiceException.getStatusCode();
                result = mockedStatus;
            }
        };

        //act
        Deencapsulation.invoke(transport, "handleMessageException", mockedPacket, mockedIothubServiceException);

        //assert
        Queue<IotHubTransportPacket> callbackQueue = Deencapsulation.getField(transport, "callbackPacketsQueue");
        assertEquals(1, callbackQueue.size());
        assertEquals(mockedPacket, callbackQueue.poll());
        new Verifications()
        {
            {
                mockedPacket.setStatus(mockedStatus);
                times = 1;

                mockedTaskScheduler.schedule((IotHubTransport.MessageRetryRunnable) any, expectedDelay, TimeUnit.MILLISECONDS);
                times = 0;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_068: [If the reconnection effort ends because the retry policy said to
    // stop, this function shall invoke close with RETRY_EXPIRED and the last transportException.]
    //Tests_SRS_IOTHUBTRANSPORT_34_065: [If the saved reconnection attempt start time is 0, this function shall 
    // save the current time as the time that reconnection started.]
    //Tests_SRS_IOTHUBTRANSPORT_34_066: [This function shall attempt to reconnect while this object's state is
    // DISCONNECTED_RETRYING, the operation hasn't timed out, and the last transport exception is retryable.]
    @Test
    public void reconnectAttemptsToReconnectUntilRetryPolicyEnds()
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock boolean hasOperationTimedOut(long time)
            {
                return false;
            }

            @Mock void close(IotHubConnectionStatusChangeReason reason, Throwable cause)
            {
                if (reason == RETRY_EXPIRED)
                {
                    methodsCalled.append("close");
                }
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", DISCONNECTED_RETRYING);
        new Expectations()
        {
            {
                mockedTransportException.isRetryable();
                result = true;

                mockedConfig.getRetryPolicy();
                result = mockedRetryPolicy;

                mockedRetryPolicy.getRetryDecision(anyInt, (TransportException) any);
                result = mockedRetryDecision;

                mockedRetryDecision.shouldRetry();
                result = false;
            }
        };

        //act
        Deencapsulation.invoke(transport, "reconnect", mockedTransportException);

        //assert
        long reconnectionAttemptStartTimeMillis = Deencapsulation.getField(transport, "reconnectionAttemptStartTimeMillis");
        assertTrue(reconnectionAttemptStartTimeMillis > 0);
        assertEquals("close", methodsCalled.toString());
    }


    //Tests_SRS_IOTHUBTRANSPORT_34_069: [If the reconnection effort ends because the reconnection timed out,
    // this function shall invoke close with RETRY_EXPIRED and a DeviceOperationTimeoutException.]
    @Test
    public void reconnectAttemptsToReconnectUntilOperationTimesOut()
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock boolean hasOperationTimedOut(long time)
            {
                return true;
            }

            @Mock void close(IotHubConnectionStatusChangeReason reason, Throwable cause)
            {
                if (reason == RETRY_EXPIRED)
                {
                    methodsCalled.append("close");
                }
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", DISCONNECTED_RETRYING);

        //act
        Deencapsulation.invoke(transport, "reconnect", mockedTransportException);

        //assert
        long reconnectionAttemptStartTimeMillis = Deencapsulation.getField(transport, "reconnectionAttemptStartTimeMillis");
        assertTrue(reconnectionAttemptStartTimeMillis > 0);
        assertEquals("close", methodsCalled.toString());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_070: [If the reconnection effort ends because a terminal exception is
    // encountered, this function shall invoke close with that terminal exception.]
    @Test
    public void reconnectAttemptsToReconnectUntilExceptionNotRetryable()
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock boolean hasOperationTimedOut(long time)
            {
                return false;
            }

            @Mock void close(IotHubConnectionStatusChangeReason reason, Throwable cause)
            {
                methodsCalled.append("close");
            }

            @Mock IotHubConnectionStatusChangeReason exceptionToStatusChangeReason(Throwable e)
            {
                if (e.equals(mockedTransportException))
                {
                    methodsCalled.append("exceptionToStatusChangeReason");
                }

                return IotHubConnectionStatusChangeReason.BAD_CREDENTIAL;
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", DISCONNECTED_RETRYING);

        //act
        Deencapsulation.invoke(transport, "reconnect", mockedTransportException);

        //assert
        long reconnectionAttemptStartTimeMillis = Deencapsulation.getField(transport, "reconnectionAttemptStartTimeMillis");
        assertTrue(reconnectionAttemptStartTimeMillis > 0);
        assertTrue(methodsCalled.toString().contains("close"));
        assertTrue(methodsCalled.toString().contains("exceptionToStatusChangeReason"));
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_071: [If an exception is encountered while closing, this function shall invoke
    // updateStatus with DISCONNECTED, COMMUNICATION_ERROR, and the last transport exception.]
    @Test
    public void reconnectUpdatesStatusIfClosingFailed()
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock void addToCallbackQueue(IotHubTransportPacket packet)
            {
                methodsCalled.append("addToCallbackQueue");
            }

            @Mock boolean hasOperationTimedOut(long time)
            {
                return true;
            }

            @Mock void updateStatus(IotHubConnectionStatus newConnectionStatus, IotHubConnectionStatusChangeReason reason, Throwable throwable)
            {
                if (newConnectionStatus == DISCONNECTED && reason == COMMUNICATION_ERROR)
                {
                    methodsCalled.append("updateStatus");
                }
            }

            @Mock void close(IotHubConnectionStatusChangeReason reason, Throwable cause) throws TransportException
            {
                if (reason == RETRY_EXPIRED)
                {
                    methodsCalled.append("close");
                }

                throw new TransportException("close failed");
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", DISCONNECTED_RETRYING);
        new NonStrictExpectations()
        {
            {
                mockedTransportException.isRetryable();
                result = true;

                mockedConfig.getRetryPolicy();
                result = mockedRetryPolicy;

                mockedRetryPolicy.getRetryDecision(anyInt, (TransportException) any);
                result = mockedRetryDecision;

                mockedRetryDecision.shouldRetry();
                result = true;
            }
        };

        //act
        Deencapsulation.invoke(transport, "reconnect", mockedTransportException);

        //assert
        long reconnectionAttemptStartTimeMillis = Deencapsulation.getField(transport, "reconnectionAttemptStartTimeMillis");
        assertTrue(reconnectionAttemptStartTimeMillis > 0);
        assertTrue(methodsCalled.toString().contains("updateStatus"));
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_008:[This function shall set the packet status to MESSAGE_EXPIRED if packet has expired.]
    //Tests_SRS_IOTHUBTRANSPORT_28_009:[This function shall add the expired packet to the Callback Queue.]
    @Test
    public void isMessageValidWithMessageExpired()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        new NonStrictExpectations(IotHubTransport.class)
        {
            {
                mockedPacket.getMessage();
                result = mockedMessage;
                mockedMessage.isExpired();
                result = true;
                Deencapsulation.invoke(transport, "addToCallbackQueue", new Class[] {IotHubTransportPacket.class}, mockedPacket);
                Deencapsulation.invoke(transport, "isSasTokenExpired");
                result = false;
            }
        };

        //act
        boolean ret = Deencapsulation.invoke(transport, "isMessageValid", new Class[] {IotHubTransportPacket.class}, mockedPacket);

        //assert
        assertFalse(ret);
        new Verifications()
        {
            {
                mockedPacket.setStatus(IotHubStatusCode.MESSAGE_EXPIRED);
                times = 1;
                Deencapsulation.invoke(transport, "addToCallbackQueue", new Class[] {IotHubTransportPacket.class}, mockedPacket);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_008:[This function shall set the packet status to MESSAGE_EXPIRED if packet has expired.]
    //Tests_SRS_IOTHUBTRANSPORT_28_009:[This function shall add the expired packet to the Callback Queue.]
    //Tests_SRS_IOTHUBTRANSPORT_28_010:[This function shall set the packet status to UNAUTHORIZED if sas token has expired.]
    //Tests_SRS_IOTHUBTRANSPORT_28_011:[This function shall add the packet which sas token has expired to the Callback Queue.]
    @Test
    public void isMessageValidWithMessageNotExpiredAndValidSasToken()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        new NonStrictExpectations(IotHubTransport.class)
        {
            {
                mockedPacket.getMessage();
                result = mockedMessage;
                mockedMessage.isExpired();
                result = false;
                Deencapsulation.invoke(transport, "addToCallbackQueue", new Class[] {IotHubTransportPacket.class}, mockedPacket);
                Deencapsulation.invoke(transport, "isSasTokenExpired");
                result = false;
            }
        };

        //act
        boolean ret = Deencapsulation.invoke(transport, "isMessageValid", new Class[] {IotHubTransportPacket.class}, mockedPacket);

        //assert
        assertTrue(ret);
        new Verifications()
        {
            {
                mockedPacket.setStatus(IotHubStatusCode.MESSAGE_EXPIRED);
                times = 0;
                Deencapsulation.invoke(transport, "addToCallbackQueue", new Class[] {IotHubTransportPacket.class}, mockedPacket);
                times = 0;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_010:[This function shall set the packet status to UNAUTHORIZED if sas token has expired.]
    //Tests_SRS_IOTHUBTRANSPORT_28_011:[This function shall add the packet which sas token has expired to the Callback Queue.]
    @Test
    public void isMessageValidWithMessageNotExpiredSasTokenExpired()
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock void addToCallbackQueue(IotHubTransportPacket packet)
            {
                methodsCalled.append("addToCallbackQueue");
            }

            @Mock boolean isSasTokenExpired()
            {
                return true;
            }

            @Mock void updateStatus(IotHubConnectionStatus newConnectionStatus, IotHubConnectionStatusChangeReason reason, Throwable throwable)
            {
                if (newConnectionStatus == DISCONNECTED && reason == EXPIRED_SAS_TOKEN)
                {
                    methodsCalled.append("updateStatus");
                }
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        new NonStrictExpectations()
        {
            {
                mockedPacket.getMessage();
                result = mockedMessage;
                mockedMessage.isExpired();
                result = false;
            }
        };

        //act
        boolean ret = Deencapsulation.invoke(transport, "isMessageValid", new Class[] {IotHubTransportPacket.class}, mockedPacket);

        //assert
        assertFalse(ret);
        new Verifications()
        {
            {
                mockedPacket.setStatus(IotHubStatusCode.UNAUTHORIZED);
                times = 1;
            }
        };
        assertTrue(methodsCalled.toString().contains("addToCallbackQueue"));
        assertTrue(methodsCalled.toString().contains("updateStatus"));
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_005:[This function shall updated the saved connection status if the connection status has changed.]
    //Tests_SRS_IOTHUBTRANSPORT_28_006:[This function shall invoke all callbacks listening for the state change if the connection status has changed.]
    //Tests_SRS_IOTHUBTRANSPORT_28_007: [This function shall reset currentReconnectionAttempt and reconnectionAttemptStartTimeMillis if connection status is changed to CONNECTED.]
    @Test
    public void updateStatusConnectionStatusChangedToConnected()
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock void invokeConnectionStatusChangeCallback(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason reason, Throwable e)
            {
                methodsCalled.append("invokeConnectionStatusChangeCallback");
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", IotHubConnectionStatus.DISCONNECTED_RETRYING);
        Deencapsulation.setField(transport, "currentReconnectionAttempt", 5);
        Deencapsulation.setField(transport, "reconnectionAttemptStartTimeMillis", 5);

        //act
        Deencapsulation.invoke(transport, "updateStatus",
                new Class[] {IotHubConnectionStatus.class, IotHubConnectionStatusChangeReason.class, Throwable.class},
                IotHubConnectionStatus.CONNECTED, IotHubConnectionStatusChangeReason.NO_NETWORK, null);

        //assert
        assertEquals(IotHubConnectionStatus.CONNECTED, Deencapsulation.getField(transport, "connectionStatus"));
        assertEquals(0, Deencapsulation.getField(transport, "currentReconnectionAttempt"));
        assertEquals(0L, Deencapsulation.getField(transport, "reconnectionAttemptStartTimeMillis"));
        assertTrue(methodsCalled.toString().equalsIgnoreCase("invokeConnectionStatusChangeCallback"));
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_005:[This function shall updated the saved connection status if the connection status has changed.]
    //Tests_SRS_IOTHUBTRANSPORT_28_006:[This function shall invoke all callbacks listening for the state change if the connection status has changed.]
    //Tests_SRS_IOTHUBTRANSPORT_28_007: [This function shall reset currentReconnectionAttempt and reconnectionAttemptStartTimeMillis if connection status is changed to CONNECTED.]
    @Test
    public void updateStatusConnectionStatusNotChanged()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", IotHubConnectionStatus.CONNECTED);
        Deencapsulation.setField(transport, "currentReconnectionAttempt", 5);
        new Expectations(IotHubTransport.class)
        {
            {
                Deencapsulation.invoke(transport, "invokeConnectionStatusChangeCallback",
                        new Class[] {IotHubConnectionStatus.class, IotHubConnectionStatusChangeReason.class, Throwable.class},
                        IotHubConnectionStatus.CONNECTED, null, null);
            }
        };

        //act
        Deencapsulation.invoke(transport, "updateStatus",
                new Class[] {IotHubConnectionStatus.class, IotHubConnectionStatusChangeReason.class, Throwable.class},
                IotHubConnectionStatus.CONNECTED, null, null);

        //assert
        assertEquals(IotHubConnectionStatus.CONNECTED, Deencapsulation.getField(transport, "connectionStatus"));
        assertEquals(5, Deencapsulation.getField(transport, "currentReconnectionAttempt"));
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_005:[This function shall updated the saved connection status if the connection status has changed.]
    //Tests_SRS_IOTHUBTRANSPORT_28_006:[This function shall invoke all callbacks listening for the state change if the connection status has changed.]
    //Tests_SRS_IOTHUBTRANSPORT_28_007: [This function shall reset currentReconnectionAttempt and reconnectionAttemptStartTimeMillis if connection status is changed to CONNECTED.]
    @Test
    public void updateStatusConnectionStatusChangedToDisconnected()
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock void invokeConnectionStatusChangeCallback(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason reason, Throwable e)
            {
                if (status == DISCONNECTED && reason == NO_NETWORK)
                {
                    methodsCalled.append("invokeConnectionStatusChangeCallback");
                }
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatus", IotHubConnectionStatus.DISCONNECTED_RETRYING);
        Deencapsulation.setField(transport, "currentReconnectionAttempt", 5);

        //act
        Deencapsulation.invoke(transport, "updateStatus",
                new Class[] {IotHubConnectionStatus.class, IotHubConnectionStatusChangeReason.class, Throwable.class},
                IotHubConnectionStatus.DISCONNECTED, IotHubConnectionStatusChangeReason.NO_NETWORK, null);

        //assert
        assertEquals(IotHubConnectionStatus.DISCONNECTED, Deencapsulation.getField(transport, "connectionStatus"));
        assertEquals(5, Deencapsulation.getField(transport, "currentReconnectionAttempt"));
        assertEquals("invokeConnectionStatusChangeCallback", methodsCalled.toString());
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_004:[This function shall notify the connection status change callback if the callback is not null]
    @Test
    public void invokeConnectionStatusChangeCallbackWithCallback()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatusChangeCallback", mockedIotHubConnectionStatusChangeCallback);

        //act
        Deencapsulation.invoke(transport, "invokeConnectionStatusChangeCallback",
                new Class[] {IotHubConnectionStatus.class, IotHubConnectionStatusChangeReason.class, Throwable.class},
                IotHubConnectionStatus.CONNECTED, IotHubConnectionStatusChangeReason.CONNECTION_OK, new IOException());

        //assert
        new Verifications()
        {
            {
                mockedIotHubConnectionStatusChangeCallback.execute(
                        (IotHubConnectionStatus)any,
                        (IotHubConnectionStatusChangeReason)any,
                        (Throwable)any,
                        any);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_004:[This function shall notify the connection status change callback if the callback is not null]
    @Test
    public void invokeConnectionStatusChangeCallbackWithNullCallback()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Deencapsulation.setField(transport, "connectionStatusChangeCallback", null);

        //act
        Deencapsulation.invoke(transport, "invokeConnectionStatusChangeCallback",
                new Class[] {IotHubConnectionStatus.class, IotHubConnectionStatusChangeReason.class, Throwable.class},
                IotHubConnectionStatus.CONNECTED, IotHubConnectionStatusChangeReason.CONNECTION_OK, new IOException());

        //assert

        new Verifications()
        {
            {
                mockedIotHubConnectionStatusChangeCallback.execute(
                        (IotHubConnectionStatus)any,
                        (IotHubConnectionStatusChangeReason)any,
                        (Throwable)any,
                        any);
                times = 0;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_003: [This function shall indicate if the device's sas token is expired.]
    @Test
    public void isSasTokenExpiredAuthenticationTypeIsSasTokenAndExpired()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        new NonStrictExpectations()
        {
            {
                mockedConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockedConfig.getSasTokenAuthentication().isRenewalNecessary();
                result = true;
            }
        };

        //act
        boolean ret = Deencapsulation.invoke(transport, "isSasTokenExpired");

        //assert
        assertTrue(ret);
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_003: [This function shall indicate if the device's sas token is expired.]
    @Test
    public void isSasTokenExpiredAuthenticationTypeIsSasTokenAndNotExpired()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        new NonStrictExpectations()
        {
            {
                mockedConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockedConfig.getSasTokenAuthentication().isRenewalNecessary();
                result = false;
            }
        };

        //act
        boolean ret = Deencapsulation.invoke(transport, "isSasTokenExpired");

        //assert
        assertFalse(ret);
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_003: [This function shall indicate if the device's sas token is expired.]
    @Test
    public void isSasTokenExpiredAuthenticationTypeNotSasToken()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        new NonStrictExpectations()
        {
            {
                mockedConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.X509_CERTIFICATE;
                mockedConfig.getSasTokenAuthentication().isRenewalNecessary();
                result = true;
            }
        };

        //act
        boolean ret = Deencapsulation.invoke(transport, "isSasTokenExpired");

        //assert
        assertFalse(ret);
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_002: [This function shall add the packet to the callback queue if it has a callback.]
    @Test
    public void addToCallbackQueuePacketHasCallback(@Mocked final IotHubEventCallback mockCallback)
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        new NonStrictExpectations()
        {
            {
                mockedPacket.getCallback();
                result = mockCallback;
            }
        };

        //act
        Deencapsulation.invoke(transport, "addToCallbackQueue", mockedPacket);

        //assert
        Queue<IotHubTransportPacket> callbackPacketsQueue = Deencapsulation.getField(transport, "callbackPacketsQueue");
        assertEquals(1, callbackPacketsQueue.size());
        assertTrue(callbackPacketsQueue.contains(mockedPacket));
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_002: [This function shall add the packet to the callback queue if it has a callback.]
    @Test
    public void addToCallbackQueuePacketNoCallback(@Mocked final IotHubEventCallback mockCallback)
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        new NonStrictExpectations()
        {
            {
                mockedPacket.getCallback();
                result = null;
            }
        };

        //act
        Deencapsulation.invoke(transport, "addToCallbackQueue", mockedPacket);

        //assert
        Queue<IotHubTransportPacket> callbackPacketsQueue = Deencapsulation.getField(transport, "callbackPacketsQueue");
        assertEquals(0, callbackPacketsQueue.size());
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_001: [This function shall set the MqttUnauthorizedException as retryable if the sas token has not expired.]
    @Test
    public void checkForUnauthorizedExceptionInMqttUnauthroizedException()
    {
        //arrange
        new MockUp<IotHubTransport>()
        {
            @Mock boolean isSasTokenExpired()
            {
                return false;
            }
        };
        final MqttUnauthorizedException testException = new MqttUnauthorizedException();
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

        //act
        Deencapsulation.invoke(transport, "checkForUnauthorizedException", testException);

        //assert
        new Verifications()
        {
            {
                testException.setRetryable(true);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_001: [This function shall set the MqttUnauthorizedException, UnauthorizedException or
    //AmqpUnauthorizedAccessException as retryable if the sas token has not expired.]
    @Test
    public void checkForUnauthorizedExceptionInUnauthorizedException()
    {
        //arrange
        new MockUp<IotHubTransport>()
        {
            @Mock boolean isSasTokenExpired()
            {
                return false;
            }
        };

        final UnauthorizedException testException = new UnauthorizedException();
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

        //act
        Deencapsulation.invoke(transport, "checkForUnauthorizedException", testException);

        //assert
        new Verifications()
        {
            {
                testException.setRetryable(true);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_001: [This function shall set the MqttUnauthorizedException, UnauthorizedException or
    //AmqpUnauthorizedAccessException as retryable if the sas token has not expired.]
    @Test
    public void checkForUnauthorizedExceptionInAmqpUnauthorizedAccessException()
    {
        //arrange
        new MockUp<IotHubTransport>()
        {
            @Mock boolean isSasTokenExpired()
            {
                return false;
            }
        };

        final AmqpUnauthorizedAccessException testException = new AmqpUnauthorizedAccessException();
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

        //act
        Deencapsulation.invoke(transport, "checkForUnauthorizedException", testException);

        //assert
        new Verifications()
        {
            {
                testException.setRetryable(true);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_001: [This function shall set the MqttUnauthorizedException, UnauthorizedException or
    //AmqpUnauthorizedAccessException as retryable if the sas token has not expired.]
    @Test
    public void checkForUnauthorizedExceptionWithExpiredSASToken()
    {
        //arrange
        new MockUp<IotHubTransport>()
        {
            @Mock boolean isSasTokenExpired()
            {
                return true;
            }
        };
        final UnauthorizedException testException = new UnauthorizedException();
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

        //act
        Deencapsulation.invoke(transport, "checkForUnauthorizedException", testException);

        //assert
        new Verifications()
        {
            {
                testException.setRetryable(true);
                times = 0;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_28_001: [This function shall set the MqttUnauthorizedException, UnauthorizedException or
    //AmqpUnauthorizedAccessException as retryable if the sas token has not expired.]
    @Test
    public void checkForUnauthorizedExceptionWithOtherTransportException()
    {
        //arrange
        new MockUp<IotHubTransport>()
        {
            @Mock boolean isSasTokenExpired()
            {
                return true;
            }
        };
        final TransportException testException = new TransportException();
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

        //act
        Deencapsulation.invoke(transport, "checkForUnauthorizedException", testException);

        //assert
        new Verifications()
        {
            {
                testException.setRetryable(true);
                times = 0;
            }
        };
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_072: [This function shall check if the provided message should expect an ACK or not.]
    //Tests_SRS_IOTHUBTRANSPORT_34_073: [This function shall send the provided message over the saved connection
    // and save the response code.]
    @Test
    public void sendPacketHappyPathWithAck() throws TransportException
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Map<String, IotHubTransportPacket> inProgressMessages = new HashMap<>();
        Deencapsulation.setField(transport, "inProgressPackets", inProgressMessages);
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedHttpsIotHubConnection);
        new NonStrictExpectations()
        {
            {
                mockedPacket.getMessage();
                result = mockedTransportMessage;

                mockedTransportMessage.isMessageAckNeeded((IotHubClientProtocol) any);
                result = true;

                mockedHttpsIotHubConnection.sendMessage((Message) any);
                result = IotHubStatusCode.OK_EMPTY;
            }
        };

        //act
        Deencapsulation.invoke(transport, "sendPacket", mockedPacket);

        //assert
        assertEquals(1, inProgressMessages.size());
    }


    //Tests_SRS_IOTHUBTRANSPORT_34_074: [If the response from sending is not OK or OK_EMPTY, this function
    // shall invoke handleMessageException with that message.]
    @Test
    public void sendPacketReceivesStatusThatIsNotOkOrOkEmpty() throws TransportException
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock void handleMessageException(IotHubTransportPacket packet, TransportException exception)
            {
                if (packet.equals(mockedPacket))
                {
                    methodsCalled.append("handleMessageException");
                }
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Map<String, IotHubTransportPacket> inProgressMessages = new HashMap<>();
        Deencapsulation.setField(transport, "inProgressPackets", inProgressMessages);
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedHttpsIotHubConnection);
        new Expectations()
        {
            {
                mockedPacket.getMessage();
                result = mockedTransportMessage;

                mockedTransportMessage.isMessageAckNeeded((IotHubClientProtocol) any);
                result = true;

                mockedHttpsIotHubConnection.sendMessage((Message) any);
                result = IotHubStatusCode.HUB_OR_DEVICE_ID_NOT_FOUND;
            }
        };

        //act
        Deencapsulation.invoke(transport, "sendPacket", mockedPacket);

        //assert
        assertEquals(0, inProgressMessages.size());
        assertEquals("handleMessageException", methodsCalled.toString());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_075: [If the response from sending is OK or OK_EMPTY and no ack is expected,
    // this function shall put that set that status in the sent packet and add that packet to the callbacks queue.]
    @Test
    public void sendPacketHappyPathWithoutAck() throws TransportException
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Map<String, IotHubTransportPacket> inProgressMessages = new HashMap<>();
        Queue<IotHubTransportPacket> callbackPacketsQueue = new ConcurrentLinkedQueue<>();
        Deencapsulation.setField(transport, "callbackPacketsQueue", callbackPacketsQueue);
        Deencapsulation.setField(transport, "inProgressPackets", inProgressMessages);
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedHttpsIotHubConnection);
        new NonStrictExpectations()
        {
            {
                mockedPacket.getMessage();
                result = mockedTransportMessage;

                mockedTransportMessage.isMessageAckNeeded((IotHubClientProtocol) any);
                result = false;

                mockedHttpsIotHubConnection.sendMessage((Message) any);
                result = IotHubStatusCode.OK_EMPTY;
            }
        };

        //act
        Deencapsulation.invoke(transport, "sendPacket", mockedPacket);

        //assert
        assertEquals(0, inProgressMessages.size());
        assertEquals(1, callbackPacketsQueue.size());
        new Verifications()
        {
            {
                mockedPacket.setStatus(IotHubStatusCode.OK_EMPTY);
                times = 1;
            }
        };
    }


    //Tests_SRS_IOTHUBTRANSPORT_34_076: [If an exception is encountered while sending the message, this function
    // shall invoke handleMessageException with that packet.]
    @Test
    public void sendPacketFailsToSend() throws TransportException
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock void handleMessageException(IotHubTransportPacket packet, TransportException exception)
            {
                methodsCalled.append("handleMessageException");
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Map<String, IotHubTransportPacket> inProgressMessages = new HashMap<>();
        Queue<IotHubTransportPacket> callbackPacketsQueue = new ConcurrentLinkedQueue<>();
        Deencapsulation.setField(transport, "callbackPacketsQueue", callbackPacketsQueue);
        Deencapsulation.setField(transport, "inProgressPackets", inProgressMessages);
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedHttpsIotHubConnection);
        new Expectations()
        {
            {
                mockedPacket.getMessage();
                result = mockedTransportMessage;

                mockedTransportMessage.isMessageAckNeeded((IotHubClientProtocol) any);
                result = false;

                mockedHttpsIotHubConnection.sendMessage((Message) any);
                result = mockedTransportException;
            }
        };

        //act
        Deencapsulation.invoke(transport, "sendPacket", mockedPacket);

        //assert
        assertEquals(0, inProgressMessages.size());
        assertEquals(0, callbackPacketsQueue.size());
        assertEquals(methodsCalled.toString(), "handleMessageException");
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_076: [If an exception is encountered while sending the message, this function
    // shall invoke handleMessageException with that packet.]
    @Test
    public void sendPacketFailsToSendAndExpectsAck() throws TransportException
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<IotHubTransport>()
        {
            @Mock void handleMessageException(IotHubTransportPacket packet, TransportException exception)
            {
                if (packet.equals(mockedPacket) && exception.equals(mockedTransportException))
                {
                    methodsCalled.append("handleMessageException");
                }
            }
        };
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Map<String, IotHubTransportPacket> inProgressMessages = new HashMap<>();
        Queue<IotHubTransportPacket> callbackPacketsQueue = new ConcurrentLinkedQueue<>();
        Deencapsulation.setField(transport, "callbackPacketsQueue", callbackPacketsQueue);
        Deencapsulation.setField(transport, "inProgressPackets", inProgressMessages);
        Deencapsulation.setField(transport, "iotHubTransportConnection", mockedHttpsIotHubConnection);
        new Expectations()
        {
            {
                mockedPacket.getMessage();
                result = mockedTransportMessage;

                mockedTransportMessage.isMessageAckNeeded((IotHubClientProtocol) any);
                result = true;

                mockedHttpsIotHubConnection.sendMessage((Message) any);
                result = mockedTransportException;
            }
        };

        //act
        Deencapsulation.invoke(transport, "sendPacket", mockedPacket);

        //assert
        assertEquals(0, inProgressMessages.size());
        assertEquals(0, callbackPacketsQueue.size());
        assertEquals("handleMessageException", methodsCalled.toString());
    }

    //Tests_SRS_IOTHUBTRANSPORT_34_079: [If the provided transportException is an AmqpConnectionThrottledException,
    // this function shall set the status of the callback packet to the error code for THROTTLED.]
    @Test
    public void handleMessageExceptionChecksForAmqpThrottling()
    {
        //arrange
        final IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);

        //act
        Deencapsulation.invoke(transport, "handleMessageException", new Class[] {IotHubTransportPacket.class, TransportException.class}, mockedPacket, new AmqpConnectionThrottledException());

        //assert
        new Verifications()
        {
            {
                mockedPacket.setStatus(IotHubStatusCode.THROTTLED);
                times = 1;
            }
        };
    }

    @Test
    public void sendMessagesChecksForExpiredMessagesInWaitingQueue()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Map<String, IotHubTransportPacket> inProgressMessages = new HashMap<>();
        Queue<IotHubTransportPacket> callbackPacketsQueue = new ConcurrentLinkedQueue<>();
        Queue<IotHubTransportPacket> waitingPacketsQueue = new ConcurrentLinkedQueue<>();
        Deencapsulation.setField(transport, "callbackPacketsQueue", callbackPacketsQueue);
        Deencapsulation.setField(transport, "inProgressPackets", inProgressMessages);
        Deencapsulation.setField(transport, "waitingPacketsQueue", waitingPacketsQueue);
        Deencapsulation.setField(transport, "connectionStatus", CONNECTED);

        waitingPacketsQueue.add(mockedPacket);

        new Expectations()
        {
            {
                mockedPacket.getMessage();
                result = mockedMessage;
                mockedMessage.isExpired();
                result = true;
            }
        };

        //act
        transport.sendMessages();

        //assert
        assertTrue(waitingPacketsQueue.isEmpty());
        assertTrue(callbackPacketsQueue.contains(mockedPacket));
        new Verifications()
        {
            {
                mockedPacket.setStatus(IotHubStatusCode.MESSAGE_EXPIRED);
                times = 1;
            }
        };
    }

    @Test
    public void sendMessagesChecksForExpiredMessagesInInProgressPackets()
    {
        //arrange
        IotHubTransport transport = new IotHubTransport(mockedConfig, mockedIotHubConnectionStatusChangeCallback);
        Map<String, IotHubTransportPacket> inProgressMessages = new HashMap<>();
        Queue<IotHubTransportPacket> callbackPacketsQueue = new ConcurrentLinkedQueue<>();
        Queue<IotHubTransportPacket> waitingPacketsQueue = new ConcurrentLinkedQueue<>();
        Deencapsulation.setField(transport, "callbackPacketsQueue", callbackPacketsQueue);
        Deencapsulation.setField(transport, "inProgressPackets", inProgressMessages);
        Deencapsulation.setField(transport, "waitingPacketsQueue", waitingPacketsQueue);
        Deencapsulation.setField(transport, "connectionStatus", CONNECTED);

        inProgressMessages.put("someMessageId", mockedPacket);

        new Expectations()
        {
            {
                mockedPacket.getMessage();
                result = mockedMessage;
                mockedMessage.isExpired();
                result = true;
            }
        };

        //act
        transport.sendMessages();

        //assert
        assertTrue(callbackPacketsQueue.contains(mockedPacket));
        assertTrue(inProgressMessages.isEmpty());
        new Verifications()
        {
            {
                mockedPacket.setStatus(IotHubStatusCode.MESSAGE_EXPIRED);
                times = 1;
            }
        };
    }
}
