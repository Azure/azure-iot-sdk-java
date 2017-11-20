/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.exceptions;

/**
 * Create transport exception
 */
public class ProvisioningServiceClientTransportException extends ProvisioningServiceClientException
{
    public ProvisioningServiceClientTransportException()
    {
        super();
    }

    public ProvisioningServiceClientTransportException(String message)
    {
        super(message);
    }

    public ProvisioningServiceClientTransportException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ProvisioningServiceClientTransportException(Throwable cause)
    {
        super(cause);
    }
}
