// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class Tools {

    public static String retrieveEnvironmentVariableValue(String environmentVariableName) {
        String environmentVariableValue = System.getenv().get(environmentVariableName);
        if (isBlank(environmentVariableValue)) {
            environmentVariableValue = System.getProperty(environmentVariableName);
            if (isBlank(environmentVariableValue)) {
                throw new IllegalArgumentException("Environment variable is not set: " + environmentVariableName);
            }
        }

        return environmentVariableValue;
    }
}
