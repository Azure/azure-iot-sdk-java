// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

/**
 * Interface that defines the callbacks used by AMQP links to notify their respective AMQP sessions about state changes
 */
public interface LinkStateCallback
{
    /**
     * Callback that executes when the sender link has opened successfully
     */
    void onSenderLinkRemoteOpen();

    /**
     * Callback that executes when the receiver link has opened successfully
     */
    void onReceiverLinkRemoteOpen();
}
