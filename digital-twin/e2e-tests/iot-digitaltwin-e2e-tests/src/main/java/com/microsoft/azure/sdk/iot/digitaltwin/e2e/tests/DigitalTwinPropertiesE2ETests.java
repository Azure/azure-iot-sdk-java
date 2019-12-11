// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestDigitalTwinDevice;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceAsyncClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceAsyncClientImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT_WS;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.*;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.*;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(Parameterized.class)
public class DigitalTwinPropertiesE2ETests {
    private static final String IOT_HUB_CONNECTION_STRING = retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static final String TEST_INTERFACE_INSTANCE_NAME = retrieveInterfaceNameFromInterfaceId(TEST_INTERFACE_ID);

    private static final String DEVICE_ID_PREFIX = "DigitalTwinPropertiesE2ETests_";

    private static final String PROPERTY_VALUE_PATTERN = "{\"value\":\"%s\"}";
    private static final String PROPERTY_PATTERN_UPDATED_FROM_SERVICE = "\"%s\":{\"desired\":%s}";
    private static final String SERVICE_PROPERTY_UPDATE_PREFIX = "propertyUpdatedFromService_";
    private static final String DEVICE_PROPERTY_UPDATE_PREFIX = "propertyUpdatedFromDevice_";
    private static final String UNKNOWN_INTERFACE_INSTANCE_NAME = "unknownInterfaceInstanceName";
    private static final String UNKNOWN_PROPERTY_NAME = "unknownPropertyName";

    private static DigitalTwinServiceClient digitalTwinServiceClient;
    private static DigitalTwinServiceAsyncClient digitalTwinServiceAsyncClient;
    private String digitalTwinId;
    private TestDigitalTwinDevice testDevice;
    private TestInterfaceInstance2 testInterfaceInstance;

    @Parameterized.Parameter(0)
    public IotHubClientProtocol protocol;

