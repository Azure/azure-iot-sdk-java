/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

public class DeviceOperationTimeoutException extends DeviceClientException
{
    public DeviceOperationTimeoutException()
    {
        super();
    }

    public DeviceOperationTimeoutException(String message)
    {
        super(message);
    }

    public DeviceOperationTimeoutException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DeviceOperationTimeoutException(Throwable cause)
    {
        super(cause);
    }
}
