# PnP Java API Review

## Properties

```java
/**
 * Retreieve the client properties.
 * @param callback The callback to be used for receiving client properties.
 * @param callbackContext An optional user context to be sent to the callback.
 */
public void getClientPropertiesAsync(ClientPropertiesCallback callback, Object callbackContext)
```


```java
/**
 * Update the client properties.
 * @param clientProperties The client properties to send.
 * @param callback the callback to be invoked when a response is received. Can be {@code null}.
 * @param callbackContext a context to be passed to the callback. Can be {@code null} if no callback is provided.
 *
 * @throws IOException Thrown from the underlying DeviceIO
 */
public void updateClientPropertiesAsync(ClientPropertyCollection clientProperties, IotHubEventCallback callback, Object callbackContext)
```


```java
/**
 * Set the global writable properties callback handler.
 * @param writablePropertyUpdateCallback The callback to be used for writable properties.
 * @param callbackContext An optional user context to be sent to the callback.
 * @throws IOException if called when client is not opened or called before starting twin.
 */
public void subscribeToWritablePropertiesEvent(WritablePropertiesRequestsCallback writablePropertyUpdateCallback, Object callbackContext)
```

## Telemetry

```java
/**
 * Sends the TelemetryMessage to IoT hub.
 * @param telemetryMessage The user supplied telemetry message.
 * @param callback the callback to be invoked when a response is received. Can be {@code null}.
 * @param callbackContext a context to be passed to the callback. Can be {@code null} if no callback is provided.
 */
public void sendTelemetryAsync(TelemetryMessage telemetryMessage, IotHubEventCallback callback, Object callbackContext)
```

## Commands

```java
/**
 * Sets the global command handler.
 *
 * @param deviceCommandCallback Callback on which commands shall be invoked. Cannot be {@code null}.
 * @param deviceCommandCallbackContext Context for command callback. Can be {@code null}.
 * @param deviceCommandStatusCallback Callback for providing IotHub status for command. Cannot be {@code null}.
 * @param deviceCommandStatusCallbackContext Context for command status callback. Can be {@code null}.
 *
 * @throws IOException if called when client is not opened.
 */
void subscribeToCommandsInternal(DeviceCommandCallback deviceCommandCallback, Object deviceCommandCallbackContext,
                                 IotHubEventCallback deviceCommandStatusCallback, Object deviceCommandStatusCallbackContext)

```

## Properties Classes

```java
/**
 * The interface for retrieving client properties from a convention based device.
 */
public interface ClientPropertiesCallback
{
    /**
     * The method to execute for the callback.
     * @param responseStatus The response status of the client.
     * @param callbackContext User supplied context for this callback. Can be {@code null} if unused.
     */
    void execute(ClientProperties responseStatus, Object callbackContext);
}
```

## Command Classes

```java
/**
 * The command callback to be executed for all commands.
 */
public interface DeviceCommandCallback
{
    /**
     * The call to be implemented.
     * @param deviceCommandRequest A populated command request that will contain the component, command name, and payload.
     * @return The response to the command.
     */
    DeviceCommandResponse call(DeviceCommandRequest deviceCommandRequest);
}
```