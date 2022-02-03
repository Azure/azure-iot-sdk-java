/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.messaging;

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

    /**
     * Helper function to get a value from the given JsonObject if the key name exists
     *
     * @param jsonObject The JsonObject object to get the value from
     * @param key The name of the key
     * @return The value of the given key if exists otherwise empty string
     */
    static String getValueFromJsonObject(JsonObject jsonObject, String key)
    {
        String retVal;
        if (jsonObject == null || key == null || key.length() == 0)
        {
            retVal = "";
        }
        else
        {
            JsonValue jsonValue = jsonObject.get(key);
            if (jsonValue != JsonValue.NULL)
            {
                retVal = getValueFromJsonString(jsonObject.getJsonString(key));
            }
            else
            {
                retVal = "";
            }
        }
        return retVal;
    }

    /**
     * Helper function to get trim the leading and trailing parenthesis from a Json string if they exists
     *
     * @param jsonString The JsonString to trim
     * @return The trimmed string
     */
    static String getValueFromJsonString(JsonString jsonString)
    {
        String retVal;
        if (jsonString == null)
        {
            retVal = "";
        }
        else
        {
            retVal = jsonString.toString();
            if (retVal.startsWith("\""))
            {
                retVal = retVal.replaceFirst("\"", "");
            }
            if (retVal.endsWith("\""))
            {
                retVal = retVal.substring(0, retVal.length()-1);
            }
        }
        return retVal;
    }

    /**
     * Equality check for objects that accounts for null value comparisons. If both objects are null, this will return false.
     * Both objects must have .equals(...) implemented correctly for this method to work properly.
     * @param a the first object
     * @param b the seconds object
     * @return if the two are equal
     */
    static boolean areEqual(Object a, Object b)
    {
        if (a == null || b == null)
        {
            //one is null, the other is not
            return false;
        }

        //neither is null, so this comparison won't throw
        return a.equals(b);
    }
}
