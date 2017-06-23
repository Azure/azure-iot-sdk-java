/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class QueryResponseParser
{
    private Gson gson;
    private enum TYPE
    {
        @SerializedName("unknown")
        UNKNOWN("unknown"),

        @SerializedName("twin")
        TWIN("twin"),

        @SerializedName("deviceJob")
        DEVICE_JOB("deviceJob"),

        @SerializedName("jobResponse")
        JOB_RESPONSE("jobResponse"),

        @SerializedName("raw")
        RAW("raw");

        private final String type;

        TYPE(String type)
        {
            this.type = type;
        }

        public String getValue()
        {
            return type;
        }
    }

    private static final String TYPE_TAG = "type";
    @Expose(serialize = false, deserialize = true)
    @SerializedName(TYPE_TAG)
    private TYPE type = null;

    private static final String ITEMS_TAG = "items";
    @Expose(serialize = false, deserialize = true)
    @SerializedName(ITEMS_TAG)
    private JsonObject[] jsonItems = null;

    private static final String CONTINUATION_TOKEN_TAG = "continuationToken";
    @Expose(serialize = false, deserialize = true)
    @SerializedName(CONTINUATION_TOKEN_TAG)
    private String continuationToken = null;


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
        QueryResponseParser queryResponseParser;

        //Codes_SRS_QUERY_RESPONSE_PARSER_25_003: [If the provided json is null, empty, or not valid, the constructor shall throws IllegalArgumentException.]
        ParserUtility.validateStringUTF8(json);
        try
        {
            queryResponseParser = gson.fromJson(json, QueryResponseParser.class);
        }
        catch (JsonSyntaxException malformed)
        {
            throw new IllegalArgumentException("Malformed json:" + malformed);
        }

        //Codes_SRS_QUERY_RESPONSE_PARSER_25_004: [If the provided json do not contains a valid type, continuationToken and jsonItems, the constructor shall throws IllegalArgumentException.]
        //Codes_SRS_QUERY_RESPONSE_PARSER_25_005: [If the provided json do not contains one of the keys type, continuationToken and jsonItems, the constructor shall throws IllegalArgumentException.]
        //Codes_SRS_QUERY_RESPONSE_PARSER_25_006: [If the provided json is of type other than twin, raw, deviceJob or jobResponse, the constructor shall throws IllegalArgumentException.]
        if (queryResponseParser.type == null)
        {
            throw new IllegalArgumentException("Not expected type");
        }
        else if (queryResponseParser.type.compareTo(TYPE.TWIN) !=0 &&
                queryResponseParser.type.compareTo(TYPE.DEVICE_JOB) !=0 &&
                queryResponseParser.type.compareTo(TYPE.JOB_RESPONSE) !=0 &&
                queryResponseParser.type.compareTo(TYPE.RAW) !=0)
        {
            throw new IllegalArgumentException("Not expected type");
        }

        ParserUtility.validateStringUTF8(queryResponseParser.type.toString());
        ParserUtility.validateStringUTF8(queryResponseParser.continuationToken);
        ParserUtility.validateStringUTF8(gson.toJson(queryResponseParser.jsonItems));

        //Codes_SRS_QUERY_RESPONSE_PARSER_25_002: [The constructor shall parse the provided json and initialize type, continuationToken and jsonItems using the information in the json.]
        this.type =  queryResponseParser.type; //TYPE.valueOf(queryResponseParser.typeString);
        this.continuationToken = queryResponseParser.continuationToken;
        this.jsonItems = queryResponseParser.jsonItems;
    }

    /**
     * Getter for the type of response.
     *
     * @return string type.
     */
    public String getType()
    {
        //Codes_SRS_QUERY_RESPONSE_PARSER_25_007: [The getType shall return the string stored in type enum.]
        return this.type.getValue();
    }

    /**
     * Getter for Json Array
     * @return the array of json as string
     */
    public String getJsonItemsArray()
    {
        //Codes_SRS_QUERY_RESPONSE_PARSER_25_008: [The getJsonItemsArray shall return the array of json items as string .]
        return gson.toJson(this.jsonItems);
    }

    /**
     * Getter for Continuation token
     * @return the continuation token as string
     */
    public String getContinuationToken()
    {
        //Codes_SRS_QUERY_RESPONSE_PARSER_25_009: [The getContinuationToken shall return the string stored in continuationToken.]
        return continuationToken;
    }

    /**
     * Getter for Twin Parser collection from Query Response
     * @return A list of twin parser objects
     * @throws IllegalStateException if this API is called when response was of a type other than twin
     * @throws IllegalArgumentException if twin parser cannot parse
     */
    public List<TwinParser> getTwins() throws IllegalStateException, IllegalArgumentException
    {
        if (this.type.compareTo(TYPE.TWIN) == 0)
        {
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_012: [The getTwins shall throw IllegalArgumentException if the twin array from the json cannot be parsed]
            List<TwinParser> twinParsers = new LinkedList<>();

            for (JsonObject twin : this.jsonItems)
            {
                TwinParser twinParser = new TwinParser();
                twinParser.enableTags();
                twinParser.updateTwin(gson.toJson(twin));
                twinParsers.add(twinParser);
            }
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_010: [The getTwins shall return the collection of twin parsers as retrieved and parsed from json.]
            return twinParsers;
        }
        else
        {
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_011: [The getTwins shall throw IllegalStateException if the type represented by json is not "twin"]
            throw new IllegalStateException("Json does not contain twin type");
        }
    }

    /**
     * Getter for device jobs collection obtained from Query Response
     * @return A list of deviceJobs objects
     * @throws IllegalStateException if this API is called when response was of a type other than device jobs
     * @throws IllegalArgumentException if device jobs parser cannot parse
     */
    public List getDeviceJobs() throws IllegalStateException, IllegalArgumentException
    {
        if (this.type.compareTo(TYPE.DEVICE_JOB) == 0)
        {
            // placeholder for creating device jobs and return the collection
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_013: [The getDeviceJobs shall return the collection of device jobs parsers as retrieved and parsed from json.]
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_015: [The getDeviceJobs shall throw IllegalArgumentException if the items array from the json cannot be parsed]
            return null;
        }
        else
        {
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_014: [The getDeviceJobs shall throw IllegalStateException if the type represented by json is not "deviceJobs"]
            throw new IllegalStateException("Json does not contain device jobs type");
        }
    }

    /**
     * Getter for jobs collection obtained from Query Response
     * @return A list of jobs objects
     * @throws IllegalStateException if this API is called when response was of a type other than jobs
     * @throws IllegalArgumentException if jobs parser cannot parse
     */
    public List getJobs() throws IllegalStateException, IllegalArgumentException
    {
        if (this.type.compareTo(TYPE.JOB_RESPONSE) == 0)
        {
            // placeholder for creating jobs response and return the collection
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_016: [The getJobs shall return the collection of jobs parsers as retrieved and parsed from json.]
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_018: [The getJobs shall throw IllegalArgumentException if the jobs array from the json cannot be parsed]
            return null;
        }
        else
        {
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_017: [The getJobs shall throw IllegalStateException if the type represented by json is not "jobResponse"]
            throw new IllegalStateException("Json does not contain Jobs type");
        }
    }

    /**
     * Getter for raw data collection obtained from Query Response
     * @return A list of raw data objects as string
     * @throws IllegalStateException if this API is called when response was of a type other than raw
     */
    public List<String> getRawData() throws IllegalStateException
    {
        if (this.type.compareTo(TYPE.RAW) == 0)
        {
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_019: [The getRawData shall return the collection of raw data json as string as retrieved and parsed from json.]
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_021: [The getRawData shall throw IllegalArgumentException if the raw data array from the json cannot be parsed]
            List<String> rawJsons = new LinkedList<>();

            for (JsonObject json : this.jsonItems)
            {
                rawJsons.add(gson.toJson(json));
            }
            return rawJsons;
        }
        else
        {
            //Codes_SRS_QUERY_RESPONSE_PARSER_25_020: [The getRawData shall throw IllegalStateException if the type represented by json is not "raw"]
            throw new IllegalStateException("Json does not contain raw type");
        }
    }

}
