// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.models;

/**
 * Represents the device command invocation results.
 */
public final class DigitalTwinCommandResponse {
    /**
     * Command invocation result status, as supplied by the device.
     */
    Integer status;

    /**
     *  Command invocation result payload, as supplied by the device.
     */
    String payload;

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getPayload()
    {
        return payload;
    }

    public void setPayload(String payload)
    {
        this.payload = payload;
    }
}