/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;

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

    /**
     * <p>
     * the issued client certificate chain in response to a certificate signing request.
     * This list will be null if no certificate signing request was provided during registration.
     * </p>
     * <p>
     * The certificate chain is returned as an array of base64-encoded certificates.
     * The first element is the device/leaf certificate, followed by intermediate CA certificates.
     * </p>
     */
    @Getter
    protected List<String> issuedClientCertificateChain;
}
