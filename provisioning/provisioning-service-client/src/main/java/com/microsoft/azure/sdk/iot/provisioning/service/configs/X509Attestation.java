// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.provisioning.service.Tools;

/**
 * Representation of a single Device Provisioning Service X509 Attestation.
 *
 * <p> The provisioning service supports Device Identifier Composition Engine, or DICE, as the device attestation
 *     mechanism. To use DICE, user must provide the X509 certificate. This class provide the means to create a new
 *     attestation for a X509 certificate and return it as an abstract interface {@link Attestation}.
 *
 * <p> An X509 attestation can contains one of the 2 types of certificate:
 *
 * <dl>
 *     <dt><b>Client or Alias certificate:</b>
 *     <dd>Called on this class as clientCertificates, this certificate can authenticate a single device.
 * </dl>
 * <dl>
 *     <dt><b>Signing or Root certificate:</b>
 *     <dd>Called on this class as rootCertificates, this certificate can create multiple Client certificates
 *         to authenticate multiple devices.
 * </dl>
 *
 * <p> The provisioning service allows user to create {@link Enrollment} and {@link EnrollmentGroup}. For all
 *     operations over {@link Enrollment} with <b>DICE</b>, user must provide a <b>clientCertificates</b>, and
 *     for operations over {@link EnrollmentGroup}, user must provide a <b>rootCertificates</b>.
 *
 * <p> For each of this types of certificates, user can provide 2 Certificates, a primary and a secondary. Only the
 *     primary is mandatory, the secondary is optional.
 *
 * <p> The provisioning service will process the provided certificates, but will never return it back. Instead of
 *     it, {@link #getPrimaryX509CertificateInfo()} and {@link #getSecondaryX509CertificateInfo()} will return a
 *     translated info in the certificate.
 *
 * @see <a href="https://www.trustedcomputinggroup.org/wp-content/uploads/Device-Identifier-Composition-Engine-Rev69_Public-Review.pdf">Device Identifier Composition Engine (DICE) spec</a>
 * @see <a href="https://www.microsoft.com/en-us/research/publication/riot-a-foundation-for-trust-in-the-internet-of-things">RIoT – A Foundation for Trust in the Internet of Things</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollmentgroup">Device Enrollment Group</a>
 */
public class X509Attestation extends Attestation
{
    // the client certificates for X509
    private static final String CLIENT_CERTIFICATES_TAG = "clientCertificates";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(CLIENT_CERTIFICATES_TAG)
    private X509Certificates clientCertificates;

    // the signed certificates for X509
    private static final String SIGNED_CERTIFICATES_TAG = "signingCertificates";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(SIGNED_CERTIFICATES_TAG)
    private X509Certificates rootCertificates;

    /**
     * Private constructor
     *
     * <p> Creates a new instance of the X509Attestation using one of the 2 certificates types. This constructor
     *     requires one, and only one certificate type.
     *
     * @param clientCertificates the {@link X509Certificates} with the primary and secondary certificates for Individual Enrollment.
     * @param rootCertificates the {@link X509Certificates} with the primary and secondary certificates for Enrollment Group.
     * @throws IllegalArgumentException if non certificate is provided or both certificates are provided.
     */
    private X509Attestation(X509Certificates clientCertificates, X509Certificates rootCertificates)
    {
        /* SRS_X509_ATTESTATION_21_001: [The constructor shall throws IllegalArgumentException if both `clientCertificates` and `rootCertificates` are null.] */
        if((clientCertificates == null) && (rootCertificates == null))
        {
            throw new IllegalArgumentException("Attestation cannot receive null client and signing certificate");
        }
        /* SRS_X509_ATTESTATION_21_002: [The constructor shall throws IllegalArgumentException if both `clientCertificates` and `rootCertificates` are not null.] */
        if((clientCertificates != null) && (rootCertificates != null))
        {
            throw new IllegalArgumentException("Attestation cannot receive client and signing certificates together");
        }
        /* SRS_X509_ATTESTATION_21_003: [The constructor shall store the provided `clientCertificates` and `rootCertificates`.] */
        this.clientCertificates = clientCertificates;
        this.rootCertificates = rootCertificates;
    }

