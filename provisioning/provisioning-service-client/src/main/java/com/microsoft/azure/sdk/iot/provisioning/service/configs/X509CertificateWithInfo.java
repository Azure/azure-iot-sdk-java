// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.io.Serializable;

/**
 * Representation of a single Device Provisioning Service X509 Certificate with its info.
 *
 * <p> this class creates a representation of an X509 certificate that can contains the certificate,
 *     the info of the certificate or both.
 *
 * <p> To create this class, users must provide the certificate as a {@code String}, from a <b>.pem</b>
 *     or <b>.cert</b> files.
 *
 * <p> The following JSON is an example of the result of this class.
 * <pre>
 * {@code
 *  {
 *      "certificate": "-----BEGIN CERTIFICATE-----\n" +
 *                     "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                     "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                     "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                     "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                     "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                     "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                     "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                     "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                     "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                     "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                     "-----END CERTIFICATE-----\n";
 *  }
 * }
 * </pre>
 *
 * <p> After send an X509 certificate to the provisioning service, it will return the {@link X509CertificateInfo}.
 *     User can get this info from this class,
 *
 * <p> The following JSON is an example what info the provisioning service will return for X509.
 * <pre>
 * {@code
 *  {
 *      "info": {
 *           "subjectName": "CN=ROOT_00000000-0000-0000-0000-000000000000, OU=Azure IoT, O=MSFT, C=US",
 *           "sha1Thumbprint": "0000000000000000000000000000000000",
 *           "sha256Thumbprint": "validEnrollmentGroupId",
 *           "issuerName": "CN=ROOT_00000000-0000-0000-0000-000000000000, OU=Azure IoT, O=MSFT, C=US",
 *           "notBeforeUtc": "2017-11-14T12:34:18Z",
 *           "notAfterUtc": "2017-11-20T12:34:18Z",
 *           "serialNumber": "000000000000000000",
 *           "version": 3
 *      }
 *  }
 * }
 * </pre>
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
public class X509CertificateWithInfo implements Serializable
{
    // the X509 certificate
    private static final String CERTIFICATE_TAG = "certificate";
    @Expose
    @SerializedName(CERTIFICATE_TAG)
    @Getter
    private String certificate;

    // the X509 certificate info
    private static final String CERTIFICATE_INFO_TAG = "info";
    @Expose
    @SerializedName(CERTIFICATE_INFO_TAG)
    @Getter
    private X509CertificateInfo info;

    /**
     * CONSTRUCTOR
     *
     * <p> Creates a new instance of the Certificate that can contains the certificate
     *     without the info.
     *
     * <P> The certificate is a {@code String}, normally stored in a <b>.pem</b> or <b>.cert</b> file,
     *     and should looks like the following example:
     * <pre>
     * {@code
     * "-----BEGIN CERTIFICATE-----\n" +
     * "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     * "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     * "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     * "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     * "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     * "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     * "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     * "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     * "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     * "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     * "-----END CERTIFICATE-----\n";
     * }
     * </pre>
     *
     * @param certificate the {@code String} with the certificate.
     * @throws IllegalArgumentException if the certificate is {@code null} of empty.
     */
    X509CertificateWithInfo(String certificate)
    {
        /* SRS_X509_CERTIFICATE_WITH_INFO_21_001: [The constructor shall throw IllegalArgumentException if the provided certificate is null or empty.] */
        if (certificate == null || certificate.isEmpty())
        {
            throw new IllegalArgumentException("certificate cannot be null or empty.");
        }
        /* SRS_X509_CERTIFICATE_WITH_INFO_21_002: [The constructor shall store the provided certificate and set info as null.] */
        this.certificate = certificate;
        this.info = null;
    }

    /**
     * Constructor [COPY]
     *
     * <p> Creates a new instance of the X509CertificateWithInfo copping the content of the provided one.
     *
     * @param x509CertificateWithInfo the original {@code X509CertificateWithInfo} to copy.
     * @throws IllegalArgumentException if the provided x509CertificateWithInfo is null.
     */
    public X509CertificateWithInfo(X509CertificateWithInfo x509CertificateWithInfo)
    {
        /* SRS_X509_CERTIFICATE_WITH_INFO_21_003: [The constructor shall throw IllegalArgumentException if the provided x509CertificateWithInfo is null.] */
        if (x509CertificateWithInfo == null)
        {
            throw new IllegalArgumentException("x509CertificateWithInfo cannot be null");
        }
        /* SRS_X509_CERTIFICATE_WITH_INFO_21_004: [The constructor shall copy the certificate form the provided x509CertificateWithInfo.] */
        this.certificate = x509CertificateWithInfo.certificate;

        /* SRS_X509_CERTIFICATE_WITH_INFO_21_005: [If the provide x509CertificateWithInfo contains `info`, the constructor shall create a new instance of the X509CertificateInfo with the provided `info`.] */
        if (x509CertificateWithInfo.info != null)
        {
            this.info = new X509CertificateInfo(x509CertificateWithInfo.info);
        }
    }

    /**
     * Empty constructor
     *
     * <p>
     *     Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    X509CertificateWithInfo()
    {
        /* SRS_X509_CERTIFICATE_WITH_INFO_21_008: [The X509CertificateWithInfo shall provide an empty constructor to make GSON happy.] */
    }
}
