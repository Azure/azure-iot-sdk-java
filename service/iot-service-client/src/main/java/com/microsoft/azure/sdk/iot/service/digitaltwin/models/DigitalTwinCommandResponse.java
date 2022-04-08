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
     * Return the DigitalTwinCommandResponse payload in json string
     * Use this if you wish to deserialize to a specific type using a deserialization library of your choice
     * @return the DigitalTwinCommandResponse payload in json string
     */
    public String getPayloadAsJsonString()
    {
        return payload.toString();
    }

    /**
     * Return the DigitalTwinCommandResponse payload in a custom type of your choosing
     * @param clazz the Custom type into which the payload can be deserialized
     * @return the DigitalTwinCommandResponse payload in Custom type
     */
    public <T> T getPayload(Class<T> clazz)
    {
        return new GsonBuilder().create().fromJson(payload, clazz);
    }

    public void setPayload(JsonElement payload)
    {
        this.payload = payload;
    }
}