/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
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

    public DeviceTestManager(InternalClient client)
            throws IOException, URISyntaxException, InterruptedException
    {
        this.client = client;

        /* Create a emulator for the device client, and connect it to the IoTHub */
        deviceEmulator = new DeviceEmulator(client);
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

    public void clearStatistics()
    {
        this.deviceEmulator.clearStatistics();
    }

    public void subscribe(boolean enableMethod, boolean enableTwin) throws IOException, InterruptedException
    {
        if (enableMethod)
        {
            /* Enable DeviceMethod on the device client using the callbacks from the DeviceEmulator */
            deviceEmulator.subscribeToDeviceMethod();
        }

        if (enableTwin)
        {
            /* Enable DeviceTwin on the device client using the callbacks from the DeviceEmulator */
            deviceEmulator.subscribeToDeviceTwin();
        }
    }

    public void tearDown() throws IOException
    {
        if (deviceEmulator != null)
        {
            deviceEmulator.tearDown();
        }
    }

    public void restartDevice(String connectionString, IotHubClientProtocol protocol, String publicCert, String privateKey) throws InterruptedException, IOException, URISyntaxException, ModuleClientException, GeneralSecurityException
    {
        deviceEmulator.tearDown();

        if (this.client instanceof DeviceClient)
        {
            if (this.client.getConfig().getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
            {
                this.client = new DeviceClient(connectionString, protocol);
            }
            else
            {
                SSLContext sslContext = SSLContextBuilder.buildSSLContext(publicCert, privateKey);
                this.client = new DeviceClient(connectionString, protocol, sslContext);
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

        deviceEmulator.open();

        /* Enable DeviceMethod on the device client using the callbacks from the DeviceEmulator */
        deviceEmulator.subscribeToDeviceMethod();

        /* Enable DeviceTwin on the device client using the callbacks from the DeviceEmulator */
        deviceEmulator.subscribeToDeviceTwin();
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
}
