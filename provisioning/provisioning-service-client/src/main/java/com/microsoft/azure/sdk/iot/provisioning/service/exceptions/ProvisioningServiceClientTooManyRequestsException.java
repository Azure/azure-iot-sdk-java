/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.exceptions;

/**
 * Create too many requests exception
 *
 * <p> Operations are being throttled by the service. For specific service limits, see IoT Hub Device Provisioning
 *     Service limits.
 * <p> HTTP status code 429.
 */
public class ProvisioningServiceClientTooManyRequestsException extends ProvisioningServiceClientBadUsageException
{
    public ProvisioningServiceClientTooManyRequestsException()
    {
        super();
    }

    public ProvisioningServiceClientTooManyRequestsException(String message)
    {
        super("Too many requests (throttled)!" + (((message == null) || message.isEmpty()) ? "" : " " + message));
    }
}
