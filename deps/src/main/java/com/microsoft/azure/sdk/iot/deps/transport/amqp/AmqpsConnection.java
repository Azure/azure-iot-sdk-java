/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.transport.amqp;

import com.microsoft.azure.sdk.iot.deps.util.ObjectLock;
import com.microsoft.azure.sdk.iot.deps.ws.impl.WebSocketImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.reactor.FlowController;
import org.apache.qpid.proton.reactor.Handshaker;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.ReactorOptions;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.concurrent.*;

@Slf4j
public class AmqpsConnection extends ErrorLoggingBaseHandlerWithCleanup
{
    private static final int MAX_WAIT_TO_OPEN_CLOSE_CONNECTION = 60 * 1000; // 1 minute timeout
    private static final int MAX_WAIT_TO_TERMINATE_EXECUTOR = 30;

    private static final String WEB_SOCKET_PATH = "/$iothub/websocket";
    private static final String WEB_SOCKET_SUB_PROTOCOL = "AMQPWSB10";
    private static final int AMQP_PORT = 5671;
    private static final int AMQP_WEB_SOCKET_PORT = 443;
    private static final int THREAD_POOL_MAX_NUMBER = 1;

    private int linkCredit;

    private long nextTag;

    private final Boolean useWebSockets;
    private Boolean isOpen;

    private final String hostName;
    private final String fullHostAddress;

    private Connection connection;
    private Session session;
    private ExecutorService executorService;

    private final AmqpDeviceOperations amqpDeviceOperations;

    private Reactor reactor;

    private SaslListenerImpl saslListener;

    private AmqpListener msgListener;

    private CountDownLatch openLatch;
    private final ObjectLock closeLock;

    private final SSLContext sslContext;

    /**
     * Constructor for the Amqp library
     * @param hostName Name of the AMQP Endpoint
     * @param amqpDeviceOperations Object holding details of the links used in this connection
     * @param sslContext SSL Context to be set over TLS.
     * @param saslHandler The sasl frame handler. This may be null if no sasl frames will be exchanged (When using x509
     *                    authentication for example)
     * @param useWebSockets WebSockets to be used or disabled.
     * @throws IOException This exception is thrown if for any reason constructor cannot succeed.
     */
    public AmqpsConnection(String hostName, AmqpDeviceOperations amqpDeviceOperations, SSLContext sslContext, SaslHandler saslHandler, boolean useWebSockets) throws IOException
    {
        if (hostName == null || hostName.isEmpty())
        {
            throw new IllegalArgumentException("The hostname cannot be null or empty.");
        }

        this.linkCredit = -1;
        this.nextTag = 0;

        this.amqpDeviceOperations = amqpDeviceOperations;
        this.useWebSockets = useWebSockets;

        if (saslHandler != null)
        {
            this.saslListener = new SaslListenerImpl(saslHandler);
        }

        this.openLatch = new CountDownLatch(1);
        this.closeLock  = new ObjectLock();

        this.sslContext = sslContext;
        this.isOpen = false;
        this.fullHostAddress = String.format("%s:%d", hostName, this.useWebSockets ? AMQP_WEB_SOCKET_PORT : AMQP_PORT );
        this.hostName = hostName;

        add(new Handshaker());
        add(new FlowController());

        ReactorOptions options = new ReactorOptions();
        options.setEnableSaslByDefault(false);
        reactor = Proton.reactor(options, this);
    }

