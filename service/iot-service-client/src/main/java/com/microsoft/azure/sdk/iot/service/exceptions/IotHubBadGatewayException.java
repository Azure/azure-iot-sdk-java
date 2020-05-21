/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

import com.microsoft.azure.sdk.iot.deps.serializer.ErrorCodeDescription;

/**
 * Create bad gateway exception (device sent malformed response)
 */
public class IotHubBadGatewayException extends IotHubException
{
    public IotHubBadGatewayException()
    {
        this(null);
    }

    public IotHubBadGatewayException(String message)
    {
        super(message);
    }

    IotHubBadGatewayException(String message, int errorCode, ErrorCodeDescription errorCodeDescription)
    {
        super(message, errorCode, errorCodeDescription);
    }
}
