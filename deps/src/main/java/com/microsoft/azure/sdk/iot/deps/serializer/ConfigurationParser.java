package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.HashMap;

public class ConfigurationParser
{
    private static final String CONFIGURATION_ID_NAME = "id";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(CONFIGURATION_ID_NAME)
    private String id;

    private static final String SCHEMA_VERSION_NAME = "schemaVersion";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(SCHEMA_VERSION_NAME)
    private String schemaVersion;

    private static final String LABELS_NAME = "labels";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(LABELS_NAME)
    private HashMap<String, String> labels;

    private static final String CONTENT_NAME = "content";
    @Expose(serialize = false, deserialize = true)
    @SerializedName(CONTENT_NAME)
    private ConfigurationContentParser content;

    private static final String CONTENT_TYPE_NAME = "contentType";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(CONTENT_TYPE_NAME)
    private String contentType;

    private static final String TARGET_CONDITION_NAME = "targetCondition";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(TARGET_CONDITION_NAME)
    private String targetCondition;

    private static final String CREATED_TIME_UTC_NAME = "createdTimeUtc";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(CREATED_TIME_UTC_NAME)
    private String createdTimeUtcString;
    private transient Date createdTimeUtc;

    private static final String LAST_UPDATED_TIME_UTC_NAME = "lastUpdatedTimeUtc";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(LAST_UPDATED_TIME_UTC_NAME)
    private String lastUpdatedTimeUtcString;
    private transient Date lastUpdatedTimeUtc;

    private static final String PRIORITY_NAME = "priority";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(PRIORITY_NAME)
    private Integer priority;

    private static final String SYSTEM_METRICS_NAME = "systemMetrics";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(SYSTEM_METRICS_NAME)
    private ConfigurationMetricsParser systemMetrics;

    private static final String METRICS_NAME = "metrics";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(METRICS_NAME)
    private ConfigurationMetricsParser metrics;

    private static final String E_TAG_NAME = "etag";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(E_TAG_NAME)
    private String eTag;

    private transient static Gson gson = new GsonBuilder().enableComplexMapKeySerialization().serializeNulls().create();

    /**
     * Empty constructor: Used only to keep GSON happy.
     */
    public ConfigurationParser()
    {
    }

    /**
     * Constructor for an ExportImportDeviceParser that is built using the provided json
     *
     * @param json the json string to build the ExportImportDeviceParser out of
     */
    public ConfigurationParser(String json)
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_001: [If the provided json is null, empty, or cannot be parsed into an
        // ConfigurationParser object, an IllegalArgumentException shall be thrown.]
        if (json == null || json.isEmpty())
        {
            throw new IllegalArgumentException("The provided json cannot be null or empty");
        }

        ConfigurationParser configurationParser;
        try
        {
            //Codes_SRS_CONFIGURATION_PARSER_28_002: [This constructor shall take the provided json and convert it into
            // a new ConfigurationParser and return it.]
            configurationParser = gson.fromJson(json, ConfigurationParser.class);
        }
        catch (JsonSyntaxException e)
        {
            //Codes_SRS_CONFIGURATION_PARSER_28_005: [If the provided json cannot be parsed into a ConfigurationParser object, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The provided json could not be parsed");
        }

        //Codes_SRS_CONFIGURATION_PARSER_28_003: [If the provided json is missing the id field or its value is empty, an IllegalArgumentException shall be thrown.]
        if (configurationParser.id == null || configurationParser.id.isEmpty())
        {
            throw new IllegalArgumentException("The provided json must contain the field for configurationId and its value may not be empty");
        }

        //Codes_SRS_CONFIGURATION_PARSER_28_004: [If the provided json is missing the schemaVersion field or its value is empty, an IllegalArgumentException shall be thrown.]
        if (configurationParser.schemaVersion == null || configurationParser.schemaVersion.isEmpty())
        {
            throw new IllegalArgumentException("The provided json must contain the field for schemaVersion and its value may not be empty");
        }

        this.id = configurationParser.id;
        this.schemaVersion = configurationParser.schemaVersion;
        this.labels = configurationParser.labels;
        this.content = configurationParser.content;
        this.contentType = configurationParser.contentType;
        this.targetCondition = configurationParser.targetCondition;
        this.priority = configurationParser.priority;
        this.systemMetrics = configurationParser.systemMetrics;
        this.metrics = configurationParser.metrics;
        this.eTag = configurationParser.eTag;

        //convert to date format
        if (configurationParser.createdTimeUtcString != null)
        {
            this.createdTimeUtcString = configurationParser.createdTimeUtcString;
            this.createdTimeUtc = ParserUtility.getDateTimeUtc(configurationParser.createdTimeUtcString);
        }

        if (configurationParser.lastUpdatedTimeUtcString != null)
        {
            this.lastUpdatedTimeUtcString = configurationParser.lastUpdatedTimeUtcString;
            this.lastUpdatedTimeUtc = ParserUtility.getDateTimeUtc(configurationParser.lastUpdatedTimeUtcString);
        }
    }

