// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.ComplexObjectTelemetry;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.ComplexValueTelemetry;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.EnumTelemetry;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestDigitalTwinDevice;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.sql.Time;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT_WS;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.serialize;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.E2ETestConstants.DCM_ID;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.generateRandomIntegerList;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.generateRandomStringList;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveInterfaceNameFromInterfaceId;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.EventHubListener.verifyThatMessageWasReceived;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TELEMETRY_NAME_ARRAY;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TELEMETRY_NAME_BOOLEAN;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TELEMETRY_NAME_COMPLEX_OBJECT;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TELEMETRY_NAME_COMPLEX_VALUE;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TELEMETRY_NAME_DATE;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TELEMETRY_NAME_DATETIME;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TELEMETRY_NAME_DOUBLE;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TELEMETRY_NAME_DURATION;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TELEMETRY_NAME_ENUM;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TELEMETRY_NAME_FLOAT;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TELEMETRY_NAME_INTEGER;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TELEMETRY_NAME_LONG;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TELEMETRY_NAME_MAP;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TELEMETRY_NAME_STRING;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TELEMETRY_NAME_TIME;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestInterfaceInstance2.TEST_INTERFACE_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomUtils.nextBoolean;
import static org.apache.commons.lang3.RandomUtils.nextDouble;
import static org.apache.commons.lang3.RandomUtils.nextFloat;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(Parameterized.class)
public class DigitalTwinTelemetryParameterizedE2ETests {
    private static final String TEST_INTERFACE_INSTANCE_NAME = retrieveInterfaceNameFromInterfaceId(TEST_INTERFACE_ID);

    private static final String DEVICE_ID_PREFIX = "DigitalTwinTelemetryParameterizedE2ETests_";
    private static final String TELEMETRY_PAYLOAD_PATTERN = "{\"%s\":%s}";

    @Rule
    public Timeout globalTimeout = Timeout.seconds(5 * 60); // 5 minutes max per method tested

    @Parameterized.Parameter(0)
    public IotHubClientProtocol protocol;
    @Parameterized.Parameter(1)
    public String telemetryName;
    @Parameterized.Parameter(2)
    public Object telemetryValue;

