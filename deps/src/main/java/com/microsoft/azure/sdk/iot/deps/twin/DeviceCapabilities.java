// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.twin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public class DeviceCapabilities
{
    private static final String IOT_EDGE_NAME = "iotEdge";
    @Expose
    @SerializedName(IOT_EDGE_NAME)
    @Getter
    @Setter
    private Boolean iotEdge = false;
}
