/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.security.hsm;

import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderDiceEmulator;
import com.microsoft.msr.RiotEmulator.RIoT;
import mockit.Deencapsulation;
import mockit.Mocked;
import org.junit.Test;

import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.Collection;

import static junit.framework.TestCase.*;

/*
 * Unit tests for SecurityProviderDiceEmulator
 * Coverage - 100% line, 100% method
 */
public class SecurityProviderDiceEmulatorTest
{
    private static final String TEST_ALIAS = "TestAlias";
    private static final String TEST_SIGNER = "TestSigner";
    private static final String TEST_ROOT = "TestRoot";

    @Mocked
    RIoT mockedRIoT;

    //SRS_SecurityClientDiceEmulator_25_001: [ Constructor shall create a default unique names for Alias Certificate, Signer Certificate and Root certificate ]
    //SRS_SecurityClientDiceEmulator_25_002: [ Constructor shall create a diceBundle by calling CreateDeviceAuthBundle ]
    @Test
    public void constructorSucceeds() throws Exception
    {
        //act
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator();
        //assert
        assertNotNull(Deencapsulation.getField(testSecurityClientDiceEmulator, "commonNameAlias"));
        assertNotNull(Deencapsulation.getField(testSecurityClientDiceEmulator, "commonNameSigner"));
        assertNotNull(Deencapsulation.getField(testSecurityClientDiceEmulator, "commonNameRoot"));
    }

    @Test
    public void constructorWithNamesSucceeds() throws Exception
    {
        //act
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(TEST_ALIAS,
                                                                                                       TEST_SIGNER,
                                                                                                       TEST_ROOT);
        //assert
        assertEquals(TEST_ALIAS, Deencapsulation.getField(testSecurityClientDiceEmulator, "commonNameAlias"));
        assertEquals(TEST_SIGNER, Deencapsulation.getField(testSecurityClientDiceEmulator, "commonNameSigner"));
        assertEquals(TEST_ROOT, Deencapsulation.getField(testSecurityClientDiceEmulator, "commonNameRoot"));
    }

    //SRS_SecurityClientDiceEmulator_25_004: [ Constructor shall throw SecurityProviderException if Alias Certificate, Signer Certificate and Root certificate names are not unique ]
    @Test (expected = SecurityProviderException.class)
    public void constructorWithSameRootAndSignerNamesThrows() throws Exception
    {
        //act
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(TEST_ALIAS,
                                                                                                       TEST_ROOT,
                                                                                                       TEST_ROOT);
    }

    @Test (expected = SecurityProviderException.class)
    public void constructorWithSameAliasAndSignerNamesThrows() throws Exception
    {
        //act
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(TEST_ALIAS,
                                                                                                       TEST_ALIAS,
                                                                                                       TEST_ROOT);
    }

    @Test (expected = SecurityProviderException.class)
    public void constructorWithSameRootAndAliasNamesThrows() throws Exception
    {

        //act
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(TEST_ALIAS,
                                                                                                       TEST_SIGNER,
                                                                                                       TEST_ALIAS);
    }

    //SRS_SecurityClientDiceEmulator_25_003: [ Constructor shall throw SecurityProviderException if Alias Certificate, Signer Certificate and Root certificate names are null or empty ]
    @Test (expected = SecurityProviderException.class)
    public void constructorWithNullAliasNamesThrows() throws Exception
    {

        //act
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(null,
                                                                                                       TEST_SIGNER,
                                                                                                       TEST_ROOT);
    }

    @Test (expected = SecurityProviderException.class)
    public void constructorWithEmptyAliasNamesThrows() throws Exception
    {

        //act
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator("",
                                                                                                       TEST_SIGNER,
                                                                                                       TEST_ROOT);
    }

    @Test (expected = SecurityProviderException.class)
    public void constructorWithNullSignerNamesThrows() throws Exception
    {

        //act
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(TEST_ALIAS,
                                                                                                       null,
                                                                                                       TEST_ROOT);
    }

    @Test (expected = SecurityProviderException.class)
    public void constructorWithEmptySignerNamesThrows() throws Exception
    {

        //act
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(TEST_ALIAS,
                                                                                                       "",
                                                                                                       TEST_ROOT);
    }

    @Test (expected = SecurityProviderException.class)
    public void constructorWithNullRootNamesThrows() throws Exception
    {

        //act
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(TEST_ALIAS,
                                                                                                       TEST_SIGNER,
                                                                                                       null);
    }

