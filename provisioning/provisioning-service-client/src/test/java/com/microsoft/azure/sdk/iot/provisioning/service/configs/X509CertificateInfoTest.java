// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mockit.Deencapsulation;
import org.junit.Test;
import com.microsoft.azure.sdk.iot.provisioning.service.Helpers;

import static org.junit.Assert.*;

/**
 * Unit tests for Device Provisioning Service X509 certificate info
 * 100% methods, 100% lines covered
 */
public class X509CertificateInfoTest
{
    private static final String SUBJECT_NAME = "CN=ROOT_00000000-0000-0000-0000-000000000000, OU=Azure IoT, O=MSFT, C=US";
    private static final String SHA1THUMBPRINT = "0000000000000000000000000000000000";
    private static final String SHA256THUMBPRINT = "validEnrollmentGroupId";
    private static final String ISSUER_NAME = "CN=ROOT_00000000-0000-0000-0000-000000000000, OU=Azure IoT, O=MSFT, C=US";
    private static final String NOT_BEFORE_UTC = "2017-11-14T12:34:183Z";
    private static final String NOT_AFTER_UTC = "2017-11-20T12:34:182Z";
    private static final String SERIAL_NUMBER = "000000000000000000";
    private static final Integer VERSION = 3;
    private static final String JSON =
            "{\n" +
            "    \"subjectName\": \"" + SUBJECT_NAME + "\",\n" +
            "    \"sha1Thumbprint\": \"" + SHA1THUMBPRINT + "\",\n" +
            "    \"sha256Thumbprint\": \"" + SHA256THUMBPRINT + "\",\n" +
            "    \"issuerName\": \"" + ISSUER_NAME + "\",\n" +
            "    \"notBeforeUtc\": \"" + NOT_BEFORE_UTC + "\",\n" +
            "    \"notAfterUtc\": \"" + NOT_AFTER_UTC + "\",\n" +
            "    \"serialNumber\": \"" + SERIAL_NUMBER + "\",\n" +
            "    \"version\": " + VERSION + "\n" +
            "}\n";

    /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCopyThrowsOnNull()
    {
        // arrange
        // act
        new X509CertificateInfo(null);

        // assert
    }

    /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullSubjectName()
    {
        // arrange
        final String invalidJson =
                "{\n" +
                        "    \"sha1Thumbprint\": \"" + SHA1THUMBPRINT + "\",\n" +
                        "    \"sha256Thumbprint\": \"" + SHA256THUMBPRINT + "\",\n" +
                        "    \"issuerName\": \"" + ISSUER_NAME + "\",\n" +
                        "    \"notBeforeUtc\": \"" + NOT_BEFORE_UTC + "\",\n" +
                        "    \"notAfterUtc\": \"" + NOT_AFTER_UTC + "\",\n" +
                        "    \"serialNumber\": \"" + SERIAL_NUMBER + "\",\n" +
                        "    \"version\": " + VERSION + "\n" +
                        "}\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        X509CertificateInfo x509CertificateInfo = gson.fromJson(invalidJson, X509CertificateInfo.class);

        // act
        new X509CertificateInfo(x509CertificateInfo);

        // arrange
    }

    /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptySubjectName()
    {
        // arrange
        final String invalidJson =
                "{\n" +
                        "    \"subjectName\": \"\",\n" +
                        "    \"sha1Thumbprint\": \"" + SHA1THUMBPRINT + "\",\n" +
                        "    \"sha256Thumbprint\": \"" + SHA256THUMBPRINT + "\",\n" +
                        "    \"issuerName\": \"" + ISSUER_NAME + "\",\n" +
                        "    \"notBeforeUtc\": \"" + NOT_BEFORE_UTC + "\",\n" +
                        "    \"notAfterUtc\": \"" + NOT_AFTER_UTC + "\",\n" +
                        "    \"serialNumber\": \"" + SERIAL_NUMBER + "\",\n" +
                        "    \"version\": " + VERSION + "\n" +
                        "}\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        X509CertificateInfo x509CertificateInfo = gson.fromJson(invalidJson, X509CertificateInfo.class);

        // act
        new X509CertificateInfo(x509CertificateInfo);

        // arrange
    }

