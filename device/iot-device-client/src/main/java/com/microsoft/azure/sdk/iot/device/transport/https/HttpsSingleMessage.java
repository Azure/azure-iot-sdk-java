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
final class HttpsSingleMessage implements HttpsMessage
{
    private static final String HTTPS_SINGLE_MESSAGE_CONTENT_TYPE =
            "binary/octet-stream";
    private static final String HTTPS_SINGLE_JSON_MESSAGE_CONTENT_TYPE =
            "application/json;charset=utf-8";

    /**
     * The property names as they are saved in the system properties of this object
     */
    private static final String CORRELATION_ID_KEY = HTTPS_SYSTEM_PROPERTY_PREFIX + "correlationid";
    private static final String MESSAGE_ID_KEY = HTTPS_SYSTEM_PROPERTY_PREFIX + "messageid";
    private static final String TO_KEY = HTTPS_SYSTEM_PROPERTY_PREFIX + "to";
    private static final String USER_ID_KEY = HTTPS_SYSTEM_PROPERTY_PREFIX + "userid";
    private static final String CONTENT_ENCODING_KEY = HTTPS_SYSTEM_PROPERTY_PREFIX + "contentencoding";
    private static final String CONTENT_TYPE_KEY = HTTPS_SYSTEM_PROPERTY_PREFIX + "contenttype";

    private byte[] body;
    @SuppressWarnings("unused") // This is never set but there might be a case we need it in the future.
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

        httpsMsg.contentType = HTTPS_SINGLE_JSON_MESSAGE_CONTENT_TYPE;

        parser(httpsMsg, message);
        return httpsMsg;
    }

    private static void parser(HttpsSingleMessage httpsMsg, Message message)
    {
        byte[] msgBody = message.getBytes();
        httpsMsg.body = Arrays.copyOf(msgBody, msgBody.length);

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
                properties.add(new MessageProperty(propertyName, propertyValue));
            }
            else if (isValidHttpsSystemProperty(propertyName, propertyValue))
            {
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
        Message msg = new Message(this.getBody());
        for (MessageProperty property : this.properties)
        {
            String propertyName = httpsAppPropertyToAppProperty(property.getName());
            String propertyValue = property.getValue();
            msg.setProperty(propertyName, propertyValue);
        }

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
        return Arrays.copyOf(this.body, this.body.length);
    }

    /**
     * Returns the message body as a string. The body is encoded using charset
     * UTF-8.
     *
     * @return the message body as a string.
     */
    public String getBodyAsString() {
        return new String(this.body, Message.DEFAULT_IOTHUB_MESSAGE_CHARSET);
    }

    /**
     * Returns the message content-type.
     *
     * @return the message content-type.
     */
    public String getContentType()
    {
        return this.contentType;
    }

    /**
     * Returns whether the message is Base64-encoded.
     *
     * @return whether the message is Base64-encoded.
     */
    public boolean isBase64Encoded()
    {
        return this.base64Encoded;
    }

    /**
     * Returns a copy of the message properties.
     *
     * @return a copy of the message properties.
     */
    public MessageProperty[] getProperties()
    {
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
