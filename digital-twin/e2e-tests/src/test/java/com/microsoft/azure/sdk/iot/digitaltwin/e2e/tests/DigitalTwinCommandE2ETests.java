// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.EventHubListener;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestDigitalTwinDevice;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance1;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveInterfaceNameFromInterfaceId;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Slf4j
public class DigitalTwinCommandE2ETests {
    private static final String IOTHUB_CONNECTION_STRING = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.IOTHUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static final String DCM_ID = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.DCM_ID_ENV_VAR_NAME);
    private static final String TEST_INTERFACE_INSTANCE_NAME_1 = retrieveInterfaceNameFromInterfaceId(TestInterfaceInstance1.TEST_INTERFACE_ID);
    private static final String TEST_INTERFACE_INSTANCE_NAME_2 = retrieveInterfaceNameFromInterfaceId(TEST_INTERFACE_ID);

    private static final String DEVICE_ID_PREFIX = "DigitalTwinCommandE2ETests_";
    private static final int MAX_THREADS_MULTITHREADED_TEST = 5;

    private static final String SAMPLE_COMMAND_PAYLOAD = "samplePayload";
    private static final String INVALID_INTERFACE_INSTANCE_NAME = "invalidInterfaceInstanceName";
    private static final String INVALID_COMMAND_NAME = "invalidCommandName";
    private static final String INTERFACE_INSTANCE_NOT_FOUND_MESSAGE_PATTERN = "Interface instance [%s] not found.";
    private static final String ASYNC_COMMAND_RESPONSE_PATTERN = "Progress of %s: %d";

    private static DigitalTwinServiceClient digitalTwinServiceClient;
    private String digitalTwinId;
    private TestDigitalTwinDevice testDevice;
    private static EventHubListener eventHubListener;

    @BeforeAll
    public static void setUp() throws IOException, EventHubException, ExecutionException, InterruptedException {
        digitalTwinServiceClient = DigitalTwinServiceClientImpl.buildFromConnectionString()
                                                               .connectionString(IOTHUB_CONNECTION_STRING)
                                                               .build();

        eventHubListener = EventHubListener.getInstance();
        eventHubListener.startReceivingEvents();
    }

    @ParameterizedTest(name = "{index}: Invoke sync command with payload: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testDeviceClientReceivesSyncCommandWithPayloadAndResponds(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        registerDigitalTwinInterface(testDevice);

        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME_2, SYNC_COMMAND_WITH_PAYLOAD, SAMPLE_COMMAND_PAYLOAD);

        assertAll("Command is not invoked",
                () -> assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull(),
                () -> assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_COMPLETED),
                () -> assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull(),
                () -> assertThat(commandResponse.getPayload()).isEqualTo(SAMPLE_COMMAND_PAYLOAD));
    }

    @ParameterizedTest(name = "{index}: Invoke sync command without payload: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testDeviceClientReceivesSyncCommandWithoutPayloadAndResponds(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        registerDigitalTwinInterface(testDevice);

        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME_2, SYNC_COMMAND_WITHOUT_PAYLOAD);

        assertAll("Command is not invoked",
                () -> assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull(),
                () -> assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_COMPLETED),
                () -> assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull());
    }

    @ParameterizedTest(name = "{index}: Invoke async command with payload: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testDeviceClientReceivesAsyncCommandWithPayloadAndResponds(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        registerDigitalTwinInterface(testDevice);

        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME_2, ASYNC_COMMAND_WITH_PAYLOAD, SAMPLE_COMMAND_PAYLOAD);

        assertAll("Command is not invoked",
                () -> assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull(),
                () -> assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_PENDING),
                () -> assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull(),
                () -> assertThat(commandResponse.getPayload()).isEqualTo(SAMPLE_COMMAND_PAYLOAD));

        // Verify that async command progress is sent to IoTHub
        String expectedPayload = String.format(ASYNC_COMMAND_RESPONSE_PATTERN, ASYNC_COMMAND_WITH_PAYLOAD, 100);
        assertThat(eventHubListener.verifyThatMessageWasReceived(digitalTwinId, expectedPayload)).as("Async command progress sent to IoTHub").isTrue();
    }

    @ParameterizedTest(name = "{index}: Invoke async command with payload: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testDeviceClientReceivesAsyncCommandWithoutPayloadAndResponds(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        registerDigitalTwinInterface(testDevice);

        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME_2, ASYNC_COMMAND_WITH_PAYLOAD);

        assertAll("Command is not invoked",
                () -> assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull(),
                () -> assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_PENDING),
                () -> assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull());

        // Verify that async command progress is sent to IoTHub
        String expectedPayload = String.format(ASYNC_COMMAND_RESPONSE_PATTERN, ASYNC_COMMAND_WITH_PAYLOAD, 100);
        assertThat(eventHubListener.verifyThatMessageWasReceived(digitalTwinId, expectedPayload)).as("Async command progress sent to IoTHub").isTrue();
    }

    @ParameterizedTest(name = "{index}: Invoke command with invalid interface instance name: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testInvokeCommandInvalidInterfaceInstanceName(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        registerDigitalTwinInterface(testDevice);

        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, INVALID_INTERFACE_INSTANCE_NAME, SYNC_COMMAND_WITHOUT_PAYLOAD);

        assertAll("Expected command response is not returned for invalid interface name",
                () -> assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull(),
                () -> assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_NOT_IMPLEMENTED),
                () -> assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull(),
                () -> assertThat(commandResponse.getPayload()).isEqualTo(String.format(INTERFACE_INSTANCE_NOT_FOUND_MESSAGE_PATTERN, INVALID_INTERFACE_INSTANCE_NAME)));
    }

    @ParameterizedTest(name = "{index}: Invoke command with invalid command name: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testInvokeCommandInvalidCommandName(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        registerDigitalTwinInterface(testDevice);

        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME_2, INVALID_COMMAND_NAME);

        assertAll("Expected command response is not returned for invalid command name",
                () -> assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull(),
                () -> assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_NOT_IMPLEMENTED),
                () -> assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull(),
                () -> assertThat(commandResponse.getPayload()).isEqualTo(String.format(COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN, INVALID_COMMAND_NAME, TestInterfaceInstance2.TEST_INTERFACE_ID)));
    }

    @ParameterizedTest(name = "{index}: Invoke command on multiple interfaces: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testSameCommandNameOnMultipleRegisteredInterfacesSuccess(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        TestInterfaceInstance1 testInterfaceInstance1 = new TestInterfaceInstance1(TEST_INTERFACE_INSTANCE_NAME_1);
        TestInterfaceInstance2 testInterfaceInstance2 = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME_2);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, asList(testInterfaceInstance1, testInterfaceInstance2)).blockingGet();
        assertThat(registrationResult).isEqualTo(DIGITALTWIN_CLIENT_OK);

        String samplePayload1 = "samplePayload1";
        String samplePayload2 = "samplePayload2";
        DigitalTwinCommandResponse commandResponse1 = digitalTwinServiceClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME_1, TestInterfaceInstance1.SYNC_COMMAND_WITH_PAYLOAD, samplePayload1);
        DigitalTwinCommandResponse commandResponse2 = digitalTwinServiceClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME_2, SYNC_COMMAND_WITH_PAYLOAD, samplePayload2);

        assertAll("Command1 is not invoked",
                () -> assertThat(commandResponse1).as("Verify Command Invocation Response").isNotNull(),
                () -> assertThat(commandResponse1.getStatus()).isEqualTo(STATUS_CODE_COMPLETED),
                () -> assertThat(commandResponse1.getPayload()).isEqualTo(samplePayload1),
                () -> assertThat(commandResponse1.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull());

        assertAll("Command2 is not invoked",
                () -> assertThat(commandResponse2).as("Verify Command Invocation Response").isNotNull(),
                () -> assertThat(commandResponse2.getStatus()).isEqualTo(STATUS_CODE_COMPLETED),
                () -> assertThat(commandResponse2.getPayload()).isEqualTo(samplePayload2),
                () -> assertThat(commandResponse2.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull());
    }

    @ParameterizedTest(name = "{index}: Invoke command by multiple threads: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testSyncCommandInvocationMultithreaded(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        registerDigitalTwinInterface(testDevice);

        List<String> payloadTextList = new Random().ints(MAX_THREADS_MULTITHREADED_TEST).boxed()
                                                   .map(Object :: toString)
                                                   .collect(Collectors.toList());
        List<DigitalTwinCommandResponse> commandResponses = Flowable.range(0, MAX_THREADS_MULTITHREADED_TEST)
                                                                    .parallel()
                                                                    .runOn(Schedulers.io())
                                                                    .map(integer -> digitalTwinServiceClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME_2, SYNC_COMMAND_WITH_PAYLOAD, payloadTextList.get(integer)))
                                                                    .sequential()
                                                                    .toList()
                                                                    .blockingGet();

        for (int i = 0; i < MAX_THREADS_MULTITHREADED_TEST; i++) {
            DigitalTwinCommandResponse commandResponse = commandResponses.get(i);
            assertAll("Command is not invoked",
                    () -> assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull(),
                    () -> assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_COMPLETED),
                    () -> assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull(),
                    () -> assertThat(payloadTextList).contains(commandResponse.getPayload()));
            payloadTextList.remove(commandResponse.getPayload());
        }
        assertThat(payloadTextList).as("All sent commands were received").isEmpty();
    }

    @ParameterizedTest(name = "{index}: Invoke command by multiple threads: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testAsyncCommandInvocationMultithreaded(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        registerDigitalTwinInterface(testDevice);

        List<String> payloadTextList = new Random().ints(MAX_THREADS_MULTITHREADED_TEST).boxed()
                                                   .map(Object :: toString)
                                                   .collect(Collectors.toList());
        List<DigitalTwinCommandResponse> commandResponses = Flowable.range(0, MAX_THREADS_MULTITHREADED_TEST)
                                                                    .parallel()
                                                                    .runOn(Schedulers.io())
                                                                    .map(integer -> digitalTwinServiceClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME_2, ASYNC_COMMAND_WITH_PAYLOAD, payloadTextList.get(integer)))
                                                                    .sequential()
                                                                    .toList()
                                                                    .blockingGet();

        for (int i = 0; i < MAX_THREADS_MULTITHREADED_TEST; i++) {
            DigitalTwinCommandResponse commandResponse = commandResponses.get(i);
            assertAll("Command is not invoked",
                    () -> assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull(),
                    () -> assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_PENDING),
                    () -> assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull(),
                    () -> assertThat(payloadTextList).contains(commandResponse.getPayload()));
            payloadTextList.remove(commandResponse.getPayload());
        }
        assertThat(payloadTextList).as("All sent commands were received").isEmpty();

        // Verify that async command progress is sent to IoTHub
        for (int i = 0; i < MAX_THREADS_MULTITHREADED_TEST; i++) {
            String expectedPayload = String.format(ASYNC_COMMAND_RESPONSE_PATTERN, ASYNC_COMMAND_WITH_PAYLOAD, 100);
            assertThat(eventHubListener.verifyThatMessageWasReceived(digitalTwinId, expectedPayload)).as("Async command progress sent to IoTHub").isTrue();
        }
    }

    @AfterEach
    public void tearDownTest() {
        testDevice.closeAndDeleteDevice();
    }

    private void registerDigitalTwinInterface(TestDigitalTwinDevice testDevice) {
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        TestInterfaceInstance2 testInterfaceInstance = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME_2);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance)).blockingGet();
        assertThat(registrationResult).isEqualTo(DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK);
    }
}
