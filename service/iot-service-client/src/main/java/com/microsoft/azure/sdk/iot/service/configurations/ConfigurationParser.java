package com.microsoft.azure.sdk.iot.service.configurations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.service.serializers.ConfigurationContentParser;
import com.microsoft.azure.sdk.iot.service.serializers.ConfigurationMetricsParser;
import com.microsoft.azure.sdk.iot.service.serializers.ParserUtility;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;

public class ConfigurationParser
{
    private static final String CONFIGURATION_ID_NAME = "id";
    @Expose
    @SerializedName(CONFIGURATION_ID_NAME)
    @Getter
    @Setter
    private String id;

    private static final String SCHEMA_VERSION_NAME = "schemaVersion";
    @Expose
    @SerializedName(SCHEMA_VERSION_NAME)
    @Getter
    @Setter
    private String schemaVersion;

    private static final String LABELS_NAME = "labels";
    @Expose
    @SerializedName(LABELS_NAME)
    @Getter
    @Setter
    private HashMap<String, String> labels;

    private static final String CONTENT_NAME = "content";
    @Expose(serialize = false)
    @SerializedName(CONTENT_NAME)
    @Getter
    @Setter
    private ConfigurationContentParser content;

    private static final String CONTENT_TYPE_NAME = "contentType";
    @Expose
    @SerializedName(CONTENT_TYPE_NAME)
    @Getter
    @Setter
    private String contentType;

    private static final String TARGET_CONDITION_NAME = "targetCondition";
    @Expose
    @SerializedName(TARGET_CONDITION_NAME)
    @Getter
    @Setter
    private String targetCondition;

    private static final String CREATED_TIME_UTC_NAME = "createdTimeUtc";
    @Expose(deserialize = false)
    @SerializedName(CREATED_TIME_UTC_NAME)
    private String createdTimeUtcString;
    private transient Date createdTimeUtc;

    private static final String LAST_UPDATED_TIME_UTC_NAME = "lastUpdatedTimeUtc";
    @Expose(deserialize = false)
    @SerializedName(LAST_UPDATED_TIME_UTC_NAME)
    private String lastUpdatedTimeUtcString;
    private transient Date lastUpdatedTimeUtc;

    private static final String PRIORITY_NAME = "priority";
    @Expose
    @SerializedName(PRIORITY_NAME)
    @Getter
    @Setter
    private Integer priority;

    private static final String SYSTEM_METRICS_NAME = "systemMetrics";
    @Expose
    @SerializedName(SYSTEM_METRICS_NAME)
    @Getter
    @Setter
    private ConfigurationMetricsParser systemMetrics;

    private static final String METRICS_NAME = "metrics";
    @Expose
    @SerializedName(METRICS_NAME)
    @Getter
    @Setter
    private ConfigurationMetricsParser metrics;

    private static final String E_TAG_NAME = "etag";
    @Expose
    @SerializedName(E_TAG_NAME)
    @Getter
    @Setter
    private String eTag;

    private final transient static Gson gson = new GsonBuilder().enableComplexMapKeySerialization().serializeNulls().create();

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
            this.createdTimeUtcString = ParserUtility.getUTCDateStringFromDate(createdTimeUtc);
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
            this.lastUpdatedTimeUtcString = ParserUtility.getUTCDateStringFromDate(lastUpdatedTimeUtc);
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
            this.createdTimeUtcString = ParserUtility.getUTCDateStringFromDate(this.createdTimeUtc);
        }

        if (this.lastUpdatedTimeUtc != null)
        {
            this.lastUpdatedTimeUtcString = ParserUtility.getUTCDateStringFromDate(this.lastUpdatedTimeUtc);
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
}