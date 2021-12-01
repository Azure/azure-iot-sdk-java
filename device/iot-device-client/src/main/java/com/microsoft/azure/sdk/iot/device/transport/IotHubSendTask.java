// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import lombok.extern.slf4j.Slf4j;

/**
 * Sends batched messages and invokes callbacks on completed requests. Meant to
 * be used with an executor that continuously calls run().
 */
@Slf4j
public final class IotHubSendTask implements Runnable
{
    private static final String THREAD_NAME = "azure-iot-sdk-IotHubSendTask";
    private final IotHubTransport transport;

    // This lock is used to communicate state between this thread and the IoTHubTransport layer. This thread will
    // wait until a message or callback is queued in that layer before continuing. This means that if the transport layer
    // has no outgoing messages and no callbacks queueing, then this thread will do nothing and cost nothing. This is useful
    // as this SDK would otherwise periodically spawn new threads of this type that would do nothing. It is the IotHubTransport
    // layer's responsibility to notify this thread when a message is queued to be sent or when a callback is queued to be executed
    // so that this thread can handle it.
    private final Object sendThreadLock;

    public IotHubSendTask(IotHubTransport transport)
    {
        if (transport == null)
        {
            throw new IllegalArgumentException("Parameter 'transport' must not be null");
        }

        this.transport = transport;
        this.sendThreadLock = this.transport.getSendThreadLock();
    }

    public void run()
    {
        String threadName = this.transport.getDeviceClientUniqueIdentifier() + "-" + "Cxn" + transport.getTransportConnectionId() + "-" + THREAD_NAME;
        Thread.currentThread().setName(threadName);

        try
        {
            synchronized (this.sendThreadLock)
            {
                if (!this.transport.hasMessagesToSend() && !this.transport.hasCallbacksToExecute() && !this.transport.isClosed())
                {
                    // IotHubTransport layer will notify this thread once a message is ready to be sent or a callback is ready
                    // to be executed. Until then, do nothing.
                    this.sendThreadLock.wait();
                }
            }

            this.transport.sendMessages();
            this.transport.invokeCallbacks();
        }
        catch (InterruptedException e)
        {
            // Typically happens if a disconnection event occurs and the DeviceIO layer cancels the send/receive threads
            // while the reconnection takes place.
            log.trace("Interrupted while waiting for work. Thread is now ending.");
        }
        catch (Throwable e)
        {
            log.warn("Send task encountered exception while sending messages", e);
        }
    }
}