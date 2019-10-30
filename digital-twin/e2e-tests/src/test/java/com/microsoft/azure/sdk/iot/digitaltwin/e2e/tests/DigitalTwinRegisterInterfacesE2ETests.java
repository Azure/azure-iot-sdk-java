// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestDigitalTwinDevice;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance1;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.UnpublishedInterfaceInstance;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.*;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveInterfaceNameFromInterfaceId;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TEST_INTERFACE_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class DigitalTwinRegisterInterfacesE2ETests {
    private static final String IOTHUB_CONNECTION_STRING = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.IOTHUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static final String DCM_ID = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.DCM_ID_ENV_VAR_NAME);
    private static final String TEST_INTERFACE_INSTANCE_NAME_1 = retrieveInterfaceNameFromInterfaceId(TestInterfaceInstance1.TEST_INTERFACE_ID);
    private static final String TEST_INTERFACE_INSTANCE_NAME_2 = retrieveInterfaceNameFromInterfaceId(TEST_INTERFACE_ID);
    private static final String UNPUBLISHED_INTERFACE_INSTANCE_NAME = retrieveInterfaceNameFromInterfaceId(UnpublishedInterfaceInstance.TEST_INTERFACE_ID);
    private static final String INVALID_INTERFACE_INSTANCE_NAME = "invalidInterfaceInstanceName";

    private static final String DEVICE_ID_PREFIX = "DigitalTwinRegisterInterfacesE2ETests_";
    private static final String DIGITALTWIN_INTERFACE_PATTERN = "\"%s\":\"%s\"";

    private static DigitalTwinServiceClient digitalTwinServiceClient;
    private String digitalTwinId;
    private TestDigitalTwinDevice testDevice;
    private TestInterfaceInstance1 testInterfaceInstance1;
    private TestInterfaceInstance2 testInterfaceInstance2;

    @BeforeAll
    public static void setUp() {
        digitalTwinServiceClient = DigitalTwinServiceClientImpl.buildFromConnectionString()
                                                                .connectionString(IOTHUB_CONNECTION_STRING)
                                                                .build();
    }

    @ParameterizedTest(name = "{index}: MRegister single interface: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testRegisterSingleInterfaceSuccess(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        testInterfaceInstance2 = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME_2);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance2)).blockingGet();
        assertThat(registrationResult).isEqualTo(DIGITALTWIN_CLIENT_OK);

        // assert that the registered interface is returned in the DigitalTwin
        String digitalTwin = digitalTwinServiceClient.getDigitalTwin(digitalTwinId);
        assertAll("Expected DigitalTwin is not returned" ,
                () -> assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull(),
                () -> assertThat(digitalTwin).contains(String.format(DIGITALTWIN_INTERFACE_PATTERN, TEST_INTERFACE_INSTANCE_NAME_2, TEST_INTERFACE_ID)));
    }

    @ParameterizedTest(name = "{index}: Register multiple interfaces: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testRegisterMultipleInterfacesSuccess(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        testInterfaceInstance1 = new TestInterfaceInstance1(TEST_INTERFACE_INSTANCE_NAME_1);
        testInterfaceInstance2 = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME_2);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, asList(testInterfaceInstance1, testInterfaceInstance2)).blockingGet();
        assertThat(registrationResult).isEqualTo(DIGITALTWIN_CLIENT_OK);

        // assert that the registered interface is returned in the DigitalTwin
        String digitalTwin = digitalTwinServiceClient.getDigitalTwin(digitalTwinId);
        assertAll("Expected DigitalTwin is not returned" ,
                () -> assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull(),
                () -> assertThat(digitalTwin).contains(String.format(DIGITALTWIN_INTERFACE_PATTERN, TEST_INTERFACE_INSTANCE_NAME_1, TestInterfaceInstance1.TEST_INTERFACE_ID)),
                () -> assertThat(digitalTwin).contains(String.format(DIGITALTWIN_INTERFACE_PATTERN, TEST_INTERFACE_INSTANCE_NAME_2, TEST_INTERFACE_ID)));
    }

    @ParameterizedTest(name = "{index}: Register interfaces multiple times sequentially: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testRegisterInterfacesMultipleTimesSequentially(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        testInterfaceInstance2 = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME_2);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance2)).blockingGet();
        assertThat(registrationResult).isEqualTo(DIGITALTWIN_CLIENT_OK);

        // assert that the registered interface is returned in the DigitalTwin
        String digitalTwin = digitalTwinServiceClient.getDigitalTwin(digitalTwinId);
        assertAll("Expected DigitalTwin is not returned" ,
                () -> assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull(),
                () -> assertThat(digitalTwin).contains(String.format(DIGITALTWIN_INTERFACE_PATTERN, TEST_INTERFACE_INSTANCE_NAME_2, TEST_INTERFACE_ID)));

        testInterfaceInstance1 = new TestInterfaceInstance1(TEST_INTERFACE_INSTANCE_NAME_1);
        DigitalTwinClientResult registrationResult2 = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance1)).blockingGet();
        assertThat(registrationResult2).isEqualTo(DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_INTERFACE_ALREADY_REGISTERED);
    }

    @ParameterizedTest(name = "{index}: Register interfaces multiple times in parallel: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testRegisterInterfacesMultipleTimesInParallel(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        testInterfaceInstance1 = new TestInterfaceInstance1(TEST_INTERFACE_INSTANCE_NAME_1);
        testInterfaceInstance2 = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME_2);

        Single<DigitalTwinClientResult> registrationResult1 = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance1));
        Single<DigitalTwinClientResult> registrationResult2 = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance2));

        List<DigitalTwinClientResult> registrationResults = Flowable.fromArray(registrationResult1, registrationResult2)
                                                                    .parallel()
                                                                    .runOn(Schedulers.io())
                                                                    .map(Single :: blockingGet)
                                                                    .sequential()
                                                                    .toList()
                                                                    .blockingGet();

        // The first call for register interfaces will be enqueued. The next call for register interfaces will immediately return "DIGITALTWIN_CLIENT_ERROR_REGISTRATION_PENDING".
        // Once registration completes successfully, the first call will return "DIGITALTWIN_CLIENT_OK".
        assertThat(registrationResults.get(0)).isEqualTo(DIGITALTWIN_CLIENT_ERROR_REGISTRATION_PENDING);
        assertThat(registrationResults.get(1)).isEqualTo(DIGITALTWIN_CLIENT_OK);
    }

    @ParameterizedTest(name = "{index}: Re-register interface after device client close: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testRegisterInterfaceAfterDeviceClientClose(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        testInterfaceInstance2 = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME_2);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance2)).blockingGet();
        assertThat(registrationResult).isEqualTo(DIGITALTWIN_CLIENT_OK);

        // assert that the registered interface is returned in the DigitalTwin
        String digitalTwin = digitalTwinServiceClient.getDigitalTwin(digitalTwinId);
        assertAll("Expected DigitalTwin is not returned" ,
                () -> assertThat(digitalTwin).as("Verify DigitalTwin").isNotNull(),
                () -> assertThat(digitalTwin).contains(String.format(DIGITALTWIN_INTERFACE_PATTERN, TEST_INTERFACE_INSTANCE_NAME_2, TEST_INTERFACE_ID)));

        // close the device client instance
        testDevice.getDeviceClient().closeNow();

        // Re-register another interface
        testInterfaceInstance1 = new TestInterfaceInstance1(TEST_INTERFACE_INSTANCE_NAME_1);
        DigitalTwinClientResult registrationResult2 = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance1)).blockingGet();
        assertThat(registrationResult2).isEqualTo(DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_INTERFACE_ALREADY_REGISTERED);
    }

    // No error thrown, unpublished interface registered
    @Disabled("Disabled until service validates and throws exception")
    @ParameterizedTest(name = "{index}: Register unpublished interface: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testRegisterInterfaceNotPublishedInRepository(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        UnpublishedInterfaceInstance unpublishedInterfaceInstance = new UnpublishedInterfaceInstance(UNPUBLISHED_INTERFACE_INSTANCE_NAME);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(unpublishedInterfaceInstance)).blockingGet();
        assertThat(registrationResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
    }

    // No error thrown, interface registered with the invalid name
    @Disabled("Disabled until service validates and throws exception")
    @ParameterizedTest(name = "{index}: Register interface with incorrect instance name: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testRegisterInterfaceWithDifferentInstanceName(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        TestInterfaceInstance1 testInterfaceInstance = new TestInterfaceInstance1(INVALID_INTERFACE_INSTANCE_NAME);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance)).blockingGet();
        assertThat(registrationResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
    }

    // No error thrown, no interface registered
    @Disabled("Disabled until service validates and throws exception")
    @ParameterizedTest(name = "{index}: Register interface with incorrect instance name: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testRegisterEmptyListOfInterfaces(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, new ArrayList<>()).blockingGet();
        assertThat(registrationResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
    }

    @AfterEach
    public void tearDownTest() throws IOException, IotHubException {
        testDevice.closeAndDeleteDevice();
    }

}
