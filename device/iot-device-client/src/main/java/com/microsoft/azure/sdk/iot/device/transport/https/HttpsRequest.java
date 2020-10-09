// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.ProxySettings;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

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
        // Codes_SRS_HTTPSREQUEST_34_031: [The function shall save the provided arguments to be used when the http connection is built during the call to send().]
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

        int responseStatus = -1;
        byte[] responseBody = new byte[0];
        byte[] errorReason = new byte[0];
        Map<String, List<String>> headerFields;

        // Codes_SRS_HTTPSREQUEST_11_008: [The function shall send an HTTPS request as formatted in the constructor.]
        connection.connect();

        responseStatus = connection.getResponseStatus();
        headerFields = connection.getResponseHeaders();

        if (responseStatus == 200)
        {
            responseBody = connection.readInput();
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
        // Codes_SRS_HTTPSREQUEST_11_014: [The function shall set the read timeout for the request to the given value.]
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
            //Codes_SRS_HTTPSREQUEST_25_015: [The function shall throw IllegalArgumentException if argument is null .]
            throw new IllegalArgumentException("Context cannot be null");
        }

        this.sslContext = sslContext;

        return this;
    }

    public byte[] getBody()
    {
        // Codes_SRS_HTTPSREQUEST_34_017: [The function shall return the body saved in this object's connection instance.]
        return this.body;
    }

    public URL getRequestUrl()
    {
        // Codes_SRS_HTTPSREQUEST_34_018: [The function shall return the request url saved in this object's connection instance.]
        return this.url;
    }

    public String getHttpMethod()
    {
        // Codes_SRS_HTTPSREQUEST_34_019: [The function shall return the http method saved in this object's connection instance.]
        return this.method.toString();
    }

    public String getRequestHeaders()
    {
        String headerString = "";

        for (String key : this.headers.keySet())
        {
            headerString += (key);
            headerString += ": ";

            for (String value : this.headers.get(key))
            {
                headerString += value;
                headerString += "; ";
            }
            headerString = headerString.substring(0, headerString.length() - 2);

            headerString += "\r\n";
        }

        //Codes_SRS_HTTPSCONNECTION_34_030: [The function shall return all the request headers in the format "<key>: <value1>; <value2>\r\n <key>: <value1>\r\n...".]
        return headerString;
    }

    @SuppressWarnings("unused")
    protected HttpsRequest()
    {
    }
}
