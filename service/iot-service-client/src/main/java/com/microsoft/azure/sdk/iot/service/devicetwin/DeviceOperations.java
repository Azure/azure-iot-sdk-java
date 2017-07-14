// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

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
     * @param iotHubConnectionString is the connection string for the IoTHub
     * @param url is the Twin URL for the device ID.
     * @param method is the HTTP method (GET, POST, DELETE, PATCH, PUT).
     * @param payload is the array of bytes that contains the payload.
     * @param requestId is an unique number that identify the request.
     * @param timeoutInMs is timeout in milliseconds.
     * @return the result of the request.
     * @throws IotHubException This exception is thrown if the response verification failed
     * @throws IOException This exception is thrown if the IO operation failed
     */
    public static HttpResponse request(
            IotHubConnectionString iotHubConnectionString, 
            URL url, 
            HttpMethod method, 
            byte[] payload, 
            String requestId,
            long timeoutInMs) 
            throws IOException, IotHubException, IllegalArgumentException
    {
        /* Codes_SRS_DEVICE_OPERATIONS_21_001: [The request shall throw IllegalArgumentException if the provided `iotHubConnectionString` is null.] */
        if(iotHubConnectionString == null)
        {
            throw new IllegalArgumentException("Null ConnectionString");
        }

        /* Codes_SRS_DEVICE_OPERATIONS_21_002: [The request shall throw IllegalArgumentException if the provided `url` is null.] */
        if(url == null)
        {
            throw new IllegalArgumentException("Null URL");
        }

        /* Codes_SRS_DEVICE_OPERATIONS_21_003: [The request shall throw IllegalArgumentException if the provided `method` is null.] */
        if(method == null)
        {
            throw new IllegalArgumentException("Null method");
        }

        /* Codes_SRS_DEVICE_OPERATIONS_21_004: [The request shall throw IllegalArgumentException if the provided `payload` is null.] */
        if(payload == null)
        {
            throw new IllegalArgumentException("Null payload");
        }

        /* Codes_SRS_DEVICE_OPERATIONS_99_018: [The request shall throw IllegalArgumentException if the provided `timeoutInMs` exceed Integer.MAX_VALUE.] */
        if((timeoutInMs + DEFAULT_HTTP_TIMEOUT_MS) > Integer.MAX_VALUE) 
        {
            throw new IllegalArgumentException("HTTP Request timeout shouldn't not exceed " + timeoutInMs + DEFAULT_HTTP_TIMEOUT_MS + " milliseconds");
        }

        /* Codes_SRS_DEVICE_OPERATIONS_21_006: [The request shall create a new SASToken with the ServiceConnect rights.] */
        String sasTokenString = new IotHubServiceSasToken(iotHubConnectionString).toString();
        /* Codes_SRS_DEVICE_OPERATIONS_21_007: [If the SASToken is null or empty, the request shall throw IOException.] */
         if((sasTokenString == null) || sasTokenString.isEmpty())
        {
            throw new IOException("Illegal sasToken null or empty");
        }

        /* Codes_SRS_DEVICE_OPERATIONS_21_008: [The request shall create a new HttpRequest with the provided `url`, http `method`, and `payload`.] */
        HttpRequest request = new HttpRequest(url, method, payload);

        /* Codes_SRS_DEVICE_OPERATIONS_21_009: [The request shall add to the HTTP header the sum of timeout and default timeout in milliseconds.] */
        request.setReadTimeoutMillis((int)(timeoutInMs + DEFAULT_HTTP_TIMEOUT_MS));
        
        /* Codes_SRS_DEVICE_OPERATIONS_21_010: [The request shall add to the HTTP header an `authorization` key with the SASToken.] */
        request.setHeaderField(AUTHORIZATION, sasTokenString);

        //Codes_SRS_DEVICE_OPERATIONS_21_011: [If the requestId is not null or empty, the request shall add to the HTTP header a Request-Id key with a new unique string value for every request.]
        if((requestId != null) && !requestId.isEmpty())
        {
            /* Codes_SRS_DEVICE_OPERATIONS_21_011: [The request shall add to the HTTP header a `Request-Id` key with a new unique string value for every request.] */
            request.setHeaderField(REQUEST_ID, requestId);
        }

        /* Codes_SRS_DEVICE_OPERATIONS_21_012: [The request shall add to the HTTP header a `User-Agent` key with the client Id and service version.] */
        request.setHeaderField(USER_AGENT, TransportUtils.getJavaServiceClientIdentifier() + TransportUtils.getServiceVersion());

        /* Codes_SRS_DEVICE_OPERATIONS_21_013: [The request shall add to the HTTP header a `Accept` key with `application/json`.] */
        request.setHeaderField(ACCEPT, ACCEPT_VALUE);

        /* Codes_SRS_DEVICE_OPERATIONS_21_014: [The request shall add to the HTTP header a `Content-Type` key with `application/json; charset=utf-8`.] */
        request.setHeaderField(CONTENT_TYPE, ACCEPT_VALUE + "; " + ACCEPT_CHARSET);

        if (headers != null)
        {
            //SRS_DEVICE_OPERATIONS_25_019: [The request shall add to the HTTP header all the additional custom headers set for this request.]
            for(Map.Entry<String, String> header : headers.entrySet())
            {
                request.setHeaderField(header.getKey(), header.getValue());
            }

            headers = null;
        }

        /* Codes_SRS_DEVICE_OPERATIONS_21_015: [The request shall send the created request and get the response.] */
        HttpResponse response = request.send();

        /* Codes_SRS_DEVICE_OPERATIONS_21_016: [If the resulted HttpResponseStatus represents fail, the request shall throw proper Exception by calling httpResponseVerification.] */
        IotHubExceptionManager.httpResponseVerification(response);
        
        /* Codes_SRS_DEVICE_OPERATIONS_21_017: [If the resulted status represents success, the request shall return the http response.] */
        return response;
    }

    /**
     * Sets headers to be used on next HTTP request
     * @param httpHeaders non null and non empty custom headers
     * @throws IllegalArgumentException This exception is thrown if headers were null or empty
     */
    public static void setHeaders(Map<String, String> httpHeaders) throws IllegalArgumentException
    {
        if (httpHeaders == null || httpHeaders.size() == 0)
        {
            //SRS_DEVICE_OPERATIONS_25_021: [If the headers map is null or empty then this method shall throw IllegalArgumentException.]
            throw new IllegalArgumentException("Null or Empty headers can't be set");
        }

        //SRS_DEVICE_OPERATIONS_25_020: [This method shall set the headers map to be used for next request only.]
        headers = httpHeaders;
    }
}
