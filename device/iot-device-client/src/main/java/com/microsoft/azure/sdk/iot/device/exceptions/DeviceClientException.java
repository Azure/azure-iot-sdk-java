/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

public class DeviceClientException extends Exception
{
    public DeviceClientException()
    {
    }

    public DeviceClientException(String message)
    {
        super(message);
    }

    public DeviceClientException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DeviceClientException(Throwable cause)
    {
        super(cause);
    }

    public DeviceClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
