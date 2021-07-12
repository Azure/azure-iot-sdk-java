package com.microsoft.azure.sdk.iot.device;

import lombok.Getter;
import lombok.Setter;

public class TelemetryMessage extends Message
{
    @Getter
    @Setter
    public ClientPropertyCollection Telemetry;

    @Override
    public byte[] getBytes()
    {
        if (Telemetry.Convention != null && Telemetry.Convention.PayloadSerializer != null)
        {
            return Telemetry.Convention.GetObjectBytes(Telemetry);
        }
        return super.getBytes();
    }
}
