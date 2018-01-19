/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.transport.mqtt;

public enum MqttQos
{
    DELIVER_AT_MOST_ONCE,
    DELIVER_AT_LEAST_ONCE,
    DELIVER_EXACTLY_ONCE,
    DELIVER_FAILURE,
    DELIVER_UNKNOWN
}
