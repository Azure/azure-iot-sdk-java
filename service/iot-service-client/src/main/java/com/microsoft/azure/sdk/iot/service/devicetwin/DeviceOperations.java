// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.microsoft.azure.sdk.iot.deps.auth.TokenCredentialType;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.Tools;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

/**
 * Set of common operations for Twin and Method.
 */
public class DeviceOperations
{
    /**
     *  Values for Http header
     */
    private static final String AUTHORIZATION = "authorization";
    private static final String REQUEST_ID = "Request-Id";
    private static final String USER_AGENT = "User-Agent";
    private static final String ACCEPT = "Accept";
    private static final String ACCEPT_VALUE = "application/json";
    private static final String ACCEPT_CHARSET = "charset=utf-8";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final Integer DEFAULT_HTTP_TIMEOUT_MS = 24000;
    private static Map<String, String> headers = null;

    /**
     * Send a http request to the IoTHub using the Twin/Method standard, and return its response.
     * 
     * @param iotHubConnectionString is the connection string for the IoTHub.
     * @param url is the Twin URL for the device ID.
     * @param method is the HTTP method (GET, POST, DELETE, PATCH, PUT).
     * @param payload is the array of bytes that contains the payload.
     * @param requestId is an unique number that identify the request.
     * @param timeoutInMs is timeout in milliseconds.
     * @return the result of the request.
     * @throws IotHubException This exception is thrown if the response verification failed.
     * @throws IOException This exception is thrown if the IO operation failed.
     * @deprecated use {@link #request(IotHubConnectionString, URL, HttpMethod, byte[], String, int, int, Proxy)} instead.
     */
    @Deprecated
    public static HttpResponse request(
            IotHubConnectionString iotHubConnectionString, 
            URL url, 
            HttpMethod method, 
            byte[] payload, 
            String requestId,
            long timeoutInMs) 
            throws IOException, IotHubException, IllegalArgumentException
    {
        if (iotHubConnectionString == null)
        {
            throw new IllegalArgumentException("Http requests must provide a non-null connection string");
        }

        if (url == null)
        {
            throw new IllegalArgumentException("Http requests must provide a non-null URL");
        }

        if (method == null)
        {
            throw new IllegalArgumentException("Http requests must provide a non-null http method");
        }

        if (payload == null)
        {
            // This is an odd requirement, but since we won't use this API since its deprecation, it will stay.
            throw new IllegalArgumentException("Http requests must provide a non-null URL");
        }

        if ((timeoutInMs + DEFAULT_HTTP_TIMEOUT_MS) > Integer.MAX_VALUE)
        {
            throw new IllegalArgumentException("HTTP Request timeout shouldn't not exceed " + timeoutInMs + DEFAULT_HTTP_TIMEOUT_MS + " milliseconds");
        }

        String sasTokenString = new IotHubServiceSasToken(iotHubConnectionString).toString();
         if (Tools.isNullOrEmpty(sasTokenString))
        {
            throw new IOException("Illegal sasToken null or empty");
        }

        HttpRequest request = new HttpRequest(url, method, payload);
        request.setReadTimeoutMillis((int)(timeoutInMs + DEFAULT_HTTP_TIMEOUT_MS));
        request.setHeaderField(AUTHORIZATION, sasTokenString);

        if (Tools.isNullOrEmpty(requestId))
        {
            request.setHeaderField(REQUEST_ID, requestId);
        }

        request.setHeaderField(USER_AGENT, TransportUtils.javaServiceClientIdentifier + TransportUtils.serviceVersion);

        request.setHeaderField(ACCEPT, ACCEPT_VALUE);

        request.setHeaderField(CONTENT_TYPE, ACCEPT_VALUE + "; " + ACCEPT_CHARSET);

        if (headers != null)
        {
            for (Map.Entry<String, String> header : headers.entrySet())
            {
                request.setHeaderField(header.getKey(), header.getValue());
            }

            headers = null;
        }

        HttpResponse response = request.send();
        IotHubExceptionManager.httpResponseVerification(response);
        return response;
    }

