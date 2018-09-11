// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;

/**
 * Representation of a single Device Provisioning Service Attestation mechanism in the IndividualEnrollment and EnrollmentGroup.
 *
 * <p> It is an internal class that converts one of the attestations into JSON format. To configure
 *     the attestation mechanism, see the external API {@link Attestation}.
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
public final class AttestationMechanism
{
    // The AttestationMechanismType that identifies if the attestation is TPM (TpmAttestation) or X509 (X509Attestation).
    private static final String ATTESTATION_TYPE_TAG = "type";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(ATTESTATION_TYPE_TAG)
    private AttestationMechanismType type = AttestationMechanismType.NONE;

    // This is the TpmAttestation that contains the TPM keys. It is valid on AttestationMechanismType.TPM.
    private static final String TPM_ATTESTATION_TAG = "tpm";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(TPM_ATTESTATION_TAG)
    private TpmAttestation tpm;

    // This is the X509Attestation that contains the X509 certificates. It is valid on AttestationMechanismType.X509.
    private static final String X509_ATTESTATION_TAG = "x509";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(X509_ATTESTATION_TAG)
    private X509Attestation x509;

    /**
     * CONSTRUCTOR
     *
     * <p> It will create a new instance of the AttestationMechanism for the provided attestation type.
     *
     * @param attestation the {@code Attestation} with the TPM keys or X509 certificates. It cannot be {@code null}.
     * @throws IllegalArgumentException If the provided tpm is {@code null}.
     */
    AttestationMechanism(Attestation attestation)
    {
        /* SRS_ATTESTATION_MECHANISM_21_001: [The constructor shall throw IllegalArgumentException if the provided attestation is null or invalid.] */
        if(attestation == null)
        {
            throw new IllegalArgumentException("Attestation cannot be null");
        }

        if(attestation instanceof TpmAttestation)
        {
            /* SRS_ATTESTATION_MECHANISM_21_002: [If the provided attestation is instance of TpmAttestation, the constructor shall store the provided tpm keys.] */
            this.tpm = new TpmAttestation((TpmAttestation)attestation);
            /* SRS_ATTESTATION_MECHANISM_21_004: [If the provided attestation is instance of TpmAttestation, the constructor shall set the x508 as null.] */
            this.x509 = null;
            /* SRS_ATTESTATION_MECHANISM_21_003: [If the provided attestation is instance of TpmAttestation, the constructor shall set the attestation type as TPM.] */
            this.type = AttestationMechanismType.TPM;
        }
        else if(attestation instanceof X509Attestation)
        {
            /* SRS_ATTESTATION_MECHANISM_21_006: [If the provided attestation is instance of X509Attestation, the constructor shall store the provided x509 certificates.] */
            this.x509 = new X509Attestation((X509Attestation)attestation);
            /* SRS_ATTESTATION_MECHANISM_21_008: [If the provided attestation is instance of X509Attestation, the constructor shall set the tpm as null.] */
            this.tpm = null;
            /* SRS_ATTESTATION_MECHANISM_21_007: [If the provided attestation is instance of X509Attestation, the constructor shall set the attestation type as X509.] */
            this.type = AttestationMechanismType.X509;
        }
        else
        {
            /* SRS_ATTESTATION_MECHANISM_21_005: [The constructor shall throw IllegalArgumentException if the provided attestation is unknown.] */
            throw new IllegalArgumentException("Unknown attestation mechanism");
        }
    }

    /**
     * Getter for the type.
     *
     * @return the {@link AttestationMechanismType} that contains the stored type. It cannot be {@code null}.
     */
    AttestationMechanismType getType()
    {
        /* SRS_ATTESTATION_MECHANISM_21_009: [The getType shall return a AttestationMechanismType with the stored mechanism type.] */
        return this.type;
    }

    /**
     * Getter for the Attestation.
     *
     * @return the {@link Attestation} that contains one of the stored Attestation. It cannot be {@code null}.
     * @throws ProvisioningServiceClientException If the type of the attestation mechanism is unknown.
     */
    Attestation getAttestation() throws ProvisioningServiceClientException
    {
        switch (this.type)
        {
            case TPM:
                /* SRS_ATTESTATION_MECHANISM_21_010: [If the type is `TPM`, the getAttestation shall return the stored TpmAttestation.] */
                return new TpmAttestation(this.tpm);
            case X509:
                /* SRS_ATTESTATION_MECHANISM_21_011: [If the type is `X509`, the getAttestation shall return the stored X509Attestation.] */
                return new X509Attestation(this.x509);
            default:
                /* SRS_ATTESTATION_MECHANISM_21_012: [If the type is not `X509` or `TPM`, the getAttestation shall throw ProvisioningServiceClientException.] */
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
