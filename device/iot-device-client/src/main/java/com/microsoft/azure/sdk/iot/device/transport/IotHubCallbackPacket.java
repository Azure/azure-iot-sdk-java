// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubResponseCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.ResponseMessage;

/**
 * A packet containing the data needed for an IoT Hub transport to invoke a
 * callback.
 */
public final class IotHubCallbackPacket
{
    private final IotHubStatusCode status;
    private final ResponseMessage responseMessage;
    private final IotHubEventCallback eventCallback;
    private final IotHubResponseCallback responseCallback;
    private final Object callbackContext;

    /**
     * Constructor.
     *
     * @param status the response status code.
     * @param callback the callback to be invoked for the completed request.
     * @param callbackContext the context to be passed in to the callback.
     */
    public IotHubCallbackPacket(IotHubStatusCode status,
            IotHubEventCallback callback,
            Object callbackContext)
    {
        // Codes_SRS_IOTHUBCALLBACKPACKET_11_001: [The constructor shall save the status, callback, and callback context.]
        this.status = status;
        this.eventCallback = callback;
        this.callbackContext = callbackContext;

        // Codes_SRS_IOTHUBCALLBACKPACKET_21_007: [The constructor shall set message and responseCallback as null.]
        this.responseCallback = null;
        this.responseMessage = null;
    }

    /**
     * Constructor.
     *
     * @param responseMessage the response from iothub with status and message.
     * @param callback the callback to be invoked for the completed request.
     * @param callbackContext the context to be passed in to the callback.
     */
    public IotHubCallbackPacket(ResponseMessage responseMessage,
                                IotHubResponseCallback callback,
                                Object callbackContext)
    {
        // Codes_SRS_IOTHUBCALLBACKPACKET_21_006: [The constructor shall save the responseMessage, responseCallback, and callback context.]
        this.responseMessage = responseMessage;
        this.responseCallback = callback;
        this.callbackContext = callbackContext;

        // Codes_SRS_IOTHUBCALLBACKPACKET_21_009: [The constructor shall set status and eventCallback as null.]
        this.eventCallback = null;
        this.status = null;
    }

    /**
     * Getter for the response status code.
     *
     * @return the response status code.
     */
    public IotHubStatusCode getStatus()
    {
        // Codes_SRS_IOTHUBCALLBACKPACKET_11_002: [The function shall return the status given in the constructor.]
        return status;
    }

    /**
     * Getter for the response message.
     *
     * @return the message function.
     */
    public ResponseMessage getResponseMessage()
    {
        // Codes_SRS_IOTHUBCALLBACKPACKET_21_008: [The function shall return the response message given in the constructor.]
        return responseMessage;
    }

    /**
     * Getter for the eventCallback to be invoked for the completed request.
     *
     * @return the eventCallback function.
     */
    public IotHubEventCallback getCallback()
    {
        // Codes_SRS_IOTHUBCALLBACKPACKET_11_003: [The function shall return the eventCallback given in the constructor.]
        return eventCallback;
    }

    /**
     * Getter for the responseCallback to be invoked for the completed request.
     *
     * @return the responseCallback function.
     */
    public IotHubResponseCallback getResponseCallback()
    {
        // Codes_SRS_IOTHUBCALLBACKPACKET_21_005: [The getResponseCallback shall return the responseCallback given in the constructor.]
        return responseCallback;
    }

    /**
     * Getter for the context to be passed in to the callback when it is
     * invoked.
     *
     * @return the callback context.
     */
    public Object getContext()
    {
        // Codes_SRS_IOTHUBCALLBACKPACKET_11_004: [The function shall return the callback context given in the constructor.]
        return callbackContext;
    }
}
