/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.dps.device.privateapi;

import com.microsoft.azure.sdk.iot.dps.device.DPSDeviceStatus;

public class DPSRegistrationInfo extends com.microsoft.azure.sdk.iot.dps.device.DPSRegistrationInfo
{
    public DPSRegistrationInfo(String iothubUri, String deviceId, DPSDeviceStatus dpsStatus)
    {
        super();
        this.iothubUri = iothubUri;
        this.deviceId = deviceId;
        this.dpsStatus = dpsStatus;
    }
}
