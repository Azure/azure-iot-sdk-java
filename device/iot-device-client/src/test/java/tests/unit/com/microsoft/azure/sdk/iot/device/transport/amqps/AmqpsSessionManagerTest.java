package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import mockit.*;
import org.apache.qpid.proton.engine.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Unit tests for DeviceClient.
 * Methods: 100%
 * Lines: 99%
 */
public class AmqpsSessionManagerTest
{
    @Mocked
    DeviceClientConfig mockDeviceClientConfig;

    @Mocked
    AmqpsDeviceAuthenticationCBS mockAmqpsDeviceAuthenticationCBS;

    @Mocked
    AmqpsDeviceAuthenticationX509 mockAmqpsDeviceAuthenticationX509;

    @Mocked
    AmqpsSessionDeviceOperation mockAmqpsSessionDeviceOperation;

    @Mocked
    AmqpsSessionDeviceOperation mockAmqpsSessionDeviceOperation1;

    @Mocked
    AmqpsDeviceOperations mockedAmqpsDeviceOperations;

    @Mocked
    ScheduledExecutorService mockScheduledExecutorService;

    @Mocked
    ObjectLock mockObjectLock;

    @Mocked
    Session mockSession;

    @Mocked
    Connection mockConnection;

    @Mocked
    Transport mockTransport;

    @Mocked
    SSLContext mockSSLContext;

    @Mocked
    Link mockLink;

    @Mocked
    Event mockEvent;

    @Mocked
    org.apache.qpid.proton.message.Message mockProtonMessage;

    @Mocked
    IotHubConnectionString mockIotHubConnectionString;

    @Mocked
    AmqpsMessage mockAmqpsMessage;

    @Mocked
    Message mockMessage;

    @Mocked
    AmqpsConvertToProtonReturnValue mockAmqpsConvertToProtonReturnValue;

    @Mocked
    AmqpsConvertFromProtonReturnValue mockAmqpsConvertFromProtonReturnValue;
    
    @Mocked
    SubscriptionMessageRequestSentCallback mockedSubscriptionMessageRequestSentCallback;

    // Tests_SRS_AMQPSESSIONMANAGER_12_001: [The constructor shall throw IllegalArgumentException if the deviceClientConfig parameter is null.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfDeviceClientIsNull() throws IllegalArgumentException, TransportException
    {
        // act
        new AmqpsSessionManager(null, mockedSubscriptionMessageRequestSentCallback);
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_002: [The constructor shall save the deviceClientConfig parameter value to a member variable.]
    @Test
    public void constructorSavesDeviceClientConfig() throws IllegalArgumentException, TransportException
    {
        // act
        AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);

        // assert
        DeviceClientConfig actualDeviceClientConfig = Deencapsulation.getField(amqpsSessionManager, "deviceClientConfig");

        assertEquals(mockDeviceClientConfig, actualDeviceClientConfig);
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_007: [The constructor shall add the create a AmqpsSessionDeviceOperation with the given deviceClientConfig.]
    @Test
    public void constructorCreatesSAS() throws IllegalArgumentException, TransportException
    {
        // arrange
        baseExpectationsSAS();

        // act
        AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);

        // assert
        ArrayList<AmqpsSessionDeviceOperation> actualList =  Deencapsulation.getField(amqpsSessionManager, "amqpsDeviceSessionList");
        assertEquals(actualList.size(), 1);
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_009: [The function shall create a new  AmqpsSessionDeviceOperation with the given deviceClientConfig and add it to the session list.]
    @Test
    public void addDeviceOperationSessionSuccess() throws IllegalArgumentException, TransportException
    {
        // arrange
        AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        // act
        Deencapsulation.invoke(amqpsSessionManager, "addDeviceOperationSession", mockDeviceClientConfig);

        // assert
        ArrayList<AmqpsSessionDeviceOperation> actualList =  Deencapsulation.getField(amqpsSessionManager, "amqpsDeviceSessionList");
        assertEquals(actualList.size(), 2);

        new Verifications()
        {
            {
                new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthenticationCBS, mockedSubscriptionMessageRequestSentCallback);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_010: [The function shall call all device session to closeNow links.]
    // Tests_SRS_AMQPSESSIONMANAGER_12_011: [The function shall closeNow the authentication links.]
    // Tests_SRS_AMQPSESSIONMANAGER_12_012: [The function shall closeNow the session.]
    // Tests_SRS_AMQPSESSIONMANAGER_12_043: [THe function shall shut down the scheduler.]
    @Test
    public void closeNowSuccess() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        baseExpectationsSAS();
        AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new NonStrictExpectations()
        {
            {
                mockScheduledExecutorService.awaitTermination(anyInt, TimeUnit.SECONDS);
                result = false;
                mockScheduledExecutorService.awaitTermination(anyInt, TimeUnit.SECONDS);
                result = false;
            }
        };

        // act
        Deencapsulation.invoke(amqpsSessionManager, "closeNow");

        // assert
        ArrayList<AmqpsSessionDeviceOperation> actualList =  Deencapsulation.getField(amqpsSessionManager, "amqpsDeviceSessionList");
        assertEquals(2, actualList.size());
        Session actualSession =  Deencapsulation.getField(amqpsSessionManager, "session");
        assertNull(actualSession);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "close");
                times = 1;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "close");
                times = 1;
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "closeLinks");
                times = 1;
                mockSession.close();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_014: [The function shall do nothing if the authentication is not open.]
    @Test
    public void authenticateDoesNothing() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        new NonStrictExpectations()
        {
            {
                mockAmqpsDeviceAuthenticationCBS.operationLinksOpened();
                result = false;
            }
        };

