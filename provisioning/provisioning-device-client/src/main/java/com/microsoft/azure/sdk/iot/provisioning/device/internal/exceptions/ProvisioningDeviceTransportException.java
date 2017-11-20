/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions;

public class ProvisioningDeviceTransportException extends ProvisioningDeviceClientException
{
    public ProvisioningDeviceTransportException(String message)
    {
        super(message);
    }

    public ProvisioningDeviceTransportException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ProvisioningDeviceTransportException(Throwable cause)
    {
        super(cause);
    }
}
