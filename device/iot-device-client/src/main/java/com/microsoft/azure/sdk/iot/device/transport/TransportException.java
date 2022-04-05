/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientRegistrationException;
import com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions.*;
import com.microsoft.azure.sdk.iot.device.transport.https.exceptions.InternalServerErrorException;
import com.microsoft.azure.sdk.iot.device.transport.https.exceptions.ServerBusyException;
import com.microsoft.azure.sdk.iot.device.transport.https.exceptions.ThrottledException;
import com.microsoft.azure.sdk.iot.device.transport.https.exceptions.UnauthorizedException;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions.MqttBadUsernameOrPasswordException;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions.MqttIdentifierRejectedException;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions.MqttServerUnavailableException;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions.MqttUnauthorizedException;

public class TransportException extends Exception
{
    protected boolean isRetryable = false;

    public TransportException()
    {
        super();
    }

    public TransportException(String message)
    {
        super(message);
    }

    public TransportException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TransportException(Throwable cause)
    {
        super(cause);
    }

    public boolean isRetryable()
    {
        return this.isRetryable;
    }

    public void setRetryable(boolean isRetryable)
    {
        this.isRetryable = isRetryable;
    }

    public IotHubClientException toIotHubClientException()
    {
        if (this instanceof MultiplexingDeviceUnauthorizedException)
        {
            // AMQP layer may throw a MultiplexingDeviceUnauthorizedException in some cases. In these
            // cases, we want to copy the registration exceptions map from this TransportException into this thrown exception
            // so that users don't need to look at the cause of the thrown exception to get this important information.
            MultiplexingClientRegistrationException newException =
                new MultiplexingClientRegistrationException("Failed to open the multiplexing connection", this);

            // Bring the exceptions map from the cause to the root level exception, so that users don't have to use
            // fields from inner exceptions.
            newException.setRegistrationExceptionsMap(((MultiplexingDeviceUnauthorizedException) this).getRegistrationExceptions());

            return newException;
        }
        else if (this instanceof UnauthorizedException
                || this instanceof AmqpUnauthorizedAccessException
                || this instanceof MqttUnauthorizedException
                || this instanceof MqttBadUsernameOrPasswordException
                || this instanceof MqttIdentifierRejectedException)
        {
            return new IotHubClientException(IotHubStatusCode.UNAUTHORIZED, "Failed to open the client due to an authentication error", this);
        }
        else if (this instanceof ThrottledException
                || this instanceof AmqpConnectionThrottledException
                || this instanceof AmqpConnectionForcedException)
        {
            return new IotHubClientException(IotHubStatusCode.THROTTLED, "Failed to open the client due to a throttling error", this);
        }
        else if (this instanceof ServerBusyException
            || this instanceof MqttServerUnavailableException)
        {
            return new IotHubClientException(IotHubStatusCode.SERVER_BUSY, "Failed to open the client due to the server being busy", this);
        }
        else if (this instanceof AmqpInternalErrorException
                || this instanceof InternalServerErrorException)
        {
            return new IotHubClientException(IotHubStatusCode.INTERNAL_SERVER_ERROR, "Failed to open the client due to the service encountering an internal server error", this);
        }
        else if (this instanceof AmqpLinkMessageSizeExceededException)
        {
            return new IotHubClientException(IotHubStatusCode.REQUEST_ENTITY_TOO_LARGE, "Failed to send the request because it exceeded the IoT Hub message size limit", this);
        }
        else if (this instanceof IotHubServiceException)
        {
            return new IotHubClientException(((IotHubServiceException) this).getStatusCode(), "", this);
        }
        else if (this instanceof ProtonIOException)
        {
            return new IotHubClientException(IotHubStatusCode.IO_ERROR, "Failed to open the client due to network issues. See inner exception for more details", this);
        }
        else if (this instanceof ProtocolException)
        {
            return new IotHubClientException(IotHubStatusCode.IO_ERROR, "Failed to open the client due to network issues. See inner exception for more details", this);
        }
        else
        {
            return new IotHubClientException(IotHubStatusCode.ERROR, "Failed to open the client. See inner exception for more details", this);
        }
    }
}
