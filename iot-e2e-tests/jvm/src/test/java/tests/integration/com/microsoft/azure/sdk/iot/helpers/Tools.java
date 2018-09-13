/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

public class Tools
{
    public static String retrieveEnvironmentVariableValue(String environmentVariableName)
    {
        String environmentVariableValue = System.getenv().get(environmentVariableName);
        if ((environmentVariableValue == null) || environmentVariableValue.isEmpty())
        {
            environmentVariableValue = System.getProperty(environmentVariableName);
            if (environmentVariableValue == null || environmentVariableValue.isEmpty())
            {
                throw new IllegalArgumentException("Environment variable is not set: " + environmentVariableName);
            }
        }

        return environmentVariableValue;
    }
}