// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.twin;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * This class is for the payload of direct method request which is received on the device/module.
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
     * Return the DirectMethodPayload payload in String type
     * @return the DirectMethodPayload payload in String type
     */
    public String getPayloadAsString()
    {
        return getPayloadAsCustomType(String.class);
    }

    /**
     * Return the DirectMethodPayload payload in a custom type of your choosing
     * Use this if you wish to deserialize to a specific type using a deserialization library of your choice
     * @param clazz the Custom type into which the payload can be deserialized
     * @return the DirectMethodPayload payload in Custom type
     */
    public <T> T getPayloadAsCustomType(Class<T> clazz)
    {
        return new GsonBuilder().create().fromJson(methodPayload, clazz);
    }
}
