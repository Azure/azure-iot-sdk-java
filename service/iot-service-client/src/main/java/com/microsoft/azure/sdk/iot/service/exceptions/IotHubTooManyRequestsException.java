/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

import com.microsoft.azure.sdk.iot.deps.serializer.ErrorCodeDescription;

/**
 * Create too many requests exception
 */
public class IotHubTooManyRequestsException extends IotHubException
{
    public IotHubTooManyRequestsException()
    {
        this(null);
    }

    public IotHubTooManyRequestsException(String message)
    {
        super(message);
    }

    IotHubTooManyRequestsException(String message, int errorCode, ErrorCodeDescription errorCodeDescription)
    {
        super(message, errorCode, errorCodeDescription);
    }
}
