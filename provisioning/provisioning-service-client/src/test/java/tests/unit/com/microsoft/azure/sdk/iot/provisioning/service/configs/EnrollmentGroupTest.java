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
 * Unit tests for Device Provisioning Service Enrollment group serializer
 * 100% methods, 100% lines covered
 */
public class EnrollmentGroupTest
{
    private static final String VALID_ENROLLMENT_GROUP_ID = "8be9cd0e-8934-4991-9cbf-cc3b6c7ac647";
    private static final String VALID_IOTHUB_HOST_NAME = "foo.net";
    private static final String VALID_ETAG = "\\\"00000000-0000-0000-0000-00000000000\\\"";
    private static final String VALID_PARSED_ETAG = "\"00000000-0000-0000-0000-00000000000\"";

    //PEM encoded representation of the public key certificate
    private static final String PUBLIC_KEY_CERTIFICATE_STRING =
            "-----BEGIN CERTIFICATE-----\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "-----END CERTIFICATE-----\n";

    //PEM encoded representation of the private key
    private static final String PRIVATE_KEY_STRING =
            "-----BEGIN EC PRIVATE KEY-----\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "-----END EC PRIVATE KEY-----\n";

    private static final Date VALID_DATE = new Date(System.currentTimeMillis()/1000 * 1000);
    private static final String VALID_DATE_AS_STRING = ParserUtility.dateTimeUtcToString(VALID_DATE);

    private final class MockEnrollmentGroup extends EnrollmentGroup
    {
        String mockedEnrollmentGroupId;
        AttestationMechanism mockedAttestationMechanism;
        Attestation mockedAttestation;
        String mockedIotHubHostName;
        ProvisioningStatus mockedProvisioningStatus;
        TwinState mockedInitialTwin;
        String mockedCreatedDateTimeUtc;
        String mockedLastUpdatedDateTimeUtc;
        String mockedEtag;
        JsonObject mockedJsonElement;

        MockEnrollmentGroup(
                String enrollmentGroupId,
                Attestation attestation)
        {
            super(enrollmentGroupId, attestation);
        }

        MockEnrollmentGroup(String json)
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

    private static EnrollmentGroup makeStandardEnrollmentGroup()
    {
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup(
                VALID_ENROLLMENT_GROUP_ID,
                X509Attestation.createFromRootCertificates(PUBLIC_KEY_CERTIFICATE_STRING, null));
        enrollmentGroup.setIotHubHostNameFinal(VALID_IOTHUB_HOST_NAME);
        enrollmentGroup.setProvisioningStatusFinal(ProvisioningStatus.ENABLED);
        return enrollmentGroup;
    }

    private MockEnrollmentGroup makeMockedEnrollmentGroup()
    {
        return new MockEnrollmentGroup(
                VALID_ENROLLMENT_GROUP_ID,
                X509Attestation.createFromRootCertificates(PUBLIC_KEY_CERTIFICATE_STRING, null));
    }

    /* SRS_ENROLLMENT_GROUP_21_001: [The constructor shall judge and store the provided parameters using the EnrollmentGroup setters.] */
    @Test
    public void constructorWithParametersUsesSetters()
    {
        // arrange

        // act
        MockEnrollmentGroup enrollmentGroup = makeMockedEnrollmentGroup();

        // assert
        assertNotNull(enrollmentGroup);
        assertEquals(VALID_ENROLLMENT_GROUP_ID, Deencapsulation.getField(enrollmentGroup, "enrollmentGroupId"));
        assertNotNull(enrollmentGroup.mockedAttestation);
    }

