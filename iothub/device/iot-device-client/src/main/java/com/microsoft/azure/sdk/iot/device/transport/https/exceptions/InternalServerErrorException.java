/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.https.exceptions;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.transport.IotHubServiceException;

public class InternalServerErrorException extends IotHubServiceException
{
    public InternalServerErrorException()
    {
        super();
        this.isRetryable = true;
    }

    public InternalServerErrorException(String message)
    {
        super(message);
        this.isRetryable = true;
    }

    public InternalServerErrorException(String message, Throwable cause)
    {
        super(message, cause);
        this.isRetryable = true;
    }

    public InternalServerErrorException(Throwable cause)
    {
        super(cause);
        this.isRetryable = true;
    }

    @Override
    public IotHubStatusCode getStatusCode()
    {
        return IotHubStatusCode.INTERNAL_SERVER_ERROR;
    }
}
