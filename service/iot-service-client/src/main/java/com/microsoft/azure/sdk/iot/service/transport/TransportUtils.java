// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.transport;

public class TransportUtils
{
    /** Version identifier key */
    public static final String versionIdentifierKey = "com.microsoft:client-version";
    public static String javaServiceClientIdentifier = "com.microsoft.azure.sdk.iot.iot-service-client/";
    public static String serviceVersion = "1.7.23";

    public static String getJavaServiceClientIdentifier()
    {
        return javaServiceClientIdentifier;
    }

    public static String getServiceVersion()
    {
        return serviceVersion;
    }

}