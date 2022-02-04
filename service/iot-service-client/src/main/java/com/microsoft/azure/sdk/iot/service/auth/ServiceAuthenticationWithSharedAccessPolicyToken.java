/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.auth;

/**
 * Extend AuthenticationMethod class, provide getters
 * for protected properties and implement populate function to set
 * ServiceAuthenticationWithSharedAccessPolicyToken type policy on
 * given IotHubConnectionString object.
 */
class ServiceAuthenticationWithSharedAccessPolicyToken extends AuthenticationMethod
{
    /**
     * Populate given IotHubConnectionString with proper
     * policy token authentication data
     *
     * @param iotHubConnectionString The iotHubConnectionString object to populate
     * @return The populated IotHubConnectionString object
     */
    @Override
    public IotHubConnectionString populate(IotHubConnectionString iotHubConnectionString)
    {
        if (iotHubConnectionString == null)
        {
            throw new IllegalArgumentException("Input parameter \"iotHubConnectionStringBuilder\" is null");
        }

        iotHubConnectionString.setSharedAccessKeyName(this.policyName);
        iotHubConnectionString.setSharedAccessSignature(this.token);

        iotHubConnectionString.setSharedAccessKey(null);

        return iotHubConnectionString;
    }

    /**
     * Constructor to create instance from policy name and policy key
     *
     * @param policyName The policy name string
     * @param token The token string
     */
    ServiceAuthenticationWithSharedAccessPolicyToken(String policyName, String token)
    {
        this.setPolicyName(policyName);
        this.setToken(token);
    }
}
