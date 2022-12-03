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
public class TpmRegistrationResult
{
    /**
     * Getter for the nonce provided by the service after provisioning a device through TPM.
     *
     * @see <a href="https://learn.microsoft.com/en-us/azure/iot-dps/concepts-tpm-attestation">TPM attestation</a>
     */
    @Getter
    protected String authenticationKey;
}
