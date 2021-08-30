/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.parser;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

/**
 * Class representing  X509RegistrationResult
 * https://docs.microsoft.com/en-us/rest/api/iot-dps/RuntimeRegistration/RegisterDevice#definitions_x509registrationresult
 */
@SuppressWarnings("unused") // A number of private members are unused but may be filled in or used by serialization
public class X509RegistrationResultParser
{
    //empty constructor for Gson
    X509RegistrationResultParser()
    {
    }

    /**
     * Class representing X509CertificateInfo
     * https://docs.microsoft.com/en-us/rest/api/iot-dps/RuntimeRegistration/RegisterDevice#definitions_x509certificateinfo
     */
    public static class X509CertificateInfo
    {
        private static final String SUBJECT_NAME = "subjectName";
        @SerializedName(SUBJECT_NAME)
        @Getter
        private String subjectName;

        private static final String SHA1_THUMBPRINT = "sha1Thumbprint";
        @SerializedName(SHA1_THUMBPRINT)
        @Getter
        private String sha1Thumbprint;

        private static final String SHA256_THUMBPRINT = "sha256Thumbprint";
        @SerializedName(SHA256_THUMBPRINT)
        @Getter
        private String sha256Thumbprint;

        private static final String ISSUER_NAME = "issuerName";
        @SerializedName(ISSUER_NAME)
        @Getter
        private String issuerName;

        private static final String NOT_BEFORE_UTC = "notBeforeUtc";
        @SerializedName(NOT_BEFORE_UTC)
        @Getter
        private String notBeforeUtc;

        private static final String NOT_AFTER_UTC = "notAfterUtc";
        @SerializedName(NOT_AFTER_UTC)
        @Getter
        private String notAfterUtc;

        private static final String SERIAL_NUMBER = "serialNumber";
        @SerializedName(SERIAL_NUMBER)
        @Getter
        private String serialNumber;

        private static final String VERSION = "version";
        @SerializedName(VERSION)
        @Getter
        private String version;

        //empty constructor for Gson
        X509CertificateInfo()
        {
        }
    }

    private static final String CERTIFICATE_INFO = "certificateInfo";
    @SerializedName(CERTIFICATE_INFO)
    @Getter
    private X509CertificateInfo certificateInfo;

    private static final String SIGNING_CERTIFICATE_INFO = "signingCertificateInfo";
    @SerializedName(SIGNING_CERTIFICATE_INFO)
    @Getter
    private X509CertificateInfo signingCertificateInfo;

    private static final String ENROLLMENT_GROUP_ID = "enrollmentGroupId";
    @SerializedName(ENROLLMENT_GROUP_ID)
    @Getter
    private String enrollmentGroupId;
}
