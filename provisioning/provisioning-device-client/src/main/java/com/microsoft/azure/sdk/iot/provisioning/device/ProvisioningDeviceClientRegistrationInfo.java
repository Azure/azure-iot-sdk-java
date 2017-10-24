/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device;

public class ProvisioningDeviceClientRegistrationInfo
{
    protected String iothubUri;
    protected String deviceId;
    protected ProvisioningDeviceClientStatus dpsStatus;

    public ProvisioningDeviceClientRegistrationInfo()
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

    public ProvisioningDeviceClientStatus getDpsStatus()
    {
        return dpsStatus;
    }
}
