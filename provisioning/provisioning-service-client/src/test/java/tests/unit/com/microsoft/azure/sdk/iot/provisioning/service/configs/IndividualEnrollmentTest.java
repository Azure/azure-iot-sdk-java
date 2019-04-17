// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.*;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import mockit.*;
import org.junit.Test;
import tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.Helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for Device Provisioning Service IndividualEnrollment serializer
 * 100% methods, 100% lines covered
 */
public class IndividualEnrollmentTest
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

    private final class MockIndividualEnrollment extends IndividualEnrollment
    {
        String mockedRegistrationId;
        String mockedDeviceId;
        AttestationMechanism mockedAttestationMechanism;
        Attestation mockedAttestation;
        String mockedIotHubHostName;
        ProvisioningStatus mockedProvisioningStatus;
        DeviceRegistrationState mockedRegistrationState;
        TwinState mockedInitialTwin;
        String mockedCreatedDateTimeUtc;
        String mockedLastUpdatedDateTimeUtc;
        String mockedEtag;
        JsonObject mockedJsonElement;

        MockIndividualEnrollment(
                String registrationId,
                Attestation attestation)
        {
            super(registrationId, attestation);
        }

        MockIndividualEnrollment(String json)
        {
            super(json);
        }

        @Mock
        public void setAttestation(Attestation attestation)
        {
            mockedAttestation = attestation;
        }

        @Mock
        public Attestation getAttestation()
        {
            return mockedAttestation;
        }

        @Mock
        public void setProvisioningStatus(ProvisioningStatus provisioningStatus)
        {
            mockedProvisioningStatus = provisioningStatus;
        }

        @Mock
        public void setInitialTwin(TwinState initialTwin)
        {
            mockedInitialTwin = initialTwin;
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

    private static IndividualEnrollment makeStandardEnrollment()
    {
        IndividualEnrollment individualEnrollment = new IndividualEnrollment(
                VALID_REGISTRATION_ID,
                new TpmAttestation(VALID_ENDORSEMENT_KEY, VALID_STORAGE_ROOT_KEY));
        individualEnrollment.setDeviceIdFinal(VALID_DEVICE_ID);
        individualEnrollment.setIotHubHostNameFinal(VALID_IOTHUB_HOST_NAME);
        individualEnrollment.setProvisioningStatusFinal(ProvisioningStatus.ENABLED);

        return individualEnrollment;
    }

    private MockIndividualEnrollment makeMockedEnrollment()
    {
        return new MockIndividualEnrollment(
                VALID_REGISTRATION_ID,
                new TpmAttestation(VALID_ENDORSEMENT_KEY, VALID_STORAGE_ROOT_KEY));
    }


    /* SRS_INDIVIDUAL_ENROLLMENT_21_001: [The constructor shall judge and store the provided parameters using the IndividualEnrollment setters.] */
    @Test
    public void constructorWithParametersUsesSetters()
    {
        // arrange

        // act
        MockIndividualEnrollment enrollment = makeMockedEnrollment();

        // assert
        assertNotNull(enrollment);
        assertEquals(VALID_REGISTRATION_ID, Deencapsulation.getField(enrollment, "registrationId"));
        assertNotNull(enrollment.mockedAttestation);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_002: [The constructor shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithJsonThrowsOnNullJson()
    {
        // arrange
        final String json = null;

        // act
        new MockIndividualEnrollment(json);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_002: [The constructor shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithJsonThrowsOnEmptyJson()
    {
        // arrange
        final String json = "";

        // act
        new MockIndividualEnrollment(json);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_003: [The constructor shall throw JsonSyntaxException if the JSON is invalid.] */
    @Test (expected = JsonSyntaxException.class)
    public void constructorWithJsonThrowsOnInvalidJson()
    {
        // arrange
        final String jsonWithExtraComma = "{\"a\":\"b\",}";

        // act
        new MockIndividualEnrollment(jsonWithExtraComma);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_004: [The constructor shall deserialize the provided JSON for the enrollment class and subclasses.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_21_005: [The constructor shall judge and store the provided mandatory parameters `registrationId` and `attestation` using the IndividualEnrollment setters.] */
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
        MockIndividualEnrollment enrollment = new MockIndividualEnrollment(json);

        // assert
        assertEquals(VALID_REGISTRATION_ID, Deencapsulation.getField(enrollment, "registrationId"));
        assertNotNull(enrollment.getAttestation());
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_006: [If the `deviceId`, `iotHubHostName`, `provisioningStatus`, or `registrationState` is not null, the constructor shall judge and store it using the IndividualEnrollment setter.] */
    @Test
    public void constructorWithJsonSetsOptionalParametersUsesSetters()
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"registrationState\": {\n" +
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
        MockIndividualEnrollment enrollment = new MockIndividualEnrollment(json);

        // assert
        assertEquals(VALID_DEVICE_ID, Deencapsulation.getField(enrollment, "deviceId"));
        assertNotNull(Deencapsulation.getField(enrollment, "registrationState"));
        assertEquals(VALID_IOTHUB_HOST_NAME, Deencapsulation.getField(enrollment, "iotHubHostName"));
        assertEquals(ProvisioningStatus.ENABLED, Deencapsulation.getField(enrollment, "provisioningStatus"));
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_006: [If the `deviceId`, `iotHubHostName`, `provisioningStatus`, or `registrationState` is not null, the constructor shall judge and store it using the IndividualEnrollment setter.] */
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
        MockIndividualEnrollment enrollment = new MockIndividualEnrollment(json);

        // assert
        assertNull(enrollment.mockedRegistrationState);
        assertNull(enrollment.mockedDeviceId);
        assertNull(enrollment.mockedIotHubHostName);
        assertNull(enrollment.mockedProvisioningStatus);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_007: [If the initialTwin is not null, the constructor shall convert the raw Twin and store it.] */
    @Test
    public void constructorWithJsonSetsInitialTwinUsesSetters(
            @Mocked final TwinState mockedTwinState)
    {
        // arrange
        final String jsonTwin =
                "  {\n" +
                        "    \"properties\": {\n" +
                        "      \"desired\": {\n" +
                        "        \"prop1\": \"value1\",\n" +
                        "        \"$version\":4\n" +
                        "      }\n" +
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
                "  \"initialTwin\" : " + jsonTwin + ",\n" +
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
        IndividualEnrollment individualEnrollment = new IndividualEnrollment(json);

        // assert
        assertNotNull(individualEnrollment.getInitialTwin());

        new Verifications()
        {
            {
                new TwinState((TwinCollection)any, (TwinCollection)any);
                times = 1;
            }
        };
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_007: [If the initialTwin is not null, the constructor shall convert the raw Twin and store it.] */
    @Test
    public void constructorWithJsonSetsInitialTwin()
    {
        // arrange
        final String jsonTwin =
                "  {\n" +
                        "    \"properties\": {\n" +
                        "      \"desired\": {\n" +
                        "        \"prop1\": \"value1\",\n" +
                        "        \"$version\":4\n" +
                        "      }\n" +
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
                "  \"initialTwin\" : " + jsonTwin + ",\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        // act
        IndividualEnrollment individualEnrollment = new IndividualEnrollment(json);

        // assert
        TwinState twinState = individualEnrollment.getInitialTwin();
        Helpers.assertJson(twinState.toString(), jsonTwin);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_007: [If the initialTwin is not null, the constructor shall convert the raw Twin and store it.] */
    @Test
    public void constructorWithJsonSetsInitialTwinSucceedOnNull()
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
        IndividualEnrollment individualEnrollment = new IndividualEnrollment(json);

        // assert
        assertNull(individualEnrollment.getInitialTwin());
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_009: [If the createdDateTimeUtc is not null, the constructor shall judge and store it using the IndividualEnrollment setter.] */
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
        MockIndividualEnrollment enrollment = new MockIndividualEnrollment(json);

        // assert
        assertEquals(VALID_DATE.toString(), Deencapsulation.getField(enrollment, "createdDateTimeUtcDate").toString());
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_009: [If the createdDateTimeUtc is not null, the constructor shall judge and store it using the IndividualEnrollment setter.] */
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
        MockIndividualEnrollment enrollment = new MockIndividualEnrollment(json);

        // assert
        assertNull(enrollment.mockedCreatedDateTimeUtc);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_010: [If the lastUpdatedDateTimeUtc is not null, the constructor shall judge and store it using the IndividualEnrollment setter.] */
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
        MockIndividualEnrollment enrollment = new MockIndividualEnrollment(json);

        // assert
        assertEquals(VALID_DATE.toString(), Deencapsulation.getField(enrollment, "lastUpdatedDateTimeUtcDate").toString());
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_010: [If the lastUpdatedDateTimeUtc is not null, the constructor shall judge and store it using the IndividualEnrollment setter.] */
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
        MockIndividualEnrollment enrollment = new MockIndividualEnrollment(json);

        // assert
        assertNull(enrollment.mockedLastUpdatedDateTimeUtc);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_011: [If the etag is not null, the constructor shall judge and store it using the IndividualEnrollment setter.] */
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
        MockIndividualEnrollment enrollment = new MockIndividualEnrollment(json);

        // assert
        assertEquals(VALID_PARSED_ETAG, Deencapsulation.getField(enrollment, "etag"));
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_011: [If the etag is not null, the constructor shall judge and store it using the IndividualEnrollment setter.] */
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
        MockIndividualEnrollment enrollment = new MockIndividualEnrollment(json);

        // assert
        assertNull(enrollment.mockedEtag);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_012: [The toJson shall return a String with the information in this class in a JSON format, by use the toJsonElement.] */
    @Test
    public void toJsonSimpleEnrollment()
    {
        // arrange
        MockIndividualEnrollment enrollment = makeMockedEnrollment();

        // act
        String result = enrollment.toJson();

        // assert
        Helpers.assertJson(enrollment.mockedJsonElement.toString(), result);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_013: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
    @Test
    public void toJsonElementSimpleEnrollment()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

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
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"capabilities\":" +
                "    {" +
                "        \"iotEdge\":false" +
                "    }" +
                "}";

        // act
        JsonElement result = individualEnrollment.toJsonElement();

        // assert
        Helpers.assertJson(result.toString(), json);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_014: [If the initialTwin is not null, the toJsonElement shall include its content in the final JSON.] */
    @Test
    public void toJsonElementSimpleEnrollmentWithTwin()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();
        individualEnrollment.setInitialTwin(new TwinState(
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
                "  \"initialTwin\" : {\n" +
                "    \"tags\": {\n" +
                "      \"tag1\": \"valueTag1\",\n" +
                "      \"tag2\": \"valueTag2\"\n" +
                "    },\n" +
                "    \"properties\": {\n" +
                "      \"desired\": {\n" +
                "        \"prop1\": \"value1\",\n" +
                "        \"prop2\": \"value2\"\n" +
                "      }\n" +
                "    }\n" +
                "  }," +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"capabilities\": {\n" +
                "    \"iotEdge\": false\n" +
                "  }\n" +
                "}";

        // act
        JsonElement result = individualEnrollment.toJsonElement();

        // assert
        Helpers.assertJson(result.toString(), json);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_015: [The toString shall return a String with the information in this class in a pretty print JSON.] */
    @Test
    public void toStringSimpleEnrollmentWithTwin()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();
        individualEnrollment.setInitialTwin(new TwinState(
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
                "  \"initialTwin\" : {\n" +
                "    \"tags\": {\n" +
                "      \"tag1\": \"valueTag1\",\n" +
                "      \"tag2\": \"valueTag2\"\n" +
                "    },\n" +
                "    \"properties\": {\n" +
                "      \"desired\": {\n" +
                "        \"prop1\": \"value1\",\n" +
                "        \"prop2\": \"value2\"\n" +
                "      }\n" +
                "    }\n" +
                "  }," +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"capabilities\": {\n" +
                "    \"iotEdge\": false\n" +
                "  }\n" +
                "}";

        // act
        String result = individualEnrollment.toString();

        // assert
        Helpers.assertJson(result, json);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_016: [The getRegistrationId shall return a String with the stored registrationId.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_21_019: [The getDeviceId shall return a String with the stored deviceId.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_21_022: [The getDeviceRegistrationState shall return a DeviceRegistrationState with the stored registrationState.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_21_025: [The getAttestation shall return a AttestationMechanism with the stored attestation.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_21_028: [The getIotHubHostName shall return a String with the stored iotHubHostName.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_21_031: [The getInitialTwin shall return a TwinState with the stored initialTwin.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_21_034: [The getProvisioningStatus shall return a TwinState with the stored provisioningStatus.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_21_037: [The getCreatedDateTimeUtc shall return a Date with the stored createdDateTimeUtcDate.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_21_040: [The getLastUpdatedDateTimeUtc shall return a Date with the stored lastUpdatedDateTimeUtcDate.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_21_046: [The getEtag shall return a String with the stored etag.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_34_052: [If the device capabilities is not null, the constructor shall judge and store it using the IndividualEnrollment setter.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_34_053: [This function shall save the provided capabilities.] */
    @Test
    public void gettersSimpleEnrollment() throws ProvisioningServiceClientException
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"registrationState\": {\n" +
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
                "  \"capabilities\": {\n" +
                "    \"iotEdge\": \"true\"\n" +
                "  },\n" +
                "  \"initialTwin\" : {\n" +
                "    \"tags\": {\n" +
                "      \"tag1\": \"valueTag1\",\n" +
                "      \"tag2\": \"valueTag2\"\n" +
                "    },\n" +
                "    \"properties\": {\n" +
                "      \"desired\": {\n" +
                "        \"prop1\": \"value1\",\n" +
                "        \"prop2\": \"value2\"\n" +
                "      }\n" +
                "    }\n" +
                "  }," +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"etag\": \"" + VALID_ETAG + "\"\n" +
                "}";
        IndividualEnrollment individualEnrollment = new IndividualEnrollment(json);

        // act - assert
        assertTrue(individualEnrollment.getCapabilities().isIotEdge());
        assertEquals(VALID_REGISTRATION_ID, individualEnrollment.getRegistrationId());
        assertEquals(VALID_DEVICE_ID, individualEnrollment.getDeviceId());
        assertNotNull(individualEnrollment.getDeviceRegistrationState());
        assertNotNull(individualEnrollment.getAttestation());
        assertEquals(VALID_IOTHUB_HOST_NAME, individualEnrollment.getIotHubHostName());
        assertNotNull(individualEnrollment.getInitialTwin());
        assertEquals(ProvisioningStatus.ENABLED, individualEnrollment.getProvisioningStatus());
        Helpers.assertDateWithError(individualEnrollment.getCreatedDateTimeUtc(), VALID_DATE_AS_STRING);
        Helpers.assertDateWithError(individualEnrollment.getLastUpdatedDateTimeUtc(), VALID_DATE_AS_STRING);
        assertEquals(VALID_PARSED_ETAG, individualEnrollment.getEtag());
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_017: [The setRegistrationId shall throw IllegalArgumentException if the provided registrationId is {@code null}, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setRegistrationIdThrowsOnNull()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment, "setRegistrationId", new Class[] {String.class}, (String)null);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_017: [The setRegistrationId shall throw IllegalArgumentException if the provided registrationId is {@code null}, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setRegistrationIdThrowsOnEmpty()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment, "setRegistrationId", new Class[] {String.class}, "");

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_017: [The setRegistrationId shall throw IllegalArgumentException if the provided registrationId is {@code null}, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setRegistrationIdThrowsOnNotUtf8()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment, "setRegistrationId", new Class[] {String.class}, "\u1234-invalid");

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_017: [The setRegistrationId shall throw IllegalArgumentException if the provided registrationId is {@code null}, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setRegistrationIdThrowsOnInvalidChar()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment, "setRegistrationId", new Class[] {String.class}, "invalid&");

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_018: [The setRegistrationId shall store the provided registrationId.] */
    @Test
    public void setRegistrationIdSucceed()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();
        final String newRegistrationId = "NewRegistrationId";
        assertNotEquals(newRegistrationId, Deencapsulation.getField(individualEnrollment, "registrationId"));

        // act
        Deencapsulation.invoke(individualEnrollment, "setRegistrationId", new Class[] {String.class}, newRegistrationId);

        // assert
        assertEquals(newRegistrationId, Deencapsulation.getField(individualEnrollment, "registrationId"));
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_020: [The setDeviceId shall throw IllegalArgumentException if the provided deviceId is {@code null}, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setDeviceIdThrowsOnNull()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment, "setDeviceId", new Class[] {String.class}, (String)null);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_020: [The setDeviceId shall throw IllegalArgumentException if the provided deviceId is {@code null}, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setDeviceIdThrowsOnEmpty()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment, "setDeviceId", new Class[] {String.class}, "");

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_021: [The setDeviceId shall store the provided deviceId.] */
    @Test
    public void setDeviceIdSucceed()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();
        final String newDeviceId = "NewDeviceId";
        assertNotEquals(newDeviceId, Deencapsulation.getField(individualEnrollment, "deviceId"));

        // act
        Deencapsulation.invoke(individualEnrollment, "setDeviceId", new Class[] {String.class}, newDeviceId);

        // assert
        assertEquals(newDeviceId, Deencapsulation.getField(individualEnrollment, "deviceId"));
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_023: [The setRegistrationState shall throw IllegalArgumentException if the provided registrationState is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setRegistrationStateThrowsOnNull()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment, "setRegistrationState", new Class[] {DeviceRegistrationState.class}, (DeviceRegistrationState)null);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_024: [The setRegistrationState shall store the provided registrationState.] */
    @Test
    public void setRegistrationStateSucceed(@Mocked final DeviceRegistrationState mockedDeviceRegistrationState)
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();
        assertNotEquals(mockedDeviceRegistrationState, Deencapsulation.getField(individualEnrollment, "registrationState"));

        // act
        Deencapsulation.invoke(individualEnrollment, "setRegistrationState", new Class[] {DeviceRegistrationState.class}, mockedDeviceRegistrationState);

        // assert
        assertEquals(mockedDeviceRegistrationState, Deencapsulation.getField(individualEnrollment, "registrationState"));
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_026: [The setAttestation shall throw IllegalArgumentException if the attestation is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setAttestationMechanismThrowsOnNull()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment, "setAttestation", new Class[]{AttestationMechanism.class}, (AttestationMechanism)null);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_027: [The setAttestation shall store the provided attestation.] */
    @Test
    public void setAttestationMechanismSucceed(
            @Mocked final TpmAttestation mockedTpmAttestation,
            @Mocked final AttestationMechanism mockedAttestationMechanism)
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();
        assertNotEquals(mockedAttestationMechanism, Deencapsulation.getField(individualEnrollment, "attestation"));

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedAttestationMechanism, "getAttestation");
                result = mockedTpmAttestation;
            }
        };

        // act
        Deencapsulation.invoke(individualEnrollment, "setAttestation", mockedAttestationMechanism);

        // assert
        assertNotNull(Deencapsulation.getField(individualEnrollment, "attestation"));
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_050: [The setAttestation shall throw IllegalArgumentException if the attestation is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setAttestationThrowsOnNull()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        individualEnrollment.setAttestation(null);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_051: [The setAttestation shall store the provided attestation using the AttestationMechanism object.] */
    @Test
    public void setAttestationSucceed(
            @Mocked final TpmAttestation mockedTpmAttestation,
            @Mocked final AttestationMechanism mockedAttestationMechanism)
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        individualEnrollment.setAttestation(mockedTpmAttestation);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(AttestationMechanism.class, new Class[]{Attestation.class}, mockedTpmAttestation);
                times = 1;
            }
        };
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_029: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnNull()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        individualEnrollment.setIotHubHostNameFinal(null);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_029: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnEmpty()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        individualEnrollment.setIotHubHostNameFinal("");

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_029: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnNotUTF8()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        individualEnrollment.setIotHubHostNameFinal("NewHostName.\u1234a.b");

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_029: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnInvalidChar()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        individualEnrollment.setIotHubHostNameFinal("NewHostName.&a.b");

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_029: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnIncompleteName()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        individualEnrollment.setIotHubHostNameFinal("NewHostName");

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_030: [The setIotHubHostName shall store the provided iotHubHostName.] */
    @Test
    public void setIotHubHostNameSucceed()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();
        final String newHostName = "NewHostName.azureDevice.net";
        assertNotEquals(newHostName, Deencapsulation.getField(individualEnrollment, "iotHubHostName"));

        // act
        individualEnrollment.setIotHubHostNameFinal(newHostName);

        // assert
        assertEquals(newHostName, Deencapsulation.getField(individualEnrollment, "iotHubHostName"));
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_032: [The setInitialTwin shall throw IllegalArgumentException if the initialTwin is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setInitialTwinThrowsOnNull()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        individualEnrollment.setInitialTwin(null);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_033: [The setInitialTwin shall store the provided initialTwin.] */
    @Test
    public void setInitialTwinSucceed(@Mocked final TwinState mockedTwinState)
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();
        assertNotEquals(mockedTwinState, Deencapsulation.getField(individualEnrollment, "initialTwin"));

        // act
        individualEnrollment.setInitialTwin(mockedTwinState);

        // assert
        assertEquals(mockedTwinState, Deencapsulation.getField(individualEnrollment, "initialTwin"));
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_035: [The setProvisioningStatus shall throw IllegalArgumentException if the provisioningStatus is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setProvisioningStatusThrowsOnNull()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        individualEnrollment.setProvisioningStatusFinal(null);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_036: [The setProvisioningStatus shall store the provided provisioningStatus.] */
    @Test
    public void setProvisioningStatusSucceed()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();
        assertNotEquals(ProvisioningStatus.DISABLED, Deencapsulation.getField(individualEnrollment, "provisioningStatus"));

        // act
        individualEnrollment.setProvisioningStatusFinal(ProvisioningStatus.DISABLED);

        // assert
        assertEquals(ProvisioningStatus.DISABLED, Deencapsulation.getField(individualEnrollment, "provisioningStatus"));
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_038: [The setCreatedDateTimeUtc shall parse the provided String as a Data and Time UTC.] */
    @Test
    public void setCreatedDateTimeUtcSucceed()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();
        assertNull(Deencapsulation.getField(individualEnrollment, "createdDateTimeUtcDate"));

        // act
        Deencapsulation.invoke(individualEnrollment,"setCreatedDateTimeUtc", new Class[] {String.class}, VALID_DATE_AS_STRING);

        // assert
        Helpers.assertDateWithError((Date)Deencapsulation.getField(individualEnrollment, "createdDateTimeUtcDate"), VALID_DATE_AS_STRING);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_039: [The setCreatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided createdDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setCreatedDateTimeUtcThrowsOnNull()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment,"setCreatedDateTimeUtc", new Class[] {String.class}, (String)null);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_039: [The setCreatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided createdDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setCreatedDateTimeUtcThrowsOnEmpty()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment,"setCreatedDateTimeUtc", new Class[] {String.class}, (String)"");

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_039: [The setCreatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided createdDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setCreatedDateTimeUtcThrowsOnInvalid()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment,"setCreatedDateTimeUtc", new Class[] {String.class}, (String)"0000-00-00 00:00:00");

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_041: [The setLastUpdatedDateTimeUtc shall parse the provided String as a Data and Time UTC.] */
    @Test
    public void setLastUpdatedDateTimeUtcSucceed()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();
        assertNull(Deencapsulation.getField(individualEnrollment, "lastUpdatedDateTimeUtcDate"));

        // act
        Deencapsulation.invoke(individualEnrollment,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, VALID_DATE_AS_STRING);

        // assert
        Helpers.assertDateWithError((Date)Deencapsulation.getField(individualEnrollment, "lastUpdatedDateTimeUtcDate"), VALID_DATE_AS_STRING);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_042: [The setLastUpdatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided lastUpdatedDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setLastUpdatedDateTimeUtcThrowsOnNull()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, (String)null);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_042: [The setLastUpdatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided lastUpdatedDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setLastUpdatedDateTimeUtcThrowsOnEmpty()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, (String)"");

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_042: [The setLastUpdatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided lastUpdatedDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setLastUpdatedDateTimeUtcThrowsOnInvalid()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, (String)"0000-00-00 00:00:00");

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_047: [The setEtag shall throw IllegalArgumentException if the etag is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEtagThrowsOnNull()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment, "setEtag", new Class[] {String.class}, (String)null);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_047: [The setEtag shall throw IllegalArgumentException if the etag is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEtagThrowsOnEmpty()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment, "setEtag", new Class[] {String.class}, (String)"");

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_047: [The setEtag shall throw IllegalArgumentException if the etag is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEtagThrowsOnNotUTF8()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment, "setEtag", new Class[] {String.class}, (String)"\u1234InvalidEtag");

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_048: [The setEtag shall store the provided etag.] */
    @Test
    public void setEtagSucceed()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();
        final String newEtag = "NewEtag";
        assertNotEquals(newEtag, Deencapsulation.getField(individualEnrollment, "etag"));

        // act
        Deencapsulation.invoke(individualEnrollment, "setEtag", new Class[] {String.class}, newEtag);

        // assert
        assertEquals(newEtag, Deencapsulation.getField(individualEnrollment, "etag"));
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_34_054: [This function shall return the saved capabilities.] */
    @Test
    public void setDeviceCapabilitiesSucceed()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();
        final DeviceCapabilities capabilities = new DeviceCapabilities();
        assertNotEquals(capabilities, Deencapsulation.getField(individualEnrollment, "capabilities"));

        // act
        Deencapsulation.invoke(individualEnrollment, "setCapabilities", new Class[] {DeviceCapabilities.class}, capabilities);

        // assert
        assertEquals(capabilities, Deencapsulation.getField(individualEnrollment, "capabilities"));
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_21_049: [The IndividualEnrollment shall provide an empty constructor to make GSON happy.] */
    @Test
    public void constructorSucceed()
    {
        // act
        IndividualEnrollment individualEnrollment = Deencapsulation.newInstance(IndividualEnrollment.class);

        // assert
        assertNotNull(individualEnrollment);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_34_069: [This function shall set the reprovision policy to the value from the json.] */
    @Test
    public void constructorWithJsonSetsReprovisioningPolicy()
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"reprovisionPolicy\": {\n" +
                "    \"updateHubAssignment\": \"true\",\n" +
                "    \"migrateDeviceData\": \"true\"\n" +
                "  },\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\",\n" +
                "      \"storageRootKey\": \"" + VALID_STORAGE_ROOT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"etag\": \"" + VALID_ETAG + "\"\n" +
                "}";

        // act
        IndividualEnrollment individualEnrollment = new IndividualEnrollment(json);

        // assert
        assertNotNull(individualEnrollment.getReprovisionPolicy());
        assertTrue(individualEnrollment.getReprovisionPolicy().getMigrateDeviceData());
        assertTrue(individualEnrollment.getReprovisionPolicy().getUpdateHubAssignment());
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_34_068: [This function shall set the custom allocation definition to the value from the json.] */
    @Test
    public void constructorWithJsonSetsCustomAllocationDefinition()
    {
        // arrange
        String expectedWebhookUrl = "https://www.microsoft.com";
        String expectedApiVersion = "2019-03-31";
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"customAllocationDefinition\": {\n" +
                "    \"webhookUrl\": \"" + expectedWebhookUrl + "\",\n" +
                "    \"apiVersion\": \"" + expectedApiVersion + "\"\n" +
                "  },\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\",\n" +
                "      \"storageRootKey\": \"" + VALID_STORAGE_ROOT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"etag\": \"" + VALID_ETAG + "\"\n" +
                "}";

        // act
        IndividualEnrollment individualEnrollment = new IndividualEnrollment(json);

        // assert
        assertNotNull(individualEnrollment.getCustomAllocationDefinition());
        assertEquals(expectedApiVersion, individualEnrollment.getCustomAllocationDefinition().getApiVersion());
        assertEquals(expectedWebhookUrl, individualEnrollment.getCustomAllocationDefinition().getWebhookUrl());
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_34_067: [This function shall set the allocation policy to the value from the json.] */
    @Test
    public void constructorWithJsonSetsAllocationPolicy()
    {
        // arrange
        AllocationPolicy expectedAllocationPolicy = AllocationPolicy.GEOLATENCY;
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"allocationPolicy\": \"" + expectedAllocationPolicy.toString() + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\",\n" +
                "      \"storageRootKey\": \"" + VALID_STORAGE_ROOT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"etag\": \"" + VALID_ETAG + "\"\n" +
                "}";

        // act
        IndividualEnrollment individualEnrollment = new IndividualEnrollment(json);

        // assert
        assertNotNull(individualEnrollment.getAllocationPolicy());
        assertEquals(expectedAllocationPolicy, individualEnrollment.getAllocationPolicy());
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_34_066: [This function shall set the iothubs list to the value from the json.] */
    @Test
    public void constructorWithJsonSetsIotHubs()
    {
        // arrange
        final String expectedIotHub1 = "some-iot-hub.azure-devices.net";
        final String expectedIotHub2 = "some-other-iot-hub.azure-devices.net";
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\",\n" +
                "      \"storageRootKey\": \"" + VALID_STORAGE_ROOT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"iotHubs\": [\"" + expectedIotHub1 + "\", \"" + expectedIotHub2 + "\"],\n" +
                "  \"etag\": \"" + VALID_ETAG + "\"\n" +
                "}";

        // act
        IndividualEnrollment individualEnrollment = new IndividualEnrollment(json);

        // assert
        assertNotNull(individualEnrollment.getIotHubs());
        assertTrue(individualEnrollment.getIotHubs().contains(expectedIotHub1));
        assertTrue(individualEnrollment.getIotHubs().contains(expectedIotHub2));
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_34_060: [This function shall set the allocation policy.] */
    @Test
    public void setAllocationPolicyWorks(final @Mocked SymmetricKeyAttestation mockedAttestation)
    {
        //arrange
        AllocationPolicy expectedAllocationPolicy = AllocationPolicy.CUSTOM;
        IndividualEnrollment individualEnrollment = new IndividualEnrollment("1234", mockedAttestation);

        //act
        individualEnrollment.setAllocationPolicy(expectedAllocationPolicy);

        //assert
        assertEquals(expectedAllocationPolicy, individualEnrollment.getAllocationPolicy());
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_34_059: [This function shall get the allocation policy.] */
    @Test
    public void getAllocationPolicyWorks(final @Mocked SymmetricKeyAttestation mockedAttestation)
    {
        //arrange
        AllocationPolicy expectedAllocationPolicy = AllocationPolicy.CUSTOM;
        IndividualEnrollment individualEnrollment = new IndividualEnrollment("1234", mockedAttestation);
        Deencapsulation.setField(individualEnrollment, "allocationPolicy", expectedAllocationPolicy);

        //act
        AllocationPolicy actualAllocationPolicy = individualEnrollment.getAllocationPolicy();

        //assert
        assertEquals(expectedAllocationPolicy, actualAllocationPolicy);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_34_064: [This function shall set the custom allocation definition.] */
    @Test
    public void setCustomAllocationDefinitionWorks(final @Mocked SymmetricKeyAttestation mockedAttestation)
    {
        //arrange
        CustomAllocationDefinition expectedCustomAllocationDefinition = new CustomAllocationDefinition();
        IndividualEnrollment individualEnrollment = new IndividualEnrollment("1234", mockedAttestation);

        //act
        individualEnrollment.setCustomAllocationDefinition(expectedCustomAllocationDefinition);

        //assert
        assertEquals(expectedCustomAllocationDefinition, individualEnrollment.getCustomAllocationDefinition());
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_34_063: [This function shall get the custom allocation definition.] */
    @Test
    public void getCustomAllocationDefinitionWorks(final @Mocked SymmetricKeyAttestation mockedAttestation)
    {
        //arrange
        CustomAllocationDefinition expectedCustomAllocationDefinition = new CustomAllocationDefinition();
        IndividualEnrollment individualEnrollment = new IndividualEnrollment("1234", mockedAttestation);
        Deencapsulation.setField(individualEnrollment, "customAllocationDefinition", expectedCustomAllocationDefinition);

        //act
        CustomAllocationDefinition actualCustomAllocationDefinition = individualEnrollment.getCustomAllocationDefinition();

        //assert
        assertEquals(expectedCustomAllocationDefinition, actualCustomAllocationDefinition);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_34_058: [This function shall set the reprovision policy.] */
    @Test
    public void setReprovisionPolicyWorks(final @Mocked SymmetricKeyAttestation mockedAttestation)
    {
        //arrange
        ReprovisionPolicy expectedReprovisionPolicy = new ReprovisionPolicy();
        IndividualEnrollment individualEnrollment = new IndividualEnrollment("1234", mockedAttestation);

        //act
        individualEnrollment.setReprovisionPolicy(expectedReprovisionPolicy);

        //assert
        assertEquals(expectedReprovisionPolicy, individualEnrollment.getReprovisionPolicy());
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_34_057: [This function shall get the reprovision policy.] */
    @Test
    public void getReprovisionPolicyWorks(final @Mocked SymmetricKeyAttestation mockedAttestation)
    {
        //arrange
        ReprovisionPolicy expectedReprovisionPolicy = new ReprovisionPolicy();
        IndividualEnrollment individualEnrollment = new IndividualEnrollment("1234", mockedAttestation);
        Deencapsulation.setField(individualEnrollment, "reprovisionPolicy", expectedReprovisionPolicy);

        //act
        ReprovisionPolicy actualReprovisionPolicy = individualEnrollment.getReprovisionPolicy();

        //assert
        assertEquals(expectedReprovisionPolicy, actualReprovisionPolicy);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_34_062: [This function shall set the iothubs list.] */
    @Test
    public void setIotHubsWorks(final @Mocked SymmetricKeyAttestation mockedAttestation)
    {
        //arrange
        List<String> expectedIotHubs = new ArrayList<>();
        IndividualEnrollment individualEnrollment = new IndividualEnrollment("1234", mockedAttestation);

        //act
        individualEnrollment.setIotHubs(expectedIotHubs);

        //assert
        assertEquals(expectedIotHubs, individualEnrollment.getIotHubs());
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_34_061: [This function shall get the iothubs list.] */
    @Test
    public void getIotHubsWorks(final @Mocked SymmetricKeyAttestation mockedAttestation)
    {
        //arrange
        List<String> expectedIotHubs = new ArrayList<>();
        IndividualEnrollment individualEnrollment = new IndividualEnrollment("1234", mockedAttestation);
        Deencapsulation.setField(individualEnrollment, "iotHubs", expectedIotHubs);

        //act
        Collection<String> actualIotHubs = individualEnrollment.getIotHubs();

        //assert
        assertEquals(expectedIotHubs, actualIotHubs);
    }
}
