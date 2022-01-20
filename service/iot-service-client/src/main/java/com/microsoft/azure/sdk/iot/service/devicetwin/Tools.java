/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

/**
 * Collection of static helper functions
 */
public class Tools
{
    /**
     * Helper function to check if the input string is null or empty
     *
     * @param value The string to check
     * @return The value true if the input string is empty or null
     */
    public static Boolean isNullOrEmpty(String value)
    {
        boolean retVal;

        if (value == null)
            // Codes_SRS_SDK_JAVA_TOOLS_12_001: [The function shall return true if the input is null]
            retVal = true;
        else
            // Codes_SRS_SDK_JAVA_TOOLS_12_002: [The function shall return true if the input string’s length is zero]
            // Codes_SRS_SDK_JAVA_TOOLS_12_003: [The function shall return false otherwise]
            retVal = value.length() == 0;

        return retVal;
    }
}
