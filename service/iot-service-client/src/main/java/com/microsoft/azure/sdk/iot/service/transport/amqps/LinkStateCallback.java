// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

public interface LinkStateCallback
{
    void onSenderLinkRemoteOpen();
    void onReceiverLinkRemoteOpen();
}
