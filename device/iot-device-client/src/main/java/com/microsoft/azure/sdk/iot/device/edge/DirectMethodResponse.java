/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.edge;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class DirectMethodResponse
{
    private static final String STATUS_KEY_NAME = "status";
    @Expose(serialize = false)
    @SerializedName(STATUS_KEY_NAME)
    @Getter
    private int status;

    private static final String PAYLOAD_KEY_NAME = "payload";
    @Expose(serialize = false)
    @SerializedName(PAYLOAD_KEY_NAME)
    private JsonElement payload;

    //empty constructor for gson
    private DirectMethodResponse()
    {

    }

    public DirectMethodResponse(String json)
    {
        DirectMethodResponse result = new GsonBuilder().create().fromJson(json, DirectMethodResponse.class);

        this.payload = result.payload;
        this.status = result.status;
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
     * Return the DirectMethodResponse payload in json string
     * Use this if you wish to deserialize to a specific type using a deserialization library of your choice
     * @return the DirectMethodResponse payload in json string
     */
    public String getPayloadAsJsonString()
    {
        return payload.toString();
    }

    /**
     * Return the DirectMethodResponse payload in a custom type of your choosing
     * @param clazz the Custom type into which the payload can be deserialized
     * @return the DirectMethodResponse payload in Custom type
     */
    public <T> T getPayload(Class<T> clazz)
    {
        return new GsonBuilder().create().fromJson(payload, clazz);
    }
}
