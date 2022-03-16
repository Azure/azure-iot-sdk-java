// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import lombok.extern.slf4j.Slf4j;

/**
 * Sends batched messages and invokes callbacks on completed requests. Meant to
 * be used with an executor that continuously calls run().
 */
@Slf4j
public final class IotHubReconnectTask implements Runnable
{
    private static final String THREAD_NAME = "azure-iot-sdk-IotHubReconnectTask";
    private final IotHubTransport transport;

    // This lock is used to communicate state between this thread and the IoTHubTransport layer. This thread will
    // wait until a message or callback is queued in that layer before continuing. This means that if the transport layer
    // has no outgoing messages and no callbacks queueing, then this thread will do nothing and cost nothing. This is useful
    // as this SDK would otherwise periodically spawn new threads of this type that would do nothing. It is the IotHubTransport
    // layer's responsibility to notify this thread when a message is queued to be sent or when a callback is queued to be executed
    // so that this thread can handle it.
    private final Object reconnectThreadLock;

    public IotHubReconnectTask(IotHubTransport transport)
    {
        if (transport == null)
        {
            throw new IllegalArgumentException("Parameter 'transport' must not be null");
        }

        this.transport = transport;
        this.reconnectThreadLock = this.transport.getReconnectThreadLock();
    }

    public void run()
    {
        String threadName = this.transport.getDeviceClientUniqueIdentifier() + "-" + "Cxn" + transport.getTransportConnectionId() + "-" + THREAD_NAME;
        Thread.currentThread().setName(threadName);

        try
        {
            try
            {
                synchronized (this.reconnectThreadLock)
                {
                    if (!transport.needsReconnect())
                    {
                        // IotHubTransport layer will notify this thread once a message is ready to be sent or a callback is ready
                        // to be executed. Until then, do nothing.
                        this.reconnectThreadLock.wait();
                    }
                }
            }
            catch (InterruptedException e)
            {
                // likely means the client is shutting down, so no need to wait for disconnection events anymore.
                log.trace("Interrupted while waiting for disconnection events. Thread is now ending.");
                return;
            }

            try
            {
                log.debug("Starting reconnection process");
                this.transport.reconnect();
            }
            catch (InterruptedException e)
            {
                // likely means the client is shutting down, and any reconnection attempts should be abandoned
                log.trace("Interrupted while reconnecting. Thread is now ending.");
            }
        }
        catch (Throwable e)
        {
            log.warn("Reconnect task encountered exception while reconnecting", e);
        }
    }
}