    /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullSha1Thumbprint()
    {
        // arrange
        final String invalidJson =
                "{\n" +
                        "    \"subjectName\": \"" + SUBJECT_NAME + "\",\n" +
                        "    \"sha256Thumbprint\": \"" + SHA256THUMBPRINT + "\",\n" +
                        "    \"issuerName\": \"" + ISSUER_NAME + "\",\n" +
                        "    \"notBeforeUtc\": \"" + NOT_BEFORE_UTC + "\",\n" +
                        "    \"notAfterUtc\": \"" + NOT_AFTER_UTC + "\",\n" +
                        "    \"serialNumber\": \"" + SERIAL_NUMBER + "\",\n" +
                        "    \"version\": " + VERSION + "\n" +
                        "}\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        X509CertificateInfo x509CertificateInfo = gson.fromJson(invalidJson, X509CertificateInfo.class);

        // act
        new X509CertificateInfo(x509CertificateInfo);

        // arrange
    }

    /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptySha1Thumbprint()
    {
        // arrange
        final String invalidJson =
                "{\n" +
                        "    \"subjectName\": \"" + SUBJECT_NAME + "\",\n" +
                        "    \"sha1Thumbprint\": \"\",\n" +
                        "    \"sha256Thumbprint\": \"" + SHA256THUMBPRINT + "\",\n" +
                        "    \"issuerName\": \"" + ISSUER_NAME + "\",\n" +
                        "    \"notBeforeUtc\": \"" + NOT_BEFORE_UTC + "\",\n" +
                        "    \"notAfterUtc\": \"" + NOT_AFTER_UTC + "\",\n" +
                        "    \"serialNumber\": \"" + SERIAL_NUMBER + "\",\n" +
                        "    \"version\": " + VERSION + "\n" +
                        "}\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        X509CertificateInfo x509CertificateInfo = gson.fromJson(invalidJson, X509CertificateInfo.class);

        // act
        new X509CertificateInfo(x509CertificateInfo);

        // arrange
    }

    /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullSha256Thumbprint()
    {
        // arrange
        final String invalidJson =
                "{\n" +
                        "    \"subjectName\": \"" + SUBJECT_NAME + "\",\n" +
                        "    \"sha1Thumbprint\": \"" + SHA1THUMBPRINT + "\",\n" +
                        "    \"issuerName\": \"" + ISSUER_NAME + "\",\n" +
                        "    \"notBeforeUtc\": \"" + NOT_BEFORE_UTC + "\",\n" +
                        "    \"notAfterUtc\": \"" + NOT_AFTER_UTC + "\",\n" +
                        "    \"serialNumber\": \"" + SERIAL_NUMBER + "\",\n" +
                        "    \"version\": " + VERSION + "\n" +
                        "}\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        X509CertificateInfo x509CertificateInfo = gson.fromJson(invalidJson, X509CertificateInfo.class);

        // act
        new X509CertificateInfo(x509CertificateInfo);

        // arrange
    }

    /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptySha256Thumbprint()
    {
        // arrange
        final String invalidJson =
                "{\n" +
                        "    \"subjectName\": \"" + SUBJECT_NAME + "\",\n" +
                        "    \"sha1Thumbprint\": \"" + SHA1THUMBPRINT + "\",\n" +
                        "    \"sha256Thumbprint\": \"\",\n" +
                        "    \"issuerName\": \"" + ISSUER_NAME + "\",\n" +
                        "    \"notBeforeUtc\": \"" + NOT_BEFORE_UTC + "\",\n" +
                        "    \"notAfterUtc\": \"" + NOT_AFTER_UTC + "\",\n" +
                        "    \"serialNumber\": \"" + SERIAL_NUMBER + "\",\n" +
                        "    \"version\": " + VERSION + "\n" +
                        "}\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        X509CertificateInfo x509CertificateInfo = gson.fromJson(invalidJson, X509CertificateInfo.class);

        // act
        new X509CertificateInfo(x509CertificateInfo);

        // arrange
    }

