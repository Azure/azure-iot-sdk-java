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
import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.SDKUtils;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.UrlPathBuilder;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.DeviceRegistrationParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.TpmRegistrationResultParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ContractState;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.RequestData;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ResponseData;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ContractAPIHttp extends ProvisioningDeviceClientContract
{
    private String idScope;
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
    private static final Integer ACCEPTABLE_NONCE_HTTP_STATUS = 401;

    @Override
    public void open(RequestData requestData) throws ProvisioningDeviceConnectionException
    {
        // dummy call for Http
    }

    @Override
    public void close() throws ProvisioningDeviceConnectionException
    {
        // dummy call for Http
    }

    /**
     * Constructor for Contract API HTTP
     * @param provisioningDeviceClientConfig Config used for provisioning Cannot be {@code null}.
     * @throws ProvisioningDeviceClientException is thrown when any of the input parameters are invalid
     */
    public ContractAPIHttp(ProvisioningDeviceClientConfig provisioningDeviceClientConfig) throws ProvisioningDeviceClientException
    {
        //SRS_ContractAPIHttp_25_002: [The constructor shall throw ProvisioningDeviceClientException if either idScope and hostName are null or empty.]
        String idScope = provisioningDeviceClientConfig.getIdScope();
        if (idScope == null || idScope.isEmpty())
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("scope id cannot be null or empty"));
        }
        String hostName = provisioningDeviceClientConfig.getProvisioningServiceGlobalEndpoint();
        if (hostName == null || hostName.isEmpty())
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("host name cannot be null or empty"));
        }

        //SRS_ContractAPIHttp_25_001: [The constructor shall save the scope id and hostname.]
        this.idScope = idScope;
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

    private HttpResponse sendRequest(HttpRequest request) throws ProvisioningDeviceHubException, IOException
    {
        HttpResponse response = request.send();
        ProvisioningDeviceClientExceptionManager.verifyHttpResponse(response);
        return response;
    }

    /**
     * Requests hub to provide a device key to begin authentication over HTTP (Only for TPM)
     * @param responseCallback A non {@code null} value for the callback
     * @param dpsAuthorizationCallbackContext An object for context. Can be {@code null}
     * @param requestData A non {@code null} value with all the required request data
     * @throws ProvisioningDeviceClientException If any of the parameters are invalid ({@code null} or empty)
     * @throws ProvisioningDeviceTransportException If any of the API calls to transport fail
     * @throws ProvisioningDeviceHubException If hub responds back with status other than 300 or less
     */
    public synchronized void requestNonceForTPM(RequestData requestData, ResponseCallback responseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException
    {
        //SRS_ContractAPIHttp_25_003: [If either registrationId, sslcontext or responseCallback is null or if registrationId is empty then this method shall throw ProvisioningDeviceClientException.]
        if (requestData.getRegistrationId() == null || requestData.getRegistrationId().isEmpty())
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("registration Id cannot be null or empty"));
        }

        if (requestData.getEndorsementKey() == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("Endorsement key cannot be null"));
        }

        if (requestData.getStorageRootKey() == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("Storage root key cannot be null"));
        }

        if (requestData.getSslContext() == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("sslContext cannot be null"));
        }

        if (responseCallback == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("responseCallback cannot be null"));
        }

        try
        {
            //SRS_ContractAPIHttp_25_004: [This method shall retrieve the Url by calling 'generateRegisterUrl' on an object for UrlPathBuilder.]
            String url = new UrlPathBuilder(this.hostName, this.idScope, ProvisioningDeviceClientTransportProtocol.HTTPS).generateRegisterUrl(requestData.getRegistrationId());
            String base64EncodedEk = new String(Base64.encodeBase64Local(requestData.getEndorsementKey()));
            String base64EncodedSrk = new String(Base64.encodeBase64Local(requestData.getStorageRootKey()));
            //SRS_ContractAPIHttp_25_025: [ This method shall build the required Json input using parser. ]
            byte[] payload = new DeviceRegistrationParser(requestData.getRegistrationId(), base64EncodedEk, base64EncodedSrk).toJson().getBytes();
            //SRS_ContractAPIHttp_25_005: [This method shall prepare the PUT request by setting following headers on a HttpRequest 1. User-Agent : User Agent String for the SDK 2. Accept : "application/json" 3. Content-Type: "application/json; charset=utf-8".]
            HttpRequest httpRequest = this.prepareRequest(new URL(url), HttpMethod.PUT, payload, DEFAULT_HTTP_TIMEOUT_MS, null, SDKUtils.getUserAgentString());
            //SRS_ContractAPIHttp_25_006: [This method shall set the SSLContext for the Http Request.]
            httpRequest.setSSLContext(requestData.getSslContext());
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
                    TpmRegistrationResultParser registerResponseTPMParser = TpmRegistrationResultParser.createFromJson(new String(e.getMessage()));
                    byte[] base64DecodedAuthKey = Base64.decodeBase64Local(registerResponseTPMParser.getAuthenticationKey().getBytes());
                    responseCallback.run(new ResponseData(base64DecodedAuthKey, ContractState.DPS_REGISTRATION_RECEIVED, 0), dpsAuthorizationCallbackContext);
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
            throw new ProvisioningDeviceTransportException(e);
        }

        throw new ProvisioningDeviceTransportException("Service did not return any authorization request");
    }

    /**
     * Requests hub to authenticate this connection and start the registration process over HTTP
     * @param requestData A non {@code null} value with all the required request data
     * @param responseCallback A non {@code null} value for the callback
     * @param dpsAuthorizationCallbackContext An object for context. Can be {@code null}
     * @throws ProvisioningDeviceClientException If any of the parameters are invalid ({@code null} or empty)
     * @throws ProvisioningDeviceTransportException If any of the API calls to transport fail
     * @throws ProvisioningDeviceHubException If hub responds back with status other than 300 or less
     */
    public synchronized void authenticateWithProvisioningService(RequestData requestData, ResponseCallback responseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException
    {
        //SRS_ContractAPIHttp_25_011: [If either registrationId, sslcontext or responseCallback is null or if registrationId is empty then this method shall throw ProvisioningDeviceClientException.]
        if (requestData.getRegistrationId() == null || requestData.getRegistrationId().isEmpty())
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("registration Id cannot be null or empty"));
        }

        if (requestData.getSslContext() == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("sslContext cannot be null"));
        }

        if (responseCallback == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("responseCallback cannot be null"));
        }

        try
        {
            //SRS_ContractAPIHttp_25_012: [This method shall retrieve the Url by calling 'generateRegisterUrl' on an object for UrlPathBuilder.]
            String url = new UrlPathBuilder(this.hostName, this.idScope, ProvisioningDeviceClientTransportProtocol.HTTPS).generateRegisterUrl(requestData.getRegistrationId());
            Map<String, String> headersMap = null;
            if(requestData.getSasToken() != null)
            {
                headersMap = new HashMap<>();
                headersMap.put(AUTHORIZATION, requestData.getSasToken());
            }
            //SRS_ContractAPIHttp_25_026: [ This method shall build the required Json input using parser. ]
            byte[] payload = null;
            if (requestData.getEndorsementKey() != null && requestData.getStorageRootKey() != null)
            {
                //SRS_ContractAPIHttp_25_027: [ This method shall base 64 encoded endorsement key, storage root key. ]
                String base64EncodedEk = new String(Base64.encodeBase64Local(requestData.getEndorsementKey()));
                String base64EncodedSrk = new String(Base64.encodeBase64Local(requestData.getStorageRootKey()));
                payload = new DeviceRegistrationParser(requestData.getRegistrationId(), base64EncodedEk, base64EncodedSrk).toJson().getBytes();
            }
            else
            {
                payload = new DeviceRegistrationParser(requestData.getRegistrationId()).toJson().getBytes();
            }

            //SRS_ContractAPIHttp_25_013: [This method shall prepare the PUT request by setting following headers on a HttpRequest 1. User-Agent : User Agent String for the SDK 2. Accept : "application/json" 3. Content-Type: "application/json; charset=utf-8" 4. Authorization: specified sas token as authorization if a non null value is given.]
            HttpRequest httpRequest = this.prepareRequest(new URL(url), HttpMethod.PUT, payload, DEFAULT_HTTP_TIMEOUT_MS, headersMap, SDKUtils.getUserAgentString());
            //SRS_ContractAPIHttp_25_014: [This method shall set the SSLContext for the Http Request.]
            httpRequest.setSSLContext(requestData.getSslContext());
            //SRS_ContractAPIHttp_25_015: [This method shall send http request and verify the status by calling 'ProvisioningDeviceClientExceptionManager.verifyHttpResponse'.]
            //SRS_ContractAPIHttp_25_017: [If service return any other status other than <300 then this method shall throw ProvisioningDeviceHubException.]
            HttpResponse httpResponse = this.sendRequest(httpRequest);
            //SRS_ContractAPIHttp_25_016: [If service return a status as < 300 then this method shall trigger the callback to the user with the response message.]
            responseCallback.run(new ResponseData(httpResponse.getBody(), ContractState.DPS_REGISTRATION_RECEIVED, 0), dpsAuthorizationCallbackContext);
        }
        catch (IOException e)
        {
            throw new ProvisioningDeviceTransportException(e);
        }
    }

    /**
     * Gets the registration status over HTTP
     * @param requestData A non {@code null} value with all the request data
     * @param responseCallback A non {@code null} value for the callback
     * @param dpsAuthorizationCallbackContext An object for context. Can be {@code null}
     * @throws ProvisioningDeviceClientException If any of the parameters are invalid ({@code null} or empty)
     * @throws ProvisioningDeviceTransportException If any of the API calls to transport fail
     * @throws ProvisioningDeviceHubException If hub responds back with status other than 300 or less.
     */
    public synchronized void getRegistrationStatus(RequestData requestData, ResponseCallback responseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException
    {
        //SRS_ContractAPIHttp_25_018: [If either operationId, registrationId, sslcontext or responseCallback is null or if operationId, registrationId is empty then this method shall throw ProvisioningDeviceClientException.]
        if (requestData.getOperationId() == null || requestData.getOperationId().isEmpty())
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("operationId cannot be null or empty"));
        }

        if (requestData.getRegistrationId() == null || requestData.getRegistrationId().isEmpty())
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("registration Id cannot be null or empty"));
        }

        if (requestData.getSslContext() == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("sslContext cannot be null"));
        }

        if (responseCallback == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("responseCallback cannot be null"));
        }

        try
        {
            //SRS_ContractAPIHttp_25_019: [This method shall retrieve the Url by calling generateRequestUrl on an object for UrlPathBuilder.]
            String url = new UrlPathBuilder(this.hostName, this.idScope, ProvisioningDeviceClientTransportProtocol.HTTPS).generateRequestUrl(requestData.getRegistrationId(), requestData.getOperationId());
            Map<String, String> headersMap = null;
            if (requestData.getSasToken() != null)
            {
                headersMap = new HashMap<>();
                headersMap.put(AUTHORIZATION, requestData.getSasToken());
            }
            //SRS_ContractAPIHttp_25_020: [This method shall prepare the GET request by setting following headers on a HttpRequest 1. User-Agent : User Agent String for the SDK 2. Accept : "application/json" 3. Content-Type: "application/json; charset=utf-8" 4. Authorization: specified sas token as authorization if a non null value is given.]
            HttpRequest httpRequest = this.prepareRequest(new URL(url), HttpMethod.GET, new byte[0], DEFAULT_HTTP_TIMEOUT_MS, headersMap, SDKUtils.getUserAgentString());
            //SRS_ContractAPIHttp_25_021: [This method shall set the SSLContext for the Http Request.]
            httpRequest.setSSLContext(requestData.getSslContext());
            //SRS_ContractAPIHttp_25_022: [This method shall send http request and verify the status by calling 'ProvisioningDeviceClientExceptionManager.verifyHttpResponse'.]
            //SRS_ContractAPIHttp_25_024: [If service return any other status other than < 300 then this method shall throw ProvisioningDeviceHubException.]
            HttpResponse httpResponse = this.sendRequest(httpRequest);
            //SRS_ContractAPIHttp_25_023: [If service return a status as < 300 then this method shall trigger the callback to the user with the response message.]
            responseCallback.run(new ResponseData(httpResponse.getBody(),ContractState.DPS_REGISTRATION_RECEIVED, 0), dpsAuthorizationCallbackContext);
        }
        catch (IOException e)
        {
            throw new ProvisioningDeviceTransportException(e);
        }
    }
}
