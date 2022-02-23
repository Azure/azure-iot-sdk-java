/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

/**
 * Create unauthorized exception
 */
public class IotHubUnathorizedException extends IotHubException
{
    public IotHubUnathorizedException()
    {
        this(null);
    }

    public IotHubUnathorizedException(String message)
    {
        super(message);
    }

    IotHubUnathorizedException(String message, int errorCode, ErrorCodeDescription errorCodeDescription)
    {
        super(message, errorCode, errorCodeDescription);
    }

    public static final String amqpErrorCode = "amqp:unauthorized-access";
}
