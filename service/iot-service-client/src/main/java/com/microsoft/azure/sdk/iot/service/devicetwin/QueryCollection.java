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

import static com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest.REQUEST_ID;

public class QueryCollection
{
    private static final String CONTINUATION_TOKEN_KEY = "x-ms-continuation";
    private static final String PAGE_SIZE_KEY = "x-ms-max-item-count";

    private final int pageSize;
    private final String query;
    private final boolean isSqlQuery;

    private String responseContinuationToken;

    private final URL url;

    private final int httpConnectTimeout;
    private final int httpReadTimeout;

    private IotHubConnectionString iotHubConnectionString;
    private AzureSasCredential azureSasCredential;
    private TokenCredentialCache credentialCache;

    private final Proxy proxy;

    private boolean isInitialQuery;

    /**
     * Constructor for sql based queries
     *
     * @param query the sql query to use.
     * @param pageSize the size of the page to return per query collection response.
     * @param iotHubConnectionString the connection string to connect with to query against.
     * @param url the url to query against.
     * @param httpConnectTimeout the http connect timeout to use.
     * @param httpReadTimeout the http read timeout to use.
     * @param proxy proxy type
     * @throws IllegalArgumentException if page size is 0 or negative, or if the query type is null or unknown, of if the query string is null or empty,
     *  or if the provided connection string is null, or if the provided url is null, or if the provided http method is null.
     */
    @SuppressWarnings("SameParameterValue") // Generic method for executing queries, "requestQueryType" and "httpMethod" can have any service-allowed value.
    QueryCollection(
        String query,
        int pageSize,
        IotHubConnectionString iotHubConnectionString,
        URL url,
        int httpConnectTimeout,
        int httpReadTimeout,
        Proxy proxy)
    {
        this.validateQueryRequestArguments(iotHubConnectionString, url, pageSize);

        ParserUtility.validateQuery(query);

        this.pageSize = pageSize;
        this.query = query;
        this.responseContinuationToken = null;
        this.httpConnectTimeout = httpConnectTimeout;
        this.httpReadTimeout = httpReadTimeout;
        this.proxy = proxy;
        this.url = url;
        this.isSqlQuery = true;
        this.isInitialQuery = true;
        this.iotHubConnectionString = iotHubConnectionString;
    }

    QueryCollection(
            String query,
            int pageSize,
            TokenCredentialCache credentialCache,
            URL url,
            int httpConnectTimeout,
            int httpReadTimeout,
            Proxy proxy)
    {
        this.pageSize = pageSize;
        this.query = query;
        this.responseContinuationToken = null;
        this.httpConnectTimeout = httpConnectTimeout;
        this.httpReadTimeout = httpReadTimeout;
        this.proxy = proxy;
        this.url = url;
        this.isSqlQuery = false;
        this.isInitialQuery = true;
        this.credentialCache = credentialCache;
    }

    QueryCollection(
            String query,
            int pageSize,
            AzureSasCredential azureSasCredential,
            URL url,
            int httpConnectTimeout,
            int httpReadTimeout,
            Proxy proxy)
    {
        this.pageSize = pageSize;
        this.query = query;
        this.responseContinuationToken = null;
        this.httpConnectTimeout = httpConnectTimeout;
        this.httpReadTimeout = httpReadTimeout;
        this.proxy = proxy;
        this.url = url;
        this.isSqlQuery = false;
        this.isInitialQuery = true;
        this.azureSasCredential = azureSasCredential;
    }

