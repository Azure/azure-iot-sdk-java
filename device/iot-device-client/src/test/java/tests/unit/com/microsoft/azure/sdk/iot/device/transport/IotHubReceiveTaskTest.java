// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.exceptions.DeviceClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubReceiveTask;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransport;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/** Unit tests for IotHubReceiveTask. */
public class IotHubReceiveTaskTest
{
    @Mocked
    IotHubTransport mockTransport;

    // Tests_SRS_IOTHUBRECEIVETASK_11_001: [The constructor shall save the transport.]
    // Tests_SRS_IOTHUBRECEIVETASK_11_002: [The function shall poll an IoT Hub for messages, invoke the message callback if one exists, and return one of COMPLETE, ABANDON, or REJECT to the IoT Hub.]
    @Test
    public void runReceivesAllMessages() throws IOException, URISyntaxException, DeviceClientException
    {
        IotHubReceiveTask receiveTask = new IotHubReceiveTask(mockTransport);
        receiveTask.run();

        new Verifications()
        {
            {
                mockTransport.handleMessage();
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
