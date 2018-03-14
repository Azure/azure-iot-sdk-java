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
    }

    public PreconditionFailedException(String message)
    {
        super(message);
    }

    public PreconditionFailedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PreconditionFailedException(Throwable cause)
    {
        super(cause);
    }

    @Override
    public IotHubStatusCode getStatusCode()
    {
        return IotHubStatusCode.PRECONDITION_FAILED;
    }
}
