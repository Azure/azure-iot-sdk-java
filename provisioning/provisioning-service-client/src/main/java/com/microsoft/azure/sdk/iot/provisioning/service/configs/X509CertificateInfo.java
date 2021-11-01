// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.provisioning.service.Tools;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Representation of a single X509 Certificate Info for the Device Provisioning Service.
 *
 * <p> User receive this info from the provisioning service as result of X509 operations.
 *
 * <p> This info contains a set of parameters, The following JSON is an example of the X509 certificate info.
 * <pre>
 * {@code
 * {
 *     "subjectName": "CN=ROOT_00000000-0000-0000-0000-000000000000, OU=Azure IoT, O=MSFT, C=US",
 *     "sha1Thumbprint": "0000000000000000000000000000000000",
 *     "sha256Thumbprint": "validEnrollmentGroupId",
 *     "issuerName": "CN=ROOT_00000000-0000-0000-0000-000000000000, OU=Azure IoT, O=MSFT, C=US",
 *     "notBeforeUtc": "2017-11-14T12:34:182Z",
 *     "notAfterUtc": "2017-11-20T12:34:183Z",
 *     "serialNumber": "000000000000000000",
 *     "version": 3
 * }
 * }
 * </pre>
 *
 * @see <a href="https://www.trustedcomputinggroup.org/wp-content/uploads/Device-Identifier-Composition-Engine-Rev69_Public-Review.pdf">Device Identifier Composition Engine (DICE) spec</a>
 * @see <a href="https://www.microsoft.com/en-us/research/publication/riot-a-foundation-for-trust-in-the-internet-of-things">RIoT – A Foundation for Trust in the Internet of Things</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollmentgroup">Device Enrollment Group</a>
 */
public class X509CertificateInfo implements Serializable
{
    // the subject name of the X509 certificate
    private static final String SUBJECT_NAME_TAG = "subjectName";
    @Expose
    @SerializedName(SUBJECT_NAME_TAG)
    @Getter
    @Setter
    private String subjectName;

    // the sha1 thumbprint of the X509 certificate
    private static final String SHA1_THUMBPRINT_TAG = "sha1Thumbprint";
    @Expose
    @SerializedName(SHA1_THUMBPRINT_TAG)
    @Getter
    @Setter
    private String sha1Thumbprint;

    // the sha256 thumbprint of the X509 certificate
    private static final String SHA256_THUMBPRINT_TAG = "sha256Thumbprint";
    @Expose
    @SerializedName(SHA256_THUMBPRINT_TAG)
    @Getter
    @Setter
    private String sha256Thumbprint;

    // the issuer name of the X509 certificate
    private static final String ISSUER_NAME_TAG = "issuerName";
    @Expose
    @SerializedName(ISSUER_NAME_TAG)
    @Getter
    @Setter
    private String issuerName;

    // the no before date and time
    private static final String NO_BEFORE_UTC_TAG = "notBeforeUtc";
    @Expose
    @SerializedName(NO_BEFORE_UTC_TAG)
    private String notBeforeUtcString;
    @Getter
    @Setter
    private transient Date notBeforeUtc;

    // the no after date and time
    private static final String NO_AFTER_UTC_TAG = "notAfterUtc";
    @Expose
    @SerializedName(NO_AFTER_UTC_TAG)
    private String notAfterUtcString;
    @Getter
    @Setter
    private transient Date notAfterUtc;

    // the serial number of the X509 certificate
    private static final String SERIAL_NUMBER_TAG = "serialNumber";
    @Expose
    @SerializedName(SERIAL_NUMBER_TAG)
    @Getter
    @Setter
    private String serialNumber;

    // the version of the X509 certificate
    private static final String VERSION_TAG = "version";
    @Expose
    @SerializedName(VERSION_TAG)
    @Getter
    @Setter
    private Integer version;

    /**
     * Constructor [COPY]
     *
     * <p> Creates a new instance of the x509CertificateInfo copping the content of the provided one.
     *
     * @param x509CertificateInfo the original {@code x509CertificateInfo} to copy.
     * @throws IllegalArgumentException if the provided x509CertificateInfo is null.
     */
    public X509CertificateInfo(X509CertificateInfo x509CertificateInfo)
    {
        /* SRS_X509_CERTIFICATE_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided x509CertificateInfo is null or invalid.] */
        if(x509CertificateInfo == null)
        {
            throw new IllegalArgumentException("x509CertificateInfo cannot be null");
        }
        /* SRS_X509_CERTIFICATE_INFO_21_002: [The constructor shall copy all fields in the provided x509CertificateInfo to the new instance.] */
        this.setSubjectName(x509CertificateInfo.subjectName);
        this.setSha1Thumbprint(x509CertificateInfo.sha1Thumbprint);
        this.setSha256Thumbprint(x509CertificateInfo.sha256Thumbprint);
        this.setIssuerName(x509CertificateInfo.issuerName);
        this.setNotBeforeUtcString(x509CertificateInfo.notBeforeUtcString);
        this.setNotAfterUtcString(x509CertificateInfo.notAfterUtcString);
        this.setSerialNumber(x509CertificateInfo.serialNumber);
        this.setVersion(x509CertificateInfo.version);
    }

    private void setNotBeforeUtcString(String notBeforeUtcString)
    {
        if(Tools.isNullOrEmpty(notBeforeUtcString))
        {
            throw new IllegalArgumentException("notBeforeUtcString on X509 info cannot be null or empty");
        }

        this.notBeforeUtc = ParserUtility.getDateTimeUtc(notBeforeUtcString);
        this.notBeforeUtcString = notBeforeUtcString;
    }

    private void setNotAfterUtcString(String notAfterUtcString)
    {
        if(Tools.isNullOrEmpty(notAfterUtcString))
        {
            throw new IllegalArgumentException("notAfterUtcString on X509 info cannot be null or empty");
        }

        this.notAfterUtc = ParserUtility.getDateTimeUtc(notAfterUtcString);
        this.notAfterUtcString = notAfterUtcString;
    }

    /**
     * Empty constructor
     *
     * <p>
     *     Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    protected X509CertificateInfo()
    {
        /* SRS_X509_CERTIFICATE_INFO_21_013: [The X509CertificateInfo shall provide an empty constructor to make GSON happy.] */
    }
}