    @Test (expected = SecurityProviderException.class)
    public void constructorWithEmptyRootNamesThrows() throws Exception
    {

        //act
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(TEST_ALIAS,
                                                                                                       TEST_SIGNER,
                                                                                                       "");
    }

    //SRS_SecurityClientDiceEmulator_25_005: [ This method shall return Root certificate name as common name ]
    @Test
    public void getterForCommonNameSucceeds() throws Exception
    {
        //arrange
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(TEST_ALIAS,
                                                                                                       TEST_SIGNER,
                                                                                                       TEST_ROOT);
        //act
        String cName = testSecurityClientDiceEmulator.getClientCertificateCommonName();

        //assert
        assertEquals(cName, TEST_ALIAS);
    }

    //SRS_SecurityClientDiceEmulator_25_006: [ This method shall return Alias certificate generated by DICE ]
    @Test
    public void getterForAliasCertSucceeds() throws Exception
    {
        //arrange
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(TEST_ALIAS,
                                                                                                       TEST_SIGNER,
                                                                                                       TEST_ROOT);
        //act
        X509Certificate cert = testSecurityClientDiceEmulator.getClientCertificate();

        //assert
        assertNotNull(cert);
    }

    //SRS_SecurityClientDiceEmulator_25_007: [ This method shall return Alias private key generated by DICE ]
    @Test
    public void getterForAliasKeySucceeds() throws Exception
    {
        //arrange
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(TEST_ALIAS,
                                                                                                       TEST_SIGNER,
                                                                                                       TEST_ROOT);
        //act
        Key aliasKey = testSecurityClientDiceEmulator.getClientPrivateKey();

        //assert
        assertNotNull(aliasKey);
    }

    //SRS_SecurityClientDiceEmulator_25_008: [ This method shall return Signer certificates generated by DICE ]
    @Test
    public void getterForDeviceSignerCertificatesSucceeds() throws Exception
    {
        //arrange
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(TEST_ALIAS,
                                                                                                       TEST_SIGNER,
                                                                                                       TEST_ROOT);
        //act
        Collection<X509Certificate> cert = testSecurityClientDiceEmulator.getIntermediateCertificatesChain();

        //assert
        assertNotNull(cert);
        assertFalse(cert.isEmpty());
    }

    //SRS_SecurityClientDiceEmulator_25_009: [ This method shall return Alias certificate generated by DICE as PEM string]
    @Test
    public void getterForAliasCertPemSucceeds() throws Exception
    {

        //arrange
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(TEST_ALIAS,
                                                                                                       TEST_SIGNER,
                                                                                                       TEST_ROOT);
        //act
        String cert = testSecurityClientDiceEmulator.getAliasCertPem();

        //assert
        assertNotNull(cert);
    }

    //SRS_SecurityClientDiceEmulator_25_010: [ This method shall return Signer certificate generated by DICE as PEM string ]
    @Test
    public void getterForSignerCertPemSucceeds() throws Exception
    {

        //arrange
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(TEST_ALIAS,
                                                                                                       TEST_SIGNER,
                                                                                                       TEST_ROOT);
        //act
        String cert = testSecurityClientDiceEmulator.getSignerCertPem();

        //assert
        assertNotNull(cert);
    }

    //SRS_SecurityClientDiceEmulator_25_011: [ This method shall return Root certificate generated by DICE as PEM string ]
    @Test
    public void getterForRootCertPemSucceeds() throws Exception
    {
        //arrange
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(TEST_ALIAS,
                                                                                                       TEST_SIGNER,
                                                                                                       TEST_ROOT);
        //act
        String cert = testSecurityClientDiceEmulator.getRootCertPem();

        //assert
        assertNotNull(cert);
    }

    //SRS_SecurityClientDiceEmulator_25_012: [ This method shall return Leaf certificate generated by DICE with unique ID as common Name in PEM Format ]
    @Test (expected =  SecurityProviderException.class)
    public void generateLeafCertThrowsOnNullID() throws Exception
    {
        //arrange
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(TEST_ALIAS,
                                                                                                       TEST_SIGNER,
                                                                                                       TEST_ROOT);
        //act
        testSecurityClientDiceEmulator.generateLeafCert(null);
    }

    @Test (expected =  SecurityProviderException.class)
    public void generateLeafCertThrowsOnEmptyID() throws Exception
    {
        //arrange
        SecurityProviderDiceEmulator testSecurityClientDiceEmulator = new SecurityProviderDiceEmulator(TEST_ALIAS,
                                                                                                       TEST_SIGNER,
                                                                                                       TEST_ROOT);
        //act
        testSecurityClientDiceEmulator.generateLeafCert("");
    }

}
