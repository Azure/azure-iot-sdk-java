/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.deps.ws.impl.WebSocketImpl;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpResponseVerification;
import com.microsoft.azure.sdk.iot.service.transport.amqps.AmqpSendHandler;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.*;
import org.apache.qpid.proton.amqp.transport.Source;
import org.apache.qpid.proton.codec.ReadableBuffer;
import org.apache.qpid.proton.codec.WritableBuffer;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.message.MessageError;
import org.apache.qpid.proton.reactor.Handshaker;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.apache.qpid.proton.reactor.Task;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Unit tests for AmqpSendHandler */
@SuppressWarnings({"ResultOfMethodCallIgnored", "ThrowableNotThrown"})
@RunWith(JMockit.class)
public class AmqpSendHandlerTest
{
    private Integer exceptionCount = 0;

    @Mocked Handshaker handshaker;
    @Mocked Proton proton;
    @Mocked Message message;
    @Mocked Message messageWithException;
    @Mocked Properties properties;
    @Mocked Binary binary;
    @Mocked Section section;
    @Mocked Data data;
    @Mocked Event event;
    @Mocked Transport transport;
    @Mocked TransportInternal transportInternal;
    @Mocked Connection connection;
    @Mocked WebSocketImpl webSocket;
    @Mocked Sasl sasl;
    @Mocked SslDomain sslDomain;
    @Mocked Session session;
    @Mocked Sender sender;
    @Mocked Link link;
    @Mocked Target target;
    @Mocked Delivery delivery;
    @Mocked Disposition disposition;
    @Mocked AmqpResponseVerification responseVerification;
    @Mocked IotHubSSLContext mockedIotHubSSLContext;

