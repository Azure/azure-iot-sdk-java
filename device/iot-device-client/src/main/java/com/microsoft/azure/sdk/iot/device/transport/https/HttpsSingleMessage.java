// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** A single HTTPS message. */
public final class HttpsSingleMessage implements HttpsMessage
{
    private static final String HTTPS_SINGLE_MESSAGE_CONTENT_TYPE =
            "binary/octet-stream";
    private static final String HTTPS_SINGLE_JSON_MESSAGE_CONTENT_TYPE =
            "application/json;charset=utf-8";

    /**
     * The property names as they are saved in the system properties of this object
     */
    protected static final String CORRELATION_ID_KEY = HTTPS_SYSTEM_PROPERTY_PREFIX + "correlationid";
    protected static final String MESSAGE_ID_KEY = HTTPS_SYSTEM_PROPERTY_PREFIX + "messageid";
    protected static final String TO_KEY = HTTPS_SYSTEM_PROPERTY_PREFIX + "to";
    protected static final String USER_ID_KEY = HTTPS_SYSTEM_PROPERTY_PREFIX + "userid";
    protected static final String CONTENT_ENCODING_KEY = HTTPS_SYSTEM_PROPERTY_PREFIX + "contentencoding";
    protected static final String CONTENT_TYPE_KEY = HTTPS_SYSTEM_PROPERTY_PREFIX + "contenttype";

    private byte[] body;
    private boolean base64Encoded;
    private MessageProperty[] properties;
    private Map<String, String> systemProperties;
    private String contentType;

    /**
     * Returns the HTTPS message represented by the service-bound message for
     * binary octets.  Content type "binary/octet-stream"
     *
     * @param message the service-bound message to be mapped to its HTTPS message
     * equivalent.
     *
     * @return the HTTPS message represented by the service-bound message.
     */
    public static HttpsSingleMessage parseHttpsMessage(Message message)
    {
        HttpsSingleMessage httpsMsg = new HttpsSingleMessage();

        // Codes_SRS_HTTPSSINGLEMESSAGE_21_002: [The parsed HttpsSingleMessage shall set the contentType as `binary/octet-stream`.]
        httpsMsg.contentType = HTTPS_SINGLE_MESSAGE_CONTENT_TYPE;

        parser(httpsMsg, message);
        return httpsMsg;
    }

    /**
     * Returns the HTTPS message represented by the service-bound message for
     * application json format. Content type "application/json;charset=utf-8"
     *
     * @param message the service-bound message to be mapped to its HTTPS message
     * equivalent.
     *
     * @return the HTTPS message represented by the service-bound message.
     */
    public static HttpsSingleMessage parseHttpsJsonMessage(Message message)
    {
        HttpsSingleMessage httpsMsg = new HttpsSingleMessage();

        // Codes_SRS_HTTPSSINGLEMESSAGE_21_017: [The parsed HttpsSingleMessage shall set the contentType as `application/json;charset=utf-8`.]
        httpsMsg.contentType = HTTPS_SINGLE_JSON_MESSAGE_CONTENT_TYPE;

        parser(httpsMsg, message);
        return httpsMsg;
    }

    private static void parser(HttpsSingleMessage httpsMsg, Message message)
    {
        // Codes_SRS_HTTPSSINGLEMESSAGE_11_001: [The parsed HttpsSingleMessage shall have a copy of the original message body as its body.]
        // Codes_SRS_HTTPSSINGLEMESSAGE_21_016: [The parsed HttpsSingleMessage shall have a copy of the original message body as its body.]
        byte[] msgBody = message.getBytes();
        httpsMsg.body = Arrays.copyOf(msgBody, msgBody.length);

        // Codes_SRS_HTTPSSINGLEMESSAGE_11_003: [The parsed HttpsSingleMessage shall add the prefix 'iothub-app-' to each of the message properties.]
        // Codes_SRS_HTTPSSINGLEMESSAGE_21_018: [The parsed HttpsSingleMessage shall add the prefix 'iothub-app-' to each of the message properties.]
        MessageProperty[] msgProperties = message.getProperties();
        httpsMsg.properties = new MessageProperty[msgProperties.length];
        int countProperty;
        for (countProperty = 0; countProperty < msgProperties.length; ++countProperty)
        {
            MessageProperty property = msgProperties[countProperty];

            httpsMsg.properties[countProperty] = new MessageProperty(
                    HTTPS_APP_PROPERTY_PREFIX + property.getName(),
                    property.getValue());
        }

        // Codes_SRS_HTTPSSINGLEMESSAGE_34_014: [If the message contains a system property, the parsed HttpsSingleMessage shall add the corresponding property with property value]
        // Codes_SRS_HTTPSSINGLEMESSAGE_34_019: [If the message contains a system property, the parsed HttpsSingleMessage shall add the corresponding property with property value]
        Map<String, String> sysProperties = new HashMap<>();

        if (message.getUserId() != null)
        {
            sysProperties.put(USER_ID_KEY, message.getUserId());
        }

        if (message.getMessageId() != null)
        {
            sysProperties.put(MESSAGE_ID_KEY, message.getMessageId());
        }

        if (message.getCorrelationId() != null)
        {
            sysProperties.put(CORRELATION_ID_KEY, message.getCorrelationId());
        }

        if (message.getTo() != null)
        {
            sysProperties.put(TO_KEY, message.getTo());
        }

        if (message.getContentEncoding() != null)
        {
            sysProperties.put(CONTENT_ENCODING_KEY, message.getContentEncoding());
        }

        if (message.getContentType() != null)
        {
            sysProperties.put(CONTENT_TYPE_KEY, message.getContentType());
        }

        httpsMsg.systemProperties = new HashMap<>(sysProperties);
    }

