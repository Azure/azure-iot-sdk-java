// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator;

import com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinInterfaceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinCallback;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
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
    private static final String COMMAND_SYNC_COMMAND_ANOTHER = "anotherSyncCommand";
    private static final String COMMAND_ASYNC_COMMAND = "asyncCommand";
    private static final String COMMAND_ASYNC_COMMAND_ANOTHER = "anotherAsyncCommand";
    private static final String PROPERTY_NAME_WRITABLE = "writableProperty";

    public SimpleTestInterfaceInstance(@NonNull String digitalTwinInterfaceInstanceName) {
        super(digitalTwinInterfaceInstanceName, SIMPLE_TEST_INTERFACE_ID);
    }

    public DigitalTwinClientResult sendIntegerTelemetry(int telemetryValue, @NonNull DigitalTwinCallback operationCallback) throws IOException {
        log.debug("Telemetry value sent: {}.", telemetryValue);
        return sendTelemetryAsync(TELEMETRY_NAME_INTEGER, serialize(telemetryValue), operationCallback, this);
    }

    public DigitalTwinClientResult updateWritableReportedProperty(String reportedPropertyValue) {
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
                updateWritableReportedProperty(payload);
                return DigitalTwinCommandResponse.builder()
                                                 .status(STATUS_CODE_COMPLETED)
                                                 .payload("Writeable property updated")
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
}
