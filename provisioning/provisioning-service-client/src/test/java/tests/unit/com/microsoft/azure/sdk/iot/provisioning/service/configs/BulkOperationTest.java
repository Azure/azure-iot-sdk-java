// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.JsonElement;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import mockit.Deencapsulation;
import org.junit.Test;
import tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.Helpers;

import static org.junit.Assert.*;

import java.util.*;

/**
 * Unit tests for Device Provisioning Service bulk operation serializer
 * 100% methods, 100% lines covered
 */
public class BulkOperationTest
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
                    "        \"provisioningStatus\": \"enabled\"\n" +
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
                    "        \"provisioningStatus\": \"disabled\"\n" +
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

    private List<Enrollment> makeBulkEnrollment()
    {
        Enrollment enrollment1 = new Enrollment(
                VALID_REGISTRATION_ID_1,
                new TpmAttestation(VALID_ENDORSEMENT_KEY, VALID_STORAGE_ROOT_KEY));
        enrollment1.setDeviceId(VALID_DEVICE_ID);
        enrollment1.setIotHubHostName(VALID_IOTHUB_HOST_NAME);
        enrollment1.setProvisioningStatus(ProvisioningStatus.ENABLED);

        Enrollment enrollment2 = new Enrollment(
                VALID_REGISTRATION_ID_2,
                new TpmAttestation(VALID_ENDORSEMENT_KEY, null));
        enrollment2.setDeviceId(VALID_DEVICE_ID);
        enrollment2.setIotHubHostName(VALID_IOTHUB_HOST_NAME);
        enrollment2.setProvisioningStatus(ProvisioningStatus.DISABLED);

        List<Enrollment> enrollments = new LinkedList<Enrollment>()
        {
            {
                add(enrollment1);
                add(enrollment2);
            }
        };
        return enrollments;
    }

    /* SRS_BULK_OPERATION_21_001: [The toJson shall return a String with the mode and the collection of enrollments using a JSON format.] */
    @Test
    public void toJsonSimpleBulkOperation()
    {
        // arrange
        final List<Enrollment> enrollments = makeBulkEnrollment();

        // act
        String result = BulkOperation.toJson(BulkOperationMode.CREATE, enrollments);

        // assert
        Helpers.assertJson(result, VALID_JSON_BULK);
    }

    /* SRS_BULK_OPERATION_21_002: [The toJson shall throws IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toJsonThrowsOnNullOperationMode()
    {
        // arrange
        final List<Enrollment> enrollments = makeBulkEnrollment();

        // act
        BulkOperation.toJson(null, enrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_002: [The toJson shall throws IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toJsonThrowsOnNullListOfEnrollments()
    {
        // arrange
        final List<Enrollment> enrollments = null;

        // act
        BulkOperation.toJson(BulkOperationMode.CREATE, enrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_002: [The toJson shall throws IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toJsonThrowsOnEmptyListOfEnrollments()
    {
        // arrange
        final List<Enrollment> enrollments = new LinkedList<>();

        // act
        BulkOperation.toJson(BulkOperationMode.CREATE, enrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_003: [The toString shall return a String with the mode and the collection of enrollments using a pretty print JSON format.] */
    @Test
    public void toStringSimpleBulkOperation()
    {
        // arrange
        final List<Enrollment> enrollments = makeBulkEnrollment();

        // act
        String result = BulkOperation.toString(BulkOperationMode.CREATE, enrollments);

        // assert
        Helpers.assertJson(result, VALID_JSON_BULK);
    }

    /* SRS_BULK_OPERATION_21_004: [The toString shall throws IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toStringThrowsOnNullOperationMode()
    {
        // arrange
        final List<Enrollment> enrollments = makeBulkEnrollment();

        // act
        BulkOperation.toString(null, enrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_004: [The toString shall throws IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toStringThrowsOnNullListOfEnrollments()
    {
        // arrange
        final List<Enrollment> enrollments = null;

        // act
        BulkOperation.toString(BulkOperationMode.CREATE, enrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_004: [The toString shall throws IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toStringThrowsOnEmptyListOfEnrollments()
    {
        // arrange
        final List<Enrollment> enrollments = new LinkedList<>();

        // act
        BulkOperation.toString(BulkOperationMode.CREATE, enrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_005: [The toJsonElement shall throws IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toJsonElementThrowsOnNullOperationMode()
    {
        // arrange
        final List<Enrollment> enrollments = makeBulkEnrollment();

        // act
        BulkOperation.toJsonElement(null, enrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_005: [The toJsonElement shall throws IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toJsonElementThrowsOnNullListOfEnrollments()
    {
        // arrange
        final List<Enrollment> enrollments = null;

        // act
        BulkOperation.toJsonElement(BulkOperationMode.CREATE, enrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_005: [The toJsonElement shall throws IllegalArgumentException if the provided mode is null or the collection of enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void toJsonElementThrowsOnEmptyListOfEnrollments()
    {
        // arrange
        final List<Enrollment> enrollments = new LinkedList<>();

        // act
        BulkOperation.toJsonElement(BulkOperationMode.CREATE, enrollments);

        // assert
    }

    /* SRS_BULK_OPERATION_21_006: [The toJsonElement shall return a JsonElement with the mode and the collection of enrollments using a JSON format.] */
    @Test
    public void toJsonElementSimpleBulkOperation()
    {
        // arrange
        final List<Enrollment> enrollments = makeBulkEnrollment();

        // act
        JsonElement result = BulkOperation.toJsonElement(BulkOperationMode.CREATE, enrollments);

        // assert
        Helpers.assertJson(result.toString(), VALID_JSON_BULK);
    }
}
