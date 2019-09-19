package com.microsoft.azure.sdk.iot.digitaltwin.device.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
/** Structure filled in by the device application when it is responding to a server initiated request to update a property. */
public class DigitalTwinReportProperty {
    /**
     * Name of the property to report.  This should match the model associated with this interface.
     */
    @NonNull
    private final String propertyName;
    /**
     * Value of the property to report.
     */
    private final String propertyValue;
    /**
     * Application response to a desired property update.
     */
    private final DigitalTwinPropertyResponse propertyResponse;
}
