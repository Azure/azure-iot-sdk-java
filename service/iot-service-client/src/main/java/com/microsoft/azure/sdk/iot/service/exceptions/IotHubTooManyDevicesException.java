/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

/**
 * Create too many devices exception
 */
public class IotHubTooManyDevicesException extends IotHubException
{
    public IotHubTooManyDevicesException()
    {
        this(null);
    }
    public IotHubTooManyDevicesException(String message)
    {
        super("Too many devices!" + (((message == null) || message.isEmpty()) ? "" : " " + message));
    }
}
