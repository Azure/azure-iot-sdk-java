/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QueryRequestParser
{
    private static final String QUERY_TAG = "query";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(QUERY_TAG)
    private String query = null;

    /**
     * CONSTRUCTOR
     * Create an instance of the QueryRequestParser based on the provided query.
     *
     * @param query is the name of the blob (file name in the blob)
     * @throws IllegalArgumentException if the query is null, empty, or not valid.
     */
    public QueryRequestParser(String query) throws IllegalArgumentException
    {
        //Codes_SRS_QUERY_REQUEST_PARSER_25_001: [The constructor shall create an instance of the QueryRequestParser.]
        //Codes_SRS_QUERY_REQUEST_PARSER_25_003: [If the provided query is null, empty, or not valid, the constructor shall throws IllegalArgumentException.]
        ParserUtility.validateQuery(query);

        //Codes_SRS_QUERY_REQUEST_PARSER_25_002: [The constructor shall set the query value with the provided query.]
        this.query = query;
    }

    /**
     * Convert this class in a valid json.
     *
     * @return a valid json that represents the content of this class.
     */
    public String toJson()
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        //Codes_SRS_QUERY_REQUEST_PARSER_25_004: [The toJson shall return a string with a json that represents the contents of the QueryRequestParser.]
        return gson.toJson(this);
    }

    /**
     * Empty constructor: Used only to keep GSON happy.
     */
    @SuppressWarnings("unused")
    protected QueryRequestParser()
    {
    }
}
