package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsDeviceAuthentication;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsMessage;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.Transport;
import org.junit.Test;

import javax.net.ssl.SSLContext;

import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    DeviceClientConfig mockDeviceClientConfig;

    private class AmqpsDeviceAuthenticationMock extends AmqpsDeviceAuthentication
    {
        public AmqpsDeviceAuthenticationMock(DeviceClientConfig config)
        {
            super(config);
        }

        @Override
        protected void setSslDomain(Transport transport) throws TransportException
        {
            //do nothing
        }

        @Override
        protected void authenticate(DeviceClientConfig deviceClientConfig, UUID correlationId) throws TransportException
        {
            //do nothing
        }

        @Override
        protected boolean handleAuthenticationMessage(AmqpsMessage amqpsMessage, UUID authenticationCorrelationId)
        {
            return false;
        }

        @Override
        public String getLinkInstanceType()
        {
            return "mockAuthentication";
        }

        @Override
        protected boolean onLinkRemoteOpen(String linkName)
        {
            return false;
        }
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATION_12_001: [The function shall get the sslDomain oject from the Proton reactor.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATION_12_002: [The function shall set the sslContext on the domain.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATION_12_003: [The functions hall set the peer authentication mode to VERIFY_PEER.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATION_12_004: [The function shall initialize the sslDomain.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATION_12_005: [The function shall return with the sslDomain.]
    @Test
    public void makeDomainSuccess()
    {
        // arrange
        final AmqpsDeviceAuthenticationMock amqpsDeviceAuthentication = new AmqpsDeviceAuthenticationMock(mockDeviceClientConfig);

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
}