    /* SRS_ENROLLMENT_GROUP_21_002: [The constructor shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithJsonThrowsOnNullJson()
    {
        // arrange
        final String json = null;

        // act
        new MockEnrollmentGroup(json);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_002: [The constructor shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithJsonThrowsOnEmptyJson()
    {
        // arrange
        final String json = "";

        // act
        new MockEnrollmentGroup(json);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_003: [The constructor shall throw JsonSyntaxException if the JSON is invalid.] */
    @Test (expected = JsonSyntaxException.class)
    public void constructorWithJsonThrowsOnInvalidJson()
    {
        // arrange
        final String jsonWithExtraComma = "{\"a\":\"b\",}";

        // act
        new MockEnrollmentGroup(jsonWithExtraComma);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_004: [The constructor shall deserialize the provided JSON for the enrollmentGroup class and subclasses.] */
    @Test
    public void constructorWithJsonSucceed() throws ProvisioningServiceClientException
    {
        // arrange
        final String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"x509\",\n" +
                "    \"x509\": {\n" +
                "      \"signingCertificates\": {\n" +
                "        \"primary\": {" +
                "          \"certificate\":\"" + PUBLIC_KEY_CERTIFICATE_STRING + "\"," +
                "          \"info\": {\n" +
                "            \"subjectName\": \"CN=ROOT_00000000-0000-0000-0000-000000000000, OU=Azure IoT, O=MSFT, C=US\",\n" +
                "            \"sha1Thumbprint\": \"000000000000000000000000000000000000000\",\n" +
                "            \"sha256Thumbprint\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "            \"issuerName\": \"CN=ROOT_00000000-0000-0000-0000-000000000000, OU=Azure IoT, O=MSFT, C=US\",\n" +
                "            \"notBeforeUtc\": \"2017-11-14T12:34:18Z\",\n" +
                "            \"notAfterUtc\": \"2017-11-20T12:34:18Z\",\n" +
                "            \"serialNumber\": \"0000000000000000\",\n" +
                "            \"version\": 3\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"initialTwin\" : {\n" +
                "    \"properties\": {\n" +
                "      \"desired\": {\n" +
                "        \"prop1\": \"value1\"\n" +
                "      }\n" +
                "    }\n" +
                "  }," +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        // act
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup(json);

        // assert
        assertEquals(VALID_ENROLLMENT_GROUP_ID, enrollmentGroup.getEnrollmentGroupId());
        assertEquals(VALID_IOTHUB_HOST_NAME, enrollmentGroup.getIotHubHostName());
        assertEquals(ProvisioningStatus.ENABLED, enrollmentGroup.getProvisioningStatus());
        Helpers.assertDateWithError(enrollmentGroup.getCreatedDateTimeUtc(), VALID_DATE_AS_STRING);
        Helpers.assertDateWithError(enrollmentGroup.getLastUpdatedDateTimeUtc(), VALID_DATE_AS_STRING);
        TwinState twinState = enrollmentGroup.getInitialTwin();
        assertNotNull(twinState);
        assertEquals("value1", twinState.getDesiredProperty().get("prop1"));
        Attestation attestation = enrollmentGroup.getAttestation();
        assertTrue("attestation is not x509", (attestation instanceof X509Attestation));
        X509Attestation x509Attestation = (X509Attestation)attestation;
        X509CertificateWithInfo primary = x509Attestation.getRootCertificatesFinal().getPrimaryFinal();
        assertEquals(PUBLIC_KEY_CERTIFICATE_STRING, primary.getCertificate());
        X509CertificateInfo info = primary.getInfo();
        assertEquals(VALID_ENROLLMENT_GROUP_ID, info.getSha256Thumbprint());
    }

    /* SRS_ENROLLMENT_GROUP_21_005: [The constructor shall judge and store the provided mandatory parameters `enrollmentGroupId` and `attestation` using the EnrollmentGroup setters.] */
    @Test
    public void constructorWithJsonUsesSetters() throws ProvisioningServiceClientException
    {
        // arrange
        final String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"x509\",\n" +
                "    \"x509\": {\n" +
                "      \"signingCertificates\": {\n" +
                "        \"primary\": {" +
                "          \"certificate\":\"" + PUBLIC_KEY_CERTIFICATE_STRING + "\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\"\n" +
                "}";

        // act
        MockEnrollmentGroup enrollmentGroup = new MockEnrollmentGroup(json);

        // assert
        assertEquals(VALID_ENROLLMENT_GROUP_ID, Deencapsulation.getField(enrollmentGroup, "enrollmentGroupId"));
        assertNotNull(enrollmentGroup.getAttestation());
    }

    /* SRS_ENROLLMENT_GROUP_21_006: [If the `iotHubHostName`, `initialTwin`, or `provisioningStatus` is not null, the constructor shall judge and store it using the EnrollmentGroup setter.] */
    @Test
    public void constructorWithJsonSetsInitialTwinUsesSetters()
    {
        // arrange
        final String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"x509\",\n" +
                "    \"x509\": {\n" +
                "      \"signingCertificates\": {\n" +
                "        \"primary\": {" +
                "          \"certificate\":\"" + PUBLIC_KEY_CERTIFICATE_STRING + "\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"initialTwin\" : {\n" +
                "    \"properties\": {\n" +
                "      \"desired\": {\n" +
                "        \"prop1\": \"value1\"\n" +
                "      }\n" +
                "    }\n" +
                "  }," +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        // act
        MockEnrollmentGroup enrollmentGroup = new MockEnrollmentGroup(json);

        // assert
        assertNotNull(Deencapsulation.getField(enrollmentGroup, "initialTwin"));
        assertEquals(VALID_IOTHUB_HOST_NAME, Deencapsulation.getField(enrollmentGroup, "iotHubHostName"));
        assertEquals(ProvisioningStatus.ENABLED, Deencapsulation.getField(enrollmentGroup, "provisioningStatus"));
    }

    /* SRS_ENROLLMENT_GROUP_21_006: [If the `iotHubHostName`, `initialTwin`, or `provisioningStatus` is not null, the constructor shall judge and store it using the EnrollmentGroup setter.] */
    @Test
    public void constructorWithJsonSetsInitialTwinSucceedOnNull()
    {
        // arrange
        final String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"x509\",\n" +
                "    \"x509\": {\n" +
                "      \"signingCertificates\": {\n" +
                "        \"primary\": {" +
                "          \"certificate\":\"" + PUBLIC_KEY_CERTIFICATE_STRING + "\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        // act
        MockEnrollmentGroup enrollmentGroup = new MockEnrollmentGroup(json);

        // assert
        assertNull(enrollmentGroup.mockedInitialTwin);
        assertNull(enrollmentGroup.mockedIotHubHostName);
        assertNull(enrollmentGroup.mockedProvisioningStatus);
    }

    /* SRS_ENROLLMENT_GROUP_21_007: [If the createdDateTimeUtc is not null, the constructor shall judge and store it using the EnrollmentGroup setter.] */
    @Test
    public void constructorWithJsonSetsCreatedDateTimeUtcUsesSetters()
    {
        // arrange
        final String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"x509\",\n" +
                "    \"x509\": {\n" +
                "      \"signingCertificates\": {\n" +
                "        \"primary\": {" +
                "          \"certificate\":\"" + PUBLIC_KEY_CERTIFICATE_STRING + "\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        // act
        MockEnrollmentGroup enrollmentGroup = new MockEnrollmentGroup(json);

        // assert
        assertEquals(VALID_DATE, Deencapsulation.getField(enrollmentGroup, "createdDateTimeUtcDate"));
    }

    /* SRS_ENROLLMENT_GROUP_21_007: [If the createdDateTimeUtc is not null, the constructor shall judge and store it using the EnrollmentGroup setter.] */
    @Test
    public void constructorWithJsonSetsCreatedDateTimeUtcSucceedOnNull()
    {
        // arrange
        final String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"x509\",\n" +
                "    \"x509\": {\n" +
                "      \"signingCertificates\": {\n" +
                "        \"primary\": {" +
                "          \"certificate\":\"" + PUBLIC_KEY_CERTIFICATE_STRING + "\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        // act
        MockEnrollmentGroup enrollmentGroup = new MockEnrollmentGroup(json);

        // assert
        assertNull(enrollmentGroup.mockedCreatedDateTimeUtc);
    }

    /* SRS_ENROLLMENT_GROUP_21_008: [If the lastUpdatedDateTimeUtc is not null, the constructor shall judge and store it using the EnrollmentGroup setter.] */
    @Test
    public void constructorWithJsonSetsLastUpdatedDateTimeUtcUsesSetters()
    {
        // arrange
        final String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"x509\",\n" +
                "    \"x509\": {\n" +
                "      \"signingCertificates\": {\n" +
                "        \"primary\": {" +
                "          \"certificate\":\"" + PUBLIC_KEY_CERTIFICATE_STRING + "\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        // act
        MockEnrollmentGroup enrollmentGroup = new MockEnrollmentGroup(json);

        // assert
        assertEquals(VALID_DATE, Deencapsulation.getField(enrollmentGroup, "lastUpdatedDateTimeUtcDate"));
    }

    /* SRS_ENROLLMENT_GROUP_21_008: [If the lastUpdatedDateTimeUtc is not null, the constructor shall judge and store it using the EnrollmentGroup setter.] */
    @Test
    public void constructorWithJsonSetsLastUpdatedDateTimeUtcSucceedOnNull()
    {
        // arrange
        final String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"x509\",\n" +
                "    \"x509\": {\n" +
                "      \"signingCertificates\": {\n" +
                "        \"primary\": {" +
                "          \"certificate\":\"" + PUBLIC_KEY_CERTIFICATE_STRING + "\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        // act
        MockEnrollmentGroup enrollmentGroup = new MockEnrollmentGroup(json);

        // assert
        assertNull(enrollmentGroup.mockedLastUpdatedDateTimeUtc);
    }

    /* SRS_ENROLLMENT_GROUP_21_009: [If the etag is not null, the constructor shall judge and store it using the EnrollmentGroup setter.] */
    @Test
    public void constructorWithJsonSetsEtagUsesSetters()
    {
        // arrange
        final String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"x509\",\n" +
                "    \"x509\": {\n" +
                "      \"signingCertificates\": {\n" +
                "        \"primary\": {" +
                "          \"certificate\":\"" + PUBLIC_KEY_CERTIFICATE_STRING + "\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"etag\": \"" + VALID_ETAG + "\"\n" +
                "}";

        // act
        MockEnrollmentGroup enrollmentGroup = new MockEnrollmentGroup(json);

        // assert
        assertEquals(VALID_PARSED_ETAG, Deencapsulation.getField(enrollmentGroup, "etag"));
    }

    /* SRS_ENROLLMENT_GROUP_21_009: [If the etag is not null, the constructor shall judge and store it using the EnrollmentGroup setter.] */
    @Test
    public void constructorWithJsonSetsEtagSucceedOnNull()
    {
        // arrange
        final String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"x509\",\n" +
                "    \"x509\": {\n" +
                "      \"signingCertificates\": {\n" +
                "        \"primary\": {" +
                "          \"certificate\":\"" + PUBLIC_KEY_CERTIFICATE_STRING + "\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\",\n" +
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\"\n" +
                "}";

        // act
        MockEnrollmentGroup enrollmentGroup = new MockEnrollmentGroup(json);

        // assert
        assertNull(enrollmentGroup.mockedEtag);
    }

    /* SRS_ENROLLMENT_GROUP_21_010: [The toJson shall return a String with the information in this class in a JSON format.] */
    @Test
    public void toJsonSimpleEnrollment()
    {
        // arrange
        MockEnrollmentGroup enrollmentGroup = makeMockedEnrollmentGroup();

        // act
        String result = enrollmentGroup.toJson();

        // assert
        Helpers.assertJson(enrollmentGroup.mockedJsonElement.toString(), result);
    }

    /* SRS_ENROLLMENT_GROUP_21_011: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
    @Test
    public void toJsonElementSimpleEnrollment()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"x509\",\n" +
                "    \"x509\": {\n" +
                "      \"signingCertificates\": {\n" +
                "        \"primary\": {" +
                "          \"certificate\":\"" + PUBLIC_KEY_CERTIFICATE_STRING + "\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"iotHubHostName\": \"" + VALID_IOTHUB_HOST_NAME + "\",\n" +
                "  \"provisioningStatus\": \"enabled\"\n" +
                "}";

        // act
        JsonElement result = enrollmentGroup.toJsonElement();

        // assert
        Helpers.assertJson(result.toString(), json);
    }

    /* SRS_ENROLLMENT_GROUP_21_012: [If the initialTwin is not null, the toJsonElement shall include its content in the final JSON.] */
    @Test
    public void toJsonElementSimpleEnrollmentWithTwin()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();
        enrollmentGroup.setInitialTwinFinal(new TwinState(
                new TwinCollection() {{
                    put("tag1", "valueTag1");
                    put("tag2", "valueTag2");
                }},
                new TwinCollection() {{
                    put("prop1", "value1");
                    put("prop2", "value2");
                }}));

        String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"x509\",\n" +
                "    \"x509\": {\n" +
                "      \"signingCertificates\": {\n" +
                "        \"primary\": {" +
                "          \"certificate\":\"" + PUBLIC_KEY_CERTIFICATE_STRING + "\"\n" +
                "        }\n" +
                "      }\n" +
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
                "  \"provisioningStatus\": \"enabled\"\n" +
                "}";

        // act
        JsonElement result = enrollmentGroup.toJsonElement();

        // assert
        Helpers.assertJson(result.toString(), json);
    }

