// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.*;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import mockit.*;
import org.junit.Test;
import tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.Helpers;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Unit tests for Device Provisioning Service Enrollment serializer
 * 100% methods, 100% lines covered
 */
public class EnrollmentTest
{
    private static final String VALID_REGISTRATION_ID = "8be9cd0e-8934-4991-9cbf-cc3b6c7ac647";
    private static final String VALID_DEVICE_ID = "19adff39-9cb4-4dcc-8f66-ac36ca2bdb15";
    private static final String VALID_IOTHUB_HOST_NAME = "foo.net";
    private static final String VALID_ENDORSEMENT_KEY = "76cadbbd-67af-49ab-b112-0c2e6a8445b0";
    private static final String VALID_STORAGE_ROOT_KEY = "validStorageRootKey";
    private static final String VALID_REGISTRATION_ID_STATUS = "ID";
    private static final String VALID_ASSIGNED_HUB_STATUS = "IoTHub-1";
    private static final String VALID_DEVICE_ID_STATUS = "Device-1";
    private static final String VALID_ETAG = "\\\"00000000-0000-0000-0000-00000000000\\\"";
    private static final String VALID_PARSED_ETAG = "\"00000000-0000-0000-0000-00000000000\"";

    private static final Date VALID_DATE = new Date();
    private static final String VALID_DATE_AS_STRING = ParserUtility.dateTimeUtcToString(VALID_DATE);

    private final class MockEnrollment extends Enrollment
    {
        String mockedRegistrationId;
        String mockedDeviceId;
        AttestationMechanism mockedAttestationMechanism;
        Attestation mockedAttestation;
        String mockedIotHubHostName;
        ProvisioningStatus mockedProvisioningStatus;
        DeviceRegistrationStatus mockedRegistrationStatus;
        TwinState mockedInitialTwinState;
        String mockedCreatedDateTimeUtc;
        String mockedLastUpdatedDateTimeUtc;
        String mockedEtag;
        JsonObject mockedJsonElement;

        MockEnrollment(
                String registrationId,
                Attestation attestation)
        {
            super(registrationId, attestation);
        }

        MockEnrollment(String json)
        {
            super(json);
        }

        @Mock
        protected void setRegistrationId(String registrationId)
        {
            mockedRegistrationId = registrationId;
        }

        @Mock
        public void setDeviceId(String deviceId)
        {
            mockedDeviceId = deviceId;
        }

        @Mock
        public void setAttestation(AttestationMechanism attestationMechanism)
        {
            mockedAttestationMechanism = attestationMechanism;
        }

        @Mock
        public void setAttestation(Attestation attestation)
        {
            mockedAttestation = attestation;
        }

        @Mock
        public void setIotHubHostName(String iotHubHostName)
        {
            mockedIotHubHostName = iotHubHostName;
        }

        @Mock
        public void setProvisioningStatus(ProvisioningStatus provisioningStatus)
        {
            mockedProvisioningStatus = provisioningStatus;
        }

        @Mock
        protected void setRegistrationStatus(DeviceRegistrationStatus registrationStatus)
        {
            mockedRegistrationStatus = registrationStatus;
        }

        @Mock
        public void setInitialTwinState(TwinState initialTwinState)
        {
            mockedInitialTwinState = initialTwinState;
        }

        @Mock
        protected void setCreatedDateTimeUtc(String createdDateTimeUtc)
        {
            mockedCreatedDateTimeUtc = createdDateTimeUtc;
        }

        @Mock
        protected void setLastUpdatedDateTimeUtc(String lastUpdatedDateTimeUtc)
        {
            mockedLastUpdatedDateTimeUtc = lastUpdatedDateTimeUtc;
        }

        @Mock
        public void setEtag(String etag)
        {
            mockedEtag = etag;
        }

        @Mock
        public JsonElement toJsonElement()
        {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
            mockedJsonElement = gson.toJsonTree(this).getAsJsonObject();
            mockedJsonElement.addProperty("mockedKey1","mockedValue1");
            return mockedJsonElement;
        }
    };

