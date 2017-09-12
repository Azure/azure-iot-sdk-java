// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.common;

import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;

public class EventCallback implements IotHubEventCallback
{
    private IotHubStatusCode expectedStatusCode;
    public EventCallback(IotHubStatusCode expectedStatusCode)
    {
        this.expectedStatusCode = expectedStatusCode;
    }

    public void execute(IotHubStatusCode status, Object context)
    {
        Success success = (Success) context;
        if (status.equals(expectedStatusCode))
        {
            success.setResult(true);
        }
        else
        {
            success.setResult(false);
        }
    }
}

