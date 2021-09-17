// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.DeviceTwin;

import lombok.Data;

@Data
public class DeviceMethodData
{
    private String responseMessage;
    private int status;

    public DeviceMethodData(int status, String responseMessage)
    {
        this.status = status;
        this.responseMessage = responseMessage;
    }
}
