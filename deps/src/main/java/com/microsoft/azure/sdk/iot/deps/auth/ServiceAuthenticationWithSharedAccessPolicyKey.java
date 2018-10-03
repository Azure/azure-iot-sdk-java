/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.auth;

import com.microsoft.azure.sdk.iot.deps.util.Tools;

/**
 * Extend AuthenticationMethod class, provide getters
 * for protected properties and implement populate function to set
 * ServiceAuthenticationWithSharedAccessPolicyKey type policy on
 * given ProvisioningConnectionString object.
 */
class ServiceAuthenticationWithSharedAccessPolicyKey extends AuthenticationMethod
{
    /**
     * Constructor to create instance from policy name and policy key
     *
     * @param policyName The policy name string
     * @param key The policy key string
     * @throws IllegalArgumentException if the provided policyName or key is null or empty.
     */
    ServiceAuthenticationWithSharedAccessPolicyKey(String policyName, String key)
    {
        /* Codes_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_KEY_21_002: [If the provided policyName is null or empty, the constructor shall throw IllegalArgumentException.] */
        if (Tools.isNullOrEmpty(policyName))
        {
            throw new IllegalArgumentException("policyName cannot be null or empty");
        }
        /* Codes_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_KEY_21_003: [If the provided key is null or empty, the constructor shall throw IllegalArgumentException.] */
        if (Tools.isNullOrEmpty(key))
        {
            throw new IllegalArgumentException("key cannot be null or empty");
        }
        /* Codes_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_KEY_21_001: [The constructor shall store the provided policyName and key.] */
        this.policyName = policyName;
        this.key = key;
    }

    /**
     * PopulateWithAuthenticationProperties given ProvisioningConnectionString with proper
     * policy key authentication data
     *
     * @param provisioningConnectionString The provisioningConnectionString object to populate
     * @return The populated provisioningConnectionString object
     * @throws IllegalArgumentException if the provided connection string is null
     */
    @Override
    protected ProvisioningConnectionString populateWithAuthenticationProperties(
            ProvisioningConnectionString provisioningConnectionString)
            throws IllegalArgumentException
    {
        /* Codes_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_KEY_21_004: [If the provided provisioningConnectionString is null, the populateWithAuthenticationProperties shall throw IllegalArgumentException.] */
        if (provisioningConnectionString == null)
        {
            throw new IllegalArgumentException("Input parameter \"ProvisioningConnectionString\" is null");
        }

        /* Codes_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_KEY_21_005: [The populateWithAuthenticationProperties shall save the policyName and key to the target object.] */
        provisioningConnectionString.setSharedAccessKeyName(this.policyName);
        provisioningConnectionString.setSharedAccessKey(this.key);

        /* Codes_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_KEY_21_006: [The populateWithAuthenticationProperties shall set the access token to null.] */
        provisioningConnectionString.setSharedAccessSignature(null);

        /* Codes_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_KEY_21_007: [The populateWithAuthenticationProperties shall return the populated connection string.] */
        return provisioningConnectionString;
    }
}
