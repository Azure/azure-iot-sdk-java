package com.microsoft.azure.sdk.iot.device.convention;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;

public interface DeviceCommandCallback
{
    DeviceMethodData call(String methodName, Object methodData, Object context);
}
