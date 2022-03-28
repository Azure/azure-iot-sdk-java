// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;

import java.util.List;

public interface MessagesSentCallback
{
    /**
     * The callback that is executed once the messages associated with this callback has been acknowledged by the service.
     *
     * These messages are sent in bulk and are either all succeeded or all failed to send.
     *
     * @param sentMessages the messages that either were sent or failed to send.
     * @param clientException the exception that was encountered while sending the request. If null, no exception was
     * encountered and the messages were all successfully sent.
     * @param callbackContext a custom context given by the developer.
     */
    void onMessagesSent(List<Message> sentMessages, IotHubClientException clientException, Object callbackContext);
}
