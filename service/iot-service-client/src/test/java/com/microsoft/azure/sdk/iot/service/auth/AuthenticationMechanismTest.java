/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.auth;

import com.microsoft.azure.sdk.iot.service.auth.AuthenticationMechanism;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import mockit.Deencapsulation;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Code coverage:
 * 100% Methods
 * 98% lines
 */
@RunWith(JMockit.class)
public class AuthenticationMechanismTest
{
    String expectedPrimaryThumbprint;
    String expectedSecondaryThumbprint;
    private SymmetricKey expectedSymmetricKey;

    @Before
    public void setUp()
    {
        expectedPrimaryThumbprint = "0000000000000000000000000000000000000000";
        expectedSecondaryThumbprint = "1111111111111111111111111111111111111111";
        expectedSymmetricKey = Deencapsulation.newInstance(SymmetricKey.class, new Class[]{});
        Deencapsulation.setField(expectedSymmetricKey, "primaryKey", "1234");
        Deencapsulation.setField(expectedSymmetricKey, "secondaryKey", "5678");
    }

    //Tests_SRS_AUTHENTICATION_MECHANISM_34_003: [This constructor shall save the provided symmetricKey to the returned instance.]
    @Test
    public void testSymmetricKeyConstructor()
    {
        //act
        AuthenticationMechanism authenticationWithSymmetricKey = new AuthenticationMechanism(expectedSymmetricKey);

        //assert
        assertEquals(expectedSymmetricKey, authenticationWithSymmetricKey.getSymmetricKey());
    }

    //Tests_SRS_AUTHENTICATION_MECHANISM_34_004: [This constructor shall save the provided thumbprint to the returned instance.]
    @Test
    public void testThumbprintConstructor()
    {
        //act
        AuthenticationMechanism authenticationSelfSigned = new AuthenticationMechanism(expectedPrimaryThumbprint, expectedSecondaryThumbprint);

        //assert
        assertEquals(expectedPrimaryThumbprint, authenticationSelfSigned.getPrimaryThumbprint());
        assertEquals(expectedSecondaryThumbprint, authenticationSelfSigned.getSecondaryThumbprint());
    }

    //Tests_SRS_AUTHENTICATION_MECHANISM_34_005: [This function shall return this object's symmetric key.]
    //Tests_SRS_AUTHENTICATION_MECHANISM_34_007: [This function shall set this object's symmetric key to the provided value.]
    @Test
    public void symmetricKeyPropertyWorks()
    {
        //arrange
        AuthenticationMechanism actualAuthentication = new AuthenticationMechanism(AuthenticationType.CERTIFICATE_AUTHORITY);

        //act
        actualAuthentication.setSymmetricKey(expectedSymmetricKey);

        //assert
        assertEquals(expectedSymmetricKey, actualAuthentication.getSymmetricKey());
        assertEquals(AuthenticationType.SAS, actualAuthentication.getAuthenticationType());
    }

    //Tests_SRS_AUTHENTICATION_MECHANISM_34_009: [This function shall return the AuthenticationType of this object.]
    //Tests_SRS_AUTHENTICATION_MECHANISM_34_011: [This function shall set this object's authentication type to the provided value.]
    @Test
    public void authenticationTypePropertyWorks()
    {
        //arrange
        AuthenticationMechanism actualAuthentication = new AuthenticationMechanism(AuthenticationType.CERTIFICATE_AUTHORITY);

        //act
        actualAuthentication.setAuthenticationType(AuthenticationType.CERTIFICATE_AUTHORITY);

        //assert
        assertEquals(AuthenticationType.CERTIFICATE_AUTHORITY, actualAuthentication.getAuthenticationType());
    }

    //Tests_SRS_AUTHENTICATION_MECHANISM_34_015: [This function shall set this object's primary thumbprint to the provided value.]
    //Tests_SRS_AUTHENTICATION_MECHANISM_34_020: [This function shall return the primary thumbprint of this object.]
    //Tests_SRS_AUTHENTICATION_MECHANISM_34_017: [This function shall set this object's authentication type to SelfSigned.]
    @Test
    public void primaryThumbprintPropertiesWorks()
    {
        //arrange
        AuthenticationMechanism actualAuthentication = new AuthenticationMechanism(AuthenticationType.CERTIFICATE_AUTHORITY);

        //act
        actualAuthentication.setPrimaryThumbprint(expectedPrimaryThumbprint);
        actualAuthentication.setSecondaryThumbprint(expectedSecondaryThumbprint);

        //assert
        assertEquals(expectedSecondaryThumbprint, actualAuthentication.getSecondaryThumbprint());
        assertEquals(expectedPrimaryThumbprint, actualAuthentication.getPrimaryThumbprint());
        assertEquals(AuthenticationType.SELF_SIGNED, actualAuthentication.getAuthenticationType());
    }

