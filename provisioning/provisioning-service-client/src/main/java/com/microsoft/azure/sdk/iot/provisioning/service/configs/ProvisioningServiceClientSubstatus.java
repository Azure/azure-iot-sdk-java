// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * The possible substatus values of a successful device provisioning.
 */
public enum ProvisioningServiceClientSubstatus implements Serializable
{
    /**
     * Device has been assigned to an IoT hub for the first time.
     */
    @SerializedName("initialAssignment")
    INITIAL_ASSIGNMENT,

    /**
     * Device has been assigned to a different IoT hub and its device data was migrated from the previously assigned
     * IoT hub. Device data was removed from the previously assigned IoT hub.
     */
    @SerializedName("deviceDataMigrated")
    DEVICE_DATA_MIGRATED,

    /**
     * Device has been assigned to a different IoT hub and its device data was populated from the initial state stored
     * in the enrollment. Device data was removed from the previously assigned IoT hub.
     */
    @SerializedName("deviceDataReset")
    DEVICE_DATA_RESET,

    /**
     * Device has been re-provisioned to a previously assigned IoT hub.
     */
    @SerializedName("reprovisionedToInitialAssignment")
    REPROVISIONED_TO_INITIAL_ASSIGNMENT,
}
