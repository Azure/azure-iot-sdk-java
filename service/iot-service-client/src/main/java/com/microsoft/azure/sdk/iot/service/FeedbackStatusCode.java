/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

/**
 * Enum for feedback status codes
 */
public enum FeedbackStatusCode
{
    success,
    expired,
    deliveryCountExceeded,
    rejected,
    unknown
}
