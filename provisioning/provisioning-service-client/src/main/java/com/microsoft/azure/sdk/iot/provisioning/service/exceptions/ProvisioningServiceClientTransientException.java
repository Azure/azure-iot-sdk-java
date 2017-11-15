/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.exceptions;

/**
 * This is the subset of the Device Provisioning Service exceptions for the exceptions related a temporary service issue.
 *
 * <p> The provisioning service will response a http request with one of the transient exception if
 *     the rest API was properly called, but the service is not able to execute that action at that
 *     time. These are the exceptions that a retry can help to fix the issue.
 * <p> HTTP status code 500 to 599.
 */
public class ProvisioningServiceClientTransientException extends ProvisioningServiceClientServiceException
{
    public ProvisioningServiceClientTransientException()
    {
        super();
    }

    public ProvisioningServiceClientTransientException(String message)
    {
        super(((message == null) || message.isEmpty()) ? "Device Provisioning Service transient error!" : message);
    }

    public ProvisioningServiceClientTransientException(String message, Throwable cause)
    {
        super((((message == null) || message.isEmpty()) ? "Device Provisioning Service transient error!" : message), cause);
    }

    public ProvisioningServiceClientTransientException(Throwable cause)
    {
        super(cause);
    }
}
