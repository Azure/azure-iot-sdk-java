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
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.reactor.impl.IO;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
public class HttpsHsmClient
{
    private final String baseUrl;
    private final String scheme;
    private final UnixDomainSocketChannel unixDomainSocketChannel;

    private static final String HTTPS_SCHEME = "https";
    private static final String HTTP_SCHEME = "http";
    private static final String UNIX_SCHEME = "unix";

    private static final String API_VERSION_QUERY_STRING_PREFIX = "api-version=";

    /**
     * Client object for sending sign requests to an HSM unit
     * @param baseUrl The base url of the HSM
     * @param unixDomainSocketChannel the implementation of the {@link UnixDomainSocketChannel} interface that will be used if any
     * unix domain socket communication is required. May be null if no unix domain socket communication is required.
     * @throws URISyntaxException if the provided base url cannot be converted to a URI
     */
    public HttpsHsmClient(String baseUrl, UnixDomainSocketChannel unixDomainSocketChannel) throws URISyntaxException
    {
        if (baseUrl == null || baseUrl.isEmpty())
        {
            throw new IllegalArgumentException("baseUrl cannot be null");
        }

        log.trace("Creating HttpsHsmClient with base url {}", baseUrl);

        this.baseUrl = baseUrl;
        this.scheme = new URI(baseUrl).getScheme();

        // unixDomainSocketChannel is allowed to be null since the module may not need to do unix domain socket communication during setup depending on the Edge environment.
        this.unixDomainSocketChannel = unixDomainSocketChannel;
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
        String uri = baseUrl != null ? baseUrl.replaceFirst("/*$", "") : "";

        byte[] body = signRequest.toJson().getBytes(StandardCharsets.UTF_8);

        String pathBuilder = "/modules/" + URLEncoder.encode(moduleName, StandardCharsets.UTF_8.name()) +
                "/genid/" + URLEncoder.encode(generationId, StandardCharsets.UTF_8.name()) +
                "/sign";
        HttpsResponse response = sendRequestBasedOnScheme(HttpsMethod.POST, body, uri, pathBuilder, API_VERSION_QUERY_STRING_PREFIX + apiVersion);

        int responseCode = response.getStatus();
        String responseBody = new String(response.getBody(), StandardCharsets.UTF_8);
        if (responseCode >= 200 && responseCode < 300)
        {
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
            throw new IllegalArgumentException("api version cannot be null or empty");
        }

        String uri = baseUrl != null ? baseUrl.replaceFirst("/*$", "") : "";

        HttpsResponse response = sendRequestBasedOnScheme(HttpsMethod.GET, new byte[0], uri, "/trust-bundle", API_VERSION_QUERY_STRING_PREFIX + apiVersion);

        int statusCode = response.getStatus();
        String body = response.getBody() != null ? new String(response.getBody(), StandardCharsets.UTF_8) : "";
        if (statusCode >= 200 && statusCode < 300)
        {
            return TrustBundleResponse.fromJson(body);
        }
        else
        {
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

        httpsRequest.setHeaderField("Accept", "application/json");

        if (body.length > 0)
        {
            httpsRequest.setHeaderField("Content-Type", "application/json");
        }

        HttpsResponse response;
        if (this.scheme.equalsIgnoreCase(HTTPS_SCHEME))
        {
            response = httpsRequest.send();
        }
        else if (this.scheme.equalsIgnoreCase(HTTP_SCHEME))
        {
            response = httpsRequest.sendAsHttpRequest();
        }
        else if (this.scheme.equalsIgnoreCase(UNIX_SCHEME))
        {
            if (this.unixDomainSocketChannel == null)
            {
                throw new IllegalArgumentException("Must provide an implementation of the UnixDomainSocketChannel interface since this edge runtime setup requires communicating over unix domain sockets.");
            }
            else
            {
                log.trace("User provided UnixDomainSocketChannel will be used for setup.");
            }

            String unixAddressPrefix = UNIX_SCHEME + "://";
            String localUnixSocketPath = baseUri.substring(baseUri.indexOf(unixAddressPrefix) + unixAddressPrefix.length());

            response = sendHttpRequestUsingUnixSocket(httpsRequest, path, queryString, localUnixSocketPath);
        }
        else
        {
            throw new UnsupportedOperationException("unrecognized URI scheme \"" + this.scheme + "\". Only HTTPS, HTTP and UNIX are supported");
        }

        return response;
    }

    /**
     * Send an HTTP request over a unix domain socket
     * @param httpsRequest the request to send
     * @return the response from the HSM unit
     * @throws IOException If the unix domain socket cannot be reached
     */
    private HttpsResponse sendHttpRequestUsingUnixSocket(HttpsRequest httpsRequest, String httpRequestPath, String httpRequestQueryString, String unixSocketAddress) throws IOException
    {
        log.debug("Sending data over unix domain socket");

        HttpsResponse response;
        try
        {
            //write to socket
            byte[] requestBytes = HttpsRequestResponseSerializer.serializeRequest(httpsRequest, httpRequestPath, httpRequestQueryString, unixSocketAddress);
            unixDomainSocketChannel.open(unixSocketAddress);

            if (httpsRequest.getBody() != null)
            {
                //append http request body to the request bytes
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(requestBytes);
                outputStream.write(httpsRequest.getBody());

                log.trace("Writing {} bytes to unix domain socket", outputStream.size());
                unixDomainSocketChannel.write(outputStream.toByteArray());
            }
            else
            {
                log.trace("Writing {} bytes to unix domain socket", requestBytes.length);
                unixDomainSocketChannel.write(requestBytes);
            }

            //read response
            String responseString = readResponseFromChannel(unixDomainSocketChannel);
            response = HttpsRequestResponseSerializer.deserializeResponse(new BufferedReader(new StringReader(responseString)));
        }
        finally
        {
            log.trace("Closing unix domain socket");
            unixDomainSocketChannel.close();
        }

        return response;
    }

    private String readResponseFromChannel(UnixDomainSocketChannel channel) throws IOException
    {
        log.debug("Reading response from unix domain socket");

        byte[] buf = new byte[400];
        StringBuilder responseStringBuilder = new StringBuilder();
        int numRead = channel.read(buf);

        // keep reading from the unix domain socket in chunks until no more bytes are read
        while (numRead >= 0)
        {
            log.trace("Read {} bytes from unix domain socket", numRead);

            // buf may not be filled completely, so take the subArray of bytes sized equal to numRead
            String readChunk = new String(Arrays.copyOfRange(buf, 0, numRead), StandardCharsets.US_ASCII);
            log.trace("Read chunk of data from unix domain socket:");
            log.trace("{}", readChunk);
            responseStringBuilder.append(readChunk);

            // Read bytes from the channel
            numRead = channel.read(buf);
        }

        String response = responseStringBuilder.toString();
        log.debug("Read response from unix domain socket channel");
        log.debug("{}", response);

        return response;
    }
}
