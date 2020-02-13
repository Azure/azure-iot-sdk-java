/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.hsm;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.hsm.parser.ErrorResponse;
import com.microsoft.azure.sdk.iot.device.hsm.parser.SignRequest;
import com.microsoft.azure.sdk.iot.device.hsm.parser.SignResponse;
import com.microsoft.azure.sdk.iot.device.hsm.parser.TrustBundleResponse;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsMethod;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsRequest;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsResponse;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Slf4j
public class HttpsHsmClient
{
    private String baseUrl;
    private String scheme;

    private static final String HTTPS_SCHEME = "https";
    private static final String HTTP_SCHEME = "http";
    private static final String UNIX_SCHEME = "unix";

    private static final String API_VERSION_QUERY_STRING_PREFIX = "api-version=";

    /**
     * Client object for sending sign requests to an HSM unit
     * @param baseUrl The base url of the HSM
     * @throws URISyntaxException if the provided base url cannot be converted to a URI
     */
    public HttpsHsmClient(String baseUrl) throws URISyntaxException
    {
        if (baseUrl == null || baseUrl.isEmpty())
        {
            throw new IllegalArgumentException("baseUrl cannot be null");
        }

        log.trace("Creating HttpsHsmClient with base url {}", baseUrl);

        this.baseUrl = baseUrl;
        this.scheme = new URI(baseUrl).getScheme();
    }

    /**
     * Send a sign request to the HSM using the provided parameters and return the HSM's response
     * @param apiVersion the api version to use
     * @param moduleName The name of the module for which the sign request is requesting access to
     * @param signRequest the request to send
     * @param generationId the generation id
     * @return The response from the HSM
     * @throws IOException If the HSM cannot be reached
     * @throws TransportException If the HSM cannot be reached
     * @throws HsmException If there was a problem interacting with the HSM
     */
    public SignResponse sign(String apiVersion, String moduleName, SignRequest signRequest, String generationId) throws IOException, TransportException, HsmException
    {
        log.debug("Sending sign request...");
        // Codes_SRS_HSMHTTPCLIENT_34_002: [This function shall build an http request with the url in the format
        // <base url>/modules/<url encoded name>/genid/<url encoded gen id>/sign?api-version=<url encoded api version>.]
        String uri = baseUrl != null ? baseUrl.replaceFirst("/*$", "") : "";
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append("/modules/" + URLEncoder.encode(moduleName, "UTF-8"));
        pathBuilder.append("/genid/" + URLEncoder.encode(generationId, "UTF-8"));
        pathBuilder.append("/sign");

        byte[] body = signRequest.toJson().getBytes();
        
        HttpsResponse response = sendRequestBasedOnScheme(HttpsMethod.POST, body, uri,pathBuilder.toString(), API_VERSION_QUERY_STRING_PREFIX + apiVersion);

        int responseCode = response.getStatus();
        String responseBody = new String(response.getBody());
        if (responseCode >= 200 && responseCode < 300)
        {
            // Codes_SRS_HSMHTTPCLIENT_34_004: [If the response from the http call is 200, this function shall return the SignResponse built from the response body json.]
            return SignResponse.fromJson(responseBody);
        }
        else
        {
            String exceptionMessage = "HttpsHsmClient received status code " + responseCode + " from provided uri.";
            ErrorResponse errorResponse = ErrorResponse.fromJson(responseBody);
            if (errorResponse != null)
            {
                exceptionMessage = exceptionMessage + " Error response message: " + errorResponse.getMessage();
            }

            // Codes_SRS_HSMHTTPCLIENT_34_005: [If the response from the http call is not 200, this function shall throw an HsmException.]
            throw new HsmException(exceptionMessage);
        }
    }

    /**
     * Retrieve a trust bundle from an hsm
     * @param apiVersion the api version to use
     * @return the trust bundle response from the hsm, contains the certificates to be trusted
     * @throws TransportException if the HSM cannot be reached
     * @throws MalformedURLException if a proper URL cannot be constructed due to the provided api version
     * @throws HsmException if the hsm rejects the request for any reason
     */
    public TrustBundleResponse getTrustBundle(String apiVersion) throws IOException, TransportException, HsmException
    {
        log.debug("Getting trust bundle...");
        if (apiVersion == null || apiVersion.isEmpty())
        {
            // Codes_SRS_HSMHTTPCLIENT_34_007: [If the provided api version is null or empty, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("api version cannot be null or empty");
        }

        // Codes_SRS_HSMHTTPCLIENT_34_008: [This function shall build an http request with the url in the format
        // <base url>/trust-bundle?api-version=<url encoded api version>.]
        String uri = baseUrl != null ? baseUrl.replaceFirst("/*$", "") : "";
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append("/trust-bundle");

        // Codes_SRS_HSMHTTPCLIENT_34_009: [This function shall send a GET http request to the built url.]
        HttpsResponse response = sendRequestBasedOnScheme(HttpsMethod.GET, new byte[0], uri, pathBuilder.toString(), API_VERSION_QUERY_STRING_PREFIX + apiVersion);

        int statusCode = response.getStatus();
        String body = response.getBody() != null ? new String(response.getBody()) : "";
        if (statusCode >= 200 && statusCode < 300)
        {
            // Codes_SRS_HSMHTTPCLIENT_34_010: [If the response from the http request is 200, this function shall return the trust bundle response.]
            return TrustBundleResponse.fromJson(body);
        }
        else
        {
            // Codes_SRS_HSMHTTPCLIENT_34_011: [If the response from the http request is not 200, this function shall throw an HSMException.]
            ErrorResponse errorResponse = ErrorResponse.fromJson(body);
            if (errorResponse != null)
            {
                throw new HsmException("Received error from hsm with status code " + statusCode + " and message " + errorResponse.getMessage());
            }
            else
            {
                throw new HsmException("Received error from hsm with status code " + statusCode);
            }
        }
    }

