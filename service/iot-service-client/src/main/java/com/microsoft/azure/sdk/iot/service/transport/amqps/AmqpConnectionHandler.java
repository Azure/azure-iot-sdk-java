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
    protected String sasToken;

    protected final int expectedLinkCount;
    private CountDownLatch openLinksLatch;
    private CountDownLatch closeReactorLatch;

    private static final int OPEN_TIMEOUT_MILLISECONDS = 60 * 1000;
    private static final int CLOSE_REACTOR_TIMEOUT = 10 * 1000;

    protected int nextSendTag = 0;

    private final IotHubServiceClientProtocol iotHubServiceClientProtocol;
    private final String webSocketHostName;

    protected Exception savedException;

    protected Connection connection;

    //Lock unused to ensure that open/close operate atomically
    protected Object stateChangeLock = new Object();

    /**
     * Callback that is executed when the service sends a message to this client
     * @param message the message that was sent from the service
     * @return the acknowledgement type to send back to the service
     */
    public abstract DeliveryOutcome onMessageArrived(Message message);

    /**
     * Callback that is executed when a message that was sent to the service has been acknowledged
     * @param deliveryState the type of acknowledgement the service gave for the message
     */
    public abstract void onMessageAcknowledged(DeliveryState deliveryState);

    /**
     * Open all links
     * @param session the session to open the links on
     * @param properties the properties to assign to the links
     */
    public abstract void openLinks(Session session, Map<Symbol, Object> properties);

    /**
     * Close all links
     */
    public abstract void closeLinks();

    /**
     * @return The postfix to append onto the thread's name
     */
    public abstract String getThreadNamePostfix();

    /**
     * Constructor to set up connection parameters and initialize
     * handshaker and flow controller for transport
     * @param hostName The address string of the service (example: AAA.BBB.CCC)
     * @param userName The username string to use SASL authentication (example: user@sas.service)
     * @param iotHubServiceClientProtocol the protocol to use
     * @param tag the link tag
     * @param endpoint the link endpoint
     * @param expectedLinkCount the number of links expected to be opened
     */
    protected AmqpConnectionHandler(String hostName, String userName, IotHubServiceClientProtocol iotHubServiceClientProtocol, String tag, String endpoint, int expectedLinkCount)
    {
        if (Tools.isNullOrEmpty(hostName)
                || Tools.isNullOrEmpty(userName)
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

        this.tag = tag;
        this.endpoint = endpoint;

        add(new Handshaker());
        add(new FlowController());

        this.expectedLinkCount = expectedLinkCount;
    }

    public void open(String sasToken) throws IOException, InterruptedException
    {
        synchronized (stateChangeLock)
        {
            if (this.reactor != null)
            {
                log.debug("Amqp connection is already opened, returning from open call without doing anything");
                return;
            }

            this.sasToken = sasToken;

            log.trace("Amqp connection expecting {} link(s) to open", expectedLinkCount);
            this.openLinksLatch = new CountDownLatch(expectedLinkCount);
            this.closeReactorLatch = new CountDownLatch(1); //Counted down when the reactor finalizes

            this.startReactorThread();

            log.trace("Waiting for links to open");
            boolean openTimedOut = !this.openLinksLatch.await(OPEN_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);

            if (openTimedOut)
            {
                throw new IOException("Timed out waiting for amqp links to open");
            }

            //Check if any exceptions were caught during open, throw them here instead of in a reactor thread
            validateConnectionWasOpenedSuccessfully();
        }
    }

    private void startReactorThread() throws IOException
    {
        this.reactor = Proton.reactor(this);
        new Thread(new ReactorRunner(this.reactor, getThreadNamePostfix())).start();
    }

    public void close()
    {
        synchronized (stateChangeLock)
        {
            if (this.reactor == null)
            {
                log.debug("Amqp connection is already closed, returning from close call without doing anything");
                return;
            }

            //Reactor should respond with onLinkLocalClose call, where we close the session. Cannot close the session here
            // because it can only be done when all of its links have closed
            this.closeLinks();

            try
            {
                //when the callback for onReactorFinal comes, only then is it safe to set the reactor to null
                log.trace("Waiting for amqp connection to close");
                this.closeReactorLatch.await(CLOSE_REACTOR_TIMEOUT, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                log.warn("InterruptedException was thrown during close, AMQP connection resources may not have been closed properly");
            }
        }
    }

    @Override
    public void onReactorInit(Event event)
    {
        log.trace("Reactor initialized");
        event.getReactor().connection(this);
    }

    @Override
    public void onReactorFinal(Event event)
    {
        log.trace("Reactor finalized, releasing reactor closed latch");
        if (closeReactorLatch != null)
        {
            closeReactorLatch.countDown();
        }

        this.reactor = null;
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
            log.trace("Received delivery on receiver link");
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

                log.trace("onMessageArrived for message with correlationId {}", msg.getCorrelationId());
                DeliveryOutcome outcome = onMessageArrived(msg);

                switch (outcome)
                {
                    case Complete:
                        log.trace("Amqp message with correlationId {} was completed, acknowledging it with Accepted", msg.getCorrelationId());
                        delivery.disposition(Accepted.getInstance());
                        delivery.settle();
                        break;

                    case Reject:
                        log.trace("Amqp message with correlationId {} was rejected, acknowledging it with Rejected", msg.getCorrelationId());
                        delivery.disposition(new Rejected());
                        delivery.settle();
                        break;

                    case Abandon:
                        log.trace("Amqp message with correlationId {} was abandoned, acknowledging it with Released", msg.getCorrelationId());
                        delivery.disposition(Released.getInstance());
                        delivery.settle();
                        break;

                    default:
                        log.error("Unknown DeliveryOutcome provided, message will not be acknowledged");
                }
            }
        }
        else //Sender link received an ack for sent message
        {
            log.trace("Received acknowledgement for a message this process sent");
            Delivery d = event.getDelivery();

            DeliveryState remoteState = d.getRemoteState();

            onMessageAcknowledged(remoteState);

            d.settle();
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
                log.debug("Adding websocket layer to transport");
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
        this.connection = event.getConnection();
        this.connection.setHostname(hostName);
        Session ssn = this.connection.session();

        Map<Symbol, Object> properties = new HashMap<>();
        properties.put(Symbol.getSymbol(TransportUtils.versionIdentifierKey), TransportUtils.USER_AGENT_STRING);

        log.trace("Opening connection");
        this.connection.open();

        log.trace("Opening session");
        ssn.open();
        openLinks(ssn, properties);
    }

    @Override
    public void onLinkInit(Event event)
    {
        Link link = event.getLink();
        if (event.getLink().getName().equals(tag))
        {
            if (link instanceof Sender)
            {
                Target t = new Target();
                t.setAddress(endpoint);
                link.setTarget(t);
            }
            else if (link instanceof Receiver)
            {
                Source source = new Source();
                source.setAddress(endpoint);
                link.setSource(source);
            }
        }
    }

    @Override
    public void onLinkRemoteOpen(Event event)
    {
        log.debug("Link with name {} opened remotely", event.getLink().getName());
        openLinksLatch.countDown();
    }

    @Override
    public void onConnectionRemoteOpen(Event event)
    {
        log.debug("Connection opened remotely");
    }

    @Override
    public void onSessionRemoteOpen(Event event)
    {
        log.debug("Session opened remotely");
    }

    @Override
    public void onConnectionRemoteClose(Event event)
    {
        super.onConnectionRemoteClose(event);
        event.getTransport().close_tail();
        event.getConnection().close();
        releaseOpenLatch();
    }

    @Override
    public void onLinkRemoteClose(Event event)
    {
        super.onLinkRemoteClose(event);
        event.getTransport().close_tail();
        event.getLink().close();
        releaseOpenLatch();
    }

    @Override
    public void onSessionRemoteClose(Event event)
    {
        super.onSessionRemoteClose(event);
        event.getTransport().close_tail();
        event.getSession().close();
        releaseOpenLatch();
    }

    @Override
    public void onTransportError(Event event)
    {
        super.onTransportError(event);
        event.getTransport().close_tail();

        // Transport errors don't mean that links were closed remotely, but there is no way to know how to recover the
        // state of the transport now, so we may as well close the whole connection, starting with the links
        this.closeLinks();

        releaseOpenLatch();
    }

    @Override
    public void onLinkLocalClose(Event event)
    {
        log.debug("Closing amqp session");

        //Reactor should respond to this session close with onSessionLocalClose call, where we close the connection.
        // Cannot close the connection here because it can only be done when all of its sessions have closed
        this.connection.session().close();
    }

    @Override
    public void onSessionLocalClose(Event event)
    {
        log.debug("Closing amqp connection");

        //Reactor should respond to this connection close with onConnectionLocalClose call, where we close the reactor.
        // Cannot close the reactor here because it can only be done when all of its connections have closed
        this.connection.close();
    }

    @Override
    public void onConnectionLocalClose(Event event)
    {
        log.trace("Stopping reactor");

        // At this point, all links, sessions, and connections should be closed locally, so it is safe to stop the reactor
        reactor.stop();
    }

    /**
     * If an exception was encountered while opening the AMQP connection, this function shall throw that saved exception
     * @throws IOException if an exception was encountered while opening the AMQP connection. The encountered
     * exception will be the inner exception
     */
    protected void validateConnectionWasOpenedSuccessfully() throws IOException
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
            log.error("Failed to build SSLContext {}", e);
            this.savedException = e;
        }

        domain.init(mode);

        return domain;
    }

    private void releaseOpenLatch()
    {
        log.trace("Releasing open latch");
        for (int i = 0; i < this.openLinksLatch.getCount(); i++)
        {
            this.openLinksLatch.countDown();
        }
    }
}
