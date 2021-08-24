package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.deps.serializer.ConfigurationContentParser;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationContent
{
    @Getter
    @Setter
    Map<String, Map<String, Object>> modulesContent;

    @Getter
    @Setter
    Map<String, Object> deviceContent;

    public ConfigurationContent()
    {
        this.modulesContent = new HashMap<>();
        this.deviceContent = new HashMap<>();
    }

    public ConfigurationContentParser toConfigurationContentParser()
    {
        ConfigurationContentParser parser = new ConfigurationContentParser();
        parser.setModulesContent(this.modulesContent);
        parser.setDeviceContent(this.deviceContent);
        return parser;
    }
}
