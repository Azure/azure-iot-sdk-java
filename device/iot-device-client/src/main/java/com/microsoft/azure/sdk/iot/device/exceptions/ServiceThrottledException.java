/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

public class ServiceThrottledException extends IotHubServiceException
{
    public ServiceThrottledException()
    {
        super();
        this.isRetryable = true;
    }

    public ServiceThrottledException(String message)
    {
        super(message);
        this.isRetryable = true;
    }

    public ServiceThrottledException(String message, Throwable cause)
    {
        super(message, cause);
        this.isRetryable = true;
    }

    public ServiceThrottledException(Throwable cause)
    {
        super(cause);
        this.isRetryable = true;
    }
}
