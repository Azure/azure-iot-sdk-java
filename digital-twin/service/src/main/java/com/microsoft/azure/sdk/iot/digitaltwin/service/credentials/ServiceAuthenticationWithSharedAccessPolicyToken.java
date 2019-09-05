/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.digitaltwin.service.credentials;

/**
 * Extend AuthenticationMethod class, provide getters
 * for protected properties and implement populate function to set
 * ServiceAuthenticationWithSharedAccessPolicyToken type policy on
 * given ServiceConnectionString object.
 */
public class ServiceAuthenticationWithSharedAccessPolicyToken extends AuthenticationMethod {
    /**
     * Populate given ServiceConnectionString with proper
     * policy token authentication data
     *
     * @param ServiceConnectionString The ServiceConnectionString object to populate
     * @return The populated ServiceConnectionString object
     */
    @Override
    public ServiceConnectionString populate(ServiceConnectionString ServiceConnectionString) {
        // Codes_SRS_SERVICE_SDK_JAVA_SERVICEAUTHENTICATIONWITHSHAREDACCESSTOKEN_12_002: [The function shall throw IllegalArgumentException if the input object is null]
        if (ServiceConnectionString == null) {
            throw new IllegalArgumentException("Input parameter \"ServiceConnectionStringBuilder\" is null");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_SERVICEAUTHENTICATIONWITHSHAREDACCESSTOKEN_12_003: [The function shall save the policyName and token to the target object]
        ServiceConnectionString.setSharedAccessKeyName(this.policyName);
        ServiceConnectionString.setSharedAccessSignature(this.token);

        // Codes_SRS_SERVICE_SDK_JAVA_SERVICEAUTHENTICATIONWITHSHAREDACCESSTOKEN_12_004: [The function shall set the access key to null]
        ServiceConnectionString.setSharedAccessKey(null);

        return ServiceConnectionString;
    }

    // Codes_SRS_SERVICE_SDK_JAVA_SERVICEAUTHENTICATIONWITHSHAREDACCESSTOKEN_12_001: [Provide access to the following properties: policyName, token]

    /**
     * Constructor to create instance from policy name and policy key
     *
     * @param policyName The policy name string
     * @param token      The token string
     */
    public ServiceAuthenticationWithSharedAccessPolicyToken(String policyName, String token) {
        this.setPolicyName(policyName);
        this.setToken(token);
    }

    /**
     * Getter for policy name
     *
     * @return The policy name string
     */
    public String getPolicyName() {
        return this.policyName;
    }

    /**
     * Setter for policy name
     *
     * @param policyName The string value to set
     */
    protected final void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    /**
     * Getter for policy token
     *
     * @return The policy token string
     */
    public String getToken() {
        return this.token;
    }

    /**
     * Setter for policy token
     *
     * @param token The string value to set
     */
    protected final void setToken(String token) {
        this.token = token;
    }
}
