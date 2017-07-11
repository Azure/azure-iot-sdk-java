/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/** Manages device twin operations on IotHub */
public class DeviceTwinSample
{
    public static final String iotHubConnectionString = "[IOT HUB Connection String]";
    public static final String deviceId = "[Device ID]";

    /**
     * Manages device twin operations on IotHub
     * @throws Exception Throws Exception if sample fails
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting sample...");
        DeviceTwin twinClient = DeviceTwin.createFromConnectionString(iotHubConnectionString);

        DeviceTwinDevice device = new DeviceTwinDevice(deviceId);

        try
        {
            // Manage complete twin
            // ============================== get initial twin properties =============================
            System.out.println("Getting device twin");
            twinClient.getTwin(device);
            System.out.println(device);

            //Update Twin Tags and Desired Properties
            Set<Pair> tags = new HashSet<Pair>();
            tags.add(new Pair("HomeID", UUID.randomUUID()));
            device.setTags(tags);

            // ============================== change desired property =============================
            Set<Pair> desProp = new HashSet<Pair>();
            int temp = new Random().nextInt(100);
            int hum = new Random().nextInt(100);
            desProp.add(new Pair("temp", temp));
            desProp.add(new Pair("hum", hum));
            device.setDesiredProperties(desProp);

            System.out.println("Updating device twin (new temp, hum)");
            twinClient.updateTwin(device);

            System.out.println("Getting device twin");
            twinClient.getTwin(device);
            System.out.println(device);

            // ============================== remove desired property =============================
            desProp.clear();
            desProp.add(new Pair("hum", null));
            device.setDesiredProperties(desProp);
            System.out.println("Updating device twin (remove hum)");
            twinClient.updateTwin(device);

            System.out.println("Getting device twin");
            twinClient.getTwin(device);
            System.out.println(device);

            //Query twin
            System.out.println("Started Querying twin");

            SqlQuery sqlQuery = SqlQuery.createSqlQuery("*", SqlQuery.FromType.DEVICES, null, null);

            Query twinQuery = twinClient.queryTwin(sqlQuery.getQuery(), 3);

            while (twinClient.hasNextDeviceTwin(twinQuery))
            {
                DeviceTwinDevice d = twinClient.getNextDeviceTwin(twinQuery);
                System.out.println(d);
            }
        }
        catch (IotHubException e)
        {
            System.out.println(e.getMessage());
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
        
        System.out.println("Shutting down sample...");
    }
    

}
