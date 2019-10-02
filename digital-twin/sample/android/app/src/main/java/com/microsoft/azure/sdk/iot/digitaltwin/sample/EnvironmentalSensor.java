// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.sample;

import com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinInterfaceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinCallback;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinAsyncCommandUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandRequest;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinPropertyResponse;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinPropertyUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinReportProperty;
import com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.deserialize;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.serialize;
import static java.util.Collections.singletonList;

@Slf4j
public class EnvironmentalSensor extends AbstractDigitalTwinInterfaceClient {
    private static final String ENVIRONMENTAL_SENSOR_INTERFACE_ID = "urn:csharp_sdk_sample:EnvironmentalSensor:1";
    private static final String COMMAND_NOT_HANDLED_MESSAGE_PATTERN = "\"Command[%s] is not handled for interface[%s].\"";
    private static final String TELEMETRY_NAME_TEMPERATURE = "temp";
    private static final String TELEMETRY_NAME_HUMIDITY = "humid";
    private static final String COMMAND_TURN_ON = "turnon";
    private static final String COMMAND_TURN_OFF = "turnoff";
    private static final String COMMAND_BLINK = "blink";
    private static final String COMMAND_RUN_DIAGNOSTICS = "rundiagnostics";
    private static final String PROPERTY_STATE = "state";
    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_BRIGHTNESS = "brightness";
    private final UiHandler uiHandler;

    protected EnvironmentalSensor(@NonNull String digitalTwinInterfaceInstanceName, @NonNull UiHandler uiHandler) {
        super(digitalTwinInterfaceInstanceName, ENVIRONMENTAL_SENSOR_INTERFACE_ID);
        this.uiHandler = uiHandler;
    }

    public DigitalTwinClientResult updateTemperatureAsync(double temperature, @NonNull DigitalTwinCallback operationCallback) throws IOException {
        log.debug("Temperature changed to {}.", temperature);
        uiHandler.updateTemperature(temperature);
        return sendTelemetryAsync(TELEMETRY_NAME_TEMPERATURE, serialize(temperature), operationCallback, this);
    }

    public DigitalTwinClientResult updateHumidityAsync(double humidity, @NonNull DigitalTwinCallback operationCallback) throws IOException {
        log.debug("Humidity changed to {}.", humidity);
        uiHandler.updateHumidity(humidity);
        return sendTelemetryAsync(TELEMETRY_NAME_HUMIDITY, serialize(humidity), operationCallback, this);
    }

