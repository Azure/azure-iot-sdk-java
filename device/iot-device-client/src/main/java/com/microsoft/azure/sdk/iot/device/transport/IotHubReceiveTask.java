// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import lombok.extern.slf4j.Slf4j;

/**
 * Polls an IoT Hub for messages and invokes a callback if one is found.
 * Meant to be used with an executor that continuously calls run().
 */
@Slf4j
public final class IotHubReceiveTask implements Runnable
{
    private static final String THREAD_NAME = "azure-iot-sdk-IotHubReceiveTask";
    private final IotHubTransport transport;

    public IotHubReceiveTask(IotHubTransport transport)
    {
        if (transport == null)
        {
            throw new IllegalArgumentException("Parameter 'transport' must not be null");
        }

        this.transport = transport;
    }

    public void run()
    {
        Thread.currentThread().setName(THREAD_NAME);

        try
        {
            this.transport.pollForReceivedMessage();
        }
        catch (Throwable e)
        {
            log.warn("Receive task thread encountered exception while processing received messages", e);
        }
    }
}