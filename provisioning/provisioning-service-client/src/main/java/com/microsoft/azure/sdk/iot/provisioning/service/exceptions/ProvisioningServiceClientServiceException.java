/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.exceptions;

/**
 * This is the subset of the Device Provisioning Service exceptions for the exceptions reported by the Service.
 */
public class ProvisioningServiceClientServiceException extends ProvisioningServiceClientException
{
    public ProvisioningServiceClientServiceException()
    {
        super();
    }

    public ProvisioningServiceClientServiceException(String message)
    {
        super(((message == null) || message.isEmpty()) ? "Device Provisioning Service error!" : message);
    }

    public ProvisioningServiceClientServiceException(String message, Throwable cause)
    {
        super((((message == null) || message.isEmpty()) ? "Device Provisioning Service error!" : message), cause);
    }

    public ProvisioningServiceClientServiceException(Throwable cause)
    {
        super(cause);
    }
}