    /**
     * Converts this into json and returns it
     *
     * @return the json representation of this
     */
    public String toJson()
    {
        if (this.createdTimeUtc != null)
        {
            this.createdTimeUtcString = ParserUtility.getDateStringFromDate(this.createdTimeUtc);
        }

        if (this.lastUpdatedTimeUtc != null)
        {
            this.lastUpdatedTimeUtcString = ParserUtility.getDateStringFromDate(this.lastUpdatedTimeUtc);
        }

        //Codes_SRS_CONFIGURATION_PARSER_28_006: [This method shall return a json representation of this.]
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        JsonObject jsonObject = gson.toJsonTree(this).getAsJsonObject();

        /* SRS_TWIN_STATE_21_009: [If the tags is null, the JSON shall not include the `tags`.] */
        if (this.content != null)
        {
            jsonObject.add(CONTENT_NAME, this.content.toJsonElement());
        }

        return jsonObject.toString();
    }

    /**
     * Getter for id
     *
     * @return The value of Id
     */
    public String getId()
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_007: [This method shall return the value of this object's id.]
        return id;
    }

    /**
     * Setter for id
     *
     * @param id the value to set id to
     * @throws IllegalArgumentException if id is null
     */
    public void setId(String id) throws IllegalArgumentException
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_008: [If the provided id value is null, an IllegalArgumentException shall be thrown.]
        if (id == null || id.isEmpty())
        {
            throw new IllegalArgumentException("Configuration Id cannot not be null");
        }

