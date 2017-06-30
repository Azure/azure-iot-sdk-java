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

/*
    Sql style query IotHub for twin, jobs, device jobs or raw data
 */
public class Query
{
    private static final String CONTINUATION_TOKEN_KEY = "x-ms-continuation";
    private static final String ITEM_TYPE_KEY = "x-ms-item-type";
    private static final String PAGE_SIZE_KEY = "x-ms-max-item-count";

    private int pageSize;
    private String query;

    private String requestContinuationToken;
    private String responseContinuationToken;

    private QueryType requestQueryType;
    private QueryType responseQueryType;

    private QueryResponse queryResponse;

    /**
     * Constructor for Query
     * @param query Sql style query to be sent to IotHub
     * @param pageSize page size for the query response to request query over
     * @param requestQueryType Type of query
     * @throws IllegalArgumentException if the input parameters are invalid
     */
    Query(String query, int pageSize, QueryType requestQueryType) throws IllegalArgumentException
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
    }

    /**
     * Continuation token to be used for next query request
     * @param continuationToken token to be used for next query request. Can be {@code null}
     */
    void continueQuery(String continuationToken)
    {
        //Codes_SRS_QUERY_25_005: [The method shall update the request continuation token and request pagesize which shall be used for processing subsequent query request.]
        this.requestContinuationToken = continuationToken;
    }

    /**
     * Continuation token and page size to be used for next query request
     * @param continuationToken token to be used for next query request. Can be {@code null}
     * @param pageSize size batch for this query
     * @throws IllegalArgumentException
     */
    void continueQuery(String continuationToken, int pageSize) throws IllegalArgumentException
    {
        if (pageSize <= 0)
        {
            //Codes_SRS_QUERY_25_006: [If the pagesize is zero or negative the constructor shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Page Size cannot be zero or negative");
        }

        this.pageSize = pageSize;
        this.requestContinuationToken = continuationToken;
    }

    /**
     * Sends request for the query to the IotHub
     * @param iotHubConnectionString Hub Connection String
     * @param url URL to Query on
     * @param method HTTP Method for the requesting a query
     * @param timeoutInMs Maximum time to wait for the hub to respond
     * @return QueryResponse object which holds the response Iterator
     * @throws IOException If any of the input parameters are not valid
     * @throws IotHubException If HTTP response other then status ok is received
     */
    QueryResponse sendQueryRequest(IotHubConnectionString iotHubConnectionString,
                                   URL url,
                                   HttpMethod method,
                                   Long timeoutInMs) throws IOException, IotHubException
    {
        Map<String, String> queryHeaders = new HashMap<>();

        if (this.requestContinuationToken != null)
        {
            queryHeaders.put(CONTINUATION_TOKEN_KEY, requestContinuationToken);
        }
        //Codes_SRS_QUERY_25_007: [The method shall set the http headers x-ms-continuation and x-ms-max-item-count with request continuation token and page size if they were not null.]
        queryHeaders.put(PAGE_SIZE_KEY, String.valueOf(pageSize));

        DeviceOperations.setHeaders(queryHeaders);

        //Codes_SRS_QUERY_25_008: [The method shall obtain the serilaized query by using QueryRequestParser.]
        QueryRequestParser requestParser = new QueryRequestParser(this.query);

        //Codes_SRS_QUERY_25_009: [The method shall use the provided HTTP Method and send request to IotHub with the serialized body over the provided URL.]
        HttpResponse httpResponse = DeviceOperations.request(iotHubConnectionString, url, method, requestParser.toJson().getBytes(), null, timeoutInMs);

        this.responseContinuationToken = null;
        Map<String, String> headers = httpResponse.getHeaderFields();
        //Codes_SRS_QUERY_25_010: [The method shall read the continuation token (x-ms-continuation) and reponse type (x-ms-item-type) from the HTTP Headers and save it.]
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
     * Getter for the continuation token received on response
     * @return continuation token. Can be {@code null}.
     */
    String getContinuationToken()
    {
        //Codes_SRS_QUERY_25_014: [The method shall return the continuation token found in response to a query (which can be null).]
        return this.responseContinuationToken;
    }

    /**
     * @return the availability of next element in the query response
     */
    boolean hasNext()
    {
        //Codes_SRS_QUERY_25_015: [The method shall return true if next element from QueryResponse is available and false otherwise.]
        return queryResponse.hasNext();
    }

    /**
     * @return the next element in query response
     */
    Object next()
    {
        //Codes_SRS_QUERY_25_016: [The method shall return the next element for this QueryResponse.]
        return queryResponse.next();
    }
}
