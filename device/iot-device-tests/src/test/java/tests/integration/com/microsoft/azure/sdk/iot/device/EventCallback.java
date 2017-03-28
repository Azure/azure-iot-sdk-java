// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.integration.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;

public class EventCallback implements IotHubEventCallback
{
    public void execute(IotHubStatusCode status, Object context)
    {
        Success success = (Success) context;
        if (status.equals(IotHubStatusCode.OK_EMPTY))
        {
            success.setResult(true);
        }
        else
        {
            success.setResult(false);
        }
    }
}

