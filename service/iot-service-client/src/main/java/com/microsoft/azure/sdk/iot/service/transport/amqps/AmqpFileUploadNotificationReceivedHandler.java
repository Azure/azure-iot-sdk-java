/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.deps.ws.impl.WebSocketImpl;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.reactor.FlowController;
import org.apache.qpid.proton.reactor.Handshaker;

import java.util.HashMap;
import java.util.Map;

/**
 * Instance of the QPID-Proton-J BaseHandler class to override
 * the events what are needed to handle the receive operation
 * Contains and sets connection parameters (path, port, endpoint)
 * Maintains the layers of AMQP protocol (Link, Session, Connection, Transport)
 * Creates and sets SASL authentication for transport
 */
public class AmqpFileUploadNotificationReceivedHandler extends BaseHandler
{
    private static final String FILE_NOTIFICATION_RECEIVE_TAG = "filenotificationreceiver";
    private static final String SEND_PORT_AMQPS = ":5671";
    private static final String SEND_PORT_AMQPS_WS = ":443";
    private static final String FILENOTIFICATION_ENDPOINT = "/messages/serviceBound/filenotifications";
    private static final String WEBSOCKET_PATH = "/$iothub/websocket";
    private static final String WEBSOCKET_SUB_PROTOCOL = "AMQPWSB10";

    private final String hostName;
    private final String userName;
    private final String sasToken;

    private final IotHubServiceClientProtocol iotHubServiceClientProtocol;
    private final String webSocketHostName;

    private AmqpFeedbackReceivedEvent amqpFeedbackReceivedEvent;

    /**
     * Constructor to set up connection parameters and initialize
     * handshaker and flow controller for transport
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     * @param amqpFeedbackReceivedEvent callback to delegate the received message to the user API
     */
    AmqpFileUploadNotificationReceivedHandler(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol, AmqpFeedbackReceivedEvent amqpFeedbackReceivedEvent)
    {
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_001: [The constructor shall copy all input parameters to private member variables for event processing]
        if (hostName == null || userName == null || sasToken == null || iotHubServiceClientProtocol == null || amqpFeedbackReceivedEvent == null ||
                hostName.isEmpty() || userName.isEmpty() || sasToken.isEmpty())
        {
            //Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_021: [** The constructor shall throw IllegalArgumentException if any of the parameters are null or empty **]
            throw new IllegalArgumentException("Input parameters cannot be null or empty");
        }
        this.iotHubServiceClientProtocol = iotHubServiceClientProtocol;
        this.webSocketHostName = hostName;
        if (this.iotHubServiceClientProtocol == IotHubServiceClientProtocol.AMQPS_WS)
        {
            this.hostName = hostName + SEND_PORT_AMQPS_WS;
        }
        else
        {
            this.hostName = hostName + SEND_PORT_AMQPS;
        }

        this.userName = userName;
        this.sasToken = sasToken;
        this.amqpFeedbackReceivedEvent = amqpFeedbackReceivedEvent;

        // Add a child handler that performs some default handshaking
        // behaviour.

        // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_002: [The constructor shall initialize a new Handshaker (Proton) object to handle communication handshake]
        add(new Handshaker());
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_003: [The constructor shall initialize a new FlowController (Proton) object to handle communication handshake]
        add(new FlowController());
    }

    /**
     * Create Proton SslDomain object from Address using the given Ssl mode
     * @param mode Proton enum value of requested Ssl mode
     * @return The created Ssl domain
     */
    private SslDomain makeDomain(SslDomain.Mode mode)
    {
        SslDomain domain = Proton.sslDomain();
        domain.init(mode);

        return domain;
    }

