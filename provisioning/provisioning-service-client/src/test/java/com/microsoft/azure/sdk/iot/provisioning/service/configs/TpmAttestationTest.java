// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.microsoft.azure.sdk.iot.provisioning.service.configs.TpmAttestation;
import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Device Provisioning Service TPM attestation
 * 100% methods, 100% lines covered
 */
public class TpmAttestationTest
{
    private static final String VALID_ENDORSEMENT_KEY = "validEndorsementKey";
    private static final String VALID_STORAGE_ROOT_KEY = "validStorageRootKey";

    /* SRS_TPM_ATTESTATION_21_002: [The constructor shall store the provided endorsementKey.] */
    /* SRS_TPM_ATTESTATION_21_003: [The constructor shall store the provided storageRootKey.] */
    @Test
    public void constructorStoresEndorsementKey()
    {
        // arrange
        // act
        TpmAttestation tpmAttestation = new TpmAttestation(VALID_ENDORSEMENT_KEY, VALID_STORAGE_ROOT_KEY);

        // assert
        assertEquals(VALID_ENDORSEMENT_KEY, Deencapsulation.getField(tpmAttestation, "endorsementKey"));
        assertEquals(VALID_STORAGE_ROOT_KEY, Deencapsulation.getField(tpmAttestation, "storageRootKey"));
    }

    /* SRS_TPM_ATTESTATION_21_005: [The constructor shall store the provided endorsementKey.] */
    @Test
    public void constructorOnlyEndorsementKeyStoresEndorsementKey()
    {
        // arrange
        // act
        TpmAttestation tpmAttestation = new TpmAttestation(VALID_ENDORSEMENT_KEY);

        // assert
        assertEquals(VALID_ENDORSEMENT_KEY, Deencapsulation.getField(tpmAttestation, "endorsementKey"));
    }

    /* SRS_TPM_ATTESTATION_21_006: [The constructor shall throw IllegalArgumentException if the provided tpm is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCopyThrowsOnNullTPM()
    {
        // arrange
        // act
        new TpmAttestation((TpmAttestation) null);

        // assert
    }

    /* SRS_TPM_ATTESTATION_21_007: [The constructor shall store the endorsementKey and storageRootKey provided in the tpm.] */
    @Test
    public void constructorCopyStoresKeys()
    {
        // arrange
        TpmAttestation tpmAttestation = new TpmAttestation(VALID_ENDORSEMENT_KEY, VALID_STORAGE_ROOT_KEY);

        // act
        TpmAttestation tpmAttestationCopy = new TpmAttestation(tpmAttestation);

        // assert
        assertEquals(VALID_ENDORSEMENT_KEY, Deencapsulation.getField(tpmAttestationCopy, "endorsementKey"));
        assertEquals(VALID_STORAGE_ROOT_KEY, Deencapsulation.getField(tpmAttestationCopy, "storageRootKey"));
    }


    /* SRS_TPM_ATTESTATION_21_008: [The getEndorsementKey shall return the store endorsementKey.] */
    /* SRS_TPM_ATTESTATION_21_009: [The getStorageRootKey shall return the store storageRootKey.] */
    @Test
    public void gettersSucceed()
    {
        // arrange
        // act
        TpmAttestation tpmAttestation = new TpmAttestation(VALID_ENDORSEMENT_KEY, VALID_STORAGE_ROOT_KEY);

        // assert
        assertEquals(VALID_ENDORSEMENT_KEY, tpmAttestation.getEndorsementKey());
        assertEquals(VALID_STORAGE_ROOT_KEY, tpmAttestation.getStorageRootKey());
    }

    /* SRS_TPM_ATTESTATION_21_010: [The TpmAttestation shall provide an empty constructor to make GSON happy.] */
    @Test
    public void emptyConstructorSucceed()
    {
        // arrange
        // act
        TpmAttestation tpmAttestation = Deencapsulation.newInstance(TpmAttestation.class);

        // assert
        assertNotNull(tpmAttestation);
    }

}
