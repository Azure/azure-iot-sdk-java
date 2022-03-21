package com.microsoft.azure.sdk.iot.device.twin;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The response fields for a reported properties update request.
 */
@AllArgsConstructor
public class ReportedPropertiesUpdateResponse
{
    /**
     * The status code returned by the service for this request. May indicate failure to send the reported properties.
     */
    @Getter
    private final IotHubStatusCode statusCode;

    /**
     * The new version of the reported properties after a successful reported properties update. If the client updating
     * its reported properties is connected to Edgehub instead of IoT Hub, then this version won't change since Edgehub
     * does not apply this reported properties update immediately.
     */
    @Getter
    private final int version;
}
