/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

import com.microsoft.azure.sdk.iot.service.serializer.ErrorCodeDescription;

/**
 * Create iot hub invalid operation exception
 */
public class IotHubInvalidOperationException extends IotHubException
{
    public IotHubInvalidOperationException()
    {
        this(null);
    }

    public IotHubInvalidOperationException(String message)
    {
        super(message);
    }

    IotHubInvalidOperationException(String message, int errorCode, ErrorCodeDescription errorCodeDescription)
    {
        super(message, errorCode, errorCodeDescription);
    }
}
