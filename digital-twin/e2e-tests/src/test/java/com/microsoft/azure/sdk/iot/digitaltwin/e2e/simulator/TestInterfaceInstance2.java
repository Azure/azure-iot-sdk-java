// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator;

import com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinInterfaceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.*;
import io.reactivex.rxjava3.core.Single;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.serialize;
import static java.util.Collections.singletonList;

@Slf4j
public class TestInterfaceInstance2 extends AbstractDigitalTwinInterfaceClient {
    public static final String TEST_INTERFACE_ID = "urn:contoso:azureiot:sdk:testinterface2:2";
    public static final String TELEMETRY_NAME_INTEGER = "telemetryWithIntegerValue";
    public static final String TELEMETRY_NAME_LONG = "telemetryWithLongValue";
    public static final String TELEMETRY_NAME_DOUBLE = "telemetryWithDoubleValue";
    public static final String TELEMETRY_NAME_FLOAT = "telemetryWithFloatValue";
    public static final String TELEMETRY_NAME_BOOLEAN = "telemetryWithBooleanValue";
    public static final String TELEMETRY_NAME_STRING = "telemetryWithStringValue";
    public static final String TELEMETRY_NAME_DATE = "telemetryWithDateValue";
    public static final String TELEMETRY_NAME_TIME = "telemetryWithTimeValue";
    public static final String TELEMETRY_NAME_DATETIME = "telemetryWithDateTimeValue";
    public static final String TELEMETRY_NAME_DURATION = "telemetryWithDurationValue";
    public static final String TELEMETRY_NAME_ARRAY = "telemetryWithIntegerArrayValue";
    public static final String TELEMETRY_NAME_MAP = "telemetryWithMapValue";
    public static final String TELEMETRY_NAME_ENUM = "telemetryWithEnumValue";
    public static final String TELEMETRY_NAME_COMPLEX_OBJECT = "telemetryWithComplexValueComplexObject";
    public static final String TELEMETRY_NAME_COMPLEX_VALUE = "telemetryWithComplexValue";
    public static final String SYNC_COMMAND_WITH_PAYLOAD = "syncCommand";
    public static final String SYNC_COMMAND_WITHOUT_PAYLOAD = "anotherSyncCommand";
    public static final String ASYNC_COMMAND_WITH_PAYLOAD = "asyncCommand";
    public static final String ASYNC_COMMAND_WITHOUT_PAYLOAD = "anotherAsyncCommand";
    public static final String PROPERTY_NAME_WRITABLE = "writableProperty";
    public static final String PROPERTY_NAME_2_WRITABLE = "anotherWritableProperty";
    public static final String PROPERTY_NAME_READONLY = "readOnlyProperty";
    public static final String COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN = "Command[%s] is not handled for interface[%s].";
    private static final String ASYNC_COMMAND_PROGRESS_MESSAGE_FORMAT = "Progress of %s [%s]: %d";
    private static final String ASYNC_COMMAND_COMPLETED_MESSAGE_FORMAT = "Progress of %s [%s]: COMPLETED";
    private static final int MAX_WAIT_FOR_PROPERTY_UPDATE_IN_SECONDS = 30;

    private static String interfaceInstanceName;
    private Map<String, String> propertyUpdatesReceived;

    public TestInterfaceInstance2(@NonNull String digitalTwinInterfaceInstanceName) {
        super(digitalTwinInterfaceInstanceName, TEST_INTERFACE_ID);
        interfaceInstanceName = digitalTwinInterfaceInstanceName;
        propertyUpdatesReceived = new ConcurrentHashMap<>();
    }

    @Override
    public void onRegistered() {
        log.debug("Interface Instance registered with name: {}", interfaceInstanceName);
    }

    public Single<DigitalTwinClientResult> sendTelemetry(@NonNull String telemetryName, @NonNull Object telemetryValue) throws IOException {
        log.debug("Telemetry value sent: telemetryName={}; telemetryValue={}", telemetryName, telemetryValue);
        return sendTelemetryAsync(telemetryName, serialize(telemetryValue));
    }

    public Single<DigitalTwinClientResult> updatePropertyFromDevice(String propertyName, String propertyValue) {
        log.debug("Updating property: propertyName={}, propertyValue={}", propertyName, propertyValue);

        DigitalTwinReportProperty digitalTwinReportProperty = DigitalTwinReportProperty.builder()
                                                                                       .propertyName(propertyName)
                                                                                       .propertyValue(propertyValue)
                                                                                       .build();
        return reportPropertiesAsync(singletonList(digitalTwinReportProperty));
    }

