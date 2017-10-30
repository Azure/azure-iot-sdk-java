package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthentication;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.Sasl;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.Transport;
import org.junit.Test;

import javax.net.ssl.SSLContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for AmqpsDeviceAuthenticationSAS.
 * Methods: 100%
 * Lines: 91%
 */
public class AmqpsDeviceAuthenticationSASTest
{
    @Mocked
    Proton mockProton;

    @Mocked
    SSLContext mockSSLContext;

    @Mocked
    SslDomain mockDomain;

    @Mocked
    Transport mockTransport;

    @Mocked
    Sasl mockSasl;

    @Mocked
    DeviceClientConfig mockDeviceClientConfig;

    @Mocked
    IotHubSasTokenAuthentication mockIotHubSasTokenAuthentication;

    @Mocked
    AmqpsDeviceAuthentication mockamqpsDeviceAuthentication;

    @Mocked
    SslDomain mockSslDomain;

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_001: [The constructor shall throw IllegalArgumentException if the  deviceClientConfig parameter is null.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfDeviceClientIsNull() throws IllegalArgumentException
    {
        // act
        new AmqpsDeviceAuthenticationSAS(null);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_002: [The constructor shall save the deviceClientConfig parameter value to a member variable.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_003: [The constructor shall set both the sender and the receiver link state to OPENED.]
    @Test
    public void constructorSavesDeviceClientConfig() throws IllegalArgumentException
    {
        // act
        AmqpsDeviceAuthenticationSAS amqpsDeviceAuthenticationSAS = new AmqpsDeviceAuthenticationSAS(mockDeviceClientConfig);

        // assert
        DeviceClientConfig actualDeviceClientConfig = Deencapsulation.getField(amqpsDeviceAuthenticationSAS, "deviceClientConfig");
        AmqpsDeviceOperationLinkState actualSendLinkState = Deencapsulation.getField(amqpsDeviceAuthenticationSAS, "amqpsSendLinkState");
        AmqpsDeviceOperationLinkState actualRecvLinkState = Deencapsulation.getField(amqpsDeviceAuthenticationSAS, "amqpsRecvLinkState");

        assertEquals(mockDeviceClientConfig, actualDeviceClientConfig);
        assertEquals(actualSendLinkState, AmqpsDeviceOperationLinkState.OPENED);
        assertEquals(actualRecvLinkState, AmqpsDeviceOperationLinkState.OPENED);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_004: [The function shall override the default behaviour and return null.]
    @Test
    public void sendMessageAndGetDeliveryHash() throws IllegalArgumentException
    {
        // arrange
        AmqpsDeviceAuthenticationSAS amqpsDeviceAuthenticationSAS = new AmqpsDeviceAuthenticationSAS(mockDeviceClientConfig);

        // act
        byte[] b = new byte[1];
        AmqpsSendReturnValue amqpsSendReturnValue = Deencapsulation.invoke(amqpsDeviceAuthenticationSAS, "sendMessageAndGetDeliveryHash", MessageType.CBS_AUTHENTICATION, b, 0, 0, b);

        // assert
        assertNull(amqpsSendReturnValue);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_005: [The function shall override the default behaviour and return null.]
    @Test
    public void getMessageFromReceiverLink() throws IllegalArgumentException
    {
        // arrange
        AmqpsDeviceAuthenticationSAS amqpsDeviceAuthenticationSAS = new AmqpsDeviceAuthenticationSAS(mockDeviceClientConfig);

        // act
        AmqpsMessage amqpsMessage = Deencapsulation.invoke(amqpsDeviceAuthenticationSAS, "getMessageFromReceiverLink", "");

        // assert
        assertNull(amqpsMessage);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_006: [The function shall throw IllegalArgumentException if any of the input parameter is null.]
    @Test (expected = IllegalArgumentException.class)
    public void setSslDomainThrowsIfTransportNull() throws IllegalArgumentException
    {
        // act
        AmqpsDeviceAuthenticationSAS amqpsDeviceAuthenticationSAS = new AmqpsDeviceAuthenticationSAS(mockDeviceClientConfig);

        // assert
        Deencapsulation.invoke(amqpsDeviceAuthenticationSAS, "setSslDomain", (Transport)null, mockSSLContext);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_006: [The function shall throw IllegalArgumentException if any of the input parameter is null.]
    @Test (expected = IllegalArgumentException.class)
    public void setSslDomainThrowsIfSslContextNull() throws IllegalArgumentException
    {
        // act
        AmqpsDeviceAuthenticationSAS amqpsDeviceAuthenticationSAS = new AmqpsDeviceAuthenticationSAS(mockDeviceClientConfig);

        // assert
        Deencapsulation.invoke(amqpsDeviceAuthenticationSAS, "setSslDomain", mockTransport, null);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_007: [The function shall get the sasl object from the transport.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_008: [The function shall construct the userName.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_009: [The function shall set SASL PLAIN authentication mode with the usrName and SAS token.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_010: [The function shall call the prototype class makeDomain function with the sslContext.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_011: [The function shall set the domain on the transport.]
    @Test
    public void setSslDomainSuccess() throws IllegalArgumentException
    {
        // act
        AmqpsDeviceAuthenticationSAS amqpsDeviceAuthenticationSAS = new AmqpsDeviceAuthenticationSAS(mockDeviceClientConfig);

        new NonStrictExpectations()
        {
            {
                mockTransport.sasl();
                result = mockSasl;
                mockDeviceClientConfig.getDeviceId();
                result = "deviceId";
                mockDeviceClientConfig.getIotHubName();
                result = "iotHubName";
                mockDeviceClientConfig.getSasTokenAuthentication();
                result = mockIotHubSasTokenAuthentication;
                mockIotHubSasTokenAuthentication.getCurrentSasToken();
                result = "sasToken";
                Deencapsulation.invoke(mockamqpsDeviceAuthentication, "makeDomain", mockSSLContext);
                result = mockSslDomain;
            }
        };

        // assert
        Deencapsulation.invoke(amqpsDeviceAuthenticationSAS, "setSslDomain", mockTransport);

        // assert
        new Verifications()
        {
            {
                mockTransport.sasl();
                times = 1;
                mockSasl.plain("deviceId@sas.iotHubName", "sasToken");
                times = 1;
                mockTransport.ssl(mockSslDomain);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONSAS_12_012: [The function shall override the default behaviour and return null.]
    @Test
    public void isLinkFound() throws IllegalArgumentException
    {
        // arrange
        AmqpsDeviceAuthenticationSAS amqpsDeviceAuthenticationSAS = new AmqpsDeviceAuthenticationSAS(mockDeviceClientConfig);

        // act
        Boolean isFound = Deencapsulation.invoke(amqpsDeviceAuthenticationSAS, "isLinkFound", "");

        // assert
        assertTrue(isFound);
    }
}
