/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.registry.Module;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;

public class DeviceConnectionString
{
    public static String get(String iotHubConnectionString, Device device)
    {
        StringBuilder stringBuilder = new StringBuilder();
        String[] tokenArray = iotHubConnectionString.split(";");
        String hostName = "";
        for (String token : tokenArray)
        {
            String[] keyValueArray = token.split("=");
            if (keyValueArray[0].equals("HostName"))
            {
                hostName = token + ';';
                break;
            }
        }

        stringBuilder.append(hostName);
        stringBuilder.append(String.format("DeviceId=%s", device.getDeviceId()));

        if (device.getAuthenticationType() == AuthenticationType.SAS)
        {
            stringBuilder.append(String.format(";SharedAccessKey=%s", device.getPrimaryKey()));
        }
        else
        {
            stringBuilder.append(";x509=true");
        }

        return stringBuilder.toString();
    }

    public static String get(String iotHubConnectionString, Device device, Module module)
    {
        StringBuilder stringBuilder = new StringBuilder();
        String[] tokenArray = iotHubConnectionString.split(";");
        String hostName = "";
        for (String token : tokenArray)
        {
            String[] keyValueArray = token.split("=");
            if (keyValueArray[0].equals("HostName"))
            {
                hostName = token + ';';
                break;
            }
        }

        stringBuilder.append(hostName);
        stringBuilder.append(String.format("DeviceId=%s", device.getDeviceId()));

        if (device.getAuthenticationType() == AuthenticationType.SAS)
        {
            stringBuilder.append(String.format(";SharedAccessKey=%s", device.getPrimaryKey()));
        }
        else
        {
            stringBuilder.append(";x509=true");
        }

        stringBuilder.append(";ModuleId=").append(module.getId());

        return stringBuilder.toString();
    }
}
