/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.helpers;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Manager to create and control a device on both service and device sides.
 */
public class DeviceTestManager
{
    private static final int STOP_DEVICE_TIMEOUT_IN_MILLISECONDS = 10000;
    private static final int SECOND_IN_MILLISECONDS = 1000;

    public InternalClient client;

    /* deviceEmulator is the device definition on the device `End`. */
    private DeviceEmulator deviceEmulator;
    private Thread deviceThread;

    public DeviceTestManager(InternalClient client)
            throws IOException, URISyntaxException, InterruptedException
    {
        this.client = client;

        /* Create a emulator for the device client, and connect it to the IoTHub */
        deviceEmulator = new DeviceEmulator(client);

        deviceThread = new Thread(deviceEmulator);
        deviceThread.start();
    }

    public void waitIotHub(int numberOfEvents, long timeoutInSeconds) throws InterruptedException, IOException
    {
        long countRetry = 0;
        while(getStatusOk() + getStatusError() < numberOfEvents)
        {
            if((countRetry++) >= timeoutInSeconds)
            {
                throw new IOException("Connection timeout");
            }
            Thread.sleep(SECOND_IN_MILLISECONDS);
        }
    }

    public void clearDevice()
    {
        this.deviceEmulator.clearStatistics();
    }

    public void start(boolean enableMethod, boolean enableTwin) throws IOException, InterruptedException
    {
        this.deviceEmulator.start();

        if (enableMethod)
        {
            /* Enable DeviceMethod on the device client using the callbacks from the DeviceEmulator */
            deviceEmulator.enableDeviceMethod();
        }

        if (enableTwin)
        {
            /* Enable DeviceTwin on the device client using the callbacks from the DeviceEmulator */
            deviceEmulator.enableDeviceTwin();
        }

        /* Wait until the device complete the connection with the IoTHub. */
        //waitIotHub(1, OPEN_CONNECTION_TIMEOUT_IN_SECONDS);
    }

    public void stop() throws IOException, InterruptedException
    {
        deviceEmulator.stop();
        int stopDeviceTimeoutInMilliseconds = STOP_DEVICE_TIMEOUT_IN_MILLISECONDS;
        deviceThread.join(stopDeviceTimeoutInMilliseconds);
    }

    public void restartDevice(String connectionString, IotHubClientProtocol protocol, String publicCert, String privateKey) throws InterruptedException, IOException, URISyntaxException, ModuleClientException
    {
        if(deviceThread.getState() == Thread.State.RUNNABLE)
        {
            deviceEmulator.stop();
        }
        deviceThread.join(STOP_DEVICE_TIMEOUT_IN_MILLISECONDS);

        if (this.client instanceof DeviceClient)
        {
            if (this.client.getConfig().getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
            {
                this.client = new DeviceClient(connectionString, protocol);
            }
            else
            {
                this.client = new DeviceClient(connectionString, protocol, publicCert, false, privateKey, false);
            }
        }
        else if (this.client instanceof ModuleClient)
        {
            if (this.client.getConfig().getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
            {
                this.client = new ModuleClient(connectionString, protocol);
            }
            else
            {
                this.client = new ModuleClient(connectionString, protocol, publicCert, false, privateKey, false);
            }
        }

        /* Create a emulator for the device client, and connect it to the IoTHub */
        deviceEmulator = new DeviceEmulator(this.client);

        deviceEmulator.start();

        /* Enable DeviceMethod on the device client using the callbacks from the DeviceEmulator */
        deviceEmulator.enableDeviceMethod();

        /* Enable DeviceTwin on the device client using the callbacks from the DeviceEmulator */
        deviceEmulator.enableDeviceTwin();

        deviceThread = new Thread(deviceEmulator);
        deviceThread.start();

        /* Wait until the device complete the connection with the IoTHub. */
        //waitIotHub(1, OPEN_CONNECTION_TIMEOUT_IN_SECONDS);
    }

    public int getStatusOk()
    {
        return deviceEmulator.getStatusOk();
    }

    public int getStatusError()
    {
        return deviceEmulator.getStatusError();
    }

    public ConcurrentMap<String, ConcurrentLinkedQueue<Object>> getTwinChanges()
    {
        return deviceEmulator.getTwinChanges();
    }

    public void sendMessageAndWaitForResponse(MessageAndResult messageAndResult, int RETRY_MILLISECONDS, int SEND_TIMEOUT_MILLISECONDS, IotHubClientProtocol protocol)
    {
        deviceEmulator.sendMessageAndWaitForResponse(messageAndResult, RETRY_MILLISECONDS, SEND_TIMEOUT_MILLISECONDS, protocol);
    }
}
