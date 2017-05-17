// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.DeviceTwin;

public class Property
{
    private Pair<String, Object> property = null;

    public Property(String key, Object value)
    {
        if (key == null || key.isEmpty())
        {

            /*
            **Codes_SRS_Property_25_002: [**If the key is null or empty, the constructor shall throw an IllegalArgumentException.**]
             */
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        if (key.contains(" ") || key.contains("$") || key.contains("."))
        {
            /*
            **Codes_SRS_Property_25_006: [**If the key contains illegal unicode control characters i.e ' ', '.', '$', the constructor shall throw an IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("Key cannot contain illegal unicode control characters '.', '$', ' '");
        }
        /*
        **Codes_SRS_Property_25_001: [**The constructor shall save the key and value representing this property.**]**
         */
        this.property = new Pair<>(key, value);
    }

    public String getKey()
    {
        /*
        **Codes_SRS_Property_25_003: [**The function shall return the value of the key corresponding to this property.**]**
        */
        return this.property.getKey();
    }

    public Object getValue()
    {
        /*
        **Codes_SRS_Property_25_004: [**The function shall return the value for this property.**]**
         */
        return this.property.getValue();
    }

    public void setValue(Object newValue)
    {
        /*
        **Codes_SRS_Property_25_005: [**The function shall overwrite the new value for old.**]**
         */
        this.property.setValue(newValue);
    }

    @Override
    public String toString()
    {
        return getKey() + " - " + getValue();
    }
}