    /* SRS_ENROLLMENT_GROUP_21_013: [The toString shall return a String with the information in this class in a pretty print JSON.] */
    @Test
    public void toStringSimpleEnrollmentWithTwin()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();
        enrollmentGroup.setInitialTwinFinal(new TwinState(
                new TwinCollection() {{
                    put("tag1", "valueTag1");
                    put("tag2", "valueTag2");
                }},
                new TwinCollection() {{
                    put("prop1", "value1");
                    put("prop2", "value2");
                }}));

        String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"x509\",\n" +
                "    \"x509\": {\n" +
                "      \"signingCertificates\": {\n" +
                "        \"primary\": {" +
                "          \"certificate\":\"" + PUBLIC_KEY_CERTIFICATE_STRING + "\"\n" +
                "        }\n" +
                "      }\n" +
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
                "  \"provisioningStatus\": \"enabled\"\n" +
                "}";

        // act
        String result = enrollmentGroup.toString();

        // assert
        Helpers.assertJson(result, json);
    }

    /* SRS_ENROLLMENT_GROUP_21_014: [The getEnrollmentGroupId shall return a String with the stored enrollmentGroupId.] */
    /* SRS_ENROLLMENT_GROUP_21_017: [The getAttestation shall return a Attestation with the stored attestation.] */
    /* SRS_ENROLLMENT_GROUP_21_020: [The getIotHubHostName shall return a String with the stored iotHubHostName.] */
    /* SRS_ENROLLMENT_GROUP_21_023: [The getInitialTwin shall return a TwinState with the stored initialTwin.] */
    /* SRS_ENROLLMENT_GROUP_21_026: [The getProvisioningStatus shall return a TwinState with the stored provisioningStatus.] */
    /* SRS_ENROLLMENT_GROUP_21_029: [The getCreatedDateTimeUtc shall return a Date with the stored createdDateTimeUtcDate.] */
    /* SRS_ENROLLMENT_GROUP_21_032: [The getLastUpdatedDateTimeUtc shall return a Date with the stored lastUpdatedDateTimeUtcDate.] */
    /* SRS_ENROLLMENT_GROUP_21_035: [The getEtag shall return a String with the stored etag.] */
    @Test
    public void gettersSimpleEnrollment() throws ProvisioningServiceClientException
    {
        // arrange
        final String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"x509\",\n" +
                "    \"x509\": {\n" +
                "      \"signingCertificates\": {\n" +
                "        \"primary\": {" +
                "          \"certificate\":\"" + PUBLIC_KEY_CERTIFICATE_STRING + "\"\n" +
                "        }\n" +
                "      }\n" +
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
                "  \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "  \"etag\": \"" + VALID_ETAG + "\"\n" +
                "}";
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup(json);

        // act - assert
        assertEquals(VALID_ENROLLMENT_GROUP_ID, enrollmentGroup.getEnrollmentGroupId());
        assertNotNull(enrollmentGroup.getAttestation());
        assertEquals(VALID_IOTHUB_HOST_NAME, enrollmentGroup.getIotHubHostName());
        assertNotNull(enrollmentGroup.getInitialTwin());
        assertEquals(ProvisioningStatus.ENABLED, enrollmentGroup.getProvisioningStatus());
        Helpers.assertDateWithError(enrollmentGroup.getCreatedDateTimeUtc(), VALID_DATE_AS_STRING);
        Helpers.assertDateWithError(enrollmentGroup.getLastUpdatedDateTimeUtc(), VALID_DATE_AS_STRING);
        assertEquals(VALID_PARSED_ETAG, enrollmentGroup.getEtag());
    }

