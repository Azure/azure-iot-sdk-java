/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.dps.device;

public class DPSRegistrationInfo
{
    protected String iothubUri;
    protected String deviceId;
    protected DPSDeviceStatus dpsStatus;

    public DPSRegistrationInfo()
    {
    }

    public String getIothubUri()
    {
        return iothubUri;
    }

    public String getDeviceId()
    {
        return deviceId;
    }

    public DPSDeviceStatus getDpsStatus()
    {
        return dpsStatus;
    }
}