    @Parameterized.Parameters(name = "{index}: Telemetry Test: protocol={0}, telemetry name={1}, telemetry value={2}")
    public static Collection<Object[]> data() {
        int arrayTelemetrySize = 5;
        String stringTelemetryValue = "StringTelemetryMessage_".concat(UUID.randomUUID().toString());
        long milliSecs = System.currentTimeMillis();
        List<Integer> telemetryIntegerArray = generateRandomIntegerList(arrayTelemetrySize);
        Map<String, String> telemetryMap = new HashMap<String, String>() {{ put("mapTelemetryKey", "mapTelemetryValue"); }};
        int telemetryEnum = EnumTelemetry.OFFLINE.getValue();
        ComplexValueTelemetry telemetryComplexValue = new ComplexValueTelemetry(nextInt(), nextInt(), nextInt());

        int integerValue = nextInt();
        String stringValue = "ComplexObjectTelemetry_".concat(UUID.randomUUID().toString());
        List<String> stringArrayValue = generateRandomStringList(arrayTelemetrySize);
        ComplexObjectTelemetry telemetryComplexObject = new ComplexObjectTelemetry(integerValue, stringValue, stringArrayValue);

        Object[][] data = new Object[][]{
                {MQTT, TELEMETRY_NAME_INTEGER, nextInt()},
                {MQTT, TELEMETRY_NAME_LONG, nextLong()},
                {MQTT, TELEMETRY_NAME_DOUBLE, nextDouble()},
                {MQTT, TELEMETRY_NAME_FLOAT, nextFloat()},
                {MQTT, TELEMETRY_NAME_BOOLEAN, nextBoolean()},
                {MQTT, TELEMETRY_NAME_STRING, stringTelemetryValue},
                {MQTT, TELEMETRY_NAME_DATE, new Date(milliSecs)},
                {MQTT, TELEMETRY_NAME_TIME, new Time(milliSecs)},
                {MQTT, TELEMETRY_NAME_DATETIME, new DateTime()},
                {MQTT, TELEMETRY_NAME_DURATION, Duration.millis(milliSecs)},
                {MQTT, TELEMETRY_NAME_ARRAY, telemetryIntegerArray},
                {MQTT, TELEMETRY_NAME_MAP, telemetryMap},
                {MQTT, TELEMETRY_NAME_ENUM, telemetryEnum},
                {MQTT, TELEMETRY_NAME_COMPLEX_VALUE, telemetryComplexValue},
                {MQTT, TELEMETRY_NAME_COMPLEX_OBJECT, telemetryComplexObject},
                {MQTT_WS, TELEMETRY_NAME_INTEGER, nextInt()},
                {MQTT_WS, TELEMETRY_NAME_LONG, nextLong()},
                {MQTT_WS, TELEMETRY_NAME_DOUBLE, nextDouble()},
                {MQTT_WS, TELEMETRY_NAME_FLOAT, nextFloat()},
                {MQTT_WS, TELEMETRY_NAME_BOOLEAN, nextBoolean()},
                {MQTT_WS, TELEMETRY_NAME_STRING, stringTelemetryValue},
                {MQTT_WS, TELEMETRY_NAME_DATE, new Date(milliSecs)},
                {MQTT_WS, TELEMETRY_NAME_TIME, new Time(milliSecs)},
                {MQTT_WS, TELEMETRY_NAME_DATETIME, new DateTime()},
                {MQTT_WS, TELEMETRY_NAME_DURATION, Duration.millis(milliSecs)},
                {MQTT_WS, TELEMETRY_NAME_ARRAY, telemetryIntegerArray},
                {MQTT_WS, TELEMETRY_NAME_MAP, telemetryMap},
                {MQTT_WS, TELEMETRY_NAME_ENUM, telemetryEnum},
                {MQTT_WS, TELEMETRY_NAME_COMPLEX_VALUE, telemetryComplexValue},
                {MQTT_WS, TELEMETRY_NAME_COMPLEX_OBJECT, telemetryComplexObject}
        };

        return asList(data);
    }

    @Test
    public void testSendTelemetryDifferentSchema() throws IOException, InterruptedException {
        Pair<TestDigitalTwinDevice, TestInterfaceInstance2> pair = initDigitalTwinDevice(protocol);
        TestDigitalTwinDevice testDevice = pair.getLeft();
        TestInterfaceInstance2 testInterfaceInstance = pair.getRight();

        try {
            log.debug("Sending telemetry: telemetryName={}, telemetryValue={}", telemetryName, telemetryValue);
            DigitalTwinClientResult digitalTwinClientResult = testInterfaceInstance.sendTelemetry(telemetryName, telemetryValue).blockingGet();
            log.debug("Telemetry operation result: {}", digitalTwinClientResult);

            String expectedPayload = String.format(TELEMETRY_PAYLOAD_PATTERN, telemetryName, serialize(telemetryValue));
            assertThat(verifyThatMessageWasReceived(testDevice.getDeviceId(), expectedPayload)).as("Verify EventHub received the sent telemetry").isTrue();
        } finally {
            testDevice.closeAndDeleteDevice();
        }
    }

    private static Pair<TestDigitalTwinDevice, TestInterfaceInstance2> initDigitalTwinDevice(IotHubClientProtocol protocol) {
        TestDigitalTwinDevice testDevice = new TestDigitalTwinDevice(DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString()), protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        TestInterfaceInstance2 testInterfaceInstance = new TestInterfaceInstance2(TEST_INTERFACE_INSTANCE_NAME);
        DigitalTwinClientResult registrationResult = digitalTwinDeviceClient.registerInterfacesAsync(DCM_ID, singletonList(testInterfaceInstance)).blockingGet();
        assertThat(registrationResult).isEqualTo(DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK);

        return Pair.of(testDevice, testInterfaceInstance);
    }
}
