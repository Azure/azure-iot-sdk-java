/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.QueryResponseParser;

import java.io.IOException;
import java.util.Iterator;

/**
 * Response for the Query
 */
public class QueryResponse implements Iterator<Object>
{
    private Iterator<?> responseElementsIterator;

    /**
     * Creates an object for the query response
     * @param jsonString json response for query to parse
     * @throws IOException If any of the input parameters are invalid
     */
    QueryResponse(String jsonString) throws IOException
    {
        if (jsonString == null || jsonString.length() == 0)
        {
            //Codes_SRS_QUERY_RESPONSE_25_002: [If the jsonString is null or empty, the constructor shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("response cannot be null or empty");
        }

        //Codes_SRS_QUERY_RESPONSE_25_001: [The constructor shall parse the json response using QueryResponseParser and set the iterator.]
        QueryResponseParser responseParser = new QueryResponseParser(jsonString);
        this.responseElementsIterator = responseParser.getJsonItems().iterator();
    }

    /**
     * returns the availability of next response
     * @return true if present and false otherwise
     */
    @Override
    public boolean hasNext()
    {
        //Codes_SRS_QUERY_RESPONSE_25_003: [The method shall return true if next element from QueryResponse is available and false otherwise.]
        return this.responseElementsIterator.hasNext();
    }

    /**
     * returns next element in the response
     * @return next element in the response
     */
    @Override
    public Object next()
    {
        //Codes_SRS_QUERY_RESPONSE_25_004: [The method shall return the next element for this QueryResponse.]
        return this.responseElementsIterator.next();
    }
}
