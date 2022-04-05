package com.microsoft.azure.sdk.iot.device.twin;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The response fields for a reported properties update request.
 */
@AllArgsConstructor
public class ReportedPropertiesUpdateResponse
{
    /**
     * The new version of the reported properties after a successful reported properties update. If the client updating
     * its reported properties is connected to Edgehub instead of IoT Hub, then this version won't change since Edgehub
     * does not apply this reported properties update immediately.
     */
    @Getter
    private final int version;
}
