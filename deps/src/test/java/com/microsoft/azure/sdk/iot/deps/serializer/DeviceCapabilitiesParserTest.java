// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.microsoft.azure.sdk.iot.deps.serializer.DeviceCapabilitiesParser;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

public class DeviceCapabilitiesParserTest
{
    //Tests_SRS_DEVICE_CAPIBILITIES_PARSER_28_001: [This Constructor shall create a new instance of an DeviceCapabilitiesParser object and return it.]
    @Test
    public void testConstructor()
    {
        //act
        DeviceCapabilitiesParser parser = new DeviceCapabilitiesParser();

        //assert
        assertNotNull(parser);
    }

    //Codes_SRS_DEVICE_CAPIBILITIES_PARSER_28_002: [This method shall return the value of this object's iotEdge.]
    //Codes_SRS_DEVICE_CAPIBILITIES_PARSER_28_003: [This method shall set the value of iotEdge equal to the provided value.]
    @Test
    public void testIotEdgeProperty()
    {
        //arrange
        DeviceCapabilitiesParser parser = new DeviceCapabilitiesParser();

        //act
        parser.setIotEdge(true);

        //assert
        assertEquals(true, parser.getIotEdge());
    }
}