    /**
     * Send a given httpsRequest using the appropriate means based on the scheme (http vs unix) of the baseUrl
     * @param httpsMethod the type of https method to call
     * @param body the body of the https call
     * @param baseUri the base uri to send the request to
     * @param path the relative path of the request
     * @param queryString the query string for the https request. Do not include the ? character
     * @return the http response to the request
     * @throws TransportException if the hsm cannot be reached
     * @throws IOException if the hsm cannot be reached
     */
    private HttpsResponse sendRequestBasedOnScheme(HttpsMethod httpsMethod, byte[] body, String baseUri, String path, String queryString) throws TransportException, IOException
    {
        URL requestUrl;
        if (this.scheme.equalsIgnoreCase(HTTPS_SCHEME) || this.scheme.equalsIgnoreCase(HTTP_SCHEME))
        {
            if (queryString != null && !queryString.isEmpty())
            {
                requestUrl = new URL(baseUri + path + "?" + queryString);
            }
            else
            {
                requestUrl = new URL(baseUri + path);
            }
        }
        else if (this.scheme.equalsIgnoreCase(UNIX_SCHEME))
        {
            //leave the url null, for unix flow, there is no need to build a URL instance
            requestUrl = null;
        }
        else
        {
            throw new UnsupportedOperationException("unrecognized URI scheme. Only HTTPS, HTTP and UNIX are supported");
        }

        // requestUrl will be null, if unix socket is used, but HttpsRequest won't null check it until we send the request.
        // In the unix case, we don't build the https request to send it, we just build it to hold all the information that
        // will go into the unix socket request later, such as headers, method, etc.
        HttpsRequest httpsRequest = new HttpsRequest(requestUrl, httpsMethod, body, "");

        // Codes_SRS_HSMHTTPCLIENT_34_003: [This function shall build an http request with headers ContentType and Accept with value application/json.]
        httpsRequest.setHeaderField("Accept", "application/json");

        if (body.length > 0)
        {
            httpsRequest.setHeaderField("Content-Type", "application/json");
        }

        HttpsResponse response;
        if (this.scheme.equalsIgnoreCase(HTTPS_SCHEME) || this.scheme.equalsIgnoreCase(HTTP_SCHEME))
        {
            response = httpsRequest.send();
        }
        else if (this.scheme.equalsIgnoreCase(UNIX_SCHEME))
        {
            String unixAddressPrefix = UNIX_SCHEME + "://";
            String localUnixSocketPath = baseUri.substring(baseUri.indexOf(unixAddressPrefix) + unixAddressPrefix.length());

            // Codes_SRS_HSMHTTPCLIENT_34_006: [If the scheme of the provided url is Unix, this function shall send the http request using unix domain sockets.]
            response = sendHttpRequestUsingUnixSocket(httpsRequest, path, queryString, localUnixSocketPath);
        }
        else
        {
            throw new UnsupportedOperationException("unrecognized URI scheme. Only HTTPS, HTTP and UNIX are supported");
        }

        return response;
    }

    /**
     * Send an HTTP request over a unix domain socket
     * @param httpsRequest the request to send
     * @return the response from the HSM unit
     * @throws IOException If the unix socket cannot be reached
     */
    private HttpsResponse sendHttpRequestUsingUnixSocket(HttpsRequest httpsRequest, String httpRequestPath, String httpRequestQueryString, String unixSocketAddress) throws IOException
    {
        log.debug("Sending data over unix socket...");

        UnixSocketChannel channel = null;
        HttpsResponse response = null;
        try
        {
            //write to socket
            byte[] requestBytes = HttpsRequestResponseSerializer.serializeRequest(httpsRequest, httpRequestPath, httpRequestQueryString, unixSocketAddress);
            UnixSocketAddress address = new UnixSocketAddress(unixSocketAddress);
            channel = UnixSocketChannel.open(address);

            if (httpsRequest.getBody() != null)
            {
                //append http request body to the request bytes
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
                outputStream.write(requestBytes);
                outputStream.write(httpsRequest.getBody());

                channel.write(ByteBuffer.wrap(outputStream.toByteArray()));
            }
            else
            {
                channel.write(ByteBuffer.wrap(requestBytes));
            }

            //read response
            String responseString = readResponseFromChannel(channel);
            response = HttpsRequestResponseSerializer.deserializeResponse(new BufferedReader(new StringReader(responseString)));
        }
        finally
        {
            if (channel != null)
            {
                log.trace("Closing unix socket channel...");
                channel.close();
            }
        }

        return response;
    }

    private String readResponseFromChannel(UnixSocketChannel channel) throws IOException
    {
        log.debug("Reading response from unix socket channel...");

        ByteBuffer buf = ByteBuffer.allocateDirect(10);
        String response = "";
        int numRead = 0;
        while (numRead >= 0)
        {
            // read() places read bytes at the buffer's position so the
            // position should always be properly set before calling read()
            // This method sets the position to 0
            buf.rewind();

            // Read bytes from the channel
            numRead = channel.read(buf);

            // The read() method also moves the position so in order to
            // read the new bytes, the buffer's position must be
            // set back to 0
            buf.rewind();

            // Read bytes from ByteBuffer; see also
            // e159 Getting Bytes from a ByteBuffer
            for (int i=0; i<numRead; i++)
            {
                response = response + new String(new byte[] {buf.get()}, StandardCharsets.US_ASCII);
            }
        }

        return response;
    }
}
