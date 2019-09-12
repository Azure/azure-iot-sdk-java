package com.microsoft.azure.sdk.iot.digitaltwin.device;

import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinAsyncCommandUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandRequest;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinPropertyUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinReportProperty;
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
    public static final int STATUS_CODE_NOT_IMPLEMENTED = 404;
    public static final int STATUS_CODE_INVALID = 400;
    public static final int STATUS_CODE_PENDING = 202;
    public static final int STATUS_CODE_COMPLETED = 200;
    private static final String COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN = "\"Command is not implemented for interface instance [%s].\"";
    /**
     *   The interface instance name associated with this interface Id. For example, environmentalSensor.
     */
    private final String digitalTwinInterfaceInstanceName;
    /**
     * The interfaceId of interface to be registered.  For example, urn:contoso:com:EnvironmentalSensor:1.
     */
    private final String digitalTwinInterfaceId;
    @Setter(PACKAGE)
    @Getter(NONE)
    private DigitalTwinDeviceClient digitalTwinDeviceClient;

    protected AbstractDigitalTwinInterfaceClient(
            @NonNull String digitalTwinInterfaceInstanceName,
            @NonNull String digitalTwinInterfaceId) {
        this.digitalTwinInterfaceInstanceName = digitalTwinInterfaceInstanceName;
        this.digitalTwinInterfaceId = digitalTwinInterfaceId;
    }

    /**
     * Sends a Digital Twin telemetry message to the server.
     * The device application calls this function to send telemetry to the server.  The call returns immediately and puts the data to send on a pending queue the SDK manages.
     * The application is notified of success or failure of the send by passing in a callback <b>digitalTwinTelemetryConfirmationCallback</b>.
     * The application may invoke this function as many times as it wants, subject to the device's resource availability.  The application does NOT need to wait for each send's callback to arrive between sending.
     * The SDK will automatically attempt to retry the telemetry send on transient errors.
     * @param telemetryName Name of the telemetry message to send.  This should match the model associated with this interface.
     * @param payload Value of the telemetry data to send.  The schema must match the data specified in the model document.
     * @param digitalTwinTelemetryConfirmationCallback Callback be invoked when the telemetry message is successfully delivered or fails.
     * @param context Context passed to {@link DigitalTwinCallback#onResult(DigitalTwinClientResult, Object)} function when it is invoked.
     * @return if this async function is accepted or not.
     */
    protected final DigitalTwinClientResult sendTelemetryAsync(
            @NonNull final String telemetryName,
            @NonNull final String payload,
            @NonNull final DigitalTwinCallback digitalTwinTelemetryConfirmationCallback,
            final Object context) {
        if (digitalTwinDeviceClient == null) {
            return DIGITALTWIN_CLIENT_ERROR_INTERFACE_NOT_REGISTERED;
        } else {
            return digitalTwinDeviceClient.sendTelemetryAsync(
                    digitalTwinInterfaceInstanceName,
                    telemetryName,
                    payload,
                    digitalTwinTelemetryConfirmationCallback,
                    context);
        }
    }

    /**
     * Sends a Digital Twin property to the server. There are two types of properties that can be modeled in the Digital Twin Definition Language (DTDL).
     * They are either configurable from the server (referred to as "writable" in the Digital Twin Definition Language). An example is a thermostat exposing its desired temperature setting.  In this case, the device also can indicate whether it has accepted the property or whether it has failed (setting the temperature above some firmware limit, for instance).
     * The other class of properties are not configurable/"writable" from the service application's perspective. Only the device can set these properties. An example is the device's manufacturer in the DeviceInformation interface.
     * Configurable properties are tied to receive updates from the server via {@link #onPropertyUpdate(DigitalTwinPropertyUpdate)}.
     * Both classes of properties use this function to report the status of the property; e.g. whether the temperature was accepted in the thermostat example or simple the value of the manufacturer for DeviceInformation.
     * The only difference is that the configurable property must fill in the <b>digitalTwinPropertyResponse</b> parameter so the server knows additional status/server version/etc. of the property.
     * This function may be invoked at any time after the interface has been successfully registered and before the interface handle is destroyed.
     * It may be invoked on {@link #onPropertyUpdate(DigitalTwinPropertyUpdate)} function though it does not have to be.
     * The call returns immediately and the puts the data to send on a pending queue that the SDK manages. The application is notified of success or failure of the send by passing in a callback <b>digitalTwinReportedPropertyUpdatedCallback</b>.
     * The application may call this function as many times as it wants, subject obviously to the device's resource availability, and does NOT need to wait for each send's callback to arrive between sending.
     * If this function is invoked multiple times on the same <b>propertyName</b>, the server has a last-writer wins algorithm and will not persist previous property values.
     * The SDK will automatically attempt to retry reporting properties on transient errors.
     * @param digitalTwinReportProperties DigitalTwin properties to be reported.
     * @param digitalTwinReportedPropertyUpdatedCallback Callback be invoked when the property is successfully reported or fails.
     * @param context Context passed to {@link DigitalTwinCallback#onResult(DigitalTwinClientResult, Object)} function when it is invoked.
     * @return if this async function is accepted or not.
     */
    protected final DigitalTwinClientResult reportPropertiesAsync(
            @NonNull final List<DigitalTwinReportProperty> digitalTwinReportProperties,
            @NonNull final DigitalTwinCallback digitalTwinReportedPropertyUpdatedCallback,
            final Object context) {
        if (digitalTwinDeviceClient == null) {
            return DIGITALTWIN_CLIENT_ERROR_INTERFACE_NOT_REGISTERED;
        } else {
            return digitalTwinDeviceClient.reportPropertiesAsync(
                    digitalTwinInterfaceInstanceName,
                    digitalTwinReportProperties,
                    digitalTwinReportedPropertyUpdatedCallback,
                    context
            );
        }
    }

    /**
     * Sends an update of the status of a pending asynchronous command. Devices must return quickly while processing command execution in their {@link #onCommandReceived(DigitalTwinCommandRequest)} function.  Commands that take longer to run - such as running a diagnostic - may be modeled as "asynchronous" commands in the Digital Twin Definition Language.
     * The device application invokes this function to update the status of an asynchronous command.  This status could indicate a success, a fatal failure, or else that the command is still running and provide some simple progress.
     * Values specified in the {@link DigitalTwinAsyncCommandUpdate} - in particular {@link DigitalTwinAsyncCommandUpdate#getCommandName()} that initiated the command name and its {@link DigitalTwinAsyncCommandUpdate#getRequestId()} are specified in the initial command callback's passed in {@link DigitalTwinCommandRequest#getRequestId()}.
     * @param digitalTwinAsyncCommandUpdate containing updates about the status to send to the server.
     * @param digitalTwinUpdateAsyncCommandStatusCallback Callback be invoked when the async command update is successfully reported or fails.
     * @param context Context passed to {@link DigitalTwinCallback#onResult(DigitalTwinClientResult, Object)} function when it is invoked.
     * @return if this async function is accepted or not.
     */
    protected final DigitalTwinClientResult updateAsyncCommandStatusAsync(
            @NonNull final DigitalTwinAsyncCommandUpdate digitalTwinAsyncCommandUpdate,
            @NonNull final DigitalTwinCallback digitalTwinUpdateAsyncCommandStatusCallback,
            final Object context) {
        if (digitalTwinDeviceClient == null) {
            return DIGITALTWIN_CLIENT_ERROR_INTERFACE_NOT_REGISTERED;
        } else {
            return digitalTwinDeviceClient.updateAsyncCommandStatusAsync(
                    digitalTwinInterfaceInstanceName,
                    digitalTwinAsyncCommandUpdate,
                    digitalTwinUpdateAsyncCommandStatusCallback,
                    context
            );
        }
    }

    /**
     * Callback that is invoked by the Digital Twin SDK when a desired property is available from the service.
     * There are two scenarios where this callback may be invoked.  After this interface is initially registered, the Digital Twin SDK will query all desired properties on
     * it and invoke the callback.  The SDK will invoke this callback even if the property has not changed since any previous invocations, since the SDK does not
     * have a persistent cache of state. After this initial update, the SDK will also invoke this callback again whenever any properties change.
     * If multiple properties are available from the service simultaneously, this callback will be called once for each updated property.  There is no attempt to batch multiple properties into one call.
     * @param digitalTwinPropertyUpdate {@link DigitalTwinPropertyUpdate} structure filled in by the SDK with information about the updated property.
     */
    protected void onPropertyUpdate(@NonNull DigitalTwinPropertyUpdate digitalTwinPropertyUpdate) {
    }

    /**
     * Callback that is invoked by the Digital Twin SDK when a command is invoked from the service.
     * When a command arrives from the service for a particular interface, {@link DigitalTwinCommandRequest} specifies its callback signature.
     * @param digitalTwinCommandRequest {@link DigitalTwinCommandRequest} structure filled in by the SDK with information about the command.
     * @return {@link DigitalTwinCommandResponse} to be filled in by the application with the response code and payload to be returned to the service.
     */
    protected DigitalTwinCommandResponse onCommandReceived(@NonNull DigitalTwinCommandRequest digitalTwinCommandRequest) {
        return DigitalTwinCommandResponse.builder()
                .status(STATUS_CODE_PENDING)
                .payload(String.format(COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN, digitalTwinInterfaceInstanceName))
                .build();
    }

    /**
     * Callback that is registration completed. Implemetation shouldn't block or throw exception.
     */
    protected void onRegistered() {
        log.debug("DigitalTwinInterfaceClient registered.");
    }
}
