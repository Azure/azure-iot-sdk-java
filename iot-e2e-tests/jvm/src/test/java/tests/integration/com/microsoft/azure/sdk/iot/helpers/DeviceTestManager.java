/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Manager to create and control a device on both service and device sides.
 */
public class DeviceTestManager
{
    private static final int OPEN_CONNECTION_TIMEOUT_IN_SECONDS = 10;
    private static final int STOP_DEVICE_TIMEOUT_IN_MILLISECONDS = 10000;
    private static final int SECOND_IN_MILLISECONDS = 1000;

    private static final String DEVICE_NAME_UUID = UUID.randomUUID().toString();

    /* deviceOnServiceClient is the device definition on the service `End`. */
    private Device deviceOnServiceClient;

    /* deviceEmulator is the device definition on the device `End`. */
    private DeviceEmulator deviceEmulator;
    private Thread deviceThread;

    /* Device connection string. */
    private String connectionString;
    private RegistryManager registryManager;

    /* Device protocol */
    private IotHubClientProtocol protocol;

    public DeviceTestManager(RegistryManager registryManager, String deviceName, IotHubClientProtocol protocol)
            throws NoSuchAlgorithmException, IotHubException, IOException, URISyntaxException, InterruptedException
    {
        /* Create unique device name */
        String deviceId = deviceName.concat("-" + DEVICE_NAME_UUID);

        /* Create device on the service */
        deviceOnServiceClient = Device.createFromId(deviceId, null, null);

        /* Add device to the IoTHub */
        this.registryManager = registryManager;
        deviceOnServiceClient = registryManager.addDevice(deviceOnServiceClient);

        /* Create a emulator for the device client, and connect it to the IoTHub */
        this.connectionString = registryManager.getDeviceConnectionString(deviceOnServiceClient);
        this.protocol = protocol;
        deviceEmulator = new DeviceEmulator(this.connectionString, this.protocol);

        /* Enable DeviceMethod on the device client using the callbacks from the DeviceEmulator */
        deviceEmulator.enableDeviceMethod();

        /* Enable DeviceTwin on the device client using the callbacks from the DeviceEmulator */
        deviceEmulator.enableDeviceTwin();

        deviceThread = new Thread(deviceEmulator);
        deviceThread.start();

        /* Wait until the device complete the connection with the IoTHub. */
        waitIotHub(1, OPEN_CONNECTION_TIMEOUT_IN_SECONDS);
    }

    public DeviceTestManager(RegistryManager registryManager, String deviceName, IotHubClientProtocol protocol, String publicKeyCert, String privateKey, String x509Thumbprint)
            throws NoSuchAlgorithmException, IotHubException, IOException, URISyntaxException, InterruptedException
    {
        /* Create unique device name */
        String deviceId = deviceName.concat("-" + DEVICE_NAME_UUID);

        /* Create device on the service */
        deviceOnServiceClient = Device.createDevice(deviceId, AuthenticationType.SELF_SIGNED);
        deviceOnServiceClient.setThumbprint(x509Thumbprint, x509Thumbprint);

        /* Add device to the IoTHub */
        this.registryManager = registryManager;
        deviceOnServiceClient = registryManager.addDevice(deviceOnServiceClient);

        /* Create a emulator for the device client, and connect it to the IoTHub */
        this.connectionString = registryManager.getDeviceConnectionString(deviceOnServiceClient);
        this.protocol = protocol;
        deviceEmulator = new DeviceEmulator(this.connectionString, this.protocol, publicKeyCert, privateKey);

        /* Enable DeviceMethod on the device client using the callbacks from the DeviceEmulator */
        deviceEmulator.enableDeviceMethod();

        /* Enable DeviceTwin on the device client using the callbacks from the DeviceEmulator */
        deviceEmulator.enableDeviceTwin();

        deviceThread = new Thread(deviceEmulator);
        deviceThread.start();

        /* Wait until the device complete the connection with the IoTHub. */
        waitIotHub(1, OPEN_CONNECTION_TIMEOUT_IN_SECONDS);
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

    public String getDeviceId()
    {
        return this.deviceOnServiceClient.getDeviceId();
    }

    public void stop() throws IOException, IotHubException, InterruptedException
    {
        deviceEmulator.stop();
        deviceThread.join(STOP_DEVICE_TIMEOUT_IN_MILLISECONDS);
        registryManager.removeDevice(deviceOnServiceClient.getDeviceId());
    }

    public void restartDevice() throws InterruptedException, IOException, URISyntaxException
    {
        if(deviceThread.getState() == Thread.State.RUNNABLE)
        {
            deviceEmulator.stop();
        }
        deviceThread.join(STOP_DEVICE_TIMEOUT_IN_MILLISECONDS);

        /* Create a emulator for the device client, and connect it to the IoTHub */
        deviceEmulator = new DeviceEmulator(connectionString, protocol);

        /* Enable DeviceMethod on the device client using the callbacks from the DeviceEmulator */
        deviceEmulator.enableDeviceMethod();

        /* Enable DeviceTwin on the device client using the callbacks from the DeviceEmulator */
        deviceEmulator.enableDeviceTwin();

        deviceThread = new Thread(deviceEmulator);
        deviceThread.start();

        /* Wait until the device complete the connection with the IoTHub. */
        waitIotHub(1, OPEN_CONNECTION_TIMEOUT_IN_SECONDS);
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
