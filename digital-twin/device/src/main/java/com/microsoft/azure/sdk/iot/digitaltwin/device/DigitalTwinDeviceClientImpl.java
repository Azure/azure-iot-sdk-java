package com.microsoft.azure.sdk.iot.digitaltwin.device;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.digitaltwin.device.exceptions.DigitalTwinException;
import lombok.NonNull;

import java.util.List;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.RegistrationStatus.REGISTERED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.RegistrationStatus.REGISTRATERING;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.RegistrationStatus.UNREGISTERED;

public class DigitalTwinDeviceClientImpl implements DigitalTwinDeviceClient {
    private final DeviceClient deviceClient;
    private final TelemetryBroker telemetryBroker;
    private final TwinBroker twinBroker;
    private final CommandBroker commandBroker;
    private final Object lock;
    private RegistrationStatus registrationStatus;
    private String deviceCapabilityModelId;

    public DigitalTwinDeviceClientImpl(@NonNull DeviceClient deviceClient) {
        this.deviceClient = deviceClient;
        this.telemetryBroker = new TelemetryBroker(deviceClient);
        this.twinBroker = new TwinBroker(deviceClient);
        this.commandBroker = new CommandBroker(deviceClient);
        this.registrationStatus = UNREGISTERED;
        this.lock = new Object();
    }

    @Override
    public void registerInterfaces(@NonNull String deviceCapabilityModelId, @NonNull List<DigitalTwinInterfaceClient> digitalTwinInterfaceClients) throws DigitalTwinException {
        synchronized (lock) {
            if (registrationStatus == REGISTRATERING) {
                throw new IllegalStateException("Registration is under progress.");
            } else if (registrationStatus == REGISTERED) {
                throw new IllegalStateException("Already registered.");
            } else {
                registrationStatus = REGISTRATERING;
            }
        }

        try {
            // TODO
            this.deviceCapabilityModelId = deviceCapabilityModelId;
            for (DigitalTwinInterfaceClient digitalTwinInterfaceClient : digitalTwinInterfaceClients) {
                digitalTwinInterfaceClient.setDigitalTwinDeviceClient(this);
            }
            synchronized (lock) {
                registrationStatus = REGISTERED;
            }
        } catch (Exception e) {
            synchronized (lock) {
                registrationStatus = UNREGISTERED;
            }
        }
    }
}
