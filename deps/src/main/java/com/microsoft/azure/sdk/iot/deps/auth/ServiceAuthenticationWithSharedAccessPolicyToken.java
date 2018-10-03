/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.auth;

import com.microsoft.azure.sdk.iot.deps.util.Tools;

/**
 * Extend AuthenticationMethod class, provide getters
 * for protected properties and implement populate function to set
 * ServiceAuthenticationWithSharedAccessPolicyToken type policy on
 * given provisioningConnectionString object.
 */
class ServiceAuthenticationWithSharedAccessPolicyToken extends AuthenticationMethod
{
    /**
     * Constructor to create instance from policy name and policy key
     *
     * @param policyName The policy name string
     * @param token The token string
     * @throws IllegalArgumentException if the provided policyName or token is null or empty.
     */
    ServiceAuthenticationWithSharedAccessPolicyToken(String policyName, String token)
    {
        /* Codes_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_TOKEN_21_002: [If the provided policyName is null or empty, the constructor shall throw IllegalArgumentException.] */
        if (Tools.isNullOrEmpty(policyName))
        {
            throw new IllegalArgumentException("policyName cannot be null or empty");
        }
        /* Codes_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_TOKEN_21_003: [If the provided token is null or empty, the constructor shall throw IllegalArgumentException.] */
        if (Tools.isNullOrEmpty(token))
        {
            throw new IllegalArgumentException("token cannot be null or empty");
        }
        /* Codes_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_TOKEN_21_001: [The constructor shall store the provided policyName and token.] */
        this.policyName = policyName;
        this.token = token;
    }

    /**
     * PopulateWithAuthenticationProperties given provisioningConnectionString with proper
     * policy token authentication data
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
        /* Codes_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_TOKEN_21_004: [If the provided provisioningConnectionString is null, the populateWithAuthenticationProperties shall throw IllegalArgumentException.] */
        if (provisioningConnectionString == null)
        {
            throw new IllegalArgumentException("Input parameter \"provisioningConnectionString\" is null");
        }

        /* Codes_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_TOKEN_21_005: [The populateWithAuthenticationProperties shall save the policyName and token to the target object.] */
        provisioningConnectionString.setSharedAccessKeyName(this.policyName);
        provisioningConnectionString.setSharedAccessSignature(this.token);

        /* Codes_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_TOKEN_21_006: [The populateWithAuthenticationProperties shall set the access key to null.] */
        provisioningConnectionString.setSharedAccessKey(null);

        /* Codes_SRS_SERVICE_AUTHENTICATION_WITH_SHARED_ACCESS_POLICY_TOKEN_21_007: [The populateWithAuthenticationProperties shall return the populated connection string.] */
        return provisioningConnectionString;
    }
}
