// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/** An interface for an IoT Hub transport. */
public interface IotHubTransport extends Closeable
{
    /**
     * Establishes a communication channel with an IoT Hub. If a channel is
     * already open, the function shall do nothing.
     *
     * @throws IOException if a communication channel cannot be
     * established.
     */
    void open(Collection<DeviceClientConfig> deviceClientConfigs) throws IOException, TransportException;

    /**
     * Establishes a communication channel usingmultiplexing with an IoT Hub. If a channel is
     * already open, the function shall do nothing.
     *
     * @param deviceClientList the list of clients use the same transport.
     * @throws IOException if a communication channel cannot be
     * established.
     */
   // void multiplexOpen(List<DeviceClient> deviceClientList) throws IOException;

    /**
     * Closes all resources used to communicate with an IoT Hub. Once {@code close()} is
     * called, the transport is no longer usable. If the transport is already
     * closed, the function shall do nothing.
     *
     * @throws IOException if an error occurs in closing the transport.
     */
    void close() throws IOException;

    /**
     * Adds a message to the transport queue.
     *
     * @param message the message to be sent.
     * @param callback the callback to be invoked when a response for the
     * message is received.
     * @param callbackContext the context to be passed in when the callback is
     * invoked.
     */
    void addMessage(Message message,
            IotHubEventCallback callback,
            Object callbackContext);

    /**
     * Adds a message to the transport queue.
     *
     * @param message the message to be sent.
     * @param callback the callback to be invoked when a response for the
     * message is received.
     * @param callbackContext the context to be passed in when the callback is
     * invoked.
     */
    void addMessage(Message message,
                    IotHubResponseCallback callback,
                    Object callbackContext);

    /**
     * Sends all messages on the transport queue. If a previous send attempt had
     * failed, the function will attempt to resend the messages in the previous
     * attempt.
     *
     * @throws IOException if the server could not be reached.
     */
    void sendMessages() throws IOException;

    /** Invokes the callbacks for all completed requests. */
    void invokeCallbacks();

    /**
     * <p>
     * Invokes the message callback if a message is found and
     * responds to the IoT Hub on how the processed message should be
     * handled by the IoT Hub.
     * </p>
     * If no message callback is set, the function will do nothing.
     *
     * @throws TransportException if the server could not be reached.
     */
    void handleMessage() throws IOException, TransportException;

    /**
     * Returns {@code true} if the transport has no more messages to handle,
     * and {@code false} otherwise.
     *
     * @return {@code true} if the transport has no more messages to handle,
     * and {@code false} otherwise.
     */
    boolean isEmpty();

    /**
     * Registers a callback to be executed whenever the connection to the IoT Hub is lost or established.
     * 
     * @param callback the callback to be called.
     * @param callbackContext a context to be passed to the callback. Can be
     * {@code null} if no callback is provided.
     */
    void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext);
}
