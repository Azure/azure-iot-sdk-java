/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.auth;

import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningConnectionString;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for Device Provisioning Service Authentication With Shared Access Policy Token
 * 100% methods, 100% lines covered
 */
public class ServiceAuthenticationWithSharedAccessPolicyTokenTest
{
    @Mocked
    ProvisioningConnectionString mockedProvisioningConnectionString;

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_TOKEN_21_001: [The constructor shall store the provided policyName and token.] */
    @Test
    public void constructorStoreParameters()
    {
        // arrange
        final String policyName = "validPolicyName";
        final String token = "validToken";

        // act
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyToken",
                new Class[] {String.class, String.class},
                policyName, token);

        // assert
        assertEquals(policyName, Deencapsulation.getField(authenticationMethodResult, "policyName"));
        assertEquals(token, Deencapsulation.getField(authenticationMethodResult, "token"));
    }

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_TOKEN_21_002: [If the provided policyName is null or empty, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullPolicyName()
    {
        // arrange
        final String policyName = null;
        final String token = "validToken";

        // act
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyToken",
                new Class[] {String.class, String.class},
                policyName, token);

        // assert
    }

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_TOKEN_21_002: [If the provided policyName is null or empty, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyPolicyName()
    {
        // arrange
        final String policyName = "";
        final String token = "validToken";

        // act
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyToken",
                new Class[] {String.class, String.class},
                policyName, token);

        // assert
    }

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_TOKEN_21_003: [If the provided token is null or empty, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullToken()
    {
        // arrange
        final String policyName = "validPolicyName";
        final String token = null;

        // act
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyToken",
                new Class[] {String.class, String.class},
                policyName, token);

        // assert
    }

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_TOKEN_21_003: [If the provided token is null or empty, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyToken()
    {
        // arrange
        final String policyName = "validPolicyName";
        final String token = "";

        // act
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyToken",
                new Class[] {String.class, String.class},
                policyName, token);

        // assert
    }

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_TOKEN_21_004: [If the provided provisioningConnectionString is null, the populateWithAuthenticationProperties shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void populateThrowsOnNullConnectionString()
    {
        // arrange
        final String policyName = "validPolicyName";
        final String token = "validToken";
        final ProvisioningConnectionString provisioningConnectionString = null;
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyToken",
                new Class[] {String.class, String.class},
                policyName, token);

        // act
        Deencapsulation.invoke(
                authenticationMethodResult,
                "populateWithAuthenticationProperties",
                new Class[]{ProvisioningConnectionString.class},
                provisioningConnectionString);

        // assert
    }

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_TOKEN_21_005: [The populateWithAuthenticationProperties shall save the policyName and token to the target object.] */
    @Test
    public void populateStorePolicyNameAndToken()
    {
        // arrange
        final String policyName = "validPolicyName";
        final String token = "validToken";
        final ProvisioningConnectionString provisioningConnectionString = mockedProvisioningConnectionString;
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyToken",
                new Class[] {String.class, String.class},
                policyName, token);

        // act
        Deencapsulation.invoke(
                authenticationMethodResult,
                "populateWithAuthenticationProperties",
                new Class[]{ProvisioningConnectionString.class},
                provisioningConnectionString);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedProvisioningConnectionString, "setSharedAccessKeyName", policyName);
                times = 1;
                Deencapsulation.invoke(mockedProvisioningConnectionString, "setSharedAccessSignature", token);
                times = 1;
            }
        };
    }

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_TOKEN_21_006: [The populateWithAuthenticationProperties shall set the access key to null.] */
    @Test
    public void populateSetKeyToNull()
    {
        // arrange
        final String policyName = "validPolicyName";
        final String token = "validToken";
        final ProvisioningConnectionString provisioningConnectionString = mockedProvisioningConnectionString;
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyToken",
                new Class[] {String.class, String.class},
                policyName, token);

        // act
        Deencapsulation.invoke(
                authenticationMethodResult,
                "populateWithAuthenticationProperties",
                new Class[]{ProvisioningConnectionString.class},
                provisioningConnectionString);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedProvisioningConnectionString, "setSharedAccessKey", new Class[] {String.class}, (String)null);
                times = 1;
            }
        };
    }

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_TOKEN_21_007: [The populateWithAuthenticationProperties shall return the populated connection string.] */
    @Test
    public void populateReturnConnectionString()
    {
        // arrange
        final String policyName = "validPolicyName";
        final String token = "validToken";
        final ProvisioningConnectionString provisioningConnectionString = mockedProvisioningConnectionString;
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyToken",
                new Class[] {String.class, String.class},
                policyName, token);

        // act
        ProvisioningConnectionString resultConnectionString = Deencapsulation.invoke(
                authenticationMethodResult,
                "populateWithAuthenticationProperties",
                new Class[]{ProvisioningConnectionString.class},
                provisioningConnectionString);

        // assert
        assertEquals(mockedProvisioningConnectionString, resultConnectionString);
    }
}
