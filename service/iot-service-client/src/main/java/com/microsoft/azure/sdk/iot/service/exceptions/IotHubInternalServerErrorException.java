/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

/**
 * Create internal server error exception
 */
public class IotHubInternalServerErrorException extends IotHubException
{
    public IotHubInternalServerErrorException()
    {
        this(null);
    }
    public IotHubInternalServerErrorException(String message)
    {
        super("Internal server error!" + (((message == null) || message.isEmpty()) ? "" : " " + message));
    }
}
