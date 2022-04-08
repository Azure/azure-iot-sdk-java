package com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions;

import com.microsoft.azure.sdk.iot.device.transport.ProtocolException;

public class ProtonIOException extends ProtocolException
{
    public static final String errorCode = "proton:io";

    public ProtonIOException()
    {
        super();
        this.isRetryable = true;
    }

    public ProtonIOException(String message)
    {
        super(message);
        this.isRetryable = true;
    }

    public ProtonIOException(String message, Throwable cause)
    {
        super(message, cause);
        this.isRetryable = true;
    }

    public ProtonIOException(Throwable cause)
    {
        super(cause);
        this.isRetryable = true;
    }
}
