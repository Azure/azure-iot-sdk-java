/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device;

public class ProvisioningDeviceClientRegistrationResult
{
    protected String iothubUri;
    protected String deviceId;
    protected ProvisioningDeviceClientStatus provisioningDeviceClientStatus;

    public ProvisioningDeviceClientRegistrationResult()
    {
        //SRS_ProvisioningDeviceClientRegistrationResult_25_001: [ The constructor shall instantiate empty and leave it inheritors to set appropriate values of private members. ]
    }

    public String getIothubUri()
    {
        //SRS_ProvisioningDeviceClientRegistrationResult_25_002: [ This method shall retrieve iothubUri. ]
        return iothubUri;
    }

    public String getDeviceId()
    {
        //SRS_ProvisioningDeviceClientRegistrationResult_25_003: [ This method shall retrieve deviceId. ]
        return deviceId;
    }

    public ProvisioningDeviceClientStatus getProvisioningDeviceClientStatus()
    {
        //SRS_ProvisioningDeviceClientRegistrationResult_25_004: [ This method shall retrieve provisioningDeviceClientStatus. ]
        return provisioningDeviceClientStatus;
    }
}
