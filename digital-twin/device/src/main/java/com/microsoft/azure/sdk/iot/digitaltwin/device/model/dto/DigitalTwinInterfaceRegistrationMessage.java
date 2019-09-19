package com.microsoft.azure.sdk.iot.digitaltwin.device.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import java.util.Map;

@Getter
@AllArgsConstructor
public class DigitalTwinInterfaceRegistrationMessage {
    private final ModelInformation modelInformation;

    @Builder
    @Getter
    public static class ModelInformation {
        @JsonProperty("capabilityModelId")
        @NonNull
        private final String dcmId;
        @JsonProperty("interfaces")
        @Singular
        private final Map<String, String> interfaceInstances;
    }
}
