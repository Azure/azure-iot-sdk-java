/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

public class PreconditionFailedException extends ProtocolException
{
    public PreconditionFailedException()
    {
        super();
    }

    public PreconditionFailedException(String message)
    {
        super(message);
    }

    public PreconditionFailedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PreconditionFailedException(Throwable cause)
    {
        super(cause);
    }
}
