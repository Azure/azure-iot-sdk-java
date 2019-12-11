// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestDigitalTwinDevice;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.rest.RestException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.DCM_ID;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveEnvironmentVariableValue;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveInterfaceNameFromInterfaceId;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.SYNC_COMMAND_WITH_PAYLOAD;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TEST_INTERFACE_ID;
import static org.assertj.core.api.Assertions.assertThat;

public class DigitalTwinServiceClientE2ETests {
    private static final String IOT_HUB_CONNECTION_STRING = retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static final String TEST_INTERFACE_INSTANCE_NAME = retrieveInterfaceNameFromInterfaceId(TEST_INTERFACE_ID);

    private static final String DEVICE_ID_PREFIX = "DigitalTwinServiceClientE2ETests_";

    private static final String INVALID_MODEL_URN = "urn:invalidNamespace:invalidModelName:1"; // Model ID format should contain a min of 4 segments [urn:namespace:name:version]
    private static final String INVALID_INTERFACE_URN = "urn:invalidNamespace:invalidInterfaceName:1"; // Interface ID format should contain a min of 4 segments [urn:namespace:name:version]
    private static final String INVALID_DEVICE_ID = "InvalidDevice";

    private static DigitalTwinServiceClient digitalTwinServiceClient;
    private String digitalTwinId;
    private TestDigitalTwinDevice testDevice;

    @BeforeClass
    public static void setUp() {
        digitalTwinServiceClient = DigitalTwinServiceClientImpl.buildFromConnectionString()
                                                               .connectionString(IOT_HUB_CONNECTION_STRING)
                                                               .build();
    }

    @Before
    public void setUpTest() throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, MQTT);
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
    @Test (expected = NoSuchElementException.class)
    public void testGetModelInformationInvalidModelUrn() {
        digitalTwinServiceClient.getModel(INVALID_MODEL_URN);
    }

    // TODO: Autorest currently does not throw Exception for GET 404 status
    @Test (expected = NoSuchElementException.class)
    public void testGetModelInformationInvalidInterfaceUrn() {
        digitalTwinServiceClient.getModel(INVALID_INTERFACE_URN);
    }

    @Test
    public void testGetAllDigitalTwinInterfacesValidDigitalTwinId() {
        String digitalTwin = digitalTwinServiceClient.getDigitalTwin(digitalTwinId);

        // Assert that returned digital twin contains the default interface implemented by all devices
        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        assertThat(digitalTwin).contains("\"urn_azureiot_ModelDiscovery_DigitalTwin\"");
        assertThat(digitalTwin).contains("\"version\":1");
    }

    // TODO: Autorest currently does not throw Exception for GET 404 status
    @Test (expected = NoSuchElementException.class)
    public void testGetAllDigitalTwinInterfacesInvalidDigitalTwinId() {
        digitalTwinServiceClient.getDigitalTwin(INVALID_DEVICE_ID);
    }

    @Test (expected = IOException.class)
    public void testUpdateDigitalTwinPropertiesInvalidPropertyPatch() throws IOException {
        String randomUuid = UUID.randomUUID().toString();
        String interfaceInstanceName = "testInterfaceInstanceName";
        String propertyName = "testPropertyName_";
        String propertyValue = "testPropertyValue_".concat(randomUuid);
        String propertyPatch = "{"
                + "  \"properties\": {"
                + "      \"" + propertyName + "\": {"
                + "          \"desired\": \"" + propertyValue + "\""
                + "          }"
                + "      }"
                + "  }";

        digitalTwinServiceClient.updateDigitalTwinProperties(digitalTwinId, interfaceInstanceName, propertyPatch);
    }

    // Service throws a 404 Not Found
    @Test (expected = RestException.class)
    public void testInvokeCommandOnInvalidDevice() {
        String samplePayload = "samplePayload";
        digitalTwinServiceClient.invokeCommand(INVALID_DEVICE_ID, TEST_INTERFACE_INSTANCE_NAME, SYNC_COMMAND_WITH_PAYLOAD, samplePayload);
    }

    @After
    public void tearDownTest() {
        if (testDevice != null) {
            testDevice.closeAndDeleteDevice();
        }
    }
}
