/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.serializer.QueryRequestParser;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class QueryCollection
{
    private static final String CONTINUATION_TOKEN_KEY = "x-ms-continuation";
    private static final String ITEM_TYPE_KEY = "x-ms-item-type";
    private static final String PAGE_SIZE_KEY = "x-ms-max-item-count";

    private int pageSize;
    private String query;
    private boolean isSqlQuery;

    private QueryType requestQueryType;
    private QueryType responseQueryType;

    private String responseContinuationToken;

    private IotHubConnectionString iotHubConnectionString;
    private URL url;
    private HttpMethod httpMethod;
    private long timeout;

    private boolean isInitialQuery;

    /**
     * Constructor for sql based queries
     *
     * @param query the sql query to use
     * @param pageSize the size of the page to return per query collection response
     * @param requestQueryType the type of query this is
     * @param iotHubConnectionString the connection string to connect with to query against
     * @param url the url to query against
     * @param httpMethod the http method to call with the query
     * @param timeout timeout until the request expires
     * @throws IllegalArgumentException if page size is 0 or negative, or if the query type is null or unknown, of if the query string is null or empty,
     *  or if the provided connection string is null, or if the provided url is null, or if the provided http method is null
     */
    protected QueryCollection(String query, int pageSize, QueryType requestQueryType, IotHubConnectionString iotHubConnectionString, URL url, HttpMethod httpMethod, long timeout)
    {
        //Codes_SRS_QUERYCOLLECTION_34_037: [If the provided connection string, url, or http method is null, this function shall throw an IllegalArgumentException.]
        //Codes_SRS_QUERYCOLLECTION_34_004: [If the provided QueryType is null or UNKNOWN, an IllegalArgumentException shall be thrown.]
        //Codes_SRS_QUERYCOLLECTION_34_002: [If the provided page size is not a positive integer, an IllegalArgumentException shall be thrown.]
        this.validateQueryRequestArguments(iotHubConnectionString, url, httpMethod, pageSize, requestQueryType);

        //Codes_SRS_QUERYCOLLECTION_34_001: [If the provided query string is invalid or does not contain both SELECT and FROM, an IllegalArgumentException shall be thrown.]
        ParserUtility.validateQuery(query);

        //Codes_SRS_QUERYCOLLECTION_34_006: [This function shall save the provided query, pageSize, requestQueryType, iotHubConnectionString, url, httpMethod and timeout.]
        this.pageSize = pageSize;
        this.query = query;
        this.requestQueryType = requestQueryType;
        this.iotHubConnectionString = iotHubConnectionString;
        this.responseContinuationToken = null;
        this.httpMethod = httpMethod;
        this.timeout = timeout;
        this.url = url;
        this.responseQueryType = QueryType.UNKNOWN;

        //Codes_SRS_QUERYCOLLECTION_34_008: [The constructed QueryCollection shall be a sql query type.]
        this.isSqlQuery = true;

        this.isInitialQuery = true;
    }

    /**
     * Constructor for non-sql based queries
     *
     * @param pageSize the size of the page to return per query collection response
     * @param requestQueryType the type of query this is
     * @param iotHubConnectionString the connection string to connect with to query against
     * @param url the url to query against
     * @param httpMethod the http method to call with the query
     * @param timeout timeout until the request expires
     * @throws IllegalArgumentException if page size is 0 or negative, or if the query type is null or unknown,
     *  or if the provided connection string is null, or if the provided url is null, or if the provided http method is null
     */
    protected QueryCollection(int pageSize, QueryType requestQueryType, IotHubConnectionString iotHubConnectionString, URL url, HttpMethod httpMethod, long timeout)
    {
        //Codes_SRS_QUERYCOLLECTION_34_038: [If the provided connection string, url, or http method is null, this function shall throw an IllegalArgumentException.]
        //Codes_SRS_QUERYCOLLECTION_34_003: [If the provided page size is not a positive integer, an IllegalArgumentException shall be thrown.]
        //Codes_SRS_QUERYCOLLECTION_34_005: [If the provided QueryType is null or UNKNOWN, an IllegalArgumentException shall be thrown.]
        this.validateQueryRequestArguments(iotHubConnectionString, url, httpMethod, pageSize, requestQueryType);

        //Codes_SRS_QUERYCOLLECTION_34_007: [This function shall save the provided pageSize, requestQueryType, iotHubConnectionString, url, httpMethod and timeout.]
        this.pageSize = pageSize;
        this.requestQueryType = requestQueryType;

        this.query = null;
        this.responseQueryType = QueryType.UNKNOWN;
        this.responseContinuationToken = null;
        this.iotHubConnectionString = iotHubConnectionString;
        this.httpMethod = httpMethod;
        this.timeout = timeout;
        this.url = url;

        //Codes_SRS_QUERYCOLLECTION_34_009: [The constructed QueryCollection shall not be a sql query type.]
        this.isSqlQuery = false;

        this.isInitialQuery = true;
    }

    /**
     * Retrieves a page of results for a query.
     *
     * @param options the options for the query. If a continuation token is set in these options, it shall override any
     *                previously saved tokens. The page size of these options shall override any previously saved page size
     * @return The QueryCollectionResponse containing the full page of results and the continuation token for the next query
     * @throws IOException If an IOException occurs when calling the Service API, or if the results of that call are unexpected
     * @throws IotHubException If an IotHubException occurs when calling the Service API
     */
    private QueryCollectionResponse<String> sendQueryRequest(QueryOptions options) throws IOException, IotHubException
    {
        //Codes_SRS_QUERYCOLLECTION_34_011: [If the provided query options is not null and contains a continuation token, it shall be put in the query headers to continue the query.]
        //Codes_SRS_QUERYCOLLECTION_34_012: [If a continuation token is not provided from the passed in query options, but there is a continuation token saved in the latest queryCollectionResponse, that token shall be put in the query headers to continue the query.]
        //Codes_SRS_QUERYCOLLECTION_34_013: [If the provided query options is not null, the query option's page size shall be included in the query headers.]
        //Codes_SRS_QUERYCOLLECTION_34_014: [If the provided query options is null, this object's page size shall be included in the query headers.]
        DeviceOperations.setHeaders(buildQueryHeaders(options));

        //Codes_SRS_QUERYCOLLECTION_34_015: [If this is a sql query, the payload of the query message shall be set to the json bytes representation of this object's query string.]
        //Codes_SRS_QUERYCOLLECTION_34_016: [If this is not a sql query, the payload of the query message shall be set to empty bytes.]
        byte[] payload = null;
        if (isSqlQuery)
        {
            QueryRequestParser requestParser = new QueryRequestParser(this.query);
            payload = requestParser.toJson().getBytes();
        }
        else
        {
            payload = new byte[0];
        }

        //Codes_SRS_QUERYCOLLECTION_34_017: [This function shall send an HTTPS request using DeviceOperations.]
        HttpResponse httpResponse = DeviceOperations.request(this.iotHubConnectionString, this.url, this.httpMethod, payload, null, this.timeout);

        //Codes_SRS_QUERYCOLLECTION_34_018: [The method shall read the continuation token (x-ms-continuation) and response type (x-ms-item-type) from the HTTP Headers and save it.]
        handleQueryResponse(httpResponse);

        //Codes_SRS_QUERYCOLLECTION_34_021: [The method shall create a QueryResponse object with the contents from the response body and its continuation token and return it.]
        this.isInitialQuery = false;
        return new QueryCollectionResponse<String>(
        		new String(httpResponse.getBody(), "UTF-8"), this.responseContinuationToken);
    }

    /**
     * Returns if this query collection has a next collection to return.
     *
     * @return true if there is another page to return in the query and false otherwise
     */
    protected boolean hasNext()
    {
        if (this.isInitialQuery)
        {
            //Codes_SRS_QUERYCOLLECTION_34_025: [If this query is the initial query, this function shall return true.]
            return true;
        }
        else
        {
            //Codes_SRS_QUERYCOLLECTION_34_026: [If this query is not the initial query, this function shall return true if there is a continuation token and false otherwise.]
            return (this.responseContinuationToken != null);
        }
    }

    /**
     * Returns the next QueryCollectionResponse object. The query shall continue with the same page size and use the internally saved continuation token.
     * @return The next QueryCollectionResponse object or null if there is not a next QueryCollectionResponse object
     * @throws IOException If an IOException occurs when calling the Service API, or if the results of that call are unexpected
     * @throws IotHubException If an IotHubException occurs when calling the Service API
     */
    protected QueryCollectionResponse<String> next() throws IOException, IotHubException
    {
        //Codes_SRS_QUERYCOLLECTION_34_032: [If this object has a next set to return, this function shall return it.]
        //Codes_SRS_QUERYCOLLECTION_34_033: [If this object does not have a next set to return, this function shall return null.]
        QueryOptions options = new QueryOptions();
        options.setPageSize(this.pageSize);
        return this.next(options);
    }

    /**
     * Returns the next QueryCollectionResponse object. If the provided query options have a continuation token, the
     * query shall continue from that token. The query shall use the page size set in the query options.
     *
     * @param options The options for the query
     * @return The next QueryCollectionResponse object or null if there is no next QueryCollectionResponse object
     * @throws IOException If an IOException occurs when calling the Service API, or if the results of that call are unexpected
     * @throws IotHubException If an IotHubException occurs when calling the Service API
     */
    protected QueryCollectionResponse<String> next(QueryOptions options) throws IOException, IotHubException
    {
        if (this.hasNext())
        {
            //Codes_SRS_QUERYCOLLECTION_34_034: [If this object has a next set to return using the provided query options, this function shall return it.]
            return this.sendQueryRequest(options);
        }
        else
        {
            //Codes_SRS_QUERYCOLLECTION_34_035: [If this object does not have a next set to return, this function shall return null.]
            return null;
        }
    }

    /**
     * Getter for page size.
     * @return the page size of this
     */
    protected Integer getPageSize()
    {
        //Codes_SRS_QUERYCOLLECTION_34_036: [This function shall return the saved page size.]
        return this.pageSize;
    }

    private void validateQueryRequestArguments(IotHubConnectionString iotHubConnectionString, URL url, HttpMethod method, int pageSize, QueryType requestQueryType)
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
