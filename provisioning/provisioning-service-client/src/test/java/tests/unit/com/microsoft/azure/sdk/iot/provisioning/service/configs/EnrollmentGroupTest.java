// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.*;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.deps.util.Base64;
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
 * Unit tests for Device Provisioning Service Enrollment group serializer
 * 100% methods, 100% lines covered
 */
public class EnrollmentGroupTest
{
    private static final String VALID_ENROLLMENT_GROUP_ID = "8be9cd0e-8934-4991-9cbf-cc3b6c7ac647";
    private static final String VALID_IOTHUB_HOST_NAME = "foo.net";
    private static final String VALID_ETAG = "\\\"00000000-0000-0000-0000-00000000000\\\"";
    private static final String VALID_PARSED_ETAG = "\"00000000-0000-0000-0000-00000000000\"";
    private static final String PRIMARY_KEY_TEXT = "validPrimaryKey";
    private static final String SECONDARY_KEY_TEXT = "validSecondaryKey";
    private static final String VALID_PRIMARY_KEY = Base64.encodeBase64StringLocal(PRIMARY_KEY_TEXT.getBytes());
    private static final String VALID_SECONDARY_KEY = Base64.encodeBase64StringLocal(SECONDARY_KEY_TEXT.getBytes());

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

    private static EnrollmentGroup makeStandardX509EnrollmentGroup()
    {
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup(
                VALID_ENROLLMENT_GROUP_ID,
                X509Attestation.createFromRootCertificates(PUBLIC_KEY_CERTIFICATE_STRING, null));
        enrollmentGroup.setIotHubHostNameFinal(VALID_IOTHUB_HOST_NAME);
        enrollmentGroup.setProvisioningStatusFinal(ProvisioningStatus.ENABLED);
        return enrollmentGroup;
    }

    private MockEnrollmentGroup makeMockedX509EnrollmentGroup()
    {
        return new MockEnrollmentGroup(
                VALID_ENROLLMENT_GROUP_ID,
                X509Attestation.createFromRootCertificates(PUBLIC_KEY_CERTIFICATE_STRING, null));
    }

    private static EnrollmentGroup makeStandardSymmetricKeyEnrollmentGroup()
    {
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup(
                VALID_ENROLLMENT_GROUP_ID,
                new SymmetricKeyAttestation(VALID_PRIMARY_KEY, VALID_SECONDARY_KEY));
        enrollmentGroup.setIotHubHostName(VALID_IOTHUB_HOST_NAME);
        enrollmentGroup.setProvisioningStatus(ProvisioningStatus.ENABLED);
        return enrollmentGroup;
    }

