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

@AllArgsConstructor
public class DirectMethodResponse
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
    private DirectMethodResponse()
    {

    }

    public DirectMethodResponse(String json)
    {
        DirectMethodResponse result = new GsonBuilder().create().fromJson(json, DirectMethodResponse.class);

        this.payload = result.payload;
        this.status = result.status;
    }

    public int getStatus()
    {
        return this.status;
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
     * Return the DirectMethodResponse payload in Custom type
     * @param customObject the Custom type in which the payload can return
     * @param <T> it describes the type parameter
     * @return the DirectMethodResponse payload in Custom type
     */
    public <T> T getPayloadAsCustomType(Class<T> customObject)
    {
        return new GsonBuilder().create().fromJson(payload, customObject);
    }
}