    @Parameterized.Parameters(name = "{index}: Update Properties Test: protocol={0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
                {MQTT},
                {MQTT_WS},
        });
    }

    @BeforeClass
    public static void setUp() {
        digitalTwinServiceClient = DigitalTwinServiceClientImpl.buildFromConnectionString()
                                                               .connectionString(IOT_HUB_CONNECTION_STRING)
                                                               .build();
        digitalTwinServiceAsyncClient = DigitalTwinServiceAsyncClientImpl.buildFromConnectionString()
                                                                         .connectionString(IOT_HUB_CONNECTION_STRING)
                                                                         .build();
    }

    @Before
    public void setUpTest() throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        testInterfaceInstance = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance)).blockingGet();
        assertThat(registrationResult).isEqualTo(DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK);
    }

    @Test
    public void testUpdateSingleWritablePropertyFromService() throws IOException {
        String randomUuid = UUID.randomUUID().toString();
        String propertyValue = SERVICE_PROPERTY_UPDATE_PREFIX.concat(randomUuid);
        String propertyPatch = createPropertyPatch(singletonMap(PROPERTY_NAME_WRITABLE, propertyValue));
        String digitalTwin = digitalTwinServiceClient.updateDigitalTwinProperties(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME, propertyPatch);

        String expectedValue = String.format(PROPERTY_VALUE_PATTERN, propertyValue);
        assertThat(testInterfaceInstance.verifyIfPropertyUpdateWasReceived(PROPERTY_NAME_WRITABLE, expectedValue)).as("Verify that device received the property update from service").isTrue();

        // Assert that property is updated in the twin - strong matching done to ensure correct property is updated - will break if service changes the twin structure
        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        assertThat(digitalTwin).contains(String.format(PROPERTY_PATTERN_UPDATED_FROM_SERVICE, TEST_INTERFACE_INSTANCE_NAME, PROPERTY_NAME_WRITABLE, expectedValue));
    }

    @Test
    public void testUpdateMultipleWritablePropertyFromService() throws IOException {
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
        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        assertThat(digitalTwin).contains(String.format(PROPERTY_PATTERN_UPDATED_FROM_SERVICE, PROPERTY_NAME_WRITABLE, expectedValue1));
        assertThat(digitalTwin).contains(String.format(PROPERTY_PATTERN_UPDATED_FROM_SERVICE, PROPERTY_NAME_2_WRITABLE, expectedValue2));
    }

    @Test
    public void testUpdateWritablePropertyFromAsyncServiceMultithreaded() throws InterruptedException {
        final Semaphore semaphore = new Semaphore(0);
        List<String> payloadValueList = generateRandomStringList(MAX_THREADS_MULTITHREADED_TEST);
        List<String> propertyPatchList = payloadValueList.stream()
                                                         .map(propertyValue -> createPropertyPatch(singletonMap(PROPERTY_NAME_WRITABLE, propertyValue)))
                                                         .collect(Collectors.toList());

        propertyPatchList.forEach(propertyPatch -> {
            try {
                digitalTwinServiceAsyncClient.updateDigitalTwinProperties(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME, propertyPatch)
                        .subscribe(s -> semaphore.release());
            } catch (IOException e) {
                log.error("Exception thrown while updating property patch = {}", propertyPatch, e);
            }
        });

        assertThat(semaphore.tryAcquire(MAX_THREADS_MULTITHREADED_TEST, MAX_WAIT_TIME_FOR_ASYNC_CALL_IN_SECONDS, SECONDS)).as("Timeout executing Async call").isTrue();

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
        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        assertThat(digitalTwin).contains(String.format(PROPERTY_PATTERN_UPDATED_FROM_SERVICE, TEST_INTERFACE_INSTANCE_NAME, PROPERTY_NAME_WRITABLE, finalActualUpdatedProperty));
    }

    @Test
    public void testUpdateSinglePropertyFromServiceUnknownInterfaceName() throws IOException {
        String randomUuid = UUID.randomUUID().toString();
        String propertyValue = SERVICE_PROPERTY_UPDATE_PREFIX.concat(randomUuid);
        String propertyPatch = createPropertyPatch(singletonMap(PROPERTY_NAME_WRITABLE, propertyValue));
        digitalTwinServiceClient.updateDigitalTwinProperties(digitalTwinId, UNKNOWN_INTERFACE_INSTANCE_NAME, propertyPatch);

        String expectedValue = String.format(PROPERTY_VALUE_PATTERN, propertyValue);
        assertThat(testInterfaceInstance.verifyIfPropertyUpdateWasReceived(PROPERTY_NAME_WRITABLE, expectedValue)).as("Verify that device did not receive the property update from service").isFalse();
    }

    @Ignore("Disabled until service validates and throws exception")
    @Test
    public void testUpdateSingleWritablePropertyFromServiceUnknownPropertyName() throws IOException {
        String randomUuid = UUID.randomUUID().toString();
        String propertyValue = SERVICE_PROPERTY_UPDATE_PREFIX.concat(randomUuid);
        String propertyPatch = createPropertyPatch(singletonMap(UNKNOWN_PROPERTY_NAME, propertyValue));
        digitalTwinServiceClient.updateDigitalTwinProperties(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME, propertyPatch);

        String expectedValue = String.format(PROPERTY_VALUE_PATTERN, propertyValue);
        assertThat(testInterfaceInstance.verifyIfPropertyUpdateWasReceived(UNKNOWN_PROPERTY_NAME, expectedValue)).as("Verify that device did not receive the property update from service").isFalse();
    }

    @Ignore("Disabled until service validates and throws exception")
    @Test
    public void testUpdateSingleReadonlyPropertyFromService() throws IOException {
        String randomUuid = UUID.randomUUID().toString();
        String propertyValue = SERVICE_PROPERTY_UPDATE_PREFIX.concat(randomUuid);
        String propertyPatch = createPropertyPatch(singletonMap(PROPERTY_NAME_READONLY, propertyValue));
        String digitalTwin = digitalTwinServiceClient.updateDigitalTwinProperties(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME, propertyPatch);

        String expectedValue = String.format(PROPERTY_VALUE_PATTERN, propertyValue);
        assertThat(testInterfaceInstance.verifyIfPropertyUpdateWasReceived(PROPERTY_NAME_READONLY, expectedValue)).as("Verify that device did not receive the property update from service").isFalse();

        // Assert that property is updated in the twin - strong matching done to ensure correct property is updated - will break if service changes the twin structure
        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        assertThat(digitalTwin).doesNotContain(String.format(PROPERTY_PATTERN_UPDATED_FROM_SERVICE, TEST_INTERFACE_INSTANCE_NAME, PROPERTY_NAME_READONLY, expectedValue));
    }

    @Test
    public void testUpdateSingleWritablePropertyFromDevice() {
        String propertyValue = DEVICE_PROPERTY_UPDATE_PREFIX.concat(UUID.randomUUID().toString());
        DigitalTwinClientResult updateResult = testInterfaceInstance.updatePropertyFromDevice(PROPERTY_NAME_WRITABLE, propertyValue).blockingGet();

        assertThat(updateResult).as("Verify that device sent the property update to service").isEqualTo(DIGITALTWIN_CLIENT_OK);
    }

    @After
    public void tearDownTest() {
        if (testDevice != null) {
            testDevice.closeAndDeleteDevice();
        }
    }
}
