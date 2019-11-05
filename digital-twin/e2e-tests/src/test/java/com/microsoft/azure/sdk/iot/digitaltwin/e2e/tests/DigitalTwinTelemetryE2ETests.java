// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT_WS;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.serialize;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveInterfaceNameFromInterfaceId;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.EventHubListener.verifyThatMessageWasReceived;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.apache.commons.lang3.RandomUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.Duration.millis;

@Slf4j
public class DigitalTwinTelemetryE2ETests {
    private static final String DCM_ID = Tools.retrieveEnvironmentVariableValue(E2ETestConstants.DCM_ID_ENV_VAR_NAME);
    private static final String TEST_INTERFACE_INSTANCE_NAME_2 = retrieveInterfaceNameFromInterfaceId(TEST_INTERFACE_ID);

    private static final String DEVICE_ID_PREFIX = "DigitalTwinTelemetryE2ETests_";
    private static final int MAX_THREADS_MULTITHREADED_TEST = 5;
    private static final String TELEMETRY_PAYLOAD_PATTERN = "{\"%s\":%s}";

    private TestInterfaceInstance2 testInterfaceInstance2;
    private String digitalTwinId;
    private TestDigitalTwinDevice testDevice;

    @ParameterizedTest(name = "{index}: Telemetry Test: protocol = {0} telemetry name = {1}, telemetry value = {2}")
    @MethodSource("telemetryTestDifferentSchemaParameters")
    public void testSendTelemetry(IotHubClientProtocol protocol, String telemetryName, Object telemetryValue) throws IOException, URISyntaxException, IotHubException, InterruptedException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        testInterfaceInstance2 = registerAndReturnDigitalTwinInterface(testDevice);

        log.debug("Sending telemetry: telemetryName={}, telemetryValue={}", telemetryName, telemetryValue);
        DigitalTwinClientResult digitalTwinClientResult = testInterfaceInstance2.sendTelemetry(telemetryName, telemetryValue).blockingGet();
        log.debug("Telemetry operation result: {}", digitalTwinClientResult);

