/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.exceptions;

/**
 * This is the subset of the Device Provisioning Service exceptions for the exceptions related to usage error.
 *
 * <p> The provisioning service will response a http request with one of the bad usage exception if
 *     the rest API was properly called, with a usage issue, for instance the user is not authorized
 *     for that operation.
 * <p> HTTP status code 400 to 499.
 */
public class ProvisioningServiceClientBadUsageException extends ProvisioningServiceClientServiceException
{
    public ProvisioningServiceClientBadUsageException()
    {
        super();
    }

    public ProvisioningServiceClientBadUsageException(String message)
    {
        super(((message == null) || message.isEmpty()) ? "Bad usage!" : message);
    }

    public ProvisioningServiceClientBadUsageException(String message, Throwable cause)
    {
        super((((message == null) || message.isEmpty()) ? "Bad usage!" : message), cause);
    }

    public ProvisioningServiceClientBadUsageException(Throwable cause)
    {
        super(cause);
    }
}
