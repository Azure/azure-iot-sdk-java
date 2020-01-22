package com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions;

import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;

public class ProtonIOException extends ProtocolException
{
    public static final String errorCode = "proton:io";

    public ProtonIOException()
    {
        super();
    }

    public ProtonIOException(String message)
    {
        super(message);
    }

    public ProtonIOException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ProtonIOException(Throwable cause)
    {
        super(cause);
    }
}