    private MockEnrollmentGroup makeMockedSymmetricKeyEnrollmentGroup()
    {
        return new MockEnrollmentGroup(
                VALID_ENROLLMENT_GROUP_ID,
                new SymmetricKeyAttestation(VALID_PRIMARY_KEY, VALID_SECONDARY_KEY));
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_001: [The constructor shall judge and store the provided parameters using the EnrollmentGroup setters.] */
    @Test
    public void constructorWithX509ParametersUsesSetters()
    {
        // arrange

        // act
        MockEnrollmentGroup enrollmentGroup = makeMockedX509EnrollmentGroup();

        // assert
        assertNotNull(enrollmentGroup);
        assertEquals(VALID_ENROLLMENT_GROUP_ID, Deencapsulation.getField(enrollmentGroup, "enrollmentGroupId"));
        assertNotNull(enrollmentGroup.mockedAttestation);
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_001: [The constructor shall judge and store the provided parameters using the EnrollmentGroup setters.] */
    @Test
    public void constructorWithSymmetricKeyParametersUsesSetters()
    {
        // arrange

        // act
        MockEnrollmentGroup enrollmentGroup = makeMockedSymmetricKeyEnrollmentGroup();

        // assert
        assertNotNull(enrollmentGroup);
        assertEquals(VALID_ENROLLMENT_GROUP_ID, Deencapsulation.getField(enrollmentGroup, "enrollmentGroupId"));
        assertNotNull(enrollmentGroup.mockedAttestation);
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_002: [The constructor shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithJsonThrowsOnNullJson()
    {
        // arrange
        final String json = null;

        // act
        new MockEnrollmentGroup(json);

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_002: [The constructor shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithJsonThrowsOnEmptyJson()
    {
        // arrange
        final String json = "";

        // act
        new MockEnrollmentGroup(json);

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_003: [The constructor shall throw JsonSyntaxException if the JSON is invalid.] */
    @Test (expected = JsonSyntaxException.class)
    public void constructorWithJsonThrowsOnInvalidJson()
    {
        // arrange
        final String jsonWithExtraComma = "{\"a\":\"b\",}";

        // act
        new MockEnrollmentGroup(jsonWithExtraComma);

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_004: [The constructor shall deserialize the provided JSON for the enrollmentGroup class and subclasses.] */
    @Test
    public void constructorWithJsonSucceedX509Attestation() throws ProvisioningServiceClientException
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

    /* Tests_SRS_ENROLLMENT_GROUP_21_004: [The constructor shall deserialize the provided JSON for the enrollmentGroup class and subclasses.] */
    @Test
    public void constructorWithJsonSucceedSymmetricKeyAttestation() throws ProvisioningServiceClientException
    {
        // arrange
        final String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"symmetricKey\",\n" +
                "    \"symmetricKey\": {\n" +
                "      \"primaryKey\": \"" + VALID_PRIMARY_KEY + "\",\n" +
                "      \"secondaryKey\": \"" + VALID_SECONDARY_KEY + "\"\n" +
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
        assertTrue("attestation is not Symmetric Key", (attestation instanceof SymmetricKeyAttestation));
        SymmetricKeyAttestation symmetricKeyAttestation = (SymmetricKeyAttestation)attestation;
        String primary = symmetricKeyAttestation.getPrimaryKey();
        String secondary = symmetricKeyAttestation.getSecondaryKey();
        assertEquals(VALID_PRIMARY_KEY, primary);
        assertEquals(VALID_SECONDARY_KEY, secondary);
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_005: [The constructor shall judge and store the provided mandatory parameters `enrollmentGroupId` and `attestation` using the EnrollmentGroup setters.] */
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

    /* Tests_SRS_ENROLLMENT_GROUP_21_006: [If the `iotHubHostName`, `initialTwin`, or `provisioningStatus` is not null, the constructor shall judge and store it using the EnrollmentGroup setter.] */
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

    /* Tests_SRS_ENROLLMENT_GROUP_21_006: [If the `iotHubHostName`, `initialTwin`, or `provisioningStatus` is not null, the constructor shall judge and store it using the EnrollmentGroup setter.] */
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

    /* Tests_SRS_ENROLLMENT_GROUP_21_007: [If the createdDateTimeUtc is not null, the constructor shall judge and store it using the EnrollmentGroup setter.] */
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

    /* Tests_SRS_ENROLLMENT_GROUP_21_007: [If the createdDateTimeUtc is not null, the constructor shall judge and store it using the EnrollmentGroup setter.] */
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

    /* Tests_SRS_ENROLLMENT_GROUP_21_008: [If the lastUpdatedDateTimeUtc is not null, the constructor shall judge and store it using the EnrollmentGroup setter.] */
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

    /* Tests_SRS_ENROLLMENT_GROUP_21_008: [If the lastUpdatedDateTimeUtc is not null, the constructor shall judge and store it using the EnrollmentGroup setter.] */
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

    /* Tests_SRS_ENROLLMENT_GROUP_21_009: [If the etag is not null, the constructor shall judge and store it using the EnrollmentGroup setter.] */
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

    /* Tests_SRS_ENROLLMENT_GROUP_21_009: [If the etag is not null, the constructor shall judge and store it using the EnrollmentGroup setter.] */
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

    /* Tests_SRS_ENROLLMENT_GROUP_21_010: [The toJson shall return a String with the information in this class in a JSON format.] */
    @Test
    public void toJsonSimpleX509Enrollment()
    {
        // arrange
        MockEnrollmentGroup enrollmentGroup = makeMockedX509EnrollmentGroup();

        // act
        String result = enrollmentGroup.toJson();

        // assert
        Helpers.assertJson(enrollmentGroup.mockedJsonElement.toString(), result);
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_010: [The toJson shall return a String with the information in this class in a JSON format.] */
    @Test
    public void toJsonSimpleSymmetricKeyEnrollment()
    {
        // arrange
        MockEnrollmentGroup enrollmentGroup = makeMockedSymmetricKeyEnrollmentGroup();

        // act
        String result = enrollmentGroup.toJson();

        // assert
        Helpers.assertJson(enrollmentGroup.mockedJsonElement.toString(), result);
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_011: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
    @Test
    public void toJsonElementSimpleX509Enrollment()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

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

    /* Tests_SRS_ENROLLMENT_GROUP_21_011: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
    @Test
    public void toJsonElementSimpleSymmetricKeyEnrollment()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardSymmetricKeyEnrollmentGroup();

        String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"symmetricKey\",\n" +
                "    \"symmetricKey\": {\n" +
                "      \"primaryKey\": \"" + VALID_PRIMARY_KEY + "\",\n" +
                "      \"secondaryKey\": \"" + VALID_SECONDARY_KEY + "\"\n" +
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

    /* Tests_SRS_ENROLLMENT_GROUP_21_012: [If the initialTwin is not null, the toJsonElement shall include its content in the final JSON.] */
    @Test
    public void toJsonElementSimpleEnrollmentWithTwin()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();
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

    /* Tests_SRS_ENROLLMENT_GROUP_21_013: [The toString shall return a String with the information in this class in a pretty print JSON.] */
    @Test
    public void toStringSimpleEnrollmentWithTwin()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();
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

    /* Tests_SRS_ENROLLMENT_GROUP_21_014: [The getEnrollmentGroupId shall return a String with the stored enrollmentGroupId.] */
    /* Tests_SRS_ENROLLMENT_GROUP_21_017: [The getAttestation shall return a Attestation with the stored attestation.] */
    /* Tests_SRS_ENROLLMENT_GROUP_21_020: [The getIotHubHostName shall return a String with the stored iotHubHostName.] */
    /* Tests_SRS_ENROLLMENT_GROUP_21_023: [The getInitialTwin shall return a TwinState with the stored initialTwin.] */
    /* Tests_SRS_ENROLLMENT_GROUP_21_026: [The getProvisioningStatus shall return a TwinState with the stored provisioningStatus.] */
    /* Tests_SRS_ENROLLMENT_GROUP_21_029: [The getCreatedDateTimeUtc shall return a Date with the stored createdDateTimeUtcDate.] */
    /* Tests_SRS_ENROLLMENT_GROUP_21_032: [The getLastUpdatedDateTimeUtc shall return a Date with the stored lastUpdatedDateTimeUtcDate.] */
    /* Tests_SRS_ENROLLMENT_GROUP_21_035: [The getEtag shall return a String with the stored etag.] */
    @Test
    public void gettersSimpleX509Enrollment() throws ProvisioningServiceClientException
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

    /* Tests_SRS_ENROLLMENT_GROUP_21_014: [The getEnrollmentGroupId shall return a String with the stored enrollmentGroupId.] */
    /* Tests_SRS_ENROLLMENT_GROUP_21_017: [The getAttestation shall return a Attestation with the stored attestation.] */
    /* Tests_SRS_ENROLLMENT_GROUP_21_020: [The getIotHubHostName shall return a String with the stored iotHubHostName.] */
    /* Tests_SRS_ENROLLMENT_GROUP_21_023: [The getInitialTwin shall return a TwinState with the stored initialTwin.] */
    /* Tests_SRS_ENROLLMENT_GROUP_21_026: [The getProvisioningStatus shall return a TwinState with the stored provisioningStatus.] */
    /* Tests_SRS_ENROLLMENT_GROUP_21_029: [The getCreatedDateTimeUtc shall return a Date with the stored createdDateTimeUtcDate.] */
    /* Tests_SRS_ENROLLMENT_GROUP_21_032: [The getLastUpdatedDateTimeUtc shall return a Date with the stored lastUpdatedDateTimeUtcDate.] */
    /* Tests_SRS_ENROLLMENT_GROUP_21_035: [The getEtag shall return a String with the stored etag.] */
    @Test
    public void gettersSimpleSymmetricKeyEnrollment() throws ProvisioningServiceClientException
    {
        // arrange
        final String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"symmetricKey\",\n" +
                "    \"symmetricKey\": {\n" +
                "      \"primaryKey\": \"" + VALID_PRIMARY_KEY + "\",\n" +
                "      \"secondaryKey\": \"" + VALID_SECONDARY_KEY + "\"\n" +
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

    /* Tests_SRS_ENROLLMENT_GROUP_21_015: [The setEnrollmentGroupId shall throw IllegalArgumentException if the provided enrollmentGroupId is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEnrollmentGroupIdThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEnrollmentGroupId", new Class[] {String.class}, (String)null);

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_015: [The setEnrollmentGroupId shall throw IllegalArgumentException if the provided enrollmentGroupId is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEnrollmentGroupIdThrowsOnEmpty()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEnrollmentGroupId", new Class[] {String.class}, "");

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_015: [The setEnrollmentGroupId shall throw IllegalArgumentException if the provided enrollmentGroupId is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEnrollmentGroupIdThrowsOnNotUtf8()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEnrollmentGroupId", new Class[] {String.class}, "\u1234-invalid");

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_015: [The setEnrollmentGroupId shall throw IllegalArgumentException if the provided enrollmentGroupId is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEnrollmentGroupIdThrowsOnInvalidChar()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEnrollmentGroupId", new Class[] {String.class}, "invalid&");

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_016: [The setEnrollmentGroupId shall store the provided enrollmentGroupId.] */
    @Test
    public void setEnrollmentGroupIdSucceed()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();
        final String newEnrollmentGroupId = "NewEnrollmentGroupId";
        assertNotEquals(newEnrollmentGroupId, Deencapsulation.getField(enrollmentGroup, "enrollmentGroupId"));

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEnrollmentGroupId", new Class[] {String.class}, newEnrollmentGroupId);

        // assert
        assertEquals(newEnrollmentGroupId, Deencapsulation.getField(enrollmentGroup, "enrollmentGroupId"));
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_018: [The setAttestation shall throw IllegalArgumentException if the attestation is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setAttestationMechanismThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup, "setAttestation", new Class[]{AttestationMechanism.class}, (AttestationMechanism)null);

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_042: [The setAttestation shall throw IllegalArgumentException if the attestation is not X509 signingCertificate or Symmetric Key] */
    @Test (expected = IllegalArgumentException.class)
    public void setAttestationMechanismThrowsOnTpm(
            @Mocked final TpmAttestation mockedTpmAttestation,
            @Mocked final AttestationMechanism mockedAttestationMechanism)
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardSymmetricKeyEnrollmentGroup();

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedAttestationMechanism, "getAttestation");
                result = mockedTpmAttestation;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroup, "setAttestation", new Class[]{AttestationMechanism.class}, mockedAttestationMechanism);

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_042: [The setAttestation shall throw IllegalArgumentException if the attestation is not X509 signingCertificate or Symmetric Key] */
    @Test (expected = IllegalArgumentException.class)
    public void setAttestationMechanismX509ThrowsOnNoSigningCertificate(
            @Mocked final X509Attestation mockedX509Attestation,
            @Mocked final AttestationMechanism mockedAttestationMechanism)
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedAttestationMechanism, "getAttestation");
                result = mockedX509Attestation;
                mockedX509Attestation.getRootCertificatesFinal();
                result = null;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroup, "setAttestation", new Class[]{AttestationMechanism.class}, mockedAttestationMechanism);

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_019: [The setAttestation shall store the provided attestation.] */
    @Test
    public void setAttestationMechanismX509Succeed(
            @Mocked final X509Attestation mockedX509Attestation,
            @Mocked final X509Certificates mockedX509Certificates,
            @Mocked final AttestationMechanism mockedAttestationMechanism)
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();
        assertNotEquals(mockedAttestationMechanism, Deencapsulation.getField(enrollmentGroup, "attestation"));

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedAttestationMechanism, "getAttestation");
                result = mockedX509Attestation;
                mockedX509Attestation.getRootCertificatesFinal();
                result = mockedX509Certificates;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroup, "setAttestation", new Class[]{AttestationMechanism.class}, mockedAttestationMechanism);

        // assert
        assertNotNull(Deencapsulation.getField(enrollmentGroup, "attestation"));
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_019: [The setAttestation shall store the provided attestation.] */
    @Test
    public void setAttestationMechanismSymmetricKeySucceed(
            @Mocked final SymmetricKeyAttestation mockedSymmetricKeyAttestation,
            @Mocked final String mockedPrimaryKey,
            @Mocked final String mockedSecondaryKey,
            @Mocked final AttestationMechanism mockedAttestationMechanism)
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardSymmetricKeyEnrollmentGroup();
        assertNotEquals(mockedAttestationMechanism, Deencapsulation.getField(enrollmentGroup, "attestation"));

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedAttestationMechanism, "getAttestation");
                result = mockedSymmetricKeyAttestation;
                mockedSymmetricKeyAttestation.getPrimaryKey();
                result = mockedPrimaryKey;
                mockedSymmetricKeyAttestation.getSecondaryKey();
                result = mockedSecondaryKey;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroup, "setAttestation", new Class[]{AttestationMechanism.class}, mockedAttestationMechanism);

        // assert
        assertNotNull(Deencapsulation.getField(enrollmentGroup, "attestation"));
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_039: [The setAttestation shall throw IllegalArgumentException if the attestation is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setAttestationThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        enrollmentGroup.setAttestation(null);

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_040: [The setAttestation shall throw IllegalArgumentException if the attestation is not X509 signingCertificate or Symmetric Key] */
    @Test (expected = IllegalArgumentException.class)
    public void setAttestationThrowsOnTpm(
            @Mocked final TpmAttestation mockedTpmAttestation,
            @Mocked final AttestationMechanism mockedAttestationMechanism)
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        enrollmentGroup.setAttestation(mockedTpmAttestation);

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_040: [The setAttestation shall throw IllegalArgumentException if the attestation is not X509 signingCertificate or Symmetric Key] */
    @Test (expected = IllegalArgumentException.class)
    public void setAttestationX509ThrowsOnNoSigningCertificate(
            @Mocked final X509Attestation mockedX509Attestation,
            @Mocked final AttestationMechanism mockedAttestationMechanism)
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

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

    /* Tests_SRS_ENROLLMENT_GROUP_21_041: [The setAttestation shall store the provided attestation using the AttestationMechanism object.] */
    @Test
    public void setAttestationX509Succeed(
            @Mocked final X509Attestation mockedX509Attestation,
            @Mocked final X509Certificates mockedX509Certificates,
            @Mocked final AttestationMechanism mockedAttestationMechanism)
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

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

    /* Tests_SRS_ENROLLMENT_GROUP_21_041: [The setAttestation shall store the provided attestation using the AttestationMechanism object.] */
    @Test
    public void setAttestationSymmetricKeySucceed(
            @Mocked final SymmetricKeyAttestation mockedSymmetricKeyAttestation,
            @Mocked final AttestationMechanism mockedAttestationMechanism)
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardSymmetricKeyEnrollmentGroup();

        // act
        enrollmentGroup.setAttestation(mockedSymmetricKeyAttestation);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(AttestationMechanism.class, new Class[]{Attestation.class}, mockedSymmetricKeyAttestation);
                times = 1;
            }
        };
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_021: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        enrollmentGroup.setIotHubHostNameFinal(null);

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_021: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnEmpty()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        enrollmentGroup.setIotHubHostNameFinal("");

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_021: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnNotUTF8()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        enrollmentGroup.setIotHubHostNameFinal("NewHostName.\u1234a.b");

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_021: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnInvalidChar()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        enrollmentGroup.setIotHubHostNameFinal("NewHostName.&a.b");

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_021: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setIotHubHostNameThrowsOnIncompleteName()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        enrollmentGroup.setIotHubHostNameFinal("NewHostName");

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_022: [The setIotHubHostName shall store the provided iotHubHostName.] */
    @Test
    public void setIotHubHostNameSucceed()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();
        final String newHostName = "NewHostName.azureDevice.net";
        assertNotEquals(newHostName, Deencapsulation.getField(enrollmentGroup, "iotHubHostName"));

        // act
        enrollmentGroup.setIotHubHostNameFinal(newHostName);

        // assert
        assertEquals(newHostName, Deencapsulation.getField(enrollmentGroup, "iotHubHostName"));
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_024: [The setInitialTwin shall throw IllegalArgumentException if the initialTwin is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setInitialTwinThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        enrollmentGroup.setInitialTwinFinal(null);

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_025: [The setInitialTwin shall store the provided initialTwin.] */
    @Test
    public void setInitialTwinSucceed(@Mocked final TwinState mockedTwinState)
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();
        assertNotEquals(mockedTwinState, Deencapsulation.getField(enrollmentGroup, "initialTwin"));

        // act
        enrollmentGroup.setInitialTwinFinal(mockedTwinState);

        // assert
        assertEquals(mockedTwinState, Deencapsulation.getField(enrollmentGroup, "initialTwin"));
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_027: [The setProvisioningStatus shall throw IllegalArgumentException if the provisioningStatus is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void setProvisioningStatusThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        enrollmentGroup.setProvisioningStatusFinal(null);

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_028: [The setProvisioningStatus shall store the provided provisioningStatus.] */
    @Test
    public void setProvisioningStatusSucceed()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();
        assertNotEquals(ProvisioningStatus.DISABLED, Deencapsulation.getField(enrollmentGroup, "provisioningStatus"));

        // act
        enrollmentGroup.setProvisioningStatusFinal(ProvisioningStatus.DISABLED);

        // assert
        assertEquals(ProvisioningStatus.DISABLED, Deencapsulation.getField(enrollmentGroup, "provisioningStatus"));
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_030: [The setCreatedDateTimeUtc shall parse the provided String as a Data and Time UTC.] */
    @Test
    public void setCreatedDateTimeUtcSucceed()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();
        assertNull(Deencapsulation.getField(enrollmentGroup, "createdDateTimeUtcDate"));

        // act
        Deencapsulation.invoke(enrollmentGroup,"setCreatedDateTimeUtc", new Class[] {String.class}, VALID_DATE_AS_STRING);

        // assert
        Helpers.assertDateWithError((Date)Deencapsulation.getField(enrollmentGroup, "createdDateTimeUtcDate"), VALID_DATE_AS_STRING);
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_031: [The setCreatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided createdDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setCreatedDateTimeUtcThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup,"setCreatedDateTimeUtc", new Class[] {String.class}, (String)null);

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_031: [The setCreatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided createdDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setCreatedDateTimeUtcThrowsOnEmpty()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup,"setCreatedDateTimeUtc", new Class[] {String.class}, (String)"");

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_031: [The setCreatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided createdDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setCreatedDateTimeUtcThrowsOnInvalid()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup,"setCreatedDateTimeUtc", new Class[] {String.class}, (String)"0000-00-00 00:00:00");

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_033: [The setLastUpdatedDateTimeUtc shall parse the provided String as a Data and Time UTC.] */
    @Test
    public void setLastUpdatedDateTimeUtcSucceed()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();
        assertNull(Deencapsulation.getField(enrollmentGroup, "lastUpdatedDateTimeUtcDate"));

        // act
        Deencapsulation.invoke(enrollmentGroup,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, VALID_DATE_AS_STRING);

        // assert
        Helpers.assertDateWithError((Date)Deencapsulation.getField(enrollmentGroup, "lastUpdatedDateTimeUtcDate"), VALID_DATE_AS_STRING);
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_034: [The setLastUpdatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided lastUpdatedDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setLastUpdatedDateTimeUtcThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, (String)null);

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_034: [The setLastUpdatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided lastUpdatedDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setLastUpdatedDateTimeUtcThrowsOnEmpty()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, (String)"");

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_034: [The setLastUpdatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided lastUpdatedDateTimeUtc] */
    @Test (expected = IllegalArgumentException.class)
    public void setLastUpdatedDateTimeUtcThrowsOnInvalid()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup,"setLastUpdatedDateTimeUtc", new Class[] {String.class}, (String)"0000-00-00 00:00:00");

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_036: [The setEtag shall throw IllegalArgumentException if the etag is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEtagThrowsOnNull()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardX509EnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEtag", new Class[] {String.class}, (String)null);

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_036: [The setEtag shall throw IllegalArgumentException if the etag is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEtagThrowsOnEmpty()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardSymmetricKeyEnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEtag", new Class[] {String.class}, (String)"");

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_036: [The setEtag shall throw IllegalArgumentException if the etag is null, empty, or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void setEtagThrowsOnNotUTF8()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardSymmetricKeyEnrollmentGroup();

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEtag", new Class[] {String.class}, (String)"\u1234InvalidEtag");

        // assert
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_037: [The setEtag shall store the provided etag.] */
    @Test
    public void setEtagSucceed()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeStandardSymmetricKeyEnrollmentGroup();
        final String newEtag = "NewEtag";
        assertNotEquals(newEtag, Deencapsulation.getField(enrollmentGroup, "etag"));

        // act
        Deencapsulation.invoke(enrollmentGroup, "setEtag", new Class[] {String.class}, newEtag);

        // assert
        assertEquals(newEtag, Deencapsulation.getField(enrollmentGroup, "etag"));
    }

    /* Tests_SRS_ENROLLMENT_GROUP_21_038: [The EnrollmentGroup shall provide an empty constructor to make GSON happy.] */
    @Test
    public void constructorSucceed()
    {
        // act
        EnrollmentGroup enrollmentGroup = Deencapsulation.newInstance(EnrollmentGroup.class);

        // assert
        assertNotNull(enrollmentGroup);
    }

    /* Tests_SRS_ENROLLMENT_GROUP_34_046: [This function shall set the reprovision policy to the value from the json.] */
    @Test
    public void constructorWithJsonSetsReprovisioningPolicy()
    {
        // arrange
        final String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"reprovisionPolicy\": {\n" +
                "    \"updateHubAssignment\": \"true\",\n" +
                "    \"migrateDeviceData\": \"true\"\n" +
                "  },\n" +
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
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup(json);

        // assert
        assertNotNull(enrollmentGroup.getReprovisionPolicy());
        assertTrue(enrollmentGroup.getReprovisionPolicy().getMigrateDeviceData());
        assertTrue(enrollmentGroup.getReprovisionPolicy().getUpdateHubAssignment());
    }

    /* Tests_SRS_ENROLLMENT_GROUP_34_045: [This function shall set the custom allocation definition to the value from the json.] */
    @Test
    public void constructorWithJsonSetsCustomAllocationDefinition()
    {
        // arrange
        String expectedWebhookUrl = "https://www.microsoft.com";
        String expectedApiVersion = "2019-03-31";
        final String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"customAllocationDefinition\": {\n" +
                "    \"webhookUrl\": \"" + expectedWebhookUrl + "\",\n" +
                "    \"apiVersion\": \"" + expectedApiVersion + "\"\n" +
                "  },\n" +
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
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup(json);

        // assert
        assertNotNull(enrollmentGroup.getCustomAllocationDefinition());
        assertEquals(expectedApiVersion, enrollmentGroup.getCustomAllocationDefinition().getApiVersion());
        assertEquals(expectedWebhookUrl, enrollmentGroup.getCustomAllocationDefinition().getWebhookUrl());
    }

    /* Tests_SRS_ENROLLMENT_GROUP_34_044: [This function shall set the allocation policy to the value from the json.] */
    @Test
    public void constructorWithJsonSetsAllocationPolicy()
    {
        // arrange
        AllocationPolicy expectedAllocationPolicy = AllocationPolicy.GEOLATENCY;
        final String json = "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"allocationPolicy\": \"" + expectedAllocationPolicy.toString() + "\",\n" +
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
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup(json);

        // assert
        assertNotNull(enrollmentGroup.getAllocationPolicy());
        assertEquals(expectedAllocationPolicy, enrollmentGroup.getAllocationPolicy());
    }

    /* Tests_SRS_ENROLLMENT_GROUP_34_043: [This function shall set the iothubs list to the value from the json.] */
    @Test
    public void constructorWithJsonSetsIotHubs()
    {
        // arrange
        final String expectedIotHub1 = "some-iot-hub.azure-devices.net";
        final String expectedIotHub2 = "some-other-iot-hub.azure-devices.net";
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
                "  \"iotHubs\": [\"" + expectedIotHub1 + "\", \"" + expectedIotHub2 + "\"],\n" +
                "  \"etag\": \"" + VALID_ETAG + "\"\n" +
                "}";

        // act
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup(json);

        // assert
        assertNotNull(enrollmentGroup.getIotHubs());
        assertTrue(enrollmentGroup.getIotHubs().contains(expectedIotHub1));
        assertTrue(enrollmentGroup.getIotHubs().contains(expectedIotHub2));
    }

    /* Tests_SRS_ENROLLMENT_GROUP_34_050: [This function shall set the allocation policy.] */
    @Test
    public void setAllocationPolicyWorks(final @Mocked SymmetricKeyAttestation mockedAttestation)
    {
        //arrange
        AllocationPolicy expectedAllocationPolicy = AllocationPolicy.CUSTOM;
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup("1234", mockedAttestation);

        //act
        enrollmentGroup.setAllocationPolicy(expectedAllocationPolicy);

        //assert
        assertEquals(expectedAllocationPolicy, enrollmentGroup.getAllocationPolicy());
    }

    /* Tests_SRS_ENROLLMENT_GROUP_34_049: [This function shall get the allocation policy.] */
    @Test
    public void getAllocationPolicyWorks(final @Mocked SymmetricKeyAttestation mockedAttestation)
    {
        //arrange
        AllocationPolicy expectedAllocationPolicy = AllocationPolicy.CUSTOM;
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup("1234", mockedAttestation);
        Deencapsulation.setField(enrollmentGroup, "allocationPolicy", expectedAllocationPolicy);

        //act
        AllocationPolicy actualAllocationPolicy = enrollmentGroup.getAllocationPolicy();

        //assert
        assertEquals(expectedAllocationPolicy, actualAllocationPolicy);
    }

    /* Tests_SRS_ENROLLMENT_GROUP_34_054: [This function shall set the custom allocation definition.] */
    @Test
    public void setCustomAllocationDefinitionWorks(final @Mocked SymmetricKeyAttestation mockedAttestation)
    {
        //arrange
        CustomAllocationDefinition expectedCustomAllocationDefinition = new CustomAllocationDefinition();
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup("1234", mockedAttestation);

        //act
        enrollmentGroup.setCustomAllocationDefinition(expectedCustomAllocationDefinition);

        //assert
        assertEquals(expectedCustomAllocationDefinition, enrollmentGroup.getCustomAllocationDefinition());
    }

    /* Tests_SRS_ENROLLMENT_GROUP_34_053: [This function shall get the custom allocation definition.] */
    @Test
    public void getCustomAllocationDefinitionWorks(final @Mocked SymmetricKeyAttestation mockedAttestation)
    {
        //arrange
        CustomAllocationDefinition expectedCustomAllocationDefinition = new CustomAllocationDefinition();
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup("1234", mockedAttestation);
        Deencapsulation.setField(enrollmentGroup, "customAllocationDefinition", expectedCustomAllocationDefinition);

        //act
        CustomAllocationDefinition actualCustomAllocationDefinition = enrollmentGroup.getCustomAllocationDefinition();

        //assert
        assertEquals(expectedCustomAllocationDefinition, actualCustomAllocationDefinition);
    }

    /* Tests_SRS_ENROLLMENT_GROUP_34_048: [This function shall set the reprovision policy.] */
    @Test
    public void setReprovisionPolicyWorks(final @Mocked SymmetricKeyAttestation mockedAttestation)
    {
        //arrange
        ReprovisionPolicy expectedReprovisionPolicy = new ReprovisionPolicy();
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup("1234", mockedAttestation);

        //act
        enrollmentGroup.setReprovisionPolicy(expectedReprovisionPolicy);

        //assert
        assertEquals(expectedReprovisionPolicy, enrollmentGroup.getReprovisionPolicy());
    }

    /* Tests_SRS_ENROLLMENT_GROUP_34_047: [This function shall get the reprovision policy.] */
    @Test
    public void getReprovisionPolicyWorks(final @Mocked SymmetricKeyAttestation mockedAttestation)
    {
        //arrange
        ReprovisionPolicy expectedReprovisionPolicy = new ReprovisionPolicy();
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup("1234", mockedAttestation);
        Deencapsulation.setField(enrollmentGroup, "reprovisionPolicy", expectedReprovisionPolicy);

        //act
        ReprovisionPolicy actualReprovisionPolicy = enrollmentGroup.getReprovisionPolicy();

        //assert
        assertEquals(expectedReprovisionPolicy, actualReprovisionPolicy);
    }

    /* Tests_SRS_ENROLLMENT_GROUP_34_052: [This function shall set the iothubs list.] */
    @Test
    public void setIotHubsWorks(final @Mocked SymmetricKeyAttestation mockedAttestation)
    {
        //arrange
        List<String> expectedIotHubs = new ArrayList<>();
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup("1234", mockedAttestation);

        //act
        enrollmentGroup.setIotHubs(expectedIotHubs);

        //assert
        assertEquals(expectedIotHubs, enrollmentGroup.getIotHubs());
    }

    /* Tests_SRS_ENROLLMENT_GROUP_34_051: [This function shall get the iothubs list.] */
    @Test
    public void getIotHubsWorks(final @Mocked SymmetricKeyAttestation mockedAttestation)
    {
        //arrange
        List<String> expectedIotHubs = new ArrayList<>();
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup("1234", mockedAttestation);
        Deencapsulation.setField(enrollmentGroup, "iotHubs", expectedIotHubs);

        //act
        Collection<String> actualIotHubs = enrollmentGroup.getIotHubs();

        //assert
        assertEquals(expectedIotHubs, actualIotHubs);
    }

    /* Tests_SRS_ENROLLMENT_GROUP_34_073: [This function shall save the provided capabilities.] */
    @Test
    public void setDeviceCapabilitiesSucceed()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = makeMockedSymmetricKeyEnrollmentGroup();
        final DeviceCapabilities capabilities = new DeviceCapabilities();
        assertNotEquals(capabilities, Deencapsulation.getField(enrollmentGroup, "capabilities"));

        // act
        enrollmentGroup.setCapabilities(capabilities);

        // assert
        assertEquals(capabilities, Deencapsulation.getField(enrollmentGroup, "capabilities"));
    }

    /* Tests_SRS_ENROLLMENT_GROUP_34_074: [This function shall return the saved capabilities.] */
    @Test
    public void getDeviceCapabilitiesSucceed()
    {
        EnrollmentGroup enrollmentGroup = makeMockedSymmetricKeyEnrollmentGroup();
        final DeviceCapabilities capabilities = new DeviceCapabilities();
        Deencapsulation.setField(enrollmentGroup, "capabilities", capabilities);

        //act
        DeviceCapabilities actualDeviceCapabilities = enrollmentGroup.getCapabilities();

        //assert
        assertEquals(capabilities, actualDeviceCapabilities);
    }
}
