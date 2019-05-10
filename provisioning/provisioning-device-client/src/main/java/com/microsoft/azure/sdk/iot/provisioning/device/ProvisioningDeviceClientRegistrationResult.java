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
    protected String payload;
    /**
     * Empty constructor to let users gather the data.
     */
    public ProvisioningDeviceClientRegistrationResult()
    {
        //SRS_ProvisioningDeviceClientRegistrationResult_25_001: [ The constructor shall instantiate empty and leave it inheritors to set appropriate values of private members. ]
    }

    /**
     * Getter for the IotHubUri.
     * @return returns IotHubUri. Can be {@code null} when registration fails.
     */
    public String getIothubUri()
    {
        //SRS_ProvisioningDeviceClientRegistrationResult_25_002: [ This method shall retrieve iothubUri. ]
        return iothubUri;
    }

    /**
     * Getter for Device ID.
     * @return Returns device ID. Can be {@code null} when registration fails.
     */
    public String getDeviceId()
    {
        //SRS_ProvisioningDeviceClientRegistrationResult_25_003: [ This method shall retrieve deviceId. ]
        return deviceId;
    }

    /**
     * Getter for the Provisioning Device Client Status
     * @return Returns the status of Provisioning Device Client
     */
    public ProvisioningDeviceClientStatus getProvisioningDeviceClientStatus()
    {
        //SRS_ProvisioningDeviceClientRegistrationResult_25_004: [ This method shall retrieve provisioningDeviceClientStatus. ]
        return provisioningDeviceClientStatus;
    }

    /**
     * Retrieves the provisioning payload results from DPS
     * @return A string representation of the provisioning payload
     */
    public String getProvisioningPayload()
    {
        return payload;
    }

}
