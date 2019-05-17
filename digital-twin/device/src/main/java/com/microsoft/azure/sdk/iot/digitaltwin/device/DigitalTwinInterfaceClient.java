package com.microsoft.azure.sdk.iot.digitaltwin.device;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor
public abstract class DigitalTwinInterfaceClient {
    private final String name;
    private final String digitalTwinInterface;
    @Setter(PACKAGE)
    private DigitalTwinDeviceClient digitalTwinDeviceClient;
}
