// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.transport.amqp;


public interface AmqpListener
{
    void MessageReceived(AmqpMessage message);

    void ConnectionEstablished();

    void ConnectionLost();

    void MessageSent();
}
