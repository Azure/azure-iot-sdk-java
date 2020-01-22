/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;

public class HubOrDeviceIdNotFoundException extends IotHubServiceException
{
    public HubOrDeviceIdNotFoundException()
    {
        super();
        this.isRetryable = false;
    }

    public HubOrDeviceIdNotFoundException(String message)
    {
        super(message);
        this.isRetryable = false;
    }

    public HubOrDeviceIdNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
        this.isRetryable = false;
    }

    public HubOrDeviceIdNotFoundException(Throwable cause)
    {
        super(cause);
        this.isRetryable = false;
    }

    @Override
    public IotHubStatusCode getStatusCode()
    {
        return IotHubStatusCode.HUB_OR_DEVICE_ID_NOT_FOUND;
    }
}
