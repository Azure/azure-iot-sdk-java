/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.transport.amqp;

import com.microsoft.azure.sdk.iot.deps.util.CustomLogger;
import com.microsoft.azure.sdk.iot.deps.util.ObjectLock;
import com.microsoft.azure.sdk.iot.deps.ws.impl.WebSocketImpl;
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

public class AmqpsConnection extends BaseHandler
{
    private static final int MAX_WAIT_TO_OPEN_CLOSE_CONNECTION = 1*60*1000; // 1 minute timeout
    private static final int MAX_WAIT_TO_TERMINATE_EXECUTOR = 30;

    private static final String WEB_SOCKET_PATH = "/$iothub/websocket";
    private static final String WEB_SOCKET_SUB_PROTOCOL = "AMQPWSB10";
    private static final int AMQP_PORT = 5671;
    private static final int AMQP_WEB_SOCKET_PORT = 443;
    private static final int THREAD_POOL_MAX_NUMBER = 1;

    private int linkCredit;

    private long nextTag;

    private Boolean useWebSockets;
    private Boolean isOpen;

    private String hostName;
    private String fullHostAddress;

    private Connection connection;
    private Session session;
    private ExecutorService executorService;

    private AmqpDeviceOperations amqpDeviceOperations;

    private Reactor reactor;

    private SaslListenerImpl saslListener;

    private AmqpListener msgListener;

    private CountDownLatch openLatch;
    private final ObjectLock closeLock;

    private SSLContext sslContext;

    private CustomLogger logger = new CustomLogger();

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
        this.isOpen  = false;
        this.fullHostAddress = String.format("%s:%d", hostName, this.useWebSockets ? AMQP_WEB_SOCKET_PORT : AMQP_PORT );
        this.hostName = hostName;

        add(new Handshaker());
        add(new FlowController());

        try
        {
            ReactorOptions options = new ReactorOptions();
            options.setEnableSaslByDefault(false);
            reactor = Proton.reactor(options, this);
        }
        catch (IOException e)
        {
            logger.LogError(e);
            throw new IOException("Could not create Proton reactor", e);
        }
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
     * @throws Exception if any exception occurred during sasl negotiation
     * @return status of the connection
     */
    public boolean isConnected() throws Exception
    {
        if (this.saslListener != null && this.saslListener.getSavedException() != null)
        {
            throw this.saslListener.getSavedException();
        }

        return this.isOpen;
    }

