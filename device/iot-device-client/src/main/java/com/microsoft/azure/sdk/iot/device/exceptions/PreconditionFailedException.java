/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;

public class PreconditionFailedException extends IotHubServiceException
{
    public PreconditionFailedException()
    {
        super();
        this.isRetryable = false;
    }

    public PreconditionFailedException(String message)
    {
        super(message);
        this.isRetryable = false;
    }

    public PreconditionFailedException(String message, Throwable cause)
    {
        super(message, cause);
        this.isRetryable = false;
    }

    public PreconditionFailedException(Throwable cause)
    {
        super(cause);
        this.isRetryable = false;
    }

    @Override
    public IotHubStatusCode getStatusCode()
    {
        return IotHubStatusCode.PRECONDITION_FAILED;
    }
}
