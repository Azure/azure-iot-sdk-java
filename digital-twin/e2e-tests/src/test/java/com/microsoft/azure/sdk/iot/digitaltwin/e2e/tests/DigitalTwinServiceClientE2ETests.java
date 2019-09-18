// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwin;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DigitalTwinServiceClientE2ETests {

    private static final String IOTHUB_CONNECTION_STRING = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.IOTHUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static final String MODEL_URN = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.MODEL_URN_ENV_VAR_NAME);

    private static final String DEVICE_ID_PREFIX = "DigitalTwinServiceClientE2ETests_";

    private static String digitalTwinId;
    private static RegistryManager registryManager;
    private static DeviceClient deviceClient;
    private static DigitalTwinDeviceClient digitalTwinDeviceClient;

    private static DigitalTwinServiceClient digitalTwinServiceClient;

    @BeforeClass
    public static void setup() throws IOException, IotHubException, URISyntaxException {
        digitalTwinServiceClient = DigitalTwinServiceClientImpl.buildFromConnectionString()
                                                               .connectionString(IOTHUB_CONNECTION_STRING)
                                                               .build();

        // Register a new device, create a DeviceClient instance and use it to initialize the DigitalTwinDeviceClient
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());

        registryManager = RegistryManager.createFromConnectionString(IOTHUB_CONNECTION_STRING);
        Device device = Device.createDevice(digitalTwinId, AuthenticationType.SAS);
        Device registeredDevice = registryManager.addDevice(device);

        deviceClient = new DeviceClient(registryManager.getDeviceConnectionString(registeredDevice), IotHubClientProtocol.AMQPS);
        digitalTwinDeviceClient = new DigitalTwinDeviceClient(deviceClient);
    }

    @Test
    public void testGetModelInformationValidModelUrn() {
        String modelString = digitalTwinServiceClient.getModel(MODEL_URN);

        assertThat(modelString).as("Verify model").isNotNull();
        assertThat(modelString).contains(MODEL_URN);
    }

    @Test
    public void testGetModelInformationInvalidModelUrn() {
        String modelString = digitalTwinServiceClient.getModel("invalidModelUrn");

        assertThat(modelString).as("Verify invalid model URN returns null").isNull();
    }

    @Test
    public void testGetAllDigitalTwinInterfacesValidDigitalTwinId() {
        DigitalTwin digitalTwin = digitalTwinServiceClient.getDigitalTwin(digitalTwinId);

        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        assertThat(digitalTwin.getVersion()).isPositive();
        assertThat(digitalTwin.getInterfaceInstances().size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void testGetAllDigitalTwinInterfacesInvalidDigitalTwinId() {

    }

    @Test
    public void testUpdateDigitalTwinPropertiesValidUpdatePatch() {

    }

    @Test
    public void testUpdateDigitalTwinPropertiesInvalidPropertyPatch() {

    }

    @Test
    public void testUpdateDigitalTwinPropertiesInvalidInterfaceInstanceName() {

    }

    @Test
    public void testInvokeCommandOnActiveDevice() {

    }

    @Test
    public void testInvokeCommandOnInactiveDevice() {

    }

    @AfterClass
    public static void cleanup() throws IOException, IotHubException {
        deviceClient.closeNow();
        registryManager.removeDevice(digitalTwinId);
        registryManager.close();

        // digital twin service client - close() ??
        // digital twin device client - close() ??
    }

}
