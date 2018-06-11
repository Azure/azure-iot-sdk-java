// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * An HTTPS request.
 */
public class HttpsRequest
{
    /** The underlying HTTPS connection stream. */
    private final HttpsConnection connection;

    /**
     * Constructor. Takes a URL as an argument and returns an HTTPS request that
     * is ready to be sent.
     *
     * @param url the URL for the request.
     * @param method the HTTPS request method (i.e. GET).
     * @param body the request body. Must be an array of size 0 if the request
     * method is GET or DELETE.
     * @param userAgentString the user agent string to attach to all http communications
     * @throws TransportException if an Exception occurs in setting up the HTTPS
     * connection.
     * @throws TransportException if the endpoint given does not use the
     * HTTPS protocol.
     */
    public HttpsRequest(URL url, HttpsMethod method, byte[] body, String userAgentString) throws TransportException
    {
        // Codes_SRS_HTTPSREQUEST_11_005: [If an IOException occurs in setting up the HTTPS connection, the function shall throw a TransportException.]
        // Codes_SRS_HTTPSREQUEST_11_001: [The function shall open a connection with the given URL as the endpoint.]
        // Codes_SRS_HTTPSREQUEST_11_004: [The function shall use the given HTTPS method (i.e. GET) as the request method.]
        this.connection = new HttpsConnection(url, method);

        if (userAgentString != null && !userAgentString.isEmpty())
        {
            this.connection.setRequestHeader("User-Agent", userAgentString);
        }

        // Codes_SRS_HTTPSREQUEST_11_002: [The function shall write the body to the connection.]
        this.connection.writeOutput(body);
    }

    /**
     * Executes the HTTPS request.
     *
     * @return an HTTPS response.
     *
     * @throws TransportException if the connection could not be established, or the
     * input/output streams could not be accessed.
     */
    public HttpsResponse send() throws TransportException
    {
        int responseStatus = -1;
        byte[] responseBody = new byte[0];
        byte[] errorReason = new byte[0];
        Map<String, List<String>> headerFields;

        // Codes_SRS_HTTPSREQUEST_11_008: [The function shall send an HTTPS request as formatted in the constructor.]
        this.connection.connect();

        responseStatus = this.connection.getResponseStatus();
        headerFields = this.connection.getResponseHeaders();

        if (responseStatus == 200)
        {
            responseBody = this.connection.readInput();
        }

        // Codes_SRS_HTTPSREQUEST_11_009: [The function shall return the HTTPS response received, including the status code, body (if 200 status code), header fields, and error reason (if any).]
        return new HttpsResponse(responseStatus, responseBody, headerFields, errorReason);
    }

    /**
     * Sets the header field to the given value.
     *
     * @param field the header field name.
     * @param value the header field value.
     *
     * @return itself, for fluent setting.
     */
    public HttpsRequest setHeaderField(String field, String value)
    {
        // Codes_SRS_HTTPSREQUEST_11_013: [The function shall set the header field with the given name to the given value.]
        this.connection.setRequestHeader(field, value);
        return this;
    }

    /**
     * Sets the read timeout, in milliseconds, for the request. The read timeout
     * is the number of milliseconds after the server receives a request and
     * before the server sends data back.
     *
     * @param timeout the read timeout.
     *
     * @return itself, for fluent setting.
     */
    public HttpsRequest setReadTimeoutMillis(int timeout)
    {
        // Codes_SRS_HTTPSREQUEST_11_014: [The function shall set the read timeout for the request to the given value.]
        this.connection.setReadTimeoutMillis(timeout);
        return this;
    }

    /**
     * Sets this object's SSL context
     * @param sslContext the value to set this object's SSL context too
     * @return itself, for fluent setting.
     * @throws IllegalArgumentException if sslContext is null
     */
    public HttpsRequest setSSLContext(SSLContext sslContext) throws IllegalArgumentException
    {
        if (sslContext == null)
        {
            //Codes_SRS_HTTPSREQUEST_25_015: [The function shall throw IllegalArgumentException if argument is null .]
            throw new IllegalArgumentException("Context cannot be null");
        }
        //Codes_SRS_HTTPSREQUEST_25_016: [The function shall set the SSL context for the IotHub.]
        this.connection.setSSLContext(sslContext);
        return this;
    }

    public byte[] getBody()
    {
        // Codes_SRS_HTTPSREQUEST_34_017: [The function shall return the body saved in this object's connection instance.]
        return this.connection.getBody();
    }

    public URL getRequestUrl()
    {
        // Codes_SRS_HTTPSREQUEST_34_018: [The function shall return the request url saved in this object's connection instance.]
        return this.connection.getRequestUrl();
    }

    public String getHttpMethod()
    {
        // Codes_SRS_HTTPSREQUEST_34_019: [The function shall return the http method saved in this object's connection instance.]
        return this.connection.getHttpMethod();
    }

    public String getRequestHeaders()
    {
        // Codes_SRS_HTTPSREQUEST_34_020: [The function shall return the request headers saved in this object's connection instance.]
        return this.connection.getRequestHeaders();
    }

    @SuppressWarnings("unused")
    protected HttpsRequest()
    {
        this.connection = null;
    }
}
