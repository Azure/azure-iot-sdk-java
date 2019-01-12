// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.SerializedName;

/**
 * Type of Device Provisioning Service attestation mechanism.
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
enum AttestationMechanismType
{
    @SerializedName("none") // There is no valid scenario for `NONE` Attestation Mechanism Type.
    NONE,

    @SerializedName("tpm")
    TPM,

    @SerializedName("x509")
    X509,

    @SerializedName("symmetricKey")
    SYMMETRIC_KEY,
}
