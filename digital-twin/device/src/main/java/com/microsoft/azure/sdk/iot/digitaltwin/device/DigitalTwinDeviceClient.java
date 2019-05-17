package com.microsoft.azure.sdk.iot.digitaltwin.device;

import com.microsoft.azure.sdk.iot.digitaltwin.device.exceptions.DigitalTwinException;
import lombok.NonNull;

import java.util.List;

public interface DigitalTwinDeviceClient {
    void registerInterfaces(@NonNull String deviceCapabilityModelId, @NonNull List<DigitalTwinInterfaceClient> digitalTwinInterfaceClients) throws DigitalTwinException;
}
