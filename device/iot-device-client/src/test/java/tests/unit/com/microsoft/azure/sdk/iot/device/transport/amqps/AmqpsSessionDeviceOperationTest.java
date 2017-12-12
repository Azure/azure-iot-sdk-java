package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Session;
import org.junit.Test;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for DeviceClient.
 * Methods: 100%
 * Lines: 97%
 */
public class AmqpsSessionDeviceOperationTest
{
    @Mocked
    DeviceClientConfig mockDeviceClientConfig;

    @Mocked
    AmqpsDeviceAuthentication mockAmqpsDeviceAuthentication;

    @Mocked
    AmqpsDeviceAuthenticationCBS mockAmqpsDeviceAuthenticationCBS;

    @Mocked
    AmqpsDeviceOperations mockAmqpsDeviceOperations;

    @Mocked
    AmqpsDeviceTelemetry mockAmqpsDeviceTelemetry;

    @Mocked
    AmqpsDeviceMethods mockAmqpsDeviceMethods;

    @Mocked
    AmqpsDeviceTwin mockAmqpsDeviceTwin;

    @Mocked
    Session mockSession;

    @Mocked
    Link mockLink;

    @Mocked
    org.apache.qpid.proton.message.Message mockProtonMessage;

    @Mocked
    AmqpsMessage mockAmqpsMessage;

    @Mocked
    Message mockMessage;

    @Mocked
    IotHubConnectionString mockIotHubConnectionString;

    @Mocked
    AmqpsSendReturnValue mockAmqpsSendReturnValue;

    @Mocked
    AmqpsConvertToProtonReturnValue mockAmqpsConvertToProtonReturnValue;

    @Mocked
    AmqpsConvertFromProtonReturnValue mockAmqpsConvertFromProtonReturnValue;

    @Mocked
    IotHubSasTokenAuthenticationProvider mockIotHubSasTokenAuthenticationProvider;

    @Mocked
    Executors mockExecutors;

    @Mocked
    ScheduledExecutorService mockScheduledExecutorService;

    @Mocked
    AmqpsDeviceAuthenticationCBSTokenRenewalTask mockAmqpsDeviceAuthenticationCBSTokenRenewalTask;

    @Mocked
    UUID mockUUID;

    @Mocked
    ObjectLock mockLock;

