package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.ProtonUnsupportedOperationException;
import org.apache.qpid.proton.engine.*;

import javax.net.ssl.SSLContext;
import java.util.UUID;

public class AmqpsDeviceAuthentication extends AmqpsDeviceOperations
{
    /**
     * Create Proton SslDomain object from Address using the given Ssl context
     *
     * @param sslContext the SslCOntext to set up the domain.
     * @return the created Ssl domain
     * @throws TransportException when ProtonUnsupportedOperationException is caught
     */
    protected SslDomain makeDomain(SSLContext sslContext) throws TransportException
    {
        SslDomain domain = null;

        try
        {
            // Codes_SRS_AMQPSDEVICEAUTHENTICATION_12_001: [The function shall get the sslDomain object from the Proton reactor.]
            domain = Proton.sslDomain();
            // Codes_SRS_AMQPSDEVICEAUTHENTICATION_12_002: [The function shall set the sslContext on the domain.]
            domain.setSslContext(sslContext);
            // Codes_SRS_AMQPSDEVICEAUTHENTICATION_12_003: [The function shall set the peer authentication mode to VERIFY_PEER.]
            domain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
            // Codes_SRS_AMQPSDEVICEAUTHENTICATION_12_004: [The function shall initialize the sslDomain.]
            domain.init(SslDomain.Mode.CLIENT);
            // Codes_SRS_AMQPSDEVICEAUTHENTICATION_12_005: [The function shall return with the sslDomain.]
        }
        catch (ProtonUnsupportedOperationException e)
        {
            throw new TransportException(e);
        }

        return domain;
    }

    /**
     * Prototype (empty) function for set the SslDomain
     *
     * @param transport The transport to set the SSL context to
     */
    protected void setSslDomain(Transport transport) throws TransportException
    {
        // Codes_SRS_AMQPSDEVICEAUTHENTICATION_12_067: [The prototype function does nothing.]
    }

    /**
     * Prototype (empty) function for set the SslDomain
     *
     * @param deviceClientConfig The deviceClientConfig to use for authentication
     * @param correlationId the authentication message's correlationId.
     */
    protected void authenticate(DeviceClientConfig deviceClientConfig, UUID correlationId) throws TransportException
    {
        // Codes_SRS_AMQPSDEVICEAUTHENTICATION_12_007: [The prototype function does nothing.]
    }

    /**
     * Prototype (empty) function for evaluation function of received message.
     *
     * @param amqpsMessage the message to evaluate.
     * @param authenticationCorrelationId the expected correlation ID.
     * @return truw id the message acknowledge the authentication, false otherwise.
     */
    protected Boolean authenticationMessageReceived(AmqpsMessage amqpsMessage, UUID authenticationCorrelationId)
    {
        // Codes_SRS_AMQPSDEVICEAUTHENTICATION_12_008: [The prototype function shall return false.]
        return false;
    }
}
