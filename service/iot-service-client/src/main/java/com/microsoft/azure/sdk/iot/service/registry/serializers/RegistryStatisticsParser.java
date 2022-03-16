// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.registry.serializers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public class RegistryStatisticsParser
{
    private final transient static Gson gson = new Gson();

    private static final String TOTAL_DEVICE_COUNT_NAME = "totalDeviceCount";
    @Expose
    @SerializedName(TOTAL_DEVICE_COUNT_NAME)
    @Getter
    @Setter
    private long totalDeviceCount;

    private static final String ENABLED_DEVICE_COUNT_NAME = "enableDeviceCount";
    @Expose
    @SerializedName(ENABLED_DEVICE_COUNT_NAME)
    @Getter
    @Setter
    private long enabledDeviceCount;

    private static final String DISABLED_DEVICE_COUNT_NAME = "disabledDeviceCount";
    @Expose
    @SerializedName(DISABLED_DEVICE_COUNT_NAME)
    @Getter
    @Setter
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
}
