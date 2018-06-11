/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.hsm;

import com.microsoft.azure.sdk.iot.device.exceptions.DeviceClientException;

public class HsmException extends DeviceClientException
{
    public HsmException()
    {
    }

    public HsmException(String message)
    {
        super(message);
    }

    public HsmException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public HsmException(Throwable cause)
    {
        super(cause);
    }

    public HsmException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
