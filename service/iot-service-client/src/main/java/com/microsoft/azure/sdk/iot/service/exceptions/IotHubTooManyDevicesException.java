/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

import com.microsoft.azure.sdk.iot.deps.serializer.ErrorCodeDescription;

/**
 * Create too many devices exception
 */
public class IotHubTooManyDevicesException extends IotHubException
{
    public IotHubTooManyDevicesException()
    {
        this(null);
    }

    public IotHubTooManyDevicesException(String message)
    {
        super(message);
    }

    IotHubTooManyDevicesException(String message, int errorCode, ErrorCodeDescription errorCodeDescription)
    {
        super(message, errorCode, errorCodeDescription);
    }
}
