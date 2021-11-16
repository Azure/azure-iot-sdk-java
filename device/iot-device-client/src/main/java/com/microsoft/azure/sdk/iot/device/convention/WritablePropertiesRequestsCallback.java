// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.convention;

import com.microsoft.azure.sdk.iot.deps.convention.ClientPropertyCollection;

/**
 * A callback used to respond to all writable property requests. See {@link com.microsoft.azure.sdk.iot.device.InternalClient#subscribeToWritablePropertiesEvent(WritablePropertiesRequestsCallback, Object)} for more information.
 */
public interface WritablePropertiesRequestsCallback
{
    /**
     * The method to execute.
     * @param writablePropertiesRequestsCollection A collection of properties to be updated.
     * @param context User supplied context.
     */
    void onWritablePropertyCallbackReceived(ClientPropertyCollection writablePropertiesRequestsCollection, Object context);
}
