package com.microsoft.azure.sdk.iot.service.configurations.serializers;

import com.google.gson.*;
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

    private final transient static Gson gson = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();

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
        if (json == null || json.isEmpty())
        {
            throw new IllegalArgumentException("The provided json cannot be null or empty");
        }

        ConfigurationContentParser configurationContentParser;
        try
        {
            configurationContentParser = gson.fromJson(json, ConfigurationContentParser.class);
        }
        catch (JsonSyntaxException e)
        {
            throw new IllegalArgumentException("The provided json could not be parsed");
        }

        this.modulesContent = configurationContentParser.modulesContent;
        this.moduleContent = configurationContentParser.moduleContent;
        this.deviceContent = configurationContentParser.deviceContent;
    }

    public JsonElement toJsonElement()
    {
        JsonObject contentJson = new JsonObject();

        if (this.modulesContent != null && this.modulesContent.size() > 0)
        {
            Map<String, Object> map = new HashMap<>();
            map.putAll(this.modulesContent);
            contentJson.add(MODULES_CONTENT_NAME, ParserUtility.mapToJsonElement(map));
        }

        if (this.deviceContent != null && this.deviceContent.size() > 0)
        {
            contentJson.add(DEVICE_CONTENT_NAME, ParserUtility.mapToJsonElement(this.deviceContent));
        }

        if (this.moduleContent != null && this.moduleContent.size() > 0)
        {
            contentJson.add(MODULE_CONTENT_NAME, ParserUtility.mapToJsonElement(this.moduleContent));
        }

        return contentJson;
    }
}
