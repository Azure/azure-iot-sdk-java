// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator;

import com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinInterfaceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinCallback;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinAsyncCommandUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandRequest;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinReportProperty;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.serialize;
import static java.util.Collections.singletonList;

@Slf4j
public class SimpleTestInterfaceInstance extends AbstractDigitalTwinInterfaceClient {
    private static final String SIMPLE_TEST_INTERFACE_ID = "urn:contoso:azureiot:sdk:testinterface:1";
    private static final String COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN = "Command[%s] is not handled for interface[%s].";
    private static final String TELEMETRY_NAME_INTEGER = "telemetryWithIntegerValue";
    private static final String COMMAND_SYNC_COMMAND = "syncCommand";
    private static final String COMMAND_ASYNC_COMMAND = "asyncCommand";
    private static final String PROPERTY_NAME_WRITABLE = "writableProperty";

    public SimpleTestInterfaceInstance(@NonNull String digitalTwinInterfaceInstanceName) {
        super(digitalTwinInterfaceInstanceName, SIMPLE_TEST_INTERFACE_ID);
    }

    public DigitalTwinClientResult sendIntegerTelemetry(int telemetryValue, @NonNull DigitalTwinCallback operationCallback) throws IOException {
        log.debug("Telemetry value sent: {}.", telemetryValue);
        return sendTelemetryAsync(TELEMETRY_NAME_INTEGER, serialize(telemetryValue), operationCallback, this);
    }

    private DigitalTwinClientResult updateWritableReportedProperty(String reportedPropertyValue) {
        log.debug("Updating Writable Property = {}", reportedPropertyValue);

        DigitalTwinReportProperty digitalTwinReportProperty = DigitalTwinReportProperty.builder()
                                                                                       .propertyName(PROPERTY_NAME_WRITABLE)
                                                                                       .propertyValue(reportedPropertyValue)
                                                                                       .build();
        return reportPropertiesAsync(singletonList(digitalTwinReportProperty),
                new DigitalTwinCallback() {

                    @Override
                    public void onResult(DigitalTwinClientResult digitalTwinClientResult, Object context) {
                        log.debug("Reported property update: propertyName={}, reportedValue={}", PROPERTY_NAME_WRITABLE, reportedPropertyValue);
                    }
                },
                this);
    }

    @Override
    protected  DigitalTwinCommandResponse onCommandReceived(DigitalTwinCommandRequest digitalTwinCommandRequest) {
        String commandName = digitalTwinCommandRequest.getCommandName();
        String requestId = digitalTwinCommandRequest.getRequestId();
        String payload = digitalTwinCommandRequest.getPayload();

        log.debug("OnCommandReceived called: commandName={}, requestId={}, commandPayload={}",
                commandName,
                requestId,
                payload);
        try{
            if (COMMAND_SYNC_COMMAND.equals(commandName)) {
                DigitalTwinClientResult digitalTwinClientResult = updateWritableReportedProperty(payload);
                return DigitalTwinCommandResponse.builder()
                                                 .status(STATUS_CODE_COMPLETED)
                                                 .payload(String.format("Writeable property updated: %s", digitalTwinClientResult))
                                                 .build();
            } else if (COMMAND_ASYNC_COMMAND.equals(commandName)) {
                runAsyncCommand(requestId, commandName);
                return DigitalTwinCommandResponse.builder()
                                                 .status(STATUS_CODE_PENDING)
                                                 .payload(String.format("Running command: %s", commandName))
                                                 .build();
            } else {
                String errorMessage = String.format(COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN, commandName, SIMPLE_TEST_INTERFACE_ID);
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

    private void runAsyncCommand(String requestId, String commandName) {
        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                String progressPercentage = String.format("Progress of %s: %d", commandName, i / 5 * 100);
                log.debug(">> Executing Async task: {}", progressPercentage);

                DigitalTwinAsyncCommandUpdate digitalTwinAsyncCommandUpdate = DigitalTwinAsyncCommandUpdate.builder()
                                                                                                           .commandName(commandName)
                                                                                                           .requestId(requestId)
                                                                                                           .statusCode(STATUS_CODE_PENDING)
                                                                                                           .payload(progressPercentage)
                                                                                                           .build();
                DigitalTwinCallback digitalTwinCallback = (digitalTwinClientResult, context) -> log.debug("Execute async command: {}; result: {}", progressPercentage, digitalTwinClientResult);
                updateAsyncCommandStatusAsync(digitalTwinAsyncCommandUpdate, digitalTwinCallback, this);

                try {
                    Thread.sleep(10 * 1000);
                }
                catch (InterruptedException e) {
                    log.error("Thread sleep was interrupted.", e);
                }
            }

            DigitalTwinAsyncCommandUpdate digitalTwinAsyncCommandUpdate = DigitalTwinAsyncCommandUpdate.builder()
                                                                                                       .commandName(commandName)
                                                                                                       .requestId(requestId)
                                                                                                       .statusCode(STATUS_CODE_COMPLETED)
                                                                                                       .build();
            DigitalTwinCallback digitalTwinCallback = (digitalTwinClientResult, context) -> log.debug("Execute async command: completed; result: {}", digitalTwinClientResult);
            updateAsyncCommandStatusAsync(digitalTwinAsyncCommandUpdate, digitalTwinCallback, this);

            log.debug("Async command execution complete.");
        }).start();

    }
}
