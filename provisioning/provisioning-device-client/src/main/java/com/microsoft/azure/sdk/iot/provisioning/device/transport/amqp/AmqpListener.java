// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.device.transport.amqp;

public interface AmqpListener
{
    void messageReceived(AmqpMessage message);

    void messageSendFailed(String exceptionMessage);
}
