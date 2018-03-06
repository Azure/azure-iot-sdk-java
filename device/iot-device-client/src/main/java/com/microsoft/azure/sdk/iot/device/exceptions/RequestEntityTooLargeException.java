/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

public class RequestEntityTooLargeException extends ProtocolException
{
    public RequestEntityTooLargeException()
    {
        super();
    }

    public RequestEntityTooLargeException(String message)
    {
        super(message);
    }

    public RequestEntityTooLargeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RequestEntityTooLargeException(Throwable cause)
    {
        super(cause);
    }
}
