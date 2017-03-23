/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

/**
 * Create server busy exception
 */
public class IotHubServerBusyException extends IotHubException
{
    public IotHubServerBusyException()
    {
        this(null);
    }
    public IotHubServerBusyException(String message)
    {
        super("Server busy!" + (((message == null) || message.isEmpty()) ? "" : " " + message));
    }
}
