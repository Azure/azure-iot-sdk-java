// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.DeviceTwin;

public interface DeviceMethodCallback
{
    DeviceMethodData call(String methodName, Object methodData, Object context);
}