    @Mocked
    List<UUID> mockListUUID;


    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_001: [The constructor shall throw IllegalArgumentException if the deviceClientConfig or the amqpsDeviceAuthentication parameter is null.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfDeviceClientIsNull() throws IllegalArgumentException
    {
        // arrange
        // act
        new AmqpsSessionDeviceOperation(null, mockAmqpsDeviceAuthentication);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_001: [The constructor shall throw IllegalArgumentException if the deviceClientConfig or the amqpsDeviceAuthentication parameter is null.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfAmqpsDeviceAuthenticationIsNull() throws IllegalArgumentException
    {
        // arrange
        // act
        new AmqpsSessionDeviceOperation(mockDeviceClientConfig, null);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_002: [The constructor shall save the deviceClientConfig and amqpsDeviceAuthentication parameter value to a member variable.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_003: [The constructor shall create AmqpsDeviceTelemetry, AmqpsDeviceMethods and AmqpsDeviceTwin and add them to the device operations list.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_047: [The constructor shall set the authentication state to authenticated if the authentication type is not CBS.]
    @Test
    public void constructorSuccessSAS() throws IllegalArgumentException
    {
        // act
        AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthenticationCBS);

        // assert
        DeviceClientConfig actualDeviceClientConfig = Deencapsulation.getField(amqpsSessionDeviceOperation, "deviceClientConfig");
        AmqpsDeviceAuthentication actualAmqpsDeviceAuthentication = Deencapsulation.getField(amqpsSessionDeviceOperation, "amqpsDeviceAuthentication");
        AmqpsDeviceAuthenticationState authenticatorState = Deencapsulation.getField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState");

        assertEquals(mockDeviceClientConfig, actualDeviceClientConfig);
        assertEquals(mockAmqpsDeviceAuthenticationCBS, actualAmqpsDeviceAuthentication);
        assertEquals(AmqpsDeviceAuthenticationState.AUTHENTICATED, authenticatorState);
        new Verifications()
        {
            {
                Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
                times = 1;
                Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
                times = 1;
                Deencapsulation.newInstance(AmqpsDeviceTwin.class, mockDeviceClientConfig);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_002: [The constructor shall save the deviceClientConfig and amqpsDeviceAuthentication parameter value to a member variable.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_003: [The constructor shall create AmqpsDeviceTelemetry, AmqpsDeviceMethods and AmqpsDeviceTwin and add them to the device operations list. ]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_004: [The constructor shall set the authentication state to not authenticated if the authentication type is CBS.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_044: [The constructor shall calculate the token renewal period as the 75% of the expiration period.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_048: [The constructor saves the calculated renewal period if it is greater than zero.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_045: [The constructor shall create AmqpsDeviceAuthenticationCBSTokenRenewalTask if the authentication type is CBS.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_046: [The constructor shall create and start a scheduler with the calculated renewal period for AmqpsDeviceAuthenticationCBSTokenRenewalTask if the authentication type is CBS.]
    @Test
    public void constructorSuccessCBS() throws IllegalArgumentException
    {
        // arrange
        final long tokenValidSecs = 3600;
        final long expectedRenewalTimeMillisecs = 2700 * 1000;

        new NonStrictExpectations()
        {
            {
                mockDeviceClientConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockDeviceClientConfig.getSasTokenAuthentication();
                result = mockIotHubSasTokenAuthenticationProvider;
                mockIotHubSasTokenAuthenticationProvider.getTokenValidSecs();
                result = tokenValidSecs;

                new AmqpsDeviceAuthenticationCBSTokenRenewalTask((AmqpsSessionDeviceOperation)any);
                result = mockAmqpsDeviceAuthenticationCBSTokenRenewalTask;
                mockExecutors.newScheduledThreadPool(1);
                result = mockScheduledExecutorService;
                mockScheduledExecutorService.scheduleAtFixedRate(mockAmqpsDeviceAuthenticationCBSTokenRenewalTask, 0, expectedRenewalTimeMillisecs, TimeUnit.MILLISECONDS);
            }
        };

        // act
        AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthenticationCBS);

        // assert
        DeviceClientConfig actualDeviceClientConfig = Deencapsulation.getField(amqpsSessionDeviceOperation, "deviceClientConfig");
        AmqpsDeviceAuthentication actualAmqpsDeviceAuthentication = Deencapsulation.getField(amqpsSessionDeviceOperation, "amqpsDeviceAuthentication");
        AmqpsDeviceAuthenticationState authenticatorState = Deencapsulation.getField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState");
        long actualRenewalPeriod = Deencapsulation.getField(amqpsSessionDeviceOperation, "tokenRenewalPeriodInMillisecSecs");

        assertEquals(mockDeviceClientConfig, actualDeviceClientConfig);
        assertEquals(mockAmqpsDeviceAuthenticationCBS, actualAmqpsDeviceAuthentication);
        assertEquals(AmqpsDeviceAuthenticationState.NOT_AUTHENTICATED, authenticatorState);
        assertEquals(actualRenewalPeriod, expectedRenewalTimeMillisecs);
        new Verifications()
        {
            {
                Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
                times = 1;
                Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
                times = 1;
                Deencapsulation.newInstance(AmqpsDeviceTwin.class, mockDeviceClientConfig);
                times = 1;
                mockExecutors.newScheduledThreadPool(1);
                times = 1;
                mockScheduledExecutorService.scheduleAtFixedRate(mockAmqpsDeviceAuthenticationCBSTokenRenewalTask, 0, expectedRenewalTimeMillisecs, TimeUnit.MILLISECONDS);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_058: [The function shall shut down the executor threads.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_059: [The function shall close the operation links.]
    @Test
    public void close()
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "deviceClientConfig", mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "taskSchedulerTokenRenewal", mockScheduledExecutorService);

        // act
        amqpsSessionDeviceOperation.close();

        // assert
        new Verifications()
        {
            {
                mockScheduledExecutorService.shutdownNow();
                times = 1;
                Deencapsulation.invoke(mockAmqpsDeviceOperations, "closeLinks");
                times = 3;
            }
        };
    }


    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_005: [The function shall set the authentication state to not authenticated if the authentication type is CBS.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_006: [The function shall start the authentication if the authentication type is CBS.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_060: [The function shall create a new UUID and add it to the correlationIdList if the authentication type is CBS.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_061: [The function shall use the correlationID to call authenticate on the authentication object if the authentication type is CBS.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_062: [The function shall start the authentication process and start the lock wait if the authentication type is CBS.]
    @Test
    public void authenticateCBS() throws IllegalArgumentException, IOException, InterruptedException
    {
        // arrange
        final int MAX_WAIT_TO_AUTHENTICATE = 10*1000;
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "deviceClientConfig", mockDeviceClientConfig);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "authenticationLock", mockLock);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "cbsCorrelationIdList", mockListUUID);

        new NonStrictExpectations()
        {
            {
                mockDeviceClientConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                UUID.randomUUID();
                result = mockUUID;
            }
        };

        // act
        amqpsSessionDeviceOperation.authenticate();

        // assert
        AmqpsDeviceAuthenticationState authenticatorState = Deencapsulation.getField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState");

        assertEquals(AmqpsDeviceAuthenticationState.AUTHENTICATING, authenticatorState);
        new Verifications()
        {
            {
                mockListUUID.add(mockUUID);
                times = 1;
                Deencapsulation.invoke(mockAmqpsDeviceAuthentication, "authenticate", mockDeviceClientConfig, mockUUID);
                times = 1;
                mockLock.waitLock(MAX_WAIT_TO_AUTHENTICATE);
            }
        };
    }

    @Test (expected = IOException.class)
    public void authenticateLockThrows() throws IllegalArgumentException, IOException, InterruptedException
    {
        // arrange
        final int MAX_WAIT_TO_AUTHENTICATE = 10*1000;
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "authenticationLock", mockLock);

        new NonStrictExpectations()
        {
            {
                mockDeviceClientConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockLock.waitLock(MAX_WAIT_TO_AUTHENTICATE);
                result = new InterruptedException();
            }
        };

        // act
        amqpsSessionDeviceOperation.authenticate();
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_050: [The function shall renew the sas token if the authentication type is CBS and the authentication state is authenticated.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_051: [The function start the authentication with the new token.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_052: [The function shall restart the scheduler with the calculated renewal period if the authentication type is CBS. ]
    @Test
    public void renewTokenSuccess() throws IllegalArgumentException, IOException
    {
        // arrange
        final long tokenValidSecs = 3600;
        final long expectedRenewalTimeMillisecs = 2700 * 1000;
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "taskSchedulerTokenRenewal", mockScheduledExecutorService);

        new NonStrictExpectations()
        {
            {
                mockDeviceClientConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockDeviceClientConfig.getSasTokenAuthentication();
                result = mockIotHubSasTokenAuthenticationProvider;
                mockIotHubSasTokenAuthenticationProvider.getTokenValidSecs();
                result = tokenValidSecs;
            }
        };

        // act
        amqpsSessionDeviceOperation.renewToken();

        // assert
        AmqpsDeviceAuthenticationState authenticatorState = Deencapsulation.getField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState");

        assertEquals(AmqpsDeviceAuthenticationState.AUTHENTICATING, authenticatorState);

        new Verifications()
        {
            {
                mockScheduledExecutorService.shutdownNow();
                times = 1;
                mockScheduledExecutorService.scheduleAtFixedRate((AmqpsDeviceAuthenticationCBSTokenRenewalTask)any, 0, expectedRenewalTimeMillisecs, TimeUnit.MILLISECONDS);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_050: [The function shall renew the sas token if the authentication type is CBS and the authentication state is authenticated.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_051: [The function start the authentication with the new token.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_052: [The function shall restart the scheduler with the calculated renewal period if the authentication type is CBS. ]
    @Test (expected = IllegalArgumentException.class)
    public void renewTokenCalculationNegative() throws IllegalArgumentException, IOException
    {
        // arrange
        final long tokenValidSecs = 3600;
        final long expectedRenewalTimeMillisecs = 2700 * 1000;
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "taskSchedulerTokenRenewal", mockScheduledExecutorService);

        new NonStrictExpectations()
        {
            {
                mockDeviceClientConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockDeviceClientConfig.getSasTokenAuthentication();
                result = mockIotHubSasTokenAuthenticationProvider;
                mockIotHubSasTokenAuthenticationProvider.getTokenValidSecs();
                result = -1;
            }
        };

        // act
        amqpsSessionDeviceOperation.renewToken();

        // assert
        AmqpsDeviceAuthenticationState authenticatorState = Deencapsulation.getField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState");

        assertEquals(AmqpsDeviceAuthenticationState.AUTHENTICATED, authenticatorState);

        new Verifications()
        {
            {
                mockScheduledExecutorService.shutdownNow();
                times = 0;
                mockExecutors.newScheduledThreadPool(1);
                times = 0;
                mockScheduledExecutorService.scheduleAtFixedRate((AmqpsDeviceAuthenticationCBSTokenRenewalTask)any, 0, expectedRenewalTimeMillisecs, TimeUnit.MILLISECONDS);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_007: [The function shall return the current authentication state.]
    @Test
    public void getAmqpsAuthenticatorState()
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.UNKNOWN);

        // act
        AmqpsDeviceAuthenticationState authenticatorState = amqpsSessionDeviceOperation.getAmqpsAuthenticatorState();

        // assert
        assertEquals(AmqpsDeviceAuthenticationState.UNKNOWN, authenticatorState);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_008: [The function shall return true if all operation links are opene, otherwise return false.]
    @Test
    public void operationLinksOpenedTrue() throws IllegalArgumentException, IOException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        ArrayList<AmqpsDeviceOperations> operationList = new ArrayList<>();
        operationList.add(mockAmqpsDeviceTelemetry);
        operationList.add(mockAmqpsDeviceMethods);
        operationList.add(mockAmqpsDeviceTwin);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsDeviceOperationsList", operationList);

        new NonStrictExpectations()
        {
            {
                mockAmqpsDeviceTelemetry.operationLinksOpened();
                result = true;
                mockAmqpsDeviceMethods.operationLinksOpened();
                result = true;
                mockAmqpsDeviceTwin.operationLinksOpened();
                result = true;
            }
        };

        // act
        Boolean actualOperationLinkOpened = amqpsSessionDeviceOperation.operationLinksOpened();

        // assert

        assertEquals(true, actualOperationLinkOpened);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_008: [The function shall return true if all operation links are opene, otherwise return false.]
    @Test
    public void operationLinksOpenedFalse() throws IllegalArgumentException, IOException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        ArrayList<AmqpsDeviceOperations> operationList = new ArrayList<>();
        operationList.add(mockAmqpsDeviceTelemetry);
        operationList.add(mockAmqpsDeviceMethods);
        operationList.add(mockAmqpsDeviceTwin);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsDeviceOperationsList", operationList);

        new NonStrictExpectations()
        {
            {
                mockAmqpsDeviceTelemetry.operationLinksOpened();
                result = false;
                mockAmqpsDeviceMethods.operationLinksOpened();
                result = true;
                mockAmqpsDeviceTwin.operationLinksOpened();
                result = true;
            }
        };

        // act
        Boolean actualOperationLinkOpened = amqpsSessionDeviceOperation.operationLinksOpened();

        // assert
        assertEquals(false, actualOperationLinkOpened);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_009: [The function shall call openLinks on all device operations if the authentication state is authenticated.]
    @Test
    public void openLinks() throws IllegalArgumentException, IOException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);

        // act
        Deencapsulation.invoke(amqpsSessionDeviceOperation, "openLinks", mockSession);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceOperations, "openLinks", mockSession);
                times = 3;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_010: [The function shall call closeLinks on all device operations.]
    @Test
    public void closeLinks() throws IllegalArgumentException, IOException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);

        // act
        Deencapsulation.invoke(amqpsSessionDeviceOperation, "closeLinks");

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceOperations, "closeLinks");
                times = 3;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_011: [The function shall call initLink on all device operations.**]**]
    @Test
    public void initLink() throws IllegalArgumentException, IOException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);

        // act
        Deencapsulation.invoke(amqpsSessionDeviceOperation, "initLink", mockLink);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceOperations, "initLink", mockLink);
                times = 3;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_012: [The function shall return -1 if the state is not authenticated.]
    @Test
    public void sendMessageNotAuthenticated() throws IllegalArgumentException, IOException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATING);

        // act
        Integer deliveryHash = Deencapsulation.invoke(amqpsSessionDeviceOperation, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, mockIotHubConnectionString);

        // assert
        assertTrue(deliveryHash == -1);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_013: [The function shall return -1 if the deviceId int he connection string is not equal to the deviceId in the config.]
    @Test
    public void sendMessageDeviceIdMismatch() throws IllegalArgumentException, IOException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);

        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.getDeviceId();
                result = "deviceId1";
                mockDeviceClientConfig.getDeviceId();
                result = "deviceId2";
            }
        };

        // act
        Integer deliveryHash = Deencapsulation.invoke(amqpsSessionDeviceOperation, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, mockIotHubConnectionString);

        // assert
        assertTrue(deliveryHash == -1);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_014: [The function shall encode the message and copy the contents to the byte buffer.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_017: [The function shall set the delivery tag for the sender.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_018: [The function shall call sendMessageAndGetDeliveryHash on all device operation objects.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_019: [The function shall return the delivery hash.]
    @Test
    public void sendMessageNoDelivery() throws IllegalArgumentException, IOException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);
        final byte[] bytes = new byte[1024];
        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.getDeviceId();
                result = "deviceId";
                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";

                mockProtonMessage.encode(bytes, anyInt, anyInt);

                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "sendMessageAndGetDeliveryHash", MessageType.DEVICE_TELEMETRY, bytes, anyInt, anyInt, bytes);
                result = mockAmqpsSendReturnValue;
                Deencapsulation.invoke(mockAmqpsSendReturnValue, "isDeliverySuccessful");
                result = false;
            }
        };

        // act
        Integer actualDeliveryHash = Deencapsulation.invoke(amqpsSessionDeviceOperation, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, mockIotHubConnectionString);

        // assert
        assertTrue(actualDeliveryHash == -1);
        new Verifications()
        {
            {
                mockProtonMessage.encode(bytes, anyInt, anyInt);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_014: [The function shall encode the message and copy the contents to the byte buffer.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_017: [The function shall set the delivery tag for the sender.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_018: [The function shall call sendMessageAndGetDeliveryHash on all device operation objects.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_019: [The function shall return the delivery hash.]
    @Test
    public void sendMessageSuccess() throws IllegalArgumentException, IOException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);
        final byte[] bytes = new byte[1024];
        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.getDeviceId();
                result = "deviceId";
                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";

                mockProtonMessage.encode(bytes, anyInt, anyInt);

                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "sendMessageAndGetDeliveryHash", MessageType.DEVICE_TELEMETRY, bytes, anyInt, anyInt, bytes);
                result = mockAmqpsSendReturnValue;
                Deencapsulation.invoke(mockAmqpsSendReturnValue, "isDeliverySuccessful");
                result = true;
            }
        };

        // act
        Integer actualDeliveryHash = Deencapsulation.invoke(amqpsSessionDeviceOperation, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, mockIotHubConnectionString);

        // assert
        assertTrue(actualDeliveryHash != -1);
        new Verifications()
        {
            {
                mockProtonMessage.encode(bytes, anyInt, anyInt);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_015: [The function shall doubles the buffer if encode throws BufferOverflowException.]
    @Test
    public void sendMessageDoublesBufferIfEncodeThrowsBufferOverflowException() throws IllegalArgumentException, IOException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);
        final byte[] bytes = new byte[1024];
        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.getDeviceId();
                result = "deviceId";
                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";

                mockProtonMessage.encode(bytes, anyInt, anyInt);
                result = new BufferOverflowException();

                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "sendMessageAndGetDeliveryHash", MessageType.DEVICE_TELEMETRY, bytes, anyInt, anyInt, bytes);
                result = mockAmqpsSendReturnValue;
                Deencapsulation.invoke(mockAmqpsSendReturnValue, "isDeliverySuccessful");
                result = true;
            }
        };

        // act
        Integer actualDeliveryHash = Deencapsulation.invoke(amqpsSessionDeviceOperation, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, mockIotHubConnectionString);

        // assert
        assertTrue(actualDeliveryHash != -1);
        new Verifications()
        {
            {
                mockProtonMessage.encode(bytes, anyInt, anyInt);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_020: [The function shall return null if the state is not authenticated or authenticating.]
    @Test
    public void getMessageFromReceiverLinkNotAuthenticated() throws IllegalArgumentException, IOException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.NOT_AUTHENTICATED);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "getMessageFromReceiverLink", linkName);
                result = mockAmqpsMessage;
            }
        };

        // act
        AmqpsMessage actualAmqpsMessage = Deencapsulation.invoke(amqpsSessionDeviceOperation, "getMessageFromReceiverLink", linkName);

        // assert
        assertEquals(actualAmqpsMessage, mockAmqpsMessage);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_021: [If the state is authenticated the function shall call getMessageFromReceiverLink on all device operations and return with the success if any.]
    @Test
    public void getMessageFromReceiverLinkAuthenticated() throws IllegalArgumentException, IOException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "getMessageFromReceiverLink", linkName);
                result = mockAmqpsMessage;
            }
        };

        // act
        AmqpsMessage actualAmqpsMessage = Deencapsulation.invoke(amqpsSessionDeviceOperation, "getMessageFromReceiverLink", linkName);

        // assert
        assertNotNull(actualAmqpsMessage);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_021: [If the state is authenticated the function shall call getMessageFromReceiverLink on all device operations and return with the success if any.]
    @Test
    public void getMessageFromReceiverLinkAuthenticatedReturnNull() throws IllegalArgumentException, IOException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "getMessageFromReceiverLink", linkName);
                result = null;
                Deencapsulation.invoke(mockAmqpsDeviceMethods, "getMessageFromReceiverLink", linkName);
                result = null;
                Deencapsulation.invoke(mockAmqpsDeviceTwin, "getMessageFromReceiverLink", linkName);
                result = null;
            }
        };

        // act
        AmqpsMessage actualAmqpsMessage = Deencapsulation.invoke(amqpsSessionDeviceOperation, "getMessageFromReceiverLink", linkName);

        // assert
        assertNull(actualAmqpsMessage);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_023: [If the state is authenticating the function shall call getMessageFromReceiverLink on the authentication object.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_055: [The function shall find the correlation ID in the correlationIdlist.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_053: [The function shall call authenticationMessageReceived with the correlation ID on the authentication object and if it returns true set the authentication state to authenticated.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_054: [The function shall call notify the lock if after receiving the message and the authentication is in authenticating state.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_056: [The function shall remove the correlationId from the list if it is found.]
    @Test
    public void getMessageFromReceiverLinkAuthenticating() throws IllegalArgumentException, IOException
    {
        // arrange
        final String linkName = "linkName";
        final String propertyKey = "status-code";
        final Integer propertyValue = 200;
        final List<UUID> cbsCorrelationIdList = Collections.synchronizedList(new ArrayList<UUID>());
        cbsCorrelationIdList.add(mockUUID);
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATING);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "cbsCorrelationIdList", cbsCorrelationIdList);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "authenticationLock", mockLock);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthentication, "getMessageFromReceiverLink", linkName);
                result = mockAmqpsMessage;
                Deencapsulation.invoke(mockAmqpsDeviceAuthentication, "authenticationMessageReceived", mockAmqpsMessage, mockUUID);
                result = true;
            }
        };

