// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.provisioning.service.Tools;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Representation of a single Device Provisioning Service TPM Attestation.
 *
 * <p> The provisioning service supports Trusted Platform Module, or TPM, as the device attestation mechanism.
 *     User must provide the Endorsement Key, and can, optionally, provide the Storage Root Key.
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 * @see <a href="https://trustedcomputinggroup.org/work-groups/trusted-platform-module">Trusted Platform Module</a>
 */
public class TpmAttestation extends Attestation implements Serializable
{
    // the endorsement key for TPM [mandatory]
    private static final String ENDORSEMENT_KEY_TAG = "endorsementKey";
    @Expose
    @SerializedName(ENDORSEMENT_KEY_TAG)
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private String endorsementKey;

    // the storage root key for TPM [optional]
    private static final String STORAGE_ROOT_KEY_TAG = "storageRootKey";
    @Expose
    @SerializedName(STORAGE_ROOT_KEY_TAG)
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private String storageRootKey;

    /**
     * CONSTRUCTOR
     *
     * <p> This function will create a new instance of the TPM attestation
     *     with both endorsement and storage root keys. Only the endorsement
     *     key is mandatory.
     *
     * @param endorsementKey the {@code String} with the TPM endorsement key. It cannot be {@code null} or empty.
     * @param storageRootKey the {@code String} with the TPM storage root key. It can be {@code null} or empty.
     * @throws IllegalArgumentException If the endorsementKey is {@code null} or empty.
     */
    public TpmAttestation(String endorsementKey, String storageRootKey)
    {
        /* SRS_TPM_ATTESTATION_21_001: [The constructor shall throw IllegalArgumentException if the provided endorsementKey is null or empty.] */
        /* SRS_TPM_ATTESTATION_21_002: [The constructor shall store the provided endorsementKey.] */
        this.setEndorsementKey(endorsementKey);
        /* SRS_TPM_ATTESTATION_21_003: [The constructor shall store the provided storageRootKey.] */
        this.storageRootKey = storageRootKey;
    }

    /**
     * CONSTRUCTOR
     *
     * <p>
     *     This function will create a new instance of the TPM attestation
     *     with the endorsement key.
     * </p>
     *
     * @param endorsementKey the {@code String} with the TPM endorsement key. It cannot be {@code null} or empty.
     * @throws IllegalArgumentException If the endorsementKey is {@code null} or empty.
     */
    public TpmAttestation(String endorsementKey)
    {
        /* SRS_TPM_ATTESTATION_21_004: [The constructor shall throw IllegalArgumentException if the provided endorsementKey is null or empty.] */
        /* SRS_TPM_ATTESTATION_21_005: [The constructor shall store the provided endorsementKey.] */
        this.setEndorsementKey(endorsementKey);
    }

    /**
     * CONSTRUCTOR (COPY)
     *
     * <p>
     *     This function will create a new instance of the TPM attestation copying
     *     the endorsementKey and storageRootKey from the provided attestation.
     * </p>
     *
     * @param tpm the original {@code TpmAttestation} to copy. It cannot be {@code null}.
     * @throws IllegalArgumentException if the provided tpm is {@code null}.
     */
    public TpmAttestation(TpmAttestation tpm)
    {
        /* SRS_TPM_ATTESTATION_21_006: [The constructor shall throw IllegalArgumentException if the provided tpm is null.] */
        if(tpm == null)
        {
            throw new IllegalArgumentException("Tpm cannot be null");
        }
        /* SRS_TPM_ATTESTATION_21_007: [The constructor shall store the endorsementKey and storageRootKey provided in the tpm.] */
        this.setEndorsementKey(tpm.endorsementKey);
        this.storageRootKey = tpm.storageRootKey;
    }

    /**
     * Empty constructor
     *
     * <p>
     *     Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    TpmAttestation()
    {
        /* SRS_TPM_ATTESTATION_21_010: [The TpmAttestation shall provide an empty constructor to make GSON happy.] */
    }
}
