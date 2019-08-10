package com.microsoft.azure.sdk.iot.digitaltwin.device.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/** Structure filled in by the device application when it is responding to a server initiated request to update a property. */
@Builder
@Getter
public class DigitalTwinPropertyResponse {
    private final static int DIGITAL_TWIN_PROPERTY_RESPONSE_VERSION_1 = 1;
    /**
     * The version <b>of the structure</b> that the SDK is passing.  This is <b>NOT</b> related to the server version.
     * Currently {@link #DIGITAL_TWIN_PROPERTY_RESPONSE_VERSION_1} but would be incremented if new fields are added.
     */
    @Builder.Default
    private int version = DIGITAL_TWIN_PROPERTY_RESPONSE_VERSION_1;
    /**
     * This is used for server to disambiguate calls for given property.
     * It should match {@link DigitalTwinPropertyUpdate#getDesiredVersion()} that this is responding to.
     */
    @NonNull
    private final Integer responseVersion;
    /** Which should map to appropriate HTTP status code - of property update.*/
    @NonNull
    private final Integer statusCode;
    /** Friendly description string of current status of update. */
    private String statusDescription;
}