    /**
     * Returns the HTTPS message represented by the HTTPS response.
     *
     * @param response the HTTPS response.
     *
     * @return the HTTPS message represented by the HTTPS response.
     */
    public static HttpsSingleMessage parseHttpsMessage(HttpsResponse response) {
        HttpsSingleMessage msg = new HttpsSingleMessage();

        // Codes_SRS_HTTPSSINGLEMESSAGE_11_004: [The parsed HttpsSingleMessage shall have a copy of the original response body as its body.]
        byte[] responseBody = response.getBody();
        msg.body = Arrays.copyOf(responseBody, responseBody.length);

        ArrayList<MessageProperty> properties = new ArrayList<>();
        Map<String, String> systemProperties = new HashMap<>();
        Map<String, String> headerFields = response.getHeaderFields();
        for (Map.Entry<String, String> field : headerFields.entrySet())
        {
            String propertyName = field.getKey();
            String propertyValue = field.getValue();
            if (isValidHttpsAppProperty(propertyName, propertyValue))
            {
                // Codes_SRS_HTTPSSINGLEMESSAGE_11_006: [The parsed HttpsSingleMessage shall include all valid HTTPS application-defined properties in the response header as message properties.]
                properties.add(new MessageProperty(propertyName, propertyValue));
            }
            else if (isValidHttpsSystemProperty(propertyName, propertyValue))
            {
                // Codes_SRS_HTTPSSINGLEMESSAGE_34_021: [The parsed HttpsSingleMessage shall include all valid HTTPS system-defined properties in the response header as message properties.]
                String systemPropertyName = propertyName.substring(HTTPS_SYSTEM_PROPERTY_PREFIX.length());
                systemProperties.put(HTTPS_SYSTEM_PROPERTY_PREFIX + systemPropertyName.toLowerCase(), propertyValue);
            }
        }
        msg.properties = new MessageProperty[properties.size()];
        msg.properties = properties.toArray(msg.properties);
        msg.systemProperties = systemProperties;

        return msg;
    }

    /**
     * Returns the Iot Hub message represented by the HTTPS message.
     *
     * @return the IoT Hub message represented by the HTTPS message.
     */
    public Message toMessage()
    {
        // Codes_SRS_HTTPSSINGLEMESSAGE_11_007: [The function shall return an IoT Hub message with a copy of the message body as its body.]
        Message msg = new Message(this.getBody());
        // Codes_SRS_HTTPSSINGLEMESSAGE_11_008: [The function shall return an IoT Hub message with application-defined properties that have the prefix 'iothub-app' removed.]
        for (MessageProperty property : this.properties)
        {
            String propertyName = httpsAppPropertyToAppProperty(property.getName());
            String propertyValue = property.getValue();
            msg.setProperty(propertyName, propertyValue);
        }

        // Codes_SRS_HTTPSSINGLEMESSAGE_34_020: [The function shall return an IoT Hub message with all system properties set accordingly.]
        if (this.systemProperties.containsKey(MESSAGE_ID_KEY))
        {
            msg.setMessageId(this.systemProperties.get(MESSAGE_ID_KEY));
        }

        if (this.systemProperties.containsKey(USER_ID_KEY))
        {
            msg.setProperty(HTTPS_APP_PROPERTY_PREFIX + USER_ID_KEY, this.systemProperties.get(USER_ID_KEY));
        }

        if (this.systemProperties.containsKey(CORRELATION_ID_KEY))
        {
            msg.setCorrelationId(this.systemProperties.get(CORRELATION_ID_KEY));
        }

        if (this.systemProperties.containsKey(CONTENT_TYPE_KEY))
        {
            msg.setContentType(this.systemProperties.get(CONTENT_TYPE_KEY));
        }

        if (this.systemProperties.containsKey(CONTENT_ENCODING_KEY))
        {
            msg.setContentEncoding(this.systemProperties.get(CONTENT_ENCODING_KEY));
        }

        if (this.systemProperties.containsKey(TO_KEY))
        {
            msg.setProperty(HTTPS_APP_PROPERTY_PREFIX + TO_KEY, this.systemProperties.get(TO_KEY));
        }

        return msg;
    }

