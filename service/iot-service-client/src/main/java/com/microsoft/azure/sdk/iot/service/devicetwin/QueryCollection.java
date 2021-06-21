/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.azure.core.credential.AzureSasCredential;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.serializer.QueryRequestParser;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class QueryCollection
{
    private static final String CONTINUATION_TOKEN_KEY = "x-ms-continuation";
    private static final String ITEM_TYPE_KEY = "x-ms-item-type";
    private static final String PAGE_SIZE_KEY = "x-ms-max-item-count";

    private final int pageSize;
    private final String query;
    private final boolean isSqlQuery;

    private final QueryType requestQueryType;
    private QueryType responseQueryType;

    private String responseContinuationToken;

    private final URL url;
    private final HttpMethod httpMethod;

    private final int httpConnectTimeout;
    private final int httpReadTimeout;

    private IotHubConnectionString iotHubConnectionString;
    private AzureSasCredential azureSasCredential;
    private TokenCredentialCache credentialCache;

    private Proxy proxy;

    private boolean isInitialQuery;

    /**
     * Constructor for sql based queries.
     *
     * @param query the sql query to use.
     * @param pageSize the size of the page to return per query collection response.
     * @param requestQueryType the type of query this is.
     * @param iotHubConnectionString the connection string to connect with to query against.
     * @param url the url to query against.
     * @param httpMethod the http method to call with the query.
     * @param timeout unused.
     * @throws IllegalArgumentException if page size is 0 or negative, or if the query type is null or unknown, of if the query string is null or empty,
     *  or if the provided connection string is null, or if the provided url is null, or if the provided http method is null.
     * @deprecated use {@link #QueryCollection(String, int, QueryType, IotHubConnectionString, URL, HttpMethod, int, int, Proxy)} instead.
     */
    @Deprecated
    protected QueryCollection(
            String query,
            int pageSize,
            QueryType requestQueryType,
            IotHubConnectionString iotHubConnectionString,
            URL url,
            HttpMethod httpMethod,
            long timeout)
    {
        this.validateQueryRequestArguments(iotHubConnectionString, url, httpMethod, pageSize, requestQueryType);

        ParserUtility.validateQuery(query);

        this.pageSize = pageSize;
        this.query = query;
        this.requestQueryType = requestQueryType;
        this.responseContinuationToken = null;
        this.httpMethod = httpMethod;
        this.httpConnectTimeout = DeviceTwinClientOptions.DEFAULT_HTTP_CONNECT_TIMEOUT_MS;
        this.httpReadTimeout = DeviceTwinClientOptions.DEFAULT_HTTP_READ_TIMEOUT_MS;
        this.url = url;
        this.responseQueryType = QueryType.UNKNOWN;
        this.isSqlQuery = true;
        this.isInitialQuery = true;
        this.iotHubConnectionString = iotHubConnectionString;
    }

    /**
     * Constructor for non-sql based queries.
     *
     * @param pageSize the size of the page to return per query collection response.
     * @param requestQueryType the type of query this is.
     * @param iotHubConnectionString the connection string to connect with to query against.
     * @param url the url to query against.
     * @param httpMethod the http method to call with the query.
     * @param timeout unused.
     * @throws IllegalArgumentException if page size is 0 or negative, or if the query type is null or unknown,
     *  or if the provided connection string is null, or if the provided url is null, or if the provided http method is null
     * @deprecated use {@link #QueryCollection(int, QueryType, IotHubConnectionString, URL, HttpMethod, int, int, Proxy)} instead.
     */
    @Deprecated
    protected QueryCollection(
            int pageSize,
            QueryType requestQueryType,
            IotHubConnectionString iotHubConnectionString,
            URL url,
            HttpMethod httpMethod,
            long timeout)
    {
        this.validateQueryRequestArguments(iotHubConnectionString, url, httpMethod, pageSize, requestQueryType);

        this.pageSize = pageSize;
        this.requestQueryType = requestQueryType;
        this.query = null;
        this.responseQueryType = QueryType.UNKNOWN;
        this.responseContinuationToken = null;
        this.httpMethod = httpMethod;
        this.httpConnectTimeout = DeviceTwinClientOptions.DEFAULT_HTTP_CONNECT_TIMEOUT_MS;
        this.httpReadTimeout = DeviceTwinClientOptions.DEFAULT_HTTP_READ_TIMEOUT_MS;
        this.url = url;
        this.isSqlQuery = false;
        this.isInitialQuery = true;
        this.iotHubConnectionString = iotHubConnectionString;
    }

    /**
     * Constructor for sql based queries
     *
     * @param query the sql query to use.
     * @param pageSize the size of the page to return per query collection response.
     * @param requestQueryType the type of query this is.
     * @param iotHubConnectionString the connection string to connect with to query against.
     * @param url the url to query against.
     * @param httpMethod the http method to call with the query.
     * @param httpConnectTimeout the http connect timeout to use.
     * @param httpReadTimeout the http read timeout to use.
     * @param proxy proxy type
     * @throws IllegalArgumentException if page size is 0 or negative, or if the query type is null or unknown, of if the query string is null or empty,
     *  or if the provided connection string is null, or if the provided url is null, or if the provided http method is null.
     */
    @SuppressWarnings("SameParameterValue") // Generic method for executing queries, "requestQueryType" and "httpMethod" can have any service-allowed value.
    protected QueryCollection(
            String query,
            int pageSize,
            QueryType requestQueryType,
            IotHubConnectionString iotHubConnectionString,
            URL url,
            HttpMethod httpMethod,
            int httpConnectTimeout,
            int httpReadTimeout,
            Proxy proxy)
    {
        this.validateQueryRequestArguments(iotHubConnectionString, url, httpMethod, pageSize, requestQueryType);

        ParserUtility.validateQuery(query);

        this.pageSize = pageSize;
        this.query = query;
        this.requestQueryType = requestQueryType;
        this.responseContinuationToken = null;
        this.httpMethod = httpMethod;
        this.httpConnectTimeout = httpConnectTimeout;
        this.httpReadTimeout = httpReadTimeout;
        this.proxy = proxy;
        this.url = url;
        this.responseQueryType = QueryType.UNKNOWN;
        this.isSqlQuery = true;
        this.isInitialQuery = true;
        this.iotHubConnectionString = iotHubConnectionString;
    }

    /**
     * Constructor for non-sql based queries.
     *
     * @param pageSize the size of the page to return per query collection response.
     * @param requestQueryType the type of query this is.
     * @param iotHubConnectionString the connection string to connect with to query against.
     * @param url the url to query against.
     * @param httpMethod the http method to call with the query.
     * @param httpConnectTimeout the http connect timeout to use.
     * @param httpReadTimeout the http read timeout to use.
     * @param proxy the proxy type
     * @throws IllegalArgumentException if page size is 0 or negative, or if the query type is null or unknown,
     *  or if the provided connection string is null, or if the provided url is null, or if the provided http method is null.
     */
    protected QueryCollection(
            int pageSize,
            QueryType requestQueryType,
            IotHubConnectionString iotHubConnectionString,
            URL url,
            HttpMethod httpMethod,
            int httpConnectTimeout,
            int httpReadTimeout,
            Proxy proxy)
    {
        this.validateQueryRequestArguments(iotHubConnectionString, url, httpMethod, pageSize, requestQueryType);

        this.pageSize = pageSize;
        this.requestQueryType = requestQueryType;
        this.query = null;
        this.responseQueryType = QueryType.UNKNOWN;
        this.responseContinuationToken = null;
        this.httpMethod = httpMethod;
        this.httpConnectTimeout = httpConnectTimeout;
        this.httpReadTimeout = httpReadTimeout;
        this.proxy = proxy;
        this.url = url;
        this.isSqlQuery = false;
        this.isInitialQuery = true;
        this.iotHubConnectionString = iotHubConnectionString;
    }

    QueryCollection(
            String query,
            int pageSize,
            QueryType requestQueryType,
            TokenCredentialCache credentialCache,
            URL url,
            HttpMethod httpMethod,
            int httpConnectTimeout,
            int httpReadTimeout,
            Proxy proxy)
    {
        this.pageSize = pageSize;
        this.requestQueryType = requestQueryType;
        this.query = query;
        this.responseQueryType = QueryType.UNKNOWN;
        this.responseContinuationToken = null;
        this.httpMethod = httpMethod;
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
            QueryType requestQueryType,
            AzureSasCredential azureSasCredential,
            URL url,
            HttpMethod httpMethod,
            int httpConnectTimeout,
            int httpReadTimeout,
            Proxy proxy)
    {
        this.pageSize = pageSize;
        this.requestQueryType = requestQueryType;
        this.query = query;
        this.responseQueryType = QueryType.UNKNOWN;
        this.responseContinuationToken = null;
        this.httpMethod = httpMethod;
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
        DeviceOperations.setHeaders(buildQueryHeaders(options));

        byte[] payload;
        if (isSqlQuery)
        {
            QueryRequestParser requestParser = new QueryRequestParser(this.query);
            payload = requestParser.toJson().getBytes();
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

        HttpResponse httpResponse = DeviceOperations.request(
                authorizationToken,
                this.url,
                this.httpMethod,
                payload,
                null,
                this.httpConnectTimeout,
                this.httpReadTimeout,
                this.proxy);

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
    protected boolean hasNext()
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
     * Returns the next QueryCollectionResponse object. If the provided query options have a continuation token, the
     * query shall continue from that token. The query shall use the page size set in the query options.
     *
     * @param options The options for the query.
     * @return The next QueryCollectionResponse object or null if there is no next QueryCollectionResponse object.
     * @throws IOException If an IOException occurs when calling the Service API, or if the results of that call are unexpected.
     * @throws IotHubException If an IotHubException occurs when calling the Service API.
     */
    protected QueryCollectionResponse<String> next(QueryOptions options) throws IOException, IotHubException
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
    protected Integer getPageSize()
    {
        return this.pageSize;
    }

    private void validateQueryRequestArguments(
            IotHubConnectionString iotHubConnectionString,
            URL url,
            HttpMethod method,
            int pageSize,
            QueryType requestQueryType)
    {
        if (iotHubConnectionString == null || url == null || method == null)
        {
            throw new IllegalArgumentException("Input parameters cannot be null");
        }

        if (pageSize <= 0)
        {
            throw new IllegalArgumentException("Page Size cannot be zero or negative");
        }

        if (requestQueryType == null || requestQueryType == QueryType.UNKNOWN)
        {
            throw new IllegalArgumentException("Cannot process a unknown type query");
        }
    }

    private void validateResponseQueryType(QueryType responseQueryType, QueryType requestQueryType) throws IOException
    {
        if (responseQueryType == null || responseQueryType == QueryType.UNKNOWN)
        {
            throw new IOException("Query response type is not defined by IotHub");
        }

        if (requestQueryType != responseQueryType)
        {
            throw new IOException("Query response does not match query request");
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

        validateResponseQueryType(this.responseQueryType, this.requestQueryType);
    }
}