    private static Enrollment makeStandardEnrollment()
    {
        Enrollment enrollment = new Enrollment(
                VALID_REGISTRATION_ID,
                new TpmAttestation(VALID_ENDORSEMENT_KEY, VALID_STORAGE_ROOT_KEY));
        enrollment.setDeviceId(VALID_DEVICE_ID);
        enrollment.setIotHubHostName(VALID_IOTHUB_HOST_NAME);
        enrollment.setProvisioningStatus(ProvisioningStatus.ENABLED);

        return enrollment;
    }

    private MockEnrollment makeMockedEnrollment()
    {
        return new MockEnrollment(
                VALID_REGISTRATION_ID,
                new TpmAttestation(VALID_ENDORSEMENT_KEY, VALID_STORAGE_ROOT_KEY));
    }


    /* SRS_DEVICE_ENROLLMENT_21_001: [The constructor shall judge and store the provided parameters using the Enrollment setters.] */
    @Test
    public void constructorWithParametersUsesSetters()
    {
        // arrange

        // act
        MockEnrollment enrollment = makeMockedEnrollment();

        // assert
        assertNotNull(enrollment);
        assertEquals(VALID_REGISTRATION_ID, enrollment.mockedRegistrationId);
        assertNotNull(enrollment.mockedAttestation);
    }

    /* SRS_DEVICE_ENROLLMENT_21_002: [The constructor shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithJsonThrowsOnNullJson()
    {
        // arrange
        final String json = null;

        // act
        new MockEnrollment(json);

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_002: [The constructor shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithJsonThrowsOnEmptyJson()
    {
        // arrange
        final String json = "";

        // act
        new MockEnrollment(json);

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_003: [The constructor shall throw JsonSyntaxException if the JSON is invalid.] */
    @Test (expected = JsonSyntaxException.class)
    public void constructorWithJsonThrowsOnInvalidJson()
    {
        // arrange
        final String jsonWithExtraComma = "{\"a\":\"b\",}";

        // act
        new MockEnrollment(jsonWithExtraComma);

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_004: [The constructor shall deserialize the provided JSON for the enrollment class and subclasses.] */
    /* SRS_DEVICE_ENROLLMENT_21_005: [The constructor shall judge and store the provided mandatory parameters `registrationId` and `attestation` using the Enrollment setters.] */
    @Test
    public void constructorWithJsonUsesSetters()
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\"\n" +
                "}";

        // act
        MockEnrollment enrollment = new MockEnrollment(json);

        // assert
        assertEquals(VALID_REGISTRATION_ID, enrollment.mockedRegistrationId);
        assertNotNull(enrollment.mockedAttestationMechanism);
    }

