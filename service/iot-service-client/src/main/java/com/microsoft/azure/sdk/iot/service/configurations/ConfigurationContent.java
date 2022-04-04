package com.microsoft.azure.sdk.iot.service.configurations;

import com.microsoft.azure.sdk.iot.service.configurations.serializers.ConfigurationContentParser;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationContent
{
    /**
     * The configurations to be applied to the Edge agent.
     * @see <a href="https://docs.microsoft.com/en-us/azure/iot-edge/module-composition?view=iotedge-2020-11#create-a-deployment-manifest">Create a deployment manifest</a>
     */
    @Getter
    @Setter
    protected Map<String, Map<String, Object>> modulesContent;

    /**
     * The configurations to be applied on device modules.
     */
    @Getter
    @Setter
    protected Map<String, Object> moduleContent;

    /**
     * The configurations to be applied on devices.
     */
    @Getter
    @Setter
    protected Map<String, Object> deviceContent;

    public ConfigurationContent()
    {
        this.modulesContent = new HashMap<>();
        this.deviceContent = new HashMap<>();
        this.moduleContent = new HashMap<>();
    }

    public ConfigurationContentParser toConfigurationContentParser()
    {
        ConfigurationContentParser parser = new ConfigurationContentParser();
        parser.setModulesContent(this.modulesContent);
        parser.setDeviceContent(this.deviceContent);
        parser.setModuleContent(this.moduleContent);
        return parser;
    }
}
