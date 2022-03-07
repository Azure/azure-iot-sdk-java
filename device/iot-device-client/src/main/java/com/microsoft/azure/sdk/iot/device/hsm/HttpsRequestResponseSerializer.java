/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.hsm;

import com.microsoft.azure.sdk.iot.device.transport.https.HttpsRequest;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

class HttpsRequestResponseSerializer
{
    private static final String SP = " ";
    private static final String CR = "\r";
    private static final String LF = "\n";
    private static final String ProtocolVersionSeparator = "/";
    private static final String Protocol = "HTTP";
    private static final String HeaderSeparator = ":";
    private static final String VERSION = "1.1";

    private static final long MAXIMUM_HEADER_COUNT = 500;

    /**
     * Serialize the provided request
     *
     * @param httpsRequest the request to be serialized
     * @param path the path for the request to invoke on (e.g. /trust-bundle)
     * @param queryString the full querystring associated with the http request. Should not include the '?' character at the beginning
     * @param host the host that the request is being made to
     * @return the serialized request
     * @throws IllegalArgumentException if the provided httpsRequest is null or has a null request url
     */
    public static byte[] serializeRequest(HttpsRequest httpsRequest, String path, String queryString, String host) throws IllegalArgumentException
    {
        if (httpsRequest == null)
        {
            throw new IllegalArgumentException("The httpsRequest cannot be null");
        }

        if (path == null || path.isEmpty())
        {
            throw new IllegalArgumentException("path cannot be null or empty");
        }

        if (host == null || host.isEmpty())
        {
            throw new IllegalArgumentException("host cannot be null or empty");
        }
        
        // POST /modules/<moduleName>/sign?api-version=2018-06-28 HTTP/1.1
        // Host: localhost:8081
        // Connection: close
        // <header>: <value>
        // <header>: <value1>; <value2>

        httpsRequest.setHeaderField("Connection", "close");

        String updatedPath = preProcessRequestPath(path);

        StringBuilder builder = new StringBuilder();
        builder.append(httpsRequest.getHttpMethod());
        builder.append(SP);

        builder.append(updatedPath);

        if (queryString != null && !queryString.isEmpty())
        {
            builder.append("?").append(queryString);
        }

        builder.append(SP);
        builder.append(Protocol + ProtocolVersionSeparator);
        builder.append(VERSION);
        builder.append(CR);
        builder.append(LF);

        builder.append("Host: ").append(host).append("\r\n");

        if (httpsRequest.getRequestHeaders() != null && !httpsRequest.getRequestHeaders().isEmpty())
        {
            builder.append(httpsRequest.getRequestHeaders());
        }

        if (httpsRequest.getBody() != null && httpsRequest.getBody().length != 0)
        {
            builder.append("Content-Length: ").append(httpsRequest.getBody().length).append("\r\n");
        }

        // Headers end
        builder.append(CR);
        builder.append(LF);

        return builder.toString().getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Deserialize a stream of bytes from an HSM party into an HttpsResponse
     * @param bufferedReader the stream to read from. Will be closed before this method returns
     * @return the deserialized response
     * @throws IOException if the stream cannot be read from
     */
    public static HttpsResponse deserializeResponse(BufferedReader bufferedReader) throws IOException
    {
        if (bufferedReader == null)
        {
            throw new IllegalArgumentException("buffered reader cannot be null");
        }

        //  <version> <status code> <error reason>
        //  <header>:<value>
        //  <header>:<value>
        //  ...
        //  <http body content>
        String statusLine = bufferedReader.readLine();
        if (statusLine == null || statusLine.isEmpty())
        {
            throw new IOException("Response is empty.");
        }

        String[] statusLineParts = statusLine.split(SP);
        if (statusLineParts.length != 3)
        {
            throw new IOException("Status line is not valid: " + statusLine);
        }

        String[] httpVersion = statusLineParts[0].split(ProtocolVersionSeparator);
        if (httpVersion.length != 2)
        {
            throw new IOException("Version is not valid " + statusLineParts[0] + ".");
        }

        int statusCode;
        try
        {
            String statusCodeString = statusLineParts[1];
            statusCode = Integer.parseInt(statusCodeString);
        }
        catch (NumberFormatException e)
        {
            throw new IOException("StatusCode is not valid " + statusLineParts[1] + ".");
        }

        Map<String, List<String>> headerFields = readHeaderFields(bufferedReader);
        byte[] body = readBody(bufferedReader);
        byte[] errorReason = statusLineParts[2].getBytes(StandardCharsets.UTF_8);

        bufferedReader.close();

        return new HttpsResponse(statusCode, body, headerFields, errorReason);
    }

    private static String preProcessRequestPath(String path)
    {
        return path.replace("[", "").replace("]", "");
    }

    private static Map<String, List<String>> readHeaderFields(BufferedReader bufferedReader) throws IOException
    {
        Map<String, List<String>> headerFields = new HashMap<>();
        Collection<String> headers = new ArrayList<>();
        String line = bufferedReader.readLine();
        while (line != null && !line.isEmpty())
        {
            headers.add(line);
            line = bufferedReader.readLine();

            if (headers.size() > MAXIMUM_HEADER_COUNT)
            {
                throw new IOException("HSM provided too many http headers");
            }
        }

        for (String header : headers)
        {
            if (header == null || header.isEmpty())
            {
                // headers end
                break;
            }

            int headerSeparatorPosition = header.indexOf(HeaderSeparator);
            if (headerSeparatorPosition <= 0)
            {
                throw new IOException("Header is invalid " + header + ".");
            }

            String headerName = header.substring(0, headerSeparatorPosition);
            String headerValue = header.substring(headerSeparatorPosition + 1);

            List<String> headerValues = new ArrayList<>();
            headerValues.add(headerValue);
            headerFields.put(headerName, headerValues);
        }

        return headerFields;
    }

    private static byte[] readBody(BufferedReader bufferedReader) throws IOException
    {
        StringBuilder bodyString = new StringBuilder();
        String next = bufferedReader.readLine();
        while (next != null && !next.isEmpty())
        {
            bodyString.append(next);
            next = bufferedReader.readLine();
        }

        return bodyString.toString().getBytes(StandardCharsets.UTF_8);
    }
}