    /* SRS_ENROLLMENT_GROUP_21_015: [The setEnrollmentGroupId shall throw IllegalArgumentException if the provided enrollmentGroupId is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEnrollmentGroupIdThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEnrollmentGroupId", new Class[] {String.class}, (String)null);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_015: [The setEnrollmentGroupId shall throw IllegalArgumentException if the provided enrollmentGroupId is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEnrollmentGroupIdThrowsOnEmpty()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEnrollmentGroupId", new Class[] {String.class}, "");

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_015: [The setEnrollmentGroupId shall throw IllegalArgumentException if the provided enrollmentGroupId is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEnrollmentGroupIdThrowsOnNotUtf8()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEnrollmentGroupId", new Class[] {String.class}, "\u1234-invalid");

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_015: [The setEnrollmentGroupId shall throw IllegalArgumentException if the provided enrollmentGroupId is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEnrollmentGroupIdThrowsOnInvalidChar()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEnrollmentGroupId", new Class[] {String.class}, "invalid&");

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_016: [The setEnrollmentGroupId shall store the provided enrollmentGroupId.] */
    @Test
    public void setEnrollmentGroupIdSucceed()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();
        final String newEnrollmentGroupId = "NewEnrollmentGroupId";
        assertNotEquals(newEnrollmentGroupId, Deencapsulation.getField(enrollmentGroup, "enrollmentGroupId"));

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEnrollmentGroupId", new Class[] {String.class}, newEnrollmentGroupId);

