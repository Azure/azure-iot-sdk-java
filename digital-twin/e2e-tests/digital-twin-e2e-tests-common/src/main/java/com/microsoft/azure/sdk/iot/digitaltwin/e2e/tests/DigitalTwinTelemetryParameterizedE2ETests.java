// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.tests;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.ComplexObjectTelemetry;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.ComplexValueTelemetry;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.EnumTelemetry;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2;
import com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestDigitalTwinDevice;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Time;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT_WS;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.serialize;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.generateRandomIntegerList;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.generateRandomStringList;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.helpers.Tools.retrieveComponentNameFromInterfaceId;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.EventHubListener.verifyThatMessageWasReceived;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TELEMETRY_NAME_ARRAY;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TELEMETRY_NAME_BOOLEAN;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TELEMETRY_NAME_COMPLEX_OBJECT;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TELEMETRY_NAME_COMPLEX_VALUE;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TELEMETRY_NAME_DATE;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TELEMETRY_NAME_DATETIME;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TELEMETRY_NAME_DOUBLE;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TELEMETRY_NAME_DURATION;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TELEMETRY_NAME_ENUM;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TELEMETRY_NAME_FLOAT;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TELEMETRY_NAME_INTEGER;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TELEMETRY_NAME_LONG;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TELEMETRY_NAME_MAP;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TELEMETRY_NAME_STRING;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TELEMETRY_NAME_TIME;
import static com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator.TestComponent2.TEST_INTERFACE_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.apache.commons.lang3.RandomUtils.nextBoolean;
import static org.apache.commons.lang3.RandomUtils.nextDouble;
import static org.apache.commons.lang3.RandomUtils.nextFloat;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(Parameterized.class)
public class DigitalTwinTelemetryParameterizedE2ETests {
    private static final String TEST_COMPONENT_NAME = retrieveComponentNameFromInterfaceId(TEST_INTERFACE_ID);

    private static final String DEVICE_ID_PREFIX = "DigitalTwinTelemetryParameterizedE2ETests_";
    private TestComponent2 testComponent;
    private TestDigitalTwinDevice testDevice;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(5 * 60); // 5 minutes max per method tested

    @Parameterized.Parameter(0)
    public IotHubClientProtocol protocol;
    @Parameterized.Parameter(1)
    public Map<String, Object> telemetryProperties;

