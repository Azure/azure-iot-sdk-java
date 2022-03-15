/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.edge;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.nio.charset.StandardCharsets;

public class MethodResult
{
    private static final String STATUS_KEY_NAME = "status";
    @Expose(serialize = false)
    @SerializedName(STATUS_KEY_NAME)
    private int status;

    private static final String PAYLOAD_KEY_NAME = "payload";
    @Expose(serialize = false)
    @SerializedName(PAYLOAD_KEY_NAME)
    private JsonElement payload;

    //empty constructor for gson
    private MethodResult()
    {

    }

    public MethodResult(String json)
    {
        MethodResult result = new GsonBuilder().create().fromJson(json, MethodResult.class);

        this.payload = result.payload;
        this.status = result.status;
    }

    public int getStatus()
    {
        return this.status;
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
}
