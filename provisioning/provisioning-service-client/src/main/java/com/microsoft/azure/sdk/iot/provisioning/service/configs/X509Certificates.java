// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.io.Serializable;

/**
 * Representation of a single Device Provisioning Service X509 Primary and Secondary Certificate.
 *
 * <p> this class creates a representation of an X509 certificate. It can receive primary and secondary
 *     certificate, but only the primary is mandatory.
 *
 * <p> Users must provide the certificate as a {@code String}, from a <b>.pem</b> files.
 *     This class will encapsulate both in a single {@link X509Attestation}. The following JSON is an example
 *     of the result of this class.
 * <pre>
 * {@code
 *  {
 *      "primary": {
 *          "certificate": "-----BEGIN CERTIFICATE-----\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "-----END CERTIFICATE-----\n"
 *      },
 *      "secondary": {
 *          "certificate": "-----BEGIN CERTIFICATE-----\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
 *                         "-----END CERTIFICATE-----\n"
 *      }
 *  }
 * }
 * </pre>
 *
 * <p> After send an X509 certificate with success, the provisioning service will return the {@link X509CertificateInfo}
 *     for both primary and secondary certificate. User can get these info from this class, and once again, only
 *     the primary info is mandatory. The following JSON is an example what info the provisioning service will
 *     return for X509.
 * <pre>
 * {@code
 *  {
 *      "primary": {
 *          "info": {
 *               "subjectName": "CN=ROOT_00000000-0000-0000-0000-000000000000, OU=Azure IoT, O=MSFT, C=US",
 *               "sha1Thumbprint": "0000000000000000000000000000000000",
 *               "sha256Thumbprint": "validEnrollmentGroupId",
 *               "issuerName": "CN=ROOT_00000000-0000-0000-0000-000000000000, OU=Azure IoT, O=MSFT, C=US",
 *               "notBeforeUtc": "2017-11-14T12:34:18Z",
 *               "notAfterUtc": "2017-11-20T12:34:18Z",
 *               "serialNumber": "000000000000000000",
 *               "version": 3
 *           }
 *      },
 *      "secondary": {
 *          "info": {
 *               "subjectName": "CN=ROOT_00000000-0000-0000-0000-000000000000, OU=Azure IoT, O=MSFT, C=US",
 *               "sha1Thumbprint": "0000000000000000000000000000000000",
 *               "sha256Thumbprint": "validEnrollmentGroupId",
 *               "issuerName": "CN=ROOT_00000000-0000-0000-0000-000000000000, OU=Azure IoT, O=MSFT, C=US",
 *               "notBeforeUtc": "2017-11-14T12:34:18Z",
 *               "notAfterUtc": "2017-11-20T12:34:18Z",
 *               "serialNumber": "000000000000000000",
 *               "version": 3
 *           }
 *      }
 *  }
 * }
 * </pre>
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
public class X509Certificates implements Serializable
{
    // the primary X509 certificate [mandatory]
    private static final String PRIMARY_TAG = "primary";
    @Expose
    @SerializedName(PRIMARY_TAG)
    @Getter
    private X509CertificateWithInfo primary;

    // the secondary X509 certificate
    private static final String SECONDARY_TAG = "secondary";
    @Expose
    @SerializedName(SECONDARY_TAG)
    @Getter
    private X509CertificateWithInfo secondary;

    /**
     * CONSTRUCTOR
     *
     * <p> Creates a new instance of the X509 certificates using the provided certificates.
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
     * "-----END CERTIFICATE-----\n"
     * }
     * </pre>
     *
     * @param primary the {@code String} with the primary certificate.
     * @param secondary the {@code String} with the secondary certificate.
     * @throws IllegalArgumentException if the primary certificate is {@code null} or empty.
     */
    X509Certificates(String primary, String secondary)
    {
        /* SRS_X509_CERTIFICATES_21_001: [The constructor shall throw IllegalArgumentException if the primary certificate is null or empty.] */
        if (primary == null || primary.isEmpty())
        {
            throw new IllegalArgumentException("primary certificate cannot be null or empty");
        }
        /* SRS_X509_CERTIFICATES_21_002: [The constructor shall create a new instance of the X509CertificateWithInfo using the provided primary certificate, and store is as the primary Certificate.] */
        this.primary = new X509CertificateWithInfo(primary);

        /* SRS_X509_CERTIFICATES_21_003: [If the secondary certificate is not null or empty, the constructor shall create a new instance of the X509CertificateWithInfo using the provided secondary certificate, and store it as the secondary Certificate.] */
        if (!(secondary == null || secondary.isEmpty()))
        {
            this.secondary = new X509CertificateWithInfo(secondary);
        }
    }

    /**
     * Constructor [COPY]
     *
     * <p> Creates a new instance of the x509Certificates copping the content of the provided one.
     *
     * @param x509Certificates the original {@code X509Certificates} to copy.
     * @throws IllegalArgumentException if the provided x509Certificates is null or if its primary certificate is null.
     */
    public X509Certificates(X509Certificates x509Certificates)
    {
        /* SRS_X509_CERTIFICATES_21_004: [The constructor shall throw IllegalArgumentException if the provide X509Certificates is null or if its primary certificate is null.] */
        if ((x509Certificates == null) || (x509Certificates.getPrimary() == null))
        {
            throw new IllegalArgumentException("original x509Certificates cannot be null and its primary certificate cannot be null.");
        }
        /* SRS_X509_CERTIFICATES_21_005: [The constructor shall create a new instance of X509CertificateWithInfo using the primary certificate on the provided x509Certificates.] */
        this.primary = new X509CertificateWithInfo(x509Certificates.primary);

        /* SRS_X509_CERTIFICATES_21_006: [If the secondary certificate is not null, the constructor shall create a new instance of the X509CertificateWithInfo using the provided secondary certificate, and store it as the secondary Certificate.] */
        if (x509Certificates.secondary != null)
        {
            this.secondary = new X509CertificateWithInfo(x509Certificates.secondary);
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
    X509Certificates()
    {
        /* SRS_X509_CERTIFICATES_21_009: [The X509Certificates shall provide an empty constructor to make GSON happy.] */
    }
}
