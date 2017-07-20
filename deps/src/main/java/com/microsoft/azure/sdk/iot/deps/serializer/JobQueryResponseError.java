/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;

public class JobQueryResponseError
{
    // Code for the error
    private static final String CODE_TAG = "code";
    @SerializedName(CODE_TAG)
    private String code;

    // Description for the error
    private static final String DESCRIPTION_TAG = "description";
    @SerializedName(DESCRIPTION_TAG)
    private String description;

    /**
     * A method to create this object from Json
     * @param json valid json for error generated during query response
     * @return an object for this class
     * @throws IOException When provided json is invalid or cannot be parsed
     */
    public JobQueryResponseError fromJson(String json) throws IOException
    {
        if((json == null) || json.isEmpty())
        {
            //Codes_SRSJOB_QUERY_RESPONSE_ERROR_25_007: [If the input json is null or empty then this method shall throw IllegalArgumentException.]
            throw new IllegalArgumentException("Json is null or empty");
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        JobQueryResponseError jobQueryResponseError = null;
        try
        {
            //Codes_SRSJOB_QUERY_RESPONSE_ERROR_25_004: [This method shall save the values of code and description to this object.]
            jobQueryResponseError = gson.fromJson(json, JobQueryResponseError.class);
        }
        catch (JsonSyntaxException e)
        {
            //Codes_SRSJOB_QUERY_RESPONSE_ERROR_25_006: [This method shall throw IOException if parsing of json fails for any reason.]
            throw new IOException("Not a valid json");
        }

        //Codes_SRSJOB_QUERY_RESPONSE_ERROR_25_005: [This method shall throw IOException if either code and description is not present in the json.]
        if (jobQueryResponseError.code == null || jobQueryResponseError.code.isEmpty())
        {
            throw new IllegalArgumentException("Json does not contains " + CODE_TAG);
        }

        if (jobQueryResponseError.description == null)
        {
            throw new IllegalArgumentException("Json does not contains " + DESCRIPTION_TAG);
        }

        return jobQueryResponseError;
    }

    /**
     * Converts this object to json
     * @return a json string representing this object
     */
    public String toJson()
    {
        //Codes_SRSJOB_QUERY_RESPONSE_ERROR_25_003: [The method shall build the json with the values provided to this object.]
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.toJson(this);
    }

    /**
     * Getter for code
     * @return string for the code
     */
    public String getCode()
    {
        //Codes_SRSJOB_QUERY_RESPONSE_ERROR_25_001: [The getCode shall return the value of the code.]
        return code;
    }

    /**
     * getter for description
     * @return string for description
     */
    public String getDescription()
    {
        //Codes_SRSJOB_QUERY_RESPONSE_ERROR_25_002: [The getDescription shall return the value of the Description.]
        return description;
    }
}
