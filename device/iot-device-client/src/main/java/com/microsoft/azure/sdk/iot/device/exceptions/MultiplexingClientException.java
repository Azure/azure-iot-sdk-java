// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.exceptions;

/**
 * The top level exception class for all operations in {@link com.microsoft.azure.sdk.iot.device.MultiplexingClient} that
 * aren't {@link InterruptedException} or {@link java.io.IOException}.
 */
public class MultiplexingClientException extends Exception {
    public MultiplexingClientException() {
    }

    public MultiplexingClientException(String message)
    {
        super(message);
    }

    public MultiplexingClientException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public MultiplexingClientException(Throwable cause)
    {
        super(cause);
    }
}
