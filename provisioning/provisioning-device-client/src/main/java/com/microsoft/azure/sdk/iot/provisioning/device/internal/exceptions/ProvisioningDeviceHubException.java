/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions;

import lombok.Getter;
import lombok.Setter;

public class ProvisioningDeviceHubException extends ProvisioningDeviceClientException
{
    public ProvisioningDeviceHubException(String message)
    {
        super(message);
    }

    public ProvisioningDeviceHubException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ProvisioningDeviceHubException(Throwable cause)
    {
        super(cause);
    }

    /**
     * The error code sent from the service to clarify what exception occurred.
     */
    @Getter
    @Setter
    private int errorCode;
}
