/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device;

import lombok.Getter;
import lombok.Setter;

public class AdditionalData
{
    /**
     * The custom provisioning provisioningPayload to send to DPS during the registration process.
     */
    @Getter
    @Setter
    private String provisioningPayload;

    /**
     * The PEM encoded operational client certificate request that DPS will send to a linked certificate authority which
     * signs and returns an X.509 certificate that the IoT device can use to authenticate itself with an IoT Hub.
     */
    @Getter
    @Setter
    public String operationalCertificateRequest;
}
