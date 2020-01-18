// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator;

import com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinComponent;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinAsyncCommandUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandRequest;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinReportProperty;
import io.reactivex.rxjava3.core.Single;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.serialize;
import static java.util.Collections.singletonList;

@Slf4j
public class TestComponent1 extends AbstractDigitalTwinComponent {
    public static final String TEST_INTERFACE_ID = "urn:contoso:azureiot:sdk:testinterface:1";
    public static final String TELEMETRY_NAME_INTEGER = "telemetryWithIntegerValue";
    public static final String SYNC_COMMAND_WITH_PAYLOAD = "syncCommand";
    public static final String SYNC_COMMAND_WITHOUT_PAYLOAD = "anotherSyncCommand";
    public static final String ASYNC_COMMAND_WITH_PAYLOAD = "asyncCommand";
    public static final String ASYNC_COMMAND_WITHOUT_PAYLOAD = "anotherAsyncCommand";
    public static final String PROPERTY_NAME_WRITABLE = "writableProperty";
    private static final String COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN = "Command[%s] is not handled for interface[%s].";
    private static final String ASYNC_COMMAND_PROGRESS_MESSAGE_FORMAT = "Progress of %s [%s]: %d";
    private static final String ASYNC_COMMAND_COMPLETED_MESSAGE_FORMAT = "Progress of %s [%s]: COMPLETED";

    public TestComponent1(@NonNull String digitalTwinComponentName) {
        super(digitalTwinComponentName, TEST_INTERFACE_ID);
    }

    public Single<DigitalTwinClientResult> sendTelemetryPropertiesAsync(@NonNull Map<String, Object> telemetryProperties) throws IOException {
        log.debug("Telemetry value sent: telemetryProperties={}", telemetryProperties);
        return sendTelemetryAsync(serialize(telemetryProperties));
    }

    private Single<DigitalTwinClientResult> updateWritableReportedProperty(String reportedPropertyValue) {
        log.debug("Updating Writable Property = {}", reportedPropertyValue);

        DigitalTwinReportProperty digitalTwinReportProperty = DigitalTwinReportProperty.builder()
                                                                                       .propertyName(PROPERTY_NAME_WRITABLE)
                                                                                       .propertyValue(reportedPropertyValue)
                                                                                       .build();
        return reportPropertiesAsync(singletonList(digitalTwinReportProperty));
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

        } catch (Exception e) {
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
