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

    public IotHubSendTask(IotHubTransport transport)
    {
        if (transport == null)
        {
            throw new IllegalArgumentException("Parameter 'transport' must not be null");
        }

        // Codes_SRS_IOTHUBSENDTASK_11_001: [The constructor shall save the transport.]
        this.transport = transport;
    }

    public void run()
    {
        Thread.currentThread().setName(THREAD_NAME);

        try
        {
            // Codes_SRS_IOTHUBSENDTASK_11_002: [The function shall send all messages on the transport queue.]
            this.transport.sendMessages();

            // Codes_SRS_IOTHUBSENDTASK_11_003: [The function shall invoke all callbacks on the transport's callback queue.]
            this.transport.invokeCallbacks();
        }
        // Codes_SRS_IOTHUBSENDTASK_11_005: [The function shall not crash because of an IOException thrown by the transport.]
        // Codes_SRS_IOTHUBSENDTASK_11_008: [The function shall not crash because of any error or exception thrown by the transport.]
        catch (Throwable e)
        {
            log.warn("Send task encountered exception while sending messages", e);
        }
    }
}