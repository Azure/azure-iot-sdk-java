// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.microsoft.azure.sdk.iot.provisioning.service.configs.Attestation;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.AttestationMechanism;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.TpmAttestation;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.X509Attestation;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import mockit.Deencapsulation;
import mockit.Mocked;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Device Provisioning Service IndividualEnrollment serializer
 * 100% methods, 100% lines covered
 */
public class AttestationMechanismTest
{

    @Mocked
    private TpmAttestation mockedTpmAttestation;

    @Mocked
    private X509Attestation mockedX509Attestation;

    /* SRS_ATTESTATION_MECHANISM_21_001: [The constructor shall throw IllegalArgumentException if the provided tpm is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNull()
    {
        // arrange

        // act
        Deencapsulation.newInstance(AttestationMechanism.class, new Class[] {Attestation.class}, (Attestation)null);

        // assert
    }

    /* SRS_ATTESTATION_MECHANISM_21_002: [If the provided attestation is instance of TpmAttestation, the constructor shall store the provided tpm keys.] */
    /* SRS_ATTESTATION_MECHANISM_21_003: [If the provided attestation is instance of TpmAttestation, the constructor shall set the attestation type as tpm.] */
    /* SRS_ATTESTATION_MECHANISM_21_004: [If the provided attestation is instance of TpmAttestation, the constructor shall set the x508 as null.] */
    @Test
    public void constructorTpmSucceed()
    {
        // arrange

        // act
        AttestationMechanism attestationMechanism = Deencapsulation.newInstance(AttestationMechanism.class, new Class[] {Attestation.class}, mockedTpmAttestation);

        // assert
        assertEquals("TPM", Deencapsulation.getField(attestationMechanism, "type").toString());
        assertNotNull(Deencapsulation.getField(attestationMechanism, "tpm"));
        assertNull(Deencapsulation.getField(attestationMechanism, "x509"));
    }

    /* SRS_ATTESTATION_MECHANISM_21_005: [The constructor shall throw IllegalArgumentException if the provided attestation is unknown.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnUnknownAttestation()
    {
        // arrange
        final class UnknownAttestation extends Attestation {}

        // act
        Deencapsulation.newInstance(AttestationMechanism.class, new Class[] {Attestation.class}, new UnknownAttestation());

        // assert
    }

    /* SRS_ATTESTATION_MECHANISM_21_006: [If the provided attestation is instance of X509Attestation, the constructor shall store the provided x509 certificates.] */
    /* SRS_ATTESTATION_MECHANISM_21_007: [If the provided attestation is instance of X509Attestation, the constructor shall set the attestation type as x509.] */
    /* SRS_ATTESTATION_MECHANISM_21_008: [If the provided attestation is instance of X509Attestation, the constructor shall set the tpm as null.] */
    @Test
    public void constructorX509Succeed()
    {
        // arrange

        // act
        AttestationMechanism attestationMechanism = Deencapsulation.newInstance(AttestationMechanism.class, new Class[] {Attestation.class}, mockedX509Attestation);

        // assert
        assertEquals("X509", Deencapsulation.getField(attestationMechanism, "type").toString());
        assertNotNull(Deencapsulation.getField(attestationMechanism, "x509"));
        assertNull(Deencapsulation.getField(attestationMechanism, "tpm"));
    }

    /* SRS_ATTESTATION_MECHANISM_21_009: [The getType shall return a AttestationMechanismType with the stored mechanism type.] */
    @Test
    public void getTypeSucceed()
    {
        // arrange
        AttestationMechanism attestationMechanism = Deencapsulation.newInstance(AttestationMechanism.class, new Class[] {Attestation.class}, mockedX509Attestation);

        // act - assert
        assertEquals("X509", Deencapsulation.invoke(attestationMechanism, "getType").toString());
    }

    /* SRS_ATTESTATION_MECHANISM_21_010: [If the type is `tpm`, the getAttestation shall return the stored TpmAttestation.] */
    @Test
    public void getAttestationX509Succeed()
    {
        // arrange
        AttestationMechanism attestationMechanism = Deencapsulation.newInstance(AttestationMechanism.class, new Class[] {Attestation.class}, mockedX509Attestation);

        // act - assert
        assertNotNull(Deencapsulation.invoke(attestationMechanism, "getAttestation"));
    }

    /* SRS_ATTESTATION_MECHANISM_21_011: [If the type is `x509`, the getAttestation shall return the stored X509Attestation.] */
    @Test
    public void getAttestationTmpSucceed()
    {
        // arrange
        AttestationMechanism attestationMechanism = Deencapsulation.newInstance(AttestationMechanism.class, new Class[] {Attestation.class}, mockedTpmAttestation);

        // act - assert
        assertNotNull(Deencapsulation.invoke(attestationMechanism, "getAttestation"));
    }

    /* SRS_ATTESTATION_MECHANISM_21_012: [If the type is not `x509` or `tpm`, the getAttestation shall throw ProvisioningServiceClientException.] */
    @Test (expected = ProvisioningServiceClientException.class)
    public void getAttestationThrowsOnUnknownAttestation()
    {
        // arrange
        AttestationMechanism attestationMechanism = Deencapsulation.newInstance(AttestationMechanism.class);

        // act
        assertEquals(mockedTpmAttestation, Deencapsulation.invoke(attestationMechanism, "getAttestation"));
    }


    /* SRS_ATTESTATION_MECHANISM_21_013: [The AttestationMechanism shall that an empty constructor to make GSON happy.] */
    @Test
    public void constructorSucceed()
    {
        // arrange

        // act
        AttestationMechanism attestationMechanism = Deencapsulation.newInstance(AttestationMechanism.class);

        // assert
        assertEquals("NONE", Deencapsulation.getField(attestationMechanism, "type").toString());
        assertNull(Deencapsulation.getField(attestationMechanism, "x509"));
        assertNull(Deencapsulation.getField(attestationMechanism, "tpm"));
    }

}
