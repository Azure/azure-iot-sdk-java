// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.JsonElement;
import org.junit.Test;
import com.microsoft.azure.sdk.iot.provisioning.service.Helpers;

import java.util.*;

/**
 * Unit tests for Device Provisioning Service bulk operation serializer
 * 100% methods, 100% lines covered
 */
public class BulkEnrollmentOperationTest
{
    private static final String VALID_REGISTRATION_ID_1 = "8be9cd0e-8934-4991-9cbf-cc3b6c7ac647";
    private static final String VALID_REGISTRATION_ID_2 = "818B129D-20C4-4E91-8EEA-955776DB4340";
    private static final String VALID_DEVICE_ID = "19adff39-9cb4-4dcc-8f66-ac36ca2bdb15";
    private static final String VALID_IOTHUB_HOST_NAME = "foo.net";
    private static final String VALID_ENDORSEMENT_KEY = "76cadbbd-67af-49ab-b112-0c2e6a8445b0";
    private static final String VALID_STORAGE_ROOT_KEY = "validStorageRootKey";

    private static final String VALID_JSON_ENROLLMENT_1 =
            "      {\n" +
                    "        \"registrationId\": \"" + VALID_REGISTRATION_ID_1 + "\",\n" +
                    "        \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                    "        \"attestation\": {\n" +
                    "          \"type\": \"tpm\",\n" +
                    "          \"tpm\": {\n" +
                    "            \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\",\n" +
                    "            \"storageRootKey\": \"" + VALID_STORAGE_ROOT_KEY + "\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                    "        \"provisioningStatus\": \"enabled\",\n" +
                    "        \"capabilities\": {\n" +
                    "           \"iotEdge\": false\n" +
                    "         }\n" +
                    "      }";

    private static final String VALID_JSON_ENROLLMENT_2 =
            "      {\n" +
                    "        \"registrationId\": \"" + VALID_REGISTRATION_ID_2 + "\",\n" +
                    "        \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                    "        \"attestation\": {\n" +
                    "          \"type\": \"tpm\",\n" +
                    "          \"tpm\": {\n" +
                    "            \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                    "        \"provisioningStatus\": \"disabled\",\n" +
                    "        \"capabilities\": {\n" +
                    "           \"iotEdge\": false\n" +
                    "         }\n" +
                    "      }";

    private static final String VALID_JSON_BULK =
            "{\n" +
                    "  \"mode\":\"create\",\n" +
                    "  \"enrollments\": \n" +
                    "    [\n" +
                    VALID_JSON_ENROLLMENT_1 + ",\n" +
                    VALID_JSON_ENROLLMENT_2 + "\n" +
                    "    ]\n" +
                    "}";

    private List<IndividualEnrollment> makeBulkEnrollment()
    {
        IndividualEnrollment individualEnrollment1 = new IndividualEnrollment(
                VALID_REGISTRATION_ID_1,
                new TpmAttestation(VALID_ENDORSEMENT_KEY, VALID_STORAGE_ROOT_KEY));
        individualEnrollment1.setDeviceIdFinal(VALID_DEVICE_ID);
        individualEnrollment1.setIotHubHostNameFinal(VALID_IOTHUB_HOST_NAME);
        individualEnrollment1.setProvisioningStatusFinal(ProvisioningStatus.ENABLED);

        IndividualEnrollment individualEnrollment2 = new IndividualEnrollment(
                VALID_REGISTRATION_ID_2,
                new TpmAttestation(VALID_ENDORSEMENT_KEY, null));
        individualEnrollment2.setDeviceIdFinal(VALID_DEVICE_ID);
        individualEnrollment2.setIotHubHostNameFinal(VALID_IOTHUB_HOST_NAME);
        individualEnrollment2.setProvisioningStatusFinal(ProvisioningStatus.DISABLED);

        return new LinkedList<IndividualEnrollment>()
        {
            {
                add(individualEnrollment1);
                add(individualEnrollment2);
            }
        };
    }

