/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.messaging.ErrorContext;
import com.microsoft.azure.sdk.iot.service.messaging.FeedbackBatch;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotification;
import com.microsoft.azure.sdk.iot.service.messaging.AcknowledgementType;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Session;

import javax.net.ssl.SSLContext;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Instance of the QPID-Proton-J BaseHandler class to override
 * the events what are needed to handle the receive operation
 * Contains and sets connection parameters (path, port, endpoint)
 * Maintains the layers of AMQP protocol (Link, Session, Connection, Transport)
 * Creates and sets SASL authentication for transport
 */
@Slf4j
public class EventReceivingConnectionHandler extends AmqpConnectionHandler implements LinkStateCallback
{
    private static final String FILE_NOTIFICATION_RECEIVE_TAG = "fileUploadNotificationReceiver";
    private static final String FILENOTIFICATION_ENDPOINT = "/messages/serviceBound/filenotifications";
    public static final String MESSAGE_FEEDBACK_RECEIVE_TAG = "cloudToDeviceMessageFeedbackReceiver";
    private static final String MESSAGE_FEEDBACK_ENDPOINT = "/messages/servicebound/feedback";

    private FileUploadNotificationReceiverLinkHandler fileUploadNotificationReceiverLinkHandler;
    private MessageFeedbackReceiverLinkHandler messageFeedbackReceiverLinkHandler;
    private Session session;

    @Setter
    private Runnable onConnectionOpenedCallback;

    private final Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationReceivedCallback;
    private final Function<FeedbackBatch, AcknowledgementType> messageFeedbackReceivedCallback;

    public EventReceivingConnectionHandler(
            String connectionString,
            IotHubServiceClientProtocol protocol,
            Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationReceivedCallback,
            Function<FeedbackBatch, AcknowledgementType> messageFeedbackReceivedCallback,
            Consumer<ErrorContext> errorProcessor,
            ProxyOptions proxyOptions,
            SSLContext sslContext,
            int keepAliveIntervalSeconds)
    {
        super(connectionString, protocol, errorProcessor, proxyOptions, sslContext, keepAliveIntervalSeconds);
        this.fileUploadNotificationReceivedCallback = fileUploadNotificationReceivedCallback;
        this.messageFeedbackReceivedCallback = messageFeedbackReceivedCallback;
    }

    public EventReceivingConnectionHandler(
            String hostName,
            TokenCredential credential,
            IotHubServiceClientProtocol protocol,
            Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationReceivedCallback,
            Function<FeedbackBatch, AcknowledgementType> messageFeedbackReceivedCallback,
            Consumer<ErrorContext> errorProcessor,
            ProxyOptions proxyOptions,
            SSLContext sslContext,
            int keepAliveIntervalSeconds)
    {
        super(hostName, credential, protocol, errorProcessor, proxyOptions, sslContext, keepAliveIntervalSeconds);
        this.fileUploadNotificationReceivedCallback = fileUploadNotificationReceivedCallback;
        this.messageFeedbackReceivedCallback = messageFeedbackReceivedCallback;
    }

    public EventReceivingConnectionHandler(
            String hostName,
            AzureSasCredential azureSasCredential,
            IotHubServiceClientProtocol protocol,
            Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationReceivedCallback,
            Function<FeedbackBatch, AcknowledgementType> messageFeedbackReceivedCallback,
            Consumer<ErrorContext> errorProcessor,
            ProxyOptions proxyOptions,
            SSLContext sslContext,
            int keepAliveIntervalSeconds)
    {
        super(hostName, azureSasCredential, protocol, errorProcessor, proxyOptions, sslContext, keepAliveIntervalSeconds);
        this.fileUploadNotificationReceivedCallback = fileUploadNotificationReceivedCallback;
        this.messageFeedbackReceivedCallback = messageFeedbackReceivedCallback;
    }

