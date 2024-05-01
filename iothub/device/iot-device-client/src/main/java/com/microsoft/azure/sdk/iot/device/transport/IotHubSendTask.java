// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;

/**
 * Sends batched messages and invokes callbacks on completed requests. Meant to
 * be used with an executor that continuously calls run().
 */
@Slf4j
public final class IotHubSendTask implements Runnable
{
    private static final String THREAD_NAME = "azure-iot-sdk-IotHubSendTask";
    private final IotHubTransport transport;
    private final String threadNamePrefix;
    private final String threadNameSuffix;
    private final boolean useIdentifiableThreadNames;

    // This lock is used to communicate state between this thread and the IoTHubTransport layer. This thread will
    // wait until a message or callback is queued in that layer before continuing. This means that if the transport layer
    // has no outgoing messages and no callbacks queueing, then this thread will do nothing and cost nothing. This is useful
    // as this SDK would otherwise periodically spawn new threads of this type that would do nothing. It is the IotHubTransport
    // layer's responsibility to notify this thread when a message is queued to be sent or when a callback is queued to be executed
    // so that this thread can handle it.
    private final Semaphore sendThreadSemaphore;

    public IotHubSendTask(IotHubTransport transport, boolean useIdentifiableThreadNames, String threadNamePrefix, String threadNameSuffix)
    {
        if (transport == null)
        {
            throw new IllegalArgumentException("Parameter 'transport' must not be null");
        }

        this.transport = transport;
        this.sendThreadSemaphore = this.transport.getSendThreadSemaphore();
        this.useIdentifiableThreadNames = useIdentifiableThreadNames;
        this.threadNamePrefix = threadNamePrefix;
        this.threadNameSuffix = threadNameSuffix;
    }

    public void run()
    {
        String threadName = "";
        if (this.useIdentifiableThreadNames)
        {
            String deviceClientId = this.transport.getDeviceClientUniqueIdentifier();
            String connectionId = transport.getTransportConnectionId();
            threadName += deviceClientId + "-" + "Cxn" + connectionId + "-" + THREAD_NAME;
        }
        else
        {
            if (this.threadNamePrefix != null && !this.threadNamePrefix.isEmpty())
            {
                threadName += this.threadNamePrefix;
            }

            threadName += THREAD_NAME;

            if (this.threadNameSuffix != null && !this.threadNameSuffix.isEmpty())
            {
                threadName += this.threadNameSuffix;
            }
        }

        Thread.currentThread().setName(threadName);

        try
        {
            if (!this.transport.hasMessagesToSend() && !this.transport.hasCallbacksToExecute() && !this.transport.isClosed())
            {
                // IotHubTransport layer will make this semaphore available to acquire only once a message is ready to
                // be sent or a callback is ready to be executed. Once it is made available to acquire, this thread will
                // wake up, send the messages and invoke the callbacks. Until then, do nothing.
                //
                // Note that this thread is not expected to release the semaphore once it is done sending messages.
                // This semaphore is not acquired to safely modify shared resources, but instead is used to signal
                // when to start working. It is more akin to the basic Java wait/notify pattern, but without the
                // order of operations dependency that wait/notify has.
                this.sendThreadSemaphore.acquire();
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