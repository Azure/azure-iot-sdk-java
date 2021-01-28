/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.reactor.FlowController;
import org.apache.qpid.proton.reactor.Handshaker;

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

    private final AmqpFeedbackReceivedEvent amqpFeedbackReceivedEvent;
    private Receiver fileUploadNotificationReceiverLink;

    /**
     * Constructor to set up connection parameters and initialize
     * handshaker and flow controller for transport
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param amqpFeedbackReceivedEvent callback to delegate the received message to the user API
     * @param proxyOptions the proxy options to tunnel through, if a proxy should be used.
     * @param sslContext the SSL context to use during the TLS handshake when opening the connection. If null, a default
     *                   SSL context will be generated. This default SSLContext trusts the IoT Hub public certificates.
     */
    AmqpFileUploadNotificationReceivedHandler(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol, AmqpFeedbackReceivedEvent amqpFeedbackReceivedEvent, ProxyOptions proxyOptions, SSLContext sslContext)
    {
        super(hostName, userName, sasToken, iotHubServiceClientProtocol, proxyOptions, sslContext);

        this.amqpFeedbackReceivedEvent = amqpFeedbackReceivedEvent;

        // Add a child handler that performs some default handshaking
        // behaviour.

        add(new Handshaker());
        add(new FlowController());
    }

    AmqpFileUploadNotificationReceivedHandler(String hostName, TokenCredential authenticationTokenProvider, AuthenticationType authorizationType, IotHubServiceClientProtocol iotHubServiceClientProtocol, AmqpFeedbackReceivedEvent amqpFeedbackReceivedEvent, ProxyOptions proxyOptions, SSLContext sslContext)
    {
        super(hostName, authenticationTokenProvider, authorizationType, iotHubServiceClientProtocol, proxyOptions, sslContext);

        this.amqpFeedbackReceivedEvent = amqpFeedbackReceivedEvent;

        // Add a child handler that performs some default handshaking
        // behaviour.

        add(new Handshaker());
        add(new FlowController());
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
          
            if (recv.getLocalState() == EndpointState.ACTIVE)
            {
                delivery.disposition(Accepted.getInstance());
                delivery.settle();

                //By closing the link locally, proton-j will fire an event onLinkLocalClose. Within ErrorLoggingBaseHandlerWithCleanup,
                // onLinkLocalClose closes the session locally and eventually the connection and reactor
                log.debug("Closing amqp file upload notification receiver link since a file upload notification was received");
                recv.close();
            }
            else
            {
                //Each connection should only handle one message. Any further deliveries must be released so that
                // another connection can receive it instead
                log.trace("Releasing a delivery since this connection already handled one, service will send it again later");
                delivery.disposition(Released.getInstance());
                delivery.settle();
            }

            if (amqpFeedbackReceivedEvent != null)
            {
                if (msg.getBody() instanceof Data)
                {
                    Data feedbackJson = (Data) msg.getBody();
                    amqpFeedbackReceivedEvent.onFeedbackReceived(feedbackJson.getValue().toString());
                }
            }
        }
    }

    @Override
    public void onAuthenticationSucceeded(Session session)
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
        }
    }
}