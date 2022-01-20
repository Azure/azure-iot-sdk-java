// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Representation of a single Device Provisioning Service Symmetric Key Attestation.
 *
 * <p> The provisioning service supports Symmetric Key attestation as the device attestation mechanism.
 *     User can auto-generate the Primary and Secondary keys, or provide the values in Base64 format.
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-dps/concepts-symmetric-key-attestation">Symmetric Key Attestation</a>
 */
public class SymmetricKeyAttestation extends Attestation implements Serializable
{
    // the primary key for attestation [mandatory]
    private static final String PRIMARY_KEY_TAG = "primaryKey";
    @Expose
    @SerializedName(PRIMARY_KEY_TAG)
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private String primaryKey;

    // the secondary key for attestation [mandatory]
    private static final String SECONDARY_KEY_TAG = "secondaryKey";
    @Expose
    @SerializedName(SECONDARY_KEY_TAG)
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private String secondaryKey;

    private static final String EMPTY_STRING = "";

    /**
     * CONSTRUCTOR
     *
     * <p> This function will create a new instance of the Symmetric Key attestation with primary and secondary keys.
     *     Both the keys are mandatory.
     *
     * @param primaryKey the {@code String} with the Symmetric Key attestation primary key. If {@code null}, the service will generate the primary key.
     * @param secondaryKey the {@code String} with the Symmetric Key attestation secondary key. if {@code null}, the service will generate the secondary key.
     */
    public SymmetricKeyAttestation(String primaryKey, String secondaryKey)
    {
        /* SRS_SYMMETRIC_KEY_ATTESTATION_44_001: [The constructor shall store the provided primary key.] */
        this.setPrimaryKey(primaryKey);
        /* SRS_SYMMETRIC_KEY_ATTESTATION_44_002: [The constructor shall store the provided secondary key.] */
        this.setSecondaryKey(secondaryKey);
    }

    /**
     * CONSTRUCTOR (COPY)
     *
     * <p>
     *     This function will create a new instance of the Symmetric Key attestation copying
     *     the primaryKey and secondaryKey from the provided attestation.
     * </p>
     *
     * @param symmetricKeyAttestation the original {@code SymmetricKeyAttestation} to copy. If {@code null}, the service will generate the primary and secondary keys.
     */
    public SymmetricKeyAttestation(SymmetricKeyAttestation symmetricKeyAttestation)
    {
        /* SRS_SYMMETRIC_KEY_ATTESTATION_44_003: [The service will generate symmetric keys if the symmetricKeyAttestation supplied is null or empty.] */
        /* SRS_SYMMETRIC_KEY_ATTESTATION_44_004: [The constructor shall store the primaryKey and secondaryKey provided in the symmetricKey.] */
        if (symmetricKeyAttestation == null)
        {
            this.setPrimaryKey(EMPTY_STRING);
            this.setSecondaryKey(EMPTY_STRING);
        }
        else
        {
            this.setPrimaryKey(symmetricKeyAttestation.primaryKey);
            this.setSecondaryKey(symmetricKeyAttestation.secondaryKey);
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
    SymmetricKeyAttestation()
    {
        /* SRS_SYMMETRIC_KEY_ATTESTATION_44_007: [The SymmetricKeyAttestation shall provide an empty constructor to make GSON happy.] */
    }
}
