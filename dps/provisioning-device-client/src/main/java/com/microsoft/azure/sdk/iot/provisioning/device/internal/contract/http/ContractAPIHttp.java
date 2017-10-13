/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.http;

import com.microsoft.azure.sdk.iot.deps.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpResponse;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.SDKUtils;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.UrlPathBuilder;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ContractAPIHttp extends ProvisioningDeviceClientContract
{
    private String scopeId;
    private String hostName;

    /*
     *  Values for Http header
     */
    private static final String AUTHORIZATION = "authorization";
    private static final String USER_AGENT = "User-Agent";
    private static final String ACCEPT = "Accept";
    private static final String ACCEPT_VALUE = "application/json";
    private static final String ACCEPT_CHARSET = "charset=utf-8";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final Integer DEFAULT_HTTP_TIMEOUT_MS = Integer.MAX_VALUE;
    private static final Integer ACCEPTABLE_NONCE_HTTP_STATUS = 404;

    /**
     * Constructor for Contract API HTTP
     * @param scopeId scope id used with the service Cannot be {@code null} or empty.
     * @param hostName host name for the service Cannot be {@code null} or empty.
     * @throws ProvisioningDeviceClientException is thrown when any of the input parameters are invalid
     */
    public ContractAPIHttp(String scopeId, String hostName) throws ProvisioningDeviceClientException
    {
        //SRS_ContractAPIHttp_25_002: [The constructor shall throw ProvisioningDeviceClientException if either scopeId and hostName are null or empty.]
        if (scopeId == null || scopeId.isEmpty())
        {
            throw new ProvisioningDeviceClientException("scope id cannot be null or empty");
        }

        if (hostName == null || hostName.isEmpty())
        {
            throw new ProvisioningDeviceClientException("host name cannot be null or empty");
        }

        //SRS_ContractAPIHttp_25_001: [The constructor shall save the scope id and hostname.]
        this.scopeId = scopeId;
        this.hostName = hostName;
    }

    private HttpRequest prepareRequest(
            URL url,
            HttpMethod method,
            byte[] payload,
            Integer timeoutInMs,
            Map<String, String> headersMap,
            String userAgentValue)
            throws IllegalArgumentException, IOException
    {
        HttpRequest request = null;

        if (url == null)
        {
            throw new IllegalArgumentException("Null URL");
        }

        if (method == null)
        {
            throw new IllegalArgumentException("Null method");
        }

        if (payload == null)
        {
            throw new IllegalArgumentException("Null payload");
        }

        if (timeoutInMs < 0)
        {
            throw new IllegalArgumentException("HTTP Request timeout shouldn't be negative");
        }

        request = new HttpRequest(url, method, payload);

        /*
            Set this method with appropriate time value once discussion with service concludes
            request.setReadTimeoutMillis(timeoutInMs);
        */

        request.setHeaderField(USER_AGENT, userAgentValue);

        request.setHeaderField(ACCEPT, ACCEPT_VALUE);

        request.setHeaderField(CONTENT_TYPE, ACCEPT_VALUE + "; " + ACCEPT_CHARSET);

        if (headersMap != null)
        {
            for (Map.Entry<String, String> header : headersMap.entrySet())
            {
                request.setHeaderField(header.getKey(), header.getValue());
            }
        }
        return request;
    }

    private HttpResponse sendRequest(HttpRequest request) throws ProvisioningDeviceHubException, ProvisioningDeviceConnectionException
    {
        try
        {
            HttpResponse response = request.send();
            ProvisioningDeviceClientExceptionManager.verifyHttpResponse(response);
            return response;
        }
        catch (IOException e)
        {
            throw new ProvisioningDeviceConnectionException(e.getMessage());
        }
    }

    /**
     * Requests hub to provide a device key to begin authentication over HTTP (Only for TPM)
     * @param payload payload used to send over this transport (Http)
     * @param registrationId A non {@code null} or empty value unique for registration
     * @param sslContext A non {@code null} value for SSL Context
     * @param responseCallback A non {@code null} value for the callback
     * @param dpsAuthorizationCallbackContext An object for context. Can be {@code null}
     * @throws ProvisioningDeviceClientException If any of the parameters are invalid ({@code null} or empty)
     * @throws ProvisioningDeviceTransportException If any of the API calls to transport fail
     * @throws ProvisioningDeviceHubException If hub responds back with status other than <300
     */
    public synchronized void requestNonceWithDPSTPM(byte[] payload, String registrationId, SSLContext sslContext, ResponseCallback responseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException
    {
        //SRS_ContractAPIHttp_25_003: [If either registrationId, sslcontext or responseCallback is null or if registrationId is empty then this method shall throw ProvisioningDeviceClientException.]
        if (registrationId == null || registrationId.isEmpty())
        {
            throw new ProvisioningDeviceClientException("registration Id cannot be null or empty");
        }

        if (sslContext == null)
        {
            throw new ProvisioningDeviceClientException("sslContext cannot be null");
        }

        if (responseCallback == null)
        {
            throw new ProvisioningDeviceClientException("responseCallback cannot be null");
        }

        try
        {
            //SRS_ContractAPIHttp_25_004: [This method shall retrieve the Url by calling 'generateRegisterUrl' on an object for UrlPathBuilder.]
            String url = new UrlPathBuilder(this.hostName, this.scopeId, ProvisioningDeviceClientTransportProtocol.HTTPS).generateRegisterUrl(registrationId);
            //SRS_ContractAPIHttp_25_005: [This method shall prepare the PUT request by setting following headers on a HttpRequest 1. User-Agent : User Agent String for the SDK 2. Accept : "application/json" 3. Content-Type: "application/json; charset=utf-8".]
            HttpRequest httpRequest = this.prepareRequest(new URL(url), HttpMethod.PUT, payload, DEFAULT_HTTP_TIMEOUT_MS, null, SDKUtils.getUserAgentString());
            //SRS_ContractAPIHttp_25_006: [This method shall set the SSLContext for the Http Request.]
            httpRequest.setSSLContext(sslContext);
            byte[] response = null;
            HttpResponse httpResponse = null;
            try
            {
                //SRS_ContractAPIHttp_25_007: [This method shall send http request and verify the status by calling 'ProvisioningDeviceClientExceptionManager.verifyHttpResponse'.]
                httpResponse = httpRequest.send();
                ProvisioningDeviceClientExceptionManager.verifyHttpResponse(httpResponse);
            }
            catch (ProvisioningDeviceHubException e)
            {
                //SRS_ContractAPIHttp_25_008: [If service return a status as 404 then this method shall trigger the callback to the user with the response message.]
                if (httpResponse.getStatus() == ACCEPTABLE_NONCE_HTTP_STATUS)
                {
                    response = e.getMessage().getBytes();
                    responseCallback.run(response, dpsAuthorizationCallbackContext);
                    return;
                }
                else
                {
                    //SRS_ContractAPIHttp_25_009: [If service return any other status other than 404 then this method shall throw ProvisioningDeviceTransportException in case of 202 or ProvisioningDeviceHubException on any other status.]
                    throw e;
                }
            }
        }
        catch (IOException e)
        {
            throw new ProvisioningDeviceTransportException(e.getMessage());
        }

        throw new ProvisioningDeviceTransportException("Service did not return any authorization request");
    }

    /**
     * Requests hub to authenticate this connection and start the registration process over HTTP
     * @param payload payload used to send over this transport (Http)
     * @param registrationId A non {@code null} or empty value unique for registration
     * @param authorization Value set for authorization can be {@code null}
     * @param sslContext A non {@code null} value for SSL Context
     * @param responseCallback A non {@code null} value for the callback
     * @param dpsAuthorizationCallbackContext An object for context. Can be {@code null}
     * @throws ProvisioningDeviceClientException If any of the parameters are invalid ({@code null} or empty)
     * @throws ProvisioningDeviceTransportException If any of the API calls to transport fail
     * @throws ProvisioningDeviceHubException If hub responds back with status other than <300
     */
    public synchronized void authenticateWithDPS(byte[] payload, String registrationId, SSLContext sslContext, String authorization, ResponseCallback responseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException
    {
        //SRS_ContractAPIHttp_25_011: [If either registrationId, sslcontext or responseCallback is null or if registrationId is empty then this method shall throw ProvisioningDeviceClientException.]
        if (registrationId == null || registrationId.isEmpty())
        {
            throw new ProvisioningDeviceClientException("registration Id cannot be null or empty");
        }

        if (sslContext == null)
        {
            throw new ProvisioningDeviceClientException("sslContext cannot be null");
        }

        if (responseCallback == null)
        {
            throw new ProvisioningDeviceClientException("responseCallback cannot be null");
        }

        try
        {
            //SRS_ContractAPIHttp_25_012: [This method shall retrieve the Url by calling 'generateRegisterUrl' on an object for UrlPathBuilder.]
            String url = new UrlPathBuilder(this.hostName, this.scopeId, ProvisioningDeviceClientTransportProtocol.HTTPS).generateRegisterUrl(registrationId);
            Map<String, String> headersMap = null;
            if(authorization != null)
            {
                headersMap = new HashMap<>();
                headersMap.put(AUTHORIZATION, authorization);
            }
            //SRS_ContractAPIHttp_25_013: [This method shall prepare the PUT request by setting following headers on a HttpRequest 1. User-Agent : User Agent String for the SDK 2. Accept : "application/json" 3. Content-Type: "application/json; charset=utf-8" 4. Authorization: specified sas token as authorization if a non null value is given.]
            HttpRequest httpRequest = this.prepareRequest(new URL(url), HttpMethod.PUT, payload, DEFAULT_HTTP_TIMEOUT_MS, headersMap, SDKUtils.getUserAgentString());
            //SRS_ContractAPIHttp_25_014: [This method shall set the SSLContext for the Http Request.]
            httpRequest.setSSLContext(sslContext);
            //SRS_ContractAPIHttp_25_015: [This method shall send http request and verify the status by calling 'ProvisioningDeviceClientExceptionManager.verifyHttpResponse'.]
            //SRS_ContractAPIHttp_25_017: [If service return any other status other than <300 then this method shall throw ProvisioningDeviceHubException.]
            HttpResponse httpResponse = this.sendRequest(httpRequest);
            //SRS_ContractAPIHttp_25_016: [If service return a status as < 300 then this method shall trigger the callback to the user with the response message.]
            responseCallback.run(httpResponse.getBody(), dpsAuthorizationCallbackContext);
        }
        catch (IOException e)
        {
            throw new ProvisioningDeviceTransportException(e.getMessage());
        }
    }

    /**
     * Gets the registration status over HTTP
     * @param operationId A non {@code null} value for the operation
     * @param registrationId A non {@code null} or empty value unique for registration
     * @param dpsAuthorization Value set for authorization can be {@code null}
     * @param sslContext A non {@code null} value for SSL Context
     * @param responseCallback A non {@code null} value for the callback
     * @param dpsAuthorizationCallbackContext An object for context. Can be {@code null}
     * @throws ProvisioningDeviceClientException If any of the parameters are invalid ({@code null} or empty)
     * @throws ProvisioningDeviceTransportException If any of the API calls to transport fail
     * @throws ProvisioningDeviceHubException If hub responds back with status other than <300
     */
    public synchronized void getRegistrationStatus(String operationId, String registrationId, String dpsAuthorization, SSLContext sslContext, ResponseCallback responseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException
    {
        //SRS_ContractAPIHttp_25_018: [If either operationId, registrationId, sslcontext or responseCallback is null or if operationId, registrationId is empty then this method shall throw ProvisioningDeviceClientException.]
        if (operationId == null || operationId.isEmpty())
        {
            throw new ProvisioningDeviceClientException("operationId cannot be null or empty");
        }

        if (registrationId == null || registrationId.isEmpty())
        {
            throw new ProvisioningDeviceClientException("registration Id cannot be null or empty");
        }

        if (sslContext == null)
        {
            throw new ProvisioningDeviceClientException("sslContext cannot be null");
        }

        if (responseCallback == null)
        {
            throw new ProvisioningDeviceClientException("responseCallback cannot be null");
        }

        try
        {
            //SRS_ContractAPIHttp_25_019: [This method shall retrieve the Url by calling generateRequestUrl on an object for UrlPathBuilder.]
            String url = new UrlPathBuilder(this.hostName, this.scopeId, ProvisioningDeviceClientTransportProtocol.HTTPS).generateRequestUrl(registrationId, operationId);
            Map<String, String> headersMap = null;
            if (dpsAuthorization != null)
            {
                headersMap = new HashMap<>();
                headersMap.put(AUTHORIZATION, dpsAuthorization);
            }
            //SRS_ContractAPIHttp_25_020: [This method shall prepare the GET request by setting following headers on a HttpRequest 1. User-Agent : User Agent String for the SDK 2. Accept : "application/json" 3. Content-Type: "application/json; charset=utf-8" 4. Authorization: specified sas token as authorization if a non null value is given.]
            HttpRequest httpRequest = this.prepareRequest(new URL(url), HttpMethod.GET, new byte[0], DEFAULT_HTTP_TIMEOUT_MS, headersMap, SDKUtils.getUserAgentString());
            //SRS_ContractAPIHttp_25_021: [This method shall set the SSLContext for the Http Request.]
            httpRequest.setSSLContext(sslContext);
            //SRS_ContractAPIHttp_25_022: [This method shall send http request and verify the status by calling 'ProvisioningDeviceClientExceptionManager.verifyHttpResponse'.]
            //SRS_ContractAPIHttp_25_024: [If service return any other status other than < 300 then this method shall throw ProvisioningDeviceHubException.]
            HttpResponse httpResponse = this.sendRequest(httpRequest);
            //SRS_ContractAPIHttp_25_023: [If service return a status as < 300 then this method shall trigger the callback to the user with the response message.]
            responseCallback.run(httpResponse.getBody(), dpsAuthorizationCallbackContext);
        }
        catch (IOException e)
        {
            throw new ProvisioningDeviceTransportException(e.getMessage());
        }
    }
}
