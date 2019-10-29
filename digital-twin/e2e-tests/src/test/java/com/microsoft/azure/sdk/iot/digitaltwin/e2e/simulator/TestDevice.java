// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.Getter;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;

@Getter
public class TestDevice {
    private static final String IOTHUB_CONNECTION_STRING = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.IOTHUB_CONNECTION_STRING_ENV_VAR_NAME);

    private String deviceId;
    private DeviceClient deviceClient;
    private DigitalTwinDeviceClient digitalTwinDeviceClient;

    public TestDevice(String deviceId) throws IotHubException, IOException, URISyntaxException {
        this.deviceId = deviceId;
        this.deviceClient = createDeviceClient();
        this.digitalTwinDeviceClient = createDigitalTwinDeviceClient();
    }

    private DeviceClient createDeviceClient() throws IOException, IotHubException, URISyntaxException {
        RegistryManager registryManager = RegistryManager.createFromConnectionString(IOTHUB_CONNECTION_STRING);

        Device device = Device.createDevice(deviceId, SAS);
        Device registeredDevice = registryManager.addDevice(device);
        String deviceConnectionString = registryManager.getDeviceConnectionString(registeredDevice);
        registryManager.close();

        return new DeviceClient(deviceConnectionString, MQTT);
    }

    private DigitalTwinDeviceClient createDigitalTwinDeviceClient() {
        return new DigitalTwinDeviceClient(deviceClient);
    }

    public void closeAndDeleteDevice() throws IOException, IotHubException {
        deviceClient.closeNow();

        RegistryManager registryManager = RegistryManager.createFromConnectionString(IOTHUB_CONNECTION_STRING);
        registryManager.removeDevice(deviceId);
        registryManager.close();
    }

}
