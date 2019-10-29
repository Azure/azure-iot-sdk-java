// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.device;

import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinAsyncCommandUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandRequest;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinPropertyUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinReportProperty;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_INTERFACE_NOT_REGISTERED;
import static lombok.AccessLevel.NONE;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

/**
 * Digital Twin interface implementations to receive requests on this interface from the server (namely commands and property updates) and to send data from the interface to the server (namely reported properties and telemetry).
 */
@Getter(PROTECTED)
@Slf4j
public abstract class AbstractDigitalTwinInterfaceClient {
    /** Indicates the function or command is not implemented. */
    public static final int STATUS_CODE_NOT_IMPLEMENTED = 404;
    /** Indicates the content is in a bad format or with invalid values. */
    public static final int STATUS_CODE_INVALID = 400;
    /** Indicates the content or operation is accepted and an async process is started. */
    public static final int STATUS_CODE_PENDING = 202;
    /** Indicates the content or operation is successfully completed. */
    public static final int STATUS_CODE_COMPLETED = 200;
    static final String COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN = "\"Command is not implemented for interface instance [%s].\"";
    /**
     * The interface instance name associated with this interface Id. For example, environmentalSensor.
     */
    private final String digitalTwinInterfaceInstanceName;
    /**
     * The interfaceId of interface to be registered.  For example, urn:contoso:com:EnvironmentalSensor:1.
     */
    private final String digitalTwinInterfaceId;
    @Setter(PACKAGE)
    @Getter(NONE)
    private DigitalTwinDeviceClient digitalTwinDeviceClient;

    protected AbstractDigitalTwinInterfaceClient(@NonNull String digitalTwinInterfaceInstanceName, @NonNull String digitalTwinInterfaceId) {
        this.digitalTwinInterfaceInstanceName = digitalTwinInterfaceInstanceName;
        this.digitalTwinInterfaceId = digitalTwinInterfaceId;
    }

    /**
     * Sends a Digital Twin telemetry message to the server.
     * The device application calls this function to send telemetry to the server.  The call returns immediately and puts the data to send on a pending queue that the SDK manages. The application can either subscribe to handle the result or block to get the result.
     * The application may invoke this function as many times as it wants, subject to the device's resource availability. The application does NOT need to wait for each send to finish.
     * The SDK will automatically attempt to retry the telemetry send on transient errors.
     *
     * @return Result of this async function.
     */
    protected final Single<DigitalTwinClientResult> sendTelemetryAsync(@NonNull final String telemetryName, @NonNull final String payload) {
        if (digitalTwinDeviceClient == null) {
            log.debug("Send TelemetryAsync from interface instance={}, telemetryName={} failed: interface instance is not registered.", digitalTwinInterfaceInstanceName, telemetryName);
            return Single.just(DIGITALTWIN_CLIENT_ERROR_INTERFACE_NOT_REGISTERED);
        } else {
            log.debug("Sending TelemetryAsync from interface instance={}, telemetryName={}...", digitalTwinInterfaceInstanceName, telemetryName);
            return digitalTwinDeviceClient.sendTelemetryAsync(
                    digitalTwinInterfaceInstanceName,
                    telemetryName,
                    payload
            ).singleOrError();
        }
    }

    /**
     * Sends a Digital Twin telemetry message to the server.
     * The device application calls this function to send telemetry to the server. The call will be blocked and will return the result once telemetry is processed.
     * The SDK will automatically attempt to retry the telemetry send on transient errors.
     *
     * @return Result of this sync function.
     */
    protected final DigitalTwinClientResult sendTelemetry(@NonNull final String telemetryName, @NonNull final String payload) {
        return sendTelemetryAsync(telemetryName, payload).blockingGet();
    }

