/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;

/**
 * Exception class that covers all exceptions communicated from the IoT Hub that are not due to connection issues in
 * the transport protocols. These exceptions map to standard status codes from the service (401 -> unauthorized,
 * 404 -> not found, etc.)
 */
public class IotHubServiceException extends TransportException
{
    public IotHubServiceException()
    {
        super();
    }

    public IotHubServiceException(String message)
    {
        super(message);
    }

    public IotHubServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public IotHubServiceException(Throwable cause)
    {
        super(cause);
    }

    public IotHubStatusCode getStatusCode()
    {
        return IotHubStatusCode.ERROR;
    }
}
