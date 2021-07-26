package com.microsoft.azure.sdk.iot.device.convention;

import com.microsoft.azure.sdk.iot.device.Message;
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
        if (Telemetry.Convention != null && Telemetry.Convention.getPayloadSerializer() != null)
        {
            return Telemetry.Convention.GetObjectBytes(Telemetry);
        }
        return super.getBytes();
    }
}
