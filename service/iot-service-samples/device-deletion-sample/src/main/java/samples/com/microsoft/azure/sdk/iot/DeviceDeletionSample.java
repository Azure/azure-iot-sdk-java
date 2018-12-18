/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeviceDeletionSample
{
    /**
     * A simple sample for deleting all devices from an iothub
     */
    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            System.out.format(
                    "Expected 2 or 3 arguments but received: %d.\n"
                            + "The program should be called with the following args: \n"
                            + "1. [Device connection string] - String containing Hostname & Device Key in the following formats: HostName=<hostname>;SharedAccessKeyName=<your shared access key's name>;SharedAccessKey=<Your iot hub's shared access key>\n",
                    args.length);
            return;
        }

        String connString = args[0];
        RegistryManager registryManager = null;
        try
        {
            registryManager = RegistryManager.createFromConnectionString(connString);
        }
        catch (IOException e)
        {
            throw new IOException("Could not create registry manager from the provided connection string", e);
        }

        System.out.println("Querying ");
        DeviceTwin deviceTwin = null;
        try
        {
            deviceTwin = DeviceTwin.createFromConnectionString(connString);
        }
        catch (Exception e)
        {
            throw new IOException("Could not create device twin client from the provided connection string", e);
        }

        Query query = null;
        try
        {
            query = deviceTwin.queryTwin("SELECT * FROM Devices", 100);
        }
        catch (Exception e)
        {
            throw new IOException("Could not execute the query on your iot hub to retrieve the device list", e);
        }

        List<String> deviceIdsToRemove = new ArrayList<>();
        try
        {
            while (deviceTwin.hasNextDeviceTwin(query))
            {
                DeviceTwinDevice device = deviceTwin.getNextDeviceTwin(query);
                deviceIdsToRemove.add(device.getDeviceId());
            }
        }
        catch (Exception e)
        {
            throw new IOException("Could not collect the full list of device ids to delete", e);
        }

        int deletedDeviceCount = 0;
        for (String deviceIdToRemove : deviceIdsToRemove)
        {
            try
            {
                registryManager.removeDevice(deviceIdToRemove);
                deletedDeviceCount++;
            }
            catch (Exception e)
            {
                System.out.println("Could not remove device with id " +deviceIdToRemove);
                e.printStackTrace();

                System.out.println("Moving onto deleting the remaining devices anyways...");
            }
        }

        System.out.println("Deleted " + deletedDeviceCount + " out of the total " + deviceIdsToRemove.size() + " devices");

        registryManager.close();
    }
}
