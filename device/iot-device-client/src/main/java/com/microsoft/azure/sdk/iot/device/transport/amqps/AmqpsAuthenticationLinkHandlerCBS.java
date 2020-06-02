// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.engine.Sasl;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.message.impl.MessageImpl;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Defines the authentication session that exists in every SAS based amqp connection. There is one CBS sender link to send
 * SAS tokens over, and one CBS receiver link to receive authentication status from.
 */
@Slf4j
public final class AmqpsAuthenticationLinkHandlerCBS extends AmqpsAuthenticationLinkHandler
{
    private String PROP_KEY_STATUS_CODE = "status-code";

    public static final String SENDER_LINK_ENDPOINT_PATH = "$cbs";
    public static final String RECEIVER_LINK_ENDPOINT_PATH = "$cbs";

    public static final String SENDER_LINK_TAG_PREFIX = "cbs-sender-";
    public static final String RECEIVER_LINK_TAG_PREFIX = "cbs-receiver-";

    private static final String CBS_TO = "$cbs";
    private static final String CBS_REPLY = "cbs";

    private static final String OPERATION_KEY = "operation";
    private static final String TYPE_KEY = "type";
    private static final String NAME_KEY = "name";

    private static final String OPERATION_VALUE = "put-token";
    private static final String TYPE_VALUE = "servicebus.windows.net:sastoken";

    private static final String DEVICES_PATH =  "/devices/";

    private long nextTag = 0;

    /**
     * This constructor creates an instance of AmqpsAuthenticationLinkHandlerCBS class and initializes member variables
     *
     * @throws IllegalArgumentException if deviceClientConfig is null.
     */
    public AmqpsAuthenticationLinkHandlerCBS() throws IllegalArgumentException
    {
        super();

        this.senderLinkTag = SENDER_LINK_TAG_PREFIX + senderLinkTag;
        this.receiverLinkTag = RECEIVER_LINK_TAG_PREFIX + receiverLinkTag;

        this.senderLinkAddress = SENDER_LINK_ENDPOINT_PATH;
        this.receiverLinkAddress = RECEIVER_LINK_ENDPOINT_PATH;
    }

    @Override
    public String getLinkInstanceType()
    {
        return "cbs";
    }

    /**
     * Get the message from Proton if the link name matches.
     * Set the message type to CBS authentication if the message is not null.
     *
     * @param linkName The receiver link's name to read from
     * @return the Proton message
     */
    @Override
    protected AmqpsMessage getMessageFromReceiverLink(String linkName)
    {
        // Codes_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_023: [The function shall call the super to get the message.]
        AmqpsMessage amqpsMessage = super.getMessageFromReceiverLink(linkName);
        if (amqpsMessage != null)
        {
            // Codes_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_024: [The function shall set the message type to CBS authentication if the message is not null.]
            amqpsMessage.setAmqpsMessageType(MessageType.CBS_AUTHENTICATION);

            if (amqpsMessage.getCorrelationId() != null)
            {
                this.log.trace("Received amqp message on cbs receiver link with link correlation id {} and message correlation id {}", this.linkCorrelationId, amqpsMessage.getCorrelationId());
            }
            else
            {
                this.log.trace("Received amqp message on cbs receiver link with link correlation id {}", this.linkCorrelationId);
            }
        }

        return amqpsMessage;
    }

    /**
     * Verify if the given message is a 200OK for authentication.
     *
     * @param amqpsMessage the message to evaluate.
     * @param authenticationCorrelationId the expected correlation ID.
     * @return true if it is 200 OK and correlationId matches, false otherwise.
     */
    @Override
    protected boolean handleAuthenticationMessage(AmqpsMessage amqpsMessage, UUID authenticationCorrelationId)
    {
        if (amqpsMessage != null)
        {
            if (amqpsMessage.getApplicationProperties() != null && amqpsMessage.getProperties() != null)
            {
                Properties properties = amqpsMessage.getProperties();
                Object correlationIdValue = properties.getCorrelationId();
                if (correlationIdValue.equals(authenticationCorrelationId))
                {
                    Map<String, Object> applicationProperties = amqpsMessage.getApplicationProperties().getValue();

                    for (Map.Entry<String, Object> entry : applicationProperties.entrySet())
                    {
                        String propertyKey = entry.getKey();
                        if (propertyKey.equals(PROP_KEY_STATUS_CODE) && entry.getValue() instanceof Integer)
                        {
                            int propertyValue = (int) entry.getValue();
                            if (propertyValue == 200)
                            {
                                amqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.COMPLETE);

                                return true;
                            }
                            else
                            {
                                this.log.error("CBS authentication message was rejected with status {}, authentication has failed", propertyValue);
                                return false;
                            }
                        }
                    }

                    log.warn("Could not handle authentication message because the received message did not contain a status code even though the correlation id was the expected value");
                }
                else
                {
                    log.trace("Could not handle authentication message because the received correlation id did not match the expected value");
                }
            }
            else
            {
                log.warn("Could not handle authentication message because it had no application properties or had no system properties");
            }
        }
        else
        {
            log.warn("Could not handle authentication message because it was null");
        }

