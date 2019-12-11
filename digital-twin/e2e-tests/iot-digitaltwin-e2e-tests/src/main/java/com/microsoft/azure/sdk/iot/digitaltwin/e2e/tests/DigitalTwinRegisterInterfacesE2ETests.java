// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestDigitalTwinDevice;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance1;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.UnpublishedInterfaceInstance;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
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
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.*;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.*;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveEnvironmentVariableValue;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveInterfaceNameFromInterfaceId;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class DigitalTwinRegisterInterfacesE2ETests {
    private static final String IOT_HUB_CONNECTION_STRING = retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static final String TEST_INTERFACE_INSTANCE_NAME_1 = retrieveInterfaceNameFromInterfaceId(TestInterfaceInstance1.TEST_INTERFACE_ID);
    private static final String TEST_INTERFACE_INSTANCE_NAME_2 = retrieveInterfaceNameFromInterfaceId(TestInterfaceInstance2.TEST_INTERFACE_ID);
    private static final String UNPUBLISHED_INTERFACE_INSTANCE_NAME = retrieveInterfaceNameFromInterfaceId(UnpublishedInterfaceInstance.TEST_INTERFACE_ID);
    private static final String INVALID_INTERFACE_INSTANCE_NAME = "invalidInterfaceInstanceName";

    private static final String DEVICE_ID_PREFIX = "DigitalTwinRegisterInterfacesE2ETests_";
    private static final String DIGITAL_TWIN_INTERFACE_PATTERN = "\"%s\":\"%s\"";

    private static DigitalTwinServiceClient digitalTwinServiceClient;
    private String digitalTwinId;
    private TestDigitalTwinDevice testDevice;
    private TestInterfaceInstance1 testInterfaceInstance1;
    private TestInterfaceInstance2 testInterfaceInstance2;

    @Parameterized.Parameter(0)
    public IotHubClientProtocol protocol;

    @Parameterized.Parameters(name = "{index}: Register interfaces Test: protocol={0}")
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
    public void testRegisterSingleInterfaceSuccess() throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        testInterfaceInstance2 = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME_2);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance2)).blockingGet();
        assertThat(registrationResult).isEqualTo(DIGITALTWIN_CLIENT_OK);

        // assert that the registered interface is returned in the DigitalTwin
        String digitalTwin = digitalTwinServiceClient.getDigitalTwin(digitalTwinId);
        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        assertThat(digitalTwin).contains(String.format(DIGITAL_TWIN_INTERFACE_PATTERN, TEST_INTERFACE_INSTANCE_NAME_2, TestInterfaceInstance2.TEST_INTERFACE_ID));
    }

    @Test
    public void testRegisterMultipleInterfacesSuccess() throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        testInterfaceInstance1 = new TestInterfaceInstance1(TEST_INTERFACE_INSTANCE_NAME_1);
        testInterfaceInstance2 = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME_2);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, asList(testInterfaceInstance1, testInterfaceInstance2)).blockingGet();
        assertThat(registrationResult).isEqualTo(DIGITALTWIN_CLIENT_OK);

        // assert that the registered interface is returned in the DigitalTwin
        String digitalTwin = digitalTwinServiceClient.getDigitalTwin(digitalTwinId);
        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        assertThat(digitalTwin).contains(String.format(DIGITAL_TWIN_INTERFACE_PATTERN, TEST_INTERFACE_INSTANCE_NAME_1, TestInterfaceInstance1.TEST_INTERFACE_ID));
        assertThat(digitalTwin).contains(String.format(DIGITAL_TWIN_INTERFACE_PATTERN, TEST_INTERFACE_INSTANCE_NAME_2, TestInterfaceInstance2.TEST_INTERFACE_ID));
    }

    @Test
    public void testRegisterInterfacesMultipleTimesSequentially() throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        testInterfaceInstance2 = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME_2);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance2)).blockingGet();
        assertThat(registrationResult).isEqualTo(DIGITALTWIN_CLIENT_OK);

        // assert that the registered interface is returned in the DigitalTwin
        String digitalTwin = digitalTwinServiceClient.getDigitalTwin(digitalTwinId);
        assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull();
        assertThat(digitalTwin).contains(String.format(DIGITAL_TWIN_INTERFACE_PATTERN, TEST_INTERFACE_INSTANCE_NAME_2, TestInterfaceInstance2.TEST_INTERFACE_ID));

        testInterfaceInstance1 = new TestInterfaceInstance1(TEST_INTERFACE_INSTANCE_NAME_1);
        DigitalTwinClientResult registrationResult2 = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance1)).blockingGet();
        assertThat(registrationResult2).isEqualTo(DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_INTERFACE_ALREADY_REGISTERED);
    }

    @Test
    public void testRegisterInterfacesMultipleTimesInParallel() throws IotHubException, IOException, URISyntaxException, InterruptedException {
        final Semaphore semaphore = new Semaphore(0);
        final List<DigitalTwinClientResult> registrationResults = synchronizedList(new ArrayList<>());

        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        testInterfaceInstance1 = new TestInterfaceInstance1(TEST_INTERFACE_INSTANCE_NAME_1);
        testInterfaceInstance2 = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME_2);

        Disposable disposable1 = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance1))
                .subscribe(digitalTwinClientResult -> {
                    registrationResults.add(digitalTwinClientResult);
                    semaphore.release();
                });
        Disposable disposable2 = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance2))
                .subscribe(digitalTwinClientResult -> {
                    registrationResults.add(digitalTwinClientResult);
                    semaphore.release();
                });

        assertThat(semaphore.tryAcquire(2, MAX_WAIT_TIME_FOR_ASYNC_CALL_IN_SECONDS, SECONDS)).as("Timeout executing Async call").isTrue();
        disposable1.dispose();
        disposable2.dispose();

        // The first call for register interfaces will be enqueued. The next call for register interfaces will immediately return "DIGITALTWIN_CLIENT_ERROR_REGISTRATION_PENDING".
        // Once registration completes successfully, the first call will return "DIGITALTWIN_CLIENT_OK".
        assertThat(registrationResults.get(0)).isEqualTo(DIGITALTWIN_CLIENT_ERROR_REGISTRATION_PENDING);
        assertThat(registrationResults.get(1)).isEqualTo(DIGITALTWIN_CLIENT_OK);
    }

    // No error thrown, unpublished interface registered
    @Ignore("Disabled until service validates and throws exception")
    @Test
    public void testRegisterInterfaceNotPublishedInRepository() throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        UnpublishedInterfaceInstance unpublishedInterfaceInstance = new UnpublishedInterfaceInstance(UNPUBLISHED_INTERFACE_INSTANCE_NAME);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(unpublishedInterfaceInstance)).blockingGet();
        assertThat(registrationResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
    }

    // No error thrown, interface registered with the invalid name
    @Ignore("Disabled until service validates and throws exception")
    @Test
    public void testRegisterInterfaceWithDifferentInstanceName() throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        TestInterfaceInstance1 testInterfaceInstance = new TestInterfaceInstance1(INVALID_INTERFACE_INSTANCE_NAME);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance)).blockingGet();
        assertThat(registrationResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
    }

    // No error thrown, no interface registered
    @Ignore("Disabled until service validates and throws exception")
    @Test
    public void testRegisterEmptyListOfInterfaces() throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, new ArrayList<>()).blockingGet();
        assertThat(registrationResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
    }

    @After
    public void tearDownTest() {
        if (testDevice != null) {
            testDevice.closeAndDeleteDevice();
        }
    }
}
