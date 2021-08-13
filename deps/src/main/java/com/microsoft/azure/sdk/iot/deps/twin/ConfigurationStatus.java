// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.twin;

import com.google.gson.annotations.SerializedName;

public enum ConfigurationStatus
{
    @SerializedName("targeted")
    TARGETED,

    @SerializedName("applied")
    APPLIED;
}
