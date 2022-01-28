/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.azure.core.credential.AzureSasCredential;
import com.microsoft.azure.sdk.iot.service.serializers.ParserUtility;
import com.microsoft.azure.sdk.iot.service.serializers.QueryRequestParser;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Sql style query IotHub for twin, jobs, device jobs or raw data
 */
public class Query
{
    private static final String CONTINUATION_TOKEN_KEY = "x-ms-continuation";
    private static final String ITEM_TYPE_KEY = "x-ms-item-type";
    private static final String PAGE_SIZE_KEY = "x-ms-max-item-count";

    private final int pageSize;
    private final String query;
    private final boolean isSqlQuery;

    private String requestContinuationToken;
    private String responseContinuationToken;

    private final QueryType requestQueryType;
    private QueryType responseQueryType;

    private QueryResponse queryResponse;

    private IotHubConnectionString iotHubConnectionString;
    private AzureSasCredential azureSasCredential;
    private TokenCredentialCache credentialCache;
    private URL url;
    private HttpMethod httpMethod;

    private int httpConnectTimeout;
    private int httpReadTimeout;

    private Proxy proxy;

    /**
     * Constructor for Query.
     *
     * @param query Sql style query to be sent to IotHub.
     * @param pageSize page size for the query response to sendHttpRequest query over.
     * @param requestQueryType Type of query.
     * @throws IllegalArgumentException if the input parameters are invalid.
     */
    public Query(String query, int pageSize, QueryType requestQueryType) throws IllegalArgumentException
    {
        ParserUtility.validateQuery(query);

        if (pageSize <= 0)
        {
            throw new IllegalArgumentException("Page Size cannot be zero or negative");
        }

        if (requestQueryType == null || requestQueryType == QueryType.UNKNOWN)
        {
            throw new IllegalArgumentException("Cannot process a unknown type query");
        }

        this.pageSize = pageSize;
        this.query = query;
        this.requestContinuationToken = null;
        this.responseContinuationToken = null;
        this.requestQueryType = requestQueryType;
        this.responseQueryType = QueryType.UNKNOWN;
        this.queryResponse = null;
        this.isSqlQuery = true;
    }

    /**
     * Constructor for Query.
     *
     * @param pageSize page size for the query response to sendHttpRequest query over.
     * @param requestQueryType Type of query.
     * @throws IllegalArgumentException if the input parameters are invalid.
     */
    public Query(int pageSize, QueryType requestQueryType) throws IllegalArgumentException
    {
        if (pageSize <= 0)
        {
            throw new IllegalArgumentException("Page Size cannot be zero or negative");
        }

        if (requestQueryType == null || requestQueryType == QueryType.UNKNOWN)
        {
            throw new IllegalArgumentException("Cannot process a unknown type query");
        }

        this.pageSize = pageSize;
        this.query = null;
        this.requestContinuationToken = null;
        this.responseContinuationToken = null;
        this.requestQueryType = requestQueryType;
        this.responseQueryType = QueryType.UNKNOWN;
        this.queryResponse = null;
        this.isSqlQuery = false;
    }

    /**
     * Continuation token to be used for next query sendHttpRequest.
     *
     * @param continuationToken token to be used for next query sendHttpRequest. Can be {@code null}.
     * @throws IOException if sending the sendHttpRequest is unsuccessful because of input parameters.
     * @throws IotHubException if sending the sendHttpRequest is unsuccessful at the Hub.
     */
    private void continueQuery(String continuationToken) throws IOException, IotHubException
    {
        this.requestContinuationToken = continuationToken;

            sendQueryRequest(
                    this.credentialCache,
                    this.azureSasCredential,
                    this.iotHubConnectionString,
                    this.url,
                    this.httpMethod,
                    this.httpConnectTimeout,
                    this.httpReadTimeout,
                    this.proxy);
    }

