// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.annotations.SerializedName;

/**
 * INNER TWINPARSER CLASS
 *
 * Enum for device status
 */
public enum TwinStatus
{
    @SerializedName("enabled")
    enabled,

    @SerializedName("disabled")
    disabled
}
