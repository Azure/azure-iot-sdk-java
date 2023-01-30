// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;

/**
 * The callback interface that defines how this client will notify you once a particular message has been sent and acknowledged
 * by the service. Used by {@link InternalClient#sendEventAsync(Message, MessageSentCallback, Object)}
 */
public interface MessageSentCallback
{
    /**
     * The callback that is executed once the message associated with this callback has been acknowledged by the service.
     *
     * @param sentMessage the message that either was sent or failed to send.
     * @param clientException the exception that was encountered while sending the request. If null, no exception was
     * encountered and the message was successfully sent.
     * @param callbackContext a custom context given by the developer. Will be null if no custom context was provided.
     */
    void onMessageSent(Message sentMessage, IotHubClientException clientException, Object callbackContext);
}
