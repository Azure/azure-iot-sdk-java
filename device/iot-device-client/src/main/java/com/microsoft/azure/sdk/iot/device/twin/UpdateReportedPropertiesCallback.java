// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.twin;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import lombok.Getter;

/**
 * Callback to be executed when a reported properties update request has completed.
 */
public interface UpdateReportedPropertiesCallback
{
    /**
     * The callback that is executed when the reported properties update request has completed.
     * @param statusCode The service's status code for the request. If {@link IotHubStatusCode#OK} then the request was
     * processed succesfully.
     * @param e The exception that was encountered if any exception was encountered.
     * @param context The context that was provided for this callback in
     * {@link com.microsoft.azure.sdk.iot.device.InternalClient#updateReportedPropertiesAsync(TwinCollection, UpdateReportedPropertiesCallback, Object)}
     */
    void onPropertiesUpdated(IotHubStatusCode statusCode, TransportException e, Object context);
}
