// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

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
public class DigitalTwinComponentRegistrationMessage {
    private final ModelInformation modelInformation;

    @Builder
    @Getter
    public static class ModelInformation {
        @JsonProperty("capabilityModelId")
        @NonNull
        private final String dcmId;
        @JsonProperty("interfaces")
        @Singular
        private final Map<String, String> components;
    }
}