    /**
     * Sets the listener for this connection.
     * @param listener Listener to be used for this connection.
     */
    public void setListener(AmqpListener listener)
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("The listener cannot be null.");
        }
        this.msgListener = listener;
    }

    /**
     * Returns the status of the connection
     * @return status of the connection
     */
    public boolean isConnected() throws Exception
    {
        if (this.saslListener != null && this.saslListener.getSavedException() != null)
        {
            throw this.saslListener.getSavedException();
        }

        if (this.protonJExceptionParser != null && this.protonJExceptionParser.getError() != null)
        {
            throw new IOException("Encountered exception during amqp connection: " + protonJExceptionParser.getError() + " with description " + protonJExceptionParser.getErrorDescription());
        }

        return this.isOpen;
    }

    /**
     * Opens the connection.
     * @throws IOException If connection could not be opened.
     */
    public void open() throws IOException
    {
        if(!this.isOpen)
        {
            try
            {
                log.debug("Opening amqp connection asynchronously");
                openAmqpAsync();
            }
            catch(Exception e)
            {
                String errorMessage = "Error opening Amqp connection: ";
                log.error(errorMessage, e);
                this.close();
                throw new IOException(errorMessage, e);
            }

            try
            {
                //noinspection ResultOfMethodCallIgnored
                openLatch.await(MAX_WAIT_TO_OPEN_CLOSE_CONNECTION, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                String errorMessage = "Amqp connection was interrupted while opening.";
                log.error(errorMessage, e);
                this.close();
                throw new IOException(errorMessage, e);
            }
        }
        else
        {
            log.trace("Open called while amqp connection was already open");
        }

        if (!this.isOpen)
        {
            throw new IOException("Timed out  to open the amqp connection");
        }
    }

    /**
     * Spawns another thread that attempts to open the AMQP connection. Use {@link #isConnected()} to check when
     * this operation has succeeded. Do not attempt to send messages before this connection has been opened
     */
    public void openAmqpAsync()
    {
        this.openLatch = new CountDownLatch(1);

        if (executorService == null)
        {
            executorService = Executors.newFixedThreadPool(THREAD_POOL_MAX_NUMBER);
        }

        log.debug("Starting amqp reactor thread...");
        AmqpReactor amqpReactor = new AmqpReactor(this.reactor);
        ReactorRunner reactorRunner = new ReactorRunner(amqpReactor);
        executorService.submit(reactorRunner);
    }

    /**
     * Closes the connection
     * @throws IOException If connection could not be closed.
     */
    public void close() throws IOException
    {
        if (this.isOpen)
        {
            log.debug("Closing amqp connection");
            this.amqpDeviceOperations.closeLinks();

            // Close Proton
            if (this.session != null)
            {
                this.session.close();
            }
            if (this.connection != null)
            {
                this.connection.close();
            }
            if (this.reactor != null)
            {
                this.reactor.stop();
            }

            try
            {
                synchronized (closeLock)
                {
                    closeLock.waitLock(MAX_WAIT_TO_OPEN_CLOSE_CONNECTION);
                }
            }
            catch (InterruptedException e)
            {
                throw new IOException("Waited too long for the connection to close.", e);
            }

            if (this.executorService != null)
            {
                this.executorService.shutdown();
                try
                {
                    // Wait a while for existing tasks to terminate
                    if (!this.executorService.awaitTermination(MAX_WAIT_TO_TERMINATE_EXECUTOR, TimeUnit.SECONDS))
                    {
                        this.executorService.shutdownNow(); // Cancel currently executing tasks
                        // Wait a while for tasks to respond to being cancelled
                        if (!this.executorService.awaitTermination(MAX_WAIT_TO_TERMINATE_EXECUTOR, TimeUnit.SECONDS))
                        {
                            log.info("Pool did not terminate");
                        }
                    }
                }
                catch (InterruptedException ie)
                {
                    this.executorService.shutdownNow();
                }
            }
            this.isOpen = false;
        }
    }

    /**
     * Event handler for reactor init event.
     * @param event Proton Event object
     */
    @Override
    public void onReactorInit(Event event)
    {
        event.getReactor().connectionToHost(this.hostName, this.useWebSockets ? AMQP_WEB_SOCKET_PORT : AMQP_PORT, this);
    }

    @Override
    public void onReactorFinal(Event event)
    {
        super.onReactorFinal(event);
        this.reactor = null;
        synchronized (closeLock)
        {
            closeLock.notifyLock();
        }
    }

    /**
     * Event handler for the connection init event
     * @param event The Proton Event object.
     */
    @Override
    public void onConnectionInit(Event event)
    {
        this.connection = event.getConnection();
        this.connection.setHostname(this.fullHostAddress);

        this.session = this.connection.session();

        this.connection.open();
        this.session.open();

        this.amqpDeviceOperations.openLinks(this.session);
    }

    /**
     * Create Proton SslDomain object from Address using the given Ssl mode
     * @return the created Ssl domain
     */
    private SslDomain makeDomain()
    {
        SslDomain domain = Proton.sslDomain();
        domain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
        domain.init(SslDomain.Mode.CLIENT);
        domain.setSslContext(this.sslContext);

        return domain;
    }

    @Override
    public void onConnectionBound(Event event)
    {
        Transport transport = event.getConnection().getTransport();
        if (transport != null)
        {
            if (this.saslListener != null)
            {
                log.debug("Setting up sasl negotiator");
                //Calling sasl here adds a transport layer for handling sasl negotiation
                transport.sasl().setListener(this.saslListener);
            }

            if (this.useWebSockets)
            {
                log.debug("Adding websocket layer");
                WebSocketImpl webSocket = new WebSocketImpl();
                webSocket.configure(this.hostName, WEB_SOCKET_PATH, 0, WEB_SOCKET_SUB_PROTOCOL, null, null);
                ((TransportInternal)transport).addTransportLayer(webSocket);
            }

            SslDomain domain = makeDomain();
            transport.ssl(domain);
        }
    }

    @Override
    public void onConnectionUnbound(Event event)
    {
        log.trace("Amqp connection unbound");
        this.isOpen = false;
    }

    /**
     * Event handler for the link init event. Sets the proper target address on the link.
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkInit(Event event)
    {
        Link link = event.getLink();
        amqpDeviceOperations.initLink(link);
    }

    /**
     * Event handler for the link remote open event. This signifies that the
     * {@link org.apache.qpid.proton.reactor.Reactor} is ready, so we set the connection to OPEN.
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkRemoteOpen(Event event)
    {
        super.onLinkRemoteOpen(event);
        String linkName = event.getLink().getName();

        if (amqpDeviceOperations.isReceiverLinkTag(linkName))
        {
            this.isOpen = true;

            if (msgListener != null)
            {
                openLatch.countDown();
            }
        }
    }

    /**
     * Send message to the Amqp Endpoint
     * @param message Message to be sent
     * @return true if message was sent successfully, false other wise.
     * @throws IOException If message could not be sent.
     */
    public boolean sendAmqpMessage(AmqpMessage message) throws Exception
    {
        // credit, the function shall return -1.]
        if (!this.isConnected())
        {
            return false;
        }
        else
        {
            byte[] msgData = new byte[1024];
            int length = 0;
            boolean encodingComplete = false;

            do
            {
                try
                {
                    length = message.encode(msgData, 0);
                    encodingComplete = true;
                }
                catch (BufferOverflowException e)
                {
                    msgData = new byte[msgData.length * 2];
                }
            } while(!encodingComplete);

            if (length > 0)
            {
                byte[] tag = String.valueOf(this.nextTag).getBytes();

                //want to avoid negative delivery tags since -1 is the designated failure value
                if (this.nextTag == Integer.MAX_VALUE || this.nextTag < 0)
                {
                    this.nextTag = 0;
                }
                else
                {
                    this.nextTag++;
                }

                amqpDeviceOperations.sendMessage(tag, msgData, length, 0);
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    /**
     * Event handler for the delivery event. This method handles both sending and receiving a message.
     * @param event The Proton Event object.
     */
    @Override
    public void onDelivery(Event event)
    {
        Link link = event.getLink();

        if (link instanceof Sender)
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_038: [If this link is the Sender link and the event type is DELIVERY, the event handler shall get the Delivery (Proton) object from the event.]
            Delivery d = event.getDelivery();
            DeliveryState remoteState = d.getRemoteState();

            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_039: [The event handler shall note the remote delivery state and use it and the Delivery (Proton) hash code to inform the AmqpsIotHubConnection of the message receipt.]
            boolean messageAcknowledgedAsSuccess = remoteState.equals(Accepted.getInstance());

            if (!messageAcknowledgedAsSuccess)
            {
                String error = "Amqp message was not accepted by service, remote state was " + remoteState.getType();
                this.msgListener.messageSendFailed(error);
            }

            //let any listener know that the message was received by the server
            // release the delivery object which created in sendMessage().
            d.free();
        }
        else if (link instanceof Receiver)
        {
            AmqpMessage message = amqpDeviceOperations.receiverMessageFromLink(event.getLink().getName());

            if (message != null)
            {
                log.debug("Amqp connection received message");
                msgListener.messageReceived(message);
            }
            else
            {
                log.warn("onDelivery executed on a receiver link but no message could be received");
            }
        }
        else
        {
            log.warn("onDelivery executed on a link that is neither a sender or a receiver");
        }
    }

    /**
     * Event handler for the link flow event. Handles sending a single message.
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkFlow(Event event)
    {
        this.linkCredit = event.getLink().getCredit();
        log.trace("Amqp link received {} link credit", this.linkCredit);
    }

    /**
     * Event handler for the transport error event. This triggers reconnection attempts until successful.
     * @param event The Proton Event object.
     */
    @Override
    public void onTransportError(Event event)
    {
        super.onTransportError(event);
        this.isOpen = false;
    }

    @Override
    public void onTransportHeadClosed(Event event)
    {
        this.openLatch.countDown();
        log.trace("Amqp transport head closed");
    }

    /**
     * Class which runs the reactor.
     */
    private static class ReactorRunner implements Callable<Object>
    {
        private final static String THREAD_NAME = "azure-iot-sdk-ReactorRunner";
        private final AmqpReactor amqpReactor;

        ReactorRunner(AmqpReactor reactor)
        {
            this.amqpReactor = reactor;
        }

        @Override
        public Object call()
        {
            Thread.currentThread().setName(THREAD_NAME);
            log.trace("Amqp reactor thread {} has started", THREAD_NAME);

            try
            {
                amqpReactor.run();
            }
            catch (HandlerException e)
            {
                log.error("Encountered an exception while running the AMQP reactor", e);
                throw e;
            }

            log.trace("Amqp reactor thread {} has finished", THREAD_NAME);

            return null;
        }
    }
}
