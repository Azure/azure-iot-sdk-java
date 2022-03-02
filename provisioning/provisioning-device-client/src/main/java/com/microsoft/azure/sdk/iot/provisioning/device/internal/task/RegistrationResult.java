/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationResult;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientSubstatus;

public class RegistrationResult extends ProvisioningDeviceClientRegistrationResult
{
    /**
     * Constructor to set iothub uri, device id and status of the service as retrieved
     * @param iothubUri Value of iothub uri. Can be {@code null}
     * @param deviceId Value of device id. Can be {@code null}
     * @param jsonPayload Value of custom json payload. Can be {@code null}
     * @param dpsStatus Status of the service.
     */
    RegistrationResult(String iothubUri, String deviceId, String jsonPayload, ProvisioningDeviceClientStatus dpsStatus)
    {
        super();
        this.iothubUri = iothubUri;
        this.deviceId = deviceId;
        this.payload = jsonPayload;
        this.provisioningDeviceClientStatus = dpsStatus;
    }

    void setRegistrationId(String registrationId)
    {
        this.registrationId = registrationId;
    }

    void setCreatedDateTimeUtc(String createdDateTimeUtc)
    {
        this.createdDateTimeUtc = createdDateTimeUtc;
    }

    void setStatus(String status)
    {
        this.status = status;
    }

    void setSubstatus(ProvisioningDeviceClientSubstatus substatus)
    {
        this.substatus = substatus;
    }

    void setETag(String eTag)
    {
        this.eTag = eTag;
    }

    void setLastUpdatesDateTimeUtc(String lastUpdatesDateTimeUtc)
    {
        this.lastUpdatesDateTimeUtc = lastUpdatesDateTimeUtc;
    }
}
