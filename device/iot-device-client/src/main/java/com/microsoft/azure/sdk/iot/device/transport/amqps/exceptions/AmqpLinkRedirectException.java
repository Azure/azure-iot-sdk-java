/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions;

import com.microsoft.azure.sdk.iot.device.transport.ProtocolException;

/**
 * This exception is thrown when a amqp:link:redirect error is encountered over an AMQP connection
 *
 * Check the headers of the message with this error to see where you are being redirected to.
 *
 * <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-complete-v1.0-os.pdf">For additional details, see this link</a>
 */
public class AmqpLinkRedirectException extends ProtocolException
{
    public static final String errorCode = "amqp:link:redirect";

    public AmqpLinkRedirectException()
    {
        super();
        this.isRetryable = true;
    }

    public AmqpLinkRedirectException(String message)
    {
        super(message);
        this.isRetryable = true;
    }

    public AmqpLinkRedirectException(String message, Throwable cause)
    {
        super(message, cause);
        this.isRetryable = true;
    }

    public AmqpLinkRedirectException(Throwable cause)
    {
        super(cause);
        this.isRetryable = true;
    }
}
