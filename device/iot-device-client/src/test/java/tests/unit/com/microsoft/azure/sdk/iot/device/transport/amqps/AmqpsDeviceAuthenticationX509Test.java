/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.ProductInfo;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import mockit.*;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.message.impl.MessageImpl;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Unit tests for AmqpsDeviceAuthenticationX509
 * methods: 100%
 * lines: 84%
 */
public class AmqpsDeviceAuthenticationX509Test
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

    @Mocked
    SslDomain mockSSLDomain;

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONX509_34_007: [This constructor shall call super with the provided user agent string.]
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
        AmqpsDeviceAuthenticationX509 actual = Deencapsulation.newInstance(AmqpsDeviceAuthenticationX509.class, mockDeviceClientConfig);

        //assert
        Map<Symbol, Object> amqpProperties = Deencapsulation.getField(actual, "amqpProperties");
        assertTrue(amqpProperties.containsValue(expectedUserAgentString));
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_001: [The constructor shall throw IllegalArgumentException if the  deviceClientConfig parameter is null.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullConfig()
    {
        new AmqpsDeviceAuthenticationX509(null);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_002: [The constructor shall save the deviceClientConfig parameter value to a member variable.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_003: [The constructor shall set both the sender and the receiver link state to OPENED.]
    @Test
    public void constructorSavesConfigAndSetsLinksToOpen()
    {
        //act
        AmqpsDeviceAuthenticationX509 auth = new AmqpsDeviceAuthenticationX509(mockDeviceClientConfig);

        //assert
        assertEquals(AmqpsDeviceOperationLinkState.OPENED, Deencapsulation.getField(auth, "amqpsSendLinkState"));
        assertEquals(AmqpsDeviceOperationLinkState.OPENED, Deencapsulation.getField(auth, "amqpsRecvLinkState"));
        assertEquals(mockDeviceClientConfig, Deencapsulation.getField(auth, "deviceClientConfig"));
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_004: [The function shall override the default behaviour and return null.]
    @Test
    public void sendMessageAndGetDeliveryHashReturnsNull()
    {
        //arrange
        AmqpsDeviceAuthenticationX509 auth = new AmqpsDeviceAuthenticationX509(mockDeviceClientConfig);

        //act
        AmqpsSendReturnValue result = Deencapsulation.invoke(auth, "sendMessageAndGetDeliveryTag",
                new Class[]{MessageType.class, byte[].class, int.class, int.class, byte[].class},
                null, null, 0, 0, null);

        //assert
        assertNull(result);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_005: [The function shall override the default behaviour and return null.]
    @Test
    public void getMessageFromReceiverLinkReturnsNull()
    {
        //arrange
        AmqpsDeviceAuthenticationX509 auth = new AmqpsDeviceAuthenticationX509(mockDeviceClientConfig);

        //act
        AmqpsSendReturnValue result = Deencapsulation.invoke(auth, "getMessageFromReceiverLink",
                new Class[]{String.class},
                "");

        //assert
        assertNull(result);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_006: [The function shall throw IllegalArgumentException if any of the input parameter is null.]
    @Test (expected = IllegalArgumentException.class)
    public void setSSLContextThrowsForNullTransport()
    {
        //arrange
        AmqpsDeviceAuthenticationX509 auth = new AmqpsDeviceAuthenticationX509(mockDeviceClientConfig);
        final Transport nullTransport = null;

        //act
        Deencapsulation.invoke(auth, "setSslDomain", new Class[]{Transport.class}, nullTransport);
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_010: [The function shall call the prototype class makeDomain function with the sslContext.]
    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_011: [The function shall set the domain on the transport.]
    @Test
    public void setSSLContextCallsMakeDomainAndSetsDomain() throws IOException, TransportException
    {
        //arrange
        final AmqpsDeviceAuthenticationX509 auth = new AmqpsDeviceAuthenticationX509(mockDeviceClientConfig);

        new StrictExpectations(auth)
        {
            {
                mockDeviceClientConfig.getAuthenticationProvider().getSSLContext();
                result = mockSSLContext;

                Deencapsulation.invoke(auth, "makeDomain", mockSSLContext);
                result = mockSSLDomain;
            }
        };

        //act
        Deencapsulation.invoke(auth, "setSslDomain", new Class[]{Transport.class}, mockTransport);

        //assert
        new Verifications()
        {
            {
                mockTransport.ssl(mockSSLDomain);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSDEVICEAUTHENTICATIONX509_12_012: [The function shall override the default behaviour and return true.]
    @Test
    public void isLinkFoundReturnsTrue()
    {
        //arrange
        final AmqpsDeviceAuthenticationX509 auth = new AmqpsDeviceAuthenticationX509(mockDeviceClientConfig);

        //act
        boolean result = Deencapsulation.invoke(auth, "isLinkFound", new Class[] {String.class}, "");

        //assert
        assertTrue(result);
    }
}
