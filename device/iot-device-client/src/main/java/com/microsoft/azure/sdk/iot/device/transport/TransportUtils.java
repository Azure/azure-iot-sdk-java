// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

public class TransportUtils
{
    public static final String IOTHUB_API_VERSION = "2020-09-30";

    private static final String JAVA_DEVICE_CLIENT_IDENTIFIER = "com.microsoft.azure.sdk.iot.iot-device-client";
    public static final String CLIENT_VERSION = "2.0.3";

    private static final String JAVA_RUNTIME = System.getProperty("java.version");
    private static final String OPERATING_SYSTEM = System.getProperty("java.runtime.name").toLowerCase().contains("android") ? "Android" : System.getProperty("os.name");
    private static final String PROCESSOR_ARCHITECTURE = System.getProperty("os.arch");

    public final static String USER_AGENT_STRING = JAVA_DEVICE_CLIENT_IDENTIFIER + "/" + CLIENT_VERSION + " (" + JAVA_RUNTIME + "; " + OPERATING_SYSTEM +"; " + PROCESSOR_ARCHITECTURE + ")";
}
