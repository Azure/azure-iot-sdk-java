// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.contract;

/**
 * Contains the SDK name and version information.
 */
public class SDKUtils
{
    private static final String SERVICE_API_VERSION = "2019-03-31";
    private static final String PROVISIONING_SERVICE_CLIENT = "com.microsoft.azure.sdk.iot.provisioning.service.provisioning-service-client/";
    private static final String PROVISIONING_SERVICE_CLIENT_VERSION = "1.5.0";

    private static String JAVA_RUNTIME = System.getProperty("java.version");
    private static String OPERATING_SYSTEM = System.getProperty("java.runtime.name").toLowerCase().contains("android") ? "Android" : System.getProperty("os.name");
    private static String PROCESSOR_ARCHITECTURE = System.getProperty("os.arch");

    /**
     * Getter for the rest API version
     *
     * @return A {@code String} with the rest API version.
     */
    public static String getServiceApiVersion()
    {
        /* SRS_SDK_UTILS_21_001: [The getServiceApiVersion shall return a string with the rest API version.] */
        return SERVICE_API_VERSION;
    }

    /**
     * Getter for the SDK name and version.
     *
     * @return A {@code String} with the SDK package name and version
     */
    public static String getUserAgentString()
    {
        /* SRS_SDK_UTILS_21_002: [The getUserAgentString shall return a string with the SDK name and version separated by `/`.] */
        return PROVISIONING_SERVICE_CLIENT + PROVISIONING_SERVICE_CLIENT_VERSION + " (" + JAVA_RUNTIME + "; " + OPERATING_SYSTEM +"; " + PROCESSOR_ARCHITECTURE + ")";
    }
}
