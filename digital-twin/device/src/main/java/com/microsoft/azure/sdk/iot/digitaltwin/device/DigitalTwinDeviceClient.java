package com.microsoft.azure.sdk.iot.digitaltwin.device;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinAsyncCommandUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.InterfacesRegistration;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.InterfacesRegistration.InterfacesRegistrationBuilder;
import com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer;
import com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.Serializer;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.List;

import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_TELEMETRY;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_INTERFACE_ALREADY_REGISTERED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_REGISTRATION_PENDING;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.RegistrationStatus.REGISTERED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.RegistrationStatus.REGISTERING;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.RegistrationStatus.UNREGISTERED;
import static lombok.AccessLevel.PACKAGE;

/**
 * Convenience layer handle to bind Digital Twin interface handles to the IoTHub transport.
 */
public class DigitalTwinDeviceClient {
    private final static String PROPERTY_DIGITAL_TWIN_INTERFACE_ID = "$.ifid";
    private final static String PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE = "$.ifname";
    private final static String DIGITAL_TWIN_MODEL_DEFINITION_INTERFACE_INSTANCE = "urn:azureiot:ModelDiscovery:ModelInformation";
    private final static String DIGITAL_TWIN_MODEL_DEFINITION_INTERFACE_ID = DIGITAL_TWIN_MODEL_DEFINITION_INTERFACE_INSTANCE + ":1";
    private final static String DIGITAL_TWIN_MODEL_DISCOVERY_MESSAGE_SCHEMA = "modelInformation";
    private final static String PROPERTY_MESSAGE_SCHEMA = "$.schema";

    private final DeviceClient deviceClient;
    private final Object lock;
    @Setter(PACKAGE)
    private Serializer serializer;
    @Getter(PACKAGE)
    private RegistrationStatus registrationStatus;

    /**
     *
     * @param deviceClient
     */
    public DigitalTwinDeviceClient(@NonNull DeviceClient deviceClient) {
        this.deviceClient = deviceClient;
        this.registrationStatus = UNREGISTERED;
        this.serializer = JsonSerializer.getInstance();
        this.lock = new Object();
    }

