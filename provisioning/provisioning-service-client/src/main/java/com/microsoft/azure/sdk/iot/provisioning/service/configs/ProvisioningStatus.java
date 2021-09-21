// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * The Device Provisioning Service provisioning status.
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
public enum ProvisioningStatus implements Serializable
{
    @SerializedName("enabled")
    ENABLED,

    @SerializedName("disabled")
    DISABLED,
}
