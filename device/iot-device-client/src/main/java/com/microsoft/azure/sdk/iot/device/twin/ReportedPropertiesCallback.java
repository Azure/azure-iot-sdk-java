// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.twin;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

/**
 * Callback to be executed when a reported properties update request has completed.
 */
public interface ReportedPropertiesCallback
{
    /**
     * The callback that is executed when the reported properties update request has completed.
     * @param statusCode The service's status code for the request. If {@link IotHubStatusCode#OK} then the request was
     * processed succesfully.
     * @param version The new version of the reported properties after a successful update.
     * @param e The exception that was encountered if any exception was encountered.
     * @param context The context that was provided for this callback in
     * {@link com.microsoft.azure.sdk.iot.device.InternalClient#updateReportedPropertiesAsync(TwinCollection, ReportedPropertiesCallback, Object)}
     */
    void onReportedPropertiesUpdateAcknowledged(IotHubStatusCode statusCode, int version, TransportException e, Object context);
}
