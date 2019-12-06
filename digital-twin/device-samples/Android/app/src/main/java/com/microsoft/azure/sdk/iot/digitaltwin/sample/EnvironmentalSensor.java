// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.sample;

import com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinInterfaceClient;
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
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.BooleanSupplier;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.deserialize;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.serialize;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class EnvironmentalSensor extends AbstractDigitalTwinInterfaceClient {
    static final String ENVIRONMENTAL_SENSOR_INTERFACE_ID = "urn:csharp_sdk_sample:EnvironmentalSensor:1";
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

    public Single<DigitalTwinClientResult> updateTemperatureAsync(double temperature) throws IOException {
        log.debug("Temperature changed to {}.", temperature);
        uiHandler.updateTemperature(temperature);
        return sendTelemetryAsync(TELEMETRY_NAME_TEMPERATURE, serialize(temperature));
    }

    public Single<DigitalTwinClientResult> updateHumidityAsync(double humidity) throws IOException {
        log.debug("Humidity changed to {}.", humidity);
        uiHandler.updateHumidity(humidity);
        return sendTelemetryAsync(TELEMETRY_NAME_HUMIDITY, serialize(humidity));
    }

    public Single<DigitalTwinClientResult> updateStatusAsync(final boolean state) {
        log.debug("EnvironmentalSensor state is changed to {}.", state);
        uiHandler.updateOnoff(state);
        DigitalTwinReportProperty reportProperty = DigitalTwinReportProperty.builder()
                .propertyName(PROPERTY_STATE)
                .propertyValue(String.valueOf(state))
                .build();
        return reportPropertiesAsync(singletonList(reportProperty));
    }

    @Override
    public void onRegistered() {
        super.onRegistered();
        final Random random = new Random();
        Disposable temperatureReportProcess= Single.just(random)
                .delay(10, SECONDS)
                .map(new Function<Random, Double>() {
                    @Override
                    public Double apply(Random random) {
                        return random.nextDouble() * 100;
                    }
                }).flatMap(new Function<Double, Single<DigitalTwinClientResult>>() {
                    @Override
                    public Single<DigitalTwinClientResult> apply(Double temperature) throws IOException {
                        return updateTemperatureAsync(temperature);
                    }
                }).repeat()
                .subscribe(new Consumer<DigitalTwinClientResult>() {
                    @Override
                    public void accept(DigitalTwinClientResult result) {
                        log.debug("Update temperature was {}", result);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        log.debug("Update temperature failed.", throwable);
                    }
                });
        Disposable humidityReportProcess = Single.just(random)
                .delay(10, SECONDS)
                .map(new Function<Random, Double>() {
                    @Override
                    public Double apply(Random random) {
                        return random.nextDouble() * 100;
                    }
                }).flatMap(new Function<Double, Single<DigitalTwinClientResult>>() {
                    @Override
                    public Single<DigitalTwinClientResult> apply(Double humidity) throws IOException {
                        return updateHumidityAsync(humidity);
                    }
                }).repeat()
                .subscribe(new Consumer<DigitalTwinClientResult>() {
                    @Override
                    public void accept(DigitalTwinClientResult result) {
                        log.debug("Update humidity was {}", result);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        log.debug("Update humidity failed.", throwable);
                    }
                });
        log.debug("Once application quit, should dispose {} and {}.", temperatureReportProcess, humidityReportProcess);
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
                    if (PROPERTY_NAME.equals(propertyName)) {
                        uiHandler.updateName(JsonSerializer.deserialize(propertyDesired, String.class));
                    } else {
                        uiHandler.updateBrightness(JsonSerializer.deserialize(propertyDesired, Double.class));
                    }
                    DigitalTwinPropertyResponse propertyResponse = DigitalTwinPropertyResponse.builder()
                            .statusCode(STATUS_CODE_COMPLETED)
                            .statusVersion(desiredVersion)
                            .statusDescription("OK").build();
                    DigitalTwinReportProperty reportProperty = DigitalTwinReportProperty.builder()
                            .propertyName(propertyName)
                            .propertyValue(propertyDesired)
                            .propertyResponse(propertyResponse)
                            .build();
                    Disposable reportPropertiesProcess = reportPropertiesAsync(singletonList(reportProperty))
                            .subscribe(new Consumer<DigitalTwinClientResult>() {
                                @Override
                                public void accept(DigitalTwinClientResult result) {
                                    log.debug("Report property: propertyName={}, reportedValue={}, desiredVersion={} was {}", propertyName, propertyDesired, desiredVersion, result);
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) {
                                    log.debug("Report property: propertyName={}, reportedValue={}, desiredVersion={} failed.", propertyName, propertyDesired, desiredVersion, throwable);
                                }
                            });
                    log.debug("Once application quit, should dispose {}.", reportPropertiesProcess);
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
            Consumer<DigitalTwinClientResult> onSuccess = new Consumer<DigitalTwinClientResult>() {
                @Override
                public void accept(DigitalTwinClientResult result) {
                    log.debug("Command {} result is {}.", commandName, result);
                }
            };
            if (COMMAND_TURN_ON.equals(commandName)) {
                updateStatusAsync(true).subscribe(onSuccess);
                return DigitalTwinCommandResponse.builder()
                                                 .status(STATUS_CODE_COMPLETED)
                                                 .build();
            } else if (COMMAND_TURN_OFF.equals(commandName)) {
                updateStatusAsync(false).subscribe(onSuccess);
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
                runDiagnosticsAsync(requestId).subscribe(onSuccess);
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

    private Flowable<DigitalTwinClientResult> runDiagnosticsAsync(final String requestId) {
        log.debug("Starting diagnostics...");
        final AtomicInteger percentage = new AtomicInteger();
        return Single.just(requestId)
                .map(new Function<String, DigitalTwinAsyncCommandUpdate>() {
                    @Override
                    public DigitalTwinAsyncCommandUpdate apply(String s) {
                        final int progressPercentage = percentage.incrementAndGet();
                        String progressMessage = String.format("Diagnostics progress %s%%...", progressPercentage);
                        log.debug(progressMessage);
                        return DigitalTwinAsyncCommandUpdate.builder()
                                .commandName(COMMAND_RUN_DIAGNOSTICS)
                                .statusCode(STATUS_CODE_PENDING)
                                .requestId(requestId)
                                .payload(progressMessage)
                                .build();
                    }
                }).flatMap(new Function<DigitalTwinAsyncCommandUpdate, Single<DigitalTwinClientResult>>() {
                    @Override
                    public Single<DigitalTwinClientResult> apply(DigitalTwinAsyncCommandUpdate asyncCommandUpdate) {
                        return updateAsyncCommandStatusAsync(asyncCommandUpdate);
                    }
                }).delay(10, SECONDS)
                .repeatUntil(new BooleanSupplier() {
                    @Override
                    public boolean getAsBoolean() {
                        return percentage.get() >= 100;
                    }
                }).doOnComplete(new Action() {
                    @Override
                    public void run() {
                        log.debug("Diagnostics async completed.");
                    }
                });
    }

    private String createBlinkResponse(String description) throws IOException {
        Map<String, String> blinkResponse = new HashMap<>();
        blinkResponse.put("description", description);
        return serialize(blinkResponse);
    }
}
