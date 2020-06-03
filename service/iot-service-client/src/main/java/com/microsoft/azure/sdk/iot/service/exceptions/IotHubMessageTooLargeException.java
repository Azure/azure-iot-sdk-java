/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

import com.microsoft.azure.sdk.iot.deps.serializer.ErrorCodeDescription;

/**
 * Create iot hub Message too large exception
 */
public class IotHubMessageTooLargeException extends IotHubException
{
    public IotHubMessageTooLargeException()
    {
        this(null);
    }

    public IotHubMessageTooLargeException(String message)
    {
        super(message);
    }

    IotHubMessageTooLargeException(String message, int errorCode, ErrorCodeDescription errorCodeDescription)
    {
        super(message, errorCode, errorCodeDescription);
    }
}
