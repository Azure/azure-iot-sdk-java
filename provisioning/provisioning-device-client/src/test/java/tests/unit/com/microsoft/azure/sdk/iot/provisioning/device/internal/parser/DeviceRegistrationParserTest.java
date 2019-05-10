/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device.internal.parser;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.DeviceRegistrationParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/*
 *  Unit tests for  DeviceRegistration
 *  Coverage : 100% lines, 100% methods
 */
public class DeviceRegistrationParserTest
{
    private static final String TEST_REGISTRATION_ID = "testRegistrationId";

    //SRS_DeviceRegistration_25_002: [ The constructor shall save the provided Registration Id. ]
    //SRS_DeviceRegistration_25_007: [ This method shall create the expected Json with the provided Registration Id, EndorsementKey and StorageRootKey. ]
    @Test
    public void constructorWithoutTPMSucceed() throws Exception
    {
        final String expectedJson = "{\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"}";

        DeviceRegistrationParser deviceRegistrationParser = new DeviceRegistrationParser(TEST_REGISTRATION_ID, "");

        assertNotNull(deviceRegistrationParser.toJson());
        assertEquals(expectedJson, deviceRegistrationParser.toJson());
    }

    //SRS_DeviceRegistration_25_001: [ The constructor shall throw IllegalArgumentException if Registration Id is null or empty. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithoutTPMOnNullRegistrationIdThrows() throws Exception
    {
        DeviceRegistrationParser deviceRegistrationParser = new DeviceRegistrationParser(null, "");
    }


    @Test (expected = IllegalArgumentException.class)
    public void constructorWithoutTPMOnEmptyRegistrationIdThrows() throws Exception
    {
        DeviceRegistrationParser deviceRegistrationParser = new DeviceRegistrationParser("", "");
    }

    //SRS_DeviceRegistration_25_006: [ The constructor shall save the provided Registration Id, EndorsementKey and StorageRootKey. ]
    //SRS_DeviceRegistration_25_007: [ This method shall create the expected Json with the provided Registration Id, EndorsementKey and StorageRootKey. ]
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

        DeviceRegistrationParser deviceRegistrationParser = new DeviceRegistrationParser(regID, "", eKey, sRKey);
        assertNotNull(deviceRegistrationParser.toJson());
        assertEquals(expectedJson, deviceRegistrationParser.toJson());
    }

    //SRS_DeviceRegistration_25_003: [ The constructor shall throw IllegalArgumentException if Registration Id is null or empty. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithTPMOnNullRegistrationIdThrows() throws Exception
    {
        final String eKey = "testEndorsementKey";
        final String sRKey = "testStorageRootKey";
        DeviceRegistrationParser deviceRegistrationParser = new DeviceRegistrationParser(null, "", eKey, sRKey);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithTPMOnEmptyRegistrationIdThrows() throws Exception
    {
        final String eKey = "testEndorsementKey";
        final String sRKey = "testStorageRootKey";
        DeviceRegistrationParser deviceRegistrationParser = new DeviceRegistrationParser("", "", eKey, sRKey);
    }

    //SRS_DeviceRegistration_25_004: [ The constructor shall throw IllegalArgumentException if EndorsementKey is null or empty. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithTPMOnNullEkThrows() throws Exception
    {
        final String eKey = "testEndorsementKey";
        final String sRKey = "testStorageRootKey";
        DeviceRegistrationParser deviceRegistrationParser = new DeviceRegistrationParser(TEST_REGISTRATION_ID, "", null, sRKey);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithTPMOnEmptyEkThrows() throws Exception
    {
        final String eKey = "testEndorsementKey";
        final String sRKey = "testStorageRootKey";
        DeviceRegistrationParser deviceRegistrationParser = new DeviceRegistrationParser(TEST_REGISTRATION_ID, "", "", sRKey);
    }
}
