/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions;

import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;

/**
 * This exception is thrown when a amqp:session:window-violation error is encountered over an AMQP connection
 *
 * See {@linktourl http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-complete-v1.0-os.pdf}
 */
public class AmqpWindowViolationException extends ProtocolException
{
    public static final String errorCode = "amqp:session:window-violation";

    public AmqpWindowViolationException()
    {
        super();
        this.isRetryable = true;
    }

    public AmqpWindowViolationException(String message)
    {
        super(message);
        this.isRetryable = true;
    }

    public AmqpWindowViolationException(String message, Throwable cause)
    {
        super(message, cause);
        this.isRetryable = true;
    }

    public AmqpWindowViolationException(Throwable cause)
    {
        super(cause);
        this.isRetryable = true;
    }
}
