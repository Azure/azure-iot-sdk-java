/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.serviceclient;

import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubBadFormatException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.storage.StorageException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.*;

public class RegistryManagerCommon
{
    protected static String iotHubConnectionString = "";
    private static String deviceId = "java-crud-e2e-test";
    private static String deviceForTest = "deviceForTest";
    private static String moduleId = "java-crud-module-e2e-test";
    private static String configId = "java-crud-adm-e2e-test";
    private static RegistryManager registryManager;
    private static final String primaryThumbprint =   "0000000000000000000000000000000000000000";
    private static final String secondaryThumbprint = "1111111111111111111111111111111111111111";
    private static final String primaryThumbprint2 =   "2222222222222222222222222222222222222222";
    private static final String secondaryThumbprint2 = "3333333333333333333333333333333333333333";

    public static void setUp() throws IOException
    {
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        deviceId = deviceId.concat("-" + UUID.randomUUID());
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

    @Test
    public void crud_device_e2e() throws Exception
    {
        // Arrange
        deleteDeviceIfItExistsAlready(registryManager, deviceId);

        //-Create-//
        Device deviceAdded = Device.createFromId(deviceId, DeviceStatus.Enabled, null);
        registryManager.addDevice(deviceAdded);

        //-Read-//
        Device deviceRetrieved = registryManager.getDevice(deviceId);

        //-Update-//
        Device deviceUpdated = registryManager.getDevice(deviceId);
        deviceUpdated.setStatus(DeviceStatus.Disabled);
        deviceUpdated = registryManager.updateDevice(deviceUpdated);

        //-Delete-//
        registryManager.removeDevice(deviceId);

        // Assert
        assertEquals(deviceId, deviceAdded.getDeviceId());
        assertEquals(deviceId, deviceRetrieved.getDeviceId());
        assertEquals(DeviceStatus.Disabled, deviceUpdated.getStatus());
        assertTrue(deviceWasDeletedSuccessfully(registryManager, deviceId));
    }

    @Test
    public void crud_device_e2e_X509_CA_signed() throws Exception
    {
        // Arrange
        deleteDeviceIfItExistsAlready(registryManager, deviceId);

        //-Create-//
        Device deviceAdded = Device.createDevice(deviceId, AuthenticationType.CERTIFICATE_AUTHORITY);
        registryManager.addDevice(deviceAdded);

        //-Read-//
        Device deviceRetrieved = registryManager.getDevice(deviceId);

        //-Update-//
        Device deviceUpdated = registryManager.getDevice(deviceId);
        deviceUpdated.setStatus(DeviceStatus.Disabled);
        deviceUpdated = registryManager.updateDevice(deviceUpdated);

        //-Delete-//
        registryManager.removeDevice(deviceId);

        // Assert
        assertEquals(deviceId, deviceAdded.getDeviceId());
        assertEquals(deviceId, deviceRetrieved.getDeviceId());
        assertEquals(AuthenticationType.CERTIFICATE_AUTHORITY, deviceRetrieved.getAuthenticationType());
        assertEquals(DeviceStatus.Disabled, deviceUpdated.getStatus());
        assertNull(deviceAdded.getPrimaryThumbprint());
        assertNull(deviceAdded.getSecondaryKey());
        assertNull(deviceRetrieved.getPrimaryThumbprint());
        assertNull(deviceRetrieved.getSecondaryThumbprint());
        assertTrue(deviceWasDeletedSuccessfully(registryManager, deviceId));
    }

    @Test
    public void crud_device_e2e_X509_self_signed() throws Exception
    {
        // Arrange
        deleteDeviceIfItExistsAlready(registryManager, deviceId);

        //-Create-//
        Device deviceAdded = Device.createDevice(deviceId, AuthenticationType.SELF_SIGNED);
        deviceAdded.setThumbprint(primaryThumbprint, secondaryThumbprint);
        registryManager.addDevice(deviceAdded);

        //-Read-//
        Device deviceRetrieved = registryManager.getDevice(deviceId);

        //-Update-//
        Device deviceUpdated = registryManager.getDevice(deviceId);
        deviceUpdated.setThumbprint(primaryThumbprint2, secondaryThumbprint2);
        deviceUpdated = registryManager.updateDevice(deviceUpdated);

        //-Delete-//
        registryManager.removeDevice(deviceId);

        // Assert
        assertEquals(deviceId, deviceAdded.getDeviceId());
        assertEquals(deviceId, deviceRetrieved.getDeviceId());
        assertEquals(AuthenticationType.SELF_SIGNED, deviceAdded.getAuthenticationType());
        assertEquals(AuthenticationType.SELF_SIGNED, deviceRetrieved.getAuthenticationType());
        assertEquals(primaryThumbprint, deviceAdded.getPrimaryThumbprint());
        assertEquals(secondaryThumbprint, deviceAdded.getSecondaryThumbprint());
        assertEquals(primaryThumbprint, deviceRetrieved.getPrimaryThumbprint());
        assertEquals(secondaryThumbprint, deviceRetrieved.getSecondaryThumbprint());
        assertEquals(primaryThumbprint2, deviceUpdated.getPrimaryThumbprint());
        assertEquals(secondaryThumbprint2, deviceUpdated.getSecondaryThumbprint());
        assertTrue(deviceWasDeletedSuccessfully(registryManager, deviceId));
    }

    @Test
    public void getDeviceStatisticsTest() throws Exception
    {
        RegistryManager registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        registryManager.getStatistics();
    }

    @Test
    public void crud_module_e2e() throws Exception
    {
        // Arrange
        SymmetricKey expectedSymmetricKey = new SymmetricKey();

        deleteDeviceIfItExistsAlready(registryManager, deviceForTest);
        Device deviceSetup = Device.createFromId(deviceForTest, DeviceStatus.Enabled, null);
        registryManager.addDevice(deviceSetup);
        deleteModuleIfItExistsAlready(registryManager, deviceForTest, moduleId);

        //-Create-//
        Module moduleAdded = Module.createFromId(deviceForTest, moduleId, null);
        registryManager.addModule(moduleAdded);

        //-Read-//
        Module moduleRetrieved = registryManager.getModule(deviceForTest, moduleId);

        //-Update-//
        Module moduleUpdated = registryManager.getModule(deviceForTest, moduleId);
        moduleUpdated.getSymmetricKey().setPrimaryKey(expectedSymmetricKey.getPrimaryKey());
        moduleUpdated = registryManager.updateModule(moduleUpdated);

        //-Delete-//
        registryManager.removeModule(deviceForTest, moduleId);
        registryManager.removeDevice(deviceForTest);

        // Assert
        assertEquals(deviceForTest, moduleAdded.getDeviceId());
        assertEquals(moduleId, moduleAdded.getId());
        assertEquals(deviceForTest, moduleRetrieved.getDeviceId());
        assertEquals(moduleId, moduleRetrieved.getId());
        assertEquals(expectedSymmetricKey.getPrimaryKey(), moduleUpdated.getPrimaryKey());
        assertTrue(moduleWasDeletedSuccessfully(registryManager, deviceForTest, moduleId));
    }

    @Test
    public void crud_module_e2e_X509_CA_signed() throws Exception
    {
        // Arrange
        deleteDeviceIfItExistsAlready(registryManager, deviceForTest);
        deleteModuleIfItExistsAlready(registryManager, deviceForTest, moduleId);
        Device deviceSetup = Device.createFromId(deviceForTest, DeviceStatus.Enabled, null);
        registryManager.addDevice(deviceSetup);
        deleteModuleIfItExistsAlready(registryManager, deviceForTest, moduleId);

        //-Create-//
        Module moduleAdded = Module.createModule(deviceForTest, moduleId, AuthenticationType.CERTIFICATE_AUTHORITY);
        registryManager.addModule(moduleAdded);

        //-Read-//
        Module moduleRetrieved = registryManager.getModule(deviceForTest, moduleId);

        //-Delete-//
        registryManager.removeModule(deviceForTest, moduleId);
        registryManager.removeDevice(deviceForTest);

        // Assert
        assertEquals(deviceForTest, moduleAdded.getDeviceId());
        assertEquals(moduleId, moduleAdded.getId());
        assertEquals(deviceForTest, moduleRetrieved.getDeviceId());
        assertEquals(moduleId, moduleRetrieved.getId());
        assertNull(moduleAdded.getPrimaryThumbprint());
        assertNull(moduleAdded.getSecondaryThumbprint());
        assertNull(moduleRetrieved.getPrimaryThumbprint());
        assertNull(moduleRetrieved.getSecondaryThumbprint());
        assertTrue(moduleWasDeletedSuccessfully(registryManager, deviceForTest, moduleId));
    }

    @Test
    public void crud_module_e2e_X509_self_signed() throws Exception
    {
        // Arrange
        deleteDeviceIfItExistsAlready(registryManager, deviceForTest);
        Device deviceSetup = Device.createFromId(deviceForTest, DeviceStatus.Enabled, null);
        registryManager.addDevice(deviceSetup);
        deleteModuleIfItExistsAlready(registryManager, deviceForTest, moduleId);

        //-Create-//
        Module moduleAdded = Module.createModule(deviceForTest, moduleId, AuthenticationType.SELF_SIGNED);
        moduleAdded.setThumbprint(primaryThumbprint, secondaryThumbprint);
        registryManager.addModule(moduleAdded);

        //-Read-//
        Module moduleRetrieved = registryManager.getModule(deviceForTest, moduleId);

        //-Update-//
        Module moduleUpdated = registryManager.getModule(deviceForTest, moduleId);
        moduleUpdated.setThumbprint(primaryThumbprint2, secondaryThumbprint2);
        moduleUpdated = registryManager.updateModule(moduleUpdated);

        //-Delete-//
        registryManager.removeModule(deviceForTest, moduleId);

        // Assert
        assertEquals(deviceForTest, moduleAdded.getDeviceId());
        assertEquals(moduleId, moduleAdded.getId());
        assertEquals(deviceForTest, moduleRetrieved.getDeviceId());
        assertEquals(moduleId, moduleRetrieved.getId());
        assertEquals(AuthenticationType.SELF_SIGNED, moduleAdded.getAuthenticationType());
        assertEquals(AuthenticationType.SELF_SIGNED, moduleRetrieved.getAuthenticationType());
        assertEquals(primaryThumbprint, moduleAdded.getPrimaryThumbprint());
        assertEquals(secondaryThumbprint, moduleAdded.getSecondaryThumbprint());
        assertEquals(primaryThumbprint, moduleRetrieved.getPrimaryThumbprint());
        assertEquals(secondaryThumbprint, moduleRetrieved.getSecondaryThumbprint());
        assertEquals(primaryThumbprint2, moduleUpdated.getPrimaryThumbprint());
        assertEquals(secondaryThumbprint2, moduleUpdated.getSecondaryThumbprint());
        assertTrue(moduleWasDeletedSuccessfully(registryManager, deviceId, moduleId));
    }

    @Test
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
        assertEquals(configId, configAdded.getId());
        assertEquals(configId, configRetrieved.getId());
        assertEquals("{temperature=66.0, pressure=28.0}",
                configRetrieved.getContent().getDeviceContent().get("properties.desired.chiller-water").toString());
        assertEquals("SELECT deviceId FROM devices WHERE properties.reported.chillerWaterSettings.status=\'pending\'",
                configRetrieved.getMetrics().getQueries().get("waterSettingsPending"));
        assertEquals("properties.reported.chillerProperties.model=\'4000x\'",
                configRetrieved.getTargetCondition());
        assertEquals(new Integer(20), configRetrieved.getPriority());
        assertEquals(configId, configUpdated.getId());
        assertEquals(new Integer(1), configUpdated.getPriority());
        assertTrue(configWasDeletedSuccessfully(registryManager, configId));
    }

    @Test(expected = IotHubBadFormatException.class)
    public void apply_configuration_e2e() throws Exception
    {
        // Arrange
        deleteDeviceIfItExistsAlready(registryManager, deviceForTest);
        Device deviceSetup = Device.createFromId(deviceForTest, DeviceStatus.Enabled, null);
        registryManager.addDevice(deviceSetup);
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


    private void deleteDeviceIfItExistsAlready(RegistryManager registryManager, String deviceId) throws IOException
    {
        try
        {
            registryManager.getDevice(deviceId);

            //if no exception yet, device exists so it can be deleted
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

    private void deleteModuleIfItExistsAlready(RegistryManager registryManager, String deviceId, String moduleId) throws IOException
    {
        try
        {
            registryManager.getModule(deviceId, moduleId);

            //if no exception yet, device exists so it can be deleted
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

            //if no exception yet, device exists so it can be deleted
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
            // device should have been deleted, so this catch is expected
            return true;
        }

        // device could still be retrieved, so it was not deleted successfully
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