    /**
     * Registers the specified {@link AbstractDigitalTwinInterfaceClient} with the DigitalTwin Service. It registers specified dtInterfaces with the Digital Twin Service.
     * This registration occurs asynchronously. While registration is in progress, {@link AbstractDigitalTwinInterfaceClient}'s that are being registered nor will they be able to receive commands.
     * It must not be called multiple times.  If a given Digital Twin device needs to have its handles re-registered, it needs to create a new one.
     * @param deviceCapabilityModelId Device Capability Model Id
     * @param digitalTwinInterfaceClients An list of {@link AbstractDigitalTwinInterfaceClient}s to register with the service.
     * @param digitalTwinInterfaceRegistrationCallback User specified callback that will be invoked on registration completion or failure. Callers should not begin sending Digital Twin telemetry until this callback is invoked.
     * @param context User context that is provided to the callback.
     * @return if this async function is accepted or not
     */
    public DigitalTwinClientResult registerInterfacesAsync(
            final @NonNull String deviceCapabilityModelId,
            final @NonNull List<AbstractDigitalTwinInterfaceClient> digitalTwinInterfaceClients,
            final @NonNull DigitalTwinCallback digitalTwinInterfaceRegistrationCallback,
            final Object context) {

        InterfacesRegistrationBuilder interfacesRegistrationBuilder = InterfacesRegistration.builder()
                                                                                            .dcmId(deviceCapabilityModelId);
        for (AbstractDigitalTwinInterfaceClient digitalTwinInterfaceClient : digitalTwinInterfaceClients) {
            interfacesRegistrationBuilder.interfaceInstance(digitalTwinInterfaceClient.getDigitalTwinInterfaceInstanceName(), digitalTwinInterfaceClient.getDigitalTwinInterfaceId());
        }

        byte[] payload;
        try {
            payload = serializer.serialize(interfacesRegistrationBuilder.build());
        } catch (IOException e) {
            return DIGITALTWIN_CLIENT_ERROR;
        }

        synchronized (lock) {
            if (registrationStatus == REGISTERING) {
                return DIGITALTWIN_CLIENT_ERROR_REGISTRATION_PENDING;
            } else if (registrationStatus == REGISTERED) {
                return DIGITALTWIN_CLIENT_ERROR_INTERFACE_ALREADY_REGISTERED;
            } else {
                registrationStatus = REGISTERING;
            }
        }

        // TODO subscribe command and twin
        Message registerInterfacesMessage = new Message(payload);
        registerInterfacesMessage.setMessageType(DEVICE_TELEMETRY);
        registerInterfacesMessage.setProperty(PROPERTY_MESSAGE_SCHEMA, DIGITAL_TWIN_MODEL_DISCOVERY_MESSAGE_SCHEMA);
        registerInterfacesMessage.setProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE, DIGITAL_TWIN_MODEL_DEFINITION_INTERFACE_INSTANCE);
        registerInterfacesMessage.setProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_ID, DIGITAL_TWIN_MODEL_DEFINITION_INTERFACE_ID);

        IotHubEventCallback iotHubEventCallback = new IotHubEventCallback() {
            @Override
            public void execute(IotHubStatusCode iotHubStatusCode, Object o) {
                if (iotHubStatusCode == OK || iotHubStatusCode == OK_EMPTY) {
                    for (AbstractDigitalTwinInterfaceClient digitalTwinInterfaceClient : digitalTwinInterfaceClients) {
                        digitalTwinInterfaceClient.setDigitalTwinDeviceClient(DigitalTwinDeviceClient.this);
                    }
                    synchronized (lock) {
                        registrationStatus = REGISTERED;
                    }
                    digitalTwinInterfaceRegistrationCallback.onResult(DIGITALTWIN_CLIENT_OK, context);
                } else {
                    digitalTwinInterfaceRegistrationCallback.onResult(DIGITALTWIN_CLIENT_ERROR, context);
                    synchronized (lock) {
                        registrationStatus = UNREGISTERED;
                    }
                }
            }
        };

        deviceClient.sendEventAsync(
                registerInterfacesMessage,
                iotHubEventCallback,
                context
        );

        return  DIGITALTWIN_CLIENT_OK;
    }

    protected DigitalTwinClientResult setPropertyUpdatedCallback(
            @NonNull String digitalTwinInterfaceInstanceName,
            @NonNull String digitalTwinInterfaceId,
            @NonNull DigitalTwinPropertyUpdateCallback digitalTwinPropertyUpdateCallback
    ) {
        // TODO
        throw new NotImplementedException();
    }

    protected DigitalTwinClientResult setCommandsCallback(
            @NonNull String digitalTwinInterfaceInstanceName,
            @NonNull String digitalTwinInterfaceId,
            @NonNull DigitalTwinCommandExecuteCallback digitalTwinCommandExecuteCallback
    ){
        // TODO
        throw new NotImplementedException();
    }

    protected DigitalTwinClientResult sendTelemetryAsync(
            final @NonNull String digitalTwinInterfaceInstanceName,
            final @NonNull String digitalTwinInterfaceId,
            final @NonNull String telemetryName,
            final @NonNull byte[] payload,
            final @NonNull DigitalTwinCallback digitalTwinTelemetryConfirmationCallback,
            final Object context) {
        // TODO
        throw new NotImplementedException();
    }

    protected DigitalTwinClientResult reportPropertyAsync(
            final @NonNull String digitalTwinInterfaceInstanceName,
            final @NonNull String digitalTwinInterfaceId,
            final @NonNull String propertyName,
            final @NonNull byte[] propertyValue,
            final @NonNull DigitalTwinCallback digitalTwinReportedPropertyUpdatedCallback,
            final Object context) {
        // TODO
        throw new NotImplementedException();
    }

    protected DigitalTwinClientResult updateAsyncCommandStatusAsync(
            final @NonNull String digitalTwinInterfaceInstanceName,
            final @NonNull String digitalTwinInterfaceId,
            final @NonNull DigitalTwinAsyncCommandUpdate digitalTwinAsyncCommandUpdate,
            final Object context) {
        // TODO
        throw new NotImplementedException();
    }
}
