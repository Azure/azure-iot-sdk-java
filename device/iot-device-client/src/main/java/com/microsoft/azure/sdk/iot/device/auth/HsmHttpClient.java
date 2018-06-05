/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsMethod;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsRequest;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsResponse;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class HsmHttpClient
{
    private String baseUrl;

    public HsmHttpClient(String baseUrl)
    {
        // Codes_SRS_HSMHTTPCLIENT_34_001: [This constructor shall save the provided baseUrl.]
        this.baseUrl = baseUrl;
    }

    public HttpHsmSignResponse sign(String api_version, String name, HttpHsmSignRequest signRequest) throws MalformedURLException, TransportException, UnsupportedEncodingException
    {
        // Codes_SRS_HSMHTTPCLIENT_34_002: [This function shall build an http request with the url in the format
        // <base url>/modules/<url encoded name>/sign?api-version=<url encoded api version>.]
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(baseUrl != null ? baseUrl.replaceFirst("/*$", "") : "");
        urlBuilder.append("/modules/" + URLEncoder.encode(name, "UTF-8") + "/sign?");
        urlBuilder.append("api-version=");
        urlBuilder.append(URLEncoder.encode(api_version, "UTF-8"));

        byte[] body = signRequest.toJson().getBytes();

        HttpsRequest httpsRequest = new HttpsRequest(new URL(urlBuilder.toString()), HttpsMethod.POST, body, TransportUtils.USER_AGENT_STRING);


        // Codes_SRS_HSMHTTPCLIENT_34_003: [This function shall build an http request with headers ContentType and Accept with value application/json.]
        httpsRequest.setHeaderField("ContentType", "application/json");
        httpsRequest.setHeaderField("Accept", "application/json");

        HttpsResponse response = httpsRequest.send();

        String responseBody = new String(response.getBody());
        switch (response.getStatus())
        {
            case 200:
                // Codes_SRS_HSMHTTPCLIENT_34_004: [If the response from the http call is 200, this function shall return the HttpHsmSignResponse built from the response body json.]
                return HttpHsmSignResponse.fromJson(responseBody);
            default:
                // Codes_SRS_HSMHTTPCLIENT_34_005: [If the response from the http call is not 200, this function shall throw a transport exception.]
                throw new TransportException("HsmHttpClient received status code " + response.getStatus() + " from provided uri. Error Message: " + HttpHsmErrorResponse.fromJson(responseBody).getMessage());
        }
    }
}
