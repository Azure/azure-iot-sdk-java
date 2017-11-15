// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.contract;

/**
 * Contains the SDK name and version information.
 */
class SDKUtils
{
    private static final String SERVICE_API_VERSION = "2017-08-31-preview";
    private static final String PROVISIONING_DEVICE_CLIENT = "com.microsoft.azure.sdk.iot.provisioning.provisioning-device-client/";
    private static final String PROVISIONING_DEVICE_CLIENT_VERSION = "0.0.1";

    /**
     * Getter for the rest API version
     *
     * @return A {@code String} with the rest API version.
     */
    static String getServiceApiVersion()
    {
        /* SRS_SDK_UTILS_21_001: [The getServiceApiVersion shall return a string with the rest API version.] */
        return SERVICE_API_VERSION;
    }

    /**
     * Getter for the SDK name and version.
     *
     * @return A {@code String} with the SDK package name and version
     */
    static String getUserAgentString()
    {
        /* SRS_SDK_UTILS_21_002: [The getUserAgentString shall return a string with the SDK name and version separated by `/`.] */
        return PROVISIONING_DEVICE_CLIENT + PROVISIONING_DEVICE_CLIENT_VERSION;
    }
}
