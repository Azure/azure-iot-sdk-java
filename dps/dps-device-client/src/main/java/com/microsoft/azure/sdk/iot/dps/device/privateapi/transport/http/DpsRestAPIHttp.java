/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.dps.device.privateapi.transport.http;

import com.microsoft.azure.sdk.iot.deps.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpResponse;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.dpstask.DPSRestResponseCallback;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.exceptions.*;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.*;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.azure.sdk.iot.dps.device.DPSTransportProtocol.HTTPS;

public class DpsRestAPIHttp extends DPSTransport
{
    private String scopeId;
    private String hostName;

    /**
     *  Values for Http header
     */
    private static final String AUTHORIZATION = "authorization";
    private static final String USER_AGENT = "User-Agent";
    private static final String ACCEPT = "Accept";
    private static final String ACCEPT_VALUE = "application/json";
    private static final String ACCEPT_CHARSET = "charset=utf-8";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final Integer DEFAULT_HTTP_TIMEOUT_MS = Integer.MAX_VALUE;

    public DpsRestAPIHttp(String scopeId, String hostName) throws DPSTransportException
    {
        this.scopeId = scopeId;
        this.hostName = hostName;
    }

    private HttpRequest prepareRequest(
            URL url,
            HttpMethod method,
            byte[] payload,
            int timeoutInMs,
            Map<String, String> headersMap,
            String userAgentValue)
            throws DPSConnectionException, DPSHubException, IllegalArgumentException
    {
        HttpRequest request = null;
        /* Codes_SRS_DEVICE_OPERATIONS_21_002: [The request shall throw IllegalArgumentException if the provided `url` is null.] */
        if (url == null)
        {
            throw new IllegalArgumentException("Null URL");
        }

        /* Codes_SRS_DEVICE_OPERATIONS_21_003: [The request shall throw IllegalArgumentException if the provided `method` is null.] */
        if (method == null)
        {
            throw new IllegalArgumentException("Null method");
        }

        /* Codes_SRS_DEVICE_OPERATIONS_21_004: [The request shall throw IllegalArgumentException if the provided `payload` is null.] */
        if (payload == null)
        {
            throw new IllegalArgumentException("Null payload");
        }

        /* Codes_SRS_DEVICE_OPERATIONS_99_018: [The request shall throw IllegalArgumentException if the provided `timeoutInMs` exceed Integer.MAX_VALUE.] */
        if ((timeoutInMs + DEFAULT_HTTP_TIMEOUT_MS) > Integer.MAX_VALUE)
        {
            throw new IllegalArgumentException("HTTP Request timeout shouldn't not exceed " + timeoutInMs + DEFAULT_HTTP_TIMEOUT_MS + " milliseconds");
        }

        try
        {
        /* Codes_SRS_DEVICE_OPERATIONS_21_008: [The request shall create a new HttpRequest with the provided `url`, http `method`, and `payload`.] */
            request = new HttpRequest(url, method, payload);
        }
        catch (IOException e)
        {
            throw new DPSConnectionException(e.getMessage());
        }

        /* Codes_SRS_DEVICE_OPERATIONS_21_009: [The request shall add to the HTTP header the sum of timeout and default timeout in milliseconds.] */
        //request.setReadTimeoutMillis(timeoutInMs + DEFAULT_HTTP_TIMEOUT_MS);

        /* Codes_SRS_DEVICE_OPERATIONS_21_012: [The request shall add to the HTTP header a `User-Agent` key with the client Id and service version.] */
        request.setHeaderField(USER_AGENT, userAgentValue);

        /* Codes_SRS_DEVICE_OPERATIONS_21_013: [The request shall add to the HTTP header a `Accept` key with `application/json`.] */
        request.setHeaderField(ACCEPT, ACCEPT_VALUE);

        /* Codes_SRS_DEVICE_OPERATIONS_21_014: [The request shall add to the HTTP header a `Content-Type` key with `application/json; charset=utf-8`.] */
        request.setHeaderField(CONTENT_TYPE, ACCEPT_VALUE + "; " + ACCEPT_CHARSET);

        if (headersMap != null)
        {
            //SRS_DEVICE_OPERATIONS_25_019: [The request shall add to the HTTP header all the additional custom headers set for this request.]
            for (Map.Entry<String, String> header : headersMap.entrySet())
            {
                request.setHeaderField(header.getKey(), header.getValue());
            }
        }
        return request;
    }

