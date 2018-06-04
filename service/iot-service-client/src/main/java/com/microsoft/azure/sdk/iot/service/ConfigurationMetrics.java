package com.microsoft.azure.sdk.iot.service;

import java.util.HashMap;

public class ConfigurationMetrics
{
    protected HashMap<String, Long> results;
    protected HashMap<String, String> queries;

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
    public void setResults(HashMap<String, Long> results)
    {
        this.results = results;
    }

    public HashMap<String, Long> getResults()
    {
        return results;
    }

    public void setQueries(HashMap<String, String> queries)
    {
        this.queries = queries;
    }

    public HashMap<String, String> getQueries()
    {
        return queries;
    }
}
