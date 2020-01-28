// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent1;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestDigitalTwinDevice;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceAsyncClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceAsyncClientImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceClientImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.rest.RestException;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT_WS;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.serialize;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.MAX_THREADS_MULTITHREADED_TEST;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.MAX_WAIT_TIME_FOR_ASYNC_CALL_IN_SECONDS;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.generateRandomStringList;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveComponentNameFromInterfaceId;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveEnvironmentVariableValue;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.EventHubListener.verifyThatMessageWasReceived;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.ASYNC_COMMAND_COMPLETED_MESSAGE_FORMAT;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.ASYNC_COMMAND_WITHOUT_PAYLOAD;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.ASYNC_COMMAND_WITH_PAYLOAD;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.STATUS_CODE_COMPLETED;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.STATUS_CODE_NOT_IMPLEMENTED;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.STATUS_CODE_PENDING;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.SYNC_COMMAND_WITHOUT_PAYLOAD;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.SYNC_COMMAND_WITH_PAYLOAD;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TEST_INTERFACE_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
@RunWith(Parameterized.class)
public class DigitalTwinCommandE2ETests {
    private static final String IOT_HUB_CONNECTION_STRING = retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
    private static final String TEST_COMPONENT_NAME_1 = retrieveComponentNameFromInterfaceId(TestComponent1.TEST_INTERFACE_ID);
    private static final String TEST_COMPONENT_NAME_2 = retrieveComponentNameFromInterfaceId(TEST_INTERFACE_ID);

    private static final String DEVICE_ID_PREFIX = "DigitalTwinCommandE2ETests_";

    private static final String SAMPLE_COMMAND_STRING_PAYLOAD = "\"samplePayload\"";
    private static final String SAMPLE_COMMAND_JSON_PAYLOAD = "{\"desc\":\"abc\"}";
    private static final String SAMPLE_COMMAND_INT_PAYLOAD = "5";
    private static final String INVALID_COMPONENT_NAME = "invalidComponentName";
    private static final String INVALID_COMMAND_NAME = "invalidCommandName";
    private static final String COMPONENT_NOT_FOUND_MESSAGE_PATTERN = "Component [%s] not found.";


