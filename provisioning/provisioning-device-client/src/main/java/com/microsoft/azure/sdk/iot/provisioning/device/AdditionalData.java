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
     * <p>
     * the base64-encoded Certificate Signing Request (CSR) to be sent during registration.
     * When set, the DPS service will return an issued certificate chain in the registration result.
     * </p>
     * <p>
     * The CSR should be a base64-encoded DER format CSR.
     * The Common Name (CN) in the CSR should match the registration ID.
     * </p>
     */
    @Getter
    @Setter
    private String clientCertificateSigningRequest;
}
