// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * The Device Provisioning Service enrollment status.
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
public enum EnrollmentStatus implements Serializable
{
    @SerializedName("unassigned")
    UNASSIGNED,

    @SerializedName("assigning")
    ASSIGNING,

    @SerializedName("assigned")
    ASSIGNED,

    @SerializedName("failed")
    FAILED,

    @SerializedName("disabled")
    DISABLED,
}
