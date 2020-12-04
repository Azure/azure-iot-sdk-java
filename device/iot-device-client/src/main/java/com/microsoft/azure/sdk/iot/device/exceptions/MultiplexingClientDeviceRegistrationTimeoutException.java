// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.exceptions;

/**
 * Exception that is thrown when a multiplexed device client registration or unregistration times out.
 */
public class MultiplexingClientDeviceRegistrationTimeoutException extends MultiplexingClientException
{
    /**
     * Construct a new MultiplexingClientDeviceRegistrationTimeoutException with no nested exception and no error message.
     */
    public MultiplexingClientDeviceRegistrationTimeoutException()
    {
        super();
    }

    /**
     * Construct a new MultiplexingClientDeviceRegistrationTimeoutException with no nested exception but with an error message.
     * @param message The top level message for this exception.
     */
    public MultiplexingClientDeviceRegistrationTimeoutException(String message)
    {
        super(message);
    }

    /**
     * Construct a new MultiplexingClientDeviceRegistrationTimeoutException with a nested exception and an error message.
     * @param message The top level message for this exception.
     * @param cause The nested exception.
     */
    public MultiplexingClientDeviceRegistrationTimeoutException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Construct a new MultiplexingClientDeviceRegistrationTimeoutException with a nested exception but no error message.
     * @param cause The nested exception.
     */
    public MultiplexingClientDeviceRegistrationTimeoutException(Throwable cause)
    {
        super(cause);
    }
}
