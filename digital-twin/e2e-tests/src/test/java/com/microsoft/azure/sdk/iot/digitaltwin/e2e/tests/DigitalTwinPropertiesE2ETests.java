// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestDigitalTwinDevice;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveInterfaceNameFromInterfaceId;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.*;
import static com.microsoft.azure.sdk.iot.digitaltwin.service.util.Tools.createPropertyPatch;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Slf4j
public class DigitalTwinPropertiesE2ETests {
    private static final String IOTHUB_CONNECTION_STRING = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.IOTHUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static final String DCM_ID = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.DCM_ID_ENV_VAR_NAME);
    private static final String TEST_INTERFACE_INSTANCE_NAME = retrieveInterfaceNameFromInterfaceId(TEST_INTERFACE_ID);

    private static final String DEVICE_ID_PREFIX = "DigitalTwinPropertiesE2ETests_";
    private static final int MAX_THREADS_MULTITHREADED_TEST = 5;
    private static final String PROPERTY_VALUE_PATTERN = "{\"value\":\"%s\"}";
    private static final String PROPERTY_EACH_PATCH_PATTERN = "\"%s\":{\"reported\":null,\"desired\":%s}";
    private static final String PROPERTY_PATTERN_UPDATED_FROM_SERVICE = "\"name\":\"%s\",\"properties\":{\"%s\":{\"reported\":null,\"desired\":%s}}";
    private static final String SERVICE_PROPERTY_UPDATE_PREFIX = "propertyUpdatedFromService_";
    private static final String DEVICE_PROPERTY_UPDATE_PREFIX = "propertyUpdatedFromDevice_";
    private static final String UNKNOWN_INTERFACE_INSTANCE_NAME = "unknownInterfaceInstanceName";
    private static final String UNKNOWN_PROPERTY_NAME = "unknownPropertyName";

    private static DigitalTwinServiceClient digitalTwinServiceClient;
    private String digitalTwinId;
    private TestDigitalTwinDevice testDevice;
    private TestInterfaceInstance2 testInterfaceInstance;

    @BeforeAll
    public static void setUp() {
        digitalTwinServiceClient = DigitalTwinServiceClientImpl.buildFromConnectionString()
                                                               .connectionString(IOTHUB_CONNECTION_STRING)
                                                               .build();
    }

    @ParameterizedTest(name = "{index}: Update single writable property from service: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testUpdateSingleWritablePropertyFromService(IotHubClientProtocol protocol) throws IOException, URISyntaxException, IotHubException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        testInterfaceInstance = registerAndReturnDigitalTwinInterface(testDevice);

        String randomUuid = UUID.randomUUID().toString();
        String propertyValue = SERVICE_PROPERTY_UPDATE_PREFIX.concat(randomUuid);
        String propertyPatch = createPropertyPatch(singletonMap(PROPERTY_NAME_WRITABLE, propertyValue));
        String digitalTwin = digitalTwinServiceClient.updateDigitalTwinProperties(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME, propertyPatch);

        String expectedValue = String.format(PROPERTY_VALUE_PATTERN, propertyValue);
        assertThat(testInterfaceInstance.verifyIfPropertyUpdateWasReceived(PROPERTY_NAME_WRITABLE, expectedValue)).as("Verify that device received the property update from service").isTrue();

        // Assert that property is updated in the twin - strong matching done to ensure correct property is updated - will break if service changes the twin structure
        assertAll("Updated DigitalTwin does not have the expected properties",
                () -> assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull(),
                () -> assertThat(digitalTwin).contains(String.format(PROPERTY_PATTERN_UPDATED_FROM_SERVICE, TEST_INTERFACE_INSTANCE_NAME, PROPERTY_NAME_WRITABLE, expectedValue)));
    }

    @ParameterizedTest(name = "{index}: Update multiple writable properties from service: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testUpdateMultipleWritablePropertyFromService(IotHubClientProtocol protocol) throws IOException, URISyntaxException, IotHubException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        testInterfaceInstance = registerAndReturnDigitalTwinInterface(testDevice);

        String propertyValue1 = SERVICE_PROPERTY_UPDATE_PREFIX.concat(UUID.randomUUID().toString());
        String propertyValue2 = SERVICE_PROPERTY_UPDATE_PREFIX.concat(UUID.randomUUID().toString());
        Map<String, String> propertyValues = new HashMap<String, String>() {{ put(PROPERTY_NAME_WRITABLE, propertyValue1); put(PROPERTY_NAME_2_WRITABLE, propertyValue2); }};

        String propertyPatch = createPropertyPatch(propertyValues);
        String digitalTwin = digitalTwinServiceClient.updateDigitalTwinProperties(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME, propertyPatch);

        String expectedValue1 = String.format(PROPERTY_VALUE_PATTERN, propertyValue1);
        String expectedValue2 = String.format(PROPERTY_VALUE_PATTERN, propertyValue2);
        assertThat(testInterfaceInstance.verifyIfPropertyUpdateWasReceived(PROPERTY_NAME_WRITABLE, expectedValue1)).as("Verify that device received the property update from service").isTrue();
        assertThat(testInterfaceInstance.verifyIfPropertyUpdateWasReceived(PROPERTY_NAME_2_WRITABLE, expectedValue2)).as("Verify that device received the property update from service").isTrue();

        // Assert that property is updated in the twin
        assertAll("Updated DigitalTwin does not have the expected properties",
                () -> assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull(),
                () -> assertThat(digitalTwin).contains(String.format(PROPERTY_EACH_PATCH_PATTERN, PROPERTY_NAME_WRITABLE, expectedValue1)),
                () -> assertThat(digitalTwin).contains(String.format(PROPERTY_EACH_PATCH_PATTERN, PROPERTY_NAME_2_WRITABLE, expectedValue2)));
    }

    @ParameterizedTest(name = "{index}: Update writable properties from service multithreaded: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testUpdateWritablePropertyFromServiceMultithreaded(IotHubClientProtocol protocol) throws IOException, URISyntaxException, IotHubException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        testInterfaceInstance = registerAndReturnDigitalTwinInterface(testDevice);

        List<String> payloadValueList = new Random().ints(MAX_THREADS_MULTITHREADED_TEST).boxed()
                                                    .map(Object :: toString)
                                                    .collect(Collectors.toList());
        List<String> propertyPatchList = payloadValueList.stream()
                                                         .map(s -> createPropertyPatch(singletonMap(PROPERTY_NAME_WRITABLE, s)))
                                                         .collect(Collectors.toList());

        Flowable.range(0, MAX_THREADS_MULTITHREADED_TEST)
                .parallel()
                .runOn(Schedulers.io())
                .map(integer -> digitalTwinServiceClient.updateDigitalTwinProperties(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME, propertyPatchList.get(integer)))
                .sequential()
                .toList()
                .blockingGet();

        boolean lastUpdatedPropertyReceived = false;
        String actualUpdatedProperty = null;
        for (int i = 0; i <  MAX_THREADS_MULTITHREADED_TEST; i++) {
            String expectedValue = String.format(PROPERTY_VALUE_PATTERN, payloadValueList.get(i));

            if (testInterfaceInstance.verifyIfPropertyUpdateWasReceived(PROPERTY_NAME_WRITABLE, expectedValue)) {
                lastUpdatedPropertyReceived = true;
                actualUpdatedProperty = expectedValue;
            }
        }

        // Only the last updated property will persist on the twin
        assertThat(lastUpdatedPropertyReceived).as("Verify that device received the property update from service").isTrue();

        // Assert that property is updated in the twin - strong matching done to ensure correct property is updated - will break if service changes the twin structure
        String digitalTwin = digitalTwinServiceClient.getDigitalTwin(digitalTwinId);
        String finalActualUpdatedProperty = actualUpdatedProperty;
        assertAll("Updated DigitalTwin does not have the expected properties",
                () -> assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull(),
                () -> assertThat(digitalTwin).contains(String.format(PROPERTY_PATTERN_UPDATED_FROM_SERVICE, TEST_INTERFACE_INSTANCE_NAME, PROPERTY_NAME_WRITABLE, finalActualUpdatedProperty)));
    }

    @ParameterizedTest(name = "{index}: Update single property from service unknown interface instance name: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testUpdateSinglePropertyFromServiceUnknownInterfaceName(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        testInterfaceInstance = registerAndReturnDigitalTwinInterface(testDevice);

        String randomUuid = UUID.randomUUID().toString();
        String propertyValue = SERVICE_PROPERTY_UPDATE_PREFIX.concat(randomUuid);
        String propertyPatch = createPropertyPatch(singletonMap(PROPERTY_NAME_WRITABLE, propertyValue));
        digitalTwinServiceClient.updateDigitalTwinProperties(digitalTwinId, UNKNOWN_INTERFACE_INSTANCE_NAME, propertyPatch);

        String expectedValue = String.format(PROPERTY_VALUE_PATTERN, propertyValue);
        assertThat(testInterfaceInstance.verifyIfPropertyUpdateWasReceived(PROPERTY_NAME_WRITABLE, expectedValue)).as("Verify that device did not receive the property update from service").isFalse();
    }

    @Disabled("Disabled until service validates and throws exception")
    @ParameterizedTest(name = "{index}: Update single property from service unknown property name: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testUpdateSingleWritablePropertyFromServiceUnknownPropertyName(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        testInterfaceInstance = registerAndReturnDigitalTwinInterface(testDevice);

        String randomUuid = UUID.randomUUID().toString();
        String propertyValue = SERVICE_PROPERTY_UPDATE_PREFIX.concat(randomUuid);
        String propertyPatch = createPropertyPatch(singletonMap(UNKNOWN_PROPERTY_NAME, propertyValue));
        digitalTwinServiceClient.updateDigitalTwinProperties(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME, propertyPatch);

        String expectedValue = String.format(PROPERTY_VALUE_PATTERN, propertyValue);
        assertThat(testInterfaceInstance.verifyIfPropertyUpdateWasReceived(UNKNOWN_PROPERTY_NAME, expectedValue)).as("Verify that device did not receive the property update from service").isFalse();
    }

    @Disabled("Disabled until service validates and throws exception")
    @ParameterizedTest(name = "{index}: Update single readonly property from service: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testUpdateSingleReadonlyPropertyFromService(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        testInterfaceInstance = registerAndReturnDigitalTwinInterface(testDevice);

        String randomUuid = UUID.randomUUID().toString();
        String propertyValue = SERVICE_PROPERTY_UPDATE_PREFIX.concat(randomUuid);
        String propertyPatch = createPropertyPatch(singletonMap(PROPERTY_NAME_READONLY, propertyValue));
        String digitalTwin = digitalTwinServiceClient.updateDigitalTwinProperties(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME, propertyPatch);

        String expectedValue = String.format(PROPERTY_VALUE_PATTERN, propertyValue);
        assertThat(testInterfaceInstance.verifyIfPropertyUpdateWasReceived(PROPERTY_NAME_READONLY, expectedValue)).as("Verify that device did not receive the property update from service").isFalse();

        // Assert that property is updated in the twin - strong matching done to ensure correct property is updated - will break if service changes the twin structure
        assertAll("Updated DigitalTwin does not have the expected properties",
                () -> assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull(),
                () -> assertThat(digitalTwin).doesNotContain(String.format(PROPERTY_PATTERN_UPDATED_FROM_SERVICE, TEST_INTERFACE_INSTANCE_NAME, PROPERTY_NAME_READONLY, expectedValue)));
    }


    @ParameterizedTest(name = "{index}: Update single writable property from device: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testUpdateSingleWritablePropertyFromDevice(IotHubClientProtocol protocol) throws IOException, URISyntaxException, IotHubException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        testInterfaceInstance = registerAndReturnDigitalTwinInterface(testDevice);

        String propertyValue = DEVICE_PROPERTY_UPDATE_PREFIX.concat(UUID.randomUUID().toString());
        DigitalTwinClientResult updateResult = testInterfaceInstance.updatePropertyFromDevice(PROPERTY_NAME_WRITABLE, propertyValue).blockingGet();

        assertThat(updateResult).as("Verify that device sent the property update to service").isEqualTo(DIGITALTWIN_CLIENT_OK);
    }

    private TestInterfaceInstance2 registerAndReturnDigitalTwinInterface(TestDigitalTwinDevice testDevice) {
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        TestInterfaceInstance2 testInterfaceInstance = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance)).blockingGet();
        assertThat(registrationResult).isEqualTo(DIGITALTWIN_CLIENT_OK);

        return testInterfaceInstance;
    }

    @AfterEach
    public void tearDownTest() {
        testDevice.closeAndDeleteDevice();
    }
}
