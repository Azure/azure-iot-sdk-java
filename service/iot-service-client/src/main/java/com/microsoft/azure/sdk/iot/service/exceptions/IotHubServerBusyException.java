/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

import com.microsoft.azure.sdk.iot.service.serializers.ErrorCodeDescription;

/**
 * Create server busy exception
 */
public class IotHubServerBusyException extends IotHubException
{
    public IotHubServerBusyException()
    {
        this(null);
    }

    public IotHubServerBusyException(String message)
    {
        super(message);
    }

    IotHubServerBusyException(String message, int errorCode, ErrorCodeDescription errorCodeDescription)
    {
        super(message, errorCode, errorCodeDescription);
    }
}