    private static DigitalTwinServiceClient digitalTwinServiceClient;
    private static DigitalTwinServiceAsyncClient digitalTwinServiceAsyncClient;
    private TestDigitalTwinDevice testDevice;
    private DigitalTwinDeviceClient digitalTwinDeviceClient;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(5 * 60); // 5 minutes max per method tested

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
        testDevice = new TestDigitalTwinDevice(DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString()), protocol);
        digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        TestComponent2 testComponent2 = new TestComponent2(TEST_COMPONENT_NAME_2);
        assertThat(digitalTwinDeviceClient.bindComponents(singletonList(testComponent2))).isEqualTo(DIGITALTWIN_CLIENT_OK);
    }

    @Test
    public void testDeviceClientReceivesSyncCommandWithStringPayloadAndResponds() {
        assertThat(digitalTwinDeviceClient.subscribeForCommands()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(testDevice.getDeviceId(), TEST_COMPONENT_NAME_2, SYNC_COMMAND_WITH_PAYLOAD, SAMPLE_COMMAND_STRING_PAYLOAD);
        assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_COMPLETED);
        assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();
        assertThat(commandResponse.getPayload()).isEqualTo(SAMPLE_COMMAND_STRING_PAYLOAD);
    }

    @Test
    public void testDeviceClientReceivesSyncCommandWithJsonPayloadAndResponds() {
        assertThat(digitalTwinDeviceClient.subscribeForCommands()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(testDevice.getDeviceId(), TEST_COMPONENT_NAME_2, SYNC_COMMAND_WITH_PAYLOAD, SAMPLE_COMMAND_JSON_PAYLOAD);

        assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_COMPLETED);
        assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();
        assertThat(commandResponse.getPayload()).isEqualTo(SAMPLE_COMMAND_JSON_PAYLOAD);
    }

    @Test
    public void testDeviceClientReceivesSyncCommandWithIntegerPayloadAndResponds() {
        assertThat(digitalTwinDeviceClient.subscribeForCommands()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(testDevice.getDeviceId(), TEST_COMPONENT_NAME_2, SYNC_COMMAND_WITH_PAYLOAD, SAMPLE_COMMAND_INT_PAYLOAD);

        assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_COMPLETED);
        assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();
        assertThat(commandResponse.getPayload()).isEqualTo(SAMPLE_COMMAND_INT_PAYLOAD);
    }

    @Test
    public void testDeviceClientReceivesSyncCommandWithoutPayloadAndResponds() {
        assertThat(digitalTwinDeviceClient.subscribeForCommands()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(testDevice.getDeviceId(), TEST_COMPONENT_NAME_2, SYNC_COMMAND_WITHOUT_PAYLOAD);

        assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_COMPLETED);
        assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();
        assertThat(commandResponse.getPayload()).isNull();
    }

    @Test
    public void testDeviceClientReceivesAsyncCommandWithStringPayloadAndResponds() throws InterruptedException {
        assertThat(digitalTwinDeviceClient.subscribeForCommands()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(testDevice.getDeviceId(), TEST_COMPONENT_NAME_2, ASYNC_COMMAND_WITH_PAYLOAD, SAMPLE_COMMAND_STRING_PAYLOAD);

        assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_PENDING);
        assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();
        assertThat(commandResponse.getPayload()).isEqualTo(SAMPLE_COMMAND_STRING_PAYLOAD);

        // Verify that async command progress is sent to IoTHub
        String expectedPayload = String.format(ASYNC_COMMAND_COMPLETED_MESSAGE_FORMAT, ASYNC_COMMAND_WITH_PAYLOAD, SAMPLE_COMMAND_STRING_PAYLOAD);
        assertThat(verifyThatMessageWasReceived(testDevice.getDeviceId(), expectedPayload)).as("Async command progress sent to IoTHub").isTrue();
    }

    @Test
    public void testDeviceClientReceivesAsyncCommandWithJsonPayloadAndResponds() throws InterruptedException {
        assertThat(digitalTwinDeviceClient.subscribeForCommands()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(testDevice.getDeviceId(), TEST_COMPONENT_NAME_2, ASYNC_COMMAND_WITH_PAYLOAD, SAMPLE_COMMAND_JSON_PAYLOAD);

        assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_PENDING);
        assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();
        assertThat(commandResponse.getPayload()).isEqualTo(SAMPLE_COMMAND_JSON_PAYLOAD);

        // Verify that async command progress is sent to IoTHub
        String expectedPayload = String.format(ASYNC_COMMAND_COMPLETED_MESSAGE_FORMAT, ASYNC_COMMAND_WITH_PAYLOAD, SAMPLE_COMMAND_JSON_PAYLOAD);
        assertThat(verifyThatMessageWasReceived(testDevice.getDeviceId(), expectedPayload)).as("Async command progress sent to IoTHub").isTrue();
    }

    @Test
    public void testDeviceClientReceivesAsyncCommandWithIntegerPayloadAndResponds() throws InterruptedException {
        assertThat(digitalTwinDeviceClient.subscribeForCommands()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(testDevice.getDeviceId(), TEST_COMPONENT_NAME_2, ASYNC_COMMAND_WITH_PAYLOAD, SAMPLE_COMMAND_INT_PAYLOAD);

        assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_PENDING);
        assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();
        assertThat(commandResponse.getPayload()).isEqualTo(SAMPLE_COMMAND_INT_PAYLOAD);

        // Verify that async command progress is sent to IoTHub
        String expectedPayload = String.format(ASYNC_COMMAND_COMPLETED_MESSAGE_FORMAT, ASYNC_COMMAND_WITH_PAYLOAD, SAMPLE_COMMAND_INT_PAYLOAD);
        assertThat(verifyThatMessageWasReceived(testDevice.getDeviceId(), expectedPayload)).as("Async command progress sent to IoTHub").isTrue();
    }

    @Test
    public void testDeviceClientReceivesAsyncCommandWithoutPayloadAndResponds() throws InterruptedException {
        assertThat(digitalTwinDeviceClient.subscribeForCommands()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(testDevice.getDeviceId(), TEST_COMPONENT_NAME_2, ASYNC_COMMAND_WITHOUT_PAYLOAD);

        assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_PENDING);
        assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();
        assertThat(commandResponse.getPayload()).isNull();

        // Verify that async command progress is sent to IoTHub
        String expectedPayload = String.format(ASYNC_COMMAND_COMPLETED_MESSAGE_FORMAT, ASYNC_COMMAND_WITHOUT_PAYLOAD, "\"\"");
        assertThat(verifyThatMessageWasReceived(testDevice.getDeviceId(), expectedPayload)).as("Async command progress sent to IoTHub").isTrue();
    }

    @Test
    public void testInvokeCommandInvalidComponentName() throws IOException {
        assertThat(digitalTwinDeviceClient.subscribeForCommands()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(testDevice.getDeviceId(), INVALID_COMPONENT_NAME, SYNC_COMMAND_WITHOUT_PAYLOAD);
        String serializedErrorMessage = serialize(String.format(COMPONENT_NOT_FOUND_MESSAGE_PATTERN, INVALID_COMPONENT_NAME));

        assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_NOT_IMPLEMENTED);
        assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();
        assertThat(commandResponse.getPayload()).isEqualTo(serializedErrorMessage);
    }

    @Test
    public void testInvokeCommandInvalidCommandName() throws IOException {
        assertThat(digitalTwinDeviceClient.subscribeForCommands()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        DigitalTwinCommandResponse commandResponse = digitalTwinServiceClient.invokeCommand(testDevice.getDeviceId(), TEST_COMPONENT_NAME_2, INVALID_COMMAND_NAME);
        String serializedErrorMessage = serialize(String.format(COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN, INVALID_COMMAND_NAME, TEST_INTERFACE_ID));
        assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_NOT_IMPLEMENTED);
        assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();
        assertThat(commandResponse.getPayload()).isEqualTo(serializedErrorMessage);
    }

    @Test
    public void testSameCommandNameOnMultipleRegisteredComponentsSuccess() throws IotHubException, IOException, URISyntaxException {
        if (testDevice != null) {
            testDevice.closeAndDeleteDevice();
        }

        testDevice = new TestDigitalTwinDevice(DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString()), protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();
        TestComponent1 testComponent1 = new TestComponent1(TEST_COMPONENT_NAME_1);
        TestComponent2 testComponent2 = new TestComponent2(TEST_COMPONENT_NAME_2);
        assertThat(digitalTwinDeviceClient.bindComponents(asList(testComponent1, testComponent2))).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(digitalTwinDeviceClient.subscribeForCommands()).isEqualTo(DIGITALTWIN_CLIENT_OK);

        String sampleStringPayload1 = "\"samplePayload1\"";
        String sampleStringPayload2 = "\"samplePayload2\"";
        DigitalTwinCommandResponse commandResponse1 = digitalTwinServiceClient.invokeCommand(
                testDevice.getDeviceId(),
                TEST_COMPONENT_NAME_1,
                TestComponent1.SYNC_COMMAND_WITH_PAYLOAD,
                sampleStringPayload1
        );
        DigitalTwinCommandResponse commandResponse2 = digitalTwinServiceClient.invokeCommand(testDevice.getDeviceId(), TEST_COMPONENT_NAME_2, SYNC_COMMAND_WITH_PAYLOAD, sampleStringPayload2);

        assertThat(commandResponse1).as("Verify Command1 Invocation Response").isNotNull();
        assertThat(commandResponse1.getStatus()).isEqualTo(STATUS_CODE_COMPLETED);
        assertThat(commandResponse1.getPayload()).isEqualTo(sampleStringPayload1);
        assertThat(commandResponse1.getRequestId()).as("Verify Command1 Invocation Response RequestId").isNotNull();

        assertThat(commandResponse2).as("Verify Command2 Invocation Response").isNotNull();
        assertThat(commandResponse2.getStatus()).isEqualTo(STATUS_CODE_COMPLETED);
        assertThat(commandResponse2.getPayload()).isEqualTo(sampleStringPayload2);
        assertThat(commandResponse2.getRequestId()).as("Verify Command2 Invocation Response RequestId").isNotNull();
    }

    @Test
    public void testSyncCommandInvocationMultithreaded() throws InterruptedException {
        assertThat(digitalTwinDeviceClient.subscribeForCommands()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        final Semaphore semaphore = new Semaphore(0);
        final List<DigitalTwinCommandResponse> commandResponses = synchronizedList(new ArrayList<>());
        List<String> requestPayloadForMultiThreadTest = generateRandomStringList(MAX_THREADS_MULTITHREADED_TEST);

        List<String> serializedRequestPayloadForMultiThreadTest = requestPayloadForMultiThreadTest.stream().map(s -> {
            try {
                return serialize(s);
            } catch (IOException e) {
                log.error("Exception thrown while serializing request to JSON string: ", e);
                return null;
            }
        }).collect(Collectors.toList());


        serializedRequestPayloadForMultiThreadTest.forEach(payload -> digitalTwinServiceAsyncClient.invokeCommand(testDevice.getDeviceId(), TEST_COMPONENT_NAME_2, SYNC_COMMAND_WITH_PAYLOAD, payload)
                                                                                                   .subscribe(response -> {
                                                                                                       commandResponses.add(response);
                                                                                                       semaphore.release();
                                                                                                   }));

        assertThat(semaphore.tryAcquire(MAX_THREADS_MULTITHREADED_TEST, MAX_WAIT_TIME_FOR_ASYNC_CALL_IN_SECONDS, SECONDS)).as("Timeout executing Async call").isTrue();

        Set<String> responsePayloads = new HashSet<>();
        for (int i = 0; i < MAX_THREADS_MULTITHREADED_TEST; i++) {
            DigitalTwinCommandResponse commandResponse = commandResponses.get(i);

            assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
            assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_COMPLETED);
            assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();

            responsePayloads.add(commandResponse.getPayload());
        }
        assertThat(responsePayloads).as("All commands were invoked").hasSameElementsAs(serializedRequestPayloadForMultiThreadTest);
    }

    @Test
    public void testAsyncCommandInvocationMultithreaded() throws InterruptedException {
        assertThat(digitalTwinDeviceClient.subscribeForCommands()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        final Semaphore semaphore = new Semaphore(0);
        final List<DigitalTwinCommandResponse> commandResponses = synchronizedList(new ArrayList<>());
        List<String> requestPayloadForMultiThreadTest = generateRandomStringList(MAX_THREADS_MULTITHREADED_TEST);

        List<String> serializedRequestPayloadForMultiThreadTest = requestPayloadForMultiThreadTest.stream().map(s -> {
            try {
                return serialize(s);
            } catch (IOException e) {
                log.error("Exception thrown while serializing request to JSON string: ", e);
                return null;
            }
        }).collect(Collectors.toList());

        serializedRequestPayloadForMultiThreadTest.forEach(payload -> digitalTwinServiceAsyncClient.invokeCommand(testDevice.getDeviceId(), TEST_COMPONENT_NAME_2, ASYNC_COMMAND_WITH_PAYLOAD, payload)
                                                                                                   .subscribe(response -> {
                                                                                                       commandResponses.add(response);
                                                                                                       semaphore.release();
                                                                                                   }));

        assertThat(semaphore.tryAcquire(MAX_THREADS_MULTITHREADED_TEST, MAX_WAIT_TIME_FOR_ASYNC_CALL_IN_SECONDS, SECONDS)).as("Timeout executing Async call").isTrue();

        Set<String> responsePayloads = new HashSet<>();
        for (int i = 0; i < MAX_THREADS_MULTITHREADED_TEST; i++) {
            DigitalTwinCommandResponse commandResponse = commandResponses.get(i);

            assertThat(commandResponse).as("Verify Command Invocation Response").isNotNull();
            assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_PENDING);
            assertThat(commandResponse.getRequestId()).as("Verify Command Invocation Response RequestId").isNotNull();

            String responsePayload = commandResponse.getPayload();
            responsePayloads.add(responsePayload);

            // Verify that async command progress is sent to IoTHub
            String expectedPayload = String.format(ASYNC_COMMAND_COMPLETED_MESSAGE_FORMAT, ASYNC_COMMAND_WITH_PAYLOAD, responsePayload);
            assertThat(verifyThatMessageWasReceived(testDevice.getDeviceId(), expectedPayload)).as("Async command progress sent to IoTHub").isTrue();
        }
        assertThat(responsePayloads).as("All commands were invoked").hasSameElementsAs(serializedRequestPayloadForMultiThreadTest);
    }

    @Test
    public void testDeviceClientReceivesCommandWithoutSubscribe() {
        try {
            digitalTwinServiceClient.invokeCommand(
                    testDevice.getDeviceId(),
                    TEST_COMPONENT_NAME_2,
                    SYNC_COMMAND_WITH_PAYLOAD,
                    SAMPLE_COMMAND_STRING_PAYLOAD
            );
            fail("Should throw RestException with 404 status code.");
        } catch (RestException ex) {
            assertThat(ex.getMessage()).contains("Status code 404");
        }
    }

    @After
    public void tearDownTest() {
        if (testDevice != null) {
            testDevice.closeAndDeleteDevice();
        }
    }
}
