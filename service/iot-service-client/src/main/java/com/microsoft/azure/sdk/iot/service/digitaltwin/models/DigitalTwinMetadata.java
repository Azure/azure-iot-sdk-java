package com.microsoft.azure.sdk.iot.service.digitaltwin.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public final class DigitalTwinMetadata {
    @JsonProperty(value = "$model", required = true)
    private String modelId;

    @JsonIgnore
    private final Map<String, Object> writeableProperties = new HashMap<>();

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
    private DigitalTwinMetadata setModelId(String modelId) {
        this.modelId = modelId;
        return this;
    }

    /**
     * Gets the model-defined writable properties' request state.
     * For your convenience, the value of each map can be turned into an instance of WritableProperty.
     * @return The model-defined writable properties' request state.
     */
    @JsonAnyGetter
    public Map<String, Object> getWriteableProperties() {
        return writeableProperties;
    }

    /**
     * Sets the model-defined writable properties' request state.
     * @return The DigitalTwinMetadata object itself.
     */
    @JsonAnySetter
    DigitalTwinMetadata setWritableProperties(String key, Object value) {
        this.writeableProperties.put(key, value);
        return this;
    }
}
