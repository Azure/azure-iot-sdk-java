package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsDeviceAuthentication;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsMessage;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.Transport;
import org.junit.Test;

import javax.net.ssl.SSLContext;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for AmqpsDeviceAuthentication.
 * Methods: 100%
 * Lines: 100%
 */
public class AmqpsDeviceAuthenticationTest
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
    DeviceClientConfig mockDeviceClientConfig;

    @Mocked
    UUID mockUUID;

    @Mocked
    AmqpsMessage mockAmqpsMessage;

    // Tests_SRS_AMQPSDEVICEAUTHENTICATION_12_001: [The function shall get the sslDomain oject from the Proton reactor.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATION_12_002: [The function shall set the sslContext on the domain.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATION_12_003: [The functions hall set the peer authentication mode to VERIFY_PEER.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATION_12_004: [The function shall initialize the sslDomain.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATION_12_005: [The function shall return with the sslDomain.]
    @Test
    public void makeDomainSuccess()
    {
        // arrange
        final AmqpsDeviceAuthentication amqpsDeviceAuthentication = new AmqpsDeviceAuthentication();

        new NonStrictExpectations()
        {
            {
                mockProton.sslDomain();
                result = mockDomain;
            }
        };

        // act
        SslDomain actualDomain = Deencapsulation.invoke(amqpsDeviceAuthentication, "makeDomain", mockSSLContext);

        // assert
        assertEquals(mockDomain, actualDomain);
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockProton, "sslDomain");
                times = 1;
                mockDomain.setSslContext(mockSSLContext);
                times = 1;
                mockDomain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
                times = 1;
                mockDomain.init(SslDomain.Mode.CLIENT);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATION_12_006: [The prototype function does nothing.]
    @Test
    public void setSslDomain()
    {
        // arrange
        final AmqpsDeviceAuthentication amqpsDeviceAuthentication = new AmqpsDeviceAuthentication();

        // act
        Deencapsulation.invoke(amqpsDeviceAuthentication, "setSslDomain", mockTransport);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATION_12_007: [The prototype function does nothing.]
    @Test
    public void authenticate()
    {
        // arrange
        final AmqpsDeviceAuthentication amqpsDeviceAuthentication = new AmqpsDeviceAuthentication();

        // act
        Deencapsulation.invoke(amqpsDeviceAuthentication, "authenticate", mockDeviceClientConfig, mockUUID);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATION_12_008: [The prototype function shall return false.]
    @Test
    public void authenticationMessageReceived()
    {
        // arrange
        final AmqpsDeviceAuthentication amqpsDeviceAuthentication = new AmqpsDeviceAuthentication();

        // act
        Boolean actualReturn = Deencapsulation.invoke(amqpsDeviceAuthentication, "authenticationMessageReceived", mockAmqpsMessage, mockUUID);

        // assert
        assertEquals(false, actualReturn);
    }
}
