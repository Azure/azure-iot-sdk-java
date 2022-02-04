/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotification;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotificationParser;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotificationReceivedCallback;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Session;

import javax.net.ssl.SSLContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Instance of the QPID-Proton-J BaseHandler class to override
 * the events what are needed to handle the receive operation
 * Contains and sets connection parameters (path, port, endpoint)
 * Maintains the layers of AMQP protocol (Link, Session, Connection, Transport)
 * Creates and sets SASL authentication for transport
 */
@Slf4j
public class AmqpFileUploadNotificationReceivedHandler extends AmqpConnectionHandler
{
    private static final String FILE_NOTIFICATION_RECEIVE_TAG = "filenotificationreceiver";
    private static final String FILENOTIFICATION_ENDPOINT = "/messages/serviceBound/filenotifications";

    private final FileUploadNotificationReceivedCallback fileUploadNotificationReceivedCallback;
    private Receiver fileUploadNotificationReceiverLink;

    public AmqpFileUploadNotificationReceivedHandler(
            String hostName,
            String sasToken,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            FileUploadNotificationReceivedCallback fileUploadNotificationReceivedCallback,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        super(hostName, sasToken, iotHubServiceClientProtocol, proxyOptions, sslContext);
        this.fileUploadNotificationReceivedCallback = fileUploadNotificationReceivedCallback;
    }

    public AmqpFileUploadNotificationReceivedHandler(
            String hostName,
            TokenCredential credential,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            FileUploadNotificationReceivedCallback fileUploadNotificationReceivedCallback,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        super(hostName, credential, iotHubServiceClientProtocol, proxyOptions, sslContext);
        this.fileUploadNotificationReceivedCallback = fileUploadNotificationReceivedCallback;
    }

    public AmqpFileUploadNotificationReceivedHandler(
            String hostName,
            AzureSasCredential sasTokenProvider,
            IotHubServiceClientProtocol iotHubServiceClientProtocol,
            FileUploadNotificationReceivedCallback fileUploadNotificationReceivedCallback,
            ProxyOptions proxyOptions,
            SSLContext sslContext)
    {
        super(hostName, sasTokenProvider, iotHubServiceClientProtocol, proxyOptions, sslContext);
        this.fileUploadNotificationReceivedCallback = fileUploadNotificationReceivedCallback;
    }

    @Override
    public void onTimerTask(Event event)
    {
        //This callback is scheduled by the reactor runner as a signal to gracefully close the connection, starting with its link
        if (this.fileUploadNotificationReceiverLink != null)
        {
            log.debug("Shutdown event occurred, closing file upload notification receiver link");
            this.fileUploadNotificationReceiverLink.close();
        }
    }

    /**
     * Event handler for the on delivery event
     * @param event The proton event object
     */
    @Override
    public void onDelivery(Event event)
    {
        Receiver recv = (Receiver)event.getLink();
        Delivery delivery = recv.current();

        if (delivery.isReadable() && !delivery.isPartial() && delivery.getLink().getName().equalsIgnoreCase(FILE_NOTIFICATION_RECEIVE_TAG))
        {
            int size = delivery.pending();
            byte[] buffer = new byte[size];
            int read = recv.recv(buffer, 0, buffer.length);
            recv.advance();

            org.apache.qpid.proton.message.Message msg = Proton.message();
            msg.decode(buffer, 0, read);

            if (msg.getBody() instanceof Data)
            {
                String feedbackJson = msg.getBody().toString();

                IotHubMessageResult messageResult = IotHubMessageResult.ABANDON;

                try
                {
                    FileUploadNotificationParser notificationParser = new FileUploadNotificationParser(feedbackJson);

                    FileUploadNotification fileUploadNotification = new FileUploadNotification(notificationParser.getDeviceId(),
                        notificationParser.getBlobUri(), notificationParser.getBlobName(), notificationParser.getLastUpdatedTime(),
                        notificationParser.getBlobSizeInBytesTag(), notificationParser.getEnqueuedTimeUtc());

                    messageResult = fileUploadNotificationReceivedCallback.onFileUploadNotificationReceived(fileUploadNotification);
                }
                catch (Exception e)
                {
                    // this should never happen. However if it does, proton can't handle it. So guard against throwing it at proton.
                    log.warn("Encountered an exception while handling file upload notification", e);
                }

                DeliveryState deliveryState = Accepted.getInstance();
                if (messageResult == IotHubMessageResult.ABANDON)
                {
                    deliveryState = Released.getInstance();
                }
                else if (messageResult == IotHubMessageResult.COMPLETE)
                {
                    deliveryState = Accepted.getInstance();
                }

                delivery.disposition(deliveryState);
                delivery.settle();
                recv.flow(1); // flow back the credit so the service can send another message now
            }
        }
    }

    @Override
    public void onAuthenticationSucceeded()
    {
        // Only open the session and receiver link if this authentication was for the first open. This callback
        // will be executed again after every proactive renewal, but nothing needs to be done after a proactive renewal
        if (fileUploadNotificationReceiverLink == null)
        {
            // Every session or link could have their own handler(s) if we
            // wanted simply by adding the handler to the given session
            // or link

            Session ssn = this.connection.session();

            // If a link doesn't have an event handler, the events go to
            // its parent session. If the session doesn't have a handler
            // the events go to its parent connection. If the connection
            // doesn't have a handler, the events go to the reactor.

            Map<Symbol, Object> properties = new HashMap<>();
            properties.put(Symbol.getSymbol(TransportUtils.versionIdentifierKey), TransportUtils.USER_AGENT_STRING);

            fileUploadNotificationReceiverLink = ssn.receiver(FILE_NOTIFICATION_RECEIVE_TAG);
            fileUploadNotificationReceiverLink.setProperties(properties);

            log.debug("Opening connection, session and link for amqp file upload notification receiver");
            ssn.open();
            fileUploadNotificationReceiverLink.open();
            Source source = new Source();
            source.setAddress(FILENOTIFICATION_ENDPOINT);
            fileUploadNotificationReceiverLink.setSource(source);

            // We only want to receive, at most, one file upload notification since each receive call the user makes can
            // only return either a single file upload notification or null (no file upload notification received).
            // Extend only a single link credit to the service so that the service can't send more than one message.
            fileUploadNotificationReceiverLink.flow(1);
        }
    }
}