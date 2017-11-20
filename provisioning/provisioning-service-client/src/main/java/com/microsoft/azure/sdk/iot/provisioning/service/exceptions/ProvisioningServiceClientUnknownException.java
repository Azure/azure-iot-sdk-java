/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.exceptions;
/**
 * This is the subset of the Device Provisioning Service exceptions for the unknown issues.
 *
 * <p> HTTP status code 300+, but not 4nn or 5nn.
 */
public class ProvisioningServiceClientUnknownException extends ProvisioningServiceClientServiceException
{
    public ProvisioningServiceClientUnknownException()
    {
        super();
    }

    public ProvisioningServiceClientUnknownException(String message)
    {
        super(((message == null) || message.isEmpty()) ? "Device Provisioning Service unknown error!" : message);
    }

    public ProvisioningServiceClientUnknownException(String message, Throwable cause)
    {
        super((((message == null) || message.isEmpty()) ? "Device Provisioning Service unknown error!" : message), cause);
    }

    public ProvisioningServiceClientUnknownException(Throwable cause)
    {
        super(cause);
    }
}
