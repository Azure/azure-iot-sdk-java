/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.parser;

import com.google.gson.JsonParseException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.RegistrationOperationStatusParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.TpmRegistrationResultParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/*
    Unit tests for  RegistrationOperationStatusParser, DeviceRegistrationResult,
    X509RegistrationResultParser, TpmRegistrationResultParser
    Coverage :
    RegistrationOperationStatusParser : 100% method, 100% line
    DeviceRegistrationResult: 100% method, 100% line
    X509RegistrationResultParser: 91% method, 92% line
    TpmRegistrationResultParser: 100% method, 100% line
 */
public class RegistrationOperationStatusParserTest
{
    private static final String TEST_REGISTRATION_ID = "testId";
    private static final String TEST_OPERATION_ID = "testOperationId";
    private static final String TEST_ASSIGNED_HUB = "testHub";
    private static final String TEST_DEVICE_ID = "testDevId";
    private static final String TEST_ENROLLMENT_GROUP_ID = "TEST_ENROLLMENT_GROUP";
    private static final String TEST_ERROR_CODE = "12345";
    private static final String TEST_ERROR_MESSAGE = "TestErrorMessage";

    // X509CertificateInfo constants
    private static final String TEST_ISSUER_NAME = "testSubjectName";
    private static final String TEST_SERIAL_NUMBER = "testSubjectName";
    private static final String TEST_SHA1_THUMBPRINT = "testSubjectName";
    private static final String TEST_SHA256_THUMBPRINT = "testSubjectName";
    private static final String TEST_SUBJECT_NAME = "testSubjectName";
    private static final String TEST_VERSION = "testSubjectName";

    // TpmRegistrationResult constants
    private static final String TEST_AUTH_KEY = "testSubjectName";

    //SRS_TpmRegistrationResultParser_25_001: [ This method returns the authentication key. ]
    //SRS_TpmRegistrationResultParser_25_003: [ The constructor shall build this object from the provided Json. ]
    @Test
    public void constructorForTPMRegistrationResultCreatesFromJson() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{" +
                "\"authenticationKey\":\"" + TEST_AUTH_KEY + "\"}";

        TpmRegistrationResultParser tpmRegistrationResultParser = TpmRegistrationResultParser.createFromJson(json);

        assertNotNull(tpmRegistrationResultParser.getAuthenticationKey());
        assertEquals(TEST_AUTH_KEY, tpmRegistrationResultParser.getAuthenticationKey());
    }

    //SRS_TpmRegistrationResultParser_25_002: [ The constructor shall throw IllegalArgumentException if the provided Json is null or empty. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorForTPMRegistrationResultCreatesFromJsonThrowsOnNull() throws IllegalArgumentException, JsonParseException
    {
        final String json = null;

        TpmRegistrationResultParser tpmRegistrationResultParser = TpmRegistrationResultParser.createFromJson(json);

    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorForTPMRegistrationResultCreatesFromJsonThrowsOnEmpty() throws IllegalArgumentException, JsonParseException
    {
        final String json = "";

        TpmRegistrationResultParser tpmRegistrationResultParser = TpmRegistrationResultParser.createFromJson(json);

    }

    //SRS_TpmRegistrationResultParser_25_004: [ The constructor shall throw IllegalArgumentException if the provided Json could not be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorForTPMRegistrationResultCreatesFromJsonThrowsOnMalformedJson() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{" +
                "\"authenticationKey\":\"" + TEST_AUTH_KEY + "\"";

        TpmRegistrationResultParser tpmRegistrationResultParser = TpmRegistrationResultParser.createFromJson(json);
    }

