// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.twin;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DirectMethodPayload
{
    private JsonElement methodPayload;

    public JsonElement getPayloadAsJsonElement()
    {
        return methodPayload;
    }

    public String getPayloadAsJsonString()
    {
        return getPayloadAsCustomType(String.class);
    }

    public <T> T getPayloadAsCustomType(Class<T> customObject)
    {
        return new GsonBuilder().create().fromJson(methodPayload, customObject);
    }
}
