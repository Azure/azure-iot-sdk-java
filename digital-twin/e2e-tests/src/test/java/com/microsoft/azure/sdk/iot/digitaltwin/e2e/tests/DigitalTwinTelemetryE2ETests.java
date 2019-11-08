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
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT_WS;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.serialize;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveInterfaceNameFromInterfaceId;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.EventHubListener.verifyThatMessageWasReceived;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomUtils.nextBoolean;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(Parameterized.class)
public class DigitalTwinTelemetryE2ETests {
    private static final String DCM_ID = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.DCM_ID_ENV_VAR_NAME);
    private static final String TEST_INTERFACE_INSTANCE_NAME = retrieveInterfaceNameFromInterfaceId(TEST_INTERFACE_ID);

    private static final String DEVICE_ID_PREFIX = "DigitalTwinTelemetryE2ETests_";
    private static final int MAX_THREADS_MULTITHREADED_TEST = 5;
    private static final String TELEMETRY_PAYLOAD_PATTERN = "{\"%s\":%s}";

    private TestInterfaceInstance2 testInterfaceInstance;
    private String digitalTwinId;
    private TestDigitalTwinDevice testDevice;

    @Parameterized.Parameter(0)
    public IotHubClientProtocol protocol;

    @Parameterized.Parameters(name = "{index}: Telemetry Test: protocol={0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                {MQTT},
                {MQTT_WS},
        });
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

    @Ignore("Disabled until Service starts validating the telemetry payload with telemetry schema.")
    @Test
    public void testSendIncompatibleSchemaTelemetry() throws IOException {
        DigitalTwinClientResult digitalTwinClientResult = testInterfaceInstance.sendTelemetry(TELEMETRY_NAME_INTEGER, nextBoolean()).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR);
    }

    @Test
    public void testMultipleThreadsSameInterfaceSameTelemetryNameSendTelemetry() throws IOException, InterruptedException {
        List<Integer> telemetryList = new Random().ints(MAX_THREADS_MULTITHREADED_TEST).boxed().collect(Collectors.toList());
        Flowable.range(0, MAX_THREADS_MULTITHREADED_TEST)
                .parallel()
                .runOn(Schedulers.io())
                .map(integer -> testInterfaceInstance.sendTelemetry(TELEMETRY_NAME_INTEGER, telemetryList.get(integer)).blockingGet())
                .sequential()
                .blockingSubscribe();

        for (int i = 0; i < MAX_THREADS_MULTITHREADED_TEST; i++) {
            String expectedPayload = String.format(TELEMETRY_PAYLOAD_PATTERN, TELEMETRY_NAME_INTEGER, serialize(telemetryList.get(i)));
            assertThat(verifyThatMessageWasReceived(digitalTwinId, expectedPayload)).as("Verify EventHub received the sent telemetry").isTrue();
        }
    }

    @Test
    public void testMultipleThreadsSameInterfaceDifferentTelemetryNameSendTelemetry() throws IOException, InterruptedException {
        int intTelemetry = nextInt();
        boolean booleanTelemetry = nextBoolean();
        Single<DigitalTwinClientResult> result1 = testInterfaceInstance.sendTelemetry(TELEMETRY_NAME_INTEGER, intTelemetry);
        Single<DigitalTwinClientResult> result2 = testInterfaceInstance.sendTelemetry(TELEMETRY_NAME_BOOLEAN, booleanTelemetry);

        Flowable.fromArray(result1, result2)
                .parallel()
                .runOn(Schedulers.io())
                .map(Single :: blockingGet)
                .sequential()
                .blockingSubscribe();

        String expectedPayloadResult1 = String.format(TELEMETRY_PAYLOAD_PATTERN, TELEMETRY_NAME_INTEGER, serialize(intTelemetry));
        assertThat(verifyThatMessageWasReceived(digitalTwinId, expectedPayloadResult1)).as("Verify EventHub received the sent telemetry").isTrue();
        String expectedPayloadResult2 = String.format(TELEMETRY_PAYLOAD_PATTERN, TELEMETRY_NAME_BOOLEAN, serialize(booleanTelemetry));
        assertThat(verifyThatMessageWasReceived(digitalTwinId, expectedPayloadResult2)).as("Verify EventHub received the sent telemetry").isTrue();
    }

    @Test
    public void testTelemetryOperationAfterClientCloseAndOpen() throws IOException, InterruptedException {
        int telemetryValue1 = nextInt();
        log.debug("Sending telemetry: telemetryName={}, telemetryValue={}", TELEMETRY_NAME_INTEGER, telemetryValue1);
        DigitalTwinClientResult digitalTwinClientResult1 = testInterfaceInstance.sendTelemetry(TELEMETRY_NAME_INTEGER, telemetryValue1).blockingGet();
        log.debug("Telemetry operation result: {}", digitalTwinClientResult1);

        String expectedPayload1 = String.format(TELEMETRY_PAYLOAD_PATTERN, TELEMETRY_NAME_INTEGER, serialize(telemetryValue1));
        assertThat(verifyThatMessageWasReceived(digitalTwinId, expectedPayload1)).as("Verify EventHub received the sent telemetry").isTrue();

        // close the device client
        testDevice.getDeviceClient().closeNow();

        // re-open the client and send telemetry
        testDevice.getDeviceClient().open();

        int telemetryValue2 = nextInt();
        log.debug("Sending telemetry: telemetryName={}, telemetryValue={}", TELEMETRY_NAME_INTEGER, telemetryValue2);
        DigitalTwinClientResult digitalTwinClientResult2 = testInterfaceInstance.sendTelemetry(TELEMETRY_NAME_INTEGER, telemetryValue2).blockingGet();
        log.debug("Telemetry operation result: {}", digitalTwinClientResult2);

        String expectedPayload2 = String.format(TELEMETRY_PAYLOAD_PATTERN, TELEMETRY_NAME_INTEGER, serialize(telemetryValue2));
        assertThat(verifyThatMessageWasReceived(digitalTwinId, expectedPayload2)).as("Verify EventHub received the sent telemetry").isTrue();
    }

    @After
    public void tearDownTest() {
        testDevice.closeAndDeleteDevice();
    }
}