    /**
     * Constructor [COPY]
     *
     * <p> Creates a new instance of the X509Attestation copping the content of the provided one.
     *
     * @param x509Attestation the original X509Attestation to copy.
     * @throws IllegalArgumentException if the provided x509Attestation is null or do not contains a primary certificate.
     */
    public X509Attestation(X509Attestation x509Attestation)
    {
        /* SRS_X509_ATTESTATION_21_004: [The constructor shall throws IllegalArgumentException if the provided x509Attestation is null.] */
        if(x509Attestation == null)
        {
            throw new IllegalArgumentException("x509Attestation cannot be null");
        }

        X509Certificates clientCertificates = x509Attestation.getClientCertificates();
        X509Certificates rootCertificates = x509Attestation.getIntermediateCertificatesChain();

        /* SRS_X509_ATTESTATION_21_005: [The constructor shall throws IllegalArgumentException if both `clientCertificates` and `rootCertificates` are null.] */
        if((clientCertificates == null) && (rootCertificates == null))
        {
            throw new IllegalArgumentException("Attestation cannot receive null client and signing certificates");
        }
        /* SRS_X509_ATTESTATION_21_006: [The constructor shall throws IllegalArgumentException if both `clientCertificates` and `rootCertificates` are not null.] */
        if((clientCertificates != null) && (rootCertificates != null))
        {
            throw new IllegalArgumentException("Attestation cannot receive client and signing certificates together");
        }

        /* SRS_X509_ATTESTATION_21_007: [The constructor shall copy `clientCertificates` and `rootCertificates` from the provided X509Attestation.] */
        if(clientCertificates != null)
        {
            this.clientCertificates = new X509Certificates(clientCertificates);
        }
        if(rootCertificates != null)
        {
            this.rootCertificates = new X509Certificates(rootCertificates);
        }
    }

    /**
     * Factory with ClientCertificate with only primary certificate.
     *
     * <p> Creates a new instance of the X509Attestation using the provided primary Certificate.
     *
     * @param primary the {@code String} with the primary certificate. It cannot be {@code null} or empty.
     * @return the new instance of the X509Attestation.
     * @throws IllegalArgumentException if the provide certificate is invalid.
     */
    public static X509Attestation createFromClientCertificates(String primary)
    {
        /* SRS_X509_ATTESTATION_21_008: [The factory shall create a new instance of the X509Attestation for clientCertificates receiving only the primary certificate.] */
        return X509Attestation.createFromClientCertificates(primary, null);
    }

    /**
     * Factory with ClientCertificates with primary and secondary certificates.
     *
     * <p> Creates a new instance of the X509Attestation with the primary and secondary certificates.
     *
     * @param primary the {@code String} with the primary certificate. It cannot be {@code null} or empty.
     * @param secondary the {@code String} with the secondary certificate. It can be {@code null} or empty (ignored).
     * @return the new instance of the X509Attestation.
     * @throws IllegalArgumentException if the provide primary certificate is invalid.
     */
    public static X509Attestation createFromClientCertificates(String primary, String secondary)
    {
        /* SRS_X509_ATTESTATION_21_009: [The factory shall throws IllegalArgumentException if the primary certificate is null or empty.] */
        if(Tools.isNullOrEmpty(primary))
        {
            throw new IllegalArgumentException("primary certificate cannot be null or empty");
        }

        /* SRS_X509_ATTESTATION_21_010: [The factory shall create a new instance of the X509Certificates with the provided primary and secondary certificates.] */
        X509Certificates x509Certificates = new X509Certificates(primary, secondary);

        /* SRS_X509_ATTESTATION_21_011: [The factory shall create a new instance of the X509Attestation with the created X509Certificates as the ClientCertificates.] */
        return new X509Attestation(x509Certificates, null);
    }

    /**
     * Factory with IntermediateCertificatesChain with only primary certificate.
     *
     * <p> Creates a new instance of the X509Attestation using the provided primary Certificate.
     *
     * @param primary the {@code String} with the primary certificate. It cannot be {@code null} or empty.
     * @return the new instance of the X509Attestation.
     * @throws IllegalArgumentException if the provide certificate is invalid.
     */
    public static X509Attestation createFromRootCertificates(String primary)
    {
        /* SRS_X509_ATTESTATION_21_012: [The factory shall create a new instance of the X509Attestation for rootCertificates receiving only the primary certificate.] */
        return X509Attestation.createFromRootCertificates(primary, null);
    }

