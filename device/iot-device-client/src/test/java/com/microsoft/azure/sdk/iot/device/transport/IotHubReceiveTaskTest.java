// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.DeviceClientException;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Semaphore;

/** Unit tests for IotHubReceiveTask. */
public class IotHubReceiveTaskTest
{
    @Mocked
    IotHubTransport mockTransport;

    // Tests_SRS_IOTHUBRECEIVETASK_11_001: [The constructor shall save the transport.]
    // Tests_SRS_IOTHUBRECEIVETASK_11_002: [The function shall poll an IoT Hub for messages, invoke the message callback if one exists, and return one of COMPLETE, ABANDON, or REJECT to the IoT Hub.]
    @Test
    public void runReceivesAllMessages() throws DeviceClientException
    {
        final Semaphore receiveThreadSemaphore = new Semaphore(1);
        new Expectations()
        {
            {
                mockTransport.getReceiveThreadSemaphore();
                result = receiveThreadSemaphore;

                mockTransport.hasReceivedMessagesToHandle();
                result = true;

                mockTransport.getProtocol();
                result = IotHubClientProtocol.AMQPS;
            }
        };
        IotHubReceiveTask receiveTask = new IotHubReceiveTask(mockTransport);

        // act
        receiveTask.run();

        new Verifications()
        {
            {
                mockTransport.handleMessage();
            }
        };
    }

    @Test
    public void runReceivesAllMessagesHTTP()
    {
        new Expectations()
        {
            {
                mockTransport.getProtocol();
                result = IotHubClientProtocol.HTTPS;
            }
        };

        IotHubReceiveTask receiveTask = new IotHubReceiveTask(mockTransport);

        // act
        receiveTask.run();

        new Verifications()
        {
            {
                mockTransport.hasReceivedMessagesToHandle();
                times = 0;
            }
        };
    }

    // Tests_SRS_IOTHUBRECEIVETASK_11_004: [The function shall not crash because of an IOException thrown by the transport.]
    @Test
    public void runDoesNotCrashFromIoException() throws IOException, URISyntaxException, DeviceClientException
    {
        new NonStrictExpectations()
        {
            {
                mockTransport.handleMessage();
                result = new IOException();
            }
        };

        IotHubReceiveTask receiveTask = new IotHubReceiveTask(mockTransport);
        receiveTask.run();
    }

    // Tests_SRS_IOTHUBRECEIVETASK_11_005: [The function shall not crash because of any error or exception thrown by the transport.]
    @Test
    public void runDoesNotCrashFromThrowable() throws IOException, URISyntaxException, DeviceClientException
    {
        new NonStrictExpectations()
        {
            {
                mockTransport.handleMessage();
                result = new Throwable("Test if the receive task does not crash.");
            }
        };

        IotHubReceiveTask receiveTask = new IotHubReceiveTask(mockTransport);
        receiveTask.run();
    }
}
