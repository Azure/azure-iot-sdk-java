/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.http;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HttpRequest
{
    private static final String USER_AGENT = "User-Agent";
    private static final String ACCEPT = "Accept";
    private static final String ACCEPT_VALUE = "application/json";
    private static final String ACCEPT_CHARSET = "charset=utf-8";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String AUTHORIZATION = "Authorization";
    public static final String REQUEST_ID = "Request-Id";
    public static final String IF_MATCH = "If-Match";

    /** The underlying HTTPS connection. */
    private final HttpsURLConnection connection;

    /**
     * The body. HttpsURLConnection silently calls connect() when the output
     * stream is written to. We buffer the body and defer writing to the output
     * stream until connect() is called.
     */
    private byte[] body;

    /**
     * Constructor. Takes a URL as an argument and returns an HTTPS request that
     * is ready to be sent.
     *
     * @param url The URL for the request.
     * @param method The HTTPS request method (i.e. GET).
     * @param body The request body. Must be an array of size 0 if the request method is GET or DELETE.
     * @param authorizationToken The header value for the Authorization header.
     *
     * @throws IOException This exception thrown if an IOException occurs
     * in setting up the HTTPS connection.
     * @throws IllegalArgumentException This exception thrown if the endpoint
     * given does not use the HTTPS protocol.
     */
    public HttpRequest(URL url, HttpMethod method, byte[] body, String authorizationToken) throws IOException
    {
        this(url, method, body, authorizationToken, null);
    }

    /**
     * Constructor. Takes a URL as an argument and returns an HTTPS request that
     * is ready to be sent through an optional proxy.
     *
     * @param url The URL for the request.
     * @param method The HTTPS request method (i.e. GET).
     * @param body The request body. Must be an array of size 0 if the request method is GET or DELETE.
     * @param authorizationToken The header value for the Authorization header.
     * @param proxy The proxy to send the request through. May be null if no proxy should be used
     *
     * @throws IOException This exception thrown if an IOException occurs
     * in setting up the HTTPS connection.
     * @throws IllegalArgumentException This exception thrown if the endpoint
     * given does not use the HTTPS protocol.
     */
    public HttpRequest(URL url, HttpMethod method, byte[] body, String authorizationToken, Proxy proxy) throws IOException
    {
        if (proxy != null)
        {
            this.connection = (HttpsURLConnection) url.openConnection(proxy);
        }
        else
        {
            this.connection = (HttpsURLConnection) url.openConnection();
        }

        if (method != HttpMethod.POST && method != HttpMethod.PUT && method != HttpMethod.PATCH)
        {
            if (body.length > 0)
            {
                throw new IllegalArgumentException(
                    "Cannot write a body to a request that "
                        + "is not a POST, PATCH or PUT request.");
            }
        }
        else
        {
            this.body = Arrays.copyOf(body, body.length);
        }

        if (method == HttpMethod.PATCH)
        {
            // This HTTP library doesn't support PATCH calls, but using this header we can override this limitation.
            // https://bugs.openjdk.java.net/browse/JDK-7016595
            this.setHeaderField("X-HTTP-Method-Override", "PATCH");
            this.connection.setRequestMethod("POST");
        }
        else
        {
            this.connection.setRequestMethod(method.name());
        }

        this.setHeaderField(USER_AGENT, TransportUtils.javaServiceClientIdentifier + TransportUtils.serviceVersion);
        this.setHeaderField(ACCEPT, ACCEPT_VALUE);
        this.setHeaderField(CONTENT_TYPE, ACCEPT_VALUE + "; " + ACCEPT_CHARSET);
        this.setHeaderField(AUTHORIZATION, authorizationToken);
        this.setHeaderField(CONTENT_LENGTH, String.valueOf(body.length));
    }

    /**
     * Executes the HTTPS request.
     *
     * @return The HTTPS response.
     *
     * @throws IOException This exception thrown if the connection could not be
     * established, or the input/output streams could not be accessed.
     * @throws IotHubException if the http request was successful, but IoT hub responded with an error code
     */
    public HttpResponse send() throws IotHubException, IOException
    {
        int responseStatus;
        byte[] responseBody = new byte[0];
        byte[] errorReason = new byte[0];
        Map<String, List<String>> headerFields;
        try
        {
            if (this.body != null && this.body.length > 0)
            {
                this.connection.setDoOutput(true);
                this.connection.getOutputStream().write(this.body);
            }

            this.connection.connect();

            responseStatus = this.connection.getResponseCode();
            headerFields = this.connection.getHeaderFields();

            try (InputStream inputStream = this.connection.getInputStream())
            {
                responseBody = readInputStream(inputStream);
            }
        }
        // Can be caused either by an unsuccessful
        // connection or by a bad status code.
        catch (IOException e)
        {
            // If the IOException was caused by a bad status code in the
            // response, then getResponseStatus() returns a valid status code.
            // Otherwise, a connection could not be established and
            // getResponseStatus() throws an IOException.
            responseStatus = this.connection.getResponseCode();
            headerFields = this.connection.getHeaderFields();
            // Connections are transparently managed by Java.
            // The error stream must be cleared so that the connection
            // can be reused later.
            try (InputStream errorStream = this.connection.getErrorStream())
            {
                if (errorStream != null)
                {
                    errorReason = readInputStream(errorStream);
                }
            }
        }

        HttpResponse response = new HttpResponse(responseStatus, responseBody, headerFields, errorReason);
        IotHubExceptionManager.httpResponseVerification(response);

        return response;
    }

    /**
     * Sets the header field to the given value.
     *
     * @param field The header field name.
     * @param value The header field value.
     *
     * @return The object itself, for fluent setting.
     */
    public HttpRequest setHeaderField(String field, String value)
    {
        this.connection.setRequestProperty(field, value);
        return this;
    }

    /**
     * Sets the header field to the given value.
     *
     * @param headers The set of headers to use for this request
     *
     * @return The object itself, for fluent setting.
     */
    public HttpRequest setHeaders(Map<String, String> headers)
    {
        if (headers == null)
        {
            return this;
        }

        for (String header : headers.keySet())
        {
            this.connection.setRequestProperty(header, headers.get(header));
        }

        return this;
    }

    /**
     * Sets the read timeout, in seconds, for the request. The read timeout
     * is the number of milliseconds after the server receives a request and
     * before the server sends data back.
     *
     * @param timeout The read timeout.
     *
     * @return The object itself, for fluent setting.
     */
    public HttpRequest setReadTimeoutSeconds(int timeout)
    {
        int readTimeoutMillis = timeout * 1000; // http client expects milliseconds, not seconds
        this.connection.setReadTimeout(readTimeoutMillis);
        return this;
    }

    /**
     * Set the connect timeout, in seconds, for the request. The connect timeout
     * is the allowed amount of time for the http connection to be established.
     * @param timeout the connect timeout
     * @return the object itself, for fluent setting.
     */
    public HttpRequest setConnectTimeoutSeconds(int timeout)
    {
        int connectTimeoutMillis = timeout * 1000; // http client expects milliseconds, not seconds
        this.connection.setConnectTimeout(connectTimeoutMillis);
        return this;
    }

    protected HttpRequest()
    {
        this.connection = null;
    }

    private static byte[] readInputStream(InputStream stream) throws IOException
    {
        ArrayList<Byte> byteBuffer = new ArrayList<>();
        int nextByte;
        // read(byte[]) reads the byte into the buffer and returns the number
        // of bytes read, or -1 if the end of the stream has been reached.
        while ((nextByte = stream.read()) > -1)
        {
            byteBuffer.add((byte) nextByte);
        }

        int bufferSize = byteBuffer.size();
        byte[] byteArray = new byte[bufferSize];
        for (int i = 0; i < bufferSize; ++i)
        {
            byteArray[i] = byteBuffer.get(i);
        }

        return byteArray;
    }
}
