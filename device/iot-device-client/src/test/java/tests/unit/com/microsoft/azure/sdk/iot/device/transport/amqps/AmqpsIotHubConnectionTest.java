/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.deps.ws.WebSocketHandler;
import com.microsoft.azure.sdk.iot.deps.ws.impl.WebSocketImpl;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.net.IotHubUri;
import com.microsoft.azure.sdk.iot.device.transport.State;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import mockit.*;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.FlowController;
import org.apache.qpid.proton.reactor.Handshaker;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/* Unit tests for AmqpsIotHubConnection
* 100% methods covered
* 93% lines covered (Thread is not mockable...)
*/
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
    protected Message mockProtonMessage;

    @Mocked
    protected AmqpsMessage mockAmqpsMessage;


    @Mocked
    protected Sender mockSender;

    @Mocked
    protected Receiver mockReceiver;

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
    protected SSLContext mockSSLContext;

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

    @Mocked
    Link mockLink;

    @Mocked
    AmqpsDeviceOperations mockAmqpsDeviceOperations;

    @Mocked
    AmqpsDeviceTelemetry mockAmqpsDeviceTelemetry;

    @Mocked
    AmqpsSendReturnValue mockAmqpsSendReturnValue;

    @Mocked
    IotHubConnectionString mockConnectionString;

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfConfigIsNull() throws IOException
    {
        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        new AmqpsIotHubConnection(null, amqpsDeviceOperationsList);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfDeviceOperationListIsNull() throws IOException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = "xxx";
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getIotHubConnectionString();
                result = mockConnectionString;
                mockConnectionString.getSharedAccessKey();
                result = deviceKey;
            }
        };
        new AmqpsIotHubConnection(mockConfig, null);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfDeviceOperationListSizeIsZero() throws IOException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = "xxx";
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = deviceKey;
            }
        };
        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfHostNameIsEmpty() throws IOException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = "";
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = deviceKey;
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfHostNameIsNull() throws IOException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = null;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = deviceKey;
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfDeviceIdIsEmpty() throws IOException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = hostName;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = "";
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = deviceKey;
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfDeviceIdIsNull() throws IOException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = hostName;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = null;
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = deviceKey;
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
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
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = deviceKey;
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
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
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = deviceKey;
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
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
        new NonStrictExpectations()
        {
            {
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

        DeviceClientConfig actualConfig = Deencapsulation.getField(connection, "config");
        String actualHostName = Deencapsulation.getField(connection, "hostName");
        String actualUserName = Deencapsulation.getField(connection, "userName");
        ArrayList<AmqpsDeviceOperations> actualAmqpsDeviceOperationsList = Deencapsulation.getField(connection, "amqpsDeviceOperationsList");

        assertEquals(mockConfig, actualConfig);
        assertEquals(hostName + ":" + amqpPort, actualHostName);
        assertEquals(deviceId + "@sas." + hubName, actualUserName);
        assertEquals(amqpsDeviceOperationsList, actualAmqpsDeviceOperationsList);

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

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_002: [The constructor shall create a Proton reactor.]
    @Test
    public void constructorCreatesProtonReactor() throws IOException
    {
        baseExpectations();

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

        new Verifications()
        {
            {
                Proton.reactor(connection);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_003: [The constructor shall throw IOException if the Proton reactor creation failed.]
    @Test (expected = IOException.class)
    public void constructorCreatesProtonReactorThrows() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                Proton.reactor((AmqpsIotHubConnection)any);
                result = new IOException();
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
    }

    @Test
    public void constructorSetsHostNameCorrectlyWhenWebSocketsAreEnabled() throws IOException
    {
        baseExpectations();
        new NonStrictExpectations()
        {
            {
                mockConfig.isUseWebsocket();
                result = true;
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

        String actualHostName = Deencapsulation.getField(connection, "hostName");
        assertEquals(hostName + ":" + amqpWebSocketPort, actualHostName);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_007: [If the AMQPS connection is already open, the function shall do nothing.]
    @Test
    public void openDoesNothingIfTheConnectionIsAlreadyOpen() throws IOException, InterruptedException
    {
        baseExpectations();

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

        Deencapsulation.setField(connection, "state", State.OPEN);

        connection.open();

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockConfig, "getSasTokenAuthentication");
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

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

        connection.open();

        new Verifications()
        {
            {
                mockConfig.getSasTokenAuthentication().getRenewedSasToken();
                times = 1;
            }
        };
    }

    @Test (expected = IOException.class)
    public void openThrowsIfProtonReactorThrows() throws IOException, InterruptedException
    {
        // arrange
        baseExpectations();

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
        Deencapsulation.setField(connection, "reactor", null);

        new NonStrictExpectations()
        {
            {
                new IotHubReactor((Reactor) any);
                result = new IOException();
            }
        };

        // act
        connection.open();
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_009: [The function shall trigger the Reactor (Proton) to begin running.]
    @Test
    public void openTriggersProtonReactor(@Mocked final Reactor mockedReactor) throws IOException, InterruptedException
    {
        baseExpectations();

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

        Deencapsulation.setField(connection, "openLock", mockOpenLock);

        connection.open();

        new Verifications()
        {
            {
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

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

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

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

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

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_004: [The function shall IOException throws if the waitLock throws.]
    @Test(expected = IOException.class)
    public void closeThrowsIfWaitLockThrows() throws Exception
    {
        baseExpectations();

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
        Deencapsulation.setField(connection, "closeLock", mockCloseLock);

        new NonStrictExpectations()
        {
            {
                mockCloseLock.waitLock(anyLong);
                result = new InterruptedException();
            }
        };

        connection.close();
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_048 [If the AMQPS connection is already closed, the function shall do nothing.]
    @Test
    public void closeDoesNothingIfTheConnectionWasNeverOpened() throws InterruptedException, IOException
    {
        baseExpectations();

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

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
                mockSender.close();
                times = 0;
                mockReceiver.close();
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

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

        Deencapsulation.setField(connection, "state", State.OPEN);
        Deencapsulation.setField(connection, "session", mockSession);
        Deencapsulation.setField(connection, "connection", mockConnection);
        Deencapsulation.setField(connection, "executorService", mockExecutorService);

        connection.close();

        State actualState = Deencapsulation.getField(connection, "state");
        assertEquals(State.CLOSED, actualState);

        new Verifications()
        {
            {
                mockSession.close();
                times = 1;
                mockConnection.close();
                times = 1;
                mockExecutorService.shutdown();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_005: [The function shall throw IOException if the executor shutdown is interrupted.]
    @Test (expected = InterruptedException.class)
    public void closeThrowsIfShutdownThrows() throws IOException, InterruptedException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockReactorFuture.cancel(true);
                mockExecutorService.shutdown();
                mockExecutorService.shutdownNow();
                result = new InterruptedException();
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

        Deencapsulation.setField(connection, "state", State.OPEN);
        Deencapsulation.setField(connection, "session", mockSession);
        Deencapsulation.setField(connection, "connection", mockConnection);
        Deencapsulation.setField(connection, "executorService", mockExecutorService);

        connection.close();

        State actualState = Deencapsulation.getField(connection, "state");
        assertEquals(State.CLOSED, actualState);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "closeLinks");
                times = 1;
                mockSession.close();
                times = 1;
                mockConnection.close();
                times = 1;
                mockExecutorService.shutdown();
                times = 1;
                mockExecutorService.shutdownNow();
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

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

        Deencapsulation.setField(connection, "state", State.CLOSED);
        Deencapsulation.setField(connection, "linkCredit", 100);

        Integer expectedDeliveryHash = -1;
        Integer actualDeliveryHash = connection.sendMessage(Message.Factory.create(), MessageType.DEVICE_TELEMETRY);

        assertEquals(expectedDeliveryHash, actualDeliveryHash);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_015: [If the state of the connection is CLOSED or there is not enough
    // credit, the function shall return -1.]
    @Test
    public void sendMessageDoesNothingIfNotEnoughLinkCredit() throws IOException
    {
        baseExpectations();

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

        Deencapsulation.setField(connection, "state", State.OPEN);
        Deencapsulation.setField(connection, "linkCredit", -1);

        Integer expectedDeliveryHash = -1;
        Integer actualDeliveryHash = connection.sendMessage(Message.Factory.create(), MessageType.DEVICE_TELEMETRY);

        assertEquals(expectedDeliveryHash, actualDeliveryHash);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_016: [The function shall encode the message and copy the contents to the byte buffer.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_017: [The function shall set the delivery tag for the sender.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_006: [The function shall call sendMessageAndGetDeliveryHash on all device operation objects.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_021: [The function shall return the delivery hash.]
    @Test
    public void sendMessage() throws IOException
    {
        baseExpectations();

        final byte[] messageBytes = new byte[] {1, 2};

        new NonStrictExpectations()
        {
            {
                mockProtonMessage.encode((byte[]) any, anyInt, anyInt);
                mockSender.delivery((byte[]) any);
                result = mockDelivery;
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "sendMessageAndGetDeliveryHash", MessageType.DEVICE_TELEMETRY, messageBytes, anyInt, anyInt, messageBytes);
                result = mockAmqpsSendReturnValue;

                Deencapsulation.invoke(mockAmqpsSendReturnValue, "isDeliverySuccessful");
                result = true;
                Deencapsulation.invoke(mockAmqpsSendReturnValue, "getDeliveryHash");
                result = 42;
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

        Deencapsulation.setField(connection, "state", State.OPEN);
        Deencapsulation.setField(connection, "linkCredit", 100);

        Integer expectedDeliveryHash = 42;
        Integer actualDeliveryHash = connection.sendMessage(mockProtonMessage, MessageType.DEVICE_TELEMETRY);

        assertEquals(expectedDeliveryHash, actualDeliveryHash);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_007: [The function shall doubles the buffer if encode throws BufferOverflowException.]
    @Test
    public void sendMessageDoublesBufferIfEncodeThrowsBufferOverflowException() throws IOException
    {
        baseExpectations();

        final byte[] bytes = new byte[1024];
        final Integer deliveryHash = 24;
        new NonStrictExpectations()
        {
            {
                mockProtonMessage.encode(bytes, anyInt, anyInt);
                result = new BufferOverflowException();

                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "sendMessageAndGetDeliveryHash", MessageType.DEVICE_TELEMETRY, bytes, anyInt, anyInt, bytes);
                result = mockAmqpsSendReturnValue;
                Deencapsulation.invoke(mockAmqpsSendReturnValue, "isDeliverySuccessful");
                result = true;
                Deencapsulation.invoke(mockAmqpsSendReturnValue, "getDeliveryHash");
                result = deliveryHash;

            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

        Deencapsulation.setField(connection, "state", State.OPEN);
        Deencapsulation.setField(connection, "linkCredit", 100);

        Integer actualDeliveryHash = connection.sendMessage(mockProtonMessage, MessageType.DEVICE_TELEMETRY);

        assertEquals(deliveryHash, actualDeliveryHash);
        assertEquals(deliveryHash, actualDeliveryHash);
        new Verifications()
        {
            {
                mockProtonMessage.encode(bytes, anyInt, anyInt);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_022: [If the AMQPS Connection is closed, the function shall return false.]
    @Test
    public void sendMessageReturnsFalseIfConnectionIsClosed() throws IOException
    {
        baseExpectations();

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

        Deencapsulation.setField(connection, "state", State.CLOSED);

        Boolean expectedResult = false;
        Boolean actualResult = connection.sendMessageResult(mockAmqpsMessage, IotHubMessageResult.COMPLETE);

        assertEquals(expectedResult, actualResult);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_008: [The function shall return false if message acknowledge throws exception.]
    @Test
    public void sendMessageReturnsFalseIfAcknowledgeThrows() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.COMPLETE);
                result = new IllegalStateException();
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

        Deencapsulation.setField(connection, "state", State.OPEN);

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

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

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
                mockConnection.open();
                mockSession.open();
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
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
                mockConnection.open();
                times = 1;
                mockSession.open();
                times = 1;
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "openLinks", mockSession);
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
                mockConfig.isUseWebsocket();
                result = false;
                mockEvent.getConnection();
                result = mockConnection;
                mockConnection.getTransport();
                result = mockTransport;
                mockTransport.sasl();
                result = mockSasl;
                mockSasl.plain(anyString, anyString);
                mockSslDomain.setSslContext(mockSSLContext);

                mockSslDomain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
                mockTransport.ssl(mockSslDomain);
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

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
                mockSslDomain.setSslContext((SSLContext) any);
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
                mockSslDomain.setSslContext(mockSSLContext);

                mockSslDomain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
                mockTransportInternal.ssl(mockSslDomain);
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

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
                mockSslDomain.setSslContext((SSLContext) any);
                times = 1;
                mockSslDomain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
                times = 1;
                mockTransportInternal.ssl(mockSslDomain);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_005: [THe function sets the state to closed.]
    @Test
    public void onConnectionUnbound() throws IOException
    {
        baseExpectations();

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
        Deencapsulation.setField(connection, "state", State.OPEN);

        connection.onConnectionUnbound(mockEvent);
        State state = Deencapsulation.getField(connection, "state");

        assertTrue(state == State.CLOSED);
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

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

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

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_033: [The event handler shall set the current handler to handle the connection events.]
    @Test
    public void onReactorInitWithWebSockets() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockConfig.isUseWebsocket();
                result = true;
                mockEvent.getReactor();
                result = mockReactor;
                mockReactor.connectionToHost(anyString, anyInt, (Handler) any);
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

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


    // Test_SRS_AMQPSIOTHUBCONNECTION_12_007: [The function shall call notify lock on close lock.]
    // Test_SRS_AMQPSIOTHUBCONNECTION_12_008: [The function shall set the reactor member variable to null.]
    @Test
    public void onReactorFinalNoReconnect() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockConfig.isUseWebsocket();
                result = true;
                mockCloseLock.notifyLock();
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
        Deencapsulation.setField(connection, "closeLock", mockCloseLock);
        Deencapsulation.setField(connection, "reactor", mockReactor);
        Deencapsulation.setField(connection, "reconnectCall", false);
        Deencapsulation.setField(connection, "state", State.CLOSED);

        new MockUp<AmqpsIotHubConnection>()
        {
            @Mock
            void openAsync()
            {
                Deencapsulation.setField(connection, "state", State.OPEN);
            }
        };

        connection.onReactorFinal(mockEvent);

        Reactor reactor = Deencapsulation.getField(connection, "reactor");
        assertEquals(null, reactor);
        State state = Deencapsulation.getField(connection, "state");
        assertEquals(State.CLOSED, state);

        new Verifications()
        {
            {
                mockCloseLock.notifyLock();
                times = 1;

            }
        };
    }

    // Test_SRS_AMQPSIOTHUBCONNECTION_12_009: [The function shall call openAsync and disable reconnection if it is a reconnection attempt.]
    @Test
    public void onReactorFinalReconnect() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockConfig.isUseWebsocket();
                result = true;
                mockCloseLock.notifyLock();
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
        Deencapsulation.setField(connection, "closeLock", mockCloseLock);
        Deencapsulation.setField(connection, "reactor", mockReactor);
        Deencapsulation.setField(connection, "reconnectCall", true);
        Deencapsulation.setField(connection, "state", State.CLOSED);

        new MockUp<AmqpsIotHubConnection>()
        {
            @Mock
            void openAsync()
            {
                Deencapsulation.setField(connection, "state", State.OPEN);
            }
        };

        connection.onReactorFinal(mockEvent);

        Reactor reactor = Deencapsulation.getField(connection, "reactor");
        assertEquals(null, reactor);
        State state = Deencapsulation.getField(connection, "state");
        assertEquals(State.OPEN, state);

        new Verifications()
        {
            {
                mockCloseLock.notifyLock();
                times = 1;

            }
        };
    }

    // Test_SRS_AMQPSIOTHUBCONNECTION_12_010: [The function shall log the error if openAsync failed.]
    @Test
    public void onReactorFinalReconnectFailed() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockConfig.isUseWebsocket();
                result = true;
                mockCloseLock.notifyLock();
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
        Deencapsulation.setField(connection, "closeLock", mockCloseLock);
        Deencapsulation.setField(connection, "reactor", mockReactor);
        Deencapsulation.setField(connection, "reconnectCall", true);
        Deencapsulation.setField(connection, "state", State.CLOSED);

        new MockUp<AmqpsIotHubConnection>()
        {
            @Mock
            void openAsync() throws IOException
            {
                throw new IOException();
            }
        };

        connection.onReactorFinal(mockEvent);

        Reactor reactor = Deencapsulation.getField(connection, "reactor");
        assertEquals(null, reactor);
        State state = Deencapsulation.getField(connection, "state");
        assertEquals(State.CLOSED, state);

        new Verifications()
        {
            {
                mockCloseLock.notifyLock();
                times = 1;

            }
        };
    }


    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_015: [The function shall call getMessageFromReceiverLink on all device operation objects.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_050: [All the listeners shall be notified that a message was received from the server.]
    @Test
    public void onDeliveryReceive() throws IOException
    {
        baseExpectations();

        final String receiverLinkName = "receiver";
        new NonStrictExpectations()
        {
            {
                mockConfig.isUseWebsocket();
                result = true;
                mockEvent.getLink();
                result = mockReceiver;
                mockReceiver.getName();
                result = receiverLinkName;
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "getMessageFromReceiverLink", receiverLinkName);
                result = mockAmqpsMessage;
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(mockAmqpsDeviceTelemetry);
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
        connection.addListener(mockServerListener);
        connection.onDelivery(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "getMessageFromReceiverLink", receiverLinkName);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_038: [If this link is the Sender link and the event type is DELIVERY, the event handler shall get the Delivery (Proton) object from the event.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_039: [The event handler shall note the remote delivery state and use it and the Delivery (Proton) hash code to inform the AmqpsIotHubConnection of the message receipt.]
    @Test
    public void onDeliverySend(
//            @Mocked final ArrayList<AmqpsDeviceOperations> mockArrayListAmqpsDeviceOperations
    ) throws IOException
    {
        baseExpectations();
        final String receiverLinkName = "receiver";

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(mockAmqpsDeviceTelemetry);
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockLink;
                mockLink.getName();
                result = receiverLinkName;
                mockEvent.getType();
                result = Event.Type.DELIVERY;
                mockEvent.getDelivery();
                result = mockDelivery;
                mockDelivery.getRemoteState();
                result = Accepted.getInstance();
                mockServerListener.messageSent(anyInt, true);
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "getMessageFromReceiverLink", receiverLinkName);
                result = null;
            }
        };

        connection.addListener(mockServerListener);
        connection.onDelivery(mockEvent);

        new Verifications()
        {
            {
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
                result = mockSender;
                mockSender.getCredit();
                result = 100;
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
        connection.onLinkFlow(mockEvent);

        Integer expectedLinkCredit = 100;
        Integer actualLinkCredit = Deencapsulation.getField(connection, "linkCredit");

        assertEquals(expectedLinkCredit, actualLinkCredit);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                mockSender.getCredit();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_041: [The connection state shall be considered OPEN when the sender link is open remotely.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_99_001: [All server listeners shall be notified when that the connection has been established.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_21_051 [The open lock shall be notified when that the connection has been established.]
    @Test
    public void onLinkRemoteOpen() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockSender;
                mockSender.getName();
                result = "sender";
                mockServerListener.connectionEstablished();
                new ObjectLock();
                result = mockOpenLock;
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "getReceiverLinkTag");
                result = "sender";
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

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
                mockSender.getName();
                times = 1;
                mockServerListener.connectionEstablished();
                times = 1;
                mockOpenLock.notifyLock();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_042 [The event handler shall attempt to reconnect to the IoTHub.]
    @Test
    public void onLinkRemoteClose() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockSender;
                mockSender.getName();
                result = "sender";
                mockServerListener.connectionLost();
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "getReceiverLinkTag");
                result = "sender";
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

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
                mockSender.getName();
                times = 1;
                mockServerListener.connectionLost();
                times = 1;
            }
        };
    }


    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_016: [The function shall get the link from the event and call device operation objects with it.]
    @Test
    public void onLinkInit() throws IOException
    {
        baseExpectations();

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

        connection.onLinkInit(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "initLink", mockLink);
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

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

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

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_048: [The event handler shall attempt to reconnect to IoTHub.]
    @Test
    public void onTransportErrorReconnectionCounterRotate() throws IOException, InterruptedException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockServerListener.connectionLost();
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
        Deencapsulation.setField(connection, "currentReconnectionAttempt", Integer.MAX_VALUE);

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

        int currentReconnectionAttempt = Deencapsulation.getField(connection, "currentReconnectionAttempt");
        assertEquals(currentReconnectionAttempt, 1);
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
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getIotHubHostname();
                result = hostName;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = deviceKey;
                mockConfig.isUseWebsocket();
                result = false;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfDeviceKeyIsEmpty() throws IOException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = hostName;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = "";
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfDeviceKeyIsNull() throws IOException
    {
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = hostName;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = null;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

    }

    //Tests_SRS_AMQPSIOTHUBCONNECTION_34_043: [If the config is not using sas token authentication, this function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void openThrowsIfNotUsingSasTokenAuth() throws IOException, InterruptedException
    {
        //arrange
        baseExpectations();
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.X509_CERTIFICATE;
            }
        };

        ArrayList<AmqpsDeviceOperations> amqpsDeviceOperationsList = new ArrayList<AmqpsDeviceOperations>();
        amqpsDeviceOperationsList.add(Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, deviceId));
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, amqpsDeviceOperationsList);

        //act
        connection.open();
    }
}