    /* SRS_DEVICE_ENROLLMENT_21_006: [If the `deviceId`, `iotHubHostName`, `provisioningStatus`, or `registrationStatus` is not null, the constructor shall judge and store it using the Enrollment setter.] */
    @Test
    public void constructorWithJsonSetsOptionalParametersUsesSetters()
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"registrationStatus\": {\n" +
                "    \"registrationId\": \"" + VALID_REGISTRATION_ID_STATUS + "\",\n" +
                "    \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "    \"assignedHub\": \"" + VALID_ASSIGNED_HUB_STATUS + "\",\n" +
                "    \"deviceId\": \"" + VALID_DEVICE_ID_STATUS + "\",\n" +
                "    \"status\": \"" + EnrollmentStatus.ASSIGNED + "\",\n" +
                "    \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "  },\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        // act
        MockEnrollment enrollment = new MockEnrollment(json);

        // assert
        assertEquals(VALID_DEVICE_ID, enrollment.mockedDeviceId);
        assertNotNull(enrollment.mockedRegistrationStatus);
        assertEquals(VALID_IOTHUB_HOST_NAME, enrollment.mockedIotHubHostName);
        assertEquals(ProvisioningStatus.ENABLED, enrollment.mockedProvisioningStatus);
    }

    /* SRS_DEVICE_ENROLLMENT_21_006: [If the `deviceId`, `iotHubHostName`, `provisioningStatus`, or `registrationStatus` is not null, the constructor shall judge and store it using the Enrollment setter.] */
    @Test
    public void constructorWithJsonWithOptionalParametersSucceedOnNull()
    {
        // arrange
        final String json =
                "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        // act
        MockEnrollment enrollment = new MockEnrollment(json);

        // assert
        assertNull(enrollment.mockedRegistrationStatus);
        assertNull(enrollment.mockedDeviceId);
        assertNull(enrollment.mockedIotHubHostName);
        assertNull(enrollment.mockedProvisioningStatus);
    }

    /* SRS_DEVICE_ENROLLMENT_21_007: [If the initialTwinState is not null, the constructor shall convert the raw Twin and store it.] */
    @Test
    public void constructorWithJsonSetsInitialTwinStateUsesSetters(
            @Mocked final TwinState mockedTwinState)
    {
        // arrange
        final String jsonTwin =
                "  {\n" +
                "    \"desiredProperties\": {\n" +
                "      \"prop1\": \"value1\",\n" +
                "      \"$version\":4\n" +
                "    }\n" +
                "  }\n";
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"initialTwinState\" : " + jsonTwin + ",\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(TwinState.class);
                result = mockedTwinState;
            }
        };

        // act
        Enrollment enrollment = new Enrollment(json);

        // assert
        assertNotNull(enrollment.getInitialTwinState());

        new Verifications()
        {
            {
                new TwinState((TwinCollection)any, (TwinCollection)any);
                times = 1;
            }
        };
    }

    /* SRS_DEVICE_ENROLLMENT_21_007: [If the initialTwinState is not null, the constructor shall convert the raw Twin and store it.] */
    @Test
    public void constructorWithJsonSetsInitialTwinState()
    {
        // arrange
        final String jsonTwin =
                "  {\n" +
                "    \"desiredProperties\": {\n" +
                "      \"prop1\": \"value1\",\n" +
                "      \"$version\":4\n" +
                "    }\n" +
                "  }\n";
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"initialTwinState\" : " + jsonTwin + ",\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        // act
        Enrollment enrollment = new Enrollment(json);

        // assert
        TwinState twinState = enrollment.getInitialTwinState();
        Helpers.assertJson(twinState.toString(), jsonTwin);
    }

    /* SRS_DEVICE_ENROLLMENT_21_007: [If the initialTwinState is not null, the constructor shall convert the raw Twin and store it.] */
    @Test
    public void constructorWithJsonSetsInitialTwinStateSucceedOnNull()
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        // act
        Enrollment enrollment = new Enrollment(json);

        // assert
        assertNull(enrollment.getInitialTwinState());
    }

    /* SRS_DEVICE_ENROLLMENT_21_009: [If the createdDateTimeUtc is not null, the constructor shall judge and store it using the Enrollment setter.] */
    @Test
    public void constructorWithJsonSetsCreatedDateTimeUtcUsesSetters()
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        // act
        MockEnrollment enrollment = new MockEnrollment(json);

        // assert
        assertEquals(VALID_DATE_AS_STRING, enrollment.mockedCreatedDateTimeUtc);
    }

    /* SRS_DEVICE_ENROLLMENT_21_009: [If the createdDateTimeUtc is not null, the constructor shall judge and store it using the Enrollment setter.] */
    @Test
    public void constructorWithJsonSetsCreatedDateTimeUtcSucceedOnNull()
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        // act
        MockEnrollment enrollment = new MockEnrollment(json);

        // assert
        assertNull(enrollment.mockedCreatedDateTimeUtc);
    }

    /* SRS_DEVICE_ENROLLMENT_21_010: [If the lastUpdatedDateTimeUtc is not null, the constructor shall judge and store it using the Enrollment setter.] */
    @Test
    public void constructorWithJsonSetsLastUpdatedDateTimeUtcUsesSetters()
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        // act
        MockEnrollment enrollment = new MockEnrollment(json);

        // assert
        assertEquals(VALID_DATE_AS_STRING, enrollment.mockedLastUpdatedDateTimeUtc);
    }

    /* SRS_DEVICE_ENROLLMENT_21_010: [If the lastUpdatedDateTimeUtc is not null, the constructor shall judge and store it using the Enrollment setter.] */
    @Test
    public void constructorWithJsonSetsLastUpdatedDateTimeUtcSucceedOnNull()
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        // act
        MockEnrollment enrollment = new MockEnrollment(json);

        // assert
        assertNull(enrollment.mockedLastUpdatedDateTimeUtc);
    }

    /* SRS_DEVICE_ENROLLMENT_21_011: [If the etag is not null, the constructor shall judge and store it using the Enrollment setter.] */
    @Test
    public void constructorWithJsonSetsEtagUsesSetters()
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"etag\": \"" + VALID_ETAG + "\"\n" +
                "}";

        // act
        MockEnrollment enrollment = new MockEnrollment(json);

        // assert
        assertEquals(VALID_PARSED_ETAG, enrollment.mockedEtag);
    }

    /* SRS_DEVICE_ENROLLMENT_21_011: [If the etag is not null, the constructor shall judge and store it using the Enrollment setter.] */
    @Test
    public void constructorWithJsonSetsEtagSucceedOnNull()
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        // act
        MockEnrollment enrollment = new MockEnrollment(json);

        // assert
        assertNull(enrollment.mockedEtag);
    }

    /* SRS_DEVICE_ENROLLMENT_21_012: [The toJson shall return a String with the information in this class in a JSON format, by use the toJsonElement.] */
    @Test
    public void toJsonSimpleEnrollment()
    {
        // arrange
        MockEnrollment enrollment = makeMockedEnrollment();

        // act
        String result = enrollment.toJson();

        // assert
        Helpers.assertJson(enrollment.mockedJsonElement.toString(), result);
    }

    /* SRS_DEVICE_ENROLLMENT_21_013: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
    @Test
    public void toJsonElementSimpleEnrollment()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\",\n" +
                "      \"storageRootKey\": \"" + VALID_STORAGE_ROOT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\"\n" +
                "}";

        // act
        JsonElement result = enrollment.toJsonElement();

        // assert
        Helpers.assertJson(result.toString(), json);
    }

    /* SRS_DEVICE_ENROLLMENT_21_014: [If the initialTwinState is not null, the toJsonElement shall include its content in the final JSON.] */
    @Test
    public void toJsonElementSimpleEnrollmentWithTwin()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();
        enrollment.setInitialTwinState(new TwinState(
                new TwinCollection() {{
                    put("tag1", "valueTag1");
                    put("tag2", "valueTag2");
                }},
                new TwinCollection() {{
                    put("prop1", "value1");
                    put("prop2", "value2");
                }}));

        String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\",\n" +
                "      \"storageRootKey\": \"" + VALID_STORAGE_ROOT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"initialTwinState\" : {\n" +
                "    \"tags\": {\n" +
                "      \"tag1\": \"valueTag1\",\n" +
                "      \"tag2\": \"valueTag2\"\n" +
                "    },\n" +
                "    \"desiredProperties\": {\n" +
                "      \"prop1\": \"value1\",\n" +
                "      \"prop2\": \"value2\"\n" +
                "    }\n" +
                "  }," +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\"\n" +
                "}";

        // act
        JsonElement result = enrollment.toJsonElement();

        // assert
        Helpers.assertJson(result.toString(), json);
    }

    /* SRS_DEVICE_ENROLLMENT_21_015: [The toString shall return a String with the information in this class in a pretty print JSON.] */
    @Test
    public void toStringSimpleEnrollmentWithTwin()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();
        enrollment.setInitialTwinState(new TwinState(
                new TwinCollection() {{
                    put("tag1", "valueTag1");
                    put("tag2", "valueTag2");
                }},
                new TwinCollection() {{
                    put("prop1", "value1");
                    put("prop2", "value2");
                }}));

        String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\",\n" +
                "      \"storageRootKey\": \"" + VALID_STORAGE_ROOT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"initialTwinState\" : {\n" +
                "    \"tags\": {\n" +
                "      \"tag1\": \"valueTag1\",\n" +
                "      \"tag2\": \"valueTag2\"\n" +
                "    },\n" +
                "    \"desiredProperties\": {\n" +
                "      \"prop1\": \"value1\",\n" +
                "      \"prop2\": \"value2\"\n" +
                "    }\n" +
                "  }," +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\"\n" +
                "}";

        // act
        String result = enrollment.toString();

        // assert
        Helpers.assertJson(result, json);
    }

    /* SRS_DEVICE_ENROLLMENT_21_016: [The getRegistrationId shall return a String with the stored registrationId.] */
    /* SRS_DEVICE_ENROLLMENT_21_019: [The getDeviceId shall return a String with the stored deviceId.] */
    /* SRS_DEVICE_ENROLLMENT_21_022: [The getRegistrationStatus shall return a DeviceRegistrationStatus with the stored registrationStatus.] */
    /* SRS_DEVICE_ENROLLMENT_21_025: [The getAttestation shall return a AttestationMechanism with the stored attestation.] */
    /* SRS_DEVICE_ENROLLMENT_21_028: [The getIotHubHostName shall return a String with the stored iotHubHostName.] */
    /* SRS_DEVICE_ENROLLMENT_21_031: [The getInitialTwinState shall return a TwinState with the stored initialTwinState.] */
    /* SRS_DEVICE_ENROLLMENT_21_034: [The getProvisioningStatus shall return a TwinState with the stored provisioningStatus.] */
    /* SRS_DEVICE_ENROLLMENT_21_037: [The getCreatedDateTimeUtc shall return a Date with the stored createdDateTimeUtcDate.] */
    /* SRS_DEVICE_ENROLLMENT_21_040: [The getLastUpdatedDateTimeUtc shall return a Date with the stored lastUpdatedDateTimeUtcDate.] */
    /* SRS_DEVICE_ENROLLMENT_21_046: [The getEtag shall return a String with the stored etag.] */
    @Test
    public void gettersSimpleEnrollment() throws ProvisioningServiceClientException
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"registrationStatus\": {\n" +
                "    \"registrationId\": \"" + VALID_REGISTRATION_ID_STATUS + "\",\n" +
                "    \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "    \"assignedHub\": \"" + VALID_ASSIGNED_HUB_STATUS + "\",\n" +
                "    \"deviceId\": \"" + VALID_DEVICE_ID_STATUS + "\",\n" +
                "    \"status\": \"" + EnrollmentStatus.ASSIGNED + "\",\n" +
                "    \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "  },\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"initialTwinState\" : {\n" +
                "    \"tags\": {\n" +
                "      \"tag1\": \"valueTag1\",\n" +
                "      \"tag2\": \"valueTag2\"\n" +
                "    },\n" +
                "    \"desiredProperties\": {\n" +
                "      \"prop1\": \"value1\",\n" +
                "      \"prop2\": \"value2\"\n" +
                "    }\n" +
                "  }," +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"etag\": \"" + VALID_ETAG + "\"\n" +
                "}";
        Enrollment enrollment = new Enrollment(json);

        // act - assert
        assertEquals(VALID_REGISTRATION_ID, enrollment.getRegistrationId());
        assertEquals(VALID_DEVICE_ID, enrollment.getDeviceId());
        assertNotNull(enrollment.getRegistrationStatus());
        assertNotNull(enrollment.getAttestation());
        assertEquals(VALID_IOTHUB_HOST_NAME, enrollment.getIotHubHostName());
        assertNotNull(enrollment.getInitialTwinState());
        assertEquals(ProvisioningStatus.ENABLED, enrollment.getProvisioningStatus());
        Helpers.assertDateWithError(enrollment.getCreatedDateTimeUtc(), VALID_DATE_AS_STRING);
        Helpers.assertDateWithError(enrollment.getLastUpdatedDateTimeUtc(), VALID_DATE_AS_STRING);
        assertEquals(VALID_PARSED_ETAG, enrollment.getEtag());
    }

    /* SRS_DEVICE_ENROLLMENT_21_017: [The setRegistrationId shall throws IllegalArgumentException if the provided registrationId is {@code null}, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setRegistrationIdThrowsOnNull()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment, "setRegistrationId", new Class[] {String.class}, (String)null);

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_017: [The setRegistrationId shall throws IllegalArgumentException if the provided registrationId is {@code null}, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setRegistrationIdThrowsOnEmpty()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment, "setRegistrationId", new Class[] {String.class}, "");

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_017: [The setRegistrationId shall throws IllegalArgumentException if the provided registrationId is {@code null}, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setRegistrationIdThrowsOnNotUtf8()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment, "setRegistrationId", new Class[] {String.class}, "\u1234-invalid");

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_017: [The setRegistrationId shall throws IllegalArgumentException if the provided registrationId is {@code null}, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setRegistrationIdThrowsOnInvalidChar()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment, "setRegistrationId", new Class[] {String.class}, "invalid&");

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_018: [The setRegistrationId shall store the provided registrationId.] */
    @Test
    public void setRegistrationIdSucceed()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();
        final String newRegistrationId = "NewRegistrationId";
        assertNotEquals(newRegistrationId, Deencapsulation.getField(enrollment, "registrationId"));

        // act
        Deencapsulation.invoke(enrollment, "setRegistrationId", new Class[] {String.class}, newRegistrationId);

        // assert
        assertEquals(newRegistrationId, Deencapsulation.getField(enrollment, "registrationId"));
    }

    /* SRS_DEVICE_ENROLLMENT_21_020: [The setDeviceId shall throws IllegalArgumentException if the provided deviceId is {@code null}, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setDeviceIdThrowsOnNull()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment, "setDeviceId", new Class[] {String.class}, (String)null);

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_020: [The setDeviceId shall throws IllegalArgumentException if the provided deviceId is {@code null}, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setDeviceIdThrowsOnEmpty()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment, "setDeviceId", new Class[] {String.class}, "");

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_020: [The setDeviceId shall throws IllegalArgumentException if the provided deviceId is {@code null}, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setDeviceIdThrowsOnNotUtf8()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment, "setDeviceId", new Class[] {String.class}, "\u1234-invalid");

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_020: [The getDeviceId shall throws IllegalArgumentException if the provided deviceId is {@code null}, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setDeviceIdThrowsOnInvalidChar()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment, "setDeviceId", new Class[] {String.class}, "invalid&");

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_021: [The setDeviceId shall store the provided deviceId.] */
    @Test
    public void setDeviceIdSucceed()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();
        final String newDeviceId = "NewDeviceId";
        assertNotEquals(newDeviceId, Deencapsulation.getField(enrollment, "deviceId"));

        // act
        Deencapsulation.invoke(enrollment, "setDeviceId", new Class[] {String.class}, newDeviceId);

        // assert
        assertEquals(newDeviceId, Deencapsulation.getField(enrollment, "deviceId"));
    }

    /* SRS_DEVICE_ENROLLMENT_21_023: [The setRegistrationStatus shall throws IllegalArgumentException if the provided registrationStatus is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setRegistrationStatusThrowsOnNull()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment, "setRegistrationStatus", new Class[] {DeviceRegistrationStatus.class}, (DeviceRegistrationStatus)null);

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_024: [The setRegistrationStatus shall store the provided registrationStatus.] */
    @Test
    public void setRegistrationStatusSucceed(@Mocked final DeviceRegistrationStatus mockedDeviceRegistrationStatus)
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();
        assertNotEquals(mockedDeviceRegistrationStatus, Deencapsulation.getField(enrollment, "registrationStatus"));

        // act
        Deencapsulation.invoke(enrollment, "setRegistrationStatus", new Class[] {DeviceRegistrationStatus.class}, mockedDeviceRegistrationStatus);

        // assert
        assertEquals(mockedDeviceRegistrationStatus, Deencapsulation.getField(enrollment, "registrationStatus"));
    }

    /* SRS_DEVICE_ENROLLMENT_21_026: [The setAttestation shall throw IllegalArgumentException if the attestation is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setAttestationMechanismThrowsOnNull()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment, "setAttestation", new Class[]{AttestationMechanism.class}, (AttestationMechanism)null);

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_027: [The setAttestation shall store the provided attestation.] */
    @Test
    public void setAttestationMechanismSucceed(
            @Mocked final TpmAttestation mockedTpmAttestation,
            @Mocked final AttestationMechanism mockedAttestationMechanism)
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();
        assertNotEquals(mockedAttestationMechanism, Deencapsulation.getField(enrollment, "attestation"));

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedAttestationMechanism, "getAttestation");
                result = mockedTpmAttestation;
            }
        };

        // act
        Deencapsulation.invoke(enrollment, "setAttestation", mockedAttestationMechanism);

        // assert
        assertNotNull(Deencapsulation.getField(enrollment, "attestation"));
    }

    /* SRS_DEVICE_ENROLLMENT_21_050: [The setAttestation shall throw IllegalArgumentException if the attestation is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setAttestationThrowsOnNull()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        enrollment.setAttestation(null);

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_051: [The setAttestation shall store the provided attestation using the AttestationMechanism object.] */
    @Test
    public void setAttestationSucceed(
            @Mocked final TpmAttestation mockedTpmAttestation,
            @Mocked final AttestationMechanism mockedAttestationMechanism)
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        enrollment.setAttestation(mockedTpmAttestation);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(AttestationMechanism.class, new Class[]{Attestation.class}, mockedTpmAttestation);
                times = 1;
            }
        };
    }

    /* SRS_DEVICE_ENROLLMENT_21_029: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnNull()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        enrollment.setIotHubHostName(null);

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_029: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnEmpty()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        enrollment.setIotHubHostName("");

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_029: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnNotUTF8()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        enrollment.setIotHubHostName("NewHostName.\u1234a.b");

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_029: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnInvalidChar()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        enrollment.setIotHubHostName("NewHostName.&a.b");

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_029: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnIncompleteName()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        enrollment.setIotHubHostName("NewHostName");

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_030: [The setIotHubHostName shall store the provided iotHubHostName.] */
    @Test
    public void setIotHubHostNameSucceed()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();
        final String newHostName = "NewHostName.azureDevice.net";
        assertNotEquals(newHostName, Deencapsulation.getField(enrollment, "iotHubHostName"));

        // act
        enrollment.setIotHubHostName(newHostName);

        // assert
        assertEquals(newHostName, Deencapsulation.getField(enrollment, "iotHubHostName"));
    }

    /* SRS_DEVICE_ENROLLMENT_21_032: [The setInitialTwinState shall throw IllegalArgumentException if the initialTwinState is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setInitialTwinStateThrowsOnNull()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        enrollment.setInitialTwinState(null);

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_033: [The setInitialTwinState shall store the provided initialTwinState.] */
    @Test
    public void setInitialTwinStateSucceed(@Mocked final TwinState mockedTwinState)
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();
        assertNotEquals(mockedTwinState, Deencapsulation.getField(enrollment, "initialTwinState"));

        // act
        enrollment.setInitialTwinState(mockedTwinState);

        // assert
        assertEquals(mockedTwinState, Deencapsulation.getField(enrollment, "initialTwinState"));
    }

    /* SRS_DEVICE_ENROLLMENT_21_035: [The setProvisioningStatus shall throw IllegalArgumentException if the provisioningStatus is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setProvisioningStatusThrowsOnNull()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        enrollment.setProvisioningStatus(null);

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_036: [The setProvisioningStatus shall store the provided provisioningStatus.] */
    @Test
    public void setProvisioningStatusSucceed()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();
        assertNotEquals(ProvisioningStatus.DISABLED, Deencapsulation.getField(enrollment, "provisioningStatus"));

        // act
        enrollment.setProvisioningStatus(ProvisioningStatus.DISABLED);

        // assert
        assertEquals(ProvisioningStatus.DISABLED, Deencapsulation.getField(enrollment, "provisioningStatus"));
    }

    /* SRS_DEVICE_ENROLLMENT_21_038: [The setCreatedDateTimeUtc shall parse the provided String as a Data and Time UTC.] */
    @Test
    public void setCreatedDateTimeUtcSucceed()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();
        assertNull(Deencapsulation.getField(enrollment, "createdDateTimeUtcDate"));

        // act
        Deencapsulation.invoke(enrollment,"setCreatedDateTimeUtc", new Class[] {String.class}, VALID_DATE_AS_STRING);

        // assert
        Helpers.assertDateWithError((Date)Deencapsulation.getField(enrollment, "createdDateTimeUtcDate"), VALID_DATE_AS_STRING);
    }

    /* SRS_DEVICE_ENROLLMENT_21_039: [The setCreatedDateTimeUtc shall throws IllegalArgumentException if it cannot parse the provided createdDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setCreatedDateTimeUtcThrowsOnNull()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment,"setCreatedDateTimeUtc", new Class[] {String.class}, (String)null);

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_039: [The setCreatedDateTimeUtc shall throws IllegalArgumentException if it cannot parse the provided createdDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setCreatedDateTimeUtcThrowsOnEmpty()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment,"setCreatedDateTimeUtc", new Class[] {String.class}, (String)"");

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_039: [The setCreatedDateTimeUtc shall throws IllegalArgumentException if it cannot parse the provided createdDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setCreatedDateTimeUtcThrowsOnInvalid()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment,"setCreatedDateTimeUtc", new Class[] {String.class}, (String)"0000-00-00 00:00:00");

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_041: [The setLastUpdatedDateTimeUtc shall parse the provided String as a Data and Time UTC.] */
    @Test
    public void setLastUpdatedDateTimeUtcSucceed()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();
        assertNull(Deencapsulation.getField(enrollment, "lastUpdatedDateTimeUtcDate"));

        // act
        Deencapsulation.invoke(enrollment,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, VALID_DATE_AS_STRING);

        // assert
        Helpers.assertDateWithError((Date)Deencapsulation.getField(enrollment, "lastUpdatedDateTimeUtcDate"), VALID_DATE_AS_STRING);
    }

    /* SRS_DEVICE_ENROLLMENT_21_042: [The setLastUpdatedDateTimeUtc shall throws IllegalArgumentException if it cannot parse the provided lastUpdatedDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setLastUpdatedDateTimeUtcThrowsOnNull()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, (String)null);

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_042: [The setLastUpdatedDateTimeUtc shall throws IllegalArgumentException if it cannot parse the provided lastUpdatedDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setLastUpdatedDateTimeUtcThrowsOnEmpty()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, (String)"");

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_042: [The setLastUpdatedDateTimeUtc shall throws IllegalArgumentException if it cannot parse the provided lastUpdatedDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setLastUpdatedDateTimeUtcThrowsOnInvalid()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, (String)"0000-00-00 00:00:00");

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_047: [The setEtag shall throw IllegalArgumentException if the etag is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEtagThrowsOnNull()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment, "setEtag", new Class[] {String.class}, (String)null);

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_047: [The setEtag shall throw IllegalArgumentException if the etag is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEtagThrowsOnEmpty()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment, "setEtag", new Class[] {String.class}, (String)"");

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_047: [The setEtag shall throw IllegalArgumentException if the etag is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEtagThrowsOnNotUTF8()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(enrollment, "setEtag", new Class[] {String.class}, (String)"\u1234InvalidEtag");

        // assert
    }

    /* SRS_DEVICE_ENROLLMENT_21_048: [The setEtag shall store the provided etag.] */
    @Test
    public void setEtagSucceed()
    {
        // arrange
        Enrollment enrollment = makeStandardEnrollment();
        final String newEtag = "NewEtag";
        assertNotEquals(newEtag, Deencapsulation.getField(enrollment, "etag"));

        // act
        Deencapsulation.invoke(enrollment, "setEtag", new Class[] {String.class}, newEtag);

        // assert
        assertEquals(newEtag, Deencapsulation.getField(enrollment, "etag"));
    }

    /* SRS_DEVICE_ENROLLMENT_21_049: [The Enrollment shall provide an empty constructor to make GSON happy.] */
    @Test
    public void constructorSucceed()
    {
        // act
        Enrollment enrollment = Deencapsulation.newInstance(Enrollment.class);

        // assert
        assertNotNull(enrollment);
    }
}
