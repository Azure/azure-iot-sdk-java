/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions;

import com.microsoft.azure.sdk.iot.device.transport.ProtocolException;

/**
 * This exception is thrown when a amqp:illegal-state error is encountered over an AMQP connection
 *
 <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-complete-v1.0-os.pdf">For additional details, see this link</a>
 */
public class AmqpIllegalStateException extends ProtocolException
{
    public static final String errorCode = "amqp:illegal-state";

    public AmqpIllegalStateException()
    {
        super();
    }

    public AmqpIllegalStateException(String message)
    {
        super(message);
    }

    public AmqpIllegalStateException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AmqpIllegalStateException(Throwable cause)
    {
        super(cause);
    }
}
