/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.ObjectLock;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.transport.State;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
//import com.microsoft.azure.sdk.iot
import com.microsoft.azure.sdk.iot.deps.ws.impl.WebSocketImpl;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.FlowController;
import org.apache.qpid.proton.reactor.Handshaker;
import org.apache.qpid.proton.reactor.Reactor;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.Reader;
import java.nio.BufferOverflowException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;



/**
 * An AMQPS IotHub connection between a device and an IoTHub. This class contains functionality for sending/receiving
 * a message, and logic to re-establish the connection with the IoTHub in case it gets lost.
 */
public final class AmqpsIotHubConnection extends BaseHandler
{
    private int maxWaitTimeForOpeningClosingConnection = 3*60*1000;
    private int maxWaitTimeForTerminateExecutor = 30;
    protected State state;

    private static final String sendTag = "sender";
    private static final String receiveTag = "receiver";

    private static final String sendEndpointFormat = "/devices/%s/messages/events";
    private final String sendEndpoint;
    private static final String receiveEndpointFormat = "/devices/%s/messages/devicebound";
    private final String receiveEndpoint;

    private int linkCredit = -1;
    /** The {@link Delivery} tag. */
    private long nextTag = 0;
    private static final String versionIdentifierKey = "com.microsoft:client-version";
    private static final String webSocketPath = "/$iothub/websocket";
    private static final String webSocketSubProtocol = "AMQPWSB10";
    private static final int amqpPort = 5671;
    private static final int amqpWebSocketPort = 443;
    private String sasToken;

    private Sender sender;
    private Receiver receiver;
    private Connection connection;
    private Session session;

    private String hostName;
    private String userName;

    private final Boolean useWebSockets;
    protected DeviceClientConfig config;

    private List<ServerListener> listeners = new ArrayList<>();
    private ExecutorService executorService;

    private ObjectLock openLock = new ObjectLock();
    private ObjectLock closeLock = new ObjectLock();

    private Reactor reactor;

    private Boolean reconnectCall = false;
    private int currentReconnectionAttempt = 1;
    protected final Logger logger = LoggerFactory.getLogger(AmqpsIotHubConnection.class);

