// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.DiagnosticPropertyData;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;
import com.microsoft.azure.sdk.iot.device.MessageType;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * Unit test for Message class.
 * 88% methods, 91% lines covered
 */
public class MessageTest
{
    protected static Charset UTF8 = StandardCharsets.UTF_8;

    // Tests_SRS_MESSAGE_11_024: [The constructor shall save the message body.]
    // Tests_SRS_MESSAGE_11_002: [The function shall return the message body.]
    @Test
    public void constructorSavesBody()
    {
        final byte[] body = { 1, 2, 3 };

        Message msg = new Message(body);
        byte[] testBody = msg.getBytes();

        byte[] expectedBody = body;
        assertThat(testBody, is(expectedBody));
    }

    // Tests_SRS_MESSAGE_11_025: [If the message body is null, the constructor shall throw an IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullBody()
    {
        final byte[] body = null;

        new Message(body);
    }

    // Tests_SRS_MESSAGE_11_022: [The function shall return the message body, encoded using charset UTF-8.]
    @Test
    public void getBodyAsStringReturnsUtf8Body()
    {
        final byte[] body = { 0x61, 0x62, 0x63 };

        Message msg = new Message(body);
        String testBody = new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET);

        String expectedBody = new String(body, UTF8);
        assertThat(testBody, is(expectedBody));
    }

    // Tests_SRS_MESSAGE_11_026: [The function shall set the message property to the given value.]
    // Tests_SRS_MESSAGE_11_032: [The function shall return the value associated with the message property name, where the name can be either the HTTPS or AMQPS property name.]
    @Test
    public void setPropertyAndGetPropertyMatch(
            @Mocked final MessageProperty mockProperty)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final String name = "test-name";
        final String value1 = "test-value1";
        final String value2 = "test-value2";
        new NonStrictExpectations()
        {
            {
                new MessageProperty(name, value1);
                result = mockProperty;
                mockProperty.hasSameName(name);
                result = true;
                mockProperty.getValue();
                result = value1;
            }
        };

        Message msg = new Message(body);
        msg.setProperty(name, value1);

        String testValue = msg.getProperty(name);
        String expectedValue = value1;

        assertThat(testValue, is(expectedValue));

        new NonStrictExpectations()
        {
            {
                new MessageProperty(name, value2);
                result = mockProperty;
                mockProperty.hasSameName(name);
                result = true;
                mockProperty.getValue();
                result = value2;
            }
        };

        msg.setProperty(name, value2);
        testValue = msg.getProperty(name);
        expectedValue = value2;

        assertThat(testValue, is(expectedValue));
    }

    // Tests_SRS_MESSAGE_11_028: [If name is null, the function shall throw an IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
    public void setPropertyRejectsNullName()
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final String value = "test-value";

        Message msg = new Message(body);
        msg.setProperty(null, value);
    }

    // Tests_SRS_MESSAGE_11_029: [If value is null, the function shall throw an IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
    public void setPropertyRejectsNullValue()
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final String name = "test-name";

        Message msg = new Message(body);
        msg.setProperty(name, null);
    }

    // Tests_SRS_MESSAGE_11_030: [If name contains a character not specified in RFC 2047, the function shall throw an IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
    public void setPropertyRejectsIllegalName(
            @Mocked final MessageProperty mockProperty)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final String invalidName = "  ";
        final String value = "test-value";
        new NonStrictExpectations()
        {
            {
                new MessageProperty(invalidName, value);
                result = new IllegalArgumentException();
            }
        };

        Message msg = new Message(body);
        msg.setProperty(invalidName, value);
    }

    // Tests_SRS_MESSAGE_11_031: [If value name contains a character not specified in RFC 2047, the function shall throw an IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
    public void setPropertyRejectsIllegalValue(
            @Mocked final MessageProperty mockProperty)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final String name = "test-name";
        final String invalidValue = "test-value@";
        new NonStrictExpectations()
        {
            {
                new MessageProperty(name, invalidValue);
                result = new IllegalArgumentException();
            }
        };

        Message msg = new Message(body);
        msg.setProperty(name, invalidValue);
    }

    // Tests_SRS_MESSAGE_11_034: [If no value associated with the property name is found, the function shall return null.]
    @Test
    public void getPropertyRejectsNonexistentProperty(
            @Mocked final MessageProperty mockProperty)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final String name = "test-name";

        Message msg = new Message(body);
        String testValue= msg.getProperty(name);
		String expectedValue = null; // expected is null since test-name property doesn't exist
        assertThat(testValue, is(expectedValue));
    }

    // Tests_SRS_MESSAGE_11_033: [The function shall return a copy of the message properties.]
    @Test
    public void getPropertiesReturnsCopyOfProperties(
            @Mocked final MessageProperty mockProperty)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final String name = "test-name";
        final String value = "test-value";
        final String httpsName = "test-https-name";
        new NonStrictExpectations()
        {
            {
                new MessageProperty(name, value);
                result = mockProperty;
                mockProperty.hasSameName(name);
                result = true;
                mockProperty.getValue();
                result = value;
                mockProperty.getName();
                result = httpsName;
            }
        };

        Message msg = new Message(body);
        msg.setProperty(name, value);
        MessageProperty[] testProperties = msg.getProperties();

        int expectedNumProperties = 1;
        assertThat(testProperties.length, is(expectedNumProperties));
        assertThat(testProperties[0], is(not(mockProperty)));
    }

    // Tests_SRS_MESSAGE_15_035: [The function shall return true if the expiryTime is set to 0.]
    @Test
    public void isExpiredReturnsTrueIfExpiryIsNotSet()
    {
        final byte[] body = { 0x61, 0x62, 0x63 };

        Message msg = new Message(body);

        boolean actualResult = msg.isExpired();

        boolean expectedResult = false;
        assertThat(expectedResult, is(actualResult));
    }

    // Tests_SRS_MESSAGE_15_036: [The function shall return true if the current time is greater than the expiry time and false otherwise.]
    @Test
    public void isExpiredReturnsTrueIfCurrentTimeIsGreaterThanExpiryTime() throws InterruptedException
    {
        final byte[] body = { 0x61, 0x62, 0x63 };

        Message msg = new Message(body);
        msg.setExpiryTime(9);

        Thread.sleep(10);

        boolean actualResult = msg.isExpired();

        boolean expectedResult = true;
        assertThat(expectedResult, is(actualResult));
    }

    // Tests_SRS_MESSAGE_15_036: [The function shall return true if the current time is greater than the expiry time and false otherwise.]
    @Test
    public void isExpiredReturnsFalseIfCurrentTimeIsSmallerThanExpiryTime() throws InterruptedException
    {
        final byte[] body = { 0x61, 0x62, 0x63 };

        Message msg = new Message(body);
        msg.setExpiryTime(1000);

        boolean actualResult = msg.isExpired();

        boolean expectedResult = false;
        assertThat(expectedResult, is(actualResult));
    }

    // Tests_SRS_MESSAGE_34_037: [The function shall set the message's expiry time to be the number of milliseconds since the epoch provided in absoluteTimeout.]
    @Test
    public void setAbsoluteTimeSetsExpiryTime()
    {
        final Long absoluteExpiryTimeExpired = 1L;
        final Long absoluteExpiryTimeNotExpired = Long.MAX_VALUE;
        Message msg = new Message("body");

        msg.setAbsoluteExpiryTime(absoluteExpiryTimeExpired);
        assertTrue(msg.isExpired());

        msg.setAbsoluteExpiryTime(absoluteExpiryTimeNotExpired);
        assertFalse(msg.isExpired());
    }

    // Tests_SRS_MESSAGE_34_038: [If the provided absolute expiry time is negative, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void setAbsoluteTimeWithNegativeTimeThrowsIllegalArgumentException()
    {
        Message msg = new Message("body");
        msg.setAbsoluteExpiryTime(-1L);
    }

    // Tests_SRS_MESSAGE_34_047: [The function shall set the message's expiry time.]
    // Tests_SRS_MESSAGE_34_048: [The function shall set the message's message type.]
    // Tests_SRS_MESSAGE_34_046: [The function shall set the message's correlation ID to the provided value.]
    // Tests_SRS_MESSAGE_34_044: [The function shall set the message's message ID to the provided value.]
    // Tests_SRS_MESSAGE_34_050: [The function shall set the message's diagnosticPropertyData value.]
    // Tests_SRS_MESSAGE_34_049: [The function shall return the message's message type.]
    // Tests_SRS_MESSAGE_34_045: [The function shall return the message's correlation ID.]
    // Tests_SRS_MESSAGE_34_043: [The function shall return the message's message Id.]
    // Tests_SRS_MESSAGE_34_039: [The function shall return the message's DeliveryAcknowledgement.]
    // Tests_SRS_MESSAGE_34_037: [The function shall return the message's user ID.]
    // Tests_SRS_MESSAGE_34_041: [The function shall return the message's To value.]
    // Tests_SRS_MESSAGE_34_051: [The function shall return the message's diagnosticPropertyData value.]
    @Test
    public void testPropertyGettersAndSetters()
    {
        //arrange
        Message msg = new Message();
        MessageType type = MessageType.DEVICE_TELEMETRY;
        String messageId = "1234";
        String correlationId = "6789";
        String diagnosticId = "diag";
        String diagnosticCreationTimeUtc = "0000000000.000";
        DiagnosticPropertyData diagnosticPropertyData = new DiagnosticPropertyData(diagnosticId, diagnosticCreationTimeUtc);

        //act
        msg.setMessageType(type);
        msg.setCorrelationId(correlationId);
        msg.setMessageId(messageId);
        msg.setDiagnosticPropertyData(diagnosticPropertyData);

        //assert
        assertEquals(type, msg.getMessageType());
        assertEquals(correlationId, msg.getCorrelationId());
        assertEquals(messageId, msg.getMessageId());
        assertEquals(diagnosticPropertyData,msg.getDiagnosticPropertyData());
        assertNull(msg.getTo());
        assertNull(msg.getUserId());
        assertNull(msg.getDeliveryAcknowledgement());
    }
}