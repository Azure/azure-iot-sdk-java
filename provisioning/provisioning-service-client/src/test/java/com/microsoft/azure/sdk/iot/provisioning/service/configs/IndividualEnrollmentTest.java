// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.*;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import mockit.*;
import org.junit.Test;
import com.microsoft.azure.sdk.iot.provisioning.service.Helpers;

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

    private static final class MockIndividualEnrollment extends IndividualEnrollment
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

        @SuppressWarnings("SameParameterValue") // Since this is a constructor "registrationId" can be passed any value.
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
    }

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

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithJsonThrowsOnNullJson()
    {
        // arrange
        final String json = null;

        // act
        new MockIndividualEnrollment(json);

        // assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithJsonThrowsOnEmptyJson()
    {
        // arrange
        final String json = "";

        // act
        new MockIndividualEnrollment(json);

        // assert
    }

    @Test (expected = JsonSyntaxException.class)
    public void constructorWithJsonThrowsOnInvalidJson()
    {
        // arrange
        final String jsonWithExtraComma = "{\"a\":\"b\",}";

        // act
        new MockIndividualEnrollment(jsonWithExtraComma);

        // assert
    }

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
    @Test
    public void constructorWithJsonSetsOptionalDeviceInformation()
    {
        // arrange
        final String jsonOptional =
                "  {\n" +
                "    \"tag1\": \"valueTag1\",\n" +
                "    \"tag2\": \"valueTag2\"\n" +
                "  }";
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"deviceId\": \"" + VALID_DEVICE_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"optionalDeviceInformation\": " + jsonOptional + ",\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        // act
        IndividualEnrollment individualEnrollment = new IndividualEnrollment(json);

        // assert
        TwinCollection optionalDeviceInformation = individualEnrollment.getOptionalDeviceInformation();
        Helpers.assertJson(optionalDeviceInformation.toString(), jsonOptional);
    }

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
                "  \"optionalDeviceInformation\" : {\n" +
                "      \"test1\": \"testVal1\",\n" +
                "      \"test2\": \"testVal2\"\n" +
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

    @Test
    public void setOptionalDeviceInformationSucceed(@Mocked TwinCollection mockedOptionalDeviceInformation)
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();
        assertNotEquals(mockedOptionalDeviceInformation, Deencapsulation.getField(individualEnrollment, "optionalDeviceInformation"));

        // act
        Deencapsulation.invoke(individualEnrollment, "setOptionalDeviceInformation", new Class[] {TwinCollection.class}, mockedOptionalDeviceInformation);

        // assert
        assertEquals(mockedOptionalDeviceInformation, Deencapsulation.getField(individualEnrollment, "optionalDeviceInformation"));
    }

    @Test (expected = IllegalArgumentException.class)
    public void setOptionalDeviceInformationThrowsOnNull()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        individualEnrollment.setOptionalDeviceInformation((TwinCollection) null);
    }

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

    @Test (expected = IllegalArgumentException.class)
    public void setAttestationThrowsOnNull()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        individualEnrollment.setAttestation((Attestation) null);

        // assert
    }

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

    @Test (expected = IllegalArgumentException.class)
    public void setCreatedDateTimeUtcThrowsOnNull()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment,"setCreatedDateTimeUtc", new Class[] {String.class}, (String)null);

        // assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void setCreatedDateTimeUtcThrowsOnEmpty()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment,"setCreatedDateTimeUtc", new Class[] {String.class}, (String)"");

        // assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void setCreatedDateTimeUtcThrowsOnInvalid()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment,"setCreatedDateTimeUtc", new Class[] {String.class}, (String)"0000-00-00 00:00:00");

        // assert
    }

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

    @Test (expected = IllegalArgumentException.class)
    public void setLastUpdatedDateTimeUtcThrowsOnNull()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, (String)null);

        // assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void setLastUpdatedDateTimeUtcThrowsOnEmpty()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, (String)"");

        // assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void setLastUpdatedDateTimeUtcThrowsOnInvalid()
    {
        // arrange
        IndividualEnrollment individualEnrollment = makeStandardEnrollment();

        // act
        Deencapsulation.invoke(individualEnrollment,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, (String)"0000-00-00 00:00:00");

        // assert
    }

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

    @Test
    public void constructorSucceed()
    {
        // act
        IndividualEnrollment individualEnrollment = Deencapsulation.newInstance(IndividualEnrollment.class);

        // assert
        assertNotNull(individualEnrollment);
    }

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

    @Test
    public void constructorWithJsonSetsAllocationPolicyGeoLatency()
    {
        // arrange
        AllocationPolicy expectedAllocationPolicy = AllocationPolicy.GEOLATENCY;
        String expectedAllocationPolicyString = "geoLatency";
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"allocationPolicy\": \"" + expectedAllocationPolicyString + "\",\n" +
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

    @Test
    public void constructorWithJsonSetsAllocationPolicyStatic()
    {
        // arrange
        AllocationPolicy expectedAllocationPolicy = AllocationPolicy.STATIC;
        String expectedAllocationPolicyString = "static";
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"allocationPolicy\": \"" + expectedAllocationPolicyString + "\",\n" +
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

    @Test
    public void constructorWithJsonSetsAllocationPolicyCustom()
    {
        // arrange
        AllocationPolicy expectedAllocationPolicy = AllocationPolicy.CUSTOM;
        String expectedAllocationPolicyString = "custom";
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"allocationPolicy\": \"" + expectedAllocationPolicyString + "\",\n" +
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

    @Test
    public void constructorWithJsonSetsAllocationPolicyHashed()
    {
        // arrange
        AllocationPolicy expectedAllocationPolicy = AllocationPolicy.HASHED;
        String expectedAllocationPolicyString = "hashed";
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"allocationPolicy\": \"" + expectedAllocationPolicyString + "\",\n" +
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
