// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.transport.IotHubCallbackPacket;
import com.microsoft.azure.sdk.iot.device.transport.IotHubOutboundPacket;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttIotHubConnection;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttTransport;
import junit.framework.AssertionFailedError;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Unit tests for MqttTransport.java
 * Method: 90%
 * Lines: 92%
 */
public class MqttTransportTest
{
    @Mocked
    DeviceClientConfig mockedConfig;

    @Mocked
    MqttIotHubConnection mockedConnection;

    @Mocked
    IotHubSasTokenAuthenticationProvider mockedSasTokenAuthentication;

    @Mocked
    IotHubConnectionStateCallback mockedConnectionStateCallback;

    @Mocked
    ConnectionStateCallbackContext mockedConnectionStateCallbackContext;

    @Mocked
    IotHubEventCallback mockedIotHubEventCallback;

    @Mocked
    Message mockedMessage;

    @Mocked
    IotHubCallbackPacket mockedCallbackPacket;

    @Mocked
    Collection<DeviceClientConfig> mockedCollection;

    @Mocked
    Queue<DeviceClientConfig> mockedQueue;

    private class ConnectionStateCallbackContext {}

    private class IsEmptyRunnable implements Runnable
    {
        private MqttTransport mqttTransport;

        public IsEmptyRunnable(MqttTransport mqttTransport)
        {
            this.mqttTransport = mqttTransport;
        }

        @Override
        public void run()
        {
            assertTrue("Expected isEmpty to return true, check lock structure", mqttTransport.isEmpty());
        }
    }

    private class AddMessageRunnable implements Runnable
    {
        private final MqttTransport mqttTransport;

        public AddMessageRunnable(MqttTransport mqttTransport)
        {
            this.mqttTransport = mqttTransport;
        }

        @Override
        public void run()
        {
            mqttTransport.addMessage(mockedMessage, mockedIotHubEventCallback, null);
        }
    }

    private class SendMessagesRunnable implements Runnable
    {
        private final MqttTransport mqttTransport;

        public SendMessagesRunnable(MqttTransport mqttTransport)
        {
            this.mqttTransport = mqttTransport;
        }

        @Override
        public void run()
        {
            this.mqttTransport.sendMessages();
        }
    }

    private class InvokeCallbacksRunnable implements Runnable
    {
        private final MqttTransport mqttTransport;

        public InvokeCallbacksRunnable(MqttTransport mqttTransport)
        {
            this.mqttTransport = mqttTransport;
        }

        @Override
        public void run()
        {
            this.mqttTransport.invokeCallbacks();
        }
    }

    private class CloseRunnable implements Runnable
    {
        private final MqttTransport mqttTransport;

        public CloseRunnable(MqttTransport mqttTransport)
        {
            this.mqttTransport = mqttTransport;
        }

        @Override
        public void run()
        {
            try
            {
                this.mqttTransport.close();
            }
            catch (IOException e)
            {
                fail("Unexpected exception thrown: " + e);
            }
        }
    }

    //Tests_SRS_MQTTTRANSPORT_34_003: [This function shall open the connection of the saved MqttIotHubConnection object.]
    @Test
    public void openOpensMqttConnection() throws IOException, NoSuchFieldException, IllegalAccessException
    {
        new NonStrictExpectations()
        {
            {
                new MqttIotHubConnection(mockedConfig);
                result = mockedConnection;
            }
        };

        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);

        final MqttIotHubConnection expectedConnection = mockedConnection;
        new Verifications()
        {
            {
                expectedConnection.open(mockedQueue);
            }
        };

