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
    private Message message;
    private IotHubEventCallback eventCallback;
    private Object callbackContext;
    private IotHubStatusCode status;
    private final long startTimeMillis;
    private int currentRetryAttempt;

    /**
     * Constructor.
     *
     * @param message the message to be sent.
     * @param eventCallback the callback to be invoked when a response from the IoT Hub is received.
     * @param callbackContext the context to be passed to the callback.
     * @param status the status code associated with the message
     * @param startTimeMillis the milliseconds since epoch that this packet was created. Used for tracking how long a
     *                        packet has been in process for
     * @throws IllegalArgumentException if startTimeMillis is 0 or negative
     */
    public IotHubTransportPacket(Message message,
                                 IotHubEventCallback eventCallback,
                                 Object callbackContext,
                                 IotHubStatusCode status,
                                 long startTimeMillis) throws IllegalArgumentException
    {
        if (startTimeMillis < 1)
        {
            // Codes_SRS_IOTHUBTRANSPORTPACKET_34_010: [If startTimeMillis is 0 or negative, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("start time cannot be 0 or negative");
        }

        if (message == null)
        {
            // Codes_SRS_IOTHUBTRANSPORTPACKET_34_011: [If message is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Message cannot be null");
        }

        // Codes_SRS_IOTHUBTRANSPORTPACKET_11_001: [The constructor shall save the message, callback, status, startTimeMillis, and callback context.]
        this.message = message;
        this.eventCallback = eventCallback;
        this.callbackContext = callbackContext;
        this.status = status;
        this.startTimeMillis = startTimeMillis;
    }

    /**
     * Getter for the message to be sent.
     *
     * @return the message to be sent.
     */
    public Message getMessage()
    {
        // Codes_SRS_IOTHUBTRANSPORTPACKET_11_002: [The function shall return the message given in the constructor.]
        return this.message;
    }

    /**
     * Getter for the callback to be invoked when a response is received.
     *
     * @return the eventCallback function. It can be {@code null}.
     */
    public IotHubEventCallback getCallback()
    {
        // Codes_SRS_IOTHUBTRANSPORTPACKET_11_003: [The function shall return the event callback given in the constructor.]
        return this.eventCallback;
    }

    /**
     * Getter for the context to be passed to the callback when it is invoked.
     *
     * @return the callback context.
     */
    public Object getContext()
    {
        // Codes_SRS_IOTHUBTRANSPORTPACKET_11_004: [The function shall return the callback context given in the constructor.]
        return this.callbackContext;
    }

    /**
     * Get the status of this transport packet
     * @return the status of this packet
     */
    public IotHubStatusCode getStatus()
    {
        // Codes_SRS_IOTHUBTRANSPORTPACKET_34_005: [This function shall return the saved status.]
        return this.status;
    }

    /**
     * Set the status of this transport packet
     * @param status the status to set for this packet
     */
    public void setStatus(IotHubStatusCode status)
    {
        // Codes_SRS_IOTHUBTRANSPORTPACKET_34_006: [This function shall save the provided status.]
        this.status = status;
    }

    /**
     * Getter for startTimeMillis
     * @return the number of milliseconds since epoch that this packet was created
     */
    public long getStartTimeMillis()
    {
        // Codes_SRS_IOTHUBTRANSPORTPACKET_34_007: [This function shall return the saved startTimeMillis.]
        return this.startTimeMillis;
    }

    /**
     * Getter for current retry attempt. This count should be incremented using incrementRetryAttempt
     * @return the current retry attempt
     */
    public int getCurrentRetryAttempt()
    {
        // Codes_SRS_IOTHUBTRANSPORTPACKET_34_008: [This function shall return the saved current retry attempt.]
        return this.currentRetryAttempt;
    }

    /**
     * Increments the saved retry attempt count by 1
     */
    public void incrementRetryAttempt()
    {
        // Codes_SRS_IOTHUBTRANSPORTPACKET_34_009: [This function shall increment the saved retry attempt count by 1.]
        this.currentRetryAttempt++;
    }
}
