/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.serializer.QueryRequestParser;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.TokenCredentialType;
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

/*
    Sql style query IotHub for twin, jobs, device jobs or raw data
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
        //Codes_SRS_QUERY_25_001: [The constructor shall validate query and save query, pagesize and request type]
        //Codes_SRS_QUERY_25_002: [If the query is null or empty or is not a valid sql query (containing select and from), the constructor shall throw an IllegalArgumentException.]
        ParserUtility.validateQuery(query);

        if (pageSize <= 0)
        {
            //Codes_SRS_QUERY_25_003: [If the pagesize is zero or negative the constructor shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Page Size cannot be zero or negative");
        }

        if (requestQueryType == null || requestQueryType == QueryType.UNKNOWN)
        {
            //Codes_SRS_QUERY_25_004: [If the QueryType is null or unknown then the constructor shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Cannot process a unknown type query");
        }

        this.pageSize = pageSize;
        this.query = query;
        this.requestContinuationToken = null;
        this.responseContinuationToken = null;
        this.requestQueryType = requestQueryType;
        this.responseQueryType = QueryType.UNKNOWN;
        this.queryResponse = null;
        //Codes_SRS_QUERY_25_017: [If the query is avaliable then isSqlQuery shall be set to true, and false otherwise.]
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
            //Codes_SRS_QUERY_25_003: [If the pagesize is zero or negative the constructor shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Page Size cannot be zero or negative");
        }

        if (requestQueryType == null || requestQueryType == QueryType.UNKNOWN)
        {
            //Codes_SRS_QUERY_25_004: [If the QueryType is null or unknown then the constructor shall throw an IllegalArgumentException.]
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
        //Codes_SRS_QUERY_25_005: [The method shall update the request continuation token and request pagesize which shall be used for processing subsequent query request.]
        this.requestContinuationToken = continuationToken;
        //Codes_SRS_QUERY_25_018: [The method shall send the query request again.]
        sendQueryRequest(this.iotHubConnectionString, this.url, this.httpMethod, this.httpConnectTimeout, this.httpReadTimeout, this.proxy);
    }

    /**
     * Continuation token and page size to be used for next query request.
     * @param continuationToken token to be used for next query request. Can be {@code null}.
     * @param pageSize size batch for this query.
     * @throws IOException if sending the request is unsuccessful because of input parameters.
     * @throws IotHubException if sending the request is unsuccessful at the Hub.
     */
    private void continueQuery(String continuationToken, int pageSize) throws IOException, IotHubException
    {
        if (pageSize <= 0)
        {
            //Codes_SRS_QUERY_25_006: [If the pagesize is zero or negative the constructor shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Page Size cannot be zero or negative");
        }

        this.pageSize = pageSize;
        this.requestContinuationToken = continuationToken;
        //Codes_SRS_QUERY_25_018: [The method shall send the query request again.]
        sendQueryRequest(this.iotHubConnectionString, this.url, this.httpMethod, this.httpConnectTimeout, this.httpReadTimeout, this.proxy);
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
    @Deprecated
    public QueryResponse sendQueryRequest(IotHubConnectionString iotHubConnectionString,
                                          URL url,
                                          HttpMethod method,
                                          Long timeoutInMs) throws IOException, IotHubException
    {
        if (iotHubConnectionString == null || url == null || method == null)
        {
            //Codes_SRS_QUERY_25_019: [This method shall throw IllegalArgumentException if any of the parameters are null or empty.]
            throw new IllegalArgumentException("Input parameters cannot be null");
        }

        //Codes_SRS_QUERY_25_020: [This method shall save all the parameters for future use.]
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
        //Codes_SRS_QUERY_25_007: [The method shall set the http headers x-ms-continuation and x-ms-max-item-count with request continuation token and page size if they were not null.]
        queryHeaders.put(PAGE_SIZE_KEY, String.valueOf(pageSize));

        DeviceOperations.setHeaders(queryHeaders);

        if (isSqlQuery)
        {
            //Codes_SRS_QUERY_25_008: [The method shall obtain the serilaized query by using QueryRequestParser.]
            QueryRequestParser requestParser = new QueryRequestParser(this.query);
            payload = requestParser.toJson().getBytes();
        }
        else
        {
            payload = new byte[0];
        }

        //Codes_SRS_QUERY_25_009: [The method shall use the provided HTTP Method and send request to IotHub with the serialized body over the provided URL.]
        HttpResponse httpResponse = DeviceOperations.request(iotHubConnectionString, url, method, payload, null, this.httpConnectTimeout, this.httpReadTimeout, null);

        this.responseContinuationToken = null;
        Map<String, String> headers = httpResponse.getHeaderFields();
        //Codes_SRS_QUERY_25_010: [The method shall read the continuation token (x-ms-continuation) and response type (x-ms-item-type) from the HTTP Headers and save it.]
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
            //Codes_SRS_QUERY_25_012: [If the response type is Unknown or not found then this method shall throw IOException.]
            throw new IOException("Query response type is not defined by IotHub");
        }

        if (this.requestQueryType != this.responseQueryType)
        {
            //Codes_SRS_QUERY_25_011: [If the request type and response does not match then the method shall throw IOException.]
            throw new IOException("Query response does not match query request");
        }

        //Codes_SRS_QUERY_25_013: [The method shall create a QueryResponse object with the contents from the response body and save it.]
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
     * @param authenticationTokenProvider The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed.
     * @param tokenCredentialType The type of authentication tokens that the provided {@link TokenCredential}
     *                          implementation will always give.
     * @param url URL to Query on.
     * @param method HTTP Method for the requesting a query.
     * @param httpConnectTimeout the http connect timeout to use for this request.
     * @param httpReadTimeout the http read timeout to use for this request.
     * @param proxy the proxy to use, or null if no proxy should be used.
     * @return QueryResponse object which holds the response Iterator.
     * @throws IOException If any of the input parameters are not valid.
     * @throws IotHubException If HTTP response other then status ok is received.
     */
    public QueryResponse sendQueryRequest(TokenCredential authenticationTokenProvider,
                                          TokenCredentialType tokenCredentialType,
                                          URL url,
                                          HttpMethod method,
                                          int httpConnectTimeout,
                                          int httpReadTimeout,
                                          Proxy proxy) throws IOException, IotHubException
    {
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

        HttpResponse httpResponse = DeviceOperations.request(authenticationTokenProvider, tokenCredentialType, url, method, payload, null, httpConnectTimeout, httpReadTimeout, proxy);

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
