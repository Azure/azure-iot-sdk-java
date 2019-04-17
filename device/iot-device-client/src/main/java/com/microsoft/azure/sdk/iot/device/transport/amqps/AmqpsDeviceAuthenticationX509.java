package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.CustomLogger;
import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.MessageType;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.Transport;

import java.io.IOException;

public class AmqpsDeviceAuthenticationX509 extends AmqpsDeviceAuthentication
{
    private final DeviceClientConfig deviceClientConfig;
    private CustomLogger logger;

    /**
     * This constructor creates an instance of AmqpsDeviceAuthenticationSAS class and initializes member variables
     *
     * @param deviceClientConfig the device config to use for authentication.
     * @throws IllegalArgumentException if deviceClientConfig is null.
     */
    public AmqpsDeviceAuthenticationX509(DeviceClientConfig deviceClientConfig) throws IllegalArgumentException
    {
        // Codes_SRS_AMQPSDEVICEAUTHENTICATIONX509_34_007: [This constructor shall call super with the provided user agent string.]
        super(deviceClientConfig);

        // Codes_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_002: [The constructor shall save the deviceClientConfig parameter value to a member variable.]
        this.deviceClientConfig = deviceClientConfig;

        // Codes_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_003: [The constructor shall set both the sender and the receiver link state to OPENED.]
        this.amqpsSendLinkState = AmqpsDeviceOperationLinkState.OPENED;
        this.amqpsRecvLinkState = AmqpsDeviceOperationLinkState.OPENED;

        this.logger = new CustomLogger(this.getClass());
    }

    /**
     * Do nothing in SAS case.
     */
    @Override
    protected AmqpsSendReturnValue sendMessageAndGetDeliveryTag(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag)
    {
        // Codes_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_004: [The function shall override the default behaviour and return null.]
        return null;
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
     * @throws TransportException if setSslDomain throws IOException
     */
    @Override
    protected void setSslDomain(Transport transport) throws TransportException
    {
        if (transport == null)
        {
            // Codes_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_006: [The function shall throw IllegalArgumentException if any of the input parameter is null.]
            throw new IllegalArgumentException("Input parameter transport cannot be null.");
        }

        SslDomain domain = null;
        try
        {
            // Codes_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_010: [The function shall call the prototype class makeDomain function with the sslContext.]
            domain = makeDomain(this.deviceClientConfig.getAuthenticationProvider().getSSLContext());
        }
        catch (IOException e)
        {
            logger.LogDebug("setSslDomain has thrown exception: %s", e.getMessage());
            throw new TransportException(e);
        }

        // Codes_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_011: [The function shall set the domain on the transport.]
        transport.ssl(domain);
    }

    /**
     * Found the link by name.
     *
     * @param linkName link name to be used.
     * @return true if link found, false otherwise.
     */
    @Override
    protected Boolean isLinkFound(String linkName)
    {
        // Codes_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_012: [The function shall override the default behaviour and return true.]
        return true;
    }
}
