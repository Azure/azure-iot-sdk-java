// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

/**
 * An interface for an IoT Hub message callback.
 * <p>
 * The developer is expected to create an implementation of this interface,
 * and the transport will call {@link MessageCallback#onCloudToDeviceMessageReceived(Message, Object)}
 * upon receiving a message from an IoT Hub.
 */
public interface MessageCallback
{
    /**
     * Executes the callback. The callback should return a response that instructs an IoT Hub to
     * {@link IotHubMessageResult#COMPLETE}, {@link IotHubMessageResult#ABANDON}, or {@link IotHubMessageResult#REJECT}
     * the message.
     *
     * <p>
     *     If this callback throws an exception it will not complete the message and can cause the messages to build
     *     up on the IoT hub until they expire. This can prevent further message delivery until all messages are
     *     expired or completed from IoT hub.
     * </p>
     *
     * @param message the message.
     * @param callbackContext a custom context given by the developer.
     *
     * @return whether the IoT Hub should {@link IotHubMessageResult#COMPLETE}, {@link IotHubMessageResult#ABANDON},
     * or {@link IotHubMessageResult#REJECT} the message.
     */
    IotHubMessageResult onCloudToDeviceMessageReceived(Message message, Object callbackContext);
}
