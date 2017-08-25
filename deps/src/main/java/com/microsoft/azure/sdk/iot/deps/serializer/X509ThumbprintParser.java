// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

public class X509ThumbprintParser
{
    private static final String PRIMARY_THUMBPRINT_SERIALIZED_NAME = "primaryThumbprint";
    @SerializedName(PRIMARY_THUMBPRINT_SERIALIZED_NAME)
    private String primaryThumbprint;

    private static final String SECONDARY_THUMBPRINT_SERIALIZED_NAME = "secondaryThumbprint";
    @SerializedName(SECONDARY_THUMBPRINT_SERIALIZED_NAME)
    private String secondaryThumbprint;

    private transient Gson gson = new Gson();

    /**
     * Empty constructor: Used only to keep GSON happy.
     */
    @SuppressWarnings("unused")
    public X509ThumbprintParser()
    {}

    /**
     * Construct an X509ThumbprintParser object with the provided thumbprints
     * @param primaryThumbprint the primary thumbprint to set
     * @param secondaryThumbprint the secondary thumbprint to set
     */
    public X509ThumbprintParser(String primaryThumbprint, String secondaryThumbprint)
    {
        //Codes_SRS_X509ThumbprintParser_34_008: [The parser shall create and return an instance of a X509ThumbprintParser object that holds the provided primary and secondary thumbprints.]
        this.primaryThumbprint = primaryThumbprint;
        this.secondaryThumbprint = secondaryThumbprint;
    }

    /**
     * Construct an X509ThumbprintParser object from json
     * @param json the json to build with
     */
    public X509ThumbprintParser(String json) throws IllegalArgumentException
    {
        //Codes_SRS_X509ThumbprintParser_34_010: [If the provided json is null or empty or cannot be parsed into an X509Thumbprint object, an IllegalArgumentException shall be thrown.]
        if (json == null || json.isEmpty())
        {
            throw new IllegalArgumentException("The provided Json must not be null or empty");
        }

        X509ThumbprintParser parser = null;
        try
        {
            //Codes_SRS_X509ThumbprintParser_34_009: [The parser shall create and return an instance of a X509ThumbprintParser object based off the provided json.]
            parser = gson.fromJson(json, X509ThumbprintParser.class);
        }
        catch (JsonSyntaxException e)
        {
            //Codes_SRS_X509ThumbprintParser_34_010: [If the provided json is null or empty or cannot be parsed into an X509Thumbprint object, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The provided json could not be parsed");
        }

        this.primaryThumbprint = parser.getPrimaryThumbprint();
        this.secondaryThumbprint = parser.getSecondaryThumbprint();
    }

    /**
     * Converts this into json format
     * @return the json representation of this
     */
    public String toJson()
    {
        //Codes_SRS_X509ThumbprintParser_34_007: [This method shall return a json representation of this.]
        return gson.toJson(this);
    }

    /**
     * Getter for PrimaryThumbprint
     *
     * @return The value of PrimaryThumbprint
     */
    public String getPrimaryThumbprint()
    {
        //Codes_SRS_X509ThumbprintParser_34_001: [This method shall return the value of primaryThumbprint.]
        return primaryThumbprint;
    }

    /**
     * Setter for PrimaryThumbprint
     * @param primaryThumbprint the value to set the primary thumbprint to
     * @throws IllegalArgumentException if primaryThumbprint is null
     */
    public void setPrimaryThumbprint(String primaryThumbprint) throws IllegalArgumentException
    {

        //Codes_SRS_X509ThumbprintParser_34_002: [If the provided primaryThumbprint value is null, an IllegalArgumentException shall be thrown.]
        if (primaryThumbprint == null)
        {
            throw new IllegalArgumentException("Primary thumbprint cannot be null or empty");
        }

        //Codes_SRS_X509ThumbprintParser_34_003: [This method shall set the value of primaryThumbprint to the provided value.]
        this.primaryThumbprint = primaryThumbprint;
    }

    /**
     * Getter for SecondaryThumbprint
     *
     * @return The value of SecondaryThumbprint
     */
    public String getSecondaryThumbprint()
    {
        //Codes_SRS_X509ThumbprintParser_34_004: [This method shall return the value of secondaryThumbprint.]
        return secondaryThumbprint;
    }

    /**
     * Setter for SecondaryThumbprint
     * @param secondaryThumbprint the value to set the secondary thumbprint to
     * @throws IllegalArgumentException if secondaryThumbprint is null
     */
    public void setSecondaryThumbprint(String secondaryThumbprint) throws IllegalArgumentException
    {
        //Codes_SRS_X509ThumbprintParser_34_005: [If the provided secondaryThumbprint value is null, an IllegalArgumentException shall be thrown.]
        if (secondaryThumbprint == null)
        {
            throw new IllegalArgumentException("Secondary thumbprint cannot be null or empty");
        }

        //Codes_SRS_X509ThumbprintParser_34_006: [This method shall set the value of secondaryThumbprint to the provided value.]
        this.secondaryThumbprint = secondaryThumbprint;
    }
}