    /**
     * Factory with IntermediateCertificatesChain with primary and secondary certificates.
     *
     * <p> Creates a new instance of the X509Attestation with the primary and secondary certificates.
     *
     * @param primary the {@code String} with the primary certificate. It cannot be {@code null} or empty.
     * @param secondary the {@code String} with the secondary certificate. It can be {@code null} or empty (ignored).
     * @return the new instance of the X509Attestation.
     * @throws IllegalArgumentException if the provide primary certificate is invalid.
     */
    public static X509Attestation createFromRootCertificates(String primary, String secondary)
    {
        /* SRS_X509_ATTESTATION_21_013: [The factory shall throws IllegalArgumentException if the primary certificate is null or empty.] */
        if(Tools.isNullOrEmpty(primary))
        {
            throw new IllegalArgumentException("primary certificate cannot be null or empty");
        }

        /* SRS_X509_ATTESTATION_21_014: [The factory shall create a new instance of the X509Certificates with the provided primary and secondary certificates.] */
        X509Certificates x509Certificates = new X509Certificates(primary, secondary);

        /* SRS_X509_ATTESTATION_21_015: [The factory shall create a new instance of the X509Attestation with the created X509Certificates as the IntermediateCertificatesChain.] */
        return new X509Attestation(null, x509Certificates);
    }

    /**
     * Getter for the clientCertificates.
     *
     * @return the {@link X509Certificates} with the stored clientCertificates. it can be {@code null}.
     */
    public X509Certificates getClientCertificates()
    {
        /* SRS_X509_ATTESTATION_21_016: [The getClientCertificates shall return the stored clientCertificates.] */
        return this.clientCertificates;
    }

    /**
     * Getter for the rootCertificates.
     *
     * @return the {@link X509Certificates} with the stored rootCertificates. it can be {@code null}.
     */
    public X509Certificates getIntermediateCertificatesChain()
    {
        /* SRS_X509_ATTESTATION_21_017: [The getIntermediateCertificatesChain shall return the stored rootCertificates.] */
        return this.rootCertificates;
    }

    /**
     * Getter for the primary X509 certificate info.
     *
     * <p> This method is a getter for the information returned from the provisioning service for the provided
     *     primary certificate.
     *
     * @return the {@link X509CertificateInfo} with the returned certificate information. it can be {@code null}.
     */
    public X509CertificateInfo getPrimaryX509CertificateInfo()
    {
        /* SRS_X509_ATTESTATION_21_018: [If the clientCertificates is not null, the getPrimaryX509CertificateInfo shall return the info in the primary key of the clientCertificates.] */
        if(this.clientCertificates != null)
        {
            return this.clientCertificates.getPrimary().getInfo();
        }
        /* SRS_X509_ATTESTATION_21_019: [If the rootCertificates is not null, the getPrimaryX509CertificateInfo shall return the info in the primary key of the rootCertificates.] */
        if(this.rootCertificates != null)
        {
            return this.rootCertificates.getPrimary().getInfo();
        }
        /* SRS_X509_ATTESTATION_21_020: [If both clientCertificates and rootCertificates are null, the getPrimaryX509CertificateInfo shall throws IllegalArgumentException.] */
        throw new IllegalArgumentException("There is no valid certificate.");
    }

    /**
     * Getter for the secondary X509 certificate info.
     *
     * <p> This method is a getter for the information returned from the provisioning service for the provided
     *     secondary certificate.
     *
     * @return the {@link X509CertificateInfo} with the returned certificate information. it can be {@code null}.
     */
    public X509CertificateInfo getSecondaryX509CertificateInfo()
    {
        X509CertificateWithInfo secondaryCertificate = null;
        /* SRS_X509_ATTESTATION_21_021: [If the clientCertificates is not null, and it contains secondary key, the getSecondaryX509CertificateInfo shall return the info in the secondary key of the rootCertificates.] */
        if(this.clientCertificates != null)
        {
            secondaryCertificate = this.clientCertificates.getSecondary();
        }
        /* SRS_X509_ATTESTATION_21_022: [If the rootCertificates is not null, and it contains secondary key, the getSecondaryX509CertificateInfo shall return the info in the secondary key of the rootCertificates.] */
        if(this.rootCertificates != null)
        {
            secondaryCertificate = this.rootCertificates.getSecondary();
        }

        if(secondaryCertificate != null)
        {
            return secondaryCertificate.getInfo();
        }
        return null;
    }

    /**
     * Empty constructor
     *
     * <p>
     *     Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    protected X509Attestation()
    {
        /* SRS_X509_ATTESTATION_21_023: [The X509Attestation shall provide an empty constructor to make GSON happy.] */
    }
}
