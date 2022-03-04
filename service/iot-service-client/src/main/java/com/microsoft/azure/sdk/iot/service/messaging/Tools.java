/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.messaging;

/**
 * Collection of static helper functions
 */
class Tools
{
    /**
     * Helper function to check if the input string is null or empty
     *
     * @param value The string to check
     * @return The value true if the input string is empty or null
     */
    static Boolean isNullOrEmpty(String value)
    {
        boolean retVal;
        
        if (value == null)
            retVal = true;
        else
            retVal = value.length() == 0;
        
        return retVal;
    }

    /**
     * Helper function to check if the input string is null or contains only whitespace(s)
     *
     * @param value The string to check
     * @return The value true if the input string is empty or contains only whitespace(s)
     */
    static Boolean isNullOrWhiteSpace(String value)
    {
        boolean retVal;
        
        if (value == null)
        {
            retVal = true;
        }
        else
        {
            retVal = isNullOrEmpty(value.trim());
        }
        return retVal;
    }
}
