package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.ProductInfo;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import mockit.*;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.message.impl.MessageImpl;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for AmqpsDeviceAuthenticationCBSTest
 * 100% methods covered
 * 92% lines covered
 */
public class AmqpsDeviceAuthenticationCBSTest
{
    private final String CBS_TO = "$cbs";
    private final String CBS_REPLY = "cbs";

    private final String OPERATION_KEY = "operation";
    private final String TYPE_KEY = "type";
    private final String NAME_KEY = "name";

    private final String OPERATION_VALUE = "put-token";
    private final String TYPE_VALUE = "servicebus.windows.net:sastoken";

    private final String DEVICES_PATH =  "/devices/";

    @Mocked
    DeviceClientConfig mockDeviceClientConfig;

    @Mocked
    MessageImpl mockMessageImpl;

    @Mocked
    Properties mockProperties;

    @Mocked
    Map<String, String> mockMapStringString;

    @Mocked
    ApplicationProperties mockApplicationProperties;

    @Mocked
    Queue<MessageImpl> mockQueue;

    @Mocked
    Sasl mockSasl;

    @Mocked
    Transport mockTransport;

    @Mocked
    SSLContext mockSSLContext;

    @Mocked
    Sender mockSender;

    @Mocked
    AmqpsMessage mockAmqpsMessage;

    @Mocked
    MessageType mockMessageType;

    @Mocked
    Session mockSession;

    @Mocked
    Receiver mockReceiver;

    @Mocked
    Delivery mockDelivery;

    @Mocked
    UUID mockUUID;

    @Mocked
    Map<String, Integer> mockMapStringInteger;

    @Mocked
    Map.Entry<String, Integer> mockStringIntegerEntry;

    @Mocked
    Section mockSection;

