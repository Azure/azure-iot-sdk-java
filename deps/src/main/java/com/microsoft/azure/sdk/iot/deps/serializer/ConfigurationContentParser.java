package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationContentParser
{
    private static final String MODULES_CONTENT_NAME = "modulesContent";
    @Expose(serialize = false, deserialize = true)
    @SerializedName(MODULES_CONTENT_NAME)
    private Map<String, Map<String, Object>> modulesContent;

    private static final String DEVICE_CONTENT_NAME = "deviceContent";
    @Expose(serialize = false, deserialize = true)
    @SerializedName(DEVICE_CONTENT_NAME)
    private Map<String, Object> deviceContent;

    private transient static Gson gson = new Gson();

    /**
     * Empty constructor: Used only to keep GSON happy.
     */
    public ConfigurationContentParser()
    {
    }

    /**
     * Constructor for an ConfigurationContentParser that is built using the provided json
     * @param json the json string to build the ConfigurationContentParser out of
     */
    public ConfigurationContentParser(String json)
    {
        //Codes_SRS_CONFIGURATION_CONTENT_PARSER_28_001: [If the provided json is null, empty an IllegalArgumentException shall be thrown.]
        if (json == null || json.isEmpty())
        {
            throw new IllegalArgumentException("The provided json cannot be null or empty");
        }

        ConfigurationContentParser configurationContentParser;
        try
        {
            //Codes_SRS_CONFIGURATION_CONTENT_PARSER_28_002: [The constructor shall take the provided json and convert
            // it into a new ConfigurationContentParser and return it.]
            configurationContentParser = gson.fromJson(json, ConfigurationContentParser.class);
        }
        catch (JsonSyntaxException e)
        {
            //Codes_SRS_CONFIGURATION_CONTENT_PARSER_28_003: [If the provided json cannot be parsed into a Configuration
            // ContentParser object, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The provided json could not be parsed");
        }

        this.modulesContent = configurationContentParser.modulesContent;
        this.deviceContent = configurationContentParser.deviceContent;
    }

    public String toJson()
    {
        return gson.toJson(this);
    }

    /**
     * Getter for modulesContent
     *
     * @return The value of modulesContent
     */
    public Map<String, Map<String, Object>> getModulesContent()
    {
        //Codes_SRS_CONFIGURATION_METRICS_PARSER_28_005: [This method shall return the value of this object's modulesContent.]
        return modulesContent;
    }

    /**
     * Setter for modulesContent
     * @param modulesContent the value to set results to
     */
    public void setModulesContent(Map<String, Map<String, Object>> modulesContent)
    {
        //Codes_SRS_CONFIGURATION_METRICS_PARSER_28_006: [This method shall set the value of results to the provided value.]
        this.modulesContent = modulesContent;
    }

    /**
     * Getter for deviceContent
     *
     * @return The value of queries
     */
    public Map<String, Object> getDeviceContent()
    {
        //Codes_SRS_CONFIGURATION_METRICS_PARSER_28_007: [This method shall return the value of this object's deviceContent.]
        return deviceContent;
    }

    /**
     * Setter for deviceContent
     * @param deviceContent the value to set deviceContent to
     */
    public void setDeviceContent(Map<String, Object> deviceContent)
    {
        //Codes_SRS_CONFIGURATION_METRICS_PARSER_28_008: [This method shall set the value of queries to the provided value.]
        this.deviceContent = deviceContent;
    }

    public JsonElement toJsonElement()
    {
        JsonObject contentJson = new JsonObject();

        /* Codes_SRS_CONFIGURATION_METRICS_PARSER_28_009: [If the modulesContent is null, the toJsonElement shall not include the `modulesContent` in the final JSON.] */
        if(this.modulesContent != null)
        {
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, Map<String, Object>> entry: this.modulesContent.entrySet())
            {
                map.put(entry.getKey(), entry.getValue());
            }
            contentJson.add(MODULES_CONTENT_NAME, ParserUtility.mapToJsonElement(map));
        }

        /* Codes_SRS_CONFIGURATION_METRICS_PARSER_28_010: [If the deviceContent is null, the toJsonElement shall not include the `deviceContent` in the final JSON.]*/
        if(this.deviceContent != null)
        {
            contentJson.add(DEVICE_CONTENT_NAME, ParserUtility.mapToJsonElement(this.deviceContent));
        }

        return contentJson;
    }
}