        //Codes_SRS_CONFIGURATION_PARSER_28_009: [This method shall set the value of id to the provided value.]
        this.id = id;
    }

    /**
     * Getter for schemaVersion
     *
     * @return The value of schemaVersion
     */
    public String getSchemaVersion()
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_010: [This method shall return the value of this object's schemaVersion.]
        return schemaVersion;
    }

    /**
     * Setter for schemaVersion
     *
     * @param schemaVersion the value to set schemaVersion to
     * @throws IllegalArgumentException if schemaVersion is null
     */
    public void setSchemaVersion(String schemaVersion) throws IllegalArgumentException
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_011: [If the provided schemaVersion value is null, an IllegalArgumentException shall be thrown.]
        if (schemaVersion == null || schemaVersion.isEmpty())
        {
            throw new IllegalArgumentException("SchemaVersion cannot not be null");
        }

        //Codes_SRS_CONFIGURATION_PARSER_28_012: [This method shall set the value of schemaVersion to the provided value.]
        this.schemaVersion = schemaVersion;
    }

    /**
     * Getter for labels
     *
     * @return The labels map
     */
    public HashMap<String, String> getLabels()
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_013: [This method shall return the value of this object's labels.]
        return labels;
    }

    /**
     * Setter for labels
     *
     * @param labels the value to set labels to
     */
    public void setLabels(HashMap<String, String> labels) throws IllegalArgumentException
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_014: [This method shall set the value of labels to the provided value.]
        this.labels = labels;
    }

    /**
     * Getter for contentParser
     *
     * @return The value of contentParser
     */
    public ConfigurationContentParser getContent()
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_015: [This method shall return the value of this object's contentParser.]
        return content;
    }

    /**
     * Setter for content
     *
     * @param content the value to set contentParser to
     */
    public void setContent(ConfigurationContentParser content)
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_016: [This method shall set the value of contentParser to the provided value.]
        this.content = content;
    }

    /**
     * Getter for contentType
     *
     * @return The value of contentType
     */
    public String getContentType()
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_017: [This method shall return the value of this object's contentType.]
        return contentType;
    }

    /**
     * Setter for contentType
     *
     * @param contentType the value to set contentType to
     */
    public void setContentType(String contentType)
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_018: [This method shall set the value of contentType to the provided value.]
        this.contentType = contentType;
    }

    /**
     * Getter for targetCondition
     *
     * @return The value of targetCondition
     */
    public String getTargetCondition()
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_019: [This method shall return the value of this object's targetCondition.]
        return targetCondition;
    }

    /**
     * Setter for targetCondition
     *
     * @param targetCondition the value to set targetCondition to
     */
    public void setTargetCondition(String targetCondition)
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_020: [This method shall set the value of targetCondition to the provided value.]
        this.targetCondition = targetCondition;
    }

    /**
     * Getter for createdTimeUtc
     *
     * @return The value of createdTimeUtc
     */
    public Date getCreatedTimeUtc()
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_021: [This method shall return the value of this object's createdTimeUtc.]
        return createdTimeUtc;
    }

    /**
     * Setter for createdTimeUtc
     *
     * @param createdTimeUtc the value to set createdTimeUtc to
     */
    public void setCreatedTimeUtc(Date createdTimeUtc)
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_022: [This method shall set the value of this object's statusUpdatedTime equal to the provided value.]
        this.createdTimeUtc = createdTimeUtc;

        if (createdTimeUtc == null)
        {
            this.createdTimeUtcString = null;
        }
        else
        {
            this.createdTimeUtcString = ParserUtility.getDateStringFromDate(createdTimeUtc);
        }
    }

    /**
     * Getter for lastUpdatedTimeUtc
     *
     * @return The value of lastUpdatedTimeUtc
     */
    public Date getLastUpdatedTimeUtc()
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_023: [This method shall return the value of this object's lastUpdatedTimeUtc.]
        return lastUpdatedTimeUtc;
    }

    /**
     * Setter for lastUpdatedTimeUtc
     *
     * @param lastUpdatedTimeUtc the value to set lastUpdatedTimeUtc to
     */
    public void setLastUpdatedTimeUtc(Date lastUpdatedTimeUtc)
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_024: [This method shall set the value of this object's lastUpdatedTimeUtc equal to the provided value.]
        this.lastUpdatedTimeUtc = lastUpdatedTimeUtc;

        if (lastUpdatedTimeUtc == null)
        {
            this.lastUpdatedTimeUtcString = null;
        }
        else
        {
            this.lastUpdatedTimeUtcString = ParserUtility.getDateStringFromDate(lastUpdatedTimeUtc);
        }
    }

    /**
     * Getter for priority
     *
     * @return The value of priority
     */
    public Integer getPriority()
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_025: [This method shall return the value of this object's priority.]
        return priority;
    }

    /**
     * Setter for priority
     *
     * @param priority the value to set priority to
     */
    public void setPriority(Integer priority)
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_026: [This method shall set the value of priority to the provided value.]
        this.priority = priority;
    }

    /**
     * Getter for systemMetricsParser
     *
     * @return The value of systemMetricsParser
     */
    public ConfigurationMetricsParser getSystemMetrics()
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_031: [This method shall return the value of this object's systemMetricsParser.]
        return systemMetrics;
    }

    /**
     * Setter for systemMetricsParser
     *
     * @param systemMetrics the value to set systemMetricsParser to
     */
    public void setSystemMetrics(ConfigurationMetricsParser systemMetrics)
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_032: [This method shall set the value of systemMetricsParser to the provided value.]
        this.systemMetrics = systemMetrics;
    }

    /**
     * Getter for metricsParser
     *
     * @return The value of metrics
     */
    public ConfigurationMetricsParser getMetrics()
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_027: [This method shall return the value of this object's metrics.]
        return metrics;
    }

    /**
     * Setter for metrics
     *
     * @param metrics the value to set metrics to
     */
    public void setMetrics(ConfigurationMetricsParser metrics)
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_028: [This method shall set the value of metrics to the provided value.]
        this.metrics = metrics;
    }

    /**
     * Getter for eTag
     *
     * @return The value of eTag
     */
    public String getETag()
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_029: [This method shall return the value of this object's ETag.]
        return eTag;
    }

    /**
     * Setter for eTag
     *
     * @param eTag the value to set eTag to
     */
    public void setETag(String eTag)
    {
        //Codes_SRS_CONFIGURATION_PARSER_28_030: [This method shall set the value of this object's ETag equal to the provided value.]
        this.eTag = eTag;
    }
}