    private HttpResponse sendRequest(HttpRequest request) throws DPSHubException, DPSConnectionException
    {
        HttpResponse response = null;
        try
        {

        /* Codes_SRS_DEVICE_OPERATIONS_21_015: [The request shall send the created request and get the response.] */
            response = request.send();
            DPSExceptionManager.verifyHttpResponse(response);
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
            throw new DPSConnectionException(e.getMessage());
        }

        /* Codes_SRS_DEVICE_OPERATIONS_21_017: [If the resulted status represents success, the request shall return the http response.] */
        return response;
    }

    public synchronized void requestNonceWithDPSTPM(byte[] payload, String registrationId, SSLContext sslContext, DPSRestResponseCallback dpsRestResponseCallback, Object dpsAuthorizationCallbackContext) throws DPSClientException, DPSTransportException, DPSHubException
    {
        //parameter check here ...

        try
        {
            String url = new DPSGenerateUrl(this.hostName, this.scopeId, HTTPS).generateRegisterUrl(registrationId);
            HttpRequest httpRequest = prepareRequest(new URL(url), HttpMethod.PUT, payload, DEFAULT_HTTP_TIMEOUT_MS, null, SDKUtils.getUserAgentString());
            httpRequest.setSSLContext(sslContext);
            HttpResponse httpResponse = sendRequest(httpRequest);
            if (dpsRestResponseCallback != null)
            {
                dpsRestResponseCallback.run(httpResponse.getBody(), dpsAuthorizationCallbackContext);
            }
            else
            {
                throw new DPSClientException("Return call back unspecified");
            }
        }
        catch (IOException e)
        {
            throw new DPSTransportException(e.getMessage());
        }
    }

    public synchronized void authenticateWithDPS(byte[] payload, String registrationId, SSLContext sslContext, String authorization, DPSRestResponseCallback dpsRestResponseCallback, Object dpsAuthorizationCallbackContext) throws DPSClientException, DPSTransportException, DPSHubException
    {
        //parameter check here ...

        try
        {
            String url = new DPSGenerateUrl(this.hostName, this.scopeId, HTTPS).generateRegisterUrl(registrationId);
            Map<String, String> headersMap = null;
            if(authorization != null)
            {
                headersMap = new HashMap<>();
                headersMap.put(AUTHORIZATION, authorization);
            }
            HttpRequest httpRequest = prepareRequest(new URL(url), HttpMethod.PUT, payload, DEFAULT_HTTP_TIMEOUT_MS, headersMap, SDKUtils.getUserAgentString());
            httpRequest.setSSLContext(sslContext);
            HttpResponse httpResponse = sendRequest(httpRequest);
            if (dpsRestResponseCallback != null)
            {
                dpsRestResponseCallback.run(httpResponse.getBody(), dpsAuthorizationCallbackContext);
            }
            else
            {
                throw new DPSClientException("Return call back unspecified");
            }
        }
        catch (IOException e)
        {
            throw new DPSTransportException(e.getMessage());
        }

    }

    public synchronized void getRegistrationStatus(String operationId, String registrationId, String dpsAuthorization, SSLContext sslContext, DPSRestResponseCallback dpsRestResponseCallback, Object dpsAuthorizationCallbackContext) throws DPSClientException, DPSTransportException, DPSHubException
    {
        //parameter check here ...

        try
        {
            String url = new DPSGenerateUrl(this.hostName, this.scopeId, HTTPS).generateRequestUrl(registrationId, operationId);
            Map<String, String> headersMap = null;
            if (dpsAuthorization != null)
            {
                headersMap = new HashMap<>();
                headersMap.put(AUTHORIZATION, dpsAuthorization);
            }

            HttpRequest httpRequest = prepareRequest(new URL(url), HttpMethod.GET, new byte[0], DEFAULT_HTTP_TIMEOUT_MS, headersMap, SDKUtils.getUserAgentString());
            httpRequest.setSSLContext(sslContext);
            HttpResponse httpResponse = httpRequest.send();
            if (dpsRestResponseCallback != null)
            {
                dpsRestResponseCallback.run(httpResponse.getBody(), dpsAuthorizationCallbackContext);
            }
            else
            {
                throw new DPSClientException("Return call back unspecified");
            }
        }
        catch (IOException e)
        {
            throw new DPSTransportException(e.getMessage());
        }
    }
}
