/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.configurations.Configuration;
import com.microsoft.azure.sdk.iot.service.configurations.ConfigurationContent;
import com.microsoft.azure.sdk.iot.service.configurations.ConfigurationsClient;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Manages configuration on IotHub - CRUD operations */
public class ConfigurationManangerSample
{
    static final HashMap<String, Object> DEVICE_CONTENT_SAMPLE = new HashMap<String, Object>()
    {
        {
            put("properties.desired.chiller-water", new HashMap<String, Object>()
                    {
                        {
                            put("temperature", 66);
                            put("pressure", 28);
                        }
                    }
            );
        }
    };

    /**
     * A simple sample for doing CRUD operations
     * @param args unused
     * @throws Exception If any exception is thrown
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting sample...");

        System.out.println("Get All Configuration started");
        GetAllConfiguration();
        System.out.println("Get All Configuration finished");

        System.out.println("=================================");

        System.out.println("Add Configuration started");
        AddConfiguration();
        System.out.println("Add Configuration finished");

        System.out.println("=================================");

        System.out.println("Get Configuration started");
        Configuration config = GetConfiguration();
        System.out.println("Get Configuration finished");

        System.out.println("=================================");

        System.out.println("Update Device started");
        UpdateConfiguration(config);
        System.out.println("Update Device finished");

        System.out.println("=================================");

        System.out.println("Remove Device started");
        RemoveConfiguration();
        System.out.println("Remove Device finished");

        System.out.println("Shutting down sample...");
    }

    private static void GetAllConfiguration()
    {
        ConfigurationsClient configurationsClient = new ConfigurationsClient(SampleUtils.iotHubConnectionString);

        try
        {
            List<Configuration> configList = configurationsClient.getConfigurations(20);
            System.out.println(configList.size() + " Configurations found");

            for (Configuration config : configList)
            {
                System.out.println("Configuration Id: " + config.getId());
            }
        }
        catch (IotHubException | IOException iote)
        {
            iote.printStackTrace();
        }
    }

    private static void AddConfiguration()
    {
        ConfigurationsClient configurationsClient = new ConfigurationsClient(SampleUtils.iotHubConnectionString);

        ConfigurationContent content = new ConfigurationContent();
        content.setDeviceContent(DEVICE_CONTENT_SAMPLE);

        Configuration config = new Configuration(SampleUtils.configurationId);
        config.setContent(content);
        config.getMetrics().setQueries(new HashMap<String, String>(){{put("waterSettingsPending",
                "SELECT deviceId FROM devices WHERE properties.reported.chillerWaterSettings.status='pending'");}});
        config.setTargetCondition("properties.reported.chillerProperties.model='4000x'");
        config.setPriority(20);

        try
        {
            config = configurationsClient.addConfiguration(config);
            System.out.println("Add configuration " + config.getId() + " succeeded.");
            printConfiguration(config);
        }
        catch (IotHubException | IOException iote)
        {
            iote.printStackTrace();
        }
    }

    private static Configuration GetConfiguration()
    {
        ConfigurationsClient configurationsClient = new ConfigurationsClient(SampleUtils.iotHubConnectionString);

        Configuration returnConfig = null;
        try
        {
            returnConfig = configurationsClient.getConfiguration(SampleUtils.configurationId);
            printConfiguration(returnConfig);
        }
        catch (IotHubException | IOException iote)
        {
            iote.printStackTrace();
        }

        return returnConfig;
    }

    private static void UpdateConfiguration(Configuration config)
    {
        ConfigurationsClient configurationsClient = new ConfigurationsClient(SampleUtils.iotHubConnectionString);

        config.setPriority(1);
        try
        {
            config = configurationsClient.updateConfiguration(config);
            printConfiguration(config);
        }
        catch (IotHubException | IOException iote)
        {
            iote.printStackTrace();
        }
    }

    private static void RemoveConfiguration()
    {
        ConfigurationsClient configurationsClient = new ConfigurationsClient(SampleUtils.iotHubConnectionString);

        try
        {
            configurationsClient.removeConfiguration(SampleUtils.configurationId);
            System.out.println("Device removed: " + SampleUtils.configurationId);
        }
        catch (IotHubException | IOException iote)
        {
            iote.printStackTrace();
        }
    }

    private static void printConfiguration(Configuration config)
    {
        System.out.println("Configuration: " + config.getId());
        System.out.println("  Etag: " + config.getEtag());

        Map<String, String> queries = config.getMetrics().getQueries();
        System.out.println("  Metric.Queries: ");
        if (queries != null)
        {
            for (Map.Entry<String, String> entry : queries.entrySet())
            {
                String key = entry.getKey();
                String val = entry.getValue();
                System.out.println("    " + key + " : " + val);
            }
        }

        Map<String, Long> results = config.getMetrics().getResults();
        System.out.println("  Metric.Results: ");
        if (results != null)
        {
            for (Map.Entry<String, Long> entry : results.entrySet())
            {
                String key = entry.getKey();
                Long val = entry.getValue();
                System.out.println("    " + key + " : " + val);
            }
        }

        Map<String, String> squeries = config.getSystemMetrics().getQueries();
        System.out.println("  SystemMetric.Queries: ");
        if (squeries != null)
        {
            for (Map.Entry<String, String> entry : squeries.entrySet())
            {
                String key = entry.getKey();
                String val = entry.getValue();
                System.out.println("    " + key + " : " + val);
            }
        }

        Map<String, Long> sresults = config.getSystemMetrics().getResults();
        System.out.println("  SystemMetric.Results: ");
        if (sresults != null)
        {
            for (Map.Entry<String, Long> entry : sresults.entrySet())
            {
                String key = entry.getKey();
                Long val = entry.getValue();
                System.out.println("    " + key + " : " + val);
            }
        }

        System.out.println("  Target Condition: " + config.getTargetCondition());
        System.out.println("  Last Updated Time: " + config.getLastUpdatedTimeUtc());
        System.out.println("  Created Time UTC: " + config.getCreatedTimeUtc());
        System.out.println("  Priority: " + config.getPriority());

        Map<String, Object> dc = config.getContent().getDeviceContent();
        System.out.println("  Content.DeviceContent: ");
        if (dc != null)
        {
            for (Map.Entry<String, Object> entry : dc.entrySet())
            {
                String key = entry.getKey();
                Object val = entry.getValue();
                System.out.println("    " + key + " : " + val.toString());
            }
        }

        Map<String, Object> moduleContent = config.getContent().getModuleContent();
        System.out.println("  Content.ModuleContent: ");
        if (dc != null)
        {
            for (Map.Entry<String, Object> entry : moduleContent.entrySet())
            {
                String key = entry.getKey();
                Object val = entry.getValue();
                System.out.println("    " + key + " : " + val.toString());
            }
        }

        Map<String, Map<String, Object>> mc = config.getContent().getModulesContent();
        System.out.println("  Content.ModuleContent: ");
        if (mc != null)
        {
            for (Map.Entry<String, Map<String, Object>> entry : mc.entrySet())
            {
                String key = entry.getKey();
                System.out.println("    " + key + " : ");
                Map<String, Object> innermap = entry.getValue();
                for (Map.Entry<String, Object> innerEntry : innermap.entrySet())
                {
                    String innerKey = entry.getKey();
                    Object innerVal = entry.getValue();
                    System.out.println("        " + innerKey + " : " + innerVal.toString());
                }
            }
        }
    }
}
