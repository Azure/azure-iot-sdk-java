// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportPacket;

/**
 * This interface has a number of call backs to notify when a message is in a specific part of the {@code IotHubTransport} lifecycle.
 *
 * <p>There is no default implementation of this interface and will be up to the developer to implement this and pass to the methods that take it. Or add it to the message class you are sending.</p>
 */
public interface CorrelatingMessageCallback
{
    /**
     * Executed when the message is queued to the transport.
     *
     * @param message The message queued to the trasnport.
     * @param packet The resulting transport packet which give access to the event callback.
     * @param callbackContext The context sent with the message.
     */
    void onQueueRequest(Message message, IotHubTransportPacket packet, Object callbackContext);

    /**
     * Executed when the message request is queued by the transport.
     *
     * @param message The message queued to the trasnport.
     * @param packet The resulting transport packet which give access to the event callback.
     * @param callbackContext The context sent with the message.
     */
    void onSendRequest(Message message, IotHubTransportPacket packet, Object callbackContext);

    /**
     * Executed when the message request has been sent and IoT hub has acknowledged the send.
     *
     * @param packet The message queued to the trasnport.
     * @param callbackContext The context sent with the message.
     */
    void onAcknowledgeSendRequestPacket(IotHubTransportPacket packet, Object callbackContext, Throwable e);

    /**
     * Executed when a response to the message has been sent by IoT hub and is being acknowledged by the transport.
     *
     * @param message The message queued to the transport.
     * @param callbackContext The context sent with the message.
     */
    void onAcknowledgeResponse(Message message, Object callbackContext, Throwable e);

    /**
     * Executed when a response to the message has been sent by IoT hub and is being receieved by the transport.
     *
     * @param message The message queued to the trasnport.
     * @param callbackContext The context sent with the message.
     */
    void onReceiveResponse(Message message, Object callbackContext, Throwable e);

    /**
     * Executed when the message has been sent and is being acknowledged by the transport; however it is unknown to the transport.
     *
     * @param message The message queued to the trasnport.
     * @param callbackContext The context sent with the message.
     */
    void onAcknowledgeUnkownMessage(Message message, Object callbackContext, Throwable e);
}
