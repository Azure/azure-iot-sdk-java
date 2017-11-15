/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.exceptions;

/**
 * Create internal server error exception
 *
 * <p> An internal error occurred.
 * <p> HTTP status code 500.
 */
public class ProvisioningServiceClientInternalServerErrorException extends ProvisioningServiceClientTransientException
{
    public ProvisioningServiceClientInternalServerErrorException()
    {
        super();
    }

    public ProvisioningServiceClientInternalServerErrorException(String message)
    {
        super("Internal server error!" + (((message == null) || message.isEmpty()) ? "" : " " + message));
    }
}
