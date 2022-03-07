// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

import java.util.Date;

public class Property
{
    private final Pair<String, Object> property;
    private Integer version = null;
    private boolean isReported = false;
    private Date lastUpdated;
    private Integer lastUpdatedVersion;
    private String lastUpdatedBy;
    private String lastUpdatedByDigest;

    public Property(String key, Object value)
    {
        if (key == null || key.isEmpty())
        {

            /*
            **Codes_SRS_Property_25_002: [**If the key is null or empty, the constructor shall throw an IllegalArgumentException.**]
             */
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        /*
        **Codes_SRS_Property_25_001: [**The constructor shall save the key and value representing this property.**]**
         */
        this.property = new Pair<>(key, value);
    }

    Property(String key, Object value, Integer version, boolean isReported, Date lastUpdated, Integer lastUpdatedVersion)
    {
        this(key, value);

        /*
         **Codes_SRS_Property_21_007: [**The constructor shall store the provided version and metadata.**]**
         */
        this.version = version;
        this.isReported = isReported;
        this.lastUpdated = lastUpdated;
        this.lastUpdatedVersion = lastUpdatedVersion;
    }

    Property(String key, Object value, Integer version, boolean isReported, Date lastUpdated, Integer lastUpdatedVersion, String lastUpdatedBy, String lastUpdatedByDigest)
    {
        this(key, value);
        
        this.version = version;
        this.isReported = isReported;
        this.lastUpdated = lastUpdated;
        this.lastUpdatedVersion = lastUpdatedVersion;
        this.lastUpdatedBy = lastUpdatedBy;
        this.lastUpdatedByDigest = lastUpdatedByDigest;
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

    public boolean getIsReported()
    {
        /*
         **Codes_SRS_Property_21_012: [**The function shall return the stored isReported.**]**
         */
        return this.isReported;
    }

    public Date getLastUpdated()
    {
        //Codes_SRS_Property_34_013: [The function shall return the stored lastUpdated.]
        return this.lastUpdated;
    }

    public Integer getLastUpdatedVersion()
    {
        //Codes_SRS_Property_34_014: [The function shall return the stored lastUpdatedVersion.]
        return this.lastUpdatedVersion;
    }

    public String getLastUpdatedBy()
    {
        return this.lastUpdatedBy;
    }

    public String getLastUpdatedByDigest()
    {
        return this.lastUpdatedByDigest;
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
