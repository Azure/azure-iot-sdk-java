/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.exceptions;

/**
 * Create precondition failed exception
 *
 * <p> The ETag in the request does not match the ETag of the existing resource, as per RFC7232.
 * <p> HTTP status code 412.
 */
public class ProvisioningServiceClientPreconditionFailedException extends ProvisioningServiceClientBadUsageException
{
    public ProvisioningServiceClientPreconditionFailedException()
    {
        super();
    }

    public ProvisioningServiceClientPreconditionFailedException(String message)
    {
        super("Precondition failed!" + (((message == null) || message.isEmpty()) ? "" : " " + message));
    }
}