    @Override
    public void onAuthenticationSucceeded()
    {
        // Only open the session and receiver link if this authentication was for the first open. This callback
        // will be executed again after every proactive renewal, but nothing needs to be done after a proactive renewal
        if (fileUploadNotificationReceiverLinkHandler == null)
        {
            // Every session or link could have their own handler(s) if we
            // wanted simply by adding the handler to the given session
            // or link

            this.session = this.connection.session();
            this.session.open();

            // If a link doesn't have an event handler, the events go to
            // its parent session. If the session doesn't have a handler
            // the events go to its parent connection. If the connection
            // doesn't have a handler, the events go to the reactor.

            Map<Symbol, Object> properties = new HashMap<>();
            properties.put(Symbol.getSymbol(TransportUtils.versionIdentifierKey), TransportUtils.USER_AGENT_STRING);

            if (this.fileUploadNotificationReceivedCallback != null)
            {
                Receiver fileUploadNotificationReceiverLink = this.session.receiver(FILE_NOTIFICATION_RECEIVE_TAG);
                fileUploadNotificationReceiverLink.setProperties(properties);
                fileUploadNotificationReceiverLink.open();
                Source source = new Source();
                source.setAddress(FILENOTIFICATION_ENDPOINT);
                fileUploadNotificationReceiverLink.setSource(source);

                // We only want to receive, at most, one file upload notification since each receive call the user makes can
                // only return either a single file upload notification or null (no file upload notification received).
                // Extend only a single link credit to the service so that the service can't send more than one message.
                fileUploadNotificationReceiverLink.flow(1);
                fileUploadNotificationReceiverLinkHandler = new FileUploadNotificationReceiverLinkHandler(fileUploadNotificationReceiverLink, this, this.fileUploadNotificationReceivedCallback);
            }

            if (this.messageFeedbackReceivedCallback != null)
            {
                Receiver feedbackReceiverLink = this.session.receiver(MESSAGE_FEEDBACK_RECEIVE_TAG);
                feedbackReceiverLink.setProperties(properties);

                log.debug("Opening connection, session and link for amqp feedback receiver");
                feedbackReceiverLink.open();
                Source source = new Source();
                source.setAddress(MESSAGE_FEEDBACK_ENDPOINT);
                feedbackReceiverLink.setSource(source);

                // We only want to receive, at most, one feedback message since each receive call the user makes can only return
                // either a single feedback message or null (no feedback message received). Extend only a single link credit
                // to the service so that the service can't send more than one message.
                feedbackReceiverLink.flow(1);
                messageFeedbackReceiverLinkHandler = new MessageFeedbackReceiverLinkHandler(feedbackReceiverLink, this, this.messageFeedbackReceivedCallback);
            }
        }
    }

    @Override
    public void onSenderLinkRemoteOpen()
    {
        // no action needed. This connection doesn't open any sender links other than the CBS link which is handled elsewhere.
    }

    @Override
    public void onReceiverLinkRemoteOpen()
    {
        if (this.onConnectionOpenedCallback != null)
        {
            this.onConnectionOpenedCallback.run();
        }
    }

    @Override
    public void closeAsync(Runnable onConnectionClosedCallback)
    {
        if (this.session != null)
        {
            log.debug("Shutdown event occurred, closing session");
            this.session.close();
        }

        if (this.fileUploadNotificationReceiverLinkHandler != null)
        {
            log.debug("Shutdown event occurred, closing file upload notification receiver link");
            this.fileUploadNotificationReceiverLinkHandler.close();
        }

        if (this.messageFeedbackReceiverLinkHandler != null)
        {
            log.debug("Shutdown event occurred, closing cloud to device feedback message receiver link");
            this.messageFeedbackReceiverLinkHandler.close();
        }

        super.closeAsync(onConnectionClosedCallback);
    }

    @Override
    public void onReactorFinal(Event event)
    {
        this.messageFeedbackReceiverLinkHandler = null;
        this.fileUploadNotificationReceiverLinkHandler = null;
    }
}