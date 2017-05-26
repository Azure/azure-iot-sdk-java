// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

/**
 * An interface for an IoT Hub response callback.
 *
 * Developers are expected to create an implementation of this interface,
 * and the transport will call {@link IotHubResponseCallback#execute(ResponseMessage, Object)}
 * upon receiving a response from an IoT Hub.
 */
public interface IotHubResponseCallback
{
    /**
     * Executes the callback.
     *
     * @param responseMessage the response from iothub that contains status code and message.
     * @param callbackContext a custom context given by the developer.
     */
    void execute(ResponseMessage responseMessage, Object callbackContext);
}
