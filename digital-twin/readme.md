# Azure IoT Digital Twin Client SDKs API design docs

The Azure IoT Digital Twin SDKs provide functionalites for devices and modules to connect to Azure IoT Digital Twin.

## Project setup
Digital Twin project is using lombok. To setup lombok, follow steps below:

1. Install Intellij lombok plugin from files -> settings -> plugins
2. Enable annotation processing from  files -> settings -> build, execution, deployment -> compiler -> annotation processors -> check "enable annotation processing"

## DigitalTwinDeviceClient
DigitalTwinDeviceClient stands for a DCM instance. It uses one SDK DeviceClient to communicate with IoTHub and provides APIs to register interface instances. 
DigitalTwinDeviceClient API is defined as following:
```java
public DigitalTwinDeviceClient(@NonNull DeviceClient deviceClient);
public DigitalTwinClientResult registerInterfacesAsync(@NonNull final String deviceCapabilityModelId, @NonNull final List<? extends AbstractDigitalTwinInterfaceClient> digitalTwinInterfaceClients, @NonNull final DigitalTwinCallback digitalTwinInterfaceRegistrationCallback, final Object context)
```

## AbstractDigitalTwinInterfaceClient
AbstractDigitalTwinInterfaceClient is an abstract class for interface instance. It provides APIs for different IoTHub features. Developers should implement their own business by inheriting from this base class. 
DigitalTwinClient API is defined as following:

```java
protected AbstractDigitalTwinInterfaceClient(@NonNull String digitalTwinInterfaceInstanceName, @NonNull String digitalTwinInterfaceId)
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
protected final DigitalTwinClientResult sendTelemetryAsync(@NonNull final String telemetryName, @NonNull final String payload, @NonNull final DigitalTwinCallback digitalTwinTelemetryConfirmationCallback, final Object context)
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
protected final DigitalTwinClientResult reportPropertyAsync(@NonNull final List<DigitalTwinReportProperty> digitalTwinReportProperties, @NonNull final DigitalTwinCallback digitalTwinReportedPropertyUpdatedCallback, final Object context)
/**
 * Sends an update of the status of a pending asynchronous command. Devices must return quickly while processing command execution in their {@link #onCommandReceived(DigitalTwinCommandRequest)} function.  Commands that take longer to run - such as running a diagnostic - may be modeled as "asynchronous" commands in the Digital Twin Definition Language.
 * The device application invokes this function to update the status of an asynchronous command.  This status could indicate a success, a fatal failure, or else that the command is still running and provide some simple progress.
 * Values specified in the {@link DigitalTwinAsyncCommandUpdate} - in particular {@link DigitalTwinAsyncCommandUpdate#getCommandName()} that initiated the command name and its {@link DigitalTwinAsyncCommandUpdate#getRequestId()} are specified in the initial command callback's passed in {@link DigitalTwinCommandRequest#getRequestId()}.
 * @param digitalTwinAsyncCommandUpdate containing updates about the status to send to the server.
 * @param digitalTwinUpdateAsyncCommandStatusCallback Callback be invoked when the async command update is successfully reported or fails.
 * @param context Context passed to {@link DigitalTwinCallback#onResult(DigitalTwinClientResult, Object)} function when it is invoked.
 * @return if this async function is accepted or not.
 */
protected final DigitalTwinClientResult updateAsyncCommandStatusAsync(@NonNull final DigitalTwinAsyncCommandUpdate digitalTwinAsyncCommandUpdate, @NonNull final DigitalTwinCallback digitalTwinUpdateAsyncCommandStatusCallback, final Object context)
/**
 * Callback that is invoked by the Digital Twin SDK when a desired property is available from the service.
 * There are two scenarios where this callback may be invoked.  After this interface is initially registered, the Digital Twin SDK will query all desired properties on
 * it and invoke the callback.  The SDK will invoke this callback even if the property has not changed since any previous invocations, since the SDK does not
 * have a persistent cache of state. After this initial update, the SDK will also invoke this callback again whenever any properties change.
 * If multiple properties are available from the service simultaneously, this callback will be called once for each updated property.  There is no attempt to batch multiple properties into one call.
 * @param digitalTwinPropertyUpdate {@link DigitalTwinPropertyUpdate} structure filled in by the SDK with information about the updated property.
 */
protected void onPropertyUpdate(@NonNull DigitalTwinPropertyUpdate digitalTwinPropertyUpdate)
/**
 * Callback that is invoked by the Digital Twin SDK when a command is invoked from the service.
 * When a command arrives from the service for a particular interface, {@link DigitalTwinCommandRequest} specifies its callback signature.
 * @param digitalTwinCommandRequest {@link DigitalTwinCommandRequest} structure filled in by the SDK with information about the command.
 * @return {@link DigitalTwinCommandResponse} to be filled in by the application with the response code and payload to be returned to the service.
 */
protected DigitalTwinCommandResponse onCommandReceived(@NonNull DigitalTwinCommandRequest digitalTwinCommandRequest)
```

## Models
Following is structure for Digital Twin data models 

