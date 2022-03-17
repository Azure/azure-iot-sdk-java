// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.transport.ConnectionStatusChangeContext;

/**
 * Callback interface for allowing users to respond to changes in the connectivity of this sdk to Iot Hub.
 */
public interface IotHubConnectionStatusChangeCallback
{
    /**
     * The callback that is executed each time the connection status of the client changes. Includes details for more
     * context on why that change occurred.
     *
     * @param connectionStatusChangeContext the context surrounding the status change, including the new status, the reason
     * for the new status, the underlying exception (if connection was lost), and the user provided context object that
     * was set when setting the connection status change callback on the client.
     */
    void onStatusChanged(ConnectionStatusChangeContext connectionStatusChangeContext);
}
