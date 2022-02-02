// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.iothub.serviceclient;

import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.DeviceStatus;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.configurations.Configuration;
import com.microsoft.azure.sdk.iot.service.configurations.ConfigurationContent;
import com.microsoft.azure.sdk.iot.service.configurations.ConfigurationsClient;
import com.microsoft.azure.sdk.iot.service.configurations.ConfigurationsClientOptions;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubBadFormatException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

@Slf4j
@IotHubTest
public class ConfigurationsClientTests extends IntegrationTest
{
    protected static String iotHubConnectionString = "";

    private static final String deviceIdPrefix = "java-crud-e2e-test-";
    private static final String moduleIdPrefix = "java-crud-module-e2e-test-";
    private static final String configIdPrefix = "java-crud-adm-e2e-test-";
    private static String hostName;

    protected static HttpProxyServer proxyServer;
    protected static String testProxyHostname = "127.0.0.1";
    protected static int testProxyPort = 8874;

    @BeforeClass
    public static void setUp() throws IOException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);

        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        hostName = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString).getHostName();
    }

    public ConfigurationsClientTests.ConfigurationsClientTestInstance testInstance;

    public static class ConfigurationsClientTestInstance
    {
        public String deviceId;
        public String moduleId;
        public String configId;
        private ConfigurationsClient configurationsClient;
        private RegistryManager registryManager;

        public ConfigurationsClientTestInstance()
        {
            this(ConfigurationsClientOptions.builder().build());
        }

        public ConfigurationsClientTestInstance(ConfigurationsClient configurationsClient)
        {
            String uuid = UUID.randomUUID().toString();
            this.deviceId = deviceIdPrefix + uuid;
            this.moduleId = moduleIdPrefix + uuid;
            this.configId = configIdPrefix + uuid;
            this.configurationsClient = configurationsClient;
            this.registryManager = new RegistryManager(iotHubConnectionString);
        }

        public ConfigurationsClientTestInstance(ConfigurationsClientOptions options)
        {
            String uuid = UUID.randomUUID().toString();
            this.deviceId = deviceIdPrefix + uuid;
            this.moduleId = moduleIdPrefix + uuid;
            this.configId = configIdPrefix + uuid;
            this.configurationsClient = new ConfigurationsClient(iotHubConnectionString, options);
            this.registryManager = new RegistryManager(iotHubConnectionString);
        }
    }

    @BeforeClass
    public static void startProxy()
    {
        proxyServer = DefaultHttpProxyServer.bootstrap()
            .withPort(testProxyPort)
            .start();
    }

    @AfterClass
    public static void stopProxy()
    {
        proxyServer.stop();
    }

    @Test
    @StandardTierHubOnlyTest
    public void crud_adm_configuration_e2e() throws Exception
    {
        // Arrange
        ConfigurationsClientTestInstance testInstance = new ConfigurationsClientTestInstance();
        final HashMap<String, Object> testDeviceContent = new HashMap<String, Object>()
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

        //-Create-//
        Configuration configAdded = new Configuration(testInstance.configId);

        ConfigurationContent content = new ConfigurationContent();
        content.setDeviceContent(testDeviceContent);
        configAdded.setContent(content);
        configAdded.getMetrics().setQueries(new HashMap<String, String>(){{put("waterSettingsPending",
            "SELECT deviceId FROM devices WHERE properties.reported.chillerWaterSettings.status=\'pending\'");}});
        configAdded.setTargetCondition("properties.reported.chillerProperties.model=\'4000x\'");
        configAdded.setPriority(20);
        testInstance.configurationsClient.addConfiguration(configAdded);

        //-Read-//
        Configuration configRetrieved = testInstance.configurationsClient.getConfiguration(testInstance.configId);

        //-Update-//
        Configuration configUpdated = testInstance.configurationsClient.getConfiguration(testInstance.configId);
        configUpdated.setPriority(1);
        configUpdated = testInstance.configurationsClient.updateConfiguration(configUpdated);

        //-Delete-//
        testInstance.configurationsClient.removeConfiguration(testInstance.configId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), testInstance.configId, configAdded.getId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.configId, configRetrieved.getId());
        String actualString = configRetrieved.getContent().getDeviceContent().get("properties.desired.chiller-water").toString();
        actualString = actualString.substring(1, actualString.length()-1);
        String[] keyValuePairs = actualString.split(",");
        HashMap<String, String> actualMap = new HashMap<>();
        for (String pair : keyValuePairs)
        {
            String[] entry = pair.split("=");
            actualMap.put(entry[0].trim(), entry[1].trim());
        }
        assertEquals(buildExceptionMessage("", hostName), "66.0", actualMap.get("temperature"));
        assertEquals(buildExceptionMessage("", hostName), "28.0", actualMap.get("pressure"));
        assertEquals(buildExceptionMessage("", hostName), "SELECT deviceId FROM devices WHERE properties.reported.chillerWaterSettings.status=\'pending\'",
            configRetrieved.getMetrics().getQueries().get("waterSettingsPending"));
        assertEquals(buildExceptionMessage("", hostName), "properties.reported.chillerProperties.model=\'4000x\'",
            configRetrieved.getTargetCondition());
        assertEquals(buildExceptionMessage("", hostName), new Integer(20), configRetrieved.getPriority());
        assertEquals(buildExceptionMessage("", hostName), testInstance.configId, configUpdated.getId());
        assertEquals(buildExceptionMessage("", hostName), new Integer(1), configUpdated.getPriority());
        assertTrue(buildExceptionMessage("", hostName), configWasDeletedSuccessfully(testInstance.configurationsClient, testInstance.configId));
    }

    @Test
    @StandardTierHubOnlyTest
    public void apply_configuration_e2e() throws Exception
    {
        // Arrange
        ConfigurationsClientTestInstance testInstance = new ConfigurationsClientTestInstance();
        Device deviceSetup = Device.createFromId(testInstance.deviceId, DeviceStatus.Enabled, null);
        Tools.addDeviceWithRetry(testInstance.registryManager, deviceSetup);
        final HashMap<String, Object> testDeviceContent = new HashMap<String, Object>()
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
        ConfigurationContent content = new ConfigurationContent();
        content.setDeviceContent(testDeviceContent);

        boolean expectedExceptionThrown = false;

        // Act
        try
        {
            testInstance.configurationsClient.applyConfigurationContentOnDevice(testInstance.deviceId, content);
        }
        catch (IotHubBadFormatException e)
        {
            expectedExceptionThrown = true;
        }

        assertTrue("Bad format exception wasn't thrown but was expected", expectedExceptionThrown);
    }

    private static boolean configWasDeletedSuccessfully(ConfigurationsClient configurationsClient, String configId) throws IOException
    {
        try
        {
            configurationsClient.getConfiguration(configId);
        }
        catch (IotHubException e)
        {
            // configuration should have been deleted, so this catch is expected
            return true;
        }

        // configuration could still be retrieved, so it was not deleted successfully
        return false;
    }
}
