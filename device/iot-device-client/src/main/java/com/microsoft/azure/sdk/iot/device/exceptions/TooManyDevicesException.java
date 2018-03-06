/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

public class TooManyDevicesException extends ProtocolException
{
    public TooManyDevicesException()
    {
        super();
    }

    public TooManyDevicesException(String message)
    {
        super(message);
    }

    public TooManyDevicesException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TooManyDevicesException(Throwable cause)
    {
        super(cause);
    }
}
