// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DeviceCapabilitiesTest
{
    private final static String DEVICECAPABILITIES_SAMPLE = "{\"iotEdge\":false}";

    /* Tests_SRS_CONFIGURATIONINFO_28_001: [The setStatus shall replace the `status` by the provided one.] */
    @Test
    public void setIotEdgeSucceed()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        DeviceCapabilities result = gson.fromJson(DEVICECAPABILITIES_SAMPLE, DeviceCapabilities.class);

        // act
        result.setIotEdge(true);

        // assert
        assertEquals(true, Deencapsulation.getField(result, "iotEdge"));
    }

    /* Tests_SRS_CONFIGURATIONINFO_28_002: [The getStatus shall return the stored `status` content.] */
    @Test
    public void getIotEdgeSucceed()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        DeviceCapabilities result = gson.fromJson(DEVICECAPABILITIES_SAMPLE, DeviceCapabilities.class);

        // act - assert
        assertEquals(false, result.isIotEdge());
    }
}
