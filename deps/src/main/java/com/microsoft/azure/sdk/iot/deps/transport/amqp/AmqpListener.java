// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.transport.amqp;


public interface AmqpListener
{
    void messageReceived(AmqpMessage message);

    void connectionEstablished();

    void connectionLost();

    void messageSent();
}
