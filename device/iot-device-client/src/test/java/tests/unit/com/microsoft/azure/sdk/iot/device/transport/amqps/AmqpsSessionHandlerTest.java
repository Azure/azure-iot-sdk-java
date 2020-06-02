package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import mockit.*;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.message.impl.MessageImpl;
import org.junit.Test;

import java.nio.BufferOverflowException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
public class AmqpsSessionHandlerTest
{
    @Mocked
    DeviceClientConfig mockDeviceClientConfig;

    @Mocked
    AmqpsAuthenticationLinkHandler mockAmqpsAuthenticationLinkHandler;

    @Mocked
    AmqpsAuthenticationLinkHandlerCBS mockAmqpsDeviceAuthenticationCBS;

    @Mocked
    AmqpsLinksHandler mockAmqpsLinksHandler;

    @Mocked
    AmqpsTelemetryLinksHandler mockAmqpsTelemetryLinksManager;

    @Mocked
    AmqpsMethodsLinksHandler mockAmqpsMethodsLinksManager;

    @Mocked
    AmqpsTwinLinksHandler mockAmqpsTwinLinksManager;

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

    @Mocked
    AmqpsConnectionStateCallback mockAmqpsConnectionStateCallback;

    @Mocked
    IotHubTransportMessage mockedTransportMessage;

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_001: [The constructor shall throw IllegalArgumentException if the deviceClientConfig or the amqpsDeviceAuthentication parameter is null.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfDeviceClientIsNull() throws IllegalArgumentException, TransportException
    {
        // arrange
        // act
        new AmqpsSessionHandler(null, mockedSubscriptionMessageRequestSentCallback);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_002: [The constructor shall save the deviceClientConfig and amqpsDeviceAuthentication parameter value to a member variable.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_003: [The constructor shall create AmqpsTelemetryLinksHandler, AmqpsMethodsLinksHandler and AmqpsTwinLinksHandler and add them to the device operations list.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_047: [The constructor shall set the authentication state to authenticated if the authentication type is not CBS.]
    @Test
    public void constructorSuccessSAS() throws IllegalArgumentException, TransportException
    {
        // act
        AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);

        // assert
        DeviceClientConfig actualDeviceClientConfig = Deencapsulation.getField(amqpsSessionHandler, "deviceClientConfig");
        AmqpsDeviceAuthenticationState authenticatorState = Deencapsulation.getField(amqpsSessionHandler, "amqpsAuthenticatorState");

        assertEquals(mockDeviceClientConfig, actualDeviceClientConfig);
        assertEquals(AmqpsDeviceAuthenticationState.AUTHENTICATED, authenticatorState);
        new Verifications()
        {
            {
                Deencapsulation.newInstance(AmqpsTelemetryLinksHandler.class, mockDeviceClientConfig);
                times = 1;
                Deencapsulation.newInstance(AmqpsMethodsLinksHandler.class, mockDeviceClientConfig);
                times = 0;
                Deencapsulation.newInstance(AmqpsTwinLinksHandler.class, mockDeviceClientConfig);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_002: [The constructor shall save the deviceClientConfig and amqpsDeviceAuthentication parameter value to a member variable.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_003: [The constructor shall create AmqpsTelemetryLinksHandler, AmqpsMethodsLinksHandler and AmqpsTwinLinksHandler and add them to the device operations list. ]
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
        AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);

        // assert
        DeviceClientConfig actualDeviceClientConfig = Deencapsulation.getField(amqpsSessionHandler, "deviceClientConfig");
        AmqpsDeviceAuthenticationState authenticatorState = Deencapsulation.getField(amqpsSessionHandler, "amqpsAuthenticatorState");

        assertEquals(mockDeviceClientConfig, actualDeviceClientConfig);
        assertEquals(AmqpsDeviceAuthenticationState.NOT_AUTHENTICATED, authenticatorState);
        new Verifications()
        {
            {
                Deencapsulation.newInstance(AmqpsTelemetryLinksHandler.class, mockDeviceClientConfig);
                times = 1;
                Deencapsulation.newInstance(AmqpsMethodsLinksHandler.class, mockDeviceClientConfig);
                times = 0;
                Deencapsulation.newInstance(AmqpsTwinLinksHandler.class, mockDeviceClientConfig);
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
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionHandler, "deviceClientConfig", mockDeviceClientConfig);

        // act
        amqpsSessionHandler.close();

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsLinksHandler, "closeLinks");
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_009: [The function shall call openLinks on all device operations if the authentication state is authenticated.]
    @Test
    public void openLinks() throws IllegalArgumentException, TransportException
    {
        // arrange
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionHandler, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);
        Deencapsulation.setField(amqpsSessionHandler, "session", mockSession);

        // act
        Deencapsulation.invoke(amqpsSessionHandler, "openLinks");

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsLinksHandler, "openLinks", mockSession);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_010: [The function shall call closeLinks on all device operations.]
    @Test
    public void closeLinks() throws IllegalArgumentException, TransportException
    {
        // arrange
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);

        // act
        Deencapsulation.invoke(amqpsSessionHandler, "closeLinks");

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsLinksHandler, "closeLinks");
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_011: [The function shall call initLink on all device operations.**]**]
    @Test
    public void initLink() throws IllegalArgumentException, TransportException
    {
        // arrange
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionHandler, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);

        // act
        Deencapsulation.invoke(amqpsSessionHandler, "initLink", mockLink);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsLinksHandler, "initLink", mockLink);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_012: [The function shall return -1 if the state is not authenticated.]
    @Test
    public void sendMessageNotAuthenticated() throws IllegalArgumentException, TransportException
    {
        // arrange
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionHandler, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATING);

        // act
        Integer deliveryHash = Deencapsulation.invoke(amqpsSessionHandler, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");

        // assert
        assertTrue(deliveryHash == -1);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_013: [The function shall return -1 if the deviceId int he connection string is not equal to the deviceId in the config.]
    @Test
    public void sendMessageDeviceIdMismatch() throws IllegalArgumentException, TransportException
    {
        // arrange
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionHandler, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);

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
        Integer deliveryHash = Deencapsulation.invoke(amqpsSessionHandler, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");

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
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionHandler, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);
        final byte[] bytes = new byte[1024];
        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.getDeviceId();
                result = "deviceId";
                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";

                mockProtonMessage.encode(bytes, anyInt, anyInt);

                Deencapsulation.invoke(mockAmqpsTelemetryLinksManager, "sendMessageAndGetDeliveryTag", MessageType.DEVICE_TELEMETRY, bytes, anyInt, anyInt, bytes);
                result = mockAmqpsSendReturnValue;
                Deencapsulation.invoke(mockAmqpsSendReturnValue, "isDeliverySuccessful");
                result = false;

                mockDeviceClientConfig.getDeviceId();
                result = "someDeviceId";
            }
        };

        // act
        Integer actualDeliveryHash = Deencapsulation.invoke(amqpsSessionHandler, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");

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
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionHandler, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);
        final byte[] bytes = new byte[1024];
        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.getDeviceId();
                result = "deviceId";
                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";

                mockProtonMessage.encode(bytes, anyInt, anyInt);

                Deencapsulation.invoke(mockAmqpsTelemetryLinksManager, "sendMessageAndGetDeliveryTag", MessageType.DEVICE_TELEMETRY, bytes, anyInt, anyInt, bytes);
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
        Integer actualDeliveryHash = Deencapsulation.invoke(amqpsSessionHandler, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");

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
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionHandler, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);
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

                Deencapsulation.invoke(mockAmqpsTelemetryLinksManager, "sendMessageAndGetDeliveryTag", MessageType.DEVICE_TELEMETRY, bytes, anyInt, anyInt, bytes);
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
        Integer actualDeliveryHash = Deencapsulation.invoke(amqpsSessionHandler, "sendMessage", mockProtonMessage, MessageType.DEVICE_TELEMETRY, "someDeviceId");

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
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionHandler, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.NOT_AUTHENTICATED);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsTelemetryLinksManager, "getMessageFromReceiverLink", linkName);
                result = mockAmqpsMessage;
            }
        };

        // act
        AmqpsMessage actualAmqpsMessage = Deencapsulation.invoke(amqpsSessionHandler, "getMessageFromReceiverLink", linkName);

        // assert
        assertEquals(actualAmqpsMessage, mockAmqpsMessage);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_021: [If the state is authenticated the function shall call getMessageFromReceiverLink on all device operations and return with the success if any.]
    @Test
    public void getMessageFromReceiverLinkAuthenticated() throws IllegalArgumentException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionHandler, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsTelemetryLinksManager, "getMessageFromReceiverLink", linkName);
                result = mockAmqpsMessage;
            }
        };

        // act
        AmqpsMessage actualAmqpsMessage = Deencapsulation.invoke(amqpsSessionHandler, "getMessageFromReceiverLink", linkName);

        // assert
        assertNotNull(actualAmqpsMessage);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_021: [If the state is authenticated the function shall call getMessageFromReceiverLink on all device operations and return with the success if any.]
    @Test
    public void getMessageFromReceiverLinkAuthenticatedReturnNull() throws IllegalArgumentException, TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionHandler, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsTelemetryLinksManager, "getMessageFromReceiverLink", linkName);
                result = null;
                Deencapsulation.invoke(mockAmqpsMethodsLinksManager, "getMessageFromReceiverLink", linkName);
                result = null;
                Deencapsulation.invoke(mockAmqpsTwinLinksManager, "getMessageFromReceiverLink", linkName);
                result = null;
            }
        };

        // act
        AmqpsMessage actualAmqpsMessage = Deencapsulation.invoke(amqpsSessionHandler, "getMessageFromReceiverLink", linkName);

        // assert
        assertNull(actualAmqpsMessage);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_024: [The function shall return true if any of the operation's link name is a match and return false otherwise.]
    @Test
    public void onLinkRemoteOpenTrue() throws TransportException
    {
        // arrange
        final String linkName = "linkName";
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionHandler, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);
        Map<MessageType, AmqpsLinksHandler> amqpsLinkMap = new HashMap<MessageType, AmqpsLinksHandler>();
        Deencapsulation.setField(amqpsSessionHandler, "amqpsLinkMap", amqpsLinkMap);
        amqpsLinkMap.put(DEVICE_TELEMETRY, mockAmqpsLinksHandler);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsLinksHandler, "onLinkRemoteOpen", mockLink);
                result = true;

                mockAmqpsLinksHandler.hasLink(mockLink);
                result = true;
            }
        };

        // act
        Boolean isFound = Deencapsulation.invoke(amqpsSessionHandler, "onLinkRemoteOpen", mockLink, mockAmqpsConnectionStateCallback);

        // assert
        assertTrue(isFound);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_024: [The function shall return true if any of the operation's link name is a match and return false otherwise.]
    @Test
    public void onLinkRemoteOpenFalse() throws TransportException
    {
        // arrange
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Deencapsulation.setField(amqpsSessionHandler, "amqpsAuthenticatorState", AmqpsDeviceAuthenticationState.AUTHENTICATED);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsTelemetryLinksManager, "onLinkRemoteOpen", mockLink);
                result = false;
            }
        };

        // act
        Boolean isFound = Deencapsulation.invoke(amqpsSessionHandler, "onLinkRemoteOpen", mockLink, mockAmqpsConnectionStateCallback);

        // assert
        assertFalse(isFound);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_040: [The function shall call all device operation's convertToProton, and if any of them not null return with the value.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_041: [The function shall call all device operation's convertFromProton, and if any of them not null return with the value.]
    @Test
    public void convertToProtonSuccess(@Mocked final MessageImpl mockMessageImpl) throws IllegalArgumentException
    {
        // arrange
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);

        new Expectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsTelemetryLinksManager, "iotHubMessageToProtonMessage", mockMessage);
                result = mockMessageImpl;
                mockMessage.getMessageType();
                result = DEVICE_TELEMETRY;
            }
        };

        // act
        Deencapsulation.invoke(amqpsSessionHandler, "convertToProton", mockMessage);
    }

    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_040: [The function shall call all device operation's convertToProton, and if any of them not null return with the value.]
    // Tests_SRS_AMQPSESSIONDEVICEOPERATION_12_041: [The function shall call all device operation's convertFromProton, and if any of them not null return with the value.]
    @Test
    public void convertFromProtonSuccess() throws IllegalArgumentException, TransportException
    {
        // arrange
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsTelemetryLinksManager, "protonMessageToIoTHubMessage", mockAmqpsMessage, mockDeviceClientConfig);
                result = mockedTransportMessage;
                mockAmqpsMessage.getAmqpsMessageType();
                result = DEVICE_TELEMETRY;
            }
        };

        // act
        IotHubTransportMessage actualAmqpsConvertFromProtonReturnValue = Deencapsulation.invoke(amqpsSessionHandler, "convertFromProton", mockAmqpsMessage, mockDeviceClientConfig);

        // assert
        assertTrue(mockedTransportMessage == actualAmqpsConvertFromProtonReturnValue);
    }

    @Test
    public void handleAuthenticationMessageSuccess()
    {
        //arrange
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        List<UUID> cbsCorrelationIdList = Deencapsulation.getField(amqpsSessionHandler, "cbsCorrelationIdList");
        final UUID uuid = UUID.randomUUID();
        cbsCorrelationIdList.add(0, uuid);

        new Expectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsAuthenticationLinkHandler, "handleAuthenticationMessage", mockAmqpsMessage, uuid);
                result = true;
            }
        };

        //act
        boolean handled = Deencapsulation.invoke(amqpsSessionHandler, "handleAuthenticationMessage", mockAmqpsMessage, mockAmqpsAuthenticationLinkHandler);

        //assert
        assertTrue(handled);
    }

    @Test
    public void handleAuthenticationMessageWithNoSavedUUID()
    {
        //arrange
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);

        //intentionally leave this list empty
        List<UUID> cbsCorrelationIdList = Deencapsulation.getField(amqpsSessionHandler, "cbsCorrelationIdList");

        //act
        boolean handled = Deencapsulation.invoke(amqpsSessionHandler, "handleAuthenticationMessage", mockAmqpsMessage, mockAmqpsAuthenticationLinkHandler);

        //assert
        assertFalse(handled);
    }

    @Test
    public void handleAuthenticationMessageFailure()
    {
        //arrange
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        List<UUID> cbsCorrelationIdList = Deencapsulation.getField(amqpsSessionHandler, "cbsCorrelationIdList");
        final UUID uuid = UUID.randomUUID();
        cbsCorrelationIdList.add(0, uuid);

        new Expectations()
        {
            {
                Deencapsulation.invoke(mockAmqpsAuthenticationLinkHandler, "handleAuthenticationMessage", mockAmqpsMessage, uuid);
                result = false;
            }
        };

        //act
        boolean handled = Deencapsulation.invoke(amqpsSessionHandler, "handleAuthenticationMessage", mockAmqpsMessage, mockAmqpsAuthenticationLinkHandler);

        //assert
        assertFalse(handled);
    }

    @Test
    public void subscribeToMessageTypeForMethodsSavesInMapAndCallsOpenLinks()
    {
        //arrange
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Map<MessageType, AmqpsLinksHandler> amqpsLinkMap = new HashMap<MessageType, AmqpsLinksHandler>();
        Deencapsulation.setField(amqpsSessionHandler, "amqpsLinkMap", amqpsLinkMap);
        Deencapsulation.setField(amqpsSessionHandler, "session", mockSession);

        new Expectations()
        {
            {
                Deencapsulation.newInstance(AmqpsMethodsLinksHandler.class, mockDeviceClientConfig);
                result = mockAmqpsMethodsLinksManager;
            }
        };

        //act
        Deencapsulation.invoke(amqpsSessionHandler, "subscribeToMessageType", DEVICE_METHODS);

        //assert
        assertTrue(amqpsLinkMap.containsKey(DEVICE_METHODS));
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsMethodsLinksManager, "openLinks", mockSession);
                times = 1;
            }
        };
    }

    @Test
    public void subscribeToMessageTypeForTwinSavesInMapAndCallsOpenLinks()
    {
        //arrange
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Map<MessageType, AmqpsLinksHandler> amqpsLinkMap = new HashMap<MessageType, AmqpsLinksHandler>();
        Deencapsulation.setField(amqpsSessionHandler, "amqpsLinkMap", amqpsLinkMap);
        Deencapsulation.setField(amqpsSessionHandler, "session", mockSession);

        new Expectations()
        {
            {
                Deencapsulation.newInstance(AmqpsTwinLinksHandler.class, mockDeviceClientConfig);
                result = mockAmqpsTwinLinksManager;
            }
        };

        //act
        Deencapsulation.invoke(amqpsSessionHandler, "subscribeToMessageType", DEVICE_TWIN);

        //assert
        assertTrue(amqpsLinkMap.containsKey(DEVICE_TWIN));
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsTwinLinksManager, "openLinks", mockSession);
                times = 1;
            }
        };
    }

    @Test
    public void subscribeToMessageTypeForTwinDoesNothingIfAlreadySubscribed()
    {
        //arrange
        final AmqpsSessionHandler amqpsSessionHandler = new AmqpsSessionHandler(mockDeviceClientConfig, mockedSubscriptionMessageRequestSentCallback);
        Map<MessageType, AmqpsLinksHandler> amqpsLinkMap = new HashMap<MessageType, AmqpsLinksHandler>();
        amqpsLinkMap.put(DEVICE_TWIN, mockAmqpsTwinLinksManager);
        Deencapsulation.setField(amqpsSessionHandler, "amqpsLinkMap", amqpsLinkMap);

        //act
        Deencapsulation.invoke(amqpsSessionHandler, "subscribeToMessageType", DEVICE_TWIN);

        //assert
        assertTrue(amqpsLinkMap.containsKey(DEVICE_TWIN));
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockAmqpsTwinLinksManager, "openLinks", mockSession);
                times = 0;
            }
        };
    }

}
