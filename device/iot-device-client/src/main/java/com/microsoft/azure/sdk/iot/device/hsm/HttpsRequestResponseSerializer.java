/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.hsm;

import com.microsoft.azure.sdk.iot.device.transport.https.HttpsRequest;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpsRequestResponseSerializer
{
    private static final String SP = " ";
    private static final String CR = "\r";
    private static final String LF = "\n";
    private static final String ProtocolVersionSeparator = "/";
    private static final String Protocol = "HTTP";
    private static final String HeaderSeparator = ":";
    private static final String VERSION = "1.1";

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
            // Codes_SRS_HTTPREQUESTRESPONSESERIALIZER_34_001: [If the provided request is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("The httpsRequest cannot be null");
        }

        if (httpsRequest.getRequestUrl() == null)
        {
            // Codes_SRS_HTTPREQUESTRESPONSESERIALIZER_34_002: [If the provided request's url is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Request uri of the request cannot be null");
        }

        if (path == null || path.isEmpty())
        {
            throw new IllegalArgumentException("path cannot be null or empty");
        }

        if (host == null || host.isEmpty())
        {
            throw new IllegalArgumentException("host cannot be null or empty");
        }
        
        // Codes_SRS_HTTPREQUESTRESPONSESERIALIZER_34_003: [This function shall serialize the provided httpsRequest into the form:
        // POST /modules/<moduleName>/sign?api-version=2018-06-28 HTTP/1.1
        // Host: localhost:8081
        // Connection: close
        // <header>: <value>
        // <header>: <value1>; <value2>
        // .]

        httpsRequest.setHeaderField("Connection", "close");

        String updatedPath = preProcessRequestPath(path);

        StringBuilder builder = new StringBuilder();
        builder.append(httpsRequest.getHttpMethod());
        builder.append(SP);

        builder.append(updatedPath);

        if (queryString != null && !queryString.isEmpty())
        {
            builder.append("?" + queryString);
        }

        builder.append(SP);
        builder.append(Protocol + ProtocolVersionSeparator);
        builder.append(VERSION);
        builder.append(CR);
        builder.append(LF);

        builder.append("Host: " + host + "\r\n");

        if (httpsRequest.getRequestHeaders() != null && !httpsRequest.getRequestHeaders().isEmpty())
        {
            builder.append(httpsRequest.getRequestHeaders());
        }

        if (httpsRequest.getBody() != null && httpsRequest.getBody().length != 0)
        {
            builder.append("Content-Length: " + httpsRequest.getBody().length + "\r\n");
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
            // Codes_SRS_HTTPREQUESTRESPONSESERIALIZER_34_004: [If the provided bufferedReader is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("buffered reader cannot be null");
        }

        // Codes_SRS_HTTPREQUESTRESPONSESERIALIZER_34_005: [This function shall read lines from the provided buffered
        // reader with the following format:
        //  <version> <status code> <error reason>
        //  <header>:<value>
        //  <header>:<value>
        //  ...
        //  <http body content>
        // .]
        String statusLine = bufferedReader.readLine();
        if (statusLine == null || statusLine.isEmpty())
        {
            // Codes_SRS_HTTPREQUESTRESPONSESERIALIZER_34_006: [If the buffered reader doesn't have at least one line, this function shall throw an IOException.]
            throw new IOException("Response is empty.");
        }

        String[] statusLineParts = statusLine.split(SP);
        if (statusLineParts.length != 3)
        {
            // Codes_SRS_HTTPREQUESTRESPONSESERIALIZER_34_006: [If the buffered reader's first line does not have the version, status code, and error reason split by a space, this function shall throw an IOException.]
            throw new IOException("Status line is not valid.");
        }

        String[] httpVersion = statusLineParts[0].split(ProtocolVersionSeparator);
        if (httpVersion.length != 2)
        {
            throw new IOException("Version is not valid " + statusLineParts[0] + ".");
        }

        //don't care about version right now
        String version = httpVersion[1];

        int statusCode;
        try
        {
            String statusCodeString = statusLineParts[1];
            statusCode = Integer.valueOf(statusCodeString);
        }
        catch (NumberFormatException e)
        {
            // Codes_SRS_HTTPREQUESTRESPONSESERIALIZER_34_007: [If the status code is not parsable into an int, this function shall throw an IOException.]
            throw new IOException("StatusCode is not valid " + statusLineParts[1] + ".");
        }

        Map<String, List<String>> headerFields = readHeaderFields(bufferedReader);
        byte[] body = readBody(bufferedReader);
        byte[] errorReason = statusLineParts[2].getBytes();

        bufferedReader.close();

        return new HttpsResponse(statusCode, body, headerFields, errorReason);
    }

    private static String preProcessRequestPath(String path)
    {
        String dnsSafePath = path.replace("[", "").replace("]", "");
        return dnsSafePath;
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
        }

        for(String header : headers)
        {
            if (header == null || header.isEmpty())
            {
                // headers end
                break;
            }

            int headerSeparatorPosition = header.indexOf(HeaderSeparator);
            if (headerSeparatorPosition <= 0)
            {
                // Codes_SRS_HTTPREQUESTRESPONSESERIALIZER_34_008: [If a header is not separated from its value by a':', this function shall throw an IOException.]
                throw new IOException("Header is invalid " + header + ".");
            }

            String headerName = header.substring(0, headerSeparatorPosition);
            String headerValue = header.substring(headerSeparatorPosition + 1);

            List headerValues = new ArrayList();
            headerValues.add(headerValue);
            headerFields.put(headerName, headerValues);
        }

        return headerFields;
    }

    private static byte[] readBody(BufferedReader bufferedReader) throws IOException
    {
        String bodyString = "";
        String next = bufferedReader.readLine();
        while (next != null && !next.isEmpty())
        {
            bodyString = bodyString + next;
            next = bufferedReader.readLine();
        }

        return bodyString.getBytes();
    }
}
