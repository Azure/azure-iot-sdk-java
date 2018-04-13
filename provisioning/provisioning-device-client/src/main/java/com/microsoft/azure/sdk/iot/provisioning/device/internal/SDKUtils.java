/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal;

public class SDKUtils
{
    private static final String SERVICE_API_VERSION = "2017-11-15";
    public static final String PROVISIONING_DEVICE_CLIENT_IDENTIFIER = "com.microsoft.azure.sdk.iot.dps.dps-device-client/";
    public static final String PROVISIONING_DEVICE_CLIENT_VERSION = "1.3.1";

    private static String JAVA_RUNTIME = System.getProperty("java.version");
    private static String OPERATING_SYSTEM = System.getProperty("os.name");
    private static String PROCESSOR_ARCHITECTURE = System.getProperty("os.arch");

    public static String getServiceApiVersion()
    {
        return SERVICE_API_VERSION;
    }

    public static String getUserAgentString()
    {
        return PROVISIONING_DEVICE_CLIENT_IDENTIFIER + PROVISIONING_DEVICE_CLIENT_VERSION + " (" + JAVA_RUNTIME + "; " + OPERATING_SYSTEM +"; " + PROCESSOR_ARCHITECTURE + ")";
    }
}
