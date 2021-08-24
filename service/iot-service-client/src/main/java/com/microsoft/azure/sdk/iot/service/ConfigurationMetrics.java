package com.microsoft.azure.sdk.iot.service;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationMetrics
{
    @Getter
    @Setter
    Map<String, Long> results;

    @Getter
    @Setter
    Map<String, String> queries;

    /**
     * Create a ConfigurationMetrics instance
     */
    public ConfigurationMetrics()
    {
        this.results = new HashMap<>();
        this.queries = new HashMap<>();
    }
}
