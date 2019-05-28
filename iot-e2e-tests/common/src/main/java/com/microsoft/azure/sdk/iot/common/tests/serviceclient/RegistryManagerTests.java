/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.serviceclient;

import com.microsoft.azure.sdk.iot.common.helpers.ConditionalIgnoreRule;
import com.microsoft.azure.sdk.iot.common.helpers.IntegrationTest;
import com.microsoft.azure.sdk.iot.common.helpers.StandardTierOnlyRule;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.deps.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubBadFormatException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.common.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;
import static org.junit.Assert.*;

/**
 * Test class containing all tests to be run on JVM and android pertaining to identity CRUD. Class needs to be extended
 * in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class RegistryManagerTests extends IntegrationTest
{
    protected static String iotHubConnectionString = "";
    private static String deviceIdPrefix = "java-crud-e2e-test-";
    private static String moduleIdPrefix = "java-crud-module-e2e-test-";
    private static String configIdPrefix = "java-crud-adm-e2e-test-";
    private static String hostName;
    private static RegistryManager registryManager;
    private static final String primaryThumbprint =   "0000000000000000000000000000000000000000";
    private static final String secondaryThumbprint = "1111111111111111111111111111111111111111";
    private static final String primaryThumbprint2 =   "2222222222222222222222222222222222222222";
    private static final String secondaryThumbprint2 = "3333333333333333333333333333333333333333";

    private static final long MAX_TEST_MILLISECONDS = 1 * 60 * 1000;
    
    public static void setUp() throws IOException
    {
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        hostName = IotHubConnectionStringBuilder.createConnectionString(iotHubConnectionString).getHostName();
    }

    public RegistryManagerTests.RegistryManagerTestInstance testInstance;

    public class RegistryManagerTestInstance
    {
        public String deviceId;
        public String moduleId;
        public String configId;

        public RegistryManagerTestInstance() throws InterruptedException, IOException, IotHubException, URISyntaxException
        {
            String uuid = UUID.randomUUID().toString();
            deviceId = deviceIdPrefix + uuid;
            moduleId = moduleIdPrefix + uuid;
            configId = configIdPrefix + uuid;
        }
    }

    @AfterClass
    public static void tearDown()
    {
        if (registryManager != null)
        {
            registryManager.close();
            registryManager = null;
        }
    }

    @Test (timeout=MAX_TEST_MILLISECONDS)
    public void crud_device_e2e() throws Exception
    {
        //-Create-//
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        Device deviceAdded = Device.createFromId(testInstance.deviceId, DeviceStatus.Enabled, null);
        Tools.addDeviceWithRetry(registryManager, deviceAdded);

        //-Read-//
        Device deviceRetrieved = registryManager.getDevice(testInstance.deviceId);

        //-Update-//
        Device deviceUpdated = registryManager.getDevice(testInstance.deviceId);
        deviceUpdated.setStatus(DeviceStatus.Disabled);
        deviceUpdated = registryManager.updateDevice(deviceUpdated);

        //-Delete-//
        registryManager.removeDevice(testInstance.deviceId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, deviceAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, deviceRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), DeviceStatus.Disabled, deviceUpdated.getStatus());
        assertTrue(buildExceptionMessage("", hostName), deviceWasDeletedSuccessfully(registryManager, testInstance.deviceId));
    }

    @Test (timeout=MAX_TEST_MILLISECONDS)
    public void crud_device_e2e_X509_CA_signed() throws Exception
    {
        //-Create-//
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        Device deviceAdded = Device.createDevice(testInstance.deviceId, AuthenticationType.CERTIFICATE_AUTHORITY);
        Tools.addDeviceWithRetry(registryManager, deviceAdded);

        //-Read-//
        Device deviceRetrieved = registryManager.getDevice(testInstance.deviceId);

        //-Update-//
        Device deviceUpdated = registryManager.getDevice(testInstance.deviceId);
        deviceUpdated.setStatus(DeviceStatus.Disabled);
        deviceUpdated = registryManager.updateDevice(deviceUpdated);

        //-Delete-//
        registryManager.removeDevice(testInstance.deviceId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, deviceAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, deviceRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), AuthenticationType.CERTIFICATE_AUTHORITY, deviceRetrieved.getAuthenticationType());
        assertEquals(buildExceptionMessage("", hostName), DeviceStatus.Disabled, deviceUpdated.getStatus());
        assertNull(buildExceptionMessage("", hostName), deviceAdded.getPrimaryThumbprint());
        assertNull(buildExceptionMessage("", hostName), deviceAdded.getSecondaryKey());
        assertNull(buildExceptionMessage("", hostName), deviceRetrieved.getPrimaryThumbprint());
        assertNull(buildExceptionMessage("", hostName), deviceRetrieved.getSecondaryThumbprint());
        assertTrue(buildExceptionMessage("", hostName), deviceWasDeletedSuccessfully(registryManager, testInstance.deviceId));
    }

    @Test (timeout=MAX_TEST_MILLISECONDS)
    public void crud_device_e2e_X509_self_signed() throws Exception
    {
        //-Create-//
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        Device deviceAdded = Device.createDevice(testInstance.deviceId, AuthenticationType.SELF_SIGNED);
        deviceAdded.setThumbprintFinal(primaryThumbprint, secondaryThumbprint);
        Tools.addDeviceWithRetry(registryManager, deviceAdded);

        //-Read-//
        Device deviceRetrieved = registryManager.getDevice(testInstance.deviceId);

        //-Update-//
        Device deviceUpdated = registryManager.getDevice(testInstance.deviceId);
        deviceUpdated.setThumbprintFinal(primaryThumbprint2, secondaryThumbprint2);
        deviceUpdated = registryManager.updateDevice(deviceUpdated);

        //-Delete-//
        registryManager.removeDevice(testInstance.deviceId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, deviceAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, deviceRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), AuthenticationType.SELF_SIGNED, deviceAdded.getAuthenticationType());
        assertEquals(buildExceptionMessage("", hostName), AuthenticationType.SELF_SIGNED, deviceRetrieved.getAuthenticationType());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint, deviceAdded.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint, deviceAdded.getSecondaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint, deviceRetrieved.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint, deviceRetrieved.getSecondaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint2, deviceUpdated.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint2, deviceUpdated.getSecondaryThumbprint());
        assertTrue(buildExceptionMessage("", hostName), deviceWasDeletedSuccessfully(registryManager, testInstance.deviceId));
    }

    //TODO what is this testing?
    @Test
    public void getDeviceStatisticsTest() throws Exception
    {
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        RegistryManager registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        Tools.getStatisticsWithRetry(registryManager);
    }

    @Test (timeout=MAX_TEST_MILLISECONDS)
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void crud_module_e2e() throws Exception
    {
        // Arrange
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        SymmetricKey expectedSymmetricKey = new SymmetricKey();

        Device deviceSetup = Device.createFromId(testInstance.deviceId, DeviceStatus.Enabled, null);
        Tools.addDeviceWithRetry(registryManager, deviceSetup);
        deleteModuleIfItExistsAlready(registryManager, testInstance.deviceId, testInstance.moduleId);

        //-Create-//
        com.microsoft.azure.sdk.iot.service.Module moduleAdded = com.microsoft.azure.sdk.iot.service.Module.createFromId(testInstance.deviceId, testInstance.moduleId, null);
        Tools.addModuleWithRetry(registryManager, moduleAdded);

        //-Read-//
        com.microsoft.azure.sdk.iot.service.Module moduleRetrieved = registryManager.getModule(testInstance.deviceId, testInstance.moduleId);

        //-Update-//
        com.microsoft.azure.sdk.iot.service.Module moduleUpdated = registryManager.getModule(testInstance.deviceId, testInstance.moduleId);
        moduleUpdated.getSymmetricKey().setPrimaryKeyFinal(expectedSymmetricKey.getPrimaryKey());
        moduleUpdated = registryManager.updateModule(moduleUpdated);

        //-Delete-//
        registryManager.removeModule(testInstance.deviceId, testInstance.moduleId);
        registryManager.removeDevice(testInstance.deviceId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, moduleAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.moduleId, moduleAdded.getId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, moduleRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.moduleId, moduleRetrieved.getId());
        assertEquals(buildExceptionMessage("", hostName), expectedSymmetricKey.getPrimaryKey(), moduleUpdated.getPrimaryKey());
        assertTrue(buildExceptionMessage("", hostName), moduleWasDeletedSuccessfully(registryManager, testInstance.deviceId, testInstance.moduleId));
    }

    @Test (timeout=MAX_TEST_MILLISECONDS)
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void crud_module_e2e_X509_CA_signed() throws Exception
    {
        // Arrange
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        deleteModuleIfItExistsAlready(registryManager, testInstance.deviceId, testInstance.moduleId);
        Device deviceSetup = Device.createFromId(testInstance.deviceId, DeviceStatus.Enabled, null);
        Tools.addDeviceWithRetry(registryManager, deviceSetup);
        deleteModuleIfItExistsAlready(registryManager, testInstance.deviceId, testInstance.moduleId);

        //-Create-//
        com.microsoft.azure.sdk.iot.service.Module moduleAdded = com.microsoft.azure.sdk.iot.service.Module.createModule(testInstance.deviceId, testInstance.moduleId, AuthenticationType.CERTIFICATE_AUTHORITY);
        Tools.addModuleWithRetry(registryManager, moduleAdded);

        //-Read-//
        com.microsoft.azure.sdk.iot.service.Module moduleRetrieved = registryManager.getModule(testInstance.deviceId, testInstance.moduleId);

        //-Delete-//
        registryManager.removeModule(testInstance.deviceId, testInstance.moduleId);
        registryManager.removeDevice(testInstance.deviceId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, moduleAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.moduleId, moduleAdded.getId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, moduleRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.moduleId, moduleRetrieved.getId());
        assertNull(buildExceptionMessage("", hostName), moduleAdded.getPrimaryThumbprint());
        assertNull(buildExceptionMessage("", hostName), moduleAdded.getSecondaryThumbprint());
        assertNull(buildExceptionMessage("", hostName), moduleRetrieved.getPrimaryThumbprint());
        assertNull(buildExceptionMessage("", hostName), moduleRetrieved.getSecondaryThumbprint());
        assertTrue(buildExceptionMessage("", hostName), moduleWasDeletedSuccessfully(registryManager, testInstance.deviceId, testInstance.moduleId));
    }

    @Test (timeout=MAX_TEST_MILLISECONDS)
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void crud_module_e2e_X509_self_signed() throws Exception
    {
        // Arrange
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        Device deviceSetup = Device.createFromId(testInstance.deviceId, DeviceStatus.Enabled, null);
        Tools.addDeviceWithRetry(registryManager, deviceSetup);
        deleteModuleIfItExistsAlready(registryManager, testInstance.deviceId, testInstance.moduleId);

        //-Create-//
        com.microsoft.azure.sdk.iot.service.Module moduleAdded = com.microsoft.azure.sdk.iot.service.Module.createModule(testInstance.deviceId, testInstance.moduleId, AuthenticationType.SELF_SIGNED);
        moduleAdded.setThumbprintFinal(primaryThumbprint, secondaryThumbprint);
        Tools.addModuleWithRetry(registryManager, moduleAdded);

        //-Read-//
        com.microsoft.azure.sdk.iot.service.Module moduleRetrieved = registryManager.getModule(testInstance.deviceId, testInstance.moduleId);

        //-Update-//
        com.microsoft.azure.sdk.iot.service.Module moduleUpdated = registryManager.getModule(testInstance.deviceId, testInstance.moduleId);
        moduleUpdated.setThumbprintFinal(primaryThumbprint2, secondaryThumbprint2);
        moduleUpdated = registryManager.updateModule(moduleUpdated);

        //-Delete-//
        registryManager.removeModule(testInstance.deviceId, testInstance.moduleId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, moduleAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.moduleId, moduleAdded.getId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.deviceId, moduleRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), testInstance.moduleId, moduleRetrieved.getId());
        assertEquals(buildExceptionMessage("", hostName), AuthenticationType.SELF_SIGNED, moduleAdded.getAuthenticationType());
        assertEquals(buildExceptionMessage("", hostName), AuthenticationType.SELF_SIGNED, moduleRetrieved.getAuthenticationType());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint, moduleAdded.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint, moduleAdded.getSecondaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint, moduleRetrieved.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint, moduleRetrieved.getSecondaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint2, moduleUpdated.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint2, moduleUpdated.getSecondaryThumbprint());
        assertTrue(buildExceptionMessage("", hostName), moduleWasDeletedSuccessfully(registryManager, testInstance.deviceId, testInstance.moduleId));
    }

    @Test (timeout=MAX_TEST_MILLISECONDS)
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void crud_adm_configuration_e2e() throws Exception
    {
        // Arrange
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
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
        registryManager.addConfiguration(configAdded);

        //-Read-//
        Configuration configRetrieved = registryManager.getConfiguration(testInstance.configId);

        //-Update-//
        Configuration configUpdated = registryManager.getConfiguration(testInstance.configId);
        configUpdated.setPriority(1);
        configUpdated = registryManager.updateConfiguration(configUpdated);

        //-Delete-//
        registryManager.removeConfiguration(testInstance.configId);

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
        assertTrue(buildExceptionMessage("", hostName), configWasDeletedSuccessfully(registryManager, testInstance.configId));
    }

    @Test (expected = IotHubBadFormatException.class)
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void apply_configuration_e2e() throws Exception
    {
        // Arrange
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        Device deviceSetup = Device.createFromId(testInstance.deviceId, DeviceStatus.Enabled, null);
        Tools.addDeviceWithRetry(registryManager, deviceSetup);
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

        // Act
        registryManager.applyConfigurationContentOnDevice(testInstance.deviceId, content);
    }

    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    @Test
    public void deviceCreationWithSecurityScope() throws IOException, InterruptedException, IotHubException, URISyntaxException
    {
        // Arrange
        RegistryManagerTestInstance testInstance = new RegistryManagerTestInstance();
        deleteDeviceIfItExistsAlready(registryManager, testInstance.deviceId);

        //-Create-//
        Device edgeDevice = Device.createFromId(testInstance.deviceId, DeviceStatus.Enabled, null);
        DeviceCapabilities capabilities = new DeviceCapabilities();
        capabilities.setIotEdge(true);
        edgeDevice.setCapabilities(capabilities);
        edgeDevice = Tools.addDeviceWithRetry(registryManager, edgeDevice);

        Device leafDevice = Device.createFromId(testInstance.deviceId + "-leaf", DeviceStatus.Enabled, null);
        assertNotNull(edgeDevice.getScope());
        leafDevice.setScope(edgeDevice.getScope());
        Tools.addDeviceWithRetry(registryManager, leafDevice);


        //-Read-//
        Device deviceRetrieved = registryManager.getDevice(testInstance.deviceId);

        //-Delete-//
        registryManager.removeDevice(testInstance.deviceId);

        // Assert
        assertEquals(buildExceptionMessage("Registered device id is not correct", hostName), testInstance.deviceId, edgeDevice.getDeviceId());
        assertEquals(buildExceptionMessage("Registered device id is not correct", hostName), testInstance.deviceId, deviceRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("Security scopes did not match", hostName), deviceRetrieved.getScope(), edgeDevice.getScope());
    }
    
    private void deleteDeviceIfItExistsAlready(RegistryManager registryManager, String deviceId) throws IOException, InterruptedException
    {
        try
        {
            Tools.getDeviceWithRetry(registryManager, deviceId);

            //if no exception yet, identity exists so it can be deleted
            try
            {
                registryManager.removeDevice(deviceId);
            }
            catch (IotHubException | IOException e)
            {
                System.out.println("Initialization failed, could not remove device: " + deviceId);
            }
        }
        catch (IotHubException e)
        {
        }
    }

    private void deleteModuleIfItExistsAlready(RegistryManager registryManager, String deviceId, String moduleId) throws IOException, InterruptedException
    {
        try
        {
            Tools.getModuleWithRetry(registryManager, deviceId, moduleId);

            //if no exception yet, identity exists so it can be deleted
            try
            {
                registryManager.removeModule(deviceId, moduleId);
            }
            catch (IotHubException | IOException e)
            {
                System.out.println("Initialization failed, could not remove deviceId/moduleId: " + deviceId + "/" + moduleId);
            }
        }
        catch (IotHubException e)
        {
        }
    }

    private boolean deviceWasDeletedSuccessfully(RegistryManager registryManager, String deviceId) throws IOException
    {
        try
        {
            registryManager.getDevice(deviceId);
        }
        catch (IotHubException e)
        {
            // identity should have been deleted, so this catch is expected
            return true;
        }

        // identity could still be retrieved, so it was not deleted successfully
        return false;
    }

    private boolean moduleWasDeletedSuccessfully(RegistryManager registryManager, String deviceId, String moduleId) throws IOException
    {
        try
        {
            registryManager.getModule(deviceId, moduleId);
        }
        catch (IotHubException e)
        {
            // module should have been deleted, so this catch is expected
            return true;
        }

        // module could still be retrieved, so it was not deleted successfully
        return false;
    }

    private boolean configWasDeletedSuccessfully(RegistryManager registryManager, String configId) throws IOException
    {
        try
        {
            registryManager.getConfiguration(configId);
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