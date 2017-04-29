/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.deps.ws.impl.WebSocketImpl;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpFeedbackReceivedEvent;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpFileUploadNotificationReceivedHandler;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.codec.WritableBuffer;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.*;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.EnumSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/** Unit tests for AmqpFileUploadNotificationReceivedHandler */
@RunWith(JMockit.class)
public class AmqpFileUploadNotificationReceivedHandlerTest
{
    @Mocked Handshaker handshaker;
    @Mocked FlowController flowcontroller;
    @Mocked Proton proton;
    @Mocked Message message;
    @Mocked Connection connection;
    @Mocked Session session;
    @Mocked Transport transport;
    @Mocked TransportInternal transportInternal;
    @Mocked WebSocketImpl webSocket;
    @Mocked Sasl sasl;
    @Mocked SslDomain sslDomain;
    @Mocked Event event;
    @Mocked Receiver receiver;
    @Mocked Delivery delivery;
    @Mocked Sender sender;
    @Mocked Target target;
    @Mocked Link link;
    @Mocked Source source;

    AmqpFeedbackReceivedEvent amqpFeedbackReceivedEvent = (String feedbackJson) -> {};

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_001: [The constructor shall copy all input parameters to private member variables for event processing]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_002: [The constructor shall initialize a new Handshaker (Proton) object to handle communication handshake]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_003: [The constructor shall initialize a new FlowController (Proton) object to handle communication handshake]
    @Test
    public void amqpReceiveHandlerCallFlowAndInitOk()
    {
        // Arrange
        final String hostName = "aaa";
        final String userName = "bbb";
        final String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Assert
        new Expectations()
        {
            {
                handshaker = new Handshaker();
                flowcontroller = new FlowController();
            }
        };
        // Act
        Object amqpReceiveHandler = Deencapsulation.newInstance(AmqpFileUploadNotificationReceivedHandler.class, hostName, userName, sasToken, iotHubServiceClientProtocol, amqpFeedbackReceivedEvent);

        final String _hostName = Deencapsulation.getField(amqpReceiveHandler, "hostName");
        final String _userName = Deencapsulation.getField(amqpReceiveHandler, "userName");
        final String _sasToken = Deencapsulation.getField(amqpReceiveHandler, "sasToken");
        AmqpFeedbackReceivedEvent _amqpFeedbackReceivedEvent = Deencapsulation.getField(amqpReceiveHandler, "amqpFeedbackReceivedEvent");
        // Assert
        assertEquals(hostName + ":5671", _hostName);
        assertEquals(userName, _userName);
        assertEquals(sasToken, _sasToken);
        assertEquals(amqpFeedbackReceivedEvent, _amqpFeedbackReceivedEvent);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_021: [** The constructor shall throw IllegalArgumentException if any of the parameters are null or empty **]
    @Test (expected = IllegalArgumentException.class)
    public void amqpReceiveHandlerNullHostNameThrows()
    {
        // Arrange
        final String hostName = null;
        final String userName = "bbb";
        final String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;

        // Act
        Object amqpReceiveHandler = Deencapsulation.newInstance(AmqpFileUploadNotificationReceivedHandler.class, String.class, userName, sasToken, iotHubServiceClientProtocol, amqpFeedbackReceivedEvent);
    }

    @Test (expected = IllegalArgumentException.class)
    public void amqpReceiveHandlerNullUserNameThrows()
    {
        // Arrange
        final String hostName = "abc";
        final String userName = null;
        final String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;

        // Act
        Object amqpReceiveHandler = Deencapsulation.newInstance(AmqpFileUploadNotificationReceivedHandler.class, hostName, String.class, sasToken, iotHubServiceClientProtocol, amqpFeedbackReceivedEvent);
    }

    @Test (expected = IllegalArgumentException.class)
    public void amqpReceiveHandlerNullSasTokenThrows()
    {
        // Arrange
        final String hostName = "abc";
        final String userName = "bbb";
        final String sasToken = null;
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;

        // Act
        Object amqpReceiveHandler = Deencapsulation.newInstance(AmqpFileUploadNotificationReceivedHandler.class, hostName, userName, String.class, iotHubServiceClientProtocol, amqpFeedbackReceivedEvent);
    }

    @Test (expected = IllegalArgumentException.class)
    public void amqpReceiveHandlerEmptyHostNameThrows()
    {
        // Arrange
        final String hostName = "";
        final String userName = "bbb";
        final String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;

        // Act
        Object amqpReceiveHandler = Deencapsulation.newInstance(AmqpFileUploadNotificationReceivedHandler.class, hostName, userName, sasToken, iotHubServiceClientProtocol, amqpFeedbackReceivedEvent);
    }

    @Test (expected = IllegalArgumentException.class)
    public void amqpReceiveHandlerEmptyUserNameThrows()
    {
        // Arrange
        final String hostName = "abc";
        final String userName = "";
        final String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;

        // Act
        Object amqpReceiveHandler = Deencapsulation.newInstance(AmqpFileUploadNotificationReceivedHandler.class, hostName, userName, sasToken, iotHubServiceClientProtocol, amqpFeedbackReceivedEvent);
    }

    @Test (expected = IllegalArgumentException.class)
    public void amqpReceiveHandlerEmptySasTokenThrows()
    {
        // Arrange
        final String hostName = "abc";
        final String userName = "bbb";
        final String sasToken = "";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;

        // Act
        Object amqpReceiveHandler = Deencapsulation.newInstance(AmqpFileUploadNotificationReceivedHandler.class, hostName, userName, sasToken, iotHubServiceClientProtocol, amqpFeedbackReceivedEvent);
    }
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_004: [The event handler shall get the Link, Receiver and Delivery (Proton) objects from the event]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_005: [The event handler shall read the received buffer]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_006: [The event handler shall create a Message (Proton) object from the decoded buffer]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_007: [The event handler shall settle the Delivery with the Accepted outcome ]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_008: [The event handler shall close the Session and Connection (Proton)]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_009: [The event handler shall call the FeedbackReceived callback if it has been initialized]
    @Test
    public void onDeliveryCallFlowAndInitOk(@Mocked Data mockData)
    {
        // Arrange
        final String hostName = "aaa";
        final String userName = "bbb";
        final String sasToken = "ccc";
        final String hostAddr = hostName + ":5671";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        createProtonObjects();
        Object amqpReceiveHandler = Deencapsulation.newInstance(AmqpFileUploadNotificationReceivedHandler.class, hostName, userName, sasToken, iotHubServiceClientProtocol, amqpFeedbackReceivedEvent);
        // Assert
        new Expectations()
        {
            {
                event.getLink();
                receiver.current();
                delivery.isReadable();
                delivery.isPartial();
                delivery.getLink();
                delivery.pending();
                byte[] buffer = new byte[1024];
                receiver.recv(buffer, 0, buffer.length);
                message.decode(withAny(buffer), 0, anyInt);
                delivery.disposition(Accepted.getInstance()); // send disposition frame and settle the outcome
                delivery.settle();
                session = receiver.getSession();
                session.close();
                connection = session.getConnection();
                connection.close();
                message.getBody();
                result = mockData;

            }
        };
        // Act
        Deencapsulation.invoke(amqpReceiveHandler, "onDelivery", event);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_009: [The event handler shall set the SASL PLAIN authentication on the Transport using the given user name and sas token]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_010: [The event handler shall set ANONYMUS_PEER authentication mode on the domain of the Transport]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_017: [The event handler shall not initialize WebSocket if the protocol is AMQP]
    @Test
    public void onConnectionBoundCallFlowAndInitOkAmqp()
    {
        // Arrange
        final String hostName = "aaa";
        final String userName = "bbb";
        final String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        Object amqpReceiveHandler = Deencapsulation.newInstance(AmqpFileUploadNotificationReceivedHandler.class, hostName, userName, sasToken, iotHubServiceClientProtocol, amqpFeedbackReceivedEvent);
        // Assert
        new Expectations()
        {
            {
                connection = event.getConnection();
                transport = connection.getTransport();
                sasl.plain(anyString, anyString);
                sslDomain = Proton.sslDomain();
                sslDomain.init(SslDomain.Mode.CLIENT);
                sslDomain.setPeerAuthentication(SslDomain.VerifyMode.ANONYMOUS_PEER);
                transport.ssl(sslDomain);
            }
        };
        // Act
        Deencapsulation.invoke(amqpReceiveHandler, "onConnectionBound", event);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_009: [The event handler shall set the SASL PLAIN authentication on the Transport using the given user name and sas token]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_010: [The event handler shall set ANONYMUS_PEER authentication mode on the domain of the Transport]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_018: [The event handler shall initialize WebSocket if the protocol is AMQP_WS]
    @Test
    public void onConnectionBoundCallFlowAndInitOkAmqps()
    {
        // Arrange
        final String hostName = "aaa";
        final String userName = "bbb";
        final String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS_WS;
        Object amqpReceiveHandler = Deencapsulation.newInstance(AmqpFileUploadNotificationReceivedHandler.class, hostName, userName, sasToken, iotHubServiceClientProtocol, amqpFeedbackReceivedEvent);

        // Assert
        new Expectations()
        {
            {
                event.getConnection();
                result = connection;
                connection.getTransport();
                result = transportInternal;
                new WebSocketImpl();
                result = webSocket;
                webSocket.configure(anyString, anyString, 0, anyString, null, null);
                transportInternal.addTransportLayer(webSocket);
                sasl.plain(anyString, anyString);
                Proton.sslDomain();
                result = sslDomain;
                sslDomain.init(SslDomain.Mode.CLIENT);
                sslDomain.setPeerAuthentication(SslDomain.VerifyMode.ANONYMOUS_PEER);
                transportInternal.ssl(sslDomain);
            }
        };
        // Act
        Deencapsulation.invoke(amqpReceiveHandler, "onConnectionBound", event);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_011: [The event handler shall set the host name on the connection]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_012: [The event handler shall create a Session (Proton) object from the connection]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_013: [The event handler shall create a Receiver (Proton) object and set the protocol tag on it to a predefined constant]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_014: [The event handler shall open the Connection, the Session and the Receiver object]
    // Tests_SRS_SERVICE_SDK_JAVA_AmqpFileUploadNotificationReceivedHandler_15_017: [The Receiver object shall have the properties set to service client version identifier.]
    @Test
    public void onConnectionInitCallFlowAndInitOk()
    {
        // Arrange
        final String hostName = "aaa";
        final String userName = "bbb";
        final String sasToken = "ccc";
        final String hostAddr = hostName + ":5671";
        final String receiver_tag = "filenotificationreceiver";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        Object amqpReceiveHandler = Deencapsulation.newInstance(AmqpFileUploadNotificationReceivedHandler.class, hostName, userName, sasToken, iotHubServiceClientProtocol, amqpFeedbackReceivedEvent);
        // Assert
        new Expectations()
        {
            {
                connection = event.getConnection();
                connection.setHostname(hostAddr);
                session = connection.session();
                receiver = session.receiver(receiver_tag);
                connection.open();
                session.open();
                receiver.open();
                receiver.setProperties((Map<Symbol, Object>) any);
            }
        };
        // Act
        Deencapsulation.invoke(amqpReceiveHandler, "onConnectionInit", event);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_015: [The event handler shall create a new Target (Proton) object using the given endpoint address]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_016: [The event handler shall get the Link (Proton) object and set its target to the created Target (Proton) object]
    @Test
    public void onLinkInitCallFlowAndInitOk()
    {
        // Arrange
        final String hostName = "aaa";
        final String userName = "bbb";
        final String sasToken = "ccc";
        final String hostAddr = hostName + ":5671";
        final String endpoint = "/messages/serviceBound/filenotifications";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        Object amqpReceiveHandler = Deencapsulation.newInstance(AmqpFileUploadNotificationReceivedHandler.class, hostName, userName, sasToken, iotHubServiceClientProtocol, amqpFeedbackReceivedEvent);

        // Assert
        new Expectations()
        {
            {
                link = event.getLink();
                link.getName();
                result = Deencapsulation.getField(amqpReceiveHandler, "FILE_NOTIFICATION_RECEIVE_TAG");
                target = new Target();
                target.setAddress(endpoint);
                source = new Source();
                source.setAddress(endpoint);
                link.setTarget(target);
                link.setSource(source);
            }
        };
        // Act
        Deencapsulation.invoke(amqpReceiveHandler, "onLinkInit", event);
    }

    private void createProtonObjects()
    {
        final String exceptionMessage = "Not expected function called";

        message = Proton.message();

        receiver = new Receiver()
        {
            @Override
            public void flow(int i)
            { }

            @Override
            public int recv(byte[] bytes, int i, int i1)
            { return 0; }

            @Override
            public int recv(WritableBuffer writableBuffer)
            {
                return 0;
            }

            @Override
            public void drain(int i)
            { }

            @Override
            public boolean advance()
            { return false; }

            @Override
            public boolean draining()
            { return false; }

            @Override
            public void setDrain(boolean b)
            { }

            @Override
            public String getName()
            { return "filenotificationreceiver";}

            @Override
            public Delivery delivery(byte[] bytes)
            { return null; }

            @Override
            public Delivery delivery(byte[] bytes, int i, int i1)
            { return null; }

            @Override
            public Delivery head()
            { return null; }

            @Override
            public Delivery current()
            { return delivery; }

            @Override
            public org.apache.qpid.proton.amqp.transport.Source getSource()
            { return null; }

            @Override
            public org.apache.qpid.proton.amqp.transport.Target getTarget()
            { return null; }

            @Override
            public void setSource(org.apache.qpid.proton.amqp.transport.Source source)
            { }

            @Override
            public void setTarget(org.apache.qpid.proton.amqp.transport.Target target)
            { }

            @Override
            public org.apache.qpid.proton.amqp.transport.Source getRemoteSource()
            { return null; }

            @Override
            public org.apache.qpid.proton.amqp.transport.Target getRemoteTarget()
            { return null; }

            @Override
            public Link next(EnumSet<EndpointState> enumSet, EnumSet<EndpointState> enumSet1)
            { return null; }

            @Override
            public int getCredit()
            { return 0; }

            @Override
            public int getQueued()
            { return 0; }

            @Override
            public int getUnsettled()
            { return 0; }

            @Override
            public Session getSession()
            { return session; }

            @Override
            public SenderSettleMode getSenderSettleMode()
            { return null; }

            @Override
            public void setSenderSettleMode(SenderSettleMode senderSettleMode)
            { }

            @Override
            public SenderSettleMode getRemoteSenderSettleMode()
            { return null; }

            @Override
            public ReceiverSettleMode getReceiverSettleMode()
            { return null; }

            @Override
            public void setReceiverSettleMode(ReceiverSettleMode receiverSettleMode)
            { }

            @Override
            public ReceiverSettleMode getRemoteReceiverSettleMode()
            { return null; }

            @Override
            public void setRemoteSenderSettleMode(SenderSettleMode senderSettleMode)
            { }

            @Override
            public Map<Symbol, Object> getProperties()
            {
                return null;
            }

            @Override
            public void setProperties(Map<Symbol, Object> map)
            {

            }

            @Override
            public Map<Symbol, Object> getRemoteProperties()
            {
                return null;
            }

            @Override
            public int drained()
            { return 0; }

            @Override
            public int getRemoteCredit()
            { return 0; }

            @Override
            public boolean getDrain()
            { return false; }

            @Override
            public void detach()
            { }

            @Override
            public boolean detached()
            { return false; }

            @Override
            public void setOfferedCapabilities(Symbol[] symbols)
            {

            }

            @Override
            public Symbol[] getOfferedCapabilities()
            {
                return new Symbol[0];
            }

            @Override
            public Symbol[] getRemoteOfferedCapabilities()
            {
                return new Symbol[0];
            }

            @Override
            public void setDesiredCapabilities(Symbol[] symbols)
            {

            }

            @Override
            public Symbol[] getDesiredCapabilities()
            {
                return new Symbol[0];
            }

            @Override
            public Symbol[] getRemoteDesiredCapabilities()
            {
                return new Symbol[0];
            }

            @Override
            public EndpointState getLocalState()
            { return null; }

            @Override
            public EndpointState getRemoteState()
            { return null; }

            @Override
            public ErrorCondition getCondition()
            { return null; }

            @Override
            public void setCondition(ErrorCondition errorCondition)
            { }

            @Override
            public ErrorCondition getRemoteCondition()
            { return null; }

            @Override
            public void free()
            { }

            @Override
            public void open()
            { }

            @Override
            public void close()
            { }

            @Override
            public void setContext(Object o)
            { }

            @Override
            public Object getContext()
            { return null; }

            @Override
            public Record attachments()
            { return null; }
        };

        event = new Event()
        {

            @Override
            public EventType getEventType()
            {
                return null;
            }

            @Override public Type getType()
            { throw new UnsupportedOperationException(exceptionMessage); }

            @Override public Object getContext()
            { throw new UnsupportedOperationException(exceptionMessage); }

            @Override
            public Handler getRootHandler()
            {
                return null;
            }

            @Override public void dispatch(Handler hndlr) throws HandlerException
            { throw new UnsupportedOperationException(exceptionMessage); }

            @Override
            public void redispatch(EventType eventType, Handler handler) throws HandlerException
            {

            }

            @Override
            public void delegate() throws HandlerException
            {

            }

            @Override public Connection getConnection()
            { return connection; }

            @Override public Session getSession()
            { throw new UnsupportedOperationException(exceptionMessage); }

            @Override
            public Link getLink()
            { return receiver; }

            @Override
            public Sender getSender()
            {
                return null;
            }

            @Override
            public Receiver getReceiver()
            {
                return null;
            }

            @Override
            public Delivery getDelivery()
            { return delivery; }

            @Override
            public Transport getTransport()
            { throw new UnsupportedOperationException(exceptionMessage); }

            @Override
            public Reactor getReactor()
            { throw new UnsupportedOperationException(exceptionMessage); }

            @Override
            public Selectable getSelectable()
            { throw new UnsupportedOperationException(exceptionMessage); }

            @Override
            public Task getTask()
            { throw new UnsupportedOperationException(exceptionMessage);}

            @Override
            public Event copy()
            { throw new UnsupportedOperationException(exceptionMessage); }

            @Override
            public Record attachments()
            { throw new UnsupportedOperationException(exceptionMessage); }
        };

        delivery = new Delivery()
        {
            @Override
            public byte[] getTag()
            { return new byte[0]; }

            @Override
            public Link getLink()
            { return receiver; }

            @Override
            public DeliveryState getLocalState()
            { return null; }

            @Override
            public DeliveryState getRemoteState()
            { return null; }

            @Override
            public int getMessageFormat()
            { return 0; }

            @Override
            public int available()
            {
                return 0;
            }

            @Override
            public void disposition(DeliveryState deliveryState)
            { }

            @Override
            public void settle()
            { }

            @Override
            public boolean isSettled()
            { return false; }

            @Override
            public boolean remotelySettled()
            { return false; }

            @Override
            public void free()
            { }

            @Override
            public Delivery getWorkNext()
            { return null; }

            @Override
            public Delivery next()
            { return null; }

            @Override
            public boolean isWritable()
            { return false; }

            @Override
            public boolean isReadable()
            { return true; }

            @Override
            public void setContext(Object o)
            { }

            @Override
            public Object getContext()
            { return null; }

            @Override
            public boolean isUpdated()
            { return false; }

            @Override
            public void clear()
            { }

            @Override
            public boolean isPartial()
            { return false; }

            @Override
            public int pending()
            { return 0; }

            @Override
            public boolean isBuffered()
            { return false; }

            @Override
            public void setDefaultDeliveryState(DeliveryState deliveryState)
            { }

            @Override
            public DeliveryState getDefaultDeliveryState()
            { return null; }

            @Override
            public void setMessageFormat(int i)
            {

            }

            @Override
            public Record attachments()
            { return null; }
        };
    }
}