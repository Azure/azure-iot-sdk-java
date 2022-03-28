/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import lombok.Getter;

/**
 * Exception that can occur when using a
 * {@link com.microsoft.azure.sdk.iot.device.DeviceClient}, {@link com.microsoft.azure.sdk.iot.device.ModuleClient},
 * or a {@link com.microsoft.azure.sdk.iot.device.MultiplexingClient}. Each instance contains the status code of the error
 * as well as the recommendation on whether the request that resulted in this exception is retryable or not.
 */
public class IotHubClientException extends Exception
{
    /**
     * The additional context on what the cause of this exception was.
     */
    @Getter
    private final IotHubStatusCode statusCode;

    /**
     * True if the action that resulted in this exception can be retried and false otherwise.
     */
    @Getter
    private final boolean isRetryable;

    public IotHubClientException(IotHubStatusCode statusCode)
    {
        this(statusCode, "");
    }

    public IotHubClientException(IotHubStatusCode statusCode, String errorMessage)
    {
        super(errorMessage);
        this.isRetryable = IotHubStatusCode.isRetryable(statusCode);
        this.statusCode = statusCode;
    }

    public IotHubClientException(IotHubStatusCode statusCode, Exception nestedException)
    {
        this(statusCode, "");
    }

    public IotHubClientException(IotHubStatusCode statusCode, String errorMessage, Exception nestedException)
    {
        super(nestedException);
        this.isRetryable = IotHubStatusCode.isRetryable(statusCode);
        this.statusCode = statusCode;
    }
}