        // assert
        assertEquals(newEnrollmentGroupId, Deencapsulation.getField(enrollmentGroup, "enrollmentGroupId"));
    }

    /* SRS_ENROLLMENT_GROUP_21_018: [The setAttestation shall throw IllegalArgumentException if the attestation is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setAttestationMechanismThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup, "setAttestation", new Class[]{AttestationMechanism.class}, (AttestationMechanism)null);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_042: [The setAttestation shall throw IllegalArgumentException if the attestation is not X509 signingCertificate.] */
    @Test (expected = IllegalArgumentException.class)
    public void setAttestationMechanismThrowsOnTpm(
            @Mocked final TpmAttestation mockedTpmAttestation,
            @Mocked final AttestationMechanism mockedAttestationMechanismMechanism)
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedAttestationMechanismMechanism, "getAttestation");
                result = mockedTpmAttestation;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroup, "setAttestation", new Class[]{AttestationMechanism.class}, mockedAttestationMechanismMechanism);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_042: [The setAttestation shall throw IllegalArgumentException if the attestation is not X509 signingCertificate.] */
    @Test (expected = IllegalArgumentException.class)
    public void setAttestationMechanismThrowsOnNoSigningCertificate(
            @Mocked final X509Attestation mockedX509Attestation,
            @Mocked final AttestationMechanism mockedAttestationMechanismMechanism)
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedAttestationMechanismMechanism, "getAttestation");
                result = mockedX509Attestation;
                mockedX509Attestation.getRootCertificatesFinal();
                result = null;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroup, "setAttestation", new Class[]{AttestationMechanism.class}, mockedAttestationMechanismMechanism);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_019: [The setAttestation shall store the provided attestation.] */
    @Test
    public void setAttestationMechanismSucceed(
            @Mocked final X509Attestation mockedX509Attestation,
            @Mocked final X509Certificates mockedX509Certificates,
            @Mocked final AttestationMechanism mockedAttestationMechanismMechanism)
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();
        assertNotEquals(mockedAttestationMechanismMechanism, Deencapsulation.getField(enrollmentGroup, "attestation"));

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedAttestationMechanismMechanism, "getAttestation");
                result = mockedX509Attestation;
                mockedX509Attestation.getRootCertificatesFinal();
                result = mockedX509Certificates;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroup, "setAttestation", new Class[]{AttestationMechanism.class}, mockedAttestationMechanismMechanism);

        // assert
        assertNotNull(Deencapsulation.getField(enrollmentGroup, "attestation"));
    }

    /* SRS_ENROLLMENT_GROUP_21_039: [The setAttestation shall throw IllegalArgumentException if the attestation is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setAttestationThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        enrollmentGroup.setAttestation(null);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_040: [The setAttestation shall throw IllegalArgumentException if the attestation is not X509 signingCertificate.] */
    @Test (expected = IllegalArgumentException.class)
    public void setAttestationThrowsOnTpm(
            @Mocked final TpmAttestation mockedTpmAttestation,
            @Mocked final AttestationMechanism mockedAttestationMechanismMechanism)
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        enrollmentGroup.setAttestation(mockedTpmAttestation);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_040: [The setAttestation shall throw IllegalArgumentException if the attestation is not X509 signingCertificate.] */
    @Test (expected = IllegalArgumentException.class)
    public void setAttestationThrowsOnNoSigningCertificate(
            @Mocked final X509Attestation mockedX509Attestation,
            @Mocked final AttestationMechanism mockedAttestationMechanismMechanism)
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        new NonStrictExpectations()
        {
            {
                mockedX509Attestation.getRootCertificatesFinal();
                result = null;
            }
        };

        // act
        enrollmentGroup.setAttestation(mockedX509Attestation);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_041: [The setAttestation shall store the provided attestation using the AttestationMechanism object.] */
    @Test
    public void setAttestationSucceed(
            @Mocked final X509Attestation mockedX509Attestation,
            @Mocked final X509Certificates mockedX509Certificates,
            @Mocked final AttestationMechanism mockedAttestationMechanismMechanism)
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        new NonStrictExpectations()
        {
            {
                mockedX509Attestation.getRootCertificatesFinal();
                result = mockedX509Certificates;
            }
        };

        // act
        enrollmentGroup.setAttestation(mockedX509Attestation);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(AttestationMechanism.class, new Class[]{Attestation.class}, mockedX509Attestation);
                times = 1;
            }
        };
    }

    /* SRS_ENROLLMENT_GROUP_21_021: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        enrollmentGroup.setIotHubHostNameFinal(null);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_021: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnEmpty()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        enrollmentGroup.setIotHubHostNameFinal("");

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_021: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnNotUTF8()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        enrollmentGroup.setIotHubHostNameFinal("NewHostName.\u1234a.b");

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_021: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnInvalidChar()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        enrollmentGroup.setIotHubHostNameFinal("NewHostName.&a.b");

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_021: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnIncompleteName()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        enrollmentGroup.setIotHubHostNameFinal("NewHostName");

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_022: [The setIotHubHostName shall store the provided iotHubHostName.] */
    @Test
    public void setIotHubHostNameSucceed()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();
        final String newHostName = "NewHostName.azureDevice.net";
        assertNotEquals(newHostName, Deencapsulation.getField(enrollmentGroup, "iotHubHostName"));

        // act
        enrollmentGroup.setIotHubHostNameFinal(newHostName);

        // assert
        assertEquals(newHostName, Deencapsulation.getField(enrollmentGroup, "iotHubHostName"));
    }

    /* SRS_ENROLLMENT_GROUP_21_024: [The setInitialTwin shall throw IllegalArgumentException if the initialTwin is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setInitialTwinThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        enrollmentGroup.setInitialTwinFinal(null);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_025: [The setInitialTwin shall store the provided initialTwin.] */
    @Test
    public void setInitialTwinSucceed(@Mocked final TwinState mockedTwinState)
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();
        assertNotEquals(mockedTwinState, Deencapsulation.getField(enrollmentGroup, "initialTwin"));

        // act
        enrollmentGroup.setInitialTwinFinal(mockedTwinState);

        // assert
        assertEquals(mockedTwinState, Deencapsulation.getField(enrollmentGroup, "initialTwin"));
    }

    /* SRS_ENROLLMENT_GROUP_21_027: [The setProvisioningStatus shall throw IllegalArgumentException if the provisioningStatus is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setProvisioningStatusThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        enrollmentGroup.setProvisioningStatusFinal(null);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_028: [The setProvisioningStatus shall store the provided provisioningStatus.] */
    @Test
    public void setProvisioningStatusSucceed()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();
        assertNotEquals(ProvisioningStatus.DISABLED, Deencapsulation.getField(enrollmentGroup, "provisioningStatus"));

        // act
        enrollmentGroup.setProvisioningStatusFinal(ProvisioningStatus.DISABLED);

        // assert
        assertEquals(ProvisioningStatus.DISABLED, Deencapsulation.getField(enrollmentGroup, "provisioningStatus"));
    }

    /* SRS_ENROLLMENT_GROUP_21_030: [The setCreatedDateTimeUtc shall parse the provided String as a Data and Time UTC.] */
    @Test
    public void setCreatedDateTimeUtcSucceed()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();
        assertNull(Deencapsulation.getField(enrollmentGroup, "createdDateTimeUtcDate"));

        // act
        Deencapsulation.invoke(enrollmentGroup,"setCreatedDateTimeUtc", new Class[] {String.class}, VALID_DATE_AS_STRING);

        // assert
        Helpers.assertDateWithError((Date)Deencapsulation.getField(enrollmentGroup, "createdDateTimeUtcDate"), VALID_DATE_AS_STRING);
    }

    /* SRS_ENROLLMENT_GROUP_21_031: [The setCreatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided createdDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setCreatedDateTimeUtcThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup,"setCreatedDateTimeUtc", new Class[] {String.class}, (String)null);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_031: [The setCreatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided createdDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setCreatedDateTimeUtcThrowsOnEmpty()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup,"setCreatedDateTimeUtc", new Class[] {String.class}, (String)"");

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_031: [The setCreatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided createdDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setCreatedDateTimeUtcThrowsOnInvalid()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup,"setCreatedDateTimeUtc", new Class[] {String.class}, (String)"0000-00-00 00:00:00");

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_033: [The setLastUpdatedDateTimeUtc shall parse the provided String as a Data and Time UTC.] */
    @Test
    public void setLastUpdatedDateTimeUtcSucceed()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();
        assertNull(Deencapsulation.getField(enrollmentGroup, "lastUpdatedDateTimeUtcDate"));

        // act
        Deencapsulation.invoke(enrollmentGroup,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, VALID_DATE_AS_STRING);

        // assert
        Helpers.assertDateWithError((Date)Deencapsulation.getField(enrollmentGroup, "lastUpdatedDateTimeUtcDate"), VALID_DATE_AS_STRING);
    }

    /* SRS_ENROLLMENT_GROUP_21_034: [The setLastUpdatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided lastUpdatedDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setLastUpdatedDateTimeUtcThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, (String)null);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_034: [The setLastUpdatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided lastUpdatedDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setLastUpdatedDateTimeUtcThrowsOnEmpty()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, (String)"");

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_034: [The setLastUpdatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided lastUpdatedDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setLastUpdatedDateTimeUtcThrowsOnInvalid()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, (String)"0000-00-00 00:00:00");

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_036: [The setEtag shall throw IllegalArgumentException if the etag is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEtagThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEtag", new Class[] {String.class}, (String)null);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_036: [The setEtag shall throw IllegalArgumentException if the etag is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEtagThrowsOnEmpty()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEtag", new Class[] {String.class}, (String)"");

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_036: [The setEtag shall throw IllegalArgumentException if the etag is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEtagThrowsOnNotUTF8()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEtag", new Class[] {String.class}, (String)"\u1234InvalidEtag");

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_21_037: [The setEtag shall store the provided etag.] */
    @Test
    public void setEtagSucceed()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardEnrollmentGroup();
        final String newEtag = "NewEtag";
        assertNotEquals(newEtag, Deencapsulation.getField(enrollmentGroup, "etag"));

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEtag", new Class[] {String.class}, newEtag);

        // assert
        assertEquals(newEtag, Deencapsulation.getField(enrollmentGroup, "etag"));
    }

    /* SRS_ENROLLMENT_GROUP_21_038: [The EnrollmentGroup shall provide an empty constructor to make GSON happy.] */
    @Test
    public void constructorSucceed()
    {
        // act
        EnrollmentGroup enrollmentGroup = Deencapsulation.newInstance(EnrollmentGroup.class);

        // assert
        assertNotNull(enrollmentGroup);
    }

}
