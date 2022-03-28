// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;

/**
 * An interface for an IoT Hub event callback.
 *
 * Developers are expected to create an implementation of this interface,
 * and the transport will call {@link MessageSentCallback#onMessageSent(Message, IotHubClientException, Object)}
 * upon receiving a response from an IoT Hub.
 */
public interface MessageSentCallback
{
    /**
     * The callback that is executed once the message associated with this callback has been acknowledged by the service.
     *
     * @param sentMessage the message that either was sent or failed to send.
     * @param clientException the exception that was encountered while sending the request. If null, no exception was
     * encountered and the message was successfully sent.
     * @param callbackContext a custom context given by the developer.
     */
    void onMessageSent(Message sentMessage, IotHubClientException clientException, Object callbackContext);
}
