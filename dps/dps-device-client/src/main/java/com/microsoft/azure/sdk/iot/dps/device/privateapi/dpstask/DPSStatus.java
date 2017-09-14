/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.dps.device.privateapi.dpstask;

public enum DPSStatus
{
    UNASSIGNED("unassigned"),
    FAILED("failed"),
    BLACKLISTED("blacklisted"),
    ASSIGNING("assigning"),
    ASSIGNED("assigned");

    private String status;
    DPSStatus(String status)
    {
        this.status = status;
    }

    public static DPSStatus fromString(String type)
    {
        for (DPSStatus dpsStatus : DPSStatus.values())
        {
            if (dpsStatus.status.equalsIgnoreCase(type))
            {
                return dpsStatus;
            }
        }
        return null;
    }
}
