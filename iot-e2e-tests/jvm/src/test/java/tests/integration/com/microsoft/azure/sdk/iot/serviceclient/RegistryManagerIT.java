/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.serviceclient;

import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.DeviceStatus;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.storage.StorageException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.UUID;

import static org.junit.Assert.*;

public class RegistryManagerIT
{
    private static String iotHubonnectionStringEnvVarName = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";
    private static String deviceId = "java-crud-e2e-test";
    private static RegistryManager registryManager;
    private static final String primaryThumbprint =   "0000000000000000000000000000000000000000";
    private static final String secondaryThumbprint = "1111111111111111111111111111111111111111";
    private static final String primaryThumbprint2 =   "2222222222222222222222222222222222222222";
    private static final String secondaryThumbprint2 = "3333333333333333333333333333333333333333";

    @BeforeClass
    public static void setUp() throws URISyntaxException, InvalidKeyException, StorageException, IOException
    {
        if (System.getenv().containsKey(iotHubonnectionStringEnvVarName))
        {
            iotHubConnectionString = System.getenv().get(iotHubonnectionStringEnvVarName);
        }
        else
        {
            throw new IllegalArgumentException("Environment variable is not set: " + iotHubonnectionStringEnvVarName);
        }

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        deviceId = deviceId.concat("-" + UUID.randomUUID());
    }

    @Test
    public void crud_e2e() throws Exception
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
    public void crud_e2e_X509_CA_signed() throws Exception
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
    public void crud_e2e_X509_self_signed() throws Exception
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
}