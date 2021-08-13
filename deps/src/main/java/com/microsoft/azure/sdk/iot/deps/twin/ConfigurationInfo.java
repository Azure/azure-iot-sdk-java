// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.twin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ConfigurationInfo
{
    private static final String STATUS_NAME = "status";
    @Expose
    @SerializedName(STATUS_NAME)
    private ConfigurationStatus status;

    /**
     * Setter for status
     *
     * @param status - status of capabilities enabled on the device
     */
    public void setStatus(ConfigurationStatus status)
    {
        /* Codes_SRS_CONFIGURATIONINFO_28_001: [The setStatus shall replace the `status` by the provided one.] */
        this.status = status;
    }

    /**
     * Getter for status
     *
     * @return the status
     */
    public ConfigurationStatus getStatus()
    {
        /* Codes_SRS_CONFIGURATIONINFO_28_002: [The getStatus shall return the stored `status` content.] */
        return this.status;
    }
}
