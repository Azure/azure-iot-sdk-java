// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

public class TransportUtils
{
    public static final String IOTHUB_API_VERSION = "2020-09-30";

    private static final String JAVA_DEVICE_CLIENT_IDENTIFIER = "com.microsoft.azure.sdk.iot.iot-device-client";
    private static final String CLIENT_VERSION = "2.0.0-preview-001";

    private static final String JAVA_RUNTIME = System.getProperty("java.version");
    private static final String OPERATING_SYSTEM = System.getProperty("java.runtime.name").toLowerCase().contains("android") ? "Android" : System.getProperty("os.name");
    private static final String PROCESSOR_ARCHITECTURE = System.getProperty("os.arch");

    public final static String USER_AGENT_STRING = JAVA_DEVICE_CLIENT_IDENTIFIER + "/" + CLIENT_VERSION + " (" + JAVA_RUNTIME + "; " + OPERATING_SYSTEM +"; " + PROCESSOR_ARCHITECTURE + ")";

    public static void throwTransportExceptionWithIotHubServiceType(String message, TransportException.IotHubService service) throws TransportException
    {
        TransportException transportException = new TransportException(message);
        transportException.setIotHubService(service);
        throw transportException;
    }

    public static void throwTransportExceptionWithIotHubServiceType(Exception e, TransportException.IotHubService service) throws TransportException
    {
        TransportException transportException = new TransportException(e);
        transportException.setIotHubService(service);
        throw transportException;
    }
}
