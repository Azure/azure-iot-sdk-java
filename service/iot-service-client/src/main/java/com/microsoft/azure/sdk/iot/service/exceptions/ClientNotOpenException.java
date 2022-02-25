// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.exceptions;

/**
 * Exception thrown by {@link com.microsoft.azure.sdk.iot.service.messaging.MessagingClient} when a user attempts to perform
 * an operation that is only supported when the client is open
 */
public class ClientNotOpenException extends Exception
{
    public ClientNotOpenException()
    {
        this(null);
    }

    public ClientNotOpenException(String message)
    {
        super(message);
    }
}
