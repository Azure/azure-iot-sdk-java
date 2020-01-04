/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.ErrorLoggingBaseHandler;
import com.microsoft.azure.sdk.iot.deps.ws.impl.WebSocketImpl;
import com.microsoft.azure.sdk.iot.service.DeliveryOutcome;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.Tools;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.FlowController;
import org.apache.qpid.proton.reactor.Handshaker;
import org.apache.qpid.proton.reactor.Reactor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AmqpConnectionHandler extends ErrorLoggingBaseHandler
{
    private Reactor reactor;

    protected String tag;
    protected String endpoint;

    protected static final String SEND_PORT_AMQPS = ":5671";
    protected static final String SEND_PORT_AMQPS_WS = ":443";
    protected static final String WEBSOCKET_PATH = "/$iothub/websocket";
    protected static final String WEBSOCKET_SUB_PROTOCOL = "AMQPWSB10";

    protected final String hostName;
    protected final String userName;
    protected final String sasToken;

    protected final int expectedLinkCount;
    private CountDownLatch openLinksLatch;

    private static final int OPEN_TIMEOUT_MILLISECONDS = 60 * 1000;

    protected int nextSendTag = 0;

    private final IotHubServiceClientProtocol iotHubServiceClientProtocol;
    private final String webSocketHostName;

    protected Exception savedException;

    public abstract DeliveryOutcome onMessageArrived(Message message);
    public abstract void onMessageAcknowledged(DeliveryState deliveryState);
    public abstract void openLinks(Session event, Map<Symbol, Object> properties);

    /**
     * Constructor to set up connection parameters and initialize
     * handshaker and flow controller for transport
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param sasToken The SAS token string
     */
    AmqpConnectionHandler(String hostName, String userName, String sasToken, IotHubServiceClientProtocol iotHubServiceClientProtocol, String tag, String endpoint, int expectedLinkCount)
    {
        if (Tools.isNullOrEmpty(hostName)
                || Tools.isNullOrEmpty(userName)
                || Tools.isNullOrEmpty(sasToken)
                || iotHubServiceClientProtocol == null
                || Tools.isNullOrEmpty(tag)
                || Tools.isNullOrEmpty(endpoint))
        {
            throw new IllegalArgumentException("Input parameters cannot be null or empty");
        }

        if (expectedLinkCount < 1)
        {
            throw new IllegalArgumentException("Expected link count must be a positive integer");
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

        this.tag = tag;
        this.endpoint = endpoint;

        add(new Handshaker());
        add(new FlowController());

        this.expectedLinkCount = expectedLinkCount;
    }

    public void open() throws IOException, InterruptedException
    {
        if (this.reactor != null)
        {
            return; //already open
        }

        this.openLinksLatch = new CountDownLatch(expectedLinkCount);

        this.openAsync();

        long startTime = System.currentTimeMillis();
        while (protonJExceptionParser == null && openLinksLatch.getCount() > 0)
        {
            if (System.currentTimeMillis() - startTime > OPEN_TIMEOUT_MILLISECONDS)
            {
                throw new IOException("Timed out waiting for amqp links to open");
            }
        }

        if (protonJExceptionParser != null && protonJExceptionParser.getError() != null)
        {
            throw new IOException(String.format("Encountered an exception while opening connection: %s with description %s", protonJExceptionParser.getError(), protonJExceptionParser.getErrorDescription()));
        }

    }

    private void openAsync() throws IOException
    {
        this.reactor = Proton.reactor(this);
        new Thread(new ReactorRunner(this.reactor)).start();
    }

    public void close()
    {
        if (this.reactor != null)
        {
            this.reactor.stop();
            this.reactor.free();
            this.reactor = null;
        }
    }

    @Override
    public void onReactorInit(Event event)
    {
        event.getReactor().connection(this);
    }

    /**
     * Event handler for the on delivery event
     * @param event The proton event object
     */
    @Override
    public void onDelivery(Event event)
    {
        if (event.getLink() instanceof Receiver)
        {
            Receiver recv = (Receiver)event.getLink();
            Delivery delivery = recv.current();

            if (delivery.isReadable() && !delivery.isPartial() && delivery.getLink().getName().equalsIgnoreCase(tag))
            {
                int size = delivery.pending();
                byte[] buffer = new byte[size];
                int read = recv.recv(buffer, 0, buffer.length);
                recv.advance();

                org.apache.qpid.proton.message.Message msg = Proton.message();
                msg.decode(buffer, 0, read);

                DeliveryOutcome outcome = onMessageArrived(msg);

                if (outcome == DeliveryOutcome.Complete)
                {
                    delivery.disposition(Accepted.getInstance());
                    delivery.settle();

                }
                else if (outcome == DeliveryOutcome.Abandon)
                {
                    delivery.disposition(Released.getInstance());
                    delivery.settle();
                }
                else if (outcome == DeliveryOutcome.Reject)
                {
                    //purposefully do nothing. Service will treat unack'd messages as rejected
                }
            }
        }
        else //Sender link received ack for sent message
        {
            Delivery d = event.getDelivery();

            DeliveryState remoteState = d.getRemoteState();

            onMessageAcknowledged(remoteState);

            d.settle();

            Sender snd = event.getSender();

            // Old API behavior dictated that the amqp connection close upon sending one C2D message
            snd.close();
            snd.getSession().close();
            snd.getSession().getConnection().close();
        }
    }

    @Override
    public void onConnectionBound(Event event)
    {
        Transport transport = event.getConnection().getTransport();
        if (transport != null)
        {
            if (this.iotHubServiceClientProtocol == IotHubServiceClientProtocol.AMQPS_WS)
            {
                WebSocketImpl webSocket = new WebSocketImpl();
                webSocket.configure(this.webSocketHostName, WEBSOCKET_PATH, 0, WEBSOCKET_SUB_PROTOCOL, null, null);
                ((TransportInternal)transport).addTransportLayer(webSocket);
            }
            Sasl sasl = transport.sasl();
            sasl.plain(this.userName, this.sasToken);

            SslDomain domain = makeDomain(SslDomain.Mode.CLIENT);
            domain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
            transport.ssl(domain);
        }
    }

    @Override
    public void onConnectionInit(Event event)
    {
        Connection conn = event.getConnection();
        conn.setHostname(hostName);
        Session ssn = conn.session();

        Map<Symbol, Object> properties = new HashMap<>();
        properties.put(Symbol.getSymbol(TransportUtils.versionIdentifierKey), TransportUtils.USER_AGENT_STRING);

        conn.open();
        ssn.open();
        openLinks(ssn, properties);
    }

    @Override
    public void onLinkInit(Event event)
    {
        Link link = event.getLink();
        if (event.getLink().getName().equals(tag))
        {
            Target t = new Target();
            t.setAddress(endpoint);
            Source source = new Source();
            source.setAddress(endpoint);
            link.setTarget(t);
            link.setSource(source);
        }
    }

    @Override
    public void onLinkRemoteOpen(Event event)
    {
        openLinksLatch.countDown();
    }

    @Override
    public void onConnectionRemoteClose(Event event)
    {
        // Code_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_34_032: [This function shall close the transport tail]
        event.getTransport().close_tail();
    }

    /**
     * If an exception was encountered while opening the AMQP connection, this function shall throw that saved exception
     * @throws IOException if an exception was encountered while openinging the AMQP connection. The encountered
     * exception will be the inner exception
     */
    protected void validateConnectionWasSuccessful() throws IOException, IotHubException
    {
        if (this.savedException != null)
        {
            throw new IOException("Connection failed to be established", this.savedException);
        }

        if (this.protonJExceptionParser != null && this.protonJExceptionParser.getError() != null)
        {
            throw new IOException("Encountered exception during amqp connection: " + protonJExceptionParser.getError() + " with description " + protonJExceptionParser.getErrorDescription());
        }
    }

    /**
     * Create Proton SslDomain object from Address using the given Ssl mode
     * @param mode Proton enum value of requested Ssl mode
     * @return The created Ssl domain
     */
    private SslDomain makeDomain(SslDomain.Mode mode)
    {
        SslDomain domain = Proton.sslDomain();

        try
        {
            // Need the base trusted certs for IotHub in our ssl context. IotHubSSLContext handles that
            domain.setSslContext(new IotHubSSLContext().getSSLContext());
        }
        catch (Exception e)
        {
            this.savedException = e;
        }

        domain.init(mode);

        return domain;
    }
}
