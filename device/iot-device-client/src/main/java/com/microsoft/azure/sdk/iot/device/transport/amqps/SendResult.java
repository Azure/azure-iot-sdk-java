/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device.transport.amqps;

enum SendResult
{
    SUCCESS,
    WRONG_DEVICE,
    UNKNOWN_FAILURE,
    DUPLICATE_SUBSCRIPTION_MESSAGE,
    SUBSCRIPTION_IN_PROGRESS
}
