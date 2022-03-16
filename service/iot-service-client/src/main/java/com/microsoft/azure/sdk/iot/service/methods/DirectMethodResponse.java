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
public final class DirectMethodResponse
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

    public DirectMethodResponse(Integer status, JsonElement payload)
    {
        this.status = status;
        this.payload = payload;
    }

    /**
     * Return the DirectMethodResponse payload in JsonElement type
     * @return the DirectMethodResponse payload in JsonElement type
     */
    public JsonElement getPayloadAsJsonElement()
    {
        return payload;
    }

    /**
     * Return the DirectMethodResponse payload in String type
     * @return the DirectMethodResponse payload in String type
     */
    public String getPayloadAsString()
    {
        return getPayloadAsCustomType(String.class);
    }

    /**
     * Return the DirectMethodResponse payload in a custom type of your choosing
     * Use this if you wish to deserialize to a specific type using a deserialization library of your choice
     * @param clazz the Custom type into which the payload can be deserialized
     * @return the DirectMethodResponse payload in Custom type
     */
    public <T> T getPayloadAsCustomType(Class<T> clazz)
    {
        return new GsonBuilder().create().fromJson(payload, clazz);
    }
}
