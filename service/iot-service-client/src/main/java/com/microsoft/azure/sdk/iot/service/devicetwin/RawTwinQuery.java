/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;

import java.io.IOException;
import java.util.NoSuchElementException;

public class RawTwinQuery
{
    private IotHubConnectionString iotHubConnectionString = null;
    private final long USE_DEFAULT_TIMEOUT = 0;
    private final int DEFAULT_PAGE_SIZE = 100;

    private RawTwinQuery()
    {

    }

    /**
     * Static constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     * @return The instance of RawTwinQuery
     * @throws IOException This exception is thrown if the object creation failed
     */
    public static RawTwinQuery createFromConnectionString(String connectionString) throws IOException
    {
        if (connectionString == null || connectionString.length() == 0)
        {

            //Codes_SRS_RAW_QUERY_25_001: [ The constructor shall throw IllegalArgumentException if the input string is null or empty ]
            throw new IllegalArgumentException("Connection string cannot be null or empty");
        }

        //Codes_SRS_RAW_QUERY_25_003: [ The constructor shall create a new RawTwinQuery instance and return it ]
        RawTwinQuery rawTwinQuery = new RawTwinQuery();

        //Codes_SRS_RAW_QUERY_25_001: [ The constructor shall throw IllegalArgumentException if the input string is null or empty ]
        rawTwinQuery.iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        return rawTwinQuery;
    }

    /**
     * Creates a query object for this query
     * @param sqlQuery Sql style query for Raw data over twin
     * @param pageSize Size to restrict response of query by
     * @return Object for the query
     * @throws IotHubException If IotHub did not respond successfully to the query
     * @throws IOException If any of the input parameters are incorrect
     */
    public synchronized Query query(String sqlQuery, Integer pageSize) throws IotHubException, IOException
    {
        if (sqlQuery == null || sqlQuery.length() == 0)
        {
            //Codes_SRS_RAW_QUERY_25_004: [ The method shall throw IllegalArgumentException if the query is null or empty.]
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        if (pageSize <= 0)
        {
            //Codes_SRS_RAW_QUERY_25_005: [ The method shall throw IllegalArgumentException if the page size is zero or negative.]
            throw new IllegalArgumentException("pagesize cannot be negative or zero");
        }

        //Codes_SRS_RAW_QUERY_25_007: [ The method shall create a new Query Object of Type Raw. ]
        Query rawQuery = new Query(sqlQuery, pageSize, QueryType.RAW);
        //Codes_SRS_RAW_QUERY_25_006: [ The method shall build the URL for this operation by calling getUrlTwinQuery ]
        //Codes_SRS_RAW_QUERY_25_008: [ The method shall send a Query Request to IotHub as HTTP Method Post on the query Object by calling sendQueryRequest.]
        rawQuery.sendQueryRequest(iotHubConnectionString, iotHubConnectionString.getUrlTwinQuery(), HttpMethod.POST, USE_DEFAULT_TIMEOUT);
        return rawQuery;
    }

    /**
     * Creates a query object for this query using default page size
     * @param sqlQuery Sql style query for Raw data over twin
     * @return Object for the query
     * @throws IotHubException If IotHub did not respond successfully to the query
     * @throws IOException If any of the input parameters are incorrect
     */
    public synchronized Query query(String sqlQuery) throws IotHubException, IOException
    {
        //Codes_SRS_RAW_QUERY_25_009: [ If the pageSize if not provided then a default pageSize of 100 is used for the query.]
        return this.query(sqlQuery, DEFAULT_PAGE_SIZE);
    }

    /**
     * Returns the availability of next element in response. Sends the request again (if possible)
     * to retrieve response until no response is found.
     * @param query Object corresponding to the query
     * @return True if available and false otherwise
     * @throws IotHubException If IotHub could not respond successfully to the query request
     * @throws IOException If any of the input parameters are incorrect
     */
    public synchronized boolean hasNext(Query query) throws IotHubException, IOException
    {
        if (query == null)
        {
            //Codes_SRS_RAW_QUERY_25_010: [ The method shall throw IllegalArgumentException if query is null ]
            throw new IllegalArgumentException("Query cannot be null");
        }

        //Codes_SRS_RAW_QUERY_25_011: [ The method shall check if a response to query is avaliable by calling hasNext on the query object.]
        return query.hasNext();
    }

    /**
     * Returns the next json element available in response
     * @param query Object corresponding for this query
     * @return Next json element as a response to this query
     * @throws IOException If any of input parameters are incorrect
     * @throws IotHubException If IotHub could not respond successfully to the query request
     * @throws NoSuchElementException If no other element is found
     */
    public synchronized String next(Query query) throws IOException, IotHubException, NoSuchElementException
    {
        //Codes_SRS_RAW_QUERY_25_015: [ The method shall check if hasNext returns true and throw NoSuchElementException otherwise ]
        //Codes_SRS_RAW_QUERY_25_018: [ If the input query is null, then this method shall throw IllegalArgumentException ]

        if (query == null)
        {
            throw new IllegalArgumentException();
        }

        Object nextObject = query.next();

        if (nextObject instanceof String)
        {
            return (String) nextObject;
        }
        else
        {
            //Codes_SRS_RAW_QUERY_25_017: [ If the next element from the query response is an object other than String, then this method shall throw IOException ]
            throw new IOException("Received a response that could not be parsed");
        }

    }
}
