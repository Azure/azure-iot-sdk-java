/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

/**
 * Create unauthorized exception
 */
public class IotHubUnauthorizedException extends IotHubException
{
    public IotHubUnauthorizedException()
    {
        this(null);
    }

    public IotHubUnauthorizedException(String message)
    {
        super(message);
    }

    IotHubUnauthorizedException(String message, int errorCode, ErrorCodeDescription errorCodeDescription)
    {
        super(message, errorCode, errorCodeDescription);
    }

    public static final String amqpErrorCode = "amqp:unauthorized-access";
}
