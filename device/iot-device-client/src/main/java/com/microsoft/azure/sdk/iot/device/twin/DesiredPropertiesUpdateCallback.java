// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.twin;

/**
 * The callback to be executed each time the client receives a desired property update from the service.
 */
public interface DesiredPropertiesUpdateCallback
{
    /**
     * The callback to be executed each time the client receives a desired property update from the service.
     *
     * @param twin A {@link Twin} instance containing the updated desired properties.
     * @param context The context that was provided for this callback in
     * {@link com.microsoft.azure.sdk.iot.device.InternalClient#subscribeToDesiredPropertiesAsync(SubscribeToDesiredPropertiesCallback, Object, DesiredPropertiesUpdateCallback, Object)}
     */
    void onDesiredPropertiesUpdate(Twin twin, Object context);
}
