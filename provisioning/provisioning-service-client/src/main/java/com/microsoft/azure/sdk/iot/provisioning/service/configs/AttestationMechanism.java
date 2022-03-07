// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import lombok.Getter;

import java.io.Serializable;

/**
 * Representation of a single Device Provisioning Service Attestation mechanism in the IndividualEnrollment and EnrollmentGroup.
 *
 * <p> It is an internal class that converts one of the attestations into JSON format. To configure
 *     the attestation mechanism, see the external API {@link Attestation}.
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
public final class AttestationMechanism implements Serializable
{
    // The AttestationMechanismType that identifies if the attestation is TPM (TpmAttestation), X509 (X509Attestation) or Symmetric Keys (SymmetricKeysAttestation).
    private static final String ATTESTATION_TYPE_TAG = "type";
    @Expose
    @SerializedName(ATTESTATION_TYPE_TAG)
    @Getter
    private final AttestationMechanismType type;

    // This is the TpmAttestation that contains the TPM keys. It is valid on AttestationMechanismType.TPM.
    private static final String TPM_ATTESTATION_TAG = "tpm";
    @Expose
    @SerializedName(TPM_ATTESTATION_TAG)
    private TpmAttestation tpm;

    // This is the X509Attestation that contains the X509 certificates. It is valid on AttestationMechanismType.X509.
    private static final String X509_ATTESTATION_TAG = "x509";
    @Expose
    @SerializedName(X509_ATTESTATION_TAG)
    private X509Attestation x509;

    // This is the SymmetricKeyAttestation that contains the Symmetric Keys. It is valid on AttestationMechanismType.SYMMETRIC_KEY.
    private static final String SYMMETRIC_KEY_TAG = "symmetricKey";
    @Expose
    @SerializedName(SYMMETRIC_KEY_TAG)
    private SymmetricKeyAttestation symmetricKey;

    /**
     * CONSTRUCTOR
     *
     * <p> It will create a new instance of the AttestationMechanism for the provided attestation type.
     *
     * @param attestation the {@code Attestation} with the TPM keys, X509 certificates or Symmetric Keys. It cannot be {@code null}.
     * @throws IllegalArgumentException If the provided tpm is {@code null}.
     */
    public AttestationMechanism(Attestation attestation)
    {
        /* SRS_ATTESTATION_MECHANISM_21_001: [The constructor shall throw IllegalArgumentException if the provided attestation is null or invalid.] */
        if (attestation == null)
        {
            throw new IllegalArgumentException("Attestation cannot be null");
        }

        if (attestation instanceof TpmAttestation)
        {
            /* SRS_ATTESTATION_MECHANISM_21_002: [If the provided attestation is instance of TpmAttestation, the constructor shall store the provided tpm keys.] */
            this.tpm = (TpmAttestation)attestation;
            /* SRS_ATTESTATION_MECHANISM_21_004: [If the provided attestation is instance of TpmAttestation, the constructor shall set the x508 as null.] */
            this.x509 = null;
            /* SRS_ATTESTATION_MECHANISM_44_014: [If the provided attestation is instance of TpmAttestation, the constructor shall set the symmetricKey as null.] */
            this.symmetricKey = null;
            /* SRS_ATTESTATION_MECHANISM_21_003: [If the provided attestation is instance of TpmAttestation, the constructor shall set the attestation type as TPM.] */
            this.type = AttestationMechanismType.TPM;
        }
        else if (attestation instanceof X509Attestation)
        {
            /* SRS_ATTESTATION_MECHANISM_21_006: [If the provided attestation is instance of X509Attestation, the constructor shall store the provided x509 certificates.] */
            this.x509 = (X509Attestation)attestation;
            /* SRS_ATTESTATION_MECHANISM_21_008: [If the provided attestation is instance of X509Attestation, the constructor shall set the tpm as null.] */
            this.tpm = null;
            /* SRS_ATTESTATION_MECHANISM_44_015: [If the provided attestation is instance of X509Attestation, the constructor shall set the symmetricKey as null.] */
            this.symmetricKey = null;
            /* SRS_ATTESTATION_MECHANISM_21_007: [If the provided attestation is instance of X509Attestation, the constructor shall set the attestation type as X509.] */
            this.type = AttestationMechanismType.X509;
        }
        else if (attestation instanceof SymmetricKeyAttestation)
        {
            /* SRS_ATTESTATION_MECHANISM_44_016: [If the provided attestation is instance of SymmetricKeyAttestation, the constructor shall store the provided symmetric keys.] */
            this.symmetricKey = (SymmetricKeyAttestation)attestation;
            /* SRS_ATTESTATION_MECHANISM_44_017: [If the provided attestation is instance of SymmetricKeyAttestation, the constructor shall set the tpm as null.] */
            this.tpm = null;
            /* SRS_ATTESTATION_MECHANISM_44_018: [If the provided attestation is instance of SymmetricKeyAttestation, the constructor shall set the x509 as null.] */
            this.x509 = null;
            /* SRS_ATTESTATION_MECHANISM_44_019: [If the provided attestation is instance of SymmetricKeyAttestation, the constructor shall set the attestation type as SYMMETRIC_KEY.] */
            this.type = AttestationMechanismType.SYMMETRIC_KEY;
        }
        else
        {
            /* SRS_ATTESTATION_MECHANISM_21_005: [The constructor shall throw IllegalArgumentException if the provided attestation is unknown.] */
            throw new IllegalArgumentException("Unknown attestation mechanism");
        }
    }

    public AttestationMechanism(String json)
    {
        if (json == null || json.isEmpty())
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        AttestationMechanism result = gson.fromJson(json, AttestationMechanism.class);

        this.symmetricKey = result.symmetricKey;
        this.tpm = result.tpm;
        this.type = result.type;
        this.x509 = result.x509;
    }

    /**
     * Getter for the Attestation.
     *
     * @return the {@link Attestation} that contains one of the stored Attestation. It cannot be {@code null}.
     * @throws ProvisioningServiceClientException If the type of the attestation mechanism is unknown.
     */
    public Attestation getAttestation() throws ProvisioningServiceClientException
    {
        switch (this.type)
        {
            case TPM:
                /* SRS_ATTESTATION_MECHANISM_21_010: [If the type is `TPM`, the getAttestation shall return the stored TpmAttestation.] */
                return new TpmAttestation(this.tpm);
            case X509:
                /* SRS_ATTESTATION_MECHANISM_21_011: [If the type is `X509`, the getAttestation shall return the stored X509Attestation.] */
                return new X509Attestation(this.x509);
            case SYMMETRIC_KEY:
                /* SRS_ATTESTATION_MECHANISM_44_020: [If the type is `SYMMETRIC_KEY`, the getAttestation shall return the stored SymmetricKeyAttestation.] */
                return new SymmetricKeyAttestation(this.symmetricKey);
            default:
                /* SRS_ATTESTATION_MECHANISM_21_012: [If the type is not `X509`, `TPM` or 'SYMMETRIC_KEY', the getAttestation shall throw ProvisioningServiceClientException.] */
                throw new ProvisioningServiceClientException("Unknown attestation mechanism");
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
    AttestationMechanism()
    {
        /* SRS_ATTESTATION_MECHANISM_21_013: [The AttestationMechanism shall provide an empty constructor to make GSON happy.] */
        this.type = AttestationMechanismType.NONE;
    }
}
