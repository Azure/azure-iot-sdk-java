// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;

/**
 * This interface has a number of call backs to notify when a message is in a specific part of the {@link com.microsoft.azure.sdk.iot.device.transport.IotHubTransport} lifecycle.
 * <p>
 *     There is no default implementation of this interface and will be up to the developer to implement.
 * </p>
 */
public interface CorrelatingMessageCallback
{
    /**
     * Called when the message has been queued to the transport.
     *
     * @param message The request message queued by the transport.
     * @param callbackContext The context sent with the message.
     */
    void onRequestQueued(Message message, Object callbackContext);

    /**
     * Called when the message request has been sent by the transport.
     *
     * @param message The request message sent by the transport.
     * @param callbackContext The context sent with the message.
     */
    void onRequestSent(Message message, Object callbackContext);

    /**
     * Called when the message request has been sent and IoT hub has acknowledged the request.
     *
     * @param message The request message acknowledged by the service.
     * @param callbackContext The context sent with the message.
     * @param e The exception given by the transport. If there are no errors this will be {@code null}.
     */
    void onRequestAcknowledged(Message message, Object callbackContext, IotHubClientException e);

    /**
     * Called when a response to the sent message has been sent by IoT hub and has been receieved by the transport.
     *
     * @param message The response message received by the transport.
     * @param callbackContext The context sent with the message.
     * @param e The exception given by the transport. If there are no errors this will be {@code null}.
     */
    void onResponseReceived(Message message, Object callbackContext, IotHubClientException e);

    /**
     * Called when a response to the message has been sent by IoT hub and has been acknowledged by the transport.
     *
     * @param message The response message queued to the transport.
     * @param callbackContext The context sent with the message.
     */
    void onResponseAcknowledged(Message message, Object callbackContext);
}
