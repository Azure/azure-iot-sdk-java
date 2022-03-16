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

    /**
     * Return the DigitalTwinCommandResponse payload in JsonElement type
     * @return the DigitalTwinCommandResponse payload in JsonElement type
     */
    public JsonElement getPayloadAsJsonElement()
    {
        return payload;
    }

    /**
     * Return the DigitalTwinCommandResponse payload in String type
     * @return the DigitalTwinCommandResponse payload in String type
     */
    public String getPayloadAsString()
    {
        return getPayloadAsCustomType(String.class);
    }

    /**
     * Return the DigitalTwinCommandResponse payload in Custom type
     * @param customObject the Custom type in which the payload can return
     * @param <T> it describes the type parameter
     * @return the DigitalTwinCommandResponse payload in Custom type
     */
    public <T> T getPayloadAsCustomType(Class<T> customObject)
    {
        return new GsonBuilder().create().fromJson(payload, customObject);
    }

    public void setPayload(JsonElement payload)
    {
        this.payload = payload;
    }
}