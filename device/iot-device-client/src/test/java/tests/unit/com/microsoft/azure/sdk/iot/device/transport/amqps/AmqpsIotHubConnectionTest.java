/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.deps.ws.WebSocketHandler;
import com.microsoft.azure.sdk.iot.deps.ws.impl.WebSocketImpl;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubServiceException;
import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.net.IotHubUri;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.device.transport.IotHubListener;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions.*;
import mockit.*;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.message.impl.MessageImpl;
import org.apache.qpid.proton.reactor.FlowController;
import org.apache.qpid.proton.reactor.Handshaker;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.Assert;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;


/**
 * Unit tests for AmqpsIotHubConnection.
 * Coverage :
 * 94% method,
 * 88% line
 */

public class AmqpsIotHubConnectionTest {

    final String hostName = "test.host.name";
    final String hubName = "test.iothub";
    final String deviceId = "test-deviceId";
    final String deviceKey = "test-devicekey?&test";
    final String amqpPort = "5671";
    final String amqpWebSocketPort = "443";

    @Mocked
    ProtocolException mockedProtocolException;

    @Mocked
    TransportException mockedTransportException;

    @Mocked
    AmqpConnectionThrottledException mockedAmqpConnectionThrottledException;

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
    protected ScheduledExecutorService mockScheduledExecutorService;

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
    CountDownLatch mockLatch;

    @Mocked
    CountDownLatch mockCloseLatch;

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
    IotHubTransportMessage mockedTransportMessage;

    @Mocked
    AmqpsConvertFromProtonReturnValue mockedAmqpsConvertFromProtonReturnValue;

    @Mocked
    AmqpsConvertToProtonReturnValue mockedAmqpsConvertToProtonReturnValue;

    @Mocked
    ErrorCondition mockedErrorCondition;

    @Mocked
    Symbol mockedSymbol;

    @Mocked
    ApplicationProperties mockedApplicationProperties;

    @Mocked
    Queue<DeviceClientConfig> mockedQueue;

    @Mocked
    Rejected mockedRejected;

    @Mocked
    Received mockedReceived;

    @Mocked
    Modified mockedModified;

    @Mocked
    Released mockedReleased;

