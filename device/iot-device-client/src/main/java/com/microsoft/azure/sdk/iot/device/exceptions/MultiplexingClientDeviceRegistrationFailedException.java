// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.exceptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Exception that is thrown when one or more devices fail to register to an active multiplexed connection.
 */
public class MultiplexingClientDeviceRegistrationFailedException extends MultiplexingClientException
{
    private Map<String, Exception> registrationExceptions = new HashMap<>();

    /**
     * Construct a new MultiplexingClientDeviceRegistrationFailedException with no nested exception and no error message.
     */
    public MultiplexingClientDeviceRegistrationFailedException()
    {
        super();
    }

    /**
     * Construct a new MultiplexingClientDeviceRegistrationFailedException with no nested exception but with an error message.
     * @param message The top level message for this exception.
     */
    public MultiplexingClientDeviceRegistrationFailedException(String message)
    {
        super(message);
    }

    /**
     * Construct a new MultiplexingClientDeviceRegistrationFailedException with a nested exception and an error message.
     * @param message The top level message for this exception.
     * @param cause The nested exception.
     */
    public MultiplexingClientDeviceRegistrationFailedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Construct a new MultiplexingClientDeviceRegistrationFailedException with a nested exception but no error message.
     * @param cause The nested exception.
     */
    public MultiplexingClientDeviceRegistrationFailedException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Associate a new registration exception to a device.
     * @param deviceId The device that failed to register to an active multiplexed connection.
     * @param registrationException The exception that explains why the device failed to register to an active multiplexed connection.
     */
    public void addRegistrationException(String deviceId, Exception registrationException)
    {
        Objects.requireNonNull(registrationException, "registrationException cannot be null");
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("DeviceId cannot be null or empty");
        }

        registrationExceptions.put(deviceId, registrationException);
    }

    /**
     * Get the exception for each device that failed to register.
     * @return A map containing the deviceId's that failed to register mapped to the exception that explains why they failed to register.
     */
    public Map<String, Exception> getRegistrationExceptions() {
        return registrationExceptions;
    }

    /**
     * Sets the full registrations exception map. This will overwrite any previously saved mappings.
     * @param registrationExceptions the new full registrations exception map.
     */
    public void setRegistrationExceptionsMap(Map<String, Exception> registrationExceptions)
    {
        this.registrationExceptions = registrationExceptions;
    }
}