    /**
     * Constructor to set up connection parameters using the {@link DeviceClientConfig}.
     *
     * @param config The {@link DeviceClientConfig} corresponding to the device associated with this {@link com.microsoft.azure.sdk.iot.device.DeviceClient}.
     * @param useWebSockets Whether the connection should use web sockets or not.
     */
    public AmqpsIotHubConnection(DeviceClientConfig config, Boolean useWebSockets) throws IOException
    {
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_001: [The constructor shall throw IllegalArgumentException if
        // any of the parameters of the configuration is null or empty.]
        if(config == null)
        {
            throw new IllegalArgumentException("The DeviceClientConfig cannot be null.");
        }
        if(config.getIotHubHostname() == null || config.getIotHubHostname().length() == 0)
        {
            throw new IllegalArgumentException("hostName cannot be null or empty.");
        }
        if (config.getDeviceId() == null || config.getDeviceId().length() == 0)
        {
            throw new IllegalArgumentException("deviceID cannot be null or empty.");
        }
        if(config.getIotHubName() == null || config.getIotHubName().length() == 0)
        {
            throw new IllegalArgumentException("hubName cannot be null or empty.");
        }
        if(config.getDeviceKey() == null || config.getDeviceKey().length() == 0)
        {
            if(config.getSharedAccessToken() == null || config.getSharedAccessToken().length() == 0)

                 throw new IllegalArgumentException("Both deviceKey and shared access signature cannot be null or empty.");
        }

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_002: [The constructor shall save the configuration into private member variables.]
        this.config = config;

        String deviceId = this.config.getDeviceId();
        String iotHubName = this.config.getIotHubName();

        this.userName = deviceId + "@sas." + iotHubName;

        this.useWebSockets = useWebSockets;
        if (useWebSockets)
        {
            this.hostName = String.format("%s:%d", this.config.getIotHubHostname(), amqpWebSocketPort);
        }
        else
        {
            this.hostName = String.format("%s:%d", this.config.getIotHubHostname(), amqpPort);
        }

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_003: [The constructor shall initialize the sender and receiver
        // endpoint private member variables using the send/receiveEndpointFormat constants and device id.]
        this.sendEndpoint = String.format(sendEndpointFormat, deviceId);
        this.receiveEndpoint = String.format(receiveEndpointFormat, deviceId);
         // Codes_SRS_AMQPSIOTHUBCONNECTION_15_004: [The constructor shall initialize a new Handshaker
        // (Proton) object to handle communication handshake.]
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_005: [The constructor shall initialize a new FlowController
        // (Proton) object to handle communication flow.]
        add(new Handshaker());
        add(new FlowController());

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_006: [The constructor shall set its state to CLOSED.]
        this.state = State.CLOSED;

        try
        {
            reactor = Proton.reactor(this);

        } catch (IOException e)
        {
            logger.error("", e);
            throw new IOException("Could not create Proton reactor");
        }
        logger.info("AmqpsIotHubConnection object is created successfully using port {} in {} method ", 
        		useWebSockets ? amqpWebSocketPort : amqpPort, Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    /**
     * Opens the {@link AmqpsIotHubConnection}.
     * <p>
     *     If the current connection is not open, this method
     *     will create a new {@link IotHubSasToken}. This method will
     *     start the {@link Reactor}, set the connection to open and make it ready for sending.
     * </p>
     *
     * @throws IOException If the reactor could not be initialized.
     */
    public void open() throws IOException
    {
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_007: [If the AMQPS connection is already open, the function shall do nothing.]
        if(this.state == State.CLOSED)
        {
            try
            {
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_009: [The function shall trigger the Reactor (Proton) to begin running.]
                openAsync();
            }
            catch(Exception e)
            {
                logger.error("", e);
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_011: [If any exception is thrown while attempting to trigger
                // the reactor, the function shall close the connection and throw an IOException.]
                this.close();
                throw new IOException("Error opening Amqp connection: ", e);
            }

            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_010: [The function shall wait for the reactor to be ready and for
            // enough link credit to become available.]
            try
            {
                synchronized (openLock)
                {
                    openLock.waitLock(maxWaitTimeForOpeningClosingConnection);
                }
            } catch (InterruptedException e)
            {
                logger.error("", e);
                throw new IOException("Waited too long for the connection to open.");
            }
        }
    }

    /**
     * Closes the {@link AmqpsIotHubConnection}.
     * <p>
     *     If the current connection is not closed, this function
     *     will set the current state to closed and invalidate all connection related variables.
     * </p>
     */
    public void close() throws IOException
    {

        closeAsync();

        try
        {
            synchronized (closeLock)
            {
                closeLock.waitLock(maxWaitTimeForOpeningClosingConnection);
            }
        } catch (InterruptedException e)
        {
            logger.error("", e);
            throw new IOException("Waited too long for the connection to close.");
        }

        if (this.executorService != null) {
            logger.info("Shutdown of executor service has started, method name is {} ", 
            		Thread.currentThread().getStackTrace()[2].getMethodName());
            this.executorService.shutdown();
            try {
                // Wait a while for existing tasks to terminate
                if (!this.executorService.awaitTermination(maxWaitTimeForTerminateExecutor, TimeUnit.SECONDS)) {
                    this.executorService.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!this.executorService.awaitTermination(maxWaitTimeForTerminateExecutor, TimeUnit.SECONDS)){
                        System.err.println("Pool did not terminate");
                    }
                }
            } catch (InterruptedException ie) {
                logger.error("", ie);
                // (Re-)Cancel if current thread also interrupted
                this.executorService.shutdownNow();
            }
            logger.info("Shutdown of executor service completed, method name is {} ", 
            		Thread.currentThread().getStackTrace()[2].getMethodName());
        }
    }

    private void openAsync() throws IOException
    {
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_008: [The function shall create a new sasToken valid for the duration
        // specified in config to be used for the communication with IoTHub.]
        this.sasToken = new IotHubSasToken(this.config, System.currentTimeMillis() / 1000L +
                this.config.getTokenValidSecs() + 1L).toString();
				
        logger.info("SAS Token is created successfully, method name is {} ", 
        		Thread.currentThread().getStackTrace()[2].getMethodName());

        if (this.reactor == null)
        {
            this.reactor = Proton.reactor(this);
        }

        if (executorService == null)
        {
            executorService = Executors.newFixedThreadPool(1);
        }

        IotHubReactor iotHubReactor = new IotHubReactor(reactor);
        ReactorRunner reactorRunner = new ReactorRunner(iotHubReactor);
        executorService.submit(reactorRunner);
        logger.info("Reactor is assigned to executor service, method name is {} ", 
        		Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    private void closeAsync()
    {
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_048 [If the AMQPS connection is already closed, the function shall do nothing.]
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_012: [The function shall set the status of the AMQPS connection to CLOSED.]
        this.state = State.CLOSED;

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_013: [The function shall close the AMQPS sender and receiver links,
        // the AMQPS session and the AMQPS connection.]
        if (this.sender != null)
            this.sender.close();
        if (this.receiver != null)
            this.receiver.close();
        if (this.session != null)
            this.session.close();
        if (this.connection != null)
            this.connection.close();

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_014: [The function shall stop the Proton reactor.]

        this.reactor.stop();
        logger.info("Proton reactor has been stopped, method name is {} ", Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    /**
     * Creates a binary message using the given content and messageId. Sends the created message using the sender link.
     * @param message The message to be sent.
     * @return An {@link Integer} representing the hash of the message, or -1 if the connection is closed.
     */
    public Integer sendMessage(Message message)
    {
        Integer deliveryHash;

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_015: [If the state of the connection is CLOSED or there is not enough
        // credit, the function shall return -1.]
        if (this.state == State.CLOSED || this.linkCredit <= 0)
        {
            deliveryHash = -1;
        }
        else
        {

            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_016: [The function shall encode the message and copy the contents to the byte buffer.]
            byte[] msgData = new byte[1024];
            int length;

            logger.info("Started encoding of message - entering in while loop, method name is {} ", 
            		Thread.currentThread().getStackTrace()[2].getMethodName());
            while (true)
            {
                try
                {
                    length = message.encode(msgData, 0, msgData.length);
                    logger.info("Completed encoding of message, length is {} - breaking the while loop to come out, method name is {} ", 
                    		length, Thread.currentThread().getStackTrace()[2].getMethodName());
                    break;
                }
                catch (BufferOverflowException e)
                {
                    logger.error("", e);
                    msgData = new byte[msgData.length * 2];
                }
            }
            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_017: [The function shall set the delivery tag for the sender.]
            byte[] tag = String.valueOf(this. nextTag++).getBytes();
	    Delivery dlv = sender.delivery(tag);
	    try
	    {
            logger.info("Attempting to send the message using the sender link, method name is {} ", 
            		Thread.currentThread().getStackTrace()[2].getMethodName());
            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_018: [The function shall attempt to send the message using the sender link.]
            sender.send(msgData, 0, length);

            logger.info("Advancing the sender link, method name is {} ", Thread.currentThread().getStackTrace()[2].getMethodName());
            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_019: [The function shall advance the sender link.]
            sender.advance();

            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_020: [The function shall set the delivery hash to the value returned by the sender link.]
            deliveryHash = dlv.hashCode();
            logger.info("Delivery hash returned by the sender link {}, method name is {} ", 
            		deliveryHash, Thread.currentThread().getStackTrace()[2].getMethodName());
        }
        catch (Exception e)
        {
            // If proton failed sending, release dlv object. Otherwise release it when received a disposition frame from proton.
            sender.advance();
            dlv.free();
            deliveryHash = -1;
	    }
	}

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_021: [The function shall return the delivery hash.]
        return deliveryHash;
    }

    /**
     * Sends the message result for the previously received message.
     *
     * @param message the message to be acknowledged.
     * @param result the message result (one of {@link IotHubMessageResult#COMPLETE},
     *               {@link IotHubMessageResult#ABANDON}, or {@link IotHubMessageResult#REJECT}).
     */
    public Boolean sendMessageResult(AmqpsMessage message, IotHubMessageResult result)
    {
        Boolean ackResult = false;
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_022: [If the AMQPS Connection is closed, the function shall return false.]
        if(this.state != State.CLOSED)
        {
            try
            {
                logger.info("Acknowledgement for received message is {}, method name is {} ", 
                		result.name(), Thread.currentThread().getStackTrace()[2].getMethodName());
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_023: [If the message result is COMPLETE, ABANDON, or REJECT,
                // the function shall acknowledge the last message with acknowledgement type COMPLETE, ABANDON, or REJECT respectively.]
                switch (result)
                {
                    case COMPLETE:
                        message.acknowledge(AmqpsMessage.ACK_TYPE.COMPLETE);
                        break;
                    case REJECT:
                        message.acknowledge(AmqpsMessage.ACK_TYPE.REJECT);
                        break;
                    case ABANDON:
                        message.acknowledge(AmqpsMessage.ACK_TYPE.ABANDON);
                        break;
                    default:
                        // should never happen.
                        logger.error("Invalid IoT Hub message result ({}), method name is {} ", 
                        		result.name(), Thread.currentThread().getStackTrace()[2].getMethodName());
                        throw new IllegalStateException("Invalid IoT Hub message result.");
                }

                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_024: [The function shall return true after the message was acknowledged.]
                ackResult = true;
            }
            catch (Exception e)
            {
                logger.error("", e);
                //do nothing, since ackResult is already false
            }
        }
        return ackResult;
    }

    /**
     * Event handler for the connection init event
     * @param event The Proton Event object.
     */
    @Override
    public void onConnectionInit(Event event)
    {
        logger.debug("Entered in method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_025: [The event handler shall get the Connection (Proton) object from the event handler and set the host name on the connection.]
        this.connection = event.getConnection();
        this.connection.setHostname(this.hostName);

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_026: [The event handler shall create a Session (Proton) object from the connection.]
        this.session = this.connection.session();

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_027: [The event handler shall create a Receiver and Sender (Proton) links and set the protocol tag on them to a predefined constant.]
        this.receiver = this.session.receiver(receiveTag);
        this.sender = this.session.sender(sendTag);

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_028: [The Receiver and Sender links shall have the properties set to client version identifier.]
        Map<Symbol, Object> properties = new HashMap<>();
        properties.put(Symbol.getSymbol(versionIdentifierKey), TransportUtils.javaDeviceClientIdentifier + TransportUtils.clientVersion);
        this.receiver.setProperties(properties);
        this.sender.setProperties(properties);

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_029: [The event handler shall open the connection, session, sender and receiver objects.]
        this.connection.open();
        this.session.open();
        receiver.open();
        sender.open();
        logger.debug("Exited from method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    /**
     * Event handler for the connection bound event. Sets Sasl authentication and proper authentication mode.
     * @param event The Proton Event object.
     */
    @Override
    public void onConnectionBound(Event event)
    {
        logger.debug("Entered in method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_030: [The event handler shall get the Transport (Proton) object from the event.]
        Transport transport = event.getConnection().getTransport();
        if(transport != null){

            if (this.useWebSockets)
            {
                WebSocketImpl webSocket = new WebSocketImpl();
                webSocket.configure(this.hostName, webSocketPath, 0, webSocketSubProtocol, null, null);
                ((TransportInternal)transport).addTransportLayer(webSocket);
            }

            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_031: [The event handler shall set the SASL_PLAIN authentication on the transport using the given user name and sas token.]
            Sasl sasl = transport.sasl();
            sasl.plain(this.userName, this.sasToken);

            SslDomain domain = makeDomain(SslDomain.Mode.CLIENT);
            transport.ssl(domain);
        }
        synchronized (openLock)
        {
            openLock.notifyLock();
        }
        logger.debug("Exited from method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onConnectionUnbound(Event event)
    {
        logger.debug("Entered in method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
        this.state = State.CLOSED;
        logger.debug("Exited from method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    /**
     * Event handler for reactor init event.
     * @param event Proton Event object
     */
    @Override
    public void onReactorInit(Event event)
    {
        logger.debug("Entered in method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_033: [The event handler shall set the current handler to handle the connection events.]
        if(this.useWebSockets)
        {
            event.getReactor().connectionToHost(this.config.getIotHubHostname(), amqpWebSocketPort, this);

        }
        else
        {
            event.getReactor().connectionToHost(this.config.getIotHubHostname(), amqpPort, this);
        }
        logger.debug("Exited from method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    @Override
    public void onReactorFinal(Event event)
    {
        logger.debug("Entered in method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
        synchronized (closeLock)
        {
            closeLock.notifyLock();
        }

        this.reactor = null;

        if (reconnectCall)
        {
            reconnectCall = false;
            try
            {
                openAsync();
            } catch (IOException e)
            {
                logger.error("", e);
                e.printStackTrace();
            }
        }
        logger.debug("Exited from method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    /**
     * Event handler for the delivery event. This method handles both sending and receiving a message.
     * @param event The Proton Event object.
     */
    @Override
    public void onDelivery(Event event)
    {
        logger.debug("Entered in method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
        if(event.getLink().getName().equals(receiveTag))
        {
            logger.info("Reading the receiver link, method name is {} ", Thread.currentThread().getStackTrace()[2].getMethodName());
            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_034: [If this link is the Receiver link, the event handler shall get the Receiver and Delivery (Proton) objects from the event.]
            Receiver receiveLink = (Receiver) event.getLink();
            Delivery delivery = receiveLink.current();
            if (delivery.isReadable() && !delivery.isPartial()) {
                logger.info("Reading the received buffer, method name is {} ", Thread.currentThread().getStackTrace()[2].getMethodName());
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_035: [The event handler shall read the received buffer.]
                int size = delivery.pending();
                byte[] buffer = new byte[size];
                int read = receiveLink.recv(buffer, 0, buffer.length);
                receiveLink.advance();
                logger.info("Reading the received buffer completed, method name is {} ", 
                		Thread.currentThread().getStackTrace()[2].getMethodName());
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_036: [The event handler shall create an AmqpsMessage object from the decoded buffer.]
                AmqpsMessage msg = new AmqpsMessage();

                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_037: [The event handler shall set the AmqpsMessage Deliver (Proton) object.]
                msg.setDelivery(delivery);
                logger.info("Decoding the received message , method name is {} ", 
                		Thread.currentThread().getStackTrace()[2].getMethodName());
                msg.decode(buffer, 0, read);
                logger.info("Decoding the received message completed , method name is {} ", 
                		Thread.currentThread().getStackTrace()[2].getMethodName());
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_049: [All the listeners shall be notified that a message was received from the server.]
                this.messageReceivedFromServer(msg);
            }
        }
        else
        {
            //Sender specific section for dispositions it receives
            if(event.getType() == Event.Type.DELIVERY)
            {
                logger.info("Reading the delivery event in Sender link, method name is {} ", 
                		Thread.currentThread().getStackTrace()[2].getMethodName());
                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_038: [If this link is the Sender link and the event type is DELIVERY, the event handler shall get the Delivery (Proton) object from the event.]
                Delivery d = event.getDelivery();
                DeliveryState remoteState = d.getRemoteState();

                // Codes_SRS_AMQPSIOTHUBCONNECTION_15_039: [The event handler shall note the remote delivery state and use it and the Delivery (Proton) hash code to inform the AmqpsIotHubConnection of the message receipt.]
                boolean state = remoteState.equals(Accepted.getInstance());
                logger.info("Is state of remote Delivery COMPLETE ? {}, method name is {} ", 
                		state, Thread.currentThread().getStackTrace()[2].getMethodName());
                logger.info("Inform listener that a message has been sent to IoT Hub along with remote state, method name is {} ", 
                		Thread.currentThread().getStackTrace()[2].getMethodName());
                //let any listener know that the message was received by the server
                for(ServerListener listener : listeners)
                {
                    listener.messageSent(d.hashCode(), state);
                }
		        // release the delivery object which created in sendMessage().
		        d.free();
            }
        }
        logger.debug("Exited from method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    /**
     * Event handler for the link flow event. Handles sending a single message.
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkFlow(Event event)
    {
        logger.debug("Entered in method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_040: [The event handler shall save the remaining link credit.]
        this.linkCredit = event.getLink().getCredit();
		logger.debug("The link credit value is {}, method name is {}", 
				this.linkCredit, Thread.currentThread().getStackTrace()[2].getMethodName());
        logger.debug("Exited from method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    /**
     * Event handler for the link remote open event. This signifies that the
     * {@link org.apache.qpid.proton.reactor.Reactor} is ready, so we set the connection to OPEN.
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkRemoteOpen(Event event)
    {
        logger.debug("Entered in method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_041: [The connection state shall be considered OPEN when the sender link is open remotely.]
        Link link = event.getLink();
        if (link.getName().equals(sendTag))
        {
            this.state = State.OPEN;
        }
        logger.debug("Exited from method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
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
        logger.debug("Entered in method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
        this.state = State.CLOSED;

        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_042 [The event handler shall attempt to startReconnect to the IoTHub.]
        if (event.getLink().getName().equals(sendTag))
        {
            logger.info("Starting to reconnect to IotHub, method name is {} ", Thread.currentThread().getStackTrace()[2].getMethodName());
            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_048: [The event handler shall attempt to startReconnect to IoTHub.]
            startReconnect();
        }
        logger.debug("Exited from method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    /**
     * Event handler for the link init event. Sets the proper target address on the link.
     * @param event The Proton Event object.
     */
    @Override
    public void onLinkInit(Event event)
    {
        logger.debug("Entered in method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
        Link link = event.getLink();
        if(link.getName().equals(sendTag))
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_043: [If the link is the Sender link, the event handler shall create a new Target (Proton) object using the sender endpoint address member variable.]
            Target t = new Target();
            t.setAddress(this.sendEndpoint);

            // Codes_SRS_AMQPSIOTHUBCONNECTION_15_044: [If the link is the Sender link, the event handler shall set its target to the created Target (Proton) object.]
            link.setTarget(t);

            // Codes_SRS_AMQPSIOTHUBCONNECTION_14_045: [If the link is the Sender link, the event handler shall set the SenderSettleMode to UNSETTLED.]
            link.setSenderSettleMode(SenderSettleMode.UNSETTLED);
        }
        else
        {
            // Codes_SRS_AMQPSIOTHUBCONNECTION_14_046: [If the link is the Receiver link, the event handler shall create a new Source (Proton) object using the receiver endpoint address member variable.]
            Source source = new Source();
            source.setAddress(this.receiveEndpoint);

            // Codes_SRS_AMQPSIOTHUBCONNECTION_14_047: [If the link is the Receiver link, the event handler shall set its source to the created Source (Proton) object.]
            link.setSource(source);
        }
        logger.debug("Exited from method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    /**
     * Event handler for the transport error event. This triggers reconnection attempts until successful.
     * @param event The Proton Event object.
     */
    @Override
    public void onTransportError(Event event)
    {
        logger.debug("Entered in method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
        this.state = State.CLOSED;
        logger.info("Starting to reconnect to IotHub, method name is {} ", 
        		Thread.currentThread().getStackTrace()[2].getMethodName());
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_048: [The event handler shall attempt to startReconnect to IoTHub.]
        startReconnect();
        logger.debug("Exited from method {}", Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    /**
     * Subscribe a listener to the list of listeners.
     * @param listener the listener to be subscribed.
     */
    public void addListener(ServerListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Notifies all listeners that the connection was lost and attempts to startReconnect to the IoTHub
     * using an exponential backoff interval.
     */
    private void startReconnect()
    {
        reconnectCall = true;

        for(ServerListener listener : listeners)
        {
            listener.connectionLost();
        }

        if (currentReconnectionAttempt == Integer.MAX_VALUE)
            currentReconnectionAttempt = 0;

        System.out.println("Lost connection to the server. Reconnection attempt " + currentReconnectionAttempt++ + "...");
        logger.info("Lost connection to the server. Reconnection attempt {}, method name is {} ", 
        		currentReconnectionAttempt, Thread.currentThread().getStackTrace()[2].getMethodName());
        try
        {
            Thread.sleep(TransportUtils.generateSleepInterval(currentReconnectionAttempt));
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }

        closeAsync();
    }

    /**
     * Notifies all the listeners that a message was received from the server.
     * @param msg The message received from server.
     */
    private void messageReceivedFromServer(AmqpsMessage msg)
    {
        logger.info("All the listeners are informed that a message has been received, method name is {} ", 
        		Thread.currentThread().getStackTrace()[2].getMethodName());
        for(ServerListener listener : listeners)
        {
            listener.messageReceived(msg);
        }
    }

    /**
     * Create Proton SslDomain object from Address using the given Ssl mode
     * @param mode Proton enum value of requested Ssl mode
     * @return the created Ssl domain
     */
    private SslDomain makeDomain(SslDomain.Mode mode)
    {
        SslDomain domain = Proton.sslDomain();
        String trustedDB = getPemFormat(this.config.getPathToCertificate());

        if (trustedDB != null )
        {
            domain.setTrustedCaDb(trustedDB);
        }
        // Codes_SRS_AMQPSIOTHUBCONNECTION_15_032: [The event handler shall set VERIFY_PEER authentication mode on the domain of the Transport.]
        if (domain.getTrustedCaDb() != null)
        {
           domain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);

        }
        else
        {
            throw new IllegalStateException("SSL connection unsecured, could not find certificate");
        }
        domain.init(mode);

        return domain;
    }

    private String getPemFormat(String certPath) {
        if (!isPemFile(certPath))
            certPath = convertToPem(certPath);
        return certPath;
    }

    private boolean isPemFile(String path) {
        PEMReader pemReader = null;
        Reader reader = null;
        try {
            reader = new FileReader(path);
            pemReader = new PEMReader(reader, null);

            for (String line = pemReader.readLine(); line != null; line = pemReader.readLine()) {
                if (line.contains("-----BEGIN CERTIFICATE-----")) return true;
            }
        }
        catch (IOException e)
        {
            logger.error("", e);
            throw new IOError(e);
        }
        finally
        {
            if (pemReader != null) {
                try {
                    pemReader.close();
                } catch (IOException e) {
                    logger.error("", e);
                    System.out.println("Couldn't close PEM Reader");
                }
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("", e);
                    System.out.println("Couldn't close reader");
                }
            }
        }

        return false;
    }

    private String  convertToPem(String derPath) {
        FileInputStream in = null;
        FileWriter writer = null;
        PEMWriter pemWriter = null;

        try
        {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            in = new FileInputStream(derPath);
            Certificate cert = certFactory.generateCertificate(in);
            writer = new FileWriter(derPath + ".pem");
            try {
                pemWriter = new PEMWriter(writer);
                pemWriter.writeObject(cert);
            }
            catch (IOException e)
            {
                throw new IOError(e);
            }

        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
        catch (CertificateException e)
        {
            throw new IOError(e);
        }
        finally
        {
            if(pemWriter != null)
            {
                try
                {
                    pemWriter.close();
                }
                catch(IOException e)
                {
                    System.out.println("Couldn't close PEM writer");
                }
            }

            if(writer != null)
            {
                try
                {
                    writer.close();
                }
                catch(IOException e)
                {
                    System.out.println("Couldn't close writer");
                }
            }

            if(in != null)
            {
                try
                {
                    in.close();
                }
                catch(IOException e)
                {
                    System.out.println("Couldn't close Input Stream");
                }
            }

        }

        return derPath + ".pem" ;
    }
    
    /**
     * Class which runs the reactor.
     */
    private class ReactorRunner implements Callable
    {
        private IotHubReactor iotHubReactor;

        ReactorRunner(IotHubReactor iotHubReactor)
        {
            this.iotHubReactor = iotHubReactor;
        }

        @Override
        public Object call()
        {
            iotHubReactor.run();
            return null;
        }
    }
}

