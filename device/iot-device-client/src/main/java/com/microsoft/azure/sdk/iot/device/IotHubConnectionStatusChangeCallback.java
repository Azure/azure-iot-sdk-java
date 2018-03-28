// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

/**
 * Callback interface for allowing users to respond to changes in the connectivity of this sdk to Iot Hub.
 */
public interface IotHubConnectionStatusChangeCallback
{
    /**
     * Callback that is executed when the connection status of this sdk to the iot hub changes. Includes details for more
     * context on why that change occurred.
     * @param status The new connection status of the sdk
     * @param statusChangeReason the reason why the sdk changed to this status
     * @param throwable The throwable that caused the change in status. May be null if there wasn't an associated throwable
     * @param callbackContext the context for this callback that was registered by the user
     */
    void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext);
}
