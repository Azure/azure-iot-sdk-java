// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.twin;

import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;

/**
 * Callback to be executed when the request to subscribe to desired properties has been acknowledged by the service.
 */
public interface SubscriptionAcknowledgedCallback
{
    /**
     * The callback that is executed when the request to subscribe to desired properties or direct methodshas been
     * acknowledged by the service.
     * @param context The context that was provided for this callback in
     * {@link com.microsoft.azure.sdk.iot.device.InternalClient#subscribeToDesiredPropertiesAsync(SubscriptionAcknowledgedCallback, Object, DesiredPropertiesCallback, Object)}
     * or in {@link  com.microsoft.azure.sdk.iot.device.InternalClient#subscribeToMethodsAsync(SubscriptionAcknowledgedCallback, Object, MethodCallback, Object)}.
     */
    void onSubscriptionAcknowledged(IotHubClientException exception, Object context);
}
