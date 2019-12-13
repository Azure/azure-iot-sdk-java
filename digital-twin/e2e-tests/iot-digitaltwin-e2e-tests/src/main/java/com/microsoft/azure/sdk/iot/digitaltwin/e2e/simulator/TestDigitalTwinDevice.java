// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT_WS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;

@Slf4j
@Getter
public class TestDigitalTwinDevice {
    private static final String IOT_HUB_CONNECTION_STRING = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);

    private String deviceId;
    private DeviceClient deviceClient;
    private DigitalTwinDeviceClient digitalTwinDeviceClient;

    public TestDigitalTwinDevice(@NonNull String deviceId, @NonNull IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        if (!protocol.equals(MQTT) && !protocol.equals(MQTT_WS)) {
            throw new IllegalArgumentException("Supported protocols for DigitalTwin are MQTT, MQTT_WS");
        }
        this.deviceId = deviceId;
        this.deviceClient = createDeviceClient(protocol);
        this.deviceClient.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallback() {

            @Override
            public void execute(IotHubConnectionStatus iotHubConnectionStatus, IotHubConnectionStatusChangeReason iotHubConnectionStatusChangeReason, Throwable throwable, Object o) {
                log.debug("status={}, reason={}", iotHubConnectionStatus, iotHubConnectionStatusChangeReason);
            }
        }, this.deviceClient);
        this.digitalTwinDeviceClient = createDigitalTwinDeviceClient();
        log.debug("Created device: {}", deviceId);
    }

    private DeviceClient createDeviceClient(IotHubClientProtocol protocol) throws IOException, IotHubException, URISyntaxException {
        RegistryManager registryManager = RegistryManager.createFromConnectionString(IOT_HUB_CONNECTION_STRING);

        Device device = Device.createDevice(deviceId, SAS);
        Device registeredDevice = registryManager.addDevice(device);
        String deviceConnectionString = registryManager.getDeviceConnectionString(registeredDevice);
        registryManager.close();

        return new DeviceClient(deviceConnectionString, protocol);
    }

    private DigitalTwinDeviceClient createDigitalTwinDeviceClient() {
        return new DigitalTwinDeviceClient(deviceClient);
    }

    public void closeAndDeleteDevice(){
        try {
            deviceClient.closeNow();

            RegistryManager registryManager = RegistryManager.createFromConnectionString(IOT_HUB_CONNECTION_STRING);
            registryManager.removeDevice(deviceId);
            registryManager.close();
        } catch (Exception ex) {
            log.error("An exception occurred while closing/ deleting the device {}: {}", deviceId, ex);
        }
    }
}
