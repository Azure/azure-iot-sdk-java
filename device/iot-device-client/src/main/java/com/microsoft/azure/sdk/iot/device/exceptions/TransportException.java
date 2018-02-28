/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

import static com.microsoft.azure.sdk.iot.device.exceptions.TransportException.IotHubService.NOT_APPLICABLE;

public class TransportException extends DeviceClientException
{
    protected boolean isRetryable = false;

    public enum IotHubService
    {
        TWIN,
        TELEMETRY,
        METHODS,
        FILE_UPLOAD,
        NOT_APPLICABLE
    };

    protected IotHubService iotHubService = NOT_APPLICABLE;

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

    public IotHubService getIotHubService()
    {
        return iotHubService;
    }

    public void setIotHubService(IotHubService iotHubService)
    {
        this.iotHubService = iotHubService;
    }
}
