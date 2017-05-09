// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * INNER TWINPARSER CLASS
 *
 * TwinParser metadata representation
 *
 */
public class TwinMetadata
{

    private static final String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'";
    private static final String TIMEZONE = "UTC";

    @SerializedName("$lastUpdated")
    private String lastUpdated;

    @SerializedName("$lastUpdatedVersion")
    private Integer lastUpdatedVersion;

    protected TwinMetadata()
    {
        update();
        this.lastUpdatedVersion = null;
    }

    protected  TwinMetadata(Integer lastUpdatedVersion)
    {
        update();
        this.lastUpdatedVersion = lastUpdatedVersion;
    }

    protected TwinMetadata(Integer lastUpdatedVersion, String lastUpdated)
    {
        this.lastUpdated = lastUpdated;
        this.lastUpdatedVersion = lastUpdatedVersion;
    }

    protected synchronized boolean update(String lastUpdated, Integer lastUpdatedVersion)
    {
        boolean updated;

        if (!this.lastUpdated.equals(lastUpdated))
        {
            updated = true;
        }
        else if(this.lastUpdatedVersion == null)
        {
            if(lastUpdatedVersion == null)
            {
                updated = false;
            }
            else
            {
                updated = true;
            }
        }
        else if(this.lastUpdatedVersion.equals(lastUpdatedVersion))
        {
            updated = false;
        }
        else
        {
            updated = true;
        }

        this.lastUpdated = lastUpdated;
        this.lastUpdatedVersion = lastUpdatedVersion;

        return updated;
    }

    protected synchronized void update(int lastUpdatedVersion)
    {
        update();
        this.lastUpdatedVersion = lastUpdatedVersion;
    }

    protected synchronized void update()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        lastUpdated = dateFormat.format(new Date());
    }

    protected synchronized Integer getLastUpdateVersion()
    {
        return this.lastUpdatedVersion;
    }

    protected synchronized String getLastUpdate()
    {
        return this.lastUpdated;
    }
}