    /* SRS_BULK_OPERATION_21_001: [The toJson shall return a String with the mode and the collection of enrollments using a JSON format.] */
    @Test
    public void toJsonSimpleBulkEnrollmentOperation()
    {
        // arrange
        final List<IndividualEnrollment> individualEnrollments = makeBulkEnrollment();

        // act
        String result = BulkEnrollmentOperation.toJson(BulkOperationMode.CREATE, individualEnrollments);

        // assert
        Helpers.assertJson(result, VALID_JSON_BULK);
    }

    /* SRS_BULK_OPERATION_21_002: [The toJson shall throw IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toJsonThrowsOnNullOperationMode()
    {
        // arrange
        final List<IndividualEnrollment> individualEnrollments = makeBulkEnrollment();

        // act
        BulkEnrollmentOperation.toJson(null, individualEnrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_002: [The toJson shall throw IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toJsonThrowsOnNullListOfEnrollments()
    {
        // arrange
        final List<IndividualEnrollment> individualEnrollments = null;

        // act
        BulkEnrollmentOperation.toJson(BulkOperationMode.CREATE, individualEnrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_002: [The toJson shall throw IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toJsonThrowsOnEmptyListOfEnrollments()
    {
        // arrange
        final List<IndividualEnrollment> individualEnrollments = new LinkedList<>();

        // act
        BulkEnrollmentOperation.toJson(BulkOperationMode.CREATE, individualEnrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_003: [The toString shall return a String with the mode and the collection of enrollments using a pretty print JSON format.] */
    @Test
    public void toStringSimpleBulkEnrollmentOperation()
    {
        // arrange
        final List<IndividualEnrollment> individualEnrollments = makeBulkEnrollment();

        // act
        String result = BulkEnrollmentOperation.toString(BulkOperationMode.CREATE, individualEnrollments);

        // assert
        Helpers.assertJson(result, VALID_JSON_BULK);
    }

    /* SRS_BULK_OPERATION_21_004: [The toString shall throw IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toStringThrowsOnNullOperationMode()
    {
        // arrange
        final List<IndividualEnrollment> individualEnrollments = makeBulkEnrollment();

        // act
        BulkEnrollmentOperation.toString(null, individualEnrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_004: [The toString shall throw IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toStringThrowsOnNullListOfEnrollments()
    {
        // arrange
        final List<IndividualEnrollment> individualEnrollments = null;

        // act
        BulkEnrollmentOperation.toString(BulkOperationMode.CREATE, individualEnrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_004: [The toString shall throw IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toStringThrowsOnEmptyListOfEnrollments()
    {
        // arrange
        final List<IndividualEnrollment> individualEnrollments = new LinkedList<>();

        // act
        BulkEnrollmentOperation.toString(BulkOperationMode.CREATE, individualEnrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_005: [The toJsonElement shall throw IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toJsonElementThrowsOnNullOperationMode()
    {
        // arrange
        final List<IndividualEnrollment> individualEnrollments = makeBulkEnrollment();

        // act
        BulkEnrollmentOperation.toJsonElement(null, individualEnrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_005: [The toJsonElement shall throw IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toJsonElementThrowsOnNullListOfEnrollments()
    {
        // arrange
        final List<IndividualEnrollment> individualEnrollments = null;

        // act
        BulkEnrollmentOperation.toJsonElement(BulkOperationMode.CREATE, individualEnrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_005: [The toJsonElement shall throw IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toJsonElementThrowsOnEmptyListOfEnrollments()
    {
        // arrange
        final List<IndividualEnrollment> individualEnrollments = new LinkedList<>();

        // act
        BulkEnrollmentOperation.toJsonElement(BulkOperationMode.CREATE, individualEnrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_006: [The toJsonElement shall return a JsonElement with the mode and the collection of enrollments using a JSON format.] */
    @Test
    public void toJsonElementSimpleBulkEnrollmentOperation()
    {
        // arrange
        final List<IndividualEnrollment> individualEnrollments = makeBulkEnrollment();

        // act
        JsonElement result = BulkEnrollmentOperation.toJsonElement(BulkOperationMode.CREATE, individualEnrollments);

        // assert
        Helpers.assertJson(result.toString(), VALID_JSON_BULK);
    }
}
