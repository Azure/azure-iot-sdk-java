package com.microsoft.azure.sdk.iot.digitaltwin.device.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import java.util.HashMap;
import java.util.Map;

@JsonRootName("modelInformation")
@Getter
public class InterfacesRegistration {
    @JsonProperty("capabilityModelId")
    private final String dcmId;
    @JsonProperty("interfaces")
    private final Map<String, String> interfaceInstances;
    @Builder
    private InterfacesRegistration(
            @NonNull
            String dcmId,
            @Singular
            Map<String, String> interfaceInstances
    ) {
        this.dcmId = dcmId;
        this.interfaceInstances = new HashMap<>(interfaceInstances);
    }
}
