// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

/**
 * The IoTHub message type
 */
public enum MessageType
{
    UNKNOWN,
    CBS_AUTHENTICATION,
    DEVICE_TELEMETRY,
    DEVICE_METHODS,
    DEVICE_TWIN
}
