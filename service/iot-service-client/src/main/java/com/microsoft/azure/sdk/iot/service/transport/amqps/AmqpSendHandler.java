/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.*;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.HashMap;
import java.util.Map;

/**
 * Instance of the QPID-Proton-J BaseHandler class to override
 * the events what are needed to handle the send operation
 * Contains and sets connection parameters (path, port, endpoint)
 * Maintains the layers of AMQP protocol (Link, Session, Connection, Transport)
 * Creates and sets SASL authentication for transport
 */
@Slf4j
public class AmqpSendHandler extends AmqpConnectionHandler
{
    public static final String SEND_TAG = "sender";
    public static final String ENDPOINT = "/messages/devicebound";
    public static final String DEVICE_PATH_FORMAT = "/devices/%s/messages/devicebound";
    public static final String MODULE_PATH_FORMAT = "/devices/%s/modules/%s/messages/devicebound";

    private Object correlationId;
    private Sender cloudToDeviceMessageSendingLink;

    private AmqpResponseVerification amqpResponse;
    private org.apache.qpid.proton.message.Message messageToBeSent;

    private int nextTag = 0;

    /**
     * Constructor to set up connection parameters and initialize handshaker for transport
     *
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param iotHubServiceClientProtocol protocol to use
     */
    public AmqpSendHandler(
            String hostName,
            String userName,
            String sasToken,
            IotHubServiceClientProtocol iotHubServiceClientProtocol)
    {
        this(hostName, userName, sasToken, iotHubServiceClientProtocol, null);
    }

    /**
     * Constructor to set up connection parameters and initialize handshaker for transport
     *
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param iotHubServiceClientProtocol protocol to use
     * @param proxyOptions the proxy options to tunnel through, if a proxy should be used.
     */
    public AmqpSendHandler(
            String hostName,
            String userName,
            String sasToken,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions)
    {
        this(hostName, userName, sasToken, iotHubServiceClientProtocol, proxyOptions, null);
    }

    /**
     * Constructor to set up connection parameters and initialize handshaker for transport
     *
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param iotHubServiceClientProtocol protocol to use
     * @param proxyOptions the proxy options to tunnel through, if a proxy should be used.
     * @param sslContext the SSL context to use during the TLS handshake when opening the connection. If null, a default
     *                   SSL context will be generated. This default SSLContext trusts the IoT Hub public certificates.
     */
    public AmqpSendHandler(
            String hostName,
            String userName,
            String sasToken,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        super(hostName, userName, sasToken, iotHubServiceClientProtocol, proxyOptions, sslContext);
    }

    AmqpSendHandler(
            String hostName,
            TokenCredential tokenProvider,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        super(hostName, tokenProvider, iotHubServiceClientProtocol, proxyOptions, sslContext);
    }

    AmqpSendHandler(
            String hostName,
            AzureSasCredential sasTokenProvider,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        super(hostName, sasTokenProvider, iotHubServiceClientProtocol, proxyOptions, sslContext);
    }

    /**
     * Create Proton message from deviceId and content string
     * @param deviceId The device name string
     * @param message The message to be sent
     */
    public void createProtonMessage(String deviceId, com.microsoft.azure.sdk.iot.service.Message message)
    {
        populateProtonMessage(String.format(DEVICE_PATH_FORMAT, deviceId), message);
    }

    /**
     * Create Proton message from deviceId and content string
     * @param deviceId The device name string
     * @param moduleId The device name string
     * @param message The message to be sent
     */
    public void createProtonMessage(
            String deviceId,
            String moduleId,
            com.microsoft.azure.sdk.iot.service.Message message)
    {
        populateProtonMessage(String.format(MODULE_PATH_FORMAT, deviceId, moduleId), message);
    }

    private void populateProtonMessage(String targetPath, com.microsoft.azure.sdk.iot.service.Message message)
    {
        org.apache.qpid.proton.message.Message protonMessage = Proton.message();

        Properties properties = new Properties();
        properties.setMessageId(message.getMessageId());
        properties.setTo(targetPath);
        properties.setAbsoluteExpiryTime(message.getExpiryTimeUtc());
        properties.setCorrelationId(message.getCorrelationId());
        if (message.getUserId() != null)
        {
            properties.setUserId(new Binary(message.getUserId().getBytes()));
        }
        protonMessage.setProperties(properties);

        if (message.getProperties() != null && message.getProperties().size() > 0)
        {
            Map<String, Object> applicationPropertiesMap = new HashMap<>(message.getProperties().size());
            for (Map.Entry<String, String> entry : message.getProperties().entrySet())
            {
                applicationPropertiesMap.put(entry.getKey(), entry.getValue());
            }
            ApplicationProperties applicationProperties = new ApplicationProperties(applicationPropertiesMap);
            protonMessage.setApplicationProperties(applicationProperties);
        }

        Binary binary;
        //Messages may have no payload, so check that the message has a payload before giving message.getBytes() as the payload
        if (message.getBytes() != null)
        {
            binary = new Binary(message.getBytes());
        }
        else
        {
            binary = new Binary(new byte[0]);
        }

        Section section = new Data(binary);
        protonMessage.setBody(section);
        messageToBeSent = protonMessage;
    }

