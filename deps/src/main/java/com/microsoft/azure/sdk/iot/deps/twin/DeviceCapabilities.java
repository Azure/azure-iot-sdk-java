// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.twin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeviceCapabilities
{
    private static final String IOT_EDGE_NAME = "iotEdge";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(IOT_EDGE_NAME)
    protected Boolean iotEdge;

    /**
     * Setter for iotEdge Boolean
     *
     * @param iotEdge - status of capabilities enabled on the device
     * @throws IllegalArgumentException if the provided argument is null
     */
    public void setIotEdge(Boolean iotEdge) throws IllegalArgumentException
    {
        if (iotEdge == null)
        {
            throw new IllegalArgumentException("iotEdge cannot be null");
        }

        this.iotEdge = iotEdge;
    }

    /**
     * Getter for iotEdge Boolean
     *
     * @return the iotEdge Boolean
     */
    public Boolean getIotEdge()
    {
        return this.iotEdge;
    }
}
