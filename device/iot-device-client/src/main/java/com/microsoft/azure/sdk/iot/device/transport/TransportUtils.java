// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

public class TransportUtils
{
    public static final String JAVA_DEVICE_CLIENT_IDENTIFIER = "com.microsoft.azure.sdk.iot.iot-device-client/";
    public static final String CLIENT_VERSION = "1.9.0";

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
