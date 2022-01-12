/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.device.transport.https;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A wrapper for the Java SE class HttpsURLConnection. Used to avoid
 * compatibility issues when testing with the mocking framework JMockit, as well
 * as to avoid some undocumented side effects when using HttpsURLConnection.
 * <p>
 * The underlying HttpsURLConnection is transparently managed by Java. To reuse
 * connections, for each time connect() is called, the input streams (input
 * stream or error stream, if input stream is not accessible) must be completely
 * read. Otherwise, the data remains in the stream and the connection will not
 * be reusable.
 */
class HttpConnection
{
    /** The underlying HTTPS connection. */
    private final HttpsURLConnection connection;

    /**
     * The body. HttpsURLConnection silently calls connect() when the output
     * stream is written to. We buffer the body and defer writing to the output
     * stream until connect() is called.
     */
    private byte[] body;

    /**
     * Constructor. Opens a connection to the given URL.
     *
     * @param url The URL for the HTTPS connection.
     * @param method The HTTPS method (i.e. GET).
     *
     * @throws IOException  This exception is thrown if the connection was unable to be opened.
     */
    public HttpConnection(URL url, HttpMethod method) throws IOException
    {
        String protocol = url.getProtocol();
        if (!protocol.equalsIgnoreCase("HTTPS"))
        {
            String errMsg = String.format("Expected URL that uses iotHubServiceClientProtocol "
                    + "HTTPS but received one that uses "
                    + "iotHubServiceClientProtocol '%s'.\n",
                protocol);
            throw new IllegalArgumentException(errMsg);
        }

        this.connection = (HttpsURLConnection) url.openConnection();

        if (method == HttpMethod.PATCH)
        {
            this.setUnsupportedMethod(method);
            method = HttpMethod.POST;
        }
        this.connection.setRequestMethod(method.name());
        this.body = new byte[0];
    }

    private void setUnsupportedMethod(HttpMethod method) throws IOException
    {
        if (method == HttpMethod.PATCH)
        {
            this.setRequestHeader("X-HTTP-Method-Override", "PATCH");
        }
        else
        {
            throw new IOException("Unexpected Http Method " + method);
        }
    }

    /**
     * Sends the request to the URL given in the constructor.
     *
     * @throws IOException This exception thrown if the connection could not be established,
     * or the server responded with a bad status code.
     */
    public void connect() throws IOException
    {
        if (this.body.length > 0)
        {
            this.connection.setDoOutput(true);
            this.connection.getOutputStream().write(this.body);
        }

        this.connection.connect();
    }

    /**
     * Sets the request method (i.e. POST).
     *
     * @param method The request method.
     *
     * @throws IllegalArgumentException This exception thrown if the request currently
     * has a non-empty body and the new method is not a POST or a PUT. This is because Java's
     * HttpsURLConnection silently converts the HTTPS method to POST or PUT if a
     * body is written to the request.
     */
    public void setRequestMethod(HttpMethod method)
    {
        if (method != HttpMethod.POST && method != HttpMethod.PUT)
        {
            if (this.body.length > 0)
            {
                throw new IllegalArgumentException(
                    "Cannot change the request method from POST "
                        + "or PUT when the request body is non-empty.");
            }
        }

        try
        {
            this.connection.setRequestMethod(method.name());
        }
        catch (ProtocolException e)
        {
            // should never happen, since the method names are hard-coded.
        }
    }

    /**
     * Sets the request header field to the given value.
     *
     * @param field The header field name.
     * @param value The header field value.
     */
    public void setRequestHeader(String field, String value)
    {
        this.connection.setRequestProperty(field, value);
    }

    /**
     * Sets the read timeout in milliseconds. The read timeout is the number of
     * milliseconds after the server receives a request and before the server
     * sends data back.
     *
     * @param timeout The read timeout.
     */
    public void setReadTimeoutMillis(int timeout)
    {
        this.connection.setReadTimeout(timeout);
    }

    /**
     * Saves the body to be sent with the request.
     *
     * @param body The request body.
     *
     * @throws IllegalArgumentException if the request does not currently use
     * method POST or PUT and the body is non-empty. This is because Java's
     * HttpsURLConnection silently converts the HTTPS method to POST or PUT if a
     * body is written to the request.
     */
    public void writeOutput(byte[] body)
    {
        HttpMethod method = HttpMethod.valueOf(
            this.connection.getRequestMethod());
        if (method != HttpMethod.POST && method != HttpMethod.PUT)
        {
            if (body.length > 0)
            {
                throw new IllegalArgumentException(
                    "Cannot write a body to a request that "
                        + "is not a POST or a PUT request.");
            }
        }
        else
        {
            this.body = Arrays.copyOf(body, body.length);
        }
    }

    /**
     * Reads from the input stream (response stream) and returns the response.
     *
     * @return The response body.
     *
     * @throws IOException This exception thrown if the input stream could not be
     * accessed, for example if the server could not be reached.
     */
    public byte[] readInput() throws IOException
    {
        byte[] input;
        try (InputStream inputStream = this.connection.getInputStream())
        {
            input = readInputStream(inputStream);
        }

        return input;
    }

    /**
     * Reads from the error stream and returns the error reason.
     *
     * @return The error reason.
     *
     * @throws IOException This exception thrown if the input stream could not be
     * accessed, for example if the server could not be reached.
     */
    public byte[] readError() throws IOException
    {
        byte[] error = new byte[0];
        try (InputStream errorStream = this.connection.getErrorStream())
        {
            // if there is no error reason, getErrorStream() returns null.
            if (errorStream != null)
            {
                error = readInputStream(errorStream);
            }
        }

        return error;
    }

    /**
     * Returns the response status code.
     *
     * @return The response status code.
     *
     * @throws IOException This exception thrown if no response was received.
     */
    public int getResponseStatus() throws IOException
    {
        return this.connection.getResponseCode();
    }

    /**
     * Returns the response headers as a Map, where the key is the header field
     * name and the values are the values associated with the header field
     * name.
     *
     * @return the response headers.
     *
     */
    public Map<String, List<String>> getResponseHeaders()
    {
        return this.connection.getHeaderFields();
    }

    /**
     * Reads the input stream until the stream is empty.
     *
     * @param stream The input stream.
     *
     * @return The content of the input stream.
     *
     * @throws IOException This exception thrown if the input stream could not be read from.
     */
    private static byte[] readInputStream(InputStream stream)
        throws IOException
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

    void setSSLContext(SSLContext sslContext) throws IllegalArgumentException
    {
        if (sslContext == null)
        {
            throw new IllegalArgumentException("SSL context cannot be null");
        }

        this.connection.setSSLSocketFactory(sslContext.getSocketFactory());
    }

    protected HttpConnection()
    {
        this.connection = null;
    }
}
