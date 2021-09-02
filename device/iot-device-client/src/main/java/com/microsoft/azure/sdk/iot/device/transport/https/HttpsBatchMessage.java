// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubSizeExceededException;

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
public final class HttpsBatchMessage implements HttpsMessage
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
    private String batchBody;

    /** The current number of messages in the batch. */
    private int numMsgs;

    public HttpsBatchMessage(List<HttpsSingleMessage> messageList) throws IotHubSizeExceededException
    {
        StringBuilder batchBodyBuilder = new StringBuilder();
        batchBodyBuilder.append('[');

        boolean atLeastOneMessage = false;
        for (HttpsSingleMessage message : messageList)
        {
            addJsonToStringBuilder(message, batchBodyBuilder);
            batchBodyBuilder.append(','); // comma to separate each object in the json array
            this.numMsgs++;
            atLeastOneMessage = true;
        }

        if (atLeastOneMessage)
        {
            batchBodyBuilder.deleteCharAt(batchBodyBuilder.length() - 1); // remove the final comma from the list
        }

        batchBodyBuilder.append(']');

        this.batchBody = batchBodyBuilder.toString();

        byte[] newBatchBodyBytes = this.batchBody.getBytes(BATCH_CHARSET);

        if (newBatchBodyBytes.length > SERVICEBOUND_MESSAGE_MAX_SIZE_BYTES)
        {
            String errMsg = String.format("Service-bound message size (%d bytes) cannot exceed %d bytes.",
                newBatchBodyBytes.length, SERVICEBOUND_MESSAGE_MAX_SIZE_BYTES);
            throw new IotHubSizeExceededException(errMsg);
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
     *
     * @return the JSON string representation of the message.
     */
    private static void addJsonToStringBuilder(HttpsSingleMessage msg, StringBuilder jsonStringBuilder)
    {
        StringBuilder jsonMsg = new StringBuilder('{');
        jsonMsg.append(BODY + KEY_VALUE_SEPARATOR);
        jsonMsg.append('\"').append(encodeBase64String(msg.getBody())).append("\",");
        jsonMsg.append(BASE_ENCODED_KEY + KEY_VALUE_SEPARATOR);
        jsonMsg.append(true);
        MessageProperty[] properties = msg.getProperties();
        Map<String, String> allProperties = new HashMap<>(msg.getSystemProperties());
        for (MessageProperty p : properties)
        {
            allProperties.put(p.getName(), p.getValue());
        }

        int numProperties = allProperties.size();
        if (numProperties > 0)
        {
            jsonMsg.append(',');
            jsonMsg.append(PROPERTIES + KEY_VALUE_SEPARATOR);
            jsonMsg.append('{');
            for (String key : allProperties.keySet())
            {
                jsonMsg.append('\"').append(key).append("\":");
                jsonMsg.append('\"').append(allProperties.get(key)).append("\",");
            }

            //remove last trailing comma
            jsonMsg.deleteCharAt(jsonMsg.length()-1);

            jsonMsg.append('}');
        }

        jsonMsg.append('}');

        jsonStringBuilder.append(jsonMsg);
    }
}