    /**
     * Returns a copy of the message body.
     *
     * @return a copy of the message body.
     */
    public byte[] getBody()
    {
        // Codes_SRS_HTTPSSINGLEMESSAGE_11_009: [The function shall return a copy of the message body.]
        return Arrays.copyOf(this.body, this.body.length);
    }

    /**
     * Returns the message body as a string. The body is encoded using charset
     * UTF-8.
     *
     * @return the message body as a string.
     */
    public String getBodyAsString() {
        // Codes_SRS_HTTPSSINGLEMESSAGE_11_010: [The function shall return the message body as a string encoded using charset UTF-8.]
        return new String(this.body, Message.DEFAULT_IOTHUB_MESSAGE_CHARSET);
    }

    /**
     * Returns the message content-type.
     *
     * @return the message content-type.
     */
    public String getContentType()
    {
        // Codes_SRS_HTTPSSINGLEMESSAGE_11_011: [The function shall return the message content-type as 'binary/octet-stream'.]
        return this.contentType;
    }

    /**
     * Returns whether the message is Base64-encoded.
     *
     * @return whether the message is Base64-encoded.
     */
    public boolean isBase64Encoded()
    {
        // Codes_SRS_HTTPSSINGLEMESSAGE_11_012: [The function shall return whether the message is Base64-encoded.]
        return this.base64Encoded;
    }

    /**
     * Returns a copy of the message properties.
     *
     * @return a copy of the message properties.
     */
    public MessageProperty[] getProperties()
    {
        // Codes_SRS_HTTPSSINGLEMESSAGE_11_013: [The function shall return a copy of the message properties.]
        int propertiesSize = this.properties.length;
        MessageProperty[] propertiesCopy =
                new MessageProperty[propertiesSize];

        for (int i = 0; i < propertiesSize; ++i)
        {
            MessageProperty property = this.properties[i];
            MessageProperty propertyCopy =
                    new MessageProperty(property.getName(),
                            property.getValue());
            propertiesCopy[i] = propertyCopy;
        }

        return propertiesCopy;
    }

    /**
     * Returns a copy of the message system properties.
     *
     * @return a copy of the message system properties.
     */
    public Map<String, String> getSystemProperties()
    {
        // Codes_SRS_HTTPSSINGLEMESSAGE_34_015: [The function shall return a copy of the message's system properties.]
        return new HashMap<>(this.systemProperties);
    }

    /**
     * Returns whether the property name and value constitute a valid HTTPS
     * application property. The property is valid if it is a valid application
     * property and its name begins with 'iothub-app-'.
     *
     * @param name the property name.
     * @param value the property value.
     *
     * @return whether the property is a valid HTTPS property.
     */
    private static boolean isValidHttpsAppProperty(String name, String value)
    {
        String lowercaseName = name.toLowerCase();
        return (MessageProperty.isValidAppProperty(name.toLowerCase(), value)
                && lowercaseName.startsWith(HTTPS_APP_PROPERTY_PREFIX));
    }

    /**
     * Returns whether the property name and value constitute a valid HTTPS
     * system property. The property is valid if it is a reserved
     * property and its name begins with 'iothub-'.
     *
     * @param name the property name.
     * @param value the property value.
     *
     * @return whether the property is a valid HTTPS property.
     */
    private static boolean isValidHttpsSystemProperty(String name, String value)
    {
        String lowercaseName = name.toLowerCase();
        return (MessageProperty.isValidSystemProperty(name.toLowerCase(), value)
                && lowercaseName.startsWith(HTTPS_SYSTEM_PROPERTY_PREFIX)
                && !lowercaseName.startsWith(HTTPS_APP_PROPERTY_PREFIX));
    }

    /**
     * Returns an application-defined property name with the prefix 'iothub-app'
     * removed. If the prefix is not present, the property name is left
     * untouched.
     *
     * @param httpsAppProperty the HTTPS property name.
     *
     * @return the property name with the prefix 'iothub-app' removed.
     */
    private static String httpsAppPropertyToAppProperty(
            String httpsAppProperty)
    {
        String canonicalizedProperty = httpsAppProperty.toLowerCase();
        if (canonicalizedProperty.startsWith(HTTPS_APP_PROPERTY_PREFIX))
        {
            return canonicalizedProperty
                    .substring(HTTPS_APP_PROPERTY_PREFIX.length());
        }

        return canonicalizedProperty;
    }

    private HttpsSingleMessage()
    {
    }
}
