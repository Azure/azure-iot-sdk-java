// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;
import com.microsoft.azure.sdk.iot.device.transport.TransportException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

class TopicParser
{
    private static final String QUESTION = "?";

    private static final String REQ_ID = "$rid=";
    private static final String VERSION = "$version=";

    /* The system property keys expected in a message */
    private final static String ABSOLUTE_EXPIRY_TIME = "$.exp";
    private final static String CORRELATION_ID = "$.cid";
    private final static String MESSAGE_ID = "$.mid";
    private final static String TO = "$.to";
    private final static String USER_ID = "$.uid";
    private final static String OUTPUT_NAME = "$.on";
    private final static String CONNECTION_DEVICE_ID = "$.cdid";
    private final static String CONNECTION_MODULE_ID = "$.cmid";
    private final static String CONTENT_TYPE = "$.ct";
    private final static String CONTENT_ENCODING = "$.ce";
    private final static String CREATION_TIME_UTC = "$.ctime";
    private final static String MQTT_SECURITY_INTERFACE_ID = "$.ifid";
    private final static String COMPONENT_ID = "$.sub";
    private final static String IOTHUB_ACK = "iothub-ack";

    @SuppressWarnings("SameParameterValue") // Method is designed to be generic, with any acceptable value for "tokenIndexReqID".
    static String getRequestId(String topic)
    {
        int tokenIndexReqId = 4;
        String[] topicTokens = topic.split("/");
        String reqId = null;

        if (tokenIndexReqId >= topicTokens.length)
        {
            throw new IllegalArgumentException("Invalid token Index for request id");
        }

        String token = topicTokens[tokenIndexReqId];

        if (token.contains(REQ_ID) && token.contains(QUESTION)) // restriction for request id
        {
            int startIndex = token.indexOf(REQ_ID) + REQ_ID.length();
            int endIndex = token.length();

            if (token.contains(VERSION) && !token.contains(QUESTION + VERSION))
            {
                // version after rid in the query
                endIndex = token.indexOf(VERSION) - 1;
            }

            reqId = token.substring(startIndex, endIndex);
        }

        return reqId;
    }

    @SuppressWarnings("SameParameterValue") // Method is designed to be generic, with any acceptable value for "tokenIndexMethod"
    static String getMethodName(String topic)
    {
        int tokenIndexMethod = 3;

        String[] topicTokens = topic.split("/");

        String methodName;

        if (tokenIndexMethod >= topicTokens.length)
        {
            throw new IllegalArgumentException("Invalid token Index for Method Name");
        }

        String token = topicTokens[tokenIndexMethod];

        if (token != null)
        {
            methodName = token;
        }
        else
        {
            throw new IllegalArgumentException("method name could not be parsed");
        }

        return methodName;
    }

    static String BuildTelemetryTopic(String baseTopic, Message message) throws TransportException
    {
        StringBuilder stringBuilder = new StringBuilder(baseTopic);

        boolean separatorNeeded;

        separatorNeeded = AppendPropertyIfPresent(stringBuilder, false, MESSAGE_ID, message.getMessageId(), false);
        separatorNeeded = AppendPropertyIfPresent(stringBuilder, separatorNeeded, CORRELATION_ID, message.getCorrelationId(), false);
        separatorNeeded = AppendPropertyIfPresent(stringBuilder, separatorNeeded, USER_ID, message.getUserId(), false);
        separatorNeeded = AppendPropertyIfPresent(stringBuilder, separatorNeeded, TO, message.getTo(), false);
        separatorNeeded = AppendPropertyIfPresent(stringBuilder, separatorNeeded, OUTPUT_NAME, message.getOutputName(), false);
        separatorNeeded = AppendPropertyIfPresent(stringBuilder, separatorNeeded, CONNECTION_DEVICE_ID, message.getConnectionDeviceId(), false);
        separatorNeeded = AppendPropertyIfPresent(stringBuilder, separatorNeeded, CONNECTION_MODULE_ID, message.getConnectionModuleId(), false);
        separatorNeeded = AppendPropertyIfPresent(stringBuilder, separatorNeeded, CONTENT_ENCODING, message.getContentEncoding(), false);
        separatorNeeded = AppendPropertyIfPresent(stringBuilder, separatorNeeded, CONTENT_TYPE, message.getContentType(), false);
        separatorNeeded = AppendPropertyIfPresent(stringBuilder, separatorNeeded, CREATION_TIME_UTC, message.getCreationTimeUTCString(), false);
        if (message.isSecurityMessage())
        {
            separatorNeeded = AppendPropertyIfPresent(stringBuilder, separatorNeeded, MQTT_SECURITY_INTERFACE_ID, MessageProperty.IOTHUB_SECURITY_INTERFACE_ID_VALUE, false);
        }

        if (message.getComponentName() != null && !message.getComponentName().isEmpty())
        {
            separatorNeeded = AppendPropertyIfPresent(stringBuilder, separatorNeeded, COMPONENT_ID, message.getComponentName(), false);
        }

        for (MessageProperty property : message.getProperties())
        {
            separatorNeeded = AppendPropertyIfPresent(stringBuilder, separatorNeeded, property.getName(), property.getValue(), true);
        }

        return stringBuilder.toString();
    }

