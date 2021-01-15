/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

public enum ProvisioningStatus
{
    UNASSIGNED("unassigned"),
    ASSIGNING("assigning"),
    ASSIGNED("assigned"),
    FAILED("failed"),
    DISABLED("disabled");

    private final String status;

    /**
     * Constructor to create an enum
     * @param status status for which enum is to be created
     */
    ProvisioningStatus(String status)
    {
        //SRS_ProvisioningStatus_25_001: [ Constructor shall create an enum ]
        this.status = status;
    }

    /**
     * returns the enum corresponding to the provided status
     * @param status the status for which enum is requested
     * @return enum for the status if defined and {@code null} otherwise.
     */
    static ProvisioningStatus fromString(String status)
    {
        for (ProvisioningStatus provisioningStatus : ProvisioningStatus.values())
        {
            if (provisioningStatus.status.equalsIgnoreCase(status))
            {
                //SRS_ProvisioningStatus_25_002: [ This method shall return the enum corresponding to the status. ]
                return provisioningStatus;
            }
        }
        //SRS_ProvisioningStatus_25_003: [ If none of the enum's match the status it shall return null. ]
        return null;
    }
}
