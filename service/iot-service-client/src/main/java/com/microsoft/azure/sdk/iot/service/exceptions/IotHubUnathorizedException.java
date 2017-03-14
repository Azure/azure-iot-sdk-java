/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

/**
 * Create unauthorized exception
 */
public class IotHubUnathorizedException extends IotHubException
{
    public IotHubUnathorizedException()
    {
        this(null);
    }

    public IotHubUnathorizedException(String message)
    {
        super("Unauthorized!" + (((message == null) || message.isEmpty()) ? "" : " " + message));
    }
}
