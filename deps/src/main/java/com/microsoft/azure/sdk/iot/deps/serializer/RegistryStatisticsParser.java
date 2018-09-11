// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegistryStatisticsParser
{
    private transient static Gson gson = new Gson();

    private static final String TOTAL_DEVICE_COUNT_NAME = "totalDeviceCount";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(TOTAL_DEVICE_COUNT_NAME)
    private long totalDeviceCount;

    private static final String ENABLED_DEVICE_COUNT_NAME = "enableDeviceCount";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(ENABLED_DEVICE_COUNT_NAME)
    private long enabledDeviceCount;

    private static final String DISABLED_DEVICE_COUNT_NAME = "disabledDeviceCount";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(DISABLED_DEVICE_COUNT_NAME)
    private long disabledDeviceCount;

    /**
     * Empty constructor
     */
    public RegistryStatisticsParser()
    {
    }

    /**
     * Constructor for a RegistryStatisticsParser that is constructed from Json.
     * @param json the json to build from.
     */
    public RegistryStatisticsParser(String json)
    {
        if (json == null || json.isEmpty())
        {
            //Codes_SRS_REGISTRY_STATISTICS_PROPERTIES_PARSER_34_003: [If the provided json is null, empty, or cannot be parsed into a RegistryStatisticsParser object, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The provided json cannot be null or empty");
        }

        RegistryStatisticsParser parser;
        try
        {
            //Codes_SRS_REGISTRY_STATISTICS_PROPERTIES_PARSER_34_002: [This constructor shall create and return an instance of a RegistryStatisticsParser object based off the provided json.]
            parser = gson.fromJson(json, RegistryStatisticsParser.class);
        }
        catch (JsonSyntaxException e)
        {
            //Codes_SRS_REGISTRY_STATISTICS_PROPERTIES_PARSER_34_003: [If the provided json is null, empty, or cannot be parsed into a RegistryStatisticsParser object, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The provided json could not be parsed");
        }

        this.totalDeviceCount = parser.totalDeviceCount;
        this.enabledDeviceCount = parser.enabledDeviceCount;
        this.disabledDeviceCount = parser.disabledDeviceCount;
    }

    /**
     * Converts this into a json string.
     * @return the json representation of this.
     */
    public String toJson()
    {
        //Codes_SRS_REGISTRY_STATISTICS_PROPERTIES_PARSER_34_001: [This method shall return a json representation of this.]
        return gson.toJson(this);
    }

    /**
     * Getter for TotalDeviceCount
     *
     * @return The value of TotalDeviceCount
     */
    public long getTotalDeviceCount()
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_005: [This method shall return the value of this object's totalDeviceCount.]
        return totalDeviceCount;
    }

    /**
     * Setter for TotalDeviceCount
     *
     * @param totalDeviceCount the value to set TotalDeviceCount to
     */
    public void setTotalDeviceCount(long totalDeviceCount)
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_004: [This method shall set the value of this object's totalDeviceCount equal to the provided value.]
        this.totalDeviceCount = totalDeviceCount;
    }

    /**
     * Getter for EnabledDeviceCount
     *
     * @return The value of EnabledDeviceCount
     */
    public long getEnabledDeviceCount()
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_007: [This method shall return the value of this object's enabledDeviceCount.]
        return enabledDeviceCount;
    }

    /**
     * Setter for EnabledDeviceCount
     *
     * @param enabledDeviceCount the value to set EnabledDeviceCount to
     */
    public void setEnabledDeviceCount(long enabledDeviceCount)
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_006: [This method shall set the value of this object's enabledDeviceCount equal to the provided value.]
        this.enabledDeviceCount = enabledDeviceCount;
    }

    /**
     * Getter for DisabledDeviceCount
     *
     * @return The value of DisabledDeviceCount
     */
    public long getDisabledDeviceCount()
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_009: [This method shall return the value of this object's disabledDeviceCount.]
        return disabledDeviceCount;
    }

    /**
     * Setter for DisabledDeviceCount
     *
     * @param disabledDeviceCount the value to set DisabledDeviceCount to
     */
    public void setDisabledDeviceCount(long disabledDeviceCount)
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_008: [This method shall set the value of this object's disabledDeviceCount equal to the provided value.]
        this.disabledDeviceCount = disabledDeviceCount;
    }
}
