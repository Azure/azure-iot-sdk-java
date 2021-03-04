// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.transport;

public class TransportUtils
{
    /** Version identifier key */
    public static final String versionIdentifierKey = "com.microsoft:client-version";
    public static String javaServiceClientIdentifier = "com.microsoft.azure.sdk.iot.iot-service-client/";
    public static String serviceVersion = "1.28.0";

    private static final String JAVA_RUNTIME = System.getProperty("java.version");
    private static final String OPERATING_SYSTEM = System.getProperty("java.runtime.name").toLowerCase().contains("android") ? "Android" : System.getProperty("os.name");
    private static final String PROCESSOR_ARCHITECTURE = System.getProperty("os.arch");

    public static final String USER_AGENT_STRING = javaServiceClientIdentifier + serviceVersion + " (" + JAVA_RUNTIME + "; " + OPERATING_SYSTEM +"; " + PROCESSOR_ARCHITECTURE + ")";
    public static final String IOTHUB_API_VERSION = "2020-09-30";
}