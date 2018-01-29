// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.transport.IotHubCallbackPacket;
import com.microsoft.azure.sdk.iot.device.transport.IotHubOutboundPacket;
import com.microsoft.azure.sdk.iot.device.transport.State;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import mockit.*;
import org.apache.qpid.proton.message.impl.MessageImpl;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.*;

/**
 * Unit tests for AmqpsTransport.
 * Coverage :
 * 100% method,
 * 100% line
 */
public class AmqpsTransportTest
{
    @Mocked
    DeviceClientConfig mockConfig;

    @Mocked
    AmqpsIotHubConnection mockConnection;

    @Mocked
    AmqpsMessage mockAmqpsMessage;

    @Mocked
    MessageImpl mockProtonMessage;

    @Mocked
    IotHubCallbackPacket mockIotHubCallbackPacket;

    @Mocked
    IotHubEventCallback mockIotHubEventCallback;

    @Mocked
    MessageCallback mockMessageCallback;

    @Mocked
    IotHubConnectionStateCallback mockConnectionStateCallback;

    @Mocked
    Map<Integer, IotHubOutboundPacket> mockMapIntegerIotHubOutboundPacket;

    @Mocked
    Map.Entry<Integer, IotHubOutboundPacket> mockMapIntegerIotHubOutboundPacketEntry;

    @Mocked
    AmqpsDeviceOperations mockAmqpsDeviceOperations;

    @Mocked
    AmqpsDeviceMethods mockAmqpsDeviceMethods;

    @Mocked
    AmqpsDeviceTwin mockAmqpsDeviceTwin;

    @Mocked
    AmqpsDeviceTelemetry mockAmqpsDeviceTelemetry;

    @Mocked
    AmqpsConvertFromProtonReturnValue mockAmqpsConvertFromProtonReturnValue;

    @Mocked
    AmqpsConvertToProtonReturnValue mockAmqpsConvertToProtonReturnValue;

    @Mocked
    AmqpsSendReturnValue mockAmqpsSendReturnValue;

    @Mocked
    DeviceClientConfig mockDeviceClientConfig;

    @Mocked
    DeviceClient mockDeviceClient;
    @Mocked
    IotHubSasTokenAuthenticationProvider mockSasTokenAuthentication;

    // Tests_SRS_AMQPSTRANSPORT_15_001: [The constructor shall save the input parameters into instance variables.]
    @Test
    public void constructorSavesInputParameters()
    {
        DeviceClientConfig expectedClientConfig = mockConfig;

        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(expectedClientConfig);

        DeviceClientConfig actualClientConfig = Deencapsulation.getField(transport, "deviceClientConfig");

        assertEquals(expectedClientConfig, actualClientConfig);
    }

