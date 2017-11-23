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
    private static final String PROVISIONING_DEVICE_CLIENT = "com.microsoft.azure.sdk.iot.dps.dps-device-client/";
    private static final String PROVISIONING_DEVICE_CLIENT_VERSION = "0.0.1";


    public static String getServiceApiVersion()
    {
        return SERVICE_API_VERSION;
    }

    public static String getUserAgentString()
    {
        return PROVISIONING_DEVICE_CLIENT + PROVISIONING_DEVICE_CLIENT_VERSION;
    }
}
