// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.twin;

/**
 * The callback to be executed when the service responds to a getTwin request with the current twin.
 */
public interface GetTwinCallback
{
    /**
     * The callback to be executed when the service responds to a getTwin request with the current twin.
     * @param twin The current twin.
     * @param context The context that was provided for this callback in
     * {@link com.microsoft.azure.sdk.iot.device.InternalClient#getTwinAsync(GetTwinCallback, Object)}.
     */
    void onTwinReceived(Twin twin, Object context);
}
