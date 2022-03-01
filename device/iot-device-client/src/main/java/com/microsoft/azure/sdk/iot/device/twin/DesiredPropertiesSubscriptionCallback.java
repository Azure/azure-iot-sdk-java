// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.twin;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;

/**
 * Callback to be executed when the request to subscribe to desired properties has been acknowledged by the service.
 */
public interface DesiredPropertiesSubscriptionCallback
{
    /**
     * The callback that is executed when the request to subscribe to desired properties has been acknowledged by the service.
     * @param statusCode The service's status code for the request. If {@link IotHubStatusCode#OK} then the request was
     * processed succesfully.
     * @param context The context that was provided for this callback in
     * {@link com.microsoft.azure.sdk.iot.device.InternalClient#subscribeToDesiredPropertiesAsync(DesiredPropertiesSubscriptionCallback, Object, DesiredPropertiesCallback, Object)}
     */
    void onSubscriptionAcknowledged(IotHubStatusCode statusCode, Object context);
}
