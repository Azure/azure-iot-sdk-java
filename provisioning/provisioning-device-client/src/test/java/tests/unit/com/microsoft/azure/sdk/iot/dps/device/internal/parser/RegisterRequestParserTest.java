/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.dps.device.internal.parser;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.RegisterRequestParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RegisterRequestParserTest
{
    @Test
    public void constructorWithoutTPMSucceed() throws Exception
    {
        final String regID = "RIoT_COMMON_device";
        final String expectedJson = "{\"registrationId\":\"RIoT_COMMON_device\"}";

        RegisterRequestParser registerRequestParser = new RegisterRequestParser(regID);

        assertNotNull(registerRequestParser.toJson());
        assertEquals(registerRequestParser.toJson(), expectedJson);
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

        RegisterRequestParser registerRequestParser = new RegisterRequestParser(regID, eKey, sRKey);
        assertNotNull(registerRequestParser.toJson());
        assertEquals(registerRequestParser.toJson(), expectedJson);
    }
}
