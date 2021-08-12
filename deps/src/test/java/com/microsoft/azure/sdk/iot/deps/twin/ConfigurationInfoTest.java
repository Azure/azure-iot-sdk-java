// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.twin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.sdk.iot.deps.twin.ConfigurationInfo;
import com.microsoft.azure.sdk.iot.deps.twin.ConfigurationStatus;

import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigurationInfoTest
{
    private final static String CONFIGURATIONS_SAMPLE = "{\"status\":\"targeted\"}";

    /* Tests_SRS_CONFIGURATIONINFO_28_001: [The setStatus shall replace the `status` by the provided one.] */
    @Test
    public void setStatusSucceed()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        ConfigurationInfo result = gson.fromJson(CONFIGURATIONS_SAMPLE, ConfigurationInfo.class);

        // act
        result.setStatus(ConfigurationStatus.APPLIED);

        // assert
        assertEquals(ConfigurationStatus.APPLIED, Deencapsulation.getField(result, "status"));
    }

    /* Tests_SRS_CONFIGURATIONINFO_28_002: [The getStatus shall return the stored `status` content.] */
    @Test
    public void getStatusSucceed()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        ConfigurationInfo result = gson.fromJson(CONFIGURATIONS_SAMPLE, ConfigurationInfo.class);

        // act - assert
        assertEquals(ConfigurationStatus.TARGETED, result.getStatus());
    }
}