    //Tests_SRS_AUTHENTICATION_MECHANISM_34_016: [This function shall set this object's secondary thumbprint to the provided value.]
    //Tests_SRS_AUTHENTICATION_MECHANISM_34_021: [This function shall return the secondary thumbprint of this object.]
    //Tests_SRS_AUTHENTICATION_MECHANISM_34_018: [This function shall set this object's authentication type to SelfSigned.]
    @Test
    public void secondaryThumbprintPropertyWorks()
    {
        //arrange
        AuthenticationMechanism actualAuthentication = new AuthenticationMechanism(AuthenticationType.CERTIFICATE_AUTHORITY);

        //act
        actualAuthentication.setSecondaryThumbprint(expectedSecondaryThumbprint);

        //assert
        assertEquals(expectedSecondaryThumbprint, actualAuthentication.getSecondaryThumbprint());
        assertEquals(AuthenticationType.SELF_SIGNED, actualAuthentication.getAuthenticationType());
    }

    //Tests_SRS_AUTHENTICATION_MECHANISM_34_009: [This function shall return the AuthenticationType of this object.]
    @Test
    public void testGetAuthenticationType()
    {
        //arrange
        AuthenticationMechanism authenticationWithSymmetricKey = new AuthenticationMechanism(expectedSymmetricKey);
        AuthenticationMechanism authenticationCASigned = new AuthenticationMechanism(AuthenticationType.CERTIFICATE_AUTHORITY);
        AuthenticationMechanism authenticationSelfSigned = new AuthenticationMechanism(expectedPrimaryThumbprint, expectedSecondaryThumbprint);

        //assert
        assertEquals(AuthenticationType.SAS, authenticationWithSymmetricKey.getAuthenticationType());
        assertEquals(AuthenticationType.CERTIFICATE_AUTHORITY, authenticationCASigned.getAuthenticationType());
        assertEquals(AuthenticationType.SELF_SIGNED, authenticationSelfSigned.getAuthenticationType());
    }

    //Tests_SRS_AUTHENTICATION_MECHANISM_34_012: [This constructor shall throw an IllegalArgumentException if the provided symmetricKey is null.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionForNullSymmetricKey()
    {
        //arrange
        SymmetricKey key = null;

        //act
        new AuthenticationMechanism(key);
    }

    //Tests_SRS_AUTHENTICATION_MECHANISM_34_013: [If the provided symmetricKey is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setSymmetricKeyThrowsForNullSymmetricKey()
    {
        //arrange
        AuthenticationMechanism authentication = new AuthenticationMechanism(expectedSymmetricKey);

        //act
        authentication.setSymmetricKey(null);
    }

    //Tests_SRS_AUTHENTICATION_MECHANISM_34_014: [If the provided type is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setAuthenticationTypeThrowsForNullType()
    {
        //arrange
        AuthenticationMechanism authentication = new AuthenticationMechanism(expectedSymmetricKey);

        //act
        authentication.setAuthenticationType(null);
    }

    //Tests_SRS_AUTHENTICATION_MECHANISM_34_023: [If the provided authentication type is self signed, a thumbprint will be generated, but no symmetric key will be generated.]
    @Test
    public void testSelfSignedAuthenticationTypeConstructor()
    {
        //act
        AuthenticationMechanism selfSignedAuth = new AuthenticationMechanism(AuthenticationType.SELF_SIGNED);

        //assert
        assertNotNull(selfSignedAuth.getPrimaryThumbprint());
        assertNotNull(selfSignedAuth.getSecondaryThumbprint());
        assertNull(selfSignedAuth.getSymmetricKey());
    }

    //Tests_SRS_AUTHENTICATION_MECHANISM_34_022: [If the provided authentication type is certificate authority signed, no thumbprint or symmetric key will be generated.]
    @Test
    public void testCertificateAuthorityAuthenticationTypeConstructor()
    {
        //act
        AuthenticationMechanism authenticationCASigned = new AuthenticationMechanism(AuthenticationType.CERTIFICATE_AUTHORITY);

        //assert
        assertNull(authenticationCASigned.getSymmetricKey());
        assertNull(authenticationCASigned.getPrimaryThumbprint());
        assertNull(authenticationCASigned.getSecondaryThumbprint());
        assertEquals(AuthenticationType.CERTIFICATE_AUTHORITY, authenticationCASigned.getAuthenticationType());
    }

    //Tests_SRS_AUTHENTICATION_MECHANISM_34_024: [If the provided authentication type is SAS, a symmetric key will be generated, but no thumbprint will be generated.]
    @Test
    public void testSASAuthenticationTypeConstructor()
    {
        //act
        AuthenticationMechanism sasAuth = new AuthenticationMechanism(AuthenticationType.SAS);

        //assert
        assertNotNull(sasAuth.getSymmetricKey());
        assertNull(sasAuth.getPrimaryThumbprint());
        assertNull(sasAuth.getSecondaryThumbprint());
    }
}