/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device;

public class AdditionalData
{
    private String payload;

    /**
     * Set the Custom Provisioning payload to send to DPS during the registration process
     * @param jsonPayload The json payload that will be transferred to DPS
     */
    public void setProvisioningPayload(String jsonPayload)
    {
        this.payload = jsonPayload;
    }

    /**
     * Gets the Custom Provisioning payload that was set for the provisioning payload
     * @return Returns payload data for provisioning. Can be {@code null} when no data is set.
     */
    public String getProvisioningPayload()
    {
        return this.payload;
    }
}
