/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.provisioningtask;

import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationInfo;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus;

class RegistrationInfo extends ProvisioningDeviceClientRegistrationInfo
{
    /**
     * Constructor to set iothub uri, device id and status of the service as retrieved
     * @param iothubUri Value of iothub uri. Can be {@code null}
     * @param deviceId Value of device id. Can be {@code null}
     * @param dpsStatus Status of the service.
     */
    RegistrationInfo(String iothubUri, String deviceId, ProvisioningDeviceClientStatus dpsStatus)
    {
        super();
        this.iothubUri = iothubUri;
        this.deviceId = deviceId;
        this.dpsStatus = dpsStatus;
    }
}
