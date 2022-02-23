/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.proton.transport.proxy.impl.ProxyHandlerImpl;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyImpl;
import com.microsoft.azure.proton.transport.ws.impl.WebSocketImpl;
import com.microsoft.azure.sdk.iot.service.ProxyOptions;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.messaging.AcknowledgementType;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotification;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.codec.ReadableBuffer;
import org.apache.qpid.proton.codec.WritableBuffer;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.EventType;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.engine.HandlerException;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Sasl;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.FlowController;
import org.apache.qpid.proton.reactor.Handshaker;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.apache.qpid.proton.reactor.Task;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Unit tests for AmqpEventProcessorHandler */
@RunWith(JMockit.class)
public class AmqpEventProcessorHandlerTest
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
    @Mocked ReadableBuffer readBuf;
    @Mocked ProxyOptions mockedProxyOptions;
    @Mocked ProxyImpl mockedProxyImpl;
    @Mocked ProxyHandlerImpl mockedProxyHandlerImpl;
    @Mocked SSLContext mockedSslContext;
    @Mocked IotHubConnectionStringBuilder mockIotHubConnectionStringBuilder;

    Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationReceivedCallback = notification -> AcknowledgementType.COMPLETE;

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
        Object amqpReceiveHandler = Deencapsulation.newInstance(AmqpEventProcessorHandler.class, String.class, userName, sasToken, iotHubServiceClientProtocol, fileUploadNotificationReceivedCallback, mockedProxyOptions, mockedSslContext, 230);
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
        Object amqpReceiveHandler = Deencapsulation.newInstance(AmqpEventProcessorHandler.class, hostName, userName, String.class, iotHubServiceClientProtocol, fileUploadNotificationReceivedCallback, mockedProxyOptions, mockedSslContext, 230);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_009: [The event handler shall set the SASL PLAIN authentication on the Transport using the given user name and sas token]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_010: [The event handler shall set VERIFY_PEER authentication mode on the domain of the Transport]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_25_018: [The event handler shall initialize WebSocket if the protocol is AMQP_WS]
    @Test
    public void onConnectionBoundCallFlowAndInitOkAmqps()
    {
        // Arrange
        final String connectionString = "aaa";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS_WS;
        Object amqpReceiveHandler = new AmqpEventProcessorHandler(connectionString, iotHubServiceClientProtocol, fileUploadNotificationReceivedCallback, null, null, mockedProxyOptions, mockedSslContext, 230);

        // Assert
        new Expectations()
        {
            {
                event.getConnection();
                result = connection;
                connection.getTransport();
                result = transportInternal;
                new ProxyImpl();
                result = mockedProxyImpl;
                new ProxyHandlerImpl();
                result = mockedProxyHandlerImpl;
                mockedProxyImpl.configure(anyString, (Map<String, String>) any, mockedProxyHandlerImpl, transportInternal);
                transportInternal.addTransportLayer(mockedProxyImpl);
                new WebSocketImpl(anyInt);
                result = webSocket;
                webSocket.configure(anyString, anyString, anyString, 443, anyString, null, null);
                transportInternal.addTransportLayer(webSocket);
                Proton.sslDomain();
                result = sslDomain;
                sslDomain.init(SslDomain.Mode.CLIENT);
                sslDomain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
                transportInternal.ssl(sslDomain);
            }
        };
        // Act
        Deencapsulation.invoke(amqpReceiveHandler, "onConnectionBound", event);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_34_022: [This function shall set the variable 'connectionWasOpened' to true]
    @Test
    public void onLinkRemoteOpenedFlagsConnectionWasOpened(@Mocked Event mockEvent)
    {
        // Arrange
        String connectionString = "aaa";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS_WS;
        AmqpEventProcessorHandler amqpReceiveHandler = new AmqpEventProcessorHandler(connectionString, iotHubServiceClientProtocol, fileUploadNotificationReceivedCallback, null, null, mockedProxyOptions, mockedSslContext, 230);

        // Act
        amqpReceiveHandler.onLinkRemoteOpen(mockEvent);

        // Assert
        assertTrue(Deencapsulation.getField(amqpReceiveHandler, "linkOpenedRemotely"));
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_34_023: [if 'connectionWasOpened' is false, or 'isConnectionError' is true, this function shall throw an IOException]
    @Test (expected = IOException.class)
    public void verifyConnectionOpenedChecksThatConnectionWasOpened() throws IOException, IotHubException
    {
        // Arrange
        String connectionString = "aaa";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS_WS;
        AmqpEventProcessorHandler amqpReceiveHandler = new AmqpEventProcessorHandler(connectionString, iotHubServiceClientProtocol, fileUploadNotificationReceivedCallback, null, null, mockedProxyOptions, mockedSslContext, 230);

        Deencapsulation.setField(amqpReceiveHandler, "connectionOpenedRemotely", false);
        Deencapsulation.setField(amqpReceiveHandler, "sessionOpenedRemotely", true);
        Deencapsulation.setField(amqpReceiveHandler, "linkOpenedRemotely", true);

        // Act
        Deencapsulation.invoke(amqpReceiveHandler, "verifyConnectionWasOpened");
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
            public ReadableBuffer recv()
            { return readBuf; }

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
            public void setMaxMessageSize(UnsignedLong unsignedLong)
            {

            }

            @Override
            public UnsignedLong getMaxMessageSize()
            {
                return null;
            }

            @Override
            public UnsignedLong getRemoteMaxMessageSize()
            {
                return null;
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
            public boolean isAborted()
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