    public DigitalTwinClientResult updateStatusAsync(final boolean state) {
        log.debug("EnvironmentalSensor state is changed to {}.", state);
        uiHandler.updateOnoff(state);
        DigitalTwinReportProperty reportProperty = DigitalTwinReportProperty.builder()
                                                                            .propertyName(PROPERTY_STATE)
                                                                            .propertyValue(String.valueOf(state))
                                                                            .build();
        return reportPropertiesAsync(
                singletonList(reportProperty),
                new DigitalTwinCallback() {
                    @Override
                    public void onResult(DigitalTwinClientResult digitalTwinClientResult, Object context) {
                        log.debug(
                                "Report property: propertyName={}, reportedValue={}",
                                PROPERTY_STATE,
                                state
                        );
                    }
                },
                this
        );
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Random random = new Random();
                for (int i = 0; i < 100; i++) {
                    try {
                        final double temperature = 100 * random.nextDouble();
                        updateTemperatureAsync(
                                temperature,
                                new DigitalTwinCallback() {
                                    @Override
                                    public void onResult(DigitalTwinClientResult digitalTwinClientResult, Object context) {
                                        log.debug("Temperature updated to {} is {}.", temperature, digitalTwinClientResult);
                                    }
                                }
                        );
                        final double humidity = 100 * random.nextDouble();
                        updateHumidityAsync(
                                humidity,
                                new DigitalTwinCallback() {
                                    @Override
                                    public void onResult(DigitalTwinClientResult digitalTwinClientResult, Object context) {
                                        log.debug("Humidity updated to {} is {}.", humidity, digitalTwinClientResult);
                                    }
                                }
                        );
                        Thread.sleep(30000);
                    } catch (Exception e) {
                        log.debug("Operation failed.", e);
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onPropertyUpdate(DigitalTwinPropertyUpdate digitalTwinPropertyUpdate) {
        final String propertyName = digitalTwinPropertyUpdate.getPropertyName();
        final String propertyDesired = digitalTwinPropertyUpdate.getPropertyDesired();
        final Integer desiredVersion = digitalTwinPropertyUpdate.getDesiredVersion();
        log.debug(
                "OnPropertyUpdate called: propertyName={}, reportedValue={}, desiredVersion={}, desiredValue={}",
                propertyName,
                digitalTwinPropertyUpdate.getPropertyReported(),
                desiredVersion,
                propertyDesired
        );
        if (PROPERTY_NAME.equals(propertyName) || PROPERTY_BRIGHTNESS.equals(propertyName)) {
            if (propertyDesired != null) {
                try {
                    DigitalTwinPropertyResponse propertyResponse = DigitalTwinPropertyResponse.builder()
                            .statusCode(STATUS_CODE_COMPLETED)
                            .statusVersion(desiredVersion)
                            .statusDescription("OK").build();
                    DigitalTwinReportProperty reportProperty = DigitalTwinReportProperty.builder()
                            .propertyName(propertyName)
                            .propertyValue(propertyDesired)
                            .propertyResponse(propertyResponse)
                            .build();
                    reportPropertiesAsync(
                            singletonList(reportProperty),
                            new DigitalTwinCallback() {
                                @Override
                                public void onResult(DigitalTwinClientResult digitalTwinClientResult, Object context) {
                                    log.debug(
                                            "Report property: propertyName={}, reportedValue={}, desiredVersion={}",
                                            propertyName,
                                            propertyDesired,
                                            desiredVersion
                                    );
                                }
                            },
                            this
                    );
                    if (PROPERTY_NAME.equals(propertyName)) {
                        uiHandler.updateName(JsonSerializer.deserialize(propertyDesired, String.class));
                    } else {
                        uiHandler.updateBrightness(JsonSerializer.deserialize(propertyDesired, Double.class));
                    }
                } catch (Exception e) {
                    log.error("Invalid property: {}={}", propertyName, propertyDesired, e);
                }
            }
        } else {
            log.debug("Unexpected property[{}] received.", propertyName);
        }
    }

    @Override
    protected DigitalTwinCommandResponse onCommandReceived(DigitalTwinCommandRequest digitalTwinCommandRequest) {
        String commandName = digitalTwinCommandRequest.getCommandName();
        String requestId = digitalTwinCommandRequest.getRequestId();
        String payload = digitalTwinCommandRequest.getPayload();
        log.debug(
                "OnCommandReceived called: commandName={}, requestId={}, commandPayload={}",
                commandName,
                requestId,
                payload
        );
        try {
            if (COMMAND_TURN_ON.equals(commandName)) {
                updateStatusAsync(true);
                return DigitalTwinCommandResponse.builder()
                                                 .status(STATUS_CODE_COMPLETED)
                                                 .build();
            } else if (COMMAND_TURN_OFF.equals(commandName)) {
                updateStatusAsync(false);
                return DigitalTwinCommandResponse.builder()
                                                 .status(STATUS_CODE_COMPLETED)
                                                 .build();
            } else if (COMMAND_BLINK.equals(commandName)) {
                long interval = deserialize(payload, Long.class);
                String responsePayload = String.format("EnvironmentalSensor is blinking every %d seconds.", interval);
                uiHandler.startBlink(interval);
                log.debug(responsePayload);
                return DigitalTwinCommandResponse.builder()
                                                 .status(STATUS_CODE_COMPLETED)
                                                 .payload(createBlinkResponse(responsePayload))
                                                 .build();
            } else if (COMMAND_RUN_DIAGNOSTICS.equals(commandName)) {
                runDiagnosticsAsync(requestId);
                return DigitalTwinCommandResponse.builder()
                                                 .status(STATUS_CODE_COMPLETED)
                                                 .build();
            } else {
                String errorMessage = String.format(COMMAND_NOT_HANDLED_MESSAGE_PATTERN, commandName, ENVIRONMENTAL_SENSOR_INTERFACE_ID);
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
                                             .build();
        }
    }

    private void runDiagnosticsAsync(final String requestId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                log.debug("Starting diagnostics...");
                for (int i = 0; i < 100; i++) {
                    final int progressPercentage = i;
                    String progressMessage = String.format("Diagnostics progress %s%%...", progressPercentage);
                    log.debug(progressMessage);
                    DigitalTwinAsyncCommandUpdate asyncCommandUpdate = DigitalTwinAsyncCommandUpdate.builder()
                                                                                                    .commandName(COMMAND_RUN_DIAGNOSTICS)
                                                                                                    .statusCode(STATUS_CODE_PENDING)
                                                                                                    .requestId(requestId)
                                                                                                    .payload(progressMessage)
                                                                                                    .build();
                    DigitalTwinCallback callback = new DigitalTwinCallback() {
                        @Override
                        public void onResult(DigitalTwinClientResult digitalTwinClientResult, Object context) {
                            log.debug("Update diagnostics progress to {}% was {}", progressPercentage, digitalTwinClientResult);
                        }
                    };
                    updateAsyncCommandStatusAsync(
                            asyncCommandUpdate,
                            callback,
                            this
                    );
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        log.error("Thread sleep was interrupted.", e);
                    }
                }

                DigitalTwinAsyncCommandUpdate asyncCommandUpdate = DigitalTwinAsyncCommandUpdate.builder()
                                                                                                .commandName(COMMAND_RUN_DIAGNOSTICS)
                                                                                                .statusCode(STATUS_CODE_COMPLETED)
                                                                                                .requestId(requestId)
                                                                                                .payload("Diagnostics finished.")
                                                                                                .build();
                DigitalTwinCallback callback = new DigitalTwinCallback() {
                    @Override
                    public void onResult(DigitalTwinClientResult digitalTwinClientResult, Object context) {
                        log.debug("Update diagnostics progress to completed was {}", digitalTwinClientResult);
                    }
                };
                updateAsyncCommandStatusAsync(
                        asyncCommandUpdate,
                        callback,
                        this
                );
                log.debug("Finished diagnostics.");
            }
        }).start();
    }

    private String createBlinkResponse(String description) throws IOException {
        Map<String, String> blinkResponse = new HashMap<>();
        blinkResponse.put("description", description);
        return serialize(blinkResponse);
    }
}
