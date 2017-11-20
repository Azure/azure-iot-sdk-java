// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

/**
 * This is the abstract class that unifies all possible types of attestation that Device Provisioning Service supports.
 *
 * <p> For now, the provisioning service supports {@link TpmAttestation} or {@link X509Attestation}.
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
public abstract class Attestation
{
    // Abstract class fully implemented by the child.
}
