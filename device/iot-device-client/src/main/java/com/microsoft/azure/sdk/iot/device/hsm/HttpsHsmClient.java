/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.hsm;

import com.microsoft.azure.sdk.iot.deps.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.hsm.parser.ErrorResponse;
import com.microsoft.azure.sdk.iot.device.hsm.parser.SignRequest;
import com.microsoft.azure.sdk.iot.device.hsm.parser.SignResponse;
import com.microsoft.azure.sdk.iot.device.hsm.parser.TrustBundleResponse;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsMethod;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsRequest;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsResponse;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;

import java.io.*;
import java.net.*;
import java.nio.channels.Channels;

public class HttpsHsmClient
{
    private String baseUrl;
    private String scheme;

    private static final String HttpsScheme = "https";
    private static final String UnixScheme = "unix";

    /**
     * Client object for sending sign requests to an HSM unit
     * @param baseUrl The base url of the HSM
     * @throws URISyntaxException if the provided base url cannot be converted to a URI
     */
    public HttpsHsmClient(String baseUrl) throws URISyntaxException
    {
        // Codes_SRS_HSMHTTPCLIENT_34_001: [This constructor shall save the provided baseUrl.]
        this.baseUrl = baseUrl;
        URI uri = new URI(baseUrl);
        this.scheme = uri.getScheme();
    }

    /**
     * Send a sign request to the HSM using the provided parameters and return the HSM's response
     * @param api_version the api version to use
     * @param moduleName The name of the module for which the sign request is requesting access to
     * @param signRequest the request to send
     * @param generationId the generation id
     * @return The response from the HSM
     * @throws IOException If the HSM cannot be reached
     * @throws TransportException If the HSM cannot be reached
     * @throws URISyntaxException If the request URI is not a valid URI
     * @throws HsmException If there was a problem interacting with the HSM
     */
    public SignResponse sign(String api_version, String moduleName, SignRequest signRequest, String generationId) throws IOException, TransportException, URISyntaxException, HsmException
    {
        // Codes_SRS_HSMHTTPCLIENT_34_002: [This function shall build an http request with the url in the format
        // <base url>/modules/<url encoded name>/genid/<url encoded gen id>/sign?api-version=<url encoded api version>.]
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(baseUrl != null ? baseUrl.replaceFirst("/*$", "") : "");
        urlBuilder.append("/modules/" + URLEncoder.encode(moduleName, "UTF-8"));
        urlBuilder.append("/genid/" + URLEncoder.encode(generationId, "UTF-8"));
        urlBuilder.append("/sign?");
        urlBuilder.append("api-version=").append(URLEncoder.encode(api_version, "UTF-8"));

        byte[] body = signRequest.toJson().getBytes();

        HttpsRequest httpsRequest = new HttpsRequest(new URL(urlBuilder.toString()), HttpsMethod.POST, body, TransportUtils.USER_AGENT_STRING);

        // Codes_SRS_HSMHTTPCLIENT_34_003: [This function shall build an http request with headers ContentType and Accept with value application/json.]
        httpsRequest.setHeaderField("ContentType", "application/json");
        httpsRequest.setHeaderField("Accept", "application/json");

        HttpsResponse response = null;
        if (this.scheme.equalsIgnoreCase(HttpsScheme))
        {
            response = httpsRequest.send();
        }
        else if (this.scheme.equalsIgnoreCase(UnixScheme))
        {
            // Codes_SRS_HSMHTTPCLIENT_34_006: [If the scheme of the provided url is Unix, this function shall send the http request using unix domain sockets.]
            response = sendHttpRequestUsingUnixSocket(httpsRequest, urlBuilder.toString());
        }
        else
        {
            throw new UnsupportedOperationException("unrecognized URI scheme. Only HTTPS and UNIX are supported");
        }

        String responseBody = new String(response.getBody());
        switch (response.getStatus())
        {
            case 200:
                // Codes_SRS_HSMHTTPCLIENT_34_004: [If the response from the http call is 200, this function shall return the SignResponse built from the response body json.]
                return SignResponse.fromJson(responseBody);
            default:
                // Codes_SRS_HSMHTTPCLIENT_34_005: [If the response from the http call is not 200, this function shall throw an HsmException.]
                throw new HsmException("HttpsHsmClient received status code " + response.getStatus() + " from provided uri. Error Message: " + ErrorResponse.fromJson(responseBody).getMessage());
        }
    }

