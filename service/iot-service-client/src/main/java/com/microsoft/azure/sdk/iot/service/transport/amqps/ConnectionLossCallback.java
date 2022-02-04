// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

/**
 * Callback that notifies the user when the connection is lost unexpectedly for a connection established by
 * {@link com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotificationReceiver} and
 * {@link com.microsoft.azure.sdk.iot.service.messaging.FeedbackReceiver}
 */
public interface ConnectionLossCallback
{
    /**
     * This callback is executed if the connection was lost and provides the cause of why it was lost if any cause
     * could be diagnosed.
     * @param e the cause of the connection loss.
     */
    void onConnectionLost(Exception e);
}