    /**
     * Sends sendHttpRequest for the query to the IotHub.
     *
     * @param iotHubConnectionString Hub Connection String.
     * @param url URL to Query on.
     * @param method HTTP Method for the requesting a query.
     * @param httpConnectTimeout the http connect timeout to use for this sendHttpRequest.
     * @param httpReadTimeout the http read timeout to use for this sendHttpRequest.
     * @param proxy the proxy to use, or null if no proxy should be used.
     * @return QueryResponse object which holds the response Iterator.
     * @throws IOException If any of the input parameters are not valid.
     * @throws IotHubException If HTTP response other then status ok is received.
     */
    @SuppressWarnings("UnusedReturnValue") // Public method
    public QueryResponse sendQueryRequest(IotHubConnectionString iotHubConnectionString,
                                          URL url,
                                          HttpMethod method,
                                          int httpConnectTimeout,
                                          int httpReadTimeout,
                                          Proxy proxy) throws IOException, IotHubException
    {
        if (iotHubConnectionString == null || url == null || method == null)
        {
            throw new IllegalArgumentException("Input parameters cannot be null");
        }

        this.iotHubConnectionString = iotHubConnectionString;
        this.url = url;
        this.httpMethod = method;

        this.httpConnectTimeout = httpConnectTimeout;
        this.httpReadTimeout = httpReadTimeout;

        this.proxy = proxy;

        byte[] payload;
        Map<String, String> queryHeaders = new HashMap<>();

        if (this.requestContinuationToken != null)
        {
            queryHeaders.put(CONTINUATION_TOKEN_KEY, requestContinuationToken);
        }
        queryHeaders.put(PAGE_SIZE_KEY, String.valueOf(pageSize));

        if (isSqlQuery)
        {
            QueryRequestParser requestParser = new QueryRequestParser(this.query);
            payload = requestParser.toJson().getBytes(StandardCharsets.UTF_8);
        }
        else
        {
            payload = new byte[0];
        }

        HttpRequest httpRequest = new HttpRequest(
            url,
            method,
            payload,
            new IotHubServiceSasToken(iotHubConnectionString).toString(),
            proxy);

        httpRequest.setReadTimeoutMillis(httpReadTimeout);
        httpRequest.setConnectTimeoutMillis(httpConnectTimeout);
        httpRequest.setHeaders(queryHeaders);

        HttpResponse httpResponse = httpRequest.send();

        this.responseContinuationToken = null;
        Map<String, String> headers = httpResponse.getHeaderFields();
        for (Map.Entry<String, String> header : headers.entrySet())
        {
            switch (header.getKey())
            {
                case CONTINUATION_TOKEN_KEY:
                    this.responseContinuationToken = header.getValue();
                    break;
                case ITEM_TYPE_KEY:
                    this.responseQueryType = QueryType.fromString(header.getValue());
                    break;
                default:
                    break;
            }
        }

        if (this.responseQueryType == null || this.responseQueryType == QueryType.UNKNOWN)
        {
            throw new IotHubException("Query response type is not defined by IotHub");
        }

        if (this.requestQueryType != this.responseQueryType)
        {
            throw new IotHubException("Query response does not match query sendHttpRequest");
        }

        this.queryResponse = new QueryResponse(new String(httpResponse.getBody(), StandardCharsets.UTF_8));
        return this.queryResponse;
    }

