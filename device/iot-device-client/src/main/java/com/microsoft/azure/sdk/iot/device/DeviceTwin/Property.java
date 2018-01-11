// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.DeviceTwin;

import java.util.Date;

public class Property
{
    private Pair<String, Object> property = null;
    private Integer version = null;
    private Date lastUpdated = null;
    private Integer lastUpdatedVersion = null;

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

    protected Property(String key, Object value, Integer version, Date lastUpdated, Integer lastUpdatedVersion)
    {
        this(key, value);

        /*
         **Codes_SRS_Property_21_007: [**The constructor shall store the provided version and metadata.**]**
         */
        this.version = version;
        this.lastUpdated = lastUpdated;
        this.lastUpdatedVersion = lastUpdatedVersion;
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

    public Integer getVersion()
    {
        /*
         **Codes_SRS_Property_21_008: [**The function shall return the value for this property.**]**
         */
        return this.version;
    }

    public Date getLastUpdated()
    {
        /*
         **Codes_SRS_Property_21_009: [**The function shall return the value for this property.**]**
         */
        return this.lastUpdated;
    }

    public Integer getLastUpdatedVersion()
    {
        /*
         **Codes_SRS_Property_21_010: [**The function shall return the value for this property.**]**
         */
        return this.lastUpdatedVersion;
    }

    /**
     * Creates a pretty print JSON with the content of this class and subclasses.
     *
     * @return The {@code String} with the pretty print JSON.
     */
    @Override
    public String toString()
    {
        /* Codes_SRS_Property_21_011: [The toString shall return a String with the information in this class in a pretty print JSON.] */
        return getKey() + " - " + getValue();
    }
}
