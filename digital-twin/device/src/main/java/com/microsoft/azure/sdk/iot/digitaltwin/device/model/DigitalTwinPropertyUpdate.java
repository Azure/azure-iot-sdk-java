package com.microsoft.azure.sdk.iot.digitaltwin.device.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/** Structure filled in by the Digital Twin SDK on invoking an interface's property callback routine with information about the request. */
@Builder
@Getter
public class DigitalTwinPropertyUpdate {
    /** Name of the property being update */
    @NonNull
    private final String propertyName;
    private final Integer reportedVersion;
    /**
     * Version (from the service, NOT the C structure) of this property.
     * This version should be specified when updating this property.
     */
    private final Integer desiredVersion;
    /** Number of bytes in propertyDesired. */
    private final String propertyDesired;
}
