/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.exceptions;

/**
 * Create bad message format exception
 *
 * <p> The body of the Http request is not valid; for example, it cannot be parsed, or the object cannot be validated.
 * <p> HTTP status code 400.
 */
public class ProvisioningServiceClientBadFormatException extends ProvisioningServiceClientBadUsageException
{
    public ProvisioningServiceClientBadFormatException()
    {
        super();
    }

    public ProvisioningServiceClientBadFormatException(String message)
    {
        super("Bad message format!" + (((message == null) || message.isEmpty()) ? "" : " " + message));
    }
}
