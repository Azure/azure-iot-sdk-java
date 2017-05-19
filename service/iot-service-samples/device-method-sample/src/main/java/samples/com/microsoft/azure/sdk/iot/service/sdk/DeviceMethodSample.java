/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethod;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Manages device Method operations on IotHub */
public class DeviceMethodSample
{
    public static final String iotHubConnectionString = "[IOT HUB Connection String]";
    public static final String deviceId = "[Device ID]";

    public static final String methodName = "[Function Name]";
    public static final Long responseTimeout = TimeUnit.SECONDS.toSeconds(200);
    public static final Long connectTimeout = TimeUnit.SECONDS.toSeconds(5);
    public static final Map<String, Object> payload = new HashMap<String, Object>()
    {
        {
            put("arg1", "value1");
            put("arg2", 20);
        }
    };

    /**
     * @param args
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting sample...");
        DeviceMethod methodClient = DeviceMethod.createFromConnectionString(iotHubConnectionString);

        try
        {
            // Manage complete Method
            System.out.println("Getting device Method");
            MethodResult result = methodClient.invoke(deviceId, methodName, responseTimeout, connectTimeout, payload);
            if(result == null)
            {
                throw new IOException("Method invoke returns null");
            }
            System.out.println("Status=" + result.getStatus());
            System.out.println("Payload=" + result.getPayload());
        }
        catch (IotHubException e)
        {
            System.out.println(e.getMessage());
        }
        
        System.out.println("Shutting down sample...");
    }
    

}
