// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.models;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * Represents the device command invocation results.
 */
public final class DigitalTwinCommandResponse {
    /**
     * Command invocation result status, as supplied by the device.
     */
    private Integer status;

    /**
     *  Command invocation result payload, as supplied by the device.
     */
    private JsonElement payload;

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public JsonElement getPayloadAsJsonElement()
    {
        return payload;
    }

    public String getPayloadAsJsonString()
    {
        return getPayloadAsCustomType(String.class);
    }

    public <T> T getPayloadAsCustomType(Class<T> customObject)
    {
        return new GsonBuilder().create().fromJson(payload, customObject);
    }

    public void setPayload(JsonElement payload)
    {
        this.payload = payload;
    }
}