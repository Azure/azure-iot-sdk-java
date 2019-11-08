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
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceAsyncClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceAsyncClientImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT_WS;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveInterfaceNameFromInterfaceId;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.EventHubListener.verifyThatMessageWasReceived;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.synchronizedList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class DigitalTwinCommandE2ETests {
    private static final String IOT_HUB_CONNECTION_STRING = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static final String DCM_ID = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.DCM_ID_ENV_VAR_NAME);
    private static final String TEST_INTERFACE_INSTANCE_NAME_1 = retrieveInterfaceNameFromInterfaceId(TestInterfaceInstance1.TEST_INTERFACE_ID);
    private static final String TEST_INTERFACE_INSTANCE_NAME_2 = retrieveInterfaceNameFromInterfaceId(TEST_INTERFACE_ID);

    private static final String DEVICE_ID_PREFIX = "DigitalTwinCommandE2ETests_";
    private static final int MAX_THREADS_MULTITHREADED_TEST = 5;

    private static final String SAMPLE_COMMAND_PAYLOAD = "samplePayload";
    private static final String INVALID_INTERFACE_INSTANCE_NAME = "invalidInterfaceInstanceName";
    private static final String INVALID_COMMAND_NAME = "invalidCommandName";
    private static final String INTERFACE_INSTANCE_NOT_FOUND_MESSAGE_PATTERN = "Interface instance [%s] not found.";
    private static final String ASYNC_COMMAND_COMPLETED_MESSAGE_FORMAT = "Progress of %s [\"%s\"]: COMPLETED";

    private static DigitalTwinServiceClient digitalTwinServiceClient;
    private static DigitalTwinServiceAsyncClient digitalTwinServiceAsyncClient;
    private String digitalTwinId;
    private TestDigitalTwinDevice testDevice;

    @Parameterized.Parameter(0)
    public IotHubClientProtocol protocol;

    @Parameterized.Parameters(name = "{index}: Invoke Commands Test: protocol={0}")
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

        TestInterfaceInstance2 testInterfaceInstance2 = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME_2);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance2)).blockingGet();
        assertThat(registrationResult).isEqualTo(DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK);
    }

    @Test
    public void testDeviceClientReceivesSyncCommandWithPayloadAndResponds() {
        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME_2, SYNC_COMMAND_WITH_PAYLOAD, SAMPLE_COMMAND_PAYLOAD);

        assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_COMPLETED);
        assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();
        assertThat(commandResponse.getPayload()).isEqualTo(SAMPLE_COMMAND_PAYLOAD);
    }

    @Test
    public void testDeviceClientReceivesSyncCommandWithoutPayloadAndResponds() {
        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME_2, SYNC_COMMAND_WITHOUT_PAYLOAD);

        assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_COMPLETED);
        assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();
    }

    @Test
    public void testDeviceClientReceivesAsyncCommandWithPayloadAndResponds() throws InterruptedException {
        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME_2, ASYNC_COMMAND_WITH_PAYLOAD, SAMPLE_COMMAND_PAYLOAD);

        assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_PENDING);
        assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();
        assertThat(commandResponse.getPayload()).isEqualTo(SAMPLE_COMMAND_PAYLOAD);

        // Verify that async command progress is sent to IoTHub
        String expectedPayload = String.format(ASYNC_COMMAND_COMPLETED_MESSAGE_FORMAT, ASYNC_COMMAND_WITH_PAYLOAD, SAMPLE_COMMAND_PAYLOAD);
        assertThat(verifyThatMessageWasReceived(digitalTwinId, expectedPayload)).as("Async command progress sent to IoTHub").isTrue();
    }

    @Test
    public void testDeviceClientReceivesAsyncCommandWithoutPayloadAndResponds() throws InterruptedException {
        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME_2, ASYNC_COMMAND_WITHOUT_PAYLOAD);

        assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_PENDING);
        assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();

        // Verify that async command progress is sent to IoTHub
        String expectedPayload = String.format(ASYNC_COMMAND_COMPLETED_MESSAGE_FORMAT, ASYNC_COMMAND_WITHOUT_PAYLOAD, "");
        assertThat(verifyThatMessageWasReceived(digitalTwinId, expectedPayload)).as("Async command progress sent to IoTHub").isTrue();
    }

    @Test
    public void testInvokeCommandInvalidInterfaceInstanceName() {
        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, INVALID_INTERFACE_INSTANCE_NAME, SYNC_COMMAND_WITHOUT_PAYLOAD);

        assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_NOT_IMPLEMENTED);
        assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();
        assertThat(commandResponse.getPayload()).isEqualTo(String.format(INTERFACE_INSTANCE_NOT_FOUND_MESSAGE_PATTERN, INVALID_INTERFACE_INSTANCE_NAME));
    }

    @Test
    public void testInvokeCommandInvalidCommandName() {
        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME_2, INVALID_COMMAND_NAME);

        assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_NOT_IMPLEMENTED);
        assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();
        assertThat(commandResponse.getPayload()).isEqualTo(String.format(COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN, INVALID_COMMAND_NAME, TestInterfaceInstance2.TEST_INTERFACE_ID));
    }

    @Test
    public void testSameCommandNameOnMultipleRegisteredInterfacesSuccess() throws IotHubException, IOException, URISyntaxException {
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

        assertThat(commandResponse1).as("Verify Command1 Invocation Response").isNotNull();
        assertThat(commandResponse1.getStatus()).isEqualTo(STATUS_CODE_COMPLETED);
        assertThat(commandResponse1.getPayload()).isEqualTo(samplePayload1);
        assertThat(commandResponse1.getRequestId()).as("Verify Command1 Invocation Response RequestId").isNotNull();

        assertThat(commandResponse2).as("Verify Command2 Invocation Response").isNotNull();
        assertThat(commandResponse2.getStatus()).isEqualTo(STATUS_CODE_COMPLETED);
        assertThat(commandResponse2.getPayload()).isEqualTo(samplePayload2);
        assertThat(commandResponse2.getRequestId()).as("Verify Command2 Invocation Response RequestId").isNotNull();
    }

    @Test
    public void testSyncCommandInvocationMultithreaded() {
        List<String> payloadTextList = new Random().ints(MAX_THREADS_MULTITHREADED_TEST).boxed()
                                                   .map(Object :: toString)
                                                   .collect(Collectors.toList());
        List<DigitalTwinCommandResponse> commandResponses = synchronizedList(new ArrayList<>());

        Flowable.range(0, MAX_THREADS_MULTITHREADED_TEST)
                .parallel()
                .runOn(Schedulers.io())
                .map(integer -> digitalTwinServiceAsyncClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME_2, SYNC_COMMAND_WITH_PAYLOAD, payloadTextList.get(integer))
                        .subscribe(commandResponses::add))
                .sequential()
                .toList()
                .blockingGet();

        for (int i = 0; i < MAX_THREADS_MULTITHREADED_TEST; i++) {
            DigitalTwinCommandResponse commandResponse = commandResponses.get(i);

            assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
            assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_COMPLETED);
            assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();
            assertThat(payloadTextList).contains(commandResponse.getPayload());

            payloadTextList.remove(commandResponse.getPayload());
        }
        assertThat(payloadTextList).as("All sent commands were received").isEmpty();
    }

    @Test
    public void testAsyncCommandInvocationMultithreaded() throws InterruptedException {
        List<String> payloadTextList = new Random().ints(MAX_THREADS_MULTITHREADED_TEST).boxed()
                                                   .map(Object :: toString)
                                                   .collect(Collectors.toList());
        List<DigitalTwinCommandResponse> commandResponses = synchronizedList(new ArrayList<>());

        Flowable.range(0, MAX_THREADS_MULTITHREADED_TEST)
                .parallel()
                .runOn(Schedulers.io())
                .map(integer -> digitalTwinServiceAsyncClient.invokeCommand(digitalTwinId, TEST_INTERFACE_INSTANCE_NAME_2, ASYNC_COMMAND_WITH_PAYLOAD, payloadTextList.get(integer))
                        .subscribe(commandResponses::add))
                .sequential()
                .toList()
                .blockingGet();

        List<String> payloadTextListCopy = new ArrayList<>(payloadTextList);
        for (int i = 0; i < MAX_THREADS_MULTITHREADED_TEST; i++) {
            DigitalTwinCommandResponse commandResponse = commandResponses.get(i);

            assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
            assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_PENDING);
            assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();
            assertThat(payloadTextList).contains(commandResponse.getPayload());

            payloadTextListCopy.remove(commandResponse.getPayload());
        }
        assertThat(payloadTextListCopy).as("All commands were invoked").isEmpty();

        // Verify that async command progress is sent to IoTHub
        for (int i = 0; i < MAX_THREADS_MULTITHREADED_TEST; i++) {
            String expectedPayload = String.format(ASYNC_COMMAND_COMPLETED_MESSAGE_FORMAT, ASYNC_COMMAND_WITH_PAYLOAD, payloadTextList.get(i));
            assertThat(verifyThatMessageWasReceived(digitalTwinId, expectedPayload)).as("Async command progress sent to IoTHub").isTrue();
        }
    }

    @After
    public void tearDownTest() {
        testDevice.closeAndDeleteDevice();
    }
}
