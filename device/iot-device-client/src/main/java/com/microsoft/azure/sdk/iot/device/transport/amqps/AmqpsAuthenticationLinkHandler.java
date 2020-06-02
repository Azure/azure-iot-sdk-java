package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.ProtonUnsupportedOperationException;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.message.impl.MessageImpl;

import javax.net.ssl.SSLContext;
import java.util.UUID;

@Slf4j
public abstract class AmqpsAuthenticationLinkHandler extends AmqpsLinksHandler
{
    public AmqpsAuthenticationLinkHandler()
    {
        super();
    }

    protected SslDomain makeDomain(SSLContext sslContext)
    {
        SslDomain domain = Proton.sslDomain();
        domain.setSslContext(sslContext);
        domain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
        domain.init(SslDomain.Mode.CLIENT);
        return domain;
    }

    /**
     * abstract function for set the SslDomain
     *
     * @param transport The transport to set the SSL context to
     * @param sslContext the context to use
     */
    abstract protected void setSslDomain(Transport transport, SSLContext sslContext);

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
    abstract protected boolean handleAuthenticationMessage(AmqpsMessage amqpsMessage, UUID authenticationCorrelationId);

    protected IotHubTransportMessage convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig)
    {
        log.warn("AmqpsAuthenticationLinkHandler called to convertFromProton, but this should never occur");
        return null;
    }

    protected MessageImpl convertToProton(Message message)
    {
        log.warn("AmqpsAuthenticationLinkHandler called to convertToProton, but this should never occur");
        return null;
    }
}