    /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullIssuerName()
    {
        // arrange
        final String invalidJson =
                "{\n" +
                        "    \"subjectName\": \"" + SUBJECT_NAME + "\",\n" +
                        "    \"sha1Thumbprint\": \"" + SHA1THUMBPRINT + "\",\n" +
                        "    \"sha256Thumbprint\": \"" + SHA256THUMBPRINT + "\",\n" +
                        "    \"notBeforeUtc\": \"" + NOT_BEFORE_UTC + "\",\n" +
                        "    \"notAfterUtc\": \"" + NOT_AFTER_UTC + "\",\n" +
                        "    \"serialNumber\": \"" + SERIAL_NUMBER + "\",\n" +
                        "    \"version\": " + VERSION + "\n" +
                        "}\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        X509CertificateInfo x509CertificateInfo = gson.fromJson(invalidJson, X509CertificateInfo.class);

        // act
        new X509CertificateInfo(x509CertificateInfo);

        // arrange
    }

    /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyIssuerName()
    {
        // arrange
        final String invalidJson =
                "{\n" +
                        "    \"subjectName\": \"" + SUBJECT_NAME + "\",\n" +
                        "    \"sha1Thumbprint\": \"" + SHA1THUMBPRINT + "\",\n" +
                        "    \"sha256Thumbprint\": \"" + SHA256THUMBPRINT + "\",\n" +
                        "    \"issuerName\": \"\",\n" +
                        "    \"notBeforeUtc\": \"" + NOT_BEFORE_UTC + "\",\n" +
                        "    \"notAfterUtc\": \"" + NOT_AFTER_UTC + "\",\n" +
                        "    \"serialNumber\": \"" + SERIAL_NUMBER + "\",\n" +
                        "    \"version\": " + VERSION + "\n" +
                        "}\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        X509CertificateInfo x509CertificateInfo = gson.fromJson(invalidJson, X509CertificateInfo.class);

        // act
        new X509CertificateInfo(x509CertificateInfo);

        // arrange
    }

    /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullNotBeforeUtc()
    {
        // arrange
        final String invalidJson =
                "{\n" +
                        "    \"subjectName\": \"" + SUBJECT_NAME + "\",\n" +
                        "    \"sha1Thumbprint\": \"" + SHA1THUMBPRINT + "\",\n" +
                        "    \"sha256Thumbprint\": \"" + SHA256THUMBPRINT + "\",\n" +
                        "    \"issuerName\": \"" + ISSUER_NAME + "\",\n" +
                        "    \"notAfterUtc\": \"" + NOT_AFTER_UTC + "\",\n" +
                        "    \"serialNumber\": \"" + SERIAL_NUMBER + "\",\n" +
                        "    \"version\": " + VERSION + "\n" +
                        "}\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        X509CertificateInfo x509CertificateInfo = gson.fromJson(invalidJson, X509CertificateInfo.class);

        // act
        new X509CertificateInfo(x509CertificateInfo);

        // arrange
    }

    /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyNotBeforeUtc()
    {
        // arrange
        final String invalidJson =
                "{\n" +
                        "    \"subjectName\": \"" + SUBJECT_NAME + "\",\n" +
                        "    \"sha1Thumbprint\": \"" + SHA1THUMBPRINT + "\",\n" +
                        "    \"sha256Thumbprint\": \"" + SHA256THUMBPRINT + "\",\n" +
                        "    \"issuerName\": \"" + ISSUER_NAME + "\",\n" +
                        "    \"notBeforeUtc\": \"\",\n" +
                        "    \"notAfterUtc\": \"" + NOT_AFTER_UTC + "\",\n" +
                        "    \"serialNumber\": \"" + SERIAL_NUMBER + "\",\n" +
                        "    \"version\": " + VERSION + "\n" +
                        "}\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        X509CertificateInfo x509CertificateInfo = gson.fromJson(invalidJson, X509CertificateInfo.class);

        // act
        new X509CertificateInfo(x509CertificateInfo);

        // arrange
    }