    @Mocked
    MessageImpl mockedMessageImpl;

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfConfigIsNull() throws TransportException
    {
        new AmqpsIotHubConnection(null);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfIoTHubHostNameNull() throws TransportException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = null;
            }
        };
        new AmqpsIotHubConnection(mockConfig);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfIoTHubHostNameEmpty() throws TransportException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = "";
            }
        };

        //act
        new AmqpsIotHubConnection(mockConfig);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfDeviceIdNull() throws TransportException
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
        new AmqpsIotHubConnection(mockConfig);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfDeviceIdIsEmpty() throws TransportException
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
        new AmqpsIotHubConnection(mockConfig);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfIoTHubNameNull() throws TransportException
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
        new AmqpsIotHubConnection(mockConfig);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
    // any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfHubNameIsEmpty() throws TransportException
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
        new AmqpsIotHubConnection(mockConfig);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_002: [The constructor shall save the configuration into private member variables.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_003: [The constructor shall initialize the sender and receiver
    // endpoint private member variables using the send/receiveEndpointFormat constants and device id.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_004: [The constructor shall initialize a new Handshaker
    // (Proton) object to handle communication handshake.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_005: [The constructor shall initialize a new FlowController
    // (Proton) object to handle communication flow.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_006: [The constructor shall set its state to DISCONNECTED.]
    @Test
    public void constructorCopiesAllData() throws TransportException
    {
        baseExpectations();
        new NonStrictExpectations()
        {
            {
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);

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

        IotHubConnectionStatus actualState = Deencapsulation.getField(connection, "state");
        assertEquals(IotHubConnectionStatus.DISCONNECTED, actualState);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_003: [The constructor shall throw TransportException if the Proton reactor creation failed.]
    @Test (expected = TransportException.class)
    public void constructorCreatesProtonReactorThrows() throws TransportException, IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                Proton.reactor((AmqpsIotHubConnection)any);
                result = new IOException();
            }
        };

        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);

        //act
        connection.open(mockedQueue, mockScheduledExecutorService);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_001: [The constructor shall initialize the AmqpsSessionManager member variable with the given config.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_017: [The constructor shall set the AMQP socket port using the configuration.]
    @Test
    public void constructorSetsHostNameCorrectlyWhenWebSocketsAreEnabled() throws TransportException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = hostName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.isUseWebsocket();
                result = true;
            }
        };

        // act
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);


        // assert
        String actualHostName = Deencapsulation.getField(connection, "hostName");
        assertEquals(hostName + ":" + amqpWebSocketPort, actualHostName);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_001: [The constructor shall initialize the AmqpsSessionManager member variable with the given config.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_017: [The constructor shall set the AMQP socket port using the configuration.]
    @Test
    public void constructorSetsHostNameCorrectlyWhenWebSocketsAreDisabled() throws TransportException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = hostName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        // act
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);

        // assert
        String actualHostName = Deencapsulation.getField(connection, "hostName");
        assertEquals(hostName + ":" + amqpPort, actualHostName);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_007: [If the AMQPS connection is already open, the function shall do nothing.]
    @Test
    public void openDoesNothingIfTheConnectionIsAlreadyOpen() throws TransportException
    {
        baseExpectations();

        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        connection.setListener(mockedIotHubListener);

        Deencapsulation.setField(connection, "state", IotHubConnectionStatus.CONNECTED);

        connection.open(mockedQueue, mockScheduledExecutorService);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockConfig, "getSasTokenAuthentication");
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_018: [The function shall do nothing if the deviceClientConfig parameter is null.]
    @Test
    public void addDeviceOperationSessionDoesNothing() throws TransportException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);

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
    public void addDeviceOperationSessionSuccess() throws TransportException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

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
    public void openCallsAuthenticateAndOpenLinks() throws TransportException, InterruptedException
    {
        // arrange
        baseExpectations();
        final CountDownLatch closeLatch = new CountDownLatch(1);

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        connection.setListener(mockedIotHubListener);

        new Expectations()
        {
            {
                new CountDownLatch(anyInt);
                result = closeLatch;

                new CountDownLatch(anyInt);
                result = mockLatch;

                new AmqpsSessionManager(mockConfig, (ScheduledExecutorService)any);
                result = mockAmqpsSessionManager;

                mockLatch.await(anyLong, TimeUnit.MILLISECONDS);

                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                result = true;

                mockAmqpsSessionManager.authenticate();

                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                result = true;

                mockAmqpsSessionManager.openDeviceOperationLinks(MessageType.DEVICE_TELEMETRY);

                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                result = true;

                Deencapsulation.invoke(mockAmqpsSessionManager, "areAllLinksOpen");
                result = true;
            }
        };

        // act
        try
        {
            connection.open(mockedQueue, mockScheduledExecutorService);
        }
        catch (TransportException e)
        {
            //exception will be thrown, but we aren't testing for what it is nor do we care that it threw
        }
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_34_062: [If, after attempting to open the connection, this
    // object has a saved exception, this function shall throw that saved exception.]
    @Test (expected = AmqpConnectionForcedException.class)
    public void openThrowsSavedExceptionsIfAnyExceptionsSavedDuringOpen() throws TransportException, InterruptedException
    {
        // arrange
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "savedException", new AmqpConnectionForcedException());
        connection.setListener(mockedIotHubListener);
        final CountDownLatch closeLatch = new CountDownLatch(1);

        new Expectations()
        {
            {
                new CountDownLatch(anyInt);
                result = closeLatch;

                new CountDownLatch(anyInt);
                result = mockLatch;

                new AmqpsSessionManager(mockConfig, (ScheduledExecutorService)any);
                result = mockAmqpsSessionManager;

                mockLatch.await(anyLong, TimeUnit.MILLISECONDS);

                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                result = true;

                mockAmqpsSessionManager.authenticate();

                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                result = true;

                mockAmqpsSessionManager.openDeviceOperationLinks(MessageType.DEVICE_TELEMETRY);

                Deencapsulation.getField(connection, "savedException");
                result = new AmqpConnectionForcedException();
            }
        };

        // act
        connection.open(mockedQueue, mockScheduledExecutorService);
    }


    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_011: [If any exception is thrown while attempting to trigger the reactor, the function shall closeNow the connection and throw an IOException.]
    @Test (expected = IOException.class)
    public void openThrowsIfProtonReactorThrows() throws TransportException
    {
        // arrange
        baseExpectations();

        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "reactor", null);

        new NonStrictExpectations()
        {
            {
                new IotHubReactor((Reactor) any);
                result = new IOException();
            }
        };

        // act
        connection.open(mockedQueue, mockScheduledExecutorService);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_009: [The function shall trigger the Reactor (Proton) to begin running.]
    @Test
    public void openTriggersProtonReactor(@Mocked final Reactor mockedReactor) throws TransportException, InterruptedException
    {
        //arrange
        baseExpectations();
        final CountDownLatch closeLatch = new CountDownLatch(1);

        new NonStrictExpectations()
        {
            {
                new CountDownLatch(anyInt);
                result = closeLatch;

                new CountDownLatch(anyInt);
                result = mockLatch;

                new AmqpsSessionManager(mockConfig, (ScheduledExecutorService)any);
                result = mockAmqpsSessionManager;

                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                result = true;

                Deencapsulation.invoke(mockAmqpsSessionManager, "areAllLinksOpen");
                result = true;
            }
        };

        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        connection.setListener(mockedIotHubListener);
        Deencapsulation.setField(connection, "openLatch", mockLatch);

        //act
        try
        {
            connection.open(mockedQueue, mockScheduledExecutorService);
        }
        catch (TransportException e)
        {
            //exception will be thrown, but this unit test does not care
        }

        //assert
        new Verifications()
        {
            {
                new IotHubReactor((Reactor)any);
                times = 1;
                mockLatch.await(anyLong, TimeUnit.MILLISECONDS);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_010: [The function shall wait for the reactor to be ready and for
    // enough link credit to become available.]
    @Test
    public void openWaitsForReactorToBeReadyAndForEnoughLinkCreditToBeAvailable() throws TransportException, InterruptedException
    {
        //arrange
        baseExpectations();
        final CountDownLatch closeLatch = new CountDownLatch(1);

        new NonStrictExpectations()
        {
            {
                new CountDownLatch(anyInt);
                result = closeLatch;

                new CountDownLatch(anyInt);
                result = mockLatch;

                new AmqpsSessionManager(mockConfig, (ScheduledExecutorService)any);
                result = mockAmqpsSessionManager;

                mockLatch.await(anyLong, TimeUnit.MILLISECONDS);

                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                result = true;

                Deencapsulation.invoke(mockAmqpsSessionManager, "areAllLinksOpen");
                result = true;
            }
        };
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        connection.setListener(mockedIotHubListener);
        Deencapsulation.setField(connection, "openLatch", mockLatch);

        //act
        try
        {
            connection.open(mockedQueue, mockScheduledExecutorService);
        }
        catch (TransportException e)
        {
            //exception will be thrown, but we aren't testing for what it is nor do we care that it threw
        }

        //assert
        new Verifications()
        {
            {
                mockLatch.await(anyLong, TimeUnit.MILLISECONDS);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_020: [The function shall do nothing if the authentication is already open.]
    @Test
    public void authenticateDoesNothing() throws TransportException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

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
    public void authenticateSuccess() throws TransportException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

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
    public void openLinksDoesNothing() throws TransportException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

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
                Deencapsulation.invoke(mockAmqpsSessionManager, "openDeviceOperationLinks", MessageType.DEVICE_TELEMETRY);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_023: [The function shall call AmqpsSessionManager.openDeviceOperationLinks.]
    @Test
    public void openLinksSuccess() throws TransportException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

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
                Deencapsulation.invoke(mockAmqpsSessionManager, "openDeviceOperationLinks", MessageType.DEVICE_TELEMETRY);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_004: [The function shall TransportException throws if the waitLatch throws.]
    @Test(expected = TransportException.class)
    public void closeThrowsIfWaitLatchThrows() throws Exception
    {
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        this.setLatches(connection);

        new NonStrictExpectations()
        {
            {
                mockCloseLatch.await(anyLong, TimeUnit.MILLISECONDS);
                result = new InterruptedException();
            }
        };

        connection.close();
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_012: [The function shall set the status of the AMQPS connection to DISCONNECTED.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_013: [The function shall closeNow the AmqpsSessionManager and the AMQP connection.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_34_014: [If this object's proton reactor is not null, this function shall stop the Proton reactor.]
    @Test
    public void closeClosesAllProtonVariablesAndStopsProtonReactor() throws TransportException
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
            void open(Queue<DeviceClientConfig> deviceClientConfigs, ScheduledExecutorService scheduledExecutorService)
            {
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);

        Deencapsulation.setField(connection, "state", IotHubConnectionStatus.CONNECTED);
        Deencapsulation.setField(connection, "connection", mockConnection);
        Deencapsulation.setField(connection, "executorService", mockExecutorService);
        Deencapsulation.setField(connection, "scheduledExecutorService", mockScheduledExecutorService);
        Deencapsulation.setField(connection, "closeLatch", mockLatch);

        connection.close();

        IotHubConnectionStatus actualState = Deencapsulation.getField(connection, "state");
        assertEquals(IotHubConnectionStatus.DISCONNECTED, actualState);

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
    public void closeThrowsIfShutdownThrows() throws TransportException
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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);

        Deencapsulation.setField(connection, "state", IotHubConnectionStatus.CONNECTED);
        Deencapsulation.setField(connection, "connection", mockConnection);
        Deencapsulation.setField(connection, "executorService", mockExecutorService);
        Deencapsulation.setField(connection, "closeLatch", mockLatch);

        connection.close();

        IotHubConnectionStatus actualState = Deencapsulation.getField(connection, "state");
        assertEquals(IotHubConnectionStatus.DISCONNECTED, actualState);

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
                mockScheduledExecutorService.shutdownNow();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_015: [If the state of the connection is DISCONNECTED or there is not enough
    // credit, the function shall return -1.]
    @Test
    public void sendMessageDoesNothingIfConnectionIsClosed() throws TransportException
    {
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);

        Deencapsulation.setField(connection, "state", IotHubConnectionStatus.DISCONNECTED);
        Deencapsulation.setField(connection, "linkCredit", 100);

        Integer expectedDeliveryHash = -1;
        Integer actualDeliveryHash = Deencapsulation.invoke(connection, "sendMessage", Message.Factory.create(), MessageType.DEVICE_TELEMETRY, "someDeviceId");

        assertEquals(expectedDeliveryHash, actualDeliveryHash);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_015: [If the state of the connection is DISCONNECTED or there is not enough
    // credit, the function shall return -1.]
    @Test
    public void sendMessageDoesNothingIfNotEnoughLinkCredit() throws TransportException
    {
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);

        Deencapsulation.setField(connection, "state", IotHubConnectionStatus.CONNECTED);
        Deencapsulation.setField(connection, "linkCredit", -1);

        Integer expectedDeliveryHash = -1;
        Integer actualDeliveryHash = Deencapsulation.invoke(connection, "sendMessage", Message.Factory.create(), MessageType.DEVICE_TELEMETRY, "someDeviceId");

        assertEquals(expectedDeliveryHash, actualDeliveryHash);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_024: [The function shall call AmqpsSessionManager.sendMessage with the given parameters.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_021: [The function shall return the delivery hash.]
    @Test
    public void sendMessage() throws TransportException
    {
        // arrange
        baseExpectations();

        final byte[] messageBytes = new byte[] {1, 2};
        final Integer expectedDeliveryHash = 42;

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "sendMessage", new Class[] {Message.class, MessageType.class, String.class}, mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");
                result = expectedDeliveryHash;

                mockConfig.getDeviceId();
                result = "someDeviceId";
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);
        Deencapsulation.setField(connection, "state", IotHubConnectionStatus.CONNECTED);
        Deencapsulation.setField(connection, "linkCredit", 100);

        // act
        Integer actualDeliveryHash = Deencapsulation.invoke(connection, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");

        // assert
        assertEquals(expectedDeliveryHash, actualDeliveryHash);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "sendMessage", new Class[] {Message.class, MessageType.class, String.class}, mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");
                times = 1;
            }
        };

    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_033: [The event handler shall set the current handler to handle the connection events.]
    @Test
    public void onReactorInit() throws TransportException
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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);

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
    public void onReactorInitWithWebSockets() throws TransportException
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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);

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

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_011: [The function shall call countdown on close latch and open latch.]
    // Test_SRS_AMQPSIOTHUBCONNECTION_12_008: [The function shall set the reactor member variable to null.]
    @Test
    public void onReactorFinalNoReconnect() throws TransportException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockConfig.isUseWebsocket();
                result = true;
                mockCloseLatch.countDown();
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        this.setLatches(connection);
        Deencapsulation.setField(connection, "reactor", mockReactor);
        Deencapsulation.setField(connection, "state", IotHubConnectionStatus.DISCONNECTED);

        new MockUp<AmqpsIotHubConnection>()
        {
            @Mock
            void openAsync()
            {
                Deencapsulation.setField(connection, "state", IotHubConnectionStatus.CONNECTED);
            }
        };

        connection.onReactorFinal(mockEvent);

        Reactor reactor = Deencapsulation.getField(connection, "reactor");
        assertEquals(null, reactor);
        IotHubConnectionStatus state = Deencapsulation.getField(connection, "state");
        assertEquals(IotHubConnectionStatus.DISCONNECTED, state);

        new Verifications()
        {
            {
                mockCloseLatch.countDown();
                times = 1;

                mockLatch.countDown();
                times = 1;
            }
        };
    }

    // Test_SRS_AMQPSIOTHUBCONNECTION_12_010: [The function shall log the error if openAsync failed.]
    @Test
    public void onReactorFinalReconnectFailed() throws TransportException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockConfig.isUseWebsocket();
                result = true;
                mockCloseLatch.countDown();
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        this.setLatches(connection);
        Deencapsulation.setField(connection, "reactor", mockReactor);
        Deencapsulation.setField(connection, "state", IotHubConnectionStatus.DISCONNECTED);

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
        IotHubConnectionStatus state = Deencapsulation.getField(connection, "state");
        assertEquals(IotHubConnectionStatus.DISCONNECTED, state);

        new Verifications()
        {
            {
                mockCloseLatch.countDown();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_025: [The event handler shall get the Connection (Proton) object from the event handler and set the host name on the connection.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_029: [The event handler shall open the connection.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_009: [The event handler shall call the amqpsSessionManager.onConnectionInit function with the connection.]
    @Test
    public void onConnectionInit() throws TransportException
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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);
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
    public void onConnectionBoundNoWebSockets() throws TransportException, IOException
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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "useWebSockets", false);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

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
    public void onConnectionBoundWebSockets() throws TransportException, IOException
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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);
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

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_005: [THe function sets the state to disconnected.]
    @Test
    public void onConnectionUnbound() throws TransportException
    {
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "state", IotHubConnectionStatus.CONNECTED);

        connection.onConnectionUnbound(mockEvent);
        IotHubConnectionStatus state = Deencapsulation.getField(connection, "state");

        assertTrue(state == IotHubConnectionStatus.DISCONNECTED);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_015: [The function shall call AmqpsSessionManager.getMessageFromReceiverLink.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_050: [All the listeners shall be notified that a message was received from the server.]
    @Test
    public void onDeliveryReceive() throws TransportException
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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);
        connection.setListener(mockedIotHubListener);
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
    // Tests_SRS_AMQPSIOTHUBCONNECTION_34_064: [If the acknowledgement sent from the service is "Accepted", this function shall notify its listener that the message was successfully sent.]
    @Test
    public void onDeliverySend(@Mocked final Map<Integer, com.microsoft.azure.sdk.iot.device.Message> mockInProgressMessages) throws TransportException
    {
        baseExpectations();
        final String receiverLinkName = "receiver";

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "inProgressMessages", mockInProgressMessages);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "getMessageFromReceiverLink", anyString);
                result = null;

                mockEvent.getLink();
                result = mockLink;

                mockLink.getSource();
                result = mockSource;

                mockSource.getAddress();
                result = "notACBSLink";

                mockLink.getName();
                result = receiverLinkName;

                mockEvent.getType();
                result = Event.Type.DELIVERY;

                mockEvent.getDelivery();
                result = mockDelivery;

                mockDelivery.getRemoteState();
                result = Accepted.getInstance();

                mockInProgressMessages.containsKey(anyInt);
                result = true;

                mockInProgressMessages.remove(anyInt);
                result = mockedTransportMessage;
            }
        };

        connection.setListener(mockedIotHubListener);

        //act
        connection.onDelivery(mockEvent);

        //assert
        new Verifications()
        {
            {
                mockEvent.getType();
                times = 1;
                mockEvent.getDelivery();
                times = 1;
                mockDelivery.getRemoteState();
                times = 1;
                mockedIotHubListener.onMessageSent(mockedTransportMessage, null);
                times = 1;
                mockDelivery.free();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_28_001: [If the acknowledgement sent from the service is "Rejected", this function shall map the error condition if it exists to amqp exceptions.]
    @Test
    public void onDeliverySendRejectedMessageWithAmqpErrorCondition(@Mocked final Map<Integer, com.microsoft.azure.sdk.iot.device.Message> mockInProgressMessages) throws TransportException
    {
        baseExpectations();
        final String receiverLinkName = "receiver";

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "inProgressMessages", mockInProgressMessages);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "getMessageFromReceiverLink", anyString);
                result = null;

                mockEvent.getLink();
                result = mockLink;

                mockLink.getSource();
                result = mockSource;

                mockSource.getAddress();
                result = "notACBSLink";

                mockLink.getName();
                result = receiverLinkName;

                mockEvent.getType();
                result = Event.Type.DELIVERY;

                mockEvent.getDelivery();
                result = mockDelivery;

                mockDelivery.getRemoteState();
                result = mockedRejected;

                mockInProgressMessages.containsKey(anyInt);
                result = true;

                mockedRejected.getError();
                result = mockedErrorCondition;

                mockedErrorCondition.getCondition();
                result = mockedSymbol;

                mockedSymbol.toString();
                result = AmqpConnectionThrottledException.errorCode;

                mockInProgressMessages.remove(anyInt);
                result = mockedTransportMessage;

                Deencapsulation.newInstance(AmqpConnectionThrottledException.class, anyString);
                result = mockedTransportException;
            }
        };

        connection.setListener(mockedIotHubListener);

        //act
        connection.onDelivery(mockEvent);

        //assert
        new Verifications()
        {
            {
                mockEvent.getType();
                times = 1;
                mockEvent.getDelivery();
                times = 1;
                mockDelivery.getRemoteState();
                times = 1;
                mockedRejected.getError();
                times = 1;
                mockedErrorCondition.getCondition();
                times = 2;
                mockedIotHubListener.onMessageSent(mockedTransportMessage, mockedTransportException);
                times = 1;
                mockDelivery.free();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_34_065: [If the acknowledgement sent from the service is "Rejected", this function shall notify its listener that the sent message was rejected and that it should not be retried.]
    @Test
    public void onDeliverySendRejectedMessage(@Mocked final Map<Integer, com.microsoft.azure.sdk.iot.device.Message> mockInProgressMessages) throws TransportException
    {
        baseExpectations();
        final String receiverLinkName = "receiver";

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "inProgressMessages", mockInProgressMessages);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "getMessageFromReceiverLink", anyString);
                result = null;

                mockEvent.getLink();
                result = mockLink;

                mockLink.getSource();
                result = mockSource;

                mockSource.getAddress();
                result = "notACBSLink";

                mockLink.getName();
                result = receiverLinkName;

                mockEvent.getType();
                result = Event.Type.DELIVERY;

                mockEvent.getDelivery();
                result = mockDelivery;

                mockDelivery.getRemoteState();
                result = mockedRejected;

                mockedRejected.getError();
                result = null;

                mockInProgressMessages.containsKey(anyInt);
                result = true;

                mockInProgressMessages.remove(anyInt);
                result = mockedTransportMessage;

                new TransportException(anyString);
                result = mockedTransportException;

                mockedTransportException.setRetryable(true);
                times = 0;
            }
        };

        connection.setListener(mockedIotHubListener);

        //act
        connection.onDelivery(mockEvent);

        //assert
        new Verifications()
        {
            {
                mockEvent.getType();
                times = 1;
                mockEvent.getDelivery();
                times = 1;
                mockDelivery.getRemoteState();
                times = 1;
                mockedIotHubListener.onMessageSent(mockedTransportMessage, mockedTransportException);
                times = 1;
                mockDelivery.free();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_34_066: [If the acknowledgement sent from the service is "Modified", "Released", or "Received", this function shall notify its listener that the sent message needs to be retried.]
    @Test
    public void onDeliverySendModifiedMessage(@Mocked final Map<Integer, com.microsoft.azure.sdk.iot.device.Message> mockInProgressMessages) throws TransportException
    {
        baseExpectations();
        final String receiverLinkName = "receiver";

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "inProgressMessages", mockInProgressMessages);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "getMessageFromReceiverLink", anyString);
                result = null;

                mockEvent.getLink();
                result = mockLink;

                mockLink.getSource();
                result = mockSource;

                mockSource.getAddress();
                result = "notACBSLink";

                mockLink.getName();
                result = receiverLinkName;

                mockEvent.getType();
                result = Event.Type.DELIVERY;

                mockEvent.getDelivery();
                result = mockDelivery;

                mockDelivery.getRemoteState();
                result = mockedModified;

                mockInProgressMessages.containsKey(anyInt);
                result = true;

                mockInProgressMessages.remove(anyInt);
                result = mockedTransportMessage;

                new TransportException(anyString);
                result = mockedTransportException;

                mockedTransportException.setRetryable(true);
                times = 1;
            }
        };

        connection.setListener(mockedIotHubListener);

        //act
        connection.onDelivery(mockEvent);

        //assert
        new Verifications()
        {
            {
                mockEvent.getType();
                times = 1;
                mockEvent.getDelivery();
                times = 1;
                mockDelivery.getRemoteState();
                times = 1;
                mockedIotHubListener.onMessageSent(mockedTransportMessage, mockedTransportException);
                times = 1;
                mockDelivery.free();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_34_066: [If the acknowledgement sent from the service is "Modified", "Released", or "Received", this function shall notify its listener that the sent message needs to be retried.]
    @Test
    public void onDeliverySendReceivedMessage(@Mocked final Map<Integer, com.microsoft.azure.sdk.iot.device.Message> mockInProgressMessages) throws TransportException
    {
        baseExpectations();
        final String receiverLinkName = "receiver";

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "inProgressMessages", mockInProgressMessages);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "getMessageFromReceiverLink", anyString);
                result = null;

                mockEvent.getLink();
                result = mockLink;

                mockLink.getSource();
                result = mockSource;

                mockSource.getAddress();
                result = "notACBSLink";

                mockLink.getName();
                result = receiverLinkName;

                mockEvent.getType();
                result = Event.Type.DELIVERY;

                mockEvent.getDelivery();
                result = mockDelivery;

                mockDelivery.getRemoteState();
                result = mockedReceived;

                mockInProgressMessages.containsKey(anyInt);
                result = true;

                mockInProgressMessages.remove(anyInt);
                result = mockedTransportMessage;

                new TransportException(anyString);
                result = mockedTransportException;

                mockedTransportException.setRetryable(true);
                times = 1;
            }
        };

        connection.setListener(mockedIotHubListener);

        //act
        connection.onDelivery(mockEvent);

        //assert
        new Verifications()
        {
            {
                mockEvent.getType();
                times = 1;
                mockEvent.getDelivery();
                times = 1;
                mockDelivery.getRemoteState();
                times = 1;
                mockedIotHubListener.onMessageSent(mockedTransportMessage, mockedTransportException);
                times = 1;
                mockDelivery.free();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_34_066: [If the acknowledgement sent from the service is "Modified", "Released", or "Received", this function shall notify its listener that the sent message needs to be retried.]
    @Test
    public void onDeliverySendReleasedMessage(@Mocked final Map<Integer, com.microsoft.azure.sdk.iot.device.Message> mockInProgressMessages) throws TransportException
    {
        baseExpectations();
        final String receiverLinkName = "receiver";

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "inProgressMessages", mockInProgressMessages);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "getMessageFromReceiverLink", anyString);
                result = null;

                mockEvent.getLink();
                result = mockLink;

                mockLink.getSource();
                result = mockSource;

                mockSource.getAddress();
                result = "notACBSLink";

                mockLink.getName();
                result = receiverLinkName;

                mockEvent.getType();
                result = Event.Type.DELIVERY;

                mockEvent.getDelivery();
                result = mockDelivery;

                mockDelivery.getRemoteState();
                result = mockedReleased;

                mockInProgressMessages.containsKey(anyInt);
                result = true;

                mockInProgressMessages.remove(anyInt);
                result = mockedTransportMessage;

                new TransportException(anyString);
                result = mockedTransportException;

                mockedTransportException.setRetryable(true);
                times = 1;
            }
        };

        connection.setListener(mockedIotHubListener);

        //act
        connection.onDelivery(mockEvent);

        //assert
        new Verifications()
        {
            {
                mockEvent.getType();
                times = 1;
                mockEvent.getDelivery();
                times = 1;
                mockDelivery.getRemoteState();
                times = 1;
                mockedIotHubListener.onMessageSent(mockedTransportMessage, mockedTransportException);
                times = 1;
                mockDelivery.free();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_016: [The function shall get the link from the event and call device operation objects with it.]
    @Test
    public void onLinkInit() throws TransportException
    {
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

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

    // Tests_SRS_AMQPSIOTHUBCONNECTION_34_067: [If an exception is thrown while executing the callback onLinkInit on the saved amqpsSessionManager, that exception shall be saved.]
    @Test
    public void onLinkInitSavesExceptions() throws TransportException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();

                Deencapsulation.invoke(mockAmqpsSessionManager, "onLinkInit", mockLink);
                result = mockedTransportException;
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        connection.onLinkInit(mockEvent);

        //assert
        TransportException savedException = Deencapsulation.getField(connection, "savedException");
        assertNotNull(savedException);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_15_040: [The event handler shall save the remaining link credit.]
    @Test
    public void onLinkFlow() throws TransportException
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

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
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

    // Tests_SRS_AMQPSIOTHUBCONNECTION_21_051: [The open latch shall be notified when that the connection has been established.]
    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_052: [The function shall call AmqpsSessionManager.onLinkRemoteOpen with the given link.]
    @Test
    public void onLinkRemoteOpen() throws TransportException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "onLinkRemoteOpen", mockEvent);
                result = true;
            }
        };

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        this.setLatches(connection);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        //act
        connection.onLinkRemoteOpen(mockEvent);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "onLinkRemoteOpen", mockEvent);
                times = 1;
                mockLatch.countDown();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_34_054: [The function shall save the given listener.]
    @Test
    public void setListenerSuccess() throws TransportException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        
        // act
        connection.setListener(mockedIotHubListener);

        // assert
        IotHubListener listener = Deencapsulation.getField(connection, "listener");
        assertEquals(mockedIotHubListener, listener);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_34_063: [If the provided listener is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setListenerThrowsForNullListener() throws TransportException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);

        // act
        connection.setListener(null);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_055: [The function shall call AmqpsSessionManager.convertToProton with the given message.]
    @Test
    public void convertToProton() throws TransportException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

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
    public void convertFromProton() throws TransportException
    {
        // arrange
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
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

    //Tests_SRS_AMQPSIOTHUBCONNECTION_34_060 [If the provided event object's transport holds an error condition object, this function shall report the associated TransportException to this object's listeners.]
    @Test
    public void OnTransportErrorReportsErrorCodeIfPresent() throws TransportException
    {
        //arrange
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<AmqpsIotHubConnection>()
        {
            @Mock void scheduleReconnection(Throwable throwable)
            {
                methodsCalled.append("scheduleReconnection");
            }
        };
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        connection.setListener(mockedIotHubListener);
        Deencapsulation.setField(connection, "scheduledExecutorService", mockScheduledExecutorService);
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
                Deencapsulation.invoke(connection, "scheduleReconnection", new Class[] {Throwable.class}, (Throwable) any);
            }
        };

        //act
        connection.onTransportError(mockEvent);

        //assert
        assertTrue(methodsCalled.toString().contains("scheduleReconnection"));
    }

    //Tests_SRS_AMQPSIOTHUBCONNECTION_34_061 [If the provided event object's transport holds a remote error condition object, this function shall report the associated TransportException to this object's listeners.]
    @Test
    public void OnLinkRemoteCloseReportsErrorCodeIfPresent() throws TransportException
    {
        final StringBuilder methodsCalled = new StringBuilder();
        new MockUp<AmqpsIotHubConnection>()
        {
            @Mock void scheduleReconnection(Throwable throwable)
            {
                methodsCalled.append("scheduleReconnection");
            }
        };
        baseExpectations();
        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        connection.setListener(mockedIotHubListener);
        new Expectations()
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
        assertEquals("scheduleReconnection", methodsCalled.toString());
    }

    //Tests_SRS_AMQPSIOTHUBCONNECTION_34_089: [If an amqp message can be received from the receiver link, and that amqp message contains a status code that is not 200 or 204, this function shall notify this object's listeners that that message was received and provide the status code's mapped exception.]
    @Test
    public void onDeliveryNotifiesListenerOfErrorCodes() throws TransportException
    {
        //arrange
        final int expectedErrorCode = 401;
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        connection.setListener(mockedIotHubListener);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);
        final Map<String, Object> applicationPropertiesMap = new HashMap<>();
        applicationPropertiesMap.put("status-code", expectedErrorCode);
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
                Deencapsulation.invoke(mockAmqpsSessionManager, "convertFromProton", new Class[] {AmqpsMessage.class, DeviceClientConfig.class}, any, any);
                result = null;
                mockAmqpsMessage.getAmqpsMessageType();
                result = MessageType.CBS_AUTHENTICATION;
            }
        };

        //act
        connection.onDelivery(mockEvent);

        //assert
        IotHubServiceException savedException = Deencapsulation.getField(connection, "savedException");
        assertEquals(IotHubStatusCode.getIotHubStatusCode(expectedErrorCode), savedException.getStatusCode());
    }

    // Tests_SRS_AMQPSTRANSPORT_34_068: [If the provided message is saved in the saved map of messages to acknowledge, and if the provided result is ABANDON, this function shall send the amqp ack with ABANDON.]
    // Tests_SRS_AMQPSTRANSPORT_34_071: [If the amqp message is acknowledged, this function shall remove it from the saved map of messages to acknowledge and return true.]
    @Test
    public void sendMessageResultCOMPLETE() throws TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "state", IotHubConnectionStatus.CONNECTED);
        Map<com.microsoft.azure.sdk.iot.device.Message, AmqpsMessage> sendAckMessages = new ConcurrentHashMap<>();
        sendAckMessages.put(mockedTransportMessage, mockAmqpsMessage);
        Deencapsulation.setField(connection, "sendAckMessages", sendAckMessages);

        //act
        boolean result = connection.sendMessageResult(mockedTransportMessage, IotHubMessageResult.COMPLETE);

        //assert
        assertTrue(result);
        assertTrue(sendAckMessages.isEmpty());
        new Verifications()
        {
            {
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.COMPLETE);
                times = 1;
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.REJECT);
                times = 0;
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.ABANDON);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_34_069: [If the provided message is saved in the saved map of messages to acknowledge, and if the provided result is REJECT, this function shall send the amqp ack with REJECT.]
    @Test
    public void sendMessageResultREJECT() throws TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "state", IotHubConnectionStatus.CONNECTED);
        Map<com.microsoft.azure.sdk.iot.device.Message, AmqpsMessage> sendAckMessages = new ConcurrentHashMap<>();
        sendAckMessages.put(mockedTransportMessage, mockAmqpsMessage);
        Deencapsulation.setField(connection, "sendAckMessages", sendAckMessages);

        //act
        boolean result = connection.sendMessageResult(mockedTransportMessage, IotHubMessageResult.REJECT);

        //assert
        new Verifications()
        {
            {
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.COMPLETE);
                times = 0;
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.REJECT);
                times = 1;
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.ABANDON);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_34_070: [If the provided message is saved in the saved map of messages to acknowledge, and if the provided result is COMPLETE, this function shall send the amqp ack with COMPLETE.]
    @Test
    public void sendMessageResultABANDON() throws TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "state", IotHubConnectionStatus.CONNECTED);
        Map<com.microsoft.azure.sdk.iot.device.Message, AmqpsMessage> sendAckMessages = new ConcurrentHashMap<>();
        sendAckMessages.put(mockedTransportMessage, mockAmqpsMessage);
        Deencapsulation.setField(connection, "sendAckMessages", sendAckMessages);

        //act
        boolean result = connection.sendMessageResult(mockedTransportMessage, IotHubMessageResult.ABANDON);

        //assert
        new Verifications()
        {
            {
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.COMPLETE);
                times = 0;
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.REJECT);
                times = 0;
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.ABANDON);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_34_072: [If the provided message is not saved in the saved map of messages to acknowledge, this function shall return false.]
    @Test
    public void sendMessageResultReturnsFalseIfNoAssociatedAmqpsMessage() throws TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "state", IotHubConnectionStatus.CONNECTED);

        //act
        boolean result = connection.sendMessageResult(mockedTransportMessage, IotHubMessageResult.ABANDON);

        //assert
        assertFalse(result);
        new Verifications()
        {
            {
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.COMPLETE);
                times = 0;
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.REJECT);
                times = 0;
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.ABANDON);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_34_073: [If this object is not CONNECTED, this function shall return false.]
    @Test
    public void sendMessageResultReturnsFalseIfNotConnected() throws TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "state", IotHubConnectionStatus.DISCONNECTED);

        //act
        boolean result = connection.sendMessageResult(mockedTransportMessage, IotHubMessageResult.ABANDON);

        //assert
        assertFalse(result);
        new Verifications()
        {
            {
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.COMPLETE);
                times = 0;
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.REJECT);
                times = 0;
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.ABANDON);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_074: [If authentication has not succeeded after calling
    // authenticate() and openLinks(), or if all links are not open yet,
    // this function shall throw a retryable transport exception.]
    @Test (expected = TransportException.class)
    public void openChecksIfActuallyOpenExceptionEncountered() throws TransportException, InterruptedException
    {
        // arrange
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        connection.setListener(mockedIotHubListener);
        final CountDownLatch closeLatch = new CountDownLatch(1);

        new Expectations()
        {
            {
                new CountDownLatch(anyInt);
                result = closeLatch;

                new CountDownLatch(anyInt);
                result = mockLatch;

                new AmqpsSessionManager(mockConfig, (ScheduledExecutorService)any);
                result = mockAmqpsSessionManager;

                mockLatch.await(anyLong, TimeUnit.MILLISECONDS);

                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                result = true;

                mockAmqpsSessionManager.authenticate();

                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                result = true;

                mockAmqpsSessionManager.openDeviceOperationLinks(MessageType.DEVICE_TELEMETRY);

                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                result = true;
            }
        };

        // act
        connection.open(mockedQueue, mockScheduledExecutorService);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_12_074: [If authentication has not succeeded after calling
    // authenticate() and openLinks(), or if all links are not open yet,
    // this function shall throw a retryable transport exception.]
    @Test (expected = TransportException.class)
    public void openChecksIfActuallyOpenNotAllLinksOpened() throws TransportException, InterruptedException
    {
        // arrange
        baseExpectations();

        final AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        connection.setListener(mockedIotHubListener);
        final CountDownLatch closeLatch = new CountDownLatch(1);

        new Expectations()
        {
            {
                new CountDownLatch(anyInt);
                result = closeLatch;

                new CountDownLatch(anyInt);
                result = mockLatch;

                new AmqpsSessionManager(mockConfig, (ScheduledExecutorService)any);
                result = mockAmqpsSessionManager;

                mockLatch.await(anyLong, TimeUnit.MILLISECONDS);

                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                result = true;

                mockAmqpsSessionManager.authenticate();

                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                result = true;

                mockAmqpsSessionManager.openDeviceOperationLinks(MessageType.DEVICE_TELEMETRY);

                Deencapsulation.invoke(mockAmqpsSessionManager, "isAuthenticationOpened");
                result = true;

                Deencapsulation.invoke(mockAmqpsSessionManager, "areAllLinksOpen");
                result = false;
            }
        };

        // act
        connection.open(mockedQueue, mockScheduledExecutorService);
    }

    // Tests_SRS_AMQPSTRANSPORT_34_076: [The function throws IllegalStateException if none of the device operation object could handle the conversion.]
    @Test (expected = IllegalStateException.class)
    public void sendMessageThrowsIfNoHandler() throws TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "convertToProton", mockedTransportMessage);
                result = null;
            }
        };

        //act
        connection.sendMessage(mockedTransportMessage);
    }

    // Tests_SRS_AMQPSTRANSPORT_34_077: [The function shall attempt to send the Proton message to IoTHub using the underlying AMQPS connection.]
    // Tests_SRS_AMQPSTRANSPORT_34_078: [If the sent message hash is valid, it shall be added to the in progress map and this function shall return OK.]
    @Test
    public void sendMessageGetsHash() throws TransportException
    {
        //arrange
        baseExpectations();
        final int expectedHash = 40;
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "state", IotHubConnectionStatus.CONNECTED);
        Deencapsulation.setField(connection, "linkCredit", 20);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "convertToProton", mockIoTMessage);
                result = mockedAmqpsConvertToProtonReturnValue;

                Deencapsulation.invoke(mockedAmqpsConvertToProtonReturnValue, "getMessageImpl");
                result = mockedMessageImpl;

                Deencapsulation.invoke(mockedAmqpsConvertToProtonReturnValue, "getMessageType");
                result = MessageType.DEVICE_TWIN;

                mockIoTMessage.getIotHubConnectionString();
                result = mockConnectionString;

                Deencapsulation.invoke(mockAmqpsSessionManager, "sendMessage", new Class[] {Message.class, MessageType.class, String.class}, mockedMessageImpl, MessageType.DEVICE_TWIN, "someDeviceId");
                result = expectedHash;

                mockConfig.getDeviceId();
                result = "someDeviceId";

                mockIoTMessage.getConnectionDeviceId();
                result = "someDeviceId";
            }
        };

        //act
        IotHubStatusCode actualStatusCode = connection.sendMessage(mockIoTMessage);

        //assert
        Map<Integer, com.microsoft.azure.sdk.iot.device.Message> inProgressMessages = Deencapsulation.getField(connection, "inProgressMessages");
        assertTrue(inProgressMessages.containsKey(expectedHash));
        assertEquals(IotHubStatusCode.OK, actualStatusCode);
    }

    // Tests_SRS_AMQPSTRANSPORT_34_079: [If the sent message hash is -1, this function shall throw a retriable ProtocolException.]
    @Test
    public void sendMessageThrowsForNegativeHash() throws TransportException
    {
        //arrange
        baseExpectations();
        final int expectedHash = -1;
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "state", IotHubConnectionStatus.CONNECTED);
        Deencapsulation.setField(connection, "linkCredit", 20);
        Deencapsulation.setField(connection, "amqpsSessionManager", mockAmqpsSessionManager);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionManager, "convertToProton", mockIoTMessage);
                result = mockedAmqpsConvertToProtonReturnValue;

                Deencapsulation.invoke(mockedAmqpsConvertToProtonReturnValue, "getMessageImpl");
                result = mockedMessageImpl;

                Deencapsulation.invoke(mockedAmqpsConvertToProtonReturnValue, "getMessageType");
                result = MessageType.DEVICE_TWIN;

                mockIoTMessage.getIotHubConnectionString();
                result = mockConnectionString;

                Deencapsulation.invoke(mockAmqpsSessionManager, "sendMessage", new Class[] {Message.class, MessageType.class, String.class}, mockedMessageImpl, MessageType.DEVICE_TWIN, "someDeviceId");
                result = expectedHash;

                new ProtocolException(anyString);
                result = mockedProtocolException;

                mockConfig.getDeviceId();
                result = "someDeviceId";

                mockIoTMessage.getConnectionDeviceId();
                result = "someDeviceId";
            }
        };

        boolean exceptionThrown = false;

        //act
        try
        {
            connection.sendMessage(mockIoTMessage);
        }
        catch (ProtocolException e)
        {
            exceptionThrown = true;
        }

        //assert
        assertTrue(exceptionThrown);
        new Verifications()
        {
            {
                mockedProtocolException.setRetryable(true);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSTRANSPORT_34_094: [This function shall return the saved connection id.]
    @Test
    public void getConnectionIdReturnsSavedConnectionId() throws TransportException
    {
        //arrange
        String expectedConnectionId = "1234";
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "connectionId", expectedConnectionId);

        //act
        String actualConnectionId = connection.getConnectionId();

        //assert
        Assert.assertEquals(expectedConnectionId, actualConnectionId);
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_34_081: [If an exception can be found in the sender, this function shall return a the mapped amqp exception derived from that exception.]
    @Test
    public void getErrorFromEventOutOfSender(final @Mocked ErrorCondition mockedErrorCondition, @Mocked final AmqpConnectionFramingErrorException mockedAmqpConnectionFramingErrorException) throws TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        final String expectedError = AmqpConnectionFramingErrorException.errorCode;
        final String expectedErrorDescription = "sender error description";
        new NonStrictExpectations()
        {
            {
                mockEvent.getSender();
                result = mockSender;
                mockEvent.getReceiver();
                result = null;
                mockEvent.getTransport();
                result = null;
                mockEvent.getSession();
                result = null;
                mockEvent.getConnection();
                result = null;
                mockEvent.getLink();
                result = null;

                mockSender.getRemoteCondition();
                result = mockedErrorCondition;

                mockedErrorCondition.getCondition();
                result = mockedSymbol;

                mockedSymbol.toString();
                result = expectedError;

                mockedErrorCondition.getDescription();
                result = expectedErrorDescription;

                new AmqpConnectionFramingErrorException(expectedErrorDescription);
                result = mockedAmqpConnectionFramingErrorException;

                mockedAmqpConnectionFramingErrorException.getMessage();
                result = expectedErrorDescription;
            }
        };

        //act
        TransportException actualException = Deencapsulation.invoke(connection, "getTransportExceptionFromEvent", mockEvent);

        //assert
        assertTrue(actualException instanceof AmqpConnectionFramingErrorException);
        assertEquals(expectedErrorDescription, actualException.getMessage());
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_34_082: [If an exception can be found in the receiver, this function shall return a the mapped amqp exception derived from that exception.]
    @Test
    public void getErrorFromEventOutOfReceiver(final @Mocked ErrorCondition mockedErrorCondition, @Mocked final AmqpConnectionFramingErrorException mockedAmqpConnectionFramingErrorException) throws TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        final String expectedError = AmqpConnectionFramingErrorException.errorCode;
        final String expectedErrorDescription = "sender error description";
        new NonStrictExpectations()
        {
            {
                mockEvent.getSender();
                result = null;
                mockEvent.getReceiver();
                result = mockReceiver;
                mockEvent.getTransport();
                result = null;
                mockEvent.getSession();
                result = null;
                mockEvent.getConnection();
                result = null;
                mockEvent.getLink();
                result = null;

                mockReceiver.getRemoteCondition();
                result = mockedErrorCondition;

                mockedErrorCondition.getCondition();
                result = mockedSymbol;

                mockedSymbol.toString();
                result = expectedError;

                mockedErrorCondition.getDescription();
                result = expectedErrorDescription;

                new AmqpConnectionFramingErrorException(expectedErrorDescription);
                result = mockedAmqpConnectionFramingErrorException;

                mockedAmqpConnectionFramingErrorException.getMessage();
                result = expectedErrorDescription;
            }
        };

        //act
        TransportException actualException = Deencapsulation.invoke(connection, "getTransportExceptionFromEvent", mockEvent);

        //assert
        assertTrue(actualException instanceof AmqpConnectionFramingErrorException);
        assertEquals(expectedErrorDescription, actualException.getMessage());
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_34_086: [If an exception can be found in the transport, this function shall return a the mapped amqp exception derived from that exception.]
    @Test
    public void getErrorFromEventOutOfTransport(final @Mocked ErrorCondition mockedErrorCondition, @Mocked final AmqpConnectionFramingErrorException mockedAmqpConnectionFramingErrorException) throws TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        final String expectedError = AmqpConnectionFramingErrorException.errorCode;
        final String expectedErrorDescription = "sender error description";
        new NonStrictExpectations()
        {
            {
                mockEvent.getSender();
                result = null;
                mockEvent.getReceiver();
                result = null;
                mockEvent.getTransport();
                result = mockTransport;
                mockEvent.getSession();
                result = null;
                mockEvent.getConnection();
                result = null;
                mockEvent.getLink();
                result = null;

                mockTransport.getRemoteCondition();
                result = mockedErrorCondition;

                mockedErrorCondition.getCondition();
                result = mockedSymbol;

                mockedSymbol.toString();
                result = expectedError;

                mockedErrorCondition.getDescription();
                result = expectedErrorDescription;

                new AmqpConnectionFramingErrorException(expectedErrorDescription);
                result = mockedAmqpConnectionFramingErrorException;

                mockedAmqpConnectionFramingErrorException.getMessage();
                result = expectedErrorDescription;
            }
        };

        //act
        TransportException actualException = Deencapsulation.invoke(connection, "getTransportExceptionFromEvent", mockEvent);

        //assert
        assertTrue(actualException instanceof AmqpConnectionFramingErrorException);
        assertEquals(expectedErrorDescription, actualException.getMessage());
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_34_083: [If an exception can be found in the session, this function shall return a the mapped amqp exception derived from that exception.]
    @Test
    public void getErrorFromEventOutOfSession(final @Mocked ErrorCondition mockedErrorCondition, @Mocked final AmqpConnectionFramingErrorException mockedAmqpConnectionFramingErrorException) throws TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        final String expectedError = AmqpConnectionFramingErrorException.errorCode;
        final String expectedErrorDescription = "sender error description";
        new NonStrictExpectations()
        {
            {
                mockEvent.getSender();
                result = null;
                mockEvent.getReceiver();
                result = null;
                mockEvent.getTransport();
                result = null;
                mockEvent.getSession();
                result = mockSession;
                mockEvent.getConnection();
                result = null;
                mockEvent.getLink();
                result = null;

                mockSession.getRemoteCondition();
                result = mockedErrorCondition;

                mockedErrorCondition.getCondition();
                result = mockedSymbol;

                mockedSymbol.toString();
                result = expectedError;

                mockedErrorCondition.getDescription();
                result = expectedErrorDescription;

                new AmqpConnectionFramingErrorException(expectedErrorDescription);
                result = mockedAmqpConnectionFramingErrorException;

                mockedAmqpConnectionFramingErrorException.getMessage();
                result = expectedErrorDescription;
            }
        };

        //act
        TransportException actualException = Deencapsulation.invoke(connection, "getTransportExceptionFromEvent", mockEvent);

        //assert
        assertTrue(actualException instanceof AmqpConnectionFramingErrorException);
        assertEquals(expectedErrorDescription, actualException.getMessage());
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_34_084: [If an exception can be found in the connection, this function shall return a the mapped amqp exception derived from that exception.]
    @Test
    public void getErrorFromEventOutOfConnection(final @Mocked ErrorCondition mockedErrorCondition, @Mocked final AmqpConnectionFramingErrorException mockedAmqpConnectionFramingErrorException) throws TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        final String expectedError = AmqpConnectionFramingErrorException.errorCode;
        final String expectedErrorDescription = "sender error description";
        new NonStrictExpectations()
        {
            {
                mockEvent.getSender();
                result = null;
                mockEvent.getReceiver();
                result = null;
                mockEvent.getTransport();
                result = null;
                mockEvent.getSession();
                result = null;
                mockEvent.getConnection();
                result = mockConnection;
                mockEvent.getLink();
                result = null;

                mockConnection.getRemoteCondition();
                result = mockedErrorCondition;

                mockedErrorCondition.getCondition();
                result = mockedSymbol;

                mockedSymbol.toString();
                result = expectedError;

                mockedErrorCondition.getDescription();
                result = expectedErrorDescription;

                new AmqpConnectionFramingErrorException(expectedErrorDescription);
                result = mockedAmqpConnectionFramingErrorException;

                mockedAmqpConnectionFramingErrorException.getMessage();
                result = expectedErrorDescription;
            }
        };

        //act
        TransportException actualException = Deencapsulation.invoke(connection, "getTransportExceptionFromEvent", mockEvent);

        //assert
        assertTrue(actualException instanceof AmqpConnectionFramingErrorException);
        assertEquals(expectedErrorDescription, actualException.getMessage());
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_34_085: [If an exception can be found in the link, this function shall return a the mapped amqp exception derived from that exception.]
    @Test
    public void getErrorFromEventOutOfLink(final @Mocked ErrorCondition mockedErrorCondition, @Mocked final AmqpConnectionFramingErrorException mockedAmqpConnectionFramingErrorException) throws TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        final String expectedError = AmqpConnectionFramingErrorException.errorCode;
        final String expectedErrorDescription = "sender error description";
        new NonStrictExpectations()
        {
            {
                mockEvent.getSender();
                result = null;
                mockEvent.getReceiver();
                result = null;
                mockEvent.getTransport();
                result = null;
                mockEvent.getSession();
                result = null;
                mockEvent.getConnection();
                result = null;
                mockEvent.getLink();
                result = mockLink;

                mockLink.getRemoteCondition();
                result = mockedErrorCondition;

                mockedErrorCondition.getCondition();
                result = mockedSymbol;

                mockedSymbol.toString();
                result = expectedError;

                mockedErrorCondition.getDescription();
                result = expectedErrorDescription;

                new AmqpConnectionFramingErrorException(expectedErrorDescription);
                result = mockedAmqpConnectionFramingErrorException;

                mockedAmqpConnectionFramingErrorException.getMessage();
                result = expectedErrorDescription;
            }
        };

        //act
        TransportException actualException = Deencapsulation.invoke(connection, "getTransportExceptionFromEvent", mockEvent);

        //assert
        assertTrue(actualException instanceof AmqpConnectionFramingErrorException);
        assertEquals(expectedErrorDescription, actualException.getMessage());
    }

    // Tests_SRS_AMQPSIOTHUBCONNECTION_34_080: [If no exception can be found in the sender, receiver, session, connection, link, or transport, this function shall return a generic TransportException.]
    @Test
    public void getErrorFromEventDefault(final @Mocked ErrorCondition mockedErrorCondition, @Mocked final AmqpConnectionFramingErrorException mockedAmqpConnectionFramingErrorException) throws TransportException
    {
        //arrange
        baseExpectations();
        AmqpsIotHubConnection connection = new AmqpsIotHubConnection(mockConfig);
        new NonStrictExpectations()
        {
            {
                mockEvent.getSender();
                result = null;
                mockEvent.getReceiver();
                result = null;
                mockEvent.getTransport();
                result = null;
                mockEvent.getSession();
                result = null;
                mockEvent.getConnection();
                result = null;
                mockEvent.getLink();
                result = null;

                new TransportException("Unknown transport exception occurred");
                result = mockedTransportException;

                mockedTransportException.getMessage();
                result = "Unknown transport exception occurred";
            }
        };

        //act
        TransportException actualException = Deencapsulation.invoke(connection, "getTransportExceptionFromEvent", mockEvent);

        //assert
        assertEquals("Unknown transport exception occurred", actualException.getMessage());
    }
    
    private void baseExpectations() throws TransportException
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
                mockConfig.isUseWebsocket();
                result = false;
            }
        };
    }

    private void setLatches(AmqpsIotHubConnection connection)
    {
        Deencapsulation.setField(connection, "openLatch", mockLatch);
        Deencapsulation.setField(connection, "closeLatch", mockCloseLatch);
    }
}