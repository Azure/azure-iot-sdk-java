// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.microsoft.azure.sdk.iot.provisioning.service.configs.SymmetricKeyAttestation;
import mockit.Deencapsulation;
import org.junit.Test;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.*;

public class SymmetricKeyAttestationTest
{
    private static final String PRIMARY_KEY_TEXT = "validPrimaryKey";
    private static final String SECONDARY_KEY_TEXT = "validSecondaryKey";

    private static final String VALID_PRIMARY_KEY = encodeBase64String(PRIMARY_KEY_TEXT.getBytes());
    private static final String VALID_SECONDARY_KEY = encodeBase64String(SECONDARY_KEY_TEXT.getBytes());

    /* SRS_SYMMETRIC_KEY_ATTESTATION_44_001: [The constructor shall store the provided primary key.] */
    /* SRS_SYMMETRIC_KEY_ATTESTATION_44_002: [The constructor shall store the provided secondary key.] */
    @Test
    public void constructorStoresPrimarySecondaryKeys()
    {
        // arrange
        // act
        SymmetricKeyAttestation symmetricKeyAttestation = new SymmetricKeyAttestation(VALID_PRIMARY_KEY, VALID_SECONDARY_KEY);

        // assert
        assertEquals(VALID_PRIMARY_KEY, Deencapsulation.getField(symmetricKeyAttestation, "primaryKey"));
        assertEquals(VALID_SECONDARY_KEY, Deencapsulation.getField(symmetricKeyAttestation, "secondaryKey"));
    }

    /* SRS_SYMMETRIC_KEY_ATTESTATION_44_003: [The service will generate symmetric keys if the symmetricKeyAttestation supplied is null or empty.] */
    @Test
    public void constructorCopyGenerateKeysOnNullSymmetricKeyAttestation()
    {
        // arrange
        // act
        SymmetricKeyAttestation symmetricKeyAttestation = new SymmetricKeyAttestation((SymmetricKeyAttestation) null);

        // assert
        assertNotNull(symmetricKeyAttestation.getPrimaryKey());
        assertNotNull(symmetricKeyAttestation.getSecondaryKey());
    }

    /* SRS_SYMMETRIC_KEY_ATTESTATION_44_004: [The constructor shall store the primaryKey and secondaryKey provided in the symmetricKey.] */
    @Test
    public void constructorCopyStorePrimarySecondaryKeys()
    {
        // arrange
        SymmetricKeyAttestation symmetricKeyAttestation = new SymmetricKeyAttestation(VALID_PRIMARY_KEY, VALID_SECONDARY_KEY);

        // act
        SymmetricKeyAttestation symmetricKeyAttestationCopy = new SymmetricKeyAttestation(symmetricKeyAttestation);

        // assert
        assertEquals(VALID_PRIMARY_KEY, Deencapsulation.getField(symmetricKeyAttestationCopy, "primaryKey"));
        assertEquals(VALID_SECONDARY_KEY, Deencapsulation.getField(symmetricKeyAttestationCopy, "secondaryKey"));
    }

    /* SRS_SYMMETRIC_KEY_ATTESTATION_44_005: [The getPrimaryKey shall return the stored primary key.] */
    /* SRS_SYMMETRIC_KEY_ATTESTATION_44_006: [The getSecondaryKey shall return the stored secondary key.] */
    @Test
    public void getterSucceeds()
    {
        // arrange
        SymmetricKeyAttestation symmetricKeyAttestation = new SymmetricKeyAttestation(VALID_PRIMARY_KEY, VALID_SECONDARY_KEY);

        // act
        // assert
        assertEquals(VALID_PRIMARY_KEY, symmetricKeyAttestation.getPrimaryKey());
        assertEquals(VALID_SECONDARY_KEY, symmetricKeyAttestation.getSecondaryKey());
    }

    /* SRS_SYMMETRIC_KEY_ATTESTATION_44_007: [The SymmetricKeyAttestation shall provide an empty constructor to make GSON happy.] */
    @Test
    public void emptyConstructorSucceed()
    {
        // arrange
        // act
        SymmetricKeyAttestation symmetricKeyAttestation = Deencapsulation.newInstance(SymmetricKeyAttestation.class);

        // assert
        assertNotNull(symmetricKeyAttestation);
    }

}