    /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidNotBeforeUtc()
    {
        // arrange
        final String invalidJson =
                "{\n" +
                        "    \"subjectName\": \"" + SUBJECT_NAME + "\",\n" +
                        "    \"sha1Thumbprint\": \"" + SHA1THUMBPRINT + "\",\n" +
                        "    \"sha256Thumbprint\": \"" + SHA256THUMBPRINT + "\",\n" +
                        "    \"issuerName\": \"" + ISSUER_NAME + "\",\n" +
                        "    \"notBeforeUtc\": \"0000-00-00 00:00:00\",\n" +
                        "    \"notAfterUtc\": \"" + NOT_AFTER_UTC + "\",\n" +
                        "    \"serialNumber\": \"" + SERIAL_NUMBER + "\",\n" +
                        "    \"version\": " + VERSION + "\n" +
                        "}\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        X509CertificateInfo x509CertificateInfo = gson.fromJson(invalidJson, X509CertificateInfo.class);

        // act
        new X509CertificateInfo(x509CertificateInfo);

        // arrange
    }

    /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullNotAfterUtc()
    {
        // arrange
        final String invalidJson =
                "{\n" +
                        "    \"subjectName\": \"" + SUBJECT_NAME + "\",\n" +
                        "    \"sha1Thumbprint\": \"" + SHA1THUMBPRINT + "\",\n" +
                        "    \"sha256Thumbprint\": \"" + SHA256THUMBPRINT + "\",\n" +
                        "    \"issuerName\": \"" + ISSUER_NAME + "\",\n" +
                        "    \"notBeforeUtc\": \"" + NOT_BEFORE_UTC + "\",\n" +
                        "    \"serialNumber\": \"" + SERIAL_NUMBER + "\",\n" +
                        "    \"version\": " + VERSION + "\n" +
                        "}\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        X509CertificateInfo x509CertificateInfo = gson.fromJson(invalidJson, X509CertificateInfo.class);

        // act
        new X509CertificateInfo(x509CertificateInfo);

        // arrange
    }

    /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyNotAfterUtc()
    {
        // arrange
        final String invalidJson =
                "{\n" +
                        "    \"subjectName\": \"" + SUBJECT_NAME + "\",\n" +
                        "    \"sha1Thumbprint\": \"" + SHA1THUMBPRINT + "\",\n" +
                        "    \"sha256Thumbprint\": \"" + SHA256THUMBPRINT + "\",\n" +
                        "    \"issuerName\": \"" + ISSUER_NAME + "\",\n" +
                        "    \"notBeforeUtc\": \"" + NOT_BEFORE_UTC + "\",\n" +
                        "    \"notAfterUtc\": \"\",\n" +
                        "    \"serialNumber\": \"" + SERIAL_NUMBER + "\",\n" +
                        "    \"version\": " + VERSION + "\n" +
                        "}\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        X509CertificateInfo x509CertificateInfo = gson.fromJson(invalidJson, X509CertificateInfo.class);

        // act
        new X509CertificateInfo(x509CertificateInfo);

        // arrange
    }

    /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidNotAfterUtc()
    {
        // arrange
        final String invalidJson =
                "{\n" +
                        "    \"subjectName\": \"" + SUBJECT_NAME + "\",\n" +
                        "    \"sha1Thumbprint\": \"" + SHA1THUMBPRINT + "\",\n" +
                        "    \"sha256Thumbprint\": \"" + SHA256THUMBPRINT + "\",\n" +
                        "    \"issuerName\": \"" + ISSUER_NAME + "\",\n" +
                        "    \"notBeforeUtc\": \"" + NOT_BEFORE_UTC + "\",\n" +
                        "    \"notAfterUtc\": \"0000-00-00 00:00:00\",\n" +
                        "    \"serialNumber\": \"" + SERIAL_NUMBER + "\",\n" +
                        "    \"version\": " + VERSION + "\n" +
                        "}\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        X509CertificateInfo x509CertificateInfo = gson.fromJson(invalidJson, X509CertificateInfo.class);

        // act
        new X509CertificateInfo(x509CertificateInfo);

        // arrange
    }

