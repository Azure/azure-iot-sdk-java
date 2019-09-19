// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools;
import com.microsoft.azure.sdk.iot.digitaltwin.sample.EnvironmentalSensor;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwin;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.rest.RestException;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class DigitalTwinServiceClientE2ETests {

    private static final String IOTHUB_CONNECTION_STRING = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.IOTHUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static final String MODEL_URN = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.MODEL_URN_ENV_VAR_NAME);

    private static final String DEVICE_ID_PREFIX = "DigitalTwinServiceClientE2ETests_";

    private static final String INVALID_MODEL_URN = "urn:invalid_namespace:invalid_name:1"; // Model ID format should contain a min of 4 segments [urn:namespace:name:version]
    private static final String INVALID_DEVICE_ID = "test_inactive_device_id";
    private static final String INVALID_INTERFACE_INSTANCE_NAME = "invalid_interface_instance_name";
    private static final String INVALID_COMMAND_NAME = "invalid_command_name";
    private static final String INTERFACE_INSTANCE_NOT_FOUND_MESSAGE_PATTERN = "Interface instance [%s] not found.";
    private static final String COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN = "Command[%s] is not handled for interface[%s].";

    // TODO: Define own interfaces - using interfaces from sample now
    private static final String ENVIRONMENTAL_SENSOR_INTERFACE_INSTANCE_NAME = "sensor";
    private static final String COMMAND_BLINK = "blink";
    private static final String COMMAND_BLINK_DURATION = "10";

    private static String digitalTwinId;
    private static RegistryManager registryManager;
    private static DeviceClient deviceClient;

    private static DigitalTwinServiceClient digitalTwinServiceClient;

    @BeforeClass
    public static void setup() throws IOException, IotHubException, URISyntaxException, InterruptedException {
        digitalTwinServiceClient = DigitalTwinServiceClientImpl.buildFromConnectionString()
                                                               .connectionString(IOTHUB_CONNECTION_STRING)
                                                               .build();

        // Register a new device, create a DeviceClient instance and use it to initialize the DigitalTwinDeviceClient
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());

        registryManager = RegistryManager.createFromConnectionString(IOTHUB_CONNECTION_STRING);
        Device device = Device.createDevice(digitalTwinId, AuthenticationType.SAS);
        Device registeredDevice = registryManager.addDevice(device);

        deviceClient = new DeviceClient(registryManager.getDeviceConnectionString(registeredDevice), IotHubClientProtocol.AMQPS);
        DigitalTwinDeviceClient digitalTwinDeviceClient = new DigitalTwinDeviceClient(deviceClient);

        // TODO: Register an existing interface - define own interfaces - using interfaces from sample now
        final CountDownLatch lock = new CountDownLatch(1);
        final EnvironmentalSensor environmentalSensor = new EnvironmentalSensor(ENVIRONMENTAL_SENSOR_INTERFACE_INSTANCE_NAME);
        digitalTwinDeviceClient.registerInterfacesAsync(
                MODEL_URN,
                singletonList(environmentalSensor),
                (digitalTwinClientResult, context) -> {
                    log.debug("Register interfaces {}.", digitalTwinClientResult);
                    lock.countDown();
                },
                digitalTwinDeviceClient);
        lock.await();
    }

    @Test
    public void testGetModelInformationValidModelUrn() {
        String modelString = digitalTwinServiceClient.getModel(MODEL_URN);

        assertThat(modelString).as("Verify model").isNotNull();
        assertThat(modelString).contains(MODEL_URN);
    }

    @Test (expected = NoSuchElementException.class)
    public void testGetModelInformationInvalidModelUrn() {
        String modelString = digitalTwinServiceClient.getModel(INVALID_MODEL_URN);
    }

    @Test
    public void testGetAllDigitalTwinInterfacesValidDigitalTwinId() {
        DigitalTwin digitalTwin = digitalTwinServiceClient.getDigitalTwin(digitalTwinId);

        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        assertThat(digitalTwin.getVersion()).isPositive();
        assertThat(digitalTwin.getInterfaceInstances().size()).isGreaterThanOrEqualTo(1);
    }

    @Test (expected = NoSuchElementException.class)
    public void testGetAllDigitalTwinInterfacesInvalidDigitalTwinId() {
        DigitalTwin digitalTwin = digitalTwinServiceClient.getDigitalTwin(INVALID_DEVICE_ID);
    }

    @Test
    public void testUpdateDigitalTwinPropertiesValidUpdatePatch() throws IOException {
        String randomUuid = UUID.randomUUID().toString();
        String interfaceInstanceName = "testInterfaceInstanceName";
        String propertyName = "testPropertyName_";
        String propertyValue = "testPropertyValue_".concat(randomUuid);
        String propertyPatch = "{"
                +"  \"properties\": {"
                +"      \"" + propertyName + "\": {"
                +"          \"desired\": {"
                +"              \"value\": \"" + propertyValue + "\""
                +"              }"
                +"          }"
                +"      }"
                +"  }";
        DigitalTwin digitalTwin = digitalTwinServiceClient.updateDigitalTwinProperties(digitalTwinId, interfaceInstanceName, propertyPatch);

        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        assertThat(digitalTwin.getInterfaceInstances()).containsKey(interfaceInstanceName);
        assertThat(digitalTwin.getInterfaceInstances().get(interfaceInstanceName).properties()).containsKey(propertyName);
        assertThat(digitalTwin.getInterfaceInstances().get(interfaceInstanceName).properties().get(propertyName).desired().value()).isEqualTo(propertyValue);
    }

    @Test (expected = IOException.class)
    public void testUpdateDigitalTwinPropertiesInvalidPropertyPatch() throws IOException {
        String randomUuid = UUID.randomUUID().toString();
        String interfaceInstanceName = "testInterfaceInstanceName";
        String propertyName = "testPropertyName_";
        String propertyValue = "testPropertyValue_".concat(randomUuid);
        String propertyPatch = "{"
                +"  \"properties\": {"
                +"      \"" + propertyName + "\": {"
                +"          \"desired\": \"" + propertyValue + "\""
                +"          }"
                +"      }"
                +"  }";

        DigitalTwin digitalTwin = digitalTwinServiceClient.updateDigitalTwinProperties(digitalTwinId, interfaceInstanceName, propertyPatch);
    }

    @Test
    public void testInvokeCommandOnActiveDevice() {
        String commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, ENVIRONMENTAL_SENSOR_INTERFACE_INSTANCE_NAME, COMMAND_BLINK, COMMAND_BLINK_DURATION);
    }

    @Test
    public void testInvokeCommandInvalidInterfaceInstanceName() {
        String commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, INVALID_INTERFACE_INSTANCE_NAME, COMMAND_BLINK, COMMAND_BLINK_DURATION);

        assertThat(commandResponse).isEqualTo(String.format(INTERFACE_INSTANCE_NOT_FOUND_MESSAGE_PATTERN, INVALID_INTERFACE_INSTANCE_NAME));
    }

    @Test
    public void testInvokeCommandInvalidCommandName() {
        String commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, ENVIRONMENTAL_SENSOR_INTERFACE_INSTANCE_NAME, INVALID_COMMAND_NAME, COMMAND_BLINK_DURATION);

        assertThat(commandResponse).isEqualTo(String.format(COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN, INVALID_COMMAND_NAME, EnvironmentalSensor.ENVIRONMENTAL_SENSOR_INTERFACE_ID));
    }

    @Test (expected = RestException.class)
    public void testInvokeCommandOnInactiveDevice() {
        String commandResponse = digitalTwinServiceClient.invokeCommand(INVALID_DEVICE_ID, ENVIRONMENTAL_SENSOR_INTERFACE_INSTANCE_NAME, COMMAND_BLINK, COMMAND_BLINK_DURATION);
    }

    @AfterClass
    public static void cleanup() throws IOException, IotHubException {
        deviceClient.closeNow();

        registryManager.removeDevice(digitalTwinId);
        registryManager.close();
    }

}
