// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

public enum IotHubConnectionStatus
{
    /**
     * The device or module is connected.
     * <p>The client is connected, and ready to be used.</p>
     */
    CONNECTED,

    /**
     * The device or module is disconnected.
     * <p>Inspect the associated {@link com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason} returned (and exception thrown, if any), and take appropriate action.</p>
     */
    DISCONNECTED,

    /**
     * The device or module is attempting to reconnect.
     * <p>The client is attempting to recover the connection. Do NOT close or open the client instance when it is retrying.</p>
     */
    DISCONNECTED_RETRYING
}