    /**
     * Event handler for the link flow event
     * @param event The proton event object
     */
    @Override
    public void onLinkFlow(Event event)
    {
        if (messageToBeSent != null)
        {
            Sender snd = (Sender)event.getLink();
            if (snd.getCredit() > 0)
            {
                this.correlationId = messageToBeSent.getCorrelationId();
                log.debug("Sending cloud to device message with correlation id {}", this.correlationId);
                byte[] msgData = new byte[1024];
                int length;
                while (true)
                {
                    try
                    {
                        length = messageToBeSent.encode(msgData, 0, msgData.length);
                        break;
                    }
                    catch (BufferOverflowException e)
                    {
                        msgData = new byte[msgData.length * 2];
                    }
                }

                byte[] tag = String.valueOf(nextTag).getBytes();

                //want to avoid negative delivery tags since -1 is the designated failure value
                if (this.nextTag == Integer.MAX_VALUE || this.nextTag < 0)
                {
                    this.nextTag = 0;
                }
                else
                {
                    this.nextTag++;
                }

                Delivery dlv = snd.delivery(tag);
                snd.send(msgData, 0, length);

                snd.advance();

                this.messageToBeSent = null;
            }
        }
    }

    @Override
    public void onDelivery(Event event)
    {
        log.trace("Acknowledgement arrived for sent cloud to device message with correlation id {}", this.correlationId);

        Delivery d = event.getDelivery();

        DeliveryState remoteState = d.getRemoteState();

        amqpResponse = new AmqpResponseVerification(remoteState);

        d.settle();

        Sender snd = event.getSender();

        if (snd.getLocalState() == EndpointState.ACTIVE)
        {
            // By closing the link locally, proton-j will fire an event onLinkLocalClose. Within ErrorLoggingBaseHandlerWithCleanup,
            // onLinkLocalClose closes the session locally and eventually the connection and reactor
            if (remoteState.getClass().equals(Accepted.class))
            {
                log.debug("Closing AMQP cloud to device message sender link since the message was delivered");
            }
            else
            {
                log.debug("Closing AMQP cloud to device message sender link since the message failed to be delivered");
            }

            snd.close();
        }
    }

    @Override
    public void onConnectionRemoteClose(Event event)
    {
        super.onConnectionRemoteClose(event);
        event.getTransport().close_tail();
    }

    public void verifySendSucceeded() throws IotHubException, IOException
    {
        super.verifyConnectionWasOpened();

        if (amqpResponse != null)
        {
            if (amqpResponse.getException() != null)
            {
                throw amqpResponse.getException();
            }
        }
    }

    @Override
    public void onAuthenticationSucceeded()
    {
        // Only open the session and sending link if this authentication was for the first open. This callback
        // will be executed again after every proactive renewal, but nothing needs to be done after a proactive renewal
        if (this.cloudToDeviceMessageSendingLink == null)
        {
            // Every session or link could have their own handler(s) if we
            // wanted simply by adding the handler to the given session
            // or link

            Session cloudToDeviceMessageSession = this.connection.session();

            // If a link doesn't have an event handler, the events go to
            // its parent session. If the session doesn't have a handler
            // the events go to its parent connection. If the connection
            // doesn't have a handler, the events go to the reactor.

            Map<Symbol, Object> properties = new HashMap<>();
            properties.put(Symbol.getSymbol(TransportUtils.versionIdentifierKey), TransportUtils.USER_AGENT_STRING);
            cloudToDeviceMessageSession.open();

            this.cloudToDeviceMessageSendingLink = cloudToDeviceMessageSession.sender(SEND_TAG);
            this.cloudToDeviceMessageSendingLink.setProperties(properties);
            Target t = new Target();
            t.setAddress(ENDPOINT);
            this.cloudToDeviceMessageSendingLink.setTarget(t);
            this.cloudToDeviceMessageSendingLink.open();

            log.debug("Opening sender link for amqp cloud to device messages");
        }
    }
}
