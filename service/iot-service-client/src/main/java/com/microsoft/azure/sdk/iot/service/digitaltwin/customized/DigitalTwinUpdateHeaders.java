// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.customized;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines headers for UpdateDigitalTwin operation.
 */
@SuppressWarnings("UnusedReturnValue") // Public method
public class DigitalTwinUpdateHeaders {
    /**
     * Weak Etag of the modified resource.
     */
    @JsonProperty(value = "ETag")
    private String eTag;

    /**
     * URI of the digital twin.
     */
    @JsonProperty(value = "Location")
    private String location;

    /**
     * Get weak Etag of the modified resource.
     *
     * @return the eTag value
     */
    public String eTag() {
        return this.eTag;
    }

    /**
     * Set weak Etag of the modified resource.
     *
     * @param eTag the eTag value to set
     * @return the DigitalTwinUpdateHeaders object itself.
     */
    public DigitalTwinUpdateHeaders withETag(String eTag) {
        this.eTag = eTag;
        return this;
    }

    /**
     * Get uRI of the digital twin.
     *
     * @return the location value
     */
    public String location() {
        return this.location;
    }

    /**
     * Set uRI of the digital twin.
     *
     * @param location the location value to set
     * @return the DigitalTwinUpdateHeaders object itself.
     */
    public DigitalTwinUpdateHeaders withLocation(String location) {
        this.location = location;
        return this;
    }

}