    /**
     * Send a http request to the IoTHub using the Twin/Method standard, and return its response.
     *
     * @param iotHubConnectionString is the connection string for the IoTHub.
     * @param url is the Twin URL for the device ID.
     * @param method is the HTTP method (GET, POST, DELETE, PATCH, PUT).
     * @param payload is the array of bytes that contains the payload.
     * @param requestId is an unique number that identify the request.
     * @param connectTimeout the http connect timeout to use, in milliseconds.
     * @param readTimeout the http read timeout to use, in milliseconds.
     * @param proxy the proxy to use, or null if no proxy will be used.
     * @return the result of the request.
     * @throws IotHubException This exception is thrown if the response verification failed.
     * @throws IOException This exception is thrown if the IO operation failed.
     */
    public static HttpResponse request(
            IotHubConnectionString iotHubConnectionString,
            URL url,
            HttpMethod method,
            byte[] payload,
            String requestId,
            int connectTimeout,
            int readTimeout,
            Proxy proxy)
            throws IOException, IotHubException, IllegalArgumentException
    {
        if (iotHubConnectionString == null)
        {
            throw new IllegalArgumentException("Http requests must provide a non-null connection string");
        }

        if (url == null)
        {
            throw new IllegalArgumentException("Http requests must provide a non-null URL");
        }

        if (method == null)
        {
            throw new IllegalArgumentException("Http requests must provide a non-null http method");
        }

        String sasTokenString = new IotHubServiceSasToken(iotHubConnectionString).toString();
        if (Tools.isNullOrEmpty(sasTokenString))
        {
            throw new IOException("Illegal sasToken null or empty");
        }

        HttpRequest request;
        if (proxy != null)
        {
            request = new HttpRequest(url, method, payload, proxy);
        }
        else
        {
            request = new HttpRequest(url, method, payload);
        }

        request.setReadTimeoutMillis(readTimeout);
        request.setConnectTimeoutMillis(connectTimeout);

        if (Tools.isNullOrEmpty(requestId))
        {
            request.setHeaderField(REQUEST_ID, requestId);
        }

        request.setHeaderField(AUTHORIZATION, sasTokenString);
        request.setHeaderField(USER_AGENT, TransportUtils.javaServiceClientIdentifier + TransportUtils.serviceVersion);
        request.setHeaderField(ACCEPT, ACCEPT_VALUE);
        request.setHeaderField(CONTENT_TYPE, ACCEPT_VALUE + "; " + ACCEPT_CHARSET);

        if (headers != null)
        {
            for (Map.Entry<String, String> header : headers.entrySet())
            {
                request.setHeaderField(header.getKey(), header.getValue());
            }

            headers = null;
        }

        HttpResponse response = request.send();
        IotHubExceptionManager.httpResponseVerification(response);
        return response;
    }

    /**
     * Send a http request to the IoTHub using the Twin/Method standard, and return its response.
     *
     * @param authenticationTokenProvider The authentication token provider that will be used to authorize the request
     * @param url is the Twin URL for the device ID.
     * @param method is the HTTP method (GET, POST, DELETE, PATCH, PUT).
     * @param payload is the array of bytes that contains the payload.
     * @param requestId is an unique number that identify the request.
     * @param connectTimeout the http connect timeout to use, in milliseconds.
     * @param readTimeout the http read timeout to use, in milliseconds.
     * @param proxy the proxy to use, or null if no proxy will be used.
     * @return the result of the request.
     * @throws IotHubException This exception is thrown if the response verification failed.
     * @throws IOException This exception is thrown if the IO operation failed.
     */
    public static HttpResponse request(
            TokenCredential authenticationTokenProvider,
            URL url,
            HttpMethod method,
            byte[] payload,
            String requestId,
            int connectTimeout,
            int readTimeout,
            Proxy proxy)
            throws IOException, IotHubException, IllegalArgumentException
    {
        Objects.requireNonNull(authenticationTokenProvider);

        if (url == null)
        {
            throw new IllegalArgumentException("Http requests must provide a non-null URL");
        }

        if (method == null)
        {
            throw new IllegalArgumentException("Http requests must provide a non-null http method");
        }

        HttpRequest request;
        if (proxy != null)
        {
            request = new HttpRequest(url, method, payload, proxy);
        }
        else
        {
            request = new HttpRequest(url, method, payload);
        }

        request.setReadTimeoutMillis(readTimeout);
        request.setConnectTimeoutMillis(connectTimeout);

        if (Tools.isNullOrEmpty(requestId))
        {
            request.setHeaderField(REQUEST_ID, requestId);
        }

        String accessToken = authenticationTokenProvider.getToken(new TokenRequestContext()).block().getToken();
        request.setHeaderField(AUTHORIZATION, accessToken);
        request.setHeaderField(USER_AGENT, TransportUtils.javaServiceClientIdentifier + TransportUtils.serviceVersion);
        request.setHeaderField(ACCEPT, ACCEPT_VALUE);
        request.setHeaderField(CONTENT_TYPE, ACCEPT_VALUE + "; " + ACCEPT_CHARSET);

        if (headers != null)
        {
            for (Map.Entry<String, String> header : headers.entrySet())
            {
                request.setHeaderField(header.getKey(), header.getValue());
            }

            headers = null;
        }

        HttpResponse response = request.send();
        IotHubExceptionManager.httpResponseVerification(response);
        return response;
    }

    /**
     * Sets headers to be used on next HTTP request.
     * @param httpHeaders non null and non empty custom headers.
     * @throws IllegalArgumentException This exception is thrown if headers were null or empty.
     */
    public static void setHeaders(Map<String, String> httpHeaders) throws IllegalArgumentException
    {
        if (httpHeaders == null || httpHeaders.size() == 0)
        {
            throw new IllegalArgumentException("Null or Empty headers can't be set");
        }

        headers = httpHeaders;
    }
}
