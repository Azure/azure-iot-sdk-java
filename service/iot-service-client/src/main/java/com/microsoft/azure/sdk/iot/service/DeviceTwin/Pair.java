/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

public class Pair
{
    private String key;
    private Object value;
    private static final int MAX_ALLOWABLE_KEY_LENGTH = 128;

    public Pair(String key, Object value) throws IllegalArgumentException
    {
        if (key == null || key.isEmpty())
        {

            /*
            **Codes_SRS_Pair_25_002: [**If the key is null or empty, the constructor shall throw an IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        if (key.contains(" ") || key.contains("$") || key.contains(".") || key.length() > MAX_ALLOWABLE_KEY_LENGTH)
        {
            /*
            **Codes_SRS_Pair_25_003: [**If the key contains illegal unicode control characters i.e ' ', '.', '$' or if length is greater than 124 chars, the constructor shall throw an IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("Key cannot contain illegal unicode control characters '.', '$', ' '");
        }
        /*
        **Codes_SRS_Pair_25_001: [**The constructor shall save the key and value representing this Pair.**]**
         */
        this.key = key;
        this.value = value;
    }

    public Object getValue()
    {
        /*
        **Codes_SRS_Pair_25_005: [**The function shall return the value for this Pair.**]**
         */
        return value;
    }

    public String getKey()
    {
        /*
        **Codes_SRS_Pair_25_004: [**The function shall return the value of the key corresponding to this Pair.**]**
         */
        return key;
    }

    public Object setValue(Object value)
    {
        Object oldValue = this.value;

        /*
        **Codes_SRS_Pair_25_006: [**The function shall overwrite the new value for old and return old value.**]**
         */
        this.value = value;

        return oldValue;
    }
}
