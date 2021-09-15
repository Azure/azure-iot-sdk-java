// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.convention;

import com.microsoft.azure.sdk.iot.deps.convention.PayloadCollection;
import com.microsoft.azure.sdk.iot.deps.convention.PayloadConvention;
import com.microsoft.azure.sdk.iot.device.Message;
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
    PayloadCollection telemetry = new PayloadCollection();

    @Override
    public byte[] getBytes()
    {
        if (getTelemetry().getConvention() != null && getTelemetry().getConvention().getPayloadSerializer() != null)
        {
            return getTelemetry().getConvention().getObjectBytes(getTelemetry());
        }
        return super.getBytes();
    }
}