        Field handleMessageLock = transport.getClass().getDeclaredField("handleMessageLock");
        handleMessageLock.setAccessible(true);
        assertNotNull(handleMessageLock.get(transport));
    }

    // SRS_MQTTTRANSPORT_15_004: [If the MQTT connection is already open, the function shall do nothing.]
    @Test
    public void openDoesNothingIfAlreadyOpened() throws IOException
    {
        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.open(mockedCollection);

        final MqttIotHubConnection expectedConnection = mockedConnection;
        new Verifications()
        {
            {
                expectedConnection.open(mockedQueue);
                times = 1;
            }
        };
    }

    // Tests_SRS_MQTTTRANSPORT_15_005: [The function shall close the MQTT connection
    // with the IoT Hub given in the configuration.]
    @Test
    public void closeClosesMqttConnection() throws IOException
    {
        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.close();

        final MqttIotHubConnection expectedConnection = mockedConnection;
        new Verifications()
        {
            {
                expectedConnection.close();
            }
        };
    }

    // Tests_SRS_MQTTTRANSPORT_15_006: [If the MQTT connection is closed, the function shall do nothing.]
    @Test
    public void closeDoesNothingIfConnectionNeverOpened() throws IOException
    {
        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.close();

        final MqttIotHubConnection expectedConnection = mockedConnection;
        new Verifications()
        {
            {
                expectedConnection.close();
                times = 0;
            }
        };
    }

    // Tests_SRS_MQTTTRANSPORT_15_006: [If the MQTT connection is closed, the function shall do nothing.]
    @Test
    public void closeDoesNothingIfConnectionAlreadyClosed() throws IOException
    {
        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.close();
        transport.close();

        final MqttIotHubConnection expectedConnection = mockedConnection;
        new Verifications()
        {
            {
                expectedConnection.close();
                times = 1;
            }
        };
    }
    
    // Tests_SRS_MQTTTRANSPORT_15_005: [The function shall close the MQTT connection with the IoT Hub given in the configuration.]
    // Tests_SRS_MQTTTRANSPORT_99_020: [The method shall remove all the messages which are in progress or waiting to be sent and add them to the callback list.]
    // Tests_SRS_MQTTTRANSPORT_99_021: [The method shall invoke the callback list]
    
   @Test
    public void closeClosesMqttsConnectionAndRemovePendingMessages(@Mocked final Message mockMsg,
                                                             @Mocked final IotHubEventCallback mockCallback,
                                                             @Mocked final IotHubOutboundPacket mockedPacket) throws IOException, InterruptedException
    {
        final MqttTransport transport = new MqttTransport (mockedConfig);
        final MqttIotHubConnection expectedConnection = mockedConnection;
        
        new NonStrictExpectations()
        {
            {
                mockedPacket.getMessage();
                result = mockMsg;
                mockMsg.getBytes();
                result = "AnyData".getBytes();
            }
        };
        
        
        transport.open(mockedCollection);
        transport.addMessage(mockMsg, mockCallback, null);
        transport.close();


        Queue<IotHubOutboundPacket> actualWaitingMessages = Deencapsulation.getField(transport, "waitingList");
        
        assertEquals(actualWaitingMessages.size(), 0);
        
        
        new Verifications()
        {
            {
                mockCallback.execute((IotHubStatusCode) any, any);
                times = 1;
                expectedConnection.close();
                minTimes = 1;
            }
        };
        
    }
 
    // Tests_SRS_MQTTTRANSPORT_15_001: [The constructor shall initialize an empty transport queue
    // for adding messages to be sent as a batch.]
    // Tests_SRS_MQTTTRANSPORT_15_007: [The function shall add a packet containing the message,
    // callback, and callback context to the transport queue.]
    @Test
    public <T extends Queue> void addMessageAddsToTransportQueue(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback,
            @Mocked final IotHubOutboundPacket mockPacket) throws IOException
    {
        final Queue mockQueue = new MockUp<T>()
        {
        }.getMockInstance();
        final Map<String, Object> context = new HashMap<>();

        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.addMessage(mockMsg, mockCallback, context);

        new VerificationsInOrder()
        {
            {
                new IotHubOutboundPacket(mockMsg, mockCallback, context);
                mockQueue.add(mockPacket);
            }
        };
    }

    // Tests_SRS_MQTTTRANSPORT_15_008: [If the transport is closed, the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void addMessageFailsIfTransportNotOpened(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback)
            throws IOException
    {
        final Map<String, Object> context = new HashMap<>();

        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.addMessage(mockMsg, mockCallback, context);
    }

    // Tests_SRS_MQTTTRANSPORT_15_008: [If the transport is closed, the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void addMessageFailsIfTransportAlreadyClosed(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback)
            throws IOException
    {
        final Map<String, Object> context = new HashMap<>();

        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.close();
        transport.addMessage(mockMsg, mockCallback, context);
    }

    // Tests_SRS_MQTTTRANSPORT_21_022: [The function shall throws `UnsupportedOperationException`.]
    @Test (expected = UnsupportedOperationException.class)
    public <T extends Queue> void addMessageWithResponseNotSupportedThrows(
            @Mocked final Message mockMsg,
            @Mocked final IotHubResponseCallback mockCallback) throws IOException
    {
        // arrange
        final Queue mockQueue = new MockUp<T>()
        {
        }.getMockInstance();
        final Map<String, Object> context = new HashMap<>();

        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);

        // act
        transport.addMessage(mockMsg, mockCallback, context);
    }

    // Tests_SRS_MQTTTRANSPORT_15_009: [The function shall attempt to send every message
    // on its waiting list, one at a time.]
    @Test
    public void sendMessagesSendsAllMessages(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback,
            @Mocked final IotHubOutboundPacket mockPacket,
            @Mocked final MqttIotHubConnection mockConnection)
            throws IOException
    {
        final Map<String, Object> context = new HashMap<>();
        new NonStrictExpectations()
        {
            {
                new MqttIotHubConnection(mockedConfig);
                result = mockConnection;
                new IotHubOutboundPacket(mockMsg, mockCallback, context);
                result = mockPacket;
                mockPacket.getMessage();
                result = mockMsg;
            }
        };

        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.addMessage(mockMsg, mockCallback, context);
        transport.addMessage(mockMsg, mockCallback, context);
        transport.sendMessages();

        final MqttIotHubConnection expectedConnection = mockConnection;
        new Verifications()
        {
            {
                expectedConnection.sendEvent(mockMsg);
                times = 2;
            }
        };
    }

    // Tests_SRS_MQTTTRANSPORT_15_010: [For each message being sent, the function shall send the message
    // and add the IoT Hub status code along with the callback and context to the callback list.]
    @Test
    public <T extends Queue> void sendMessagesAddsToCallbackQueue(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback,
            @Mocked final IotHubOutboundPacket mockPacket,
            @Mocked final IotHubCallbackPacket mockCallbackPacket)
            throws IOException
    {
        final Queue mockQueue = new MockUp<T>()
        {

        }.getMockInstance();
        final Map<String, Object> context = new HashMap<>();
        new NonStrictExpectations()
        {
            {
                new MqttIotHubConnection(mockedConfig);
                result = mockedConnection;
                new IotHubOutboundPacket(mockMsg, mockCallback, context);
                result = mockPacket;
                mockPacket.getCallback();
                result = mockCallback;
                mockPacket.getContext();
                result = context;
                mockedConnection.sendEvent((Message) any);
                returns(IotHubStatusCode.OK_EMPTY, IotHubStatusCode.ERROR);
                new IotHubCallbackPacket(IotHubStatusCode.OK_EMPTY, mockCallback, context);
                result = mockCallbackPacket;
                new IotHubCallbackPacket(IotHubStatusCode.ERROR, mockCallback, context);
                result = mockCallbackPacket;
            }
        };

        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.addMessage(mockMsg, mockCallback, context);
        transport.addMessage(mockMsg, mockCallback, context);
        transport.sendMessages();

        new VerificationsInOrder()
        {
            {
                new IotHubCallbackPacket(IotHubStatusCode.OK_EMPTY, mockCallback, context);
                mockQueue.add(mockCallbackPacket);
                new IotHubCallbackPacket(IotHubStatusCode.ERROR, mockCallback, context);
                mockQueue.add(mockCallbackPacket);
            }
        };
    }

    //Tests_SRS_MQTTTRANSPORT_34_027: [If the packet to be sent contains a message that has expired, the message shall not be sent, but shall be added to the callback list with IotHubStatusCode MESSAGE_EXPIRED.]
    @Test
    public void sendMessagesWithExpiredMessageAddsMessageExpiredToCallbackQueue(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback,
            @Mocked final IotHubOutboundPacket mockPacket,
            @Mocked final IotHubCallbackPacket mockCallbackPacket)
            throws IOException
    {
        //arrange
        final Map<String, Object> context = new HashMap<>();
        new NonStrictExpectations()
        {
            {
                new MqttIotHubConnection(mockedConfig);
                result = mockedConnection;
                new IotHubOutboundPacket(mockMsg, mockCallback, context);
                result = mockPacket;
                mockedConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.X509_CERTIFICATE;
                mockPacket.getMessage().isExpired();
                result = true;
            }
        };

        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.addMessage(mockMsg, mockCallback, context);

        //act
        transport.sendMessages();

        //assert
        Queue<IotHubCallbackPacket> callbackList = Deencapsulation.getField(transport, "callbackList");
        assertEquals(1, callbackList.size());
        new Verifications()
        {
            {
                new IotHubCallbackPacket(IotHubStatusCode.MESSAGE_EXPIRED, (IotHubEventCallback) any, any);
                times = 1;
            }
        };
    }

    // Tests_SRS_MQTTTRANSPORT_15_011: [If the IoT Hub could not be reached,
    // the message shall be buffered to be sent again next time.]
    @Test
    public void sendMessagesBuffersFailedMessages(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback)
            throws IOException
    {
        final Map<String, Object> context = new HashMap<>();
        new NonStrictExpectations()
        {
            {
                mockedConnection.sendEvent((Message) any);
                result = new IllegalStateException(anyString);
                result = IotHubStatusCode.OK_EMPTY;
            }
        };

        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.addMessage(mockMsg, mockCallback, context);
        transport.sendMessages();
        transport.sendMessages();

        final MqttIotHubConnection expectedConnection = mockedConnection;
        new Verifications()
        {
            {
                expectedConnection.sendEvent(mockMsg);
                times = 2;
            }
        };
    }

    // Tests_SRS_MQTTTRANSPORT_15_011: [If the MQTT connection is closed,
    // the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void sendMessagesFailsIfTransportNeverOpened() throws IOException
    {
        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.sendMessages();
    }

    // Tests_SRS_MQTTTRANSPORT_15_011: [If the MQTT connection is closed,
    // the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void sendMessagesFailsIfTransportClosed() throws IOException
    {
        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.close();
        transport.sendMessages();
    }

    // Tests_SRS_MQTTTRANSPORT_15_013: [The function shall invoke all callbacks on the callback queue.]
    @Test
    public void invokeCallbacksInvokesAllCallbacks(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback,
            @Mocked final IotHubCallbackPacket mockCallbackPacket)
            throws IOException
    {
        final Map<String, Object> context = new HashMap<>();
        new NonStrictExpectations()
        {
            {
                new MqttIotHubConnection(mockedConfig);
                result = mockedConnection;
                mockCallbackPacket.getCallback();
                result = mockCallback;
                mockCallbackPacket.getContext();
                result = context;
            }
        };

        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.addMessage(mockMsg, mockCallback, context);
        transport.addMessage(mockMsg, mockCallback, context);
        transport.sendMessages();
        transport.invokeCallbacks();

        final IotHubEventCallback expectedCallback = mockCallback;
        new VerificationsInOrder()
        {
            {
                expectedCallback.execute((IotHubStatusCode) any, any);
                times = 2;
            }
        };
    }

    // Tests_SRS_MQTTTRANSPORT_15_015: [If an exception is thrown during the callback,
    // the function shall drop the callback from the queue.]
    @Test
    public void invokeCallbacksDropsFailedCallback()
            throws IOException
    {
        final Map<String, Object> context = new HashMap<>();
        new NonStrictExpectations()
        {
            {
                new MqttIotHubConnection(mockedConfig);
                result = mockedConnection;
                mockedCallbackPacket.getStatus();
                result = IotHubStatusCode.OK_EMPTY;
                mockedCallbackPacket.getCallback();
                result = mockedIotHubEventCallback;
                mockedCallbackPacket.getContext();
                result = context;
                mockedIotHubEventCallback.execute(IotHubStatusCode.OK_EMPTY, context);
                result = new IllegalStateException();
                result = null;
            }
        };

        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.addMessage(mockedMessage, mockedIotHubEventCallback, context);
        transport.sendMessages();
        try
        {
            transport.invokeCallbacks();
            throw new AssertionFailedError();
        }
        catch (IllegalStateException e)
        {
            transport.invokeCallbacks();
        }

        final IotHubEventCallback expectedCallback = mockedIotHubEventCallback;
        final Map<String, Object> expectedContext = context;
        new VerificationsInOrder()
        {
            {
                expectedCallback.execute(IotHubStatusCode.OK_EMPTY, expectedContext);
                times = 1;
            }
        };
    }

    // Tests_SRS_MqttTransport_11_019: [The function shall return true if the waiting list
    // and callback list are all empty, and false otherwise.]
    @Test
    public void isEmptyReturnsFalseIfWaitingListIsNotEmpty(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback) throws IOException
    {
        final Map<String, Object> context = new HashMap<>();

        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.addMessage(mockMsg, mockCallback, context);
        boolean testIsEmpty = transport.isEmpty();

        boolean expectedIsEmpty = false;
        assertThat(testIsEmpty, is(expectedIsEmpty));
    }

    // Tests_SRS_MqttTransport_11_019: [The function shall return true if the waiting list
    // and callback list are all empty, and false otherwise.]
    @Test
    public void isEmptyReturnsFalseIfCallbackListIsNotEmpty(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback)
            throws IOException
    {
        final Map<String, Object> context = new HashMap<>();

        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.addMessage(mockMsg, mockCallback, context);
        transport.addMessage(mockMsg, mockCallback, context);
        transport.addMessage(mockMsg, mockCallback, context);
        transport.sendMessages();
        boolean testIsEmpty = transport.isEmpty();

        final boolean expectedIsEmpty = false;
        assertThat(testIsEmpty, is(expectedIsEmpty));
    }

    // Tests_SRS_MqttTransport_11_019: [The function shall return true if the waiting list
    // and callback list are all empty, and false otherwise.]
    @Test
    public void isEmptyReturnsTrueIfEmpty(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback)
            throws IOException
    {
        final Map<String, Object> context = new HashMap<>();

        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.addMessage(mockMsg, mockCallback, context);
        transport.addMessage(mockMsg, mockCallback, context);
        transport.addMessage(mockMsg, mockCallback, context);
        transport.sendMessages();
        transport.invokeCallbacks();
        boolean testIsEmpty = transport.isEmpty();

        final boolean expectedIsEmpty = true;
        assertThat(testIsEmpty, is(expectedIsEmpty));
    }

    // Tests_SRS_MQTTTRANSPORT_15_016: [The function shall attempt to consume a message from the IoT Hub.]
    @Test
    public void handleMessageAttemptsToReceiveMessage(@Mocked final MessageCallback mockCallback) throws IOException
    {
        final Object context = new Object();
        new NonStrictExpectations()
        {
            {
                mockedConfig.getDeviceTelemetryMessageCallback();
                result = mockCallback;
                mockedConfig.getDeviceTelemetryMessageContext();
                result = context;
            }
        };

        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.handleMessage();

        final MqttIotHubConnection expectedConnection = mockedConnection;
        new Verifications()
        {
            {
                expectedConnection.receiveMessage();
            }
        };
    }

    // Tests_SRS_MQTTTRANSPORT_15_017: [If a message is found and a message callback is registered,
    // the function shall invoke the callback on the message.]
    @Test
    public void handleMessageInvokesCallbackIfMessageReceived(
            @Mocked final MessageCallback mockCallback,
            @Mocked final Message mockMsg) throws IOException
    {
        final Object context = new Object();
        new NonStrictExpectations()
        {
            {
                mockedConfig.getDeviceTelemetryMessageCallback();
                result = mockCallback;
                mockedConfig.getDeviceTelemetryMessageContext();
                result = context;
                mockedConnection.receiveMessage();
                result = mockMsg;
            }
        };

        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.handleMessage();

        final MessageCallback expectedCallback = mockCallback;
        final Message expectedMsg = mockMsg;
        final Object expectedContext = context;
        new Verifications()
        {
            {
                expectedCallback.execute(expectedMsg, expectedContext);
            }
        };
    }

    // Tests_SRS_MQTTTRANSPORT_15_018: [If the MQTT connection is closed,
    // the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void handleMessageFailsIfTransportNeverOpened() throws IOException
    {
        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.handleMessage();
    }

    // Tests_SRS_MQTTTRANSPORT_15_018: [If the MQTT connection is closed,
    // the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void handleMessageFailsIfTransportAlreadyClosed() throws IOException
    {
        MqttTransport transport = new MqttTransport(mockedConfig);
        transport.open(mockedCollection);
        transport.close();
        transport.handleMessage();
    }

    //Tests_SRS_MQTTTRANSPORT_34_025: [This function shall register the provided connection state callback and context with the saved mqtt iot hub connection.]
    @Test
    public void registerConnectionStateCallbackRegistersWithMqttConnection()
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                new MqttIotHubConnection(mockedConfig);
                result = mockedConnection;
            }
        };
        MqttTransport transport = new MqttTransport(mockedConfig);

        //act
        transport.registerConnectionStateCallback(mockedConnectionStateCallback, mockedConnectionStateCallbackContext);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedConnection, "registerConnectionStateCallback", mockedConnectionStateCallback, mockedConnectionStateCallbackContext);
                times = 1;
            }
        };
    }

    //Tests_SRS_MQTTTRANSPORT_34_029: [This function shall block and wait on the read lock before reading from the waiting list.]
    //Tests_SRS_MQTTTRANSPORT_34_031: [This function shall block and wait on the read lock before reading from the in progress list.]
    @Test
    public void onlyOneThreadReadsFromWaitingListAtATimeBetweenSendMessagesAndClose() throws IOException, InterruptedException
    {
        //arrange
        final MqttTransport mqttTransport = new MqttTransport(mockedConfig);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        final int messageCount = 1000;
        mqttTransport.open(mockedCollection);

        for (int i = 0; i < messageCount; i++)
        {
            mqttTransport.addMessage(mockedMessage, mockedIotHubEventCallback, null);
        }

        //act
        executorService.submit(new SendMessagesRunnable(mqttTransport));
        executorService.submit(new CloseRunnable(mqttTransport));

        //wait for all threads to finish
        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);

        //assert
        new VerificationsInOrder()
        {
            {
                //All messages should have been sent before the connection was closed
                mockedConnection.sendEvent(mockedMessage);
                times = messageCount;

                mockedConnection.close();
                times = 1;
            }
        };
    }

    //Tests_SRS_MQTTTRANSPORT_34_029: [This function shall block and wait on the read lock before reading from the waiting list.]
    @Test
    public void onlyOneThreadReadsFromWaitingListAtATimeBetweenSendMessagesAndIsEmpty() throws IOException, InterruptedException
    {
        //arrange
        final MqttTransport mqttTransport = new MqttTransport(mockedConfig);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        final int messageCount = 1000;
        mqttTransport.open(mockedCollection);

        for (int i = 0; i < messageCount; i++)
        {
            mqttTransport.addMessage(mockedMessage, mockedIotHubEventCallback, null);
        }

        //act
        executorService.submit(new SendMessagesRunnable(mqttTransport));
        executorService.submit(new IsEmptyRunnable(mqttTransport));

        //wait for all threads to finish
        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);

        //assert
        new Verifications()
        {
            {
                //All messages should have been sent before isEmpty ran. The isEmpty runnable asserts that waitinglist is empty
                mockedConnection.sendEvent(mockedMessage);
                times = messageCount;
            }
        };
    }

    //Tests_SRS_MQTTTRANSPORT_34_031: [This function shall block and wait on the read lock before reading from the in progress list or the callback list.]
    @Test
    public void onlyOneThreadReadsFromWaitingListAtATimeBetweenCloseAndIsEmpty() throws IOException, InterruptedException
    {
        //arrange
        final MqttTransport mqttTransport = new MqttTransport(mockedConfig);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        final int messageCount = 1000;
        mqttTransport.open(mockedCollection);

        for (int i = 0; i < messageCount; i++)
        {
            mqttTransport.addMessage(mockedMessage, mockedIotHubEventCallback, null);
        }

        //act
        executorService.submit(new CloseRunnable(mqttTransport));
        executorService.submit(new IsEmptyRunnable(mqttTransport));

        //wait for all threads to finish
        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);

        //assert
        new Verifications()
        {
            {
                //IsEmptyRunnable also asserts that the waitinglist was empty
                mockedConnection.close();
                times = 1;
            }
        };
    }

    //Tests_SRS_MQTTTRANSPORT_34_030: [This function shall block and wait on the read lock before reading from the callback list.]
    @Test
    public void onlyOneThreadReadsFromWaitingListAtATimeBetweenSendMessagesAndInvokeCallbacks() throws IOException, InterruptedException
    {
        //arrange
        final MqttTransport mqttTransport = new MqttTransport(mockedConfig);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        final int messageCount = 1000;
        mqttTransport.open(mockedCollection);

        for (int i = 0; i < messageCount; i++)
        {
            mqttTransport.addMessage(mockedMessage, mockedIotHubEventCallback, null);
        }

        new NonStrictExpectations()
        {
            {
                new IotHubCallbackPacket((IotHubStatusCode) any, mockedIotHubEventCallback, null);
                result = mockedCallbackPacket;

                mockedCallbackPacket.getCallback();
                result = mockedIotHubEventCallback;
            }
        };

        //act
        executorService.submit(new SendMessagesRunnable(mqttTransport));
        executorService.submit(new InvokeCallbacksRunnable(mqttTransport));

        //wait for all threads to finish
        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);

        //assert
        new VerificationsInOrder()
        {
            {
                //All added messages are sent first, and then all of their callbacks executed
                mockedConnection.sendEvent(mockedMessage);
                times = messageCount;

                mockedIotHubEventCallback.execute((IotHubStatusCode) any, any);
                times = messageCount;
            }
        };
    }

    //Tests_SRS_MQTTTRANSPORT_34_028: [This function shall not block and wait on the read lock.]
    @Test
    public void addMessagesDoesNotBlockAndWaitForReadOperationThreadsToFinish() throws IOException, InterruptedException
    {
        //arrange
        final MqttTransport mqttTransport = new MqttTransport(mockedConfig);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        final int messageCount = 10000;
        mqttTransport.open(mockedCollection);

        for (int i = 0; i < messageCount; i++)
        {
            mqttTransport.addMessage(mockedMessage, mockedIotHubEventCallback, null);
        }

        //start the thread for sending all of these messages
        executorService.submit(new SendMessagesRunnable(mqttTransport));

        //act
        executorService.submit(new AddMessageRunnable(mqttTransport));

        //wait for all threads to finish
        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);

        //assert
        new Verifications()
        {
            {
                //Message added is sent successfully when the other thread reads it from the waiting list
                mockedConnection.sendEvent((Message) any);
                times = messageCount + 1;
            }
        };
    }
}