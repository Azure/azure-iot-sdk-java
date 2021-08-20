package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public class ConfigurationMetricsParser
{
    private static final String RESULTS_NAME = "results";
    @Expose
    @SerializedName(RESULTS_NAME)
    @Getter
    @Setter
    private Map<String, Long> results;

    private static final String QUERIES_NAME = "queries";
    @Expose
    @SerializedName(QUERIES_NAME)
    @Getter
    @Setter
    private Map<String, String> queries;

    private final transient static Gson gson = new Gson();

    /**
     * Empty constructor: Used only to keep GSON happy.
     */
    public ConfigurationMetricsParser()
    {
    }

    /**
     * Constructor for an ConfigurationMetricsParser that is built using the provided json
     * @param json the json string to build the ConfigurationMetricsParser out of
     */
    public ConfigurationMetricsParser(String json)
    {
        //Codes_SRS_CONFIGURATION_METRICS_PARSER_28_001: [If the provided json is null, empty, an IllegalArgumentException shall be thrown.]
        if (json == null || json.isEmpty())
        {
            throw new IllegalArgumentException("The provided json cannot be null or empty");
        }

        ConfigurationMetricsParser configurationMetricsParser;
        try
        {
            //Codes_SRS_CONFIGURATION_METRICS_PARSER_28_002: [The constructor shall take the provided json and convert
            // it into a new ConfigurationMetricsParser and return it.]
            configurationMetricsParser = gson.fromJson(json, ConfigurationMetricsParser.class);
        }
        catch (JsonSyntaxException e)
        {
            //Codes_SRS_CONFIGURATION_METRICS_PARSER_28_003: [If the provided json cannot be parsed into a ConfigurationMetricsParser
            // object, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The provided json could not be parsed");
        }

        this.results = configurationMetricsParser.results;
        this.queries = configurationMetricsParser.queries;
    }

    /**
     * Converts this into json and returns it
     * @return the json representation of this
     */
    public String toJson()
    {
        //Codes_SRS_CONFIGURATION_METRICS_PARSER_28_004: [This method shall return a json representation of this.]
        return gson.toJson(this);
    }
}
