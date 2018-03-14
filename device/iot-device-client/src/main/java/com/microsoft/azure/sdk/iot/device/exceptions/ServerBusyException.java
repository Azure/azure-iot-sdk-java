/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;

public class ServerBusyException extends IotHubServiceException
{
    public ServerBusyException()
    {
        super();
        this.isRetryable = true;
    }

    public ServerBusyException(String message)
    {
        super(message);
        this.isRetryable = true;
    }

    public ServerBusyException(String message, Throwable cause)
    {
        super(message, cause);
        this.isRetryable = true;
    }

    public ServerBusyException(Throwable cause)
    {
        super(cause);
        this.isRetryable = true;
    }

    @Override
    public IotHubStatusCode getStatusCode()
    {
        return IotHubStatusCode.SERVER_BUSY;
    }
}
