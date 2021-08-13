/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.deps.serializer.ConfigurationContentParser;
import com.microsoft.azure.sdk.iot.deps.serializer.ConfigurationMetricsParser;
import com.microsoft.azure.sdk.iot.deps.serializer.ConfigurationParser;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;

import java.util.HashMap;

public class Configuration
{
    private final String UTC_TIME_DEFAULT = "0001-01-01T00:00:00";

    /**
     * Create a Configuration instance using the given configuration name
     *
     * @param configurationId Name of the configuration (used as configuration id)
     * @throws IllegalArgumentException This exception is thrown if {@code deviceId} or {@code moduleId} is {@code null} or empty.
     */
    public Configuration(String configurationId)
    {
        this();

        // Codes_SRS_SERVICE_SDK_JAVA_CONFIGURATION_28_001: [The function shall throw IllegalArgumentException if the input string is empty or null]
        if (Tools.isNullOrEmpty(configurationId))
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
        // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_002: [The constructor shall initialize all properties to default values]
        this.schemaVersion = "1.0";
        this.metrics = new ConfigurationMetrics();

        this.etag = "";
        this.lastUpdatedTimeUtc = UTC_TIME_DEFAULT;
        this.createdTimeUtc = UTC_TIME_DEFAULT;
    }

    // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_003: [The Configuration class shall have the following properties: id, schemaVersion,
    // labels, content, targetCondition, createdTimeUtc, lastUpdatedTimeUtc, priority, systemMetrics, metrics, etag

    /**
     * Configuration name
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     */
    private String id;

    /**
     * Getter for configuration name
     *
     * @return The configuration string
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * Specifies the schemaVersion
     */
    private String schemaVersion;

    /**
     * Getter for schema version
     *
     * @return The schema version
     */
    public String getSchemaVersion()
    {
        return this.schemaVersion;
    }

    /**
     * Specifies the labels map of the configuration
     */
    private HashMap<String, String> labels = null;

    /**
     * Setter for the labels of the configuration
     *
     * @param labels labels to be set
     */
    public void setLabels(HashMap<String, String> labels)
    {
        this.labels = labels;
    }

    /**
     * Getter for labels
     *
     * @return The labels map
     */
    public HashMap<String, String> getLabels()
    {
        return labels;
    }

    /**
     * Set the configuration metrics of this object
     * @param metrics the metrics to set
     */
    public void setMetrics(ConfigurationMetrics metrics)
    {
        this.metrics = metrics;
    }

    /**
     * Specifies the configuration content
     */
    private ConfigurationContent content;

    /**
     * Setter for the configuration content
     *
     * @param content configuration content to be set
     */
    public void setContent(ConfigurationContent content)
    {
        this.content = content;
    }

    /**
     * Getter for configuration content
     *
     * @return The configuration content object
     */
    public ConfigurationContent getContent()
    {
        return this.content;
    }

    /**
     * Specifies the targetCondition
     */
    private String targetCondition;

    /**
     * Setter for the targetCondition
     *
     * @param targetCondition targetCondition to be set
     */
    public void setTargetCondition(String targetCondition)
    {
        this.targetCondition = targetCondition;
    }

    /**
     * Getter for targetCondition
     *
     * @return The targetCondition string
     */
    public String getTargetCondition()
    {
        return this.targetCondition;
    }

    /**
     * Datetime of configuration created time.
     */
    private String createdTimeUtc;

    /**
     * Getter for configuration created time
     *
     * @return The string containing the time when the configuration was created
     */
    public String getCreatedTimeUtc()
    {
        return createdTimeUtc;
    }

    /**
     * Datetime of configuration last updated time.
     */
    private String lastUpdatedTimeUtc;

    /**
     * Getter for configuration last updated time string
     *
     * @return The string containing the time when the configuration was last updated
     */
    public String getLastUpdatedTimeUtc()
    {
        return lastUpdatedTimeUtc;
    }

    /**
     * Specifies the priority
     */
    private Integer priority;

    /**
     * Setter for the configuration priority
     *
     * @param priority to be set
     */
    public void setPriority(Integer priority)
    {
        this.priority = priority;
    }

    /**
     * Getter for the configuration priority
     *
     * @return The Integer containing the priority
     */
    public Integer getPriority()
    {
        return priority;
    }

    /**
     * Specifies the system configuration metrics
     */
    private ConfigurationMetrics systemMetrics = null;

    /**
     * Getter for the system configuration metrics
     *
     * @return The system configuration metrics object
     */
    public ConfigurationMetrics getSystemMetrics()
    {
        return systemMetrics;
    }

    /**
     * Specifies the custom configuration metrics
     */
    private ConfigurationMetrics metrics = null;

    /**
     * Getter for the custom configuration metrics
     *
     * @return The custom configuration metrics object
     */
    public ConfigurationMetrics getMetrics()
    {
        return metrics;
    }

    /**
     * A string representing a ETAG
     */
    private String etag;

    /**
     * Setter for the ETAG
     *
     * @param etag to be set
     */
    public void setEtag(String etag)
    {
        this.etag = etag;
    }

    /**
     * Getter for eTag
     *
     * @return The eTag string
     */
    public String getEtag()
    {
        return etag;
    }

    /**
     * Converts this into a ConfigurationParser object. To serialize a Configuration object, it must first be converted
     * to a ConfigurationParser object.
     *
     * @return the ConfigurationParser object that can be serialized.
     */
    ConfigurationParser toConfigurationParser()
    {
        //Codes_SRS_SERVICE_SDK_JAVA_CONFIGURATION_28_004: [This method shall return a new instance of a ConfigurationParser
        //object that is populated using the properties of this.]
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
            //Codes_SRS_SERVICE_SDK_JAVA_CONFIGURATION_28_005: [If the provided parser is missing the id,
            //an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("configurationParser must have a configurationId assigned");
        }

        //Codes_SRS_SERVICE_SDK_JAVA_CONFIGURATION_28_005: [This constructor shall create a new Configuration object using the values within the provided parser.]
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
            this.createdTimeUtc = ParserUtility.getDateStringFromDate(parser.getCreatedTimeUtc());
        }

        if (parser.getLastUpdatedTimeUtc() != null)
        {
            this.lastUpdatedTimeUtc = ParserUtility.getDateStringFromDate(parser.getLastUpdatedTimeUtc());
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
