// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportPacket;

/**
 * This interface has a number of call backs to notify when a message is in a specific part of the {@code IotHubTransport} lifecycle.
 */
public interface CorrelatingMessageCallback
{
    /**
     * Executed when the message is queued to the transport.
     * @param message The message queued to the trasnport.
     * @param packet The resulting transport packet which give access to the event callback.
     * @param callbackContext The context sent with the message.
     */
    void onQueue(Message message, IotHubTransportPacket packet, Object callbackContext);

    /**
     * Executed when the message is sent by the transport.
     * @param message The message queued to the trasnport.
     * @param packet The resulting transport packet which give access to the event callback.
     * @param callbackContext The context sent with the message.
     */
    void onSend(Message message, IotHubTransportPacket packet, Object callbackContext);

    /**
     * Executed when the message has been sent and is being acknowledged by the transport.
     * @param message The message queued to the trasnport.
     * @param callbackContext The context sent with the message.
     */
    void onAcknowledge(Message message, Object callbackContext);
}
