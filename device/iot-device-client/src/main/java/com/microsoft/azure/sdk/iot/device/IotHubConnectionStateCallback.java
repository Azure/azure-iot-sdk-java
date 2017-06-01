package com.microsoft.azure.sdk.iot.device;

/**
 * An interface for an IoT Hub connection state callback.
 *
 * Developers are expected to create an implementation of this interface,
 * and the transport will call {@link IotHubConnectionStateCallback#connectionUp()}
 * when connection is established with the IoT Hub and call
 * {@link IotHubConnectionStateCallback#connectionDown()} when connection is lost.
 */
public interface IotHubConnectionStateCallback {
    /**
     * Method executed when the connection with the IoTHub is lost.
     */
    void connectionUp();

    /**
     * Method executed when the connection with the IoTHub is established.
     */
    void connectionDown();
}