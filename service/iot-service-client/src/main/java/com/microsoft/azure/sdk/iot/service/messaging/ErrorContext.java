// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.messaging;

import com.azure.core.annotation.Immutable;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

/**
 * The context for a given connection loss event for {@link MessageFeedbackProcessorClient}, {@link FileUploadNotificationProcessorClient},
 * and {@link MessagingClient}. The context includes the cause of the connection loss and makes a distinction between
 * network level issues (no internet) and IoT Hub level issues (resource not found, throttling, internal server error, etc.).
 */
@Immutable
public class ErrorContext
{
    /**
     * The IoT Hub level exception, if any IoT Hub level exception caused this connection loss. For example, if you attempt
     * to send a cloud to device message to a device that does not exist, this will be a {@link com.microsoft.azure.sdk.iot.service.exceptions.IotHubNotFoundException}.
     *
     * If this exception is null, then {@link #getNetworkException()} will not be null.
     */
    @Getter
    IotHubException iotHubException;

    /**
     * The network level exception, if any network level exception caused this connection loss. For example, if you attempt
     * to start your {@link MessageFeedbackProcessorClient} when your device has no internet connection, this exception
     * won't be null, and it will contain the details of what network step failed.
     *
     * If this exception is null, then {@link #getIotHubException()} will not be null.
     */
    @Getter
    IOException networkException;

    public ErrorContext(IotHubException exception)
    {
        this.iotHubException = exception;
    }

    public ErrorContext(IOException exception)
    {
        this.networkException = exception;
    }
}
