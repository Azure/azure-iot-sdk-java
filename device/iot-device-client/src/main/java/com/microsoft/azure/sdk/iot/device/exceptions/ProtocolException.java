/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

/**
 * Exception class that covers all exceptions that occur within transport protocol communication. For example, if a
 * amqp:connection:forced exception is given by AMQP.
 */
public class ProtocolException extends TransportException
{
    public ProtocolException()
    {
        super();
    }

    public ProtocolException(String message)
    {
        super(message);
    }

    public ProtocolException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ProtocolException(Throwable cause)
    {
        super(cause);
    }
}
