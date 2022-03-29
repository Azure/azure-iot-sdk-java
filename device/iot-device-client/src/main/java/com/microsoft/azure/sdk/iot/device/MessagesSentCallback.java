// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;

import java.util.List;

/**
 * The callback interface that defines how this client will notify you once a batch of messages has been sent and acknowledged
 * by the service. Used by {@link InternalClient#sendEventsAsync(List, MessagesSentCallback, Object)}
 */
public interface MessagesSentCallback
{
    /**
     * The callback that is executed once the messages associated with this callback has been acknowledged by the service.
     *
     * These messages are sent in bulk and have either all succeeded or all failed to send.
     *
     * @param sentMessages the messages that either were sent or failed to send.
     * @param clientException the exception that was encountered while sending the request. If null, no exception was
     * encountered and the messages were all successfully sent.
     * @param callbackContext a custom context given by the developer. Will be null if no custom context was provided.
     */
    void onMessagesSent(List<Message> sentMessages, IotHubClientException clientException, Object callbackContext);
}
