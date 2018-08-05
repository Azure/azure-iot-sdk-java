/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.LinkedList;
import java.util.List;

public class QueryResponseParser
{
    private Gson gson;
    private JsonObject[] jsonItems = null;

    /**
     * CONSTRUCTOR
     * Create an instance of the QueryResponseParser using the information in the provided json.
     *
     * @param json is the string that contains a valid json with the QueryResponse.
     * @throws IllegalArgumentException if the json is null, empty, or not valid.
     */
    public QueryResponseParser(String json) throws IllegalArgumentException
    {
        //Codes_SRS_QUERY_RESPONSE_PARSER_25_001: [The constructor shall create an instance of the QueryResponseParser.]
        gson = new GsonBuilder().disableHtmlEscaping().create();

        //Codes_SRS_QUERY_RESPONSE_PARSER_25_003: [If the provided json is null, empty, or not valid, the constructor shall throws IllegalArgumentException.]
        if((json == null) || json.isEmpty())
        {
            throw new IllegalArgumentException("parameter is null or empty");
        }

        try
        {
            this.jsonItems = gson.fromJson(json, JsonObject[].class);
        }
        catch (JsonSyntaxException malformed)
        {
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_004: [If the provided json do not contains a valid array of json items the constructor shall throws IllegalArgumentException.]
            throw new IllegalArgumentException("Malformed json:" + malformed);
        }
    }

    /**
     * Getter for Json Items from Json Array
     * @return the array of json as string
     */
    public List<String> getJsonItems()
    {
        List<String> jsonElements = new LinkedList<>();

        for (JsonObject json : this.jsonItems)
        {
            jsonElements.add(gson.toJson(json));
        }
        //Codes_SRS_QUERY_RESPONSE_PARSER_25_008: [The getJsonItems shall return the list of json items as strings .]
        return jsonElements;
    }

    /**
     * Empty constructor: Used only to keep GSON happy.
     */
    @SuppressWarnings("unused")
    protected QueryResponseParser()
    {
    }
}
