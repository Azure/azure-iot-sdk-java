/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.dps.device.privateapi.dpsparser;

import com.microsoft.azure.sdk.iot.dps.device.privateapi.dpsparser.DPSRegisterRequestParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DPSRegisterRequestParserTest
{
    @Test
    public void constructorWithoutTPMSucceed() throws Exception
    {
        final String regID = "RIoT_COMMON_device";
        final String expectedJson = "{\"registrationId\":\"RIoT_COMMON_device\"}";

        DPSRegisterRequestParser dpsRegisterRequestParser = new DPSRegisterRequestParser(regID);

        assertNotNull(dpsRegisterRequestParser.toJson());
        assertEquals(dpsRegisterRequestParser.toJson(), expectedJson);
    }

    @Test
    public void constructorWithTPMSucceed() throws Exception
    {
        final String regID = "testID";
        final String eKey = "testEndorsementKey";
        final String sRKey = "testStorageRootKey";
        final String expectedJson = "{\"registrationId\":\"testID\"," +
                "\"tpm\":{" +
                    "\"endorsementKey\":\"testEndorsementKey\"," +
                    "\"storageRootKey\":\"testStorageRootKey\"" +
                "}}";

        DPSRegisterRequestParser dpsRegisterRequestParser = new DPSRegisterRequestParser(regID, eKey, sRKey);
        assertNotNull(dpsRegisterRequestParser.toJson());
        assertEquals(dpsRegisterRequestParser.toJson(), expectedJson);
    }
}