    /**
     * Sends sendHttpRequest for the query to the IotHub.
     *
     * @param credentialCache The RBAC authorization token provider. May be null if azureSasCredential or iotHubConnectionString is not.
     * @param azureSasCredential The SAS authorization token provider. May be null if credential or iotHubConnectionString is not.
     * @param iotHubConnectionString The iot hub connection string that SAS tokens will be derived from. May be null if azureSasCredential or credential is not.
     * @param url URL to Query on.
     * @param method HTTP Method for the requesting a query.
     * @param httpConnectTimeout the http connect timeout to use for this sendHttpRequest.
     * @param httpReadTimeout the http read timeout to use for this sendHttpRequest.
     * @param proxy the proxy to use, or null if no proxy should be used.
     * @return QueryResponse object which holds the response Iterator.
     * @throws IOException If any of the input parameters are not valid.
     * @throws IotHubException If HTTP response other then status ok is received.
     */
    public QueryResponse sendQueryRequest(
            TokenCredentialCache credentialCache,
            AzureSasCredential azureSasCredential,
            IotHubConnectionString iotHubConnectionString,
            URL url,
            HttpMethod method,
            int httpConnectTimeout,
            int httpReadTimeout,
            Proxy proxy)
            throws IOException, IotHubException
    {
        this.url = url;
        this.httpMethod = method;
        this.azureSasCredential = azureSasCredential;
        this.credentialCache = credentialCache;
        this.iotHubConnectionString = iotHubConnectionString;

        this.httpConnectTimeout = httpConnectTimeout;
        this.httpReadTimeout = httpReadTimeout;

        this.proxy = proxy;

        byte[] payload;
        Map<String, String> queryHeaders = new HashMap<>();

        if (this.requestContinuationToken != null)
        {
            queryHeaders.put(CONTINUATION_TOKEN_KEY, requestContinuationToken);
        }
        queryHeaders.put(PAGE_SIZE_KEY, String.valueOf(pageSize));

        if (isSqlQuery)
        {
            QueryRequestParser requestParser = new QueryRequestParser(this.query);
            payload = requestParser.toJson().getBytes(StandardCharsets.UTF_8);
        }
        else
        {
            payload = new byte[0];
        }

        String authorizationToken;
        if (credentialCache != null)
        {
            authorizationToken = this.credentialCache.getTokenString();
        }
        else if (azureSasCredential != null)
        {
            authorizationToken = azureSasCredential.getSignature();
        }
        else
        {
            authorizationToken = new IotHubServiceSasToken(iotHubConnectionString).toString();
        }

        HttpRequest httpRequest = new HttpRequest(
            url,
            method,
            payload,
            authorizationToken,
            proxy);

        httpRequest.setReadTimeoutMillis(httpReadTimeout);
        httpRequest.setConnectTimeoutMillis(httpConnectTimeout);

        HttpResponse httpResponse = httpRequest.send();

        this.responseContinuationToken = null;
        Map<String, String> headers = httpResponse.getHeaderFields();
        for (Map.Entry<String, String> header : headers.entrySet())
        {
            switch (header.getKey())
            {
                case CONTINUATION_TOKEN_KEY:
                    this.responseContinuationToken = header.getValue();
                    break;
                case ITEM_TYPE_KEY:
                    this.responseQueryType = QueryType.fromString(header.getValue());
                    break;
                default:
                    break;
            }
        }

        if (this.responseQueryType == null || this.responseQueryType == QueryType.UNKNOWN)
        {
            throw new IotHubException("Query response type is not defined by IotHub");
        }

        if (this.requestQueryType != this.responseQueryType)
        {
            throw new IotHubException("Query response does not match query sendHttpRequest");
        }

        this.queryResponse = new QueryResponse(new String(httpResponse.getBody(), StandardCharsets.UTF_8));
        return this.queryResponse;
    }

    /**
     * Getter for the continuation token received on response
     * @return continuation token. Can be {@code null}.
     */
    private String getContinuationToken()
    {
        return this.responseContinuationToken;
    }

    /**
     * Returns the availability of next element in the query response.
     *
     * @return the availability of next element in the query response.
     * @throws IOException if sending the sendHttpRequest is unsuccessful because of input parameters.
     * @throws IotHubException if sending the sendHttpRequest is unsuccessful at the Hub.
     */
    public boolean hasNext() throws IOException, IotHubException
    {
        boolean isNextAvailable = this.queryResponse.hasNext();
        if (!isNextAvailable && this.getContinuationToken() != null)
        {
            this.continueQuery(this.getContinuationToken());
            return this.queryResponse.hasNext();
        }
        else
        {
            return isNextAvailable;
        }
    }

    /**
     * provides the next element in query response.
     *
     * @return the next element in query response.
     * @throws IOException if sending the sendHttpRequest is unsuccessful because of input parameters.
     * @throws IotHubException if sending the sendHttpRequest is unsuccessful at the Hub.
     * @throws NoSuchElementException if no further elements are available.
     */
    public Object next() throws IOException, IotHubException, NoSuchElementException
    {
       if (this.hasNext())
       {
           return queryResponse.next();
       }
       else
       {
           throw new NoSuchElementException();
       }
    }
}
