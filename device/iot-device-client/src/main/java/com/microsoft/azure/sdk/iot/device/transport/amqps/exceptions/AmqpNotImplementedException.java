/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions;

import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;

/**
 * This exception is thrown when a amqp:not-implemented error is encountered over an AMQP connection
 *
 <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-complete-v1.0-os.pdf">For additional details, see this link</a>
 */
public class AmqpNotImplementedException extends ProtocolException
{
    public static final String errorCode = "amqp:not-implemented";

    public AmqpNotImplementedException()
    {
        super();
        this.isRetryable = false;
    }

    public AmqpNotImplementedException(String message)
    {
        super(message);
        this.isRetryable = false;
    }

    public AmqpNotImplementedException(String message, Throwable cause)
    {
        super(message, cause);
        this.isRetryable = false;
    }

    public AmqpNotImplementedException(Throwable cause)
    {
        super(cause);
        this.isRetryable = false;
    }
}
