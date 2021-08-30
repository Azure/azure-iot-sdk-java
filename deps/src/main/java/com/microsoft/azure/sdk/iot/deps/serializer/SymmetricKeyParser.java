/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/**
 * Store primary and secondary keys
 * Provide function for key length validation
 */
public class SymmetricKeyParser
{
    private final transient Gson gson = new Gson();

    private static final String PRIMARY_KEY_SERIALIZED_NAME = "primaryKey";
    @SerializedName(PRIMARY_KEY_SERIALIZED_NAME)
    @Getter
    @Setter
    private String primaryKey;

    private static final String SECONDARY_KEY_SERIALIZED_NAME = "secondaryKey";
    @SerializedName(SECONDARY_KEY_SERIALIZED_NAME)
    @Getter
    @Setter
    private String secondaryKey;

    /**
     * Empty constructor: Used only to keep GSON happy.
     */
    @SuppressWarnings("unused")
    public SymmetricKeyParser()
    {}

    public SymmetricKeyParser(String primaryKey, String secondaryKey)
    {
        //Codes_SRS_SymmetricKeyParser_34_008: [This constructor shall create and return an instance of a SymmetricKeyParser object that holds the provided primary and secondary keys.]
        this.primaryKey = primaryKey;
        this.secondaryKey = secondaryKey;
    }

    public SymmetricKeyParser(String json)
    {
        if (json == null || json.isEmpty())
        {
            //Codes_SRS_SYMMETRIC_KEY_PARSER_34_011: [If the provided json null, empty, or cannot be parsed to a SymmetricKeyParser object, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The provided json cannot be null or empty");
        }

        SymmetricKeyParser parser;
        try
        {
            //Codes_SRS_SymmetricKeyParser_34_009: [This constructor shall create and return an instance of a SymmetricKeyParser object based off the provided json.]
            parser = gson.fromJson(json, SymmetricKeyParser.class);
        }
        catch (JsonSyntaxException e)
        {
            //Codes_SRS_SYMMETRIC_KEY_PARSER_34_011: [If the provided json null, empty, or cannot be parsed to a SymmetricKeyParser object, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The provided json could not be parsed");
        }

        if (parser.getPrimaryKey() == null
                || parser.getPrimaryKey().isEmpty()
                || parser.getSecondaryKey() == null
                || parser.getSecondaryKey().isEmpty())
        {
            //Codes_SRS_SYMMETRIC_KEY_PARSER_34_010: [If the provided json is missing the field for either PrimaryKey or SecondaryKey, or either is missing a value, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("Both the primary key and secondary key must be present and have a value in the provided json.");
        }

        this.primaryKey = parser.primaryKey;
        this.secondaryKey = parser.secondaryKey;
    }

    /**
     * Converts this into json and returns it
     * @return the json representation of this
     */
    public String toJson()
    {
        //Codes_SRS_SymmetricKeyParser_34_007: [This method shall return a json representation of this.]
        return gson.toJson(this);
    }
}