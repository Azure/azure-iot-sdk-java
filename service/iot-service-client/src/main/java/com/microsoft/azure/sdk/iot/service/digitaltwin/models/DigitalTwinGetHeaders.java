// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines headers for GetDigitalTwin operation.
 */
public class DigitalTwinGetHeaders {
    /**
     * Weak Etag.
     */
    @JsonProperty(value = "ETag")
    private String eTag;

    /**
     * Get weak Etag.
     *
     * @return the eTag value
     */
    public String eTag() {
        return this.eTag;
    }

    /**
     * Set weak Etag.
     *
     * @param eTag the eTag value to set
     * @return the DigitalTwinGetHeaders object itself.
     */
    public DigitalTwinGetHeaders withETag(String eTag) {
        this.eTag = eTag;
        return this;
    }

}
