package com.microsoft.azure.sdk.iot.digitaltwin.device;

import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinAsyncCommandUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandRequest;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinPropertyResponse;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static lombok.AccessLevel.NONE;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

/**
 * Digital Twin interface implementations to receive requests on this interface from the server (namely commands and property updates) and to send data from the interface to the server (namely reported properties and telemetry).
 */
@RequiredArgsConstructor
@Getter(PROTECTED)
public abstract class AbstractDigitalTwinInterfaceClient implements DigitalTwinCallback {
    /**
     *   The interface instance name associated with this interface Id. For example, environmentalSensor.
     */
    private final String digitalTwinInterfaceInstanceName;
    /**
     * The interfaceId of interface to be registered.  For example, urn:contoso:com:EnvironmentalSensor:1.
     */
    private final String digitalTwinInterfaceId;
    /**
     * Context passed to digitalTwinInterfaceRegisteredCallback function on interface change, command updates, and property updates.
     */
    private final Object context;
    @Setter(PACKAGE)
    @Getter(NONE)
    private DigitalTwinDeviceClient digitalTwinDeviceClient;

    /**
     * Specifies callback function to process property updates from the server.
     * The device application calls this function to provide the callback function to process property updates for this interface.
     * This <b>must</b> be called prior to the {@link AbstractDigitalTwinInterfaceClient} being registered and may only be called once for the lifetime.
     * @param digitalTwinPropertyUpdateCallback Callback to be invoked when property updated for this interface arrive.
     * @return if property updated callback is accepted or not.
     */
    protected final DigitalTwinClientResult setPropertyUpdatedCallback(@NonNull DigitalTwinPropertyUpdateCallback digitalTwinPropertyUpdateCallback) {
        // TODO
        throw new NotImplementedException();
    }

    /**
     * Specifies callback function to process commands from the server.
     * The device application calls this function to provide the callback function to process commands for this interface.
     * This <b>must</b> be called prior to the {@link AbstractDigitalTwinInterfaceClient} being registered and may only be called once for the lifetime.
     * @param digitalTwinCommandExecuteCallback  Callback to be invoked when commands for this interface arrive.
     * @return if command callback is accepted or not.
     */
    protected final DigitalTwinClientResult setCommandsCallback(@NonNull DigitalTwinCommandExecuteCallback digitalTwinCommandExecuteCallback) {
        // TODO
        throw new NotImplementedException();
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
            @NonNull final byte[] payload,
            @NonNull final DigitalTwinCallback digitalTwinTelemetryConfirmationCallback,
            final Object context) {
        // TODO
        throw new NotImplementedException();
    }

    /**
     * Sends a Digital Twin property to the server. There are two types of properties that can be modeled in the Digital Twin Definition Language (DTDL).
     * They are either configurable from the server (referred to as "writable" in the Digital Twin Definition Language). An example is a thermostat exposing its desired temperature setting.  In this case, the device also can indicate whether it has accepted the property or whether it has failed (setting the temperature above some firmware limit, for instance).
     * The other class of properties are not configurable/"writable" from the service application's perspective. Only the device can set these properties. An example is the device's manufacturer in the DeviceInformation interface.
     * Configurable properties are tied to receive updates from the server via {@link #setPropertyUpdatedCallback(DigitalTwinPropertyUpdateCallback)}.
     * Both classes of properties use this function to report the status of the property; e.g. whether the temperature was accepted in the thermostat example or simple the value of the manufacturer for DeviceInformation.
     * The only difference is that the configurable property must fill in the <b>digitalTwinPropertyResponse</b> parameter so the server knows additional status/server version/etc. of the property.
     * This function may be invoked at any time after the interface has been successfully registered and before the interface handle is destroyed.
     * It may be invoked on a callback - in particular on the application's {@link DigitalTwinPropertyUpdateCallback} - though it does not have to be.
     * The call returns immediately and the puts the data to send on a pending queue that the SDK manages. The application is notified of success or failure of the send by passing in a callback <b>digitalTwinReportedPropertyUpdatedCallback</b>.
     * The application may call this function as many times as it wants, subject obviously to the device's resource availability, and does NOT need to wait for each send's callback to arrive between sending.
     * If this function is invoked multiple times on the same <b>propertyName</b>, the server has a last-writer wins algorithm and will not persist previous property values.
     * The SDK will automatically attempt to retry reporting properties on transient errors.
     * @param propertyName Name of the property to report.  This should match the model associated with this interface.
     * @param propertyValue Value of the property to report.
     * @param digitalTwinPropertyResponse Application response to a desired property update.
     * @param digitalTwinReportedPropertyUpdatedCallback Callback be invoked when the property is successfully reported or fails.
     * @param context Context passed to {@link DigitalTwinCallback#onResult(DigitalTwinClientResult, Object)} function when it is invoked.
     * @return if this async function is accepted or not.
     */
    protected final DigitalTwinClientResult reportPropertyAsync(
            @NonNull final String propertyName,
            @NonNull final byte[] propertyValue,
            final DigitalTwinPropertyResponse digitalTwinPropertyResponse,
            @NonNull final DigitalTwinCallback digitalTwinReportedPropertyUpdatedCallback,
            final Object context) {
        // TODO
        throw new NotImplementedException();
    }

    /**
     * Sends an update of the status of a pending asynchronous command. Devices must return quickly while processing command execution callbacks in their {@link DigitalTwinCommandExecuteCallback} callback.  Commands that take longer to run - such as running a diagnostic - may be modeled as "asynchronous" commands in the Digital Twin Definition Language.
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
        // TODO
        throw new NotImplementedException();
    }
}
