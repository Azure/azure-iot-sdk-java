// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsResponse;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsSingleMessage;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.azure.sdk.iot.device.transport.https.HttpsMessage.HTTPS_APP_PROPERTY_PREFIX;
import static com.microsoft.azure.sdk.iot.device.transport.https.HttpsMessage.HTTPS_SYSTEM_PROPERTY_PREFIX;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;


/* Unit tests for HttpsSingleMessage.
* 100% methods covered
* 98% lines covered
*/
public class HttpsSingleMessageTest
{
    // Tests_SRS_HTTPSSINGLEMESSAGE_11_001: [The parsed HttpsSingleMessage shall have a copy of the original message body as its body.]
    @Test
    public void parseHttpsMessageFromMessageCopiesBody(
            @Mocked final Message mockMsg,
            @Mocked final MessageProperty mockProperty)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final MessageProperty[] properties = { mockProperty };
        final String propertyName = "test-property-name";
        final String propertyValue = "test-property-value";
        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
                result = body;
                mockMsg.getProperties();
                result = properties;
                mockProperty.getName();
                result = propertyName;
                mockProperty.getValue();
                result = propertyValue;
            }
        };

        HttpsSingleMessage httpsMsg =
                HttpsSingleMessage.parseHttpsMessage(mockMsg);
        byte[] testBody = httpsMsg.getBody();

        byte[] expectedBody = body;
        assertThat(testBody, is(expectedBody));

        expectedBody[0] = 0x34;
        assertThat(testBody, is(not(expectedBody)));
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_21_002: [The parsed HttpsSingleMessage shall set the contentType as `binary/octet-stream`.]
    @Test
    public void parseHttpsMessageFromMessageSetContentType(
            @Mocked final Message mockMsg,
            @Mocked final MessageProperty mockProperty)
    {
        // arrange
        final byte[] body = { 0x61, 0x62, 0x63 };
        final MessageProperty[] properties = { mockProperty };
        final String propertyName = "test-property-name";
        final String propertyValue = "test-property-value";
        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
                result = body;
                mockMsg.getProperties();
                result = properties;
                mockProperty.getName();
                result = propertyName;
                mockProperty.getValue();
                result = propertyValue;
            }
        };

        // act
        HttpsSingleMessage httpsMsg =
                HttpsSingleMessage.parseHttpsMessage(mockMsg);

        // assert
        String testContentType = httpsMsg.getContentType();
        assertThat(testContentType, is("binary/octet-stream"));
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_11_003: [The parsed HttpsSingleMessage shall add the prefix 'iothub-app-' to each of the message properties.]
    @Test
    public void parseHttpsMessageFromMessageSavesPropertiesWithPrefix(
            @Mocked final Message mockMsg,
            @Mocked final MessageProperty mockProperty)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final MessageProperty[] properties = { mockProperty };
        final String propertyName = "test-property-name";
        final String propertyValue = "test-property-value";
        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
                result = body;
                mockMsg.getProperties();
                result = properties;
                mockProperty.getName();
                result = propertyName;
                mockProperty.getValue();
                result = propertyValue;
            }
        };

        HttpsSingleMessage.parseHttpsMessage(mockMsg);

        final String expectedPrefix = "iothub-app-";
        final String expectedPropertyName = expectedPrefix + propertyName;
        final String expectedPropertyValue = propertyValue;
        new Verifications()
        {
            {
                new MessageProperty(expectedPropertyName,
                        expectedPropertyValue);
            }
        };
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_11_004: [The parsed HttpsSingleMessage shall have a copy of the original response body as its body.]
    @Test
    public void parseHttpsMessageFromResponseCopiesBody(
            @Mocked final HttpsResponse mockResponse,
            @Mocked final MessageProperty mockProperty)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final Map<String, String> headerFields = new HashMap<>();
        final String propertyName = "test-property-name";
        final String propertyValue = "test-property-value";
        headerFields.put(propertyName, propertyValue);
        new NonStrictExpectations()
        {
            {
                mockResponse.getBody();
                result = body;
                mockResponse.getHeaderFields();
                result = headerFields;
            }
        };

        HttpsSingleMessage httpsMsg =
                HttpsSingleMessage.parseHttpsMessage(mockResponse);
        byte[] testBody = httpsMsg.getBody();

        byte[] expectedBody = body;
        assertThat(testBody, is(expectedBody));

        expectedBody[0] = 0x34;
        assertThat(testBody, is(not(expectedBody)));
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_34_014: [If the message contains a system property, the parsed HttpsSingleMessage shall add the corresponding property with property value]
    @Test
    public void parseHttpsMessageFromMessageWithMessageId(
            @Mocked final Message mockMsg,
            @Mocked final MessageProperty mockProperty)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final MessageProperty[] properties = { mockProperty };
        final String messageidName = HTTPS_SYSTEM_PROPERTY_PREFIX + "messageid";
        final String messageidValue = "test_messageid-value";
        final String correlationidName = HTTPS_SYSTEM_PROPERTY_PREFIX + "correlationid";
        final String correlationidValue = "1234";
        final String useridName = HTTPS_SYSTEM_PROPERTY_PREFIX + "userid";
        final String useridValue = "3456";
        final String toName = HTTPS_SYSTEM_PROPERTY_PREFIX + "to";
        final String toValue = "device4";

        final String propertyName = "test-property-name";
        final String propertyValue = "test-property-value";
        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
                result = body;
                mockMsg.getProperties();
                result = properties;
                mockMsg.getMessageId();
                result = messageidValue;
                mockProperty.getName();
                result = propertyName;
                mockProperty.getValue();
                result = propertyValue;
                mockMsg.getCorrelationId();
                result = correlationidValue;
                mockMsg.getUserId();
                result = useridValue;
                mockMsg.getTo();
                result = toValue;
            }
        };

        HttpsSingleMessage httpsSingleMessage = HttpsSingleMessage.parseHttpsMessage(mockMsg);

        assertTrue(systemPropertyAssignedCorrectly(httpsSingleMessage.getSystemProperties(), messageidName, messageidValue));
        assertTrue(systemPropertyAssignedCorrectly(httpsSingleMessage.getSystemProperties(), correlationidName, correlationidValue));
        assertTrue(systemPropertyAssignedCorrectly(httpsSingleMessage.getSystemProperties(), useridName, useridValue));
        assertTrue(systemPropertyAssignedCorrectly(httpsSingleMessage.getSystemProperties(), toName, toValue));
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_21_016: [The parsed HttpsSingleMessage shall have a copy of the original message body as its body.]
    @Test
    public void parseHttpsJsonMessageFromMessageCopiesBody(
            @Mocked final Message mockMsg,
            @Mocked final MessageProperty mockProperty)
    {
        // arrange
        final byte[] body = { 0x61, 0x62, 0x63 };
        final MessageProperty[] properties = { mockProperty };
        final String propertyName = "test-property-name";
        final String propertyValue = "test-property-value";
        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
                result = body;
                mockMsg.getProperties();
                result = properties;
                mockProperty.getName();
                result = propertyName;
                mockProperty.getValue();
                result = propertyValue;
            }
        };

        // act
        HttpsSingleMessage httpsMsg =
                HttpsSingleMessage.parseHttpsJsonMessage(mockMsg);

        // assert
        byte[] testBody = httpsMsg.getBody();

        byte[] expectedBody = body;
        assertThat(testBody, is(expectedBody));

        expectedBody[0] = 0x34;
        assertThat(testBody, is(not(expectedBody)));
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_21_017: [The parsed HttpsSingleMessage shall set the contentType as `application/json;charset=utf-8`.]
    @Test
    public void parseHttpsJsonMessageFromMessageSetContentType(
            @Mocked final Message mockMsg,
            @Mocked final MessageProperty mockProperty)
    {
        // arrange
        final byte[] body = { 0x61, 0x62, 0x63 };
        final MessageProperty[] properties = { mockProperty };
        final String propertyName = "test-property-name";
        final String propertyValue = "test-property-value";
        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
                result = body;
                mockMsg.getProperties();
                result = properties;
                mockProperty.getName();
                result = propertyName;
                mockProperty.getValue();
                result = propertyValue;
            }
        };

        // act
        HttpsSingleMessage httpsMsg =
                HttpsSingleMessage.parseHttpsJsonMessage(mockMsg);

        // assert
        String testContentType = httpsMsg.getContentType();
        assertThat(testContentType, is("application/json;charset=utf-8"));
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_21_018: [The parsed HttpsSingleMessage shall add the prefix 'iothub-app-' to each of the message properties.]
    @Test
    public void parseHttpsJsonMessageFromMessageSavesPropertiesWithPrefix(
            @Mocked final Message mockMsg,
            @Mocked final MessageProperty mockProperty)
    {
        // arrange
        final byte[] body = { 0x61, 0x62, 0x63 };
        final MessageProperty[] properties = { mockProperty };
        final String propertyName = "test-property-name";
        final String propertyValue = "test-property-value";
        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
                result = body;
                mockMsg.getProperties();
                result = properties;
                mockProperty.getName();
                result = propertyName;
                mockProperty.getValue();
                result = propertyValue;
            }
        };

        // act
        HttpsSingleMessage.parseHttpsJsonMessage(mockMsg);

        // assert
        final String expectedPrefix = "iothub-app-";
        final String expectedPropertyName = expectedPrefix + propertyName;
        final String expectedPropertyValue = propertyValue;
        new Verifications()
        {
            {
                new MessageProperty(expectedPropertyName,
                        expectedPropertyValue);
            }
        };
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_34_019: [If the message contains a system property, the parsed HttpsSingleMessage shall add the corresponding property with property value.]
    @Test
    public void parseHttpsJsonMessageFromMessageWithMessageId(
            @Mocked final Message mockMsg,
            @Mocked final MessageProperty mockProperty)
    {
        // arrange
        final byte[] body = { 0x61, 0x62, 0x63 };
        final MessageProperty[] properties = { mockProperty };
        final String messageidName = HTTPS_SYSTEM_PROPERTY_PREFIX + "messageid";
        final String messageidValue = "test_messageid-value";
        final String correlationidName = HTTPS_SYSTEM_PROPERTY_PREFIX + "correlationid";
        final String correlationidValue = "1234";
        final String useridName = HTTPS_SYSTEM_PROPERTY_PREFIX + "userid";
        final String useridValue = "3456";
        final String toName = HTTPS_SYSTEM_PROPERTY_PREFIX + "to";
        final String toValue = "device4";
        final String contentEncodingName = HTTPS_SYSTEM_PROPERTY_PREFIX + "contentencoding";
        final String contentEncodingValue = "test_contentencoding-value";
        final String contentTypeName = HTTPS_SYSTEM_PROPERTY_PREFIX + "contenttype";
        final String contentTypeValue = "test_contenttype-value";
        final String propertyName = "test-property-name";
        final String propertyValue = "test-property-value";
        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
                result = body;
                mockMsg.getProperties();
                result = properties;
                mockMsg.getMessageId();
                result = messageidValue;
                mockProperty.getName();
                result = propertyName;
                mockProperty.getValue();
                result = propertyValue;
                mockMsg.getCorrelationId();
                result = correlationidValue;
                mockMsg.getUserId();
                result = useridValue;
                mockMsg.getTo();
                result = toValue;
                mockMsg.getContentType();
                result = contentTypeValue;
                mockMsg.getContentEncoding();
                result = contentEncodingValue;
            }
        };

        // act
        HttpsSingleMessage httpsSingleMessage = HttpsSingleMessage.parseHttpsJsonMessage(mockMsg);

        // assert
        assertTrue(systemPropertyAssignedCorrectly(httpsSingleMessage.getSystemProperties(), messageidName, messageidValue));
        assertTrue(systemPropertyAssignedCorrectly(httpsSingleMessage.getSystemProperties(), correlationidName, correlationidValue));
        assertTrue(systemPropertyAssignedCorrectly(httpsSingleMessage.getSystemProperties(), useridName, useridValue));
        assertTrue(systemPropertyAssignedCorrectly(httpsSingleMessage.getSystemProperties(), toName, toValue));
        assertTrue(systemPropertyAssignedCorrectly(httpsSingleMessage.getSystemProperties(), contentTypeName, contentTypeValue));
        assertTrue(systemPropertyAssignedCorrectly(httpsSingleMessage.getSystemProperties(), contentEncodingName, contentEncodingValue));
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_11_005: [The parsed HttpsSingleMessage shall not be Base64-encoded.]
    @Test
    public void parseHttpsMessageFromResponseDoesNotBase64EncodeBody(
            @Mocked final HttpsResponse mockResponse,
            @Mocked final MessageProperty mockProperty)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final Map<String, String> headerFields = new HashMap<>();
        final String propertyName = "test-property-name";
        final String propertyValue = "test-property-value";
        headerFields.put(propertyName, propertyValue);
        new NonStrictExpectations()
        {
            {
                mockResponse.getBody();
                result = body;
                mockResponse.getHeaderFields();
                result = headerFields;
            }
        };

        HttpsSingleMessage httpsMsg =
                HttpsSingleMessage.parseHttpsMessage(mockResponse);
        boolean testBase64Encoded = httpsMsg.isBase64Encoded();

        boolean expectedBase64Encoded = false;
        assertThat(testBase64Encoded, is(expectedBase64Encoded));
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_11_006: [The parsed HttpsSingleMessage shall include all valid HTTPS application-defined properties in the response header as message properties.]
    @Test
    public void parseHttpsMessageFromResponseDoesNotIncludeNonAppProperties(
            @Mocked final HttpsResponse mockResponse,
            @Mocked final MessageProperty mockProperty)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final Map<String, String> headerFields = new HashMap<>();
        final String propertyName = "test-property-name";
        final String propertyValue = "test-property-value";
        headerFields.put(propertyName, propertyValue);
        new NonStrictExpectations()
        {
            {
                mockResponse.getBody();
                result = body;
                mockResponse.getHeaderFields();
                result = headerFields;
            }
        };

        HttpsSingleMessage httpsMsg =
                HttpsSingleMessage.parseHttpsMessage(mockResponse);
        MessageProperty[] testProperties = httpsMsg.getProperties();

        MessageProperty[] expectedProperties = {};
        assertThat(testProperties, is(expectedProperties));
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_11_006: [The parsed HttpsSingleMessage shall include all valid HTTPS application-defined properties in the response header as message properties.]
    @Test
    public void parseHttpsMessageFromResponseIncludesAppProperties(
            @Mocked final HttpsResponse mockResponse,
            @Mocked final MessageProperty mockProperty)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final Map<String, String> headerFields = new HashMap<>();
        final String propertyName = "iothub-app-test-property-name";
        final String propertyValue = "test-property-value";
        headerFields.put(propertyName, propertyValue);
        new NonStrictExpectations()
        {
            {
                mockResponse.getBody();
                result = body;
                mockResponse.getHeaderFields();
                result = headerFields;
                MessageProperty.isValidAppProperty(
                        propertyName, propertyValue);
                result = true;
            }
        };

        HttpsSingleMessage.parseHttpsMessage(mockResponse);

        new Verifications()
        {
            {
                new MessageProperty(propertyName, propertyValue);
            }
        };
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_11_007: [The function shall return an IoT Hub message with a copy of the message body as its body.]
    @Test
    public void toMessageCopiesBody(@Mocked final HttpsResponse mockResponse,
            @Mocked final MessageProperty mockProperty,
            @Mocked final Message mockMsg)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final Map<String, String> headerFields = new HashMap<>();
        final String propertyName = "iothub-app-test-property-name";
        final String propertyValue = "test-property-value";
        headerFields.put(propertyName, propertyValue);
        new Expectations()
        {
            {
                mockResponse.getBody();
                result = body;
                mockResponse.getHeaderFields();
                result = headerFields;
                MessageProperty.isValidAppProperty(
                        propertyName, propertyValue);
                result = true;
                new MessageProperty(propertyName, propertyValue);
                result = mockProperty;
                mockProperty.getName();
                result = propertyName;
                mockProperty.getValue();
                result = propertyValue;
                new Message(body);
                result = mockMsg;
            }
        };

        HttpsSingleMessage httpsMsg =
                HttpsSingleMessage.parseHttpsMessage(mockResponse);
        httpsMsg.toMessage();

        final byte[] expectedBody = body;
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_11_008: [The function shall return an IoT Hub message with application-defined properties that have the prefix 'iothub-app' removed.]
    @Test
    public void toMessageRemovesPrefixFromProperties(
            @Mocked final HttpsResponse mockResponse,
            @Mocked final MessageProperty mockProperty,
            @Mocked final Message mockMsg)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final Map<String, String> headerFields = new HashMap<>();
        final String propertyName = "iothub-app-test-property-name";
        final String propertyValue = "test-property-value";
        headerFields.put(propertyName, propertyValue);
        new NonStrictExpectations()
        {
            {
                mockResponse.getBody();
                result = body;
                mockResponse.getHeaderFields();
                result = headerFields;
                MessageProperty.isValidAppProperty(
                        propertyName, propertyValue);
                result = true;
                new MessageProperty(propertyName, propertyValue);
                result = mockProperty;
                mockProperty.getName();
                result = propertyName;
                mockProperty.getValue();
                result = propertyValue;
                new Message(body);
                result = mockMsg;
            }
        };

        HttpsSingleMessage httpsMsg =
                HttpsSingleMessage.parseHttpsMessage(mockResponse);
        httpsMsg.toMessage();

        final String expectedPropertyName = "test-property-name";
        final String expectedPropertyValue = propertyValue;
        new Verifications()
        {
            {
                mockMsg.setProperty(expectedPropertyName,
                        expectedPropertyValue);
            }
        };
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_11_009: [The function shall return a copy of the message body.]
    @Test
    public void getBodyReturnsCopyOfBody(
            @Mocked final Message mockMsg,
            @Mocked final MessageProperty mockProperty)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final MessageProperty[] properties = { mockProperty };
        final String propertyName = "test-property-name";
        final String propertyValue = "test-property-value";
        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
                result = body;
                mockMsg.getProperties();
                result = properties;
                mockProperty.getName();
                result = propertyName;
                mockProperty.getValue();
                result = propertyValue;
            }
        };

        HttpsSingleMessage httpsMsg =
                HttpsSingleMessage.parseHttpsMessage(mockMsg);
        byte[] testBody = httpsMsg.getBody();

        byte[] expectedBody = body;
        assertThat(testBody, is(expectedBody));

        testBody[0] = 0x34;
        testBody = httpsMsg.getBody();
        assertThat(testBody, is(expectedBody));
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_11_010: [The function shall return the message body as a string encoded using charset UTF-8.]
    @Test
    public void getBodyAsStringsReturnsUtf8Body(
            @Mocked final Message mockMsg,
            @Mocked final MessageProperty mockProperty)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final MessageProperty[] properties = { mockProperty };
        final String propertyName = "test-property-name";
        final String propertyValue = "test-property-value";
        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
                result = body;
                mockMsg.getProperties();
                result = properties;
                mockProperty.getName();
                result = propertyName;
                mockProperty.getValue();
                result = propertyValue;
            }
        };

        HttpsSingleMessage httpsMsg =
                HttpsSingleMessage.parseHttpsMessage(mockMsg);
        String testBody = httpsMsg.getBodyAsString();

        String expectedBody = "abc";
        assertThat(testBody, is(expectedBody));
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_11_011: [The function shall return the message content-type as 'binary/octet-stream'.]
    @Test
    public void getContentTypeReturnsCorrectContentType(
            @Mocked final Message mockMsg,
            @Mocked final MessageProperty mockProperty)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final boolean base64Encoded = false;
        final MessageProperty[] properties = { mockProperty };
        final String propertyName = "test-property-name";
        final String propertyValue = "test-property-value";
        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
                result = body;
                mockMsg.getProperties();
                result = properties;
                mockProperty.getName();
                result = propertyName;
                mockProperty.getValue();
                result = propertyValue;
            }
        };

        HttpsSingleMessage httpsMsg =
                HttpsSingleMessage.parseHttpsMessage(mockMsg);
        String testContentType = httpsMsg.getContentType();

        String expectedContentType = "binary/octet-stream";
        assertThat(testContentType, is(expectedContentType));
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_11_013: [The function shall return a copy of the message properties.]
    @Test
    public void getPropertiesReturnsCopyOfProperties(
            @Mocked final Message mockMsg,
            @Mocked final MessageProperty mockProperty)
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final MessageProperty[] properties = { mockProperty };
        final String propertyName = "test-property-name";
        final String httpsPropertyName = "iothub-app-test-property-name";
        final String propertyValue = "test-property-value";
        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
                result = body;
                mockMsg.getProperties();
                result = properties;
                mockProperty.getName();
                result = propertyName;
                result = httpsPropertyName;
                mockProperty.getValue();
                result = propertyValue;
            }
        };

        HttpsSingleMessage httpsMsg = HttpsSingleMessage.parseHttpsMessage(mockMsg);
        MessageProperty[] testProperties = httpsMsg.getProperties();

        final MessageProperty[] expectedProperties = properties;
        assertThat(testProperties.length, is(expectedProperties.length));
        final String expectedPropertyName = httpsPropertyName;
        final String expectedPropertyValue = propertyValue;
        new Verifications()
        {
            {
                new MessageProperty(expectedPropertyName, expectedPropertyValue);
                times = 2;
            }
        };
    }

    // Tests_SRS_HTTPSSINGLEMESSAGE_34_020: [The function shall return an IoT Hub message with all system properties set accordingly.]
    // Tests_SRS_HTTPSSINGLEMESSAGE_34_021: [The parsed HttpsSingleMessage shall include all valid HTTPS system-defined properties in the response header as message properties.]
    // Tests_SRS_HTTPSSINGLEMESSAGE_34_015: [The function shall return a copy of the message's system properties.]
    @Test
    public void parseHttpsMessageHandlesPropertiesCorrectlyAndToMessageExposesCorrectSystemProperties(@Mocked final HttpsResponse httpsResponse)
    {
        // arrange
        String correlationIdKey = HTTPS_SYSTEM_PROPERTY_PREFIX + "correlationid";
        String userIdKey = HTTPS_SYSTEM_PROPERTY_PREFIX + "userid";
        String messageIdKey = HTTPS_SYSTEM_PROPERTY_PREFIX + "messageid";
        String toKey = HTTPS_SYSTEM_PROPERTY_PREFIX + "to";
        String appPropertyKey = "app_property";
        String correlationId = "1234";
        String messageId = "3456";
        String userId = "6789";
        String to = "device4";
        String appProperty = "app_property_value";

        final Map<String, String> headerFields = new HashMap<String, String>();
        headerFields.put(correlationIdKey, correlationId);
        headerFields.put(userIdKey, userId);
        headerFields.put(messageIdKey, messageId);
        headerFields.put(toKey, to);
        headerFields.put(HTTPS_APP_PROPERTY_PREFIX + appPropertyKey, appProperty);

        new NonStrictExpectations()
        {
            {
                httpsResponse.getBody();
                result = "body".getBytes();

                httpsResponse.getHeaderFields();
                result = headerFields;
            }
        };

        // act (parseHttpMessage)
        final HttpsSingleMessage actualHttpMessage = HttpsSingleMessage.parseHttpsMessage(httpsResponse);

        // assert (parseHttpMessage)
        assertEquals(1, actualHttpMessage.getProperties().length);
        assertTrue(propertyAssignedCorrectly(actualHttpMessage.getProperties(),HTTPS_APP_PROPERTY_PREFIX + appPropertyKey, appProperty));

        assertTrue(systemPropertyAssignedCorrectly(actualHttpMessage.getSystemProperties(), correlationIdKey, correlationId));
        assertTrue(systemPropertyAssignedCorrectly(actualHttpMessage.getSystemProperties(), messageIdKey, messageId));
        assertTrue(systemPropertyAssignedCorrectly(actualHttpMessage.getSystemProperties(), userIdKey, userId));
        assertTrue(systemPropertyAssignedCorrectly(actualHttpMessage.getSystemProperties(), toKey, to));

        // act (toMessage)
        Message actualMessage = actualHttpMessage.toMessage();

        // assert (toMessage)
        assertEquals(correlationId, actualMessage.getCorrelationId());
        assertEquals(messageId, actualMessage.getMessageId());

        assertEquals(3, actualMessage.getProperties().length);
        assertTrue(propertyAssignedCorrectly(actualMessage.getProperties(), appPropertyKey, appProperty));
        assertTrue(propertyAssignedCorrectly(actualMessage.getProperties(), HTTPS_APP_PROPERTY_PREFIX + userIdKey, userId));
        assertTrue(propertyAssignedCorrectly(actualMessage.getProperties(), HTTPS_APP_PROPERTY_PREFIX + toKey, to));
    }

    private static boolean propertyAssignedCorrectly(MessageProperty[] properties, String name, String value)
    {
        for (int i = 0; i < properties.length; i++)
        {
            if (properties[i].getName().equals(name))
            {
                if (properties[i].getValue().equals(value))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean systemPropertyAssignedCorrectly(Map<String, String> systemProperties, String name, String value)
    {
        for (String key : systemProperties.keySet())
        {
            if (key.equals(name))
            {
                return systemProperties.get(key).equals(value);
            }
        }

        return false;
    }
}
