/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.deps.ws.WebSocketHandler;
import com.microsoft.azure.sdk.iot.deps.ws.impl.WebSocketImpl;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.net.IotHubUri;
import com.microsoft.azure.sdk.iot.device.transport.State;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import mockit.*;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.FlowController;
import org.apache.qpid.proton.reactor.Handshaker;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class AmqpsIotHubConnectionTest {

    final String hostName = "test.host.name";
    final String hubName = "test.iothub";
    final String deviceId = "test-deviceId";
    final String deviceKey = "test-devicekey?&test";
    final String amqpPort = "5671";
    final String amqpWebSocketPort = "443";

    @Mocked
    protected Handshaker mockHandshaker;

    @Mocked
    protected FlowController mockFlowController;

    @Mocked
    protected Proton mockProton;

    @Mocked
    protected IotHubReactor mockIotHubReactor;

    @Mocked
    protected Reactor mockReactor;

    @Mocked
    protected DeviceClientConfig mockConfig;

    @Mocked
    protected IotHubUri mockIotHubUri;

    @Mocked
    protected IotHubSasToken mockToken;

    @Mocked
    protected Message mockProtonMessage;

    @Mocked
    protected AmqpsMessage mockAmqpsMessage;

    @Mocked
    protected IotHubSasToken mockSasToken;

    @Mocked
    protected Sender mockSenderTelemetry;

    @Mocked
    protected Receiver mockReceiverTelemetry;

    @Mocked
    protected Sender mockSenderDeviceMethods;

    @Mocked
    protected Receiver mockReceiverDeviceMethods;

    @Mocked
    protected Sender mockSenderDeviceTwin;

    @Mocked
    protected Receiver mockReceiverDeviceTwin;

    @Mocked
    protected Connection mockConnection;

    @Mocked
    protected Session mockSession;

    @Mocked
    protected Event mockEvent;

    @Mocked
    protected Future mockReactorFuture;

    @Mocked
    protected ExecutorService mockExecutorService;

    @Mocked
    protected Delivery mockDelivery;

    @Mocked
    protected Transport mockTransport;

    @Mocked
    protected TransportInternal mockTransportInternal;

    @Mocked
    protected Sasl mockSasl;

    @Mocked
    protected SslDomain mockSslDomain;

    @Mocked
    protected IotHubSSLContext mockIotHubSSLContext;

    @Mocked
    protected WebSocketImpl mockWebSocket;

    @Mocked
    ServerListener mockServerListener;

    @Mocked
    Target mockTarget;

    @Mocked
    Source mockSource;

    @Mocked
    ObjectLock mockOpenLock;

    @Mocked
    ObjectLock mockCloseLock;

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfHostNameIsEmpty() throws IOException
    {
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = "";
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getDeviceKey();
                result = deviceKey;
            }
        };

        new AmqpsIotHubConnection(mockConfig, false);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfHostNameIsNull() throws IOException
    {
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = null;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getDeviceKey();
                result = deviceKey;
            }
        };

        new AmqpsIotHubConnection(mockConfig, false);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfDeviceIdIsEmpty() throws IOException
    {
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = hostName;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = "";
                mockConfig.getDeviceKey();
                result = deviceKey;
            }
        };

        new AmqpsIotHubConnection(mockConfig, false);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfDeviceIdIsNull() throws IOException
    {
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = hostName;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = null;
                mockConfig.getDeviceKey();
                result = deviceKey;
            }
        };

        new AmqpsIotHubConnection(mockConfig, false);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfUserNameIsEmpty() throws IOException
    {
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = hostName;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getDeviceKey();
                result = "";
            }
        };

        new AmqpsIotHubConnection(mockConfig, false);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfUserNameIsNull() throws IOException
    {
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = hostName;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getDeviceKey();
                result = null;
            }
        };

        new AmqpsIotHubConnection(mockConfig, false);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfHubNameIsEmpty() throws IOException
    {
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = hostName;
                mockConfig.getIotHubName();
                result = "";
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getDeviceKey();
                result = deviceKey;
            }
        };

        new AmqpsIotHubConnection(mockConfig, false);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfHubNameIsNull() throws IOException
    {
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = hostName;
                mockConfig.getIotHubName();
                result = null;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getDeviceKey();
                result = deviceKey;
            }
        };

        new AmqpsIotHubConnection(mockConfig, false);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_002: [The constructor shall save the configuration into private member variables.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_003: [The constructor shall initialize the sender and receiver
    // endpoint private member variables using the send/receiveEndpointFormat constants and device id.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_004: [The constructor shall initialize a new Handshaker
    // (Proton) object to handle communication handshake.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_005: [The constructor shall initialize a new FlowController
    // (Proton) object to handle communication flow.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_006: [The constructor shall set its state to CLOSED.]
    @Test
    public void constructorCopiesAllData() throws IOException
    {
        baseExpectations();

        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        DeviceClientConfig actualConfig = Deencapsulation.getField(connection, "config");
        String actualHostName = Deencapsulation.getField(connection, "hostName");
        String actualUserName = Deencapsulation.getField(connection, "userName");
        AmqpsDeviceTelemetry actualDeviceTelemetry = Deencapsulation.getField(connection, "deviceTelemetry");
        AmqpsDeviceMethods actualDeviceMethods = Deencapsulation.getField(connection, "deviceMethod");
        AmqpsDeviceTwin actualDeviceTwin = Deencapsulation.getField(connection, "deviceTwin");

        assertEquals(mockConfig, actualConfig);
        assertEquals(hostName + ":" + amqpPort, actualHostName);
        assertEquals(deviceId + "@sas." + hubName, actualUserName);

        String expectedTelemetrySenderLinkAddress = "/devices/test-deviceId/messages/events";
        assertEquals(expectedTelemetrySenderLinkAddress, actualDeviceTelemetry.getSenderLinkAddress());
        String expectedTelemetryReceiveLinkAddress = "/devices/test-deviceId/messages/devicebound";
        assertEquals(expectedTelemetryReceiveLinkAddress, actualDeviceTelemetry.getReceiverLinkAddress());

        String expectedMethodSenderLinkAddress = "/devices/test-deviceId/methods/devicebound";
        assertEquals(expectedMethodSenderLinkAddress, actualDeviceMethods.getSenderLinkAddress());
        String expectedMethodReceiveEndpoint = "/devices/test-deviceId/methods/devicebound";
        assertEquals(expectedMethodReceiveEndpoint, actualDeviceMethods.getReceiverLinkAddress());

        String expectedTwinSenderLinkAddress = "/devices/test-deviceId/twin";
        assertEquals(expectedTwinSenderLinkAddress, actualDeviceTwin.getSenderLinkAddress());
        String expectedTwinReceiveEndpoint = "/devices/test-deviceId/twin";
        assertEquals(expectedTwinReceiveEndpoint, actualDeviceTwin.getReceiverLinkAddress());

        new Verifications()
        {
            {
                new Handshaker();
                times = 1;
                new FlowController();
                times = 1;
            }
        };

        State actualState = Deencapsulation.getField(connection, "state");
        assertEquals(State.CLOSED, actualState);
    }

    @Test
    public void constructorSetsHostNameCorrectlyWhenWebSocketsAreEnabled() throws IOException
    {
        baseExpectations();

        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, true);

        String actualHostName = Deencapsulation.getField(connection, "hostName");
        assertEquals(hostName + ":" + amqpWebSocketPort, actualHostName);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_007: [If the AMQPS connection is already open, the function shall do nothing.]
    @Test
    public void openDoesNothingIfTheConnectionIsAlreadyOpen() throws IOException, InterruptedException
    {
        baseExpectations();

        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        Deencapsulation.setField(connection, "state", State.OPEN);

        connection.open();

        new Verifications()
        {
            {
                new IotHubSasToken(mockConfig, anyLong);
                times = 0;
            }
        };
    }


    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_008: [The function shall create a new sasToken valid for the duration
    // specified in config to be used for the communication with IoTHub.]
    @Test
    public void openCreatesSasToken() throws IOException, InterruptedException
    {
        baseExpectations();

        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        connection.open();

        new Verifications()
        {
            {
                new IotHubSasToken(mockConfig, anyLong);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_009: [The function shall trigger the Reactor (Proton) to begin running.]
    @Test
    public void openTriggersProtonReactor() throws IOException, InterruptedException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                new IotHubReactor((Reactor) any);
                result = mockIotHubReactor;
                mockIotHubReactor.run();

                mockOpenLock.waitLock(anyLong);
            }
        };

        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);
        Deencapsulation.setField(connection, "openLock", mockOpenLock);

        connection.open();

        new Verifications()
        {
            {
                new IotHubSasToken(mockConfig, anyLong);
                times = 1;
                new IotHubReactor((Reactor)any);
                times = 1;
                mockOpenLock.waitLock(anyLong);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_010: [The function shall wait for the reactor to be ready and for
    // enough link credit to become available.]
    @Test
    public void openWaitsForReactorToBeReadyAndForEnoughLinkCreditToBeAvailable() throws IOException, InterruptedException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockOpenLock.waitLock(anyLong);
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);
        Deencapsulation.setField(connection, "openLock", mockOpenLock);

        connection.open();

        new Verifications()
        {
            {
                mockOpenLock.waitLock(anyLong);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_011: [If any exception is thrown while attempting to trigger
    // the reactor, the function shall close the connection and throw an IOException.]
    @Test(expected = IOException.class)
    public void openFailsIfConnectionIsNotOpenedInTime() throws Exception
    {
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);
        Deencapsulation.setField(connection, "openLock", mockOpenLock);

        new NonStrictExpectations()
        {
            {
                mockOpenLock.waitLock(anyLong);
                result = new InterruptedException();
            }
        };

        connection.open();
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_048 [If the AMQPS connection is already closed, the function shall do nothing.]
    @Test
    public void closeDoesNothingIfTheConnectionWasNeverOpened() throws InterruptedException, IOException
    {
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);
        Deencapsulation.setField(connection, "closeLock", mockCloseLock);

        new NonStrictExpectations()
        {
            {
                mockCloseLock.waitLock(anyLong);
            }
        };

        connection.close();

        new Verifications()
        {
            {
                mockSenderTelemetry.close();
                times = 0;
                mockReceiverTelemetry.close();
                times = 0;
                mockSenderDeviceMethods.close();
                times = 0;
                mockReceiverDeviceMethods.close();
                times = 0;
                mockSenderDeviceTwin.close();
                times = 0;
                mockReceiverDeviceTwin.close();
                times = 0;
                mockSession.close();
                times = 0;
                mockConnection.close();
                times = 0;
                mockReactorFuture.cancel(true);
                times = 0;
                mockExecutorService.shutdown();
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_012: [The function shall set the status of the AMQPS connection to CLOSED.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_013: [The function shall close the AMQPS sender and receiver links,
    // the AMQPS session and the AMQPS connection.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_014: [The function shall stop the Proton reactor.]
    @Test
    public void closeClosesAllProtonVariablesAndStopsProtonReactor() throws IOException, InterruptedException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockReactorFuture.cancel(true);
                mockExecutorService.shutdown();
            }
        };

        new MockUp<AmqpsIotHubConnection>() {
            @Mock
            void open()
            {
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        Deencapsulation.setField(connection, "state", State.OPEN);
        Deencapsulation.setField(connection, "senderLinkDeviceTelemetry", mockSenderTelemetry);
        Deencapsulation.setField(connection, "receiverLinkDeviceTelemetry", mockReceiverTelemetry);
        Deencapsulation.setField(connection, "senderLinkDeviceMethods", mockSenderDeviceMethods);
        Deencapsulation.setField(connection, "receiverLinkDeviceMethods", mockReceiverDeviceMethods);
        Deencapsulation.setField(connection, "senderLinkDeviceTwin", mockSenderDeviceTwin);
        Deencapsulation.setField(connection, "receiverLinkDeviceTwin", mockReceiverDeviceTwin);
        Deencapsulation.setField(connection, "session", mockSession);
        Deencapsulation.setField(connection, "connection", mockConnection);
        Deencapsulation.setField(connection, "executorService", mockExecutorService);

        connection.close();

        State actualState = Deencapsulation.getField(connection, "state");
        assertEquals(State.CLOSED, actualState);

        new Verifications()
        {
            {
                mockSenderTelemetry.close();
                times = 1;
                mockReceiverTelemetry.close();
                times = 1;
                mockSenderDeviceMethods.close();
                times = 1;
                mockReceiverDeviceMethods.close();
                times = 1;
                mockSenderDeviceTwin.close();
                times = 1;
                mockReceiverDeviceTwin.close();
                times = 1;
                mockSession.close();
                times = 1;
                mockConnection.close();
                times = 1;
                mockExecutorService.shutdown();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_015: [If the state of the connection is CLOSED or there is not enough
    // credit, the function shall return -1.]
    @Test
    public void sendMessageDoesNothingIfConnectionIsClosed() throws IOException
    {
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        Deencapsulation.setField(connection, "state", State.CLOSED);
        Deencapsulation.setField(connection, "linkCredit", 100);

        Integer expectedDeliveryHash = -1;
        Integer actualDeliveryHash = connection.sendMessage(Message.Factory.create(), null);

        assertEquals(expectedDeliveryHash, actualDeliveryHash);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_015: [If the state of the connection is CLOSED or there is not enough
    // credit, the function shall return -1.]
    @Test
    public void sendMessageDoesNothingIfNotEnoughLinkCredit() throws IOException
    {
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        Deencapsulation.setField(connection, "state", State.OPEN);
        Deencapsulation.setField(connection, "linkCredit", -1);

        Integer expectedDeliveryHash = -1;
        Integer actualDeliveryHash = connection.sendMessage(Message.Factory.create(), null);

        assertEquals(expectedDeliveryHash, actualDeliveryHash);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_016: [The function shall encode the message and copy the contents to the byte buffer.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_017: [The function shall set the delivery tag for the sender.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_018: [The function shall attempt to send the message using the sender link.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_019: [The function shall advance the sender link.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_020: [The function shall set the delivery hash to the value returned by the sender link.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_021: [The function shall return the delivery hash.]
    @Test
    public void sendMessageTelemetry() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockProtonMessage.encode((byte[]) any, anyInt, anyInt);
                mockSenderTelemetry.delivery((byte[]) any);
                result = mockDelivery;
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        Deencapsulation.setField(connection, "state", State.OPEN);
        Deencapsulation.setField(connection, "linkCredit", 100);
        Deencapsulation.setField(connection, "senderLinkDeviceTelemetry", mockSenderTelemetry);

        Integer expectedDeliveryHash = mockDelivery.hashCode();
        Integer actualDeliveryHash = connection.sendMessage(mockProtonMessage, MessageType.Telemetry);

        assertEquals(expectedDeliveryHash, actualDeliveryHash);

        new Verifications()
        {
            {
                mockProtonMessage.encode((byte[]) any, anyInt, anyInt);
                times = 1;
                mockSenderTelemetry.delivery((byte[]) any);
                times = 1;
                mockSenderTelemetry.send((byte[]) any, anyInt, anyInt);
                times = 1;
                mockSenderTelemetry.advance();
                times = 1;
                mockDelivery.hashCode();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_016: [The function shall encode the message and copy the contents to the byte buffer.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_017: [The function shall set the delivery tag for the sender.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_018: [The function shall attempt to send the message using the sender link.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_019: [The function shall advance the sender link.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_020: [The function shall set the delivery hash to the value returned by the sender link.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_021: [The function shall return the delivery hash.]
    @Test
    public void sendMessageDeviceMethods() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockProtonMessage.encode((byte[]) any, anyInt, anyInt);
                mockSenderDeviceMethods.delivery((byte[]) any);
                result = mockDelivery;
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        Deencapsulation.setField(connection, "state", State.OPEN);
        Deencapsulation.setField(connection, "linkCredit", 100);
        Deencapsulation.setField(connection, "senderLinkDeviceMethods", mockSenderDeviceMethods);

        Integer expectedDeliveryHash = mockDelivery.hashCode();
        Integer actualDeliveryHash = connection.sendMessage(mockProtonMessage, MessageType.DeviceMethods);

        assertEquals(expectedDeliveryHash, actualDeliveryHash);

        new Verifications()
        {
            {
                mockProtonMessage.encode((byte[]) any, anyInt, anyInt);
                times = 1;
                mockSenderDeviceMethods.delivery((byte[]) any);
                times = 1;
                mockSenderDeviceMethods.send((byte[]) any, anyInt, anyInt);
                times = 1;
                mockSenderDeviceMethods.advance();
                times = 1;
                mockDelivery.hashCode();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_016: [The function shall encode the message and copy the contents to the byte buffer.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_017: [The function shall set the delivery tag for the sender.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_018: [The function shall attempt to send the message using the sender link.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_019: [The function shall advance the sender link.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_020: [The function shall set the delivery hash to the value returned by the sender link.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_021: [The function shall return the delivery hash.]
    @Test
    public void sendMessageDeviceTwin() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockProtonMessage.encode((byte[]) any, anyInt, anyInt);
                mockSenderDeviceMethods.delivery((byte[]) any);
                result = mockDelivery;
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        Deencapsulation.setField(connection, "state", State.OPEN);
        Deencapsulation.setField(connection, "linkCredit", 100);
        Deencapsulation.setField(connection, "senderLinkDeviceMethods", mockSenderDeviceMethods);

        Integer expectedDeliveryHash = mockDelivery.hashCode();
        Integer actualDeliveryHash = connection.sendMessage(mockProtonMessage, MessageType.DeviceMethods);

        assertEquals(expectedDeliveryHash, actualDeliveryHash);

        new Verifications()
        {
            {
                mockProtonMessage.encode((byte[]) any, anyInt, anyInt);
                times = 1;
                mockSenderDeviceMethods.delivery((byte[]) any);
                times = 1;
                mockSenderDeviceMethods.send((byte[]) any, anyInt, anyInt);
                times = 1;
                mockSenderDeviceMethods.advance();
                times = 1;
                mockDelivery.hashCode();
                times = 1;
            }
        };
    }

    @Test
    public void sendMessageFreesDeliveryIfSendFailsTelemetry() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockProtonMessage.encode((byte[]) any, anyInt, anyInt);
                mockSenderTelemetry.delivery((byte[]) any);
                result = mockDelivery;
                mockSenderTelemetry.send((byte[]) any, anyInt, anyInt);
                result = new Exception();
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        Deencapsulation.setField(connection, "state", State.OPEN);
        Deencapsulation.setField(connection, "linkCredit", 100);
        Deencapsulation.setField(connection, "senderLinkDeviceTelemetry", mockSenderTelemetry);

        Integer expectedDeliveryHash = -1;
        Integer actualDeliveryHash = connection.sendMessage(mockProtonMessage, MessageType.Telemetry);

        assertEquals(expectedDeliveryHash, actualDeliveryHash);

        new Verifications()
        {
            {
                mockProtonMessage.encode((byte[]) any, anyInt, anyInt);
                times = 1;
                mockSenderTelemetry.delivery((byte[]) any);
                times = 1;
                mockSenderTelemetry.send((byte[]) any, anyInt, anyInt);
                times = 1;
                mockSenderTelemetry.advance();
                times = 1;
                mockDelivery.free();
                times = 1;
            }
        };
    }

    @Test
    public void sendMessageFreesDeliveryIfSendFailsDeviceMethods() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockProtonMessage.encode((byte[]) any, anyInt, anyInt);
                mockSenderDeviceMethods.delivery((byte[]) any);
                result = mockDelivery;
                mockSenderDeviceMethods.send((byte[]) any, anyInt, anyInt);
                result = new Exception();
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        Deencapsulation.setField(connection, "state", State.OPEN);
        Deencapsulation.setField(connection, "linkCredit", 100);
        Deencapsulation.setField(connection, "senderLinkDeviceMethods", mockSenderDeviceMethods);

        Integer expectedDeliveryHash = -1;
        Integer actualDeliveryHash = connection.sendMessage(mockProtonMessage, MessageType.DeviceMethods);

        assertEquals(expectedDeliveryHash, actualDeliveryHash);

        new Verifications()
        {
            {
                mockProtonMessage.encode((byte[]) any, anyInt, anyInt);
                times = 1;
                mockSenderDeviceMethods.delivery((byte[]) any);
                times = 1;
                mockSenderDeviceMethods.send((byte[]) any, anyInt, anyInt);
                times = 1;
                mockSenderDeviceMethods.advance();
                times = 1;
                mockDelivery.free();
                times = 1;
            }
        };
    }

    @Test
    public void sendMessageFreesDeliveryIfSendFailsDeviceTwin() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockProtonMessage.encode((byte[]) any, anyInt, anyInt);
                mockSenderDeviceTwin.delivery((byte[]) any);
                result = mockDelivery;
                mockSenderDeviceTwin.send((byte[]) any, anyInt, anyInt);
                result = new Exception();
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        Deencapsulation.setField(connection, "state", State.OPEN);
        Deencapsulation.setField(connection, "linkCredit", 100);
        Deencapsulation.setField(connection, "senderLinkDeviceTwin", mockSenderDeviceTwin);

        Integer expectedDeliveryHash = -1;
        Integer actualDeliveryHash = connection.sendMessage(mockProtonMessage, MessageType.DeviceTwin);

        assertEquals(expectedDeliveryHash, actualDeliveryHash);

        new Verifications()
        {
            {
                mockProtonMessage.encode((byte[]) any, anyInt, anyInt);
                times = 1;
                mockSenderDeviceTwin.delivery((byte[]) any);
                times = 1;
                mockSenderDeviceTwin.send((byte[]) any, anyInt, anyInt);
                times = 1;
                mockSenderDeviceTwin.advance();
                times = 1;
                mockDelivery.free();
                times = 1;
            }
        };
    }


    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_022: [If the AMQPS Connection is closed, the function shall return false.]
    @Test
    public void sendMessageReturnsFalseIfConnectionIsClosed() throws IOException
    {
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        Deencapsulation.setField(connection, "state", State.CLOSED);

        Boolean expectedResult = false;
        Boolean actualResult = connection.sendMessageResult(mockAmqpsMessage, IotHubMessageResult.COMPLETE);

        assertEquals(expectedResult, actualResult);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_023: [If the message result is COMPLETE, ABANDON, or REJECT,
    // the function shall acknowledge the last message with acknowledgement type COMPLETE, ABANDON, or REJECT respectively.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_024: [The function shall return true after the message was acknowledged.]
    @Test
    public void sendMessageAcknowledgesProperlyBasedOnMessageResult() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.COMPLETE);
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        Deencapsulation.setField(connection, "state", State.OPEN);

        for(final AmqpsMessage.ACK_TYPE ackType : AmqpsMessage.ACK_TYPE.values())
        {
            Boolean expectedResult = true;
            Boolean actualResult = connection.sendMessageResult(mockAmqpsMessage, IotHubMessageResult.valueOf(ackType.toString()));

            assertEquals(expectedResult, actualResult);

            new Verifications()
            {
                {
                    mockAmqpsMessage.acknowledge(ackType);
                    times = 1;
                }
            };
        }
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_025: [The event handler shall get the Connection (Proton) object from the event handler and set the host name on the connection.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_026: [The event handler shall create a Session (Proton) object from the connection.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_027: [The event handler shall create a Receiver and Sender (Proton) links and set the protocol tag on them to a predefined constant.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_028: [The Receiver and Sender links shall have the properties set to client version identifier.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_029: [The event handler shall open the connection, session, sender and receiver objects.]
    @Test
    public void onConnectionInit() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getConnection();
                result = mockConnection;
                mockConnection.session();
                result = mockSession;

                mockSession.receiver("receiver_link_telemetry");
                result = mockReceiverTelemetry;
                mockSession.sender("sender_link_telemetry");
                result = mockSenderTelemetry;

                mockSession.receiver("receiver_link_devicemethods");
                result = mockReceiverDeviceMethods;
                mockSession.sender("sender_link_devicemethods");
                result = mockSenderDeviceMethods;

                mockSession.receiver("receiver_link_devicetwin");
                result = mockReceiverDeviceTwin;
                mockSession.sender("sender_link_devicetwin");
                result = mockSenderDeviceTwin;
                
                mockConnection.open();
                mockSession.open();
                mockReceiverTelemetry.open();
                mockSenderTelemetry.open();
                mockReceiverDeviceMethods.open();
                mockSenderDeviceMethods.open();
                mockReceiverDeviceTwin.open();
                mockSenderDeviceTwin.open();
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        connection.onConnectionInit(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getConnection();
                times = 1;
                mockConnection.setHostname(hostName + ":" + amqpPort);
                times = 1;
                mockConnection.session();
                times = 1;

                mockSession.receiver("receiver_link_telemetry");
                times = 1;
                mockSession.sender("sender_link_telemetry");
                times = 1;
                mockReceiverTelemetry.setProperties((Map<Symbol, Object>) any);
                times = 1;
                mockSenderTelemetry.setProperties((Map<Symbol, Object>) any);
                times = 1;

                mockSession.receiver("receiver_link_devicemethods");
                times = 1;
                mockSession.sender("sender_link_devicemethods");
                times = 1;
                mockReceiverDeviceMethods.setProperties((Map<Symbol, Object>) any);
                times = 1;
                mockSenderDeviceMethods.setProperties((Map<Symbol, Object>) any);
                times = 1;

                mockSession.receiver("receiver_link_devicetwin");
                times = 1;
                mockSession.sender("sender_link_devicetwin");
                times = 1;
                mockReceiverDeviceTwin.setProperties((Map<Symbol, Object>) any);
                times = 1;
                mockSenderDeviceTwin.setProperties((Map<Symbol, Object>) any);
                times = 1;

                mockConnection.open();
                times = 1;
                mockSession.open();
                times = 1;
                mockReceiverTelemetry.open();
                times = 1;
                mockSenderTelemetry.open();
                times = 1;
                mockReceiverDeviceMethods.open();
                times = 1;
                mockSenderDeviceMethods.open();
                times = 1;
                mockReceiverDeviceTwin.open();
                times = 1;
                mockSenderDeviceTwin.open();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_030: [The event handler shall get the Transport (Proton) object from the event.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_031: [The event handler shall set the SASL_PLAIN authentication on the transport using the given user name and sas token.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_032: [The event handler shall set VERIFY_PEER authentication mode on the domain of the Transport.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_25_049: [The event handler shall set the SSL Context to IOTHub SSL context containing valid certificates.]
    @Test
    public void onConnectionBoundNoWebSockets() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getConnection();
                result = mockConnection;
                mockConnection.getTransport();
                result = mockTransport;
                mockTransport.sasl();
                result = mockSasl;
                mockSasl.plain(anyString, anyString);
                mockSslDomain.setSslContext(mockIotHubSSLContext.getIotHubSSlContext());
                mockSslDomain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
                mockTransport.ssl(mockSslDomain);
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        connection.onConnectionBound(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getConnection();
                times = 1;
                mockConnection.getTransport();
                times = 1;
                mockTransport.sasl();
                times = 1;
                mockSasl.plain(anyString, anyString);
                times = 1;
                mockSslDomain.setSslContext(mockIotHubSSLContext.getIotHubSSlContext());
                times = 1;
                mockSslDomain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
                times = 1;
                mockTransport.ssl(mockSslDomain);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_030: [The event handler shall get the Transport (Proton) object from the event.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_031: [The event handler shall set the SASL_PLAIN authentication on the transport using the given user name and sas token.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_032: [The event handler shall set VERIFY_PEER authentication mode on the domain of the Transport.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_25_049: [The event handler shall set the SSL Context to IOTHub SSL context containing valid certificates.]
    @Test
    public void onConnectionBoundWebSockets() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getConnection();
                result = mockConnection;
                mockConnection.getTransport();
                result = mockTransportInternal;
                new WebSocketImpl();
                result = mockWebSocket;
                mockWebSocket.configure(anyString, anyString, anyInt, anyString, (Map<String, String>) any, (WebSocketHandler) any);
                mockTransportInternal.addTransportLayer(mockWebSocket);
                mockTransportInternal.sasl();
                result = mockSasl;
                mockSasl.plain(anyString, anyString);
                mockSslDomain.setSslContext(mockIotHubSSLContext.getIotHubSSlContext());
                mockSslDomain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
                mockTransportInternal.ssl(mockSslDomain);
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        Deencapsulation.setField(connection, "useWebSockets", true);

        connection.onConnectionBound(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getConnection();
                times = 1;
                mockConnection.getTransport();
                times = 1;
                mockWebSocket.configure(hostName + ":" + amqpPort, "/$iothub/websocket", 0, "AMQPWSB10", null, null);
                times = 1;
                mockTransportInternal.addTransportLayer(mockWebSocket);
                times = 1;
                mockTransportInternal.sasl();
                times = 1;
                mockSasl.plain(deviceId + "@sas." + hubName, anyString);
                times = 1;
                mockSslDomain.setSslContext(mockIotHubSSLContext.getIotHubSSlContext());
                times = 1;
                mockSslDomain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
                times = 1;
                mockTransportInternal.ssl(mockSslDomain);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_033: [The event handler shall set the current handler to handle the connection events.]
    @Test
    public void onReactorInit() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getReactor();
                result = mockReactor;
                mockReactor.connectionToHost(anyString, anyInt, (Handler) any);
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        connection.onReactorInit(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getReactor();
                times = 1;
                mockReactor.connectionToHost(anyString, anyInt, (Handler) connection);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_034: [If this link is the Receiver link, the event handler shall get the Receiver and Delivery (Proton) objects from the event.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_035: [The event handler shall read the received buffer.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_036: [The event handler shall create an AmqpsMessage object from the decoded buffer.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_037: [The event handler shall set the AmqpsMessage Deliver (Proton) object.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_049: [All the listeners shall be notified that a message was received from the server.]
    @Test
    public void onDeliveryReceiveTelemetry() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockReceiverTelemetry;
                mockReceiverTelemetry.getName();
                result = "receiver_link_telemetry";
                mockReceiverTelemetry.current();
                result = mockDelivery;
                mockDelivery.isReadable();
                result = true;
                mockDelivery.isPartial();
                result = false;
                mockDelivery.pending();
                result = 10;
                mockReceiverTelemetry.recv((byte[]) any, anyInt, anyInt);
                result = 10;
                mockReceiverTelemetry.advance();
                new AmqpsMessage();
                result = mockAmqpsMessage;
                mockAmqpsMessage.setDelivery(mockDelivery);
                mockAmqpsMessage.decode((byte[]) any, anyInt, anyInt);
                mockServerListener.messageReceived(mockAmqpsMessage);
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);
        connection.addListener(mockServerListener);
        connection.onDelivery(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockReceiverTelemetry.getName();
                times = 2;
                mockReceiverTelemetry.current();
                times = 1;
                mockDelivery.isReadable();
                times = 1;
                mockDelivery.isPartial();
                times = 1;
                mockDelivery.pending();
                times = 1;
                mockReceiverTelemetry.recv((byte[]) any, anyInt, anyInt);
                times = 1;
                mockReceiverTelemetry.advance();
                times = 1;
                mockAmqpsMessage.setDelivery(mockDelivery);
                times = 1;
                mockAmqpsMessage.decode((byte[]) any, anyInt, anyInt);
                times = 1;
                mockServerListener.messageReceived(mockAmqpsMessage);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_034: [If this link is the Receiver link, the event handler shall get the Receiver and Delivery (Proton) objects from the event.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_035: [The event handler shall read the received buffer.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_036: [The event handler shall create an AmqpsMessage object from the decoded buffer.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_037: [The event handler shall set the AmqpsMessage Deliver (Proton) object.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_049: [All the listeners shall be notified that a message was received from the server.]
    @Test
    public void onDeliveryReceiveDeviceMethods() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockReceiverDeviceMethods;
                mockReceiverDeviceMethods.getName();
                result = "receiver_link_devicemethods";
                mockReceiverDeviceMethods.current();
                result = mockDelivery;
                mockDelivery.isReadable();
                result = true;
                mockDelivery.isPartial();
                result = false;
                mockDelivery.pending();
                result = 10;
                mockReceiverDeviceMethods.recv((byte[]) any, anyInt, anyInt);
                result = 10;
                mockReceiverDeviceMethods.advance();
                new AmqpsMessage();
                result = mockAmqpsMessage;
                mockAmqpsMessage.setDelivery(mockDelivery);
                mockAmqpsMessage.decode((byte[]) any, anyInt, anyInt);
                mockServerListener.messageReceived(mockAmqpsMessage);
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);
        connection.addListener(mockServerListener);
        connection.onDelivery(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockReceiverDeviceMethods.getName();
                times = 4;
                mockReceiverDeviceMethods.current();
                times = 1;
                mockDelivery.isReadable();
                times = 1;
                mockDelivery.isPartial();
                times = 1;
                mockDelivery.pending();
                times = 1;
                mockReceiverDeviceMethods.recv((byte[]) any, anyInt, anyInt);
                times = 1;
                mockReceiverDeviceMethods.advance();
                times = 1;
                mockAmqpsMessage.setDelivery(mockDelivery);
                times = 1;
                mockAmqpsMessage.decode((byte[]) any, anyInt, anyInt);
                times = 1;
                mockServerListener.messageReceived(mockAmqpsMessage);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_034: [If this link is the Receiver link, the event handler shall get the Receiver and Delivery (Proton) objects from the event.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_035: [The event handler shall read the received buffer.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_036: [The event handler shall create an AmqpsMessage object from the decoded buffer.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_037: [The event handler shall set the AmqpsMessage Deliver (Proton) object.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_049: [All the listeners shall be notified that a message was received from the server.]
    @Test
    public void onDeliveryReceiveDeviceTwin() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockReceiverDeviceTwin;
                mockReceiverDeviceTwin.getName();
                result = "receiver_link_devicemethods";
                mockReceiverDeviceTwin.current();
                result = mockDelivery;
                mockDelivery.isReadable();
                result = true;
                mockDelivery.isPartial();
                result = false;
                mockDelivery.pending();
                result = 10;
                mockReceiverDeviceTwin.recv((byte[]) any, anyInt, anyInt);
                result = 10;
                mockReceiverDeviceTwin.advance();
                new AmqpsMessage();
                result = mockAmqpsMessage;
                mockAmqpsMessage.setDelivery(mockDelivery);
                mockAmqpsMessage.decode((byte[]) any, anyInt, anyInt);
                mockServerListener.messageReceived(mockAmqpsMessage);
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);
        connection.addListener(mockServerListener);
        connection.onDelivery(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockReceiverDeviceTwin.getName();
                times = 4;
                mockReceiverDeviceTwin.current();
                times = 1;
                mockDelivery.isReadable();
                times = 1;
                mockDelivery.isPartial();
                times = 1;
                mockDelivery.pending();
                times = 1;
                mockReceiverDeviceTwin.recv((byte[]) any, anyInt, anyInt);
                times = 1;
                mockReceiverDeviceTwin.advance();
                times = 1;
                mockAmqpsMessage.setDelivery(mockDelivery);
                times = 1;
                mockAmqpsMessage.decode((byte[]) any, anyInt, anyInt);
                times = 1;
                mockServerListener.messageReceived(mockAmqpsMessage);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_038: [If this link is the Sender link and the event type is DELIVERY, the event handler shall get the Delivery (Proton) object from the event.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_039: [The event handler shall note the remote delivery state and use it and the Delivery (Proton) hash code to inform the AmqpsIotHubConnection of the message receipt.]
    @Test
    public void onDeliverySend() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockReceiverTelemetry;
                mockReceiverTelemetry.getName();
                result = "sender";
                mockEvent.getType();
                result = Event.Type.DELIVERY;
                mockEvent.getDelivery();
                result = mockDelivery;
                mockDelivery.getRemoteState();
                result = Accepted.getInstance();
                mockServerListener.messageSent(anyInt, true);
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);
        connection.addListener(mockServerListener);
        connection.onDelivery(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockReceiverTelemetry.getName();
                times = 3;
                mockEvent.getType();
                times = 1;
                mockEvent.getDelivery();
                times = 1;
                mockDelivery.getRemoteState();
                times = 1;
                mockServerListener.messageSent(mockDelivery.hashCode(), true);
                times = 1;
                mockDelivery.free();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_040: [The event handler shall save the remaining link credit.]
    @Test
    public void onLinkFlow() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockSenderTelemetry;
                mockSenderTelemetry.getCredit();
                result = 100;
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);
        connection.onLinkFlow(mockEvent);

        Integer expectedLinkCredit = 100;
        Integer actualLinkCredit = Deencapsulation.getField(connection, "linkCredit");

        assertEquals(expectedLinkCredit, actualLinkCredit);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockSenderTelemetry.getCredit();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_041: [The connection state shall be considered OPEN when the sender link is open remotely for telemetry, methods or twin.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_99_001: [All server listeners shall be notified when that the connection has been established.]
    @Test
    public void onLinkRemoteOpen_Telemetry() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockSenderTelemetry;
                mockSenderTelemetry.getName();
                result = "sender_link_telemetry";
                mockServerListener.connectionEstablished();
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);
        connection.addListener(mockServerListener);
        connection.onLinkRemoteOpen(mockEvent);

        State expectedState = State.OPEN;
        State actualState = Deencapsulation.getField(connection, "state");

        assertEquals(expectedState, actualState);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockSenderTelemetry.getName();
                times = 1;
                mockServerListener.connectionEstablished();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_041: [The connection state shall be considered OPEN when the sender link is open remotely for telemetry, methods or twin.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_99_001: [All server listeners shall be notified when that the connection has been established.]
    @Test
    public void onLinkRemoteOpen_DeviceMethods() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockSenderDeviceMethods;
                mockSenderDeviceMethods.getName();
                result = "sender_link_devicemethods";
                mockServerListener.connectionEstablished();
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);
        connection.addListener(mockServerListener);
        connection.onLinkRemoteOpen(mockEvent);

        State expectedState = State.OPEN;
        State actualState = Deencapsulation.getField(connection, "state");

        assertEquals(expectedState, actualState);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockSenderDeviceMethods.getName();
                times = 2;
                mockServerListener.connectionEstablished();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_041: [The connection state shall be considered OPEN when the sender link is open remotely for telemetry, methods or twin.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_99_001: [All server listeners shall be notified when that the connection has been established.]
    @Test
    public void onLinkRemoteOpen_DeviceTwin() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockSenderDeviceMethods;
                mockSenderDeviceMethods.getName();
                result = "sender_link_devicetwin";
                mockServerListener.connectionEstablished();
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);
        connection.addListener(mockServerListener);
        connection.onLinkRemoteOpen(mockEvent);

        State expectedState = State.OPEN;
        State actualState = Deencapsulation.getField(connection, "state");

        assertEquals(expectedState, actualState);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockSenderDeviceMethods.getName();
                times = 3;
                mockServerListener.connectionEstablished();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_042 [The event handler shall attempt to reconnect to the IoTHub either the telemetry, methods or twin link closed.]
    @Test
    public void onLinkRemoteClose_Telemetry() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockSenderTelemetry;
                mockSenderTelemetry.getName();
                result = "sender_link_telemetry";
                mockServerListener.connectionLost();
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        final Boolean[] openAsyncCalled = { false };
        final Boolean[] closeAsyncCalled = { false };

        new MockUp<AmqpsIotHubConnection>()
        {
            @Mock
            void openAsync()
            {
                openAsyncCalled[0] = true;
                Deencapsulation.setField(connection, "state", State.OPEN);
            }

            @Mock
            void closeAsync()
            {
                closeAsyncCalled[0] = true;
                Deencapsulation.setField(connection, "state", State.CLOSED);
            }
        };

        connection.addListener(mockServerListener);
        connection.onLinkRemoteClose(mockEvent);

        assertEquals(true, closeAsyncCalled[0]);
        assertEquals(false, openAsyncCalled[0]);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockSenderTelemetry.getName();
                times = 1;
                mockServerListener.connectionLost();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_042 [The event handler shall attempt to reconnect to the IoTHub either the telemetry, methods or twin link closed.]
    @Test
    public void onLinkRemoteClose_DeviceMethods() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockSenderDeviceMethods;
                mockSenderDeviceMethods.getName();
                result = "sender_link_devicemethods";
                mockServerListener.connectionLost();
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        final Boolean[] openAsyncCalled = { false };
        final Boolean[] closeAsyncCalled = { false };

        new MockUp<AmqpsIotHubConnection>()
        {
            @Mock
            void openAsync()
            {
                openAsyncCalled[0] = true;
                Deencapsulation.setField(connection, "state", State.OPEN);
            }

            @Mock
            void closeAsync()
            {
                closeAsyncCalled[0] = true;
                Deencapsulation.setField(connection, "state", State.CLOSED);
            }
        };

        connection.addListener(mockServerListener);
        connection.onLinkRemoteClose(mockEvent);

        assertEquals(true, closeAsyncCalled[0]);
        assertEquals(false, openAsyncCalled[0]);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockSenderDeviceMethods.getName();
                times = 2;
                mockServerListener.connectionLost();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_042 [The event handler shall attempt to reconnect to the IoTHub either the telemetry, methods or twin link closed.]
    @Test
    public void onLinkRemoteClose_Twin() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockSenderDeviceTwin;
                mockSenderDeviceTwin.getName();
                result = "sender_link_devicetwin";
                mockServerListener.connectionLost();
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        final Boolean[] openAsyncCalled = { false };
        final Boolean[] closeAsyncCalled = { false };

        new MockUp<AmqpsIotHubConnection>()
        {
            @Mock
            void openAsync()
            {
                openAsyncCalled[0] = true;
                Deencapsulation.setField(connection, "state", State.OPEN);
            }

            @Mock
            void closeAsync()
            {
                closeAsyncCalled[0] = true;
                Deencapsulation.setField(connection, "state", State.CLOSED);
            }
        };

        connection.addListener(mockServerListener);
        connection.onLinkRemoteClose(mockEvent);

        assertEquals(true, closeAsyncCalled[0]);
        assertEquals(false, openAsyncCalled[0]);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockSenderDeviceTwin.getName();
                times = 3;
                mockServerListener.connectionLost();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_043: [If the link is the Sender link, the event handler shall create a new Target (Proton) object using the sender endpoint address member variable.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_044: [If the link is the Sender link, the event handler shall set its target to the created Target (Proton) object.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_14_045: [If the link is the Sender link, the event handler shall set the SenderSettleMode to UNSETTLED.]
    @Test
    public void onLinkInitSendTelemetry() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockSenderTelemetry;
                mockSenderTelemetry.getName();
                result = "sender_link_telemetry";
                new Target();
                result = mockTarget;
                mockTarget.setAddress("/devices/test-deviceId/messages/events");
                mockSenderTelemetry.setTarget(mockTarget);
                mockSenderTelemetry.setSenderSettleMode(SenderSettleMode.UNSETTLED);
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        connection.onLinkInit(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockSenderTelemetry.getName();
                times = 6;
                new Target();
                times = 1;
                mockTarget.setAddress(anyString);
                times = 1;
                mockSenderTelemetry.setTarget((org.apache.qpid.proton.amqp.transport.Target) any);
                times = 1;
                mockSenderTelemetry.setSenderSettleMode(SenderSettleMode.UNSETTLED);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_14_046: [If the link is the Receiver link, the event handler shall create a new Source (Proton) object using the receiver endpoint address member variable.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_14_047: [If the link is the Receiver link, the event handler shall set its source to the created Source (Proton) object.]
    @Test
    public void onLinkInitReceiveTelemetry() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockReceiverTelemetry;
                mockReceiverTelemetry.getName();
                result = "receiver_link_telemetry";
                new Source();
                result = mockSource;
                mockSource.setAddress("/devices/test-deviceId/messages/devicebound");
                mockReceiverTelemetry.setSource(mockSource);
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        connection.onLinkInit(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockReceiverTelemetry.getName();
                times = 6;
                new Source();
                times = 1;
                mockSource.setAddress(anyString);
                times = 1;
                mockReceiverTelemetry.setSource((org.apache.qpid.proton.amqp.transport.Source) any);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_043: [If the link is the Sender link, the event handler shall create a new Target (Proton) object using the sender endpoint address member variable.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_044: [If the link is the Sender link, the event handler shall set its target to the created Target (Proton) object.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_14_045: [If the link is the Sender link, the event handler shall set the SenderSettleMode to UNSETTLED.]
    @Test
    public void onLinkInitSendDeviceMethods() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockSenderDeviceMethods;
                mockSenderDeviceMethods.getName();
                result = "sender_link_devicemethods";
                new Target();
                result = mockTarget;
                mockTarget.setAddress("/devices/test-deviceId/methods/devicebound");
                mockSenderDeviceMethods.setTarget(mockTarget);
                mockSenderDeviceMethods.setSenderSettleMode(SenderSettleMode.UNSETTLED);
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        connection.onLinkInit(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockSenderDeviceMethods.getName();
                times = 6;
                new Target();
                times = 1;
                mockTarget.setAddress(anyString);
                times = 1;
                mockSenderDeviceMethods.setTarget((org.apache.qpid.proton.amqp.transport.Target) any);
                times = 1;
                mockSenderDeviceMethods.setSenderSettleMode(SenderSettleMode.UNSETTLED);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_14_046: [If the link is the Receiver link, the event handler shall create a new Source (Proton) object using the receiver endpoint address member variable.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_14_047: [If the link is the Receiver link, the event handler shall set its source to the created Source (Proton) object.]
    @Test
    public void onLinkInitReceiveDeviceMethods() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockReceiverDeviceMethods;
                mockReceiverDeviceMethods.getName();
                result = "receiver_link_devicemethods";
                new Source();
                result = mockSource;
                mockSource.setAddress("/devices/test-deviceId/messages/devicebound");
                mockReceiverDeviceMethods.setSource(mockSource);
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        connection.onLinkInit(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockReceiverDeviceMethods.getName();
                times = 6;
                new Source();
                times = 1;
                mockSource.setAddress(anyString);
                times = 1;
                mockReceiverDeviceMethods.setSource((org.apache.qpid.proton.amqp.transport.Source) any);
                times = 1;
            }
        };
    }


    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_043: [If the link is the Sender link, the event handler shall create a new Target (Proton) object using the sender endpoint address member variable.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_044: [If the link is the Sender link, the event handler shall set its target to the created Target (Proton) object.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_14_045: [If the link is the Sender link, the event handler shall set the SenderSettleMode to UNSETTLED.]
    @Test
    public void onLinkInitSendDeviceTwin() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockSenderDeviceTwin;
                mockSenderDeviceTwin.getName();
                result = "sender_link_devicetwin";
                new Target();
                result = mockTarget;
                mockTarget.setAddress("/devices/test-deviceId/methods/devicebound");
                mockSenderDeviceTwin.setTarget(mockTarget);
                mockSenderDeviceTwin.setSenderSettleMode(SenderSettleMode.UNSETTLED);
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        connection.onLinkInit(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockSenderDeviceTwin.getName();
                times = 6;
                new Target();
                times = 1;
                mockTarget.setAddress(anyString);
                times = 1;
                mockSenderDeviceTwin.setTarget((org.apache.qpid.proton.amqp.transport.Target) any);
                times = 1;
                mockSenderDeviceTwin.setSenderSettleMode(SenderSettleMode.UNSETTLED);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_14_046: [If the link is the Receiver link, the event handler shall create a new Source (Proton) object using the receiver endpoint address member variable.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_14_047: [If the link is the Receiver link, the event handler shall set its source to the created Source (Proton) object.]
    @Test
    public void onLinkInitReceiveDeviceTwin() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockReceiverDeviceTwin;
                mockReceiverDeviceTwin.getName();
                result = "receiver_link_devicetwin";
                new Source();
                result = mockSource;
                mockSource.setAddress("/devices/test-deviceId/messages/devicebound");
                mockReceiverDeviceTwin.setSource(mockSource);
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        connection.onLinkInit(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockReceiverDeviceTwin.getName();
                times = 6;
                new Source();
                times = 1;
                mockSource.setAddress(anyString);
                times = 1;
                mockReceiverDeviceTwin.setSource((org.apache.qpid.proton.amqp.transport.Source) any);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_048: [The event handler shall attempt to reconnect to IoTHub.]
    @Test
    public void onTransportError() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockServerListener.connectionLost();
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, false);

        final Boolean[] openAsyncCalled = { false };
        final Boolean[] closeAsyncCalled = { false };

        new MockUp<AmqpsIotHubConnection>()
        {
            @Mock
            void openAsync()
            {
                openAsyncCalled[0] = true;
                Deencapsulation.setField(connection, "state", State.OPEN);
            }

            @Mock
            void closeAsync()
            {
                closeAsyncCalled[0] = true;
                Deencapsulation.setField(connection, "state", State.CLOSED);
            }
        };

        connection.addListener(mockServerListener);
        connection.onTransportError(mockEvent);

        assertEquals(true, closeAsyncCalled[0]);
        assertEquals(false, openAsyncCalled[0]);

        new Verifications()
        {
            {
                mockServerListener.connectionLost();
                times = 1;
            }
        };
    }

    private void baseExpectations()
    {
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = hostName;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getDeviceKey();
                result = deviceKey;
            }
        };
    }
}