    // Test_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_001: [The constructor shall copy all input parameters to private member variables for event processing]
    // Test_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_002: [The constructor shall concatenate the host name with the port]
    // Test_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_003: [The constructor shall initialize a new Handshaker (Proton) object to handle communication handshake]
    @Test
    public void constructor_copies_params_to_members()
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        new Expectations()
        {
            {
                handshaker = new Handshaker();
            }
        };
        // Act
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
        String _hostName = Deencapsulation.getField(amqpSendHandler, "hostName");
        String _userName = Deencapsulation.getField(amqpSendHandler, "userName");
        String _sasToken = Deencapsulation.getField(amqpSendHandler, "sasToken");
        IotHubServiceClientProtocol _ioIotHubServiceClientProtocol = Deencapsulation.getField(amqpSendHandler, "iotHubServiceClientProtocol");
        // Assert
        assertEquals(hostName, _hostName);
        assertEquals(userName, _userName);
        assertEquals(sasToken, _sasToken);
        assertEquals(iotHubServiceClientProtocol, _ioIotHubServiceClientProtocol);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_001: [The constructor shall throw IllegalArgumentException if any of the input parameter is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_checks_if_hostName_null()
    {
        // Arrange
        String hostName = null;
        String userName = "bbb";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        AmqpSendHandler amqpSend = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_001: [The constructor shall throw IllegalArgumentException if any of the input parameter is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_checks_if_hostName_empty()
    {
        // Arrange
        String hostName = "";
        String userName = "bbb";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        AmqpSendHandler amqpSend = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_001: [The constructor shall throw IllegalArgumentException if any of the input parameter is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_checks_if_userName_null()
    {
        // Arrange
        String hostName = "aaa";
        String userName = null;
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        AmqpSendHandler amqpSend = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_001: [The constructor shall throw IllegalArgumentException if any of the input parameter is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_checks_if_userName_empty()
    {
        // Arrange
        String hostName = "aaa";
        String userName = "";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        AmqpSendHandler amqpSend = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_001: [The constructor shall throw IllegalArgumentException if any of the input parameter is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_checks_if_sasToken_null()
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = null;
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        AmqpSendHandler amqpSend = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_001: [The constructor shall throw IllegalArgumentException if any of the input parameter is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_checks_if_sasToken_empty()
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        // Act
        AmqpSendHandler amqpSend = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
    }
    
     // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_001: [The constructor shall throw IllegalArgumentException if any of the input parameter is null or empty]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_checks_if_protocol_null()
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = null;
        // Act
        AmqpSendHandler amqpSend = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_004: [The function shall create a new Message (Proton) object]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_005: [The function shall set the “to” property on the Message object using the created device path]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_006: [The function shall create a Binary (Proton) object from the content string]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_007: [The function shall create a data Section (Proton) object from the Binary]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_008: [The function shall set the Message body to the created data section]
    @Test
    public void createProtonMessage_creates_Message_and_sets_Properties() throws UnsupportedEncodingException
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        String deviceId = "deviceId";
        String content = "abcdefghijklmnopqrst";
        String toProperty = "/devices/deviceId/messages/devicebound";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
        com.microsoft.azure.sdk.iot.service.Message iotMessage = new com.microsoft.azure.sdk.iot.service.Message(content);
        Map<String, String> userDefinedProperties = new HashMap<>(5);
        userDefinedProperties.put("key1", "value1");
        userDefinedProperties.put("key2", "value2");
        userDefinedProperties.put("key3", "value3");
        userDefinedProperties.put("key4", "value4");
        userDefinedProperties.put("key5", "value5");
        iotMessage.setProperties(userDefinedProperties);
        // Assert
        new Expectations()
        {
            {
                message = Proton.message();
                new Properties();
                result = properties;
                message.setProperties(properties);
                binary = new Binary(content.getBytes());
                section = new Data(binary);
                message.setApplicationProperties((ApplicationProperties) any);
                message.setBody(section);
            }
        };
        // Act
        amqpSendHandler.createProtonMessage(deviceId, iotMessage);

        new Verifications()
        {
            {
                properties.setTo(toProperty);
                properties.setMessageId(any);
                properties.setAbsoluteExpiryTime((Date) any);
                properties.setCorrelationId(any);
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_004: [The function shall create a new Message (Proton) object]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_005: [The function shall set the “to” property on the Message object using the created device path]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_006: [The function shall create a Binary (Proton) object from the content string]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_007: [The function shall create a data Section (Proton) object from the Binary]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_008: [The function shall set the Message body to the created data section]
    @Test
    public void createProtonMessage_creates_Message_and_sets_Properties_ForModule() throws UnsupportedEncodingException
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        String deviceId = "deviceId";
        String moduleId = "moduleId";
        String content = "abcdefghijklmnopqrst";
        String toProperty = "/devices/deviceId/modules/moduleId/messages/devicebound";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
        com.microsoft.azure.sdk.iot.service.Message iotMessage = new com.microsoft.azure.sdk.iot.service.Message(content);
        Map<String, String> userDefinedProperties = new HashMap<>(5);
        userDefinedProperties.put("key1", "value1");
        userDefinedProperties.put("key2", "value2");
        userDefinedProperties.put("key3", "value3");
        userDefinedProperties.put("key4", "value4");
        userDefinedProperties.put("key5", "value5");
        iotMessage.setProperties(userDefinedProperties);
        // Assert
        new Expectations()
        {
            {
                message = Proton.message();
                new Properties();
                result = properties;
                message.setProperties(properties);
                binary = new Binary(content.getBytes());
                section = new Data(binary);
                message.setApplicationProperties((ApplicationProperties) any);
                message.setBody(section);
            }
        };
        // Act
        amqpSendHandler.createProtonMessage(deviceId, moduleId, iotMessage);

        new Verifications()
        {
            {
                properties.setTo(toProperty);
                properties.setMessageId(any);
                properties.setAbsoluteExpiryTime((Date) any);
                properties.setCorrelationId(any);
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_009: [The event handler shall set the SASL PLAIN authentication on the Transport using the given user name and sas token]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_010: [The event handler shall set VERIFY_PEER authentication mode on the domain of the Transport]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_023: [The event handler shall not initialize WebSocket if the protocol is AMQP]
    @Test
    public void onConnectionBound_call_flow_and_init_ok_amqp()
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        String hostAddr = hostName + ":5671";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
        // Assert
        new Expectations()
        {
            {
                connection = event.getConnection();
                transport = connection.getTransport();
                sasl.plain(anyString, anyString);
                sslDomain = Proton.sslDomain();
                sslDomain.init(SslDomain.Mode.CLIENT);
                sslDomain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
                transport.ssl(sslDomain);
            }
        };
        // Act
        amqpSendHandler.onConnectionBound(event);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_009: [The event handler shall set the SASL PLAIN authentication on the Transport using the given user name and sas token]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_010: [The event handler shall set VERIFY_PEER authentication mode on the domain of the Transport]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_024: [The event handler shall initialize WebSocket if the protocol is AMQP_WS]
    @Test
    public void onConnectionBound_call_flow_and_init_ok_amqp_ws() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        String hostAddr = hostName + ":443";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS_WS;
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
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
                webSocket.configure(anyString, anyString, 443, anyString, null, null);
                transportInternal.addTransportLayer(webSocket);
                sasl.plain(anyString, anyString);
                Proton.sslDomain();
                result = sslDomain;
                sslDomain.init(SslDomain.Mode.CLIENT);
                sslDomain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
                transportInternal.ssl(sslDomain);
                new IotHubSSLContext();
                result = mockedIotHubSSLContext;
            }
        };
        // Act
        amqpSendHandler.onConnectionBound(event);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_011: [The event handler shall set the host name on the connection]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_012: [The event handler shall create a Session (Proton) object from the connection]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_013: [The event handler shall create a Sender (Proton) object and set the protocol tag on it to a predefined constant]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_014: [The event handler shall open the Connection, the Session and the Sender object]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_15_023: [The Sender object shall have the properties set to service client version identifier.]
    @Test
    public void onConnectionInit_creates_Session_and_open_Connection()
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
        // Assert
        new Expectations()
        {
            {
                connection = event.getConnection();
                connection.setHostname(hostName);
                session = connection.session();
                sender = session.sender(anyString);
                connection.open();
                session.open();
                sender.open();
                sender.setProperties((Map<Symbol, Object>) any);
            }
        };
        // Act
        amqpSendHandler.onConnectionInit(event);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_015: [The event handler shall create a new Target (Proton) object using the given endpoint address]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_016: [The event handler shall get the Link (Proton) object and set its target to the created Target (Proton) object]
    @Test
    public void onLinkInit_call_flow_ok()
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        String endpoint = "/messages/devicebound";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
        // Assert
        new Expectations()
        {
            {
                link = event.getLink();
                target = new Target();
                target.setAddress(endpoint);
            }
        };
        // Act
        amqpSendHandler.onLinkInit(event);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_017: [The event handler shall get the Sender (Proton) object from the link]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_018: [The event handler shall encode the message and copy to the byte buffer]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_019: [The event handler shall set the delivery tag on the Sender (Proton) object]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_020: [The event handler shall send the encoded bytes]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_021: [The event handler shall close the Sender, Session and Connection]
    @Test
    public void onLinkFlow_call_flow_ok() throws UnsupportedEncodingException
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        String hostAddr = hostName + ":5671";
        String deviceId = "deviceId";
        String content = "abcdefghijklmnopqrst";
        com.microsoft.azure.sdk.iot.service.Message iotMessage = new com.microsoft.azure.sdk.iot.service.Message(content);
        String endpoint = "/messages/devicebound";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        createProtonObjects();
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
        amqpSendHandler.createProtonMessage(deviceId, iotMessage);
        // Assert
        new Expectations()
        {
            {
                link = event.getLink();
                link.getCredit();
                byte[] buffer = new byte[1024];
                message.encode(buffer, 0, 1024);
            }
        };
        // Act
        amqpSendHandler.onLinkFlow(event);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_017: [The event handler shall get the Sender (Proton) object from the link]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_018: [The event handler shall encode the message and copy to the byte buffer]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_019: [The event handler shall set the delivery tag on the Sender (Proton) object]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_020: [The event handler shall send the encoded bytes]
    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_12_021: [The event handler shall close the Sender, Session and Connection]
    @Test
    public void onLinkFlowBufferOverflow_call_flow_ok() throws UnsupportedEncodingException
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        String hostAddr = hostName + ":5671";
        String deviceId = "deviceId";
        String content = "abcdefghijklmnopqrst";
        com.microsoft.azure.sdk.iot.service.Message iotMessage = new com.microsoft.azure.sdk.iot.service.Message(content);
        String endpoint = "/messages/devicebound";
        exceptionCount = 0;
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        createProtonObjects();
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
        Deencapsulation.setField(amqpSendHandler,"messageToBeSent", messageWithException);
        // Assert
        new Expectations()
        {
            {
                link = event.getLink();
                link.getCredit();
            }
        };
        // Act
        amqpSendHandler.onLinkFlow(event);
    }

    /*
    Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_25_023: [** The event handler shall get the Delivery from the event only if the event type is DELIVERY **]**

    Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_25_024: [** The event handler shall get the Delivery remote state from the delivery **]**

    Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_25_025: [** The event handler shall verify the Amqp response and add the response to a queue. **]**

    Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_25_026: [** The event handler shall settle the delivery. **]**

    Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_25_027: [** The event handler shall get the Sender (Proton) object from the event **]**

    Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_25_028: [** The event handler shall close the Sender, Session and Connection **]**
     */
    @Test
    public void onDelivery_flow_ok(final @Mocked Event mockedEvent,
                                   final @Mocked DeliveryState mockedDeliveryState,
                                   final @Mocked Delivery mockedDelivery) throws UnsupportedEncodingException
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);

        // Assert
        new Expectations()
        {
            {
                mockedEvent.getType();
                result = Event.Type.DELIVERY;
                mockedEvent.getDelivery();
                result = mockedDelivery;
                mockedDelivery.getRemoteState();
                result = mockedDeliveryState;
                sender.getLocalState();
                result = EndpointState.ACTIVE;
                sender.close();
            }
        };
        // Act
        amqpSendHandler.onDelivery(mockedEvent);
    }

    /*
    Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_25_029: [** The event handler shall check the status queue to get the response for the sent message **]**

    Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_25_030: [** The event handler shall remove the response from the queue **]**

    Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_25_031: [** The event handler shall get the exception from the response and throw is it is not null **]**
     */
    @Test
    public void sendComplete_flow_OK(final @Mocked AmqpResponseVerification mockedVerification) throws IotHubException, IOException
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
        Deencapsulation.setField(amqpSendHandler,"amqpResponse", mockedVerification );
        Deencapsulation.setField(amqpSendHandler, "linkOpenedRemotely", true);
        Deencapsulation.setField(amqpSendHandler, "sessionOpenedRemotely", true);
        Deencapsulation.setField(amqpSendHandler, "connectionOpenedRemotely", true);

        // Assert
        new Expectations()
        {
            {
                mockedVerification.getException();
                result = null;
            }
        };
        // Act
        amqpSendHandler.verifySendSucceeded();
    }

    //Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_25_031: [** The event handler shall get the exception from the response and throw is it is not null **]**
    @Test (expected = IotHubException.class)
    public void sendComplete_throws_exception_if_found(final @Mocked AmqpResponseVerification mockedVerification,
                                                       final @Mocked IotHubException mockedIotHubException) throws IotHubException, IOException
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
        Deencapsulation.setField(amqpSendHandler,"amqpResponse", mockedVerification );
        Deencapsulation.setField(amqpSendHandler, "linkOpenedRemotely", true);
        Deencapsulation.setField(amqpSendHandler, "sessionOpenedRemotely", true);
        Deencapsulation.setField(amqpSendHandler, "connectionOpenedRemotely", true);

        // Assert
        new Expectations()
        {
            {
                mockedVerification.getException();
                result = mockedIotHubException;
            }
        };
        // Act
        amqpSendHandler.verifySendSucceeded();

    }


    //Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_25_031: [** The event handler shall get the exception from the response and throw is it is not null **]**
    @Test (expected = IOException.class)
    public void sendComplete_throws_Connection_exception_if_found(final @Mocked AmqpResponseVerification mockedVerification,
                                                                  final @Mocked IotHubException mockedIotHubException,
                                                                  final @Mocked Event mockedEvent) throws IotHubException, IOException
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);
        amqpSendHandler.onTransportError(mockedEvent);

        // Act
        amqpSendHandler.verifySendSucceeded();

    }

    // Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_34_032: [This function shall close the transport tail]
    @Test
    public void onConnectionRemoteCloseClosesTransportTail(@Mocked final Event mockEvent, @Mocked final Transport mockTransport)
    {
        // arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);

        new Expectations()
        {
            {
                mockEvent.getTransport();
                result = mockTransport;
            }
        };

        // act
        amqpSendHandler.onConnectionRemoteClose(mockEvent);

        //assert
        new Verifications()
        {
            {
                mockTransport.close_tail();
                times = 1;
            }
        };
    }

    //Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_34_033: [This function shall set the variable 'verifyConnectionWasOpened' to true]
    @Test
    public void onConnectionRemoteOpenedFlagsVerifyConnectionOpened(@Mocked Event mockEvent)
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);

        // Act
        amqpSendHandler.onConnectionRemoteOpen(mockEvent);

        // Assert
        assertTrue(Deencapsulation.getField(amqpSendHandler, "connectionOpenedRemotely"));
    }

    //Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_34_034: [if 'verifyConnectionWasOpened' is false, or 'isConnectionError' is true, this function shall throw an IOException]
    @Test (expected = IOException.class)
    public void sendCompleteChecksForSavedException() throws IOException, IotHubException
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);

        Deencapsulation.setField(amqpSendHandler, "linkOpenedRemotely", true);
        Deencapsulation.setField(amqpSendHandler, "sessionOpenedRemotely", true);
        Deencapsulation.setField(amqpSendHandler, "connectionOpenedRemotely", true);
        Deencapsulation.setField(amqpSendHandler, "savedException", new SSLHandshakeException("some nonsense exception"));

        // Act
        amqpSendHandler.verifySendSucceeded();
    }

