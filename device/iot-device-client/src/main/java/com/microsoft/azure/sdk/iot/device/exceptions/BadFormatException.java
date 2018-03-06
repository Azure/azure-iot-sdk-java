/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

public class BadFormatException extends ProtocolException
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
}
