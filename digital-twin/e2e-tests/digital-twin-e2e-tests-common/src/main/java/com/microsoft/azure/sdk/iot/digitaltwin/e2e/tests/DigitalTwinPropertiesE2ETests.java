// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestDigitalTwinDevice;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
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
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.MAX_THREADS_MULTITHREADED_TEST;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.MAX_WAIT_TIME_FOR_ASYNC_CALL_IN_SECONDS;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.createPropertyPatch;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.generateRandomStringList;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveEnvironmentVariableValue;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveComponentNameFromInterfaceId;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.PROPERTY_NAME_2_WRITABLE;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.PROPERTY_NAME_READONLY;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.PROPERTY_NAME_WRITABLE;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TEST_INTERFACE_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
@RunWith(Parameterized.class)
public class DigitalTwinPropertiesE2ETests extends DigitalTwinE2ETests {
    private static final String IOT_HUB_CONNECTION_STRING = retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static final String TEST_COMPONENT_NAME = retrieveComponentNameFromInterfaceId(TEST_INTERFACE_ID);

    private static final String DEVICE_ID_PREFIX = "DigitalTwinPropertiesE2ETests_";

    private static final String PROPERTY_VALUE_PATTERN = "{\"value\":\"%s\"}";
    private static final String SERVICE_PROPERTY_UPDATE_PREFIX = "propertyUpdatedFromService_";
    private static final String DEVICE_PROPERTY_UPDATE_PREFIX = "propertyUpdatedFromDevice_";
    private static final String UNKNOWN_COMPONENT_NAME = "unknownComponentName";
    private static final String UNKNOWN_PROPERTY_NAME = "unknownPropertyName";

    private static DigitalTwinServiceClient digitalTwinServiceClient;
    private static DigitalTwinServiceAsyncClient digitalTwinServiceAsyncClient;
    private TestDigitalTwinDevice testDevice;
    private TestComponent2 testComponent;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Rule
    public Timeout globalTimeout = Timeout.seconds(5 * 60); // 5 minutes max per method tested

    @Parameterized.Parameter(0)
    public IotHubClientProtocol protocol;
    private DigitalTwinDeviceClient digitalTwinDeviceClient;

