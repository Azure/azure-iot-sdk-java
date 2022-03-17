// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.twin;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * This class contains the payload of a direct method request that was received by the device/module.
 * It is used with the onMethodInvoked() callback which is executed each time a direct method is invoked.
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DirectMethodPayload
{
    private JsonElement methodPayload;

    /**
     * Return the DirectMethodPayload payload in JsonElement type
     * @return the DirectMethodPayload payload in JsonElement type
     */
    public JsonElement getPayloadAsJsonElement()
    {
        return methodPayload;
    }

    /**
     * Return the DirectMethodPayload payload in json string
     * Use this if you wish to deserialize to a specific type using a deserialization library of your choice
     * @return the DirectMethodPayload payload in json string
     */
    public String getPayloadAsJsonString()
    {
        return methodPayload.toString();
    }

    /**
     * Return the DirectMethodPayload payload in a custom type of your choosing
     * @param clazz the Custom type into which the payload can be deserialized
     * @return the DirectMethodPayload payload in Custom type
     */
    public <T> T getPayload(Class<T> clazz)
    {
        return new GsonBuilder().create().fromJson(methodPayload, clazz);
    }
}
