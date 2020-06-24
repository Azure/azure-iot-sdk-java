/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Interface of what all a Transport Connection object must do. Serves to decouple the Message Queueing of the
 * Transport layer from the protocol specific details of the Connection layer.
 */
public interface IotHubTransportConnection
{
    /**
     * Opens the transport connection object
     * @param deviceClientConfigs The list of configs to use. If more than 1 configs are in this list, multiplexing
     *                            will be used
     * @param scheduledExecutorService the executor service to use when spawning threads
     * @throws TransportException If any exceptions are encountered while opening the connection
     */
    void open(Queue<DeviceClientConfig> deviceClientConfigs, ScheduledExecutorService scheduledExecutorService) throws TransportException;

    /**
     * Sets a listener into the Transport Connection object. This listener updates the Transport layer of connection status
     * updates, message arrivals, and message acknowledgements
     * @param listener the listener for connection status updates, message arrivals, and message acknowledgements
     * @throws TransportException If the provided listener is null
     */
    void setListener(IotHubListener listener) throws TransportException;

    /**
     * Closes the transport connection.
     * @throws TransportException If any exceptions are encountered while closing.
     */
    void close() throws TransportException;

    /**
     * Send a single message to the IotHub over the Transport Connection
     * @param message the message to send
     * @return the status code from the service
     * @throws TransportException if any exception is encountered while sending the message
     */
    IotHubStatusCode sendMessage(Message message) throws TransportException;

    /**
     * Send an acknowledgement to the IotHub for a message that the Transport layer received
     * @param message the message to acknowledge
     * @param result the acknowledgement value to notify the service of
     * @return true if the acknowledgement was sent successfully, and false otherwise
     * @throws TransportException if an exception occurred while sending the acknowledgement
     */
    boolean sendMessageResult(IotHubTransportMessage message, IotHubMessageResult result) throws TransportException;

    /**
     * Gives the UUID associated with this connection instance. This string is used in conjunction with the callbacks
     * with connection status updates to ensure that all connection status updates are relevant to this connection object
     *
     * @return the UUID associated with this connection instance
     */
    String getConnectionId();
}
