package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.Transport;

import javax.net.ssl.SSLContext;
import java.util.UUID;

@Slf4j
public class AmqpsAuthenticationLinkHandlerX509 extends AmqpsAuthenticationLinkHandler
{
    /**
     * Do nothing in SAS case.
     */
    @Override
    protected AmqpsSendReturnValue sendMessageAndGetDeliveryTag(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag)
    {
        // Codes_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_004: [The function shall override the default behaviour and return null.]
        return null;
    }

    @Override
    public String getLinkInstanceType()
    {
        return "x509";
    }

    /**
     * Do nothing in SAS case.
     */
    @Override
    protected AmqpsMessage getMessageFromReceiverLink(String linkName)
    {
        // Codes_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_005: [The function shall override the default behaviour and return null.]
        return null;
    }

    /**
     * Set authentication mode to PLAIN for SAS.
     *
     * @param transport The transport to set the SSL context to
     */
    @Override
    protected void setSslDomain(Transport transport, SSLContext sslContext)
    {
        if (transport == null)
        {
            throw new IllegalArgumentException("Input parameter transport cannot be null.");
        }

        SslDomain domain = makeDomain(sslContext);
        transport.ssl(domain);
    }

    @Override
    protected void authenticate(DeviceClientConfig deviceClientConfig, UUID correlationId) throws TransportException
    {
        throw new TransportException("Cannot authenticate on demand with x509 auth");
    }

    @Override
    protected boolean handleAuthenticationMessage(AmqpsMessage amqpsMessage, UUID authenticationCorrelationId)
    {
        return false;
    }

    /**
     * Found the link by name.
     *
     * @param link The link that opened remotely
     * @return true if link found, false otherwise.
     */
    @Override
    protected boolean onLinkRemoteOpen(Link link)
    {
        //No link should ever be opened specifically for x509 auth, so this always returns false
        return false;
    }
}
