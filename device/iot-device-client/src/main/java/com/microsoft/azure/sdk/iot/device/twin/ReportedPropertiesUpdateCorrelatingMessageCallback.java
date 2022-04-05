// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.twin;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;

/**
 * Detailed state notification callback for tracking a particular
 * {@link com.microsoft.azure.sdk.iot.device.InternalClient#updateReportedPropertiesAsync(TwinCollection, ReportedPropertiesUpdateCorrelatingMessageCallback, Object)}
 * request.
 *
 * Users who don't need all this information are advised to use
 * {@link com.microsoft.azure.sdk.iot.device.InternalClient#updateReportedPropertiesAsync(TwinCollection, ReportedPropertiesCallback, Object)}
 * instead.
 */
public interface ReportedPropertiesUpdateCorrelatingMessageCallback
{
    /**
     * Called when the message has been queued to the transport.
     *
     * @param message The request message queued by the transport.
     * @param callbackContext The context sent with the message.
     */
    void onRequestQueued(Message message, Object callbackContext);

    /**
     * Called when the message request has been sent by the transport.
     *
     * @param message The request message sent by the transport.
     * @param callbackContext The context sent with the message.
     */
    void onRequestSent(Message message, Object callbackContext);

    /**
     * Called when the message request has been sent and IoT hub has acknowledged the request.
     *
     * @param message The request message sent by the transport.
     * @param callbackContext The context sent with the message.
     * @param e The error or exception given by the transport. If there are no errors this will be {@code null}.
     */
    void onRequestAcknowledged(Message message, Object callbackContext, IotHubClientException e);

    /**
     * Called when a response to the sent message has been sent by IoT hub and has been receieved by the transport.
     *
     * @param message The response message received by the transport.
     * @param callbackContext The context sent with the message.
     * @param statusCode The status of the update reported properties onMethodInvoked as a whole.
     * @param response The new version of the reported properties after a successful update. If the client updating
     * its reported properties is connected to Edgehub instead of IoT Hub, then this version won't change since Edgehub
     * does not apply this reported properties update immediately.
     * @param e The error or exception given by the transport. If there are no errors this will be {@code null}.
     */
    void onResponseReceived(Message message, Object callbackContext, IotHubStatusCode statusCode, ReportedPropertiesUpdateResponse response, IotHubClientException e);

    /**
     * Called when a response to the message has been sent by IoT hub and has been acknowledged by the transport.
     *
     * @param message The response message queued to the transport.
     * @param callbackContext The context sent with the message.
     */
    void onResponseAcknowledged(Message message, Object callbackContext);
}