    @Parameterized.Parameters(name = "{index}: Update Properties Test: protocol={0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
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
        testDevice = new TestDigitalTwinDevice(DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString()), protocol);
        digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        testComponent = new TestComponent2(TEST_COMPONENT_NAME);
        assertThat(digitalTwinDeviceClient.bindComponents(singletonList(testComponent))).isEqualTo(DIGITALTWIN_CLIENT_OK);
    }

    @Test @Ignore
    public void testUpdateSingleWritablePropertyFromService() throws IOException {
        assertThat(digitalTwinDeviceClient.subscribeForProperties()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        String randomUuid = UUID.randomUUID().toString();
        String propertyValue = SERVICE_PROPERTY_UPDATE_PREFIX.concat(randomUuid);
        String propertyPatch = createPropertyPatch(singletonMap(PROPERTY_NAME_WRITABLE, propertyValue));
        String digitalTwin = digitalTwinServiceClient.updateDigitalTwinProperties(testDevice.getDeviceId(), TEST_COMPONENT_NAME, propertyPatch);

        JsonNode expectedValue = objectMapper.readTree(String.format(PROPERTY_VALUE_PATTERN, propertyValue));
        assertThat(testComponent.verifyIfPropertyUpdateWasReceived(PROPERTY_NAME_WRITABLE, expectedValue)).as("Verify that device received the property update from service").isTrue();

        // Assert that property is updated in the twin
        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        JsonNode updatedProperty = getPropertyJsonNodeFromTwin(digitalTwin, TEST_COMPONENT_NAME);
        assertThat(updatedProperty.has(PROPERTY_NAME_WRITABLE)).isTrue();
        String updatePropertyValue = getPropertyValueFromTwin(updatedProperty, PROPERTY_NAME_WRITABLE);
        assertThat(updatePropertyValue).isEqualTo(propertyValue);
    }

    @Test @Ignore
    public void testUpdateMultipleWritablePropertyFromService() throws IOException {
        assertThat(digitalTwinDeviceClient.subscribeForProperties()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        String propertyValue1 = SERVICE_PROPERTY_UPDATE_PREFIX.concat(UUID.randomUUID().toString());
        String propertyValue2 = SERVICE_PROPERTY_UPDATE_PREFIX.concat(UUID.randomUUID().toString());
        Map<String, String> propertyValues = new HashMap<String, String>() {{
            put(PROPERTY_NAME_WRITABLE, propertyValue1);
            put(PROPERTY_NAME_2_WRITABLE, propertyValue2);
        }};

        String propertyPatch = createPropertyPatch(propertyValues);
        String digitalTwin = digitalTwinServiceClient.updateDigitalTwinProperties(testDevice.getDeviceId(), TEST_COMPONENT_NAME, propertyPatch);

        JsonNode expectedValue1 = objectMapper.readTree(String.format(PROPERTY_VALUE_PATTERN, propertyValue1));
        JsonNode expectedValue2 = objectMapper.readTree(String.format(PROPERTY_VALUE_PATTERN, propertyValue2));
        assertThat(testComponent.verifyIfPropertyUpdateWasReceived(PROPERTY_NAME_WRITABLE, expectedValue1)).as("Verify that device received the property update from service").isTrue();
        assertThat(testComponent.verifyIfPropertyUpdateWasReceived(PROPERTY_NAME_2_WRITABLE, expectedValue2)).as("Verify that device received the property update from service").isTrue();

        // Assert that property is updated in the twin
        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        JsonNode updatedProperty = getPropertyJsonNodeFromTwin(digitalTwin, TEST_COMPONENT_NAME);

        assertThat(updatedProperty.has(PROPERTY_NAME_WRITABLE)).isTrue();
        String updatePropertyValue1 = getPropertyValueFromTwin(updatedProperty, PROPERTY_NAME_WRITABLE);
        assertThat(updatePropertyValue1).isEqualTo(propertyValue1);

        assertThat(updatedProperty.has(PROPERTY_NAME_2_WRITABLE)).isTrue();
        String updatePropertyValue2 = getPropertyValueFromTwin(updatedProperty, PROPERTY_NAME_2_WRITABLE);
        assertThat(updatePropertyValue2).isEqualTo(propertyValue2);
    }

    @Test @Ignore
    public void testUpdateWritablePropertyFromAsyncServiceMultithreaded() throws InterruptedException, IOException {
        assertThat(digitalTwinDeviceClient.subscribeForProperties()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        final Semaphore semaphore = new Semaphore(0);
        List<String> payloadValueList = generateRandomStringList(MAX_THREADS_MULTITHREADED_TEST);
        List<String> propertyPatchList = payloadValueList.stream()
                                                         .map(propertyValue -> createPropertyPatch(singletonMap(PROPERTY_NAME_WRITABLE, propertyValue)))
                                                         .collect(Collectors.toList());

        propertyPatchList.forEach(propertyPatch -> {
            try {
                digitalTwinServiceAsyncClient.updateDigitalTwinProperties(testDevice.getDeviceId(), TEST_COMPONENT_NAME, propertyPatch)
                                             .subscribe(s -> semaphore.release());
            } catch (IOException e) {
                log.error("Exception thrown while updating property patch = {}", propertyPatch, e);
            }
        });

        assertThat(semaphore.tryAcquire(MAX_THREADS_MULTITHREADED_TEST, MAX_WAIT_TIME_FOR_ASYNC_CALL_IN_SECONDS, SECONDS)).as("Timeout executing Async call").isTrue();

        boolean lastUpdatedPropertyReceived = false;
        String actualUpdatedProperty = null;
        for (int i = 0; i < MAX_THREADS_MULTITHREADED_TEST; i++) {
            JsonNode expectedValue = objectMapper.readTree(String.format(PROPERTY_VALUE_PATTERN, payloadValueList.get(i)));

            if (testComponent.verifyIfPropertyUpdateWasReceived(PROPERTY_NAME_WRITABLE, expectedValue)) {
                lastUpdatedPropertyReceived = true;
                actualUpdatedProperty = payloadValueList.get(i);
            }
        }

        // Only the last updated property will persist on the twin
        assertThat(lastUpdatedPropertyReceived).as("Verify that device received the property update from service").isTrue();

        // Assert that property is updated in the twin
        String digitalTwin = digitalTwinServiceClient.getDigitalTwin(testDevice.getDeviceId());
        String finalActualUpdatedProperty = actualUpdatedProperty;
        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();

        JsonNode updatedProperty = getPropertyJsonNodeFromTwin(digitalTwin, TEST_COMPONENT_NAME);
        assertThat(updatedProperty.has(PROPERTY_NAME_WRITABLE)).isTrue();
        String updatePropertyValue = getPropertyValueFromTwin(updatedProperty, PROPERTY_NAME_WRITABLE);
        assertThat(updatePropertyValue).isEqualTo(finalActualUpdatedProperty);
    }

    @Test @Ignore
    public void testUpdateSinglePropertyFromServiceUnknownInterfaceName() throws IOException {
        assertThat(digitalTwinDeviceClient.subscribeForProperties()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        String randomUuid = UUID.randomUUID().toString();
        String propertyValue = SERVICE_PROPERTY_UPDATE_PREFIX.concat(randomUuid);
        String propertyPatch = createPropertyPatch(singletonMap(PROPERTY_NAME_WRITABLE, propertyValue));
        digitalTwinServiceClient.updateDigitalTwinProperties(testDevice.getDeviceId(), UNKNOWN_COMPONENT_NAME, propertyPatch);

        JsonNode expectedValue = objectMapper.readTree(String.format(PROPERTY_VALUE_PATTERN, propertyValue));
        assertThat(testComponent.verifyIfPropertyUpdateWasReceived(PROPERTY_NAME_WRITABLE, expectedValue)).as("Verify that device did not receive the property update from service").isFalse();
    }

    @Test @Ignore
    public void testUpdateFromServiceWithoutSubscribe() throws IOException {
        String randomUuid = UUID.randomUUID().toString();
        String propertyValue = SERVICE_PROPERTY_UPDATE_PREFIX.concat(randomUuid);
        String propertyPatch = createPropertyPatch(singletonMap(PROPERTY_NAME_WRITABLE, propertyValue));
        String digitalTwin = digitalTwinServiceClient.updateDigitalTwinProperties(testDevice.getDeviceId(), TEST_COMPONENT_NAME, propertyPatch);

        JsonNode expectedValue = objectMapper.readTree(String.format(PROPERTY_VALUE_PATTERN, propertyValue));
        assertThat(testComponent.verifyIfPropertyUpdateWasReceived(PROPERTY_NAME_WRITABLE, expectedValue)).as("Verify that device did not receive the property update from service").isFalse();

        // Assert that property is updated in the twin
        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        JsonNode updatedProperty = getPropertyJsonNodeFromTwin(digitalTwin, TEST_COMPONENT_NAME);
        assertThat(updatedProperty.has(PROPERTY_NAME_WRITABLE)).isTrue();
        String updatePropertyValue = getPropertyValueFromTwin(updatedProperty, PROPERTY_NAME_WRITABLE);
        assertThat(updatePropertyValue).isEqualTo(propertyValue);
    }

    @Ignore("Disabled until service validates and throws exception")
    @Test
    public void testUpdateSingleWritablePropertyFromServiceUnknownPropertyName() throws IOException {
        assertThat(digitalTwinDeviceClient.subscribeForProperties()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        String randomUuid = UUID.randomUUID().toString();
        String propertyValue = SERVICE_PROPERTY_UPDATE_PREFIX.concat(randomUuid);
        String propertyPatch = createPropertyPatch(singletonMap(UNKNOWN_PROPERTY_NAME, propertyValue));
        digitalTwinServiceClient.updateDigitalTwinProperties(testDevice.getDeviceId(), TEST_COMPONENT_NAME, propertyPatch);

        JsonNode expectedValue = objectMapper.readTree(String.format(PROPERTY_VALUE_PATTERN, propertyValue));
        assertThat(testComponent.verifyIfPropertyUpdateWasReceived(UNKNOWN_PROPERTY_NAME, expectedValue)).as("Verify that device did not receive the property update from service").isFalse();
    }

    @Ignore("Disabled until service validates and throws exception")
    @Test
    public void testUpdateSingleReadonlyPropertyFromService() throws IOException {
        assertThat(digitalTwinDeviceClient.subscribeForProperties()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        String randomUuid = UUID.randomUUID().toString();
        String propertyValue = SERVICE_PROPERTY_UPDATE_PREFIX.concat(randomUuid);
        String propertyPatch = createPropertyPatch(singletonMap(PROPERTY_NAME_READONLY, propertyValue));
        String digitalTwin = digitalTwinServiceClient.updateDigitalTwinProperties(testDevice.getDeviceId(), TEST_COMPONENT_NAME, propertyPatch);

        JsonNode expectedValue = objectMapper.readTree(String.format(PROPERTY_VALUE_PATTERN, propertyValue));
        assertThat(testComponent.verifyIfPropertyUpdateWasReceived(PROPERTY_NAME_READONLY, expectedValue)).as("Verify that device did not receive the property update from service").isFalse();

        // Assert that property is not updated in the twin
        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        JsonNode updatedProperty = getPropertyJsonNodeFromTwin(digitalTwin, TEST_COMPONENT_NAME);
        assertThat(updatedProperty.has(PROPERTY_NAME_READONLY)).isTrue();
        String updatePropertyValue = getPropertyValueFromTwin(updatedProperty, PROPERTY_NAME_READONLY);
        assertThat(updatePropertyValue).isNotEqualTo(propertyValue);
    }

    @Test @Ignore
    public void testUpdateSingleWritablePropertyFromDevice() {
        assertThat(digitalTwinDeviceClient.subscribeForProperties()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        String propertyValue = DEVICE_PROPERTY_UPDATE_PREFIX.concat(UUID.randomUUID().toString());
        DigitalTwinClientResult updateResult = testComponent.updatePropertyFromDevice(PROPERTY_NAME_WRITABLE, propertyValue).blockingGet();

        assertThat(updateResult).as("Verify that device sent the property update to service").isEqualTo(DIGITALTWIN_CLIENT_OK);
    }

    @Test @Ignore
    public void testUpdatePropertyFromDevice() {
        assertThat(digitalTwinDeviceClient.subscribeForProperties()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        String propertyValue = DEVICE_PROPERTY_UPDATE_PREFIX.concat(UUID.randomUUID().toString());
        DigitalTwinClientResult updateResult = testComponent.updatePropertyFromDevice(PROPERTY_NAME_WRITABLE, propertyValue).blockingGet();

        assertThat(updateResult).as("Verify that device sent the property update to service").isEqualTo(DIGITALTWIN_CLIENT_OK);
    }

    // TODO (Java only limitation) subscribeForProperties should be called before syncupProperties.
    @Test @Ignore
    public void testUpdatePropertyWithoutSubscribe() {
        String propertyValue = DEVICE_PROPERTY_UPDATE_PREFIX.concat(UUID.randomUUID().toString());
        try {
            testComponent.updatePropertyFromDevice(PROPERTY_NAME_WRITABLE, propertyValue).blockingGet();
            fail("Should throw exception.");
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            assertThat(cause).isInstanceOf(IOException.class);
            assertThat(cause.getMessage()).contains("Start twin before using it");
        }
    }

    private static JsonNode getPropertyJsonNodeFromTwin(String digitalTwin, String componentName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode digitalTwinObject = mapper.readTree(digitalTwin);
        return digitalTwinObject.get("interfaces").get(componentName).get("properties");
    }

    private static String getPropertyValueFromTwin(JsonNode propertyJsonNode, String propertyName) {
        return propertyJsonNode.get(propertyName).get("desired").get("value").asText();
    }

    @After
    public void tearDownTest() {
        if (testDevice != null) {
            testDevice.closeAndDeleteDevice();
        }
    }
}
