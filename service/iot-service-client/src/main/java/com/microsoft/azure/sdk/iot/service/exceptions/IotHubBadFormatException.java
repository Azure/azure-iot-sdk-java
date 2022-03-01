/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

/**
 * Create bad message format exception
 */
public class IotHubBadFormatException extends IotHubException 
{
    public IotHubBadFormatException()
    {
        this(null);
    }

    public IotHubBadFormatException(String message)
    {
        super(message);
    }

    IotHubBadFormatException(String message, int errorCode, ErrorCodeDescription errorCodeDescription)
    {
        super(message, errorCode, errorCodeDescription);
    }
}
