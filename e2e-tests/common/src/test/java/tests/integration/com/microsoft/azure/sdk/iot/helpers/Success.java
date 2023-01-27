// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;

public class Success
{
    public Boolean result = null;
    private IotHubStatusCode callbackStatusCode;
    private boolean callbackWasFired = false;

    public void setResult(Boolean result)
    {
        this.result = result;
    }

    public void setCallbackStatusCode(IotHubStatusCode callbackStatusCode)
    {
        this.callbackStatusCode = callbackStatusCode;
        this.callbackWasFired = true;
    }

    public Boolean getResult()
    {
        return this.result;
    }

    public boolean wasCallbackFired()
    {
        return this.callbackWasFired;
    }

    public IotHubStatusCode getCallbackStatusCode()
    {
        return this.callbackStatusCode;
    }

    public void callbackWasFired()
    {
        callbackWasFired = true;
    }
}