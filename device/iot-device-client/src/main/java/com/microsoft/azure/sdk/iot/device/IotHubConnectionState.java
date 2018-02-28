// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

/**
 * The IoT Hub connection state value.
 */
public enum IotHubConnectionState
{
    CONNECTION_SUCCESS,
    CONNECTION_DROP,
    SAS_TOKEN_EXPIRED
}