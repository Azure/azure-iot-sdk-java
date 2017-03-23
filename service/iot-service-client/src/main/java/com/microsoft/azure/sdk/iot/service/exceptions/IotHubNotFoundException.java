/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

/**
 * Create iot hub not found exception
 */
public class IotHubNotFoundException extends IotHubException
{
    public IotHubNotFoundException()
    {
        this(null);
    }

    public IotHubNotFoundException(String message)
    {
        super("IoT Hub not found!" + (((message == null) || message.isEmpty()) ? "" : " " + message));
    }
}
