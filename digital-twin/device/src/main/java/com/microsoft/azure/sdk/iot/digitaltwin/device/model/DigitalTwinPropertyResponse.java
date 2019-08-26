package com.microsoft.azure.sdk.iot.digitaltwin.device.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/** Structure filled in by the device application when it is responding to a server initiated request to update a property to specify the status. */
@Builder
@Getter
public class DigitalTwinPropertyResponse {
    /**
     * This is used for server to disambiguate calls for given property.
     * It should match {@link DigitalTwinPropertyUpdate#getDesiredVersion()} that this is responding to.
     */
    @NonNull
    private final Integer statusVersion;
    /** Which should map to appropriate HTTP status code - of property update.*/
    @NonNull
    private final Integer statusCode;
    /** Friendly description string of current status of update. */
    private String statusDescription;
}
