// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestDevice;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.rest.RestException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveInterfaceNameFromInterfaceId;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.*;
import static com.microsoft.azure.sdk.iot.digitaltwin.service.util.Tools.createPropertyPatch;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class DigitalTwinServiceClientE2ETests {
    private static final String IOTHUB_CONNECTION_STRING = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.IOTHUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static final String DCM_ID = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.DCM_ID_ENV_VAR_NAME);
    private static final String TEST_INTERFACE_INSTANCE_NAME = retrieveInterfaceNameFromInterfaceId(TEST_INTERFACE_ID);

    private static final String DEVICE_ID_PREFIX = "DigitalTwinServiceClientE2ETests_";

    private static final String INVALID_MODEL_URN = "urn:invalidNamespace:invalidModelName:1"; // Model ID format should contain a min of 4 segments [urn:namespace:name:version]
    private static final String INVALID_INTERFACE_URN = "urn:invalidNamespace:invalidInterfaceName:1"; // Model ID format should contain a min of 4 segments [urn:namespace:name:version]
    private static final String INVALID_DEVICE_ID = "InvalidDevice";
    private static final String INVALID_INTERFACE_INSTANCE_NAME = "invalidInterfaceInstanceName";
    private static final String INVALID_COMMAND_NAME = "invalidCommandName";
    private static final String INTERFACE_INSTANCE_NOT_FOUND_MESSAGE_PATTERN = "Interface instance [%s] not found.";
    private static final int STATUS_NOT_FOUND = 404;

    private static final String COMMAND_WRITABLE_PROPERTY_UPDATE_VALUE = "updatedFromCommandInvocation";

    private String digitalTwinId;
    private TestDevice testDevice;

    private static DigitalTwinServiceClient digitalTwinServiceClient;

    @BeforeAll
    public static void setUp() {
        digitalTwinServiceClient = DigitalTwinServiceClientImpl.buildFromConnectionString()
                                                               .connectionString(IOTHUB_CONNECTION_STRING)
                                                               .build();
    }

    @BeforeEach
    public void setUpTest() throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDevice(digitalTwinId);
    }

    @Test
    public void testGetModelInformationValidModelUrn() {
        String modelString = digitalTwinServiceClient.getModel(DCM_ID);

        assertThat(modelString).as("Verify model").isNotNull();
        assertThat(modelString).contains(String.format("\"@id\":\"%s\"", DCM_ID));
    }

    @Test
    public void testGetModelInformationValidInterfaceUrn() {
        String modelString = digitalTwinServiceClient.getModel(TEST_INTERFACE_ID);

        assertThat(modelString).as("Verify Interface").isNotNull();
        assertThat(modelString).contains(String.format("\"@id\":\"%s\"", TEST_INTERFACE_ID));
    }

    // TODO: Autorest currently does not throw Exception for GET 404 status
    @Test
    public void testGetModelInformationInvalidModelUrn() {
        assertThrows(NoSuchElementException.class, () -> digitalTwinServiceClient.getModel(INVALID_MODEL_URN));
    }

    // TODO: Autorest currently does not throw Exception for GET 404 status
    @Test
    public void testGetModelInformationInvalidInterfaceUrn() {
        assertThrows(NoSuchElementException.class, () -> digitalTwinServiceClient.getModel(INVALID_INTERFACE_URN));
    }

    @Test
    public void testGetAllDigitalTwinInterfacesValidDigitalTwinId() {
        String digitalTwin = digitalTwinServiceClient.getDigitalTwin(digitalTwinId);

        // Assert that returned digital twin contains the default interface implemented by all devices
        assertAll("Expected DigitalTwin is not returned" ,
                () -> assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull(),
                () -> assertThat(digitalTwin).contains("\"urn_azureiot_ModelDiscovery_DigitalTwin\""),
                () -> assertThat(digitalTwin).contains("\"version\":1"));
    }

    // TODO: Autorest currently does not throw Exception for GET 404 status
    @Test
    public void testGetAllDigitalTwinInterfacesInvalidDigitalTwinId() {
        assertThrows(NoSuchElementException.class, () -> digitalTwinServiceClient.getDigitalTwin(INVALID_DEVICE_ID));
    }

    @Test
    public void testUpdateDigitalTwinPropertiesValidUpdatePatch() throws IOException {
        String randomUuid = UUID.randomUUID().toString();
        String interfaceInstanceName = "testInterfaceInstanceName";
        String propertyName = "testPropertyName_".concat(randomUuid);
        String propertyValue = "testPropertyValue_".concat(randomUuid);
        String propertyPatch = createPropertyPatch(propertyName, propertyValue);
        String digitalTwin = digitalTwinServiceClient.updateDigitalTwinProperties(digitalTwinId, interfaceInstanceName, propertyPatch);

        assertAll("Updated DigitalTwin does not have the expected properties",
                () -> assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull(),
                () -> assertThat(digitalTwin).contains(interfaceInstanceName),
                () -> assertThat(digitalTwin).contains(String.format("\"%s\":{\"reported\":null,\"desired\":{\"value\":\"%s\"}}", propertyName, propertyValue)));
    }

    @Test
    public void testUpdateDigitalTwinPropertiesInvalidPropertyPatch() {
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

        assertThrows(IOException.class, () -> digitalTwinServiceClient.updateDigitalTwinProperties(digitalTwinId, interfaceInstanceName, propertyPatch));
    }

    @Test
    public void testInvokeCommandOnOnlineDevice() {
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        TestInterfaceInstance2 testInterfaceInstance = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance)).blockingGet();
        log.debug("Register interfaces: {} = {}.", TEST_INTERFACE_INSTANCE_NAME, registrationResult);

        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME, COMMAND_SYNC_COMMAND, COMMAND_WRITABLE_PROPERTY_UPDATE_VALUE);

        assertAll("Command is not invoked",
                () -> assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull(),
                () -> assertThat(commandResponse.getStatus()).as("Verify Command Invocation Response Status").isNotNull(),
                () -> assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull());
    }

    @Test
    public void testInvokeCommandInvalidInterfaceInstanceName() {
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        TestInterfaceInstance2 testInterfaceInstance = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance)).blockingGet();
        log.debug("Register interfaces: {} = {}.", TEST_INTERFACE_INSTANCE_NAME, registrationResult);

        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, INVALID_INTERFACE_INSTANCE_NAME, COMMAND_SYNC_COMMAND, COMMAND_WRITABLE_PROPERTY_UPDATE_VALUE);

        assertAll("Expected command response is not returned for invalid interface name",
                () -> assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull(),
                () -> assertThat(commandResponse.getStatus()).isEqualTo(STATUS_NOT_FOUND),
                () -> assertThat(commandResponse.getPayload()).isEqualTo(String.format(INTERFACE_INSTANCE_NOT_FOUND_MESSAGE_PATTERN, INVALID_INTERFACE_INSTANCE_NAME)));
    }

    @Test
    public void testInvokeCommandInvalidCommandName() {
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        TestInterfaceInstance2 testInterfaceInstance = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance)).blockingGet();
        log.debug("Register interfaces: {} = {}.", TEST_INTERFACE_INSTANCE_NAME, registrationResult);

        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME, INVALID_COMMAND_NAME, COMMAND_WRITABLE_PROPERTY_UPDATE_VALUE);

        assertAll("Expected command response is not returned for invalid command name",
                () -> assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull(),
                () -> assertThat(commandResponse.getStatus()).isEqualTo(STATUS_NOT_FOUND),
                () -> assertThat(commandResponse.getPayload()).isEqualTo(String.format(COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN, INVALID_COMMAND_NAME, TestInterfaceInstance2.TEST_INTERFACE_ID)));
    }

    // Service throws a 404 Not Found
    @Test
    public void testInvokeCommandOnInvalidDevice() {
        assertThrows(RestException.class, () -> digitalTwinServiceClient.invokeCommand(INVALID_DEVICE_ID, TEST_INTERFACE_INSTANCE_NAME, COMMAND_SYNC_COMMAND, COMMAND_WRITABLE_PROPERTY_UPDATE_VALUE));
    }

    @AfterEach
    public void tearDownTest() throws IOException, IotHubException {
        testDevice.closeAndDeleteDevice();
    }
}