    /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullSerialNumber()
    {
        // arrange
        final String invalidJson =
                "{\n" +
                        "    \"subjectName\": \"" + SUBJECT_NAME + "\",\n" +
                        "    \"sha1Thumbprint\": \"" + SHA1THUMBPRINT + "\",\n" +
                        "    \"sha256Thumbprint\": \"" + SHA256THUMBPRINT + "\",\n" +
                        "    \"issuerName\": \"" + ISSUER_NAME + "\",\n" +
                        "    \"notBeforeUtc\": \"" + NOT_BEFORE_UTC + "\",\n" +
                        "    \"notAfterUtc\": \"" + NOT_AFTER_UTC + "\",\n" +
                        "    \"version\": " + VERSION + "\n" +
                        "}\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        X509CertificateInfo x509CertificateInfo = gson.fromJson(invalidJson, X509CertificateInfo.class);

        // act
        new X509CertificateInfo(x509CertificateInfo);

        // arrange
    }

    /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptySerialNumber()
    {
        // arrange
        final String invalidJson =
                "{\n" +
                        "    \"subjectName\": \"" + SUBJECT_NAME + "\",\n" +
                        "    \"sha1Thumbprint\": \"" + SHA1THUMBPRINT + "\",\n" +
                        "    \"sha256Thumbprint\": \"" + SHA256THUMBPRINT + "\",\n" +
                        "    \"issuerName\": \"" + ISSUER_NAME + "\",\n" +
                        "    \"notBeforeUtc\": \"" + NOT_BEFORE_UTC + "\",\n" +
                        "    \"notAfterUtc\": \"" + NOT_AFTER_UTC + "\",\n" +
                        "    \"serialNumber\": \"\",\n" +
                        "    \"version\": " + VERSION + "\n" +
                        "}\n";
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        X509CertificateInfo x509CertificateInfo = gson.fromJson(invalidJson, X509CertificateInfo.class);

        // act
        new X509CertificateInfo(x509CertificateInfo);

        // arrange
    }

    /* SRS_X509_CERTIFICATE_INFO_21_002: [The constructor shall copy all fields in the provided x509CertificateInfo to the new instance.] */
    /* SRS_X509_CERTIFICATE_INFO_21_003: [The getSubjectName shall return the stored subjectName.] */
    /* SRS_X509_CERTIFICATE_INFO_21_004: [The getSha1Thumbprint shall return the stored sha1Thumbprint.] */
    /* SRS_X509_CERTIFICATE_INFO_21_005: [The getSha256Thumbprint shall return the stored sha256Thumbprint.] */
    /* SRS_X509_CERTIFICATE_INFO_21_006: [The getIssuerName shall return the stored issuerName.] */
    /* SRS_X509_CERTIFICATE_INFO_21_007: [The getNotBeforeUtc shall return the stored notBeforeUtc in a Date object.] */
    /* SRS_X509_CERTIFICATE_INFO_21_009: [The getNotAfterUtc shall return the stored notAfterUtc in a Date object.] */
    /* SRS_X509_CERTIFICATE_INFO_21_011: [The getSerialNumber shall return the stored serialNumber.] */
    /* SRS_X509_CERTIFICATE_INFO_21_012: [The getVersion shall return the stored version.] */
    @Test
    public void constructorCopySucceed()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        X509CertificateInfo x509CertificateInfo = gson.fromJson(JSON, X509CertificateInfo.class);

        // act
        X509CertificateInfo result = new X509CertificateInfo(x509CertificateInfo);

        // act - assert
        assertEquals(SUBJECT_NAME, result.getSubjectName());
        assertEquals(SHA1THUMBPRINT, result.getSha1Thumbprint());
        assertEquals(SHA256THUMBPRINT, result.getSha256Thumbprint());
        assertEquals(ISSUER_NAME, result.getIssuerName());
        Helpers.assertDateWithError(result.getNotBeforeUtc(), NOT_BEFORE_UTC);
        Helpers.assertDateWithError(result.getNotAfterUtc(), NOT_AFTER_UTC);
        assertEquals(SERIAL_NUMBER, result.getSerialNumber());
        assertEquals((int)VERSION, (int)result.getVersion());
    }

    /* SRS_X509_CERTIFICATE_INFO_21_013: [The X509CertificateInfo shall provide an empty constructor to make GSON happy.] */
    @Test
    public void emptyConstructorSucceed()
    {
        // arrange
        // act
        X509CertificateInfo x509CertificateInfo = Deencapsulation.newInstance(X509CertificateInfo.class);

        // assert
        assertNotNull(x509CertificateInfo);
    }
}
