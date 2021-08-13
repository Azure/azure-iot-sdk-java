package com.microsoft.azure.sdk.iot.service;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationMetrics
{
    Map<String, Long> results;
    Map<String, String> queries;

    /**
     * Create a ConfigurationMetrics instance
     */
    public ConfigurationMetrics()
    {
        // Codes_SRS_SERVICE_SDK_JAVA_CONFIGURATION_METRICS_28_001: [The constructor shall initialize results and queries fields.]
        this.results = new HashMap<>();
        this.queries = new HashMap<>();
    }

    // Codes_SRS_SERVICE_SDK_JAVA_CONFIGURATION_METRICS_28_002: [The ConfigurationMetrics class shall have the following properties: results and queries
    public void setResults(Map<String, Long> results)
    {
        this.results = results;
    }

    public Map<String, Long> getResults()
    {
        return results;
    }

    public void setQueries(Map<String, String> queries)
    {
        this.queries = queries;
    }

    public Map<String, String> getQueries()
    {
        return queries;
    }
}
