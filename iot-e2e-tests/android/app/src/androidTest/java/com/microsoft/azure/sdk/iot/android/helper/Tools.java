/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.sdk.iot.android.helper;

import android.os.Bundle;
import android.util.Log;

public class Tools
{
    public static String retrieveEnvironmentVariableValue(String envVarName, Bundle bundle)
    {
        if (bundle.containsKey(envVarName))
        {
            String envValue = bundle.getString(envVarName);
            Log.d("Test Log", envVarName + ": " + envValue);
            return envValue;
        }
        else
        {
            Log.d("Test Log", envVarName + " env was not found in extras");
            return null;
        }
    }
}