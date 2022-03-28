// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.MessageProperty;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;


/**
 * Builds a batched IoT Hub request body as a JSON array. The batched message
 * has a maximum size of 256 kb.
 */
final class HttpsBatchMessage implements HttpsMessage
{
    // Note: this limit is defined by the IoT Hub.
    private static final int SERVICEBOUND_MESSAGE_MAX_SIZE_BYTES = 255 * 1024 - 1;

    /**
     * The value for the "content-type" header field in a batched HTTPS
     * request.
     */
    private static final String HTTPS_BATCH_CONTENT_TYPE = "application/vnd.microsoft.iothub.json";

    /**
     * The charset used to encode IoT Hub messages. The server will interpret
     * the JSON array using UTF-8 by default according to RFC4627.
     */
    private static final Charset BATCH_CHARSET = StandardCharsets.UTF_8;

    private static final String BASE_ENCODED_KEY = "\"base64Encoded\"";
    private static final String BODY = "\"body\"";
    private static final String KEY_VALUE_SEPARATOR = ":";
    private static final String PROPERTIES = "\"properties\"";

    /** The current batched message body. */
    private final String batchBody;

    /** The current number of messages in the batch. */
    private int numMsgs;

    public HttpsBatchMessage(List<HttpsSingleMessage> messageList) throws IllegalArgumentException
    {
        StringBuilder batchBodyBuilder = new StringBuilder();
        batchBodyBuilder.append('[');

        boolean isSubsequentMessage = false;
        for (HttpsSingleMessage message : messageList)
        {
            if (isSubsequentMessage)
            {
                batchBodyBuilder.append(','); // comma to separate each object in the json array
            }

            addJsonToStringBuilder(message, batchBodyBuilder);
            this.numMsgs++;
            isSubsequentMessage = true; // the next message, and all subsequent messages, will need a comma before them
        }

        batchBodyBuilder.append(']');

        this.batchBody = batchBodyBuilder.toString();

        byte[] newBatchBodyBytes = this.batchBody.getBytes(BATCH_CHARSET);

        if (newBatchBodyBytes.length > SERVICEBOUND_MESSAGE_MAX_SIZE_BYTES)
        {
            String errMsg = String.format("Service-bound message size (%d bytes) cannot exceed %d bytes.",
                newBatchBodyBytes.length, SERVICEBOUND_MESSAGE_MAX_SIZE_BYTES);
            throw new IllegalArgumentException(errMsg);
        }
    }

    /**
     * Returns the current batch body as a UTF-8 encoded byte array.
     *
     * @return the current batch body as a UTF-8 encoded byte array.
     */
    public byte[] getBody()
    {
        return this.batchBody.getBytes(BATCH_CHARSET);
    }

    /**
     * Returns the message content-type as 'application/vnd.microsoft.iothub.json'.
     *
     * @return the message content-type as 'application/vnd.microsoft.iothub.json'.
     */
    public String getContentType()
    {
        return HTTPS_BATCH_CONTENT_TYPE;
    }

    /**
     * Returns an empty list of properties for the batched message.
     *
     * @return an empty list of properties for the batched message.
     */
    public MessageProperty[] getProperties()
    {
        return new MessageProperty[0];
    }

    /**
     * It is part of the HttpsMessage interface to get the collection of system message
     * properties. For batch, it just returns a empty Map.
     *
     * @return an empty Map.
     */
    public Map<String, String> getSystemProperties()
    {
        return new HashMap<>();
    }

    /**
     * Returns the number of messages currently in the batch.
     *
     * @return the number of messages currently in the batch.
     */
    public int numMessages()
    {
        return this.numMsgs;
    }

    /**
     * Converts a service-bound message to a JSON object with the correct
     * format.
     *
     * @param msg the message to be converted to a corresponding JSON object.
     */
    private static void addJsonToStringBuilder(HttpsSingleMessage msg, StringBuilder jsonStringBuilder)
    {
        jsonStringBuilder.append('{' + BODY + KEY_VALUE_SEPARATOR);
        jsonStringBuilder.append('\"').append(encodeBase64String(msg.getBody())).append("\",");
        jsonStringBuilder.append(BASE_ENCODED_KEY + KEY_VALUE_SEPARATOR);
        jsonStringBuilder.append(true);
        MessageProperty[] properties = msg.getProperties();
        Map<String, String> allProperties = new HashMap<>(msg.getSystemProperties());
        for (MessageProperty p : properties)
        {
            allProperties.put(p.getName(), p.getValue());
        }

        int numProperties = allProperties.size();
        if (numProperties > 0)
        {
            jsonStringBuilder.append(',');
            jsonStringBuilder.append(PROPERTIES + KEY_VALUE_SEPARATOR);
            jsonStringBuilder.append('{');
            for (String key : allProperties.keySet())
            {
                jsonStringBuilder.append('\"').append(key).append("\":");
                jsonStringBuilder.append('\"').append(allProperties.get(key)).append("\",");
            }

            //remove last trailing comma
            jsonStringBuilder.deleteCharAt(jsonStringBuilder.length()-1);

            jsonStringBuilder.append('}');
        }

        jsonStringBuilder.append('}');
    }
}
