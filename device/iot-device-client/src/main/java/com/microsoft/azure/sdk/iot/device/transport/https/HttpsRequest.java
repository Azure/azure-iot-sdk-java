// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.ProxySettings;
import com.microsoft.azure.sdk.iot.device.transport.TransportException;

import javax.net.ssl.SSLContext;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An HTTPS request.
 */
public class HttpsRequest
{
    private byte[] body;
    private HttpsMethod method;
    private URL url;
    private Map<String, List<String>> headers;
    private int readTimeout;
    private int connectTimeout;
    private SSLContext sslContext;
    private ProxySettings proxySettings;

    /**
     * Constructor. Takes a URL as an argument and returns an HTTPS request that
     * is ready to be sent.
     *
     * @param url the URL for the request.
     * @param method the HTTPS request method (i.e. GET).
     * @param body the request body. Must be an array of size 0 if the request
     * method is GET or DELETE.
     * @param userAgentString the user agent string to attach to all http communications
     */
    public HttpsRequest(URL url, HttpsMethod method, byte[] body, String userAgentString)
    {
        this(url, method, body, userAgentString, null);
    }

    /**
     * Constructor. Takes a URL as an argument and returns an HTTPS request that
     * is ready to be sent.
     *
     * @param url the URL for the request.
     * @param method the HTTPS request method (i.e. GET).
     * @param body the request body. Must be an array of size 0 if the request
     * method is GET or DELETE.
     * @param userAgentString the user agent string to attach to all http communications
     * @param proxySettings The proxy settings to use when connecting. If null then no proxy will be used
     */
    public HttpsRequest(URL url, HttpsMethod method, byte[] body, String userAgentString, ProxySettings proxySettings)
    {
        this.url = url;
        this.method = method;
        this.body = body;
        headers = new HashMap<>();

        List<String> hostHeaderValues = new ArrayList<>();
        if (url != null && url.getHost() != null && !url.getHost().isEmpty())
        {
            String host = url.getHost();
            if (url.getPort() != -1)
            {
                host += ":" + url.getPort();
            }
            hostHeaderValues.add(host);

            headers.put("Host", hostHeaderValues);
        }

        if (userAgentString != null && !userAgentString.isEmpty())
        {
            List<String> headerValues = new ArrayList<>();
            headerValues.add(userAgentString);
            headers.put("User-Agent", headerValues);
        }

        this.proxySettings = proxySettings;
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
        return send(true);
    }

    /**
     * Executes the HTTPS request as an HTTP request. This method should only be called when a user supplied
     * url contains HTTP rather than HTTPS. Currently, this only happens from the {@link com.microsoft.azure.sdk.iot.device.hsm.HttpsHsmClient}
     * for some edge workload urls.
     *
     * @return an HTTPS response.
     *
     * @throws TransportException if the connection could not be established, or the
     * input/output streams could not be accessed.
     */
    public HttpsResponse sendAsHttpRequest() throws TransportException
    {
        return send(false);
    }

    /**
     * Executes the HTTPS request.
     *
     * @param isHttps if true, the request will be sent as an HTTPS request. Otherwise it will be sent as an Http request
     * @return an HTTPS response.
     *
     * @throws TransportException if the connection could not be established, or the
     * input/output streams could not be accessed.
     */
    private HttpsResponse send(boolean isHttps) throws TransportException
    {
        if (this.url == null)
        {
            throw new IllegalArgumentException("url cannot be null");
        }

        HttpsConnection connection = new HttpsConnection(url, method, this.proxySettings, isHttps);

        for (String headerKey : headers.keySet())
        {
            for (String headerValue : this.headers.get(headerKey))
            {
                connection.setRequestHeader(headerKey, headerValue);
            }
        }

        connection.writeOutput(this.body);

        if (this.sslContext != null && isHttps)
        {
            connection.setSSLContext(this.sslContext);
        }

        if (this.readTimeout != 0)
        {
            connection.setReadTimeout(this.readTimeout);
        }

        if (this.connectTimeout != 0)
        {
            connection.setConnectTimeout(this.connectTimeout);
        }

        int responseStatus;
        byte[] responseBody = new byte[0];
        byte[] errorReason = new byte[0];
        Map<String, List<String>> headerFields;

        connection.connect();

        responseStatus = connection.getResponseStatus();
        headerFields = connection.getResponseHeaders();

        if (responseStatus == 200)
        {
            responseBody = connection.readInput();
        }

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
        if (this.headers.containsKey(field))
        {
            this.headers.get(field).add(value);
        }
        else
        {
            List<String> headerValues = new ArrayList<>();
            headerValues.add(value);
            this.headers.put(field, headerValues);
        }

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
    public HttpsRequest setReadTimeout(int timeout)
    {
        this.readTimeout = timeout;
        return this;
    }

    /**
     * Sets the connect timeout, in milliseconds, for the request.
     *
     * @param timeout the connect timeout in milliseconds.
     *
     * @return itself, for fluent setting.
     */
    public HttpsRequest setConnectTimeout(int timeout)
    {
        this.connectTimeout = timeout;
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
            throw new IllegalArgumentException("Context cannot be null");
        }

        this.sslContext = sslContext;

        return this;
    }

    public byte[] getBody()
    {
        return this.body;
    }

    public URL getRequestUrl()
    {
        return this.url;
    }

    public String getHttpMethod()
    {
        return this.method.toString();
    }

    public String getRequestHeaders()
    {
        StringBuilder headerString = new StringBuilder();

        for (String key : this.headers.keySet())
        {
            headerString.append(key);
            headerString.append(": ");

            for (String value : this.headers.get(key))
            {
                headerString.append(value);
                headerString.append("; ");
            }
            headerString = new StringBuilder(headerString.substring(0, headerString.length() - 2));

            headerString.append("\r\n");
        }

        return headerString.toString();
    }

    @SuppressWarnings("unused")
    protected HttpsRequest()
    {
    }
}