    @Override
    protected void onPropertyUpdate(@NonNull DigitalTwinPropertyUpdate digitalTwinPropertyUpdate) {
        log.debug("OnPropertyUpdate was received: ");
        log.debug("getPropertyName: {}", digitalTwinPropertyUpdate.getPropertyName());
        log.debug("getPropertyReported: {}", digitalTwinPropertyUpdate.getPropertyReported());
        log.debug("getPropertyDesired: {}", digitalTwinPropertyUpdate.getPropertyDesired());
        log.debug("getDesiredVersion: {}", digitalTwinPropertyUpdate.getDesiredVersion());
        propertyUpdatesReceived.put(digitalTwinPropertyUpdate.getPropertyName(), digitalTwinPropertyUpdate.getPropertyDesired());
    }

    public boolean verifyIfPropertyUpdateWasReceived(String expectedPropertyName, String expectedValue) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + MAX_WAIT_FOR_PROPERTY_UPDATE_IN_SECONDS) {
            if (propertyUpdatesReceived.entrySet().stream()
                                       .anyMatch(stringStringEntry -> stringStringEntry.getKey().equals(expectedPropertyName) && stringStringEntry.getValue().equals(expectedValue))) {
                return true;
            }
        }

        log.debug("Timed out before received property update: propertyName={}, propertyValue={}", expectedPropertyName, expectedValue);
        return false;
    }

    @Override
    protected DigitalTwinCommandResponse onCommandReceived(DigitalTwinCommandRequest digitalTwinCommandRequest) {
        String commandName = digitalTwinCommandRequest.getCommandName();
        String requestId = digitalTwinCommandRequest.getRequestId();
        String payload = digitalTwinCommandRequest.getPayload();

        log.debug("OnCommandReceived called: commandName={}, requestId={}, commandPayload={}",
                commandName,
                requestId,
                payload);
        try {
            if (SYNC_COMMAND_WITH_PAYLOAD.equals(commandName) || SYNC_COMMAND_WITHOUT_PAYLOAD.equals(commandName)) {
                return DigitalTwinCommandResponse.builder()
                                                 .status(STATUS_CODE_COMPLETED)
                                                 .payload(payload)
                                                 .build();
            } else if (ASYNC_COMMAND_WITH_PAYLOAD.equals(commandName) || ASYNC_COMMAND_WITHOUT_PAYLOAD.equals(commandName)) {
                runAsyncCommand(requestId, commandName, payload);
                return DigitalTwinCommandResponse.builder()
                                                 .status(STATUS_CODE_PENDING)
                                                 .payload(payload)
                                                 .build();
            } else {
                String errorMessage = String.format(COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN, commandName, TEST_INTERFACE_ID);
                log.debug(errorMessage);
                return DigitalTwinCommandResponse.builder()
                                                 .status(STATUS_CODE_NOT_IMPLEMENTED)
                                                 .payload(errorMessage)
                                                 .build();
            }
        }
        catch (Exception e) {
            log.debug("OnCommandReceived failed.", e);
            return DigitalTwinCommandResponse.builder()
                                             .status(500)
                                             .payload(e.getMessage())
                                             .build();
        }
    }

    private void runAsyncCommand(String requestId, String commandName, String payload) {
        new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                String progressPercentage = String.format(ASYNC_COMMAND_PROGRESS_MESSAGE_FORMAT, commandName, payload, i * 100 / 5);
                log.debug(">> Executing Async task: {}", progressPercentage);

                DigitalTwinAsyncCommandUpdate digitalTwinAsyncCommandUpdate = DigitalTwinAsyncCommandUpdate.builder()
                                                                                                           .commandName(commandName)
                                                                                                           .requestId(requestId)
                                                                                                           .statusCode(STATUS_CODE_PENDING)
                                                                                                           .payload(progressPercentage)
                                                                                                           .build();
                DigitalTwinClientResult digitalTwinClientResult = updateAsyncCommandStatusAsync(digitalTwinAsyncCommandUpdate).blockingGet();
                log.debug("Execute async command: {}; result: {}", progressPercentage, digitalTwinClientResult);

                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    log.error("Thread sleep was interrupted.", e);
                }
            }

            String payloadMessage = String.format(ASYNC_COMMAND_COMPLETED_MESSAGE_FORMAT, commandName, payload);
            DigitalTwinAsyncCommandUpdate digitalTwinAsyncCommandUpdate = DigitalTwinAsyncCommandUpdate.builder()
                                                                                                       .commandName(commandName)
                                                                                                       .requestId(requestId)
                                                                                                       .statusCode(STATUS_CODE_COMPLETED)
                                                                                                       .payload(payloadMessage)
                                                                                                       .build();
            DigitalTwinClientResult digitalTwinClientResult = updateAsyncCommandStatusAsync(digitalTwinAsyncCommandUpdate).blockingGet();
            log.debug("Execute async command: completed; result: {}", digitalTwinClientResult);

            log.debug("Async command execution complete.");
        }).start();
    }
}
