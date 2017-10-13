/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal;

import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus;

public class ProvisioningDeviceClientRegistrationInfo extends com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationInfo
{
    public ProvisioningDeviceClientRegistrationInfo(String iothubUri, String deviceId, ProvisioningDeviceClientStatus dpsStatus)
    {
        super();
        this.iothubUri = iothubUri;
        this.deviceId = deviceId;
        this.dpsStatus = dpsStatus;
    }
}
