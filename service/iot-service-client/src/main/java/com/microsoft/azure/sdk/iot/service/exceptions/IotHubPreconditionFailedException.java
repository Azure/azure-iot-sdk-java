/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

import com.microsoft.azure.sdk.iot.deps.serializer.ErrorCodeDescription;

/**
 * Create precondition failed exception
 */
public class IotHubPreconditionFailedException extends IotHubException
{
    public IotHubPreconditionFailedException()
    {
        this(null);
    }

    public IotHubPreconditionFailedException(String message)
    {
        super(message);
    }

    IotHubPreconditionFailedException(String message, int errorCode, ErrorCodeDescription errorCodeDescription)
    {
        super(message, errorCode, errorCodeDescription);
    }
}
