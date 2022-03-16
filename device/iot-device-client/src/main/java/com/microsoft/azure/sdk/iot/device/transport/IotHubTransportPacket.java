// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;

/**
 * A packet containing the data needed for an IoT Hub transport to send a
 * message.
 */
public final class IotHubTransportPacket
{
    private final Message message;
    private final IotHubEventCallback eventCallback;
    private final Object callbackContext;
    private IotHubStatusCode status;
    private final long startTimeMillis;
    private int currentRetryAttempt;
    private final String deviceId;

    /**
     * Constructor.
     *
     * @param message the message to be sent.
     * @param eventCallback the callback to be invoked when a response from the IoT Hub is received.
     * @param callbackContext the context to be passed to the callback.
     * @param status the status code associated with the message
     * @param startTimeMillis the milliseconds since epoch that this packet was created. Used for tracking how long a
     *                        packet has been in process for
     * @param deviceId The Id of the device that this message will be sent by.
     * @throws IllegalArgumentException if startTimeMillis is 0 or negative
     */
    public IotHubTransportPacket(Message message,
                                 IotHubEventCallback eventCallback,
                                 Object callbackContext,
                                 IotHubStatusCode status,
                                 long startTimeMillis,
                                 String deviceId) throws IllegalArgumentException
    {
        if (startTimeMillis < 1)
        {
            throw new IllegalArgumentException("start time cannot be 0 or negative");
        }

        if (message == null)
        {
            throw new IllegalArgumentException("Message cannot be null");
        }

        this.message = message;
        this.eventCallback = eventCallback;
        this.callbackContext = callbackContext;
        this.status = status;
        this.startTimeMillis = startTimeMillis;
        this.deviceId = deviceId;
    }

    /**
     * Getter for the message to be sent.
     *
     * @return the message to be sent.
     */
    public Message getMessage()
    {
        return this.message;
    }

    /**
     * Getter for the callback to be invoked when a response is received.
     *
     * @return the eventCallback function. It can be {@code null}.
     */
    public IotHubEventCallback getCallback()
    {
        return this.eventCallback;
    }

    /**
     * Getter for the context to be passed to the callback when it is invoked.
     *
     * @return the callback context.
     */
    public Object getContext()
    {
        return this.callbackContext;
    }

    /**
     * Get the status of this transport packet
     * @return the status of this packet
     */
    public IotHubStatusCode getStatus()
    {
        return this.status;
    }

    /**
     * Set the status of this transport packet
     * @param status the status to set for this packet
     */
    public void setStatus(IotHubStatusCode status)
    {
        this.status = status;
    }

    /**
     * Getter for startTimeMillis
     * @return the number of milliseconds since epoch that this packet was created
     */
    public long getStartTimeMillis()
    {
        return this.startTimeMillis;
    }

    /**
     * Getter for current retry attempt. This count should be incremented using incrementRetryAttempt
     * @return the current retry attempt
     */
    public int getCurrentRetryAttempt()
    {
        return this.currentRetryAttempt;
    }

    /**
     * Increments the saved retry attempt count by 1
     */
    public void incrementRetryAttempt()
    {
        this.currentRetryAttempt++;
    }

    /**
     * Get the Id of the device that this packet is being sent from.
     * @return The Id of the device that this packet is being sent from.
     */
    public String getDeviceId()
    {
        return deviceId;
    }
}
