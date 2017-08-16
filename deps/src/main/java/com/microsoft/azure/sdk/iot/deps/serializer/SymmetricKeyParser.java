/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * Store primary and secondary keys
 * Provide function for key length validation
 */
public class SymmetricKeyParser
{
    private transient Gson gson = new Gson();

    private static final String PRIMARY_KEY_SERIALIZED_NAME = "primaryKey";
    @SerializedName(PRIMARY_KEY_SERIALIZED_NAME)
    private String primaryKey;

    private static final String SECONDARY_KEY_SERIALIZED_NAME = "secondaryKey";
    @SerializedName(SECONDARY_KEY_SERIALIZED_NAME)
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

    /**
     * Getter for PrimaryKey
     *
     * @return The value of PrimaryKey
     */
    public String getPrimaryKey()
    {
        //Codes_SRS_SymmetricKeyParser_34_001: [This method shall return the value of primaryKey]
        return primaryKey;
    }

    /**
     * Setter for PrimaryKey
     * @param primaryKey the value to set the primary key to
     * @throws IllegalArgumentException if primaryKey is null
     */
    public void setPrimaryKey(String primaryKey) throws IllegalArgumentException
    {
        //Codes_SRS_SymmetricKeyParser_34_002: [If the provided primaryKey value is null, an IllegalArgumentException shall be thrown.]
        if (primaryKey == null)
        {
            throw new IllegalArgumentException("primaryKey cannot be null.");
        }

        //Codes_SRS_SymmetricKeyParser_34_003: [This method shall set the value of primaryKey to the provided value.]
        this.primaryKey = primaryKey;
    }

    /**
     * Getter for SecondaryKey
     *
     * @return The value of SecondaryKey
     */
    public String getSecondaryKey()
    {
        //Codes_SRS_SymmetricKeyParser_34_004: [This method shall return the value of secondaryKey]
        return secondaryKey;
    }

    /**
     * Setter for SecondaryKey
     * @param secondaryKey the value to set the secondary key to
     * @throws IllegalArgumentException if secondaryKey is null
     */
    public void setSecondaryKey(String secondaryKey) throws IllegalArgumentException
    {
        //Codes_SRS_SymmetricKeyParser_34_005: [If the provided secondaryKey value is null, an IllegalArgumentException shall be thrown.]
        if (secondaryKey == null)
        {
            throw new IllegalArgumentException("secondaryKey cannot be null.");
        }

        //Codes_SRS_SymmetricKeyParser_34_006: [This method shall set the value of secondaryKey to the provided value.]
        this.secondaryKey = secondaryKey;
    }
}