    @Test
    public void constructorWithoutRegistrationStateSucceed() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\""+ TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigning\"}";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);

        assertNotNull(operationsRegistrationOperationStatusParser.getOperationId());
        assertNotNull(operationsRegistrationOperationStatusParser.getStatus());
        assertNull(operationsRegistrationOperationStatusParser.getRegistrationState());
    }

    //SRS_RegistrationOperationStatusParser_25_004: [ This method shall throw IllegalArgumentException if operationId cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullOperationId() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{" +
                "\"status\":\"assigning\"}";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);

    }

    //SRS_RegistrationOperationStatusParser_25_001: [ This method shall throw IllegalArgumentException if provided Json is null or empty. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullJson() throws IllegalArgumentException, JsonParseException
    {
        final String json = null;

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyJson() throws IllegalArgumentException, JsonParseException
    {
        final String json = "";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }

    //SRS_RegistrationOperationStatusParser_25_002: [ This method shall parse the provided Json. ]
    @Test
    public void constructorWithNoHSMJsonSucceed() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigning\"," +
                "\"registrationState\":" +
                    "{\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                    "\"status\":\"assigning\"}" +
                "}";
        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);

        assertNotNull(operationsRegistrationOperationStatusParser.getOperationId());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getRegistrationId());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getStatus());
        assertNotNull(operationsRegistrationOperationStatusParser.getStatus());
        assertNull(operationsRegistrationOperationStatusParser.getRegistrationState().getTpm());
        assertNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509());
    }

    //SRS_DeviceRegistrationResultParser_25_010: [ This method shall return the parsed errorCode. ]
    //SRS_DeviceRegistrationResultParser_25_011: [ This method shall return the parsed errorMessage. ]
    @Test
    public void constructorWithErrorJsonSucceed() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigning\"," +
                "\"registrationState\":" +
                "{\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                "\"errorCode\":\"" + TEST_ERROR_CODE + "\"," +
                "\"errorMessage\":\"" + TEST_ERROR_MESSAGE + "\"," +
                "\"status\":\"assigning\"}" +
                "}";
        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);

        assertNotNull(operationsRegistrationOperationStatusParser.getOperationId());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getRegistrationId());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getStatus());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getErrorCode());
        assertEquals((Integer)Integer.parseInt(TEST_ERROR_CODE), operationsRegistrationOperationStatusParser.getRegistrationState().getErrorCode());
        assertNotNull(operationsRegistrationOperationStatusParser.getStatus());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getErrorMessage());
        assertEquals(TEST_ERROR_MESSAGE, operationsRegistrationOperationStatusParser.getRegistrationState().getErrorMessage());
        assertNotNull(operationsRegistrationOperationStatusParser.getStatus());
        assertNull(operationsRegistrationOperationStatusParser.getRegistrationState().getTpm());
        assertNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509());
    }

    //SRS_RegistrationOperationStatusParser_25_005: [ This method shall throw IllegalArgumentException if Registration Id cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullRegistrationId() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigning\"," +
                "\"registrationState\":" +
                "{" +
                "\"status\":\"assigning\"}" +
                "}";
        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);

    }

    //SRS_RegistrationOperationStatusParser_25_006: [ This method shall throw IllegalArgumentException if status cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullStatus() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigning\"," +
                "\"registrationState\":" +
                "{\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"" +
                "}" +
                "}";
        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }

    //SRS_X509RegistrationResultParser_25_001: [ This method shall return the parsed Subject name. ]
    //SRS_X509RegistrationResultParser_25_001: [ This method shall return the parsed Subject name. ]
    //SRS_X509RegistrationResultParser_25_003: [ This method shall return the parsed sha256Thumbprint. ]
    //SRS_X509RegistrationResultParser_25_004: [ This method shall return the parsed issuerName. ]
    //SRS_X509RegistrationResultParser_25_005: [ This method shall return the parsed notBeforeUtc time. ]
    //SRS_X509RegistrationResultParser_25_006: [ This method shall return the parsed notAfterUtc time. ]
    //SRS_X509RegistrationResultParser_25_007: [ This method shall return the parsed serialNumber. ]
    //SRS_X509RegistrationResultParser_25_008: [ This method shall return the parsed version. ]
    //SRS_X509RegistrationResultParser_25_009: [ This method shall return the parsed certificateInfo. ]
    //SRS_X509RegistrationResultParser_25_010: [ This method shall return the parsed signingCertificateInfo. ]
    //SRS_X509RegistrationResultParser_25_011: [ This method shall return the parsed enrollmentGroupId. ]
    //SRS_DeviceRegistrationResultParser_25_001: [ This method shall return the parsed registrationId. ]
    //SRS_DeviceRegistrationResultParser_25_002: [ This method shall return the parsed createdDateTimeUtc. ]
    //SRS_DeviceRegistrationResultParser_25_003: [ This method shall return the parsed assignedHub. ]
    //SRS_DeviceRegistrationResultParser_25_004: [ This method shall return the parsed deviceId. ]
    //SRS_DeviceRegistrationResultParser_25_005: [ This method shall return the parsed status. ]
    //SRS_DeviceRegistrationResultParser_25_006: [ This method shall return the parsed eTag. ]
    //SRS_DeviceRegistrationResultParser_25_007: [ This method shall return the parsed lastUpdatesDateTimeUtc. ]
    //SRS_DeviceRegistrationResultParser_25_009: [ This method shall return the parsed X509RegistrationResultParser object. ]
     @Test
    public void constructorWithX509JsonSucceed() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"registrationState\":" +
                    "{\"x509\":" +
                        "{\"certificateInfo\":" +
                            "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                            "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                            "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                            "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                            "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                            "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                            "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                            "\"version\":" + TEST_VERSION + "}," +
                        "\"enrollmentGroupId\":\"" + TEST_ENROLLMENT_GROUP_ID + "\"," +
                        "\"signingCertificateInfo\":" +
                            "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                            "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                            "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                            "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                            "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                            "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                            "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                            "\"version\":" + TEST_VERSION + "}" +
                        "}," +
                    "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                    "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                    "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                    "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                    "\"status\":\"assigned\"," +
                    "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                    "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);

        assertNotNull(operationsRegistrationOperationStatusParser.getOperationId());
        assertEquals(TEST_OPERATION_ID, operationsRegistrationOperationStatusParser.getOperationId());
        assertNotNull(operationsRegistrationOperationStatusParser.getStatus());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getRegistrationId());
        assertEquals(TEST_REGISTRATION_ID, operationsRegistrationOperationStatusParser.getRegistrationState().getRegistrationId());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getCreatedDateTimeUtc());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getAssignedHub());
        assertEquals(TEST_ASSIGNED_HUB, operationsRegistrationOperationStatusParser.getRegistrationState().getAssignedHub());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getDeviceId());
        assertEquals(TEST_DEVICE_ID, operationsRegistrationOperationStatusParser.getRegistrationState().getDeviceId());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getStatus());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getETag());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getLastUpdatesDateTimeUtc());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getCertificateInfo());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getCertificateInfo().getSubjectName());
        assertEquals(TEST_SUBJECT_NAME, operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getCertificateInfo().getSubjectName());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getCertificateInfo().getSha1Thumbprint());
        assertEquals(TEST_SHA1_THUMBPRINT, operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getCertificateInfo().getSha1Thumbprint());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getCertificateInfo().getSha256Thumbprint());
        assertEquals(TEST_SHA256_THUMBPRINT, operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getCertificateInfo().getSha256Thumbprint());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getCertificateInfo().getIssuerName());
        assertEquals(TEST_ISSUER_NAME, operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getCertificateInfo().getIssuerName());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getCertificateInfo().getNotBeforeUtc());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getCertificateInfo().getNotAfterUtc());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getCertificateInfo().getSerialNumber());
        assertEquals(TEST_SERIAL_NUMBER, operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getCertificateInfo().getSerialNumber());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getEnrollmentGroupId());
        assertEquals(TEST_ENROLLMENT_GROUP_ID, operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getEnrollmentGroupId());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getSigningCertificateInfo());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getSigningCertificateInfo().getSubjectName());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getSigningCertificateInfo().getSha1Thumbprint());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getSigningCertificateInfo().getSha256Thumbprint());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getSigningCertificateInfo().getIssuerName());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getSigningCertificateInfo().getNotBeforeUtc());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getSigningCertificateInfo().getNotAfterUtc());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getX509().getSigningCertificateInfo().getSerialNumber());
    }

    //SRS_RegistrationOperationStatusParser_25_007: [ This method shall throw IllegalArgumentException if Issuer Name from X509 Certificate Info cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509JsonThrowsOnNullIssuerName() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"registrationState\":" +
                "{\"x509\":" +
                "{\"certificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}," +
                "\"enrollmentGroupId\":\"" + TEST_ENROLLMENT_GROUP_ID + "\"," +
                "\"signingCertificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}" +
                "}," +
                "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);

    }

    //SRS_RegistrationOperationStatusParser_25_008: [ This method shall throw IllegalArgumentException if Subject Name from X509 Certificate Info cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509JsonThrowsOnNullSubjectName() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"registrationState\":" +
                "{\"x509\":" +
                "{\"certificateInfo\":" +
                "{" +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}," +
                "\"enrollmentGroupId\":\"" + TEST_ENROLLMENT_GROUP_ID + "\"," +
                "\"signingCertificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}" +
                "}," +
                "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);

    }

    //SRS_RegistrationOperationStatusParser_25_009: [ This method shall throw IllegalArgumentException if Sha1 Thumbprint from X509 Certificate Info cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509JsonThrowsOnNullSha1Thumbprint() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"registrationState\":" +
                "{\"x509\":" +
                "{\"certificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}," +
                "\"enrollmentGroupId\":\"" + TEST_ENROLLMENT_GROUP_ID + "\"," +
                "\"signingCertificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}" +
                "}," +
                "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }

    //SRS_RegistrationOperationStatusParser_25_010: [ This method shall throw IllegalArgumentException if SHA256 Thumbprint  from X509 Certificate Info cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509JsonThrowsOnNullSha256Thumbprint() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"registrationState\":" +
                "{\"x509\":" +
                "{\"certificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}," +
                "\"enrollmentGroupId\":\"" + TEST_ENROLLMENT_GROUP_ID + "\"," +
                "\"signingCertificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}" +
                "}," +
                "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }

    //SRS_RegistrationOperationStatusParser_25_011: [ This method shall throw IllegalArgumentException if NotBeforeUtc from X509 Certificate Info cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509JsonThrowsOnNullNotAfterUtc() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"registrationState\":" +
                "{\"x509\":" +
                "{\"certificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}," +
                "\"enrollmentGroupId\":\"" + TEST_ENROLLMENT_GROUP_ID + "\"," +
                "\"signingCertificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}" +
                "}," +
                "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }

    //SRS_RegistrationOperationStatusParser_25_012: [ This method shall throw IllegalArgumentException if NotAfterUtc from X509 Certificate Info cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509JsonThrowsOnNullNotBeforeUtc() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"registrationState\":" +
                "{\"x509\":" +
                "{\"certificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}," +
                "\"enrollmentGroupId\":\"" + TEST_ENROLLMENT_GROUP_ID + "\"," +
                "\"signingCertificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}" +
                "}," +
                "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }

    //SRS_RegistrationOperationStatusParser_25_013: [ This method shall throw IllegalArgumentException if Serial Number from X509 Certificate Info cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509JsonThrowsOnNullSerialNumber() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"registrationState\":" +
                "{\"x509\":" +
                "{\"certificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"version\":" + TEST_VERSION + "}," +
                "\"enrollmentGroupId\":\"" + TEST_ENROLLMENT_GROUP_ID + "\"," +
                "\"signingCertificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}" +
                "}," +
                "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }

    //SRS_RegistrationOperationStatusParser_25_014: [ This method shall throw IllegalArgumentException if version from X509 Certificate Info cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509JsonThrowsOnNullVersion() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"registrationState\":" +
                "{\"x509\":" +
                "{\"certificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"" +
                "}," +
                "\"enrollmentGroupId\":\"" + TEST_ENROLLMENT_GROUP_ID + "\"," +
                "\"signingCertificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}" +
                "}," +
                "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }

    //SRS_RegistrationOperationStatusParser_25_015: [ This method shall throw IllegalArgumentException if Issuer Name from X509 Signing Certificate Info cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509JsonThrowsOnNullIssuerNameSigningCert() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"registrationState\":" +
                "{\"x509\":" +
                "{\"certificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}," +
                "\"enrollmentGroupId\":\"" + TEST_ENROLLMENT_GROUP_ID + "\"," +
                "\"signingCertificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}" +
                "}," +
                "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }

    //SRS_RegistrationOperationStatusParser_25_016: [ This method shall throw IllegalArgumentException if Subject Name from X509 Signing Certificate Info cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509JsonThrowsOnNullSubjectNameSigningCert() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"registrationState\":" +
                "{\"x509\":" +
                "{\"certificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}," +
                "\"enrollmentGroupId\":\"" + TEST_ENROLLMENT_GROUP_ID + "\"," +
                "\"signingCertificateInfo\":" +
                "{" +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}" +
                "}," +
                "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }

    //SRS_RegistrationOperationStatusParser_25_017: [ This method shall throw IllegalArgumentException if Sha1 Thumbprint from X509 Signing Certificate Info cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509JsonThrowsOnNullSha1ThumbprintSigningCert() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"registrationState\":" +
                "{\"x509\":" +
                "{\"certificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}," +
                "\"enrollmentGroupId\":\"" + TEST_ENROLLMENT_GROUP_ID + "\"," +
                "\"signingCertificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}" +
                "}," +
                "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }

    //SRS_RegistrationOperationStatusParser_25_018: [ This method shall throw IllegalArgumentException if SHA256 Thumbprint from X509 Signing Certificate Info cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509JsonThrowsOnNullSha256ThumbprintSigningCert() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"registrationState\":" +
                "{\"x509\":" +
                "{\"certificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}," +
                "\"enrollmentGroupId\":\"" + TEST_ENROLLMENT_GROUP_ID + "\"," +
                "\"signingCertificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}" +
                "}," +
                "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }

    //SRS_RegistrationOperationStatusParser_25_019: [ This method shall throw IllegalArgumentException if Not before UTC time from X509 Signing Certificate Info cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509JsonThrowsOnNullNotAfterUtcSigningCert() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"registrationState\":" +
                "{\"x509\":" +
                "{\"certificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}," +
                "\"enrollmentGroupId\":\"" + TEST_ENROLLMENT_GROUP_ID + "\"," +
                "\"signingCertificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}" +
                "}," +
                "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }

    //SRS_RegistrationOperationStatusParser_25_020: [ This method shall throw IllegalArgumentException if Not After UTC  from X509 Signing Certificate Info cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509JsonThrowsOnNullNotBeforeUtcSigningCert() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"registrationState\":" +
                "{\"x509\":" +
                "{\"certificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}," +
                "\"enrollmentGroupId\":\"" + TEST_ENROLLMENT_GROUP_ID + "\"," +
                "\"signingCertificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}" +
                "}," +
                "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }

    //SRS_RegistrationOperationStatusParser_25_021: [ This method shall throw IllegalArgumentException if Serial Number from X509 Signing Certificate Info cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509JsonThrowsOnNullSerialNumberSigningCert() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"registrationState\":" +
                "{\"x509\":" +
                "{\"certificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}," +
                "\"enrollmentGroupId\":\"" + TEST_ENROLLMENT_GROUP_ID + "\"," +
                "\"signingCertificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"version\":" + TEST_VERSION + "}" +
                "}," +
                "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }

    //SRS_RegistrationOperationStatusParser_25_022: [ This method shall throw IllegalArgumentException if Version from X509 Signing Certificate Info cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithX509JsonThrowsOnNullVersionSigningCert() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"registrationState\":" +
                "{\"x509\":" +
                "{\"certificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"," +
                "\"version\":" + TEST_VERSION + "}," +
                "\"enrollmentGroupId\":\"" + TEST_ENROLLMENT_GROUP_ID + "\"," +
                "\"signingCertificateInfo\":" +
                "{\"subjectName\":\"" + TEST_SUBJECT_NAME + "\"," +
                "\"sha1Thumbprint\":\"" + TEST_SHA1_THUMBPRINT + "\"," +
                "\"sha256Thumbprint\":\""+ TEST_SHA256_THUMBPRINT +"\"," +
                "\"issuerName\":\"" + TEST_ISSUER_NAME + "\"," +
                "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                "\"serialNumber\":\"" + TEST_SERIAL_NUMBER + "\"" +
                "}" +
                "}," +
                "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                "\"status\":\"assigned\"," +
                "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }

    //SRS_DeviceRegistrationResultParser_25_008: [ This method shall return the parsed TpmRegistrationResultParser Object. ]
    //SRS_RegistrationOperationStatusParser_25_023: [ This method shall return operationId. ]
    //SRS_RegistrationOperationStatusParser_25_024: [ This method shall return status . ]
    //SRS_RegistrationOperationStatusParser_25_025: [ This method shall return DeviceRegistrationResultParser Object. ]
    @Test
    public void constructorWithTPMJsonSucceed() throws IllegalArgumentException
    {
        final String json = "" +
                "{" +
                    "\"operationId\":\"" + TEST_OPERATION_ID + "\"," +
                    "\"status\":\"assigned\"," +
                    "\"registrationState\":" +
                    "{" +
                        "\"tpm\":{" +
                        "\"authenticationKey\":\"" + TEST_AUTH_KEY + "\"" +
                        "}," +
                    "\"registrationId\":\"" + TEST_REGISTRATION_ID + "\"," +
                    "\"createdDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                    "\"assignedHub\":\"" + TEST_ASSIGNED_HUB + "\"," +
                    "\"deviceId\":\"" + TEST_DEVICE_ID + "\"," +
                    "\"status\":\"assigned\"," +
                    "\"etag\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                    "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);

        assertNotNull(operationsRegistrationOperationStatusParser.getOperationId());
        assertEquals(TEST_OPERATION_ID, operationsRegistrationOperationStatusParser.getOperationId());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getRegistrationId());
        assertEquals(TEST_REGISTRATION_ID, operationsRegistrationOperationStatusParser.getRegistrationState().getRegistrationId());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getCreatedDateTimeUtc());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getAssignedHub());
        assertEquals(TEST_ASSIGNED_HUB, operationsRegistrationOperationStatusParser.getRegistrationState().getAssignedHub());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getDeviceId());
        assertEquals(TEST_DEVICE_ID, operationsRegistrationOperationStatusParser.getRegistrationState().getDeviceId());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getStatus());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getETag());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getLastUpdatesDateTimeUtc());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getTpm());
        assertNotNull(operationsRegistrationOperationStatusParser.getRegistrationState().getTpm().getAuthenticationKey());
        assertEquals(TEST_AUTH_KEY, operationsRegistrationOperationStatusParser.getRegistrationState().getTpm().getAuthenticationKey());
        assertNotNull(operationsRegistrationOperationStatusParser.getStatus());
    }

    //SRS_RegistrationOperationStatusParser_25_003: [ This method shall throw IllegalArgumentException if Json cannot be parsed. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnMalformedJson() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\""+ TEST_OPERATION_ID + "\"," +
                "\"status\":\"assigning\"";

        RegistrationOperationStatusParser operationsRegistrationOperationStatusParser = RegistrationOperationStatusParser.createFromJson(json);
    }
}
