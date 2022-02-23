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
import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Unit tests for EventReceivingConnectionHandler */
@RunWith(JMockit.class)
public class EventReceivingConnectionHandlerTest
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
        Object amqpReceiveHandler = Deencapsulation.newInstance(EventReceivingConnectionHandler.class, String.class, userName, sasToken, iotHubServiceClientProtocol, fileUploadNotificationReceivedCallback, mockedProxyOptions, mockedSslContext, 230);
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
        Object amqpReceiveHandler = Deencapsulation.newInstance(EventReceivingConnectionHandler.class, hostName, userName, String.class, iotHubServiceClientProtocol, fileUploadNotificationReceivedCallback, mockedProxyOptions, mockedSslContext, 230);
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
        Object amqpReceiveHandler = new EventReceivingConnectionHandler(connectionString, iotHubServiceClientProtocol, fileUploadNotificationReceivedCallback, null, null, mockedProxyOptions, mockedSslContext, 230);

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

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPFILEUPLOADNOTIFICATIONRECEIVEDHANDLER_34_023: [if 'connectionWasOpened' is false, or 'isConnectionError' is true, this function shall throw an IOException]
    @Test (expected = IotHubException.class)
    public void verifyConnectionOpenedChecksThatConnectionWasOpened() throws IOException, IotHubException
    {
        // Arrange
        String connectionString = "aaa";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS_WS;
        EventReceivingConnectionHandler amqpReceiveHandler = new EventReceivingConnectionHandler(connectionString, iotHubServiceClientProtocol, fileUploadNotificationReceivedCallback, null, null, mockedProxyOptions, mockedSslContext, 230);

        Deencapsulation.setField(amqpReceiveHandler, "protonJExceptionParser", new ProtonJExceptionParser(new IotHubException()));

        // Act
        Deencapsulation.invoke(amqpReceiveHandler, "verifyConnectionWasOpened");
    }
}