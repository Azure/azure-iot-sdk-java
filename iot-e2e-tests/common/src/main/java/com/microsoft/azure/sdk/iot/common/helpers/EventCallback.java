// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.common.helpers;

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
        if (context != null)
        {
            Success success = (Success) context;

            //null case is for testing that the callback is fired, but not caring what the status code was.
            // In some error injection scenarios, the status code reported cannot be predicted, but the callback
            // still must have been fired.
            if (this.expectedStatusCode == null || status.equals(expectedStatusCode))
            {
                success.setResult(true);
            }
            else
            {
                success.setResult(false);
            }

            success.callbackWasFired();

            success.setCallbackStatusCode(status);
        }
    }
}

