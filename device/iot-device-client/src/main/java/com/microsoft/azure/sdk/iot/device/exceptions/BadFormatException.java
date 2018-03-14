/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;

public class BadFormatException extends IotHubServiceException
{
    public BadFormatException()
    {
        super();
    }

    public BadFormatException(String message)
    {
        super(message);
    }

    public BadFormatException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BadFormatException(Throwable cause)
    {
        super(cause);
    }

    @Override
    public IotHubStatusCode getStatusCode()
    {
        return IotHubStatusCode.BAD_FORMAT;
    }
}
