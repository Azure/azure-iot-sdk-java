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
    }

    public HubOrDeviceIdNotFoundException(String message)
    {
        super(message);
    }

    public HubOrDeviceIdNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public HubOrDeviceIdNotFoundException(Throwable cause)
    {
        super(cause);
    }

    @Override
    public IotHubStatusCode getStatusCode()
    {
        return IotHubStatusCode.HUB_OR_DEVICE_ID_NOT_FOUND;
    }
}
