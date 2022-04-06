/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions;

import com.microsoft.azure.sdk.iot.device.transport.ProtocolException;

/**
 * This exception is thrown when a com.microsoft:device-container-throttled error is encountered over an AMQP connection
 */
public class AmqpConnectionThrottledException extends ProtocolException
{
    public static final String errorCode = "com.microsoft:device-container-throttled";

    public AmqpConnectionThrottledException()
    {
        super();
        this.isRetryable = true;
    }

    public AmqpConnectionThrottledException(String message)
    {
        super(message);
        this.isRetryable = true;
    }

    public AmqpConnectionThrottledException(String message, Throwable cause)
    {
        super(message, cause);
        this.isRetryable = true;
    }

    public AmqpConnectionThrottledException(Throwable cause)
    {
        super(cause);
        this.isRetryable = true;
    }
}
