// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;

/**
 * Polls an IoT Hub for messages and invokes a callback if one is found.
 * Meant to be used with an executor that continuously calls run().
 */
@Slf4j
public final class IotHubReceiveTask implements Runnable
{
    private static final String THREAD_NAME = "azure-iot-sdk-IotHubReceiveTask";
    private final IotHubTransport transport;

    // This lock is used to communicate state between this thread and the IoTHubTransport layer. This thread will
    // wait until a message has been received in that layer before continuing. This means that if the transport layer
    // has no received messages to handle, then this thread will do nothing and cost nothing. This is useful
    // as this SDK would otherwise periodically spawn new threads of this type that would do nothing. It is the IotHubTransport
    // layer's responsibility to notify this thread when a message has been received so that this thread can handle it.
    private final Semaphore receiveThreadSemaphore;

    public IotHubReceiveTask(IotHubTransport transport)
    {
        if (transport == null)
        {
            throw new IllegalArgumentException("Parameter 'transport' must not be null");
        }

        this.transport = transport;
        this.receiveThreadSemaphore = this.transport.getReceiveThreadSemaphore();
    }

    public void run()
    {
        String threadName = this.transport.getDeviceClientUniqueIdentifier() + "-" + "Cxn" + transport.getTransportConnectionId() + "-" + THREAD_NAME;
        Thread.currentThread().setName(threadName);

        try
        {
            // HTTP is the only protocol where the SDK must actively poll for received messages. Because of that, never
            // wait on the IoTHubTransport layer to notify this thread that a received message is ready to be handled.
            if (this.transport.getProtocol() != IotHubClientProtocol.HTTPS)
            {
                if (!this.transport.hasReceivedMessagesToHandle() && !this.transport.isClosed())
                {
                    // AMQP and MQTT layers will notify the IoTHubTransport layer once a message arrives, and at
                    // that time, this thread will be notified to handle them.
                    this.receiveThreadSemaphore.acquire();
                }
            }

            this.transport.handleMessage();

        }
        catch (InterruptedException e)
        {
            // Typically happens if a disconnection event occurs and the DeviceIO layer cancels the send/receive threads
            // while the reconnection takes place.
            log.trace("Interrupted while waiting for work. Thread is now ending.");
        }
        catch (Throwable e)
        {
            log.warn("Receive task thread encountered exception while processing received messages", e);
        }
    }
}