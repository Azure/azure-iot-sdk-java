// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;


import com.microsoft.azure.sdk.iot.provisioning.service.configs.X509CAReferences;
import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Device Provisioning Service X509 CA references
 * 100% methods, 100% lines covered
 */
public class X509CAReferencesTest
{
    private static final String CA_REFERENCE_STRING = "validCAReference";

    /* SRS_X509_CAREFERENCE_21_001: [The constructor shall throw IllegalArgumentException if the primary CA reference is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnPrimaryNull()
    {
        // arrange
        // act
        Deencapsulation.newInstance(X509CAReferences.class, new Class[] {String.class, String.class},null, null);
        // assert
    }

    /* SRS_X509_CAREFERENCE_21_001: [The constructor shall throw IllegalArgumentException if the primary CA reference is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnPrimaryNullWitValidSecondary()
    {
        // arrange
        // act
        Deencapsulation.newInstance(X509CAReferences.class, new Class[] {String.class, String.class},null, CA_REFERENCE_STRING);
        // assert
    }

    /* SRS_X509_CAREFERENCE_21_002: [The constructor shall store the primary and secondary CA references.] */
    @Test
    public void constructorSucceedOnValidPrimaryWithoutSecondary()
    {
        // arrange
        // act
        X509CAReferences x509CAReferences = Deencapsulation.newInstance(X509CAReferences.class, new Class[] {String.class, String.class},CA_REFERENCE_STRING, null);

        // assert
        assertEquals(CA_REFERENCE_STRING, Deencapsulation.getField(x509CAReferences, "primary"));
        assertNull(Deencapsulation.getField(x509CAReferences, "secondary"));
    }

    /* SRS_X509_CAREFERENCE_21_002: [The constructor shall store the primary and secondary CA references.] */
    @Test
    public void constructorSucceedOnValidPrimaryAndSecondary()
    {
        // arrange
        // act
        X509CAReferences x509CAReferences = Deencapsulation.newInstance(X509CAReferences.class, new Class[] {String.class, String.class},CA_REFERENCE_STRING, CA_REFERENCE_STRING);

        // assert
        assertEquals(CA_REFERENCE_STRING, Deencapsulation.getField(x509CAReferences, "primary"));
        assertEquals(CA_REFERENCE_STRING, Deencapsulation.getField(x509CAReferences, "secondary"));
    }

    /* SRS_X509_CAREFERENCE_21_003: [The constructor shall throw IllegalArgumentException if the provide X509CAReferences is null or if its primary certificate is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCopyThrowsOnX509CAReferencesNull()
    {
        // arrange
        // act
        new X509CAReferences(null);

        // assert
    }

    /* SRS_X509_CAREFERENCE_21_003: [The constructor shall throw IllegalArgumentException if the provide X509CAReferences is null or if its primary certificate is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorCopyThrowsOnPrimaryNull()
    {
        // arrange
        X509CAReferences x509CAReferencesOld = Deencapsulation.newInstance(X509CAReferences.class);

        // act
        new X509CAReferences(x509CAReferencesOld);

        // assert
    }

    /* SRS_X509_CAREFERENCE_21_004: [The constructor shall create a copy of the primary and secondary CA references and store it.] */
    @Test
    public void constructorCopySucceedOnValidPrimaryAndSecondary()
    {
        // arrange
        X509CAReferences x509CAReferencesOld = Deencapsulation.newInstance(X509CAReferences.class, new Class[] {String.class, String.class},CA_REFERENCE_STRING, CA_REFERENCE_STRING);

        // act
        X509CAReferences x509CAReferences = new X509CAReferences(x509CAReferencesOld);

        // assert
        assertEquals(CA_REFERENCE_STRING, Deencapsulation.getField(x509CAReferences, "primary"));
        assertEquals(CA_REFERENCE_STRING, Deencapsulation.getField(x509CAReferences, "secondary"));
    }

    /* SRS_X509_CAREFERENCE_21_005: [The getPrimary shall return the stored primary.] */
    /* SRS_X509_CAREFERENCE_21_006: [The getSecondary shall return the stored secondary.] */
    @Test
    public void gettersSucceed()
    {
        // arrange
        X509CAReferences x509CAReferences = Deencapsulation.newInstance(X509CAReferences.class, new Class[] {String.class, String.class},CA_REFERENCE_STRING, CA_REFERENCE_STRING);

        // act - assert
        assertEquals(CA_REFERENCE_STRING, x509CAReferences.getPrimaryFinal());
        assertEquals(CA_REFERENCE_STRING, x509CAReferences.getSecondaryFinal());
    }

    /* SRS_X509_CAREFERENCE_21_007: [The X509CAReferences shall provide an empty constructor to make GSON happy.] */
    @Test
    public void constructorSucceed()
    {
        // act
        X509CAReferences x509CAReferences = Deencapsulation.newInstance(X509CAReferences.class);

        // assert
        assertNotNull(x509CAReferences);
    }
}
