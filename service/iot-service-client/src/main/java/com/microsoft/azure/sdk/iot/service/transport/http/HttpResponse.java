/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An HTTPS response. Contains the status code, body, header fields, and error
 * reason (if any).
 */
public class HttpResponse
{
    private final int status;
    private final byte[] body;
    private final byte[] errorReason;
    private final Map<String, String> headerFields;

    /**
     * Constructor.
     *
     * @param status The HTTPS status code.
     * @param body The response body.
     * @param headerFields The map of header field names and the values associated with the field name.
     * @param errorReason The error reason.
     */
    public HttpResponse(int status, byte[] body,
                        Map<String, List<String>> headerFields,
                        byte[] errorReason)
    {
        this.status = status;
        this.body = Arrays.copyOf(body, body.length);
        this.errorReason = errorReason;

        this.headerFields = new HashMap<>();
        for (Map.Entry<String, List<String>> headerField : headerFields
                .entrySet())
        {
            String key = headerField.getKey();
            if (key != null)
            {
                String field = canonicalizeFieldName(key);
                String values = flattenValuesList(headerField.getValue());
                this.headerFields.put(field, values);
            }
        }
    }

    /**
     * Getter for the HTTPS status code.
     *
     * @return The HTTPS status code.
     */
    public int getStatus()
    {
        return this.status;
    }

    /**
     * Getter for the response body.
     *
     * @return The response body.
     */
    public byte[] getBody()
    {
        return Arrays.copyOf(this.body, this.body.length);
    }

    /**
     * Getter for a header field.
     *
     * @param field the header field name.
     *
     * @return the header field value. If multiple values are present, they are
     * returned as a comma-separated list according to RFC2616.
     *
     * @throws IllegalArgumentException if no value exists for the given field
     * name.
     */
    public String getHeaderField(String field)
    {
        String canonicalizedField = canonicalizeFieldName(field);
        String values = this.headerFields.get(canonicalizedField);
        if (values == null)
        {
            String errMsg = String.format("Could not find a value "
                    + "associated with the header field name '%s'.\n", field);

            throw new IllegalArgumentException(errMsg);
        }

        return values;
    }

    /**
     * Getter for the header fields.
     *
     * @return The copy of the header fields for this response.
     */
    public Map<String, String> getHeaderFields()
    {
        Map<String, String> headerFieldsCopy = new HashMap<>();
        headerFieldsCopy.putAll(this.headerFields);

        return headerFieldsCopy;
    }

    /**
     * Getter for the error reason.
     *
     * @return The error reason.
     */
    public byte[] getErrorReason()
    {
        return this.errorReason;
    }

    private static String canonicalizeFieldName(String field)
    {
        String canonicalizedField = field;
        if (canonicalizedField != null)
        {
            canonicalizedField = field.toLowerCase();
        }

        return canonicalizedField;
    }

    private static String flattenValuesList(List<String> values)
    {
        StringBuilder valuesStr = new StringBuilder();
        for (String value : values)
        {
            valuesStr.append(value).append(",");
        }
        // remove the trailing comma.
        valuesStr = new StringBuilder(valuesStr.substring(0, Math.max(0, valuesStr.length() - 1)));

        return valuesStr.toString();
    }

    protected HttpResponse()
    {
        this.status = 0;
        this.body = null;
        this.headerFields = null;
        this.errorReason = null;
    }
}
