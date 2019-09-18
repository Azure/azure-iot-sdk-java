// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceAsyncClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceAsyncClientImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwin;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class DigitalTwinServiceAsyncClientE2ETests {

    private static final String IOTHUB_CONNECTION_STRING = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.IOTHUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static final String MODEL_URN = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.MODEL_URN_ENV_VAR_NAME);

    private static final String DEVICE_ID_PREFIX = "DigitalTwinServiceClientE2ETests_";

    private static DigitalTwinServiceAsyncClient digitalTwinServiceAsyncClient;
    private static DigitalTwinDeviceClient digitalTwinDeviceClient;
    private static String digitalTwinId;

    @BeforeClass
    public static void setup() throws IOException, IotHubException, URISyntaxException {
        digitalTwinServiceAsyncClient = DigitalTwinServiceAsyncClientImpl.buildFromConnectionString()
                                                                         .connectionString(IOTHUB_CONNECTION_STRING)
                                                                         .build();

        // Register a new device, create a DeviceClient instance and use it to initialize the DigitalTwinDeviceClient
        /*digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());

        RegistryManager registryManager = RegistryManager.createFromConnectionString(IOTHUB_CONNECTION_STRING);
        Device device = Device.createDevice(digitalTwinId, AuthenticationType.SAS);
        Device registeredDevice = registryManager.addDevice(device);

        DeviceClient deviceClient = new DeviceClient(registryManager.getDeviceConnectionString(registeredDevice), IotHubClientProtocol.AMQPS);
        digitalTwinDeviceClient = new DigitalTwinDeviceClient(deviceClient);*/
    }

    @Test
    public void testGetModelInformationAsync() {
        String modelString = digitalTwinServiceAsyncClient.getModel(MODEL_URN).toBlocking().single();

        assertThat(modelString).as("Verify model").isNotNull();
        assertThat(modelString).contains(MODEL_URN);
    }

    @Test
    public void testGetAllDigitalTwinInterfacesAsync() {
        DigitalTwin digitalTwin = digitalTwinServiceAsyncClient.getDigitalTwin(digitalTwinId).toBlocking().single();

        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        assertThat(digitalTwin.getVersion()).isPositive();
        assertThat(digitalTwin.getInterfaceInstances().size()).isGreaterThanOrEqualTo(1);
    }

}
