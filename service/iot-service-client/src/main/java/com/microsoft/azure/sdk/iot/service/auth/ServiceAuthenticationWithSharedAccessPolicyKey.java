/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.auth;

/**
 * Extend AuthenticationMethod class, provide getters
 * for protected properties and implement populate function to set
 * ServiceAuthenticationWithSharedAccessPolicyKey type policy on
 * given IotHubConnectionString object.
 */
class ServiceAuthenticationWithSharedAccessPolicyKey extends AuthenticationMethod
{
    /**
     * Constructor to create instance from policy name and policy key
     *
     * @param policyName The policy name string
     * @param key The policy key string
     */
    ServiceAuthenticationWithSharedAccessPolicyKey(String policyName, String key)
    {
        this.policyName = policyName;
        this.key = key;
    }

    /**
     * Populate given IotHubConnectionString with proper
     * policy key authentication data
     *
     * @param iotHubConnectionString The iotHubConnectionString object to populate
     * @return The populated IotHubConnectionString object
     */
    @Override
    protected IotHubConnectionString populate(IotHubConnectionString iotHubConnectionString)
    {
        if (iotHubConnectionString == null)
        {
            throw new IllegalArgumentException("Input parameter \"iotHubConnectionStringBuilder\" is null");
        }

        iotHubConnectionString.setSharedAccessKeyName(this.policyName);
        iotHubConnectionString.setSharedAccessKey(this.key);
        iotHubConnectionString.setSharedAccessSignature(null);

        return iotHubConnectionString;
    }
}
