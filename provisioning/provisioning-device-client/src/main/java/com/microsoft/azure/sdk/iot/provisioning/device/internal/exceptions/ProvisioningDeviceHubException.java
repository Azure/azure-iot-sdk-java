/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions;

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
}
