/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubDeviceMaximumQueueDepthExceededException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubInternalServerErrorException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubInvalidOperationException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubMessageTooLargeException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubNotFoundException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubNotSupportedException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubPreconditionFailedException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnauthorizedException;
import lombok.Getter;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Endpoint;
import org.apache.qpid.proton.engine.Event;

import java.io.IOException;

@Getter
public class ProtonJExceptionParser
{
    private String error;
    private String errorDescription;

    private final IotHubException iotHubException;
    private final IOException networkException;

    private static final String DEFAULT_ERROR_DESCRIPTION = "NoErrorDescription";

    public ProtonJExceptionParser(Event event)
    {
        getErrorFromEndpoints(event.getSender(), event.getReceiver(), event.getConnection(), event.getTransport(), event.getSession(), event.getLink());

        this.iotHubException = getIotHubException(this.error, this.errorDescription);
        this.networkException = getNetworkException(this.error, this.errorDescription);
    }

    public ProtonJExceptionParser(String error, String errorDescription)
    {
        this.error = error;
        this.errorDescription = errorDescription;
        this.iotHubException = getIotHubException(this.error, this.errorDescription);
        this.networkException = getNetworkException(this.error, this.errorDescription);
    }

    public ProtonJExceptionParser(IotHubException iotHubException)
    {
        this.iotHubException = iotHubException;
        this.networkException = null;
    }

    private static IotHubException getIotHubException(String error, String errorDescription)
    {
        switch (error)
        {
            case IotHubUnauthorizedException.amqpErrorCode:
                return new IotHubUnauthorizedException(errorDescription);
            case IotHubNotFoundException.amqpErrorCode:
                return new IotHubNotFoundException(errorDescription);
            case IotHubDeviceMaximumQueueDepthExceededException.amqpErrorCode:
                return new IotHubDeviceMaximumQueueDepthExceededException(errorDescription);
            case IotHubMessageTooLargeException.amqpErrorCode:
                return new IotHubMessageTooLargeException(errorDescription);
            case IotHubInternalServerErrorException.amqpErrorCode:
                return new IotHubInternalServerErrorException(errorDescription);
            case IotHubInvalidOperationException.amqpErrorCode:
                return new IotHubInvalidOperationException(errorDescription);
            case IotHubNotSupportedException.amqpErrorCode:
                return new IotHubNotSupportedException(errorDescription);
            case IotHubPreconditionFailedException.amqpErrorCode:
                return new IotHubPreconditionFailedException(errorDescription);
        }

        if (getNetworkException(error, errorDescription) == null)
        {
            // by default, must return at least the error code and description to the user
            return new IotHubException(error + ":" + errorDescription);
        }

        return null;
    }

    private static IOException getNetworkException(String error, String errorDescription)
    {
        // all proton IO exceptions use this error code. If it isn't present, then it is likely an IoT Hub level error code instead
        if (error.equals("proton:io"))
        {
            return new IOException(errorDescription);
        }

        return null;
    }

    private ErrorCondition getErrorConditionFromEndpoint(Endpoint endpoint)
    {
        return endpoint.getCondition() != null && endpoint.getCondition().getCondition() != null ? endpoint.getCondition() : endpoint.getRemoteCondition();
    }

    private void getErrorFromEndpoints(Endpoint... endpoints)
    {
        for (Endpoint endpoint : endpoints)
        {
            if (endpoint == null)
            {
                continue;
            }

            ErrorCondition errorCondition = getErrorConditionFromEndpoint(endpoint);
            if (errorCondition == null || errorCondition.getCondition() == null)
            {
                continue;
            }

            error = errorCondition.getCondition().toString();

            if (errorCondition.getDescription() != null)
            {
                errorDescription = errorCondition.getDescription();
            }
            else
            {
                errorDescription = DEFAULT_ERROR_DESCRIPTION; //log statements can assume that if error != null, errorDescription != null, too.
            }
        }
    }
}
