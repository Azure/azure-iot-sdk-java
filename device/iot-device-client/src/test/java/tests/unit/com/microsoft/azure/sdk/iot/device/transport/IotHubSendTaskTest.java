// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.exceptions.DeviceClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubSendTask;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransport;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.IOException;

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
        final Object sendThreadLock = new Object();
        new Expectations()
        {
            {
                mockTransport.getSendThreadLock();
                result = sendThreadLock;

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
        final Object sendThreadLock = new Object();
        new Expectations()
        {
            {
                mockTransport.getSendThreadLock();
                result = sendThreadLock;

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
    public void runDoesNotCrashFromIoException() throws DeviceClientException
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
    public void runDoesNotCrashFromThrowable() throws DeviceClientException
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
