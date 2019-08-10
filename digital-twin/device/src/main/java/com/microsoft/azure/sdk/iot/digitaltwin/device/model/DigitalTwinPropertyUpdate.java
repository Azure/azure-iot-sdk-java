package com.microsoft.azure.sdk.iot.digitaltwin.device.model;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;

/** Structure filled in by the Digital Twin SDK on invoking an interface's property callback routine with information about the request. */
@Builder
@Getter
public class DigitalTwinPropertyUpdate {
    private final static int DIGITAL_TWIN_PROPERTY_UPDATE_VERSION_1 = 1;
    /**
     * The version <b>of the structure</b> that the SDK is passing. This is <b>NOT</b> related to the server version.
     * Currently {@link #DIGITAL_TWIN_PROPERTY_UPDATE_VERSION_1} but would be incremented if new fields are added.
     */
    @Default
    private int version = 1;
    /** Name of the property being update */
    @NonNull
    private final String propertyName;
    private final Integer reportedVersion;
    /**
     * Value that the device application had previously reported for this property.
     * This value may be NULL if the application never reported a property.
     * It will also be NULL when an update arrives to the given property <b>after</b> the initial callback.
     */
    private final byte[] propertyReported;
    /**
     * Version (from the service, NOT the C structure) of this property.
     * This version should be specified when updating this property.
     */
    private final Integer desiredVersion;
    /** Number of bytes in propertyDesired. */
    private final byte[] propertyDesired;
}