    @Mocked
    ProductInfo mockedProductInfo;

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_34_050: [This constructor shall call super with the provided user agent string.]
    @Test
    public void constructorCallsSuperWithConfigUserAgentString()
    {
        //arrange
        final String expectedUserAgentString = "asdf";

        new NonStrictExpectations()
        {
            {
                mockDeviceClientConfig.getProductInfo();
                result = mockedProductInfo;

                mockedProductInfo.getUserAgentString();
                result = expectedUserAgentString;
            }
        };

        //act
        AmqpsDeviceAuthenticationCBS actual = Deencapsulation.newInstance(AmqpsDeviceAuthenticationCBS.class, mockDeviceClientConfig);

        //assert
        Map<Symbol, Object> amqpProperties = Deencapsulation.getField(actual, "amqpProperties");
        assertTrue(amqpProperties.containsValue(expectedUserAgentString));
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_023: [The function shall call the super to get the message.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_024: [The function shall set the message type to CBS authentication if the message is not null.]
    @Test
    public void getMessageFromReceiverLinkSuccess() throws IOException
    {
        //arrange
        final String linkName = "linkName";

        AmqpsDeviceAuthenticationCBS amqpsDeviceAuthenticationCBS = Deencapsulation.newInstance(AmqpsDeviceAuthenticationCBS.class, mockDeviceClientConfig);
        Deencapsulation.invoke(amqpsDeviceAuthenticationCBS, "openLinks", mockSession);
        Deencapsulation.setField(amqpsDeviceAuthenticationCBS, "receiverLink", mockReceiver);
        Deencapsulation.setField(amqpsDeviceAuthenticationCBS, "senderLink", mockSender);
        Deencapsulation.setField(amqpsDeviceAuthenticationCBS, "receiverLinkTag", linkName);

        new NonStrictExpectations()
        {
            {
                mockReceiver.current();
                result = mockDelivery;
                mockDelivery.isReadable();
                result = true;
                mockDelivery.isPartial();
                result = false;
                new AmqpsMessage();
                result = mockAmqpsMessage;
                mockAmqpsMessage.getAmqpsMessageType();
                result = MessageType.CBS_AUTHENTICATION;
            }
        };

        //act
        AmqpsMessage amqpsMessage = Deencapsulation.invoke(amqpsDeviceAuthenticationCBS, "getMessageFromReceiverLink", linkName);

        //assert
        assertNotNull(amqpsMessage);
        assertEquals(MessageType.CBS_AUTHENTICATION, amqpsMessage.getAmqpsMessageType());
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_025: [The function shall return the message.]
    @Test
    public void getMessageFromReceiverLinkSuperFailed() throws IOException
    {
        //arrange
        String linkName = "receiver";

        AmqpsDeviceAuthenticationCBS amqpsDeviceAuthenticationCBS = Deencapsulation.newInstance(AmqpsDeviceAuthenticationCBS.class, mockDeviceClientConfig);

        //act
        AmqpsMessage amqpsMessage = Deencapsulation.invoke(amqpsDeviceAuthenticationCBS, "getMessageFromReceiverLink", linkName);

        //assert
        assertNull(amqpsMessage);
        new Verifications()
        {
            {
                mockAmqpsMessage.setAmqpsMessageType(mockMessageType);
                times = 0;
            }
        };
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_027: [The function shall read the correlationId property and compare it to the given correlationId and if they are different return false.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_028: [The function shall read the application properties and if the status code property is not 200 return false.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_029: [The function shall return true If both the correlationID and status code matches.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_34_035: [If the correlationId and status code matches, this function will acknowledge the provided amqpsMessage.]
    @Test
    public void authenticationMessageReceivedSuccess() throws IOException
    {
        //arrange
        final String propertyKey = "status-code";
        final Integer propertyValue = 200;
        new NonStrictExpectations()
        {
            {
                mockAmqpsMessage.getProperties();
                result = mockProperties;
                mockAmqpsMessage.getApplicationProperties();
                result = mockApplicationProperties;
                mockProperties.getCorrelationId();
                result = mockUUID;
                mockUUID.equals(any);
                result = true;
                mockApplicationProperties.getValue();
                result = mockMapStringInteger;
                mockMapStringInteger.entrySet();
                result = mockStringIntegerEntry;
                mockStringIntegerEntry.getKey();
                result = propertyKey;
                mockStringIntegerEntry.getValue();
                result = propertyValue;
            }
        };

        AmqpsDeviceAuthenticationCBS amqpsDeviceAuthenticationCBS = Deencapsulation.newInstance(AmqpsDeviceAuthenticationCBS.class, mockDeviceClientConfig);

        //act
        Boolean isAuthenticated = Deencapsulation.invoke(amqpsDeviceAuthenticationCBS, "authenticationMessageReceived", mockAmqpsMessage, mockUUID);

        //assert
        assertTrue(isAuthenticated);
        new Verifications()
        {
            {
                mockAmqpsMessage.acknowledge(AmqpsMessage.ACK_TYPE.COMPLETE);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_026: [The function shall return false if the amqpdMessage parameter is null or does not have Properties and Application properties.]
    @Test
    public void authenticationMessageReceivePropertiesNull() throws IOException
    {
        //arrange
        final String propertyKey = "status-code";
        final Integer propertyValue = 200;
        new NonStrictExpectations()
        {
            {
                mockAmqpsMessage.getProperties();
                result = null;
            }
        };

        AmqpsDeviceAuthenticationCBS amqpsDeviceAuthenticationCBS = Deencapsulation.newInstance(AmqpsDeviceAuthenticationCBS.class, mockDeviceClientConfig);

        //act
        Boolean isAuthenticated = Deencapsulation.invoke(amqpsDeviceAuthenticationCBS, "authenticationMessageReceived", mockAmqpsMessage, mockUUID);

        //assert
        assertFalse(isAuthenticated);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_026: [The function shall return false if the amqpdMessage parameter is null or does not have Properties and Application properties.]
    @Test
    public void authenticationMessageReceiveApplicationPropertiesNull() throws IOException
    {
        //arrange
        final String propertyKey = "status-code";
        final Integer propertyValue = 200;
        new NonStrictExpectations()
        {
            {
                mockAmqpsMessage.getProperties();
                result = mockProperties;
                mockAmqpsMessage.getApplicationProperties();
                result = null;
            }
        };

        AmqpsDeviceAuthenticationCBS amqpsDeviceAuthenticationCBS = Deencapsulation.newInstance(AmqpsDeviceAuthenticationCBS.class, mockDeviceClientConfig);

        //act
        Boolean isAuthenticated = Deencapsulation.invoke(amqpsDeviceAuthenticationCBS, "authenticationMessageReceived", mockAmqpsMessage, mockUUID);

        //assert
        assertFalse(isAuthenticated);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_029: [The function shall return true If both the correlationID and status code matches.]
    @Test
    public void authenticationMessageReceiveUUIDMismatch() throws IOException
    {
        //arrange
        final String propertyKey = "status-code";
        final Integer propertyValue = 200;
        new NonStrictExpectations()
        {
            {
                mockAmqpsMessage.getProperties();
                result = mockProperties;
                mockAmqpsMessage.getApplicationProperties();
                result = mockApplicationProperties;
                mockProperties.getCorrelationId();
                result = mockUUID;
                mockUUID.equals(any);
                result = false;
            }
        };

        AmqpsDeviceAuthenticationCBS amqpsDeviceAuthenticationCBS = Deencapsulation.newInstance(AmqpsDeviceAuthenticationCBS.class, mockDeviceClientConfig);

        //act
        Boolean isAuthenticated = Deencapsulation.invoke(amqpsDeviceAuthenticationCBS, "authenticationMessageReceived", mockAmqpsMessage, mockUUID);

        //assert
        assertFalse(isAuthenticated);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_029: [The function shall return true If both the correlationID and status code matches.]
    @Test
    public void authenticationMessageReceive404() throws IOException
    {
        //arrange
        final String propertyKey = "status-code";
        final Integer propertyValue = 404;
        new NonStrictExpectations()
        {
            {
                mockAmqpsMessage.getProperties();
                result = mockProperties;
                mockAmqpsMessage.getApplicationProperties();
                result = mockApplicationProperties;
                mockProperties.getCorrelationId();
                result = mockUUID;
                mockUUID.equals(any);
                result = true;
                mockApplicationProperties.getValue();
                result = mockMapStringInteger;
                mockMapStringInteger.entrySet();
                result = mockStringIntegerEntry;
                mockStringIntegerEntry.getKey();
                result = propertyKey;
                mockStringIntegerEntry.getValue();
                result = propertyValue;
            }
        };

        AmqpsDeviceAuthenticationCBS amqpsDeviceAuthenticationCBS = Deencapsulation.newInstance(AmqpsDeviceAuthenticationCBS.class, mockDeviceClientConfig);

        //act
        Boolean isAuthenticated = Deencapsulation.invoke(amqpsDeviceAuthenticationCBS, "authenticationMessageReceived", mockAmqpsMessage, mockUUID);

        //assert
        assertFalse(isAuthenticated);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_011: [The function shall set get the sasl layer from the transport.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_012: [The function shall set the sasl mechanism to PLAIN.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_013: [The function shall set the SslContext on the domain.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_014: [The function shall set the domain on the transport.]
    @Test
    public void setSslDomain()
    {
        // arrange
        final AmqpsDeviceAuthenticationCBS amqpsDeviceAuthenticationCBS = new AmqpsDeviceAuthenticationCBS(mockDeviceClientConfig);

        new NonStrictExpectations()
        {
            {
                mockTransport.sasl();
                result = mockSasl;
            }
        };

        Deencapsulation.invoke(amqpsDeviceAuthenticationCBS, "setSslDomain", mockTransport);
        // act

        // assert
        new Verifications()
        {
            {
                mockTransport.sasl();
                times = 1;
                mockSasl.setMechanisms("ANONYMOUS");
                times = 1;
                mockTransport.ssl((SslDomain)any);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_030: [The function shall create a CBS authentication message using the device configuration and the correlationID.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_031: [The function shall set the CBS related properties on the message.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_032: [The function shall set the CBS related application properties on the message.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_033: [The function shall set the the SAS token to the message body.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_034: [THe function shall put the message into the waiting queue.]
    @Test
    public void authenticateSuccess()
    {
        // arrange
        final String CBS_TO = "$cbs";
        final String CBS_REPLY = "cbs";
        final String OPERATION_KEY = "operation";
        final String OPERATION_VALUE = "put-token";
        final String TYPE_KEY = "type";
        final String NAME_KEY = "name";

        final AmqpsDeviceAuthenticationCBS amqpsDeviceAuthenticationCBS = new AmqpsDeviceAuthenticationCBS(mockDeviceClientConfig);

        new NonStrictExpectations()
        {
            {
                mockMessageImpl = (MessageImpl) Proton.message();
                new Properties();
                result = mockProperties;
                mockMapStringString = new HashMap<>(3);
            }
        };

        Deencapsulation.invoke(amqpsDeviceAuthenticationCBS, "openLinks", mockSession);

        // act
        Deencapsulation.invoke(amqpsDeviceAuthenticationCBS, "authenticate", mockDeviceClientConfig, mockUUID);

        // assert
        new Verifications()
        {
            {
                Proton.message();
                times = 1;
                mockProperties.setMessageId(mockUUID);
                times = 1;
                mockProperties.setTo(CBS_TO);
                times = 1;
                mockProperties.setReplyTo(CBS_REPLY);
                times = 1;
                mockMessageImpl.setProperties(mockProperties);
                times = 1;
                new HashMap<>(3);
                times = 1;
                mockMapStringString.put(OPERATION_KEY, OPERATION_VALUE);
                times = 1;
                mockMapStringString.put(TYPE_KEY, TYPE_VALUE);
                times = 1;
                mockMapStringString.put(NAME_KEY, NAME_KEY + mockDeviceClientConfig.getIotHubHostname() + DEVICES_PATH + mockDeviceClientConfig.getDeviceId());
                times = 1;
                new ApplicationProperties((Map) any);
                times = 1;
                mockMessageImpl.setApplicationProperties((ApplicationProperties) any);
                times = 1;
                mockMessageImpl.setBody((Section) any);
                times = 1;
            }
        };
    }



    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_020: [The function shall return true and set the sendLinkState to OPENED if the senderLinkTag is equal to the given linkName.]
    @Test
    public void isLinkFoundSendTrue()
    {
        // arrange
        final AmqpsDeviceAuthenticationCBS amqpsDeviceAuthenticationCBS = new AmqpsDeviceAuthenticationCBS(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsDeviceAuthenticationCBS, "amqpsSendLinkState", AmqpsDeviceOperationLinkState.UNKNOWN);
        Deencapsulation.setField(amqpsDeviceAuthenticationCBS, "amqpsRecvLinkState", AmqpsDeviceOperationLinkState.UNKNOWN);

        final String linkName = "linkName";

        Deencapsulation.setField(amqpsDeviceAuthenticationCBS, "senderLinkTag", linkName);

        // act
        Boolean isFound = Deencapsulation.invoke(amqpsDeviceAuthenticationCBS, "isLinkFound", linkName);

        // assert
        assertTrue(isFound);

        AmqpsDeviceOperationLinkState actualsendState = Deencapsulation.getField(amqpsDeviceAuthenticationCBS, "amqpsSendLinkState");
        AmqpsDeviceOperationLinkState actualrecvState = Deencapsulation.getField(amqpsDeviceAuthenticationCBS, "amqpsRecvLinkState");
        assertEquals(AmqpsDeviceOperationLinkState.OPENED, actualsendState);
        assertEquals(AmqpsDeviceOperationLinkState.UNKNOWN, actualrecvState);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_021: [The function shall return true and set the recvLinkState to OPENED if the receiverLinkTag is equal to the given linkName.]
    @Test
    public void isLinkFoundRecvTrue()
    {
        // arrange
        final AmqpsDeviceAuthenticationCBS amqpsDeviceAuthenticationCBS = new AmqpsDeviceAuthenticationCBS(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsDeviceAuthenticationCBS, "amqpsSendLinkState", AmqpsDeviceOperationLinkState.UNKNOWN);
        Deencapsulation.setField(amqpsDeviceAuthenticationCBS, "amqpsRecvLinkState", AmqpsDeviceOperationLinkState.UNKNOWN);

        final String linkName = "linkName";

        Deencapsulation.setField(amqpsDeviceAuthenticationCBS, "receiverLinkTag", linkName);

        // act
        Boolean isFound = Deencapsulation.invoke(amqpsDeviceAuthenticationCBS, "isLinkFound", linkName);

        // assert
        assertTrue(isFound);

        AmqpsDeviceOperationLinkState actualsendState = Deencapsulation.getField(amqpsDeviceAuthenticationCBS, "amqpsSendLinkState");
        AmqpsDeviceOperationLinkState actualrecvState = Deencapsulation.getField(amqpsDeviceAuthenticationCBS, "amqpsRecvLinkState");
        assertEquals(AmqpsDeviceOperationLinkState.UNKNOWN, actualsendState);
        assertEquals(AmqpsDeviceOperationLinkState.OPENED, actualrecvState);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_022: [The function shall return false if neither the senderLinkTag nor the receiverLinkTag is matcing with the given linkName.]
    @Test
    public void isLinkFoundFalse()
    {
        // arrange
        final AmqpsDeviceAuthenticationCBS amqpsDeviceAuthenticationCBS = new AmqpsDeviceAuthenticationCBS(mockDeviceClientConfig);
        Deencapsulation.setField(amqpsDeviceAuthenticationCBS, "amqpsSendLinkState", AmqpsDeviceOperationLinkState.UNKNOWN);
        Deencapsulation.setField(amqpsDeviceAuthenticationCBS, "amqpsRecvLinkState", AmqpsDeviceOperationLinkState.UNKNOWN);

        final String linkName = "linkName";

        // act
        Boolean isFound = Deencapsulation.invoke(amqpsDeviceAuthenticationCBS, "isLinkFound", linkName);

        // assert
        assertFalse(isFound);

        AmqpsDeviceOperationLinkState actualsendState = Deencapsulation.getField(amqpsDeviceAuthenticationCBS, "amqpsSendLinkState");
        AmqpsDeviceOperationLinkState actualrecvState = Deencapsulation.getField(amqpsDeviceAuthenticationCBS, "amqpsRecvLinkState");
        assertEquals(AmqpsDeviceOperationLinkState.UNKNOWN, actualsendState);
        assertEquals(AmqpsDeviceOperationLinkState.UNKNOWN, actualrecvState);
    }
}
