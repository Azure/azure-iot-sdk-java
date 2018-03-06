/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

public class InternalServerErrorException extends ProtocolException
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
}
