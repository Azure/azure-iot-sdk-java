// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.DeviceTwin;

/**
 * The interface for describing the callback that is triggered when there are changes in the client's twin desired properties.
 * @param <Type1> The type of the desired property key. Since the twin is a json object, the key will always be a String.
 * @param <Type2> The type of the desired property value.
 */
public interface PropertyCallBack<Type1, Type2>
{
    /**
     * The callback that is triggered when there are changes in the client's twin desired properties.
     * @param propertyKey The desired property key that was updated.
     * @param propertyValue The desired property value that was updated.
     * @param context The context passed to the callback.
     */
    void PropertyCall(Type1 propertyKey, Type2 propertyValue,  Object context);
}
