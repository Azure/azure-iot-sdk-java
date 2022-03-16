// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.registry.serializers;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public class DeviceCapabilitiesParser
{
    private static final String IOT_EDGE_NAME = "iotEdge";
    @Expose
    @SerializedName(IOT_EDGE_NAME)
    @Getter
    @Setter
    private Boolean iotEdge;

    public DeviceCapabilitiesParser()
    {
        //Codes_SRS_DEVICE_CAPIBILITIES_PARSER_28_001: [This Constructor shall create a new instance of an DeviceCapabilitiesParser object and return it.]
        //do nothing
    }
}