    /**
     * Retrieve a trust bundle from an hsm
     * @param apiVersion the api version to use
     * @return the trust bundle response from the hsm, contains the certificates to be trusted
     * @throws UnsupportedEncodingException if UTF-8 encoding is not supported
     * @throws TransportException if the HSM cannot be reached
     * @throws MalformedURLException if a proper URL cannot be constructed due to the provided api version
     * @throws HsmException if the hsm rejects the request for any reason
     */
    public TrustBundleResponse getTrustBundle(String apiVersion) throws UnsupportedEncodingException, TransportException, MalformedURLException, HsmException
    {
        if (apiVersion == null || apiVersion.isEmpty())
        {
            // Codes_SRS_HSMHTTPCLIENT_34_007: [If the provided api version is null or empty, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("api version cannot be null or empty");
        }

        // Codes_SRS_HSMHTTPCLIENT_34_008: [This function shall build an http request with the url in the format
        // <base url>/trust-bundle?api-version=<url encoded api version>.]
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(baseUrl != null ? baseUrl.replaceFirst("/*$", "") : "");
        urlBuilder.append("/trust-bundle?");
        urlBuilder.append("api-version=").append(URLEncoder.encode(apiVersion, "UTF-8"));

        // Codes_SRS_HSMHTTPCLIENT_34_009: [This function shall send a GET http request to the built url.]
        HttpsRequest request = new HttpsRequest(new URL(urlBuilder.toString()), HttpsMethod.GET, null, TransportUtils.USER_AGENT_STRING);
        HttpsResponse response = request.send();

        int statusCode = response.getStatus();
        if (statusCode == 200)
        {
            // Codes_SRS_HSMHTTPCLIENT_34_010: [If the response from the http request is 200, this function shall return the trust bundle response.]
            return new TrustBundleResponse(new String(response.getBody()));
        }
        else
        {
            // Codes_SRS_HSMHTTPCLIENT_34_011: [If the response from the http request is not 200, this function shall throw an HSMException.]
            ErrorResponse errorResponse = ErrorResponse.fromJson(new String(response.getBody()));
            throw new HsmException("Received error from hsm with status code " + statusCode + " and message " + errorResponse.getMessage());
        }
    }

    /**
     * Send an HTTP request over a unix domain socket
     * @param httpsRequest the request to send
     * @param url the url of the unix socket to send to
     * @return the response from the HSM unit
     * @throws IOException If the unix socket cannot be reached
     * @throws URISyntaxException the the url cannot be parsed
     */
    private static HttpsResponse sendHttpRequestUsingUnixSocket(HttpsRequest httpsRequest, String url) throws IOException, URISyntaxException
    {
        //write to socket
        byte[] requestBytes = HttpsRequestResponseSerializer.serializeRequest(httpsRequest);
        UnixSocketAddress address = new UnixSocketAddress(url);
        UnixSocketChannel channel = UnixSocketChannel.open(address);
        PrintWriter writer = new PrintWriter(Channels.newOutputStream(channel));

        writer.print(requestBytes);

        if (httpsRequest.getBody() != null)
        {
            writer.print(httpsRequest.getBody());
        }

        //ensure all data is pushed to writer, then close
        writer.flush();
        writer.close();

        //read response
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Channels.newInputStream(channel)));
        return HttpsRequestResponseSerializer.deserializeResponse(bufferedReader);
    }
}
