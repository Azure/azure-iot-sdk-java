package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.ProtonUnsupportedOperationException;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.Transport;

import javax.net.ssl.SSLContext;
import java.util.UUID;

public abstract class AmqpsDeviceAuthentication extends AmqpsDeviceOperations
{
    public AmqpsDeviceAuthentication(DeviceClientConfig config)
    {
        // Codes_SRS_AMQPSDEVICEAUTHENTICATION_34_009: [This constructor shall call super with the provided user agent string.]
        super(config, "", "", "", "", "", "");
    }

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
            domain = Proton.sslDomain();
            domain.setSslContext(sslContext);
            domain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
            domain.init(SslDomain.Mode.CLIENT);
        }
        catch (ProtonUnsupportedOperationException e)
        {
            throw new TransportException(e);
        }

        return domain;
    }

    /**
     * abstract function for set the SslDomain
     *
     * @param transport The transport to set the SSL context to
     * @throws TransportException if setting ssl domain fails
     */
    abstract protected void setSslDomain(Transport transport) throws TransportException;

    /**
     * abstract function for set the SslDomain
     *
     * @param deviceClientConfig The deviceClientConfig to use for authentication
     * @param correlationId the authentication message's correlationId.
     * @throws TransportException if authentication fails
     */
    abstract protected void authenticate(DeviceClientConfig deviceClientConfig, UUID correlationId) throws TransportException;

    /**
     * abstract function for evaluation function of received message.
     *
     * @param amqpsMessage the message to evaluate.
     * @param authenticationCorrelationId the expected correlation ID.
     * @return true if the message acknowledge the authentication, false otherwise.
     */
    abstract protected boolean authenticationMessageReceived(AmqpsMessage amqpsMessage, UUID authenticationCorrelationId);

    @Override
    protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws TransportException
    {
        throw new TransportException("Should not be called");
    }

    @Override
    protected AmqpsConvertToProtonReturnValue convertToProton(Message message) throws TransportException
    {
        throw new TransportException("Should not be called");
    }
}
