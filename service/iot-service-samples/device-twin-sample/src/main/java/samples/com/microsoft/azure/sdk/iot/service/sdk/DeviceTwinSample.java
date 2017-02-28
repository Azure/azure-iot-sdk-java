/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot.service.sdk;

import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;
import java.net.URISyntaxException;

/** Manages device twin operations on IotHub */
public class DeviceTwinSample
{
    public static final String iotHubConnectionString = "[IOT HUB Connection String]";
    public static final String deviceId = "[Device ID]";

    /**
     * @param args
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting sample...");
        DeviceTwin twinClient = DeviceTwin.createFromConnectionString(iotHubConnectionString);

        DeviceTwinDevice device = new DeviceTwinDevice(deviceId);

        try
        {
            // Manage complete twin
            System.out.println("Getting device twin");
            twinClient.getTwin(device);
            System.out.println(device);
        }
        catch (IotHubException e)
        {
            System.out.println(e.getMessage());
        }
        
        System.out.println("Shutting down sample...");
    }
    

}
