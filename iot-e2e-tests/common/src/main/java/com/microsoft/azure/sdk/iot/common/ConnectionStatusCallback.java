/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.common;

import com.microsoft.azure.sdk.iot.common.Success;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionState;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStateCallback;

/**
 * Generic connection status callback that changes the context's state only on the expected state provided
 */
public class ConnectionStatusCallback implements IotHubConnectionStateCallback
{
    private IotHubConnectionState expectedState;

    public ConnectionStatusCallback(IotHubConnectionState expectedState)
    {
        this.expectedState = expectedState;
    }

    public void execute(IotHubConnectionState state, Object context)
    {
        if (state == expectedState)
        {
            ((Success) context).setResult(true);
        }
    }
}