    /**
     * Opens the connection.
     * @throws IOException If connection could not be opened.
     */
    public void open() throws IOException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());
        if(!this.isOpen)
        {
            try
            {
                openAmqpAsync();
            }
            catch(Exception e)
            {
                logger.LogError(e);
                this.close();
                throw new IOException("Error opening Amqp connection: ", e);
            }

            try
            {
                openLatch.await(MAX_WAIT_TO_OPEN_CLOSE_CONNECTION, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                logger.LogError(e);
                this.close();
                throw new IOException("Waited too long for the connection to open.");
            }
        }

        if (!this.isOpen)
        {
            throw new IOException("Failed to open the connection");
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Spawns another thread that attempts to open the AMQP connection. Use {@link #isConnected()} to check when
     * this operation has succeeded. Do not attempt to send messages before this connection has been opened
     */
    public void openAmqpAsync()
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        this.openLatch = new CountDownLatch(1);

        if (executorService == null)
        {
            executorService = Executors.newFixedThreadPool(THREAD_POOL_MAX_NUMBER);
        }

        AmqpReactor amqpReactor = new AmqpReactor(this.reactor);
        ReactorRunner reactorRunner = new ReactorRunner(amqpReactor, this.logger);
        executorService.submit(reactorRunner);

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Closes the connection
     * @throws IOException If connection could not be closed.
     */
    public void close() throws IOException
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        if (this.isOpen)
        {
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
                logger.LogError(e);
                throw new IOException("Waited too long for the connection to open.");
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
                            logger.LogInfo("Pool did not terminate");
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
        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for reactor init event.
     * @param event Proton Event object
     */
    @Override
    public void onReactorInit(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());
        event.getReactor().connectionToHost(this.hostName, this.useWebSockets ? AMQP_WEB_SOCKET_PORT : AMQP_PORT, this);
        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    @Override
    public void onReactorFinal(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());
        this.reactor = null;
        synchronized (closeLock)
        {
            closeLock.notifyLock();
        }
        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for the connection init event
     * @param event The Proton Event object.
     */
    @Override
    public void onConnectionInit(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());
        this.connection = event.getConnection();
        this.connection.setHostname(this.fullHostAddress);

        this.session = this.connection.session();

        this.connection.open();
        this.session.open();

        try
        {
            this.amqpDeviceOperations.openLinks(this.session);
        }
        catch (Exception e)
        {
            logger.LogDebug("openLinks has thrown exception: %s", e.getMessage());
        }
        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Create Proton SslDomain object from Address using the given Ssl mode
     * @return the created Ssl domain
     */
    private SslDomain makeDomain() throws IOException
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
        logger.LogDebug("Entered in method %s", logger.getMethodName());
        Transport transport = event.getConnection().getTransport();
        if (transport != null)
        {
            if (this.saslListener != null)
            {
                //Calling sasl here adds a transport layer for handling sasl negotiation
                transport.sasl().setListener(this.saslListener);
            }

            if (this.useWebSockets)
            {
                WebSocketImpl webSocket = new WebSocketImpl();
                webSocket.configure(this.hostName, WEB_SOCKET_PATH, 0, WEB_SOCKET_SUB_PROTOCOL, null, null);
                ((TransportInternal)transport).addTransportLayer(webSocket);
            }

            try
            {
                SslDomain domain = makeDomain();
                transport.ssl(domain);
            }
            catch (IOException e)
            {
                logger.LogDebug("onConnectionBound has thrown exception while creating ssl context: %s", e.getMessage());
            }
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    @Override
    public void onConnectionUnbound(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());
        this.isOpen = false;
        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for the link init event. Sets the proper target address on the link.
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkInit(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());
        try
        {
            Link link = event.getLink();
            amqpDeviceOperations.initLink(link);
        }
        catch (Exception e)
        {
            logger.LogDebug("Exception in onLinkInit: %s", e.getMessage());
        }
        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for the link remote open event. This signifies that the
     * {@link org.apache.qpid.proton.reactor.Reactor} is ready, so we set the connection to OPEN.
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkRemoteOpen(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());
        String linkName = event.getLink().getName();

        if (amqpDeviceOperations.isReceiverLinkTag(linkName))
        {
            this.isOpen = true;

            if (msgListener != null)
            {
                msgListener.connectionEstablished();

                openLatch.countDown();
            }
        }
        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Send message to the Amqp Endpoint
     * @param message Message to be sent
     * @return true if message was sent successfully, false other wise.
     * @throws IOException If message could not be sent.
     */
    public boolean sendAmqpMessage(AmqpMessage message) throws Exception
    {
        if (this.saslListener != null && this.saslListener.getSavedException() != null)
        {
            throw this.saslListener.getSavedException();
        }

        boolean result;
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        // credit, the function shall return -1.]
        if (!this.isOpen)
        {
            result = false;
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
                result = true;
            }
            else
            {
                result = false;
            }
        }
        logger.LogDebug("Exited from method %s", logger.getMethodName());
        return result;
    }

    /**
     * Event handler for the delivery event. This method handles both sending and receiving a message.
     * @param event The Proton Event object.
     */
    @Override
    public void onDelivery(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());

        AmqpMessage message = amqpDeviceOperations.receiverMessageFromLink(event.getLink().getName());
        if (message == null)
        {
            //Sender specific section for dispositions it receives
            if (event.getType() == Event.Type.DELIVERY)
            {
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_038: [If this link is the Sender link and the event type is DELIVERY, the event handler shall get the Delivery (Proton) object from the event.]
                Delivery d = event.getDelivery();
                DeliveryState remoteState = d.getRemoteState();

                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_039: [The event handler shall note the remote delivery state and use it and the Delivery (Proton) hash code to inform the AmqpsIotHubConnection of the message receipt.]
                boolean state = remoteState.equals(Accepted.getInstance());
                //let any listener know that the message was received by the server
                // release the delivery object which created in sendMessage().
                d.free();
            }
        }
        else
        {
            msgListener.messageReceived(message);
        }

        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for the link flow event. Handles sending a single message.
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkFlow(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());
        this.linkCredit = event.getLink().getCredit();
        logger.LogDebug("The link credit value is %s, method name is %s", this.linkCredit, logger.getMethodName());
        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for the link remote close event. This triggers reconnection attempts until successful.
     * Both sender and receiver links closing trigger this event, so we only handle one of them,
     * since the other is redundant.
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkRemoteClose(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());
        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    /**
     * Event handler for the transport error event. This triggers reconnection attempts until successful.
     * @param event The Proton Event object.
     */
    @Override
    public void onTransportError(Event event)
    {
        logger.LogDebug("Entered in method %s", logger.getMethodName());
        this.isOpen = false;
        logger.LogDebug("Exited from method %s", logger.getMethodName());
    }

    @Override
    public void onTransportHeadClosed(Event event)
    {
        this.openLatch.countDown();
    }

    /**
     * Class which runs the reactor.
     */
    private class ReactorRunner implements Callable
    {
        private final static String THREAD_NAME = "azure-iot-sdk-ReactorRunner";
        private final AmqpReactor amqpReactor;
        private final CustomLogger logger;

        ReactorRunner(AmqpReactor reactor, CustomLogger logger)
        {
            this.amqpReactor = reactor;
            this.logger = logger;
        }

        @Override
        public Object call()
        {
            Thread.currentThread().setName(THREAD_NAME);

            try
            {
                amqpReactor.run();
            }
            catch (HandlerException e)
            {
                logger.LogError(e);
                throw e;
            }

            return null;
        }
    }
}
