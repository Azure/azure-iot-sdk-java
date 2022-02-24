/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.messaging.ErrorContext;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.messaging.Message;
import com.microsoft.azure.sdk.iot.service.messaging.SendResult;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;

import javax.net.ssl.SSLContext;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Instance of the QPID-Proton-J BaseHandler class to override
 * the events what are needed to handle the receive operation
 * Contains and sets connection parameters (path, port, endpoint)
 * Maintains the layers of AMQP protocol (Link, Session, Connection, Transport)
 * Creates and sets SASL authentication for transport
 */
@Slf4j
public class CloudToDeviceMessageConnectionHandler extends AmqpConnectionHandler implements LinkStateCallback
{
    private static final String SEND_TAG = "sender";
    private static final String ENDPOINT = "/messages/devicebound";

    private Session session;
    private CloudToDeviceMessageSenderLinkHandler cloudToDeviceMessageSenderLinkHandler;

    @Setter
    private Runnable onConnectionOpenedCallback;

    public CloudToDeviceMessageConnectionHandler(
            String connectionString,
            IotHubServiceClientProtocol protocol,
            Consumer<ErrorContext> errorProcessor,
            ProxyOptions proxyOptions,
            SSLContext sslContext,
            int keepAliveIntervalSeconds)
    {
        super(connectionString, protocol, errorProcessor, proxyOptions, sslContext, keepAliveIntervalSeconds);
    }

    public CloudToDeviceMessageConnectionHandler(
            String hostName,
            TokenCredential credential,
            IotHubServiceClientProtocol protocol,
            Consumer<ErrorContext> errorProcessor,
            ProxyOptions proxyOptions,
            SSLContext sslContext,
            int keepAliveIntervalSeconds)
    {
        super(hostName, credential, protocol, errorProcessor, proxyOptions, sslContext, keepAliveIntervalSeconds);
    }

    public CloudToDeviceMessageConnectionHandler(
            String hostName,
            AzureSasCredential azureSasCredential,
            IotHubServiceClientProtocol protocol,
            Consumer<ErrorContext> errorProcessor,
            ProxyOptions proxyOptions,
            SSLContext sslContext,
            int keepAliveIntervalSeconds)
    {
        super(hostName, azureSasCredential, protocol, errorProcessor, proxyOptions, sslContext, keepAliveIntervalSeconds);
    }

    @Override
    public void onAuthenticationSucceeded()
    {
        // Only open the session and sending link if this authentication was for the first open. This callback
        // will be executed again after every proactive renewal, but nothing needs to be done after a proactive renewal
        if (this.cloudToDeviceMessageSenderLinkHandler == null)
        {
            // Every session or link could have their own handler(s) if we
            // wanted simply by adding the handler to the given session
            // or link

            this.session = this.connection.session();

            // If a link doesn't have an event handler, the events go to
            // its parent session. If the session doesn't have a handler
            // the events go to its parent connection. If the connection
            // doesn't have a handler, the events go to the reactor.

            Map<Symbol, Object> properties = new HashMap<>();
            properties.put(Symbol.getSymbol(TransportUtils.versionIdentifierKey), TransportUtils.USER_AGENT_STRING);
            this.session.open();

            Sender cloudToDeviceMessageSendingLink = this.session.sender(SEND_TAG);
            cloudToDeviceMessageSendingLink.setProperties(properties);
            Target t = new Target();
            t.setAddress(ENDPOINT);
            cloudToDeviceMessageSendingLink.setTarget(t);
            cloudToDeviceMessageSendingLink.open();

            this.cloudToDeviceMessageSenderLinkHandler =
                new CloudToDeviceMessageSenderLinkHandler(
                    cloudToDeviceMessageSendingLink,
                    UUID.randomUUID().toString(),
                    this);

            log.debug("Opening sender link for amqp cloud to device messages");
        }
    }

    @Override
    public void onSenderLinkRemoteOpen()
    {
        this.onConnectionOpenedCallback.run();
    }

    @Override
    public void onReceiverLinkRemoteOpen()
    {
        // no action needed. This connection doesn't open any receiver links other than the CBS link which is handled elsewhere.
    }

    public boolean isOpen()
    {
        return super.isOpen()
            && this.session != null
            && this.session.getLocalState() == EndpointState.ACTIVE
            && this.session.getRemoteState() == EndpointState.ACTIVE
            && this.cloudToDeviceMessageSenderLinkHandler != null
            && this.cloudToDeviceMessageSenderLinkHandler.isOpen();
    }

    public void sendAsync(String deviceId, String moduleId, Message iotHubMessage, Consumer<SendResult> callback, Object context)
    {
        if (!isOpen())
        {
            throw new IllegalStateException("Client is currently closed. Must open messagingClient before sending.");
        }

        this.cloudToDeviceMessageSenderLinkHandler.sendAsync(deviceId, moduleId, iotHubMessage, callback, context);
    }

    @Override
    public void closeAsync(Runnable onConnectionClosedCallback)
    {
        if (this.cloudToDeviceMessageSenderLinkHandler != null)
        {
            log.debug("Shutdown event occurred, closing file upload notification receiver link");
            this.cloudToDeviceMessageSenderLinkHandler.close();
        }

        if (this.session != null)
        {
            log.debug("Shutdown event occurred, closing session");
            this.session.close();
        }

        super.closeAsync(onConnectionClosedCallback);
    }

    @Override
    public void onReactorFinal(Event event)
    {
        this.cloudToDeviceMessageSenderLinkHandler = null;
    }
}