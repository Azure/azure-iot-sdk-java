package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import mockit.*;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Session;
import org.junit.Test;

import java.nio.BufferOverflowException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_METHODS;
import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_TELEMETRY;
import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_TWIN;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

/**
 * Unit tests for DeviceClient.
 * Methods: 88%
 * Lines: 91%
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
    UUID mockUUID;

    @Mocked
    CountDownLatch mockCountDownLatch;

    @Mocked
    List<UUID> mockListUUID;

    @Mocked
    Event mockEvent;
    
    @Mocked
    SubscriptionMessageRequestSentCallback mockedSubscriptionMessageRequestSentCallback;


    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_001: [The constructor shall throw IllegalArgumentException if the deviceClientConfig or the amqpsDeviceAuthentication parameter is null.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfDeviceClientIsNull() throws IllegalArgumentException, TransportException
    {
        // arrange
        // act
        new AmqpsSessionDeviceOperation(null, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_001: [The constructor shall throw IllegalArgumentException if the deviceClientConfig or the amqpsDeviceAuthentication parameter is null.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfAmqpsDeviceAuthenticationIsNull() throws IllegalArgumentException, TransportException
    {
        // arrange
        // act
        new AmqpsSessionDeviceOperation(mockDeviceClientConfig, null, mockedSubscriptionMessageRequestSentCallback);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_002: [The constructor shall save the deviceClientConfig and amqpsDeviceAuthentication parameter value to a member variable.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_003: [The constructor shall create AmqpsDeviceTelemetry, AmqpsDeviceMethods and AmqpsDeviceTwin and add them to the device operations list.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_047: [The constructor shall set the authentication state to authenticated if the authentication type is not CBS.]
    @Test
    public void constructorSuccessSAS() throws IllegalArgumentException, TransportException
    {
        // act
        AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthenticationCBS, mockedSubscriptionMessageRequestSentCallback);

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
                times = 0;
                Deencapsulation.newInstance(AmqpsDeviceTwin.class, mockDeviceClientConfig);
                times = 0;
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
    public void constructorSuccessCBS() throws IllegalArgumentException, TransportException
    {
        // arrange
        final long tokenValidSecs = 3600;
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
        AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthenticationCBS, mockedSubscriptionMessageRequestSentCallback);

        // assert
        DeviceClientConfig actualDeviceClientConfig = Deencapsulation.getField(amqpsSessionDeviceOperation, "deviceClientConfig");
        AmqpsDeviceAuthentication actualAmqpsDeviceAuthentication = Deencapsulation.getField(amqpsSessionDeviceOperation, "amqpsDeviceAuthentication");
        AmqpsDeviceAuthenticationState authenticatorState = Deencapsulation.getField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState");

        assertEquals(mockDeviceClientConfig, actualDeviceClientConfig);
        assertEquals(mockAmqpsDeviceAuthenticationCBS, actualAmqpsDeviceAuthentication);
        assertEquals(AmqpsDeviceAuthenticationState.NOT_AUTHENTICATED, authenticatorState);
        new Verifications()
        {
            {
                Deencapsulation.newInstance(AmqpsDeviceTelemetry.class, mockDeviceClientConfig);
                times = 1;
                Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
                times = 0;
                Deencapsulation.newInstance(AmqpsDeviceTwin.class, mockDeviceClientConfig);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_058: [The function shall shut down the executor threads.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_059: [The function shall close the operation links.]
    @Test
    public void close() throws TransportException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "deviceClientConfig", mockDeviceClientConfig);

        // act
        amqpsSessionDeviceOperation.close();

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceOperations, "closeLinks");
                times = 1;
            }
        };
    }


    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_005: [The function shall set the authentication state to not authenticated if the authentication type is CBS.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_006: [The function shall start the authentication if the authentication type is CBS.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_060: [The function shall create a new UUID and add it to the correlationIdList if the authentication type is CBS.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_061: [The function shall use the correlationID to call authenticate on the authentication object if the authentication type is CBS.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_062: [The function shall start the authentication process and start the lock wait if the authentication type is CBS.]
    @Test
    public void authenticateCBS() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final int MAX_WAIT_TO_AUTHENTICATE = 10*1000;
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "deviceClientConfig", mockDeviceClientConfig);
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
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_009: [The function shall call openLinks on all device operations if the authentication state is authenticated.]
    @Test
    public void openLinks() throws IllegalArgumentException, TransportException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);

        // act
        Deencapsulation.invoke(amqpsSessionDeviceOperation, "openLinks", mockSession);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceOperations, "openLinks", mockSession);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_010: [The function shall call closeLinks on all device operations.]
    @Test
    public void closeLinks() throws IllegalArgumentException, TransportException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);

        // act
        Deencapsulation.invoke(amqpsSessionDeviceOperation, "closeLinks");

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceOperations, "closeLinks");
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_011: [The function shall call initLink on all device operations.**]**]
    @Test
    public void initLink() throws IllegalArgumentException, TransportException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);

        // act
        Deencapsulation.invoke(amqpsSessionDeviceOperation, "initLink", mockLink);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceOperations, "initLink", mockLink);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_012: [The function shall return -1 if the state is not authenticated.]
    @Test
    public void sendMessageNotAuthenticated() throws IllegalArgumentException, TransportException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATING);

        // act
        Integer deliveryHash = Deencapsulation.invoke(amqpsSessionDeviceOperation, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");

        // assert
        assertTrue(deliveryHash == -1);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_013: [The function shall return -1 if the deviceId int he connection string is not equal to the deviceId in the config.]
    @Test
    public void sendMessageDeviceIdMismatch() throws IllegalArgumentException, TransportException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
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
        Integer deliveryHash = Deencapsulation.invoke(amqpsSessionDeviceOperation, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");

        // assert
        assertTrue(deliveryHash == -1);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_014: [The function shall encode the message and copy the contents to the byte buffer.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_017: [The function shall set the delivery tag for the sender.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_018: [The function shall call sendMessageAndGetDeliveryTag on all device operation objects.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_019: [The function shall return the delivery hash.]
    @Test
    public void sendMessageNoDelivery() throws IllegalArgumentException, TransportException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
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

                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "sendMessageAndGetDeliveryTag", MessageType.DEVICE_TELEMETRY, bytes, anyInt, anyInt, bytes);
                result = mockAmqpsSendReturnValue;
                Deencapsulation.invoke(mockAmqpsSendReturnValue, "isDeliverySuccessful");
                result = false;

                mockDeviceClientConfig.getDeviceId();
                result = "someDeviceId";
            }
        };

        // act
        Integer actualDeliveryHash = Deencapsulation.invoke(amqpsSessionDeviceOperation, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");

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
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_018: [The function shall call sendMessageAndGetDeliveryTag on all device operation objects.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_019: [The function shall return the delivery hash.]
    @Test
    public void sendMessageSuccess() throws IllegalArgumentException, TransportException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
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

                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "sendMessageAndGetDeliveryTag", MessageType.DEVICE_TELEMETRY, bytes, anyInt, anyInt, bytes);
                result = mockAmqpsSendReturnValue;
                Deencapsulation.invoke(mockAmqpsSendReturnValue, "isDeliverySuccessful");
                result = true;
                mockAmqpsSendReturnValue.getDeliveryTag();
                result = "12".getBytes();

                mockDeviceClientConfig.getDeviceId();
                result = "someDeviceId";
            }
        };

        // act
        Integer actualDeliveryHash = Deencapsulation.invoke(amqpsSessionDeviceOperation, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");

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
    public void sendMessageDoublesBufferIfEncodeThrowsBufferOverflowException() throws IllegalArgumentException, TransportException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
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

                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "sendMessageAndGetDeliveryTag", MessageType.DEVICE_TELEMETRY, bytes, anyInt, anyInt, bytes);
                result = mockAmqpsSendReturnValue;
                Deencapsulation.invoke(mockAmqpsSendReturnValue, "isDeliverySuccessful");
                result = true;

                mockDeviceClientConfig.getDeviceId();
                result = "someDeviceId";

                mockAmqpsSendReturnValue.getDeliveryTag();
                result = "12".getBytes();
            }
        };

        // act
        Integer actualDeliveryHash = Deencapsulation.invoke(amqpsSessionDeviceOperation, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");

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
    public void getMessageFromReceiverLinkNotAuthenticated() throws IllegalArgumentException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
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
    public void getMessageFromReceiverLinkAuthenticated() throws IllegalArgumentException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
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
    public void getMessageFromReceiverLinkAuthenticatedReturnNull() throws IllegalArgumentException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
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
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_053: [The function shall call handleAuthenticationMessage with the correlation ID on the authentication object and if it returns true set the authentication state to authenticated.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_054: [The function shall call notify the lock if after receiving the message and the authentication is in authenticating state.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_056: [The function shall remove the correlationId from the list if it is found.]
    @Test
    public void getMessageFromReceiverLinkAuthenticatingNoUUID() throws IllegalArgumentException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final String propertyKey = "status-code";
        final Integer propertyValue = 200;
        final List<UUID> cbsCorrelationIdList = Collections.synchronizedList(new ArrayList<UUID>());
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATING);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "cbsCorrelationIdList", cbsCorrelationIdList);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthentication, "getMessageFromReceiverLink", linkName);
                result = mockAmqpsMessage;
                Deencapsulation.invoke(mockAmqpsDeviceAuthentication, "handleAuthenticationMessage", mockAmqpsMessage, mockUUID);
                result = true;
            }
        };

        // act
        AmqpsMessage actualAmqpsMessage = Deencapsulation.invoke(amqpsSessionDeviceOperation, "getMessageFromReceiverLink", linkName);

        // assert
        AmqpsDeviceAuthenticationState actualAmqpsDeviceAuthenticationState = Deencapsulation.getField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState");
        assertNotNull(actualAmqpsMessage);
        assertTrue(actualAmqpsDeviceAuthenticationState == actualAmqpsDeviceAuthenticationState.AUTHENTICATING);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_023: [If the state is authenticating the function shall call getMessageFromReceiverLink on the authentication object.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_055: [The function shall find the correlation ID in the correlationIdlist.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_053: [The function shall call handleAuthenticationMessage with the correlation ID on the authentication object and if it returns true set the authentication state to authenticated.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_054: [The function shall call notify the lock if after receiving the message and the authentication is in authenticating state.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_056: [The function shall remove the correlationId from the list if it is found.]
    @Test
    public void getMessageFromReceiverLinkAuthenticatingNotAuthenticationMessage() throws IllegalArgumentException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final String propertyKey = "status-code";
        final Integer propertyValue = 200;
        final List<UUID> cbsCorrelationIdList = Collections.synchronizedList(new ArrayList<UUID>());
        cbsCorrelationIdList.add(mockUUID);
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATING);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsDeviceAuthentication", mockAmqpsDeviceAuthentication);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "cbsCorrelationIdList", cbsCorrelationIdList);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthentication, "getMessageFromReceiverLink", linkName);
                result = mockAmqpsMessage;
                Deencapsulation.invoke(mockAmqpsDeviceAuthentication, "handleAuthenticationMessage", mockAmqpsMessage, mockUUID);
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
                cbsCorrelationIdList.remove(mockUUID);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_057: [If the state is other than authenticating the function shall try to read the message from the device operation objects.]
    @Test
    public void getMessageFromReceiverLinkNotAuthenticating() throws IllegalArgumentException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final String propertyKey = "status-code";
        final Integer propertyValue = 200;
        final List<UUID> cbsCorrelationIdList = Collections.synchronizedList(new ArrayList<UUID>());
        cbsCorrelationIdList.add(mockUUID);
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.UNKNOWN);
        Map<MessageType, AmqpsDeviceOperations> amqpsDeviceOperationsMap = new HashMap<MessageType, AmqpsDeviceOperations>();
        amqpsDeviceOperationsMap.put(DEVICE_TELEMETRY, mockAmqpsDeviceTelemetry);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsDeviceOperationsMap", amqpsDeviceOperationsMap);

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
    public void onLinkRemoteOpenTrue() throws TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "onLinkRemoteOpen", linkName);
                result = true;
            }
        };

        // act
        Boolean isFound = Deencapsulation.invoke(amqpsSessionDeviceOperation, "onLinkRemoteOpen", linkName);

        // assert
        assertTrue(isFound);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_024: [The function shall return true if any of the operation's link name is a match and return false otherwise.]
    @Test
    public void onLinkRemoteOpenFalse() throws TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "onLinkRemoteOpen", linkName);
                result = false;
            }
        };

        // act
        Boolean isFound = Deencapsulation.invoke(amqpsSessionDeviceOperation, "onLinkRemoteOpen", linkName);

        // assert
        assertFalse(isFound);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_040: [The function shall call all device operation's convertToProton, and if any of them not null return with the value.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_041: [The function shall call all device operation's convertFromProton, and if any of them not null return with the value.]
    @Test
    public void convertToProtonSuccess() throws IllegalArgumentException, InterruptedException, TransportException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "convertToProton", mockMessage);
                result = mockAmqpsConvertToProtonReturnValue;
                mockMessage.getMessageType();
                result = DEVICE_TELEMETRY;
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
    public void convertFromProtonSuccess() throws IllegalArgumentException, TransportException
    {
        // arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceTelemetry, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);
                result = mockAmqpsConvertFromProtonReturnValue;
                mockAmqpsMessage.getAmqpsMessageType();
                result = DEVICE_TELEMETRY;
            }
        };

        // act
        AmqpsConvertFromProtonReturnValue actualAmqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsSessionDeviceOperation, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);

        // assert
        assertTrue(mockAmqpsConvertFromProtonReturnValue == actualAmqpsConvertFromProtonReturnValue);
    }

    @Test
    public void handleAuthenticationMessageSuccess()
    {
        //arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        List<UUID> cbsCorrelationIdList = Deencapsulation.getField(amqpsSessionDeviceOperation, "cbsCorrelationIdList");
        final UUID uuid = UUID.randomUUID();
        cbsCorrelationIdList.add(0, uuid);

        new Expectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthentication, "handleAuthenticationMessage", mockAmqpsMessage, uuid);
                result = true;
            }
        };

        //act
        boolean handled = Deencapsulation.invoke(amqpsSessionDeviceOperation, "handleAuthenticationMessage", mockAmqpsMessage);

        //assert
        assertTrue(handled);
    }

    @Test
    public void handleAuthenticationMessageWithNoSavedUUID()
    {
        //arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);

        //intentionally leave this list empty
        List<UUID> cbsCorrelationIdList = Deencapsulation.getField(amqpsSessionDeviceOperation, "cbsCorrelationIdList");

        //act
        boolean handled = Deencapsulation.invoke(amqpsSessionDeviceOperation, "handleAuthenticationMessage", mockAmqpsMessage);

        //assert
        assertFalse(handled);
    }

    @Test
    public void handleAuthenticationMessageFailure()
    {
        //arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        List<UUID> cbsCorrelationIdList = Deencapsulation.getField(amqpsSessionDeviceOperation, "cbsCorrelationIdList");
        final UUID uuid = UUID.randomUUID();
        cbsCorrelationIdList.add(0, uuid);

        new Expectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceAuthentication, "handleAuthenticationMessage", mockAmqpsMessage, uuid);
                result = false;
            }
        };

        //act
        boolean handled = Deencapsulation.invoke(amqpsSessionDeviceOperation, "handleAuthenticationMessage", mockAmqpsMessage);

        //assert
        assertFalse(handled);
    }

    @Test
    public void getExpectedWorkerLinkCountWithTelemetry()
    {
        //arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Map<MessageType, AmqpsDeviceOperations> amqpsDeviceOperationsMap = new HashMap<MessageType, AmqpsDeviceOperations>();
        amqpsDeviceOperationsMap.put(DEVICE_TELEMETRY, mockAmqpsDeviceTelemetry);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsDeviceOperationsMap", amqpsDeviceOperationsMap);

        //act
        int actualExpectedWorkerLinkCount = Deencapsulation.invoke(amqpsSessionDeviceOperation, "getExpectedWorkerLinkCount");

        //assert
        assertEquals(2, actualExpectedWorkerLinkCount);
    }

    @Test
    public void getExpectedWorkerLinkCountWithTwin()
    {
        //arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Map<MessageType, AmqpsDeviceOperations> amqpsDeviceOperationsMap = new HashMap<MessageType, AmqpsDeviceOperations>();
        amqpsDeviceOperationsMap.put(DEVICE_TWIN, mockAmqpsDeviceTwin);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsDeviceOperationsMap", amqpsDeviceOperationsMap);

        //act
        int actualExpectedWorkerLinkCount = Deencapsulation.invoke(amqpsSessionDeviceOperation, "getExpectedWorkerLinkCount");

        //assert
        assertEquals(2, actualExpectedWorkerLinkCount);
    }

    @Test
    public void getExpectedWorkerLinkCountWithMethods()
    {
        //arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Map<MessageType, AmqpsDeviceOperations> amqpsDeviceOperationsMap = new HashMap<MessageType, AmqpsDeviceOperations>();
        amqpsDeviceOperationsMap.put(DEVICE_METHODS, mockAmqpsDeviceMethods);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsDeviceOperationsMap", amqpsDeviceOperationsMap);

        //act
        int actualExpectedWorkerLinkCount = Deencapsulation.invoke(amqpsSessionDeviceOperation, "getExpectedWorkerLinkCount");

        //assert
        assertEquals(2, actualExpectedWorkerLinkCount);
    }

    @Test
    public void getExpectedWorkerLinkCountWithTelemetryAndMethods()
    {
        //arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Map<MessageType, AmqpsDeviceOperations> amqpsDeviceOperationsMap = new HashMap<MessageType, AmqpsDeviceOperations>();
        amqpsDeviceOperationsMap.put(DEVICE_TELEMETRY, mockAmqpsDeviceTelemetry);
        amqpsDeviceOperationsMap.put(DEVICE_METHODS, mockAmqpsDeviceMethods);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsDeviceOperationsMap", amqpsDeviceOperationsMap);

        //act
        int actualExpectedWorkerLinkCount = Deencapsulation.invoke(amqpsSessionDeviceOperation, "getExpectedWorkerLinkCount");

        //assert
        assertEquals(4, actualExpectedWorkerLinkCount);
    }

    @Test
    public void getExpectedWorkerLinkCountWithTelemetryAndMethodsAndTwin()
    {
        //arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Map<MessageType, AmqpsDeviceOperations> amqpsDeviceOperationsMap = new HashMap<MessageType, AmqpsDeviceOperations>();
        amqpsDeviceOperationsMap.put(DEVICE_TELEMETRY, mockAmqpsDeviceTelemetry);
        amqpsDeviceOperationsMap.put(DEVICE_METHODS, mockAmqpsDeviceMethods);
        amqpsDeviceOperationsMap.put(DEVICE_TWIN, mockAmqpsDeviceTwin);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsDeviceOperationsMap", amqpsDeviceOperationsMap);

        //act
        int actualExpectedWorkerLinkCount = Deencapsulation.invoke(amqpsSessionDeviceOperation, "getExpectedWorkerLinkCount");

        //assert
        assertEquals(6, actualExpectedWorkerLinkCount);
    }

    @Test
    public void subscribeToMessageTypeForMethodsSavesInMapAndCallsOpenLinks()
    {
        //arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Map<MessageType, AmqpsDeviceOperations> amqpsDeviceOperationsMap = new HashMap<MessageType, AmqpsDeviceOperations>();
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsDeviceOperationsMap", amqpsDeviceOperationsMap);

        new Expectations()
        {
            {
                Deencapsulation.newInstance(AmqpsDeviceMethods.class, mockDeviceClientConfig);
                result = mockAmqpsDeviceMethods;
            }
        };

        //act
        Deencapsulation.invoke(amqpsSessionDeviceOperation, "subscribeToMessageType", mockSession, DEVICE_METHODS);

        //assert
        assertTrue(amqpsDeviceOperationsMap.containsKey(DEVICE_METHODS));
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceMethods, "openLinks", mockSession);
                times = 1;
            }
        };
    }

    @Test
    public void subscribeToMessageTypeForTwinSavesInMapAndCallsOpenLinks()
    {
        //arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Map<MessageType, AmqpsDeviceOperations> amqpsDeviceOperationsMap = new HashMap<MessageType, AmqpsDeviceOperations>();
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsDeviceOperationsMap", amqpsDeviceOperationsMap);

        new Expectations()
        {
            {
                Deencapsulation.newInstance(AmqpsDeviceTwin.class, mockDeviceClientConfig);
                result = mockAmqpsDeviceTwin;
            }
        };

        //act
        Deencapsulation.invoke(amqpsSessionDeviceOperation, "subscribeToMessageType", mockSession, DEVICE_TWIN);

        //assert
        assertTrue(amqpsDeviceOperationsMap.containsKey(DEVICE_TWIN));
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceTwin, "openLinks", mockSession);
                times = 1;
            }
        };
    }

    @Test
    public void subscribeToMessageTypeForTwinDoesNothingIfAlreadySubscribed()
    {
        //arrange
        final AmqpsSessionDeviceOperation amqpsSessionDeviceOperation = new AmqpsSessionDeviceOperation(mockDeviceClientConfig, mockAmqpsDeviceAuthentication, mockedSubscriptionMessageRequestSentCallback);
        Map<MessageType, AmqpsDeviceOperations> amqpsDeviceOperationsMap = new HashMap<MessageType, AmqpsDeviceOperations>();
        amqpsDeviceOperationsMap.put(DEVICE_TWIN, mockAmqpsDeviceTwin);
        Deencapsulation.setField(amqpsSessionDeviceOperation, "amqpsDeviceOperationsMap", amqpsDeviceOperationsMap);

        //act
        Deencapsulation.invoke(amqpsSessionDeviceOperation, "subscribeToMessageType", mockSession, DEVICE_TWIN);

        //assert
        assertTrue(amqpsDeviceOperationsMap.containsKey(DEVICE_TWIN));
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsDeviceTwin, "openLinks", mockSession);
                times = 0;
            }
        };
    }

}
