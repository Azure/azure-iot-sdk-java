// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.SimpleTestInterfaceInstance;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceAsyncClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceAsyncClientImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.Property;
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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class DigitalTwinServiceAsyncClientE2ETests {

    private static final String IOTHUB_CONNECTION_STRING = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.IOTHUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static final String MODEL_URN = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.MODEL_URN_ENV_VAR_NAME);

    private static final String DEVICE_ID_PREFIX = "DigitalTwinServiceAsyncClientE2ETests_";

    private static final String INVALID_MODEL_URN = "urn:invalid_namespace:invalid_name:1"; // Model ID format should contain a min of 4 segments [urn:namespace:name:version]
    private static final String INVALID_DEVICE_ID = "test_inactive_device_id";
    private static final String INVALID_INTERFACE_INSTANCE_NAME = "invalid_interface_instance_name";
    private static final String INVALID_COMMAND_NAME = "invalid_command_name";
    private static final String INTERFACE_INSTANCE_NOT_FOUND_MESSAGE_PATTERN = "Interface instance [%s] not found.";
    private static final String COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN = "Command[%s] is not handled for interface[%s].";

    private static final String SIMPLE_TEST_INTERFACE_ID = "urn:contoso:azureiot:sdk:testinterface:1";
    private static final String SIMPLE_TEST_INTERFACE_INSTANCE_NAME = "test_interface_1";
    private static final String COMMAND_SYNC_COMMAND = "syncCommand";
    private static final String COMMAND_WRITABLE_PROPERTY_UPDATE_VALUE = "updatedFromCommandInvocation";

    private static String digitalTwinId;
    private static RegistryManager registryManager;
    private static DeviceClient deviceClient;

    private static DigitalTwinServiceAsyncClient digitalTwinServiceAsyncClient;

    @BeforeClass
    public static void setup() throws IOException, IotHubException, URISyntaxException, InterruptedException {
        digitalTwinServiceAsyncClient = DigitalTwinServiceAsyncClientImpl.buildFromConnectionString()
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
        final SimpleTestInterfaceInstance simpleTestInterfaceInstance = new SimpleTestInterfaceInstance(SIMPLE_TEST_INTERFACE_INSTANCE_NAME);
        digitalTwinDeviceClient.registerInterfacesAsync(
                MODEL_URN,
                singletonList(simpleTestInterfaceInstance),
                (digitalTwinClientResult, context) -> {
                    log.debug("Register interfaces {}.", digitalTwinClientResult);
                    lock.countDown();
                },
                digitalTwinDeviceClient);
        lock.await();
    }

    @Test
    public void testGetModelInformationValidModelUrn() {
        String modelString = digitalTwinServiceAsyncClient.getModel(MODEL_URN).toBlocking().single();

        assertThat(modelString).as("Verify model").isNotNull();
        assertThat(modelString).contains(MODEL_URN);
    }

    // TODO: Autorest currently does not throw Exception for GET 404 status
    @Test (expected = NoSuchElementException.class)
    public void testGetModelInformationInvalidModelUrn() {
        String modelString = digitalTwinServiceAsyncClient.getModel(INVALID_MODEL_URN).toBlocking().single();
    }

    @Test
    public void testGetAllDigitalTwinInterfacesValidDigitalTwinId() {
        DigitalTwin digitalTwin = digitalTwinServiceAsyncClient.getDigitalTwin(digitalTwinId).toBlocking().single();

        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        assertThat(digitalTwin.getVersion()).isPositive();
        assertThat(digitalTwin.getInterfaceInstances().size()).isGreaterThanOrEqualTo(1);
    }

    // TODO: Autorest currently does not throw Exception for GET 404 status
    @Test (expected = NoSuchElementException.class)
    public void testGetAllDigitalTwinInterfacesInvalidDigitalTwinId() {
        DigitalTwin digitalTwin = digitalTwinServiceAsyncClient.getDigitalTwin(INVALID_DEVICE_ID).toBlocking().single();
    }

    @Test
    public void testUpdateDigitalTwinPropertiesValidUpdatePatch() throws IOException {
        String randomUuid = UUID.randomUUID().toString();
        String interfaceInstanceName = "testInterfaceInstanceName";
        String propertyName = "testPropertyName_".concat(randomUuid);
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
        DigitalTwin digitalTwin = digitalTwinServiceAsyncClient.updateDigitalTwinProperties(digitalTwinId, interfaceInstanceName, propertyPatch).toBlocking().single();

        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        assertThat(digitalTwin.getInterfaceInstances()).containsKey(interfaceInstanceName);
        Map<String, Property> properties = digitalTwin.getInterfaceInstances().get(interfaceInstanceName).properties();
        assertThat(properties).containsKey(propertyName);
        assertThat(properties.get(propertyName).desired().value()).isEqualTo(propertyValue);
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

        DigitalTwin digitalTwin = digitalTwinServiceAsyncClient.updateDigitalTwinProperties(digitalTwinId, interfaceInstanceName, propertyPatch).toBlocking().single();
    }

    @Test
    public void testInvokeCommandOnActiveDevice() {
        String commandResponse = digitalTwinServiceAsyncClient.invokeCommand(
                digitalTwinId,
                SIMPLE_TEST_INTERFACE_INSTANCE_NAME,
                COMMAND_SYNC_COMMAND,
                COMMAND_WRITABLE_PROPERTY_UPDATE_VALUE).toBlocking().single();
    }

    @Test
    public void testInvokeCommandInvalidInterfaceInstanceName() {
        String commandResponse = digitalTwinServiceAsyncClient.invokeCommand(
                digitalTwinId,
                INVALID_INTERFACE_INSTANCE_NAME,
                COMMAND_SYNC_COMMAND,
                COMMAND_WRITABLE_PROPERTY_UPDATE_VALUE).toBlocking().single();

        assertThat(commandResponse).isEqualTo(String.format(INTERFACE_INSTANCE_NOT_FOUND_MESSAGE_PATTERN, INVALID_INTERFACE_INSTANCE_NAME));
    }

    @Test
    public void testInvokeCommandInvalidCommandName() {
        String commandResponse = digitalTwinServiceAsyncClient.invokeCommand(
                digitalTwinId,
                SIMPLE_TEST_INTERFACE_INSTANCE_NAME,
                INVALID_COMMAND_NAME,
                COMMAND_WRITABLE_PROPERTY_UPDATE_VALUE).toBlocking().single();

        assertThat(commandResponse).isEqualTo(String.format(COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN, INVALID_COMMAND_NAME, SIMPLE_TEST_INTERFACE_ID));
    }

    @Test (expected = RestException.class)
    public void testInvokeCommandOnInactiveDevice() {
        String commandResponse = digitalTwinServiceAsyncClient.invokeCommand(
                INVALID_DEVICE_ID,
                SIMPLE_TEST_INTERFACE_INSTANCE_NAME,
                COMMAND_SYNC_COMMAND,
                COMMAND_WRITABLE_PROPERTY_UPDATE_VALUE).toBlocking().single();
    }

    @AfterClass
    public static void cleanup() throws IOException, IotHubException {
        deviceClient.closeNow();

        registryManager.removeDevice(digitalTwinId);
        registryManager.close();
    }
}
