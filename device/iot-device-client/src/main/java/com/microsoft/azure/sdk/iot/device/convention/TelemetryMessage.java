package com.microsoft.azure.sdk.iot.device.convention;

import com.microsoft.azure.sdk.iot.deps.convention.PayloadConvention;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.PayloadCollection;
import lombok.Getter;
import lombok.Setter;

/**
 * The extension of Message that uses a {@link PayloadConvention} to generate the message payload.
 */
public class TelemetryMessage extends Message
{
    /**
     * Telemetry collection to be sent for this message
     */
    @Getter
    @Setter
    PayloadCollection telemetry;

    @Override
    /**
     * {@inheritDoc}
     */
    public byte[] getBytes()
    {
        if (telemetry.Convention != null && telemetry.Convention.getPayloadSerializer() != null)
        {
            return telemetry.Convention.getObjectBytes(telemetry);
        }
        return super.getBytes();
    }
}
