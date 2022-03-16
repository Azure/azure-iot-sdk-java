/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.messaging;

import com.google.gson.annotations.SerializedName;

/**
 * Enum for feedback status codes
 */
public enum FeedbackStatusCode
{
    @SerializedName("Success")
    success,

    @SerializedName("Expired")
    expired,

    @SerializedName("DeliveryCountExceeded")
    deliveryCountExceeded,

    @SerializedName("Rejected")
    rejected,

    unknown
}
