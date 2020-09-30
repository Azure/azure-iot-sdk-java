// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.serialization;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public final class DigitalTwinMetadata {
    /* The Id of the model that the digital twin or component is modeled by. This is present on a digital twin's root level metadata */
    @JsonProperty(value = "$model", required = true)
    private String modelId;

    /* Model-defined writable properties' request state. */
    @JsonIgnore
    private final Map<String, WritableProperty> writeableProperties = new HashMap<>();

    /**
     * Creates an instance of digital twin metadata.
     */
    public DigitalTwinMetadata() {
    }

    /**
     * Gets the Id of the model that the digital twin or component is modeled by.
     * @return The Id of the model that the digital twin or component is modeled by.
     */
    public String getModelId() {
        return modelId;
    }

    /**
     * Sets the Id of the model that the digital twin or component is modeled by.
     * @param modelId The Id of the model that the digital twin or component is modeled by.
     * @return The DigitalTwinMetadata object itself.
     */
    public DigitalTwinMetadata setModelId(String modelId) {
        this.modelId = modelId;
        return this;
    }

    /**
     * Gets the model-defined writable properties' request state.
     * For your convenience, the value of each map can be turned into an instance of WritableProperty.
     * @return The model-defined writable properties' request state.
     */
    @JsonAnyGetter
    public Map<String, WritableProperty> getWriteableProperties() {
        return writeableProperties;
    }

    /**
     * Sets the model-defined writable properties' request state.
     * @return The DigitalTwinMetadata object itself.
     */
    @JsonAnySetter
    DigitalTwinMetadata setWritableProperties(String key, WritableProperty value) {
        this.writeableProperties.put(key, value);
        return this;
    }
}
