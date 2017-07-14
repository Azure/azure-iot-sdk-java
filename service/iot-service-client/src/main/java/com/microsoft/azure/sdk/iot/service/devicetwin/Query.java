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
    private String query;
    private boolean isSqlQuery;

    private String requestContinuationToken;
    private String responseContinuationToken;

    private QueryType requestQueryType;
    private QueryType responseQueryType;

    private QueryResponse queryResponse;

    private IotHubConnectionString iotHubConnectionString;
    private URL url;
    private HttpMethod httpMethod;
    private long timeout;

    /**
     * Constructor for Query
     * @param query Sql style query to be sent to IotHub
     * @param pageSize page size for the query response to request query over
     * @param requestQueryType Type of query
     * @throws IllegalArgumentException if the input parameters are invalid
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
     * Constructor for Query
     * @param pageSize page size for the query response to request query over
     * @param requestQueryType Type of query
     * @throws IllegalArgumentException if the input parameters are invalid
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
     * Continuation token to be used for next query request
     * @param continuationToken token to be used for next query request. Can be {@code null}
     * @throws IOException if sending the request is unsuccessful because of input parameters
     * @throws IotHubException if sending the request is unsuccessful at the Hub
     */
    private void continueQuery(String continuationToken) throws IOException, IotHubException
    {
        //Codes_SRS_QUERY_25_005: [The method shall update the request continuation token and request pagesize which shall be used for processing subsequent query request.]
        this.requestContinuationToken = continuationToken;
        //Codes_SRS_QUERY_25_018: [The method shall send the query request again.]
        sendQueryRequest(this.iotHubConnectionString, this.url, this.httpMethod, this.timeout);
    }

    /**
     * Continuation token and page size to be used for next query request
     * @param continuationToken token to be used for next query request. Can be {@code null}
     * @param pageSize size batch for this query
     * @throws IOException if sending the request is unsuccessful because of input parameters
     * @throws IotHubException if sending the request is unsuccessful at the Hub
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
        sendQueryRequest(this.iotHubConnectionString, this.url, this.httpMethod, this.timeout);
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
        this.timeout = timeoutInMs;

        byte[] payload = null;
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
        HttpResponse httpResponse = DeviceOperations.request(iotHubConnectionString, url, method, payload, null, timeoutInMs);

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
    private String getContinuationToken()
    {
        //Codes_SRS_QUERY_25_014: [The method shall return the continuation token found in response to a query (which can be null).]
        return this.responseContinuationToken;
    }

    /**
     * Returns the availability of next element in the query response
     * @return the availability of next element in the query response
     * @throws IOException if sending the request is unsuccessful because of input parameters
     * @throws IotHubException if sending the request is unsuccessful at the Hub
     */
    public boolean hasNext() throws IOException, IotHubException
    {
        //Codes_SRS_QUERY_25_015: [The method shall return true if next element from QueryResponse is available and false otherwise.]
        boolean isNextAvailable = this.queryResponse.hasNext();
        if (!isNextAvailable && this.getContinuationToken() != null)
        {
            //Codes_SRS_QUERY_25_021: [If no further query response is available, then this method shall continue to request query to IotHub if continuation token is available.]
            this.continueQuery(this.getContinuationToken());
            return this.queryResponse.hasNext();
        }
        else
        {
            //Codes_SRS_QUERY_25_015: [The method shall return true if next element from QueryResponse is available and false otherwise.]
            return isNextAvailable;
        }
    }

    /**
     * provides the next element in query response
     * @return the next element in query response
     * @throws IOException if sending the request is unsuccessful because of input parameters
     * @throws IotHubException if sending the request is unsuccessful at the Hub
     * @throws NoSuchElementException if no further elements are available
     */
    public Object next() throws IOException, IotHubException, NoSuchElementException
    {
        //Codes_SRS_QUERY_25_016: [The method shall return the next element for this QueryResponse.]
       if (this.hasNext())
       {
           return queryResponse.next();
       }
       else
       {
           //Codes_SRS_QUERY_25_022: [The method shall check if any further elements are available by calling hasNext and if none is available then it shall throw NoSuchElementException.]
           throw new NoSuchElementException();
       }

    }
}
