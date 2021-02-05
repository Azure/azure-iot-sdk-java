/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.serializer.QueryRequestParser;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
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

    private int pageSize;
    private final String query;
    private final boolean isSqlQuery;

    private String requestContinuationToken;
    private String responseContinuationToken;

    private final QueryType requestQueryType;
    private QueryType responseQueryType;

    private QueryResponse queryResponse;

    private IotHubConnectionString iotHubConnectionString;
    private AzureSasCredential azureSasCredential;
    private TokenCredential credential;
    private URL url;
    private HttpMethod httpMethod;

    private int httpConnectTimeout;
    private int httpReadTimeout;

    private Proxy proxy;

    /**
     * Constructor for Query.
     *
     * @param query Sql style query to be sent to IotHub.
     * @param pageSize page size for the query response to request query over.
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
     * @param pageSize page size for the query response to request query over.
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
     * Continuation token to be used for next query request.
     *
     * @param continuationToken token to be used for next query request. Can be {@code null}.
     * @throws IOException if sending the request is unsuccessful because of input parameters.
     * @throws IotHubException if sending the request is unsuccessful at the Hub.
     */
    private void continueQuery(String continuationToken) throws IOException, IotHubException
    {
        this.requestContinuationToken = continuationToken;

        if (this.credential != null)
        {
            sendQueryRequest(
                    this.credential,
                    this.url,
                    this.httpMethod,
                    this.httpConnectTimeout,
                    this.httpReadTimeout,
                    this.proxy);
        }
        else if (this.azureSasCredential != null)
        {
            sendQueryRequest(
                    this.azureSasCredential,
                    this.url,
                    this.httpMethod,
                    this.httpConnectTimeout,
                    this.httpReadTimeout,
                    this.proxy);
        }
        else
        {
            sendQueryRequest(
                    this.iotHubConnectionString,
                    this.url,
                    this.httpMethod,
                    this.httpConnectTimeout,
                    this.httpReadTimeout,
                    this.proxy);
        }
    }

    /**
     * Sends request for the query to the IotHub.
     *
     * @param iotHubConnectionString Hub Connection String.
     * @param url URL to Query on.
     * @param method HTTP Method for the requesting a query.
     * @param timeoutInMs Unused.
     * @return QueryResponse object which holds the response Iterator.
     * @throws IOException If any of the input parameters are not valid.
     * @throws IotHubException If HTTP response other then status ok is received.
     * @deprecated use {@link #sendQueryRequest(IotHubConnectionString, URL, HttpMethod, int, int, Proxy)} instead.
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public QueryResponse sendQueryRequest(
            IotHubConnectionString iotHubConnectionString,
            URL url,
            HttpMethod method,
            Long timeoutInMs)
            throws IOException, IotHubException
    {
        if (iotHubConnectionString == null || url == null || method == null)
        {
            throw new IllegalArgumentException("Input parameters cannot be null");
        }

        this.iotHubConnectionString = iotHubConnectionString;
        this.url = url;
        this.httpMethod = method;

        this.httpConnectTimeout = DeviceTwinClientOptions.DEFAULT_HTTP_CONNECT_TIMEOUT_MS;
        this.httpReadTimeout = DeviceTwinClientOptions.DEFAULT_HTTP_READ_TIMEOUT_MS;

        byte[] payload;
        Map<String, String> queryHeaders = new HashMap<>();

        if (this.requestContinuationToken != null)
        {
            queryHeaders.put(CONTINUATION_TOKEN_KEY, requestContinuationToken);
        }
        queryHeaders.put(PAGE_SIZE_KEY, String.valueOf(pageSize));

        DeviceOperations.setHeaders(queryHeaders);

        if (isSqlQuery)
        {
            QueryRequestParser requestParser = new QueryRequestParser(this.query);
            payload = requestParser.toJson().getBytes();
        }
        else
        {
            payload = new byte[0];
        }

        HttpResponse httpResponse = DeviceOperations.request(iotHubConnectionString, url, method, payload, null, this.httpConnectTimeout, this.httpReadTimeout, null);

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
            throw new IOException("Query response type is not defined by IotHub");
        }

        if (this.requestQueryType != this.responseQueryType)
        {
            throw new IOException("Query response does not match query request");
        }

        this.queryResponse = new QueryResponse(new String(httpResponse.getBody()));
        return this.queryResponse;
    }

    /**
     * Sends request for the query to the IotHub.
     *
     * @param iotHubConnectionString Hub Connection String.
     * @param url URL to Query on.
     * @param method HTTP Method for the requesting a query.
     * @param httpConnectTimeout the http connect timeout to use for this request.
     * @param httpReadTimeout the http read timeout to use for this request.
     * @param proxy the proxy to use, or null if no proxy should be used.
     * @return QueryResponse object which holds the response Iterator.
     * @throws IOException If any of the input parameters are not valid.
     * @throws IotHubException If HTTP response other then status ok is received.
     */
    public QueryResponse sendQueryRequest(
            IotHubConnectionString iotHubConnectionString,
            URL url,
            HttpMethod method,
            int httpConnectTimeout,
            int httpReadTimeout,
            Proxy proxy)
            throws IOException, IotHubException
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

        DeviceOperations.setHeaders(queryHeaders);

        if (isSqlQuery)
        {
            QueryRequestParser requestParser = new QueryRequestParser(this.query);
            payload = requestParser.toJson().getBytes();
        }
        else
        {
            payload = new byte[0];
        }

        HttpResponse httpResponse = DeviceOperations.request(iotHubConnectionString, url, method, payload, null, httpConnectTimeout, httpReadTimeout, proxy);

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
            throw new IOException("Query response type is not defined by IotHub");
        }

        if (this.requestQueryType != this.responseQueryType)
        {
            throw new IOException("Query response does not match query request");
        }

        this.queryResponse = new QueryResponse(new String(httpResponse.getBody()));
        return this.queryResponse;
    }

    /**
     * Sends request for the query to the IotHub.
     *
     * @param azureSasCredential The SAS authorization token provider.
     * @param url URL to Query on.
     * @param method HTTP Method for the requesting a query.
     * @param httpConnectTimeout the http connect timeout to use for this request.
     * @param httpReadTimeout the http read timeout to use for this request.
     * @param proxy the proxy to use, or null if no proxy should be used.
     * @return QueryResponse object which holds the response Iterator.
     * @throws IOException If any of the input parameters are not valid.
     * @throws IotHubException If HTTP response other then status ok is received.
     */
    public QueryResponse sendQueryRequest(
            AzureSasCredential azureSasCredential,
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

        DeviceOperations.setHeaders(queryHeaders);

        if (isSqlQuery)
        {
            QueryRequestParser requestParser = new QueryRequestParser(this.query);
            payload = requestParser.toJson().getBytes();
        }
        else
        {
            payload = new byte[0];
        }

        HttpResponse httpResponse = DeviceOperations.request(azureSasCredential.getSignature(), url, method, payload, null, httpConnectTimeout, httpReadTimeout, proxy);

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
            throw new IOException("Query response type is not defined by IotHub");
        }

        if (this.requestQueryType != this.responseQueryType)
        {
            throw new IOException("Query response does not match query request");
        }

        this.queryResponse = new QueryResponse(new String(httpResponse.getBody()));
        return this.queryResponse;
    }

    /**
     * Sends request for the query to the IotHub.
     *
     * @param credential The authorization token provider.
     * @param url URL to Query on.
     * @param method HTTP Method for the requesting a query.
     * @param httpConnectTimeout the http connect timeout to use for this request.
     * @param httpReadTimeout the http read timeout to use for this request.
     * @param proxy the proxy to use, or null if no proxy should be used.
     * @return QueryResponse object which holds the response Iterator.
     * @throws IOException If any of the input parameters are not valid.
     * @throws IotHubException If HTTP response other then status ok is received.
     */
    public QueryResponse sendQueryRequest(
            TokenCredential credential,
            URL url,
            HttpMethod method,
            int httpConnectTimeout,
            int httpReadTimeout,
            Proxy proxy)
            throws IOException, IotHubException
    {
        this.url = url;
        this.httpMethod = method;

        this.credential = credential;
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

        DeviceOperations.setHeaders(queryHeaders);

        if (isSqlQuery)
        {
            QueryRequestParser requestParser = new QueryRequestParser(this.query);
            payload = requestParser.toJson().getBytes();
        }
        else
        {
            payload = new byte[0];
        }

        HttpResponse httpResponse =
                DeviceOperations.request(
                        credential.getToken(new TokenRequestContext()).block().getToken(),
                        url,
                        method,
                        payload,
                        null,
                        httpConnectTimeout,
                        httpReadTimeout,
                        proxy);

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
            throw new IOException("Query response type is not defined by IotHub");
        }

        if (this.requestQueryType != this.responseQueryType)
        {
            throw new IOException("Query response does not match query request");
        }

        this.queryResponse = new QueryResponse(new String(httpResponse.getBody()));
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
     * @throws IOException if sending the request is unsuccessful because of input parameters.
     * @throws IotHubException if sending the request is unsuccessful at the Hub.
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
     * @throws IOException if sending the request is unsuccessful because of input parameters.
     * @throws IotHubException if sending the request is unsuccessful at the Hub.
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