### DigitalTwinCommandRequest
```java
/** Structure filled in by the Digital Twin SDK on invoking an interface's command callback routine with information about the request.*/
@Builder
@Getter
public class DigitalTwinCommandRequest {
    /** Name of the command to execute on this interface. */
    @NonNull
    private final String commandName;
    /**
     * A server generated string passed as part of the command.
     * This is used when sending responses to asynchronous commands to act as a correlation Id and/or for diagnostics purposes.
     */
    @NonNull
    private final String requestId;
    /** Raw payload of the request. */
    private String payload;
}
```

### DigitalTwinCommandResponse
```java
/** Structure filled by the device application after processing a command on its interface and returned to the Digital Twin SDK. */
@Builder
@Getter
public class DigitalTwinCommandResponse {
    private static final int DIGITAL_TWIN_ASYNC_STATUS_CODE_PENDING = 202;
    /**
     * Status code to map back to the server.  Roughly maps to HTTP status codes.
     * To indicate that this command has been accepted but that the final response is pending, set this to {@link #DIGITAL_TWIN_ASYNC_STATUS_CODE_PENDING}.
     */
    @NonNull
    private final Integer status;
    /**
     * Response payload to send to server.  This *MUST* be allocated with <c>malloc()</c> by the application.
     * The Digital Twin SDK takes responsibility for calling <c>free()</c> on this value when the structure is returned.
     */
    private String payload;
}
```

### DigitalTwinAsyncCommandUpdate
```java
/** Structure filled in by the device application when it is updating an asynchronous command's status. */
@Builder
@Getter
public class DigitalTwinAsyncCommandUpdate {
    /** Status code to map back to the server. Roughly maps to HTTP status codes.*/
    @NonNull
    private Integer statusCode;
    /**
     * The command from the server that initiated the request that we are updating.
     * This comes from the structure {@link DigitalTwinCommandRequest#getCommandName()} passed to the device application's command callback handler.
     */
    @NonNull
    private String commandName;
    /**
     * The requestId from the server that initiated the request that we are updating.
     * This comes from the structure {@link DigitalTwinCommandRequest#getRequestId()} ()} passed to the device application's command callback handler.
     */
    @NonNull
    private String requestId;
    /** Payload that the device should send to the service. */
    private String payload;
}
```

### DigitalTwinPropertyUpdate
```java
/** Structure filled in by the Digital Twin SDK on invoking an interface's property callback routine with information about the request. */
@Builder
@Getter
public class DigitalTwinPropertyUpdate {
    /** Name of the property being update */
    @NonNull
    private final String propertyName;
    private final Integer reportedVersion;
    /**
     * Value that the device application had previously reported for this property.
     * This value may be NULL if the application never reported a property.
     * It will also be NULL when an update arrives to the given property <b>after</b> the initial callback.
     */
    private final String propertyReported;
    /**
     * Version (from the service, NOT the C structure) of this property.
     * This version should be specified when updating this property.
     */
    private final Integer desiredVersion;
    /** Number of bytes in propertyDesired. */
    private final String propertyDesired;
}
```

### DigitalTwinReportProperty
```java
/** Structure filled in by the device application when it is responding to a server initiated request to update a property. */
@Builder
@Getter
public class DigitalTwinReportProperty {
    /** Name of the property to report.  This should match the model associated with this interface. */
    @NonNull
    private final String propertyName;
    /**Value of the property to report. */
    @NonNull
    private final String propertyValue;
    /** Application response to a desired property update. */
    private final DigitalTwinPropertyResponse propertyResponse;
}
```

### DigitalTwinPropertyResponse
```java
/** Structure filled in by the device application when it is responding to a server initiated request to update a property to specify the status. */
@Builder
@Getter
public class DigitalTwinPropertyResponse {
    /**
     * This is used for server to disambiguate calls for given property.
     * It should match {@link DigitalTwinPropertyUpdate#getDesiredVersion()} that this is responding to.
     */
    @NonNull
    private final Integer statusVersion;
    /** Which should map to appropriate HTTP status code - of property update.*/
    @NonNull
    private final Integer statusCode;
    /** Friendly description string of current status of update. */
    private String statusDescription;
}
```

## Async callback and result
Following is interface for Digital Twin async operation callbacks and results

### DigitalTwinCallback
```java
/**
 * User specified callback that will be invoked on async operation completion or failure.
 */
public interface DigitalTwinCallback {
    /**
     * Function to be invoked when the async operation is successfully or fails.
     * @param digitalTwinClientResult Result for the async operation
     * @param context Context passed in when async operation is invoked.
     */
    void onResult(DigitalTwinClientResult digitalTwinClientResult, Object context);
}
```

### DigitalTwinClientResult
```java
public enum DigitalTwinClientResult {
    DIGITALTWIN_CLIENT_OK,
    DIGITALTWIN_CLIENT_ERROR_REGISTRATION_PENDING,
    DIGITALTWIN_CLIENT_ERROR_INTERFACE_ALREADY_REGISTERED,
    DIGITALTWIN_CLIENT_ERROR_INTERFACE_NOT_REGISTERED,
    DIGITALTWIN_CLIENT_ERROR
}
``` 

## Samples