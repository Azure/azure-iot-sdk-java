/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

/**
 * Create iot hub not found exception
 */
public class IotHubDeviceNotFoundException extends IotHubException
{
    public IotHubDeviceNotFoundException()
    {
        this(null);
    }

    public IotHubDeviceNotFoundException(String message)
    {
        super("Device not found!" + (((message == null) || message.isEmpty()) ? "" : " " + message));
    }
}
