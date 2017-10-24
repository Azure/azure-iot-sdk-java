/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions;

public class ProvisioningDeviceClientException extends Exception
{
    public ProvisioningDeviceClientException(String message)
    {
        super(message);
    }

    public ProvisioningDeviceClientException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ProvisioningDeviceClientException(Throwable cause)
    {
        super(cause);
    }
}
