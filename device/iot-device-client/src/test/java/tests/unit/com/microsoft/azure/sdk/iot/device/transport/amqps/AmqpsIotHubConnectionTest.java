/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.deps.ws.impl.WebSocketImpl;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.exceptions.UnauthorizedException;
import com.microsoft.azure.sdk.iot.device.net.IotHubUri;
import com.microsoft.azure.sdk.iot.device.transport.IotHubListener;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions.*;
import mockit.*;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.FlowController;
import org.apache.qpid.proton.reactor.Handshaker;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Unit tests for AmqpsIotHubConnection.
 * Coverage :
 * 100% method,
 * 93% line
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
    IotHubListener mockedIotHubListener;

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

    @Mocked
    AmqpsSessionManager mockAmqpsSessionManager;

    @Mocked
    IotHubSasTokenAuthenticationProvider mockIotHubSasTokenAuthenticationProvider;

    @Mocked
    com.microsoft.azure.sdk.iot.device.Message mockIoTMessage;

    @Mocked
    com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage mockIoTTransportMessage;

    @Mocked
    ErrorCondition mockedErrorCondition;

    @Mocked
    Symbol mockedSymbol;

    @Mocked
    ApplicationProperties mockedApplicationProperties;

    /*

    @Mocked
    Queue<DeviceClientConfig> mockedQueue;

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfConfigIsNull() throws IOException
    {
        new AmqpsIotHubConnection(null, 1);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfIoTHubHostNameNull() throws IOException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = null;
            }
        };
        new AmqpsIotHubConnection(mockConfig, 1);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfIoTHubHostNameEmpty() throws IOException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = "";
            }
        };
        new AmqpsIotHubConnection(mockConfig, 1);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfDeviceIdNull() throws IOException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = "xxx";
                mockConfig.getDeviceId();
                result = null;
            }
        };
        new AmqpsIotHubConnection(mockConfig, 1);
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
                mockConfig.getDeviceId();
                result = "";
            }
        };
        new AmqpsIotHubConnection(mockConfig, 1);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfIoTHubNameNull() throws IOException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = "xxx";
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getIotHubName();
                result = null;
            }
        };
        new AmqpsIotHubConnection(mockConfig, 1);
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
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getIotHubName();
                result = "";
            }
        };
        new AmqpsIotHubConnection(mockConfig, 1);
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

        new AmqpsIotHubConnection(mockConfig, 1);
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

        new AmqpsIotHubConnection(mockConfig, 1);
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

        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        DeviceClientConfig actualConfig = Deencapsulation.getField(connection, "deviceClientConfig");
        String actualHostName = Deencapsulation.getField(connection, "hostName");

        assertEquals(mockConfig, actualConfig);
        assertEquals(hostName + ":" + amqpPort, actualHostName);

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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

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

        new AmqpsIotHubConnection(mockConfig, 1);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_001: [The constructor shall initialize the AmqpsSessionManager member variable with the given config.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_017: [The constructor shall set the AMQP socket port using the configuration.]
    @Test
    public void constructorSetsHostNameCorrectlyWhenWebSocketsAreEnabled() throws IOException
    {
        // arrange
        baseExpectations();
        new NonStrictExpectations()
        {
            {
                mockConfig.isUseWebsocket();
                result = true;
                new AmqpsSessionManager(mockConfig);
                result = mockAmqpsSessionManager;
            }
        };

        // act
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);


        // assert
        String actualHostName = Deencapsulation.getField(connection, "hostName");
        assertEquals(hostName + ":" + amqpWebSocketPort, actualHostName);

        new Verifications()
        {
            {
                new AmqpsSessionManager(mockConfig);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_001: [The constructor shall initialize the AmqpsSessionManager member variable with the given config.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_017: [The constructor shall set the AMQP socket port using the configuration.]
    @Test
    public void constructorSetsHostNameCorrectlyWhenWebSocketsAreDisabled() throws IOException
    {
        // arrange
        baseExpectations();
        new NonStrictExpectations()
        {
            {
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        // act
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        // assert
        String actualHostName = Deencapsulation.getField(connection, "hostName");
        assertEquals(hostName + ":" + amqpPort, actualHostName);

        new Verifications()
        {
            {
                new AmqpsSessionManager(mockConfig);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_007: [If the AMQPS connection is already open, the function shall do nothing.]
    @Test
    public void openDoesNothingIfTheConnectionIsAlreadyOpen() throws IOException, InterruptedException
    {
        baseExpectations();

        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        Deencapsulation.setField(connection, "state", State.OPEN);

        connection.open(mockedQueue);

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

        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        connection.open(mockedQueue);

        new Verifications()
        {
            {
                mockConfig.getSasTokenAuthentication().getRenewedSasToken();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_018: [The function shall do nothing if the deviceClientConfig parameter is null.]
    @Test
    public void addDeviceOperationSessionDoesNothing() throws IOException, InterruptedException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        // act
        connection.addDeviceOperationSession(null);

        // assert
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "addDeviceOperationSession", mockConfig);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_019: [The function shall call AmqpsSessionManager.addDeviceOperationSession with the given deviceClientConfig.]
    @Test
    public void addDeviceOperationSessionSuccess() throws IOException, InterruptedException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        // act
        connection.addDeviceOperationSession(mockConfig);

        // assert
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "addDeviceOperationSession", mockConfig);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_057: [The function shall call the connection to authenticate.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_058: [The function shall call the connection to open device client links.]
    @Test
    public void openCallsAuthenticateAndOpenLinks() throws IOException, InterruptedException
    {
        // arrange
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
        Deencapsulation.setField(connection, "openLock", mockOpenLock);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        // act
        connection.open(mockedQueue);

        // assert
        new Verifications()
        {
            {
                mockConfig.getSasTokenAuthentication().getRenewedSasToken();
                times = 1;
                mockOpenLock.waitLock(anyLong);
                times = 1;

                connection.authenticate();
                times = 2;
                connection.openLinks();
                times = 2;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_011: [If any exception is thrown while attempting to trigger the reactor, the function shall closeNow the connection and throw an IOException.]
    @Test (expected = IOException.class)
    public void openThrowsIfProtonReactorThrows() throws IOException, InterruptedException
    {
        // arrange
        baseExpectations();

        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
        Deencapsulation.setField(connection, "reactor", null);

        new NonStrictExpectations()
        {
            {
                new IotHubReactor((Reactor) any);
                result = new IOException();
            }
        };

        // act
        connection.open(mockedQueue);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_009: [The function shall trigger the Reactor (Proton) to begin running.]
    @Test
    public void openTriggersProtonReactor(@Mocked final Reactor mockedReactor) throws IOException, InterruptedException
    {
        baseExpectations();

        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        Deencapsulation.setField(connection, "openLock", mockOpenLock);

        connection.open(mockedQueue);

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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        Deencapsulation.setField(connection, "openLock", mockOpenLock);

        connection.open(mockedQueue);

        new Verifications()
        {
            {
                mockOpenLock.waitLock(anyLong);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_011: [If any exception is thrown while attempting to trigger
    // the reactor, the function shall closeNow the connection and throw an IOException.]
    @Test(expected = IOException.class)
    public void openFailsIfConnectionIsNotOpenedInTime() throws Exception
    {
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        Deencapsulation.setField(connection, "openLock", mockOpenLock);

        new NonStrictExpectations()
        {
            {
                mockOpenLock.waitLock(anyLong);
                result = new InterruptedException();
            }
        };

        connection.open(mockedQueue);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_020: [The function shall do nothing if the authentication is already open.]
    @Test
    public void authenticateDoesNothing() throws IOException, InterruptedException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                result = false;
            }
        };

        // act
        connection.authenticate();

        // assert
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                times = 1;
                Deencapsulation.invoke(mockAmqpsSessionManager, "authenticate");
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_021: [The function shall call AmqpsSessionManager.authenticate.]
    @Test
    public void authenticateSuccess() throws IOException, InterruptedException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                result = true;
            }
        };

        // act
        connection.authenticate();

        // assert
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                times = 1;
                Deencapsulation.invoke(mockAmqpsSessionManager, "authenticate");
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_022: [The function shall do nothing if the authentication is already open.]
    @Test
    public void openLinksDoesNothing() throws IOException, InterruptedException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                result = false;
            }
        };

        // act
        connection.openLinks();

        // assert
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                times = 1;
                Deencapsulation.invoke(mockAmqpsSessionManager, "openDeviceOperationLinks");
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_023: [The function shall call AmqpsSessionManager.openDeviceOperationLinks.]
    @Test
    public void openLinksSuccess() throws IOException, InterruptedException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                result = true;
            }
        };

        // act
        connection.openLinks();

        // assert
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                times = 1;
                Deencapsulation.invoke(mockAmqpsSessionManager, "openDeviceOperationLinks");
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_004: [The function shall IOException throws if the waitLock throws.]
    @Test(expected = IOException.class)
    public void closeThrowsIfWaitLockThrows() throws Exception
    {
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

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
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_013: [The function shall closeNow the AmqpsSessionManager and the AMQP connection.]
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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        Deencapsulation.setField(connection, "state", State.OPEN);
        Deencapsulation.setField(connection, "connection", mockConnection);
        Deencapsulation.setField(connection, "executorService", mockExecutorService);

        connection.close();

        State actualState = Deencapsulation.getField(connection, "state");
        assertEquals(State.CLOSED, actualState);

        new Verifications()
        {
            {
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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        Deencapsulation.setField(connection, "state", State.OPEN);
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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        Deencapsulation.setField(connection, "state", State.CLOSED);
        Deencapsulation.setField(connection, "linkCredit", 100);

        Integer expectedDeliveryHash = -1;
        Integer actualDeliveryHash = connection.sendMessage(Message.Factory.create(), MessageType.DEVICE_TELEMETRY, mockConnectionString);

        assertEquals(expectedDeliveryHash, actualDeliveryHash);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_015: [If the state of the connection is CLOSED or there is not enough
    // credit, the function shall return -1.]
    @Test
    public void sendMessageDoesNothingIfNotEnoughLinkCredit() throws IOException
    {
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        Deencapsulation.setField(connection, "state", State.OPEN);
        Deencapsulation.setField(connection, "linkCredit", -1);

        Integer expectedDeliveryHash = -1;
        Integer actualDeliveryHash = connection.sendMessage(Message.Factory.create(), MessageType.DEVICE_TELEMETRY, mockConnectionString);

        assertEquals(expectedDeliveryHash, actualDeliveryHash);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_024: [The function shall call AmqpsSessionManager.sendMessage with the given parameters.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_021: [The function shall return the delivery hash.]
    @Test
    public void sendMessage() throws IOException
    {
        // arrange
        baseExpectations();

        final byte[] messageBytes = new byte[] {1, 2};
        final Integer expectedDeliveryHash = 42;

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, mockConnectionString);
                result = expectedDeliveryHash;
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);
        Deencapsulation.setField(connection, "state", State.OPEN);
        Deencapsulation.setField(connection, "linkCredit", 100);

        // act
        Integer actualDeliveryHash = connection.sendMessage(mockProtonMessage, MessageType.DEVICE_TELEMETRY, mockConnectionString);

        // assert
        assertEquals(expectedDeliveryHash, actualDeliveryHash);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, mockConnectionString);
                times = 1;
            }
        };

    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_022: [If the AMQPS Connection is closed, the function shall return false.]
    @Test
    public void sendMessageReturnsFalseIfConnectionIsClosed() throws IOException
    {
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

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

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_011: [The function shall call notify lock on close lock.]
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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
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
        assertEquals(State.CLOSED, state);

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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
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

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_025: [The event handler shall get the Connection (Proton) object from the event handler and set the host name on the connection.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_029: [The event handler shall open the connection.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_009: [The event handler shall call the amqpsSessionManager.onConnectionInit function with the connection.]
    @Test
    public void onConnectionInit() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getConnection();
                result = mockConnection;
                mockConnection.open();
                mockSession.open();
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
        connection.onConnectionInit(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getConnection();
                times = 1;
                mockConnection.setHostname(hostName + ":" + amqpPort);
                times = 1;
                mockConnection.open();
                times = 1;
                Deencapsulation.invoke(mockAmqpsSessionManager, "onConnectionInit", mockConnection);
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
                mockConfig.getSasTokenAuthentication();
                result = mockIotHubSasTokenAuthenticationProvider;
                mockIotHubSasTokenAuthenticationProvider.getSSLContext();
                result =  mockSSLContext;
                mockEvent.getConnection();
                result = mockConnection;
                mockConnection.getTransport();
                result = mockTransport;
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
        Deencapsulation.setField(connection, "useWebSockets", false);

        connection.onConnectionBound(mockEvent);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "onConnectionBound", mockTransport);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_030: [The event handler shall get the Transport (Proton) object from the event.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_25_049: [If websocket enabled the event handler shall configure the transport layer for websocket.]
    @Test
    public void onConnectionBoundWebSockets() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockConfig.isUseWebsocket();
                result = false;
                mockConfig.getSasTokenAuthentication();
                result = mockIotHubSasTokenAuthenticationProvider;
                mockIotHubSasTokenAuthenticationProvider.getSSLContext();
                result =  mockSSLContext;
                mockEvent.getConnection();
                result = mockConnection;
                mockConnection.getTransport();
                result = mockTransportInternal;
                new WebSocketImpl();
                result = mockWebSocket;
                mockWebSocket.configure(anyString, anyString, anyInt, anyString, (Map<String, String>) any, (WebSocketHandler) any);
                mockTransportInternal.addTransportLayer(mockWebSocket);
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        Deencapsulation.setField(connection, "useWebSockets", true);

        connection.onConnectionBound(mockEvent);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "onConnectionBound", mockTransportInternal);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_005: [THe function sets the state to closed.]
    @Test
    public void onConnectionUnbound() throws IOException
    {
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
        Deencapsulation.setField(connection, "state", State.OPEN);

        connection.onConnectionUnbound(mockEvent);
        State state = Deencapsulation.getField(connection, "state");

        assertTrue(state == State.CLOSED);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_015: [The function shall call AmqpsSessionManager.getMessageFromReceiverLink.]
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
                Deencapsulation.invoke(mockAmqpsSessionManager, "getMessageFromReceiverLink", receiverLinkName);
                result = mockAmqpsMessage;
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
        connection.addListener(mockServerListener);
        connection.onDelivery(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                Deencapsulation.invoke(mockAmqpsSessionManager, "getMessageFromReceiverLink", receiverLinkName);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_038: [If this link is the Sender link and the event type is DELIVERY, the event handler shall get the Delivery (Proton) object from the event.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_039: [The event handler shall note the remote delivery state and use it and the Delivery (Proton) hash code to inform the AmqpsIotHubConnection of the message receipt.]
    @Test
    public void onDeliverySend(
    ) throws IOException
    {
        baseExpectations();
        final String receiverLinkName = "receiver";

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

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
                mockedIotHubListener.messageSent(anyInt, true);
                Deencapsulation.invoke(mockAmqpsSessionManager, "getMessageFromReceiverLink", receiverLinkName);
                result = null;
            }
        };

        connection.addListener(mockedIotHubListener);
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
                mockedIotHubListener.messageSent(mockDelivery.hashCode(), true);
                times = 1;
                mockDelivery.free();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_016: [The function shall get the link from the event and call device operation objects with it.]
    @Test
    public void onLinkInit() throws IOException
    {
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        connection.onLinkInit(mockEvent);

        new Verifications()
        {
            {
                mockEvent.getLink();
                times = 1;
                Deencapsulation.invoke(mockAmqpsSessionManager, "onLinkInit", mockLink);
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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
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
    // Tests_SRS_AMQPSIOTHUBCONNECTION_21_051: [The open lock shall be notified when that the connection has been established.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_052: [The function shall call AmqpsSessionManager.onLinkRemoteOpen with the given link.]
    @Test
    public void onLinkRemoteOpen() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                new ObjectLock();
                result = mockOpenLock;
                Deencapsulation.invoke(mockAmqpsSessionManager, "onLinkRemoteOpen", mockEvent);
                result = true;
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

        connection.addListener(mockedIotHubListener);
        connection.onLinkRemoteOpen(mockEvent);

        State expectedState = State.OPEN;
        State actualState = Deencapsulation.getField(connection, "state");

        assertEquals(expectedState, actualState);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "onLinkRemoteOpen", mockEvent);
                times = 1;
                mockedIotHubListener.connectionEstablished();
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
                mockedIotHubListener.connectionLost();
                Deencapsulation.invoke(mockAmqpsSessionManager, "isLinkFound", "sender");
                result = true;
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

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

        connection.addListener(mockedIotHubListener);
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
                mockedIotHubListener.connectionLost();
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
                mockedIotHubListener.connectionLost();
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);

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

        connection.addListener(mockedIotHubListener);
        connection.onTransportError(mockEvent);

        assertEquals(true, closeAsyncCalled[0]);
        assertEquals(false, openAsyncCalled[0]);

        new Verifications()
        {
            {
                mockedIotHubListener.connectionLost();
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
                mockedIotHubListener.connectionLost();
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
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

        connection.addListener(mockedIotHubListener);
        connection.onTransportError(mockEvent);

        int currentReconnectionAttempt = Deencapsulation.getField(connection, "currentReconnectionAttempt");
        assertEquals(currentReconnectionAttempt, 0);
        assertEquals(true, closeAsyncCalled[0]);
        assertEquals(false, openAsyncCalled[0]);

        new Verifications()
        {
            {
                mockedIotHubListener.connectionLost();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_054: [The function shall add the given listener to the listener list.]
    @Test
    public void addListenerSuccess() throws IOException, InterruptedException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
        Deencapsulation.setField(connection, "listeners", mockServerListenerList);

        // act
        connection.addListener(mockedIotHubListener);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockServerListenerList, "add", mockedIotHubListener);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_055: [The function shall call AmqpsSessionManager.convertToProton with the given message.]
    @Test
    public void convertToProton() throws IOException, InterruptedException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
        Deencapsulation.setField(connection, "listeners", mockServerListenerList);

        // act
        Deencapsulation.invoke(connection, "convertToProton", mockIoTMessage);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "convertToProton", mockIoTMessage);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_056: [*The function shall call AmqpsSessionManager.convertFromProton with the given message. ]
    @Test
    public void convertFromProton() throws IOException, InterruptedException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        // act
        Deencapsulation.invoke(connection, "convertFromProton", mockAmqpsMessage, mockConfig);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "convertFromProton", mockAmqpsMessage, mockConfig);
                times = 1;
            }
        };
    }
    */

    //Tests_SRS_AMQPSIOTHUBCONNECTION_34_060 [If the provided event object's transport holds an error condition object, this function shall report the associated TransportException to this object's listeners.]
    @Test
    public void OnTransportErrorReportsErrorCodeIfPresent() throws IOException, TransportException
    {
        //arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
        connection.addListener(mockedIotHubListener);
        Deencapsulation.setField(connection, "reactor", mockReactor);
        new NonStrictExpectations()
        {
            {
                mockEvent.getTransport();
                result = mockTransport;
                mockTransport.getCondition();
                result = mockedErrorCondition;
                mockedErrorCondition.getCondition();
                result = mockedSymbol;
                mockedSymbol.toString();
                result = AmqpSessionWindowViolationException.errorCode;
            }
        };

        //act
        connection.onTransportError(mockEvent);

        //assert
        new Verifications()
        {
            {
                mockedIotHubListener.onConnectionLost((AmqpSessionWindowViolationException) any);
                times = 1;
            }
        };
    }

    //Tests_SRS_AMQPSIOTHUBCONNECTION_34_061 [If the provided event object's transport holds a remote error condition object, this function shall report the associated TransportException to this object's listeners.]
    @Test
    public void OnLinkRemoteCloseReportsErrorCodeIfPresent() throws IOException, TransportException
    {
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
        connection.addListener(mockedIotHubListener);
        new NonStrictExpectations()
        {
            {
                mockEvent.getTransport();
                result = mockTransport;
                mockTransport.getRemoteCondition();
                result = mockedErrorCondition;
                mockedErrorCondition.getCondition();
                result = mockedSymbol;
                mockedSymbol.toString();
                result = AmqpLinkRedirectException.errorCode;
            }
        };

        //act
        connection.onLinkRemoteClose(mockEvent);

        //assert
        new Verifications()
        {
            {
                mockedIotHubListener.onConnectionLost((AmqpLinkRedirectException) any);
                times = 1;
            }
        };
    }

    //Tests_SRS_AMQPSIOTHUBCONNECTION_34_089: [If an amqp message can be received from the receiver link, and that amqp message contains a status code that is not 200 or 204, this function shall notify this object's listeners that that message was received and provide the status code's mapped exception.]
    @Test
    public void onDeliveryNotifiesListenerOfErrorCodes() throws IOException, TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
        connection.addListener(mockedIotHubListener);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);
        final Map<String, Object> applicationPropertiesMap = new HashMap<>();
        applicationPropertiesMap.put("status-code", 401);
        applicationPropertiesMap.put("status-description", "You can't do that");
        new NonStrictExpectations()
        {
            {
                mockEvent.getLink().getName();
                result = "";
                Deencapsulation.invoke(mockAmqpsSessionManager, "getMessageFromReceiverLink", "");
                result = mockAmqpsMessage;
                mockAmqpsMessage.getApplicationProperties();
                result = mockedApplicationProperties;
                mockedApplicationProperties.getValue();
                result = applicationPropertiesMap;
                new IotHubTransportMessage((byte[]) any, (MessageType) any);
                result = mockIoTMessage;
            }
        };

        //act
        connection.onDelivery(mockEvent);

        //assert
        new Verifications()
        {
            {
                mockedIotHubListener.onMessageReceived(mockIoTMessage, (UnauthorizedException) any);
                times = 1;
            }
        };
    }

    //Tests_SRS_AMQPSIOTHUBCONNECTION_34_090: [If an amqp message can be received from the receiver link, and that amqp message contains a status code that is 200 or 204, this function shall notify this object's listeners that that that message was received with a null exception.]
    @Test
    public void onDeliveryNotifiesListenerOf200Code() throws IOException, TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
        connection.addListener(mockedIotHubListener);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);
        final Map<String, Object> applicationPropertiesMap = new HashMap<>();
        applicationPropertiesMap.put("status-code", 200);
        new NonStrictExpectations()
        {
            {
                mockEvent.getLink().getName();
                result = "";
                Deencapsulation.invoke(mockAmqpsSessionManager, "getMessageFromReceiverLink", "");
                result = mockAmqpsMessage;
                mockAmqpsMessage.getApplicationProperties();
                result = mockedApplicationProperties;
                mockedApplicationProperties.getValue();
                result = applicationPropertiesMap;
                new IotHubTransportMessage((byte[]) any, (MessageType) any);
                result = mockIoTMessage;
            }
        };

        //act
        connection.onDelivery(mockEvent);

        //assert
        new Verifications()
        {
            {
                mockedIotHubListener.onMessageReceived(mockIoTMessage, null);
                times = 1;
            }
        };
    }

    //Tests_SRS_AMQPSIOTHUBCONNECTION_34_091: [If an amqp message can be received from the receiver link, and that amqp message contains no status code, this function shall notify this object's listeners that that message was received with a null exception.]
    @Test
    public void onDeliveryNotifiesListenerOfMessageReceivedWithNoStatusCode() throws IOException, TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
        connection.addListener(mockedIotHubListener);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);
        final Map<String, Object> applicationPropertiesMap = new HashMap<>();
        new NonStrictExpectations()
        {
            {
                mockEvent.getLink().getName();
                result = "";
                Deencapsulation.invoke(mockAmqpsSessionManager, "getMessageFromReceiverLink", "");
                result = mockAmqpsMessage;
                mockAmqpsMessage.getApplicationProperties();
                result = mockedApplicationProperties;
                mockedApplicationProperties.getValue();
                result = applicationPropertiesMap;
                new IotHubTransportMessage((byte[]) any, (MessageType) any);
                result = mockIoTMessage;
            }
        };

        //act
        connection.onDelivery(mockEvent);

        //assert
        new Verifications()
        {
            {
                mockedIotHubListener.onMessageReceived(mockIoTMessage, null);
                times = 1;
            }
        };
    }

    //Tests_SRS_AMQPSIOTHUBCONNECTION_34_092: [If an amqp message can be received from the receiver link, and that amqp message contains no application properties, this function shall notify this object's listeners that that message was received with a null exception.]
    @Test
    public void onDeliveryNotifiesListenerOfMessageReceivedWithNoApplicationProperties() throws IOException, TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
        connection.addListener(mockedIotHubListener);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);
        new NonStrictExpectations()
        {
            {
                mockEvent.getLink().getName();
                result = "";
                Deencapsulation.invoke(mockAmqpsSessionManager, "getMessageFromReceiverLink", "");
                result = mockAmqpsMessage;
                mockAmqpsMessage.getApplicationProperties();
                result = mockedApplicationProperties;
                mockedApplicationProperties.getValue();
                result = null;
                new IotHubTransportMessage((byte[]) any, (MessageType) any);
                result = mockIoTTransportMessage;
            }
        };

        //act
        connection.onDelivery(mockEvent);

        //assert
        new Verifications()
        {
            {
                mockedIotHubListener.onMessageReceived(mockIoTTransportMessage, null);
                times = 1;
            }
        };
    }

    //Tests_SRS_AMQPSIOTHUBCONNECTION_34_093: [If an amqp message can be received from the receiver link, and that amqp message contains a status code, but that status code cannot be parsed to an integer, this function shall notify this object's listeners that that message was received with a null exception.]
    @Test
    public void onDeliveryNotifiesListenerEvenIfStatusCodeCannotBeParsedToInteger() throws IOException, TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig, 1);
        connection.addListener(mockedIotHubListener);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);
        final Map<String, Object> applicationPropertiesMap = new HashMap<>();
        applicationPropertiesMap.put("status-code", "This is not a valid status code");
        new NonStrictExpectations()
        {
            {
                mockEvent.getLink().getName();
                result = "";
                Deencapsulation.invoke(mockAmqpsSessionManager, "getMessageFromReceiverLink", "");
                result = mockAmqpsMessage;
                mockAmqpsMessage.getApplicationProperties();
                result = mockedApplicationProperties;
                mockedApplicationProperties.getValue();
                result = applicationPropertiesMap;
                new IotHubTransportMessage((byte[]) any, (MessageType) any);
                result = mockIoTMessage;
            }
        };

        //act
        connection.onDelivery(mockEvent);

        //assert
        new Verifications()
        {
            {
                mockedIotHubListener.onMessageReceived(mockIoTMessage, null);
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
}