    /**
     * Event handler for the on delivery event
     * @param event The proton event object
     */
    @Override
    public void onDelivery(Event event)
    {
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_004: [The event handler shall get the Link, Receiver and Delivery (Proton) objects from the event]
        Receiver recv = (Receiver)event.getLink();
        Delivery delivery = recv.current();

        if (delivery.isReadable() && !delivery.isPartial() && delivery.getLink().getName().equalsIgnoreCase(FILE_NOTIFICATION_RECEIVE_TAG))
        {
            // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_005: [The event handler shall read the received buffer]
            int size = delivery.pending();
            byte[] buffer = new byte[size];
            int read = recv.recv(buffer, 0, buffer.length);
            recv.advance();

            // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_006: [The event handler shall create a Message (Proton) object from the decoded buffer]
            org.apache.qpid.proton.message.Message msg = Proton.message();
            msg.decode(buffer, 0, read);
          
            // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_007: [The event handler shall settle the Delivery with the Accepted outcome]
            delivery.disposition(Accepted.getInstance());
            delivery.settle();
          
            // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_008: [The event handler shall close the Session and Connection (Proton)]
            recv.getSession().close();
            recv.getSession().getConnection().close();

            // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_009: [The event handler shall call the FeedbackReceived callback if it has been initialized]
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
    public void onConnectionBound(Event event)
    {
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_019: [The event handler shall set the SASL PLAIN authentication on the Transport using the given user name and sas token]
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_010: [The event handler shall set ANONYMUS_PEER authentication mode on the domain of the Transport]
        Transport transport = event.getConnection().getTransport();
        if (transport != null)
        {
            if (this.iotHubServiceClientProtocol == IotHubServiceClientProtocol.AMQPS_WS)
            {
                // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_020: [** The event handler shall not initialize WebSocket if the protocol is AMQP **]
                WebSocketImpl webSocket = new WebSocketImpl();
                webSocket.configure(this.webSocketHostName, WEBSOCKET_PATH, 0, WEBSOCKET_SUB_PROTOCOL, null, null);
                ((TransportInternal)transport).addTransportLayer(webSocket);
            }
            Sasl sasl = transport.sasl();
            sasl.plain(this.userName, this.sasToken);

            SslDomain domain = makeDomain(SslDomain.Mode.CLIENT);
            domain.setPeerAuthentication(SslDomain.VerifyMode.ANONYMOUS_PEER);
            Ssl ssl = transport.ssl(domain);
        }
    }

    @Override
    public void onConnectionInit(Event event)
    {
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_011: [The event handler shall set the host name on the connection]
        Connection conn = event.getConnection();
        conn.setHostname(hostName);

        // Every session or link could have their own handler(s) if we
        // wanted simply by adding the handler to the given session
        // or link

        // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_012: [The event handler shall create a Session (Proton) object from the connection]
        Session ssn = conn.session();

        // If a link doesn't have an event handler, the events go to
        // its parent session. If the session doesn't have a handler
        // the events go to its parent connection. If the connection
        // doesn't have a handler, the events go to the reactor.

        // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_013: [The event handler shall create a Receiver (Proton) object and set the protocol tag on it to a predefined constant]
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_017: [The Receiver object shall have the properties set to service client version identifier.]
        Map<Symbol, Object> properties = new HashMap<>();
        properties.put(Symbol.getSymbol(TransportUtils.versionIdentifierKey), TransportUtils.javaServiceClientIdentifier + TransportUtils.serviceVersion);

        Receiver notificationReceiver = ssn.receiver(FILE_NOTIFICATION_RECEIVE_TAG);
        notificationReceiver.setProperties(properties);

        // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_014: [The event handler shall open the Connection, the Session and the Receiver object]
        conn.open();
        ssn.open();
        notificationReceiver.open();
    }

    @Override
    public void onLinkInit(Event event)
    {
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_015: [The event handler shall create a new Target (Proton) object using the given endpoint address]
        // Codes_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_016: [The event handler shall get the Link (Proton) object and set its target to the created Target (Proton) object]
        Link link = event.getLink();
        if (event.getLink().getName().equals(FILE_NOTIFICATION_RECEIVE_TAG))
        {

            Target t = new Target();
            t.setAddress(FILENOTIFICATION_ENDPOINT);
            Source source = new Source();
            source.setAddress(FILENOTIFICATION_ENDPOINT);
            link.setTarget(t);
            link.setSource(source);
        }
    }
}