    // Tests_SRS_AMQPSTRANSPORT_15_002: [The constructor shall set the transport state to CLOSED.]
    @Test
    public void constructorSetsStateToClosed()
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);

        State state = Deencapsulation.getField(transport, "state");

        assertEquals(State.CLOSED, state);
    }

    // Tests_SRS_AMQPSTRANSPORT_15_003: [If an AMQPS connection is already open, the function shall do nothing.]
    @Test
    public void openDoesNothingIfAlreadyOpened() throws IOException, InterruptedException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();
        transport.open();

        final AmqpsIotHubConnection expectedConnection = mockConnection;
        new Verifications()
        {
            {
                expectedConnection.open();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_15_004: [The function shall open an AMQPS connection with the IoT Hub given in the configuration.]
    @Test
    public void openOpensAmqpsConnection() throws IOException, InterruptedException
    {
        new NonStrictExpectations()
        {
            {
                new AmqpsIotHubConnection(mockConfig);
                result = mockConnection;
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();

        final AmqpsIotHubConnection expectedConnection = mockConnection;
        new Verifications()
        {
            {
                expectedConnection.open();
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_12_004: [The function shall throw IOException if connection open throws.]
    @Test (expected = IOException.class)
    public void openThrowsIfConnectionOpenThrows() throws IOException, InterruptedException
    {
        new NonStrictExpectations()
        {
            {
                new AmqpsIotHubConnection(mockConfig);
                result = mockConnection;
                mockConnection.open();
                result = new IOException();
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();
    }

    // Tests_SRS_AMQPSTRANSPORT_15_005: [The function shall add the transport to the list of listeners subscribed to the connection events.]
    @Test
    public void openAddsTransportToConnectionListenersList() throws IOException
    {
        new NonStrictExpectations()
        {
            {
                new AmqpsIotHubConnection(mockConfig);
                result = mockConnection;
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        final AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();

        new Verifications()
        {
            {
                mockConnection.addListener(transport);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_15_006: [If the connection was opened successfully, the transport state shall be set to OPEN.]
    @Test
    public void openSetsStateToOpenIfSuccessful() throws IOException
    {
        new NonStrictExpectations()
        {
            {
                new AmqpsIotHubConnection(mockConfig);
                result = mockConnection;
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        final AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();

        State state = Deencapsulation.getField(transport, "state");
        assertEquals(State.OPEN, state);
    }

    // Tests_SRS_AMQPSTRANSPORT_12_009: [The function shall throw IllegalArgumentException if the deviceClientList is null or empty.]
    @Test (expected = IllegalArgumentException.class)
    public void multiplexOpenThrowsIfDeviceClientListNull() throws IOException
    {
        // arrange
        AmqpsTransport transport = new AmqpsTransport(mockConfig);

        // act
        transport.multiplexOpen(null);
    }

    // Tests_SRS_AMQPSTRANSPORT_12_009: [The function shall throw IllegalArgumentException if the deviceClientList is null or empty.]
    @Test (expected = IllegalArgumentException.class)
    public void multiplexOpenThrowsIfDeviceClientListEmpty() throws IOException
    {
        // arrange
        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        ArrayList<DeviceClient> deviceClientList = new ArrayList<>();

        // act
        transport.multiplexOpen(deviceClientList);
    }

    // Tests_SRS_AMQPSTRANSPORT_12_010: [The function shall throw IllegalStateException if the transport state is already OPEN.]
    @Test (expected = IllegalStateException.class)
    public void multiplexOpenThrowsIftransportOpen() throws IOException
    {
        // arrange
        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        ArrayList<DeviceClient> deviceClientList = new ArrayList<>();
        deviceClientList.add(mockDeviceClient);
        Deencapsulation.setField(transport, "state", State.OPEN);

        // act
        transport.multiplexOpen(deviceClientList);
    }

    // Tests_SRS_AMQPSTRANSPORT_12_017: [The function shall throw IOException if the underlying connection throws.]
    @Test (expected = IOException.class)
    public void multiplexOpenThrowsIfConnectionThrows() throws IOException
    {
        // arrange
        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        ArrayList<DeviceClient> deviceClientList = new ArrayList<>();
        deviceClientList.add(mockDeviceClient);
        Deencapsulation.setField(transport, "connection", mockConnection);

        new NonStrictExpectations()
        {
            {
                mockConnection.open();
                result = new IOException();
            }
        };

        // act
        transport.multiplexOpen(deviceClientList);
    }

    // Tests_SRS_AMQPSTRANSPORT_12_011: [The function shall open an AMQPS connection with the IoT Hub given in the configuration. ]
    // Tests_SRS_AMQPSTRANSPORT_12_012: [The function shall add the transport to the list of listeners subscribed to the connection events.]
    // Tests_SRS_AMQPSTRANSPORT_12_013: [The function shall add the device clients to the underlying connection.]
    // Tests_SRS_AMQPSTRANSPORT_12_014: [The function shall open the connection.]
    // Tests_SRS_AMQPSTRANSPORT_12_015: [The function shall call the connection to authenticate.]
    // Tests_SRS_AMQPSTRANSPORT_12_016: [The function shall call the connection to open device client links.]
    // Tests_SRS_AMQPSTRANSPORT_12_018: [The function shal set the transport state to OPEN.]
    @Test
    public void multiplexOpenSuccess() throws IOException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                new AmqpsIotHubConnection(mockConfig);
                result = mockConnection;
                mockDeviceClient.getConfig();
                result = mockDeviceClientConfig;
            }
        };

        final AmqpsTransport transport = new AmqpsTransport(mockConfig);
        final ArrayList<DeviceClient> deviceClientList = new ArrayList<>();
        deviceClientList.add(mockDeviceClient);
        deviceClientList.add(mockDeviceClient);

        // act
        transport.multiplexOpen(deviceClientList);

        // assert
        State actualState = Deencapsulation.getField(transport, "state");
        assertEquals(State.OPEN, actualState);

        new Verifications()
        {
            {
                mockConnection.open();
                mockConnection.addListener(transport);
                times = 1;
                mockConnection.addDeviceOperationSession(deviceClientList.get(1).getConfig());
                times = 1;
                mockConnection.open();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_15_007: [If the AMQPS connection is closed, the function shall do nothing.]
    @Test
    public void closeDoesNothingIfConnectionAlreadyClosed() throws IOException, InterruptedException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };
        AmqpsTransport transport = new AmqpsTransport(mockConfig);

        transport.open();
        transport.close();
        transport.close();

        final AmqpsIotHubConnection expectedConnection = mockConnection;
        new Verifications()
        {
            {
                expectedConnection.close();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_15_008: [The function shall closeNow an AMQPS connection with the IoT Hub given in the configuration.]
    @Test
    public void closeClosesAmqpsConnection() throws IOException, InterruptedException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };
        AmqpsTransport transport = new AmqpsTransport(mockConfig);

        transport.open();
        transport.close();

        final AmqpsIotHubConnection expectedConnection = mockConnection;
        new Verifications()
        {
            {

                expectedConnection.close();
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_15_008: [The function shall closeNow an AMQPS connection with the IoT Hub given in the configuration.]
    // Tests_SRS_AMQPSTRANSPORT_99_036: [The method shall remove all the messages which are in progress or waiting to be sent and add them to the callback list.]
    // Tests_SRS_AMQPSTRANSPORT_99_037: [The method shall invoke all the callbacks.]
    // Tests_SRS_AMQPSTRANSPORT_12_005: [The function shall add a new outbound packet to the callback list.]
   @Test
    public void closeClosesAmqpsConnectionAndRemovePendingMessages(@Mocked final Message mockMsg,
                                                             @Mocked final IotHubEventCallback mockCallback,
                                                             @Mocked final IotHubOutboundPacket mockedPacket) throws IOException, InterruptedException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };
        final AmqpsTransport transport = new AmqpsTransport(mockConfig);
        final AmqpsIotHubConnection expectedConnection = mockConnection;

        new NonStrictExpectations()
        {
            {
                mockedPacket.getMessage();
                result = mockMsg;
                mockMsg.getBytes();
                result = "AnyData".getBytes();
                mockMapIntegerIotHubOutboundPacket.size();
                result = 1;
                mockMapIntegerIotHubOutboundPacket.entrySet();
                result = mockMapIntegerIotHubOutboundPacketEntry;
            }
        };
        Deencapsulation.setField(transport, "inProgressMessages", mockMapIntegerIotHubOutboundPacket);

        transport.open();
        transport.addMessage(mockMsg, mockCallback, null);
        transport.close();


        Queue<IotHubOutboundPacket> actualWaitingMessages = Deencapsulation.getField(transport, "waitingMessages");
        Map<Integer, IotHubOutboundPacket> actualInProgressMessages = Deencapsulation.getField(transport, "inProgressMessages");

        assertEquals(actualWaitingMessages.size(), 0);
        assertEquals(actualInProgressMessages.size(), 1);

        new Verifications()
        {
            {
                mockedPacket.getMessage();
                times = 1;
                mockCallback.execute((IotHubStatusCode) any, any);
                times = 2;
                expectedConnection.close();
                minTimes = 1;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_15_009: [The function shall set the transport state to CLOSED.]
    @Test
    public void closeSetsStateToClosed() throws IOException
    {

        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };
        AmqpsTransport transport = new AmqpsTransport(mockConfig);

        transport.open();
        transport.close();

        State actualState = Deencapsulation.getField(transport, "state");
        Assert.assertEquals(State.CLOSED, actualState);
    }

    // Tests_SRS_AMQPSTRANSPORT_15_010: [If the AMQPS session is closed, the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void addMessageFailsIfTransportNotOpened(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback)
            throws IOException
    {
        final Map<String, Object> context = new HashMap<>();


        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };
        AmqpsTransport transport = new AmqpsTransport(mockConfig);

        transport.addMessage(mockMsg, mockCallback, context);
    }

    // Tests_SRS_AMQPSTRANSPORT_15_010: [If the AMQPS session is closed, the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void addMessageFailsIfTransportAlreadyClosed(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback)
            throws IOException
    {
        final Map<String, Object> context = new HashMap<>();


        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };
        AmqpsTransport transport = new AmqpsTransport(mockConfig);

        transport.open();
        transport.close();
        transport.addMessage(mockMsg, mockCallback, context);
    }

    // Tests_SRS_AMQPSTRANSPORT_15_011: [The function shall add a packet containing the message, callback,
    // and callback context to the queue of messages waiting to be sent.]
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


        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };
        AmqpsTransport transport = new AmqpsTransport(mockConfig);

        transport.open();
        transport.addMessage(mockMsg, mockCallback, context);

        new VerificationsInOrder()
        {
            {
                new IotHubOutboundPacket(mockMsg, mockCallback, context);
                mockQueue.add(mockPacket);
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_21_040: [The function shall throws `UnsupportedOperationException`.]
    @Test(expected = UnsupportedOperationException.class)
    public void addMessageWithResponseNotSupportedThrows(
            @Mocked final Message mockMsg,
            @Mocked final IotHubResponseCallback mockCallback)
            throws IOException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };
        final Map<String, Object> context = new HashMap<>();
        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();

        // act
        transport.addMessage(mockMsg, mockCallback, context);
    }

    // Tests_SRS_AMQPSTRANSPORT_15_012: [If the AMQPS session is closed, the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void sendMessagesFailsIfTransportNeverOpened() throws IOException
    {

        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };
        AmqpsTransport transport = new AmqpsTransport(mockConfig);

        transport.sendMessages();
    }

    // Tests_SRS_AMQPSTRANSPORT_15_012: [If the AMQPS session is closed, the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void sendMessagesFailsIfTransportAlreadyClosed() throws IOException
    {

        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };
        AmqpsTransport transport = new AmqpsTransport(mockConfig);

        transport.open();
        transport.close();
        transport.sendMessages();
    }

    // Tests_SRS_AMQPSTRANSPORT_12_003: [The function throws IllegalStateException if none of the device operation object could handle the conversion.]
    @Test(expected = IllegalStateException.class)
    public void sendMessagesThrowsIfNoDeviceOperationEnabled(
            @Mocked final Message mockMessage,
            @Mocked final IotHubOutboundPacket mockPacket,
            @Mocked final IotHubEventCallback mockCallback)
            throws IOException
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();

        final AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();
        transport.addMessage(mockMessage, mockCallback, context);
        Deencapsulation.setField(transport, "connection", mockConnection);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockConnection, "convertToProton", mockMessage);
                result = null;
            }
        };

        // act
        transport.sendMessages();
    }

    // Tests_SRS_AMQPSTRANSPORT_15_013: [If there are no messages in the waiting list, the function shall return.]
    @Test
    public void sendMessagesReturnsIfNoMessagesAreWaiting(@Mocked final IotHubOutboundPacket mockPacket)
            throws IOException
    {
        new NonStrictExpectations()
        {
            {
                new AmqpsIotHubConnection(mockConfig);
                result = mockConnection;
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();
        transport.sendMessages();

        assertTrue(transport.isEmpty());
        new VerificationsInOrder()
        {
            {
                mockPacket.getMessage();
                times = 0;
            }
        };
    }


    // Tests_SRS_AMQPSTRANSPORT_15_014: [The function shall attempt to send every message on its waiting list, one at a time.]
    // Tests_SRS_AMQPSTRANSPORT_15_036: [The function shall create a new Proton message from the IoTHub message.]
    // Tests_SRS_AMQPSTRANSPORT_15_037: [The function shall attempt to send the Proton message to IoTHub using the underlying AMQPS connection.]
    @Test
    public void sendMessagesSendsAllMessages(
            @Mocked final Message mockMessage,
            @Mocked final IotHubEventCallback mockCallback,
            @Mocked final IotHubOutboundPacket mockPacket)
            throws IOException
    {
        final Map<String, Object> context = new HashMap<>();
        final byte[] messageBytes = new byte[] {1, 2};
        new NonStrictExpectations()
        {
            {
                new AmqpsIotHubConnection(mockConfig);
                result = mockConnection;
                new IotHubOutboundPacket(mockMessage, mockCallback, context);
                result = mockPacket;
                mockPacket.getMessage();
                result = mockMessage;
                mockMessage.getBytes();
                result = messageBytes;
                mockMessage.getMessageType();
                result = MessageType.DEVICE_TELEMETRY;
                mockConfig.getDeviceId();
                result = "deviceId";
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "convertToProton", mockMessage);
                result = mockAmqpsConvertToProtonReturnValue;
                Deencapsulation.invoke(mockAmqpsConvertToProtonReturnValue, "getMessageImpl");
                result = mockAmqpsMessage;
                Deencapsulation.invoke(mockAmqpsConvertToProtonReturnValue, "getMessageType");
                result = MessageType.DEVICE_TELEMETRY;
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();
        transport.addMessage(mockMessage, mockCallback, context);
        transport.addMessage(mockMessage, mockCallback, context);
        transport.sendMessages();

        new Verifications()
        {
            {
                new IotHubOutboundPacket(mockMessage, mockCallback, context);
                times = 2;
                mockPacket.getMessage();
                times = 2;
                mockConnection.sendMessage((org.apache.qpid.proton.message.Message) any, MessageType.DEVICE_TELEMETRY, (IotHubConnectionString) any);
                times = 2;
            }
        };
    }

    //Tests_SRS_AMQPSTRANSPORT_34_041: [If the config is using sas token authentication and its sas token has expired and cannot be renewed, the message shall not be sent, an UNAUTHORIZED message callback shall be added to the callback queue and SAS_TOKEN_EXPIRED state callback shall be fired.]
    //Tests_SRS_AMQPSTRANSPORT_34_043: [If the config is using sas token authentication and its sas token has expired and cannot be renewed, the message shall not be put back into the waiting messages queue to be re-sent.]
    @Test
    public void sendMessagesWithExpiredSasTokenSendsCallbacks(
            @Mocked final Message mockMessage,
            @Mocked final IotHubEventCallback mockCallback,
            @Mocked final IotHubOutboundPacket mockPacket)
            throws IOException
    {
        //arrange
        final Map<String, Object> context = new HashMap<>();
        final byte[] messageBytes = new byte[] {1, 2};
        new NonStrictExpectations()
        {
            {
                new AmqpsIotHubConnection(mockConfig);
                result = mockConnection;
                new IotHubOutboundPacket(mockMessage, mockCallback, context);
                result = mockPacket;
                mockPacket.getMessage();
                result = mockMessage;
                mockMessage.getBytes();
                result = messageBytes;
                mockMessage.getMessageType();
                result = MessageType.DEVICE_TELEMETRY;
                mockConfig.getDeviceId();
                result = "deviceId";
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "convertToProton", mockMessage);
                result = mockAmqpsConvertToProtonReturnValue;
                Deencapsulation.invoke(mockAmqpsConvertToProtonReturnValue, "getMessageImpl");
                result = mockAmqpsMessage;
                Deencapsulation.invoke(mockAmqpsConvertToProtonReturnValue, "getMessageType");
                result = MessageType.DEVICE_TELEMETRY;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getSasTokenAuthentication();
                result = mockSasTokenAuthentication;
                mockSasTokenAuthentication.isRenewalNecessary();
                result = true;
                new IotHubCallbackPacket(IotHubStatusCode.UNAUTHORIZED, (IotHubEventCallback) any, any);
                result = mockIotHubCallbackPacket;
                mockIotHubCallbackPacket.getStatus();
                result = IotHubStatusCode.UNAUTHORIZED;
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);

        Deencapsulation.setField(transport, "stateCallback", mockConnectionStateCallback);
        transport.open();
        transport.addMessage(mockMessage, mockCallback, context);

        //act
        transport.sendMessages();

        //assert
        Queue<IotHubOutboundPacket> waitingMessagesList = Deencapsulation.getField(transport, "waitingMessages");
        assertTrue(waitingMessagesList.isEmpty());

        Queue<IotHubCallbackPacket> callbackList = Deencapsulation.getField(transport, "callbackList");
        assertEquals(1, callbackList.size());
        assertEquals(mockIotHubCallbackPacket.getStatus(), callbackList.remove().getStatus());

        new Verifications()
        {
            {
                mockConnection.sendMessage((org.apache.qpid.proton.message.Message) any, MessageType.DEVICE_TELEMETRY, (IotHubConnectionString) any);
                times = 0;
                mockConnectionStateCallback.execute(IotHubConnectionState.SAS_TOKEN_EXPIRED, any);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_15_015: [The function shall skip messages with null or empty body.]
    @Test
    public void sendMessagesSkipsMessagesWithNullBody(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback,
            @Mocked final IotHubOutboundPacket mockPacket)
            throws IOException
    {
        final Map<String, Object> context = new HashMap<>();
        new NonStrictExpectations()
        {
            {
                new AmqpsIotHubConnection(mockConfig);
                result = mockConnection;
                new IotHubOutboundPacket(mockMsg, mockCallback, context);
                result = mockPacket;
                mockPacket.getMessage();
                result = null;
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();
        transport.addMessage(mockMsg, mockCallback, context);
        transport.addMessage(mockMsg, mockCallback, context);
        transport.sendMessages();

        new Verifications()
        {
            {
                mockConnection.sendMessage((org.apache.qpid.proton.message.Message) any, MessageType.DEVICE_TELEMETRY, (IotHubConnectionString) any);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_15_015: [The function shall skip messages with null or empty body.]
    @Test
    public void sendMessagesSkipsMessagesWithEmptyBody(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback,
            @Mocked final IotHubOutboundPacket mockPacket)
            throws IOException
    {
        final Map<String, Object> context = new HashMap<>();
        new NonStrictExpectations()
        {
            {
                new AmqpsIotHubConnection(mockConfig);
                result = mockConnection;
                new IotHubOutboundPacket(mockMsg, mockCallback, context);
                result = mockPacket;
                mockPacket.getMessage();
                result = mockMsg;
                mockMsg.getBytes();
                result = new byte[0];
                mockMsg.getMessageType();
                result = MessageType.DEVICE_TELEMETRY;
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();
        transport.addMessage(mockMsg, mockCallback, context);
        transport.addMessage(mockMsg, mockCallback, context);
        transport.sendMessages();

        new Verifications()
        {
            {
                mockConnection.sendMessage((org.apache.qpid.proton.message.Message) any, MessageType.DEVICE_TELEMETRY, (IotHubConnectionString) any);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_15_016: [If the sent message hash is valid, it shall be added to the in progress map.]
    @Test
    public void sendMessagesAddsSentMessagesToInProgressMap(
            @Mocked final Message mockMessage,
            @Mocked final IotHubEventCallback mockCallback,
            @Mocked final IotHubOutboundPacket mockPacket)
            throws IOException
    {
        final Map<String, Object> context = new HashMap<>();
        final byte[] messageBytes = new byte[] {1, 2};
        new NonStrictExpectations()
        {
            {
                new AmqpsIotHubConnection(mockConfig);
                result = mockConnection;
                new IotHubOutboundPacket(mockMessage, mockCallback, context);
                result = mockPacket;
                mockPacket.getMessage();
                result = mockMessage;
                mockMessage.getBytes();
                result = messageBytes;
                mockMessage.getMessageType();
                result = MessageType.DEVICE_TELEMETRY;
                mockConnection.sendMessage((org.apache.qpid.proton.message.Message) any, MessageType.DEVICE_TELEMETRY, (IotHubConnectionString) any);
                returns (1, 2);
                mockConfig.getDeviceId();
                result = "deviceId";
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "convertToProton", mockMessage);
                result = mockAmqpsConvertToProtonReturnValue;
                Deencapsulation.invoke(mockAmqpsConvertToProtonReturnValue, "getMessageImpl");
                result = mockAmqpsMessage;
                Deencapsulation.invoke(mockAmqpsConvertToProtonReturnValue, "getMessageType");
                result = MessageType.DEVICE_TELEMETRY;
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();
        transport.addMessage(mockMessage, mockCallback, context);
        transport.addMessage(mockMessage, mockCallback, context);
        transport.sendMessages();

        Map<Integer, IotHubOutboundPacket> inProgressMessages = Deencapsulation.getField(transport, "inProgressMessages");
        Assert.assertEquals(2, inProgressMessages.size());

        new Verifications()
        {
            {
                new IotHubOutboundPacket(mockMessage, mockCallback, context);
                times = 2;
                mockPacket.getMessage();
                times = 2;
                mockConnection.sendMessage((org.apache.qpid.proton.message.Message) any, MessageType.DEVICE_TELEMETRY, (IotHubConnectionString) any);
                times = 2;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_15_017: [If the sent message hash is not valid, it shall be buffered to be sent in a subsequent attempt.]
    @Test
    public void sendMessagesAddsNotSentMessagesToInProgressMap(
            @Mocked final Message mockMessage,
            @Mocked final IotHubEventCallback mockCallback,
            @Mocked final IotHubOutboundPacket mockPacket)
            throws IOException
    {
        final Map<String, Object> context = new HashMap<>();
        final byte[] messageBytes = new byte[] {1, 2};
        new NonStrictExpectations()
        {
            {

                new AmqpsIotHubConnection(mockConfig);

                result = mockConnection;
                new IotHubOutboundPacket(mockMessage, mockCallback, context);
                result = mockPacket;
                mockPacket.getMessage();
                result = mockMessage;
                mockMessage.getBytes();
                result = messageBytes;
                mockMessage.getMessageType();
                result = MessageType.DEVICE_TELEMETRY;
                mockConnection.sendMessage((org.apache.qpid.proton.message.Message) any, MessageType.DEVICE_TELEMETRY, (IotHubConnectionString) any);
                returns (1, -1);
                mockConfig.getDeviceId();
                result = "deviceId";
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "convertToProton", mockMessage);
                result = mockAmqpsConvertToProtonReturnValue;
                Deencapsulation.invoke(mockAmqpsConvertToProtonReturnValue, "getMessageImpl");
                result = mockAmqpsMessage;
                Deencapsulation.invoke(mockAmqpsConvertToProtonReturnValue, "getMessageType");
                result = MessageType.DEVICE_TELEMETRY;
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();
        transport.addMessage(mockMessage, mockCallback, context);
        transport.addMessage(mockMessage, mockCallback, context);
        transport.sendMessages();

        Map<Integer, IotHubOutboundPacket> inProgressMessages = Deencapsulation.getField(transport, "inProgressMessages");
        Assert.assertEquals(1, inProgressMessages.size());

        Queue<IotHubOutboundPacket> waitingMessages = Deencapsulation.getField(transport, "waitingMessages");
        Assert.assertEquals(1, waitingMessages.size());

        new Verifications()
        {
            {
                new IotHubOutboundPacket(mockMessage, mockCallback, context);
                times = 2;
                mockPacket.getMessage();
                times = 2;
                mockConnection.sendMessage((org.apache.qpid.proton.message.Message) any, MessageType.DEVICE_TELEMETRY, (IotHubConnectionString) any);
                times = 2;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_15_039: [If the message is expired, the function shall create a callback
    // with the MESSAGE_EXPIRED status and add it to the callback list.]
    @Test
    public void sendMessagesAddsExpiredMessagesToCallbackListWithCorrectCode(
            @Mocked final Message mockMessage,
            @Mocked final IotHubEventCallback mockCallback,
            @Mocked final IotHubOutboundPacket mockPacket)
            throws IOException
    {
        final Map<String, Object> context = new HashMap<>();
        final byte[] messageBytes = new byte[] {1, 2};
        new NonStrictExpectations()
        {
            {

                new AmqpsIotHubConnection(mockConfig);

                result = mockConnection;
                new IotHubOutboundPacket(mockMessage, mockCallback, context);
                result = mockPacket;
                mockPacket.getMessage();
                result = mockMessage;
                mockMessage.getBytes();
                result = messageBytes;
                mockMessage.isExpired();
                returns (true, false);
                mockMessage.getMessageType();
                result = MessageType.DEVICE_TELEMETRY;
                mockConnection.sendMessage((org.apache.qpid.proton.message.Message) any, MessageType.DEVICE_TELEMETRY, (IotHubConnectionString) any);
                result = 1;
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();
        transport.addMessage(mockMessage, mockCallback, context);
        transport.addMessage(mockMessage, mockCallback, context);
        transport.sendMessages();

        Map<Integer, IotHubOutboundPacket> inProgressMessages = Deencapsulation.getField(transport, "inProgressMessages");
        Assert.assertEquals(1, inProgressMessages.size());

        Queue<IotHubOutboundPacket> waitingMessages = Deencapsulation.getField(transport, "waitingMessages");
        Assert.assertEquals(0, waitingMessages.size());

        Queue<IotHubCallbackPacket> callbackList = Deencapsulation.getField(transport, "callbackList");
        Assert.assertEquals(1, callbackList.size());

        new Verifications()
        {
            {
                new IotHubOutboundPacket(mockMessage, mockCallback, context);
                times = 2;
                mockPacket.getMessage();
                times = 2;
                mockConnection.sendMessage((org.apache.qpid.proton.message.Message) any, MessageType.DEVICE_TELEMETRY, (IotHubConnectionString) any);
                times = 0;
                new IotHubCallbackPacket(IotHubStatusCode.MESSAGE_EXPIRED, (IotHubEventCallback) any, any);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_15_019: [If the transport closed, the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void invokeCallbacksFailsIfTransportNotOpen()
            throws IOException
    {

        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };
        AmqpsTransport transport = new AmqpsTransport(mockConfig);

        transport.invokeCallbacks();
    }

    // Tests_SRS_AMQPSTRANSPORT_15_019: [If the transport closed, the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void invokeCallbacksFailsIfTransportOpenedAndClosed()
            throws IOException
    {

        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };
        AmqpsTransport transport = new AmqpsTransport(mockConfig);

        transport.open();
        transport.close();
        transport.invokeCallbacks();
    }

    // Tests_SRS_AMQPSTRANSPORT_15_020: [The function shall invoke all the callbacks from the callback queue.]
    @Test
    public void invokeCallbacksInvokesAllCallbacksFromQueue() throws IOException
    {
        final Integer context = 24;

        new NonStrictExpectations()
        {
            {

                new AmqpsIotHubConnection(mockConfig);

                result = mockConnection;
                mockIotHubCallbackPacket.getCallback();
                result = mockIotHubEventCallback;
                mockIotHubCallbackPacket.getStatus();
                result = IotHubStatusCode.OK_EMPTY;
                mockIotHubCallbackPacket.getContext();
                result = context;
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();

        Queue<IotHubCallbackPacket> callbackList = new LinkedList<>();
        callbackList.add(mockIotHubCallbackPacket);
        callbackList.add(mockIotHubCallbackPacket);
        Deencapsulation.setField(transport, "callbackList", callbackList);

        transport.invokeCallbacks();

        new Verifications()
        {
            {
                mockIotHubCallbackPacket.getStatus();
                times = 2;
                mockIotHubCallbackPacket.getCallback();
                times = 2;
                mockIotHubCallbackPacket.getCallback();
                times = 2;
                mockIotHubEventCallback.execute(IotHubStatusCode.OK_EMPTY, context);
                times = 2;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_15_021: [If the transport is closed, the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void handleMessageFailsIfTransportNeverOpened() throws IOException
    {

        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };
        AmqpsTransport transport = new AmqpsTransport(mockConfig);

        transport.handleMessage();
    }

    // Tests_SRS_AMQPSTRANSPORT_15_021: [If the transport is closed, the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void handleMessageFailsIfTransportAlreadyClosed() throws IOException
    {
        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();
        transport.close();
        transport.handleMessage();
    }

    // Tests_SRS_AMQPSTRANSPORT_15_024: [If no message was received from IotHub, the function shall return.]
    @Test
    public void handleMessageReturnsNoReceivedMessage() throws IOException
    {
        new NonStrictExpectations()
        {
            {

                new AmqpsIotHubConnection(mockConfig);

                result = mockConnection;
                mockConfig.getDeviceTelemetryMessageCallback();
                result = mockMessageCallback;
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();
        transport.handleMessage();

        Queue<AmqpsMessage> receivedMessages = Deencapsulation.getField(transport, "receivedMessages");
        Assert.assertEquals(0, receivedMessages.size());

        new Verifications()
        {
            {
                mockMessageCallback.execute((Message) any, any);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_12_007: [The function throws IllegalStateException if none of the device operation object could handle the conversion.]
    @Test (expected = IllegalStateException.class)
    public void handleMessageThrowsIfNoDeviceOperationEnabled() throws IOException
    {
        final AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();

        final Queue<AmqpsMessage> receivedMessages = new LinkedBlockingQueue<>();
        receivedMessages.add(mockAmqpsMessage);
        receivedMessages.add(mockAmqpsMessage);
        Deencapsulation.setField(transport, "receivedMessages", receivedMessages);
        Deencapsulation.setField(transport, "connection", mockConnection);

        new NonStrictExpectations()
        {
            {
                mockAmqpsMessage.getDeviceClientConfig();
                result = mockDeviceClientConfig;
                Deencapsulation.invoke(mockConnection, "convertFromProton", mockAmqpsMessage, mockAmqpsMessage.getDeviceClientConfig());
                result = null;
            }
        };

        transport.handleMessage();
    }

    // Tests_SRS_AMQPSTRANSPORT_12_008: [The function shall return if there is no message callback defined.]
    @Test
    public void handleMessageReturnIfNoCallback(
    ) throws IOException
    {
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);
                result = mockAmqpsConvertFromProtonReturnValue;
                Deencapsulation.invoke(mockAmqpsConvertFromProtonReturnValue, "getMessageCallback");
                result = null;
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();

        Queue<AmqpsMessage> receivedMessages = new LinkedBlockingQueue<>();
        receivedMessages.add(mockAmqpsMessage);
        receivedMessages.add(mockAmqpsMessage);
        Deencapsulation.setField(transport, "receivedMessages", receivedMessages);

        transport.handleMessage();

        Queue<AmqpsMessage> receivedTransportMessages = Deencapsulation.getField(transport, "receivedMessages");

        new Verifications()
        {
            {
                mockMessageCallback.execute((Message) any, any);
                times = 0;
                mockConnection.sendMessageResult(mockAmqpsMessage, IotHubMessageResult.COMPLETE);
                times = 0;
            }
        };

        Assert.assertTrue(receivedTransportMessages.size() == 1);
    }


    // Tests_SRS_AMQPSTRANSPORT_15_023: [The function shall attempt to consume a message from the IoT Hub.]
    // Tests_SRS_AMQPSTRANSPORT_15_026: [The function shall invoke the callback on the message.]
    // Tests_SRS_AMQPSTRANSPORT_15_027: [The function shall return the message result (one of COMPLETE, ABANDON, or REJECT) to the IoT Hub.]
    @Test
    public void handleMessageConsumesAMessage(
    ) throws IOException
    {
        new NonStrictExpectations()
        {
            {

                new AmqpsIotHubConnection(mockConfig);

                result = mockConnection;
                mockConfig.getDeviceTelemetryMessageCallback();
                result = mockMessageCallback;
                mockMessageCallback.execute((Message) any, any);
                result = IotHubMessageResult.COMPLETE;
                mockConnection.sendMessageResult(mockAmqpsMessage, IotHubMessageResult.COMPLETE);
                result = true;
                mockConfig.getDeviceId();
                result = "deviceId";
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);
                result = mockAmqpsConvertFromProtonReturnValue;
                Deencapsulation.setField(mockAmqpsConvertFromProtonReturnValue, "message", new Message());
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();

        Queue<AmqpsMessage> receivedMessages = new LinkedBlockingQueue<>();
        receivedMessages.add(mockAmqpsMessage);
        receivedMessages.add(mockAmqpsMessage);
        Deencapsulation.setField(transport, "receivedMessages", receivedMessages);

        transport.handleMessage();

        Queue<AmqpsMessage> receivedTransportMessages = Deencapsulation.getField(transport, "receivedMessages");

        new Verifications()
        {
            {
                mockMessageCallback.execute((Message) any, any);
                times = 1;
                mockConnection.sendMessageResult(mockAmqpsMessage, IotHubMessageResult.COMPLETE);
                times = 1;
            }
        };

        Assert.assertTrue(receivedTransportMessages.size() == 1);
    }

    // Tests_SRS_AMQPSTRANSPORT_15_028: [If the result could not be sent to IoTHub, the message shall be put back in the received messages queue to be processed again.]
    // Tests_SRS_AMQPSTRANSPORT_15_028: [If the result could not be sent to IoTHub, the message shall be put back in the received messages queue to be processed again.]
    @Test
    public void handleMessagePutsMessageBackIntoQueueIfCannotSendResultBackToServer() throws IOException
    {
        new NonStrictExpectations()
        {
            {

                new AmqpsIotHubConnection(mockConfig);

                result = mockConnection;
                mockConfig.getDeviceTelemetryMessageCallback();
                result = mockMessageCallback;
                mockMessageCallback.execute((Message) any, any);
                result = IotHubMessageResult.COMPLETE;
                mockConnection.sendMessageResult(mockAmqpsMessage, IotHubMessageResult.COMPLETE);
                result = false;
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);
                result = mockAmqpsConvertFromProtonReturnValue;
                Deencapsulation.setField(mockAmqpsConvertFromProtonReturnValue, "message", new Message());
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();

        Queue<AmqpsMessage> receivedMessages = new LinkedBlockingQueue<>();
        receivedMessages.add(mockAmqpsMessage);
        receivedMessages.add(mockAmqpsMessage);
        Deencapsulation.setField(transport, "receivedMessages", receivedMessages);

        transport.handleMessage();

        Queue<AmqpsMessage> receivedTransportMessages = Deencapsulation.getField(transport, "receivedMessages");

        Assert.assertTrue(receivedTransportMessages.size() == 2);

        new Verifications()
        {
            {
                mockMessageCallback.execute((Message) any, any);
                times = 1;
                mockConnection.sendMessageResult(mockAmqpsMessage, IotHubMessageResult.COMPLETE);
                times = 1;
            }
        };

        Assert.assertTrue(receivedTransportMessages.size() == 2);
    }

    // Tests_SRS_AMQPSTRANSPORT_15_029: [If the hash cannot be found in the list of keys for the messages in progress, the method returns.]
    @Test
    public void messageSentReturnsIfThereAreNoMessagesInProgress() throws IOException
    {
        new NonStrictExpectations()
        {
            {

                new AmqpsIotHubConnection(mockConfig);

                result = mockConnection;
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();

        Map<Integer, IotHubOutboundPacket> inProgressMessages = new ConcurrentHashMap<>();
        Deencapsulation.setField(transport, "inProgressMessages", inProgressMessages);

        transport.messageSent(1, true);

        new Verifications()
        {
            {
                new IotHubCallbackPacket(IotHubStatusCode.OK_EMPTY, (IotHubEventCallback) any, any);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_15_030: [If the message was successfully delivered,
    // its callback is added to the list of callbacks to be executed.]
    @Test
    public void messageSentRemovesSuccessfullyDeliveredMessageFromInProgressMap() throws IOException
    {
        new NonStrictExpectations()
        {
            {

                new AmqpsIotHubConnection(mockConfig);

                result = mockConnection;
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();

        Map<Integer, IotHubOutboundPacket> inProgressMessages = new ConcurrentHashMap<>();
        inProgressMessages.put(1, new IotHubOutboundPacket(new Message(), mockIotHubEventCallback, new Object()));
        inProgressMessages.put(2, new IotHubOutboundPacket(new Message(), mockIotHubEventCallback, new Object()));
        Deencapsulation.setField(transport, "inProgressMessages", inProgressMessages);

        transport.messageSent(1, true);

        new Verifications()
        {
            {
                new IotHubCallbackPacket(IotHubStatusCode.OK_EMPTY, (IotHubEventCallback) any, any);
                times = 1;
            }
        };

        Queue<IotHubOutboundPacket> waitingMessages = Deencapsulation.getField(transport, "waitingMessages");
        Queue<IotHubCallbackPacket> callbackList  = Deencapsulation.getField(transport, "callbackList");

        Assert.assertTrue(inProgressMessages.size() == 1);
        Assert.assertTrue(waitingMessages.size() == 0);
        Assert.assertTrue(callbackList.size() == 1);
    }

    // Tests_SRS_AMQPSTRANSPORT_15_031: [If the message was not delivered successfully, it is buffered to be sent again.]
    @Test
    public void messageSentBuffersPreviouslySentMessageIfNotSuccessfullyDelivered() throws IOException
    {
        new NonStrictExpectations()
        {
            {

                new AmqpsIotHubConnection(mockConfig);

                result = mockConnection;
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();

        Map<Integer, IotHubOutboundPacket> inProgressMessages = new ConcurrentHashMap<>();
        inProgressMessages.put(1, new IotHubOutboundPacket(new Message(), mockIotHubEventCallback, new Object()));
        inProgressMessages.put(2, new IotHubOutboundPacket(new Message(), mockIotHubEventCallback, new Object()));
        Deencapsulation.setField(transport, "inProgressMessages", inProgressMessages);

        transport.messageSent(1, false);

        new Verifications()
        {
            {
                new IotHubCallbackPacket(IotHubStatusCode.OK_EMPTY, (IotHubEventCallback) any, any);
                times = 0;
            }
        };

        Queue<IotHubOutboundPacket> waitingMessages = Deencapsulation.getField(transport, "waitingMessages");
        Queue<IotHubCallbackPacket> callbackList  = Deencapsulation.getField(transport, "callbackList");

        Assert.assertTrue(inProgressMessages.size() == 1);
        Assert.assertTrue(waitingMessages.size() == 1);
        Assert.assertTrue(callbackList.size() == 0);
    }

    // Tests_SRS_AMQPSTRANSPORT_15_032: [The messages in progress are buffered to be sent again.]
    // Tests_SRS_AMQPSTRANSPORT_15_033: [The map of messages in progress is cleared.]
    @Test
    public void connectionLostClearsAllInProgressMessagesAndAddsThemToTheWaitingList() throws IOException
    {
        new NonStrictExpectations()
        {
            {

                new AmqpsIotHubConnection(mockConfig);

                result = mockConnection;
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();

        Map<Integer, IotHubOutboundPacket> inProgressMessages = new ConcurrentHashMap<>();
        inProgressMessages.put(1, new IotHubOutboundPacket(new Message(), mockIotHubEventCallback, new Object()));
        inProgressMessages.put(2, new IotHubOutboundPacket(new Message(), mockIotHubEventCallback, new Object()));
        Deencapsulation.setField(transport, "inProgressMessages", inProgressMessages);

        Queue<IotHubOutboundPacket> waitingMessages = new LinkedBlockingQueue<>();
        waitingMessages.add(new IotHubOutboundPacket(new Message(), mockIotHubEventCallback, new Object()));
        waitingMessages.add(new IotHubOutboundPacket(new Message(), mockIotHubEventCallback, new Object()));
        Deencapsulation.setField(transport, "waitingMessages", waitingMessages);

        transport.connectionLost();

        Assert.assertTrue(inProgressMessages.size() == 0);
        Assert.assertTrue(waitingMessages.size() == 4);
    }

    // Tests_SRS_AMQPSTRANSPORT_99_001: [All registered connection state callbacks are notified that the connection has been lost.]
    // Tests_SRS_AMQPSTRANSPORT_99_003: [RegisterConnectionStateCallback shall register the connection state callback.]
    @Test
    public void connectionLostNotifyAllConnectionStateCallbacks() throws IOException
    {
        new NonStrictExpectations()
        {
            {

                new AmqpsIotHubConnection(mockConfig);

                result = mockConnection;
                mockConnectionStateCallback.execute(IotHubConnectionState.CONNECTION_DROP, null);
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.registerConnectionStateCallback(mockConnectionStateCallback, null);
        transport.open();

        transport.connectionLost();

        new Verifications()
        {
            {
                mockConnectionStateCallback.execute(IotHubConnectionState.CONNECTION_DROP, null);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_34_042: If the provided callback is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void registerConnectionStatusCallbackThrowsForNullCallback() throws IOException
    {
        //arrange
        AmqpsTransport transport = new AmqpsTransport(mockConfig);

        //act
        transport.registerConnectionStateCallback(null, null);
    }


    // Tests_SRS_AMQPSTRANSPORT_99_002: [All registered connection state callbacks are notified that the connection has been established.]
    // Tests_SRS_AMQPSTRANSPORT_99_003: [RegisterConnectionStateCallback shall register the connection state callback.]
    @Test
    public void connectionEstablishedNotifyAllConnectionStateCallbacks() throws IOException
    {
        new NonStrictExpectations()
        {
            {

                new AmqpsIotHubConnection(mockConfig);

                result = mockConnection;
                mockConnectionStateCallback.execute(IotHubConnectionState.CONNECTION_SUCCESS, null);
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.registerConnectionStateCallback(mockConnectionStateCallback, null);
        transport.open();

        transport.connectionEstablished();

        new Verifications()
        {
            {
                mockConnectionStateCallback.execute(IotHubConnectionState.CONNECTION_SUCCESS, null);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_15_034: [The message received is added to the list of messages to be processed.]
    @Test
    public void messageReceivedAddsTheMessageToTheListOfMessagesToBeProcessed() throws IOException
    {
        new NonStrictExpectations()
        {
            {

                new AmqpsIotHubConnection(mockConfig);

                result = mockConnection;
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);
        transport.open();

        Queue<AmqpsMessage> receivedMessages = new LinkedBlockingQueue<>();
        receivedMessages.add(mockAmqpsMessage);
        receivedMessages.add(mockAmqpsMessage);
        Deencapsulation.setField(transport, "receivedMessages", receivedMessages);

        transport.messageReceived(mockAmqpsMessage);

        Assert.assertTrue(receivedMessages.size() == 3);
    }

    // Tests_SRS_AMQPSTRANSPORT_15_035: [The function shall return true if the waiting list,
    // in progress list and callback list are all empty, and false otherwise.]
    @Test
    public void isEmptyReturnsTrue() throws IOException
    {

        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);

        Boolean isEmpty = transport.isEmpty();

        Assert.assertTrue(isEmpty);
    }

    // Tests_SRS_AMQPSTRANSPORT_15_035: [The function shall return true if the waiting list,
    // in progress list and callback list are all empty, and false otherwise.]
    @Test
    public void isEmptyReturnsFalseIfWaitingListIsNotEmpty() throws IOException
    {

        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);


        Queue<IotHubOutboundPacket> waitingMessages = new LinkedBlockingQueue<>();
        waitingMessages.add(new IotHubOutboundPacket(new Message(), mockIotHubEventCallback, new Object()));
        Deencapsulation.setField(transport, "waitingMessages", waitingMessages);

        Boolean isEmpty = transport.isEmpty();

        Assert.assertFalse(isEmpty);
    }

    // Tests_SRS_AMQPSTRANSPORT_15_035: [The function shall return true if the waiting list,
    // in progress list and callback list are all empty, and false otherwise.]
    @Test
    public void isEmptyReturnsFalseIfInProgressMapIsNotEmpty() throws IOException
    {

        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);


        Map<Integer, IotHubOutboundPacket> inProgressMessages = new ConcurrentHashMap<>();
        inProgressMessages.put(1, new IotHubOutboundPacket(new Message(), mockIotHubEventCallback, new Object()));
        Deencapsulation.setField(transport, "inProgressMessages", inProgressMessages);

        Boolean isEmpty = transport.isEmpty();

        Assert.assertFalse(isEmpty);
    }

    // Tests_SRS_AMQPSTRANSPORT_15_035: [The function shall return true if the waiting list,
    // in progress list and callback list are all empty, and false otherwise.]
    @Test
    public void isEmptyReturnsFalseIfCallbackListIsNotEmpty() throws IOException
    {

        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = "deviceId";
            }
        };

        AmqpsTransport transport = new AmqpsTransport(mockConfig);

        Queue<IotHubCallbackPacket> callbackList = new LinkedList<>();
        callbackList.add(mockIotHubCallbackPacket);
        Deencapsulation.setField(transport, "callbackList", callbackList);

        Boolean isEmpty = transport.isEmpty();

        Assert.assertFalse(isEmpty);
    }
}