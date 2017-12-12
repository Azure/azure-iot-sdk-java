package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import mockit.*;
import org.apache.qpid.proton.engine.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Unit tests for DeviceClient.
 * Methods: 100%
 * Lines: 96%
 */
public class AmqpsSessionManagerTest
{
    @Mocked
    DeviceClientConfig mockDeviceClientConfig;

    @Mocked
    AmqpsDeviceAuthenticationCBS mockAmqpsDeviceAuthenticationCBS;

    @Mocked
    AmqpsSessionDeviceOperation mockAmqpsSessionDeviceOperation;

    @Mocked
    AmqpsSessionDeviceOperation mockAmqpsSessionDeviceOperation1;

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

    // Tests_SRS_AMQPSESSIONMANAGER_12_001: [The constructor shall throw IllegalArgumentException if the deviceClientConfig parameter is null.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfDeviceClientIsNull() throws IllegalArgumentException
    {
        // act
        new AmqpsSessionManager(null);
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_002: [The constructor shall save the deviceClientConfig parameter value to a member variable.]
    @Test
    public void constructorSavesDeviceClientConfig() throws IllegalArgumentException
    {
        // act
        AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);

        // assert
        DeviceClientConfig actualDeviceClientConfig = Deencapsulation.getField(amqpsSessionManager, "deviceClientConfig");

        assertEquals(mockDeviceClientConfig, actualDeviceClientConfig);
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_007: [The constructor shall add the create a AmqpsSessionDeviceOperation with the given deviceClientConfig.]
    @Test
    public void constructorCreatesSAS() throws IllegalArgumentException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockDeviceClientConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                new AmqpsDeviceAuthenticationCBS(mockDeviceClientConfig);
                result = mockAmqpsDeviceAuthenticationCBS;
                new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthenticationCBS);
                result = mockAmqpsSessionDeviceOperation;
            }
        };

        // act
        AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "deviceClientConfig", mockDeviceClientConfig);

        // assert
        ArrayList<AmqpsSessionDeviceOperation> actualList =  Deencapsulation.getField(amqpsSessionManager, "amqpsDeviceSessionList");
        assertEquals(actualList.size(), 1);
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_004: [The constructor shall create AmqpsDeviceAuthenticationCBS if the authentication type is CBS.]
    // Tests_SRS_AMQPSESSIONMANAGER_12_005: [The constructor shall create AmqpsDeviceAuthenticationCBSTokenRenewalTask if the authentication type is CBS.]
    // Tests_SRS_AMQPSESSIONMANAGER_12_006: [The constructor shall create and start a scheduler for AmqpsDeviceAuthenticationCBSTokenRenewalTask if the authentication type is CBS.]
    // Tests_SRS_AMQPSESSIONMANAGER_12_007: [The constructor shall add the create a AmqpsSessionDeviceOperation with the given deviceClientConfig.]
    @Test
    public void constructorCreatesCBS() throws IllegalArgumentException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockDeviceClientConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                new AmqpsDeviceAuthenticationCBS(mockDeviceClientConfig);
                result = mockAmqpsDeviceAuthenticationCBS;
                new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthenticationCBS);
                result = mockAmqpsSessionDeviceOperation;
            }
        };

        // act
        AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "deviceClientConfig", mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        // assert
        ArrayList<AmqpsSessionDeviceOperation> actualList =  Deencapsulation.getField(amqpsSessionManager, "amqpsDeviceSessionList");
        assertEquals(actualList.size(), 1);

        new Verifications()
        {
            {
                new AmqpsDeviceAuthenticationCBS(mockDeviceClientConfig);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_008: [The function shall throw IllegalArgumentException if the deviceClientConfig parameter is null.]
    @Test (expected = IllegalArgumentException.class)
    public void addDeviceOperationSessionThrowsIfDeviceClientIsNull() throws IllegalArgumentException
    {
        // arrange
        AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);

        // act
        Deencapsulation.invoke(amqpsSessionManager, "addDeviceOperationSession", (DeviceClientConfig)null);
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_009: [The function shall create a new  AmqpsSessionDeviceOperation with the given deviceClietnConfig and add it to the session list.]
    @Test
    public void addDeviceOperationSessionSuccess() throws IllegalArgumentException
    {
        // arrange
        AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);

        // act
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.invoke(amqpsSessionManager, "addDeviceOperationSession", mockDeviceClientConfig);

        // assert
        ArrayList<AmqpsSessionDeviceOperation> actualList =  Deencapsulation.getField(amqpsSessionManager, "amqpsDeviceSessionList");
        assertEquals(actualList.size(), 2);

        new Verifications()
        {
            {
                new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthenticationCBS);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_010: [The function shall call all device session to closeNow links.]
    // Tests_SRS_AMQPSESSIONMANAGER_12_011: [The function shall closeNow the authentication links.]
    // Tests_SRS_AMQPSESSIONMANAGER_12_012: [The function shall closeNow the session.]
    // Tests_SRS_AMQPSESSIONMANAGER_12_043: [THe function shall shut down the scheduler.]
    @Test
    public void closeNowSuccess() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);
        Deencapsulation.invoke(amqpsSessionManager, "addDeviceOperationSession", mockDeviceClientConfig);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);
        Deencapsulation.setField(amqpsSessionManager, "taskSchedulerCBSSend", mockScheduledExecutorService);

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
                mockScheduledExecutorService.shutdown();
                times = 1;
                mockScheduledExecutorService.shutdownNow();
                times = 1;
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
    public void authenticateDoesNothing() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
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
    public void authenticateSuccess() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
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
    public void openDeviceOperationLinksDoesNothing() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.setField(amqpsSessionManager, "session", null);

        // act
        Deencapsulation.invoke(amqpsSessionManager, "openDeviceOperationLinks");
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_021: [The function shall throw IOException if the lock throws.]
    @Test (expected = IOException.class)
    public void openDeviceOperationLinksLockThrows() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);
        Deencapsulation.setField(amqpsSessionManager, "openLinksLock", mockObjectLock);

        new NonStrictExpectations()
        {
            {
                mockObjectLock.waitLock(anyLong);
                result = new InterruptedException();
            }
        };

        // act
        Deencapsulation.invoke(amqpsSessionManager, "openDeviceOperationLinks");
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_019: [The function shall call openLinks on all session list members.]
    // Tests_SRS_AMQPSESSIONMANAGER_12_020: [The function shall lock the execution with waitLock.]
    @Test
    public void openDeviceOperationLinksSuccess() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);
        Deencapsulation.setField(amqpsSessionManager, "openLinksLock", mockObjectLock);

        new NonStrictExpectations()
        {
            {
                mockAmqpsDeviceAuthenticationCBS.operationLinksOpened();
                result = true;
            }
        };

        // act
        Deencapsulation.invoke(amqpsSessionManager, "openDeviceOperationLinks");

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "openLinks", mockSession);
                times = 1;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "openLinks", mockSession);
                times = 1;
                mockObjectLock.waitLock(anyLong);
                times = 2;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_023: [The function shall initialize the session member variable from the connection if the session is null.]
    // Tests_SRS_AMQPSESSIONMANAGER_12_024: [The function shall open the initialized session.]
    @Test
    public void onConnectionInitOpensSession() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
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
    public void onConnectionInitCallsAuthOpenLinks() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);

        // act
        Deencapsulation.invoke(amqpsSessionManager, "onConnectionInit", mockConnection);

        // assert

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "openLinks", mockSession);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_042: [The function shall call openLinks on all device sessions if the session is not null and the authentication is open.]
    @Test
    public void onConnectionInitCallsDeviceSessionsOpenLinks() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
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
        Deencapsulation.invoke(amqpsSessionManager, "onConnectionInit", mockConnection);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "openLinks", mockSession);
                times = 1;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "openLinks", mockSession);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_026: [The function shall call setSslDomain on authentication if the session is not null.]
    @Test
    public void onConnectionBoundCallsAuthSetSslDomain() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
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
    public void onLinkInitCallsDeviceSessionInitLink() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
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
    public void onLinkInitCallsAuthInitLink() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
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

    // Tests_SRS_AMQPSESSIONMANAGER_12_029: [The function shall call authentication isLinkFound if the authentication is not open and return true if both links are open]
    @Test
    public void onLinkRemoteOpenCallsAuthOneLinkIsOpen() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        new NonStrictExpectations()
        {
            {
                mockEvent.getLink();
                result = mockLink;
                mockLink.getName();
                result = linkName;
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "isLinkFound", linkName);
                result = true;
            }
        };

        // act
        Boolean returnValue = Deencapsulation.invoke(amqpsSessionManager, "onLinkRemoteOpen", mockEvent);

        // assert
        assertFalse(returnValue);
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "isLinkFound", linkName);
                times = 1;
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "operationLinksOpened");
                times = 2;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_030: [The function shall call authentication isLinkFound if the authentication is not open and return false if only one link is open]
    @Test
    public void onLinkRemoteOpenCallsAuthBothLinkAreOpen() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        new Expectations()
        {
            {
                mockEvent.getLink();
                result = mockLink;
                mockLink.getName();
                result = linkName;
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "isLinkFound", linkName);
                result = true;
            }
        };

        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "operationLinksOpened");
                result = false;
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "operationLinksOpened");
                result = true;
            }
        };

        // act
        Boolean returnValue = Deencapsulation.invoke(amqpsSessionManager, "onLinkRemoteOpen", mockEvent);

        // assert
        assertTrue(returnValue);
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "isLinkFound", linkName);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_031: [The function shall call all all device session's isLinkFound, and if both links are opened notify the lock.]
    @Test
    public void onLinkRemoteOpenNotify() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        Deencapsulation.setField(amqpsSessionManager, "openLinksLock", mockObjectLock);

        new Expectations()
        {
            {
                mockEvent.getLink();
                result = mockLink;
                mockLink.getName();
                result = linkName;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "isLinkFound", linkName);
                result = true;
                mockAmqpsSessionDeviceOperation.operationLinksOpened();
                result = true;
            }
        };

        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "operationLinksOpened");
                result = true;
            }
        };

        // act
        Boolean returnValue = Deencapsulation.invoke(amqpsSessionManager, "onLinkRemoteOpen", mockEvent);

        // assert
        assertTrue(returnValue);
        new Verifications()
        {
            {
                mockObjectLock.notifyLock();
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_032: [The function shall call sendMessage on all session list member and if there is a successful send return with the deliveryHash, otherwise return -1.]
    @Test
    public void sendMessageNoSender() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, mockIotHubConnectionString);
                result = -1;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, mockIotHubConnectionString);
                result = -1;
            }
        };

        // act
        Integer deliveryHash = Deencapsulation.invoke(amqpsSessionManager, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, mockIotHubConnectionString);

        // assert
        assertTrue(deliveryHash == -1);
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_032: [The function shall call sendMessage on all session list member and if there is a successful send return with the deliveryHash, otherwise return -1.]
    @Test
    public void sendMessageSuccess() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, mockIotHubConnectionString);
                result = -1;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, mockIotHubConnectionString);
                result = 42;
            }
        };

        // act
        Integer deliveryHash = Deencapsulation.invoke(amqpsSessionManager, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, mockIotHubConnectionString);

        // assert
        assertEquals((Integer)42, deliveryHash);
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_033: [The function shall do nothing and return null if the session is not open.]
    @Test
    public void getMessageFromReceiverLinkDoesNothing() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "session", null);

        // act
        AmqpsMessage amqpsMessage = Deencapsulation.invoke(amqpsSessionManager, "getMessageFromReceiverLink", linkName);

        // assert
        assertNull(amqpsMessage);

    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_034: [The function shall call authentication getMessageFromReceiverLink if the authentication is not open.]
    @Test
    public void getMessageFromReceiverLinkCallsAuth() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        new Expectations()
        {
            {
                mockAmqpsDeviceAuthenticationCBS.operationLinksOpened();
                result = false;
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "getMessageFromReceiverLink", linkName);
                result = mockAmqpsMessage;
            }
        };

        // act
        AmqpsMessage amqpsMessage = Deencapsulation.invoke(amqpsSessionManager, "getMessageFromReceiverLink", linkName);

        // assert
        assertEquals(mockAmqpsMessage, amqpsMessage);

    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_035: [The function shall call device sessions getMessageFromReceiverLink if the authentication is open.]
    @Test
    public void getMessageFromReceiverLinkCallsDeviceSessionsAuthenticated() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new Expectations()
        {
            {
                mockAmqpsDeviceAuthenticationCBS.operationLinksOpened();
                result = true;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "getMessageFromReceiverLink", linkName);
                result = mockAmqpsMessage;
            }
        };

        // act
        AmqpsMessage amqpsMessage = Deencapsulation.invoke(amqpsSessionManager, "getMessageFromReceiverLink", linkName);

        // assert
        assertEquals(mockAmqpsMessage, amqpsMessage);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "getMessageFromReceiverLink", linkName);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_037: [The function shall return with the authentication isLinkFound's return value if the authentication is not open.]
    @Test
    public void isLinkFoundCallsAuth() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        new Expectations()
        {
            {
                mockAmqpsDeviceAuthenticationCBS.operationLinksOpened();
                result = false;
            }
        };

        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthenticationCBS, "isLinkFound", linkName);
                result = true;
            }
        };

        // act
        Boolean isFound = Deencapsulation.invoke(amqpsSessionManager, "isLinkFound", linkName);

        // assert
        assertEquals(true, isFound);
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_038: [The function shall call all device session's isLinkFound, and if any of them true return true otherwise return false.]
    @Test
    public void isLinkFoundCallsDeviceSessions() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "session", mockSession);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        ArrayList<AmqpsSessionDeviceOperation> sessionList = new ArrayList<>();
        sessionList.add(mockAmqpsSessionDeviceOperation);
        sessionList.add(mockAmqpsSessionDeviceOperation1);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceSessionList", sessionList);

        new Expectations()
        {
            {
                mockAmqpsDeviceAuthenticationCBS.operationLinksOpened();
                result = true;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "isLinkFound", linkName);
                result = true;
            }
        };

        // act
        Boolean isFound = Deencapsulation.invoke(amqpsSessionManager, "isLinkFound", linkName);

        // assert
        assertEquals(true, isFound);
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_039: [The function shall return with the return value of authentication.operationLinksOpened.]
    @Test
    public void isAuthenticationOpenedTrue() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        new Expectations()
        {
            {
                mockAmqpsDeviceAuthenticationCBS.operationLinksOpened();
                result = true;
            }
        };

        // act
        Boolean isOpened = Deencapsulation.invoke(amqpsSessionManager, "isAuthenticationOpened");

        // assert
        assertEquals(true, isOpened);
    }


    // Tests_SRS_AMQPSESSIONMANAGER_12_039: [The function shall return with the return value of authentication.operationLinksOpened.]
    @Test
    public void isAuthenticationOpenedFalse() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionManager, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthenticationCBS);

        new Expectations()
        {
            {
                mockAmqpsDeviceAuthenticationCBS.operationLinksOpened();
                result = false;
            }
        };

        // act
        Boolean isOpened = Deencapsulation.invoke(amqpsSessionManager, "isAuthenticationOpened");

        // assert
        assertEquals(false, isOpened);
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_040: [The function shall call all device session's convertToProton, and if any of them not null return with the value.]
    @Test
    public void convertToProtonSuccess() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
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
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "convertToProton", mockMessage);
                times = 1;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "convertToProton", mockMessage);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_040: [The function shall call all device session's convertToProton, and if any of them not null return with the value.]
    @Test
    public void convertToProtonNull() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
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

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "convertToProton", mockMessage);
                times = 1;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "convertToProton", mockMessage);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_041: [The function shall call all device session's convertFromProton, and if any of them not null return with the value.]

    @Test
    public void convertFromProtonSuccess() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
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
            }
        };

        // act
        AmqpsConvertFromProtonReturnValue amqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsSessionManager, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);

        // assert
        assertNotNull(amqpsConvertFromProtonReturnValue);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);
                times = 1;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONMANAGER_12_041: [The function shall call all device session's convertFromProton, and if any of them not null return with the value.]
    @Test
    public void convertFromProtonNull() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionManager amqpsSessionManager = new AmqpsSessionManager(mockDeviceClientConfig);
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

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);
                times = 1;
                Deencapsulation.invoke(mockAmqpsSessionDeviceOperation1, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);
                times = 1;
            }
        };
    }
}
