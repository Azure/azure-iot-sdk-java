/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

/**
 * Create iot hub not found exception
 */
public class IotHubNotSupportedException extends IotHubException
{
    public IotHubNotSupportedException()
    {
        super("Not supported");
    }

    public IotHubNotSupportedException(String message)
    {
        super(message);
    }
}
