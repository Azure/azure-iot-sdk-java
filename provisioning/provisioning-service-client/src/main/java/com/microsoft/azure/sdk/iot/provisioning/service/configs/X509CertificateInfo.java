// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.provisioning.service.Tools;

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
public class X509CertificateInfo
{
    // the subject name of the X509 certificate
    private static final String SUBJECT_NAME_TAG = "subjectName";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(SUBJECT_NAME_TAG)
    private String subjectName;

    // the sha1 thumbprint of the X509 certificate
    private static final String SHA1_THUMBPRINT_TAG = "sha1Thumbprint";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(SHA1_THUMBPRINT_TAG)
    private String sha1Thumbprint;

    // the sha256 thumbprint of the X509 certificate
    private static final String SHA256_THUMBPRINT_TAG = "sha256Thumbprint";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(SHA256_THUMBPRINT_TAG)
    private String sha256Thumbprint;

    // the issuer name of the X509 certificate
    private static final String ISSUER_NAME_TAG = "issuerName";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(ISSUER_NAME_TAG)
    private String issuerName;

    // the no before date and time
    private static final String NO_BEFORE_UTC_TAG = "notBeforeUtc";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(NO_BEFORE_UTC_TAG)
    private String notBeforeUtc = null;
    private transient Date notBeforeUtcDate;

    // the no after date and time
    private static final String NO_AFTER_UTC_TAG = "notAfterUtc";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(NO_AFTER_UTC_TAG)
    private String notAfterUtc = null;
    private transient Date notAfterUtcDate;

    // the serial number of the X509 certificate
    private static final String SERIAL_NUMBER_TAG = "serialNumber";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(SERIAL_NUMBER_TAG)
    private String serialNumber;

    // the version of the X509 certificate
    private static final String VERSION_TAG = "version";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(VERSION_TAG)
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
        this.setNotBeforeUtc(x509CertificateInfo.notBeforeUtc);
        this.setNotAfterUtc(x509CertificateInfo.notAfterUtc);
        this.setSerialNumber(x509CertificateInfo.serialNumber);
        this.setVersion(x509CertificateInfo.version);
    }

    /**
     * Getter for the subjectName.
     *
     * @return the {@code String} with the stored subjectName. It can be {@code null} or empty.
     */
    public String getSubjectName()
    {
        /* SRS_X509_CERTIFICATE_INFO_21_003: [The getSubjectName shall return the stored subjectName.] */
        return this.subjectName;
    }

    /**
     * Getter for the sha1Thumbprint.
     *
     * @return the {@code String} with the stored sha1Thumbprint. It can be {@code null} or empty.
     */
    public String getSha1Thumbprint()
    {
        /* SRS_X509_CERTIFICATE_INFO_21_004: [The getSha1Thumbprint shall return the stored sha1Thumbprint.] */
        return this.sha1Thumbprint;
    }

    /**
     * Getter for the sha256Thumbprint.
     *
     * @return the {@code String} with the stored sha256Thumbprint. It can be {@code null} or empty.
     */
    public String getSha256Thumbprint()
    {
        /* SRS_X509_CERTIFICATE_INFO_21_005: [The getSha256Thumbprint shall return the stored sha256Thumbprint.] */
        return this.sha256Thumbprint;
    }

    /**
     * Getter for the issuerName.
     *
     * @return the {@code String} with the stored issuerName. It can be {@code null} or empty.
     */
    public String getIssuerName()
    {
        /* SRS_X509_CERTIFICATE_INFO_21_006: [The getIssuerName shall return the stored issuerName.] */
        return this.issuerName;
    }

    /**
     * Getter for the notBeforeUtc.
     *
     * @return the {@code Date} with the stored notBeforeUtc. It can be {@code null}.
     */
    public Date getNotBeforeUtc()
    {
        /* SRS_X509_CERTIFICATE_INFO_21_007: [The getNotBeforeUtc shall return the stored notBeforeUtc in a Date object.] */
        return this.notBeforeUtcDate;
    }

    /**
     * Getter for the notAfterUtc.
     *
     * @return the {@code Date} with the stored notAfterUtc. It can be {@code null}.
     */
    public Date getNotAfterUtc()
    {
        /* SRS_X509_CERTIFICATE_INFO_21_009: [The getNotAfterUtc shall return the stored notAfterUtc in a Date object.] */
        return this.notAfterUtcDate;
    }

    /**
     * Getter for the serialNumber.
     *
     * @return the {@code String} with the stored serialNumber. It can be {@code null} or empty.
     */
    public String getSerialNumber()
    {
        /* SRS_X509_CERTIFICATE_INFO_21_011: [The getSerialNumber shall return the stored serialNumber.] */
        return this.serialNumber;
    }

    /**
     * Getter for the version.
     *
     * @return the {@code Integer} with the stored version. It can be {@code null}.
     */
    public Integer getVersion()
    {
        /* SRS_X509_CERTIFICATE_INFO_21_012: [The getVersion shall return the stored version.] */
        return this.version;
    }

    private void setSubjectName(String subjectName)
    {
        if(Tools.isNullOrEmpty(subjectName))
        {
            throw new IllegalArgumentException("subjectName on X509 info cannot be null or empty");
        }
        this.subjectName = subjectName;
    }

    private void setSha1Thumbprint(String sha1Thumbprint)
    {
        if(Tools.isNullOrEmpty(sha1Thumbprint))
        {
            throw new IllegalArgumentException("sha1Thumbprint on X509 info cannot be null or empty");
        }
        this.sha1Thumbprint = sha1Thumbprint;
    }

    private void setSha256Thumbprint(String sha256Thumbprint)
    {
        if(Tools.isNullOrEmpty(sha256Thumbprint))
        {
            throw new IllegalArgumentException("sha256Thumbprint on X509 info cannot be null or empty");
        }
        this.sha256Thumbprint = sha256Thumbprint;
    }

    private void setIssuerName(String issuerName)
    {
        if(Tools.isNullOrEmpty(issuerName))
        {
            throw new IllegalArgumentException("issuerName on X509 info cannot be null or empty");
        }
        this.issuerName = issuerName;
    }

    private void setNotBeforeUtc(String notBeforeUtc)
    {
        if(Tools.isNullOrEmpty(notBeforeUtc))
        {
            throw new IllegalArgumentException("notBeforeUtc on X509 info cannot be null or empty");
        }

        this.notBeforeUtcDate = ParserUtility.getDateTimeUtc(notBeforeUtc);
        this.notBeforeUtc = notBeforeUtc;
    }

    private void setNotAfterUtc(String notAfterUtc)
    {
        if(Tools.isNullOrEmpty(notAfterUtc))
        {
            throw new IllegalArgumentException("notAfterUtc on X509 info cannot be null or empty");
        }

        this.notAfterUtcDate = ParserUtility.getDateTimeUtc(notAfterUtc);
        this.notAfterUtc = notAfterUtc;
    }

    private void setSerialNumber(String serialNumber)
    {
        if(Tools.isNullOrEmpty(serialNumber))
        {
            throw new IllegalArgumentException("serialNumber on X509 info cannot be null or empty");
        }
        this.serialNumber = serialNumber;
    }

    private void setVersion(Integer version)
    {
        if(version == null)
        {
            throw new IllegalArgumentException("version on X509 info cannot be null");
        }
        this.version = version;
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
