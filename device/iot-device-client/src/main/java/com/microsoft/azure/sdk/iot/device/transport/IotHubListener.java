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
 * (mqtt, https, amqps) to the Tranpsort layer that handles queueing of messages and connecting/reconnecting/disconnecting
 */
public interface IotHubListener
{
    /**
     * Callback to be fired when a message that the transport client sent has been acknowledged by Iot Hub
     * @param message The message that was acknowledged
     * @param e Null if the message was successfully acknowledged. Otherwise, this exception communicates if the message
     *          should be resent at all
     */
    void onMessageSent(Message message, Throwable e);

    /**
     * Callback to be fired when a transport message has been received.
     * @param transportMessage The message that was received. May be null if e is not null
     * @param e the exception that was encountered while receiving messages. May be null if transportMessage
     *          is not null
     */
    void onMessageReceived(IotHubTransportMessage transportMessage, Throwable e);

    /**
     * Callback to be fired when connection has been lost
     * @param e the cause of the connection loss
     * @param connectionId the id of the connection this update is relevant to
     */
    void onConnectionLost(Throwable e, String connectionId);

    /**
     * Callback to be fired when the connection has been successfully established
     * @param connectionId the id of the connection this update is relevant to
     */
    void onConnectionEstablished(String connectionId);
}
