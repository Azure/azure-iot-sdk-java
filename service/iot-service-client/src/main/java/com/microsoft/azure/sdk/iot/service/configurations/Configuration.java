/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.configurations;

import com.microsoft.azure.sdk.iot.service.serializers.ConfigurationContentParser;
import com.microsoft.azure.sdk.iot.service.serializers.ConfigurationMetricsParser;
import com.microsoft.azure.sdk.iot.service.serializers.ParserUtility;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

public class Configuration
{
    private static final String UTC_TIME_DEFAULT = "0001-01-01T00:00:00";

    /**
     * Create a Configuration instance using the given configuration name
     *
     * @param configurationId Name of the configuration (used as configuration id)
     * @throws IllegalArgumentException This exception is thrown if {@code deviceId} or {@code moduleId} is {@code null} or empty.
     */
    public Configuration(String configurationId)
    {
        this();

        if (configurationId == null || configurationId.isEmpty())
        {
            throw new IllegalArgumentException("configuration id cannot be null or empty");
        }

        this.id = configurationId;
    }

    /**
     * Create a Configuration instance using the given configuration name
     */
    private Configuration()
    {
        this.schemaVersion = "1.0";
        this.metrics = new ConfigurationMetrics();

        this.etag = "";
        this.lastUpdatedTimeUtc = UTC_TIME_DEFAULT;
        this.createdTimeUtc = UTC_TIME_DEFAULT;
    }

    /**
     * Configuration name
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     */
    @Getter
    private String id;

    /**
     * Specifies the schemaVersion
     */
    @Getter
    private final String schemaVersion;

    /**
     * Specifies the labels map of the configuration
     */
    @Getter
    @Setter
    private HashMap<String, String> labels = null;

    /**
     * Specifies the configuration content
     */
    @Getter
    @Setter
    private ConfigurationContent content;

    /**
     * Specifies the targetCondition
     */
    @Getter
    @Setter
    private String targetCondition;

    /**
     * Datetime of configuration created time.
     */
    @Getter
    private String createdTimeUtc;

    /**
     * Datetime of configuration last updated time.
     */
    @Getter
    private String lastUpdatedTimeUtc;

    /**
     * Specifies the priority
     */
    @Getter
    @Setter
    private Integer priority;

    /**
     * Specifies the system configuration metrics
     */
    @Getter
    private ConfigurationMetrics systemMetrics = null;

    /**
     * Specifies the custom configuration metrics
     */
    @Getter
    @Setter
    private ConfigurationMetrics metrics = null;

    /**
     * A string representing a ETAG
     */
    @Getter
    @Setter
    private String etag;

    /**
     * Converts this into a ConfigurationParser object. To serialize a Configuration object, it must first be converted
     * to a ConfigurationParser object.
     *
     * @return the ConfigurationParser object that can be serialized.
     */
    ConfigurationParser toConfigurationParser()
    {
        ConfigurationParser configurationParser = new ConfigurationParser();

        configurationParser.setId(this.id);
        configurationParser.setSchemaVersion(this.schemaVersion);
        configurationParser.setLabels(this.labels);
        configurationParser.setTargetCondition(this.targetCondition);
        configurationParser.setCreatedTimeUtc(ParserUtility.getDateTimeUtc(this.createdTimeUtc));
        configurationParser.setLastUpdatedTimeUtc(ParserUtility.getDateTimeUtc(this.lastUpdatedTimeUtc));
        configurationParser.setPriority(this.priority);
        configurationParser.setETag(this.etag);

        if (this.content != null)
        {
            ConfigurationContentParser parser = new ConfigurationContentParser();
            parser.setDeviceContent(this.content.getDeviceContent());
            parser.setModulesContent(this.content.getModulesContent());
            parser.setModuleContent(this.content.getModuleContent());
            configurationParser.setContent(parser);
        }

        if (this.systemMetrics != null)
        {
            ConfigurationMetricsParser parser = new ConfigurationMetricsParser();
            parser.setQueries(this.systemMetrics.getQueries());
            parser.setResults(this.systemMetrics.getResults());
            configurationParser.setSystemMetrics(parser);
        }

        if (this.metrics != null)
        {
            ConfigurationMetricsParser parser = new ConfigurationMetricsParser();
            parser.setQueries(this.metrics.getQueries());
            parser.setResults(this.metrics.getResults());
            configurationParser.setMetrics(parser);
        }

        return  configurationParser;
    }

    /**
     * Retrieves information from the provided parser and saves it to this. All information on this will be overwritten.
     * @param parser the parser to read from
     * @throws IllegalArgumentException if the provided parser is missing the Id field. It also shall
     */
    Configuration(ConfigurationParser parser) throws IllegalArgumentException
    {
        if (parser.getId() == null)
        {
            throw new IllegalArgumentException("configurationParser must have a configurationId assigned");
        }

        this.id = parser.getId();
        this.schemaVersion = parser.getSchemaVersion();
        this.labels = parser.getLabels();
        this.targetCondition = parser.getTargetCondition();
        this.priority = parser.getPriority();
        this.etag = parser.getETag();

        if (parser.getContent() != null)
        {
            this.content = new ConfigurationContent();
            this.content.deviceContent = parser.getContent().getDeviceContent();
            this.content.modulesContent = parser.getContent().getModulesContent();
        }

        if (parser.getCreatedTimeUtc() != null)
        {
            this.createdTimeUtc = ParserUtility.getUTCDateStringFromDate(parser.getCreatedTimeUtc());
        }

        if (parser.getLastUpdatedTimeUtc() != null)
        {
            this.lastUpdatedTimeUtc = ParserUtility.getUTCDateStringFromDate(parser.getLastUpdatedTimeUtc());
        }

        if (parser.getSystemMetrics() != null)
        {
            this.systemMetrics = new ConfigurationMetrics();
            this.systemMetrics.queries = parser.getSystemMetrics().getQueries();
            this.systemMetrics.results = parser.getSystemMetrics().getResults();
        }

        if (parser.getMetrics() != null)
        {
            this.metrics = new ConfigurationMetrics();
            this.metrics.queries = parser.getMetrics().getQueries();
            this.metrics.results = parser.getMetrics().getResults();
        }
    }
}
