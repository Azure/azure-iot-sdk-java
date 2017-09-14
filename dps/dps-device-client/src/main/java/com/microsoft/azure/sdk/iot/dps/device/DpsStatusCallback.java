package com.microsoft.azure.sdk.iot.dps.device;

public interface DpsStatusCallback
{
    void run(DPSDeviceStatus status, String reason, Object context);
}
