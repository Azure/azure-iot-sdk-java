// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device;

/**
 * The type of gateway to which the device/module client is connecting while specifying the property of GatewayHostName.
 */
public enum GatewayType
{
    /**
     * The device/module client is connecting to Edgehub.
     */
    EDGE,

    /**
     * The device/module client is connecting to an E4K MQTT broker.
     */
    E4K
}
