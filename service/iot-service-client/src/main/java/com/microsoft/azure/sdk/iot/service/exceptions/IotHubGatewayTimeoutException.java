/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

/**
 * Create gateway timeout exception (device does not answer in time)
 */
public class IotHubGatewayTimeoutException extends IotHubException
{
    public IotHubGatewayTimeoutException()
    {
        this(null);
    }
    public IotHubGatewayTimeoutException(String message)
    {
        super("Gateway timeout!" + (((message == null) || message.isEmpty()) ? "" : " " + message));
    }
}
