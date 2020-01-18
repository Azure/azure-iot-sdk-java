// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestDigitalTwinDevice;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent1;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.UnpublishedComponent;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT_WS;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_REGISTRATION_PENDING;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.COMPONENT_KEY;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.DCM_ID;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.DEFAULT_IMPLEMENTED_MODEL_INFORMATION_COMPONENT_NAME;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.MAX_WAIT_TIME_FOR_ASYNC_CALL_IN_SECONDS;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.MODEL_DISCOVERY_MODEL_NAME;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.PROPERTY_KEY;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.REPORTED_KEY;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.convertJsonStringToJsonNode;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveEnvironmentVariableValue;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveComponentNameFromInterfaceId;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class DigitalTwinRegisterComponentsE2ETests {
    private static final String IOT_HUB_CONNECTION_STRING = retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static final String TEST_COMPONENT_NAME_1 = retrieveComponentNameFromInterfaceId(TestComponent1.TEST_INTERFACE_ID);
    private static final String TEST_COMPONENT_NAME_2 = retrieveComponentNameFromInterfaceId(TestComponent2.TEST_INTERFACE_ID);
    private static final String UNPUBLISHED_COMPONENT_NAME = retrieveComponentNameFromInterfaceId(UnpublishedComponent.TEST_INTERFACE_ID);
    private static final String INVALID_COMPONENT_NAME = "invalidComponentName";

    private static final String DEVICE_ID_PREFIX = "DigitalTwinRegisterComponentsE2ETests_";
    private static final String DIGITAL_TWIN_INTERFACE_PATTERN = "\"%s\":\"%s\"";

    private static DigitalTwinServiceClient digitalTwinServiceClient;
    private String digitalTwinId;
    private TestDigitalTwinDevice testDevice;
    private TestComponent1 testComponent1;
    private TestComponent2 testComponent2;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(5 * 60); // 5 minutes max per method tested

    @Parameterized.Parameter(0)
    public IotHubClientProtocol protocol;

    @Parameterized.Parameters(name = "{index}: Register components Test: protocol={0}")
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
    }

    @Test
    public void testRegisterSingleComponentSuccess() throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        testComponent2 = new TestComponent2(TEST_COMPONENT_NAME_2);

        assertThat(digitalTwinDeviceClient.bindComponents(singletonList(testComponent2))).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(digitalTwinDeviceClient.registerComponents()).isEqualTo(DIGITALTWIN_CLIENT_OK);

        // assert that the registered component is returned in the DigitalTwin
        String digitalTwin = digitalTwinServiceClient.getDigitalTwin(digitalTwinId);
        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        assertThat(digitalTwin).contains(String.format(DIGITAL_TWIN_INTERFACE_PATTERN, TEST_COMPONENT_NAME_2, TestComponent2.TEST_INTERFACE_ID));
    }

    @Test
    public void testRegisterMultipleComponentsSuccess() throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        testComponent1 = new TestComponent1(TEST_COMPONENT_NAME_1);
        testComponent2 = new TestComponent2(TEST_COMPONENT_NAME_2);
        assertThat(digitalTwinDeviceClient.bindComponents(asList(testComponent1, testComponent2))).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(digitalTwinDeviceClient.registerComponents()).isEqualTo(DIGITALTWIN_CLIENT_OK);

        // assert that the registered component is returned in the DigitalTwin
        String digitalTwin = digitalTwinServiceClient.getDigitalTwin(digitalTwinId);
        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        assertThat(digitalTwin).contains(String.format(DIGITAL_TWIN_INTERFACE_PATTERN, TEST_COMPONENT_NAME_1, TestComponent1.TEST_INTERFACE_ID));
        assertThat(digitalTwin).contains(String.format(DIGITAL_TWIN_INTERFACE_PATTERN, TEST_COMPONENT_NAME_2, TestComponent2.TEST_INTERFACE_ID));
    }

    @Test
    public void testRegisterComponentsMultipleTimesSequentially() throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        testComponent1 = new TestComponent1(TEST_COMPONENT_NAME_1);
        testComponent2 = new TestComponent2(TEST_COMPONENT_NAME_2);
        assertThat(digitalTwinDeviceClient.bindComponents(asList(testComponent1, testComponent2))).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(digitalTwinDeviceClient.registerComponents()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(digitalTwinDeviceClient.registerComponents()).isEqualTo(DIGITALTWIN_CLIENT_OK);

        // assert that the registered component is returned in the DigitalTwin
        String digitalTwin = digitalTwinServiceClient.getDigitalTwin(digitalTwinId);
        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        assertThat(digitalTwin).contains(String.format(DIGITAL_TWIN_INTERFACE_PATTERN, TEST_COMPONENT_NAME_2, TestComponent2.TEST_INTERFACE_ID));
    }

    @Test
    public void testRegisterComponentsMultipleTimesInParallel() throws IotHubException, IOException, URISyntaxException, InterruptedException {
        final Semaphore semaphore = new Semaphore(0);
        final List<DigitalTwinClientResult> registrationResults = synchronizedList(new ArrayList<>());

        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        testComponent1 = new TestComponent1(TEST_COMPONENT_NAME_1);
        testComponent2 = new TestComponent2(TEST_COMPONENT_NAME_2);
        assertThat(digitalTwinDeviceClient.bindComponents(asList(testComponent1, testComponent2))).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Disposable disposable1 = digitalTwinDeviceClient.registerComponentsAsync()
                .subscribe(digitalTwinClientResult -> {
                    registrationResults.add(digitalTwinClientResult);
                    semaphore.release();
                });
        Disposable disposable2 = digitalTwinDeviceClient.registerComponentsAsync()
                .subscribe(digitalTwinClientResult -> {
                    registrationResults.add(digitalTwinClientResult);
                    semaphore.release();
                });

        assertThat(semaphore.tryAcquire(2, MAX_WAIT_TIME_FOR_ASYNC_CALL_IN_SECONDS, SECONDS)).as("Timeout executing Async call").isTrue();
        disposable1.dispose();
        disposable2.dispose();

        assertThat(registrationResults.get(0)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(registrationResults.get(1)).isEqualTo(DIGITALTWIN_CLIENT_OK);
    }

    // No error thrown, unpublished interface registered
    @Ignore("Disabled until service validates and throws exception")
    @Test
    public void testRegisterComponentNotPublishedInRepository() throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        UnpublishedComponent unpublishedComponent = new UnpublishedComponent(UNPUBLISHED_COMPONENT_NAME);
        assertThat(digitalTwinDeviceClient.bindComponents(singletonList(unpublishedComponent))).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(digitalTwinDeviceClient.registerComponents()).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
    }

    // No error thrown, interface registered with the invalid name
    @Ignore("Disabled until service validates and throws exception")
    @Test
    public void testRegisterComponentsWithDifferentComponentName() throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        TestComponent1 testComponent = new TestComponent1(INVALID_COMPONENT_NAME);
        assertThat(digitalTwinDeviceClient.bindComponents(singletonList(testComponent))).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(digitalTwinDeviceClient.registerComponents()).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
    }

    // No error thrown, no interface registered
    @Ignore("Disabled until service validates and throws exception")
    @Test
    public void testRegisterEmptyListOfComponents() throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        assertThat(digitalTwinDeviceClient.registerComponents()).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
    }

    @After
    public void tearDownTest() {
        if (testDevice != null) {
            testDevice.closeAndDeleteDevice();
        }
    }
}
