/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.provisioningtask;

public enum ProvisioningStatus
{
    UNASSIGNED("unassigned"),
    FAILED("failed"),
    BLACKLISTED("blacklisted"),
    ASSIGNING("assigning"),
    ASSIGNED("assigned");

    private String status;
    ProvisioningStatus(String status)
    {
        this.status = status;
    }

    public static ProvisioningStatus fromString(String type)
    {
        for (ProvisioningStatus provisioningStatus : ProvisioningStatus.values())
        {
            if (provisioningStatus.status.equalsIgnoreCase(type))
            {
                return provisioningStatus;
            }
        }
        return null;
    }
}