    //Tests_SRS_SERVICE_SDK_JAVA_AMQPSENDHANDLER_34_034: [if 'verifyConnectionWasOpened' is false, or 'isConnectionError' is true, this function shall throw an IOException]
    @Test (expected = IOException.class)
    public void sendCompleteChecksThatverifyConnectionOpened() throws IOException, IotHubException
    {
        // Arrange
        String hostName = "aaa";
        String userName = "bbb";
        String sasToken = "ccc";
        IotHubServiceClientProtocol iotHubServiceClientProtocol = IotHubServiceClientProtocol.AMQPS;
        AmqpSendHandler amqpSendHandler = new AmqpSendHandler(hostName, userName, sasToken, iotHubServiceClientProtocol);

        Deencapsulation.setField(amqpSendHandler, "connectionOpenedRemotely", false);
        Deencapsulation.setField(amqpSendHandler, "sessionOpenedRemotely", false);
        Deencapsulation.setField(amqpSendHandler, "linkOpenedRemotely", false);
        Deencapsulation.setField(amqpSendHandler, "savedException", null);

        // Act
        amqpSendHandler.verifySendSucceeded();
    }

    private void createProtonObjects()
    {
        String exceptionMessage = "Not expected function called";

        message = Proton.message();

        messageWithException = new Message()
        {
            @Override
            public boolean isDurable()
            { return false; }

            @Override
            public long getDeliveryCount()
            { return 0; }

            @Override
            public short getPriority()
            { return 0; }

            @Override
            public boolean isFirstAcquirer()
            { return false; }

            @Override
            public long getTtl()
            { return 0; }

            @Override
            public void setDurable(boolean b)
            { }

            @Override
            public void setTtl(long l)
            { }

            @Override public void setDeliveryCount(long l)
            { }

            @Override
            public void setFirstAcquirer(boolean b)
            { }

            @Override
            public void setPriority(short i)
            { }

            @Override
            public Object getMessageId()
            { return null; }

            @Override
            public long getGroupSequence()
            { return 0; }

            @Override
            public String getReplyToGroupId()
            { return null; }

            @Override
            public long getCreationTime()
            { return 0; }

            @Override
            public String getAddress()
            { return null; }

            @Override
            public byte[] getUserId()
            { return new byte[0]; }

            @Override
            public String getReplyTo()
            { return null; }

            @Override
            public String getGroupId()
            { return null; }

            @Override
            public String getContentType()
            { return null; }

            @Override
            public long getExpiryTime()
            { return 0; }

            @Override
            public Object getCorrelationId()
            { return null; }

            @Override
            public String getContentEncoding()
            { return null; }

            @Override
            public String getSubject()
            { return null; }

            @Override
            public void setGroupSequence(long l)
            { }

            @Override
            public void setUserId(byte[] bytes)
            { }

            @Override
            public void setCreationTime(long l)
            { }

            @Override
            public void setSubject(String s)
            { }

            @Override
            public void setGroupId(String s)
            { }

            @Override
            public void setAddress(String s)
            { }

            @Override
            public void setExpiryTime(long l)
            { }

            @Override
            public void setReplyToGroupId(String s)
            { }

            @Override
            public void setContentEncoding(String s)
            { }

            @Override
            public void setContentType(String s)
            { }

            @Override
            public void setReplyTo(String s)
            { }

            @Override
            public void setCorrelationId(Object o)
            { }

            @Override
            public void setMessageId(Object o)
            { }

            @Override
            public Header getHeader()
            { return null; }

            @Override
            public DeliveryAnnotations getDeliveryAnnotations()
            { return null; }

            @Override
            public MessageAnnotations getMessageAnnotations()
            { return null; }

            @Override
            public Properties getProperties()
            { return null; }

            @Override
            public ApplicationProperties getApplicationProperties()
            { return null; }

            @Override
            public Section getBody()
            { return null; }

            @Override
            public Footer getFooter()
            { return null; }

            @Override
            public void setHeader(Header header)
            { }

            @Override
            public void setDeliveryAnnotations(DeliveryAnnotations deliveryAnnotations)
            { }

            @Override
            public void setMessageAnnotations(MessageAnnotations messageAnnotations)
            { }

            @Override
            public void setProperties(Properties properties)
            { }

            @Override
            public void setApplicationProperties(ApplicationProperties applicationProperties)
            { }

            @Override
            public void setBody(Section section)
            { }

            @Override
            public void setFooter(Footer footer)
            { }

            @Override
            public int decode(byte[] bytes, int i, int i1)
            { return 0; }

            @Override
            public void decode(ReadableBuffer buf)
            { }

            @Override
            public int encode(byte[] bytes, int i, int i1)
            {
                if (exceptionCount == 0)
                {
                    exceptionCount++;
                    throw new BufferOverflowException();
                }
                else
                {
                    return 0;
                }
            }
            @Override
            public int encode(WritableBuffer buf)
            { return 0; }

            @Override
            public void clear()
            { }

            @Override
            public MessageError getError()
            { return null; }
        };

        sender = new Sender()
        {
            @Override
            public Record attachments()
            { return null; }

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
            public void offer(int i)
            { }

            @Override
            public int send(byte[] bytes, int i, int i1)
            { return 0; }

            @Override
            public int send(ReadableBuffer readableBuffer)
            {
                return 0;
            }

            @Override
            public int sendNoCopy(ReadableBuffer readableBuffer)
            {
                return 0;
            }

            @Override
            public void abort()
            { }

            @Override
            public String getName()
            { return null; }

            @Override
            public Delivery delivery(byte[] bytes)
            { return delivery; }

            @Override
            public Delivery delivery(byte[] bytes, int i, int i1)
            { return null; }

            @Override
            public Delivery head()
            { return null; }

            @Override
            public Delivery current()
            { return null; }

            @Override
            public boolean advance()
            { return false; }

            @Override
            public Source getSource()
            { return null; }

            @Override
            public org.apache.qpid.proton.amqp.transport.Target getTarget()
            { return null; }

            @Override
            public void setSource(Source source)
            { }

            @Override
            public void setTarget(org.apache.qpid.proton.amqp.transport.Target target)
            { }

            @Override
            public Source getRemoteSource()
            { return null; }

            @Override
            public org.apache.qpid.proton.amqp.transport.Target getRemoteTarget()
            { return null; }

            @Override
            public Link next(EnumSet<EndpointState> enumSet, EnumSet<EndpointState> enumSet1)
            { return null; }

            @Override
            public int getCredit()
            { return 1; }

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
        };

        event = new Event()
        {

            @Override
            public EventType getEventType()
            {
                return null;
            }

            @Override public Event.Type getType()
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
            { return sender; }

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
            { throw new UnsupportedOperationException(exceptionMessage); }

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
    }
}
