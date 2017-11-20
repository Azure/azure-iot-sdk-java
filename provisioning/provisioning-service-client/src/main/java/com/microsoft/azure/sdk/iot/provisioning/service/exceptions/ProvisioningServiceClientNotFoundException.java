/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.exceptions;

/**
 * Create Device Provisioning Service not found exception
 *
 * <p> The Device Provisioning Service instance, or a resource (e.g. an enrollment) does not exist.
 * <p> HTTP status code 404.
 */
public class ProvisioningServiceClientNotFoundException extends ProvisioningServiceClientBadUsageException
{
    public ProvisioningServiceClientNotFoundException()
    {
        super();
    }

    public ProvisioningServiceClientNotFoundException(String message)
    {
        super("Device Provisioning Service not found!" + (((message == null) || message.isEmpty()) ? "" : " " + message));
    }
}