    /**
     * Sends a Digital Twin property to the server. There are two types of properties that can be modeled in the Digital Twin Definition Language (DTDL).
     * They are either configurable from the server (referred to as "writable" in the Digital Twin Definition Language). An example is a thermostat exposing its desired temperature setting.  In this case, the device also can indicate whether it has accepted the property or whether it has failed (setting the temperature above some firmware limit, for instance).
     * The other class of properties are not configurable/"writable" from the service application's perspective. Only the device can set these properties. An example is the device's manufacturer in the DeviceInformation interface.
     * Configurable properties are tied to receive updates from the server via {@link #onPropertyUpdate(DigitalTwinPropertyUpdate)}.
     * Both classes of properties use this function to report the status of the property; e.g. whether the temperature was accepted in the thermostat example or simply the value of the manufacturer for DeviceInformation.
     * The only difference is that the configurable property must fill in the <b>digitalTwinPropertyResponse</b> parameter so the server knows additional status/server version/etc. of the property.
     * This function may be invoked at any time after the interface has been successfully registered.
     * It may be invoked on {@link #onPropertyUpdate(DigitalTwinPropertyUpdate)} function though it does not have to be.
     * The call returns immediately and the puts the data to send on a pending queue that the SDK manages. The application can either subscribe to handle the result or block to get the result.
     * The application may call this function as many times as it wants, subject to the device's resource availability, and does NOT need to wait for each send to finish.
     * If this function is invoked multiple times on the same <b>propertyName</b>, the server has a last-writer wins algorithm and will not persist previous property values.
     * The SDK will automatically attempt to retry on transient errors.
     *
     * @param digitalTwinReportProperties DigitalTwin properties to be reported.
     * @return Result of this async function.
     */
    protected final Single<DigitalTwinClientResult> reportPropertiesAsync(@NonNull final List<DigitalTwinReportProperty> digitalTwinReportProperties) {
        log.debug("Reporting PropertiesAsync from interface instance={}", digitalTwinInterfaceInstanceName);
        if (digitalTwinDeviceClient == null) {
            log.debug("Report PropertiesAsync from interface instance={} failed: interface instance is not registered.", digitalTwinInterfaceInstanceName);
            return Single.just(DIGITALTWIN_CLIENT_ERROR_INTERFACE_NOT_REGISTERED);
        } else {
            log.debug("Reporting Properties from interface instance={}.", digitalTwinInterfaceInstanceName);
            return digitalTwinDeviceClient.reportPropertiesAsync(
                    digitalTwinInterfaceInstanceName,
                    digitalTwinReportProperties
            ).singleOrError();
        }
    }

    /**
     * Sends a Digital Twin property to the server. There are two types of properties that can be modeled in the Digital Twin Definition Language (DTDL).
     * They are either configurable from the server (referred to as "writable" in the Digital Twin Definition Language). An example is a thermostat exposing its desired temperature setting.  In this case, the device also can indicate whether it has accepted the property or whether it has failed (setting the temperature above some firmware limit, for instance).
     * The other class of properties are not configurable/"writable" from the service application's perspective. Only the device can set these properties. An example is the device's manufacturer in the DeviceInformation interface.
     * Configurable properties are tied to receive updates from the server via {@link #onPropertyUpdate(DigitalTwinPropertyUpdate)}.
     * Both classes of properties use this function to report the status of the property; e.g. whether the temperature was accepted in the thermostat example or simply the value of the manufacturer for DeviceInformation.
     * The only difference is that the configurable property must fill in the <b>digitalTwinPropertyResponse</b> parameter so the server knows additional status/server version/etc. of the property.
     * This function may be invoked at any time after the interface has been successfully registered.
     * It may be invoked on {@link #onPropertyUpdate(DigitalTwinPropertyUpdate)} function though it does not have to be.
     * The call will be blocked and will return the result once the reported property is processed.
     * If this function is invoked multiple times on the same <b>propertyName</b>, the server has a last-writer wins algorithm and will not persist previous property values.
     * The SDK will automatically attempt to retry on transient errors.
     *
     * @param digitalTwinReportProperties DigitalTwin properties to be reported.
     * @return Result of this sync function.
     */
    protected final DigitalTwinClientResult reportProperties(@NonNull final List<DigitalTwinReportProperty> digitalTwinReportProperties) {
        return reportPropertiesAsync(digitalTwinReportProperties).blockingGet();
    }

    /**
     * Sends an update of the status of a pending asynchronous command. Devices must return quickly while processing command execution in their {@link #onCommandReceived(DigitalTwinCommandRequest)} function.  Commands that take longer to run - such as running a diagnostic - may be modeled as "asynchronous" commands in the Digital Twin Definition Language.
     * The device application invokes this function to update the status of an asynchronous command.  This status could indicate a success, a fatal failure, or else that the command is still running and provide some simple progress.
     * Values specified in the {@link DigitalTwinAsyncCommandUpdate} - in particular {@link DigitalTwinAsyncCommandUpdate#getCommandName()} that initiated the command name and its {@link DigitalTwinAsyncCommandUpdate#getRequestId()} are specified in the initial command callback's passed in {@link DigitalTwinCommandRequest#getRequestId()}.
     * The call returns immediately and the puts the data to send on a pending queue that the SDK manages. The application can either subscribe to handle the result or block to get the result.
     * The application may call this function as many times as it wants, subject to the device's resource availability, and does NOT need to wait for each send to finish.
     * The SDK will automatically attempt to retry on transient errors.
     *
     * @param digitalTwinAsyncCommandUpdate containing updates about the status to send to the server.
     * @return Result of this async function.
     */
    protected final Single<DigitalTwinClientResult> updateAsyncCommandStatusAsync(@NonNull final DigitalTwinAsyncCommandUpdate digitalTwinAsyncCommandUpdate) {
        if (digitalTwinDeviceClient == null) {
            return Single.just(DIGITALTWIN_CLIENT_ERROR_INTERFACE_NOT_REGISTERED);
        } else {
            log.debug("Updating async command status from interface instance={}.", digitalTwinInterfaceInstanceName);
            return digitalTwinDeviceClient.updateAsyncCommandStatusAsync(
                    digitalTwinInterfaceInstanceName,
                    digitalTwinAsyncCommandUpdate
            ).singleOrError();
        }
    }

