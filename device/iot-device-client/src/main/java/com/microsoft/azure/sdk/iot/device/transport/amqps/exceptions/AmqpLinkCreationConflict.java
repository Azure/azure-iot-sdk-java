package com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions;

import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;

public class AmqpLinkCreationConflict extends ProtocolException
{
    public static final String errorCode = "com.microsoft:link-creation-conflict";

    public AmqpLinkCreationConflict()
    {
        super();
        this.isRetryable = true;
    }

    public AmqpLinkCreationConflict(String message)
    {
        super(message);
        this.isRetryable = true;
    }

    public AmqpLinkCreationConflict(String message, Throwable cause)
    {
        super(message, cause);
        this.isRetryable = true;
    }

    public AmqpLinkCreationConflict(Throwable cause)
    {
        super(cause);
        this.isRetryable = true;
    }
}
