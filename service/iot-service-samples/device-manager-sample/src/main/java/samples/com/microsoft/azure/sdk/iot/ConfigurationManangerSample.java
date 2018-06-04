/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.Configuration;
import com.microsoft.azure.sdk.iot.service.ConfigurationContent;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

/** Manages configuration on IotHub - CRUD operations */
public class ConfigurationManangerSample
{
    /**
     * A simple sample for doing CRUD operations
     * @param args
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting sample...");

        System.out.println("Add Configuration started");
        AddConfiguration();
        System.out.println("Add Configuration finished");

        System.out.println("Get Configuration started");
        Configuration config = GetConfiguration();
        System.out.println("Get Configuration finished");

        System.out.println("Update Device started");
        UpdateConfiguration(config);
        System.out.println("Update Device finished");

        System.out.println("Remove Device started");
        RemoveConfiguration();
        System.out.println("Remove Device finished");

        System.out.println("Shutting down sample...");
    }

    private static void AddConfiguration() throws Exception
    {
        final HashMap<String, HashMap<String, Object>> PROPERTIES_SAMPLE = new HashMap<String, HashMap<String, Object>>()
        {
            {
                put("fakeModule", new HashMap<String, Object>()
                {
                    {
                        put("properties.desired", new HashMap<String, Object>()
                        {
                            {
                                put("prop1", "foo");
                            }
                        });
                    }
                });
            }
        };

        final HashMap<String, Object> DEVICE_CONTENT_SAMPLE = new HashMap<String, Object>()
        {
            {
                put("properties.desired.deviceContent_key", new HashMap<String, Object>()
                        {
                            {
                                put("p1", new HashMap<String, Object>()
                                        {{
                                            put("p2", new HashMap<String,String>()
                                            {{
                                                put("new_key_value", "value_1");
                                            }});
                                        }}
                                );

                            }
                        }
                );

            }
        };

        RegistryManager registryManager = RegistryManager.createFromConnectionString(SampleUtils.iotHubConnectionString);

        ConfigurationContent content = new ConfigurationContent();
        //content.setModulesContent(PROPERTIES_SAMPLE);
        content.setDeviceContent(DEVICE_CONTENT_SAMPLE);

        Configuration config = new Configuration(SampleUtils.configurationId);
        config.setContent(content);
        config.setTargetCondition("");
        config.setPriority(0);

        try
        {
            config = registryManager.addConfiguration(config);

            System.out.println("Device created: " + config.getId());
        }
        catch (IotHubException iote)
        {
            iote.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        registryManager.close();
    }

    private static Configuration GetConfiguration() throws Exception
    {
        RegistryManager registryManager = RegistryManager.createFromConnectionString(SampleUtils.iotHubConnectionString);

        Configuration returnConfig = null;
        try
        {
            returnConfig = registryManager.getConfiguration(SampleUtils.configurationId);

            System.out.println("Configuration: " + returnConfig.getId());
            System.out.println("Configuration schema version: " + returnConfig.getSchemaVersion());
            System.out.println("Configuration etag: " + returnConfig.getEtag());
        }
        catch (IotHubException iote)
        {
            iote.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        registryManager.close();

        return returnConfig;
    }

    private static void UpdateConfiguration(Configuration config) throws Exception
    {
        RegistryManager registryManager = RegistryManager.createFromConnectionString(SampleUtils.iotHubConnectionString);

        config.setPriority(1);
        try
        {
            config = registryManager.updateConfiguration(config);

            System.out.println("Configuration updated: " + config.getId());
            System.out.println("Device primary key: " + config.getPriority());
        }
        catch (IotHubException iote)
        {
            iote.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        registryManager.close();
    }

    private static void RemoveConfiguration() throws Exception
    {
        RegistryManager registryManager = RegistryManager.createFromConnectionString(SampleUtils.iotHubConnectionString);

        try
        {
            registryManager.removeConfiguration(SampleUtils.configurationId);
            System.out.println("Device removed: " + SampleUtils.configurationId);
        }
        catch (IotHubException iote)
        {
            iote.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        registryManager.close();
    }
}
