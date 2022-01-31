// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Representation of a single Device Provisioning Service X509 Attestation.
 *
 * <p> The provisioning service supports Device Identifier Composition Engine, or DICE, as the device attestation
 *     mechanism. To use DICE, user must provide the X509 certificate. This class provide the means to create a new
 *     attestation for a X509 certificate and return it as an abstract interface {@link Attestation}.
 *
 * <p> An X509 attestation can contains one of the 3 types of certificate:
 *
 * <dl>
 *     <dt><b>Client or Alias certificate:</b>
 *     <dd>Called on this class as clientCertificates, this certificate can authenticate a single device.
 *     <dt><b>Signing or Root certificate:</b>
 *     <dd>Called on this class as rootCertificates, this certificate can create multiple Client certificates
 *         to authenticate multiple devices.
 *     <dt><b>CA Reference:</b>
 *     <dd>Called on this class as X509CAReferences, this is a CA reference for a rootCertificate that can
 *         create multiple Client certificates to authenticate multiple devices.
 * </dl>
 *
 * <p> The provisioning service allows user to create {@link IndividualEnrollment} and {@link EnrollmentGroup}. For all
 *     operations over {@link IndividualEnrollment} with <b>DICE</b>, user must provide a <b>clientCertificates</b>, and
 *     for operations over {@link EnrollmentGroup}, user must provide a <b>rootCertificates</b> or a <b>X509CAReferences</b>.
 *
 * <p> For each of this types of certificates, user can provide 2 Certificates, a primary and a secondary. Only the
 *     primary is mandatory, the secondary is optional.
 *
 * <p> The provisioning service will process the provided certificates, but will never return it back. Instead of
 *     it, {@link #getPrimaryX509CertificateInfo()} and {@link #getSecondaryX509CertificateInfo()} will return the
 *     certificate information for the certificates.
 *
 * @see <a href="https://www.trustedcomputinggroup.org/wp-content/uploads/Device-Identifier-Composition-Engine-Rev69_Public-Review.pdf">Device Identifier Composition Engine (DICE) spec</a>
 * @see <a href="https://www.microsoft.com/en-us/research/publication/riot-a-foundation-for-trust-in-the-internet-of-things">RIoT – A Foundation for Trust in the Internet of Things</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollmentgroup">Device Enrollment Group</a>
 */
public class X509Attestation extends Attestation implements Serializable
{
    // the client certificates for X509
    private static final String CLIENT_CERTIFICATES_TAG = "clientCertificates";
    @Expose
    @SerializedName(CLIENT_CERTIFICATES_TAG)
    private X509Certificates clientCertificates;

    // the signed certificates for X509
    private static final String SIGNED_CERTIFICATES_TAG = "signingCertificates";
    @Expose
    @SerializedName(SIGNED_CERTIFICATES_TAG)
    private X509Certificates rootCertificates;

    // the CA references.
    private static final String CA_REFERENCES_TAG = "caReferences";
    @Expose
    @SerializedName(CA_REFERENCES_TAG)
    private X509CAReferences caReferences;

    /**
     * Private constructor
     *
     * <p> Creates a new instance of the X509Attestation using one of the 3 certificates types. This constructor
     *     requires one, and only one certificate type.
     *
     * @param clientCertificates the {@link X509Certificates} with the primary and secondary certificates for IndividualEnrollment.
     * @param rootCertificates the {@link X509Certificates} with the primary and secondary certificates for Enrollment Group.
     * @param caReferences the {@link X509CAReferences} with the primary and secondary CA references for Enrollment Group.
     * @throws IllegalArgumentException if non certificate is provided or more than one certificates are provided.
     */
    private X509Attestation(X509Certificates clientCertificates, X509Certificates rootCertificates, X509CAReferences caReferences)
    {
        /* SRS_X509_ATTESTATION_21_001: [The constructor shall throw IllegalArgumentException if `clientCertificates`, `rootCertificates`, and `caReferences` are null.] */
        /* SRS_X509_ATTESTATION_21_002: [The constructor shall throw IllegalArgumentException if more than one certificate type are not null.] */
        validateCertificates(clientCertificates, rootCertificates, caReferences);

        /* SRS_X509_ATTESTATION_21_003: [The constructor shall store the provided `clientCertificates`, `rootCertificates`, and `caReferences`.] */
        this.clientCertificates = clientCertificates;
        this.rootCertificates = rootCertificates;
        this.caReferences = caReferences;
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
        /* SRS_X509_ATTESTATION_21_004: [The constructor shall throw IllegalArgumentException if the provided x509Attestation is null.] */
        if (x509Attestation == null)
        {
            throw new IllegalArgumentException("x509Attestation cannot be null");
        }

        X509Certificates clientCertificates = x509Attestation.getClientCertificates();
        X509Certificates rootCertificates = x509Attestation.getRootCertificates();
        X509CAReferences caReferences = x509Attestation.getCAReferences();

        /* SRS_X509_ATTESTATION_21_005: [The constructor shall throw IllegalArgumentException if `clientCertificates`, `rootCertificates`, and `caReferences` are null.] */
        /* SRS_X509_ATTESTATION_21_006: [The constructor shall throw IllegalArgumentException if more than one certificate type are not null.] */
        validateCertificates(clientCertificates, rootCertificates, caReferences);

        /* SRS_X509_ATTESTATION_21_007: [The constructor shall copy `clientCertificates`, `rootCertificates`, and `caReferences` from the provided X509Attestation.] */
        this.clientCertificates = clientCertificates;
        this.rootCertificates = rootCertificates;
        this.caReferences = caReferences;
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
        /* SRS_X509_ATTESTATION_21_009: [The factory shall throw IllegalArgumentException if the primary certificate is null or empty.] */
        if (primary == null || primary.isEmpty())
        {
            throw new IllegalArgumentException("primary certificate cannot be null or empty");
        }

        /* SRS_X509_ATTESTATION_21_010: [The factory shall create a new instance of the X509Certificates with the provided primary and secondary certificates.] */
        X509Certificates x509Certificates = new X509Certificates(primary, secondary);

        /* SRS_X509_ATTESTATION_21_011: [The factory shall create a new instance of the X509Attestation with the created X509Certificates as the ClientCertificates.] */
        return new X509Attestation(x509Certificates, null, null);
    }

    /**
     * Factory with RootCertificates with only primary certificate.
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
     * Factory with RootCertificates with primary and secondary certificates.
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
        /* SRS_X509_ATTESTATION_21_013: [The factory shall throw IllegalArgumentException if the primary certificate is null or empty.] */
        if (primary == null || primary.isEmpty())
        {
            throw new IllegalArgumentException("primary certificate cannot be null or empty");
        }

        /* SRS_X509_ATTESTATION_21_014: [The factory shall create a new instance of the X509Certificates with the provided primary and secondary certificates.] */
        X509Certificates x509Certificates = new X509Certificates(primary, secondary);

        /* SRS_X509_ATTESTATION_21_015: [The factory shall create a new instance of the X509Attestation with the created X509Certificates as the RootCertificates.] */
        return new X509Attestation(null, x509Certificates, null);
    }

    /**
     * Factory with CAReferences with only primary reference.
     *
     * <p> Creates a new instance of the X509Attestation using the provided primary CA reference.
     *
     * @param primary the {@code String} with the primary CA reference. It cannot be {@code null} or empty.
     * @return the new instance of the X509Attestation.
     * @throws IllegalArgumentException if the provide CA reference is invalid.
     */
    public static X509Attestation createFromCAReferences(String primary)
    {
        /* SRS_X509_ATTESTATION_21_025: [The factory shall create a new instance of the X509Attestation for CA reference receiving only the primary certificate.] */
        return X509Attestation.createFromCAReferences(primary, null);
    }

    /**
     * Factory with CAReferences with primary and secondary references.
     *
     * <p> Creates a new instance of the X509Attestation with the primary and secondary CA references.
     *
     * @param primary the {@code String} with the primary CA references. It cannot be {@code null} or empty.
     * @param secondary the {@code String} with the secondary CA references. It can be {@code null} or empty (ignored).
     * @return the new instance of the X509Attestation.
     * @throws IllegalArgumentException if the provide primary CA reference is invalid.
     */
    public static X509Attestation createFromCAReferences(String primary, String secondary)
    {
        /* SRS_X509_ATTESTATION_21_026: [The factory shall throw IllegalArgumentException if the primary CA reference is null or empty.] */
        if (primary == null || primary.isEmpty())
        {
            throw new IllegalArgumentException("primary CA reference cannot be null or empty");
        }

        /* SRS_X509_ATTESTATION_21_027: [The factory shall create a new instance of the X509CAReferences with the provided primary and secondary CA references.] */
        X509CAReferences x509CAReferences = new X509CAReferences(primary, secondary);

        /* SRS_X509_ATTESTATION_21_028: [The factory shall create a new instance of the X509Attestation with the created X509CAReferences as the caReferences.] */
        return new X509Attestation(null, null, x509CAReferences);
    }

    /**
     * Getter for the clientCertificates.
     *
     * @return the {@link X509Certificates} with the stored clientCertificates. it can be {@code null}.
     */
    public final X509Certificates getClientCertificates()
    {
        /* SRS_X509_ATTESTATION_21_016: [The getClientCertificates shall return the stored clientCertificates.] */
        if (this.clientCertificates == null)
        {
            return null;
        }
        return new X509Certificates(this.clientCertificates);
    }

    /**
     * Getter for the rootCertificates.
     *
     * @return the {@link X509Certificates} with the stored rootCertificates. it can be {@code null}.
     */
    public final X509Certificates getRootCertificates()
    {
        /* SRS_X509_ATTESTATION_21_017: [The getRootCertificates shall return the stored rootCertificates.] */
        if (this.rootCertificates == null)
        {
            return null;
        }
        return new X509Certificates(this.rootCertificates);
    }

    /**
     * Getter for the caReferences.
     *
     * @return the {@link X509CAReferences} with the stored caReferences. it can be {@code null}.
     */
    public final X509CAReferences getCAReferences()
    {
        /* SRS_X509_ATTESTATION_21_024: [The getCAReferences shall return the stored caReferences.] */
        if (this.caReferences == null)
        {
            return null;
        }
        return new X509CAReferences(this.caReferences);
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
        if (this.clientCertificates != null)
        {
            return this.clientCertificates.getPrimary().getInfo();
        }
        /* SRS_X509_ATTESTATION_21_019: [If the rootCertificates is not null, the getPrimaryX509CertificateInfo shall return the info in the primary key of the rootCertificates.] */
        if (this.rootCertificates != null)
        {
            return this.rootCertificates.getPrimary().getInfo();
        }
        /* SRS_X509_ATTESTATION_21_020: [If both clientCertificates and rootCertificates are null, the getPrimaryX509CertificateInfo shall throw IllegalArgumentException.] */
        throw new IllegalArgumentException("There is no valid certificate information.");
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
        if (this.clientCertificates != null)
        {
            secondaryCertificate = this.clientCertificates.getSecondary();
        }
        /* SRS_X509_ATTESTATION_21_022: [If the rootCertificates is not null, and it contains secondary key, the getSecondaryX509CertificateInfo shall return the info in the secondary key of the rootCertificates.] */
        if (this.rootCertificates != null)
        {
            secondaryCertificate = this.rootCertificates.getSecondary();
        }

        if (secondaryCertificate != null)
        {
            return secondaryCertificate.getInfo();
        }
        return null;
    }

    private void validateCertificates(X509Certificates clientCertificates, X509Certificates rootCertificates, X509CAReferences caReferences)
    {
        if ((clientCertificates == null) && (rootCertificates == null) && (caReferences == null))
        {
            throw new IllegalArgumentException("Attestation shall receive one no null Certificate");
        }
        if (((clientCertificates != null) && ((rootCertificates != null) || (caReferences != null))) || ((rootCertificates != null) && (caReferences != null)))
        {
            throw new IllegalArgumentException("Attestation cannot receive more than one certificate together");
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
    X509Attestation()
    {
        /* SRS_X509_ATTESTATION_21_023: [The X509Attestation shall provide an empty constructor to make GSON happy.] */
    }
}
