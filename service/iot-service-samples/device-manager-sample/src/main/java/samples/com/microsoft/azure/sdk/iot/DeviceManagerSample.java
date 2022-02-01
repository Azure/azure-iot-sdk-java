/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;

/** Manages device on IotHub - CRUD operations */
public class DeviceManagerSample
{
    /**
     * A simple sample for doing CRUD operations
     * @param args unused
     * @throws Exception if any exception is thrown
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting sample...");
        
        System.out.println("Add Device started");
        AddDevice();
        System.out.println("Add Device finished");

        System.out.println("Get Device started");
        GetDevice();
        System.out.println("Get Device finished");

        System.out.println("Update Device started");
        UpdateDevice();
        System.out.println("Update Device finished");

        System.out.println("Remove Device started");
        RemoveDevice();
        System.out.println("Remove Device finished");
        
        System.out.println("Shutting down sample...");
    }
    
    private static void AddDevice()
    {
        RegistryManager registryManager = new RegistryManager(SampleUtils.iotHubConnectionString);

        Device device = Device.createFromId(SampleUtils.deviceId, null, null);
        try
        {
            device = registryManager.addDevice(device);

            System.out.println("Device created: " + device.getDeviceId());
            System.out.println("Device key: " + device.getPrimaryKey());
        }
        catch (IotHubException | IOException iote)
        {
            iote.printStackTrace();
        }
    }
    
    private static void GetDevice()
    {
        RegistryManager registryManager = new RegistryManager(SampleUtils.iotHubConnectionString);
        
        Device returnDevice;
        try
        {
            returnDevice = registryManager.getDevice(SampleUtils.deviceId);

            System.out.println("Device: " + returnDevice.getDeviceId());
            System.out.println("Device primary key: " + returnDevice.getPrimaryKey());
            System.out.println("Device secondary key: " + returnDevice.getSecondaryKey());
            System.out.println("Device ETag: " + returnDevice.geteTag());
        }
        catch (IotHubException | IOException iote)
        {
            iote.printStackTrace();
        }
    }
    
    private static void UpdateDevice()
    {
        RegistryManager registryManager = new RegistryManager(SampleUtils.iotHubConnectionString);

        // Create an Edge device, and set leaf-device as a child.
        Device edge = Device.createFromId(SampleUtils.edgeId, null, null);
        DeviceCapabilities capabilities = new DeviceCapabilities();
        capabilities.setIotEdge(true);
        edge.setCapabilities(capabilities);

        try
        {
            edge = registryManager.addDevice(edge);

            // Set Edge device as a parent by getting its scope and adding it to the device's device scope.
            Device device = registryManager.getDevice(SampleUtils.deviceId);
            device.setScope(edge.getScope());
            device = registryManager.updateDevice(device);

            System.out.println("Device updated: " + device.getDeviceId());
            System.out.println("Device scope: " + device.getScope());
            System.out.println("Device parent: " + device.getParentScopes().get(0));
        }
        catch (IotHubException | IOException iote)
        {
            iote.printStackTrace();
        }
    }
    
    private static void RemoveDevice()
    {
        RegistryManager registryManager = new RegistryManager(SampleUtils.iotHubConnectionString);
        
        try
        {
            registryManager.removeDevice(SampleUtils.deviceId);
            System.out.println("Device removed: " + SampleUtils.deviceId);

            registryManager.removeDevice(SampleUtils.edgeId);
            System.out.println("Edge removed: " + SampleUtils.edgeId);
        }
        catch (IotHubException | IOException iote)
        {
            iote.printStackTrace();
        }
    }
}
