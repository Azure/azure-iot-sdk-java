package com.microsoft.azure.sdk.iot.service.digitaltwin.models;

import lombok.Getter;
import lombok.Setter;

public class DigitalTwinInvokeCommandRequestOptions {

    @Getter
    @Setter
    Integer connectTimeoutInSeconds;

    @Getter
    @Setter
    Integer responseTimeoutInSeconds;
}