    /**
     * Retrieves a page of results for a query.
     *
     * @param options the options for the query. If a continuation token is set in these options, it shall override any
     *                previously saved tokens. The page size of these options shall override any previously saved page size.
     * @return The QueryCollectionResponse containing the full page of results and the continuation token for the next query.
     * @throws IOException If an IOException occurs when calling the Service API, or if the results of that call are unexpected.
     * @throws IotHubException If an IotHubException occurs when calling the Service API.
     */
    private QueryCollectionResponse<String> sendQueryRequest(QueryOptions options) throws IOException, IotHubException
    {
        byte[] payload;
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
        if (this.credentialCache != null)
        {
            authorizationToken = this.credentialCache.getTokenString();
        }
        else if (this.azureSasCredential != null)
        {
            authorizationToken = this.azureSasCredential.getSignature();
        }
        else
        {
            authorizationToken = new IotHubServiceSasToken(iotHubConnectionString).toString();
        }

        HttpRequest httpRequest = new HttpRequest(
            url,
            HttpMethod.POST,
            payload,
            authorizationToken,
            proxy);

        httpRequest.setReadTimeoutMillis(httpReadTimeout);
        httpRequest.setConnectTimeoutMillis(httpConnectTimeout);
        httpRequest.setHeaders(buildQueryHeaders(options));
        HttpResponse httpResponse = httpRequest.send();

        handleQueryResponse(httpResponse);

        this.isInitialQuery = false;
        return new QueryCollectionResponse<>(
                new String(httpResponse.getBody(), StandardCharsets.UTF_8), this.responseContinuationToken);
    }

    /**
     * Returns if this query collection has a next collection to return.
     *
     * @return true if there is another page to return in the query and false otherwise.
     */
    boolean hasNext()
    {
        if (this.isInitialQuery)
        {
            return true;
        }
        else
        {
            return (this.responseContinuationToken != null);
        }
    }

    /**
     * Returns the next QueryCollectionResponse object. The query shall continue with the same page size and use the internally saved continuation token.
     * @return The next QueryCollectionResponse object or null if there is not a next QueryCollectionResponse object.
     * @throws IOException If an IOException occurs when calling the Service API, or if the results of that call are unexpected.
     * @throws IotHubException If an IotHubException occurs when calling the Service API.
     */
    @SuppressWarnings("unused") // Used by reflection in tests
    protected QueryCollectionResponse<String> next() throws IOException, IotHubException
    {
        QueryOptions options = new QueryOptions();
        options.setPageSize(this.pageSize);
        return this.next(options);
    }

    /**
     * Returns the next QueryCollectionResponse object. If the provided query options have a continuation token, the
     * query shall continue from that token. The query shall use the page size set in the query options.
     *
     * @param options The options for the query.
     * @return The next QueryCollectionResponse object or null if there is no next QueryCollectionResponse object.
     * @throws IOException If an IOException occurs when calling the Service API, or if the results of that call are unexpected.
     * @throws IotHubException If an IotHubException occurs when calling the Service API.
     */
    QueryCollectionResponse<String> next(QueryOptions options) throws IOException, IotHubException
    {
        if (this.hasNext())
        {
            return this.sendQueryRequest(options);
        }
        else
        {
            return null;
        }
    }

    /**
     * Getter for page size.
     * @return the page size of this.
     */
    Integer getPageSize()
    {
        return this.pageSize;
    }

    private void validateQueryRequestArguments(IotHubConnectionString iotHubConnectionString, URL url, int pageSize)
    {
        if (iotHubConnectionString == null || url == null)
        {
            throw new IllegalArgumentException("Input parameters cannot be null");
        }

        if (pageSize <= 0)
        {
            throw new IllegalArgumentException("Page Size cannot be zero or negative");
        }
    }

    private Map<String, String> buildQueryHeaders(QueryOptions options)
    {
        Map<String, String> queryHeaders = new HashMap<>();
        if (options != null && options.getContinuationToken() != null)
        {
            queryHeaders.put(CONTINUATION_TOKEN_KEY, options.getContinuationToken());
        }
        else if (this.responseContinuationToken != null)
        {
            queryHeaders.put(CONTINUATION_TOKEN_KEY, this.responseContinuationToken);
        }

        if (options != null)
        {
            queryHeaders.put(PAGE_SIZE_KEY, String.valueOf(options.getPageSize()));
        }
        else
        {
            queryHeaders.put(PAGE_SIZE_KEY, String.valueOf(this.pageSize));
        }

        return queryHeaders;
    }

    private void handleQueryResponse(HttpResponse httpResponse) throws IOException
    {
        Map<String, String> headers = httpResponse.getHeaderFields();
        this.responseContinuationToken = null;
        for (Map.Entry<String, String> header : headers.entrySet())
        {
            if (CONTINUATION_TOKEN_KEY.equals(header.getKey()))
            {
                this.responseContinuationToken = header.getValue();
            }
        }
    }
}
