package com.microsoft.azure.sdk.iot.service.digitaltwin.models;

import lombok.Getter;
import lombok.Setter;

public class DigitalTwinCommandResponse {
    @Getter
    @Setter
    Integer status;

    @Getter
    @Setter
    String payload;
}