    /**
     * Takes propertiesString and parses it for all the properties it holds and then assigns them to the provided message
     * @param propertiesString the string to parse containing all the properties
     * @param message the message to add the parsed properties to
     * @throws IllegalArgumentException if a property's key and value are not separated by the '=' symbol
     * @throws IllegalStateException if the property for expiry time is present, but the value cannot be parsed as a Long
     * */
    static void AssignPropertiesToMessage(Message message, String propertiesString) throws IllegalStateException, IllegalArgumentException
    {
        for (String propertyString : propertiesString.split("&"))
        {
            if (propertyString.contains("="))
            {
                //Expected format is <key>=<value> where both key and value may be encoded
                String key = propertyString.split("=")[0];
                String value = propertyString.split("=")[1];

                try
                {
                    key = URLDecoder.decode(key, StandardCharsets.UTF_8.name());
                    value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
                }
                catch (UnsupportedEncodingException e)
                {
                    // should never happen, since the encoding is hard-coded.
                    throw new IllegalStateException(e);
                }

                //Some properties are reserved system properties and must be saved in the message differently
                //Codes_SRS_Mqtt_34_057: [This function shall parse the messageId, correlationId, outputname, content encoding and content type from the provided property string]
                switch (key)
                {
                    case TO:
                    case IOTHUB_ACK:
                    case USER_ID:
                    case ABSOLUTE_EXPIRY_TIME:
                        //do nothing
                        break;
                    case MESSAGE_ID:
                        message.setMessageId(value);
                        break;
                    case CORRELATION_ID:
                        message.setCorrelationId(value);
                        break;
                    case OUTPUT_NAME:
                        message.setOutputName(value);
                        break;
                    case CONTENT_ENCODING:
                        message.setContentEncoding(value);
                        break;
                    case CONTENT_TYPE:
                        message.setContentType(value);
                        break;
                    default:
                        message.setProperty(key, value);
                }
            }
            else
            {
                throw new IllegalArgumentException("Unexpected property string provided. Expected '=' symbol between key and value of the property in string: " + propertyString);
            }
        }
    }

    /**
     * Appends the property to the provided stringbuilder if the property value is not null.
     * @param stringBuilder the builder to build upon
     * @param separatorNeeded if a separator should precede the new property
     * @param propertyKey the mqtt topic string property key
     * @param propertyValue the property value (message id, correlation id, etc.)
     * @return true if a separator will be needed for any later properties appended on
     */
    private static boolean AppendPropertyIfPresent(StringBuilder stringBuilder, boolean separatorNeeded, String propertyKey, String propertyValue, boolean isApplicationProperty) throws TransportException
    {
        try
        {
            if (propertyValue != null && !propertyValue.isEmpty())
            {
                if (separatorNeeded)
                {
                    stringBuilder.append("&");
                }

                if (isApplicationProperty)
                {
                    // URLEncoder.Encode incorrectly encodes space characters as '+'. For MQTT to work, we need to replace those '+' with "%20"
                    stringBuilder.append(URLEncoder.encode(propertyKey, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20"));
                }
                else
                {
                    stringBuilder.append(propertyKey);
                }

                stringBuilder.append("=");

                // URLEncoder.Encode incorrectly encodes space characters as '+'. For MQTT to work, we need to replace those '+' with "%20"
                stringBuilder.append(URLEncoder.encode(propertyValue, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20"));

                return true;
            }

            return separatorNeeded;
        }
        catch (UnsupportedEncodingException e)
        {
            throw new TransportException("Could not utf-8 encode the property with name " + propertyKey + " and value " + propertyValue, e);
        }
    }
}
