/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

/**
 * Create IotHub size exceeded exceptions
 *
 * This exception will be throw when a function tries to add
 * content in a buffer or message and the final size will
 * exceed the maximum size acceptable by the IotHub.
 *
 */
public class IotHubSizeExceededException extends IotHubException
{
    public IotHubSizeExceededException()
    {
        this(null);
    }

    public IotHubSizeExceededException(String message)
    {
        super("IotHub size exceeded!" + (((message == null) || message.isEmpty()) ? "" : " " + message));
    }
}
