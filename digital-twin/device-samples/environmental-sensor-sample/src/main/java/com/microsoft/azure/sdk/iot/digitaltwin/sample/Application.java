// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.sample;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static java.util.Arrays.asList;

@Slf4j
public class Application {
    private static final String DIGITAL_TWIN_DEVICE_CONNECTION_STRING = System.getenv("DIGITAL_TWIN_DEVICE_CONNECTION_STRING");
    private static final String DCM_ID = "urn:java_sdk_sample:sample_device:1";
    private static final String ENVIRONMENTAL_SENSOR_INTERFACE_INSTANCE_NAME = "environmentalSensor";
    private static final String MODEL_DEFINITION_INTERFACE_NAME = "urn_azureiot_ModelDiscovery_ModelDefinition";

    public static void main(String[] args) throws URISyntaxException, IOException {
        if (DIGITAL_TWIN_DEVICE_CONNECTION_STRING == null || DIGITAL_TWIN_DEVICE_CONNECTION_STRING.isEmpty()) {
            log.info("Please set a value for the environment variable \"DIGITAL_TWIN_DEVICE_CONNECTION_STRING\"");
            return;
        }

        DeviceClient deviceClient = new DeviceClient(DIGITAL_TWIN_DEVICE_CONNECTION_STRING, MQTT);
        deviceClient.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallback() {
            @Override
            public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext) {
                log.info("Device client status changed to: {}, reason: {}, cause: {}", status, statusChangeReason, throwable);
            }
        }, deviceClient);
        DigitalTwinDeviceClient digitalTwinDeviceClient = new DigitalTwinDeviceClient(deviceClient);
        final EnvironmentalSensor environmentalSensor = new EnvironmentalSensor(ENVIRONMENTAL_SENSOR_INTERFACE_INSTANCE_NAME);
        final DeviceInformation deviceInformation = DeviceInformation.builder()
                                                                     .manufacturer("Microsoft")
                                                                     .model("1.0.0")
                                                                     .osName(System.getProperty("os.name"))
                                                                     .processorArchitecture(System.getProperty ("os.arch"))
                                                                     .processorManufacturer("Intel(R) Core(TM)")
                                                                     .softwareVersion("JDK" + System.getProperty ("java.version"))
                                                                     .totalMemory(16e9)
                                                                     .totalStorage(1e12)
                                                                     .build();
        final ModelDefinition modelDefinition = ModelDefinition.builder()
                .digitalTwinInterfaceInstanceName(MODEL_DEFINITION_INTERFACE_NAME)
                .build();
        DigitalTwinClientResult result = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, asList(deviceInformation, environmentalSensor, modelDefinition)).blockingGet();
        log.info("Register interfaces result: {}.", result);

        log.info("Updating state of environmental sensor to true...");
        environmentalSensor.updateStatusAsync(true).blockingGet();
        log.info("State of environmental sensor was set to true");

        log.info("Waiting for service updates...");
        log.info("Enter any key to finish");
        new Scanner(System.in).nextLine();
        System.exit(0);
    }

}
