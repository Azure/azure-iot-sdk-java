/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.parser;

import com.google.gson.annotations.SerializedName;

/**
 * Class representing  X509RegistrationResult
 * https://docs.microsoft.com/en-us/rest/api/iot-dps/RuntimeRegistration/RegisterDevice#definitions_x509registrationresult
 */
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
    public class X509CertificateInfo
    {
        private static final String SUBJECT_NAME = "subjectName";
        @SerializedName(SUBJECT_NAME)
        private String subjectName;

        private static final String SHA1_THUMBPRINT = "sha1Thumbprint";
        @SerializedName(SHA1_THUMBPRINT)
        private String sha1Thumbprint;

        private static final String SHA256_THUMBPRINT = "sha256Thumbprint";
        @SerializedName(SHA256_THUMBPRINT)
        private String sha256Thumbprint;

        private static final String ISSUER_NAME = "issuerName";
        @SerializedName(ISSUER_NAME)
        private String issuerName;

        private static final String NOT_BEFORE_UTC = "notBeforeUtc";
        @SerializedName(NOT_BEFORE_UTC)
        private String notBeforeUtc;

        private static final String NOT_AFTER_UTC = "notAfterUtc";
        @SerializedName(NOT_AFTER_UTC)
        private String notAfterUtc;

        private static final String SERIAL_NUMBER = "serialNumber";
        @SerializedName(SERIAL_NUMBER)
        private String serialNumber;

        private static final String VERSION = "version";
        @SerializedName(VERSION)
        private String version;

        //empty constructor for Gson
        X509CertificateInfo()
        {
        }

        /**
         * Getter for the Subject Name
         * @return Getter for the Subject Name. Cannot be {@code null}
         */
        public String getSubjectName()
        {
            //SRS_X509RegistrationResultParser_25_001: [ This method shall return the parsed Subject name. ]
            return subjectName;
        }

        /**
         * Getter for the SHA1Thumbprint
         * @return Getter for the SHA1 Thumbprint. Cannot be {@code null}
         */
        public String getSha1Thumbprint()
        {
            //SRS_X509RegistrationResultParser_25_001: [ This method shall return the parsed Subject name. ]
            return sha1Thumbprint;
        }

        /**
         * Getter for the Sha256Thumbprint
         * @return Getter for the Sha256Thumbprint. Cannot be {@code null}
         */
        public String getSha256Thumbprint()
        {
            //SRS_X509RegistrationResultParser_25_003: [ This method shall return the parsed sha256Thumbprint. ]
            return sha256Thumbprint;
        }

        /**
         * Getter for the IssuerName
         * @return Getter for the IssuerName. Cannot be {@code null}
         */
        public String getIssuerName()
        {
            //SRS_X509RegistrationResultParser_25_004: [ This method shall return the parsed issuerName. ]
            return issuerName;
        }

        /**
         * Getter for the NotBeforeUtc
         * @return Getter for the NotBeforeUtc time. Cannot be {@code null}
         */
        public String getNotBeforeUtc()
        {
            //SRS_X509RegistrationResultParser_25_005: [ This method shall return the parsed notBeforeUtc time. ]
            return notBeforeUtc;
        }

        /**
         * Getter for the NotAfterUtc Time
         * @return Getter for the NotAfterUtc Time. Cannot be {@code null}
         */
        public String getNotAfterUtc()
        {
            //SRS_X509RegistrationResultParser_25_006: [ This method shall return the parsed notAfterUtc time. ]
            return notAfterUtc;
        }

        /**
         * Getter for the SerialNumber
         * @return Getter for the SerialNumber. Cannot be {@code null}
         */
        public String getSerialNumber()
        {
            //SRS_X509RegistrationResultParser_25_007: [ This method shall return the parsed serialNumber. ]
            return serialNumber;
        }

        /**
         * Getter for the Version
         * @return Getter for the Version. Cannot be {@code null}
         */
        public String getVersion()
        {
            //SRS_X509RegistrationResultParser_25_008: [ This method shall return the parsed version. ]
            return version;
        }
    }

    private static final String CERTIFICATE_INFO = "certificateInfo";
    @SerializedName(CERTIFICATE_INFO)
    private X509CertificateInfo certificateInfo;

    private static final String SIGNING_CERTIFICATE_INFO = "signingCertificateInfo";
    @SerializedName(SIGNING_CERTIFICATE_INFO)
    private X509CertificateInfo signingCertificateInfo;

    private static final String ENROLLMENT_GROUP_ID = "enrollmentGroupId";
    @SerializedName(ENROLLMENT_GROUP_ID)
    private String enrollmentGroupId;

    /**
     * Getter for the CertificateInfo Object
     * @return The CertificateInfo in X509CertificateInfo Object
     */
    public X509CertificateInfo getCertificateInfo()
    {
        //SRS_X509RegistrationResultParser_25_009: [ This method shall return the parsed certificateInfo. ]
        return certificateInfo;
    }

    /**
     * Getter for the SigningCertificateInfo Object
     * @return The SigningCertificateInfo in X509CertificateInfo Object
     */
    public X509CertificateInfo getSigningCertificateInfo()
    {
        //SRS_X509RegistrationResultParser_25_010: [ This method shall return the parsed signingCertificateInfo. ]
        return signingCertificateInfo;
    }

    /**
     * Getter for the Enrollment Group ID
     * @return Getter for the Enrollment Group ID
     */
    public String getEnrollmentGroupId()
    {
        //SRS_X509RegistrationResultParser_25_011: [ This method shall return the parsed enrollmentGroupId. ]
        return enrollmentGroupId;
    }
}
