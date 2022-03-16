// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.twin;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * This class is for the payload of direct method request which is received on the device/module.
 * It is used with onMethodInvoked() callback which is executed each time a direct method is invoked.
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
     * Return the DirectMethodPayload payload in Custom type
     * @param customObject the Custom type in which the payload can return
     * @param <T> it describes the type parameter
     * @return the DirectMethodPayload payload in Custom type
     */
    public <T> T getPayloadAsCustomType(Class<T> customObject)
    {
        return new GsonBuilder().create().fromJson(methodPayload, customObject);
    }
}
