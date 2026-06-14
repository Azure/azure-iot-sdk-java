// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Tools
{
    private static String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static final String IS_BASIC_TIER_HUB_ENV_VAR_NAME = "IS_BASIC_TIER_HUB";

    public static final String iotHubConnectionString = retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
    public static final boolean isBasicTierHub = Boolean.parseBoolean(retrieveEnvironmentVariableValue(IS_BASIC_TIER_HUB_ENV_VAR_NAME));

    public static String retrieveEnvironmentVariableValue(String environmentVariableName)
    {
        String environmentVariableValue;

        environmentVariableValue = System.getenv().get(environmentVariableName);
        if ((environmentVariableValue == null) || environmentVariableValue.isEmpty())
        {
            environmentVariableValue = System.getProperty(environmentVariableName);
        }

        return environmentVariableValue;
    }

    public static String getHostName(String iotHubConnectionString)
    {
        return IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString).getHostName();
    }

    public static String getDeviceConnectionString(String iothubConnectionString, Device device)
    {
        if (device == null)
        {
            throw new IllegalArgumentException("device cannot be null");
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("HostName=%s;", getHostName(iothubConnectionString)));
        stringBuilder.append(String.format("DeviceId=%s;", device.getDeviceId()));
        if (device.getPrimaryKey() == null)
        {
            //self signed or CA signed
            stringBuilder.append("x509=true");
        }
        else
        {
            stringBuilder.append(String.format("SharedAccessKey=%s", device.getPrimaryKey()));
        }
        return stringBuilder.toString();
    }
}
