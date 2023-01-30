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
    }

    public InternalServerErrorException(String message)
    {
        super(message);
    }

    public InternalServerErrorException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InternalServerErrorException(Throwable cause)
    {
        super(cause);
    }

    @Override
    public IotHubStatusCode getStatusCode()
    {
        return IotHubStatusCode.INTERNAL_SERVER_ERROR;
    }
}
