/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.registry.DeviceStatus;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;

/** Manages device on IotHub - CRUD operations */
public class DeviceManagerX509Sample
{
    //x509 authenticated devices are either self signed or certificate authority signed. Use this boolean to choose which kind to use in this sample
    @SuppressWarnings("CanBeFinal")
    static boolean isSelfSigned = false;

    /**
     * A simple sample for doing CRUD operations involving X509 authenticated devices
     * @param args unused
     * @throws Exception if any exception occurs
     */
    public static void main(String[] args) throws Exception
    {
        //Uncomment the next line to use Self Signed authentication instead of certificate authority signed
        //isSelfSigned = true;

        //Connection strings and thumbprints will need to be set in SampleUtils.java file
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
        RegistryClient registryClient = new RegistryClient(SampleUtils.iotHubConnectionString);

        Device device;

        if (isSelfSigned)
        {
            device = new Device(SampleUtils.deviceId, AuthenticationType.SELF_SIGNED);
        }
        else
        {
            device = new Device(SampleUtils.deviceId, AuthenticationType.CERTIFICATE_AUTHORITY);
        }

        try
        {
            device = registryClient.addDevice(device);

            System.out.println("Device created: " + device.getDeviceId());
            if (isSelfSigned)
            {
                System.out.println("Device primary thumbprint: " + device.getPrimaryThumbprint());
                System.out.println("Device secondary thumbprint: " + device.getSecondaryThumbprint());
            }
        }
        catch (IotHubException | IOException iote)
        {
            iote.printStackTrace();
        }
    }

    private static void GetDevice()
    {
        RegistryClient registryClient = new RegistryClient(SampleUtils.iotHubConnectionString);

        Device returnDevice;
        try
        {
            returnDevice = registryClient.getDevice(SampleUtils.deviceId);

            System.out.println("Device: " + returnDevice.getDeviceId());
            System.out.println("Device eTag: " + returnDevice.getETag());

            if (isSelfSigned)
            {
                System.out.println("Device primary thumbprint: " + returnDevice.getPrimaryThumbprint());
                System.out.println("Device secondary thumbprint: " + returnDevice.getSecondaryThumbprint());
            }
        }
        catch (IotHubException | IOException iote)
        {
            iote.printStackTrace();
        }
    }

    private static void UpdateDevice()
    {
        RegistryClient registryClient = new RegistryClient(SampleUtils.iotHubConnectionString);

        Device device;
        if (isSelfSigned)
        {
            device = new Device(SampleUtils.deviceId, AuthenticationType.SELF_SIGNED);
        }
        else
        {
            device = new Device(SampleUtils.deviceId, AuthenticationType.CERTIFICATE_AUTHORITY);
        }

        device.setStatus(DeviceStatus.Disabled);

        try
        {
            device = registryClient.updateDevice(device);

            System.out.println("Device updated: " + device.getDeviceId());

            if (isSelfSigned)
            {
                System.out.println("Device primary thumbprint: " + device.getPrimaryThumbprint());
                System.out.println("Device secondary thumbprint: " + device.getSecondaryThumbprint());
            }
        }
        catch (IotHubException | IOException iote)
        {
            iote.printStackTrace();
        }
    }

    private static void RemoveDevice()
    {
        RegistryClient registryClient = new RegistryClient(SampleUtils.iotHubConnectionString);

        try
        {
            registryClient.removeDevice(SampleUtils.deviceId);

            System.out.println("Device removed: " + SampleUtils.deviceId);
        }
        catch (IotHubException | IOException iote)
        {
            iote.printStackTrace();
        }
    }
}
