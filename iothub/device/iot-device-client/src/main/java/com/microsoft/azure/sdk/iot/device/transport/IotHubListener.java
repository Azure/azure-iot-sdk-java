/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.Message;

/**
 * Callback interface for communicating connection and message status updates from individual protocol clients
 * (mqtt, https, amqps) to the transport layer that handles queueing of messages and connecting/reconnecting/disconnecting
 */
public interface IotHubListener
{
    /**
     * Callback to be fired when a message that the transport client sent has been acknowledged by Iot Hub
     * @param message The message that was acknowledged
     * @param deviceId The device that the message was sent from
     * @param e Null if the message was successfully acknowledged. Otherwise, this exception communicates if the message
     *          should be resent at all
     */
    void onMessageSent(Message message, String deviceId, TransportException e);

    /**
     * Callback to be fired when a transport message has been received.
     * @param transportMessage The message that was received. May be null if e is not null
     * @param e the exception that was encountered while receiving messages. May be null if transportMessage
     *          is not null
     */
    void onMessageReceived(IotHubTransportMessage transportMessage, TransportException e);

    /**
     * Callback to be fired when connection has been lost
     * @param e the cause of the connection loss
     * @param connectionId the id of the connection this update is relevant to
     */
    void onConnectionLost(TransportException e, String connectionId);

    /**
     * Callback to be fired when the connection has been successfully established
     * @param connectionId the id of the connection this update is relevant to
     */
    void onConnectionEstablished(String connectionId);

    /**
     * Callback to be fired when the multiplexed connection establishes a new device session.
     * @param connectionId the Id of the connection, used to identify which of possible many reconnection attempts
     *                     this event belongs to.
     * @param deviceId the Id of the device that the session belongs to
     */
    void onMultiplexedDeviceSessionEstablished(String connectionId, String deviceId);

    /**
     * Callback to be fired when the multiplexed connection loses a device session.
     * @param e The exception that caused the connection to be lost.
     * @param connectionId the Id of the connection, used to identify which of possible many reconnection attempts
     *                     this event belongs to.
     * @param deviceId the Id of the device that the session belongs to
     * @param shouldReconnect false if the disconnect was desired by the users, and true if it is the consequence of an
     * issue where the SDK should retry.
     */
    void onMultiplexedDeviceSessionLost(TransportException e, String connectionId, String deviceId, boolean shouldReconnect);

    /**
     * Callback to be fired when the multiplexed connection fails to register a device session.
     * @param connectionId the Id of the connection, used to identify which of possible many reconnection attempts
     *                     this event belongs to.
     * @param deviceId the Id of the device that the session belongs to
     * @param e the throwable that explains why the registration failed.
     */
    void onMultiplexedDeviceSessionRegistrationFailed(String connectionId, String deviceId, Exception e);

    /**
     * The current Iot Hub connection status
     *
     * @return
     */
    IotHubConnectionStatus getConnectionStatus();
}
