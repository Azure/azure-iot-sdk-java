// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubResponseCallback;
import com.microsoft.azure.sdk.iot.device.Message;

/**
 * A packet containing the data needed for an IoT Hub transport to send a
 * message.
 */
public final class IotHubOutboundPacket
{
    private final Message message;
    private final IotHubEventCallback eventCallback;
    private final IotHubResponseCallback responseCallback;
    private final Object callbackContext;

    /**
     * Constructor.
     *
     * @param message the message to be sent.
     * @param eventCallback the callback to be invoked when a response from the IoT
     * Hub is received.
     * @param callbackContext the context to be passed to the callback.
     */
    public IotHubOutboundPacket(Message message,
            IotHubEventCallback eventCallback,
            Object callbackContext)
    {
        // Codes_SRS_IOTHUBOUTBOUNDPACKET_11_001: [The constructor shall save the message, callback, and callback context.]
        this.message = message;
        this.eventCallback = eventCallback;
        this.callbackContext = callbackContext;

        // Codes_SRS_IOTHUBOUTBOUNDPACKET_21_007: [The constructor shall set the response callback as null.]
        this.responseCallback = null;
    }

    /**
     * Constructor.
     *
     * @param message the message to be sent.
     * @param callback the callback to be invoked when a response from the IoT
     * Hub is received.
     * @param callbackContext the context to be passed to the callback.
     */
    public IotHubOutboundPacket(Message message,
                                IotHubResponseCallback callback,
                                Object callbackContext)
    {
        // Codes_SRS_IOTHUBOUTBOUNDPACKET_21_005: [The constructor shall save the message, callback, and callback context.]
        this.message = message;
        this.responseCallback = callback;
        this.callbackContext = callbackContext;

        // Codes_SRS_IOTHUBOUTBOUNDPACKET_21_008: [The constructor shall set the event callback as null.]
        this.eventCallback = null;
    }

    /**
     * Getter for the message to be sent.
     *
     * @return the message to be sent.
     */
    public Message getMessage()
    {
        // Codes_SRS_IOTHUBOUTBOUNDPACKET_11_002: [The function shall return the message given in the constructor.]
        return message;
    }

    /**
     * Getter for the callback to be invoked when a response is received.
     *
     * @return the eventCallback function. It can be {@code null}.
     */
    public IotHubEventCallback getCallback()
    {
        // Codes_SRS_IOTHUBOUTBOUNDPACKET_11_003: [The function shall return the event callback given in the constructor.]
        return eventCallback;
    }

    /**
     * Getter for the callback to be invoked when a response is received.
     *
     * @return the eventCallback function. It can be {@code null}.
     */
    public IotHubResponseCallback getResponseCallback()
    {
        // Codes_SRS_IOTHUBOUTBOUNDPACKET_21_006: [The function shall return the response callback given in the constructor.]
        return responseCallback;
    }

    /**
     * Getter for the context to be passed to the callback when it is invoked.
     *
     * @return the callback context.
     */
    public Object getContext()
    {
        // Codes_SRS_IOTHUBOUTBOUNDPACKET_11_004: [The function shall return the callback context given in the constructor.]
        return callbackContext;
    }
}
