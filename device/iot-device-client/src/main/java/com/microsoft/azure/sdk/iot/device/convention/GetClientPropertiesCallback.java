// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.convention;

import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.twin.GetTwinCallback;

/**
 * The interface for retrieving client properties from a convention based device.
 */
public interface GetClientPropertiesCallback
{
    /**
     * The callback to be executed when the service responds to a getTwin request with the current twin.
     * @param clientProperties The current client properties.
     * @param context The context that was provided for this callback in
     * {@link com.microsoft.azure.sdk.iot.device.InternalClient#getTwinAsync(GetTwinCallback, Object)}.
     */
    void onClientPropertiesReceived(ClientProperties clientProperties, IotHubClientException clientException, Object context);
}
