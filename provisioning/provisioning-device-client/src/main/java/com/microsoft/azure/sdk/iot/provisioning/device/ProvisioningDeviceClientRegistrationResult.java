/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProvisioningDeviceClientRegistrationResult
{
    @Getter
    protected String iothubUri;

    @Getter
    protected String registrationId;

    @Getter
    protected String createdDateTimeUtc;

    @Getter
    protected String status;

    @Getter
    protected ProvisioningDeviceClientSubstatus substatus;

    @Getter
    protected String eTag;

    @Getter
    protected String lastUpdatesDateTimeUtc;

    /**
     * Getter for the IotHubUri.
     * @return returns IotHubUri. Can be {@code null} when registration fails.
     */
    public String getIothubUri()
    {
        //SRS_ProvisioningDeviceClientRegistrationResult_25_002: [ This method shall retrieve iothubUri. ]
        return iothubUri;
    }

    @Getter
    protected String deviceId;

    @Getter
    protected ProvisioningDeviceClientStatus provisioningDeviceClientStatus;

    @Getter
    protected String provisioningPayload;
}
