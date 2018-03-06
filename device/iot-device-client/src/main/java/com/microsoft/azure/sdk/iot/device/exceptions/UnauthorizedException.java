/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

public class UnauthorizedException extends ProtocolException
{
    public UnauthorizedException()
    {
        super();
    }

    public UnauthorizedException(String message)
    {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public UnauthorizedException(Throwable cause)
    {
        super(cause);
    }
}
