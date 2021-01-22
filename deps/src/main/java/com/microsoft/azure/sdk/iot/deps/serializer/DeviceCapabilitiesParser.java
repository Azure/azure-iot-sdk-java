// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeviceCapabilitiesParser
{
    private static final String IOT_EDGE_NAME = "iotEdge";
    @Expose
    @SerializedName(IOT_EDGE_NAME)
    private Boolean iotEdge;

    public DeviceCapabilitiesParser()
    {
        //Codes_SRS_DEVICE_CAPIBILITIES_PARSER_28_001: [This Constructor shall create a new instance of an DeviceCapabilitiesParser object and return it.]
        //do nothing
    }

    /**
     * Getter for IotEdge
     *
     * @return The value of IotEdge
     */
    public Boolean getIotEdge()
    {
        //Codes_SRS_DEVICE_CAPIBILITIES_PARSER_28_002: [This method shall return the value of this object's iotEdge.]
        return iotEdge;
    }

    /**
     * Setter for IotEdge
     *
     * @param iotEdge the value to set IotEdge to
     */
    public void setIotEdge(Boolean iotEdge)
    {
        //Codes_SRS_DEVICE_CAPIBILITIES_PARSER_28_003: [This method shall set the value of iotEdge equal to the provided value.]
        this.iotEdge = iotEdge;
    }
}