        // act
        Deencapsulation.invoke(amqpsSessionManager, "authenticate");
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_015: [The function shall call authenticate on all session list members.]
    @Test
    public void authenticateSuccess() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new NonStrictExpectations()
        {
            {
                mockDeviceClientConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockAmqpsDeviceAuthenticationCBS.operationLinksOpened();
                result = true;
            }
        };

        // act
        Deencapsulation.invoke(amqpsSessionManager, "authenticate");

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "authenticate");
                times = 1;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "authenticate");
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_018: [The function shall do nothing if the session is not open.]
    @Test
    public void openWorkerLinksDoesNothing() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.setField(amqpsSessionManager, "session", null);

        // act
        Deencapsulation.invoke(amqpsSessionManager, "openWorkerLinks");
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_019: [The function shall call openLinks on all session list members.]
    // Tests_SRS_AMQPSESSIONMANAGER_12_020: [The function shall lock the execution with waitLock.]
    @Test
    public void openWorkerLinksSuccess() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new Expectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "openLinks", mockSession);
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "openLinks", mockSession);
            }
        };

        // act
        Deencapsulation.invoke(amqpsSessionManager, "openWorkerLinks");
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_023: [The function shall initialize the session member variable from the connection if the session is null.]
    // Tests_SRS_AMQPSESSIONMANAGER_12_024: [The function shall open the initialized session.]
    @Test
    public void onConnectionInitOpensSession() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        new NonStrictExpectations()
        {
            {
                mockConnection.session();
                result = mockSession;
            }
        };

        // act
        Deencapsulation.invoke(amqpsSessionManager, "onConnectionInit", mockConnection);

        // assert

        new Verifications()
        {
            {
                mockConnection.session();
                times = 1;
                mockSession.open();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_025: [The function shall call authentication's openLink if the session is not null and the authentication is not open.]
    @Test
    public void onSessionRemoteOpenCallsAuthOpenLinks() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);

        // act
        Deencapsulation.invoke(amqpsSessionManager, "onSessionRemoteOpen", mockSession);

        // assert

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "openLinks", mockSession);
                times = 1;
            }
        };
    }

    @Test
    public void onSessionRemoteOpenCallsOpenWorkerLinksForX509() throws IllegalArgumentException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationX509);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        // act
        Deencapsulation.invoke(amqpsSessionManager, "onSessionRemoteOpen", mockSession);

        // assert

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationX509, "openLinks", mockSession);
                times = 0;

                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "openLinks", mockSession);
                times = 1;

                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "openLinks", mockSession);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_042: [The function shall call openLinks on all device sessions if the session is not null and the authentication is open.]
    @Test
    public void onConnectionInitCallsDeviceSessionsOpenLinks() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        new NonStrictExpectations()
        {
            {
                mockConnection.session();
                result = mockSession;
            }
        };

        // act
        Deencapsulation.invoke(amqpsSessionManager, "onConnectionInit", mockConnection);

        // assert
        new Verifications()
        {
            {
                mockSession.open();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_026: [The function shall call setSslDomain on authentication if the session is not null.]
    @Test
    public void onConnectionBoundCallsAuthSetSslDomain() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);

        // act
        Deencapsulation.invoke(amqpsSessionManager, "onConnectionBound", mockTransport);

        // assert

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "setSslDomain", mockTransport);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_027: [The function shall call authentication initLink on all session list member if the authentication is open and the session is not null.]
    @Test
    public void onLinkInitCallsDeviceSessionInitLink() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new NonStrictExpectations()
        {
            {
                mockAmqpsDeviceAuthenticationCBS.operationLinksOpened();
                result = true;
            }
        };

        // act
        Deencapsulation.invoke(amqpsSessionManager, "onLinkInit", mockLink);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "initLink", mockLink);
                times = 1;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "initLink", mockLink);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_028: [The function shall call authentication initLink if the authentication is not open and the session is not null.]
    @Test
    public void onLinkInitCallsAuthInitLink() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);

        new NonStrictExpectations()
        {
            {
                mockAmqpsDeviceAuthenticationCBS.operationLinksOpened();
                result = false;
            }
        };

        // act
        Deencapsulation.invoke(amqpsSessionManager, "onLinkInit", mockLink);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "initLink", mockLink);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_029: [The function shall call authentication onLinkRemoteOpen if the authentication is not open and return true if both links are open]
    @Test
    public void onLinkRemoteOpenCallsAuthOneLinkIsOpen() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockLink;
                mockLink.getName();
                result = linkName;
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "onLinkRemoteOpen", linkName);
                result = true;
            }
        };

        // act
        boolean returnValue = Deencapsulation.invoke(amqpsSessionManager, "onLinkRemoteOpen", mockLink);

        // assert
        assertTrue(returnValue);
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "onLinkRemoteOpen", linkName);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_030: [The function shall call authentication onLinkRemoteOpen if the authentication is not open and return false if only one link is open]
    @Test
    public void onLinkRemoteOpenCallsAuthBothLinkAreOpen() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        new Expectations()
        {
            {
                mockLink.getName();
                result = linkName;
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "onLinkRemoteOpen", linkName);
                result = true;
                times = 1;
            }
        };

        // act
        boolean returnValue = Deencapsulation.invoke(amqpsSessionManager, "onLinkRemoteOpen", mockLink);

        // assert
        assertTrue(returnValue);
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_032: [The function shall call sendMessage on all session list member and if there is a successful send return with the deliveryHash, otherwise return -1.]
    @Test
    public void sendMessageNoSender() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");
                result = -1;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");
                result = -1;
            }
        };

        // act
        Integer deliveryHash = Deencapsulation.invoke(amqpsSessionManager, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");

        // assert
        assertTrue(deliveryHash == -1);
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_032: [The function shall call sendMessage on all session list member and if there is a successful send return with the deliveryHash, otherwise return -1.]
    @Test
    public void sendMessageSuccess() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");
                result = -1;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");
                result = 42;
            }
        };

        // act
        Integer deliveryHash = Deencapsulation.invoke(amqpsSessionManager, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");

        // assert
        assertEquals((Integer)42, deliveryHash);
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_039: [The function shall return with the return value of authentication.operationLinksOpened.]
    @Test
    public void isAuthenticationOpenedTrue() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        new Expectations()
        {
            {
                mockAmqpsDeviceAuthenticationCBS.operationLinksOpened();
                result = true;
            }
        };

        // act
        boolean isOpened = Deencapsulation.invoke(amqpsSessionManager, "isAuthenticationOpened");

        // assert
        assertEquals(true, isOpened);
    }


    // Tests_SRS_AMQPSESSIONMANAGER_12_039: [The function shall return with the return value of authentication.operationLinksOpened.]
    @Test
    public void isAuthenticationOpenedFalse() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        new Expectations()
        {
            {
                mockAmqpsDeviceAuthenticationCBS.operationLinksOpened();
                result = false;
            }
        };

        // act
        boolean isOpened = Deencapsulation.invoke(amqpsSessionManager, "isAuthenticationOpened");

        // assert
        assertEquals(false, isOpened);
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_040: [The function shall call all device session's convertToProton, and if any of them not null return with the value.]
    @Test
    public void convertToProtonSuccess() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new Expectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "convertToProton", mockMessage);
                result = mockAmqpsConvertToProtonReturnValue;
            }
        };

        // act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsSessionManager, "convertToProton", mockMessage);

        // assert
        assertNotNull(amqpsConvertToProtonReturnValue);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "convertToProton", mockMessage);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_040: [The function shall call all device session's convertToProton, and if any of them not null return with the value.]
    @Test
    public void convertToProtonNull() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new Expectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "convertToProton", mockMessage);
                result = null;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "convertToProton", mockMessage);
                result = null;
            }
        };

        // act
        AmqpsConvertToProtonReturnValue amqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsSessionManager, "convertToProton", mockMessage);

        // assert
        assertNull(amqpsConvertToProtonReturnValue);
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_041: [The function shall call all device session's convertFromProton, and if any of them not null return with the value.]
    @Test
    public void convertFromProtonSuccess() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new Expectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);
                result = mockAmqpsConvertFromProtonReturnValue;
                times = 1;
            }
        };

        // act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsSessionManager, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);

        // assert
        assertNotNull(amqpsConvertFromProtonReturnValue);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_041: [The function shall call all device session's convertFromProton, and if any of them not null return with the value.]
    @Test
    public void convertFromProtonNull() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new Expectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);
                result = null;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);
                result = null;
            }
        };

        // act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsSessionManager, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);

        // assert
        assertNull(amqpsConvertFromProtonReturnValue);
    }

    @Test
    public void getExpectedWorkerLinkCountWithOneSession()
    {
        //arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new Expectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "getExpectedWorkerLinkCount");
                result = 2;
            }
        };

        //act
        int actualExpectedWorkerLinkCount = Deencapsulation.invoke(amqpsSessionManager, "getExpectedWorkerLinkCount");

        //assert
        assertEquals(2, actualExpectedWorkerLinkCount);
    }

    @Test
    public void getExpectedWorkerLinkCountWithMultipleSession()
    {
        //arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new Expectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "getExpectedWorkerLinkCount");
                result = 2;

                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "getExpectedWorkerLinkCount");
                result = 4;
            }
        };

        //act
        int actualExpectedWorkerLinkCount = Deencapsulation.invoke(amqpsSessionManager, "getExpectedWorkerLinkCount");

        //assert
        assertEquals(6, actualExpectedWorkerLinkCount);
    }

    @Test
    public void subscribeDeviceToMessageTypeChecksDeviceId()
    {
        // arrange
        final String expectedDeviceId = "aDeviceId";
        final MessageType expectedMessageType = MessageType.DEVICE_TWIN;
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationX509);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new Expectations()
        {
            {
                mockAmqpsSessionDeviceOperation.getDeviceId();
                result = "some unrelated deviceId";

                mockAmqpsSessionDeviceOperation1.getDeviceId();
                result = expectedDeviceId;
            }
        };

        // act
        Deencapsulation.invoke(amqpsSessionManager, "subscribeDeviceToMessageType", new Class[] {MessageType.class, String.class}, expectedMessageType, expectedDeviceId);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "subscribeToMessageType", new Class[] {Session.class, MessageType.class}, mockSession, (MessageType) any);
                times = 0;

                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "subscribeToMessageType", new Class[] {Session.class, MessageType.class}, mockSession, expectedMessageType);
                times = 1;

            }
        };
    }

    @Test
    public void onLinkRemoteOpenChecksSessionListsWorkerLinksIfAuthLinksAlreadyOpened()
    {
        // arrange
        final String expectedLinkName = "someTelemetryLink";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new Expectations()
        {
            {
                mockLink.getName();
                result = expectedLinkName;

                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "operationLinksOpened");
                result = true;

                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "onLinkRemoteOpen", expectedLinkName);
                result = false;

                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "onLinkRemoteOpen", expectedLinkName);
                result = true;
            }
        };

        //act
        Deencapsulation.invoke(amqpsSessionManager, "onLinkRemoteOpen", mockLink);
    }

    @Test
    public void onLinkRemoteOpenChecksSessionListsWorkerLinksIfAuthLinksAlreadyOpenedAndStopsOnceItIsHandled()
    {
        // arrange
        final String expectedLinkName = "someTelemetryLink";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new Expectations()
        {
            {
                mockLink.getName();
                result = expectedLinkName;

                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "operationLinksOpened");
                result = true;

                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "onLinkRemoteOpen", expectedLinkName);
                result = true;

            }
        };

        //act
        Deencapsulation.invoke(amqpsSessionManager, "onLinkRemoteOpen", mockLink);

        //assert
        new Verifications()
        {
            {
                //first entry in list returns true, so there should be no need to check the second entry in the list
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "onLinkRemoteOpen", expectedLinkName);
                times = 0;
            }
        };
    }

    private void baseExpectationsSAS()
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockDeviceClientConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                new AmqpsDeviceAuthenticationCBS(mockDeviceClientConfig);
                result = mockAmqpsDeviceAuthenticationCBS;
                new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthenticationCBS, mockedSubscriptionMessageRequestSentCallback);
                result = mockAmqpsSessionDeviceOperation;
            }
        };
    }
}
