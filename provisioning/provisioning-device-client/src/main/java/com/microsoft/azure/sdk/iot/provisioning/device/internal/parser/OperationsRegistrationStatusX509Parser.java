/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.parser;

import com.google.gson.annotations.SerializedName;

public class OperationsRegistrationStatusX509Parser
{
    /*
    {
        "certificateInfo":{"subjectName":"CN=RIoT_COMMON_device, C=US, O=MSR_TEST","sha1Thumbprint":"1F67F7C5F4E86103E8D3E27BF96BD901AFF35D03","sha256Thumbprint":"2CECA2C307FB65CD58C54A598018B8267DA3588C7613612A5C387450000313A9","issuerName":"CN=RIoT_Signer_Core, C=US, O=MSR_TEST","notBeforeUtc":"2017-01-01T00:00:00Z","notAfterUtc":"2037-01-01T00:00:00Z","serialNumber":"0A0B0C0D0E","version":3},
        "enrollmentGroupId":"DPS_REGISTRATION_GROUP",
        "signingCertificateInfo":{"subjectName":"CN=RIoT_Signer_Core, C=US, O=MSR_TEST","sha1Thumbprint":"3B07D9203B9F68BF532E520B46A897A3F9AAADA9","sha256Thumbprint":"99D988984AC326D4D62B5752AB4CC4BAD64387CEDAF4351FCFFAB10A69198B98","issuerName":"CN=RIoT_Signer_Core, C=US, O=MSR_TEST","notBeforeUtc":"2017-01-01T00:00:00Z","notAfterUtc":"2037-01-01T00:00:00Z","serialNumber":"0E0D0C0B0A","version":3}
    }
     */

    public class CertificateInfo
    {
        private static final String SUBJECT_NAME = "subjectName";
        @SerializedName(SUBJECT_NAME)
        String subjectName;

        private static final String SHA1_THUMBPRINT = "sha1Thumbprint";
        @SerializedName(SHA1_THUMBPRINT)
        String sha1Thumbprint;

        private static final String SHA256_THUMBPRINT = "sha256Thumbprint";
        @SerializedName(SHA256_THUMBPRINT)
        String sha256Thumbprint;

        private static final String ISSUER_NAME = "issuerName";
        @SerializedName(ISSUER_NAME)
        String issuerName;

        private static final String NOT_BEFORE_UTC = "notBeforeUtc";
        @SerializedName(NOT_BEFORE_UTC)
        String notBeforeUtc;

        private static final String NOT_AFTER_UTC = "notAfterUtc";
        @SerializedName(NOT_AFTER_UTC)
        String notAfterUtc;

        private static final String SERIAL_NUMBER = "serialNumber";
        @SerializedName(SERIAL_NUMBER)
        String serialNumber;

        private static final String VERSION = "version";
        @SerializedName(VERSION)
        String version;

        public String getSubjectName()
        {
            return subjectName;
        }

        public String getSha1Thumbprint()
        {
            return sha1Thumbprint;
        }

        public String getSha256Thumbprint()
        {
            return sha256Thumbprint;
        }

        public String getIssuerName()
        {
            return issuerName;
        }

        public String getNotBeforeUtc()
        {
            return notBeforeUtc;
        }

        public String getNotAfterUtc()
        {
            return notAfterUtc;
        }

        public String getSerialNumber()
        {
            return serialNumber;
        }

        public String getVersion()
        {
            return version;
        }
    }

    private static final String CERTIFICATE_INFO = "certificateInfo";
    @SerializedName(CERTIFICATE_INFO)
    private CertificateInfo certificateInfo;

    private static final String SIGNING_CERTIFICATE_INFO = "signingCertificateInfo";
    @SerializedName(SIGNING_CERTIFICATE_INFO)
    private CertificateInfo signingCertificateInfo;

    private static final String ENROLLMENT_GROUP_ID = "enrollmentGroupId";
    @SerializedName(ENROLLMENT_GROUP_ID)
    private String enrollmentGroupId;

    public CertificateInfo getCertificateInfo()
    {
        return certificateInfo;
    }

    public CertificateInfo getSigningCertificateInfo()
    {
        return signingCertificateInfo;
    }

    public String getEnrollmentGroupId()
    {
        return enrollmentGroupId;
    }
}
