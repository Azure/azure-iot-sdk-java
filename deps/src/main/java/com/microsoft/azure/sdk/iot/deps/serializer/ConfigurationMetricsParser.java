package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class ConfigurationMetricsParser
{
    private static final String RESULTS_NAME = "results";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(RESULTS_NAME)
    private Map<String, Long> results;

    private static final String QUERIES_NAME = "queries";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(QUERIES_NAME)
    private Map<String, String> queries;

    private transient static Gson gson = new Gson();

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

    /**
     * Getter for results
     *
     * @return The value of results
     */
    public Map<String, Long> getResults()
    {
        //Codes_SRS_CONFIGURATION_METRICS_PARSER_28_005: [This method shall return the value of this object's results.]
        return results;
    }

    /**
     * Setter for results
     * @param results the value to set results to
     */
    public void setResults(Map<String, Long> results)
    {
        //Codes_SRS_CONFIGURATION_METRICS_PARSER_28_006: [This method shall set the value of results to the provided value.]
        this.results = results;
    }

    /**
     * Getter for queries
     *
     * @return The value of queries
     */
    public Map<String, String> getQueries()
    {
        //Codes_SRS_CONFIGURATION_METRICS_PARSER_28_007: [This method shall return the value of this object's queries.]
        return queries;
    }

    /**
     * Setter for queries
     * @param queries the value to set queries to
     */
    public void setQueries(Map<String, String> queries)
    {
        //Codes_SRS_CONFIGURATION_METRICS_PARSER_28_008: [This method shall set the value of queries to the provided value.]
        this.queries = queries;
    }
}
