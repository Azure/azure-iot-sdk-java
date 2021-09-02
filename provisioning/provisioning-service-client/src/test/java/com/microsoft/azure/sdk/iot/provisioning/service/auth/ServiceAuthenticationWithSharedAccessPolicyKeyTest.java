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

import static org.junit.Assert.*;

/**
 * Unit test for Device Provisioning Service Authentication With Shared Access Policy Key
 * 100% methods, 100% lines covered
 */
public class ServiceAuthenticationWithSharedAccessPolicyKeyTest
{
    @Mocked
    ProvisioningConnectionString mockedProvisioningConnectionString;

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_KEY_21_001: [The constructor shall store the provided policyName and key.] */
    @Test
    public void constructorStoreParameters()
    {
        // arrange
        final String policyName = "validPolicyName";
        final String key = "validKey";

        // act
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyKey",
                new Class[] {String.class, String.class},
                policyName, key);

        // assert
        assertEquals(policyName, Deencapsulation.getField(authenticationMethodResult, "policyName"));
        assertEquals(key, Deencapsulation.getField(authenticationMethodResult, "key"));
    }

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_KEY_21_002: [If the provided policyName is null or empty, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullPolicyName()
    {
        // arrange
        final String policyName = null;
        final String key = "validKey";

        // act
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyKey",
                new Class[] {String.class, String.class},
                policyName, key);

        // assert
    }

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_KEY_21_002: [If the provided policyName is null or empty, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyPolicyName()
    {
        // arrange
        final String policyName = "";
        final String key = "validKey";

        // act
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyKey",
                new Class[] {String.class, String.class},
                policyName, key);

        // assert
    }

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_KEY_21_003: [If the provided key is null or empty, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullKey()
    {
        // arrange
        final String policyName = "validPolicyName";
        final String key = null;

        // act
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyKey",
                new Class[] {String.class, String.class},
                policyName, key);

        // assert
    }

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_KEY_21_003: [If the provided key is null or empty, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyKey()
    {
        // arrange
        final String policyName = "validPolicyName";
        final String key = "";

        // act
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyKey",
                new Class[] {String.class, String.class},
                policyName, key);

        // assert
    }

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_KEY_21_004: [If the provided provisioningConnectionString is null, the populateWithAuthenticationProperties shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void populateThrowsOnNullConnectionString()
    {
        // arrange
        final String policyName = "validPolicyName";
        final String key = "validKey";
        final ProvisioningConnectionString provisioningConnectionString = null;
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyKey",
                new Class[] {String.class, String.class},
                policyName, key);

        // act
        Deencapsulation.invoke(
                authenticationMethodResult,
                "populateWithAuthenticationProperties",
                new Class[]{ProvisioningConnectionString.class},
                provisioningConnectionString);

        // assert
    }

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_KEY_21_005: [The populateWithAuthenticationProperties shall save the policyName and key to the target object.] */
    @Test
    public void populateStorePolicyNameAndKey()
    {
        // arrange
        final String policyName = "validPolicyName";
        final String key = "validKey";
        final ProvisioningConnectionString provisioningConnectionString = mockedProvisioningConnectionString;
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyKey",
                new Class[] {String.class, String.class},
                policyName, key);

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
                Deencapsulation.invoke(mockedProvisioningConnectionString, "setSharedAccessKey", key);
                times = 1;
            }
        };
    }

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_KEY_21_006: [The populateWithAuthenticationProperties shall set the access token to null.] */
    @Test
    public void populateSetTokenToNull()
    {
        // arrange
        final String policyName = "validPolicyName";
        final String key = "validKey";
        final ProvisioningConnectionString provisioningConnectionString = mockedProvisioningConnectionString;
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyKey",
                new Class[] {String.class, String.class},
                policyName, key);

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
                Deencapsulation.invoke(mockedProvisioningConnectionString, "setSharedAccessSignature", new Class[]{String.class}, (String)null);
                times = 1;
            }
        };
    }

    /* Tests_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_KEY_21_007: [The populateWithAuthenticationProperties shall return the populated connection string.] */
    @Test
    public void populateReturnConnectionString()
    {
        // arrange
        final String policyName = "validPolicyName";
        final String key = "validKey";
        final ProvisioningConnectionString provisioningConnectionString = mockedProvisioningConnectionString;
        Object authenticationMethodResult = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyKey",
                new Class[] {String.class, String.class},
                policyName, key);

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
