// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.provisioning.device;

/**
 * The possible substatus values of a successful device provisioning.
 */
public enum ProvisioningDeviceClientSubstatus
{
    /**
     * Device has been assigned to an IoT hub for the first time.
     */
    INITIAL_ASSIGNMENT("initialAssignment"),

    /**
     * Device has been assigned to a different IoT hub and its device data was migrated from the previously assigned
     * IoT hub. Device data was removed from the previously assigned IoT hub.
     */
    DEVICE_DATA_MIGRATED("deviceDataMigrated"),

    /**
     * Device has been assigned to a different IoT hub and its device data was populated from the initial state stored
     * in the enrollment. Device data was removed from the previously assigned IoT hub.
     */
    DEVICE_DATA_RESET("deviceDataReset"),

    /**
     * Device has been re-provisioned to a previously assigned IoT hub.
     */
    REPROVISIONED_TO_INITIAL_ASSIGNMENT("reprovisionedToInitialAssignment");

    private final String substatus;

    ProvisioningDeviceClientSubstatus(String substatus)
    {
        this.substatus = substatus;
    }

    public String getValue()
    {
        return this.substatus;
    }

    public static ProvisioningDeviceClientSubstatus fromString(String substatus)
    {
        for (ProvisioningDeviceClientSubstatus substatusCode : ProvisioningDeviceClientSubstatus.values())
        {
            if (substatusCode.substatus.equalsIgnoreCase(substatus))
            {
                return substatusCode;
            }
        }

        return null;
    }
}
