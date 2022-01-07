// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public class ConfigurationInfo
{
    private static final String STATUS_NAME = "status";
    @Expose
    @SerializedName(STATUS_NAME)
    @Getter
    @Setter
    private ConfigurationStatus status;
}
