/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.serviceclient;

import com.microsoft.azure.sdk.iot.common.helpers.ConditionalIgnoreRule;
import com.microsoft.azure.sdk.iot.common.helpers.IntegrationTest;
import com.microsoft.azure.sdk.iot.common.helpers.StandardTierOnlyRule;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubBadFormatException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
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
    private static String deviceId = "java-crud-e2e-test";
    private static String deviceForTest = "deviceForTest";
    private static String moduleId = "java-crud-module-e2e-test";
    private static String configId = "java-crud-adm-e2e-test";
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
        deviceId = deviceId.concat("-" + UUID.randomUUID());
        hostName = IotHubConnectionStringBuilder.createConnectionString(iotHubConnectionString).getHostName();
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
        // Arrange
        deleteDeviceIfItExistsAlready(registryManager, deviceId);

        //-Create-//
        Device deviceAdded = Device.createFromId(deviceId, DeviceStatus.Enabled, null);
        Tools.addDeviceWithRetry(registryManager, deviceAdded);

        //-Read-//
        Device deviceRetrieved = registryManager.getDevice(deviceId);

        //-Update-//
        Device deviceUpdated = registryManager.getDevice(deviceId);
        deviceUpdated.setStatus(DeviceStatus.Disabled);
        deviceUpdated = registryManager.updateDevice(deviceUpdated);

        //-Delete-//
        registryManager.removeDevice(deviceId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), deviceId, deviceAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), deviceId, deviceRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), DeviceStatus.Disabled, deviceUpdated.getStatus());
        assertTrue(buildExceptionMessage("", hostName), deviceWasDeletedSuccessfully(registryManager, deviceId));
    }

    @Test (timeout=MAX_TEST_MILLISECONDS)
    public void crud_device_e2e_X509_CA_signed() throws Exception
    {
        // Arrange
        deleteDeviceIfItExistsAlready(registryManager, deviceId);

        //-Create-//
        Device deviceAdded = Device.createDevice(deviceId, AuthenticationType.CERTIFICATE_AUTHORITY);
        Tools.addDeviceWithRetry(registryManager, deviceAdded);

        //-Read-//
        Device deviceRetrieved = registryManager.getDevice(deviceId);

        //-Update-//
        Device deviceUpdated = registryManager.getDevice(deviceId);
        deviceUpdated.setStatus(DeviceStatus.Disabled);
        deviceUpdated = registryManager.updateDevice(deviceUpdated);

        //-Delete-//
        registryManager.removeDevice(deviceId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), deviceId, deviceAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), deviceId, deviceRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), AuthenticationType.CERTIFICATE_AUTHORITY, deviceRetrieved.getAuthenticationType());
        assertEquals(buildExceptionMessage("", hostName), DeviceStatus.Disabled, deviceUpdated.getStatus());
        assertNull(buildExceptionMessage("", hostName), deviceAdded.getPrimaryThumbprint());
        assertNull(buildExceptionMessage("", hostName), deviceAdded.getSecondaryKey());
        assertNull(buildExceptionMessage("", hostName), deviceRetrieved.getPrimaryThumbprint());
        assertNull(buildExceptionMessage("", hostName), deviceRetrieved.getSecondaryThumbprint());
        assertTrue(buildExceptionMessage("", hostName), deviceWasDeletedSuccessfully(registryManager, deviceId));
    }

    @Test (timeout=MAX_TEST_MILLISECONDS)
    public void crud_device_e2e_X509_self_signed() throws Exception
    {
        // Arrange
        deleteDeviceIfItExistsAlready(registryManager, deviceId);

        //-Create-//
        Device deviceAdded = Device.createDevice(deviceId, AuthenticationType.SELF_SIGNED);
        deviceAdded.setThumbprintFinal(primaryThumbprint, secondaryThumbprint);
        Tools.addDeviceWithRetry(registryManager, deviceAdded);

        //-Read-//
        Device deviceRetrieved = registryManager.getDevice(deviceId);

        //-Update-//
        Device deviceUpdated = registryManager.getDevice(deviceId);
        deviceUpdated.setThumbprintFinal(primaryThumbprint2, secondaryThumbprint2);
        deviceUpdated = registryManager.updateDevice(deviceUpdated);

        //-Delete-//
        registryManager.removeDevice(deviceId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), deviceId, deviceAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), deviceId, deviceRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), AuthenticationType.SELF_SIGNED, deviceAdded.getAuthenticationType());
        assertEquals(buildExceptionMessage("", hostName), AuthenticationType.SELF_SIGNED, deviceRetrieved.getAuthenticationType());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint, deviceAdded.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint, deviceAdded.getSecondaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint, deviceRetrieved.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint, deviceRetrieved.getSecondaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint2, deviceUpdated.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint2, deviceUpdated.getSecondaryThumbprint());
        assertTrue(buildExceptionMessage("", hostName), deviceWasDeletedSuccessfully(registryManager, deviceId));
    }

    //TODO what is this testing?
    @Test
    public void getDeviceStatisticsTest() throws Exception
    {
        RegistryManager registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        Tools.getStatisticsWithRetry(registryManager);
    }

    @Test (timeout=MAX_TEST_MILLISECONDS)
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void crud_module_e2e() throws Exception
    {
        // Arrange
        SymmetricKey expectedSymmetricKey = new SymmetricKey();

        deleteDeviceIfItExistsAlready(registryManager, deviceForTest);
        Device deviceSetup = Device.createFromId(deviceForTest, DeviceStatus.Enabled, null);
        Tools.addDeviceWithRetry(registryManager, deviceSetup);
        deleteModuleIfItExistsAlready(registryManager, deviceForTest, moduleId);

        //-Create-//
        Module moduleAdded = Module.createFromId(deviceForTest, moduleId, null);
        Tools.addModuleWithRetry(registryManager, moduleAdded);

        //-Read-//
        Module moduleRetrieved = registryManager.getModule(deviceForTest, moduleId);

        //-Update-//
        Module moduleUpdated = registryManager.getModule(deviceForTest, moduleId);
        moduleUpdated.getSymmetricKey().setPrimaryKeyFinal(expectedSymmetricKey.getPrimaryKey());
        moduleUpdated = registryManager.updateModule(moduleUpdated);

        //-Delete-//
        registryManager.removeModule(deviceForTest, moduleId);
        registryManager.removeDevice(deviceForTest);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), deviceForTest, moduleAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), moduleId, moduleAdded.getId());
        assertEquals(buildExceptionMessage("", hostName), deviceForTest, moduleRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), moduleId, moduleRetrieved.getId());
        assertEquals(buildExceptionMessage("", hostName), expectedSymmetricKey.getPrimaryKey(), moduleUpdated.getPrimaryKey());
        assertTrue(buildExceptionMessage("", hostName), moduleWasDeletedSuccessfully(registryManager, deviceForTest, moduleId));
    }

    @Test (timeout=MAX_TEST_MILLISECONDS)
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void crud_module_e2e_X509_CA_signed() throws Exception
    {
        // Arrange
        deleteDeviceIfItExistsAlready(registryManager, deviceForTest);
        deleteModuleIfItExistsAlready(registryManager, deviceForTest, moduleId);
        Device deviceSetup = Device.createFromId(deviceForTest, DeviceStatus.Enabled, null);
        Tools.addDeviceWithRetry(registryManager, deviceSetup);
        deleteModuleIfItExistsAlready(registryManager, deviceForTest, moduleId);

        //-Create-//
        Module moduleAdded = Module.createModule(deviceForTest, moduleId, AuthenticationType.CERTIFICATE_AUTHORITY);
        Tools.addModuleWithRetry(registryManager, moduleAdded);

        //-Read-//
        Module moduleRetrieved = registryManager.getModule(deviceForTest, moduleId);

        //-Delete-//
        registryManager.removeModule(deviceForTest, moduleId);
        registryManager.removeDevice(deviceForTest);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), deviceForTest, moduleAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), moduleId, moduleAdded.getId());
        assertEquals(buildExceptionMessage("", hostName), deviceForTest, moduleRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), moduleId, moduleRetrieved.getId());
        assertNull(buildExceptionMessage("", hostName), moduleAdded.getPrimaryThumbprint());
        assertNull(buildExceptionMessage("", hostName), moduleAdded.getSecondaryThumbprint());
        assertNull(buildExceptionMessage("", hostName), moduleRetrieved.getPrimaryThumbprint());
        assertNull(buildExceptionMessage("", hostName), moduleRetrieved.getSecondaryThumbprint());
        assertTrue(buildExceptionMessage("", hostName), moduleWasDeletedSuccessfully(registryManager, deviceForTest, moduleId));
    }

    @Test (timeout=MAX_TEST_MILLISECONDS)
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void crud_module_e2e_X509_self_signed() throws Exception
    {
        // Arrange
        deleteDeviceIfItExistsAlready(registryManager, deviceForTest);
        Device deviceSetup = Device.createFromId(deviceForTest, DeviceStatus.Enabled, null);
        Tools.addDeviceWithRetry(registryManager, deviceSetup);
        deleteModuleIfItExistsAlready(registryManager, deviceForTest, moduleId);

        //-Create-//
        Module moduleAdded = Module.createModule(deviceForTest, moduleId, AuthenticationType.SELF_SIGNED);
        moduleAdded.setThumbprintFinal(primaryThumbprint, secondaryThumbprint);
        Tools.addModuleWithRetry(registryManager, moduleAdded);

        //-Read-//
        Module moduleRetrieved = registryManager.getModule(deviceForTest, moduleId);

        //-Update-//
        Module moduleUpdated = registryManager.getModule(deviceForTest, moduleId);
        moduleUpdated.setThumbprintFinal(primaryThumbprint2, secondaryThumbprint2);
        moduleUpdated = registryManager.updateModule(moduleUpdated);

        //-Delete-//
        registryManager.removeModule(deviceForTest, moduleId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), deviceForTest, moduleAdded.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), moduleId, moduleAdded.getId());
        assertEquals(buildExceptionMessage("", hostName), deviceForTest, moduleRetrieved.getDeviceId());
        assertEquals(buildExceptionMessage("", hostName), moduleId, moduleRetrieved.getId());
        assertEquals(buildExceptionMessage("", hostName), AuthenticationType.SELF_SIGNED, moduleAdded.getAuthenticationType());
        assertEquals(buildExceptionMessage("", hostName), AuthenticationType.SELF_SIGNED, moduleRetrieved.getAuthenticationType());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint, moduleAdded.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint, moduleAdded.getSecondaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint, moduleRetrieved.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint, moduleRetrieved.getSecondaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), primaryThumbprint2, moduleUpdated.getPrimaryThumbprint());
        assertEquals(buildExceptionMessage("", hostName), secondaryThumbprint2, moduleUpdated.getSecondaryThumbprint());
        assertTrue(buildExceptionMessage("", hostName), moduleWasDeletedSuccessfully(registryManager, deviceId, moduleId));
    }

    @Test (timeout=MAX_TEST_MILLISECONDS)
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void crud_adm_configuration_e2e() throws Exception
    {
        // Arrange
        deleteConfigurationIfItExistsAlready(registryManager, deviceId);
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
        Configuration configAdded = new Configuration(configId);

        ConfigurationContent content = new ConfigurationContent();
        content.setDeviceContent(testDeviceContent);
        configAdded.setContent(content);
        configAdded.getMetrics().setQueries(new HashMap<String, String>(){{put("waterSettingsPending",
                "SELECT deviceId FROM devices WHERE properties.reported.chillerWaterSettings.status=\'pending\'");}});
        configAdded.setTargetCondition("properties.reported.chillerProperties.model=\'4000x\'");
        configAdded.setPriority(20);
        registryManager.addConfiguration(configAdded);

        //-Read-//
        Configuration configRetrieved = registryManager.getConfiguration(configId);

        //-Update-//
        Configuration configUpdated = registryManager.getConfiguration(configId);
        configUpdated.setPriority(1);
        configUpdated = registryManager.updateConfiguration(configUpdated);

        //-Delete-//
        registryManager.removeConfiguration(configId);

        // Assert
        assertEquals(buildExceptionMessage("", hostName), configId, configAdded.getId());
        assertEquals(buildExceptionMessage("", hostName), configId, configRetrieved.getId());
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
        assertEquals(buildExceptionMessage("", hostName), configId, configUpdated.getId());
        assertEquals(buildExceptionMessage("", hostName), new Integer(1), configUpdated.getPriority());
        assertTrue(buildExceptionMessage("", hostName), configWasDeletedSuccessfully(registryManager, configId));
    }

    @Test (expected = IotHubBadFormatException.class)
    @ConditionalIgnoreRule.ConditionalIgnore(condition = StandardTierOnlyRule.class)
    public void apply_configuration_e2e() throws Exception
    {
        // Arrange
        deleteDeviceIfItExistsAlready(registryManager, deviceForTest);
        Device deviceSetup = Device.createFromId(deviceForTest, DeviceStatus.Enabled, null);
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
        registryManager.applyConfigurationContentOnDevice(deviceForTest, content);
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

    private void deleteConfigurationIfItExistsAlready(RegistryManager registryManager, String configId) throws IOException
    {
        try
        {
            registryManager.getConfiguration(configId);

            //if no exception yet, identity exists so it can be deleted
            try
            {
                registryManager.removeConfiguration(configId);
            }
            catch (IotHubException | IOException e)
            {
                System.out.println("Initialization failed, could not remove configuration" + configId);
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