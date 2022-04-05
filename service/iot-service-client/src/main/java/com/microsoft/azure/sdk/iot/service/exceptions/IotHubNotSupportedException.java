/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

/**
 * Create iot hub not found exception
 */
public class IotHubNotSupportedException extends IotHubException
{
    public IotHubNotSupportedException()
    {
        this(null);
    }

    public IotHubNotSupportedException(String message)
    {
        super(message);
    }

    IotHubNotSupportedException(String message, int errorCode, ErrorCodeDescription errorCodeDescription)
    {
        super(message, errorCode, errorCodeDescription);
    }

    public static final String amqpErrorCode = "amqp:not-implemented";
}
