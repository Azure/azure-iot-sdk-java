// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device;

/**
 * The type of gateway to which the device/module client is connecting while specifying the property of GatewayHostName.
 */
public enum GatewayType
{
    /**
     * The device/module client is connecting to an Edge hub in the default mode.
     */
    EDGE,

    /**
     * The device/module client is connecting to an Edge hub in the E4K mode.
     */
    E4K
}
