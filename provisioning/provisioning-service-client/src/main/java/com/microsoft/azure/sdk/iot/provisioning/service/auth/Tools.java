/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.auth;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Map;

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
     * Helper function to get a value from the given Map if the key name exists
     *
     * @param map The Map object to get the value from
     * @param keyName The name of the key
     * @return The value of the given key if exists otherwise empty string
     */
    static String getValueStringByKey(Map<String, ?> map, String keyName)
    {
        String retVal;

        if ((map == null) || (keyName == null))
        {
            retVal = "";
        }
        else
        {
            Object val = map.get(keyName);
            if (val != null)
                retVal = val.toString().trim();
            else
                retVal = "";
        }

        return retVal;
    }
}
