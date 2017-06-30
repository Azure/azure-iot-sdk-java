package com.microsoft.azure.sdk.iot.device;

/**
 * An interface for an IoT Hub connection state callback.
 *
 * Developers are expected to create an implementation of this interface,
 * and the transport will call {@link IotHubConnectionStateCallback#execute(IotHubConnectionState, Object)}
 * passing in the connection status (down, success, etc.).
 */
public interface IotHubConnectionStateCallback {
    void execute(IotHubConnectionState state, Object callbackContext);
}