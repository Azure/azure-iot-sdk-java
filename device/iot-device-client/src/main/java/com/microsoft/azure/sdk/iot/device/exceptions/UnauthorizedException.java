/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;

public class UnauthorizedException extends IotHubServiceException
{
    public UnauthorizedException()
    {
        super();
        this.isRetryable = false;
    }

    public UnauthorizedException(String message)
    {
        super(message);
        this.isRetryable = false;
    }

    public UnauthorizedException(String message, Throwable cause)
    {
        super(message, cause);
        this.isRetryable = false;
    }

    public UnauthorizedException(Throwable cause)
    {
        super(cause);
        this.isRetryable = false;
    }

    @Override
    public IotHubStatusCode getStatusCode()
    {
        return IotHubStatusCode.UNAUTHORIZED;
    }
}
