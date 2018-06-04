package com.microsoft.azure.sdk.iot.service;

import java.util.HashMap;

public class ConfigurationContent
{
    protected HashMap<String, HashMap<String, Object>> modulesContent;
    protected HashMap<String, Object> deviceContent;

    public ConfigurationContent()
    {
        // Codes_SRS_SERVICE_SDK_JAVA_CONFIGURATION_CONTENT_28_001: [The constructor shall initialize modulesContent and deviceContent fields.]
        this.modulesContent = new HashMap<>();
        this.deviceContent = new HashMap<>();
    }

    // Codes_SRS_SERVICE_SDK_JAVA_CONFIGURATION_CONTENT_28_002: [The ConfigurationContent class shall have the following properties: modulesContent and deviceContent
    public void setModulesContent(HashMap<String, HashMap<String, Object>> modulesContent)
    {
        this.modulesContent = modulesContent;
    }

    public HashMap<String, HashMap<String, Object>> getModulesContent()
    {
        return modulesContent;
    }

    public void setDeviceContent(HashMap<String, Object> deviceContent)
    {
        this.deviceContent = deviceContent;
    }

    public HashMap<String, Object> getDeviceContent()
    {
        return deviceContent;
    }
}
