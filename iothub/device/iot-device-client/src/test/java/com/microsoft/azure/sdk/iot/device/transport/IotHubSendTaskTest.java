// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Semaphore;

/** Unit tests for IotHubSendTask. */
public class IotHubSendTaskTest
{
    @Mocked
    IotHubTransport mockTransport;

    // Tests_SRS_IOTHUBSENDTASK_11_001: [The constructor shall save the transport.]
    // Tests_SRS_IOTHUBSENDTASK_11_002: [The function shall send all messages on the transport queue.]
    @Test
    public void runSendsAllMessages()
    {
        final Semaphore sendThreadSemaphore = new Semaphore(1);
        new Expectations()
        {
            {
                mockTransport.getSendThreadSemaphore();
                result = sendThreadSemaphore;

                mockTransport.hasMessagesToSend();
                result = true;
            }
        };

        IotHubSendTask sendTask = new IotHubSendTask(mockTransport);
        sendTask.run();

        new Verifications()
        {
            {
                mockTransport.sendMessages();
            }
        };
    }

    @Test
    public void runInvokesAllCallbacks()
    {
        final Semaphore sendThreadSemaphore = new Semaphore(1);
        new Expectations()
        {
            {
                mockTransport.getSendThreadSemaphore();
                result = sendThreadSemaphore;

                mockTransport.hasMessagesToSend();
                result = false;

                mockTransport.hasCallbacksToExecute();
                result = true;
            }
        };

        IotHubSendTask sendTask = new IotHubSendTask(mockTransport);
        sendTask.run();

        new Verifications()
        {
            {
                mockTransport.sendMessages();
            }
        };
    }

    // Tests_SRS_IOTHUBSENDTASK_11_005: [The function shall not crash because of an IOException thrown by the transport.]
    @Test
    public void runDoesNotCrashFromIoException()
    {
        new NonStrictExpectations()
        {
            {
                mockTransport.sendMessages();
                result = new IOException();
            }
        };

        IotHubSendTask sendTask = new IotHubSendTask(mockTransport);
        sendTask.run();
    }

    // Tests_SRS_IOTHUBSENDTASK_11_008: [The function shall not crash because of any error or exception thrown by the transport.]
    @Test
    public void runDoesNotCrashFromThrowable()
    {
        new NonStrictExpectations()
        {
            {
                mockTransport.sendMessages();
                result = new Throwable("Test that send does not crash.");
            }
        };

        IotHubSendTask sendTask = new IotHubSendTask(mockTransport);
        sendTask.run();
    }
}
