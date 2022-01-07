// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

import com.google.gson.annotations.SerializedName;

public enum ConfigurationStatus
{
    @SerializedName("targeted")
    TARGETED(1),

    @SerializedName("applied")
    APPLIED(2);

    private final int numVal;

    ConfigurationStatus(int numVal)
    {
        this.numVal = numVal;
    }

    public int getNumVal()
    {
        return numVal;
    }
}