        return false;
    }

    /**
     * Create domain from the SSLContext, set the sasl mechanism to 
     * ANONYMOUS and set domain on the transport
     * 
     * @param transport Proton-J Transport object
     */
    @Override
    protected void setSslDomain(Transport transport, SSLContext sslContext)
    {
        Sasl sasl = transport.sasl();
        sasl.setMechanisms("ANONYMOUS");
        SslDomain domain = makeDomain(sslContext);
        transport.ssl(domain);
    }

    /**
     * Start CBS authentication process by creating an adding 
     * authentication message to the send queue 
     * 
     * @param deviceClientConfig device configuration to use for 
     *                           authentication
     * @throws TransportException when CBS Authentication Message failed to be created
     */
    @Override
    protected void authenticate(DeviceClientConfig deviceClientConfig, UUID correlationId) throws TransportException
    {
        this.log.trace("authenticate called in AmqpsAuthenticationLinkHandlerCBS");
        MessageImpl outgoingMessage = createCBSAuthenticationMessage(deviceClientConfig, correlationId);
        byte[] msgData = new byte[1024];
        int length;

        while (true)
        {
            try
            {
                length = outgoingMessage.encode(msgData, 0, msgData.length);
                break;
            }
            catch (BufferOverflowException e)
            {
                msgData = new byte[msgData.length * 2];
            }
        }
        byte[] deliveryTag = String.valueOf(this.nextTag).getBytes();

        if (this.nextTag == Integer.MAX_VALUE || this.nextTag < 0)
        {
            this.nextTag = 0;
        }
        else
        {
            this.nextTag++;
        }

        this.sendMessageAndGetDeliveryTag(MessageType.CBS_AUTHENTICATION, msgData, 0, length, deliveryTag);
    }

    /**
     * Create a CBS authentication message for the given device 
     * client 
     * 
     * @param deviceClientConfig device client configuration
     * @throws TransportException when failed to get renewed SAS token
     * @return MessageImpl the Proton-j message to send
     */
    private MessageImpl createCBSAuthenticationMessage(DeviceClientConfig deviceClientConfig, UUID messageId) throws TransportException
    {
        MessageImpl outgoingMessage = (MessageImpl) Proton.message();

        Properties properties = new Properties();
        properties.setMessageId(messageId);

        properties.setTo(CBS_TO);
        properties.setReplyTo(CBS_REPLY);
        outgoingMessage.setProperties(properties);

        Map<String, Object> userProperties = new HashMap<>(3);
        userProperties.put(OPERATION_KEY, OPERATION_VALUE);
        userProperties.put(TYPE_KEY, TYPE_VALUE);

        String host = deviceClientConfig.getGatewayHostname();
        if (host == null || host.isEmpty())
        {
            host = deviceClientConfig.getIotHubHostname();
        }

        userProperties.put(NAME_KEY, host + DEVICES_PATH + deviceClientConfig.getDeviceId());
        ApplicationProperties applicationProperties = new ApplicationProperties(userProperties);
        outgoingMessage.setApplicationProperties(applicationProperties);

        Section section;
        try
        {
            section = new AmqpValue(deviceClientConfig.getSasTokenAuthentication().getRenewedSasToken(true, true));
            outgoingMessage.setBody(section);
        }
        catch (IOException e)
        {
            log.error("getRenewedSasToken has thrown exception while building new cbs authentication message", e);
            throw new TransportException(e);
        }

        return outgoingMessage;
    }
}
