// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Tools
{
    private static final Map<String, String> ANDROID_ENV_VAR = retrieveAndroidEnvVariables();

    private static final String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static final String IS_BASIC_TIER_HUB_ENV_VAR_NAME = "IS_BASIC_TIER_HUB";

    public static final String iotHubConnectionString = retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
    public static final boolean isBasicTierHub = Boolean.parseBoolean(retrieveEnvironmentVariableValue(IS_BASIC_TIER_HUB_ENV_VAR_NAME));

    private static final String ANDROID_BUILD_CONFIG_CLASS = "com.iothub.azure.microsoft.com.androide2e.test.BuildConfig";

    public static String retrieveEnvironmentVariableValue(String environmentVariableName)
    {
        String environmentVariableValue;

        if (ANDROID_ENV_VAR.containsKey(environmentVariableName))
        {
            environmentVariableValue = ANDROID_ENV_VAR.get(environmentVariableName);
        }
        else
        {
            environmentVariableValue = System.getenv().get(environmentVariableName);
            if ((environmentVariableValue == null) || environmentVariableValue.isEmpty())
            {
                environmentVariableValue = System.getProperty(environmentVariableName);
            }
        }

        return environmentVariableValue;
    }

    public static Map<String, String> retrieveAndroidEnvVariables()
    {
        Map<String, String> envVariables = new HashMap<>();
        try
        {
            Class buildConfig = Class.forName(ANDROID_BUILD_CONFIG_CLASS);
            Arrays.stream(buildConfig.getFields()).forEach(field -> {
                try
                {
                    envVariables.put(field.getName(), field.get(null).toString());
                }
                catch (IllegalAccessException e)
                {
                    log.error("Cannot access the following field: {}", field.getName(), e);
                }
            });
        }
        catch (ClassNotFoundException e)
        {
            log.debug("Likely running the JVM tests, ignoring ClassNotFoundException\n");
        }

        return envVariables;
    }
}