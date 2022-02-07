package com.microsoft.azure.sdk.iot.service.configurations.serializers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.service.ParserUtility;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationContentParser
{
    private static final String MODULES_CONTENT_NAME = "modulesContent";
    @Expose(serialize = false)
    @SerializedName(MODULES_CONTENT_NAME)
    @Getter
    @Setter
    private Map<String, Map<String, Object>> modulesContent;

    private static final String MODULE_CONTENT_NAME = "moduleContent";
    @Expose(serialize = false)
    @SerializedName(MODULE_CONTENT_NAME)
    @Getter
    @Setter
    private Map<String, Object> moduleContent;

    private static final String DEVICE_CONTENT_NAME = "deviceContent";
    @Expose(serialize = false)
    @SerializedName(DEVICE_CONTENT_NAME)
    @Getter
    @Setter
    private Map<String, Object> deviceContent;

    private final transient static Gson gson = new Gson();

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
        this.moduleContent = configurationContentParser.moduleContent;
        this.deviceContent = configurationContentParser.deviceContent;
    }

    public String toJson()
    {
        return gson.toJson(this);
    }

    public JsonElement toJsonElement()
    {
        JsonObject contentJson = new JsonObject();

        /* Codes_SRS_CONFIGURATION_METRICS_PARSER_28_009: [If the modulesContent is null, the toJsonElement shall not include the `modulesContent` in the final JSON.] */
        if (this.modulesContent != null)
        {
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, Map<String, Object>> entry: this.modulesContent.entrySet())
            {
                map.put(entry.getKey(), entry.getValue());
            }
            contentJson.add(MODULES_CONTENT_NAME, ParserUtility.mapToJsonElement(map));
        }

        /* Codes_SRS_CONFIGURATION_METRICS_PARSER_28_010: [If the deviceContent is null, the toJsonElement shall not include the `deviceContent` in the final JSON.]*/
        if (this.deviceContent != null)
        {
            contentJson.add(DEVICE_CONTENT_NAME, ParserUtility.mapToJsonElement(this.deviceContent));
        }

        if (this.moduleContent != null)
        {
            contentJson.add(MODULE_CONTENT_NAME, ParserUtility.mapToJsonElement(this.moduleContent));
        }

        return contentJson;
    }
}
