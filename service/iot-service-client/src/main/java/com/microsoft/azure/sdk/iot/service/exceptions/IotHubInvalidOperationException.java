/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

/**
 * Create iot hub invalid operation exception
 */
public class IotHubInvalidOperationException extends IotHubException
{
    public IotHubInvalidOperationException()
    {
        super("Invalid operation!");
    }

    public IotHubInvalidOperationException(String message)
    {
        super(message);
    }
}
