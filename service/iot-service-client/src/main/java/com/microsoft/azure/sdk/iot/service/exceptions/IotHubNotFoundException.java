/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

/**
 * Create iot hub not found exception
 */
public class IotHubNotFoundException extends IotHubException
{
    public IotHubNotFoundException()
    {
        this(null);
    }

    public IotHubNotFoundException(String message)
    {
        super(message);
    }

    IotHubNotFoundException(String message, int errorCode, ErrorCodeDescription errorCodeDescription)
    {
        super(message, errorCode, errorCodeDescription);
    }

    public static final String amqpErrorCode = "amqp:not-found";
}
