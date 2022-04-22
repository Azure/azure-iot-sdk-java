// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.convention;

import com.microsoft.azure.sdk.iot.device.twin.SubscriptionAcknowledgedCallback;

/**
 * A callback used to respond to all writable property requests. See
 * {@link com.microsoft.azure.sdk.iot.device.InternalClient#subscribeToWritablePropertiesAsync(WritablePropertiesCallback, Object, SubscriptionAcknowledgedCallback, Object)}
 * for more information.
 */
public interface WritablePropertiesCallback
{
    /**
     * The method to execute.
     * @param clientProperties A collection of properties that were updated.
     * @param context User supplied context.
     */
    void onWritablePropertiesUpdated(ClientProperties clientProperties, Object context);
}