        String expectedPayload = String.format(TELEMETRY_PAYLOAD_PATTERN, telemetryName, serialize(telemetryValue));
        assertThat(verifyThatMessageWasReceived(digitalTwinId, expectedPayload)).as("Verify EventHub received the sent telemetry").isTrue();
    }

    @Disabled("Disabled until Service starts validating the telemetry payload with telemetry schema.")
    @ParameterizedTest(name = "{index}: Telemetry Test: incompatible schema - protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testSendIncompatibleSchemaTelemetry(IotHubClientProtocol protocol) throws IOException, URISyntaxException, IotHubException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        testInterfaceInstance2 = registerAndReturnDigitalTwinInterface(testDevice);

        DigitalTwinClientResult digitalTwinClientResult = testInterfaceInstance2.sendTelemetry(TELEMETRY_NAME_INTEGER, nextBoolean()).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR);
    }

    @ParameterizedTest(name = "{index}: Multiple Threads send Telemetry: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testMultipleThreadsSameInterfaceSameTelemetryNameSendTelemetry(IotHubClientProtocol protocol) throws IOException, URISyntaxException, IotHubException, InterruptedException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        testInterfaceInstance2 = registerAndReturnDigitalTwinInterface(testDevice);

        List<Integer> telemetryList = new Random().ints(MAX_THREADS_MULTITHREADED_TEST).boxed().collect(Collectors.toList());
        Flowable.range(0, MAX_THREADS_MULTITHREADED_TEST)
                .parallel()
                .runOn(Schedulers.io())
                .map(integer -> testInterfaceInstance2.sendTelemetry(TELEMETRY_NAME_INTEGER, telemetryList.get(integer)).blockingGet())
                .sequential()
                .blockingSubscribe();

        for (int i = 0; i < MAX_THREADS_MULTITHREADED_TEST; i++) {
            String expectedPayload = String.format(TELEMETRY_PAYLOAD_PATTERN, TELEMETRY_NAME_INTEGER, serialize(telemetryList.get(i)));
            assertThat(verifyThatMessageWasReceived(digitalTwinId, expectedPayload)).as("Verify EventHub received the sent telemetry").isTrue();
        }
    }

    @ParameterizedTest(name = "{index}: Multiple Threads send Telemetry: protocol = {0}")
    @EnumSource(value = IotHubClientProtocol.class, names = {"MQTT", "MQTT_WS"})
    public void testMultipleThreadsSameInterfaceDifferentTelemetryNameSendTelemetry(IotHubClientProtocol protocol) throws IotHubException, IOException, URISyntaxException, InterruptedException {
        digitalTwinId = DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString());
        testDevice = new TestDigitalTwinDevice(digitalTwinId, protocol);
        testInterfaceInstance2 = registerAndReturnDigitalTwinInterface(testDevice);

        int intTelemetry = nextInt();
        boolean booleanTelemetry = nextBoolean();
        Single<DigitalTwinClientResult> result1 = testInterfaceInstance2.sendTelemetry(TELEMETRY_NAME_INTEGER, intTelemetry);
        Single<DigitalTwinClientResult> result2 = testInterfaceInstance2.sendTelemetry(TELEMETRY_NAME_BOOLEAN, booleanTelemetry);

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

    private static Stream<Arguments> telemetryTestDifferentSchemaParameters() {
        String stringTelemetryValue = "StringTelemetryMessage_".concat(UUID.randomUUID().toString());
        long milliSecs = System.currentTimeMillis();
        List<Integer> telemetryIntegerArray = asList(nextInt(), nextInt(), nextInt());
        Map<String, String> telemetryMap = singletonMap("mapTelemetryKey", "mapTelemetryValue");

        int telemetryEnum = EnumTelemetry.OFFLINE.getValue();
        ComplexValueTelemetry telemetryComplexValue = new ComplexValueTelemetry(nextInt(), nextInt(), nextInt());

        int integerValue = nextInt();
        String stringValue = "ComplexObjectTelemetry_".concat(UUID.randomUUID().toString());
        List<String> stringArrayValue = singletonList("ArrayTelemetry_".concat(UUID.randomUUID().toString()));
        ComplexObjectTelemetry telemetryComplexObject = new ComplexObjectTelemetry(integerValue, stringValue, stringArrayValue);

        return Stream.of(
                Arguments.of(MQTT, TELEMETRY_NAME_INTEGER, nextInt()),
                Arguments.of(MQTT, TELEMETRY_NAME_LONG, nextLong()),
                Arguments.of(MQTT, TELEMETRY_NAME_DOUBLE, nextDouble()),
                Arguments.of(MQTT, TELEMETRY_NAME_FLOAT, nextFloat()),
                Arguments.of(MQTT, TELEMETRY_NAME_BOOLEAN, nextBoolean()),
                Arguments.of(MQTT, TELEMETRY_NAME_STRING, stringTelemetryValue),
                Arguments.of(MQTT, TELEMETRY_NAME_DATE, new Date(milliSecs)),
                Arguments.of(MQTT, TELEMETRY_NAME_TIME, new Time(milliSecs)),
                Arguments.of(MQTT, TELEMETRY_NAME_DATETIME, new DateTime()),
                Arguments.of(MQTT, TELEMETRY_NAME_DURATION, millis(milliSecs)),
                Arguments.of(MQTT, TELEMETRY_NAME_ARRAY, telemetryIntegerArray),
                Arguments.of(MQTT, TELEMETRY_NAME_MAP, telemetryMap),
                Arguments.of(MQTT, TELEMETRY_NAME_ENUM, telemetryEnum),
                Arguments.of(MQTT, TELEMETRY_NAME_COMPLEX_VALUE, telemetryComplexValue),
                Arguments.of(MQTT, TELEMETRY_NAME_COMPLEX_OBJECT, telemetryComplexObject),
                Arguments.of(MQTT_WS, TELEMETRY_NAME_INTEGER, nextInt()),
                Arguments.of(MQTT_WS, TELEMETRY_NAME_LONG, nextLong()),
                Arguments.of(MQTT_WS, TELEMETRY_NAME_DOUBLE, nextDouble()),
                Arguments.of(MQTT_WS, TELEMETRY_NAME_FLOAT, nextFloat()),
                Arguments.of(MQTT_WS, TELEMETRY_NAME_BOOLEAN, nextBoolean()),
                Arguments.of(MQTT_WS, TELEMETRY_NAME_STRING, stringTelemetryValue),
                Arguments.of(MQTT_WS, TELEMETRY_NAME_DATE, new Date(milliSecs)),
                Arguments.of(MQTT_WS, TELEMETRY_NAME_TIME, new Time(milliSecs)),
                Arguments.of(MQTT_WS, TELEMETRY_NAME_DATETIME, new DateTime()),
                Arguments.of(MQTT_WS, TELEMETRY_NAME_DURATION, millis(milliSecs)),
                Arguments.of(MQTT_WS, TELEMETRY_NAME_ARRAY, telemetryIntegerArray),
                Arguments.of(MQTT_WS, TELEMETRY_NAME_MAP, telemetryMap),
                Arguments.of(MQTT_WS, TELEMETRY_NAME_ENUM, telemetryEnum),
                Arguments.of(MQTT_WS, TELEMETRY_NAME_COMPLEX_VALUE, telemetryComplexValue),
                Arguments.of(MQTT_WS, TELEMETRY_NAME_COMPLEX_OBJECT, telemetryComplexObject)
        );
    }

    private TestInterfaceInstance2 registerAndReturnDigitalTwinInterface(TestDigitalTwinDevice testDevice) {
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        TestInterfaceInstance2 testInterfaceInstance = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME_2);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance)).blockingGet();
        assertThat(registrationResult).isEqualTo(DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK);

        return testInterfaceInstance;
    }

    @AfterEach
    public void tearDownTest() {
        testDevice.closeAndDeleteDevice();
    }
}
