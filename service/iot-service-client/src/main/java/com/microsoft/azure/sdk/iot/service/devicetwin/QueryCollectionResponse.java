/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.QueryResponseParser;

import java.util.Collection;

public class QueryCollectionResponse<E>
{
    private final Collection<E> responseElementsCollection;
    private final String continuationToken;

    /**
     * Constructor that takes a json string and parses it into a Collection.
     *
     * @param jsonString The json string to parse into a Collection
     * @param continuationToken The continuation token to save
     * @throws IllegalArgumentException if responseElementsCollection is null or empty
     */
    QueryCollectionResponse(String jsonString, String continuationToken)
    {
        if (jsonString == null || jsonString.length() == 0)
        {
            //Codes_SRS_QUERY_COLLECTION_RESPONSE_34_001: [If the provided jsonString is null or empty, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("response cannot be null or empty");
        }

        //Codes_SRS_QUERY_COLLECTION_RESPONSE_34_002: [This constructor shall parse the provided jsonString using the QueryResponseParser class into an Collection and save it.]
        QueryResponseParser responseParser = new QueryResponseParser(jsonString);
        this.responseElementsCollection = (Collection<E>) responseParser.getJsonItems();

        //Codes_SRS_QUERY_COLLECTION_RESPONSE_34_003: [This constructor shall save the provided continuation token.]
        this.continuationToken = continuationToken;
    }

    /**
     * Constructor that takes a collection and a continuation token.
     *
     * @param responseElementsCollection The collection to save
     * @param continuationToken The continuation token to save
     * @throws IllegalArgumentException if responseElementsCollection is null
     */
    QueryCollectionResponse(Collection<E> responseElementsCollection, String continuationToken)
    {
        if (responseElementsCollection == null)
        {
            //Codes_SRS_QUERY_COLLECTION_RESPONSE_34_007: [If the provided Collection is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Provided Collection must not be null");
        }

        //Codes_SRS_QUERY_COLLECTION_RESPONSE_34_004: [This constructor shall save the provided continuation token and Collection.]
        this.responseElementsCollection = responseElementsCollection;
        this.continuationToken = continuationToken;
    }

    /**
     * Getter for continuation token
     * @return the saved continuation token. The continuation token may be null.
     */
    public String getContinuationToken()
    {
        //Codes_SRS_QUERY_COLLECTION_RESPONSE_34_005: [This function shall return the saved continuation token.]
        return this.continuationToken;
    }

    /**
     * Getter for collection
     * @return the saved Collection
     */
    public Collection<E> getCollection()
    {
        //Codes_SRS_QUERY_COLLECTION_RESPONSE_34_006: [This function shall return the saved Collection.]
        return this.responseElementsCollection;
    }
}
