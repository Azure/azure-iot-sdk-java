/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device;

import lombok.Getter;
import lombok.Setter;
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

    @Getter
    protected String deviceId;

    @Getter
    protected ProvisioningDeviceClientStatus provisioningDeviceClientStatus;

    @Getter
    protected String provisioningPayload;

    /*
     * The client certificate used by IoT Hub to authenticate a device.
     */
    @Getter
    @Setter
    protected String issuedClientCertificate;
}