    @Parameterized.Parameters(name = "{index}: Telemetry Test: protocol={0}, telemetry properties={1}")
    public static Collection<Object[]> data() {
        int arrayTelemetrySize = 5;
        String stringTelemetryValue = "StringTelemetryMessage_".concat(UUID.randomUUID().toString());
        long milliSecs = System.currentTimeMillis();
        List<Integer> telemetryIntegerArray = generateRandomIntegerList(arrayTelemetrySize);
        Map<String, String> telemetryMap = new HashMap<String, String>() {{
            put("mapTelemetryKey", "mapTelemetryValue");
        }};
        int telemetryEnum = EnumTelemetry.OFFLINE.getValue();
        ComplexValueTelemetry telemetryComplexValue = new ComplexValueTelemetry(nextInt(), nextInt(), nextInt());

        int integerValue = nextInt();
        String stringValue = "ComplexObjectTelemetry_".concat(UUID.randomUUID().toString());
        List<String> stringArrayValue = generateRandomStringList(arrayTelemetrySize);
        ComplexObjectTelemetry telemetryComplexObject = new ComplexObjectTelemetry(integerValue, stringValue, stringArrayValue);

        Object[][] data = new Object[][] {
                { MQTT, singletonMap(TELEMETRY_NAME_INTEGER, nextInt()) },
                { MQTT, singletonMap(TELEMETRY_NAME_LONG, nextLong()) },
                { MQTT, singletonMap(TELEMETRY_NAME_DOUBLE, nextDouble()) },
                { MQTT, singletonMap(TELEMETRY_NAME_FLOAT, nextFloat()) },
                { MQTT, singletonMap(TELEMETRY_NAME_BOOLEAN, nextBoolean()) },
                { MQTT, singletonMap(TELEMETRY_NAME_STRING, stringTelemetryValue) },
                { MQTT, singletonMap(TELEMETRY_NAME_DATE, new Date(milliSecs)) },
                { MQTT, singletonMap(TELEMETRY_NAME_TIME, new Time(milliSecs)) },
                { MQTT, singletonMap(TELEMETRY_NAME_DATETIME, new DateTime()) },
                { MQTT, singletonMap(TELEMETRY_NAME_DURATION, Duration.millis(milliSecs)) },
                { MQTT, singletonMap(TELEMETRY_NAME_ARRAY, telemetryIntegerArray) },
                { MQTT, singletonMap(TELEMETRY_NAME_MAP, telemetryMap) },
                { MQTT, singletonMap(TELEMETRY_NAME_ENUM, telemetryEnum) },
                { MQTT, singletonMap(TELEMETRY_NAME_COMPLEX_VALUE, telemetryComplexValue) },
                { MQTT, singletonMap(TELEMETRY_NAME_COMPLEX_OBJECT, telemetryComplexObject) },
                { MQTT, new HashMap<String, Object>() {
                    {
                        put(TELEMETRY_NAME_INTEGER, nextInt());
                        put(TELEMETRY_NAME_LONG, nextLong());
                        put(TELEMETRY_NAME_DOUBLE, nextDouble());
                        put(TELEMETRY_NAME_FLOAT, nextFloat());
                        put(TELEMETRY_NAME_BOOLEAN, nextBoolean());
                        put(TELEMETRY_NAME_STRING, stringTelemetryValue);
                        put(TELEMETRY_NAME_COMPLEX_OBJECT, telemetryComplexObject);
                    }
                }},
                { MQTT_WS, singletonMap(TELEMETRY_NAME_INTEGER, nextInt()) },
                { MQTT_WS, singletonMap(TELEMETRY_NAME_LONG, nextLong()) },
                { MQTT_WS, singletonMap(TELEMETRY_NAME_DOUBLE, nextDouble()) },
                { MQTT_WS, singletonMap(TELEMETRY_NAME_FLOAT, nextFloat()) },
                { MQTT_WS, singletonMap(TELEMETRY_NAME_BOOLEAN, nextBoolean()) },
                { MQTT_WS, singletonMap(TELEMETRY_NAME_STRING, stringTelemetryValue) },
                { MQTT_WS, singletonMap(TELEMETRY_NAME_DATE, new Date(milliSecs)) },
                { MQTT_WS, singletonMap(TELEMETRY_NAME_TIME, new Time(milliSecs)) },
                { MQTT_WS, singletonMap(TELEMETRY_NAME_DATETIME, new DateTime()) },
                { MQTT_WS, singletonMap(TELEMETRY_NAME_DURATION, Duration.millis(milliSecs)) },
                { MQTT_WS, singletonMap(TELEMETRY_NAME_ARRAY, telemetryIntegerArray) },
                { MQTT_WS, singletonMap(TELEMETRY_NAME_MAP, telemetryMap) },
                { MQTT_WS, singletonMap(TELEMETRY_NAME_ENUM, telemetryEnum) },
                { MQTT_WS, singletonMap(TELEMETRY_NAME_COMPLEX_VALUE, telemetryComplexValue) },
                { MQTT_WS, singletonMap(TELEMETRY_NAME_COMPLEX_OBJECT, telemetryComplexObject) },
                { MQTT_WS, new HashMap<String, Object>() {
                    {
                        put(TELEMETRY_NAME_INTEGER, nextInt());
                        put(TELEMETRY_NAME_LONG, nextLong());
                        put(TELEMETRY_NAME_DOUBLE, nextDouble());
                        put(TELEMETRY_NAME_FLOAT, nextFloat());
                        put(TELEMETRY_NAME_BOOLEAN, nextBoolean());
                        put(TELEMETRY_NAME_STRING, stringTelemetryValue);
                        put(TELEMETRY_NAME_COMPLEX_OBJECT, telemetryComplexObject);
                    }
                }}
        };

        return asList(data);
    }

    @Before
    public void setUpTest() throws IotHubException, IOException, URISyntaxException {
        testDevice = new TestDigitalTwinDevice(DEVICE_ID_PREFIX.concat(UUID.randomUUID().toString()), protocol);
        DigitalTwinDeviceClient digitalTwinDeviceClient = testDevice.getDigitalTwinDeviceClient();

        testComponent = new TestComponent2(TEST_COMPONENT_NAME);
        assertThat(digitalTwinDeviceClient.bindComponents(singletonList(testComponent))).isEqualTo(DIGITALTWIN_CLIENT_OK);
    }

    @Test
    public void testSendTelemetryDifferentSchema() throws IOException, InterruptedException {
        log.debug("Sending telemetry: telemetryProperties={}", telemetryProperties);
        DigitalTwinClientResult digitalTwinClientResult = testComponent.sendTelemetryPropertiesAsync(telemetryProperties).blockingGet();
        log.debug("Telemetry operation result: {}", digitalTwinClientResult);

        String expectedPayload = serialize(telemetryProperties);
        assertThat(verifyThatMessageWasReceived(testDevice.getDeviceId(), expectedPayload)).as("Verify EventHub received the sent telemetry").isTrue();
    }

    @After
    public void tearDownTest() {
        if (testDevice != null) {
            testDevice.closeAndDeleteDevice();
        }
    }

}
