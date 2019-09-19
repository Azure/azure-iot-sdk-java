package com.microsoft.azure.sdk.iot.digitaltwin.device.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitalTwinCommand {
    private CommandRequest commandRequest;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommandRequest {
        private String requestId;
        private JsonRawValue value;
    }
}