        // act
        AmqpsMessage actualAmqpsMessage = Deencapsulation.invoke(amqpsSessionDeviceOperation, "getMessageFromReceiverLink", linkName);

        // assert
        AmqpsDeviceAuthenticationState actualAmqpsDeviceAuthenticationState = Deencapsulation.getField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState");
        assertNotNull(actualAmqpsMessage);
        assertTrue(actualAmqpsDeviceAuthenticationState == actualAmqpsDeviceAuthenticationState.AUTHENTICATED);

        new Verifications()
        {
            {
                mockLock.notifyLock();
                times = 1;
                cbsCorrelationIdList.remove(mockUUID);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_023: [If the state is authenticating the function shall call getMessageFromReceiverLink on the authentication object.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_055: [The function shall find the correlation ID in the correlationIdlist.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_053: [The function shall call authenticationMessageReceived with the correlation ID on the authentication object and if it returns true set the authentication state to authenticated.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_054: [The function shall call notify the lock if after receiving the message and the authentication is in authenticating state.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_056: [The function shall remove the correlationId from the list if it is found.]
    @Test
    public void getMessageFromReceiverLinkAuthenticatingNoUUID() throws IllegalArgumentException, IOException
    {
        // arrange
        final String linkName = "linkName";
        final String propertyKey = "status-code";
        final Integer propertyValue = 200;
        final List<UUID> cbsCorrelationIdList = Collections.synchronizedList(new ArrayList<UUID>());
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATING);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "cbsCorrelationIdList", cbsCorrelationIdList);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "authenticationLock", mockLock);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthentication, "getMessageFromReceiverLink", linkName);
                result = mockAmqpsMessage;
                Deencapsulation.invoke(mockAmqpsDeviceAuthentication, "authenticationMessageReceived", mockAmqpsMessage, mockUUID);
                result = true;
            }
        };

        // act
        AmqpsMessage actualAmqpsMessage = Deencapsulation.invoke(amqpsSessionDeviceOperation, "getMessageFromReceiverLink", linkName);

        // assert
        AmqpsDeviceAuthenticationState actualAmqpsDeviceAuthenticationState = Deencapsulation.getField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState");
        assertNotNull(actualAmqpsMessage);
        assertTrue(actualAmqpsDeviceAuthenticationState == actualAmqpsDeviceAuthenticationState.AUTHENTICATING);

        new Verifications()
        {
            {
                mockLock.notifyLock();
                times = 0;
                cbsCorrelationIdList.remove(mockUUID);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_023: [If the state is authenticating the function shall call getMessageFromReceiverLink on the authentication object.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_055: [The function shall find the correlation ID in the correlationIdlist.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_053: [The function shall call authenticationMessageReceived with the correlation ID on the authentication object and if it returns true set the authentication state to authenticated.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_054: [The function shall call notify the lock if after receiving the message and the authentication is in authenticating state.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_056: [The function shall remove the correlationId from the list if it is found.]
    @Test
    public void getMessageFromReceiverLinkAuthenticatingNotAuthenticationMessage() throws IllegalArgumentException, IOException
    {
        // arrange
        final String linkName = "linkName";
        final String propertyKey = "status-code";
        final Integer propertyValue = 200;
        final List<UUID> cbsCorrelationIdList = Collections.synchronizedList(new ArrayList<UUID>());
        cbsCorrelationIdList.add(mockUUID);
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATING);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "cbsCorrelationIdList", cbsCorrelationIdList);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "authenticationLock", mockLock);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthentication, "getMessageFromReceiverLink", linkName);
                result = mockAmqpsMessage;
                Deencapsulation.invoke(mockAmqpsDeviceAuthentication, "authenticationMessageReceived", mockAmqpsMessage, mockUUID);
                result = false;
            }
        };

        // act
        AmqpsMessage actualAmqpsMessage = Deencapsulation.invoke(amqpsSessionDeviceOperation, "getMessageFromReceiverLink", linkName);

        // assert
        AmqpsDeviceAuthenticationState actualAmqpsDeviceAuthenticationState = Deencapsulation.getField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState");
        assertNotNull(actualAmqpsMessage);
        assertTrue(actualAmqpsDeviceAuthenticationState == actualAmqpsDeviceAuthenticationState.AUTHENTICATING);

        new Verifications()
        {
            {
                mockLock.notifyLock();
                times = 0;
                cbsCorrelationIdList.remove(mockUUID);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_057: [If the state is other than authenticating the function shall try to read the message from the device operation objects.]
    @Test
    public void getMessageFromReceiverLinkNotAuthenticating() throws IllegalArgumentException, IOException
    {
        // arrange
        final String linkName = "linkName";
        final String propertyKey = "status-code";
        final Integer propertyValue = 200;
        final List<UUID> cbsCorrelationIdList = Collections.synchronizedList(new ArrayList<UUID>());
        cbsCorrelationIdList.add(mockUUID);
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.UNKNOWN);
        ArrayList<AmqpsDeviceOperations> operationList = new ArrayList<>();
        operationList.add(mockAmqpsDeviceTelemetry);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsDeviceOperationsList", operationList);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "getMessageFromReceiverLink", linkName);
                result = mockAmqpsMessage;
            }
        };

        // act
        AmqpsMessage actualAmqpsMessage = Deencapsulation.invoke(amqpsSessionDeviceOperation, "getMessageFromReceiverLink", linkName);

        // assert
        assertEquals(actualAmqpsMessage, mockAmqpsMessage);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "getMessageFromReceiverLink", linkName);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_024: [The function shall return true if any of the operation's link name is a match and return false otherwise.]
    @Test
    public void isLinkFoundTrue()
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "isLinkFound", linkName);
                result = true;
            }
        };

        // act
        Boolean isFound = Deencapsulation.invoke(amqpsSessionDeviceOperation, "isLinkFound", linkName);

        // assert
        assertTrue(isFound);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_024: [The function shall return true if any of the operation's link name is a match and return false otherwise.]
    @Test
    public void isLinkFoundFalse()
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "isLinkFound", linkName);
                result = false;
            }
        };

        // act
        Boolean isFound = Deencapsulation.invoke(amqpsSessionDeviceOperation, "isLinkFound", linkName);

        // assert
        assertFalse(isFound);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_040: [The function shall call all device operation's convertToProton, and if any of them not null return with the value.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_041: [The function shall call all device operation's convertFromProton, and if any of them not null return with the value.]
    @Test
    public void convertToProtonSuccess() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "convertToProton", mockMessage);
                result = mockAmqpsConvertToProtonReturnValue;
            }
        };

        // act
        AmqpsConvertToProtonReturnValue actualAmqpsConvertToProtonReturnValue = Deencapsulation.invoke(amqpsSessionDeviceOperation, "convertToProton", mockMessage);

        // assert
        assertTrue(mockAmqpsConvertToProtonReturnValue == actualAmqpsConvertToProtonReturnValue);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_040: [The function shall call all device operation's convertToProton, and if any of them not null return with the value.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_041: [The function shall call all device operation's convertFromProton, and if any of them not null return with the value.]
    @Test
    public void convertFromoProtonSuccess() throws IllegalArgumentException, InterruptedException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);
                result = mockAmqpsConvertFromProtonReturnValue;
            }
        };

        // act
        AmqpsConvertFromProtonReturnValue actualAmqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsSessionDeviceOperation, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);

        // assert
        assertTrue(mockAmqpsConvertFromProtonReturnValue == actualAmqpsConvertFromProtonReturnValue);
    }
}
