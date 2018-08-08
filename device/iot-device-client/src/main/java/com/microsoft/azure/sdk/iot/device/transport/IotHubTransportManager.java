// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.ResponseMessage;

import java.io.IOException;
import java.util.Map;

/**
 * An interface for an simple and synchronous IoT Hub transport.
 */
public interface IotHubTransportManager
{
    /**
     * Establishes a communication channel with an IoT Hub. If a channel is
     * already open, the function shall do nothing.
     *
     * @throws IOException if a communication channel cannot be established.
     */
    void open() throws IOException;

    /**
     * Establishes a communication channel with an IoT Hub for a specific set of
     * topics. If a channel is already open, the function shall do nothing.
     *
     * @param topics is a list of topics to signed in.
     * @throws IOException if a communication channel cannot be established.
     */
    void open(String[] topics) throws IOException;

    /**
     * Closes all resources used to communicate with an IoT Hub. Once {@code close()} is
     * called, the transport is no longer usable. If the transport is already
     * closed, the function shall do nothing.
     *
     * @throws IOException if an error occurs in closing the transport.
     */
    void close() throws IOException;

    /**
     * Synchronously send a message to the IoT Hub. And return the response
     * with the status and message.
     *
     * @param message is the message to send.
     * @param additionalHeaders any extra headers to include
     * @return the response from IoT Hub, including status and message.
     * @throws IOException if an error occurs in sending a message.
     */
    ResponseMessage send(IotHubTransportMessage message, Map<String, String> additionalHeaders) throws IOException;

    /**
     * Synchronously receive message from the IoT Hub.
     *
     * @return the received message. If there is no new message, it will return null.
     * @throws IOException if an error occurs in receiving a message.
     */
    Message receive()  throws IOException;

}
