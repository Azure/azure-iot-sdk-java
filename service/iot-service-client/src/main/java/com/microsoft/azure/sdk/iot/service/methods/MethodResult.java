/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.methods;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import lombok.Getter;

/**
 * Store the status and payload received as result of a method invoke.
 */
public final class MethodResult
{
    /**
     * Status of the Invoke Method.
     */
    @Getter
    private final Integer status;

    /**
     * Payload with the result of the Invoke Method
     */
    private final JsonElement payload;

    public MethodResult(Integer status, JsonElement payload)
    {
        this.status = status;
        this.payload = payload;
    }

    // Payload getters with different types
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
}