    /**
     * Sends an update of the status of a pending asynchronous command. Devices must return quickly while processing command execution in their {@link #onCommandReceived(DigitalTwinCommandRequest)} function.  Commands that take longer to run - such as running a diagnostic - may be modeled as "asynchronous" commands in the Digital Twin Definition Language.
     * The device application invokes this function to update the status of an asynchronous command.  This status could indicate a success, a fatal failure, or else that the command is still running and provide some simple progress.
     * Values specified in the {@link DigitalTwinAsyncCommandUpdate} - in particular {@link DigitalTwinAsyncCommandUpdate#getCommandName()} that initiated the command name and its {@link DigitalTwinAsyncCommandUpdate#getRequestId()} are specified in the initial command callback's passed in {@link DigitalTwinCommandRequest#getRequestId()}.
     * The call will be blocked and will return the result once command status updated is processed.
     * The SDK will automatically attempt to retry reporting properties on transient errors.
     *
     * @param digitalTwinAsyncCommandUpdate containing updates about the status to send to the server.
     * @return Result of this async function.
     */
    protected final DigitalTwinClientResult updateAsyncCommandStatus(@NonNull final DigitalTwinAsyncCommandUpdate digitalTwinAsyncCommandUpdate) {
        return updateAsyncCommandStatusAsync(digitalTwinAsyncCommandUpdate).blockingGet();
    }

    /**
     * Callback that is invoked by the Digital Twin SDK when a desired property is available from the service.
     * There are two scenarios where this callback may be invoked.  After this interface is initially registered, the Digital Twin SDK will query all desired properties on
     * it and invoke the callback.  The SDK will invoke this callback even if the property has not changed since any previous invocations, since the SDK does not
     * have a persistent cache of state. After this initial update, the SDK will also invoke this callback again whenever any properties change.
     * If multiple properties are available from the service simultaneously, this callback will be called once for each updated property.  There is no attempt to batch multiple properties into one call.
     *
     * @param digitalTwinPropertyUpdate {@link DigitalTwinPropertyUpdate} structure filled in by the SDK with information about the updated property.
     */
    protected void onPropertyUpdate(@NonNull DigitalTwinPropertyUpdate digitalTwinPropertyUpdate) {
        log.debug("OnPropertyUpdate is ignored since it's not implemented by interface instance {}", digitalTwinInterfaceInstanceName);
    }

    /**
     * Callback that is invoked by the Digital Twin SDK when a command is invoked from the service.
     * When a command arrives from the service for a particular interface, {@link DigitalTwinCommandRequest} specifies its callback signature.
     *
     * @param digitalTwinCommandRequest {@link DigitalTwinCommandRequest} structure filled in by the SDK with information about the command.
     * @return {@link DigitalTwinCommandResponse} to be filled in by the application with the response code and payload to be returned to the service.
     */
    protected DigitalTwinCommandResponse onCommandReceived(@NonNull DigitalTwinCommandRequest digitalTwinCommandRequest) {
        log.debug("OnCommandReceived returns {} since it's not implemented by interface instance {}", STATUS_CODE_NOT_IMPLEMENTED, digitalTwinInterfaceInstanceName);
        return DigitalTwinCommandResponse.builder()
                                         .status(STATUS_CODE_NOT_IMPLEMENTED)
                                         .payload(String.format(COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN, digitalTwinInterfaceInstanceName))
                                         .build();
    }

    /**
     * Called once registration is completed. Implementation shouldn't block or throw exception.
     */
    protected void onRegistered() {
        log.debug("{} was registered.", digitalTwinInterfaceInstanceName);
    }
}
