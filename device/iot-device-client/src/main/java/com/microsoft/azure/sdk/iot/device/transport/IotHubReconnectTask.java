// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;

/**
 * Thread that waits for disconnection events and then owns the reconnection execution once a disconnection event is detected.
 */
@Slf4j
public final class IotHubReconnectTask implements Runnable
{
    private static final String THREAD_NAME = "azure-iot-sdk-IotHubReconnectTask";
    private final IotHubTransport transport;

    // This lock is used to communicate state between this thread and the IoTHubTransport layer. This thread will
    // wait until a disconnection event occurs in that layer before continuing. This means that if the transport layer
    // has no connectivity problems, then this thread will do nothing and cost nothing.
    private final Semaphore reconnectThreadSemaphore;

    public IotHubReconnectTask(IotHubTransport transport)
    {
        if (transport == null)
        {
            throw new IllegalArgumentException("Parameter 'transport' must not be null");
        }

        this.transport = transport;
        this.reconnectThreadSemaphore = this.transport.getReconnectThreadSemaphore();
    }

    public void run()
    {
        String deviceClientId = this.transport.getDeviceClientUniqueIdentifier();
        String connectionId = transport.getTransportConnectionId();
        String threadName = deviceClientId + "-" + "Cxn" + connectionId + "-" + THREAD_NAME;
        Thread.currentThread().setName(threadName);

        try
        {
            try
            {
                if (!transport.needsReconnect())
                {
                    // IotHubTransport layer will make this semaphore available to acquire only once a disconnection
                    // event occurs. Once it is made available to acquire, this thread will wake up and run the reconnection
                    // logic.
                    this.reconnectThreadSemaphore.acquire();
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